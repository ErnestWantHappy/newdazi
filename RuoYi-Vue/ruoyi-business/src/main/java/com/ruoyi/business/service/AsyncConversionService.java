package com.ruoyi.business.service;

import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.utils.FileConversionUtils;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;

/**
 * 异步文件转换服务
 * 用于在后台异步处理 DOCX 到 PDF 的转换，避免用户长时间等待
 */
@Service
public class AsyncConversionService {

    private static final Logger log = LoggerFactory.getLogger(AsyncConversionService.class);
    private static final int MAX_AUTO_RETRY_COUNT = 3;
    private static final int MAX_ERROR_MESSAGE_LENGTH = 255;

    @Autowired
    private BizStudentAnswerMapper studentAnswerMapper;

    /**
     * 异步转换操作题文件
     * @param answerId 答题记录ID
     */
    @Async("conversionExecutor")
    public void convertAsync(Long answerId, String docxFullPath, String outputDir, String previewUrlPrefix) {
        convertStudentAnswerPreview(answerId, false, true, "submit");
    }

    /**
     * 异步重试指定答题记录的预览转换
     */
    @Async("conversionExecutor")
    public void retryAnswerPreviewAsync(Long answerId, boolean ignoreRetryLimit, String triggerSource) {
        convertStudentAnswerPreview(answerId, true, ignoreRetryLimit, triggerSource);
    }

    /**
     * 学生答题预览转换统一入口
     */
    private void convertStudentAnswerPreview(Long answerId, boolean countAsRetry, boolean ignoreRetryLimit, String triggerSource) {
        BizStudentAnswer answer = studentAnswerMapper.selectById(answerId);
        if (answer == null) {
            log.warn("【异步转换】未找到答题记录，answerId={}, source={}", answerId, triggerSource);
            return;
        }
        log.info("【异步转换】已读取答题记录，answerId={}, source={}, status={}, retryCount={}",
                answerId, triggerSource, answer.getPreviewStatus(), answer.getPreviewRetryCount());

        String answerFilePath = answer.getStudentAnswer();
        if (answerFilePath == null || answerFilePath.trim().isEmpty()) {
            log.warn("【异步转换】答题记录没有源文件，answerId={}, source={}", answerId, triggerSource);
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

        if (countAsRetry) {
            Integer currentRetryCount = answer.getPreviewRetryCount() == null ? 0 : answer.getPreviewRetryCount();
            if (!ignoreRetryLimit && currentRetryCount >= MAX_AUTO_RETRY_COUNT) {
                log.info("【异步转换】达到自动重试上限，跳过 answerId={}, retryCount={}, source={}",
                        answerId, currentRetryCount, triggerSource);
                return;
            }
            answer.setPreviewRetryCount(currentRetryCount + 1);
            answer.setPreviewLastRetryTime(new Date());
        } else if (answer.getPreviewRetryCount() == null) {
            answer.setPreviewRetryCount(0);
        }

        answer.setPreviewStatus("converting");
        answer.setPreviewPath(null);
        answer.setPreviewErrorMessage(null);
        studentAnswerMapper.updatePreviewStatus(answer);
        log.info("【异步转换】answerId={}, source={} 已切换为 converting", answerId, triggerSource);

        String fileSystemRelativePath = answerFilePath.replaceFirst(Constants.RESOURCE_PREFIX, "");
        String docxFullPath = RuoYiConfig.getProfile() + fileSystemRelativePath;
        String outputDir = new File(docxFullPath).getParent();
        String previewUrlPrefix = answerFilePath.substring(0, answerFilePath.lastIndexOf('/') + 1);

        log.info("【异步转换】开始处理 answerId={}, source={}, retryCount={}, file={}",
                answerId, triggerSource, answer.getPreviewRetryCount(), docxFullPath);

        try {
            File docxFile = new File(docxFullPath);
            if (!docxFile.exists()) {
                log.error("【异步转换】源文件不存在: {}", docxFullPath);
                markFailed(answer, "源文件不存在");
                return;
            }

            if (!FileConversionUtils.isLibreOfficeInstalled()) {
                log.error("【异步转换】LibreOffice 未安装");
                markFailed(answer, "LibreOffice 未安装或不可用");
                return;
            }

            String pdfFullPath = FileConversionUtils.convertDocxToPdfWithLibreOffice(docxFullPath, outputDir);
            if (pdfFullPath != null && new File(pdfFullPath).exists()) {
                String pdfFileName = new File(pdfFullPath).getName();
                String previewUrlPath = previewUrlPrefix + pdfFileName;
                log.info("【异步转换】成功 answerId={}, previewPath={}", answerId, previewUrlPath);
                answer.setPreviewStatus("success");
                answer.setPreviewPath(previewUrlPath);
                answer.setPreviewErrorMessage(null);
                studentAnswerMapper.updatePreviewStatus(answer);
            } else {
                log.error("【异步转换】转换失败 answerId={}", answerId);
                markFailed(answer, "PDF 文件生成失败");
            }
        } catch (Exception e) {
            log.error("【异步转换】异常 answerId={}, error={}", answerId, e.getMessage(), e);
            markFailed(answer, "转换执行异常：" + e.getMessage());
        }
    }
    
    /**
     * 更新答题记录的预览状态
     */
    private void markFailed(BizStudentAnswer answer, String errorMessage) {
        answer.setPreviewStatus("failed");
        answer.setPreviewPath(null);
        answer.setPreviewErrorMessage(truncateErrorMessage(errorMessage));
        studentAnswerMapper.updatePreviewStatus(answer);
        log.warn("【异步转换】失败收口，answerId={}, status=failed, reason={}",
                answer.getAnswerId(), answer.getPreviewErrorMessage());
    }

    private String truncateErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.isEmpty()) {
            return null;
        }
        return errorMessage.length() <= MAX_ERROR_MESSAGE_LENGTH
                ? errorMessage
                : errorMessage.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }

    @Autowired
    private com.ruoyi.business.mapper.BizQuestionMapper bizQuestionMapper;

    /**
     * 异步转换题库中的操作题文件
     * @param questionId 题目ID
     * @param docxFullPath DOCX文件完整路径
     * @param outputDir PDF输出目录
     * @param previewUrlPath 预览URL路径（已计算好）
     */
    @Async("conversionExecutor")
    public void convertQuestionAsync(Long questionId, String docxFullPath, String outputDir, String previewUrlPath) {
        log.info("【题库异步转换】开始处理 questionId={}, docxPath={}", questionId, docxFullPath);
        
        try {
            // 1. 更新状态为 converting
            updateQuestionPreviewStatus(questionId, "converting", null);
            
            // 2. 检查源文件是否存在
            File docxFile = new File(docxFullPath);
            if (!docxFile.exists()) {
                log.error("【题库异步转换】源文件不存在: {}", docxFullPath);
                updateQuestionPreviewStatus(questionId, "failed", null);
                return;
            }
            
            // 3. 检查 LibreOffice 是否可用
            if (!FileConversionUtils.isLibreOfficeInstalled()) {
                log.error("【题库异步转换】LibreOffice 未安装");
                updateQuestionPreviewStatus(questionId, "failed", null);
                return;
            }
            
            // 4. 执行转换
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
    
    /**
     * 更新题目的预览状态
     */
    private void updateQuestionPreviewStatus(Long questionId, String status, String previewPath) {
        bizQuestionMapper.updatePreviewStatus(questionId, status, previewPath);
    }
}
