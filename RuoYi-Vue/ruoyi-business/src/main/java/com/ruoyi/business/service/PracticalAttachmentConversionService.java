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

    @Autowired private PracticalArtifactMapper artifactMapper;
    @Autowired private BizStudentAnswerMapper studentAnswerMapper;
    @Autowired private PracticalPageRenderer pageRenderer;
    @Autowired @Lazy private PracticalAttachmentConversionService self;

    public boolean convertAsync(Long attachmentId)
    {
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
                    markPreviewFailed(attachment, "PDF 文件生成失败");
                    markNormalizationFailed(attachment, "PDF 文件生成失败");
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
        if (artifactMapper.resetAttachmentNormalization(attachmentId, new Date()) <= 0) return false;
        return self.convertAsync(attachmentId);
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
