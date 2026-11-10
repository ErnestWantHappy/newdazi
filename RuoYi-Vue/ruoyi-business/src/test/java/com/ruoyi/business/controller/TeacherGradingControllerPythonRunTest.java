package com.ruoyi.business.controller;

import java.util.Collections;
import java.util.Map;

import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.ProgrammingSubmission;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.ProgrammingJudgeMapper;
import com.ruoyi.business.service.GuideSheetAccessService;
import com.ruoyi.business.service.PracticalPythonRunService;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TeacherGradingControllerPythonRunTest {
    private ProgrammingJudgeMapper programming;
    private BizStudentMapper students;
    private GuideSheetAccessService access;
    private PracticalPythonRunService runner;
    private TeacherGradingController controller;

    @BeforeEach
    void setUp() {
        controller = new TeacherGradingController();
        programming = mock(ProgrammingJudgeMapper.class);
        students = mock(BizStudentMapper.class);
        access = mock(GuideSheetAccessService.class);
        runner = mock(PracticalPythonRunService.class);
        ReflectionTestUtils.setField(controller, "programmingJudgeMapper", programming);
        ReflectionTestUtils.setField(controller, "studentMapper", students);
        ReflectionTestUtils.setField(controller, "guideSheetAccessService", access);
        ReflectionTestUtils.setField(controller, "practicalPythonRunService", runner);
        LoginUser loginUser = new LoginUser(2L, 139L,
                new com.ruoyi.common.core.domain.entity.SysUser(), Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void teacherRunsAuthorizedSubmissionSource() {
        ProgrammingSubmission submission = new ProgrammingSubmission();
        submission.setSubmissionId(1001L);
        submission.setStudentId(10L);
        submission.setLessonId(20L);
        submission.setSubmissionKind("SUBMIT");
        submission.setSourceCode("print(1)");
        when(programming.selectSubmissionById(1001L)).thenReturn(submission);
        when(students.selectBizStudentByStudentId(10L)).thenReturn(student());
        Map<String, Object> runResult = Collections.singletonMap("output", "1\n");
        when(runner.run("print(1)", "7")).thenReturn(runResult);

        Object data = controller.runPythonSubmission(
                1001L, Collections.singletonMap("stdin", "7")).get("data");

        assertEquals(runResult, data);
    }

    @Test
    void rejectsUnauthorizedClassBeforeRunning() {
        ProgrammingSubmission submission = new ProgrammingSubmission();
        submission.setSubmissionId(1001L);
        submission.setStudentId(10L);
        submission.setLessonId(20L);
        submission.setSubmissionKind("SUBMIT");
        submission.setSourceCode("print(1)");
        when(programming.selectSubmissionById(1001L)).thenReturn(submission);
        when(students.selectBizStudentByStudentId(10L)).thenReturn(student());
        org.mockito.Mockito.doThrow(new ServiceException("无权查看该班级"))
                .when(access).assertCanViewLessonClass(20L, "2026", "1");

        assertThrows(ServiceException.class,
                () -> controller.runPythonSubmission(1001L, Collections.emptyMap()));
    }

    private BizStudent student() {
        BizStudent student = new BizStudent();
        student.setStudentId(10L);
        student.setEntryYear("2026");
        student.setClassCode("1");
        return student;
    }
}
