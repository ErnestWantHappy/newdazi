package com.ruoyi.business.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface ClassGroupingMapper {
    List<Map<String,Object>> selectSchemes(@Param("deptId") Long deptId, @Param("entryYear") String entryYear, @Param("classCode") String classCode);
    Map<String,Object> selectScheme(@Param("schemeId") Long schemeId);
    List<Map<String,Object>> selectGroups(@Param("schemeId") Long schemeId);
    List<Map<String,Object>> selectMembers(@Param("schemeId") Long schemeId);
    int countManagedClass(@Param("userId") Long userId, @Param("deptId") Long deptId, @Param("entryYear") String entryYear, @Param("classCode") String classCode);
    List<Long> selectManagedClassDeptIds(@Param("userId") Long userId, @Param("entryYear") String entryYear, @Param("classCode") String classCode);
    int countLessonAssignment(@Param("lessonId") Long lessonId, @Param("deptId") Long deptId, @Param("entryYear") String entryYear, @Param("classCode") String classCode);
    List<Map<String,Object>> selectClassStudents(@Param("deptId") Long deptId, @Param("entryYear") String entryYear, @Param("classCode") String classCode);
    int selectNextSchemeVersion(@Param("deptId") Long deptId, @Param("entryYear") String entryYear, @Param("classCode") String classCode, @Param("schemeName") String schemeName);
    int insertScheme(Map<String,Object> row);
    int insertGroup(Map<String,Object> row);
    int insertMember(Map<String,Object> row);
    int deleteMembers(@Param("schemeId") Long schemeId);
    int deleteGroups(@Param("schemeId") Long schemeId);
    int deleteScheme(@Param("schemeId") Long schemeId);
    int insertSnapshot(Map<String,Object> row);
    Map<String,Object> selectSnapshot(@Param("lessonId") Long lessonId, @Param("deptId") Long deptId, @Param("entryYear") String entryYear, @Param("classCode") String classCode);
    int insertSnapshotGroup(Map<String,Object> row);
    int insertSnapshotMember(Map<String,Object> row);
    List<Map<String,Object>> selectDesktopStudents(@Param("deptId") Long deptId, @Param("entryYear") String entryYear, @Param("classCode") String classCode, @Param("layoutId") Long layoutId);
    /** 当前课程中每名学生的作答汇总，只统计仍属于课程的题目。 */
    List<Map<String,Object>> selectDesktopAnswerOverview(@Param("lessonId") Long lessonId,
                                                          @Param("studentIds") List<Long> studentIds);
    Map<String,Object> selectLayout(@Param("teacherUserId") Long teacherUserId, @Param("deptId") Long deptId, @Param("entryYear") String entryYear, @Param("classCode") String classCode);
    List<Map<String,Object>> selectLayoutItems(@Param("layoutId") Long layoutId);
    int insertLayout(Map<String,Object> row);
    int updateLayout(Map<String,Object> row);
    int deleteLayoutItems(@Param("layoutId") Long layoutId);
    int insertLayoutItem(Map<String,Object> row);
}
