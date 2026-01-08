package com.ruoyi.business.service.impl;

import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.domain.BizScoringItem; // P6 import
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.business.mapper.BizScoringItemMapper; // P6 import
import com.ruoyi.business.service.IBizQuestionService;
import com.ruoyi.business.utils.FileConversionUtils;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants; // 导入常量类
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 题库管理Service业务层处理 (集成PDF转换与字数统计功能)
 * @author ruoyi
 */
@Service
public class BizQuestionServiceImpl implements IBizQuestionService
{
    private static final Logger log = LoggerFactory.getLogger(BizQuestionServiceImpl.class);

    @Autowired
    private BizQuestionMapper bizQuestionMapper;

    @Autowired
    private BizScoringItemMapper bizScoringItemMapper; // P6 mapper

    @Override
    public BizQuestion selectBizQuestionByQuestionId(Long questionId) {
        BizQuestion question = bizQuestionMapper.selectBizQuestionByQuestionId(questionId);
        // P6: 查询评分项 (仅操作题)
        if (question != null && "practical".equals(question.getQuestionType())) {
            question.setScoringItems(bizScoringItemMapper.selectItemsByQuestion(questionId));
        }
        return question;
    }

    @Override
    public List<BizQuestion> selectBizQuestionList(BizQuestion bizQuestion) {
        return bizQuestionMapper.selectBizQuestionList(bizQuestion);
    }

    @Override
    public int insertBizQuestion(BizQuestion bizQuestion)
    {
        bizQuestion.setCreateTime(DateUtils.getNowDate());
        bizQuestion.setCreatorId(SecurityUtils.getUserId());
        bizQuestion.setCreateBy(SecurityUtils.getUsername());

        processQuestionByType(bizQuestion);

        int rows = bizQuestionMapper.insertBizQuestion(bizQuestion);
        
        // P6: 保存评分项
        insertScoringItems(bizQuestion);
        
        return rows;
    }

    @Override
    public int updateBizQuestion(BizQuestion bizQuestion)
    {
        bizQuestion.setUpdateTime(DateUtils.getNowDate());
        bizQuestion.setUpdateBy(SecurityUtils.getUsername());

        processQuestionByType(bizQuestion);

        int rows = bizQuestionMapper.updateBizQuestion(bizQuestion);
        
        // P6: 更新评分项 (先删后增)
        bizScoringItemMapper.deleteBizScoringItemByQuestion(bizQuestion.getQuestionId());
        insertScoringItems(bizQuestion);
        
        return rows;
    }

    /**
     * P6: 自定义辅助方法：批量保存评分项
     */
    private void insertScoringItems(BizQuestion bizQuestion) {
        if ("practical".equals(bizQuestion.getQuestionType()) && bizQuestion.getScoringItems() != null) {
            int order = 0;
            for (BizScoringItem item : bizQuestion.getScoringItems()) {
                item.setQuestionId(bizQuestion.getQuestionId());
                item.setOrderNum(order++);
                bizScoringItemMapper.insertBizScoringItem(item);
            }
        }
    }

    @Override
    public int deleteBizQuestionByQuestionIds(Long[] questionIds) {
        return bizQuestionMapper.deleteBizQuestionByQuestionIds(questionIds);
    }

    @Override
    public int deleteBizQuestionByQuestionId(Long questionId) {
        return bizQuestionMapper.deleteBizQuestionByQuestionId(questionId);
    }

    @Override
    public String importQuestion(List<BizQuestion> questionList, String operName)
    {
        if (StringUtils.isNull(questionList) || questionList.size() == 0)
        {
            throw new ServiceException("导入题目数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (BizQuestion question : questionList)
        {
            try
            {
                this.insertBizQuestion(question);
                successNum++;
                String plainTextContent = question.getQuestionContent().replaceAll("<[^>]*>", "");
                String contentPreview = StringUtils.substring(plainTextContent, 0, 15);
                successMsg.append("<br/>").append(successNum).append("、题目 ").append(contentPreview).append("... 导入成功");
            }
            catch (Exception e)
            {
                failureNum++;
                String plainTextContent = question.getQuestionContent().replaceAll("<[^>]*>", "");
                String contentPreview = StringUtils.substring(plainTextContent, 0, 15);
                String msg = "<br/>" + failureNum + "、题目 " + contentPreview + "... 导入失败：";
                failureMsg.append(msg).append(e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

    private void processQuestionByType(BizQuestion bizQuestion) {
        String questionType = bizQuestion.getQuestionType();

        if ("typing".equals(questionType)) {
            calculateWordCount(bizQuestion);
            bizQuestion.setFilePath(null);
            bizQuestion.setPreviewPath(null);
        } else if ("practical".equals(questionType)) {
            handlePracticalQuestionFile(bizQuestion);
            bizQuestion.setWordCount(null);
            bizQuestion.setTypingDuration(null);
        } else {
            bizQuestion.setWordCount(null);
            bizQuestion.setTypingDuration(null);
            bizQuestion.setFilePath(null);
            bizQuestion.setPreviewPath(null);
        }
    }

    /**
     * 处理操作题的文件转换 (使用LibreOffice进行高质量转换)
     */
    private void handlePracticalQuestionFile(BizQuestion bizQuestion) {
        if (StringUtils.isEmpty(bizQuestion.getFilePath())) {
            bizQuestion.setPreviewPath(null);
            return;
        }

        // 1. 获取URL相对路径
        String urlPath = bizQuestion.getFilePath();

        // 2. 将URL相对路径转换为文件系统相对路径
        // 例如，从 "/profile/upload/file.docx" 转换为 "/upload/file.docx"
        String fileSystemRelativePath = urlPath.replaceFirst(Constants.RESOURCE_PREFIX, "");

        // 3. 构造源文件的绝对路径和输出目录
        String originalFullPath = RuoYiConfig.getProfile() + fileSystemRelativePath;
        java.io.File originalFile = new java.io.File(originalFullPath);
        String outputDir = originalFile.getParent();

        // 4. 检查LibreOffice是否已安装
        if (!FileConversionUtils.isLibreOfficeInstalled()) {
            log.error("LibreOffice未安装或路径不正确，无法转换文件");
            bizQuestion.setPreviewPath(null);
            return;
        }

        // 5. 使用LibreOffice进行转换
        String pdfFullPath = FileConversionUtils.convertDocxToPdfWithLibreOffice(originalFullPath, outputDir);

        if (pdfFullPath != null) {
            // 6. 直接基于fileSystemRelativePath计算PDF的URL路径
            // 将 /upload/xxx.docx 转换为 /upload/xxx.pdf
            String pdfRelativePath = fileSystemRelativePath.replaceAll("(?i)\\.docx?$", ".pdf");
            String previewUrlPath = Constants.RESOURCE_PREFIX + pdfRelativePath;
            bizQuestion.setPreviewPath(previewUrlPath);
            log.info("LibreOffice转换成功: {} -> {}", originalFullPath, previewUrlPath);
        } else {
            log.error("LibreOffice转换失败: {}", originalFullPath);
            bizQuestion.setPreviewPath(null);
        }
    }

    private void calculateWordCount(BizQuestion bizQuestion) {
        if (StringUtils.isNotEmpty(bizQuestion.getQuestionContent())) {
            String plainText = bizQuestion.getQuestionContent().replaceAll("<[^>]*>", "");
            String denseText = plainText.replaceAll("\\s+", "");
            bizQuestion.setWordCount(denseText.length());
        } else {
            bizQuestion.setWordCount(0);
        }
    }
}
