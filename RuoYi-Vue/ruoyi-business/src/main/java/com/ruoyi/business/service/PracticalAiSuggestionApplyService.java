package com.ruoyi.business.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.PracticalAiApplyAudit;
import com.ruoyi.business.domain.BizScoringDetail;
import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.domain.PracticalAiJob;
import com.ruoyi.business.domain.PracticalAiResult;
import com.ruoyi.business.domain.PracticalRubricSnapshot;
import com.ruoyi.business.domain.FlowchartSubmission;
import com.ruoyi.business.domain.vo.PracticalScoringItemVo;
import com.ruoyi.business.mapper.BizScoringDetailMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.PracticalAiGradingMapper;
import com.ruoyi.business.mapper.PracticalRubricSnapshotMapper;
import com.ruoyi.business.mapper.FlowchartMapper;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/** 批量采用 AI 建议；覆盖模式必须保留正式成绩修改前后的逐份审计。 */
@Service
public class PracticalAiSuggestionApplyService
{
    public static final String FILL_UNGRADED = "FILL_UNGRADED";
    public static final String OVERWRITE_ALL = "OVERWRITE_ALL";

    @Autowired private PracticalAiGradingMapper aiMapper;
    @Autowired private BizStudentAnswerMapper answerMapper;
    @Autowired private BizScoringDetailMapper detailMapper;
    @Autowired private PracticalRubricSnapshotMapper snapshotMapper;
    @Autowired private PracticalRubricSnapshotService rubricService;
    @Autowired private PracticalScoringPolicyService scoringPolicyService;
    @Autowired private PracticalGradingDeadlineService deadlineService;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private FlowchartMapper flowchartMapper;
    @Autowired private BizLessonQuestionMapper lessonQuestionMapper;

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> apply(Long jobId, Long teacherUserId, Long deptId, String applyMode)
    {
        if (!(FILL_UNGRADED.equals(applyMode) || OVERWRITE_ALL.equals(applyMode)))
            throw new ServiceException("批量采用模式无效");
        PracticalAiJob job = aiMapper.selectJob(jobId, teacherUserId);
        if (job == null || !Objects.equals(deptId, job.getDeptId()))
            throw new ServiceException("AI 批改任务不存在或无权访问");
        if (StringUtils.isBlank(job.getReferenceAnswerJson()))
            throw new ServiceException("旧任务缺少教师参考答案，不能批量采用");
        if (!("COMPLETED".equals(job.getJobStatus()) || "PARTIAL_FAILED".equals(job.getJobStatus())))
            throw new ServiceException("AI 建议尚未生成完成");

        int applied = 0, filledUngraded = 0, overwritten = 0;
        int skippedManual = 0, skippedVersion = 0, failed = 0;
        for (PracticalAiResult result : aiMapper.selectResultsByJob(jobId))
        {
            if (!"SUCCESS".equals(result.getResultStatus())) continue;
            BizStudentAnswer answer = answerMapper.selectByIdForUpdate(result.getAnswerId());
            boolean flowchart = "FLOWCHART".equals(job.getReferenceAnswerJson());
            if (answer == null || !Objects.equals(job.getLessonId(), answer.getLessonId())
                    || !Objects.equals(job.getQuestionId(), answer.getQuestionId())
                    || (!flowchart && !Objects.equals(result.getPracticalVersionId(), answer.getPracticalVersionId()))
                    || (flowchart && (!Objects.equals(result.getPracticalVersionId(), answer.getPracticalVersionId())
                    || !("FLOWCHART:" + result.getPracticalVersionId()).equals(answer.getStudentAnswer()))))
            {
                skippedVersion++;
                aiMapper.updateApplyStatus(result.getResultId(), "SKIPPED_VERSION_CHANGED", null, null);
                continue;
            }
            if (answer.getScore() != null && FILL_UNGRADED.equals(applyMode))
            {
                skippedManual++;
                aiMapper.updateApplyStatus(result.getResultId(), "SKIPPED_MANUAL_GRADE", null, null);
                continue;
            }
            try
            {
                if (flowchart) {
                    FlowchartSubmission submission = flowchartMapper.selectSubmissionById(result.getPracticalVersionId());
                    if (submission == null || !Objects.equals(submission.getAnswerId(), answer.getAnswerId()))
                        throw new ServiceException("流程图提交版本已变化");
                    int maxScore = resolveFlowchartScore(job.getLessonId(), job.getQuestionId());
                    if (result.getSuggestedScore() == null || result.getSuggestedScore() < 0
                            || result.getSuggestedScore() > maxScore)
                        throw new ServiceException("AI 流程图建议分无效");
                    Integer oldScore = answer.getScore();
                    List<BizScoringDetail> oldDetails = detailMapper.selectDetailsByAnswerId(answer.getAnswerId());
                    deadlineService.assertCanGrade(answer.getAnswerId());
                    answerMapper.updateScore(answer.getAnswerId(), result.getSuggestedScore().intValue());
                    detailMapper.deleteBizScoringDetailByAnswerId(answer.getAnswerId());
                    PracticalAiApplyAudit audit = buildAudit(job, result, teacherUserId, applyMode,
                            oldScore, result.getSuggestedScore().intValue(), oldDetails,
                            new ArrayList<BizScoringDetail>());
                    aiMapper.insertApplyAudit(audit);
                    String applyStatus = OVERWRITE_ALL.equals(applyMode) ? "APPLIED_OVERWRITE" : "APPLIED";
                    aiMapper.updateApplyStatus(result.getResultId(), applyStatus, teacherUserId, new Date());
                    applied++;
                    if (oldScore == null) filledUngraded++; else overwritten++;
                    continue;
                }
                PracticalRubricSnapshot snapshot = snapshotMapper.selectByVersionId(result.getPracticalVersionId());
                if (snapshot == null || !Objects.equals(result.getRubricSnapshotId(), snapshot.getSnapshotId()))
                    throw new ServiceException("评分标准快照已失效");
                List<BizScoringDetail> details = parseDetails(result);
                List<PracticalScoringItemVo> items = rubricService.buildScoringItems(snapshot);
                int finalScore = scoringPolicyService.resolveFinalScore(
                        result.getSuggestedScore(), snapshot.getQuestionScore(), items, details);
                deadlineService.assertCanGrade(answer.getAnswerId());
                Integer oldScore = answer.getScore();
                List<BizScoringDetail> oldDetails = detailMapper.selectDetailsByAnswerId(answer.getAnswerId());
                answerMapper.updateScore(answer.getAnswerId(), finalScore);
                detailMapper.deleteBizScoringDetailByAnswerId(answer.getAnswerId());
                for (BizScoringDetail detail : details)
                {
                    detail.setAnswerId(answer.getAnswerId());
                    detailMapper.insertBizScoringDetail(detail);
                }
                PracticalAiApplyAudit audit = buildAudit(job, result, teacherUserId, applyMode,
                        oldScore, finalScore, oldDetails, details);
                aiMapper.insertApplyAudit(audit);
                String applyStatus = OVERWRITE_ALL.equals(applyMode) ? "APPLIED_OVERWRITE" : "APPLIED";
                aiMapper.updateApplyStatus(result.getResultId(), applyStatus, teacherUserId, new Date());
                applied++;
                if (oldScore == null) filledUngraded++; else overwritten++;
            }
            catch (ServiceException e)
            {
                failed++;
                aiMapper.updateApplyStatus(result.getResultId(), "APPLY_FAILED", null, null);
            }
        }
        Map<String, Object> summary = new LinkedHashMap<String, Object>();
        summary.put("applyMode", applyMode); summary.put("appliedCount", applied);
        summary.put("filledUngradedCount", filledUngraded); summary.put("overwrittenCount", overwritten);
        summary.put("skippedManualCount", skippedManual);
        summary.put("skippedVersionCount", skippedVersion); summary.put("failedCount", failed);
        return summary;
    }

    private int resolveFlowchartScore(Long lessonId, Long questionId)
    {
        for (com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo item :
                lessonQuestionMapper.selectDetailsByLessonId(lessonId))
            if (questionId.equals(item.getQuestionId()) && item.getQuestionScore() != null)
                return item.getQuestionScore().intValue();
        throw new ServiceException("流程图题目分值无效");
    }

    /** 保留旧接口语义，避免已打开页面或旧客户端误触覆盖模式。 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> applyUngraded(Long jobId, Long teacherUserId, Long deptId)
    {
        return apply(jobId, teacherUserId, deptId, FILL_UNGRADED);
    }

    private PracticalAiApplyAudit buildAudit(PracticalAiJob job, PracticalAiResult result, Long teacherUserId,
                                             String applyMode, Integer oldScore, Integer newScore,
                                             List<BizScoringDetail> oldDetails,
                                             List<BizScoringDetail> newDetails)
    {
        PracticalAiApplyAudit audit = new PracticalAiApplyAudit();
        audit.setJobId(job.getJobId()); audit.setResultId(result.getResultId());
        audit.setAnswerId(result.getAnswerId()); audit.setPracticalVersionId(result.getPracticalVersionId());
        audit.setApplyMode(applyMode); audit.setOldScore(oldScore); audit.setNewScore(newScore);
        audit.setOldScoringDetailsJson(serializeDetails(oldDetails));
        audit.setNewScoringDetailsJson(serializeDetails(newDetails));
        audit.setOperatorUserId(teacherUserId);
        return audit;
    }

    private String serializeDetails(List<BizScoringDetail> details)
    {
        try
        {
            List<Map<String, Object>> values = new ArrayList<Map<String, Object>>();
            if (details != null)
            {
                for (BizScoringDetail detail : details)
                {
                    Map<String, Object> value = new LinkedHashMap<String, Object>();
                    value.put("itemId", detail.getItemId()); value.put("score", detail.getScore());
                    values.add(value);
                }
            }
            return objectMapper.writeValueAsString(values);
        }
        catch (Exception e)
        {
            throw new ServiceException("成绩审计快照生成失败");
        }
    }

    private List<BizScoringDetail> parseDetails(PracticalAiResult result)
    {
        try
        {
            if (StringUtils.isBlank(result.getScoringDetailsJson())) return new ArrayList<BizScoringDetail>();
            return objectMapper.readValue(result.getScoringDetailsJson(),
                    new TypeReference<List<BizScoringDetail>>() { });
        }
        catch (Exception e)
        {
            throw new ServiceException("AI 分项建议格式异常");
        }
    }
}
