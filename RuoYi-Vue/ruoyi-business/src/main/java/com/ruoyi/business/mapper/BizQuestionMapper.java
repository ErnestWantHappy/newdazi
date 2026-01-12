package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizQuestion;

/**
 * 题库管理Mapper接口
 * 
 * @author zdx
 * @date 2025-08-18
 */
public interface BizQuestionMapper 
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
     * 删除题库管理
     * 
     * @param questionId 题库管理主键
     * @return 结果
     */
    public int deleteBizQuestionByQuestionId(Long questionId);

    /**
     * 批量删除题库管理
     * 
     * @param questionIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizQuestionByQuestionIds(Long[] questionIds);

    /**
     * 更新题目预览状态
     */
    public int updatePreviewStatus(@org.apache.ibatis.annotations.Param("questionId") Long questionId, 
                                   @org.apache.ibatis.annotations.Param("previewStatus") String previewStatus,
                                   @org.apache.ibatis.annotations.Param("previewPath") String previewPath);
}
