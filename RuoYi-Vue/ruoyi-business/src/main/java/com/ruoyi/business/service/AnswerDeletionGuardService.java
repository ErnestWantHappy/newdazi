package com.ruoyi.business.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.common.exception.ServiceException;

/**
 * 保护答题与成绩历史，避免硬删除父记录后产生孤儿答案。
 */
@Service
public class AnswerDeletionGuardService
{
    @Autowired
    private BizStudentAnswerMapper studentAnswerMapper;

    public void assertLessonsDeletable(Long[] lessonIds)
    {
        List<Long> ids = normalizedIds(lessonIds);
        if (!ids.isEmpty() && studentAnswerMapper.countByLessonIds(ids) > 0) {
            throw new ServiceException("课程已有学生答题记录，为保留成绩历史不可删除");
        }
    }

    public void assertStudentsDeletable(Long[] studentIds)
    {
        List<Long> ids = normalizedIds(studentIds);
        if (!ids.isEmpty() && studentAnswerMapper.countByStudentIds(ids) > 0) {
            throw new ServiceException("学生已有答题记录，为保留成绩历史不可删除");
        }
    }

    private List<Long> normalizedIds(Long[] ids)
    {
        if (ids == null || ids.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.stream(ids).filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }
}
