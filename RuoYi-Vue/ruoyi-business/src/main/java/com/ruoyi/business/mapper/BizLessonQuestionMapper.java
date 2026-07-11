package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizLessonQuestion;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;

/**
 * 课程-题目关联Mapper接口
 * 
 * @author ruoyi
 * @date 2025-08-18
 */
public interface BizLessonQuestionMapper 
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
     * 删除课程-题目关联
     * 
     * @param id 课程-题目关联主键
     * @return 结果
     */
    public int deleteBizLessonQuestionById(Long id);

    /**
     * 批量删除课程-题目关联
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizLessonQuestionByIds(Long[] ids);

    /**
     * 根据课程ID删除课程与题目的关联信息
     * * @param lessonId 课程ID
     * @return 结果
     */
    public int deleteBizLessonQuestionByLessonId(Long lessonId);

    /**
     * 批量新增课程与题目的关联信息
     * * @param bizLessonQuestionList 关联列表
     * @return 结果
     */
    public int batchBizLessonQuestion(List<BizLessonQuestion> bizLessonQuestionList);

    /**
     * 根据课程ID删除所有关联题目
     * @param lessonId 课程ID
     */
    void deleteByLessonId(Long lessonId);

    /**
     * 批量新增课程题目关联
     * @param lessonQuestions 关联列表
     */
    void batchInsert(List<BizLessonQuestion> lessonQuestions);

    /**
     * 根据课程ID查询所有关联的题目信息
     * @param lessonId 课程ID
     * @return 课程题目关联列表
     */
    List<BizLessonQuestionDetailVo> selectDetailsByLessonId(Long lessonId);
}
