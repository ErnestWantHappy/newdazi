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
import com.ruoyi.business.domain.PracticalAiEvent;
import com.ruoyi.business.domain.PracticalAiResult;
import com.ruoyi.business.domain.TeacherAiConfig;
import com.ruoyi.business.domain.AiModelPrice;
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
    @Autowired private AiModelPricingService pricingService;

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

        boolean flowchart = isFlowchart(submissions);
        List<PracticalQuestionMaterial> referenceAnswers = flowchart
                ? Collections.<PracticalQuestionMaterial>emptyList()
                : effectiveReferenceAnswers(teacherUserId, deptId, lessonId, questionId);
        if (!flowchart && referenceAnswers.isEmpty())
            throw new ServiceException("AI 批改前必须上传教师参考答案");

        int eligible = 0;
        int skipped = 0;
        for (PracticalSubmissionVo submission : submissions)
        {
            if (eligible(submission, flowchart) && selectedByScope(submission, normalizedScope)) eligible++;
            else if (Boolean.TRUE.equals(submission.getSubmitted())) skipped++;
        }
        if (eligible == 0) throw new ServiceException("本班暂无已完成页图转换的操作题作品");

        PracticalAiJob job = new PracticalAiJob();
        job.setTeacherUserId(teacherUserId); job.setDeptId(deptId); job.setLessonId(lessonId);
        job.setQuestionId(questionId); job.setEntryYear(entryYear); job.setClassCode(classCode);
        job.setProviderCode(config.getProviderCode()); job.setModelName(config.getModelName());
        AiModelPrice price = pricingService.require(config.getProviderCode(), config.getModelName());
        job.setInputPricePerThousand(price.getInputPricePerThousand());
        job.setOutputPricePerThousand(price.getOutputPricePerThousand());
        job.setPriceStatus(price.getPriceStatus()); job.setPriceNote(price.getPriceNote());
        job.setPromptVersion(PROMPT_VERSION); job.setScopeMode(normalizedScope);
        job.setReferenceAnswerJson(flowchart ? "FLOWCHART" : writeJson(referenceAnswers));
        job.setStarterMaterialsJson(writeJson(materials(questionId, "STARTER")));
        job.setJobStatus("PENDING");
        job.setTotalCount(eligible); job.setSkippedCount(skipped);
        mapper.insertJob(job);
        for (PracticalSubmissionVo submission : submissions)
        {
            if (!eligible(submission, flowchart) || !selectedByScope(submission, normalizedScope)) continue;
            PracticalAiResult result = new PracticalAiResult();
            result.setJobId(job.getJobId()); result.setAnswerId(submission.getAnswerId());
            result.setPracticalVersionId(submission.getPracticalVersionId());
            // 流程图没有普通文档评分快照，但结果表要求非空；提交版本本身就是不可变评分锚点。
            result.setRubricSnapshotId(flowchart ? submission.getFlowchartSubmissionId()
                    : submission.getRubricSnapshotId());
            result.setResultStatus("PENDING");
            mapper.insertResult(result);
        }
        addEvent(job.getJobId(), null, "INFO", "QUEUED",
                "任务已创建，共有 " + eligible + " 份作品进入 AI 队列");
        afterCommit(job.getJobId());
        return job;
    }

    public Map<String, Object> detail(Long jobId, Long teacherUserId)
    {
        PracticalAiJob job = requireOwned(jobId, teacherUserId);
        List<PracticalAiResult> results = mapper.selectResultsByJob(jobId);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("job", job); result.put("results", results);
        result.put("progress", progress(job, results));
        result.put("usage", pricingService.usage(job, results));
        result.put("batchAdoptAllowed", StringUtils.isNotBlank(job.getReferenceAnswerJson()));
        return result;
    }

    public List<PracticalAiEvent> events(Long jobId, Long teacherUserId, Long afterEventId)
    {
        requireOwned(jobId, teacherUserId);
        long safeAfterId = afterEventId == null || afterEventId < 0 ? 0L : afterEventId;
        return mapper.selectEvents(jobId, safeAfterId, 200);
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
            if (eligible(submission, isFlowchart(submissions)))
            {
                ready++;
                if (submission.getScore() == null) readyUngraded++;
            }
        }
        List<PracticalQuestionMaterial> references = isFlowchart(submissions)
                ? Collections.<PracticalQuestionMaterial>emptyList()
                : effectiveReferenceAnswers(teacherUserId, deptId, lessonId, questionId);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("submittedCount", submitted); result.put("gradedCount", graded);
        result.put("ungradedCount", submitted - graded); result.put("readyCount", ready);
        result.put("readyUngradedCount", readyUngraded);
        result.put("referenceReady", isFlowchart(submissions) || !references.isEmpty());
        result.put("referenceFileName", isFlowchart(submissions) ? "课程标准答案流程图（自动生成）"
                : (references.isEmpty() ? null : references.get(0).getOriginalFileName()));
        result.put("starterCount", materials(questionId, "STARTER").size());
        return result;
    }

    public void pause(Long jobId, Long teacherUserId)
    {
        PracticalAiJob job = requireOwned(jobId, teacherUserId);
        if (!"RUNNING".equals(job.getJobStatus()) && !"PENDING".equals(job.getJobStatus()))
            throw new ServiceException("当前任务不能暂停");
        mapper.updateJobStatus(jobId, "PAUSED", null, null, null);
        addEvent(jobId, null, "WARN", "PAUSED", "教师已暂停任务；当前模型请求完成后停止处理下一份");
    }

    public void resume(Long jobId, Long teacherUserId)
    {
        PracticalAiJob job = requireOwned(jobId, teacherUserId);
        if (!"PAUSED".equals(job.getJobStatus())) throw new ServiceException("当前任务不是暂停状态");
        if (StringUtils.isBlank(job.getReferenceAnswerJson()))
            throw new ServiceException("旧任务缺少教师参考答案，不能继续；请取消后重新发起");
        mapper.updateJobStatus(jobId, "PENDING", null, null, null);
        addEvent(jobId, null, "INFO", "RESUMED", "教师已继续任务，将从未完成作品接续");
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
            addEvent(jobId, null, "WARN", "CANCELLED", "教师已取消任务，已完成建议保留");
            return;
        }
        mapper.updateJobStatus(jobId, "CANCEL_REQUESTED", null, null, null);
        addEvent(jobId, null, "WARN", "CANCEL_REQUESTED", "已收到取消请求；当前模型请求完成后停止");
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
        addEvent(jobId, null, "INFO", "RETRY_QUEUED", count + " 份失败作品已重新进入队列");
        worker.run(jobId);
    }

    private Map<String, Object> progress(PracticalAiJob job, List<PracticalAiResult> results)
    {
        int waiting = 0, processing = 0, completed = 0;
        long durationTotal = 0L;
        int durationCount = 0;
        PracticalAiResult current = null;
        for (PracticalAiResult item : results)
        {
            if ("PENDING".equals(item.getResultStatus())) waiting++;
            else if ("PROCESSING".equals(item.getResultStatus())) processing++;
            else completed++;
            if (item.getDurationMs() != null && item.getDurationMs() > 0)
            {
                durationTotal += item.getDurationMs();
                durationCount++;
            }
            if (job.getCurrentResultId() != null && job.getCurrentResultId().equals(item.getResultId())) current = item;
        }
        long now = System.currentTimeMillis();
        Date basis = job.getStartTime() == null ? job.getCreateTime() : job.getStartTime();
        Date end = job.getFinishTime() == null ? new Date(now) : job.getFinishTime();
        long elapsedSeconds = basis == null ? 0L : Math.max(0L, (end.getTime() - basis.getTime()) / 1000L);
        long averageMs = durationCount == 0 ? 0L : durationTotal / durationCount;
        long estimatedRemainingSeconds = averageMs == 0L ? -1L
                : Math.max(0L, averageMs * (waiting + processing) / 1000L);
        Date heartbeat = job.getHeartbeatTime() == null ? basis : job.getHeartbeatTime();
        boolean stalled = "RUNNING".equals(job.getJobStatus()) && heartbeat != null
                && now - heartbeat.getTime() > 6L * 60L * 1000L;

        Map<String, Object> progress = new LinkedHashMap<String, Object>();
        progress.put("waitingCount", waiting);
        progress.put("processingCount", processing);
        progress.put("completedCount", completed);
        progress.put("elapsedSeconds", elapsedSeconds);
        progress.put("averageDurationMs", averageMs);
        progress.put("estimatedRemainingSeconds", estimatedRemainingSeconds);
        progress.put("stalled", stalled);
        progress.put("heartbeatTime", heartbeat);
        progress.put("preparationStatus", job.getPreparationStatus());
        progress.put("currentResultId", current == null ? null : current.getResultId());
        progress.put("currentAnswerId", current == null ? null : current.getAnswerId());
        progress.put("currentStage", current == null ? null : current.getProcessingStage());
        progress.put("currentStageUpdatedTime", current == null ? null : current.getStageUpdatedTime());
        return progress;
    }

    private void addEvent(Long jobId, Long resultId, String level, String stage, String message)
    {
        try
        {
            PracticalAiEvent event = new PracticalAiEvent();
            event.setJobId(jobId); event.setResultId(resultId); event.setEventLevel(level);
            event.setEventStage(stage); event.setEventMessage(message);
            mapper.insertEvent(event);
        }
        catch (Exception ignored)
        {
            // 日志属于辅助信息，不能让暂停、继续或取消等主操作失败。
        }
    }

    private PracticalAiJob requireOwned(Long jobId, Long teacherUserId)
    {
        PracticalAiJob job = mapper.selectJob(jobId, teacherUserId);
        if (job == null) throw new ServiceException("AI 批改任务不存在或无权访问");
        return job;
    }

    private boolean eligible(PracticalSubmissionVo submission, boolean flowchart)
    {
        if (submission == null || !Boolean.TRUE.equals(submission.getSubmitted())
                || submission.getAnswerId() == null || submission.getPracticalVersionId() == null
                || (!flowchart && (submission.getRubricSnapshotId() == null || submission.getAttachments() == null
                || submission.getAttachments().isEmpty()))) return false;
        if (flowchart) return "FLOWCHART".equalsIgnoreCase(submission.getPracticalMode());
        return submission.getAttachments().stream().allMatch(attachment ->
                "success".equalsIgnoreCase(attachment.getNormalizedStatus())
                && attachment.getNormalizedPages() != null && !attachment.getNormalizedPages().isEmpty());
    }

    private boolean isFlowchart(List<PracticalSubmissionVo> submissions)
    {
        if (submissions == null) return false;
        for (PracticalSubmissionVo submission : submissions)
            if (submission != null && "FLOWCHART".equalsIgnoreCase(submission.getPracticalMode())) return true;
        return false;
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
