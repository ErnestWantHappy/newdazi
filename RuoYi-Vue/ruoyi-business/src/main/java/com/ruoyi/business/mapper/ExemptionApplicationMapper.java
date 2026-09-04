package com.ruoyi.business.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 教师免抽测申请数据访问。
 */
public interface ExemptionApplicationMapper
{
    Map<String, Object> selectTeacherIdentity(@Param("teacherId") Long teacherId,
                                              @Param("deptId") Long deptId);

    Integer selectRequiredLessonCount(@Param("academicYear") String academicYear,
                                      @Param("semester") String semester,
                                      @Param("grade") Integer grade);

    List<Map<String, Object>> selectStandards(@Param("academicYear") String academicYear,
                                              @Param("semester") String semester);

    int upsertStandard(@Param("academicYear") String academicYear,
                       @Param("semester") String semester,
                       @Param("grade") Integer grade,
                       @Param("requiredLessonCount") Integer requiredLessonCount,
                       @Param("operator") String operator);

    List<Map<String, Object>> selectTeacherClasses(@Param("teacherId") Long teacherId,
                                                   @Param("deptId") Long deptId,
                                                   @Param("entryYear") String entryYear);

    List<Map<String, Object>> selectTeacherCourseMetrics(@Param("teacherId") Long teacherId,
                                                         @Param("deptId") Long deptId,
                                                         @Param("entryYear") String entryYear,
                                                         @Param("startTime") Date startTime,
                                                         @Param("endTime") Date endTime);

    int countApplication(@Param("deptId") Long deptId,
                         @Param("teacherId") Long teacherId,
                         @Param("academicYear") String academicYear,
                         @Param("semester") String semester,
                         @Param("grade") Integer grade);

    int insertApplication(Map<String, Object> application);

    int insertClassSnapshot(Map<String, Object> classSnapshot);

    int insertCourseSnapshots(@Param("items") List<Map<String, Object>> items);

    int insertAttachment(Map<String, Object> attachment);

    List<Map<String, Object>> selectMyApplications(@Param("teacherId") Long teacherId,
                                                   @Param("deptId") Long deptId);

    List<Map<String, Object>> selectReviewApplications(@Param("q") Map<String, Object> query);

    Map<String, Object> selectApplicationById(@Param("applicationId") Long applicationId);

    List<Map<String, Object>> selectClassSnapshots(@Param("applicationId") Long applicationId);

    List<Map<String, Object>> selectCourseSnapshots(@Param("applicationId") Long applicationId);

    List<Map<String, Object>> selectAttachments(@Param("applicationId") Long applicationId);

    int reviewApplication(@Param("applicationId") Long applicationId,
                          @Param("status") String status,
                          @Param("reviewRemark") String reviewRemark,
                          @Param("reviewerId") Long reviewerId,
                          @Param("reviewerName") String reviewerName,
                          @Param("operator") String operator,
                          @Param("version") Integer version);
}
