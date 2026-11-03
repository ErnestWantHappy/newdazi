package com.ruoyi.business.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/** 拓展班使用独立成员关系，行政班与学号保持原样。 */
public interface BizExtensionClassMapper {
    List<Map<String,Object>> classes(@Param("deptId") Long deptId, @Param("ownerId") Long ownerId);
    Map<String,Object> find(@Param("id") Long id);
    Map<String,Object> lock(@Param("id") Long id);
    Long lockSchool(@Param("deptId") Long deptId);
    int occupied(@Param("deptId") Long deptId, @Param("year") String year, @Param("code") String code);
    int teacher(@Param("deptId") Long deptId, @Param("userId") Long userId);
    List<Map<String,Object>> teachers(@Param("deptId") Long deptId);
    int insert(Map<String,Object> row);
    int rename(Map<String,Object> row);
    int archive(@Param("id") Long id);
    int clearMembers(@Param("id") Long id);
    List<Map<String,Object>> members(@Param("id") Long id);
    List<Map<String,Object>> candidates(@Param("deptId") Long deptId, @Param("keyword") String keyword);
    Map<String,Object> lockStudent(@Param("studentId") Long studentId);
    Map<String,Object> membership(@Param("studentId") Long studentId);
    Map<String,Object> lockMembership(@Param("studentId") Long studentId);
    Map<String,Object> carrier(@Param("deptId") Long deptId, @Param("year") String year, @Param("code") String code);
    int addMember(Map<String,Object> row);
    int removeMember(@Param("id") Long id, @Param("studentId") Long studentId);
    int clearInactiveMembers();
    int saveSpace(@Param("studentId") Long studentId, @Param("space") String space);
    Map<String,Object> preference(@Param("studentId") Long studentId);
    int resetSpace(@Param("studentId") Long studentId);
    int insertLesson(Map<String,Object> row);
    int snapshot(@Param("id") Long id, @Param("lessonId") Long lessonId);
    int assign(Map<String,Object> row);
    Map<String,Object> currentLesson(@Param("id") Long id);
    Map<String,Object> currentLessonForUpdate(@Param("id") Long id);
    Map<String,Object> lesson(@Param("lessonId") Long lessonId);
    int inSnapshot(@Param("lessonId") Long lessonId, @Param("studentId") Long studentId);
    List<Map<String,Object>> roster(@Param("lessonId") Long lessonId);
    List<Map<String,Object>> history(@Param("studentId") Long studentId, @Param("id") Long id);
    List<Map<String,Object>> lessons(@Param("id") Long id);
    List<Map<String,Object>> plans(@Param("id") Long id);
    int setPlanScope(@Param("planId") Long planId, @Param("scope") String scope, @Param("id") Long id);
    int snapshotPlan(@Param("id") Long id, @Param("versionId") Long versionId);
    int joinPublishedPlans(@Param("id") Long id, @Param("studentId") Long studentId);
    int deletePlanMembers(@Param("planId") Long planId);
}
