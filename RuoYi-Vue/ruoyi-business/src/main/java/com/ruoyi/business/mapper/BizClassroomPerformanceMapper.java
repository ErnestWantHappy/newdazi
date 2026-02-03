package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizClassroomPerformance;
import org.apache.ibatis.annotations.Param;

/**
 * 课堂表现记录Mapper接口
 * 
 * @author ruoyi
 */
public interface BizClassroomPerformanceMapper 
{
    /**
     * 查询课堂表现记录
     */
    BizClassroomPerformance selectById(@Param("performanceId") Long performanceId);

    /**
     * 根据学生ID和课程ID查询
     */
    BizClassroomPerformance selectByStudentAndLesson(@Param("studentId") Long studentId, @Param("lessonId") Long lessonId);

    /**
     * 查询课程的班级课堂表现列表（带学生信息）
     */
    List<BizClassroomPerformance> selectListByLessonAndClass(
        @Param("lessonId") Long lessonId, 
        @Param("classCode") String classCode, 
        @Param("entryYear") String entryYear);

    /**
     * 新增课堂表现记录
     */
    int insert(BizClassroomPerformance performance);

    /**
     * 修改课堂表现记录
     */
    int update(BizClassroomPerformance performance);

    /**
     * 删除课堂表现记录
     */
    int deleteById(@Param("performanceId") Long performanceId);

    /**
     * 批量插入或更新（用于批量保存）
     */
    int insertOrUpdate(BizClassroomPerformance performance);

    /**
     * 查询学生的所有课程平时分记录
     */
    List<BizClassroomPerformance> selectByStudentId(@Param("studentId") Long studentId);
}
