package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.domain.BizLessonQuestion;
import com.ruoyi.business.service.IBizLessonQuestionService;

/**
 * 课程-题目关联Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-08-18
 */
@Service
public class BizLessonQuestionServiceImpl implements IBizLessonQuestionService 
{
    @Autowired
    private BizLessonQuestionMapper bizLessonQuestionMapper;

    /**
     * 查询课程-题目关联
     * 
     * @param id 课程-题目关联主键
     * @return 课程-题目关联
     */
    @Override
    public BizLessonQuestion selectBizLessonQuestionById(Long id)
    {
        return bizLessonQuestionMapper.selectBizLessonQuestionById(id);
    }

    /**
     * 查询课程-题目关联列表
     * 
     * @param bizLessonQuestion 课程-题目关联
     * @return 课程-题目关联
     */
    @Override
    public List<BizLessonQuestion> selectBizLessonQuestionList(BizLessonQuestion bizLessonQuestion)
    {
        return bizLessonQuestionMapper.selectBizLessonQuestionList(bizLessonQuestion);
    }

    /**
     * 新增课程-题目关联
     * 
     * @param bizLessonQuestion 课程-题目关联
     * @return 结果
     */
    @Override
    public int insertBizLessonQuestion(BizLessonQuestion bizLessonQuestion)
    {
        return bizLessonQuestionMapper.insertBizLessonQuestion(bizLessonQuestion);
    }

    /**
     * 修改课程-题目关联
     * 
     * @param bizLessonQuestion 课程-题目关联
     * @return 结果
     */
    @Override
    public int updateBizLessonQuestion(BizLessonQuestion bizLessonQuestion)
    {
        return bizLessonQuestionMapper.updateBizLessonQuestion(bizLessonQuestion);
    }

    /**
     * 批量删除课程-题目关联
     * 
     * @param ids 需要删除的课程-题目关联主键
     * @return 结果
     */
    @Override
    public int deleteBizLessonQuestionByIds(Long[] ids)
    {
        return bizLessonQuestionMapper.deleteBizLessonQuestionByIds(ids);
    }

    /**
     * 删除课程-题目关联信息
     * 
     * @param id 课程-题目关联主键
     * @return 结果
     */
    @Override
    public int deleteBizLessonQuestionById(Long id)
    {
        return bizLessonQuestionMapper.deleteBizLessonQuestionById(id);
    }
}
