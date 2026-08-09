package com.ruoyi.business.service;

import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.ruoyi.business.domain.PracticalAiJob;
import com.ruoyi.business.domain.PracticalAiResult;
import com.ruoyi.business.domain.TeacherAiConfig;
import com.ruoyi.business.domain.PracticalQuestionMaterial;
import com.ruoyi.business.domain.TeacherPracticalReferenceAnswer;
import com.ruoyi.business.domain.vo.PracticalSubmissionVo;
import com.ruoyi.business.mapper.PracticalArtifactMapper;
import com.ruoyi.business.mapper.PracticalAiGradingMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PracticalAiJobService
{
    public static final String PROMPT_VERSION = "operation-rubric-v2";
    @Autowired private PracticalAiGradingMapper mapper;
    @Autowired private TeacherAiConfigService configService;
    @Autowired private PracticalAiJobWorker worker;
    @Autowired private PracticalAiReferenceAnswerService referenceAnswerService;
    @Autowired private PracticalArtifactMapper artifactMapper;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PracticalFilePolicyService filePolicyService;

    @Transactional(rollbackFor = Exception.class)
    public PracticalAiJob create(Long teacherUserId, Long deptId, Long lessonId, Long questionId,
                                 String entryYear, String classCode, String scopeMode,
                                 List<PracticalSubmissionVo> submissions)
    {
        // 锁定教师配置行，使同一教师双击/并发创建任务时只能生成一个活动任务。
        TeacherAiConfig config = configService.statusForUpdate(teacherUserId);
        configService.apiKey(config); // 创建任务前先验证主密钥和教师密文均可用。
        String normalizedScope = normalizeScopeMode(scopeMode);
        PracticalAiJob running = mapper.selectActiveJob(teacherUserId, lessonId, questionId, entryYear, classCode);
        if (running != null) return running;

        List<PracticalQuestionMaterial> referenceAnswers = effectiveReferenceAnswers(
                teacherUserId, deptId, lessonId, questionId);
        if (referenceAnswers.isEmpty())
            throw new ServiceException("AI 批改前必须上传教师参考答案");

        int eligible = 0;
        int skipped = 0;
        for (PracticalSubmissionVo submission : submissions)
        {
            if (eligible(submission) && selectedByScope(submission, normalizedScope)) eligible++;
            else if (Boolean.TRUE.equals(submission.getSubmitted())) skipped++;
        }
        if (eligible == 0) throw new ServiceException("本班暂无已完成页图转换的操作题作品");

        PracticalAiJob job = new PracticalAiJob();
        job.setTeacherUserId(teacherUserId); job.setDeptId(deptId); job.setLessonId(lessonId);
        job.setQuestionId(questionId); job.setEntryYear(entryYear); job.setClassCode(classCode);
        job.setProviderCode(config.getProviderCode()); job.setModelName(config.getModelName());
        job.setPromptVersion(PROMPT_VERSION); job.setScopeMode(normalizedScope);
        job.setReferenceAnswerJson(writeJson(referenceAnswers));
        job.setStarterMaterialsJson(writeJson(materials(questionId, "STARTER")));
        job.setJobStatus("PENDING");
        job.setTotalCount(eligible); job.setSkippedCount(skipped);
        mapper.insertJob(job);
        for (PracticalSubmissionVo submission : submissions)
        {
            if (!eligible(submission) || !selectedByScope(submission, normalizedScope)) continue;
            PracticalAiResult result = new PracticalAiResult();
            result.setJobId(job.getJobId()); result.setAnswerId(submission.getAnswerId());
            result.setPracticalVersionId(submission.getPracticalVersionId());
            result.setRubricSnapshotId(submission.getRubricSnapshotId()); result.setResultStatus("PENDING");
            mapper.insertResult(result);
        }
        afterCommit(job.getJobId());
        return job;
    }

    public Map<String, Object> detail(Long jobId, Long teacherUserId)
    {
        PracticalAiJob job = requireOwned(jobId, teacherUserId);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("job", job); result.put("results", mapper.selectResultsByJob(jobId));
        result.put("batchAdoptAllowed", StringUtils.isNotBlank(job.getReferenceAnswerJson()));
        return result;
    }

    public Map<String, Object> latest(Long teacherUserId, Long lessonId, Long questionId,
                                      String entryYear, String classCode)
    {
        PracticalAiJob job = mapper.selectLatestJob(teacherUserId, lessonId, questionId, entryYear, classCode);
        return job == null ? null : detail(job.getJobId(), teacherUserId);
    }

    public Map<String, Object> preflight(Long teacherUserId, Long deptId, Long lessonId, Long questionId,
                                         List<PracticalSubmissionVo> submissions)
    {
        int submitted = 0, graded = 0, ready = 0, readyUngraded = 0;
        for (PracticalSubmissionVo submission : submissions)
        {
            if (!Boolean.TRUE.equals(submission.getSubmitted())) continue;
            submitted++;
            if (submission.getScore() != null) graded++;
            if (eligible(submission))
            {
                ready++;
                if (submission.getScore() == null) readyUngraded++;
            }
        }
        List<PracticalQuestionMaterial> references = effectiveReferenceAnswers(
                teacherUserId, deptId, lessonId, questionId);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("submittedCount", submitted); result.put("gradedCount", graded);
        result.put("ungradedCount", submitted - graded); result.put("readyCount", ready);
        result.put("readyUngradedCount", readyUngraded);
        result.put("referenceReady", !references.isEmpty());
        result.put("referenceFileName", references.isEmpty() ? null : references.get(0).getOriginalFileName());
        result.put("starterCount", materials(questionId, "STARTER").size());
        return result;
    }

    public void pause(Long jobId, Long teacherUserId)
    {
        PracticalAiJob job = requireOwned(jobId, teacherUserId);
        if (!"RUNNING".equals(job.getJobStatus()) && !"PENDING".equals(job.getJobStatus()))
            throw new ServiceException("当前任务不能暂停");
        mapper.updateJobStatus(jobId, "PAUSED", null, null, null);
    }

    public void resume(Long jobId, Long teacherUserId)
    {
        PracticalAiJob job = requireOwned(jobId, teacherUserId);
        if (!"PAUSED".equals(job.getJobStatus())) throw new ServiceException("当前任务不是暂停状态");
        if (StringUtils.isBlank(job.getReferenceAnswerJson()))
            throw new ServiceException("旧任务缺少教师参考答案，不能继续；请取消后重新发起");
        mapper.updateJobStatus(jobId, "PENDING", null, null, null);
        worker.run(jobId);
    }

    public void cancel(Long jobId, Long teacherUserId)
    {
        PracticalAiJob job = requireOwned(jobId, teacherUserId);
        if (!"RUNNING".equals(job.getJobStatus()) && !"PENDING".equals(job.getJobStatus())
                && !"PAUSED".equals(job.getJobStatus())) throw new ServiceException("当前任务不能取消");
        if ("PAUSED".equals(job.getJobStatus()))
        {
            Date now = new Date();
            mapper.updatePendingResultsStatus(jobId, "CANCELLED", "教师已取消", now);
            mapper.updateJobCounts(jobId);
            mapper.updateJobStatus(jobId, "CANCELLED", null, now, null);
            return;
        }
        mapper.updateJobStatus(jobId, "CANCEL_REQUESTED", null, null, null);
    }

    public void retryFailed(Long jobId, Long teacherUserId)
    {
        PracticalAiJob job = requireOwned(jobId, teacherUserId);
        if ("RUNNING".equals(job.getJobStatus()) || "PENDING".equals(job.getJobStatus()))
            throw new ServiceException("任务仍在执行中");
        if (StringUtils.isBlank(job.getReferenceAnswerJson()))
            throw new ServiceException("旧任务缺少教师参考答案，不能重试；请重新发起");
        int count = mapper.resetFailedResults(jobId);
        if (count == 0) throw new ServiceException("没有可重试的失败作品");
        mapper.updateJobStatus(jobId, "PENDING", null, null, null);
        worker.run(jobId);
    }

    private PracticalAiJob requireOwned(Long jobId, Long teacherUserId)
    {
        PracticalAiJob job = mapper.selectJob(jobId, teacherUserId);
        if (job == null) throw new ServiceException("AI 批改任务不存在或无权访问");
        return job;
    }

    private boolean eligible(PracticalSubmissionVo submission)
    {
        if (submission == null || !Boolean.TRUE.equals(submission.getSubmitted())
                || submission.getAnswerId() == null || submission.getPracticalVersionId() == null
                || submission.getRubricSnapshotId() == null || submission.getAttachments() == null
                || submission.getAttachments().isEmpty()) return false;
        return submission.getAttachments().stream().allMatch(attachment ->
                "success".equalsIgnoreCase(attachment.getNormalizedStatus())
                && attachment.getNormalizedPages() != null && !attachment.getNormalizedPages().isEmpty());
    }

    private boolean selectedByScope(PracticalSubmissionVo submission, String scopeMode)
    {
        return "ALL_SUBMITTED".equals(scopeMode) || submission.getScore() == null;
    }

    private String normalizeScopeMode(String value)
    {
        if (StringUtils.isBlank(value) || "UNGRADED_ONLY".equals(value)) return "UNGRADED_ONLY";
        if ("ALL_SUBMITTED".equals(value)) return value;
        throw new ServiceException("AI 批改范围无效");
    }

    private List<PracticalQuestionMaterial> effectiveReferenceAnswers(Long teacherUserId, Long deptId,
                                                                       Long lessonId, Long questionId)
    {
        TeacherPracticalReferenceAnswer current = referenceAnswerService.current(
                teacherUserId, deptId, lessonId, questionId);
        if (current != null)
        {
            PracticalQuestionMaterial material = new PracticalQuestionMaterial();
            material.setMaterialId(current.getReferenceId()); material.setQuestionId(questionId);
            material.setMaterialType("REFERENCE"); material.setOriginalFileName(current.getOriginalFileName());
            material.setResourcePath(current.getResourcePath()); material.setFileExtension(current.getFileExtension());
            material.setMimeType(current.getMimeType()); material.setFileSize(current.getFileSize());
            material.setSha256(current.getSha256());
            return Collections.singletonList(material);
        }
        return materials(questionId, "REFERENCE");
    }

    private List<PracticalQuestionMaterial> materials(Long questionId, String type)
    {
        List<PracticalQuestionMaterial> result = new ArrayList<PracticalQuestionMaterial>();
        for (PracticalQuestionMaterial material : artifactMapper.selectMaterialsByQuestion(questionId))
            if (material != null && type.equals(material.getMaterialType())
                    && filePolicyService.parseAllowedExtensions(
                            PracticalFilePolicyService.DEFAULT_ALLOWED_EXTENSIONS)
                            .contains(material.getFileExtension() == null ? ""
                                    : material.getFileExtension().toLowerCase())) result.add(material);
        return result;
    }

    private String writeJson(Object value)
    {
        try { return objectMapper.writeValueAsString(value); }
        catch (Exception e) { throw new ServiceException("AI 批改依据快照生成失败"); }
    }

    private void afterCommit(final Long jobId)
    {
        if (TransactionSynchronizationManager.isSynchronizationActive())
        {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
            {
                @Override public void afterCommit() { worker.run(jobId); }
            });
        }
        else worker.run(jobId);
    }
}
