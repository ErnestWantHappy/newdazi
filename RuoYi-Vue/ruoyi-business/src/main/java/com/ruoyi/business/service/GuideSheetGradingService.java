package com.ruoyi.business.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

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

    /** 默认评分类型常量 */
    private static final String GRADING_TYPE_EXACT   = "exact";
    private static final String GRADING_TYPE_CONTAINS = "contains";
    private static final String GRADING_TYPE_REGEX    = "regex";
    private static final String GRADING_TYPE_MANUAL   = "manual";
    private static final String GRADING_TYPE_AI       = "ai";

    /** 评分状态常量 */
    public static final String STATUS_AUTO    = "auto";    // 全部自动评分完成
    public static final String STATUS_PARTIAL = "partial"; // 部分自动 + 部分人工
    public static final String STATUS_MANUAL  = "manual";  // 全部需要人工
    public static final String STATUS_PENDING = "pending"; // 待评分

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
     * @return 评分结果
     */
    @SuppressWarnings("unchecked")
    public GradingResult grade(String formJson, String answerJson) {
        GradingResult result = new GradingResult();
        int totalScore = 0;
        List<Map<String, Object>> detailList = new ArrayList<>();
        int autoCount = 0;
        int manualCount = 0;

        try {
            Map<String, Object> formObj = objectMapper.readValue(formJson,
                    new TypeReference<Map<String, Object>>() {});

            // 1. 优先读取 _scoringConfig 快照（label → config），label 稳定不受 VForm3 id/guid 变化影响
            Map<String, Map<String, Object>> scoringSnapshot = (Map<String, Map<String, Object>>) formObj.get("_scoringConfig");
            boolean hasSnapshot = scoringSnapshot != null && !scoringSnapshot.isEmpty();

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

                // 主观/手动/AI 类型 → 不计分，标记待处理
                if (GRADING_TYPE_MANUAL.equals(answerType) || GRADING_TYPE_AI.equals(answerType)
                        || correctAnswer == null) {
                    manualCount++;
                    detailList.add(buildDetail(fieldKey, fieldLabel, 0, maxScore,
                            "manual", "待人工批改"));
                    continue;
                }

                // 获取学生答案
                Object studentAnswer = answerObj.get(fieldKey);
                if (studentAnswer == null || "".equals(String.valueOf(studentAnswer).trim())) {
                    autoCount++;
                    detailList.add(buildDetail(fieldKey, fieldLabel, 0, maxScore,
                            "auto", "未作答"));
                    continue;
                }

                // 评分比对
                boolean matched = compareAnswer(studentAnswer, correctAnswer, answerType, fieldType);
                int fieldScore;
                String desc;

                if ("checkbox".equals(fieldType) && !matched && answerType.equals(GRADING_TYPE_EXACT)) {
                    int partialScore = gradePartialCheckbox(studentAnswer, correctAnswer, maxScore);
                    fieldScore = partialScore;
                    desc = partialScore > 0
                            ? "部分正确 (" + partialScore + "/" + maxScore + ")"
                            : "错误";
                } else {
                    fieldScore = matched ? maxScore : 0;
                    desc = matched ? "正确" : "错误";
                }

                autoCount++;
                totalScore += fieldScore;

                detailList.add(buildDetail(fieldKey, fieldLabel, fieldScore, maxScore,
                        "auto", desc));
            }

        } catch (Exception e) {
            log.error("导学单自动评分异常", e);
            result.totalScore = totalScore;
            result.gradingStatus = STATUS_PARTIAL;
            result.gradingDetail = toJson(detailList);
            return result;
        }

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
        for (Map<String, Object> widget : widgets) {
            walkFlatten(widget, result, visited);
        }
    }

    /**
     * 通用递归遍历：深入任意 Map 和 List，提取所有可评分字段
     */
    @SuppressWarnings("unchecked")
    private void walkFlatten(Object value, List<Map<String, Object>> result,
                             Set<Object> visited) {
        if (value == null || visited.contains(value)) return;
        visited.add(value);

        if (value instanceof List) {
            for (Object item : (List<?>) value) {
                walkFlatten(item, result, visited);
            }
        } else if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            // 有 id/name + type 的即为一个字段 widget
            Object id = map.get("id");
            if (id == null) id = map.get("name");
            Object type = map.get("type");
            if (id != null && type != null) {
                result.add(map);
            }
            // 无论是否为字段，继续深入所有属性值
            for (Object v : map.values()) {
                if (v instanceof Map || v instanceof List) {
                    walkFlatten(v, result, visited);
                }
            }
        }
    }

    /**
     * 比对学生答案和正确答案
     */
    private boolean compareAnswer(Object studentAnswer, Object correctAnswer,
                                  String answerType, String fieldType) {
        String studentStr = normalize(studentAnswer);
        String correctStr = normalize(correctAnswer);

        switch (answerType) {
            case GRADING_TYPE_CONTAINS:
                return studentStr.contains(correctStr) || correctStr.contains(studentStr);
            case GRADING_TYPE_REGEX:
                try {
                    return studentStr.matches(correctStr);
                } catch (Exception e) {
                    log.warn("正则匹配异常: pattern={}", correctStr);
                    return studentStr.equals(correctStr);
                }
            case GRADING_TYPE_EXACT:
            default:
                return studentStr.equals(correctStr);
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
            correctList.add(normalize(correctAnswer));
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
                                            String matchType, String desc) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("fieldKey", fieldKey);
        detail.put("fieldTitle", fieldTitle);
        detail.put("score", score);
        detail.put("maxScore", maxScore);
        detail.put("matchType", matchType);
        detail.put("desc", desc);
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
}