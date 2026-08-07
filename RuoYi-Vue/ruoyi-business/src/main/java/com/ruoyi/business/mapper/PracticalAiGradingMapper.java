package com.ruoyi.business.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.PracticalAiJob;
import com.ruoyi.business.domain.PracticalAiResult;
import com.ruoyi.business.domain.TeacherAiConfig;

public interface PracticalAiGradingMapper
{
    TeacherAiConfig selectConfig(@Param("teacherUserId") Long teacherUserId);
    TeacherAiConfig selectConfigForUpdate(@Param("teacherUserId") Long teacherUserId);
    int upsertConfig(TeacherAiConfig config);
    int deleteConfig(@Param("teacherUserId") Long teacherUserId);
    int insertJob(PracticalAiJob job);
    PracticalAiJob selectJob(@Param("jobId") Long jobId, @Param("teacherUserId") Long teacherUserId);
    PracticalAiJob selectJobForWorker(@Param("jobId") Long jobId);
    PracticalAiJob selectRunningJob(@Param("teacherUserId") Long teacherUserId,
                                    @Param("lessonId") Long lessonId,
                                    @Param("questionId") Long questionId,
                                    @Param("entryYear") String entryYear,
                                    @Param("classCode") String classCode);
    int updateJobStatus(@Param("jobId") Long jobId, @Param("jobStatus") String jobStatus,
                        @Param("startTime") Date startTime, @Param("finishTime") Date finishTime,
                        @Param("errorMessage") String errorMessage);
    int updateJobCounts(@Param("jobId") Long jobId);
    int insertResult(PracticalAiResult result);
    PracticalAiResult selectResult(@Param("resultId") Long resultId);
    List<PracticalAiResult> selectResultsByJob(@Param("jobId") Long jobId);
    int updateResult(PracticalAiResult result);
    int resetFailedResults(@Param("jobId") Long jobId);
    int updatePendingResultsStatus(@Param("jobId") Long jobId, @Param("resultStatus") String resultStatus,
                                   @Param("errorMessage") String errorMessage, @Param("finishTime") Date finishTime);
}
