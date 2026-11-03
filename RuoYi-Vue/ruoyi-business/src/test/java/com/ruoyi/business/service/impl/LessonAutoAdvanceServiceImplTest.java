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
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verifyNoInteractions;

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

    @AfterEach
    void clearLogin()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void previewUsesRealAssignmentAndAllowsLessonNumberGapsWithoutWriting()
    {
        BizLesson current = previewClass(5, 10);
        current.setAutoAdvanceEnabled(false);
        BizLesson next = lesson(3L, 4, "assessment");
        when(lessonMapper.selectBizLessonList(any(BizLesson.class)))
                .thenReturn(Arrays.asList(current, next));

        Map<String, Object> row = previewRow();

        assertEquals(1L, row.get("currentLessonId"));
        assertEquals(3L, row.get("nextLessonId"));
        assertEquals(5, row.get("requiredCount"));
        assertEquals(true, row.get("canAdvance"));
        assertEquals("已关闭，不会自动推进", row.get("autoStatus"));
        verify(assignmentMapper, never()).selectCurrentAssignmentForUpdate(any(), any(), anyLong());
        verify(assignmentMapper, never()).markAutoAdvanceReady(anyLong(), any());
        verify(assignmentMapper, never()).advanceCurrentAssignment(anyLong(), anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    void previewRoundsRequiredPeopleUpAndBlocksUnmetThreshold()
    {
        BizLesson current = previewClass(19, 39);
        when(lessonMapper.selectBizLessonList(any(BizLesson.class)))
                .thenReturn(Arrays.asList(current, lesson(2L, 2, "assessment")));

        Map<String, Object> row = previewRow();

        assertEquals(20, row.get("requiredCount"));
        assertEquals(false, row.get("canAdvance"));
        assertTrue(String.valueOf(row.get("reason")).contains("19/39"));
    }

    @Test
    void previewShowsNoNextLessonInsteadOfInventingOne()
    {
        BizLesson current = previewClass(10, 10);
        when(lessonMapper.selectBizLessonList(any(BizLesson.class))).thenReturn(Collections.singletonList(current));

        Map<String, Object> row = previewRow();

        assertNull(row.get("nextLessonId"));
        assertEquals(false, row.get("canAdvance"));
        assertTrue(String.valueOf(row.get("reason")).contains("先备好"));
    }

    @Test
    void previewDoesNotRevealUnmanagedClassOrOtherTeachersCourse()
    {
        loginTeacher();
        Map<String, Object> row = previewRow();
        assertEquals("只能推进自己管理的班级", row.get("reason"));
        verifyNoInteractions(lessonMapper, assignmentMapper, studentAnswerMapper, studentMapper);
    }

    @Test
    void previewRejectsOtherTeachersCourseBeforeReturningItsTitle()
    {
        loginTeacher();
        when(teacherClassMapper.checkTeacherClassExists(any())).thenReturn(1);
        when(assignmentMapper.selectCurrentAssignment("2025", "1", 10L)).thenReturn(assignment(101L, "1", null));
        BizLesson other = lesson(1L, 1, "assessment");
        other.setCreatorId(999L);
        when(lessonMapper.selectBizLessonByLessonId(1L)).thenReturn(other);

        Map<String, Object> row = previewRow();

        assertEquals("只能推进自己创建的课程", row.get("reason"));
        assertNull(row.get("currentLessonTitle"));
        verifyNoInteractions(studentAnswerMapper, studentMapper);
    }

    @Test
    void previewShowsTimerButNeverStartsTimerOnRead()
    {
        BizLesson current = previewClass(10, 10);
        current.setAutoAdvanceDelayHours(BigDecimal.ONE);
        Date ready = new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10));
        BizLessonAssignment target = assignment(101L, "1", ready);
        when(assignmentMapper.selectCurrentAssignment("2025", "1", 10L)).thenReturn(target);
        when(lessonMapper.selectBizLessonList(any(BizLesson.class)))
                .thenReturn(Arrays.asList(current, lesson(2L, 2, "assessment")));

        Map<String, Object> row = previewRow();

        assertEquals(ready.getTime() + TimeUnit.HOURS.toMillis(1), row.get("autoDueTime"));
        assertEquals("已达标，正在等待", row.get("autoStatus"));
        verify(assignmentMapper, never()).markAutoAdvanceReady(anyLong(), any());
    }

    @Test
    void previewDistinguishesReadyBeforeFirstScan()
    {
        BizLesson current = previewClass(10, 10);
        when(lessonMapper.selectBizLessonList(any(BizLesson.class)))
                .thenReturn(Arrays.asList(current, lesson(2L, 2, "assessment")));
        Map<String, Object> row = previewRow();
        assertTrue(String.valueOf(row.get("autoStatus")).contains("等待定时扫描开始计时"));
        assertNull(row.get("autoDueTime"));
    }

    @Test
    void stalePreviewCannotAdvanceTheNewCurrentLesson()
    {
        loginTeacher();
        when(teacherClassMapper.checkTeacherClassExists(any())).thenReturn(1);
        BizLessonAssignment moved = assignment(101L, "1", null);
        moved.setLessonId(2L);
        when(assignmentMapper.selectCurrentAssignmentForUpdate("2025", "1", 10L)).thenReturn(moved);

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.manualAdvanceOneClassTx("2025", "1", 1L));

        assertTrue(error.getMessage().contains("当前课程已变化"));
        verifyNoInteractions(lessonMapper, studentMapper, studentAnswerMapper, lessonClassScopeMapper);
    }

    @Test
    void partialSuccessKeepsPerClassResultsAndStopsAfterSystemFailure()
    {
        LessonAutoAdvanceServiceImpl proxy = spy(service);
        ReflectionTestUtils.setField(service, "self", proxy);
        Map<String, Object> success = new java.util.HashMap<>();
        success.put("advanced", 1);
        success.put("message", "1班已进入下一课");
        doReturn(success).when(proxy).manualAdvanceOneClassTx("2025", "1", 1L);
        doThrow(new IllegalStateException("database unavailable"))
                .when(proxy).manualAdvanceOneClassTx("2025", "2", 1L);
        Map<String, Long> expected = new java.util.HashMap<>();
        expected.put("1", 1L); expected.put("2", 1L); expected.put("3", 1L);

        Map<String, Object> result = service.manualAdvanceClasses("2025", Arrays.asList("1", "2", "3"), expected);

        assertEquals(1, result.get("advanced"));
        assertEquals(2, result.get("failed"));
        java.util.List<?> details = (java.util.List<?>) result.get("details");
        assertEquals(3, details.size());
        assertEquals(true, ((Map<?, ?>) details.get(0)).get("success"));
        verify(proxy, never()).manualAdvanceOneClassTx("2025", "3", 1L);
    }

    @Test
    void missingPreviewSnapshotBlocksExecution()
    {
        Map<String, Object> result = service.manualAdvanceClasses("2025", Collections.singletonList("1"), Collections.emptyMap());
        assertEquals(0, result.get("advanced"));
        assertEquals(1, result.get("failed"));
        verifyNoInteractions(assignmentMapper, lessonMapper);
    }

    @Test
    void manualExecutionUsesLiveCountsAndSnapshotThenRecordsExactlyOneAdvance()
    {
        BizLesson current = manualClass(39, 40);
        BizLesson next = lesson(2L, 4, "assessment");
        when(lessonMapper.selectBizLessonList(any(BizLesson.class))).thenReturn(Arrays.asList(current, next));
        when(assignmentMapper.advanceCurrentAssignment(eq(101L), eq(1L), eq(2L), eq(40L), any()))
                .thenReturn(1);

        Map<String, Object> result = service.manualAdvanceClasses("2025", Collections.singletonList("1"),
                Collections.singletonMap("1", 1L));

        assertEquals(1, result.get("advanced"));
        verify(assignmentMapper).insertAdvanceHistory(any(), eq(2L), eq(40L), eq("MANUAL"), any());
        verify(lessonClassScopeMapper).markAssignmentInactive(1L, 10L, "2025", "1");
        verify(lessonClassScopeMapper).upsertCurrentAssignment(any());
    }

    @Test
    void manualExecutionRechecksThresholdEvenIfPreviewPreviouslyPassed()
    {
        manualClass(19, 40);
        Map<String, Object> result = service.manualAdvanceClasses("2025", Collections.singletonList("1"),
                Collections.singletonMap("1", 1L));
        assertEquals(0, result.get("advanced"));
        assertEquals(1, result.get("failed"));
        verify(assignmentMapper, never()).advanceCurrentAssignment(anyLong(), anyLong(), anyLong(), anyLong(), any());
        verifyNoInteractions(lessonClassScopeMapper);
    }

    @Test
    void manualExecutionDoesNotInventNextLessonWhenNoneExists()
    {
        BizLesson current = manualClass(39, 40);
        when(lessonMapper.selectBizLessonList(any(BizLesson.class))).thenReturn(Collections.singletonList(current));
        Map<String, Object> result = service.manualAdvanceClasses("2025", Collections.singletonList("1"),
                Collections.singletonMap("1", 1L));
        assertEquals(0, result.get("advanced"));
        assertEquals(1, result.get("failed"));
        verifyNoInteractions(lessonClassScopeMapper);
    }

    private BizLesson manualClass(int ready, int total)
    {
        loginTeacher();
        when(teacherClassMapper.checkTeacherClassExists(any())).thenReturn(1);
        when(assignmentMapper.selectCurrentAssignmentForUpdate("2025", "1", 10L))
                .thenReturn(assignment(101L, "1", null));
        BizLesson current = lesson(1L, 1, "assessment");
        when(lessonMapper.selectBizLessonByLessonId(1L)).thenReturn(current);
        when(studentAnswerMapper.countScoredStudentsByLessonAndClass(1L, "1", "2025", 10L)).thenReturn(ready);
        when(studentMapper.countByDeptIdAndClass(10L, "2025", "1")).thenReturn(total);
        return current;
    }

    private void loginTeacher()
    {
        SysUser user = new SysUser();
        user.setUserId(40L); user.setDeptId(10L); user.setUserName("teacher");
        LoginUser login = new LoginUser(40L, 10L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(login, null, Collections.emptyList()));
    }

    private BizLesson previewClass(int ready, int total)
    {
        loginTeacher();
        when(teacherClassMapper.checkTeacherClassExists(any())).thenReturn(1);
        when(assignmentMapper.selectCurrentAssignment("2025", "1", 10L)).thenReturn(assignment(101L, "1", null));
        BizLesson current = lesson(1L, 1, "assessment");
        when(lessonMapper.selectBizLessonByLessonId(1L)).thenReturn(current);
        when(studentAnswerMapper.countScoredStudentsByLessonAndClass(1L, "1", "2025", 10L)).thenReturn(ready);
        when(studentMapper.countByDeptIdAndClass(10L, "2025", "1")).thenReturn(total);
        return current;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> previewRow()
    {
        Map<String, Object> result = service.previewAdvanceClasses("2025", Arrays.asList("1班", "1"));
        java.util.List<Map<String, Object>> rows = (java.util.List<Map<String, Object>>) result.get("rows");
        assertEquals(1, rows.size());
        return rows.get(0);
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
    void unmetManualAdvanceConditionReturnsStructuredBusinessResult()
    {
        LessonAutoAdvanceServiceImpl proxy = spy(service);
        ReflectionTestUtils.setField(service, "self", proxy);
        doThrow(new ServiceException("有成绩 0/43 人，需达到 50%"))
                .when(proxy).manualAdvanceOneClassTx("2025", "7");

        Map<String, Object> result = service.manualAdvanceClasses("2025", Collections.singletonList("7班"));

        assertEquals(0, result.get("advanced"));
        assertEquals(1, result.get("failed"));
        assertEquals("成功推进 0 个班级，1 个未达条件或无法推进（7班：有成绩 0/43 人，需达到 50%）",
                result.get("message"));
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
