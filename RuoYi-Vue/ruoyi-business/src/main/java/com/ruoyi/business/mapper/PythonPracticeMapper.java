package com.ruoyi.business.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/** Python 刷题独立业务域数据访问。 */
public interface PythonPracticeMapper {
    List<Map<String, Object>> selectPlans(@Param("deptId") Long deptId, @Param("entryYear") String entryYear);
    Map<String, Object> selectPlan(@Param("planId") Long planId);
    Map<String, Object> selectPlanByScope(@Param("deptId") Long deptId, @Param("grade") Integer grade,
                                          @Param("semester") String semester, @Param("entryYear") String entryYear);
    Map<String, Object> selectPlanByVersion(@Param("planVersionId") Long planVersionId);
    List<Map<String, Object>> selectPlanQuestions(@Param("planVersionId") Long planVersionId);
    List<Map<String, Object>> selectStudentQuestions(@Param("deptId") Long deptId, @Param("entryYear") String entryYear,
                                                       @Param("classCode") String classCode);
    Map<String, Object> selectQuestion(@Param("sourceType") String sourceType, @Param("sourceId") Long sourceId,
                                        @Param("questionId") Long questionId);
    Map<String, Object> selectDraft(@Param("studentId") Long studentId, @Param("sourceType") String sourceType,
                                    @Param("sourceId") Long sourceId, @Param("questionId") Long questionId);
    int upsertDraft(Map<String, Object> params);
    List<Map<String, Object>> selectSubmissions(@Param("studentId") Long studentId, @Param("sourceType") String sourceType,
                                                @Param("sourceId") Long sourceId, @Param("questionId") Long questionId);
    Map<String, Object> selectSubmission(@Param("submissionId") Long submissionId);
    List<Map<String, Object>> selectSnapshotCases(@Param("snapshotId") Long snapshotId, @Param("publicOnly") boolean publicOnly);
    int markSubmissionJudging(@Param("submissionId") Long submissionId);
    int updateSubmissionResult(Map<String, Object> params);
    int insertSubmission(Map<String, Object> params);
    int insertPlan(Map<String, Object> params);
    int insertPlanVersion(Map<String, Object> params);
    int insertSnapshot(Map<String, Object> params);
    int insertSnapshotCase(Map<String, Object> params);
    int insertPlanQuestion(Map<String, Object> params);
    int deletePlanQuestion(@Param("planVersionId") Long planVersionId, @Param("questionId") Long questionId);
    int countPlanVersionQuestion(@Param("planVersionId") Long planVersionId, @Param("questionId") Long questionId);
    int updatePlanStatus(@Param("planId") Long planId, @Param("status") String status);
    int publishVersion(@Param("planId") Long planId, @Param("planVersionId") Long planVersionId);
    int retractPublishedVersions(@Param("planId") Long planId, @Param("exceptVersionId") Long exceptVersionId);
    int selectPlanQuestionCount(@Param("planVersionId") Long planVersionId);
    int selectSnapshotPublicCaseCount(@Param("planVersionId") Long planVersionId, @Param("publicOnly") boolean publicOnly);
    int updateCurrentVersion(@Param("planId") Long planId, @Param("versionNo") Integer versionNo);
    Integer selectVersionNo(@Param("planVersionId") Long planVersionId);
    List<Map<String, Object>> selectExtensions(@Param("planId") Long planId);
    int insertExtension(Map<String, Object> params);
    int insertExtensionClass(Map<String, Object> params);
    int insertExtensionQuestion(Map<String, Object> params);
    int publishExtension(@Param("extensionId") Long extensionId);
    int retractExtension(@Param("extensionId") Long extensionId);
    Map<String, Object> selectExtension(@Param("extensionId") Long extensionId);
    int selectExtensionQuestionCount(@Param("extensionId") Long extensionId);
    int selectExtensionPublicCaseCount(@Param("extensionId") Long extensionId, @Param("publicOnly") boolean publicOnly);
    int countExtensionQuestion(@Param("extensionId") Long extensionId, @Param("questionId") Long questionId);
    int countBaseQuestion(@Param("planId") Long planId, @Param("questionId") Long questionId);
    int countPublishedExtensionConflict(@Param("extensionId") Long extensionId, @Param("questionId") Long questionId);
    List<Map<String, Object>> selectAnalytics(@Param("sourceType") String sourceType, @Param("sourceId") Long sourceId, @Param("deptId") Long deptId, @Param("entryYear") String entryYear, @Param("classCode") String classCode);
    List<Map<String, Object>> selectStudentProgress(@Param("studentId") Long studentId);
    int upsertProgress(Map<String, Object> params);
    int countPublishedVersions(@Param("planId") Long planId);
    int deletePlanSnapshotCases(@Param("planId") Long planId);
    int deletePlanSnapshots(@Param("planId") Long planId);
    int deletePlanQuestions(@Param("planId") Long planId);
    int deletePlanVersions(@Param("planId") Long planId);
    int deleteExtensionQuestions(@Param("planId") Long planId);
    int deleteExtensionClasses(@Param("planId") Long planId);
    int deleteExtensions(@Param("planId") Long planId);
    int deletePlan(@Param("planId") Long planId);
}
