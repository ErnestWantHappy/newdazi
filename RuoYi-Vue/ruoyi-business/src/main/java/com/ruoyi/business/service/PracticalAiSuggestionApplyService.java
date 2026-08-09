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
import com.ruoyi.business.domain.BizScoringDetail;
import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.domain.PracticalAiJob;
import com.ruoyi.business.domain.PracticalAiResult;
import com.ruoyi.business.domain.PracticalRubricSnapshot;
import com.ruoyi.business.domain.vo.PracticalScoringItemVo;
import com.ruoyi.business.mapper.BizScoringDetailMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.PracticalAiGradingMapper;
import com.ruoyi.business.mapper.PracticalRubricSnapshotMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/** 方案 A：批量采用只写入当前仍未人工评分、且版本未变化的 AI 建议。 */
@Service
public class PracticalAiSuggestionApplyService
{
    @Autowired private PracticalAiGradingMapper aiMapper;
    @Autowired private BizStudentAnswerMapper answerMapper;
    @Autowired private BizScoringDetailMapper detailMapper;
    @Autowired private PracticalRubricSnapshotMapper snapshotMapper;
    @Autowired private PracticalRubricSnapshotService rubricService;
    @Autowired private PracticalScoringPolicyService scoringPolicyService;
    @Autowired private PracticalGradingDeadlineService deadlineService;
    @Autowired private ObjectMapper objectMapper;

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> applyUngraded(Long jobId, Long teacherUserId, Long deptId)
    {
        PracticalAiJob job = aiMapper.selectJob(jobId, teacherUserId);
        if (job == null || !Objects.equals(deptId, job.getDeptId()))
            throw new ServiceException("AI 批改任务不存在或无权访问");
        if (StringUtils.isBlank(job.getReferenceAnswerJson()))
            throw new ServiceException("旧任务缺少教师参考答案，不能批量采用");
        if (!("COMPLETED".equals(job.getJobStatus()) || "PARTIAL_FAILED".equals(job.getJobStatus())))
            throw new ServiceException("AI 建议尚未生成完成");

        int applied = 0, skippedManual = 0, skippedVersion = 0, failed = 0;
        for (PracticalAiResult result : aiMapper.selectResultsByJob(jobId))
        {
            if (!"SUCCESS".equals(result.getResultStatus())) continue;
            if ("APPLIED".equals(result.getApplyStatus())) { skippedManual++; continue; }
            BizStudentAnswer answer = answerMapper.selectByIdForUpdate(result.getAnswerId());
            if (answer == null || !Objects.equals(job.getLessonId(), answer.getLessonId())
                    || !Objects.equals(job.getQuestionId(), answer.getQuestionId())
                    || !Objects.equals(result.getPracticalVersionId(), answer.getPracticalVersionId()))
            {
                skippedVersion++;
                aiMapper.updateApplyStatus(result.getResultId(), "SKIPPED_VERSION_CHANGED", null, null);
                continue;
            }
            if (answer.getScore() != null)
            {
                skippedManual++;
                aiMapper.updateApplyStatus(result.getResultId(), "SKIPPED_MANUAL_GRADE", null, null);
                continue;
            }
            try
            {
                PracticalRubricSnapshot snapshot = snapshotMapper.selectByVersionId(result.getPracticalVersionId());
                if (snapshot == null || !Objects.equals(result.getRubricSnapshotId(), snapshot.getSnapshotId()))
                    throw new ServiceException("评分标准快照已失效");
                List<BizScoringDetail> details = parseDetails(result);
                List<PracticalScoringItemVo> items = rubricService.buildScoringItems(snapshot);
                int finalScore = scoringPolicyService.resolveFinalScore(
                        result.getSuggestedScore(), snapshot.getQuestionScore(), items, details);
                deadlineService.assertCanGrade(answer.getAnswerId());
                answerMapper.updateScore(answer.getAnswerId(), finalScore);
                detailMapper.deleteBizScoringDetailByAnswerId(answer.getAnswerId());
                for (BizScoringDetail detail : details)
                {
                    detail.setAnswerId(answer.getAnswerId());
                    detailMapper.insertBizScoringDetail(detail);
                }
                aiMapper.updateApplyStatus(result.getResultId(), "APPLIED", teacherUserId, new Date());
                applied++;
            }
            catch (ServiceException e)
            {
                failed++;
                aiMapper.updateApplyStatus(result.getResultId(), "APPLY_FAILED", null, null);
            }
        }
        Map<String, Object> summary = new LinkedHashMap<String, Object>();
        summary.put("appliedCount", applied); summary.put("skippedManualCount", skippedManual);
        summary.put("skippedVersionCount", skippedVersion); summary.put("failedCount", failed);
        return summary;
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
