package com.ruoyi.business.service;

import java.util.Arrays;
import java.util.List;
import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import com.ruoyi.common.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class StudentAnswerSubmissionServiceTest
{
    @Mock
    private BizStudentAnswerMapper studentAnswerMapper;
    @Mock private TheoryRedoService theoryRedoService;
    @Mock private ClassroomTaskStateWriter stateWriter;

    @InjectMocks
    private StudentAnswerSubmissionService service;

    @Test
    void wholeSubmissionUsesOrderedAtomicUpsertsAndReturnsPendingIds()
    {
        BizStudentAnswer first = answer(7L, "a.docx", "pending");
        BizStudentAnswer second = answer(8L, "A", null);
        doAnswer(invocation -> {
            BizStudentAnswer answer = invocation.getArgument(0);
            answer.setAnswerId(answer.getQuestionId() + 100L);
            return 1;
        }).when(studentAnswerMapper).upsertAnswer(org.mockito.ArgumentMatchers.any());

        List<Long> pendingIds = service.persistAnswers(30L, 5L, Arrays.asList(first, second));

        verify(studentAnswerMapper).upsertAnswer(first);
        verify(studentAnswerMapper).upsertAnswer(second);
        assertEquals(Arrays.asList(107L), pendingIds);
    }

    @Test
    void staleTypingCannotRestoreOldScore()
    {
        BizStudentAnswer answer = answer(8L, "abc", null);
        answer.setTypingSubmission(true);
        answer.setTypingRevision(0L);
        org.mockito.Mockito.when(theoryRedoService.guardTypingSubmission(30L, 5L, 0L))
                .thenThrow(new ServiceException("过期轮次"));
        assertThrows(ServiceException.class, () -> service.persistAnswers(30L, 5L, Arrays.asList(answer)));
        org.mockito.Mockito.verifyNoInteractions(studentAnswerMapper, stateWriter);
    }

    @Test
    void typingScoreAndStateShareTransaction()
    {
        BizStudentAnswer answer = answer(8L, "abc", null);
        answer.setTypingSubmission(true);
        answer.setTypingRevision(2L);
        com.ruoyi.business.domain.BizStudent student = new com.ruoyi.business.domain.BizStudent();
        student.setDeptId(9L);
        org.mockito.Mockito.when(theoryRedoService.guardTypingSubmission(30L, 5L, 2L)).thenReturn(student);
        service.persistAnswers(30L, 5L, Arrays.asList(answer));
        org.mockito.InOrder order = org.mockito.Mockito.inOrder(theoryRedoService, studentAnswerMapper, stateWriter);
        order.verify(theoryRedoService).guardTypingSubmission(30L, 5L, 2L);
        order.verify(studentAnswerMapper).upsertAnswer(answer);
        order.verify(stateWriter).markInTransaction(student, 9L, 5L, 8L, "GRADED");
    }

    @Test
    void theorySubmissionUsesInsertOnlyAndRejectsDuplicate()
    {
        BizStudentAnswer answer = answer(7L, "A", null);
        answer.setTerminalSubmission(true);
        org.mockito.Mockito.when(studentAnswerMapper.insertAnswerIfAbsent(answer)).thenReturn(0);

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.persistAnswers(30L, 5L, Arrays.asList(answer)));

        assertEquals("理论题已提交，不能重复提交", error.getMessage());
        verify(studentAnswerMapper).insertAnswerIfAbsent(answer);
        verify(studentAnswerMapper, never()).upsertAnswer(answer);
    }

    @Test
    void staleTheoryIsRejectedBeforeAnyAnswerWrite()
    {
        BizStudentAnswer answer = answer(7L, "A", null);
        answer.setTerminalSubmission(true);
        answer.setTheoryRevision(3L);
        org.mockito.Mockito.when(theoryRedoService.guardSubmission(30L, 5L, 3L))
                .thenThrow(new ServiceException("过期轮次"));
        assertThrows(ServiceException.class, () -> service.persistAnswers(30L, 5L, Arrays.asList(answer)));
        org.mockito.Mockito.verifyNoInteractions(studentAnswerMapper, stateWriter);
    }

    @Test
    void newTheoryScoreAndStateAreWrittenInTheSameTransaction()
    {
        BizStudentAnswer answer = answer(7L, "B", null);
        answer.setTerminalSubmission(true);
        answer.setTheoryRevision(3L);
        answer.setScore(0);
        com.ruoyi.business.domain.BizStudent student = new com.ruoyi.business.domain.BizStudent();
        student.setDeptId(9L);
        org.mockito.Mockito.when(theoryRedoService.guardSubmission(30L, 5L, 3L)).thenReturn(student);
        org.mockito.Mockito.when(studentAnswerMapper.insertAnswerIfAbsent(answer)).thenReturn(1);
        service.persistAnswers(30L, 5L, Arrays.asList(answer));
        verify(stateWriter).markInTransaction(student, 9L, 5L, 7L, "GRADED");
        verify(studentAnswerMapper, never()).upsertAnswer(answer);
    }

    private BizStudentAnswer answer(Long questionId, String value, String previewStatus)
    {
        BizStudentAnswer answer = new BizStudentAnswer();
        answer.setStudentId(30L);
        answer.setLessonId(5L);
        answer.setQuestionId(questionId);
        answer.setStudentAnswer(value);
        answer.setPreviewStatus(previewStatus);
        return answer;
    }
}
