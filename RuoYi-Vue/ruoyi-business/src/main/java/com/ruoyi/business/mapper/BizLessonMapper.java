package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.vo.LessonInfoVo; // 确保导入
import org.apache.ibatis.annotations.Param;
/**
 * 课程/作业信息Mapper接口
 * 
 * @author ruoyi
 * @date 2025-08-14
 */
public interface BizLessonMapper 
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
     * 删除课程/作业信息
     * 
     * @param lessonId 课程/作业信息主键
     * @return 结果
     */
    public int deleteBizLessonByLessonId(Long lessonId);

    /**
     * 批量删除课程/作业信息
     * 
     * @param lessonIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizLessonByLessonIds(Long[] lessonIds);

    /**
     * 根据年级和创建者查询简化课程信息
     * @param grade 年级
     * @param creatorName 创建者用户名
     * @return 简化课程信息列表
     */
    List<LessonInfoVo> selectLessonsByGradeAndCreator(@Param("grade") Long grade, @Param("creatorName") String creatorName);
}
