package com.ruoyi.business.service;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;
import com.ruoyi.business.domain.PracticalAttachment;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.PracticalArtifactMapper;
import com.ruoyi.business.utils.FileConversionUtils;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;

/**
 * 统一处理 Office/PDF/图片附件：人工预览与 AI 页图规范化彼此独立收口。
 */
@Service
public class PracticalAttachmentConversionService
{
    private static final Logger log = LoggerFactory.getLogger(PracticalAttachmentConversionService.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    /**
     * 与 PracticalArtifactService.MAX_NORMALIZATION_RETRY_COUNT 及重置 SQL 的
     * {@code normalized_retry_count < 3} 保持一致：顶满即不再被任何自动/人工重试领取。
     */
    private static final int MAX_RETRY_COUNT = 3;

    @Autowired private PracticalArtifactMapper artifactMapper;
    @Autowired private BizStudentAnswerMapper studentAnswerMapper;
    @Autowired private PracticalPageRenderer pageRenderer;
    @Autowired @Lazy private PracticalAttachmentConversionService self;

    public boolean convertAsync(Long attachmentId)
    {
        PracticalAttachment attachment = artifactMapper.selectAttachmentById(attachmentId);
        if (attachment == null || !("OFFICE".equals(attachment.getFileKind()) || "PDF".equals(attachment.getFileKind())
                || "IMAGE".equals(attachment.getFileKind()))) return false;
        Date claimedAt = new Date();
        if (artifactMapper.claimAttachmentNormalization(attachmentId, claimedAt) <= 0) return false;
        self.processClaimedAsync(attachmentId);
        return true;
    }

    @Async("conversionExecutor")
    public void processClaimedAsync(Long attachmentId)
    {
        Date claimedAt = new Date();
        PracticalAttachment attachment = artifactMapper.selectAttachmentById(attachmentId);
        if (attachment == null) return;

        try
        {
            File source = resourceFile(attachment.getResourcePath());
            if (!source.isFile())
            {
                markNormalizationFailed(attachment, "源文件不存在");
                return;
            }
            File visualSource = source;
            if ("OFFICE".equals(attachment.getFileKind()))
            {
                artifactMapper.claimAttachmentPreview(attachmentId, claimedAt);
                if (!FileConversionUtils.isLibreOfficeInstalled())
                {
                    markPreviewFailed(attachment, "LibreOffice 未安装或不可用");
                    markNormalizationFailed(attachment, "LibreOffice 未安装或不可用");
                    return;
                }
                String pdfPath = FileConversionUtils.convertOfficeToPdfWithLibreOffice(
                        source.getAbsolutePath(), source.getParent());
                visualSource = pdfPath == null ? null : new File(pdfPath);
                if (visualSource == null || !visualSource.isFile())
                {
                    markOfficeConversionFailed(attachment, source.getAbsolutePath());
                    return;
                }
                String prefix = attachment.getResourcePath().substring(
                        0, attachment.getResourcePath().lastIndexOf('/') + 1);
                attachment.setPreviewStatus("success");
                attachment.setPreviewPath(prefix + visualSource.getName());
                attachment.setPreviewErrorMessage(null);
                attachment.setUpdateTime(new Date());
                artifactMapper.updateAttachmentPreview(attachment);
                syncLegacyPreview(attachment);
            }
            else if (!"PDF".equals(attachment.getFileKind()) && !"IMAGE".equals(attachment.getFileKind()))
            {
                markNormalizationFailed(attachment, "历史文件格式不支持自动页图化");
                return;
            }

            // 页图缓存只省去渲染；PDF 预览必须属于当前附件，不能跳过状态回写。
            PracticalAttachment cached = attachment.getSha256() == null ? null
                    : artifactMapper.selectReusableNormalization(
                            attachment.getSha256(), PracticalPageRenderer.RENDERER_VERSION, attachmentId);
            if (cached != null && normalizedFilesExist(cached.getNormalizedPagesJson()))
            {
                List<String> copiedPages = pageRenderer.copyCachedPages(
                        attachment, source, parsePages(cached.getNormalizedPagesJson()));
                markNormalized(attachment, copiedPages,
                        PracticalPageRenderer.RENDERER_VERSION);
                return;
            }

            markNormalized(attachment, pageRenderer.render(attachment, visualSource),
                    PracticalPageRenderer.RENDERER_VERSION);
        }
        catch (Exception e)
        {
            log.error("【逻辑作品页图】失败 attachmentId={}", attachmentId, e);
            markNormalizationFailed(attachment,
                    e.getMessage() == null ? "规范化渲染异常" : e.getMessage());
        }
    }

    public boolean retry(Long attachmentId)
    {
        PracticalAttachment attachment = artifactMapper.selectAttachmentById(attachmentId);
        if (attachment == null || "unsupported".equals(attachment.getNormalizedStatus())) return false;
        if (artifactMapper.resetAttachmentNormalization(attachmentId, new Date()) <= 0) return false;
        return self.convertAsync(attachmentId);
    }

    /** 对账旧版本已经生成的 PDF，避免重复转换并消除附件状态误报。 */
    public boolean reconcileExistingOfficePreview(Long attachmentId)
    {
        PracticalAttachment attachment = artifactMapper.selectAttachmentById(attachmentId);
        if (attachment == null || !"OFFICE".equals(attachment.getFileKind())
                || attachment.getResourcePath() == null)
        {
            return false;
        }
        File source = resourceFile(attachment.getResourcePath());
        String pdfName = source.getName().replaceFirst("(?i)\\.[^.]+$", ".pdf");
        if (pdfName.equals(source.getName())) pdfName = source.getName() + ".pdf";
        File pdf = new File(source.getParentFile(), pdfName);
        if (!pdf.isFile() || pdf.length() <= 0) return false;

        String prefix = attachment.getResourcePath().substring(
                0, attachment.getResourcePath().lastIndexOf('/') + 1);
        attachment.setPreviewStatus("success");
        attachment.setPreviewPath(prefix + pdf.getName());
        attachment.setPreviewErrorMessage(null);
        attachment.setUpdateTime(new Date());
        artifactMapper.updateAttachmentPreview(attachment);
        syncLegacyPreview(attachment);
        return true;
    }

    private void markNormalized(PracticalAttachment attachment, List<String> pages, String version)
            throws Exception
    {
        attachment.setNormalizedStatus("success");
        attachment.setNormalizedPages(pages);
        attachment.setNormalizedPagesJson(JSON.writeValueAsString(pages));
        attachment.setRendererVersion(version);
        attachment.setNormalizedErrorMessage(null);
        attachment.setUpdateTime(new Date());
        artifactMapper.updateAttachmentNormalization(attachment);
        log.info("【逻辑作品页图】成功 attachmentId={}, pages={}",
                attachment.getAttachmentId(), pages.size());
    }

    private void markNormalizationFailed(PracticalAttachment attachment, String reason)
    {
        attachment.setNormalizedStatus("failed");
        attachment.setNormalizedPagesJson(null);
        attachment.setNormalizedPages(Collections.<String>emptyList());
        attachment.setRendererVersion(PracticalPageRenderer.RENDERER_VERSION);
        attachment.setNormalizedErrorMessage(truncate(reason));
        attachment.setUpdateTime(new Date());
        artifactMapper.updateAttachmentNormalization(attachment);
    }

    /**
     * Office 素材转 PDF 失败后的收口。
     * <p>
     * 与操作题（AsyncConversionWorkerService）、区域抽测（同前）两条通道保持同一判定：
     * 只有共享池和一次性隔离进程都转不动，FileConversionUtils 才会把它登记为不可转换，
     * 此时必须顶满两个重试计数并落库标记文本。
     * <p>
     * 为什么不能只写“PDF 文件生成失败”：教师看到这句话会以为是临时故障而反复手动重转，
     * 而手动重转走的 {@code resetAttachmentNormalization} 条件是
     * {@code normalized_retry_count < 3}，计数没顶满就每次都能捞回，
     * 每捞一次就把共享池再打崩一轮，这正是线上反复崩溃的成因之一。
     * <p>
     * 原始素材文件一律保留不删；教师重新上传后是新文件新行，不会被这条标记误伤。
     *
     * @param sourceAbsolutePath 源文件绝对路径，用于查询是否已被登记为不可转换
     */
    void markOfficeConversionFailed(PracticalAttachment attachment, String sourceAbsolutePath)
    {
        if (FileConversionUtils.isUnconvertible(sourceAbsolutePath))
        {
            attachment.setPreviewRetryCount(MAX_RETRY_COUNT);
            attachment.setNormalizedRetryCount(MAX_RETRY_COUNT);
            markPreviewFailed(attachment, FileConversionUtils.UNCONVERTIBLE_MESSAGE);
            markNormalizationFailed(attachment, FileConversionUtils.UNCONVERTIBLE_MESSAGE);
            log.warn("【逻辑作品页图】素材两条通道均无法转换，已标记并停止重试 attachmentId={}",
                    attachment.getAttachmentId());
        }
        else
        {
            markPreviewFailed(attachment, "PDF 文件生成失败");
            markNormalizationFailed(attachment, "PDF 文件生成失败");
        }
    }

    private void markPreviewFailed(PracticalAttachment attachment, String reason)
    {
        attachment.setPreviewStatus("failed");
        attachment.setPreviewPath(null);
        attachment.setPreviewErrorMessage(truncate(reason));
        attachment.setUpdateTime(new Date());
        artifactMapper.updateAttachmentPreview(attachment);
        syncLegacyPreview(attachment);
    }

    private void syncLegacyPreview(PracticalAttachment attachment)
    {
        if (attachment.getFileOrder() != null && attachment.getFileOrder() == 0)
        {
            studentAnswerMapper.updatePreviewByPracticalVersion(
                    attachment.getVersionId(), attachment.getPreviewStatus(),
                    attachment.getPreviewPath(), attachment.getPreviewErrorMessage());
        }
    }

    private File resourceFile(String resourcePath)
    {
        String relative = resourcePath.replaceFirst(Constants.RESOURCE_PREFIX, "");
        return new File(RuoYiConfig.getProfile() + relative);
    }

    private boolean normalizedFilesExist(String pagesJson)
    {
        try
        {
            for (String page : parsePages(pagesJson)) if (!resourceFile(page).isFile()) return false;
            return !parsePages(pagesJson).isEmpty();
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private List<String> parsePages(String pagesJson) throws Exception
    {
        if (pagesJson == null || pagesJson.trim().isEmpty()) return Collections.emptyList();
        return JSON.readValue(pagesJson, new TypeReference<List<String>>() { });
    }

    private String truncate(String value)
    {
        if (value == null) return null;
        return value.length() <= 255 ? value : value.substring(0, 255);
    }
}
