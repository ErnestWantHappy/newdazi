package com.ruoyi.business.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/** Python 刷题独立业务域数据访问。 */
public interface PythonPracticeMapper {
    List<Map<String, Object>> selectPlans(@Param("deptId") Long deptId,
                                          @Param("entryYear") String entryYear);
    Map<String, Object> selectPlan(@Param("planId") Long planId);
    Map<String, Object> selectPlanByVersion(@Param("planVersionId") Long planVersionId);
    Map<String, Object> selectLatestDraftVersion(@Param("planId") Long planId);
    Integer selectMaxVersionNo(@Param("planId") Long planId);
    List<Map<String, Object>> selectPlanQuestions(@Param("planVersionId") Long planVersionId);
    List<Map<String, Object>> selectPlanClasses(@Param("planVersionId") Long planVersionId);
    List<Map<String, Object>> selectManagedClasses(@Param("userId") Long userId,
                                                   @Param("deptId") Long deptId,
                                                   @Param("privileged") boolean privileged);
    List<Map<String, Object>> selectRecommendedQuestions(@Param("planVersionId") Long planVersionId);
    List<Map<String, Object>> selectStudentQuestions(@Param("deptId") Long deptId,
                                                     @Param("entryYear") String entryYear,
                                                     @Param("classCode") String classCode);
    Map<String, Object> selectQuestion(@Param("sourceType") String sourceType,
                                       @Param("sourceId") Long sourceId,
                                       @Param("questionId") Long questionId);
    Map<String, Object> selectDraft(@Param("studentId") Long studentId,
                                    @Param("sourceType") String sourceType,
                                    @Param("sourceId") Long sourceId,
                                    @Param("questionId") Long questionId);
    int upsertDraft(Map<String, Object> params);
    List<Map<String, Object>> selectSubmissions(@Param("studentId") Long studentId,
                                                @Param("sourceType") String sourceType,
                                                @Param("sourceId") Long sourceId,
                                                @Param("questionId") Long questionId);
    Map<String, Object> selectSubmission(@Param("submissionId") Long submissionId);
    List<Map<String, Object>> selectSubmissionCases(@Param("submissionId") Long submissionId);
    List<Map<String, Object>> selectSnapshotCases(@Param("snapshotId") Long snapshotId,
                                                  @Param("publicOnly") boolean publicOnly);
    int markSubmissionJudging(@Param("submissionId") Long submissionId);
    int updateSubmissionResult(Map<String, Object> params);
    int insertSubmission(Map<String, Object> params);
    int insertSubmissionCase(Map<String, Object> params);

    int insertPlan(Map<String, Object> params);
    int updatePlanName(@Param("planId") Long planId,
                       @Param("planName") String planName,
                       @Param("updateBy") String updateBy);
    int insertPlanVersion(Map<String, Object> params);
    int clonePlanClasses(@Param("fromVersionId") Long fromVersionId,
                         @Param("toVersionId") Long toVersionId,
                         @Param("createBy") String createBy);
    int cloneSnapshots(@Param("fromVersionId") Long fromVersionId,
                       @Param("toVersionId") Long toVersionId,
                       @Param("createBy") String createBy);
    int cloneSnapshotCases(@Param("fromVersionId") Long fromVersionId,
                           @Param("toVersionId") Long toVersionId);
    int clonePlanQuestions(@Param("fromVersionId") Long fromVersionId,
                           @Param("toVersionId") Long toVersionId);
    int insertPlanClass(Map<String, Object> params);
    int deletePlanClasses(@Param("planVersionId") Long planVersionId);
    int countPlanClasses(@Param("planVersionId") Long planVersionId);

    int insertSnapshot(Map<String, Object> params);
    int insertSnapshotCase(Map<String, Object> params);
    int insertPlanQuestion(Map<String, Object> params);
    int deletePlanQuestion(@Param("planVersionId") Long planVersionId,
                           @Param("questionId") Long questionId);
    int countPlanVersionQuestion(@Param("planVersionId") Long planVersionId,
                                 @Param("questionId") Long questionId);
    int offsetPlanQuestionSort(@Param("planVersionId") Long planVersionId);
    int updatePlanQuestionSort(@Param("planVersionId") Long planVersionId,
                               @Param("questionId") Long questionId,
                               @Param("sortNo") Integer sortNo);

    int publishVersion(@Param("planId") Long planId, @Param("planVersionId") Long planVersionId);
    int retractPublishedVersions(@Param("planId") Long planId, @Param("exceptVersionId") Long exceptVersionId);
    int selectPlanQuestionCount(@Param("planVersionId") Long planVersionId);
    int selectSnapshotPublicCaseCount(@Param("planVersionId") Long planVersionId,
                                      @Param("publicOnly") boolean publicOnly);
    int updateCurrentVersion(@Param("planId") Long planId, @Param("versionNo") Integer versionNo);
    Integer selectVersionNo(@Param("planVersionId") Long planVersionId);

    int deletePlanDrafts(@Param("planId") Long planId);
    int deletePlanSubmissionCases(@Param("planId") Long planId);
    int deletePlanSubmissions(@Param("planId") Long planId);
    int deletePlanProgress(@Param("planId") Long planId);
    int deleteAllPlanClasses(@Param("planId") Long planId);
    int deletePlanSnapshotCases(@Param("planId") Long planId);
    int deletePlanSnapshots(@Param("planId") Long planId);
    int deletePlanQuestions(@Param("planId") Long planId);
    int deletePlanVersions(@Param("planId") Long planId);
    int deleteExtensionQuestions(@Param("planId") Long planId);
    int deleteExtensionClasses(@Param("planId") Long planId);
    int deleteExtensions(@Param("planId") Long planId);
    int deletePlan(@Param("planId") Long planId);

    List<Map<String, Object>> selectPlanAnalyticsStudents(@Param("planVersionId") Long planVersionId,
                                                          @Param("deptId") Long deptId,
                                                          @Param("entryYear") String entryYear,
                                                          @Param("classCode") String classCode);
    List<Map<String, Object>> selectPlanAnalyticsQuestions(@Param("planVersionId") Long planVersionId,
                                                           @Param("deptId") Long deptId,
                                                           @Param("entryYear") String entryYear,
                                                           @Param("classCode") String classCode);
    int countPlanClass(@Param("planVersionId") Long planVersionId,
                       @Param("deptId") Long deptId,
                       @Param("entryYear") String entryYear,
                       @Param("classCode") String classCode);
    List<Map<String, Object>> selectStudentProgress(@Param("studentId") Long studentId);
    int upsertProgress(Map<String, Object> params);
}
