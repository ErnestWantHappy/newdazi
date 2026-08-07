package com.ruoyi.business.service;

import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.domain.CountyExamAnswer;
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.CountyExamAnswerMapper;
import com.ruoyi.business.utils.FileConversionUtils;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 文件转换执行器。
 */
@Service
public class AsyncConversionWorkerService {

    private static final Logger log = LoggerFactory.getLogger(AsyncConversionWorkerService.class);
    private static final int MAX_ERROR_MESSAGE_LENGTH = 255;
    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired
    private BizStudentAnswerMapper studentAnswerMapper;

    @Autowired
    private CountyExamAnswerMapper countyExamAnswerMapper;

    @Autowired
    private BizQuestionMapper bizQuestionMapper;

    @Autowired
    private PracticalPageRenderer pageRenderer;

    /**
     * 执行已领取的日常操作题预览转换任务。
     */
    @Async("conversionExecutor")
    public void executeClaimedPreviewAsync(Long answerId, String triggerSource) {
        convertClaimedPreview(answerId, triggerSource);
    }

    /**
     * 执行已领取的区域抽测答卷预览转换任务。
     */
    @Async("conversionExecutor")
    public void executeCountyExamPreviewAsync(Long answerId, String triggerSource) {
        convertCountyExamPreview(answerId, triggerSource);
    }

    /**
     * 执行题库素材预览转换任务。
     */
    @Async("conversionExecutor")
    public void executeQuestionPreviewAsync(Long questionId, String docxFullPath, String outputDir, String previewUrlPath) {
        log.info("【题库异步转换】开始处理 questionId={}, docxPath={}", questionId, docxFullPath);

        try {
            updateQuestionPreviewStatus(questionId, "converting", null);
            File docxFile = new File(docxFullPath);
            if (!docxFile.exists()) {
                log.error("【题库异步转换】源文件不存在: {}", docxFullPath);
                updateQuestionPreviewStatus(questionId, "failed", null);
                return;
            }
            if (!FileConversionUtils.isLibreOfficeInstalled()) {
                log.error("【题库异步转换】LibreOffice 未安装");
                updateQuestionPreviewStatus(questionId, "failed", null);
                return;
            }

            String pdfFullPath = FileConversionUtils.convertDocxToPdfWithLibreOffice(docxFullPath, outputDir);
            if (pdfFullPath != null && new File(pdfFullPath).exists()) {
                log.info("【题库异步转换】成功 questionId={}, previewPath={}", questionId, previewUrlPath);
                updateQuestionPreviewStatus(questionId, "success", previewUrlPath);
            } else {
                log.error("【题库异步转换】转换失败 questionId={}", questionId);
                updateQuestionPreviewStatus(questionId, "failed", null);
            }
        } catch (Exception e) {
            log.error("【题库异步转换】异常 questionId={}, error={}", questionId, e.getMessage(), e);
            updateQuestionPreviewStatus(questionId, "failed", null);
        }
    }

    private void convertClaimedPreview(Long answerId, String triggerSource) {
        BizStudentAnswer answer = studentAnswerMapper.selectById(answerId);
        if (answer == null) {
            log.warn("【异步转换】未找到答题记录，answerId={}, source={}", answerId, triggerSource);
            return;
        }
        log.info("【异步转换】已进入线程 {}，answerId={}, source={}, status={}, retryCount={}",
                Thread.currentThread().getName(), answerId, triggerSource, answer.getPreviewStatus(), answer.getPreviewRetryCount());

        if (!"converting".equals(answer.getPreviewStatus())) {
            log.info("【异步转换】跳过未领取任务，answerId={}, source={}, status={}",
                    answerId, triggerSource, answer.getPreviewStatus());
            return;
        }

        String answerFilePath = answer.getStudentAnswer();
        if (answerFilePath == null || answerFilePath.trim().isEmpty()) {
            markFailed(answer, "未找到学生提交的源文件");
            return;
        }

        String lowerCaseAnswer = answerFilePath.toLowerCase();
        if (lowerCaseAnswer.endsWith(".pdf")) {
            answer.setPreviewStatus("success");
            answer.setPreviewPath(answerFilePath);
            answer.setPreviewErrorMessage(null);
            studentAnswerMapper.updatePreviewStatus(answer);
            return;
        }
        if (!lowerCaseAnswer.endsWith(".docx") && !lowerCaseAnswer.endsWith(".doc")) {
            markFailed(answer, "不支持重转的文件类型");
            return;
        }

        String fileSystemRelativePath = answerFilePath.replaceFirst(Constants.RESOURCE_PREFIX, "");
        String docxFullPath = RuoYiConfig.getProfile() + fileSystemRelativePath;
        String outputDir = new File(docxFullPath).getParent();
        String previewUrlPrefix = answerFilePath.substring(0, answerFilePath.lastIndexOf('/') + 1);

        try {
            String pdfFullPath = convertToPdf(docxFullPath, outputDir, "【异步转换】");
            if (pdfFullPath != null && new File(pdfFullPath).exists()) {
                String previewUrlPath = previewUrlPrefix + new File(pdfFullPath).getName();
                answer.setPreviewStatus("success");
                answer.setPreviewPath(previewUrlPath);
                answer.setPreviewErrorMessage(null);
                studentAnswerMapper.updatePreviewStatus(answer);
            } else {
                markFailed(answer, "PDF 文件生成失败");
            }
        } catch (Exception e) {
            log.error("【异步转换】异常 answerId={}, error={}", answerId, e.getMessage(), e);
            markFailed(answer, "转换执行异常：" + e.getMessage());
        }
    }

    private void convertCountyExamPreview(Long answerId, String triggerSource) {
        CountyExamAnswer answer = countyExamAnswerMapper.selectById(answerId);
        if (answer == null) {
            log.warn("【区域抽测转换】未找到答题记录，answerId={}, source={}", answerId, triggerSource);
            return;
        }
        log.info("【区域抽测页图】已进入线程 {}，answerId={}, source={}, status={}, retryCount={}",
                Thread.currentThread().getName(), answerId, triggerSource,
                answer.getNormalizedStatus(), answer.getNormalizedRetryCount());

        if (!"converting".equals(answer.getNormalizedStatus())) {
            log.info("【区域抽测页图】跳过未领取任务，answerId={}, source={}, status={}",
                    answerId, triggerSource, answer.getNormalizedStatus());
            return;
        }

        String answerFilePath = answer.getStudentAnswer();
        if (answerFilePath == null || answerFilePath.trim().isEmpty()) {
            markCountyFailed(answer, "未找到学生提交的源文件");
            return;
        }

        String fileSystemRelativePath = answerFilePath.replaceFirst(Constants.RESOURCE_PREFIX, "");
        File source = new File(RuoYiConfig.getProfile() + fileSystemRelativePath);

        try {
            if (!source.isFile()) {
                markCountyFailed(answer, "源文件不存在");
                return;
            }
            String fileKind = countyFileKind(answerFilePath);
            if (fileKind == null) {
                markCountyFailed(answer, "不支持页图化的文件类型");
                return;
            }
            File visualSource = source;
            if ("OFFICE".equals(fileKind)) {
                if (!FileConversionUtils.isLibreOfficeInstalled()) {
                    markCountyFailed(answer, "LibreOffice 未安装或不可用");
                    return;
                }
                String pdfFullPath = FileConversionUtils.convertOfficeToPdfWithLibreOffice(
                        source.getAbsolutePath(), source.getParent());
                visualSource = pdfFullPath == null ? null : new File(pdfFullPath);
                if (visualSource == null || !visualSource.isFile()) {
                    markCountyFailed(answer, "PDF 文件生成失败");
                    return;
                }
                String previewPrefix = answerFilePath.substring(0, answerFilePath.lastIndexOf('/') + 1);
                answer.setPreviewPath(previewPrefix + visualSource.getName());
            } else {
                answer.setPreviewPath(answerFilePath);
            }
            answer.setPreviewStatus("success");
            answer.setPreviewErrorMessage(null);
            List<String> pages = pageRenderer.renderForOwner(
                    "county-answer-" + answerId, fileKind, visualSource);
            answer.setNormalizedStatus("success");
            answer.setNormalizedPages(pages);
            answer.setNormalizedPagesJson(JSON.writeValueAsString(pages));
            answer.setRendererVersion(PracticalPageRenderer.RENDERER_VERSION);
            answer.setNormalizedErrorMessage(null);
            countyExamAnswerMapper.updatePreviewStatus(answer);
        } catch (Exception e) {
            log.error("【区域抽测页图】异常 answerId={}, error={}", answerId, e.getMessage(), e);
            markCountyFailed(answer, e.getMessage() == null ? "页图生成异常" : e.getMessage());
        }
    }

    private String countyFileKind(String path) {
        String lower = path.toLowerCase();
        if (lower.matches(".*\\.(doc|docx|ppt|pptx|xls|xlsx)$")) return "OFFICE";
        if (lower.endsWith(".pdf")) return "PDF";
        if (lower.matches(".*\\.(jpg|jpeg|png)$")) return "IMAGE";
        return null;
    }

    private String convertToPdf(String docxFullPath, String outputDir, String logPrefix) {
        File docxFile = new File(docxFullPath);
        if (!docxFile.exists()) {
            log.error("{}源文件不存在: {}", logPrefix, docxFullPath);
            return null;
        }
        if (!FileConversionUtils.isLibreOfficeInstalled()) {
            log.error("{}LibreOffice 未安装", logPrefix);
            return null;
        }
        return FileConversionUtils.convertDocxToPdfWithLibreOffice(docxFullPath, outputDir);
    }

    private void markFailed(BizStudentAnswer answer, String errorMessage) {
        answer.setPreviewStatus("failed");
        answer.setPreviewPath(null);
        answer.setPreviewErrorMessage(truncateErrorMessage(errorMessage));
        studentAnswerMapper.updatePreviewStatus(answer);
        log.warn("【异步转换】失败收口，answerId={}, reason={}",
                answer.getAnswerId(), answer.getPreviewErrorMessage());
    }

    private void markCountyFailed(CountyExamAnswer answer, String errorMessage) {
        answer.setPreviewStatus("failed");
        answer.setPreviewPath(null);
        answer.setPreviewErrorMessage(truncateErrorMessage(errorMessage));
        answer.setNormalizedStatus("failed");
        answer.setNormalizedPagesJson(null);
        answer.setNormalizedPages(java.util.Collections.<String>emptyList());
        answer.setRendererVersion(PracticalPageRenderer.RENDERER_VERSION);
        answer.setNormalizedErrorMessage(truncateErrorMessage(errorMessage));
        countyExamAnswerMapper.updatePreviewStatus(answer);
        log.warn("【区域抽测页图】失败收口，answerId={}, reason={}",
                answer.getAnswerId(), answer.getNormalizedErrorMessage());
    }

    private void updateQuestionPreviewStatus(Long questionId, String status, String previewPath) {
        bizQuestionMapper.updatePreviewStatus(questionId, status, previewPath);
    }

    private String truncateErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.isEmpty()) {
            return null;
        }
        return errorMessage.length() <= MAX_ERROR_MESSAGE_LENGTH
                ? errorMessage
                : errorMessage.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }
}
