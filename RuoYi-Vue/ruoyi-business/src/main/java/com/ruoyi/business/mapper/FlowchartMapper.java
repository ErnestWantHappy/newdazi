package com.ruoyi.business.mapper;

import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.FlowchartDraft;
import com.ruoyi.business.domain.FlowchartLessonSnapshot;
import com.ruoyi.business.domain.FlowchartQuestionConfig;
import com.ruoyi.business.domain.FlowchartSubmission;

/** 画程题目、草稿、快照和提交持久化。 */
public interface FlowchartMapper {
    FlowchartQuestionConfig selectQuestionConfig(@Param("questionId") Long questionId);
    int insertQuestionConfig(FlowchartQuestionConfig config);
    int updateQuestionConfig(@Param("config") FlowchartQuestionConfig config,
                             @Param("expectedRevision") Integer expectedRevision);
    int deleteQuestionConfig(@Param("questionId") Long questionId);

    FlowchartLessonSnapshot selectLessonSnapshot(@Param("lessonId") Long lessonId,
                                                 @Param("questionId") Long questionId);
    int insertLessonSnapshot(FlowchartLessonSnapshot snapshot);

    FlowchartDraft selectDraft(@Param("studentId") Long studentId,
                               @Param("lessonId") Long lessonId,
                               @Param("questionId") Long questionId);
    FlowchartDraft selectDraftForUpdate(@Param("studentId") Long studentId,
                                        @Param("lessonId") Long lessonId,
                                        @Param("questionId") Long questionId);
    int insertDraft(FlowchartDraft draft);
    int updateDraft(@Param("draft") FlowchartDraft draft,
                    @Param("expectedRevision") Integer expectedRevision);
    int replaceDraftFromSubmission(@Param("draft") FlowchartDraft draft);

    Integer selectNextSubmissionVersion(@Param("studentId") Long studentId,
                                        @Param("lessonId") Long lessonId,
                                        @Param("questionId") Long questionId);
    FlowchartSubmission selectSubmissionByDraftRevision(@Param("studentId") Long studentId,
                                                        @Param("lessonId") Long lessonId,
                                                        @Param("questionId") Long questionId,
                                                        @Param("draftRevision") Integer draftRevision);
    int insertSubmission(FlowchartSubmission submission);
    int updateSubmissionAnswerId(@Param("submissionId") Long submissionId,
                                 @Param("answerId") Long answerId);
    FlowchartSubmission selectLatestSubmission(@Param("studentId") Long studentId,
                                               @Param("lessonId") Long lessonId,
                                               @Param("questionId") Long questionId);
    FlowchartSubmission selectSubmission(@Param("studentId") Long studentId,
                                         @Param("lessonId") Long lessonId,
                                         @Param("questionId") Long questionId,
                                         @Param("versionNo") Integer versionNo);
}

