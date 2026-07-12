package com.ruoyi.business.service.impl;

import com.ruoyi.business.domain.BizGuideSheetAnswer;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.GuideSheetAnswerMapper;
import com.ruoyi.business.mapper.GuideSheetMapper;
import com.ruoyi.business.mapper.GuideSheetProgressMapper;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuideSheetAnswerServiceImplTest
{
    @Mock
    private GuideSheetAnswerMapper answerMapper;

    @Mock
    private GuideSheetProgressMapper progressMapper;

    @Mock
    private GuideSheetMapper guideSheetMapper;

    @Mock
    private BizStudentMapper studentMapper;

    @InjectMocks
    private GuideSheetAnswerServiceImpl service;

    @Test
    void shouldSaveManualGradeAndRecalculateTotal()
    {
        BizGuideSheetAnswer answer = submittedAnswer();
        when(answerMapper.selectByStudentAndSheet(9L, 7L)).thenReturn(answer);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("fieldKey", "q1");
        item.put("score", 50);
        item.put("comment", "要点完整");

        BizGuideSheetAnswer result = service.saveManualGrades(7L, 9L, Collections.singletonList(item));

        assertEquals(90, result.getTotalScore());
        assertEquals("complete", result.getGradingStatus());
        assertTrue(result.getGradingDetail().contains("\"manualGraded\":true"));
        assertTrue(result.getGradingDetail().contains("人工批改：要点完整"));
        ArgumentCaptor<BizGuideSheetAnswer> captor = ArgumentCaptor.forClass(BizGuideSheetAnswer.class);
        verify(answerMapper).updateBizGuideSheetAnswer(captor.capture());
        assertEquals(90, captor.getValue().getTotalScore());
    }

    @Test
    void shouldRejectManualGradeAboveMaximum()
    {
        when(answerMapper.selectByStudentAndSheet(9L, 7L)).thenReturn(submittedAnswer());
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("fieldKey", "q1");
        item.put("score", 61);

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.saveManualGrades(7L, 9L, Collections.singletonList(item)));

        assertTrue(error.getMessage().contains("0 到 60"));
    }

    @Test
    void shouldRejectDraftSaveAfterSubmission()
    {
        BizGuideSheetAnswer existing = submittedAnswer();
        existing.setAnswerId(11L);
        existing.setAnswerJson("{\"q1\":\"最终答案\"}");
        when(answerMapper.selectByStudentAndSheet(9L, 7L)).thenReturn(existing);

        BizGuideSheetAnswer draft = new BizGuideSheetAnswer();
        draft.setSheetId(7L);
        draft.setStudentId(9L);
        draft.setAnswerJson("{\"q1\":\"旧草稿\"}");

        ServiceException error = assertThrows(ServiceException.class, () -> service.saveAnswer(draft));

        assertTrue(error.getMessage().contains("已提交"));
        verify(answerMapper, never()).updateBizGuideSheetAnswer(draft);
    }

    @Test
    void shouldAllowExplicitResubmissionToReplaceSubmittedAnswer()
    {
        BizGuideSheetAnswer existing = submittedAnswer();
        existing.setAnswerId(11L);
        when(answerMapper.selectByStudentAndSheet(9L, 7L)).thenReturn(existing);

        BizGuideSheetAnswer resubmission = new BizGuideSheetAnswer();
        resubmission.setSheetId(7L);
        resubmission.setStudentId(9L);
        resubmission.setAnswerJson("{\"q1\":\"新答案\"}");
        resubmission.setStatus("2");

        assertEquals(1, service.saveAnswer(resubmission));

        assertEquals(11L, resubmission.getAnswerId());
        verify(answerMapper).updateBizGuideSheetAnswer(resubmission);
    }

    @Test
    void shouldRejectDraftWhenSubmissionWinsConcurrentUpdate()
    {
        BizGuideSheetAnswer existingDraft = submittedAnswer();
        existingDraft.setAnswerId(11L);
        existingDraft.setStatus("1");
        when(answerMapper.selectByStudentAndSheet(9L, 7L)).thenReturn(existingDraft);

        BizGuideSheetAnswer lateDraft = new BizGuideSheetAnswer();
        lateDraft.setSheetId(7L);
        lateDraft.setStudentId(9L);
        lateDraft.setAnswerJson("{\"q1\":\"迟到的草稿\"}");
        when(answerMapper.updateBizGuideSheetAnswer(lateDraft)).thenReturn(0);

        ServiceException error = assertThrows(ServiceException.class, () -> service.saveAnswer(lateDraft));

        assertTrue(error.getMessage().contains("已提交"));
        assertEquals(Boolean.TRUE, lateDraft.getParams().get("onlyIfNotSubmitted"));
    }

    private BizGuideSheetAnswer submittedAnswer()
    {
        BizGuideSheetAnswer answer = new BizGuideSheetAnswer();
        answer.setSheetId(7L);
        answer.setStudentId(9L);
        answer.setStatus("2");
        answer.setGradingDetail("["
                + "{\"fieldKey\":\"q1\",\"score\":0,\"maxScore\":60,\"matchType\":\"manual\"},"
                + "{\"fieldKey\":\"q2\",\"score\":40,\"maxScore\":40,\"matchType\":\"auto\"}]");
        return answer;
    }
}
