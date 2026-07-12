package com.ruoyi.business.service.impl;

import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.BizGuideSheetAnswer;
import com.ruoyi.business.domain.BizGuideSheetProgress;
import com.ruoyi.business.domain.BizGuideSheet;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.GuideSheetAnswerMapper;
import com.ruoyi.business.mapper.GuideSheetMapper;
import com.ruoyi.business.mapper.GuideSheetProgressMapper;
import com.ruoyi.business.service.IGuideSheetAnswerService;
import com.ruoyi.business.service.GuideSheetGradingService;
import com.ruoyi.business.service.GuideSheetGradingService.GradingResult;
import com.ruoyi.common.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuideSheetAnswerServiceImpl implements IGuideSheetAnswerService
{
    private static final Logger log = LoggerFactory.getLogger(GuideSheetAnswerServiceImpl.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // DevTools reload marker - v2
    private static final String RELOAD_MARKER = "merge-v2";

    @Autowired
    private GuideSheetAnswerMapper guideSheetAnswerMapper;

    @Autowired
    private GuideSheetProgressMapper guideSheetProgressMapper;

    @Autowired
    private GuideSheetMapper guideSheetMapper;

    @Autowired
    private BizStudentMapper bizStudentMapper;

    @Autowired
    private GuideSheetGradingService gradingService;

    @Override
    public BizGuideSheetAnswer getByStudentAndSheet(Long studentId, Long sheetId)
    {
        return guideSheetAnswerMapper.selectByStudentAndSheet(studentId, sheetId);
    }

    @Override
    public BizGuideSheetAnswer getByAnswerId(Long answerId)
    {
        return guideSheetAnswerMapper.selectBizGuideSheetAnswerByAnswerId(answerId);
    }

    @Override
    public List<BizGuideSheetAnswer> getBySheetId(Long sheetId)
    {
        return guideSheetAnswerMapper.selectBizGuideSheetAnswerList(new BizGuideSheetAnswer() {{
            setSheetId(sheetId);
        }});
    }

    @Override
    public List<BizGuideSheetAnswer> getBySheetIdByClassCode(Long sheetId, String entryYear, String classCode)
    {
        return guideSheetAnswerMapper.selectBySheetIdByClassCode(sheetId, entryYear, classCode);
    }

    @Override
    public Double getAvgScore(Long sheetId, String entryYear, String classCode)
    {
        return guideSheetAnswerMapper.selectAvgScore(sheetId, entryYear, classCode);
    }

    @Override
    @Transactional
    public BizGuideSheetAnswer saveManualGrades(Long sheetId, Long studentId,
                                                 List<Map<String, Object>> items)
    {
        if (items == null || items.isEmpty())
        {
            throw new ServiceException("请填写人工评分");
        }
        BizGuideSheetAnswer answer = guideSheetAnswerMapper.selectByStudentAndSheet(studentId, sheetId);
        if (answer == null || !"2".equals(answer.getStatus()))
        {
            throw new ServiceException("学生尚未提交导学单");
        }
        try
        {
            List<Map<String, Object>> details = objectMapper.readValue(answer.getGradingDetail(),
                    new TypeReference<List<Map<String, Object>>>() {});
            Map<String, Map<String, Object>> requested = new LinkedHashMap<>();
            for (Map<String, Object> item : items)
            {
                String fieldKey = item.get("fieldKey") == null ? "" : String.valueOf(item.get("fieldKey")).trim();
                if (fieldKey.isEmpty() || requested.put(fieldKey, item) != null)
                {
                    throw new ServiceException("人工评分字段无效或重复");
                }
            }

            Set<String> updated = new HashSet<>();
            for (Map<String, Object> detail : details)
            {
                String fieldKey = String.valueOf(detail.get("fieldKey"));
                Map<String, Object> requestedItem = requested.get(fieldKey);
                if (requestedItem == null || !"manual".equals(detail.get("matchType"))) continue;

                int maxScore = numberValue(detail.get("maxScore"), 0);
                int score = numberValue(requestedItem.get("score"), -1);
                if (score < 0 || score > maxScore)
                {
                    throw new ServiceException("人工评分必须在 0 到 " + maxScore + " 分之间");
                }
                String comment = requestedItem.get("comment") == null
                        ? "" : String.valueOf(requestedItem.get("comment")).trim();
                detail.put("score", score);
                detail.put("manualGraded", true);
                detail.put("desc", comment.isEmpty() ? "人工批改完成" : "人工批改：" + comment);
                if (!comment.isEmpty()) detail.put("manualComment", comment);
                updated.add(fieldKey);
            }
            if (updated.size() != requested.size())
            {
                throw new ServiceException("只能修改待人工批改的评分项");
            }

            answer.setGradingDetail(objectMapper.writeValueAsString(details));
            answer.setTotalScore(calculateTotalScoreFromDetail(answer.getGradingDetail()));
            answer.setGradingStatus(calculateGradingStatus(answer.getGradingDetail()));
            guideSheetAnswerMapper.updateBizGuideSheetAnswer(answer);
            return answer;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.error("保存导学单人工评分失败 sheetId={} studentId={}", sheetId, studentId, e);
            throw new ServiceException("保存人工评分失败");
        }
    }

    @Override
    @Transactional
    public int saveAnswer(BizGuideSheetAnswer answer)
    {
        try
        {
            Date now = new Date();
            BizGuideSheetAnswer existing = guideSheetAnswerMapper.selectByStudentAndSheet(
                    answer.getStudentId(), answer.getSheetId());
            if (existing != null)
            {
                boolean draftSave = !"2".equals(answer.getStatus());
                // 草稿接口不能覆盖最终答卷；显式重新提交会携带 status=2 并重新评分。
                if ("2".equals(existing.getStatus()) && draftSave)
                {
                    throw new ServiceException("导学单已提交，请使用重新提交功能修改答案");
                }
                answer.setAnswerId(existing.getAnswerId());
                answer.setUpdateTime(now);
                if (answer.getStatus() == null)
                {
                    answer.setStatus(existing.getStatus() != null ? existing.getStatus() : "1");
                }
                if (draftSave)
                {
                    // SQL 条件负责兜住并发提交，避免状态检查后最终答卷被迟到的草稿覆盖。
                    answer.getParams().put("onlyIfNotSubmitted", true);
                }
                int updated = guideSheetAnswerMapper.updateBizGuideSheetAnswer(answer);
                if (draftSave && updated == 0)
                {
                    throw new ServiceException("导学单已提交，请使用重新提交功能修改答案");
                }
            }
            else
            {
                if (answer.getStatus() == null)
                {
                    answer.setStatus("1");
                }
                answer.setCreateTime(now);
                answer.setUpdateTime(now);
                guideSheetAnswerMapper.insertBizGuideSheetAnswer(answer);
            }

            BizGuideSheetProgress progress = new BizGuideSheetProgress();
            progress.setSheetId(answer.getSheetId());
            progress.setStudentId(answer.getStudentId());

            // 从学生信息中获取班级编号
            BizStudent student = bizStudentMapper.selectBizStudentByStudentId(answer.getStudentId());
            String classCode = student != null && student.getClassCode() != null ? student.getClassCode() : "1";
            progress.setClassCode(classCode);

            progress.setCurrentPage(answer.getCurrentPage() != null ? answer.getCurrentPage() : 0);
            progress.setIsSubmitted("2".equals(answer.getStatus()) ? "Y" : "N");
            progress.setLastHeartbeat(now);

            // 计算当前页进度详情（仅在保存草稿/填写中时计算）
            if (!"2".equals(answer.getStatus())) {
                try {
                    BizGuideSheet sheet = guideSheetMapper.selectBizGuideSheetBySheetId(answer.getSheetId());
                    if (sheet != null && sheet.getFormJson() != null) {
                        String detail = calculateProgressDetail(sheet.getFormJson(), answer.getAnswerJson(), answer.getCurrentPage());
                        progress.setProgressDetail(detail);
                    }
                } catch (Exception e) {
                    log.warn("计算进度详情失败 sheetId={} studentId={}", answer.getSheetId(), answer.getStudentId(), e);
                }
            }

            guideSheetProgressMapper.insertOrUpdate(progress);

            return 1;
        }
        catch (DuplicateKeyException e)
        {
            log.warn("学生重复提交导学单 studentId={} sheetId={}", answer.getStudentId(), answer.getSheetId());
            throw e;
        }
    }

    @Override
    @Transactional
    public int submitAnswer(BizGuideSheetAnswer answer) {
        return submitAnswer(answer, null);
    }

    @Override
    @Transactional
    public int submitAnswer(BizGuideSheetAnswer answer, Integer tabIndex)
    {
        answer.setStatus("2");
        answer.setSubmitTime(new Date());

        // 分页批改：提交前先获取旧评分数据，用于后续合并
        String oldGradingDetail = null;
        if (tabIndex != null) {
            BizGuideSheetAnswer existing = guideSheetAnswerMapper.selectByStudentAndSheet(
                    answer.getStudentId(), answer.getSheetId());
            if (existing != null) {
                oldGradingDetail = existing.getGradingDetail();
            }
        }
        // 清除旧评分，后续由 gradePage 重新计算
        answer.setTotalScore(null);
        answer.setGradingDetail(null);
        answer.setGradingStatus(null);
        int result = saveAnswer(answer);

        // 自动评分
        try
        {
            BizGuideSheet sheet = guideSheetMapper.selectBizGuideSheetBySheetId(answer.getSheetId());
            if (sheet != null && sheet.getFormJson() != null)
            {
                GradingResult gradingResult = gradingService.gradePage(
                        sheet.getFormJson(), answer.getAnswerJson(),
                        answer.getStudentId(), answer.getSheetId(), tabIndex);

                if (tabIndex != null) {
                    // 分页批改：仅评分当前标签页，合并其他标签页旧数据
                    String mergedDetail = mergeGradingDetail(oldGradingDetail, gradingResult.gradingDetail, tabIndex);
                    int mergedTotalScore = calculateTotalScoreFromDetail(mergedDetail);
                    String mergedStatus = calculateGradingStatus(mergedDetail);
                    answer.setTotalScore(mergedTotalScore);
                    answer.setGradingStatus(mergedStatus);
                    answer.setGradingDetail(mergedDetail);
                } else {
                    answer.setTotalScore(gradingResult.totalScore);
                    answer.setGradingStatus(gradingResult.gradingStatus);
                    answer.setGradingDetail(gradingResult.gradingDetail);
                }
                guideSheetAnswerMapper.updateBizGuideSheetAnswer(answer);
            }
        }
        catch (Exception e)
        {
            log.error("自动评分失败 sheetId={} studentId={}", answer.getSheetId(), answer.getStudentId(), e);
            // 评分失败不影响提交
        }
        return result;
    }

    @Override
    @Transactional
    public int updateGrading(BizGuideSheetAnswer answer)
    {
        answer.setUpdateTime(new Date());
        return guideSheetAnswerMapper.updateBizGuideSheetAnswer(answer);
    }

    /**
     * 计算当前页的填写进度详情
     * 返回 JSON: {"filled": N, "total": M, "fields": {"field1": true, "field2": false, ...}}
     */
    @SuppressWarnings("unchecked")
    private String calculateProgressDetail(String formJson, String answerJson, Integer currentPage) {
        try {
            Map<String, Object> formObj = objectMapper.readValue(formJson, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> answerObj = new LinkedHashMap<>();
            if (answerJson != null && !answerJson.isEmpty()) {
                answerObj = objectMapper.readValue(answerJson, new TypeReference<Map<String, Object>>() {});
            }

            int pageIndex = (currentPage != null && currentPage > 0) ? currentPage - 1 : 0;

            // 递归查找 tab widget 并获取指定页的字段列表
            List<String> fieldNames = new ArrayList<>();
            List<Map<String, Object>> widgetList = (List<Map<String, Object>>) formObj.get("widgetList");
            if (widgetList != null) {
                extractPageFields(widgetList, pageIndex, fieldNames, new HashSet<>());
            }

            int total = fieldNames.size();
            int filled = 0;
            Map<String, Boolean> fields = new LinkedHashMap<>();
            for (String name : fieldNames) {
                Object val = answerObj.get(name);
                boolean isFilled = val != null && !String.valueOf(val).trim().isEmpty();
                fields.put(name, isFilled);
                if (isFilled) {
                    filled++;
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("filled", filled);
            result.put("total", total);
            result.put("fields", fields);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            log.warn("计算进度详情异常", e);
            return "{\"filled\":0,\"total\":0}";
        }
    }

    /**
     * 递归遍历 widgetList，提取指定 tab-pane 页内的所有字段名（name 属性）
     */
    @SuppressWarnings("unchecked")
    private void extractPageFields(List<Map<String, Object>> widgets, int targetPageIndex,
                                    List<String> fieldNames, Set<Object> visited) {
        if (widgets == null) return;
        for (Map<String, Object> widget : widgets) {
            if (widget == null || visited.contains(widget)) continue;
            visited.add(widget);

            String type = (String) widget.get("type");
            if ("tab".equals(type)) {
                // 找到 tab，获取对应 tab-pane
                List<Map<String, Object>> tabs = (List<Map<String, Object>>) widget.get("tabs");
                if (tabs != null && targetPageIndex >= 0 && targetPageIndex < tabs.size()) {
                    Map<String, Object> targetPane = tabs.get(targetPageIndex);
                    if (targetPane != null) {
                        List<Map<String, Object>> paneWidgets = (List<Map<String, Object>>) targetPane.get("widgetList");
                        if (paneWidgets != null) {
                            collectFieldNames(paneWidgets, fieldNames, new HashSet<>());
                        }
                    }
                }
                return; // 找到 tab 后不再继续深入
            }

            // 继续深入其他容器
            for (Object value : widget.values()) {
                if (value instanceof List) {
                    extractPageFields((List<Map<String, Object>>) value, targetPageIndex, fieldNames, visited);
                } else if (value instanceof Map) {
                    List<Map<String, Object>> list = new ArrayList<>();
                    list.add((Map<String, Object>) value);
                    extractPageFields(list, targetPageIndex, fieldNames, visited);
                }
            }
        }
    }

    /**
     * 递归收集 widgetList 中的所有字段名（name 属性）
     */
    @SuppressWarnings("unchecked")
    private void collectFieldNames(List<Map<String, Object>> widgets, List<String> fieldNames, Set<Object> visited) {
        if (widgets == null) return;
        for (Map<String, Object> widget : widgets) {
            if (widget == null || visited.contains(widget)) continue;
            visited.add(widget);

            String type = (String) widget.get("type");
            // 跳过纯容器类型，不收集为字段
            if ("tab".equals(type) || "tab-pane".equals(type) || "grid".equals(type) || "grid-col".equals(type)
                    || "card".equals(type) || "table".equals(type) || "table-cell".equals(type)) {
                // 继续深入容器
                for (Object value : widget.values()) {
                    if (value instanceof List) {
                        collectFieldNames((List<Map<String, Object>>) value, fieldNames, visited);
                    }
                }
                continue;
            }

            // 收集字段名（name 属性优先，回退到 id）
            String name = (String) widget.get("name");
            if (name == null) name = (String) widget.get("id");
            if (name != null && type != null) {
                fieldNames.add(name);
            }

            // 继续深入子属性（有些字段可能嵌套）
            for (Object value : widget.values()) {
                if (value instanceof List) {
                    collectFieldNames((List<Map<String, Object>>) value, fieldNames, visited);
                }
            }
        }
    }

    /**
     * 合并评分详情：移除旧数据中同 tabIndex 的项，追加新数据
     */
    @SuppressWarnings("unchecked")
    private String mergeGradingDetail(String existingJson, String newPageJson, int tabIndex) {
        List<Map<String, Object>> merged = new ArrayList<>();
        try {
            // 保留不属于当前 tabIndex 的旧数据
            if (existingJson != null && !existingJson.isEmpty() && !"null".equals(existingJson)) {
                List<Map<String, Object>> existingList = objectMapper.readValue(existingJson,
                        new TypeReference<List<Map<String, Object>>>() {});
                for (Map<String, Object> item : existingList) {
                    Object itemTabIdx = item.get("tabIndex");
                    if (itemTabIdx == null || !Integer.valueOf(itemTabIdx.toString()).equals(tabIndex)) {
                        merged.add(item);
                    }
                }
            }
            // 追加新评分数据
            if (newPageJson != null && !newPageJson.isEmpty()) {
                List<Map<String, Object>> newList = objectMapper.readValue(newPageJson,
                        new TypeReference<List<Map<String, Object>>>() {});
                merged.addAll(newList);
            }
            return objectMapper.writeValueAsString(merged);
        } catch (Exception e) {
            log.warn("合并评分详情失败，使用新数据", e);
            return newPageJson != null ? newPageJson : "[]";
        }
    }

    /**
     * 根据合并后的评分详情计算总分
     */
    @SuppressWarnings("unchecked")
    private int calculateTotalScoreFromDetail(String detailJson) {
        try {
            int total = 0;
            if (detailJson != null && !detailJson.isEmpty()) {
                List<Map<String, Object>> list = objectMapper.readValue(detailJson,
                        new TypeReference<List<Map<String, Object>>>() {});
                for (Map<String, Object> item : list) {
                    Object score = item.get("score");
                    if (score instanceof Number) {
                        total += ((Number) score).intValue();
                    }
                }
            }
            return total;
        } catch (Exception e) {
            log.warn("计算总分失败", e);
            return 0;
        }
    }

    /**
     * 根据评分详情判断评分状态
     */
    @SuppressWarnings("unchecked")
    private String calculateGradingStatus(String detailJson) {
        try {
            int autoCount = 0, manualCount = 0, completedManualCount = 0;
            if (detailJson != null && !detailJson.isEmpty()) {
                List<Map<String, Object>> list = objectMapper.readValue(detailJson,
                        new TypeReference<List<Map<String, Object>>>() {});
                for (Map<String, Object> item : list) {
                    String matchType = (String) item.get("matchType");
                    if ("manual".equals(matchType)) {
                        if (Boolean.TRUE.equals(item.get("manualGraded"))) completedManualCount++;
                        else manualCount++;
                    } else if ("auto".equals(matchType)) {
                        autoCount++;
                    }
                }
            }
            if (manualCount > 0 && (autoCount > 0 || completedManualCount > 0)) return "partial";
            if (manualCount > 0) return "manual";
            if (completedManualCount > 0) return "complete";
            if (autoCount > 0) return "auto";
            return "pending";
        } catch (Exception e) {
            log.warn("计算评分状态失败", e);
            return "partial";
        }
    }

    private int numberValue(Object value, int defaultValue)
    {
        if (value instanceof Number) return ((Number) value).intValue();
        try
        {
            return value == null ? defaultValue : Integer.parseInt(String.valueOf(value));
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }
}
