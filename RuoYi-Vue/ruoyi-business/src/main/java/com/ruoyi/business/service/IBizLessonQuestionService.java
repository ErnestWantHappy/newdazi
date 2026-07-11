package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.BizLessonQuestion;

/**
 * 课程-题目关联Service接口
 * 
 * @author ruoyi
 * @date 2025-08-18
 */
public interface IBizLessonQuestionService 
{
    /**
     * 查询课程-题目关联
     * 
     * @param id 课程-题目关联主键
     * @return 课程-题目关联
     */
    public BizLessonQuestion selectBizLessonQuestionById(Long id);

    /**
     * 查询课程-题目关联列表
     * 
     * @param bizLessonQuestion 课程-题目关联
     * @return 课程-题目关联集合
     */
    public List<BizLessonQuestion> selectBizLessonQuestionList(BizLessonQuestion bizLessonQuestion);

    /**
     * 新增课程-题目关联
     * 
     * @param bizLessonQuestion 课程-题目关联
     * @return 结果
     */
    public int insertBizLessonQuestion(BizLessonQuestion bizLessonQuestion);

    /**
     * 修改课程-题目关联
     * 
     * @param bizLessonQuestion 课程-题目关联
     * @return 结果
     */
    public int updateBizLessonQuestion(BizLessonQuestion bizLessonQuestion);

    /**
     * 批量删除课程-题目关联
     * 
     * @param ids 需要删除的课程-题目关联主键集合
     * @return 结果
     */
    public int deleteBizLessonQuestionByIds(Long[] ids);

    /**
     * 删除课程-题目关联信息
     * 
     * @param id 课程-题目关联主键
     * @return 结果
     */
    public int deleteBizLessonQuestionById(Long id);
}
