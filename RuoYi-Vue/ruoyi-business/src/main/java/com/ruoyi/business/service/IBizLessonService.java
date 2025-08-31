package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.vo.GradeGroupVo;
import com.ruoyi.business.domain.vo.LessonDetailVo;

/**
 * 课程/作业信息Service接口
 * 
 * @author ruoyi
 * @date 2025-08-14
 */
public interface IBizLessonService 
{
    /**
     * 查询课程/作业信息
     * 
     * @param lessonId 课程/作业信息主键
     * @return 课程/作业信息
     */
    public BizLesson selectBizLessonByLessonId(Long lessonId);

    /**
     * 查询课程/作业信息列表
     * 
     * @param bizLesson 课程/作业信息
     * @return 课程/作业信息集合
     */
    public List<BizLesson> selectBizLessonList(BizLesson bizLesson);

    /**
     * 新增课程/作业信息
     * 
     * @param bizLesson 课程/作业信息
     * @return 结果
     */
    public int insertBizLesson(BizLesson bizLesson);

    /**
     * 修改课程/作业信息
     * 
     * @param bizLesson 课程/作业信息
     * @return 结果
     */
    public int updateBizLesson(BizLesson bizLesson);

    /**
     * 批量删除课程/作业信息
     * 
     * @param lessonIds 需要删除的课程/作业信息主键集合
     * @return 结果
     */
    public int deleteBizLessonByLessonIds(Long[] lessonIds);

    /**
     * 删除课程/作业信息信息
     * 
     * @param lessonId 课程/作业信息主键
     * @return 结果
     */
    public int deleteBizLessonByLessonId(Long lessonId);

    /**
     * 获取教师首页的完整数据
     * @return 教师首页视图对象列表
     */
    public List<GradeGroupVo> getTeacherDashboardData();

    /**
     * 获取课程完整详情，包括题目列表和已指派班级
     * @param lessonId 课程ID
     * @return 课程完整详情视图对象
     */
    public LessonDetailVo selectLessonDetailsByLessonId(Long lessonId);

    public LessonDetailVo saveLessonDetails(LessonDetailVo lessonDetailVo);
}
