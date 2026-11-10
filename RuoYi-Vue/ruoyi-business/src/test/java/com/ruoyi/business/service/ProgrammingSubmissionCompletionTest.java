package com.ruoyi.business.service;

import com.ruoyi.business.domain.*;
import com.ruoyi.business.mapper.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class ProgrammingSubmissionCompletionTest {
    @Test void advancedClassCompletesWithoutWritingOldAnswer() { complete(2L, true, false); }
    @Test void removedAssignmentCompletesWithoutWritingOldAnswer() { complete(null, true, false); }
    @Test void removedStudentCompletesWithoutWritingOldAnswer() { complete(null, false, false); }
    @Test void currentClassWritesAnswerWithoutSystemScore() { complete(1L, true, true); }

    private void complete(Long currentLesson, boolean studentExists, boolean writesAnswer) {
        ProgrammingSubmissionService service = new ProgrammingSubmissionService();
        ProgrammingJudgeMapper mapper = mock(ProgrammingJudgeMapper.class);
        BizStudentMapper students = mock(BizStudentMapper.class);
        BizLessonAssignmentMapper assignments = mock(BizLessonAssignmentMapper.class);
        BizLessonQuestionMapper questions = mock(BizLessonQuestionMapper.class);
        BizStudentAnswerMapper answers = mock(BizStudentAnswerMapper.class);
        ReflectionTestUtils.setField(service, "programmingMapper", mapper);
        ReflectionTestUtils.setField(service, "studentMapper", students);
        ReflectionTestUtils.setField(service, "lessonAssignmentMapper", assignments);
        ReflectionTestUtils.setField(service, "lessonQuestionMapper", questions);
        ReflectionTestUtils.setField(service, "studentAnswerMapper", answers);
        ProgrammingSubmission submission = new ProgrammingSubmission();
        submission.setSubmissionId(11L); submission.setStudentId(12L);
        submission.setLessonId(1L); submission.setQuestionId(13L);
        submission.setSubmissionKind("SUBMIT"); submission.setStatusCode("JUDGING");
        when(mapper.selectSubmissionById(11L)).thenReturn(submission);
        when(mapper.updateSubmissionResult(any())).thenReturn(1);
        when(questions.selectDetailsByLessonId(1L)).thenReturn(Collections.emptyList());
        if (studentExists) {
            BizStudent student = new BizStudent();
            student.setEntryYear("2026"); student.setClassCode("1"); student.setDeptId(139L);
            when(students.selectBizStudentByStudentId(12L)).thenReturn(student);
            BizLessonAssignment assignment = null;
            if (currentLesson != null) {
                assignment = new BizLessonAssignment(); assignment.setLessonId(currentLesson);
            }
            when(assignments.selectCurrentAssignmentForUpdate("2026", "1", 139L)).thenReturn(assignment);
        }
        ReflectionTestUtils.invokeMethod(service, "completeInTransaction", submission,
                new ProgrammingQuestionConfig(), Collections.emptyList(), Collections.emptyList(), "ACCEPTED");
        verify(mapper).updateSubmissionResult(submission);
        assertEquals("ACCEPTED", submission.getStatusCode());
        assertNotNull(submission.getJudgedAt());
        // Python 判题只同步代码，系统分不能代替教师确认正式成绩。
        verify(answers, times(writesAnswer ? 1 : 0)).upsertProgrammingAnswer(any());
        if (writesAnswer) {
            org.mockito.ArgumentCaptor<BizStudentAnswer> captor =
                    org.mockito.ArgumentCaptor.forClass(BizStudentAnswer.class);
            verify(answers).upsertProgrammingAnswer(captor.capture());
            assertNull(captor.getValue().getScore());
        }
    }
}
