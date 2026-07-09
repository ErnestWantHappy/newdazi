package com.ruoyi.business.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 导学单自动评分引擎
 * 优先从 formJson._scoringConfig 快照读取评分配置（label 作 key，稳定），
 * 回退到 formJson.widgetList[].scoring（id 作 key，可能因 VForm3 重分配而变化）
 *
 * @author ruoyi
 */
@Service
public class GuideSheetGradingService {

    private static final Logger log = LoggerFactory.getLogger(GuideSheetGradingService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AiGradingService aiGradingService;

    /** 默认评分类型常量 */
    private static final String GRADING_TYPE_EXACT   = "exact";
    private static final String GRADING_TYPE_CONTAINS = "contains";
    private static final String GRADING_TYPE_MANUAL   = "manual";
    private static final String GRADING_TYPE_AI       = "ai";

    /** 评分状态常量 */
    public static final String STATUS_AUTO    = "auto";    // 全部自动评分完成
    public static final String STATUS_PARTIAL = "partial"; // 部分自动 + 部分人工
    public static final String STATUS_MANUAL  = "manual";  // 全部需要人工
    public static final String STATUS_PENDING = "pending"; // 待评分

    /** AI 评分频率限制：同一学生同一导学单，5分钟内最多3次 */
    private static final int RATE_LIMIT_WINDOW_MS = 5 * 60 * 1000;
    private static final int RATE_LIMIT_MAX_CALLS = 3;
    /** 频率限制记录：key = "studentId:sheetId", value = 调用时间戳队列 */
    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<Long>> rateLimitMap = new ConcurrentHashMap<>();

    /** 评分配置对象（从 _scoringConfig 或 w.scoring 解析得到） */
    private static class ScoringEntry {
        int score;
        String answer;
        String type;
    }

    /**
     * 评分结果
     */
    public static class GradingResult {
        public int totalScore;
        public String gradingStatus;
        public String gradingDetail;
    }

    /**
     * 执行自动评分
     *
     * @param formJson   导学单表单结构 JSON（含 _scoringConfig 快照和 widgetList[].scoring）
     * @param answerJson 学生答案 JSON（key 为 widget 的 name 字段）
     * @param studentId  学生ID（用于频率限制）
     * @param sheetId    导学单ID（用于频率限制）
     * @return 评分结果
     */
    @SuppressWarnings("unchecked")
    public GradingResult grade(String formJson, String answerJson, Long studentId, Long sheetId) {
        GradingResult result = new GradingResult();
        int totalScore = 0;
        List<Map<String, Object>> detailList = new ArrayList<>();
        int autoCount = 0;
        int manualCount = 0;

        try {
            Map<String, Object> formObj = objectMapper.readValue(formJson,
                    new TypeReference<Map<String, Object>>() {});

            // 1. 优先读取 _scoringConfig 快照（label -> config），label 稳定不受 VForm3 id/guid 变化影响
            Map<String, Map<String, Object>> scoringSnapshot = (Map<String, Map<String, Object>>) formObj.get("_scoringConfig");
            boolean hasSnapshot = scoringSnapshot != null && !scoringSnapshot.isEmpty();
            log.info("评分调试: hasSnapshot={}, scoringKeys={}", hasSnapshot,
                    hasSnapshot ? scoringSnapshot.keySet() : "null");

            // 2. 解析学生答案
            Map<String, Object> answerObj = new LinkedHashMap<>();
            if (answerJson != null && !answerJson.isEmpty()) {
                answerObj = objectMapper.readValue(answerJson,
                        new TypeReference<Map<String, Object>>() {});
            }

            // 3. 递归展平所有 widget
            List<Map<String, Object>> widgetList = (List<Map<String, Object>>) formObj.get("widgetList");
            if (widgetList == null || widgetList.isEmpty()) {
                result.gradingStatus = STATUS_PENDING;
                result.gradingDetail = "[]";
                return result;
            }
            List<Map<String, Object>> flatWidgets = new ArrayList<>();
            flattenWidgets(widgetList, flatWidgets);

            // 4. 遍历每个字段进行评分
            for (Map<String, Object> widget : flatWidgets) {
                String fieldKey = (String) widget.getOrDefault("name", widget.get("id"));
                if (fieldKey == null) continue;

                String fieldLabel = getWidgetLabel(widget);
                String fieldType = (String) widget.get("type");

                // 获取评分配置：优先从 _scoringConfig 快照读取（label 匹配），回退到 w.scoring
                ScoringEntry scoringEntry = null;
                if (hasSnapshot) {
                    Map<String, Object> snapCfg = scoringSnapshot.get(fieldLabel);
                    log.info("评分调试: fieldKey={}, fieldLabel={}, type={}, inSnapshot={}",
                            fieldKey, fieldLabel, fieldType, snapCfg != null);
                    if (snapCfg != null) {
                        scoringEntry = new ScoringEntry();
                        scoringEntry.score = toInt(snapCfg.get("score"), 0);
                        scoringEntry.type = (String) snapCfg.getOrDefault("type", GRADING_TYPE_EXACT);
                        scoringEntry.answer = snapshotAnswerToString(snapCfg.get("answer"));
                    }
                }
                if (scoringEntry == null) {
                    // 回退：从 w.scoring 读取
                    Map<String, Object> scoring = (Map<String, Object>) widget.get("scoring");
                    if (scoring != null) {
                        scoringEntry = new ScoringEntry();
                        scoringEntry.score = toInt(scoring.get("score"), 0);
                        scoringEntry.type = (String) scoring.getOrDefault("type", GRADING_TYPE_EXACT);
                        scoringEntry.answer = scoringAnswerToString(scoring.get("answer"));
                    }
                }
                if (scoringEntry == null || scoringEntry.score == 0) {
                    continue; // 未配置评分，跳过
                }

                int maxScore = scoringEntry.score;
                String answerType = scoringEntry.type;
                String correctAnswer = scoringEntry.answer;

                // 人工批改 → 不计分，标记待处理
                if (GRADING_TYPE_MANUAL.equals(answerType)) {
                    manualCount++;
                    log.info("评分调试: 人工批改分支 fieldKey={}, fieldLabel={}, answerType={}", fieldKey, fieldLabel, answerType);
                    detailList.add(buildDetail(fieldKey, fieldLabel, 0, maxScore,
                            "manual", "待人工批改", correctAnswer, null));
                    continue;
                }

                // AI 评分
                if (GRADING_TYPE_AI.equals(answerType)) {
                    log.info("评分调试: AI评分分支 fieldKey={}, fieldLabel={}", fieldKey, fieldLabel);
                    if (correctAnswer == null || correctAnswer.isEmpty()) {
                        manualCount++;
                        detailList.add(buildDetail(fieldKey, fieldLabel, 0, maxScore,
                                "manual", "未设置参考答案", null, null));
                        continue;
                    }
                    // 获取学生答案
                    Object studentAnswer = answerObj.get(fieldKey);
                    if (studentAnswer == null || "".equals(String.valueOf(studentAnswer).trim())) {
                        autoCount++;
                        detailList.add(buildDetail(fieldKey, fieldLabel, 0, maxScore,
                                "auto", "未作答", correctAnswer, null));
                        continue;
                    }
                    // 调用 AI 评分
                    try {
                        // 频率限制检查
                        if (!checkRateLimit(studentId, sheetId)) {
                            manualCount++;
                            detailList.add(buildDetail(fieldKey, fieldLabel, 0, maxScore,
                                    "manual", "AI评分频率限制：5分钟内已调用" + RATE_LIMIT_MAX_CALLS + "次，请稍后再试", correctAnswer, null));
                            continue;
                        }
                        String aiApiKey = (String) formObj.get("_aiApiKey");
                        String aiProviderCode = (String) formObj.get("_aiProvider");
                        String aiModel = (String) formObj.get("_aiModel");
                        String aiCustomUrl = (String) formObj.get("_aiCustomUrl");
                        AiProviderConfig provider = AiProviderConfig.fromCode(aiProviderCode);
                        String model = provider.getModel(aiModel);
                        String prompt = buildAiPrompt(fieldLabel, correctAnswer,
                                String.valueOf(studentAnswer).trim(), maxScore);
                        AiGradingService.AiGradeResult aiResult = aiGradingService.grade(provider, aiApiKey, model, aiCustomUrl, prompt, maxScore);
                        recordRateLimit(studentId, sheetId);
                        totalScore += aiResult.score;
                        autoCount++;
                        detailList.add(buildDetail(fieldKey, fieldLabel, aiResult.score, maxScore,
                                "auto", "AI评分(" + provider.getCode() + "): " + aiResult.score + "/" + maxScore, correctAnswer, aiResult.comment));
                    } catch (Exception e) {
                        log.error("AI评分调用失败 fieldKey={}", fieldKey, e);
                        manualCount++;
                        detailList.add(buildDetail(fieldKey, fieldLabel, 0, maxScore,
                                "manual", "AI评分失败: " + (e.getMessage() != null ? e.getMessage() : "未知错误"), correctAnswer, null));
                    }
                    continue;
                }

                // 获取学生答案（精确/包含/正则匹配）
                Object studentAnswer = answerObj.get(fieldKey);
                if (studentAnswer == null || "".equals(String.valueOf(studentAnswer).trim())) {
                    autoCount++;
                    detailList.add(buildDetail(fieldKey, fieldLabel, 0, maxScore,
                            "auto", "未作答", correctAnswer, null));
                    continue;
                }

                // 评分比对
                double ratio = compareAnswer(studentAnswer, correctAnswer, answerType, fieldType);
                int fieldScore;
                String desc;

                if ("checkbox".equals(fieldType) && ratio < 1.0 && answerType.equals(GRADING_TYPE_EXACT)) {
                    int partialScore = gradePartialCheckbox(studentAnswer, correctAnswer, maxScore);
                    fieldScore = partialScore;
                    desc = partialScore > 0
                            ? "部分正确 (" + partialScore + "/" + maxScore + ")"
                            : "错误";
                } else {
                    fieldScore = (int) Math.round(ratio * maxScore);
                    if (ratio >= 1.0) {
                        desc = "正确";
                    } else if (ratio > 0) {
                        desc = "部分正确 (" + fieldScore + "/" + maxScore + ")";
                    } else {
                        desc = "错误";
                    }
                }

                autoCount++;
                totalScore += fieldScore;

                detailList.add(buildDetail(fieldKey, fieldLabel, fieldScore, maxScore,
                        "auto", desc, correctAnswer, null));
            }

        } catch (Exception e) {
            log.error("导学单自动评分异常", e);
            totalScore = deduplicateDetailList(detailList, totalScore);
            result.totalScore = totalScore;
            result.gradingStatus = STATUS_PARTIAL;
            result.gradingDetail = toJson(detailList);
            return result;
        }

        // 去重并重新计算总分（防止同一字段被重复评分）
        totalScore = deduplicateDetailList(detailList, totalScore);

        // 判断评分状态
        if (manualCount > 0 && autoCount > 0) {
            result.gradingStatus = STATUS_PARTIAL;
        } else if (manualCount > 0 && autoCount == 0) {
            result.gradingStatus = STATUS_MANUAL;
        } else if (autoCount > 0) {
            result.gradingStatus = STATUS_AUTO;
        } else {
            result.gradingStatus = STATUS_PENDING;
        }

        result.totalScore = totalScore;
        result.gradingDetail = toJson(detailList);
        return result;
    }

    /**
     * 提取 widget 的显示标签（与前端 designer.vue 中 getWidgetLabel 逻辑一致）
     */
    @SuppressWarnings("unchecked")
    private String getWidgetLabel(Map<String, Object> widget) {
        // VForm3 标签通常存储在 options.label
        String label = (String) widget.get("label");
        if (label != null && !label.isEmpty()) return label;

        Map<String, Object> options = (Map<String, Object>) widget.get("options");
        if (options != null) {
            label = (String) options.get("label");
            if (label != null && !label.isEmpty()) return label;
        }

        label = (String) widget.get("title");
        if (label != null && !label.isEmpty()) return label;

        // 回退到 id/name/guid
        Object id = widget.get("id");
        if (id == null) id = widget.get("name");
        if (id == null) id = widget.get("guid");
        return id != null ? String.valueOf(id) : "";
    }

    /**
     * 将 _scoringConfig 快照中的 answer 转为字符串（快照中可能是数组）
     */
    @SuppressWarnings("unchecked")
    private String snapshotAnswerToString(Object answer) {
        if (answer == null) return null;
        if (answer instanceof List) {
            return normalize(answer);
        }
        return String.valueOf(answer).trim();
    }

    /**
     * 将 w.scoring 中的 answer 转为字符串
     */
    @SuppressWarnings("unchecked")
    private String scoringAnswerToString(Object answer) {
        if (answer == null) return null;
        if (answer instanceof List) {
            return normalize(answer);
        }
        if (answer instanceof Boolean) {
            return String.valueOf(answer);
        }
        return String.valueOf(answer).trim();
    }

    /**
     * 通用递归展平 widget 树——不依赖特定容器属性名
     * 凡是带 id/name + type 的 Map 即为字段，所有数组和子 Map 一律深入
     */
    @SuppressWarnings("unchecked")
    private void flattenWidgets(List<Map<String, Object>> widgets,
                                List<Map<String, Object>> result) {
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<String> addedKeys = new HashSet<>();
        for (Map<String, Object> widget : widgets) {
            walkFlatten(widget, result, visited, addedKeys);
        }
    }

    /**
     * 通用递归遍历：深入任意 Map 和 List，提取所有可评分字段
     * 使用 addedKeys Set 按 fieldKey 去重（O(1)查重），避免同一字段被多次添加
     */
    @SuppressWarnings("unchecked")
    private void walkFlatten(Object value, List<Map<String, Object>> result,
                             Set<Object> visited, Set<String> addedKeys) {
        if (value == null || visited.contains(value)) return;
        visited.add(value);

        if (value instanceof List) {
            for (Object item : (List<?>) value) {
                walkFlatten(item, result, visited, addedKeys);
            }
        } else if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            // 有 id/name + type 且包含控件特征属性的才是真正的 widget（排除 options 等配置对象）
            Object id = map.get("id");
            if (id == null) id = map.get("name");
            Object type = map.get("type");
            if (id != null && type != null && isRealWidget(map)) {
                String fieldKey = String.valueOf(id);
                // 使用 Set 去重（O(1)），替代 O(n) 的 stream 扫描
                if (addedKeys.add(fieldKey)) {
                    result.add(map);
                }
            }
            }
            // 无论是否为字段，继续深入所有属性值
            for (Object v : map.values()) {
                if (v instanceof Map || v instanceof List) {
                    walkFlatten(v, result, visited, addedKeys);
                }
            }
        }
    }

    /**
     * 判断是否是真正的控件对象（而非 options 等配置对象）。
     * 控件对象包含 formItemFlag、key、widgetList、tabs 或 category 等特征属性。
     */
    private boolean isRealWidget(Map<String, Object> map) {
        return map.containsKey("formItemFlag")
                || map.containsKey("key")
                || map.containsKey("widgetList")
                || map.containsKey("tabs")
                || map.containsKey("category");
    }

    /**
     * 去除评分详情中的重复条目（按 fieldKey 去重），并重新计算总分。
     */
    @SuppressWarnings("unchecked")
    private int deduplicateDetailList(List<Map<String, Object>> detailList, int currentTotal) {
        if (detailList == null || detailList.isEmpty()) {
            return currentTotal;
        }
        java.util.Set<String> seenKeys = new java.util.LinkedHashSet<>();
        List<Map<String, Object>> deduplicated = new java.util.ArrayList<>();
        int newTotal = 0;
        int removed = 0;
        for (Map<String, Object> detail : detailList) {
            String key = (String) detail.get("fieldKey");
            if (key != null && !seenKeys.add(key)) {
                removed++;
                continue;
            }
            deduplicated.add(detail);
            newTotal += toInt(detail.get("score"), 0);
        }
        if (removed > 0) {
            log.warn("评分详情去重：移除 {} 条重复记录，总分从 {} 修正为 {}", removed, currentTotal, newTotal);
            detailList.clear();
            detailList.addAll(deduplicated);
        }
        return newTotal;
    }

    /**
     * 比对学生答案和正确答案，返回匹配比例（0.0 ~ 1.0）
     * contains：按关键词数量比例计分
     * exact：完全匹配为 1.0，否则 0.0
     */
    private double compareAnswer(Object studentAnswer, Object correctAnswer,
                                  String answerType, String fieldType) {
        String studentStr = normalize(studentAnswer);
        String correctStr = normalize(correctAnswer);

        switch (answerType) {
            case GRADING_TYPE_CONTAINS:
                // 关键词匹配：按匹配关键词数量比例计分
                String[] keywords = correctStr.split(",");
                int totalKw = 0;
                int matchedKw = 0;
                for (String kw : keywords) {
                    String trimmed = kw.trim();
                    if (!trimmed.isEmpty()) {
                        totalKw++;
                        if (studentStr.contains(trimmed)) {
                            matchedKw++;
                        }
                    }
                }
                return totalKw > 0 ? (double) matchedKw / totalKw : 0.0;
            case GRADING_TYPE_EXACT:
            default:
                return studentStr.equals(correctStr) ? 1.0 : 0.0;
        }
    }

    /**
     * 规范化答案值（去除首尾空格、处理大小写）
     */
    private String normalize(Object value) {
        if (value == null) return "";
        if (value instanceof List) {
            // checkbox 多选：排序后用逗号连接
            List<String> sorted = new ArrayList<>();
            for (Object item : (List<?>) value) {
                sorted.add(String.valueOf(item).trim());
            }
            Collections.sort(sorted);
            return String.join(",", sorted);
        }
        if (value instanceof Boolean) {
            return value.toString().toLowerCase();
        }
        return String.valueOf(value).trim();
    }

    /**
     * 多选框部分给分：计算学生答对的选项占比
     * 例如：正确答案 ABC，学生选了 AB → 2/3 得分
     */
    @SuppressWarnings("unchecked")
    private int gradePartialCheckbox(Object studentAnswer, Object correctAnswer, int maxScore) {
        List<String> studentList = new ArrayList<>();
        List<String> correctList = new ArrayList<>();

        // 解析学生答案
        if (studentAnswer instanceof List) {
            for (Object item : (List<?>) studentAnswer) {
                studentList.add(String.valueOf(item).trim());
            }
        } else {
            studentList.add(normalize(studentAnswer));
        }

        // 解析正确答案
        if (correctAnswer instanceof List) {
            for (Object item : (List<?>) correctAnswer) {
                correctList.add(String.valueOf(item).trim());
            }
        } else {
            // 修复：当正确答案为逗号分隔的字符串时，按逗号拆分为多个选项
            // 上游 snapshotAnswerToString 会将 List 通过 normalize() join 为 "2,3" 格式
            String normalized = normalize(correctAnswer);
            if (normalized.contains(",")) {
                for (String part : normalized.split(",")) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        correctList.add(trimmed);
                    }
                }
            } else if (!normalized.isEmpty()) {
                correctList.add(normalized);
            }
        }

        if (correctList.isEmpty() || studentList.isEmpty()) {
            return 0;
        }

        // 计算答对的选项数
        long correctCount = studentList.stream().filter(correctList::contains).count();
        // 按比例给分
        return (int) Math.round((double) correctCount / correctList.size() * maxScore);
    }

    private Map<String, Object> buildDetail(String fieldKey, String fieldTitle,
                                            int score, int maxScore,
                                            String matchType, String desc,
                                            String referenceAnswer, String aiComment) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("fieldKey", fieldKey);
        detail.put("fieldTitle", fieldTitle);
        detail.put("score", score);
        detail.put("maxScore", maxScore);
        detail.put("matchType", matchType);
        detail.put("desc", desc);
        if (referenceAnswer != null) {
            detail.put("referenceAnswer", referenceAnswer);
        }
        if (aiComment != null) {
            detail.put("aiComment", aiComment);
        }
        return detail;
    }

    private int toInt(Object value, int defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }

    /**
     * 检查 AI 评分频率限制
     * @return true=允许调用, false=频率超限
     */
    private boolean checkRateLimit(Long studentId, Long sheetId) {
        if (studentId == null || sheetId == null) return true;
        String key = studentId + ":" + sheetId;
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = rateLimitMap.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        // 清理过期记录
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > RATE_LIMIT_WINDOW_MS) {
                timestamps.pollFirst();
            }
            return timestamps.size() < RATE_LIMIT_MAX_CALLS;
        }
    }

    /**
     * 记录一次 AI 评分调用
     */
    private void recordRateLimit(Long studentId, Long sheetId) {
        if (studentId == null || sheetId == null) return;
        String key = studentId + ":" + sheetId;
        Deque<Long> timestamps = rateLimitMap.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        timestamps.addLast(System.currentTimeMillis());
    }

    /**
     * 构建 AI 评分提示词（关键词匹配，不严不松）
     */
    private String buildAiPrompt(String fieldLabel, String referenceAnswer,
                                 String studentAnswer, int maxScore) {
        return "你是一个评分助手。请根据参考答案的关键词对学生答案进行评分。\n\n" +
                "题目：" + fieldLabel + "\n" +
                "参考答案：" + referenceAnswer + "\n" +
                "学生答案：" + studentAnswer + "\n" +
                "满分：" + maxScore + "分\n\n" +
                "评分规则（基于关键词匹配，不严不松）：\n" +
                "1. 若学生答案涵盖了参考答案中的主要关键词，且意思正确，给满分 " + maxScore + " 分\n" +
                "2. 若学生答案仅匹配了部分关键词，但意思基本正确，给予 " + (int)(maxScore * 0.6) + "~" + (int)(maxScore * 0.8) + " 分\n" +
                "3. 若学生答案仅匹配了少量关键词，意思偏差较大，给予 " + (int)(maxScore * 0.2) + "~" + (int)(maxScore * 0.5) + " 分\n" +
                "4. 若学生答案完全不相关或没有任何关键词匹配，给0分\n\n" +
                "请直接返回一个JSON对象，格式为：{\"score\": 整数, \"comment\": \"简短评语\"}\n" +
                "只返回JSON，不要包含其他任何内容。";
    }
}