package com.ruoyi.business.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.vo.StudentProfileVo;

/**
 * 学生成绩画像 Mapper
 */
public interface StudentProfileMapper {

    /**
     * 查询学生基础信息
     */
    Map<String, Object> selectStudentBasicInfo(@Param("studentId") Long studentId);

    /**
     * 查询学生历次课程成绩
     */
    List<StudentProfileVo.CourseScoreItem> selectCourseScores(
        @Param("studentId") Long studentId,
        @Param("semesterStart") String semesterStart,
        @Param("semesterEnd") String semesterEnd
    );

    /**
     * 查询班级各课程平均分
     */
    List<Map<String, Object>> selectClassAvgScores(
        @Param("entryYear") String entryYear,
        @Param("classCode") String classCode,
        @Param("deptId") Long deptId,
        @Param("semesterStart") String semesterStart,
        @Param("semesterEnd") String semesterEnd
    );

    /**
     * 查询学生打字速度记录
     */
    List<StudentProfileVo.TypingSpeedItem> selectTypingSpeeds(
        @Param("studentId") Long studentId,
        @Param("semesterStart") String semesterStart,
        @Param("semesterEnd") String semesterEnd
    );

    /**
     * 查询学生课堂表现分记录
     */
    List<StudentProfileVo.PerformanceItem> selectPerformances(
        @Param("studentId") Long studentId,
        @Param("semesterStart") String semesterStart,
        @Param("semesterEnd") String semesterEnd
    );

    /**
     * 查询班级所有学生的历次课程总分（用于计算排名）
     */
    List<Map<String, Object>> selectClassStudentScores(
        @Param("entryYear") String entryYear,
        @Param("classCode") String classCode,
        @Param("deptId") Long deptId,
        @Param("semesterStart") String semesterStart,
        @Param("semesterEnd") String semesterEnd
    );

    /**
     * 查询班级学生总数
     */
    Integer selectClassStudentCount(
        @Param("entryYear") String entryYear,
        @Param("classCode") String classCode,
        @Param("deptId") Long deptId
    );
}
