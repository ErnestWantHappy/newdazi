package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.BizLesson;

/**
 * 课程/主题Service接口
 * 
 * @author ruoyi
 * @date 2025-06-23
 */
public interface IBizLessonService 
{
    /**
     * 查询课程/主题
     * 
     * @param lessonId 课程/主题主键
     * @return 课程/主题
     */
    public BizLesson selectBizLessonByLessonId(Long lessonId);

    /**
     * 查询课程/主题列表
     * 
     * @param bizLesson 课程/主题
     * @return 课程/主题集合
     */
    public List<BizLesson> selectBizLessonList(BizLesson bizLesson);

    /**
     * 新增课程/主题
     * 
     * @param bizLesson 课程/主题
     * @return 结果
     */
    public int insertBizLesson(BizLesson bizLesson);

    /**
     * 修改课程/主题
     * 
     * @param bizLesson 课程/主题
     * @return 结果
     */
    public int updateBizLesson(BizLesson bizLesson);

    /**
     * 批量删除课程/主题
     * 
     * @param lessonIds 需要删除的课程/主题主键集合
     * @return 结果
     */
    public int deleteBizLessonByLessonIds(Long[] lessonIds);

    /**
     * 删除课程/主题信息
     * 
     * @param lessonId 课程/主题主键
     * @return 结果
     */
    public int deleteBizLessonByLessonId(Long lessonId);
}
