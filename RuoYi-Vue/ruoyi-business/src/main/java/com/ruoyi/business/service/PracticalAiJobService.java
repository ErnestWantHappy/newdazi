package com.ruoyi.business.service;

import java.util.Date;
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
import com.ruoyi.business.domain.vo.PracticalSubmissionVo;
import com.ruoyi.business.mapper.PracticalAiGradingMapper;
import com.ruoyi.common.exception.ServiceException;

@Service
public class PracticalAiJobService
{
    public static final String PROMPT_VERSION = "operation-rubric-v1";
    @Autowired private PracticalAiGradingMapper mapper;
    @Autowired private TeacherAiConfigService configService;
    @Autowired private PracticalAiJobWorker worker;

    @Transactional(rollbackFor = Exception.class)
    public PracticalAiJob create(Long teacherUserId, Long deptId, Long lessonId, Long questionId,
                                 String entryYear, String classCode, List<PracticalSubmissionVo> submissions)
    {
        // 锁定教师配置行，使同一教师双击/并发创建任务时只能生成一个活动任务。
        TeacherAiConfig config = configService.statusForUpdate(teacherUserId);
        configService.apiKey(config); // 创建任务前先验证主密钥和教师密文均可用。
        PracticalAiJob running = mapper.selectRunningJob(teacherUserId, lessonId, questionId, entryYear, classCode);
        if (running != null) return running;

        int eligible = 0;
        int skipped = 0;
        for (PracticalSubmissionVo submission : submissions)
        {
            if (eligible(submission)) eligible++; else if (Boolean.TRUE.equals(submission.getSubmitted())) skipped++;
        }
        if (eligible == 0) throw new ServiceException("本班暂无已完成页图转换的操作题作品");

        PracticalAiJob job = new PracticalAiJob();
        job.setTeacherUserId(teacherUserId); job.setDeptId(deptId); job.setLessonId(lessonId);
        job.setQuestionId(questionId); job.setEntryYear(entryYear); job.setClassCode(classCode);
        job.setProviderCode(config.getProviderCode()); job.setModelName(config.getModelName());
        job.setPromptVersion(PROMPT_VERSION); job.setJobStatus("PENDING");
        job.setTotalCount(eligible); job.setSkippedCount(skipped);
        mapper.insertJob(job);
        for (PracticalSubmissionVo submission : submissions)
        {
            if (!eligible(submission)) continue;
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
        mapper.updateJobStatus(jobId, "PENDING", null, null, null);
        worker.run(jobId);
    }

    public void cancel(Long jobId, Long teacherUserId)
    {
        PracticalAiJob job = requireOwned(jobId, teacherUserId);
        if (!"RUNNING".equals(job.getJobStatus()) && !"PENDING".equals(job.getJobStatus())
                && !"PAUSED".equals(job.getJobStatus())) throw new ServiceException("当前任务不能取消");
        mapper.updateJobStatus(jobId, "CANCEL_REQUESTED", null, null, null);
    }

    public void retryFailed(Long jobId, Long teacherUserId)
    {
        PracticalAiJob job = requireOwned(jobId, teacherUserId);
        if ("RUNNING".equals(job.getJobStatus()) || "PENDING".equals(job.getJobStatus()))
            throw new ServiceException("任务仍在执行中");
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
