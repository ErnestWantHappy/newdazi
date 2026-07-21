package com.ruoyi.business.service;

import java.util.Collections;

import com.ruoyi.business.domain.BizGuideSheet;
import com.ruoyi.business.domain.BizGuideSheetProgress;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizLessonAssignment;
import com.ruoyi.business.domain.BizLessonGuideSheetBinding;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizTeacherClassMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.GuideSheetBindingMapper;
import com.ruoyi.business.mapper.GuideSheetProgressMapper;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuideSheetAccessServiceTest
{
    @Mock
    private GuideSheetBindingMapper bindingMapper;
    @Mock
    private BizLessonMapper lessonMapper;
    @Mock
    private BizLessonAssignmentMapper lessonAssignmentMapper;
    @Mock
    private BizTeacherClassMapper teacherClassMapper;
    @Mock
    private BizStudentAnswerMapper studentAnswerMapper;
    @Mock
    private ICountyExamService countyExamService;
    @Mock
    private GuideSheetProgressMapper progressMapper;

    @InjectMocks
    private GuideSheetAccessService service;

    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void publicTemplateIsVisibleOnlyInsideCounty()
    {
        BizGuideSheet sheet = new BizGuideSheet();
        sheet.setCreatorId(1L);
        sheet.setCountyDeptId(100L);
        sheet.setIsPublic("Y");
        sheet.setDelFlag("0");

        assertTrue(service.isTemplateVisibleTo(sheet, 2L, 100L, false));
        assertFalse(service.isTemplateVisibleTo(sheet, 2L, 200L, false));
        sheet.setDelFlag("2");
        assertFalse(service.isTemplateVisibleTo(sheet, 2L, 100L, false));
        assertTrue(service.isTemplateVisibleTo(sheet, 1L, 100L, false));
    }

    @Test
    void privateTemplateIsVisibleOnlyToCreatorOrAdministrator()
    {
        BizGuideSheet sheet = new BizGuideSheet();
        sheet.setCreatorId(1L);
        sheet.setCountyDeptId(100L);
        sheet.setIsPublic("N");
        sheet.setDelFlag("0");

        assertTrue(service.isTemplateVisibleTo(sheet, 1L, 100L, false));
        assertFalse(service.isTemplateVisibleTo(sheet, 2L, 100L, false));
        assertFalse(service.isTemplateVisibleTo(sheet, 2L, 200L, false));
        assertTrue(service.isTemplateVisibleTo(sheet, 2L, 200L, true));
    }

    @Test
    void studentMustUseBindingOfExactCurrentAssignedLesson()
    {
        BizStudent student = student();
        BizLessonGuideSheetBinding binding = binding();
        when(countyExamService.checkCurrentStudentExam()).thenReturn(Collections.emptyMap());
        when(bindingMapper.selectByBindingId(7L)).thenReturn(binding);
        BizLesson lesson = new BizLesson();
        lesson.setLessonId(3L);
        lesson.setDeptId(10L);
        when(lessonMapper.selectBizLessonByLessonId(3L)).thenReturn(lesson);
        when(lessonAssignmentMapper.selectCurrentLessonByClass("2025", "1", 10L)).thenReturn(3L);
        when(bindingMapper.selectEnabledByLessonId(3L)).thenReturn(binding);

        assertSame(binding, service.requireStudentBinding(student, 7L));

        when(lessonAssignmentMapper.selectCurrentLessonByClass("2025", "1", 10L)).thenReturn(4L);
        assertThrows(ServiceException.class, () -> service.requireStudentBinding(student, 7L));
    }

    @Test
    void pendingCountyExamBlocksDailyStudentAccess()
    {
        java.util.Map<String, Object> currentExam = new java.util.LinkedHashMap<>();
        currentExam.put("hasExam", true);
        currentExam.put("ended", false);
        when(countyExamService.checkCurrentStudentExam()).thenReturn(currentExam);

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.assertNoPendingCountyExam());

        assertTrue(error.getMessage().contains("区域抽测"));
    }

    @Test
    void transferredStudentCanStillBeGradedThroughHistoricalClassSnapshot()
    {
        BizGuideSheetProgress progress = new BizGuideSheetProgress();
        progress.setBindingId(7L);
        progress.setStudentId(9L);
        progress.setDeptId(10L);
        progress.setEntryYear("2025");
        progress.setClassCode("1");
        when(progressMapper.selectByBindingAndStudent(7L, 9L)).thenReturn(progress);

        service.assertStudentInBindingClass(7L, 9L, 10L, "2025", "1班");

        assertThrows(ServiceException.class,
                () -> service.assertStudentInBindingClass(7L, 9L, 10L, "2025", "2班"));
    }

    @Test
    void lessonCreatorMayViewActuallyAssignedClassWithoutTeacherClassGrant()
    {
        login(40L, 10L, "creator");
        BizLessonGuideSheetBinding binding = binding();
        BizLesson lesson = new BizLesson();
        lesson.setLessonId(3L);
        lesson.setDeptId(10L);
        lesson.setCreatorId(40L);
        lesson.setEntryYear("2025");
        BizLessonAssignment assignment = new BizLessonAssignment();
        assignment.setLessonId(3L);
        assignment.setDeptId(10L);
        assignment.setEntryYear("2025");
        assignment.setClassCode("1");
        when(bindingMapper.selectByBindingId(7L)).thenReturn(binding);
        when(lessonMapper.selectBizLessonByLessonId(3L)).thenReturn(lesson);
        when(lessonAssignmentMapper.selectBizLessonAssignmentList(any()))
                .thenReturn(Collections.singletonList(assignment));

        assertSame(binding, service.requireBindingClassAccess(7L, "2025", "1班"));
    }

    @Test
    void historicalAnswerKeepsExactLessonClassReadableAfterAssignmentAdvances()
    {
        login(40L, 10L, "creator");
        BizLesson lesson = new BizLesson();
        lesson.setLessonId(3L);
        lesson.setDeptId(10L);
        lesson.setCreatorId(40L);
        lesson.setEntryYear("2024");
        when(lessonMapper.selectBizLessonByLessonId(3L)).thenReturn(lesson);
        when(lessonAssignmentMapper.selectBizLessonAssignmentList(any())).thenReturn(Collections.emptyList());
        when(studentAnswerMapper.existsLessonClassAnswer(3L, "1", "2024", 10L)).thenReturn(1);

        service.assertCanViewLessonClass(3L, "2024", "1班");

        assertThrows(ServiceException.class,
                () -> service.assertCanViewLessonClass(3L, "2025", "1班"));
    }

    @Test
    void lessonEntryYearMismatchFailsClosedBeforeClassEvidence()
    {
        login(40L, 10L, "creator");
        BizLesson lesson = new BizLesson();
        lesson.setLessonId(3L);
        lesson.setDeptId(10L);
        lesson.setCreatorId(40L);
        lesson.setEntryYear("2024");
        when(lessonMapper.selectBizLessonByLessonId(3L)).thenReturn(lesson);

        assertThrows(ServiceException.class,
                () -> service.assertCanViewLessonClass(3L, "2025", "1班"));
    }

    private void login(Long userId, Long deptId, String username)
    {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setDeptId(deptId);
        user.setUserName(username);
        LoginUser loginUser = new LoginUser(userId, deptId, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }

    private BizStudent student()
    {
        BizStudent student = new BizStudent();
        student.setStudentId(9L);
        student.setDeptId(10L);
        student.setEntryYear("2025");
        student.setClassCode("1");
        return student;
    }

    private BizLessonGuideSheetBinding binding()
    {
        BizLessonGuideSheetBinding binding = new BizLessonGuideSheetBinding();
        binding.setBindingId(7L);
        binding.setLessonId(3L);
        binding.setIsCurrent("Y");
        binding.setEnabled("Y");
        return binding;
    }
}
