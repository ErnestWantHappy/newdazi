package com.ruoyi.business.service;

import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.domain.CountyExamAnswer;
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

    @Autowired
    private CountyExamAnswerMapper countyExamAnswerMapper;

    @Autowired
    private AsyncConversionWorkerService conversionWorkerService;

    /**
     * 提交后触发首次预览转换
     */
    public boolean triggerSubmitPreviewConversion(Long answerId) {
        BizStudentAnswer answer = studentAnswerMapper.selectById(answerId);
        if (answer == null) {
            log.warn("【异步转换】首次转换领取失败，未找到答题记录，answerId={}", answerId);
            return false;
        }
        if (!isWordDocument(answer.getStudentAnswer())) {
            log.info("【异步转换】首次转换跳过，answerId={}, file={}", answerId, answer.getStudentAnswer());
            return false;
        }

        int updated = studentAnswerMapper.claimSubmitPreviewConversion(answerId);
        if (updated <= 0) {
            log.info("【异步转换】首次转换领取被跳过，answerId={}, currentStatus={}",
                    answerId, answer.getPreviewStatus());
            return false;
        }

        conversionWorkerService.executeClaimedPreviewAsync(answerId, "submit");
        log.info("【异步转换】首次转换领取成功，answerId={}", answerId);
        return true;
    }

    /**
     * 区域抽测操作题答卷首次预览转换
     */
    public boolean triggerCountyExamPreviewConversion(Long answerId) {
        CountyExamAnswer answer = countyExamAnswerMapper.selectById(answerId);
        if (answer == null) {
            log.warn("【区域抽测转换】领取失败，未找到答题记录，answerId={}", answerId);
            return false;
        }
        if (!isWordDocument(answer.getStudentAnswer())) {
            log.info("【区域抽测转换】跳过非 Word 文件，answerId={}, file={}", answerId, answer.getStudentAnswer());
            return false;
        }

        Date claimedAt = new Date();
        int updated = countyExamAnswerMapper.claimSubmitPreviewConversion(answerId, claimedAt);
        if (updated <= 0) {
            log.info("【区域抽测转换】领取被跳过，answerId={}, currentStatus={}",
                    answerId, answer.getPreviewStatus());
            return false;
        }

        conversionWorkerService.executeCountyExamPreviewAsync(answerId, "county-submit");
        log.info("【区域抽测转换】领取成功，answerId={}", answerId);
        return true;
    }

    /**
     * 领取失败或卡住的预览重转任务，再交给异步线程执行
     */
    public boolean claimRetryAndExecute(BizStudentAnswer answerSnapshot, boolean ignoreRetryLimit, String triggerSource) {
        if (answerSnapshot == null || answerSnapshot.getAnswerId() == null) {
            return false;
        }

        Integer currentRetryCount = normalizeRetryCount(answerSnapshot.getPreviewRetryCount());
        if (!ignoreRetryLimit && currentRetryCount >= MAX_AUTO_RETRY_COUNT) {
            log.info("【异步转换】达到自动重试上限，跳过 answerId={}, retryCount={}, source={}",
                    answerSnapshot.getAnswerId(), currentRetryCount, triggerSource);
            return false;
        }

        Date claimedAt = new Date();
        int updated = studentAnswerMapper.claimRetryPreviewConversion(
                answerSnapshot.getAnswerId(),
                answerSnapshot.getPreviewStatus(),
                currentRetryCount,
                answerSnapshot.getPreviewLastRetryTime(),
                currentRetryCount + 1,
                claimedAt
        );
        if (updated <= 0) {
            log.info("【异步转换】重转领取被跳过，answerId={}, source={}, status={}, retryCount={}",
                    answerSnapshot.getAnswerId(), triggerSource,
                    answerSnapshot.getPreviewStatus(), currentRetryCount);
            return false;
        }

        conversionWorkerService.executeClaimedPreviewAsync(answerSnapshot.getAnswerId(), triggerSource);
        log.info("【异步转换】重转领取成功，answerId={}, source={}, retryCount={}",
                answerSnapshot.getAnswerId(), triggerSource, currentRetryCount + 1);
        return true;
    }

    /**
     * 领取区域抽测失败或卡住的预览重转任务。
     */
    public boolean claimCountyRetryAndExecute(CountyExamAnswer answerSnapshot, boolean ignoreRetryLimit, String triggerSource) {
        if (answerSnapshot == null || answerSnapshot.getAnswerId() == null) {
            return false;
        }

        Integer currentRetryCount = normalizeRetryCount(answerSnapshot.getPreviewRetryCount());
        if (!ignoreRetryLimit && currentRetryCount >= MAX_AUTO_RETRY_COUNT) {
            log.info("【区域抽测转换】达到自动重试上限，跳过 answerId={}, retryCount={}, source={}",
                    answerSnapshot.getAnswerId(), currentRetryCount, triggerSource);
            return false;
        }

        Date claimedAt = new Date();
        int updated = countyExamAnswerMapper.claimRetryPreviewConversion(
                answerSnapshot.getAnswerId(),
                answerSnapshot.getPreviewStatus(),
                currentRetryCount,
                answerSnapshot.getPreviewLastRetryTime(),
                currentRetryCount + 1,
                claimedAt
        );
        if (updated <= 0) {
            log.info("【区域抽测转换】重转领取被跳过，answerId={}, source={}, status={}, retryCount={}",
                    answerSnapshot.getAnswerId(), triggerSource,
                    answerSnapshot.getPreviewStatus(), currentRetryCount);
            return false;
        }

        conversionWorkerService.executeCountyExamPreviewAsync(answerSnapshot.getAnswerId(), triggerSource);
        log.info("【区域抽测转换】重转领取成功，answerId={}, source={}, retryCount={}",
                answerSnapshot.getAnswerId(), triggerSource, currentRetryCount + 1);
        return true;
    }

    /**
     * 执行已领取的转换任务
     */
    @Async("conversionExecutor")
    public void executeClaimedPreviewAsync(Long answerId, String triggerSource) {
        convertClaimedPreview(answerId, triggerSource);
    }

    /**
     * 执行区域抽测答卷预览转换
     */
    @Async("conversionExecutor")
    public void executeCountyExamPreviewAsync(Long answerId, String triggerSource) {
        convertCountyExamPreview(answerId, triggerSource);
    }

    /**
     * 学生答题预览转换统一执行入口
     */
    private void convertClaimedPreview(Long answerId, String triggerSource) {
        BizStudentAnswer answer = studentAnswerMapper.selectById(answerId);
        if (answer == null) {
            log.warn("【异步转换】未找到答题记录，answerId={}, source={}", answerId, triggerSource);
            return;
        }
        log.info("【异步转换】已读取答题记录，answerId={}, source={}, status={}, retryCount={}",
                answerId, triggerSource, answer.getPreviewStatus(), answer.getPreviewRetryCount());

        if (!"converting".equals(answer.getPreviewStatus())) {
            log.info("【异步转换】跳过未领取任务，answerId={}, source={}, status={}",
                    answerId, triggerSource, answer.getPreviewStatus());
            return;
        }

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

        if (answer.getPreviewRetryCount() == null) {
            answer.setPreviewRetryCount(0);
        }

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
     * 区域抽测答卷预览转换执行入口。
     */
    private void convertCountyExamPreview(Long answerId, String triggerSource) {
        CountyExamAnswer answer = countyExamAnswerMapper.selectById(answerId);
        if (answer == null) {
            log.warn("【区域抽测转换】未找到答题记录，answerId={}, source={}", answerId, triggerSource);
            return;
        }
        if (!"converting".equals(answer.getPreviewStatus())) {
            log.info("【区域抽测转换】跳过未领取任务，answerId={}, source={}, status={}",
                    answerId, triggerSource, answer.getPreviewStatus());
            return;
        }

        String answerFilePath = answer.getStudentAnswer();
        if (answerFilePath == null || answerFilePath.trim().isEmpty()) {
            markCountyFailed(answer, "未找到学生提交的源文件");
            return;
        }

        String lowerCaseAnswer = answerFilePath.toLowerCase();
        if (lowerCaseAnswer.endsWith(".pdf")) {
            answer.setPreviewStatus("success");
            answer.setPreviewPath(answerFilePath);
            answer.setPreviewErrorMessage(null);
            countyExamAnswerMapper.updatePreviewStatus(answer);
            return;
        }
        if (!lowerCaseAnswer.endsWith(".docx") && !lowerCaseAnswer.endsWith(".doc")) {
            markCountyFailed(answer, "不支持重转的文件类型");
            return;
        }

        String fileSystemRelativePath = answerFilePath.replaceFirst(Constants.RESOURCE_PREFIX, "");
        String docxFullPath = RuoYiConfig.getProfile() + fileSystemRelativePath;
        String outputDir = new File(docxFullPath).getParent();
        String previewUrlPrefix = answerFilePath.substring(0, answerFilePath.lastIndexOf('/') + 1);

        try {
            File docxFile = new File(docxFullPath);
            if (!docxFile.exists()) {
                markCountyFailed(answer, "源文件不存在");
                return;
            }
            if (!FileConversionUtils.isLibreOfficeInstalled()) {
                markCountyFailed(answer, "LibreOffice 未安装或不可用");
                return;
            }

            String pdfFullPath = FileConversionUtils.convertDocxToPdfWithLibreOffice(docxFullPath, outputDir);
            if (pdfFullPath != null && new File(pdfFullPath).exists()) {
                String pdfFileName = new File(pdfFullPath).getName();
                String previewUrlPath = previewUrlPrefix + pdfFileName;
                answer.setPreviewStatus("success");
                answer.setPreviewPath(previewUrlPath);
                answer.setPreviewErrorMessage(null);
                countyExamAnswerMapper.updatePreviewStatus(answer);
            } else {
                markCountyFailed(answer, "PDF 文件生成失败");
            }
        } catch (Exception e) {
            log.error("【区域抽测转换】异常 answerId={}, error={}", answerId, e.getMessage(), e);
            markCountyFailed(answer, "转换执行异常：" + e.getMessage());
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

    /**
     * 区域抽测预览失败状态收口。
     */
    private void markCountyFailed(CountyExamAnswer answer, String errorMessage) {
        answer.setPreviewStatus("failed");
        answer.setPreviewPath(null);
        answer.setPreviewErrorMessage(truncateErrorMessage(errorMessage));
        countyExamAnswerMapper.updatePreviewStatus(answer);
        log.warn("【区域抽测转换】失败收口，answerId={}, reason={}",
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

    private int normalizeRetryCount(Integer retryCount) {
        return retryCount == null ? 0 : retryCount;
    }

    private boolean isWordDocument(String answerFilePath) {
        if (answerFilePath == null || answerFilePath.trim().isEmpty()) {
            return false;
        }
        String lowerCaseAnswer = answerFilePath.toLowerCase();
        return lowerCaseAnswer.endsWith(".docx") || lowerCaseAnswer.endsWith(".doc");
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
    public void convertQuestionAsync(Long questionId, String docxFullPath, String outputDir, String previewUrlPath) {
        conversionWorkerService.executeQuestionPreviewAsync(questionId, docxFullPath, outputDir, previewUrlPath);
    }
    
    /**
     * 更新题目的预览状态
     */
    private void updateQuestionPreviewStatus(Long questionId, String status, String previewPath) {
        bizQuestionMapper.updatePreviewStatus(questionId, status, previewPath);
    }
}
