package com.ruoyi.business.service.impl;

import java.util.*;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.BizGuideSheetAnswer;
import com.ruoyi.business.domain.BizGuideSheetProgress;
import com.ruoyi.business.domain.BizLessonGuideSheetBinding;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.GuideSheetAnswerMapper;
import com.ruoyi.business.mapper.GuideSheetBindingMapper;
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
    private static final int MAX_ANSWER_JSON_BYTES = 2 * 1024 * 1024;
    private static final Set<String> NON_ANSWER_WIDGET_TYPES = new HashSet<>(Arrays.asList(
            "tab", "tab-pane", "grid", "grid-col", "card", "table", "table-cell",
            "static-text", "html-text", "divider", "button"
    ));

    @Autowired
    private GuideSheetAnswerMapper guideSheetAnswerMapper;

    @Autowired
    private GuideSheetProgressMapper guideSheetProgressMapper;

    @Autowired
    private GuideSheetBindingMapper bindingMapper;

    @Autowired
    private BizStudentMapper bizStudentMapper;

    @Autowired
    private GuideSheetGradingService gradingService;

    @Override
    public BizGuideSheetAnswer getByStudentAndBinding(Long studentId, Long bindingId)
    {
        return guideSheetAnswerMapper.selectByStudentAndBinding(studentId, bindingId);
    }

    @Override
    public BizGuideSheetAnswer getByAnswerId(Long answerId)
    {
        return guideSheetAnswerMapper.selectBizGuideSheetAnswerByAnswerId(answerId);
    }

    @Override
    public List<BizGuideSheetAnswer> getByBindingAndClass(Long bindingId, Long deptId,
                                                          String entryYear, String classCode)
    {
        return guideSheetAnswerMapper.selectByBindingAndClass(bindingId, deptId, entryYear, classCode);
    }

    @Override
    public Double getAvgScore(Long bindingId, Long deptId, String entryYear, String classCode)
    {
        return guideSheetAnswerMapper.selectAvgScore(bindingId, deptId, entryYear, classCode);
    }

    @Override
    @Transactional
    public BizGuideSheetAnswer saveManualGrades(Long bindingId, Long studentId,
                                                 List<Map<String, Object>> items)
    {
        if (items == null || items.isEmpty())
        {
            throw new ServiceException("请填写人工评分");
        }
        BizGuideSheetAnswer answer = guideSheetAnswerMapper.selectByStudentAndBinding(studentId, bindingId);
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
            int totalScore = calculateTotalScoreFromDetail(answer.getGradingDetail());
            int autoScore = answer.getAutoScore() == null ? 0 : answer.getAutoScore();
            answer.setManualAdjustment(totalScore - autoScore);
            answer.setTotalScore(totalScore);
            answer.setGradingStatus(calculateGradingStatus(answer.getGradingDetail()));
            if (guideSheetAnswerMapper.updateGradingFields(answer) != 1)
            {
                throw new ServiceException("答卷已重新提交，请刷新后再保存评分");
            }
            return answer;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.error("保存导学单人工评分失败 bindingId={} studentId={}", bindingId, studentId, e);
            throw new ServiceException("保存人工评分失败");
        }
    }

    @Override
    @Transactional
    public int saveAnswer(BizGuideSheetAnswer answer)
    {
        validateAnswerPayload(answer);
        try
        {
            Date now = new Date();
            BizLessonGuideSheetBinding binding = bindingMapper.selectByBindingId(answer.getBindingId());
            if (binding == null)
            {
                throw new ServiceException("课程导学单绑定不存在");
            }
            answer.setLessonId(binding.getLessonId());
            answer.setSourceSheetId(binding.getSourceSheetId());
            BizGuideSheetAnswer existing = guideSheetAnswerMapper.selectByStudentAndBinding(
                    answer.getStudentId(), answer.getBindingId());
            if (existing != null)
            {
                if (!updateExistingAnswer(answer, existing, now))
                {
                    return 1;
                }
            }
            else
            {
                if (answer.getStatus() == null)
                {
                    answer.setStatus("1");
                }
                normalizeFirstRevision(answer);
                answer.setCreateTime(now);
                answer.setUpdateTime(now);
                try
                {
                    guideSheetAnswerMapper.insertBizGuideSheetAnswer(answer);
                }
                catch (DuplicateKeyException e)
                {
                    // 同一学生首次自动保存并发时，唯一键胜出的答卷就是后续更新目标。
                    BizGuideSheetAnswer concurrent = guideSheetAnswerMapper.selectByStudentAndBinding(
                            answer.getStudentId(), answer.getBindingId());
                    if (concurrent == null)
                    {
                        throw e;
                    }
                    if (!updateExistingAnswer(answer, concurrent, now))
                    {
                        return 1;
                    }
                }
            }

            BizGuideSheetProgress progress = new BizGuideSheetProgress();
            progress.setBindingId(answer.getBindingId());
            progress.setStudentId(answer.getStudentId());

            // 从学生信息中获取班级编号
            BizStudent student = bizStudentMapper.selectBizStudentByStudentId(answer.getStudentId());
            if (student == null || student.getDeptId() == null)
            {
                throw new ServiceException("学生班级归属信息不完整");
            }
            progress.setDeptId(student.getDeptId());
            progress.setEntryYear(student.getEntryYear());
            progress.setClassCode(student.getClassCode());

            progress.setCurrentPage(answer.getCurrentPage() != null ? answer.getCurrentPage() : 0);
            progress.setIsSubmitted("2".equals(answer.getStatus()) ? "Y" : "N");
            progress.setLastHeartbeat(now);

            // 计算当前页进度详情（仅在保存草稿/填写中时计算）
            if (!"2".equals(answer.getStatus())) {
                try {
                    if (binding.getSnapshotFormJson() != null) {
                        String detail = calculateProgressDetail(binding.getSnapshotFormJson(), answer.getAnswerJson(), answer.getCurrentPage());
                        progress.setProgressDetail(detail);
                    }
                } catch (Exception e) {
                    log.warn("计算进度详情失败 bindingId={} studentId={}", answer.getBindingId(), answer.getStudentId(), e);
                }
            }

            guideSheetProgressMapper.insertOrUpdate(progress);

            return 1;
        }
        catch (DuplicateKeyException e)
        {
            log.warn("学生重复提交导学单 studentId={} bindingId={}", answer.getStudentId(), answer.getBindingId());
            throw e;
        }
    }

    private void validateAnswerPayload(BizGuideSheetAnswer answer)
    {
        if (answer == null || answer.getAnswerJson() == null
                || answer.getAnswerJson().trim().isEmpty())
        {
            throw new ServiceException("答卷内容不能为空，请刷新页面后重试");
        }
        String answerJson = answer.getAnswerJson();
        if (answerJson.length() > MAX_ANSWER_JSON_BYTES
                || answerJson.getBytes(StandardCharsets.UTF_8).length > MAX_ANSWER_JSON_BYTES)
        {
            throw new ServiceException("答卷内容不能超过 2MB，文件请通过上传题提交");
        }
        try
        {
            JsonNode root = objectMapper.reader()
                    .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                    .readTree(answerJson);
            if (root == null || !root.isObject())
            {
                throw new ServiceException("答卷内容格式无效，请刷新页面后重试");
            }
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("答卷内容格式无效，请刷新页面后重试");
        }
    }

    private boolean updateExistingAnswer(BizGuideSheetAnswer answer,
                                         BizGuideSheetAnswer existing, Date now)
    {
        boolean draftSave = !"2".equals(answer.getStatus());
        if ("2".equals(existing.getStatus()) && draftSave)
        {
            throw new ServiceException("导学单已提交，请使用重新提交功能修改答案");
        }
        long existingRevision = existing.getDraftRevision() == null ? 0L : existing.getDraftRevision();
        if (draftSave)
        {
            long requestedRevision = answer.getDraftRevision() == null
                    ? existingRevision + 1L : answer.getDraftRevision();
            if (requestedRevision <= existingRevision)
            {
                copyPersistedIdentity(answer, existing);
                return false;
            }
            answer.setDraftRevision(requestedRevision);
            answer.getParams().put("onlyIfNotSubmitted", true);
            answer.getParams().put("onlyIfNewerDraft", true);
        }
        else
        {
            if (answer.getDraftRevision() == null)
            {
                throw new ServiceException("缺少答卷版本，请刷新后重新提交");
            }
            if (answer.getDraftRevision() <= existingRevision)
            {
                throw new ServiceException("答卷版本已变化，请刷新后重新提交");
            }
            answer.getParams().put("onlyIfNewerSubmission", true);
            // 新答案与旧评分必须在同一条 CAS 更新中切换，避免暴露短暂的不一致状态。
            answer.getParams().put("clearGrading", true);
        }

        answer.setAnswerId(existing.getAnswerId());
        answer.setUpdateTime(now);
        if (answer.getStatus() == null)
        {
            answer.setStatus(existing.getStatus() != null ? existing.getStatus() : "1");
        }
        int updated = guideSheetAnswerMapper.updateBizGuideSheetAnswer(answer);
        if (updated > 0)
        {
            return true;
        }

        if (!draftSave)
        {
            throw new ServiceException("答卷版本已变化，请刷新后重新提交");
        }

        BizGuideSheetAnswer latest = guideSheetAnswerMapper.selectByStudentAndBinding(
                answer.getStudentId(), answer.getBindingId());
        if (latest != null && !"2".equals(latest.getStatus())
                && latest.getDraftRevision() != null
                && latest.getDraftRevision() >= answer.getDraftRevision())
        {
            copyPersistedIdentity(answer, latest);
            return false;
        }
        throw new ServiceException("导学单已提交或草稿版本已变化，请刷新后重试");
    }

    private void normalizeFirstRevision(BizGuideSheetAnswer answer)
    {
        if (answer.getDraftRevision() == null || answer.getDraftRevision() <= 0L)
        {
            answer.setDraftRevision(1L);
        }
    }

    private void copyPersistedIdentity(BizGuideSheetAnswer target, BizGuideSheetAnswer persisted)
    {
        target.setAnswerId(persisted.getAnswerId());
        target.setDraftRevision(persisted.getDraftRevision() == null ? 0L : persisted.getDraftRevision());
        target.setStatus(persisted.getStatus());
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
            BizGuideSheetAnswer existing = guideSheetAnswerMapper.selectByStudentAndBinding(
                    answer.getStudentId(), answer.getBindingId());
            if (existing != null) {
                oldGradingDetail = existing.getGradingDetail();
            }
        }
        // 清除旧评分，后续由 gradePage 重新计算
        answer.setTotalScore(null);
        answer.setAutoScore(null);
        answer.setManualAdjustment(null);
        answer.setGradingDetail(null);
        answer.setGradingStatus(null);
        int result = saveAnswer(answer);

        // 自动评分
        try
        {
            BizLessonGuideSheetBinding binding = bindingMapper.selectByBindingId(answer.getBindingId());
            if (binding != null && binding.getSnapshotFormJson() != null)
            {
                GradingResult gradingResult = gradingService.gradePage(
                        binding.getSnapshotFormJson(), answer.getAnswerJson(),
                        answer.getStudentId(), answer.getBindingId(), tabIndex);

                if (tabIndex != null) {
                    // 分页批改：仅评分当前标签页，合并其他标签页旧数据
                    String mergedDetail = mergeGradingDetail(oldGradingDetail, gradingResult.gradingDetail, tabIndex);
                    int mergedTotalScore = calculateTotalScoreFromDetail(mergedDetail);
                    String mergedStatus = calculateGradingStatus(mergedDetail);
                    answer.setTotalScore(mergedTotalScore);
                    answer.setAutoScore(mergedTotalScore);
                    answer.setManualAdjustment(0);
                    answer.setGradingStatus(mergedStatus);
                    answer.setGradingDetail(mergedDetail);
                } else {
                    answer.setTotalScore(gradingResult.totalScore);
                    answer.setAutoScore(gradingResult.totalScore);
                    answer.setManualAdjustment(0);
                    answer.setGradingStatus(gradingResult.gradingStatus);
                    answer.setGradingDetail(gradingResult.gradingDetail);
                }
                if (guideSheetAnswerMapper.updateGradingFields(answer) != 1)
                {
                    log.info("评分结果未写入，答卷已被更新 answerId={} revision={}",
                            answer.getAnswerId(), answer.getDraftRevision());
                }
            }
        }
        catch (Exception e)
        {
            log.error("自动评分失败 bindingId={} studentId={}", answer.getBindingId(), answer.getStudentId(), e);
            // 评分失败不影响提交
        }
        return result;
    }

    @Override
    @Transactional
    public int updateGrading(BizGuideSheetAnswer answer)
    {
        if (answer == null || answer.getAnswerId() == null || answer.getDraftRevision() == null)
        {
            throw new ServiceException("答卷评分版本信息不完整");
        }
        answer.setUpdateTime(new Date());
        int updated = guideSheetAnswerMapper.updateGradingFields(answer);
        if (updated != 1)
        {
            throw new ServiceException("答卷已重新提交，请刷新后再评分");
        }
        return updated;
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
                boolean hasTab = extractPageFields(widgetList, pageIndex, fieldNames, new HashSet<>());
                if (!hasTab) {
                    collectFieldNames(widgetList, fieldNames, new HashSet<>());
                }
            }

            int total = fieldNames.size();
            int filled = 0;
            Map<String, Boolean> fields = new LinkedHashMap<>();
            for (String name : fieldNames) {
                Object val = answerObj.get(name);
                boolean isFilled = isAnswerFilled(val);
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
    private boolean extractPageFields(Object node, int targetPageIndex,
                                      List<String> fieldNames, Set<Object> visited) {
        if (node instanceof List) {
            for (Object child : (List<?>) node) {
                if (extractPageFields(child, targetPageIndex, fieldNames, visited)) return true;
            }
            return false;
        }
        if (!(node instanceof Map) || visited.contains(node)) return false;

        Map<String, Object> widget = (Map<String, Object>) node;
        visited.add(node);
        if ("tab".equals(widget.get("type"))) {
            Object rawTabs = widget.get("tabs");
            if (rawTabs instanceof List) {
                List<?> tabs = (List<?>) rawTabs;
                if (targetPageIndex >= 0 && targetPageIndex < tabs.size()) {
                    Object targetPane = tabs.get(targetPageIndex);
                    if (targetPane instanceof Map) {
                        collectFieldNames(((Map<String, Object>) targetPane).get("widgetList"),
                                fieldNames, new HashSet<>());
                    }
                }
            }
            return true;
        }
        for (Object value : widget.values()) {
            if (extractPageFields(value, targetPageIndex, fieldNames, visited)) return true;
        }
        return false;
    }

    /**
     * 递归收集 widgetList 中的所有答卷字段名。
     */
    @SuppressWarnings("unchecked")
    private void collectFieldNames(Object node, List<String> fieldNames, Set<Object> visited) {
        if (node instanceof List) {
            for (Object child : (List<?>) node) {
                collectFieldNames(child, fieldNames, visited);
            }
            return;
        }
        if (!(node instanceof Map) || visited.contains(node)) return;

        Map<String, Object> widget = (Map<String, Object>) node;
        visited.add(node);
        String type = widget.get("type") == null ? null : String.valueOf(widget.get("type"));
        if (!NON_ANSWER_WIDGET_TYPES.contains(type)) {
            String name = getWidgetFieldKey(widget);
            if (name != null && type != null && !fieldNames.contains(name)) {
                fieldNames.add(name);
            }
        }
        // 展示容器本身不计入进度，但其内部仍可能包含可填写组件。
        for (Object value : widget.values()) {
            collectFieldNames(value, fieldNames, visited);
        }
    }

    /**
     * VForm3 运行时以 options.name 作为答卷字段名，旧模板再回退到顶层字段。
     */
    @SuppressWarnings("unchecked")
    private String getWidgetFieldKey(Map<String, Object> widget) {
        Object fieldKey = null;
        Object rawOptions = widget.get("options");
        if (rawOptions instanceof Map) {
            fieldKey = ((Map<String, Object>) rawOptions).get("name");
        }
        if (isBlank(fieldKey)) fieldKey = widget.get("name");
        if (isBlank(fieldKey)) fieldKey = widget.get("id");
        return isBlank(fieldKey) ? null : String.valueOf(fieldKey).trim();
    }

    private boolean isBlank(Object value) {
        return value == null || String.valueOf(value).trim().isEmpty();
    }

    private boolean isAnswerFilled(Object value) {
        if (value == null) return false;
        if (value instanceof CharSequence) return value.toString().trim().length() > 0;
        if (value instanceof Collection) return !((Collection<?>) value).isEmpty();
        if (value instanceof Map) return !((Map<?, ?>) value).isEmpty();
        if (value.getClass().isArray()) return java.lang.reflect.Array.getLength(value) > 0;
        return true;
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
