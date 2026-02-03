package com.ruoyi.business.service.impl;

import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.domain.BizScoringItem; // P6 import
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.business.mapper.BizScoringItemMapper; // P6 import
import com.ruoyi.business.service.AsyncConversionService;
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

    @Autowired
    private AsyncConversionService asyncConversionService;

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
        // 权限过滤：非管理员只能看到公共题目和自己的私有题目
        if (!SecurityUtils.isAdmin(SecurityUtils.getUserId())) {
            bizQuestion.setCreatorId(SecurityUtils.getUserId());
        }
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
        
        // 操作题异步转换
        triggerAsyncConversionIfNeeded(bizQuestion);
        
        return rows;
    }

    @Override
    public int updateBizQuestion(BizQuestion bizQuestion)
    {
        // 权限校验：非管理员只能编辑自己创建的题目
        Long currentUserId = SecurityUtils.getUserId();
        boolean isAdmin = SecurityUtils.isAdmin(currentUserId);
        
        if (!isAdmin) {
            BizQuestion existing = bizQuestionMapper.selectBizQuestionByQuestionId(bizQuestion.getQuestionId());
            if (existing != null && !currentUserId.equals(existing.getCreatorId())) {
                // 返回-1表示无权限，由Controller层处理
                return -1;
            }
        }
        
        bizQuestion.setUpdateTime(DateUtils.getNowDate());
        bizQuestion.setUpdateBy(SecurityUtils.getUsername());

        processQuestionByType(bizQuestion);

        int rows = bizQuestionMapper.updateBizQuestion(bizQuestion);
        
        // P6: 更新评分项 (先删后增)
        bizScoringItemMapper.deleteBizScoringItemByQuestion(bizQuestion.getQuestionId());
        insertScoringItems(bizQuestion);
        
        // 操作题异步转换
        triggerAsyncConversionIfNeeded(bizQuestion);
        
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
        // 权限校验：非管理员只能删除自己创建的题目
        Long currentUserId = SecurityUtils.getUserId();
        boolean isAdmin = SecurityUtils.isAdmin(currentUserId);
        
        if (!isAdmin) {
            for (Long questionId : questionIds) {
                BizQuestion question = bizQuestionMapper.selectBizQuestionByQuestionId(questionId);
                if (question != null && !currentUserId.equals(question.getCreatorId())) {
                    // 返回-1表示无权限，由Controller层处理
                    return -1;
                }
            }
        }
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
                // P6.1: 数据规范化 (导入时处理)
                if ("judgment".equals(question.getQuestionType())) {
                    String ans = question.getAnswer();
                    if (StringUtils.isNotEmpty(ans)) {
                         if ("正确".equals(ans) || "对".equals(ans) || "1".equals(ans) || "T".equalsIgnoreCase(ans)) {
                             question.setAnswer("T");
                         } else if ("错误".equals(ans) || "错".equals(ans) || "0".equals(ans) || "F".equalsIgnoreCase(ans)) {
                             question.setAnswer("F");
                         }
                    }
                } else if ("choice".equals(question.getQuestionType())) {
                    if (StringUtils.isNotEmpty(question.getAnswer())) {
                        question.setAnswer(question.getAnswer().toUpperCase());
                    }
                }
                
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
            // 自动计算打字时长：字数 ÷ 基准速度（小学20，初中及以上40）
            if (bizQuestion.getWordCount() != null && bizQuestion.getWordCount() > 0) {
                int baseSpeed = (bizQuestion.getGrade() != null && bizQuestion.getGrade() <= 6) ? 20 : 40;
                int duration = (int) Math.ceil((double) bizQuestion.getWordCount() / baseSpeed);
                bizQuestion.setTypingDuration(duration);
            }
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
     * 处理操作题的文件转换 (异步模式)
     */
    private void handlePracticalQuestionFile(BizQuestion bizQuestion) {
        if (StringUtils.isEmpty(bizQuestion.getFilePath())) {
            bizQuestion.setPreviewPath(null);
            bizQuestion.setPreviewStatus(null);
            return;
        }

        // 设置为待转换状态，稍后在 insert 之后触发异步转换
        bizQuestion.setPreviewStatus("pending");
        
        // 预先计算 previewPath（PDF URL路径）
        String urlPath = bizQuestion.getFilePath();
        String fileSystemRelativePath = urlPath.replaceFirst(Constants.RESOURCE_PREFIX, "");
        String pdfRelativePath = fileSystemRelativePath.replaceAll("(?i)\\.docx?$", ".pdf");
        String previewUrlPath = Constants.RESOURCE_PREFIX + pdfRelativePath;
        bizQuestion.setPreviewPath(previewUrlPath);
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

    /**
     * 触发操作题异步转换（如果需要）
     */
    private void triggerAsyncConversionIfNeeded(BizQuestion bizQuestion) {
        if ("practical".equals(bizQuestion.getQuestionType()) 
            && "pending".equals(bizQuestion.getPreviewStatus())
            && bizQuestion.getQuestionId() != null) {
            
            String urlPath = bizQuestion.getFilePath();
            String fileSystemRelativePath = urlPath.replaceFirst(Constants.RESOURCE_PREFIX, "");
            String originalFullPath = RuoYiConfig.getProfile() + fileSystemRelativePath;
            String outputDir = new java.io.File(originalFullPath).getParent();
            
            asyncConversionService.convertQuestionAsync(
                bizQuestion.getQuestionId(), 
                originalFullPath, 
                outputDir, 
                bizQuestion.getPreviewPath()
            );
            log.info("【题库】已触发异步转换 questionId={}", bizQuestion.getQuestionId());
        }
    }
}
