package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.framework.web.service.PermissionService;

class TextbookStudentAccessServiceTest
{
    private static final Long USER_ID = 9001L;
    private static final Long DEPT_ID = 103L;
    private static final Long LESSON_ID = 252L;
    private static final Long BOUND_BOOK_ID = 6L;

    private TextbookStudentAccessService service;
    private PermissionService permissionService;
    private BizStudentMapper studentMapper;
    private BizLessonAssignmentMapper assignmentMapper;
    private BizLessonMapper lessonMapper;

    @BeforeEach
    void setUp()
    {
        service = new TextbookStudentAccessService();
        permissionService = mock(PermissionService.class);
        studentMapper = mock(BizStudentMapper.class);
        assignmentMapper = mock(BizLessonAssignmentMapper.class);
        lessonMapper = mock(BizLessonMapper.class);
        ReflectionTestUtils.setField(service, "permissionService", permissionService);
        ReflectionTestUtils.setField(service, "studentMapper", studentMapper);
        ReflectionTestUtils.setField(service, "lessonAssignmentMapper", assignmentMapper);
        ReflectionTestUtils.setField(service, "lessonMapper", lessonMapper);
        when(studentMapper.selectBizStudentByUserId(USER_ID)).thenReturn(student());
        when(assignmentMapper.selectCurrentLessonByClass("2025", "01", DEPT_ID)).thenReturn(LESSON_ID);
        when(lessonMapper.selectBizLessonByLessonId(LESSON_ID)).thenReturn(lesson(BOUND_BOOK_ID, DEPT_ID));
    }

    @Test
    void staffKeepsFullShelfAccessWithoutStudentLookup()
    {
        when(permissionService.hasAnyRoles("admin,teacher,researcher")).thenReturn(true);

        assertDoesNotThrow(() -> service.assertCanReadBook(BOUND_BOOK_ID));

        verifyNoInteractions(studentMapper, assignmentMapper, lessonMapper);
    }

    @Test
    void studentCanReadCurrentBoundBook()
    {
        assertDoesNotThrow(() -> service.assertStudentBookBinding(BOUND_BOOK_ID, USER_ID, DEPT_ID));
    }

    @Test
    void studentCannotSwitchToAnotherBookByChangingUrl()
    {
        ServiceException error = assertThrows(ServiceException.class,
                () -> service.assertStudentBookBinding(7L, USER_ID, DEPT_ID));

        assertEquals("仅可阅读当前课程绑定的教材", error.getMessage());
    }

    @Test
    void studentCannotJoinLessonFromAnotherSchool()
    {
        when(lessonMapper.selectBizLessonByLessonId(LESSON_ID)).thenReturn(lesson(BOUND_BOOK_ID, 999L));

        assertThrows(ServiceException.class,
                () -> service.assertStudentBookBinding(BOUND_BOOK_ID, USER_ID, DEPT_ID));
    }

    @Test
    void studentWithoutCurrentAssignmentGetsBusinessError()
    {
        when(assignmentMapper.selectCurrentLessonByClass("2025", "01", DEPT_ID)).thenReturn(null);

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.assertStudentBookBinding(BOUND_BOOK_ID, USER_ID, DEPT_ID));

        assertEquals("当前课程未绑定教材", error.getMessage());
    }

    private BizStudent student()
    {
        BizStudent student = new BizStudent();
        student.setStudentId(3001L);
        student.setUserId(USER_ID);
        student.setEntryYear("2025");
        student.setClassCode("01");
        return student;
    }

    private BizLesson lesson(Long textbookBookId, Long deptId)
    {
        BizLesson lesson = new BizLesson();
        lesson.setLessonId(LESSON_ID);
        lesson.setTextbookBookId(textbookBookId);
        lesson.setDeptId(deptId);
        return lesson;
    }
}
