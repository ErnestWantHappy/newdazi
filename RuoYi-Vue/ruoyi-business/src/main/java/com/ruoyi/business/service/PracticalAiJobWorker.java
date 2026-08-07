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
import com.ruoyi.business.domain.PracticalAiJob;
import com.ruoyi.business.domain.PracticalAiResult;
import com.ruoyi.business.domain.PracticalAttachment;
import com.ruoyi.business.domain.PracticalRubricSnapshot;
import com.ruoyi.business.domain.PracticalQuestionMaterial;
import com.ruoyi.business.domain.TeacherAiConfig;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.PracticalAiGradingMapper;
import com.ruoyi.business.mapper.PracticalArtifactMapper;
import com.ruoyi.business.mapper.PracticalRubricSnapshotMapper;
import com.ruoyi.business.utils.FileConversionUtils;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.exception.ServiceException;

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

    @Async("practicalAiExecutor")
    public void run(Long jobId)
    {
        PracticalAiJob job = aiMapper.selectJobForWorker(jobId);
        if (job == null || !("PENDING".equals(job.getJobStatus()) || "RUNNING".equals(job.getJobStatus()))) return;
        try
        {
            aiMapper.updateJobStatus(jobId, "RUNNING", new Date(), null, null);
            TeacherAiConfig config = configService.status(job.getTeacherUserId());
            String apiKey = configService.apiKey(config);
            for (PracticalAiResult result : aiMapper.selectResultsByJob(jobId))
            {
                if (!"PENDING".equals(result.getResultStatus())) continue;
                PracticalAiJob current = aiMapper.selectJobForWorker(jobId);
                if ("PAUSED".equals(current.getJobStatus())) return;
                if ("CANCEL_REQUESTED".equals(current.getJobStatus()))
                {
                    Date now = new Date();
                    aiMapper.updatePendingResultsStatus(jobId, "CANCELLED", "教师已取消", now);
                    aiMapper.updateJobCounts(jobId);
                    aiMapper.updateJobStatus(jobId, "CANCELLED", null, now, null);
                    return;
                }
                gradeOne(config, apiKey, result);
                aiMapper.updateJobCounts(jobId);
            }
            finish(jobId);
        }
        catch (Exception e)
        {
            aiMapper.updateJobCounts(jobId);
            aiMapper.updateJobStatus(jobId, "FAILED", null, new Date(), safeMessage(e));
        }
    }

    private void gradeOne(TeacherAiConfig config, String apiKey, PracticalAiResult result)
    {
        try
        {
            BizStudentAnswer answer = answerMapper.selectById(result.getAnswerId());
            if (answer == null || !result.getPracticalVersionId().equals(answer.getPracticalVersionId()))
                throw new ServiceException("学生已补交，原 AI 任务版本失效");
            PracticalRubricSnapshot rubric = snapshotMapper.selectByVersionId(result.getPracticalVersionId());
            if (rubric == null || !result.getRubricSnapshotId().equals(rubric.getSnapshotId()))
                throw new ServiceException("提交版本未绑定有效评分标准快照");
            PracticalAiGradingInput input = new PracticalAiGradingInput();
            input.setRubric(rubric); input.setScoringItems(rubricService.buildScoringItems(rubric));
            loadPages(result.getPracticalVersionId(), input);
            loadReferencePages(result.getResultId(), rubric, input);
            PracticalAiGradingOutput output = provider.grade(config, apiKey, input);
            BizStudentAnswer latest = answerMapper.selectById(result.getAnswerId());
            if (latest == null || !result.getPracticalVersionId().equals(latest.getPracticalVersionId()))
                throw new ServiceException("AI 返回前学生已补交，本建议已作废");
            result.setResultStatus("SUCCESS"); result.setSuggestedScore(output.getSuggestedScore());
            result.setScoringDetailsJson(output.getScoringDetailsJson()); result.setEvidenceJson(output.getEvidenceJson());
            result.setConfidence(output.getConfidence()); result.setProviderRequestId(output.getRequestId());
            result.setPromptTokens(output.getPromptTokens()); result.setCompletionTokens(output.getCompletionTokens());
            result.setErrorMessage(null); result.setFinishTime(new Date()); aiMapper.updateResult(result);
        }
        catch (Exception e)
        {
            result.setResultStatus("FAILED"); result.setErrorMessage(safeMessage(e));
            result.setFinishTime(new Date()); aiMapper.updateResult(result);
        }
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
                if (resource == null || !resource.toLowerCase().startsWith("/profile/upload/"))
                    throw new ServiceException("作品页图路径非法");
                Path file = Paths.get(RuoYiConfig.getProfile(), resource.substring("/profile/".length()))
                        .toAbsolutePath().normalize();
                if (!file.startsWith(profile) || !file.toFile().isFile()) throw new ServiceException("作品页图不存在");
                files.add(file.toFile()); labels.add("附件" + attachmentIndex + "第" + pageIndex + "页");
            }
        }
        if (files.isEmpty()) throw new ServiceException("作品没有可识别页图");
        input.setPageImages(files); input.setPageLabels(labels);
    }

    /** 参考材料只作为额外视觉证据，并用明确标签与学生作品页面隔开。 */
    private synchronized void loadReferencePages(Long resultId, PracticalRubricSnapshot rubric,
                                                 PracticalAiGradingInput input)
    {
        int materialIndex = 0;
        for (PracticalQuestionMaterial material : rubricService.getReferenceMaterials(rubric))
        {
            materialIndex++;
            try
            {
                File source = resolveResource(material.getResourcePath());
                String extension = material.getFileExtension();
                if (extension == null || extension.trim().isEmpty())
                {
                    String path = material.getResourcePath();
                    int dot = path == null ? -1 : path.lastIndexOf('.');
                    extension = dot < 0 ? "" : path.substring(dot + 1);
                }
                extension = extension.toLowerCase(Locale.ROOT);
                String kind;
                File visualSource;
                if ("jpg".equals(extension) || "jpeg".equals(extension) || "png".equals(extension))
                {
                    kind = "IMAGE"; visualSource = source;
                }
                else if ("pdf".equals(extension))
                {
                    kind = "DOCUMENT"; visualSource = source;
                }
                else if ("doc".equals(extension) || "docx".equals(extension)
                        || "ppt".equals(extension) || "pptx".equals(extension)
                        || "xls".equals(extension) || "xlsx".equals(extension))
                {
                    File outputDir = new File(source.getParentFile(), "ai-reference-pdf");
                    if (!outputDir.exists() && !outputDir.mkdirs()) throw new ServiceException("参考材料转换目录创建失败");
                    String pdf = FileConversionUtils.convertOfficeToPdfWithLibreOffice(
                            source.getAbsolutePath(), outputDir.getAbsolutePath());
                    visualSource = new File(pdf); kind = "DOCUMENT";
                }
                else continue; // 压缩包等教师资源不作为视觉评分输入。

                List<String> pages = pageRenderer.renderForOwner(
                        "ai-ref-" + rubric.getSnapshotId() + "-" + material.getMaterialId(), kind, visualSource);
                int pageIndex = 0;
                for (String page : pages)
                {
                    pageIndex++;
                    input.getPageImages().add(resolveResource(page));
                    input.getPageLabels().add("参考材料" + materialIndex + "第" + pageIndex + "页（仅供对照）");
                }
            }
            catch (Exception ignored)
            {
                // 单个参考材料不可渲染时仍可依据题干和作品生成建议，最终由教师复核。
            }
        }
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
        String status = job.getFailedCount() != null && job.getFailedCount() > 0 ? "PARTIAL_FAILED" : "COMPLETED";
        aiMapper.updateJobStatus(jobId, status, null, new Date(), null);
    }

    private String safeMessage(Exception e)
    {
        String message = e instanceof ServiceException ? e.getMessage() : "AI 批改处理失败";
        if (message == null) message = "AI 批改处理失败";
        return message.length() > 480 ? message.substring(0, 480) : message;
    }
}
