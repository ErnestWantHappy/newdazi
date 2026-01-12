package com.ruoyi.business.service;

import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.utils.FileConversionUtils;
import com.ruoyi.common.config.RuoYiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * 异步文件转换服务
 * 用于在后台异步处理 DOCX 到 PDF 的转换，避免用户长时间等待
 */
@Service
public class AsyncConversionService {

    private static final Logger log = LoggerFactory.getLogger(AsyncConversionService.class);

    @Autowired
    private BizStudentAnswerMapper studentAnswerMapper;

    /**
     * 异步转换操作题文件
     * @param answerId 答题记录ID
     * @param docxFullPath DOCX文件完整路径
     * @param outputDir PDF输出目录
     * @param previewUrlPrefix 预览URL前缀（如 /profile/upload/practical/preview/）
     */
    @Async("conversionExecutor")
    public void convertAsync(Long answerId, String docxFullPath, String outputDir, String previewUrlPrefix) {
        log.info("【异步转换】开始处理 answerId={}, docxPath={}", answerId, docxFullPath);
        
        try {
            // 1. 更新状态为 converting
            updatePreviewStatus(answerId, "converting", null);
            
            // 2. 检查源文件是否存在
            File docxFile = new File(docxFullPath);
            if (!docxFile.exists()) {
                log.error("【异步转换】源文件不存在: {}", docxFullPath);
                updatePreviewStatus(answerId, "failed", null);
                return;
            }
            
            // 3. 检查 LibreOffice 是否可用
            if (!FileConversionUtils.isLibreOfficeInstalled()) {
                log.error("【异步转换】LibreOffice 未安装");
                updatePreviewStatus(answerId, "failed", null);
                return;
            }
            
            // 4. 执行转换
            String pdfFullPath = FileConversionUtils.convertDocxToPdfWithLibreOffice(docxFullPath, outputDir);
            
            if (pdfFullPath != null && new File(pdfFullPath).exists()) {
                // 5. 转换成功，计算预览URL路径
                String pdfFileName = new File(pdfFullPath).getName();
                String previewUrlPath = previewUrlPrefix + pdfFileName;
                
                log.info("【异步转换】成功 answerId={}, previewPath={}", answerId, previewUrlPath);
                updatePreviewStatus(answerId, "success", previewUrlPath);
            } else {
                log.error("【异步转换】转换失败 answerId={}", answerId);
                updatePreviewStatus(answerId, "failed", null);
            }
            
        } catch (Exception e) {
            log.error("【异步转换】异常 answerId={}, error={}", answerId, e.getMessage(), e);
            updatePreviewStatus(answerId, "failed", null);
        }
    }
    
    /**
     * 更新答题记录的预览状态
     */
    private void updatePreviewStatus(Long answerId, String status, String previewPath) {
        BizStudentAnswer answer = new BizStudentAnswer();
        answer.setAnswerId(answerId);
        answer.setPreviewStatus(status);
        answer.setPreviewPath(previewPath);
        studentAnswerMapper.updatePreviewStatus(answer);
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
