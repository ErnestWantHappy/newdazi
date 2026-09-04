package com.ruoyi.business.service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.domain.FlowchartLessonSnapshot;
import com.ruoyi.business.domain.FlowchartSubmission;
import com.ruoyi.business.domain.PracticalAiEvent;
import com.ruoyi.business.domain.PracticalAiJob;
import com.ruoyi.business.domain.PracticalAiResult;
import com.ruoyi.business.domain.PracticalAttachment;
import com.ruoyi.business.domain.PracticalQuestionMaterial;
import com.ruoyi.business.domain.PracticalRubricSnapshot;
import com.ruoyi.business.domain.TeacherAiConfig;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.FlowchartMapper;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.mapper.PracticalAiGradingMapper;
import com.ruoyi.business.mapper.PracticalArtifactMapper;
import com.ruoyi.business.mapper.PracticalRubricSnapshotMapper;
import com.ruoyi.business.utils.FileConversionUtils;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class PracticalAiJobWorker
{
    @Autowired private PracticalAiGradingMapper aiMapper;
    @Autowired private BizStudentAnswerMapper answerMapper;
    @Autowired private PracticalArtifactMapper artifactMapper;
    @Autowired private PracticalRubricSnapshotMapper snapshotMapper;
    @Autowired private PracticalRubricSnapshotService rubricService;
    @Autowired private TeacherAiConfigService configService;
    @Autowired private PracticalVisionGradingProvider provider;
    @Autowired private PracticalPageRenderer pageRenderer;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private FlowchartMapper flowchartMapper;
    @Autowired private BizLessonQuestionMapper lessonQuestionMapper;
    @Autowired private FlowchartImageRenderer flowchartImageRenderer;

    @Async("practicalAiExecutor")
    public void run(Long jobId)
    {
        PracticalAiJob job = aiMapper.selectJobForWorker(jobId);
        if (job == null || !("PENDING".equals(job.getJobStatus()) || "RUNNING".equals(job.getJobStatus())
                || "CANCEL_REQUESTED".equals(job.getJobStatus()))) return;
        if ("CANCEL_REQUESTED".equals(job.getJobStatus()))
        {
            cancelRemaining(jobId);
            return;
        }
        try
        {
            aiMapper.updateJobStatus(jobId, "RUNNING", new Date(), null, null);
            event(jobId, null, "INFO", "JOB_STARTED", "AI 批改任务开始或继续执行");
            List<ComparisonPage> comparisonPages = "FLOWCHART".equals(job.getReferenceAnswerJson())
                    ? new ArrayList<ComparisonPage>() : prepareComparisonPages(job);
            TeacherAiConfig config = configService.status(job.getTeacherUserId());
            String apiKey = configService.apiKey(config);
            for (PracticalAiResult candidate : aiMapper.selectResultsByJob(jobId))
            {
                if (!"PENDING".equals(candidate.getResultStatus())) continue;
                PracticalAiJob current = aiMapper.selectJobForWorker(jobId);
                if (current == null || "PAUSED".equals(current.getJobStatus())) return;
                if ("CANCEL_REQUESTED".equals(current.getJobStatus()))
                {
                    cancelRemaining(jobId);
                    return;
                }
                // 条件更新保证即使意外重复唤醒，同一份作品也只会被一个线程认领。
                if (aiMapper.markResultProcessing(candidate.getResultId(), "PREPARING_STUDENT") == 0) continue;
                PracticalAiResult result = aiMapper.selectResult(candidate.getResultId());
                aiMapper.updateJobHeartbeat(jobId, result.getResultId());
                event(jobId, result.getResultId(), "INFO", "PREPARING_STUDENT", "开始准备本份学生作品");
                gradeOne(job, config, apiKey, result, comparisonPages);
                aiMapper.updateJobHeartbeat(jobId, null);
                aiMapper.updateJobCounts(jobId);
            }
            finish(jobId);
        }
        catch (Exception e)
        {
            aiMapper.updateJobCounts(jobId);
            String message = safeMessage(e);
            aiMapper.updateJobStatus(jobId, "FAILED", null, new Date(), message);
            event(jobId, null, "ERROR", "JOB_FAILED", message);
        }
    }

    private void gradeOne(PracticalAiJob job, TeacherAiConfig config, String apiKey, PracticalAiResult result,
                          List<ComparisonPage> comparisonPages)
    {
        long startedAt = System.currentTimeMillis();
        try
        {
            BizStudentAnswer answer = answerMapper.selectById(result.getAnswerId());
            boolean flowchart = "FLOWCHART".equals(job.getReferenceAnswerJson());
            boolean sameFlowchartSubmission = flowchart
                    && result.getPracticalVersionId().equals(answer == null ? null : answer.getPracticalVersionId())
                    && ("FLOWCHART:" + result.getPracticalVersionId()).equals(answer == null ? null : answer.getStudentAnswer());
            if (answer == null || (flowchart ? !sameFlowchartSubmission
                    : !result.getPracticalVersionId().equals(answer.getPracticalVersionId())))
                throw new ServiceException("学生已补交，原 AI 任务版本失效");
            PracticalAiGradingInput input = new PracticalAiGradingInput();
            if ("FLOWCHART".equals(job.getReferenceAnswerJson())) {
                prepareFlowchartInput(answer, result, input);
            } else {
                PracticalRubricSnapshot rubric = snapshotMapper.selectByVersionId(result.getPracticalVersionId());
                if (rubric == null || !result.getRubricSnapshotId().equals(rubric.getSnapshotId()))
                    throw new ServiceException("提交版本未绑定有效评分标准快照");
                input.setRubric(rubric);
                input.setScoringItems(rubricService.buildScoringItems(rubric));
                loadPages(result.getPracticalVersionId(), input);
            }
            addComparisonPages(comparisonPages, input);

            updateStage(job.getJobId(), result.getResultId(), "REQUESTING_MODEL", "作品页图已准备，正在等待视觉模型返回");
            PracticalAiGradingOutput output = provider.grade(config, apiKey, input);
            updateStage(job.getJobId(), result.getResultId(), "VALIDATING_RESULT", "模型已返回，正在校验分项分数并保存建议");
            BizStudentAnswer latest = answerMapper.selectById(result.getAnswerId());
            boolean latestFlowchartSubmission = flowchart
                    && result.getPracticalVersionId().equals(latest == null ? null : latest.getPracticalVersionId())
                    && ("FLOWCHART:" + result.getPracticalVersionId()).equals(latest == null ? null : latest.getStudentAnswer());
            if (latest == null || (flowchart ? !latestFlowchartSubmission
                    : !result.getPracticalVersionId().equals(latest.getPracticalVersionId())))
                throw new ServiceException("AI 返回前学生已补交，本建议已作废");
            result.setResultStatus("SUCCESS");
            result.setProcessingStage("COMPLETED");
            result.setSuggestedScore(output.getSuggestedScore());
            result.setScoringDetailsJson(output.getScoringDetailsJson());
            result.setEvidenceJson(output.getEvidenceJson());
            result.setConfidence(output.getConfidence());
            result.setProviderRequestId(output.getRequestId());
            result.setPromptTokens(output.getPromptTokens());
            result.setCompletionTokens(output.getCompletionTokens());
            result.setErrorMessage(null);
            result.setFinishTime(new Date());
            result.setDurationMs(System.currentTimeMillis() - startedAt);
            aiMapper.updateResult(result);
            event(job.getJobId(), result.getResultId(), "INFO", "COMPLETED", "本份 AI 建议已完成并保存");
        }
        catch (Exception e)
        {
            String message = safeMessage(e);
            result.setResultStatus("FAILED");
            result.setProcessingStage("FAILED");
            result.setErrorMessage(message);
            result.setFinishTime(new Date());
            result.setDurationMs(System.currentTimeMillis() - startedAt);
            aiMapper.updateResult(result);
            event(job.getJobId(), result.getResultId(), "ERROR", "FAILED", message);
        }
    }

    private void prepareFlowchartInput(BizStudentAnswer answer, PracticalAiResult result,
                                       PracticalAiGradingInput input) throws Exception {
        FlowchartSubmission submission = flowchartMapper.selectSubmissionById(result.getPracticalVersionId());
        if (submission == null || !result.getAnswerId().equals(submission.getAnswerId()))
            throw new ServiceException("流程图提交版本不存在或已失效");
        FlowchartLessonSnapshot snapshot = flowchartMapper.selectLessonSnapshot(
                submission.getLessonId(), submission.getQuestionId());
        if (snapshot == null) throw new ServiceException("流程图课程快照不存在");
        PracticalRubricSnapshot rubric = new PracticalRubricSnapshot();
        rubric.setLessonId(submission.getLessonId()); rubric.setQuestionId(submission.getQuestionId());
        rubric.setQuestionContent("流程图操作题：依据题干、标准答案图和学生作品图判断完成质量");
        rubric.setQuestionScore(resolveQuestionScore(submission.getLessonId(), submission.getQuestionId()));
        rubric.setScoringItemsJson("[]");
        input.setRubric(rubric);
        input.setScoringItems(new ArrayList<com.ruoyi.business.domain.vo.PracticalScoringItemVo>());
        File dir = new File(RuoYiConfig.getProfile(), "upload/ai-flowchart/" + result.getResultId());
        File student = flowchartImageRenderer.render(submission.getDocumentJson(), new File(dir, "student.jpg"));
        File answerImage = flowchartImageRenderer.render(snapshot.getAnswerJson(), new File(dir, "answer.jpg"));
        input.setPageImages(new ArrayList<File>()); input.setPageLabels(new ArrayList<String>());
        input.getPageImages().add(student); input.getPageLabels().add("学生流程图作品");
        input.getPageImages().add(answerImage); input.getPageLabels().add("教师标准答案图（仅供对照）");
        java.util.Map<String, Object> auxiliary = new java.util.LinkedHashMap<String, Object>();
        auxiliary.put("studentDocumentJson", submission.getDocumentJson());
        auxiliary.put("answerDocumentJson", snapshot.getAnswerJson());
        auxiliary.put("structureCheckResult", submission.getCheckResultJson());
        auxiliary.put("structureRules", submission.getRulesSnapshotJson());
        input.setAuxiliaryContextJson(objectMapper.writeValueAsString(auxiliary));
    }

    private int resolveQuestionScore(Long lessonId, Long questionId) {
        for (com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo item :
                lessonQuestionMapper.selectDetailsByLessonId(lessonId)) {
            if (questionId.equals(item.getQuestionId()) && item.getQuestionScore() != null)
                return item.getQuestionScore().intValue();
        }
        throw new ServiceException("流程图题目分值无效");
    }

    private void updateStage(Long jobId, Long resultId, String stage, String message)
    {
        aiMapper.updateResultStage(resultId, stage);
        aiMapper.updateJobHeartbeat(jobId, resultId);
        event(jobId, resultId, "INFO", stage, message);
    }

    private void loadPages(Long versionId, PracticalAiGradingInput input) throws Exception
    {
        Path profile = Paths.get(RuoYiConfig.getProfile()).toAbsolutePath().normalize();
        List<File> files = new ArrayList<File>();
        List<String> labels = new ArrayList<String>();
        int attachmentIndex = 0;
        for (PracticalAttachment attachment : artifactMapper.selectAttachmentsByVersion(versionId))
        {
            attachmentIndex++;
            if (!"success".equalsIgnoreCase(attachment.getNormalizedStatus())
                    || attachment.getNormalizedPagesJson() == null) throw new ServiceException("作品页图尚未转换完成");
            List<String> pages = objectMapper.readValue(attachment.getNormalizedPagesJson(),
                    new TypeReference<List<String>>() { });
            int pageIndex = 0;
            for (String resource : pages)
            {
                pageIndex++;
                File file = resolveResource(resource);
                if (!file.toPath().toAbsolutePath().normalize().startsWith(profile))
                    throw new ServiceException("作品页图路径非法");
                files.add(file);
                labels.add("附件" + attachmentIndex + "第" + pageIndex + "页");
            }
        }
        if (files.isEmpty()) throw new ServiceException("作品没有可识别页图");
        input.setPageImages(files);
        input.setPageLabels(labels);
    }

    /** 公共对照材料只转换一次并持久化页图路径，后续学生直接复用。 */
    private synchronized List<ComparisonPage> prepareComparisonPages(PracticalAiJob initialJob)
    {
        PracticalAiJob job = aiMapper.selectJobForWorker(initialJob.getJobId());
        List<ComparisonPage> cached = readPreparedPages(job.getComparisonPagesJson());
        if ("READY".equals(job.getPreparationStatus()) && !cached.isEmpty() && pagesExist(cached)) return cached;
        if (StringUtils.isBlank(job.getReferenceAnswerJson()))
            throw new ServiceException("AI 任务缺少教师参考答案");

        aiMapper.updateJobPreparation(job.getJobId(), "PREPARING", null, null);
        event(job.getJobId(), null, "INFO", "PREPARING_REFERENCE", "正在准备空白材料与教师参考答案（每个任务仅执行一次）");
        List<ComparisonPage> pages = new ArrayList<ComparisonPage>();
        renderMaterials(job, job.getStarterMaterialsJson(), "空白起始材料", false, pages);
        int referencePages = renderMaterials(job, job.getReferenceAnswerJson(), "教师参考答案", true, pages);
        if (referencePages == 0) throw new ServiceException("教师参考答案无法转换为可识别页图");
        try
        {
            aiMapper.updateJobPreparation(job.getJobId(), "READY", objectMapper.writeValueAsString(pages), null);
        }
        catch (Exception e)
        {
            throw new ServiceException("对照材料页图缓存保存失败");
        }
        event(job.getJobId(), null, "INFO", "REFERENCE_READY", "教师参考答案与空白材料已准备完成，后续作品将复用");
        return pages;
    }

    private List<ComparisonPage> readPreparedPages(String json)
    {
        if (StringUtils.isBlank(json)) return new ArrayList<ComparisonPage>();
        try { return objectMapper.readValue(json, new TypeReference<List<ComparisonPage>>() { }); }
        catch (Exception e) { return new ArrayList<ComparisonPage>(); }
    }

    private boolean pagesExist(List<ComparisonPage> pages)
    {
        try
        {
            for (ComparisonPage page : pages) resolveResource(page.getResourcePath());
            return true;
        }
        catch (Exception e) { return false; }
    }

    private void addComparisonPages(List<ComparisonPage> pages, PracticalAiGradingInput input)
    {
        for (ComparisonPage page : pages)
        {
            input.getPageImages().add(resolveResource(page.getResourcePath()));
            input.getPageLabels().add(page.getLabel());
        }
    }

    private int renderMaterials(PracticalAiJob job, String materialsJson, String labelPrefix,
                                boolean required, List<ComparisonPage> target)
    {
        if (StringUtils.isBlank(materialsJson)) return 0;
        int addedPages = 0;
        List<PracticalQuestionMaterial> materials;
        try
        {
            materials = objectMapper.readValue(materialsJson,
                    new TypeReference<List<PracticalQuestionMaterial>>() { });
        }
        catch (Exception e)
        {
            if (required) throw new ServiceException(labelPrefix + "快照无法读取");
            return 0;
        }
        int materialIndex = 0;
        for (PracticalQuestionMaterial material : materials)
        {
            materialIndex++;
            try
            {
                File source = resolveResource(material.getResourcePath());
                String extension = extensionOf(material);
                String kind;
                File visualSource;
                if ("jpg".equals(extension) || "jpeg".equals(extension) || "png".equals(extension))
                {
                    kind = "IMAGE";
                    visualSource = source;
                }
                else if ("pdf".equals(extension))
                {
                    kind = "DOCUMENT";
                    visualSource = source;
                }
                else if ("doc".equals(extension) || "docx".equals(extension)
                        || "ppt".equals(extension) || "pptx".equals(extension)
                        || "xls".equals(extension) || "xlsx".equals(extension))
                {
                    File outputDir = new File(source.getParentFile(), "ai-reference-pdf");
                    if (!outputDir.exists() && !outputDir.mkdirs())
                        throw new ServiceException(labelPrefix + "转换目录创建失败");
                    String pdf = FileConversionUtils.convertOfficeToPdfWithLibreOffice(
                            source.getAbsolutePath(), outputDir.getAbsolutePath());
                    visualSource = new File(pdf);
                    kind = "DOCUMENT";
                }
                else continue;

                List<String> pages = pageRenderer.renderForOwner(
                        "ai-job-" + job.getJobId() + "-" + labelPrefix.hashCode() + "-" + materialIndex,
                        kind, visualSource);
                int pageIndex = 0;
                for (String page : pages)
                {
                    pageIndex++;
                    target.add(new ComparisonPage(page,
                            labelPrefix + materialIndex + "第" + pageIndex + "页（仅供对照）"));
                    addedPages++;
                }
                aiMapper.updateJobHeartbeat(job.getJobId(), null);
            }
            catch (Exception e)
            {
                if (required) throw e instanceof ServiceException
                        ? (ServiceException) e : new ServiceException(labelPrefix + "无法转换");
                event(job.getJobId(), null, "WARN", "PREPARING_REFERENCE", "一个非必填空白材料无法转换，已跳过");
            }
        }
        return addedPages;
    }

    private String extensionOf(PracticalQuestionMaterial material)
    {
        String extension = material.getFileExtension();
        if (StringUtils.isBlank(extension))
        {
            String path = material.getResourcePath();
            int dot = path == null ? -1 : path.lastIndexOf('.');
            extension = dot < 0 ? "" : path.substring(dot + 1);
        }
        return extension.toLowerCase(Locale.ROOT);
    }

    private File resolveResource(String resource)
    {
        if (resource == null || !resource.toLowerCase(Locale.ROOT).startsWith("/profile/upload/"))
            throw new ServiceException("AI 视觉资源路径非法");
        Path profile = Paths.get(RuoYiConfig.getProfile()).toAbsolutePath().normalize();
        Path file = Paths.get(RuoYiConfig.getProfile(), resource.substring("/profile/".length()))
                .toAbsolutePath().normalize();
        if (!file.startsWith(profile) || !file.toFile().isFile()) throw new ServiceException("AI 视觉资源不存在");
        return file.toFile();
    }

    private void finish(Long jobId)
    {
        aiMapper.updateJobCounts(jobId);
        PracticalAiJob job = aiMapper.selectJobForWorker(jobId);
        if (job == null || "PAUSED".equals(job.getJobStatus())) return;
        if ("CANCEL_REQUESTED".equals(job.getJobStatus()))
        {
            cancelRemaining(jobId);
            return;
        }
        String status = job.getFailedCount() != null && job.getFailedCount() > 0 ? "PARTIAL_FAILED" : "COMPLETED";
        aiMapper.updateJobStatus(jobId, status, null, new Date(), null);
        event(jobId, null, "INFO", status, "PARTIAL_FAILED".equals(status)
                ? "任务已结束，部分作品失败，可单独查看原因并重试" : "全部 AI 建议已生成完成");
    }

    private void cancelRemaining(Long jobId)
    {
        Date now = new Date();
        aiMapper.updatePendingResultsStatus(jobId, "CANCELLED", "教师已取消", now);
        aiMapper.updateJobCounts(jobId);
        aiMapper.updateJobStatus(jobId, "CANCELLED", null, now, null);
        event(jobId, null, "WARN", "CANCELLED", "教师已取消任务，已完成建议保留，未完成作品不再处理");
    }

    private void event(Long jobId, Long resultId, String level, String stage, String message)
    {
        try
        {
            PracticalAiEvent event = new PracticalAiEvent();
            event.setJobId(jobId);
            event.setResultId(resultId);
            event.setEventLevel(level);
            event.setEventStage(stage);
            event.setEventMessage(message == null ? "" : (message.length() > 480 ? message.substring(0, 480) : message));
            aiMapper.insertEvent(event);
        }
        catch (Exception ignored)
        {
            // 可视化日志写入失败不能反向阻断正式 AI 建议处理。
        }
    }

    private String safeMessage(Exception e)
    {
        String message = e instanceof ServiceException ? e.getMessage() : "AI 批改处理失败";
        if (message == null) message = "AI 批改处理失败";
        return message.length() > 480 ? message.substring(0, 480) : message;
    }

    public static class ComparisonPage
    {
        private String resourcePath;
        private String label;

        public ComparisonPage() { }
        public ComparisonPage(String resourcePath, String label)
        {
            this.resourcePath = resourcePath;
            this.label = label;
        }
        public String getResourcePath() { return resourcePath; }
        public void setResourcePath(String resourcePath) { this.resourcePath = resourcePath; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }
}
