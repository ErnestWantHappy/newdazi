package com.ruoyi.business.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.domain.ProgrammingSubmission;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.domain.vo.PracticalGradingStatusVo;
import com.ruoyi.business.domain.vo.PracticalSubmissionVo;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.mapper.BizScoringDetailMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.ProgrammingJudgeMapper;
import com.ruoyi.business.service.ClassroomTaskStateService;
import com.ruoyi.business.service.GuideSheetAccessService;
import com.ruoyi.business.service.PracticalGradingDeadlineService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TeacherGradingControllerPythonAdoptTest {
    private BizStudentAnswerMapper answers;
    private BizLessonQuestionMapper questions;
    private BizStudentMapper students;
    private GuideSheetAccessService access;
    private PracticalGradingDeadlineService deadlines;
    private BizScoringDetailMapper details;
    private ClassroomTaskStateService taskStates;
    private ProgrammingJudgeMapper programming;
    private TeacherGradingController controller;

    @BeforeEach
    void setUp() {
        controller = new TeacherGradingController();
        answers = mock(BizStudentAnswerMapper.class);
        questions = mock(BizLessonQuestionMapper.class);
        students = mock(BizStudentMapper.class);
        access = mock(GuideSheetAccessService.class);
        deadlines = mock(PracticalGradingDeadlineService.class);
        details = mock(BizScoringDetailMapper.class);
        taskStates = mock(ClassroomTaskStateService.class);
        programming = mock(ProgrammingJudgeMapper.class);
        ReflectionTestUtils.setField(controller, "studentAnswerMapper", answers);
        ReflectionTestUtils.setField(controller, "lessonQuestionMapper", questions);
        ReflectionTestUtils.setField(controller, "studentMapper", students);
        ReflectionTestUtils.setField(controller, "guideSheetAccessService", access);
        ReflectionTestUtils.setField(controller, "practicalGradingDeadlineService", deadlines);
        ReflectionTestUtils.setField(controller, "scoringDetailMapper", details);
        ReflectionTestUtils.setField(controller, "classroomTaskStateService", taskStates);
        ReflectionTestUtils.setField(controller, "programmingJudgeMapper", programming);

        LoginUser loginUser = new LoginUser(2L, 139L, new com.ruoyi.common.core.domain.entity.SysUser(),
                Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));

        BizLessonQuestionDetailVo question = new BizLessonQuestionDetailVo();
        question.setQuestionId(30L);
        question.setQuestionType("practical");
        question.setPracticalMode("PYTHON");
        question.setQuestionScore(100L);
        when(questions.selectDetailsByLessonId(20L)).thenReturn(Collections.singletonList(question));

        PracticalGradingStatusVo status = new PracticalGradingStatusVo();
        status.setCanGrade(true);
        when(deadlines.getStatus(eq(20L), eq(139L), eq("2026"), eq("1"), eq(false))).thenReturn(status);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsAnswerWhenProgrammingSubmissionHasNoAnswerRow() {
        PracticalSubmissionVo row = submittedRow(null, 10L, 80, 1001L, "print(1)");
        when(answers.selectPracticalSubmissions(20L, 30L, "1", "2026", 139L))
                .thenReturn(Collections.singletonList(row));
        BizStudent student = student(10L);
        when(students.selectBizStudentByStudentId(10L)).thenReturn(student);
        ProgrammingSubmission best = submission(1001L, 80, "print(1)");
        when(programming.selectBestFinalSubmission(10L, 20L, 30L)).thenReturn(best);
        when(answers.insertAnswerIfAbsent(any(BizStudentAnswer.class))).thenReturn(1);
        BizStudentAnswer locked = new BizStudentAnswer();
        locked.setAnswerId(900L);
        locked.setStudentId(10L);
        locked.setLessonId(20L);
        locked.setQuestionId(30L);
        when(answers.selectByStudentLessonQuestionForUpdate(10L, 20L, 30L)).thenReturn(locked);

        AjaxResult result = controller.batchAdoptPythonSystemScores(request());

        assertEquals(200, result.get("code"));
        Map<?, ?> data = (Map<?, ?>) result.get("data");
        assertEquals(1, data.get("adoptedCount"));
        assertEquals(0, data.get("skippedCount"));
        verify(answers).updateScoreAndStudentAnswer(900L, 80, "print(1)");
        verify(taskStates).markInTransaction(student, 139L, 20L, 30L,
                ClassroomTaskStateService.GRADED);
    }

    @Test
    void skipsConcurrentSnapshotAndMissingStudentWithoutFailingWholeClass() {
        PracticalSubmissionVo changed = submittedRow(50L, 10L, 80, 1001L, "print(1)");
        changed.setScore(60);
        PracticalSubmissionVo missing = submittedRow(51L, 11L, 70, 1002L, "print(2)");
        PracticalSubmissionVo normal = submittedRow(52L, 12L, 90, 1003L, "print(3)");
        normal.setStudentAnswer("print(2-old)");
        when(answers.selectPracticalSubmissions(20L, 30L, "1", "2026", 139L))
                .thenReturn(Arrays.asList(changed, missing, normal));
        when(students.selectBizStudentByStudentId(10L)).thenReturn(student(10L));
        when(students.selectBizStudentByStudentId(11L)).thenReturn(null);
        when(students.selectBizStudentByStudentId(12L)).thenReturn(student(12L));
        BizStudentAnswer changedLocked = answer(50L, 10L, 70, "print(0)");
        when(answers.selectByIdForUpdate(50L)).thenReturn(changedLocked);
        BizStudentAnswer normalLocked = answer(52L, 12L, 60, "print(2-old)");
        when(answers.selectByIdForUpdate(52L)).thenReturn(normalLocked);
        when(programming.selectBestFinalSubmission(12L, 20L, 30L))
                .thenReturn(submission(1003L, 90, "print(3)"));

        AjaxResult result = controller.batchAdoptPythonSystemScores(request());

        assertEquals(200, result.get("code"));
        Map<?, ?> data = (Map<?, ?>) result.get("data");
        assertEquals(1, data.get("adoptedCount"));
        assertEquals(2, data.get("skippedCount"));
        verify(answers).updateScoreAndStudentAnswer(52L, 90, "print(3)");
        verify(answers, never()).insertAnswerIfAbsent(any(BizStudentAnswer.class));
    }

    @Test
    void rejectsInvalidMaxScoreBeforeAnyWrite() {
        BizLessonQuestionDetailVo question = new BizLessonQuestionDetailVo();
        question.setQuestionId(30L);
        question.setQuestionType("practical");
        question.setPracticalMode("PYTHON");
        question.setQuestionScore(null);
        when(questions.selectDetailsByLessonId(20L)).thenReturn(Collections.singletonList(question));

        AjaxResult result = controller.batchAdoptPythonSystemScores(request());

        assertEquals(500, result.get("code"));
        verify(answers, never()).selectPracticalSubmissions(anyLong(), anyLong(), any(), any(), anyLong());
        verify(answers, never()).updateScoreAndStudentAnswer(anyLong(), any(), any());
    }

    @Test
    void skipsWhenTeacherPageSnapshotHasChangedBeforeBatchAdopt() {
        PracticalSubmissionVo current = submittedRow(52L, 12L, 90, 1003L, "print(new)");
        current.setScore(80);
        when(answers.selectPracticalSubmissions(20L, 30L, "1", "2026", 139L))
                .thenReturn(Collections.singletonList(current));
        when(students.selectBizStudentByStudentId(12L)).thenReturn(student(12L));
        when(answers.selectByIdForUpdate(52L))
                .thenReturn(answer(52L, 12L, 80, "print(new)"));
        when(programming.selectBestFinalSubmission(12L, 20L, 30L))
                .thenReturn(submission(1003L, 90, "print(new)"));
        TeacherGradingController.PythonBatchAdoptRequest request = request();
        request.setExpectedSnapshots(Collections.singletonList(
                snapshot(12L, 52L, 70, "print(old)", 1003L, 90)));

        AjaxResult result = controller.batchAdoptPythonSystemScores(request);

        Map<?, ?> data = (Map<?, ?>) result.get("data");
        assertEquals(0, data.get("adoptedCount"));
        assertEquals(1, data.get("skippedCount"));
        verify(answers, never()).updateScoreAndStudentAnswer(anyLong(), any(), any());
    }

    private TeacherGradingController.PythonBatchAdoptRequest request() {
        TeacherGradingController.PythonBatchAdoptRequest request =
                new TeacherGradingController.PythonBatchAdoptRequest();
        request.setLessonId(20L);
        request.setQuestionId(30L);
        request.setClassCode("1");
        request.setEntryYear("2026");
        return request;
    }

    private PracticalSubmissionVo submittedRow(Long answerId, Long studentId, int score,
                                               Long submissionId, String code) {
        PracticalSubmissionVo row = new PracticalSubmissionVo();
        row.setAnswerId(answerId);
        row.setStudentId(studentId);
        row.setQuestionId(30L);
        row.setSubmitted(true);
        row.setScore(60);
        row.setSubmitTime(new Date(1_700_000_000_000L));
        row.setStudentAnswer("print(0)");
        row.setMaxScore(100);
        row.setSystemScore(score);
        row.setSystemSubmissionId(submissionId);
        row.setSystemSourceCode(code);
        return row;
    }

    private BizStudentAnswer answer(Long id, Long studentId, int score, String code) {
        BizStudentAnswer answer = new BizStudentAnswer();
        answer.setAnswerId(id);
        answer.setStudentId(studentId);
        answer.setLessonId(20L);
        answer.setQuestionId(30L);
        answer.setScore(score);
        answer.setSubmitTime(new Date(1_700_000_000_000L));
        answer.setStudentAnswer(code);
        return answer;
    }

    private TeacherGradingController.PythonBatchAdoptSnapshot snapshot(
            Long studentId, Long answerId, int score, String code,
            Long systemSubmissionId, int systemScore) {
        TeacherGradingController.PythonBatchAdoptSnapshot snapshot =
                new TeacherGradingController.PythonBatchAdoptSnapshot();
        snapshot.setStudentId(studentId);
        snapshot.setAnswerId(answerId);
        snapshot.setScore(score);
        snapshot.setStudentAnswer(code);
        snapshot.setSystemSubmissionId(systemSubmissionId);
        snapshot.setSystemScore(systemScore);
        return snapshot;
    }

    private BizStudent student(Long id) {
        BizStudent student = new BizStudent();
        student.setStudentId(id);
        student.setDeptId(139L);
        student.setEntryYear("2026");
        student.setClassCode("1");
        return student;
    }

    private ProgrammingSubmission submission(Long id, int score, String code) {
        ProgrammingSubmission submission = new ProgrammingSubmission();
        submission.setSubmissionId(id);
        submission.setScore(score);
        submission.setSourceCode(code);
        submission.setSubmissionKind("SUBMIT");
        submission.setStatusCode("PARTIAL");
        submission.setJudgedAt(new Date(1_700_000_000_000L));
        return submission;
    }
}
