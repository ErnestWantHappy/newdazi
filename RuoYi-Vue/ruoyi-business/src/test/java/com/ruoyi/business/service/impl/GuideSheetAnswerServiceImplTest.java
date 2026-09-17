package com.ruoyi.business.service.impl;

import com.ruoyi.business.domain.BizGuideSheetAnswer;
import com.ruoyi.business.domain.BizGuideSheetProgress;
import com.ruoyi.business.domain.BizLessonGuideSheetBinding;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.GuideSheetAnswerMapper;
import com.ruoyi.business.mapper.GuideSheetBindingMapper;
import com.ruoyi.business.mapper.GuideSheetProgressMapper;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class GuideSheetAnswerServiceImplTest
{
    @Mock
    private GuideSheetAnswerMapper answerMapper;

    @Mock
    private GuideSheetProgressMapper progressMapper;

    @Mock
    private GuideSheetBindingMapper bindingMapper;

    @Mock
    private BizStudentMapper studentMapper;

    @InjectMocks
    private GuideSheetAnswerServiceImpl service;

    @Test
    void shouldSaveManualGradeAndRecalculateTotal()
    {
        BizGuideSheetAnswer answer = submittedAnswer();
        when(answerMapper.selectByStudentAndBinding(9L, 7L)).thenReturn(answer);
        when(answerMapper.updateGradingFields(answer)).thenReturn(1);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("fieldKey", "q1");
        item.put("score", 50);
        item.put("comment", "要点完整");

        BizGuideSheetAnswer result = service.saveManualGrades(7L, 9L, Collections.singletonList(item));

        assertEquals(90, result.getTotalScore());
        assertEquals(50, result.getManualAdjustment());
        assertEquals("complete", result.getGradingStatus());
        assertTrue(result.getGradingDetail().contains("\"manualGraded\":true"));
        assertTrue(result.getGradingDetail().contains("人工批改：要点完整"));
        ArgumentCaptor<BizGuideSheetAnswer> captor = ArgumentCaptor.forClass(BizGuideSheetAnswer.class);
        verify(answerMapper).updateGradingFields(captor.capture());
        assertEquals(90, captor.getValue().getTotalScore());
    }

    @Test
    void shouldRejectManualGradeAboveMaximum()
    {
        when(answerMapper.selectByStudentAndBinding(9L, 7L)).thenReturn(submittedAnswer());
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
        prepareBindingSave(existing);

        BizGuideSheetAnswer draft = new BizGuideSheetAnswer();
        draft.setBindingId(7L);
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
        prepareBindingSave(existing);

        BizGuideSheetAnswer resubmission = new BizGuideSheetAnswer();
        resubmission.setBindingId(7L);
        resubmission.setStudentId(9L);
        resubmission.setAnswerJson("{\"q1\":\"新答案\"}");
        resubmission.setStatus("2");
        resubmission.setDraftRevision(6L);
        when(answerMapper.updateBizGuideSheetAnswer(resubmission)).thenReturn(1);

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
        prepareBindingSave(existingDraft);

        BizGuideSheetAnswer lateDraft = new BizGuideSheetAnswer();
        lateDraft.setBindingId(7L);
        lateDraft.setStudentId(9L);
        lateDraft.setAnswerJson("{\"q1\":\"迟到的草稿\"}");
        when(answerMapper.updateBizGuideSheetAnswer(lateDraft)).thenReturn(0);

        ServiceException error = assertThrows(ServiceException.class, () -> service.saveAnswer(lateDraft));

        assertTrue(error.getMessage().contains("已提交"));
        assertEquals(Boolean.TRUE, lateDraft.getParams().get("onlyIfNotSubmitted"));
    }

    @Test
    void staleDraftRevisionDoesNotOverwriteNewerAnswer()
    {
        BizGuideSheetAnswer existing = submittedAnswer();
        existing.setStatus("1");
        existing.setAnswerId(11L);
        existing.setDraftRevision(5L);
        prepareBindingSave(existing);

        BizGuideSheetAnswer stale = new BizGuideSheetAnswer();
        stale.setBindingId(7L);
        stale.setStudentId(9L);
        stale.setAnswerJson("{\"q1\":\"旧内容\"}");
        stale.setDraftRevision(4L);

        assertEquals(1, service.saveAnswer(stale));

        assertEquals(5L, stale.getDraftRevision());
        verify(answerMapper, never()).updateBizGuideSheetAnswer(stale);
        verify(progressMapper, never()).insertOrUpdate(any());
    }

    @Test
    void concurrentFirstDraftReusesUniqueAnswer()
    {
        prepareBindingSave(null);
        BizGuideSheetAnswer draft = new BizGuideSheetAnswer();
        draft.setBindingId(7L);
        draft.setStudentId(9L);
        draft.setAnswerJson("{\"q1\":\"内容\"}");
        draft.setDraftRevision(1L);
        org.mockito.Mockito.doThrow(new org.springframework.dao.DuplicateKeyException("duplicate"))
                .when(answerMapper).insertBizGuideSheetAnswer(draft);
        BizGuideSheetAnswer concurrent = new BizGuideSheetAnswer();
        concurrent.setAnswerId(21L);
        concurrent.setBindingId(7L);
        concurrent.setStudentId(9L);
        concurrent.setStatus("1");
        concurrent.setDraftRevision(1L);
        when(answerMapper.selectByStudentAndBinding(9L, 7L)).thenReturn(null, concurrent);

        assertEquals(1, service.saveAnswer(draft));

        assertEquals(21L, draft.getAnswerId());
        verify(answerMapper, times(1)).insertBizGuideSheetAnswer(draft);
    }

    @Test
    void draftProgressUsesVFormFieldsNamedInsideOptions()
    {
        BizLessonGuideSheetBinding binding = new BizLessonGuideSheetBinding();
        binding.setBindingId(7L);
        binding.setLessonId(3L);
        binding.setSourceSheetId(5L);
        binding.setSnapshotFormJson("{\"widgetList\":[{\"id\":\"bg-home-tab\",\"type\":\"tab\","
                + "\"options\":{\"name\":\"HomeTab\"},\"tabs\":[{\"id\":\"bg-home-pane\","
                + "\"type\":\"tab-pane\",\"options\":{\"name\":\"tab1\"},\"widgetList\":["
                + "{\"id\":\"bg-objective-0\",\"type\":\"static-text\","
                + "\"options\":{\"name\":\"bg_objective_0\",\"content\":\"学习目标\"}},"
                + "{\"id\":\"bg-pre-class-check-1\",\"type\":\"input\","
                + "\"options\":{\"name\":\"bg_preClassCheck_1\",\"label\":\"课前检测\"}},"
                + "{\"id\":\"bg-multiple-choice-2\",\"type\":\"checkbox\","
                + "\"options\":{\"name\":\"bg_multipleChoice_2\",\"label\":\"多选题\"}},"
                + "{\"id\":\"legacy-short-answer-id\",\"name\":\"legacyShortAnswer\","
                + "\"type\":\"textarea\",\"options\":{\"label\":\"旧简答题\"}},"
                + "{\"id\":\"legacy-id-only\",\"type\":\"input\","
                + "\"options\":{\"label\":\"旧输入题\"}}]}]}]}");
        when(bindingMapper.selectByBindingId(7L)).thenReturn(binding);
        when(answerMapper.selectByStudentAndBinding(9L, 7L)).thenReturn(null);
        BizStudent student = new BizStudent();
        student.setStudentId(9L);
        student.setDeptId(10L);
        student.setEntryYear("2025");
        student.setClassCode("1");
        when(studentMapper.selectBizStudentByStudentId(9L)).thenReturn(student);

        BizGuideSheetAnswer draft = new BizGuideSheetAnswer();
        draft.setBindingId(7L);
        draft.setStudentId(9L);
        draft.setCurrentPage(1);
        draft.setDraftRevision(1L);
        draft.setAnswerJson("{\"bg_preClassCheck_1\":\"循环\","
                + "\"bg_multipleChoice_2\":[],\"legacyShortAnswer\":\"旧模板答案\","
                + "\"legacy-id-only\":null}");

        assertEquals(1, service.saveAnswer(draft));

        ArgumentCaptor<BizGuideSheetProgress> captor = ArgumentCaptor.forClass(BizGuideSheetProgress.class);
        verify(progressMapper).insertOrUpdate(captor.capture());
        assertEquals("{\"filled\":2,\"total\":4,\"fields\":{"
                + "\"bg_preClassCheck_1\":true,\"bg_multipleChoice_2\":false,"
                + "\"legacyShortAnswer\":true,\"legacy-id-only\":false}}",
                captor.getValue().getProgressDetail());
    }

    @Test
    void draftProgressSupportsFormsWithoutTabsAndIgnoresNonWidgetArrays()
    {
        BizLessonGuideSheetBinding binding = new BizLessonGuideSheetBinding();
        binding.setBindingId(7L);
        binding.setLessonId(3L);
        binding.setSourceSheetId(5L);
        binding.setSnapshotFormJson("{\"widgetList\":["
                + "{\"id\":\"intro\",\"type\":\"static-text\",\"options\":{\"name\":\"intro\"}},"
                + "{\"id\":\"plain\",\"type\":\"input\",\"options\":{\"name\":\"plainAnswer\","
                + "\"choices\":[\"A\",\"B\"]}}]}");
        when(bindingMapper.selectByBindingId(7L)).thenReturn(binding);
        when(answerMapper.selectByStudentAndBinding(9L, 7L)).thenReturn(null);
        BizStudent student = new BizStudent();
        student.setStudentId(9L);
        student.setDeptId(10L);
        student.setEntryYear("2025");
        student.setClassCode("1");
        when(studentMapper.selectBizStudentByStudentId(9L)).thenReturn(student);

        BizGuideSheetAnswer draft = new BizGuideSheetAnswer();
        draft.setBindingId(7L);
        draft.setStudentId(9L);
        draft.setCurrentPage(1);
        draft.setDraftRevision(1L);
        draft.setAnswerJson("{\"plainAnswer\":\"已填写\"}");

        assertEquals(1, service.saveAnswer(draft));

        ArgumentCaptor<BizGuideSheetProgress> captor = ArgumentCaptor.forClass(BizGuideSheetProgress.class);
        verify(progressMapper).insertOrUpdate(captor.capture());
        assertEquals("{\"filled\":1,\"total\":1,\"fields\":{\"plainAnswer\":true}}",
                captor.getValue().getProgressDetail());
    }

    @Test
    void shouldRejectMalformedAnswerJsonBeforeDatabaseAccess()
    {
        BizGuideSheetAnswer answer = answerWithJson("{");

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.saveAnswer(answer));

        assertTrue(error.getMessage().contains("格式无效"));
        verifyNoInteractions(answerMapper, bindingMapper, studentMapper, progressMapper);
    }

    @Test
    void shouldRejectArrayAnswerJsonBeforeDatabaseAccess()
    {
        BizGuideSheetAnswer answer = answerWithJson("[]");

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.saveAnswer(answer));

        assertTrue(error.getMessage().contains("格式无效"));
        verifyNoInteractions(answerMapper, bindingMapper, studentMapper, progressMapper);
    }

    @Test
    void shouldRejectAnswerJsonLargerThanTwoMegabytesBeforeDatabaseAccess()
    {
        char[] content = new char[2 * 1024 * 1024];
        Arrays.fill(content, 'a');
        BizGuideSheetAnswer answer = answerWithJson("{\"q1\":\"" + new String(content) + "\"}");

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.saveAnswer(answer));

        assertTrue(error.getMessage().contains("不能超过 2MB"));
        verifyNoInteractions(answerMapper, bindingMapper, studentMapper, progressMapper);
    }

    private BizGuideSheetAnswer submittedAnswer()
    {
        BizGuideSheetAnswer answer = new BizGuideSheetAnswer();
        answer.setBindingId(7L);
        answer.setStudentId(9L);
        answer.setStatus("2");
        answer.setDraftRevision(5L);
        answer.setAutoScore(40);
        answer.setGradingDetail("["
                + "{\"fieldKey\":\"q1\",\"score\":0,\"maxScore\":60,\"matchType\":\"manual\"},"
                + "{\"fieldKey\":\"q2\",\"score\":40,\"maxScore\":40,\"matchType\":\"auto\"}]");
        return answer;
    }

    private BizGuideSheetAnswer answerWithJson(String answerJson)
    {
        BizGuideSheetAnswer answer = new BizGuideSheetAnswer();
        answer.setBindingId(7L);
        answer.setStudentId(9L);
        answer.setAnswerJson(answerJson);
        return answer;
    }

    private void prepareBindingSave(BizGuideSheetAnswer existing)
    {
        BizLessonGuideSheetBinding binding = new BizLessonGuideSheetBinding();
        binding.setBindingId(7L);
        binding.setLessonId(3L);
        binding.setSourceSheetId(5L);
        when(bindingMapper.selectByBindingId(7L)).thenReturn(binding);
        when(answerMapper.selectByStudentAndBinding(9L, 7L)).thenReturn(existing);
        BizStudent student = new BizStudent();
        student.setStudentId(9L);
        student.setDeptId(10L);
        student.setEntryYear("2025");
        student.setClassCode("1");
        lenient().when(studentMapper.selectBizStudentByStudentId(9L)).thenReturn(student);
    }
}
