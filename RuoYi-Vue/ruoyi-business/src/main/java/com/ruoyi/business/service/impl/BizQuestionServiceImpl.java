package com.ruoyi.business.service.impl;

import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.mapper.BizQuestionMapper;
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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

    @Override
    public BizQuestion selectBizQuestionByQuestionId(Long questionId) {
        return bizQuestionMapper.selectBizQuestionByQuestionId(questionId);
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

        return bizQuestionMapper.insertBizQuestion(bizQuestion);
    }

    @Override
    public int updateBizQuestion(BizQuestion bizQuestion)
    {
        bizQuestion.setUpdateTime(DateUtils.getNowDate());
        bizQuestion.setUpdateBy(SecurityUtils.getUsername());

        processQuestionByType(bizQuestion);

        return bizQuestionMapper.updateBizQuestion(bizQuestion);
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
     * 处理操作题的文件转换 (核心修复)
     */
    private void handlePracticalQuestionFile(BizQuestion bizQuestion) {
        if (StringUtils.isEmpty(bizQuestion.getFilePath())) {
            bizQuestion.setPreviewPath(null);
            return;
        }

        // 1. 获取URL相对路径
        String urlPath = bizQuestion.getFilePath();

        // 2. 核心修复：将URL相对路径转换为文件系统相对路径
        // 例如，从 "/profile/upload/file.docx" 转换为 "/upload/file.docx"
        String fileSystemRelativePath = urlPath.replaceFirst(Constants.RESOURCE_PREFIX, "");

        // 3. 构造PDF预览文件的相对路径和绝对路径
        String previewFileSystemRelativePath = fileSystemRelativePath.toLowerCase().endsWith(".docx")
                ? fileSystemRelativePath.substring(0, fileSystemRelativePath.length() - 5) + ".pdf"
                : fileSystemRelativePath + ".pdf";

        String originalFullPath = RuoYiConfig.getProfile() + fileSystemRelativePath;
        String previewFullPath = RuoYiConfig.getProfile() + previewFileSystemRelativePath;

        // 4. 创建目录（如果不存在）
        java.io.File previewFile = new java.io.File(previewFullPath);
        previewFile.getParentFile().mkdirs();

        // 5. 执行转换
        try (InputStream in = new FileInputStream(originalFullPath);
             OutputStream out = new FileOutputStream(previewFullPath)) {

            FileConversionUtils.convertDocxToPdf(in, out);
            // 6. 存储URL相对路径到数据库
            String previewUrlPath = Constants.RESOURCE_PREFIX + previewFileSystemRelativePath;
            bizQuestion.setPreviewPath(previewUrlPath);
            log.info("Successfully converted {} to {}", originalFullPath, previewFullPath);

        } catch (Exception e) {
            log.error("Failed to convert docx to pdf for file: {}", originalFullPath, e);
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
