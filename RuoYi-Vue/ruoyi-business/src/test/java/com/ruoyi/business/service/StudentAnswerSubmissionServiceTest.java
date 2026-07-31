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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

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
