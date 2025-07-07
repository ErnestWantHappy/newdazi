package com.ruoyi.business.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.service.IBizLessonService;

/**
 * 课程/主题Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-06-23
 */
@Service
public class BizLessonServiceImpl implements IBizLessonService 
{
    @Autowired
    private BizLessonMapper bizLessonMapper;

    /**
     * 查询课程/主题
     * 
     * @param lessonId 课程/主题主键
     * @return 课程/主题
     */
    @Override
    public BizLesson selectBizLessonByLessonId(Long lessonId)
    {
        return bizLessonMapper.selectBizLessonByLessonId(lessonId);
    }

    /**
     * 查询课程/主题列表
     * 
     * @param bizLesson 课程/主题
     * @return 课程/主题
     */
    @Override
    public List<BizLesson> selectBizLessonList(BizLesson bizLesson)
    {
        return bizLessonMapper.selectBizLessonList(bizLesson);
    }

    /**
     * 新增课程/主题
     * 
     * @param bizLesson 课程/主题
     * @return 结果
     */
    @Override
    public int insertBizLesson(BizLesson bizLesson)
    {
        bizLesson.setCreateTime(DateUtils.getNowDate());
        return bizLessonMapper.insertBizLesson(bizLesson);
    }

    /**
     * 修改课程/主题
     * 
     * @param bizLesson 课程/主题
     * @return 结果
     */
    @Override
    public int updateBizLesson(BizLesson bizLesson)
    {
        return bizLessonMapper.updateBizLesson(bizLesson);
    }

    /**
     * 批量删除课程/主题
     * 
     * @param lessonIds 需要删除的课程/主题主键
     * @return 结果
     */
    @Override
    public int deleteBizLessonByLessonIds(Long[] lessonIds)
    {
        return bizLessonMapper.deleteBizLessonByLessonIds(lessonIds);
    }

    /**
     * 删除课程/主题信息
     * 
     * @param lessonId 课程/主题主键
     * @return 结果
     */
    @Override
    public int deleteBizLessonByLessonId(Long lessonId)
    {
        return bizLessonMapper.deleteBizLessonByLessonId(lessonId);
    }
}
