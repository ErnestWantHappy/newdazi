package com.ruoyi.business.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.business.domain.vo.StudentProfileVo;

/**
 * 学生成绩画像 Service 接口
 */
public interface IStudentProfileService {

    /**
     * 获取学生画像汇总数据
     */
    StudentProfileVo getStudentProfile(Long studentId, String semesterStart, String semesterEnd);

    /**
     * 获取历次课程成绩（含班级平均对比）
     */
    List<StudentProfileVo.CourseScoreItem> getCourseScores(Long studentId, String semesterStart, String semesterEnd);

    /**
     * 获取打字速度数据
     */
    List<StudentProfileVo.TypingSpeedItem> getTypingSpeeds(Long studentId, String semesterStart, String semesterEnd);

    /**
     * 获取课堂表现分数据
     */
    List<StudentProfileVo.PerformanceItem> getPerformances(Long studentId, String semesterStart, String semesterEnd);

    /**
     * 获取排名变化数据
     */
    List<StudentProfileVo.RankItem> getRankChanges(Long studentId, String semesterStart, String semesterEnd);

    /**
     * 获取班级列表
     */
    List<Map<String, Object>> getClassList();

    /**
     * 获取指定班级的学生列表
     */
    List<com.ruoyi.business.domain.BizStudent> getStudentList(String entryYear, String classCode);
}
