package com.ruoyi.business.mapper;

import java.util.List;
import java.util.Date;
import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.domain.vo.LessonRankingVo;
import com.ruoyi.business.domain.vo.PracticalSubmissionVo;
import org.apache.ibatis.annotations.Param;

/**
 * 学生答题记录Mapper接口
 * 
 * @author zdx
 * @date 2025-12-30
 */
public interface BizStudentAnswerMapper 
{
    /**
     * 新增单条答题记录
     */
    int insertAnswer(BizStudentAnswer answer);

    /**
     * 原子新增或覆盖同一学生、课程、题目的答案，并回填 answerId。
     */
    int upsertAnswer(BizStudentAnswer answer);

    /**
     * 批量插入答题记录
     */
    void batchInsert(List<BizStudentAnswer> answers);

    /**
     * 更新答题记录
     */
    int updateAnswerById(BizStudentAnswer answer);

    /**
     * 查询学生某课程的答题记录
     */
    List<BizStudentAnswer> selectByStudentAndLesson(@Param("studentId") Long studentId, @Param("lessonId") Long lessonId);

    /**
     * 查询学生某课程某题最新一条答题记录
     */
    BizStudentAnswer selectLatestByStudentLessonQuestion(@Param("studentId") Long studentId,
                                                         @Param("lessonId") Long lessonId,
                                                         @Param("questionId") Long questionId);

    /** 统计指定课程下的答题记录，用于阻止破坏成绩历史的硬删除。 */
    int countByLessonIds(@Param("lessonIds") List<Long> lessonIds);

    /** 统计指定学生的答题记录，用于阻止产生无学生归属的孤儿答案。 */
    int countByStudentIds(@Param("studentIds") List<Long> studentIds);

    /** 统计指定题目的答题记录，用于阻止删题破坏历史成绩。 */
    int countByQuestionIds(@Param("questionIds") List<Long> questionIds);

    /** 统计指定课程和题目的答题记录数量，用于阻止在已有提交时修改题目总分。 */
    int countAnswersByLessonAndQuestion(@Param("lessonId") Long lessonId, @Param("questionId") Long questionId);
    
    /**
     * 查询某课程的所有答题记录
     */
    List<BizStudentAnswer> selectByLessonId(@Param("lessonId") Long lessonId);

    /**
     * 查询某课程指定班级的答题记录
     */
    List<BizStudentAnswer> selectByLessonAndClass(@Param("lessonId") Long lessonId, @Param("classCode") String classCode, @Param("entryYear") String entryYear, @Param("deptId") Long deptId);

    /**
     * 删除学生某课程的旧答题记录（用于重新提交）
     */
    void deleteByStudentAndLesson(@Param("studentId") Long studentId, @Param("lessonId") Long lessonId);

    /**
     * 查询课程排行榜
     */
    List<LessonRankingVo> selectLessonRanking(@Param("lessonId") Long lessonId);

    /**
     * 查询学生是否已提交过该课程
     */
    /**
     * 删除学生某课程指定题目的旧答题记录（用于增量更新）
     */
    void deleteByStudentLessonAndQuestions(@Param("studentId") Long studentId, 
                                          @Param("lessonId") Long lessonId, 
                                          @Param("questionIds") List<Long> questionIds);

    /**
     * 一次聚合学生指定自然年的历史成绩，避免按课程循环查询和题目评分项 N+1。
     */
    List<java.util.Map<String, Object>> selectHistoryScores(@Param("studentId") Long studentId,
                                                            @Param("startTime") Date startTime,
                                                            @Param("endTime") Date endTime);

    /**
     * 查询错题列表
     */
    List<BizLessonQuestionDetailVo> selectWrongQuestions(@Param("studentId") Long studentId, @Param("lessonId") Long lessonId);

    /**
     * P5: 查询班级所有学生的操作题提交情况（含未提交）
     */
    List<PracticalSubmissionVo> selectPracticalSubmissions(@Param("lessonId") Long lessonId, @Param("questionId") Long questionId, @Param("classCode") String classCode, @Param("entryYear") String entryYear, @Param("deptId") Long deptId);

    /**
     * 更新答题记录分数（批改打分）
     */
    int updateScore(@Param("answerId") Long answerId, @Param("score") Integer score);

    /**
     * 锁定答卷后再批改，避免学生重交或其他批改请求并发覆盖。
     */
    BizStudentAnswer selectByIdForUpdate(@Param("answerId") Long answerId);
    
    /**
     * P2: 查询学生成绩汇总（按课程分组）
     */
    List<java.util.Map<String, Object>> selectScoreSummaryByStudent(
        @Param("studentId") Long studentId, 
        @Param("lessonId") Long lessonId);

    /**
     * 批量查询学生成绩汇总（按学生和课程分组）
     */
    List<java.util.Map<String, Object>> selectScoreSummaryByStudents(
        @Param("studentIds") List<Long> studentIds,
        @Param("lessonIds") List<Long> lessonIds);

    /**
     * 更新答题记录的预览状态
     */
    int updatePreviewStatus(BizStudentAnswer answer);

    /** 将当前作品首附件预览状态同步到旧答题字段，供旧页面兼容读取。 */
    int updatePreviewByPracticalVersion(@Param("practicalVersionId") Long practicalVersionId,
                                        @Param("previewStatus") String previewStatus,
                                        @Param("previewPath") String previewPath,
                                        @Param("previewErrorMessage") String previewErrorMessage);

    /** 清空当前作品兼容字段，但保留答案行和逻辑作品ID。 */
    int clearPracticalAnswer(@Param("answerId") Long answerId,
                             @Param("practicalArtifactId") Long practicalArtifactId,
                             @Param("submitTime") Date submitTime);

    /**
     * 领取首次提交后的预览转换任务
     */
    int claimSubmitPreviewConversion(@Param("answerId") Long answerId);

    /**
     * 领取失败或卡住的预览重转任务
     */
    int claimRetryPreviewConversion(@Param("answerId") Long answerId,
                                    @Param("expectedStatus") String expectedStatus,
                                    @Param("expectedRetryCount") Integer expectedRetryCount,
                                    @Param("expectedLastRetryTime") Date expectedLastRetryTime,
                                    @Param("nextRetryCount") Integer nextRetryCount,
                                    @Param("claimedAt") Date claimedAt);

    /**
     * 根据ID查询答题记录
     */
    BizStudentAnswer selectById(@Param("answerId") Long answerId);
    /**
     * 查询学生答题矩阵详情
     */
    List<com.ruoyi.business.domain.vo.StudentAnswerMatrixVo> selectStudentAnswerMatrix(@Param("lessonId") Long lessonId, @Param("classCode") String classCode, @Param("entryYear") String entryYear, @Param("deptId") Long deptId);

    /**
     * 查询某课程有答题记录的班级列表（用于批改页面班级选择）
     */
    List<java.util.Map<String, Object>> selectClassesByLessonAnswers(@Param("lessonId") Long lessonId);

    /**
     * 聚合查询某课程下当前教师可管理班级的提交、批改和成绩状态。
     */
    List<java.util.Map<String, Object>> selectClassStatusByLesson(@Param("lessonId") Long lessonId,
                                                                  @Param("teacherUserId") Long teacherUserId,
                                                                  @Param("deptId") Long deptId);

    /**
     * 统计当前课程当前班级已有成绩的学生数。
     */
    int countScoredStudentsByLessonAndClass(@Param("lessonId") Long lessonId,
                                            @Param("classCode") String classCode,
                                            @Param("entryYear") String entryYear,
                                            @Param("deptId") Long deptId);

    /** 历史答卷证明该课程曾真实用于指定届别和班级。 */
    int existsLessonClassAnswer(@Param("lessonId") Long lessonId,
                                @Param("classCode") String classCode,
                                @Param("entryYear") String entryYear,
                                @Param("deptId") Long deptId);

    /**
     * 查询达到自动重试条件的失败操作题记录
     */
    List<BizStudentAnswer> selectRecoverablePracticalAnswersForRetry(@Param("retryBefore") Date retryBefore,
                                                                     @Param("stuckBefore") Date stuckBefore,
                                                                     @Param("maxRetryCount") Integer maxRetryCount);

    /**
     * 查询当前班级当前操作题下可手动重转的失败记录
     */
    List<BizStudentAnswer> selectRecoverablePracticalAnswersForManualRetry(@Param("lessonId") Long lessonId,
                                                                           @Param("questionId") Long questionId,
                                                                           @Param("classCode") String classCode,
                                                                           @Param("entryYear") String entryYear,
                                                                           @Param("deptId") Long deptId,
                                                                           @Param("stuckBefore") Date stuckBefore);
}
