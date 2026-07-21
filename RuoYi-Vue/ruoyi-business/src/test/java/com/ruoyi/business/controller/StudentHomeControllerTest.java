package com.ruoyi.business.controller;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentHomeControllerTest
{
    @Mock private BizLessonAssignmentMapper assignmentMapper;
    @Mock private BizLessonMapper lessonMapper;
    @Mock private BizStudentAnswerMapper studentAnswerMapper;
    @Mock private RedisCache redisCache;

    @InjectMocks
    private StudentHomeController controller;

    @BeforeEach
    void configureGraceWindow()
    {
        ReflectionTestUtils.setField(controller, "submissionGraceMinutes", 15L);
    }

    @Test
    void currentAssignmentDoesNotNeedHistory()
    {
        BizStudent student = student();
        when(lessonMapper.selectBizLessonByLessonId(5L)).thenReturn(lesson());
        when(assignmentMapper.selectCurrentLessonByClass("2025", "1", 10L)).thenReturn(5L);

        assertNull(validate(student, 5L));
        verify(assignmentMapper, never()).countRecentAdvanceHistory(
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(Date.class));
    }

    @Test
    void recentlyAdvancedLessonMayBeSubmittedWithinConfiguredWindow()
    {
        BizStudent student = student();
        when(lessonMapper.selectBizLessonByLessonId(5L)).thenReturn(lesson());
        when(assignmentMapper.selectCurrentLessonByClass("2025", "1", 10L)).thenReturn(6L);
        when(assignmentMapper.countRecentAdvanceHistory(
                org.mockito.ArgumentMatchers.eq(5L), org.mockito.ArgumentMatchers.eq("2025"),
                org.mockito.ArgumentMatchers.eq("1"), org.mockito.ArgumentMatchers.eq(10L),
                org.mockito.ArgumentMatchers.any(Date.class))).thenReturn(1);

        long before = System.currentTimeMillis();
        assertNull(validate(student, 5L));
        long after = System.currentTimeMillis();

        ArgumentCaptor<Date> cutoff = ArgumentCaptor.forClass(Date.class);
        verify(assignmentMapper).countRecentAdvanceHistory(
                org.mockito.ArgumentMatchers.eq(5L), org.mockito.ArgumentMatchers.eq("2025"),
                org.mockito.ArgumentMatchers.eq("1"), org.mockito.ArgumentMatchers.eq(10L), cutoff.capture());
        long age = after - cutoff.getValue().getTime();
        assertTrue(age >= TimeUnit.MINUTES.toMillis(15));
        assertTrue(age <= TimeUnit.MINUTES.toMillis(15) + (after - before) + 100L);
    }

    @Test
    void neverAssignedOrExpiredLessonIsRejected()
    {
        when(lessonMapper.selectBizLessonByLessonId(5L)).thenReturn(lesson());
        when(assignmentMapper.selectCurrentLessonByClass("2025", "1", 10L)).thenReturn(null);

        String error = validate(student(), 5L);

        assertTrue(error.contains("补交时间已超过15分钟"));
    }

    @Test
    void crossSchoolLessonIsRejectedBeforeHistoryLookup()
    {
        BizLesson otherSchool = lesson();
        otherSchool.setDeptId(11L);
        when(lessonMapper.selectBizLessonByLessonId(5L)).thenReturn(otherSchool);

        String error = validate(student(), 5L);

        assertTrue(error.contains("不属于当前学校"));
        verify(assignmentMapper, never()).selectCurrentLessonByClass("2025", "1", 10L);
    }

    @Test
    void practicalAnswerPathMustBelongToCurrentStudentUpload()
    {
        String path = "/profile/upload/student-answer/random/answer.docx";
        when(redisCache.getCacheObject("student:practical-upload-owner:" + path)).thenReturn(30L);

        ReflectionTestUtils.invokeMethod(controller, "validatePracticalAnswerPath", 30L, 5L, 7L, path);

        when(redisCache.getCacheObject("student:practical-upload-owner:" + path)).thenReturn(31L);
        assertThrows(ServiceException.class, () -> ReflectionTestUtils.invokeMethod(
                controller, "validatePracticalAnswerPath", 30L, 5L, 7L, path));
        assertThrows(ServiceException.class, () -> ReflectionTestUtils.invokeMethod(
                controller, "validatePracticalAnswerPath", 30L, 5L, 7L,
                "/profile/upload/2026/07/other-student.docx"));
    }

    private String validate(BizStudent student, Long lessonId)
    {
        return ReflectionTestUtils.invokeMethod(
                controller, "validateSubmissionAccess", student, 10L, lessonId);
    }

    private BizStudent student()
    {
        BizStudent student = new BizStudent();
        student.setStudentId(30L);
        student.setEntryYear("2025");
        student.setClassCode("1");
        return student;
    }

    private BizLesson lesson()
    {
        BizLesson lesson = new BizLesson();
        lesson.setLessonId(5L);
        lesson.setDeptId(10L);
        return lesson;
    }
}
