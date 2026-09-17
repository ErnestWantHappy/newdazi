package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 彻底清除（用户已确认语义）专用的批量删除。
 * 普通删除与兼容 purge 入口统一复用；课程/学生权限在调用前校验。
 * 全部为单语句（不依赖 allowMultiQueries）。
 */
public interface PurgeMapper
{
    // ------- 课程范围：查询 -------
    List<String> selectLessonAnswerFilePaths(@Param("lessonIds") List<Long> lessonIds);

    List<Long> selectDoomedArtifactIds(@Param("lessonIds") List<Long> lessonIds);

    List<String> selectLessonReferenceFilePaths(@Param("lessonIds") List<Long> lessonIds);

    List<String> selectArtifactFilePaths(@Param("artifactIds") List<Long> artifactIds);

    List<Long> selectRoomIdsByLessons(@Param("lessonIds") List<Long> lessonIds);

    List<String> selectCollabFilePathsByRooms(@Param("roomIds") List<Long> roomIds);


    List<Long> selectQuestionIdsByLessons(@Param("lessonIds") List<Long> lessonIds);

    // ------- 课程范围：删除（按依赖顺序调用） -------
    int deleteScoringDetailsByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteAiResultsByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteAttachmentsByArtifacts(@Param("artifactIds") List<Long> artifactIds);

    int deleteVersionsByArtifacts(@Param("artifactIds") List<Long> artifactIds);

    int deleteArtifactsByIds(@Param("artifactIds") List<Long> artifactIds);

    int deleteAnswersByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteAnswerBackupTableByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteAnswerOrphansByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteTaskStatesByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deletePerformancesByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteCheckinsByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteProgrammingSubmissionsByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteProgrammingDraftsByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteFlowchartSubmissionsByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteFlowchartDraftsByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteFlowchartSnapshotsByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteGuideAnswersByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteGuideBindingsByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteLessonToolsByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteCollabTicketsByRooms(@Param("roomIds") List<Long> roomIds);

    int deleteCollabRevisionsByRooms(@Param("roomIds") List<Long> roomIds);

    int deleteCollabRoomsByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteCollabActivitiesByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteGradingDeadlineAuditsByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteGradingDeadlinesMainByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteRubricsByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteReferenceAnswersByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteAiJobsByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteScoreAdjustmentsByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteAssignmentHistoryRowsByLessons(@Param("lessonIds") List<Long> lessonIds);

    int clearAssignmentNextPointersByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteGroupSnapshotsByLessons(@Param("lessonIds") List<Long> lessonIds);

    int deleteClassScopesByLessons(@Param("lessonIds") List<Long> lessonIds);

    // ------- 学生范围：查询 -------
    List<String> selectStudentAnswerFilePaths(@Param("studentIds") List<Long> studentIds);

    List<Long> selectArtifactIdsByStudents(@Param("studentIds") List<Long> studentIds);

    // ------- 学生范围：删除（按依赖顺序调用） -------
    int deleteScoringDetailsByStudents(@Param("studentIds") List<Long> studentIds);

    int deleteAiResultsByStudents(@Param("studentIds") List<Long> studentIds);

    int deleteAnswersByStudents(@Param("studentIds") List<Long> studentIds);

    int deleteAnswerBackupTableByStudents(@Param("studentIds") List<Long> studentIds);

    int deleteAnswerOrphansByStudents(@Param("studentIds") List<Long> studentIds);

    int deleteTaskStatesByStudents(@Param("studentIds") List<Long> studentIds);

    int deletePerformancesByStudents(@Param("studentIds") List<Long> studentIds);

    int deleteCheckinsByStudents(@Param("studentIds") List<Long> studentIds);

    int deleteProgrammingSubmissionsByStudents(@Param("studentIds") List<Long> studentIds);

    int deleteProgrammingDraftsByStudents(@Param("studentIds") List<Long> studentIds);

    int deletePythonPracticeCasesByStudents(@Param("studentIds") List<Long> studentIds);

    int deletePythonPracticeSubmissionsByStudents(@Param("studentIds") List<Long> studentIds);

    int deletePythonPracticeDraftsByStudents(@Param("studentIds") List<Long> studentIds);

    int deletePythonPracticeProgressByStudents(@Param("studentIds") List<Long> studentIds);

    int deleteFlowchartSubmissionsByStudents(@Param("studentIds") List<Long> studentIds);

    int deleteFlowchartDraftsByStudents(@Param("studentIds") List<Long> studentIds);

    int deleteGuideAnswersByStudents(@Param("studentIds") List<Long> studentIds);

    int deleteCountyAnswersByStudents(@Param("studentIds") List<Long> studentIds);

    int deleteCountyStudentsByStudents(@Param("studentIds") List<Long> studentIds);

    int deleteIotGroupStudents(@Param("studentIds") List<Long> studentIds);

    int deleteScoreAdjustmentsByStudents(@Param("studentIds") List<Long> studentIds);

    int deleteCollabTicketsByStudents(@Param("studentIds") List<Long> studentIds);

    // ------- 文件引用复核 -------
    int countPathReferences(@Param("path") String path);
}
