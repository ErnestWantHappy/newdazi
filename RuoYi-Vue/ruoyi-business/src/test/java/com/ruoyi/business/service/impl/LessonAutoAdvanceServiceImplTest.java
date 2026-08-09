package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizLessonAssignment;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.BizTeacherClassMapper;
import com.ruoyi.business.mapper.LessonClassScopeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonAutoAdvanceServiceImplTest
{
    @Mock private BizLessonMapper lessonMapper;
    @Mock private BizLessonAssignmentMapper assignmentMapper;
    @Mock private BizStudentAnswerMapper studentAnswerMapper;
    @Mock private BizStudentMapper studentMapper;
    @Mock private BizTeacherClassMapper teacherClassMapper;
    @Mock private LessonClassScopeMapper lessonClassScopeMapper;

    @InjectMocks
    private LessonAutoAdvanceServiceImpl service;

    @BeforeEach
    void useServiceAsTransactionalProxyForUnitTest()
    {
        ReflectionTestUtils.setField(service, "self", service);
    }

    @Test
    void differentClassesKeepIndependentReadyTimes()
    {
        BizLesson current = lesson(1L, 1, "assessment");
        current.setAutoAdvanceThresholdPct(50);
        current.setAutoAdvanceDelayHours(BigDecimal.ONE);
        BizLesson next = lesson(2L, 2, "assessment");
        BizLessonAssignment readyLongEnough = assignment(101L, "1",
                new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2)));
        BizLessonAssignment justReady = assignment(102L, "2", null);

        when(assignmentMapper.selectAssignmentsByLessonId(1L))
                .thenReturn(Arrays.asList(readyLongEnough, justReady));
        when(lessonMapper.selectBizLessonList(any(BizLesson.class)))
                .thenReturn(Arrays.asList(current, next));
        when(studentAnswerMapper.countScoredStudentsByLessonAndClass(eq(1L), any(), eq("2025"), eq(10L)))
                .thenReturn(5);
        when(studentMapper.countByDeptIdAndClass(eq(10L), eq("2025"), any())).thenReturn(10);
        when(assignmentMapper.selectCurrentAssignmentForUpdate("2025", "1", 10L))
                .thenReturn(readyLongEnough);
        when(assignmentMapper.advanceCurrentAssignment(eq(101L), eq(1L), eq(2L), eq(40L), any(Date.class)))
                .thenReturn(1);

        assertEquals(1, service.processLessonForScan(current));

        verify(assignmentMapper).markAutoAdvanceReady(eq(102L), any(Date.class));
        verify(assignmentMapper).advanceCurrentAssignment(eq(101L), eq(1L), eq(2L), eq(40L), any(Date.class));
        verify(assignmentMapper, never()).advanceCurrentAssignment(eq(102L), anyLong(), anyLong(), anyLong(), any(Date.class));
        verify(assignmentMapper).insertAdvanceHistory(eq(readyLongEnough), eq(2L), eq(40L), eq("AUTO"), any(Date.class));
    }

    @Test
    void conditionalUpdatePreventsDuplicateHistoryDuringRace()
    {
        BizLesson current = lesson(1L, 1, "assessment");
        current.setAutoAdvanceThresholdPct(50);
        current.setAutoAdvanceDelayHours(new BigDecimal("0.5"));
        BizLesson next = lesson(2L, 2, "assessment");
        BizLessonAssignment assignment = assignment(101L, "1",
                new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)));
        when(assignmentMapper.selectAssignmentsByLessonId(1L)).thenReturn(Collections.singletonList(assignment));
        when(lessonMapper.selectBizLessonList(any(BizLesson.class))).thenReturn(Arrays.asList(current, next));
        when(studentAnswerMapper.countScoredStudentsByLessonAndClass(1L, "1", "2025", 10L)).thenReturn(5);
        when(studentMapper.countByDeptIdAndClass(10L, "2025", "1")).thenReturn(10);
        when(assignmentMapper.selectCurrentAssignmentForUpdate("2025", "1", 10L)).thenReturn(assignment);
        when(assignmentMapper.advanceCurrentAssignment(eq(101L), eq(1L), eq(2L), eq(40L), any(Date.class)))
                .thenReturn(0);

        assertEquals(0, service.processLessonForScan(current));
        verify(assignmentMapper, never()).insertAdvanceHistory(any(), anyLong(), anyLong(), any(), any(Date.class));
    }

    @Test
    void attendanceLessonIsNeverScannedOrAdvanced()
    {
        BizLesson attendance = lesson(8L, 3, "attendance");
        attendance.setAutoAdvanceEnabled(Boolean.TRUE);
        when(lessonMapper.selectAutoAdvanceCandidates()).thenReturn(Collections.singletonList(attendance));

        Map<String, Object> result = service.scanAndAdvance();

        assertEquals(0, result.get("scanned"));
        assertEquals(0, result.get("advanced"));
        assertEquals(1, result.get("skipped"));
        verify(assignmentMapper, never()).selectAssignmentsByLessonId(anyLong());
    }

    @Test
    void nextLessonNeverCrossesOpeningGradeWhenLessonNumbersRestart()
    {
        BizLesson current = lesson(1L, 1, "assessment");
        BizLesson differentGrade = lesson(2L, 2, "assessment");
        differentGrade.setGrade(8L);
        when(lessonMapper.selectBizLessonList(any(BizLesson.class)))
                .thenReturn(Arrays.asList(current, differentGrade));

        BizLesson result = ReflectionTestUtils.invokeMethod(service, "findNextLesson", current);

        assertNull(result);
    }

    private BizLesson lesson(Long id, int number, String mode)
    {
        BizLesson lesson = new BizLesson();
        lesson.setLessonId(id);
        lesson.setLessonNum(number);
        lesson.setLessonMode(mode);
        lesson.setGrade(7L);
        lesson.setEntryYear("2025");
        lesson.setDeptId(10L);
        lesson.setCreatorId(40L);
        lesson.setAutoAdvanceEnabled(Boolean.TRUE);
        return lesson;
    }

    private BizLessonAssignment assignment(Long id, String classCode, Date readyTime)
    {
        BizLessonAssignment assignment = new BizLessonAssignment();
        assignment.setAssignmentId(id);
        assignment.setLessonId(1L);
        assignment.setEntryYear("2025");
        assignment.setClassCode(classCode);
        assignment.setDeptId(10L);
        assignment.setAssignerId(40L);
        assignment.setAutoAdvanceReadyTime(readyTime);
        return assignment;
    }
}
