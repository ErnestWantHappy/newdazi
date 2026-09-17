package com.ruoyi.business.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.mapper.StudentAnswerArchiveMapper;
import com.ruoyi.common.exception.ServiceException;

/**
 * 将已经移出课程的题目答案迁出在线统计表，同时保留完整成绩与审计证据。
 */
@Service
public class StudentAnswerArchiveService
{
    @Autowired
    private StudentAnswerArchiveMapper mapper;

    public int archiveRemovedQuestions(Long lessonId, Collection<Long> removedQuestionIds)
    {
        List<Long> questionIds = removedQuestionIds == null
                ? new ArrayList<Long>()
                : removedQuestionIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (lessonId == null || questionIds.isEmpty())
        {
            return 0;
        }

        int targetCount = mapper.countLiveAnswers(lessonId, questionIds);
        if (targetCount == 0)
        {
            return 0;
        }

        mapper.archiveAnswers(lessonId, questionIds);
        if (mapper.countArchivedAnswers(lessonId, questionIds) != targetCount)
        {
            throw new ServiceException("被移除题目的答案未完整归档，课程保存已取消");
        }

        String archiveBatch = "lesson-edit-" + lessonId + "-" + UUID.randomUUID().toString();
        mapper.archiveMetadata(lessonId, questionIds, archiveBatch);
        if (mapper.countArchivedMetadata(lessonId, questionIds) != targetCount)
        {
            throw new ServiceException("被移除题目的归档元数据不完整，课程保存已取消");
        }

        int deletedCount = mapper.deleteArchivedLiveAnswers(lessonId, questionIds);
        if (deletedCount != targetCount)
        {
            throw new ServiceException("被移除题目的在线答案清理数量不一致，课程保存已取消");
        }
        return deletedCount;
    }
}
