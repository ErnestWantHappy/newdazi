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

    private final ObjectMapper objectMapper = new ObjectMapper();

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
    public List<BizGuideSheetAnswer> getBySheetIdByClassCode(Long sheetId, String classCode)
    {
        return guideSheetAnswerMapper.selectBySheetIdByClassCode(sheetId, classCode);
    }

    @Override
    public Double getAvgScore(Long sheetId, String classCode)
    {
        return guideSheetAnswerMapper.selectAvgScore(sheetId, classCode);
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
                answer.setAnswerId(existing.getAnswerId());
                answer.setUpdateTime(now);
                if (answer.getStatus() == null)
                {
                    answer.setStatus(existing.getStatus() != null ? existing.getStatus() : "1");
                }
                guideSheetAnswerMapper.updateBizGuideSheetAnswer(answer);
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
    public int submitAnswer(BizGuideSheetAnswer answer)
    {
        answer.setStatus("2");
        answer.setSubmitTime(new Date());
        int result = saveAnswer(answer);

        // 自动评分
        try
        {
            BizGuideSheet sheet = guideSheetMapper.selectBizGuideSheetBySheetId(answer.getSheetId());
            if (sheet != null && sheet.getFormJson() != null)
            {
                GradingResult gradingResult = gradingService.grade(
                        sheet.getFormJson(), answer.getAnswerJson(),
                        answer.getStudentId(), answer.getSheetId());
                answer.setTotalScore(gradingResult.totalScore);
                answer.setGradingStatus(gradingResult.gradingStatus);
                answer.setGradingDetail(gradingResult.gradingDetail);
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
}
