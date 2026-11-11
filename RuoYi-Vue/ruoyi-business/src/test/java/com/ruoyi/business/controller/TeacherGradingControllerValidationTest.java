package com.ruoyi.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.domain.ProgrammingSubmission;
import com.ruoyi.business.domain.PracticalRubricSnapshot;
import com.ruoyi.business.domain.vo.PracticalGradingStatusVo;
import com.ruoyi.business.domain.vo.PracticalSubmissionVo;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.ProgrammingJudgeMapper;
import com.ruoyi.business.service.ClassroomTaskStateService;
import com.ruoyi.business.service.GuideSheetAccessService;
import com.ruoyi.business.service.PracticalGradingDeadlineService;
import com.ruoyi.business.service.PracticalScoringPolicyService;
import com.ruoyi.business.service.PracticalRubricSnapshotService;
import com.ruoyi.common.core.domain.model.LoginUser;

/**
 * 批改接口的并发与快照边界：页面旧快照、晚到的系统提交、首次补建答题行的分项明细归属。
 */
class TeacherGradingControllerValidationTest {
    private TeacherGradingController controller;
    private ProgrammingJudgeMapper programming;
    private BizStudentMapper students;
    private BizStudentAnswerMapper answers;
    private BizLessonQuestionMapper lessonQuestions;
    private PracticalGradingDeadlineService deadlines;
    private PracticalRubricSnapshotService rubrics;
    private PracticalScoringPolicyService scoringPolicy;
    private com.ruoyi.business.mapper.BizScoringDetailMapper details;
    private ClassroomTaskStateService taskState;

    @BeforeEach
    void setUp() {
        controller = new TeacherGradingController();
        programming = mock(ProgrammingJudgeMapper.class);
        students = mock(BizStudentMapper.class);
        answers = mock(BizStudentAnswerMapper.class);
        lessonQuestions = mock(BizLessonQuestionMapper.class);
        deadlines = mock(PracticalGradingDeadlineService.class);
        rubrics = mock(PracticalRubricSnapshotService.class);
        scoringPolicy = mock(PracticalScoringPolicyService.class);
        details = mock(com.ruoyi.business.mapper.BizScoringDetailMapper.class);
        taskState = mock(ClassroomTaskStateService.class);
        ReflectionTestUtils.setField(controller, "programmingJudgeMapper", programming);
        ReflectionTestUtils.setField(controller, "studentMapper", students);
        ReflectionTestUtils.setField(controller, "studentAnswerMapper", answers);
        ReflectionTestUtils.setField(controller, "lessonQuestionMapper", lessonQuestions);
        ReflectionTestUtils.setField(controller, "practicalGradingDeadlineService", deadlines);
        ReflectionTestUtils.setField(controller, "rubricSnapshotService", rubrics);
        ReflectionTestUtils.setField(controller, "practicalScoringPolicyService", scoringPolicy);
        ReflectionTestUtils.setField(controller, "scoringDetailMapper", details);
        ReflectionTestUtils.setField(controller, "classroomTaskStateService", taskState);
        ReflectionTestUtils.setField(controller, "guideSheetAccessService", mock(GuideSheetAccessService.class));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new LoginUser(2L, 139L, new com.ruoyi.common.core.domain.entity.SysUser(), Collections.emptySet()),
                null, Collections.emptyList()));
        when(students.selectBizStudentByStudentId(10L)).thenReturn(student(10L));
        when(students.selectBizStudentByStudentId(11L)).thenReturn(student(11L));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void bothGradingTransactionsUseReadCommitted() throws Exception {
        for (String name : Arrays.asList("batchAdoptPythonSystemScores", "grade")) {
            Transactional annotation = Arrays.stream(TeacherGradingController.class.getMethods())
                    .filter(method -> method.getName().equals(name))
                    .findFirst().orElseThrow(AssertionError::new)
                    .getAnnotation(Transactional.class);
            assertNotNull(annotation);
            assertEquals(Isolation.READ_COMMITTED, annotation.isolation());
        }
    }

    @Test
    void stalePageCannotScoreWhenStudentSubmittedNewCode() {
        BizStudentAnswer answer = new BizStudentAnswer();
        answer.setAnswerId(55L); answer.setStudentId(10L); answer.setLessonId(20L); answer.setQuestionId(30L);
        answer.setStudentAnswer("print(1)");
        when(answers.selectByIdForUpdate(55L)).thenReturn(answer);
        when(lessonQuestions.selectDetailsByLessonId(20L)).thenReturn(Collections.singletonList(question()));
        when(programming.selectBestFinalSubmission(10L, 20L, 30L)).thenReturn(submission(1001L, "print(2)"));

        TeacherGradingController.GradeRequest request = new TeacherGradingController.GradeRequest();
        request.setAnswerId(55L); request.setScore(8);
        Object message = controller.grade(request).get("msg");

        assertTrue(String.valueOf(message).contains("新的判题提交"), String.valueOf(message));
        verify(answers, never()).updateScoreAndStudentAnswer(anyLong(), anyInt(), any());
    }

    @Test
    void batchAdoptSkipsStudentMissingFromPageSnapshot() {
        PracticalSubmissionVo known = row(11L, 101L);
        PracticalSubmissionVo late = row(10L, 100L);
        when(lessonQuestions.selectDetailsByLessonId(20L)).thenReturn(Collections.singletonList(question()));
        PracticalGradingStatusVo status = new PracticalGradingStatusVo();
        status.setCanGrade(true);
        when(deadlines.getStatus(anyLong(), anyLong(), any(), any(), org.mockito.ArgumentMatchers.anyBoolean()))
                .thenReturn(status);
        when(answers.selectPracticalSubmissions(20L, 30L, "1", "2026", 139L))
                .thenReturn(Arrays.asList(late, known));
        TeacherGradingController.PythonBatchAdoptSnapshot snapshot = new TeacherGradingController.PythonBatchAdoptSnapshot();
        snapshot.setStudentId(11L); snapshot.setAnswerId(101L); snapshot.setScore(null);
        snapshot.setStudentAnswer("print(11)"); snapshot.setSystemSubmissionId(1001L); snapshot.setSystemScore(7);
        TeacherGradingController.PythonBatchAdoptRequest request = new TeacherGradingController.PythonBatchAdoptRequest();
        request.setLessonId(20L); request.setQuestionId(30L); request.setClassCode("1"); request.setEntryYear("2026");
        request.setExpectedSnapshots(Collections.singletonList(snapshot));

        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> data = (java.util.Map<String, Object>) controller
                .batchAdoptPythonSystemScores(request).get("data");

        assertEquals(2, data.get("skippedCount"));
        // 缺快照的学生在最早分支被跳过，不能继续查锁或判题记录。
        verify(students, never()).selectBizStudentByStudentId(10L);
        verify(answers, never()).updateScoreAndStudentAnswer(anyLong(), anyInt(), any());
    }

    @Test
    void firstCreatedAnswerUsesRealAnswerIdInScoringDetails() {
        ProgrammingSubmission submission = submission(1001L, "print(1)");
        submission.setScore(8);
        when(programming.selectSubmissionById(1001L)).thenReturn(submission);
        when(programming.selectBestFinalSubmission(10L, 20L, 30L)).thenReturn(submission);
        when(lessonQuestions.selectDetailsByLessonId(20L)).thenReturn(Collections.singletonList(question()));
        when(answers.insertAnswerIfAbsent(any(BizStudentAnswer.class))).thenReturn(1);
        BizStudentAnswer created = new BizStudentAnswer();
        created.setAnswerId(555L); created.setStudentId(10L); created.setLessonId(20L); created.setQuestionId(30L);
        created.setStudentAnswer("print(1)");
        when(answers.selectByStudentLessonQuestionForUpdate(10L, 20L, 30L)).thenReturn(created);
        PracticalRubricSnapshot rubric = new PracticalRubricSnapshot();
        rubric.setSnapshotId(9L); rubric.setQuestionScore(10);
        when(rubrics.resolve(any(), anyLong(), any(), anyLong())).thenReturn(rubric);
        when(rubrics.buildScoringItems(rubric)).thenReturn(Collections.emptyList());
        when(scoringPolicy.resolveFinalScore(any(), anyInt(), any(), anyInt(), any(), any())).thenReturn(8);

        TeacherGradingController.GradeRequest request = new TeacherGradingController.GradeRequest();
        request.setScore(8); request.setSystemSubmissionId(1001L); request.setExpectedAnswer("print(1)");
        TeacherGradingController.ScoringDetailRequest detail = new TeacherGradingController.ScoringDetailRequest();
        detail.setItemId(1L); detail.setScore(8);
        request.setScoringDetails(Collections.singletonList(detail));

        assertEquals("批改成功", controller.grade(request).get("msg"));
        org.mockito.ArgumentCaptor<com.ruoyi.business.domain.BizScoringDetail> saved =
                org.mockito.ArgumentCaptor.forClass(com.ruoyi.business.domain.BizScoringDetail.class);
        verify(details).insertBizScoringDetail(saved.capture());
        assertEquals(555L, saved.getValue().getAnswerId());
        verify(deadlines).assertCanGrade(555L);
    }

    private BizStudent student(Long id) {
        BizStudent student = new BizStudent();
        student.setStudentId(id); student.setDeptId(139L);
        student.setEntryYear("2026"); student.setClassCode("1");
        return student;
    }

    private BizLessonQuestionDetailVo question() {
        BizLessonQuestionDetailVo question = new BizLessonQuestionDetailVo();
        question.setQuestionId(30L); question.setQuestionType("practical");
        question.setPracticalMode("PYTHON"); question.setQuestionScore(10L);
        return question;
    }

    private ProgrammingSubmission submission(Long id, String code) {
        ProgrammingSubmission submission = new ProgrammingSubmission();
        submission.setSubmissionId(id); submission.setStudentId(10L);
        submission.setLessonId(20L); submission.setQuestionId(30L);
        submission.setSubmissionKind("SUBMIT"); submission.setSourceCode(code);
        submission.setJudgedAt(new Date());
        return submission;
    }

    private PracticalSubmissionVo row(Long studentId, Long answerId) {
        PracticalSubmissionVo row = new PracticalSubmissionVo();
        row.setStudentId(studentId); row.setAnswerId(answerId); row.setSubmitted(true);
        row.setStudentAnswer("print(" + studentId + ")");
        row.setSystemSubmissionId(1001L); row.setSystemScore(7);
        return row;
    }
}
