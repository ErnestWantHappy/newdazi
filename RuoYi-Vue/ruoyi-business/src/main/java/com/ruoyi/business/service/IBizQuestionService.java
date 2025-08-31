package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.BizQuestion;

/**
 * 题库管理Service接口
 * 
 * @author zdx
 * @date 2025-08-18
 */
public interface IBizQuestionService 
{
    /**
     * 查询题库管理
     * 
     * @param questionId 题库管理主键
     * @return 题库管理
     */
    public BizQuestion selectBizQuestionByQuestionId(Long questionId);

    /**
     * 查询题库管理列表
     * 
     * @param bizQuestion 题库管理
     * @return 题库管理集合
     */
    public List<BizQuestion> selectBizQuestionList(BizQuestion bizQuestion);

    /**
     * 新增题库管理
     * 
     * @param bizQuestion 题库管理
     * @return 结果
     */
    public int insertBizQuestion(BizQuestion bizQuestion);

    /**
     * 修改题库管理
     * 
     * @param bizQuestion 题库管理
     * @return 结果
     */
    public int updateBizQuestion(BizQuestion bizQuestion);

    /**
     * 批量删除题库管理
     * 
     * @param questionIds 需要删除的题库管理主键集合
     * @return 结果
     */
    public int deleteBizQuestionByQuestionIds(Long[] questionIds);

    /**
     * 删除题库管理信息
     * 
     * @param questionId 题库管理主键
     * @return 结果
     */
    public int deleteBizQuestionByQuestionId(Long questionId);

    /**
     * 批量导入题目数据
     *
     * @param questionList 题目数据列表
     * @param operName 操作用户
     * @return 结果
     */
    public String importQuestion(List<BizQuestion> questionList, String operName);
}
