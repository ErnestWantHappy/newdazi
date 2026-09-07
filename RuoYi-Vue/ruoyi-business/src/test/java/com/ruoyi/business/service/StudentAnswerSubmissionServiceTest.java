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
