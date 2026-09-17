package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

/** 被移出课程的答案归档数据访问。 */
public interface StudentAnswerArchiveMapper
{
    int countLiveAnswers(@Param("lessonId") Long lessonId,
                         @Param("questionIds") List<Long> questionIds);

    int archiveAnswers(@Param("lessonId") Long lessonId,
                       @Param("questionIds") List<Long> questionIds);

    int countArchivedAnswers(@Param("lessonId") Long lessonId,
                             @Param("questionIds") List<Long> questionIds);

    int archiveMetadata(@Param("lessonId") Long lessonId,
                        @Param("questionIds") List<Long> questionIds,
                        @Param("archiveBatch") String archiveBatch);

    int countArchivedMetadata(@Param("lessonId") Long lessonId,
                              @Param("questionIds") List<Long> questionIds);

    int deleteArchivedLiveAnswers(@Param("lessonId") Long lessonId,
                                  @Param("questionIds") List<Long> questionIds);
}
