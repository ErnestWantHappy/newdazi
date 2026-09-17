package com.ruoyi.business.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.PracticalAiJob;
import com.ruoyi.business.domain.PracticalAiApplyAudit;
import com.ruoyi.business.domain.PracticalAiEvent;
import com.ruoyi.business.domain.PracticalAiResult;
import com.ruoyi.business.domain.TeacherAiConfig;
import com.ruoyi.business.domain.TeacherPracticalReferenceAnswer;

public interface PracticalAiGradingMapper
{
    TeacherAiConfig selectConfig(@Param("teacherUserId") Long teacherUserId);
    TeacherAiConfig selectConfigForUpdate(@Param("teacherUserId") Long teacherUserId);
    int upsertConfig(TeacherAiConfig config);
    int deleteConfig(@Param("teacherUserId") Long teacherUserId);
    int insertJob(PracticalAiJob job);
    PracticalAiJob selectJob(@Param("jobId") Long jobId, @Param("teacherUserId") Long teacherUserId);
    PracticalAiJob selectJobForWorker(@Param("jobId") Long jobId);
    PracticalAiJob selectActiveJob(@Param("teacherUserId") Long teacherUserId,
                                    @Param("lessonId") Long lessonId,
                                    @Param("questionId") Long questionId,
                                    @Param("entryYear") String entryYear,
                                    @Param("classCode") String classCode);
    PracticalAiJob selectLatestJob(@Param("teacherUserId") Long teacherUserId,
                                   @Param("lessonId") Long lessonId,
                                   @Param("questionId") Long questionId,
                                   @Param("entryYear") String entryYear,
                                   @Param("classCode") String classCode);
    List<PracticalAiJob> selectRecoverableJobs();
    int updateJobStatus(@Param("jobId") Long jobId, @Param("jobStatus") String jobStatus,
                        @Param("startTime") Date startTime, @Param("finishTime") Date finishTime,
                        @Param("errorMessage") String errorMessage);
    int updateJobCounts(@Param("jobId") Long jobId);
    int updateJobPreparation(@Param("jobId") Long jobId,
                             @Param("preparationStatus") String preparationStatus,
                             @Param("comparisonPagesJson") String comparisonPagesJson,
                             @Param("errorMessage") String errorMessage);
    int updateJobHeartbeat(@Param("jobId") Long jobId, @Param("currentResultId") Long currentResultId);
    int insertResult(PracticalAiResult result);
    PracticalAiResult selectResult(@Param("resultId") Long resultId);
    List<PracticalAiResult> selectResultsByJob(@Param("jobId") Long jobId);
    int updateResult(PracticalAiResult result);
    int markResultProcessing(@Param("resultId") Long resultId, @Param("processingStage") String processingStage);
    int updateResultStage(@Param("resultId") Long resultId, @Param("processingStage") String processingStage);
    int resetInterruptedResults(@Param("jobId") Long jobId);
    int resetFailedResults(@Param("jobId") Long jobId);
    int updatePendingResultsStatus(@Param("jobId") Long jobId, @Param("resultStatus") String resultStatus,
                                   @Param("errorMessage") String errorMessage, @Param("finishTime") Date finishTime);
    int updateApplyStatus(@Param("resultId") Long resultId, @Param("applyStatus") String applyStatus,
                          @Param("appliedByUserId") Long appliedByUserId, @Param("appliedTime") Date appliedTime);
    int insertApplyAudit(PracticalAiApplyAudit audit);
    int insertEvent(PracticalAiEvent event);
    List<PracticalAiEvent> selectEvents(@Param("jobId") Long jobId,
                                        @Param("afterEventId") Long afterEventId,
                                        @Param("limit") Integer limit);
    TeacherPracticalReferenceAnswer selectReferenceAnswer(@Param("teacherUserId") Long teacherUserId,
                                                          @Param("deptId") Long deptId,
                                                          @Param("lessonId") Long lessonId,
                                                          @Param("questionId") Long questionId);
    int upsertReferenceAnswer(TeacherPracticalReferenceAnswer answer);
}
