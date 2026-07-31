package com.ruoyi.business.service.impl;

import com.ruoyi.business.domain.BizLessonGuideSheetBinding;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizTeacherClass;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.domain.vo.GradeGroupVo;
import com.ruoyi.business.domain.vo.LessonDetailVo;
import com.ruoyi.business.domain.vo.LessonInfoVo;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.GuideSheetBindingMapper;
import com.ruoyi.business.mapper.BizTeacherClassMapper;
import com.ruoyi.business.util.AcademicYearUtils;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.ruoyi.system.mapper.SysDeptMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class BizLessonServiceImplTest
{
    @Mock
    private GuideSheetBindingMapper guideSheetBindingMapper;
    @Mock
    private BizLessonMapper bizLessonMapper;
    @Mock
    private BizLessonAssignmentMapper lessonAssignmentMapper;
    @Mock
    private BizTeacherClassMapper teacherClassMapper;
    @Mock
    private SysDeptMapper deptMapper;

    @InjectMocks
    private BizLessonServiceImpl service;

    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void blankNewLessonIsRejected()
    {
        LessonDetailVo detail = new LessonDetailVo();
        detail.setGuideSheetEnabled(false);
        detail.setQuestions(Collections.emptyList());

        assertThrows(ServiceException.class, () -> validate(detail));
    }

    @Test
    void guideSheetOnlyLessonIsAllowedWhenTemplateIsSelected()
    {
        LessonDetailVo detail = new LessonDetailVo();
        detail.setGuideSheetEnabled(true);
        detail.setSourceSheetId(5L);
        detail.setQuestions(Collections.emptyList());

        assertDoesNotThrow(() -> validate(detail));
    }

    @Test
    void closedGuideSheetOnlyLessonCanKeepItsHistoricalBinding()
    {
        LessonDetailVo detail = new LessonDetailVo();
        detail.setLessonId(3L);
        detail.setGuideSheetEnabled(false);
        detail.setQuestions(Collections.emptyList());
        when(guideSheetBindingMapper.selectCurrentByLessonId(3L))
                .thenReturn(new BizLessonGuideSheetBinding());

        assertDoesNotThrow(() -> validate(detail));
    }

    @Test
    void ordinaryQuestionsStillRequireOneHundredPoints()
    {
        LessonDetailVo detail = new LessonDetailVo();
        detail.setGuideSheetEnabled(false);
        detail.setQuestions(Collections.singletonList(question(90)));

        assertThrows(ServiceException.class, () -> validate(detail));
    }

    @Test
    void ordinaryQuestionsAtOneHundredPointsRemainValid()
    {
        LessonDetailVo detail = new LessonDetailVo();
        detail.setGuideSheetEnabled(false);
        detail.setQuestions(Collections.singletonList(question(100)));

        assertDoesNotThrow(() -> validate(detail));
    }

    @Test
    void teacherWithoutLessonsStillGetsStablePolicyDefaults()
    {
        loginTeacher();
        when(bizLessonMapper.selectAdvancePolicyByTeacher(8L, 10L)).thenReturn(null);

        Map<String, Object> result = service.getTeacherAdvancePolicy();

        assertFalse((Boolean) result.get("hasPolicy"));
        assertFalse((Boolean) result.get("autoAdvanceEnabled"));
        assertEquals(50, result.get("autoAdvanceThresholdPct"));
        assertEquals(new BigDecimal("2.0"), result.get("autoAdvanceDelayHours"));
    }

    @Test
    void savingPolicyPersistsEvenWhenNoLessonExists()
    {
        loginTeacher();
        LessonDetailVo request = new LessonDetailVo();
        request.setAutoAdvanceEnabled(Boolean.TRUE);
        request.setAutoAdvanceThresholdPct(70);
        request.setAutoAdvanceDelayHours(new BigDecimal("0.25"));
        BizLesson stored = new BizLesson();
        stored.setAutoAdvanceEnabled(Boolean.TRUE);
        stored.setAutoAdvanceThresholdPct(70);
        stored.setAutoAdvanceDelayHours(new BigDecimal("0.5"));
        when(bizLessonMapper.selectAdvancePolicyByTeacher(8L, 10L)).thenReturn(stored);
        when(bizLessonMapper.updateAdvancePolicyByCreator(
                8L, "teacher", 10L, true, 70, new BigDecimal("0.5"), "teacher"))
                .thenReturn(0);

        Map<String, Object> result = service.updateTeacherAdvancePolicy(request);

        assertTrue((Boolean) result.get("hasPolicy"));
        assertEquals(0, result.get("updatedLessons"));
        verify(bizLessonMapper).upsertAdvancePolicy(
                8L, 10L, true, 70, new BigDecimal("0.5"), "teacher");
        verify(lessonAssignmentMapper).clearReadyTimesByTeacher(8L, "teacher", 10L);
    }

    @Test
    void newAssessmentInheritsPolicyButAttendanceAlwaysDisablesAdvance()
    {
        BizLesson policy = new BizLesson();
        policy.setAutoAdvanceEnabled(Boolean.TRUE);
        policy.setAutoAdvanceThresholdPct(80);
        policy.setAutoAdvanceDelayHours(new BigDecimal("3.0"));
        when(bizLessonMapper.selectAdvancePolicyByTeacher(8L, 10L)).thenReturn(policy);
        BizLesson assessment = new BizLesson();

        ReflectionTestUtils.invokeMethod(service, "applyTeacherAdvancePolicyToLesson",
                assessment, 8L, 10L, "assessment");

        assertTrue(assessment.getAutoAdvanceEnabled());
        assertEquals(80, assessment.getAutoAdvanceThresholdPct());
        assertEquals(new BigDecimal("3.0"), assessment.getAutoAdvanceDelayHours());

        BizLesson attendance = new BizLesson();
        ReflectionTestUtils.invokeMethod(service, "applyTeacherAdvancePolicyToLesson",
                attendance, 8L, 10L, "attendance");
        assertFalse(attendance.getAutoAdvanceEnabled());
        assertEquals(50, attendance.getAutoAdvanceThresholdPct());
    }

    @Test
    void dashboardKeepsLessonsInTheirPersistedCohortAcrossAcademicYears()
    {
        loginTeacher();
        int academicStartYear = AcademicYearUtils.resolveAcademicStartYear(LocalDate.now());
        String ninthGradeEntryYear = String.valueOf(academicStartYear - 2);
        String eighthGradeEntryYear = String.valueOf(academicStartYear - 1);
        SysDept school = new SysDept();
        school.setDeptId(10L);
        school.setSchoolType("2");
        when(deptMapper.selectDeptById(10L)).thenReturn(school);
        when(teacherClassMapper.selectBizTeacherClassList(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Arrays.asList(teacherClass(ninthGradeEntryYear, "1"),
                        teacherClass(eighthGradeEntryYear, "1")));

        LessonInfoVo ninthGradeLesson = lessonInfo(24L, ninthGradeEntryYear);
        LessonInfoVo eighthGradeLesson = lessonInfo(25L, eighthGradeEntryYear);
        when(bizLessonMapper.selectLessonsByEntryYearAndCreator(ninthGradeEntryYear, "teacher", 10L))
                .thenReturn(Collections.singletonList(ninthGradeLesson));
        when(bizLessonMapper.selectLessonsByEntryYearAndCreator(eighthGradeEntryYear, "teacher", 10L))
                .thenReturn(Collections.singletonList(eighthGradeLesson));
        when(bizLessonMapper.selectSharedLessonsByEntryYearAndUser(
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(8L),
                org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.eq("teacher")))
                .thenReturn(Collections.emptyList());
        when(lessonAssignmentMapper.selectClassCodesByLessonIdAndEntryYear(
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Collections.emptyList());

        List<GradeGroupVo> groups = service.getTeacherDashboardData();

        GradeGroupVo ninthGradeGroup = groups.stream()
                .filter(g -> ninthGradeEntryYear.equals(g.getEntryYear())).findFirst().orElse(null);
        GradeGroupVo eighthGradeGroup = groups.stream()
                .filter(g -> eighthGradeEntryYear.equals(g.getEntryYear())).findFirst().orElse(null);
        assertNotNull(ninthGradeGroup);
        assertNotNull(eighthGradeGroup);
        assertEquals(Collections.singletonList(ninthGradeLesson), ninthGradeGroup.getLessons());
        assertEquals(Collections.singletonList(eighthGradeLesson), eighthGradeGroup.getLessons());
        assertEquals("九年级", ninthGradeGroup.getGradeName());
        assertEquals("八年级", eighthGradeGroup.getGradeName());
    }

    @Test
    void dashboardLoadsLessonsForGraduatedEntryYear()
    {
        // 小学 2020 级在 7 月 20 日后为「已毕业」(gradeId=-1)，旧逻辑因 gradeId>0 才装课导致空卡片。
        loginTeacher();
        int academicStartYear = AcademicYearUtils.resolveAcademicStartYear(LocalDate.now());
        String graduatedEntryYear = String.valueOf(academicStartYear - 6);
        SysDept school = new SysDept();
        school.setDeptId(10L);
        school.setSchoolType("1");
        when(deptMapper.selectDeptById(10L)).thenReturn(school);
        when(teacherClassMapper.selectBizTeacherClassList(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Collections.singletonList(teacherClass(graduatedEntryYear, "1")));

        LessonInfoVo graduatedLesson = lessonInfo(201L, graduatedEntryYear);
        when(bizLessonMapper.selectLessonsByEntryYearAndCreator(graduatedEntryYear, "teacher", 10L))
                .thenReturn(Collections.singletonList(graduatedLesson));
        when(bizLessonMapper.selectSharedLessonsByEntryYearAndUser(
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(8L),
                org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.eq("teacher")))
                .thenReturn(Collections.emptyList());
        when(lessonAssignmentMapper.selectClassCodesByLessonIdAndEntryYear(
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Collections.singletonList("1"));

        List<GradeGroupVo> groups = service.getTeacherDashboardData();

        GradeGroupVo graduatedGroup = groups.stream()
                .filter(g -> graduatedEntryYear.equals(g.getEntryYear())).findFirst().orElse(null);
        assertNotNull(graduatedGroup);
        assertEquals("已毕业", graduatedGroup.getGradeName());
        assertEquals(Long.valueOf(-1L), graduatedGroup.getGradeId());
        assertEquals(Collections.singletonList(graduatedLesson), graduatedGroup.getLessons());
    }

    @Test
    void persistedLessonEntryYearCannotDriftDuringEdit()
    {
        BizLesson existing = new BizLesson();
        existing.setEntryYear("2024");

        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                service, "preserveLessonEntryYear", existing, "2024"));
        assertThrows(ServiceException.class, () -> ReflectionTestUtils.invokeMethod(
                service, "preserveLessonEntryYear", existing, "2025"));
    }

    @Test
    void ordinaryCreateAlwaysWritesCreateTime()
    {
        loginTeacher();
        BizLesson lesson = new BizLesson();
        lesson.setEntryYear("2025");

        service.insertBizLesson(lesson);

        ArgumentCaptor<BizLesson> captor = ArgumentCaptor.forClass(BizLesson.class);
        verify(bizLessonMapper).insertBizLesson(captor.capture());
        assertNotNull(captor.getValue().getCreateTime());
    }

    @Test
    void teacherEditAlwaysWritesUpdateTime()
    {
        loginTeacher();
        BizLesson existing = new BizLesson();
        existing.setLessonId(10L);
        existing.setCreatorId(8L);
        existing.setDeptId(10L);
        existing.setEntryYear("2025");
        when(bizLessonMapper.selectBizLessonByLessonId(10L)).thenReturn(existing);
        when(bizLessonMapper.updateBizLesson(org.mockito.ArgumentMatchers.any())).thenReturn(1);
        BizLesson update = new BizLesson();
        update.setLessonId(10L);
        update.setEntryYear("2025");

        service.updateBizLesson(update);

        ArgumentCaptor<BizLesson> captor = ArgumentCaptor.forClass(BizLesson.class);
        verify(bizLessonMapper).updateBizLesson(captor.capture());
        assertNotNull(captor.getValue().getUpdateTime());
    }

    private BizLessonQuestionDetailVo question(long score)
    {
        BizLessonQuestionDetailVo question = new BizLessonQuestionDetailVo();
        question.setQuestionId(9L);
        question.setQuestionScore(score);
        return question;
    }

    private BizTeacherClass teacherClass(String entryYear, String classCode)
    {
        BizTeacherClass teacherClass = new BizTeacherClass();
        teacherClass.setUserId(8L);
        teacherClass.setDeptId(10L);
        teacherClass.setEntryYear(entryYear);
        teacherClass.setClassCode(classCode);
        return teacherClass;
    }

    private LessonInfoVo lessonInfo(Long lessonId, String entryYear)
    {
        LessonInfoVo lesson = new LessonInfoVo();
        lesson.setLessonId(lessonId);
        lesson.setEntryYear(entryYear);
        return lesson;
    }

    private void validate(LessonDetailVo detail)
    {
        ReflectionTestUtils.invokeMethod(service, "validateLessonContent", detail);
    }

    private void loginTeacher()
    {
        SysUser user = new SysUser();
        user.setUserId(8L);
        user.setDeptId(10L);
        user.setUserName("teacher");
        LoginUser loginUser = new LoginUser(8L, 10L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }
}
