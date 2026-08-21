package com.ruoyi.business.service.impl;

import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizLessonAssignment;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.LessonClassScopeMapper;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BizLessonAssignmentServiceImplTest
{
    @Mock
    private BizLessonAssignmentMapper assignmentMapper;
    @Mock
    private BizLessonMapper lessonMapper;
    @Mock
    private LessonClassScopeMapper scopeMapper;

    @InjectMocks
    private BizLessonAssignmentServiceImpl service;

    @BeforeEach
    void setUpSecurityContext()
    {
        SysUser user = new SysUser();
        user.setUserId(2L);
        user.setDeptId(7L);
        user.setUserName("teacher");
        LoginUser loginUser = new LoginUser(2L, 7L, user, java.util.Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, java.util.Collections.emptyList()));
    }

    @Test
    void validAssignmentInheritsLessonSchoolAndNormalizesClass()
    {
        BizLesson lesson = lesson(10L, 7L);
        BizLessonAssignment assignment = assignment(10L, null, " 3班 ");
        when(lessonMapper.selectBizLessonByLessonId(10L)).thenReturn(lesson);
        when(assignmentMapper.insertBizLessonAssignment(assignment)).thenReturn(1);

        assertEquals(1, service.insertBizLessonAssignment(assignment));
        assertEquals(7L, assignment.getDeptId());
        assertEquals("3", assignment.getClassCode());
        verify(scopeMapper).upsertCurrentAssignment(assignment);
    }

    @Test
    void missingLessonIsRejectedBeforeInsert()
    {
        BizLessonAssignment assignment = assignment(99L, 7L, "3");
        when(lessonMapper.selectBizLessonByLessonId(99L)).thenReturn(null);

        assertThrows(ServiceException.class, () -> service.insertBizLessonAssignment(assignment));
        verify(assignmentMapper, never()).insertBizLessonAssignment(any());
    }

    @Test
    void lessonWithoutSchoolIsRejected()
    {
        BizLessonAssignment assignment = assignment(10L, null, "3");
        when(lessonMapper.selectBizLessonByLessonId(10L)).thenReturn(lesson(10L, null));

        assertThrows(ServiceException.class, () -> service.insertBizLessonAssignment(assignment));
        verify(assignmentMapper, never()).insertBizLessonAssignment(any());
    }

    @Test
    void mismatchedSchoolIsRejected()
    {
        BizLessonAssignment assignment = assignment(10L, 8L, "3");
        when(lessonMapper.selectBizLessonByLessonId(10L)).thenReturn(lesson(10L, 7L));

        assertThrows(ServiceException.class, () -> service.insertBizLessonAssignment(assignment));
        verify(assignmentMapper, never()).insertBizLessonAssignment(any());
    }

    @Test
    void differentCurrentSchoolCannotCreateAssignment()
    {
        BizLessonAssignment assignment = assignment(10L, null, "3");
        when(lessonMapper.selectBizLessonByLessonId(10L)).thenReturn(lesson(10L, 8L));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.insertBizLessonAssignment(assignment));
        assertEquals(HttpStatus.FORBIDDEN, exception.getCode());
        verify(assignmentMapper, never()).insertBizLessonAssignment(any());
    }

    private BizLesson lesson(Long lessonId, Long deptId)
    {
        BizLesson lesson = new BizLesson();
        lesson.setLessonId(lessonId);
        lesson.setDeptId(deptId);
        return lesson;
    }

    private BizLessonAssignment assignment(Long lessonId, Long deptId, String classCode)
    {
        BizLessonAssignment assignment = new BizLessonAssignment();
        assignment.setLessonId(lessonId);
        assignment.setDeptId(deptId);
        assignment.setEntryYear("2025");
        assignment.setClassCode(classCode);
        return assignment;
    }
}
