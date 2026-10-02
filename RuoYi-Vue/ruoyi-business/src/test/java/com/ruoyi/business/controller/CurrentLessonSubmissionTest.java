package com.ruoyi.business.controller;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizLessonAssignment;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.service.ClassroomTaskStateService;
import com.ruoyi.business.service.FlowchartService;
import com.ruoyi.business.service.FlowchartDocumentService;
import com.ruoyi.business.service.FlowchartStructureCheckService;
import com.ruoyi.business.service.GuideSheetAccessService;
import com.ruoyi.business.service.PracticalArtifactService;
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.business.mapper.FlowchartMapper;
import com.ruoyi.common.exception.ServiceException;

class CurrentLessonSubmissionTest {
    @Test
    void studentSubmissionRejectsPreviousLessonEvenImmediatelyAfterAdvance() {
        StudentHomeController controller = new StudentHomeController();
        BizLessonMapper lessons = mock(BizLessonMapper.class);
        BizLessonAssignmentMapper assignments = mock(BizLessonAssignmentMapper.class);
        ReflectionTestUtils.setField(controller, "lessonMapper", lessons);
        ReflectionTestUtils.setField(controller, "lessonAssignmentMapper", assignments);
        BizLesson oldLesson = new BizLesson();
        oldLesson.setLessonId(10L);
        oldLesson.setDeptId(9L);
        when(lessons.selectBizLessonByLessonId(10L)).thenReturn(oldLesson);
        when(assignments.selectCurrentLessonByClass("2025", "1", 9L)).thenReturn(11L, 10L);
        BizStudent student = student();

        String rejected = ReflectionTestUtils.invokeMethod(controller,
                "validateSubmissionAccess", student, 9L, 10L);
        String allowed = ReflectionTestUtils.invokeMethod(controller,
                "validateSubmissionAccess", student, 9L, 10L);

        assertTrue(rejected.contains("仅能提交当前课程"));
        assertNull(allowed);
    }

    @Test
    void teacherCannotReturnPreviousLesson() {
        ClassroomTaskStateController controller = new ClassroomTaskStateController();
        GuideSheetAccessService access = mock(GuideSheetAccessService.class);
        BizLessonQuestionMapper questions = mock(BizLessonQuestionMapper.class);
        BizStudentMapper students = mock(BizStudentMapper.class);
        BizLessonAssignmentMapper assignments = mock(BizLessonAssignmentMapper.class);
        ClassroomTaskStateService states = mock(ClassroomTaskStateService.class);
        ReflectionTestUtils.setField(controller, "guideSheetAccessService", access);
        ReflectionTestUtils.setField(controller, "lessonQuestionMapper", questions);
        ReflectionTestUtils.setField(controller, "studentMapper", students);
        ReflectionTestUtils.setField(controller, "assignmentMapper", assignments);
        ReflectionTestUtils.setField(controller, "answerMapper", mock(BizStudentAnswerMapper.class));
        ReflectionTestUtils.setField(controller, "taskStateService", states);
        when(access.requireViewableLessonClassDept(10L, "2025", "1")).thenReturn(9L);
        BizLessonQuestionDetailVo question = new BizLessonQuestionDetailVo();
        question.setQuestionId(20L);
        when(questions.selectDetailsByLessonId(10L)).thenReturn(Collections.singletonList(question));
        when(students.selectBizStudentByStudentId(30L)).thenReturn(student());
        BizLessonAssignment current = new BizLessonAssignment();
        current.setLessonId(11L);
        when(assignments.selectCurrentAssignmentForUpdate("2025", "1", 9L)).thenReturn(current);
        ClassroomTaskStateController.StateRequest request = new ClassroomTaskStateController.StateRequest();
        request.setLessonId(10L);
        request.setQuestionId(20L);
        request.setStudentId(30L);
        request.setEntryYear("2025");
        request.setClassCode("1");

        assertThrows(ServiceException.class, () -> controller.returnForResubmission(request));
        verifyNoInteractions(states);
    }

    @Test
    void fileWorkCannotCreateNewVersionAfterAdvance() {
        PracticalArtifactService service = new PracticalArtifactService();
        BizStudentMapper students = mock(BizStudentMapper.class);
        BizLessonAssignmentMapper assignments = mock(BizLessonAssignmentMapper.class);
        ReflectionTestUtils.setField(service, "studentMapper", students);
        ReflectionTestUtils.setField(service, "lessonAssignmentMapper", assignments);
        when(students.selectBizStudentByStudentId(30L)).thenReturn(student());
        BizLessonAssignment current = new BizLessonAssignment();
        current.setLessonId(11L);
        when(assignments.selectCurrentAssignmentForUpdate("2025", "1", 9L)).thenReturn(current);

        assertThrows(ServiceException.class, () -> service.submitLessonArtifact(30L, 40L,
                new BizLessonQuestionDetailVo(), 10L, null, Collections.emptyList()));
    }

    @Test
    void returnedFlowchartCannotReopenAfterAdvance() {
        BizStudentMapper students = mock(BizStudentMapper.class);
        BizLessonAssignmentMapper assignments = mock(BizLessonAssignmentMapper.class);
        ClassroomTaskStateService states = mock(ClassroomTaskStateService.class);
        when(students.selectBizStudentByUserId(40L)).thenReturn(student());
        BizLessonAssignment current = new BizLessonAssignment();
        current.setLessonId(11L);
        when(assignments.selectCurrentAssignmentForUpdate("2025", "1", 9L)).thenReturn(current);
        when(states.isReturned(30L, 10L, 20L)).thenReturn(true);
        FlowchartService service = new FlowchartService(mock(FlowchartMapper.class),
                mock(BizQuestionMapper.class), students, assignments,
                mock(BizLessonQuestionMapper.class), mock(BizStudentAnswerMapper.class),
                mock(GuideSheetAccessService.class), mock(FlowchartDocumentService.class),
                mock(FlowchartStructureCheckService.class), states);

        assertThrows(ServiceException.class, () -> service.studentWorkspace(40L, 9L, 10L, 20L));
        assertThrows(ServiceException.class, () -> service.reopen(40L, 9L, 10L, 20L));
    }

    private static BizStudent student() {
        BizStudent student = new BizStudent();
        student.setStudentId(30L);
        student.setDeptId(9L);
        student.setEntryYear("2025");
        student.setClassCode("1");
        return student;
    }
}
