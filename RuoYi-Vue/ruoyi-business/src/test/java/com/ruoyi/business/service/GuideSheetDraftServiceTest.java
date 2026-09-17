package com.ruoyi.business.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.BizGuideSheetDraft;
import com.ruoyi.business.domain.dto.GuideSheetAiGenerateRequest;
import com.ruoyi.business.domain.dto.GuideSheetDraftSaveRequest;
import com.ruoyi.business.domain.vo.GuideSheetDraftVo;
import com.ruoyi.business.mapper.GuideSheetDraftMapper;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuideSheetDraftServiceTest
{
    @Mock
    private GuideSheetDraftMapper draftMapper;
    @Mock
    private AiChatGateway aiGateway;

    private GuideSheetDraftService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp()
    {
        objectMapper = new ObjectMapper();
        service = new GuideSheetDraftService(draftMapper, objectMapper);
    }

    @Test
    void firstSaveCreatesOwnerScopedDraftAtRevisionOne() throws Exception
    {
        GuideSheetDraftSaveRequest request = request("browser-1", 0L, "{\"title\":\"第一课\"}");
        when(draftMapper.selectByOwnerAndKey(8L, "browser-1")).thenReturn(null);

        GuideSheetDraftVo saved = service.save(8L, "teacher", request);

        ArgumentCaptor<BizGuideSheetDraft> captor = ArgumentCaptor.forClass(BizGuideSheetDraft.class);
        verify(draftMapper).insertDraft(captor.capture());
        assertEquals(8L, captor.getValue().getOwnerId());
        assertEquals("browser-1", captor.getValue().getClientDraftKey());
        assertEquals(1L, saved.getRevision());
        assertEquals("第一课", objectMapper.readTree(saved.getContent()).get("title").asText());
    }

    @Test
    void aiFailureDoesNotBlockOrdinaryDraftSaving() throws Exception
    {
        when(aiGateway.isConfigured()).thenReturn(true);
        when(aiGateway.chat(anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("provider unavailable"));
        GuideSheetAiGenerateRequest aiRequest = new GuideSheetAiGenerateRequest();
        aiRequest.setAction("generateObjectives");
        aiRequest.setGrade(6);
        aiRequest.setLessonNum(1);
        aiRequest.setTopic("网络安全");
        GuideSheetAiContentService aiService = new GuideSheetAiContentService(aiGateway);

        assertFalse(aiService.generate(8L, aiRequest).isAvailable());
        when(draftMapper.selectByOwnerAndKey(8L, "browser-ai-fallback")).thenReturn(null);

        GuideSheetDraftVo saved = service.save(8L, "teacher",
                request("browser-ai-fallback", 0L, "{\"title\":\"普通草稿\"}"));

        assertEquals(1L, saved.getRevision());
        assertEquals("普通草稿", objectMapper.readTree(saved.getContent()).path("title").asText());
        verify(draftMapper).insertDraft(any());
    }

    @Test
    void acceptsFrontendStringifiedContentAndReturnsRestorableString() throws Exception
    {
        GuideSheetDraftSaveRequest request = new GuideSheetDraftSaveRequest();
        request.setDraftKey("browser-1");
        request.setRevision(0L);
        request.setContent(objectMapper.getNodeFactory().textNode("{\"step\":2}"));
        when(draftMapper.selectByOwnerAndKey(8L, "browser-1")).thenReturn(null);

        GuideSheetDraftVo saved = service.save(8L, "teacher", request);

        assertEquals(2, objectMapper.readTree(saved.getContent()).path("step").asInt());
    }

    @Test
    void replayingSamePayloadDoesNotCreateOrAdvanceDraft() throws Exception
    {
        BizGuideSheetDraft existing = draft(8L, "browser-1", 3L, "{\"title\":\"第一课\"}");
        when(draftMapper.selectByOwnerAndKey(8L, "browser-1")).thenReturn(existing);

        GuideSheetDraftVo saved = service.save(
                8L, "teacher", request("browser-1", 2L, "{\"title\":\"第一课\"}"));

        assertEquals(3L, saved.getRevision());
        verify(draftMapper, never()).insertDraft(any());
        verify(draftMapper, never()).updateDraftCas(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void staleRevisionCannotOverwriteNewerContent() throws Exception
    {
        BizGuideSheetDraft existing = draft(8L, "browser-1", 3L, "{\"title\":\"新内容\"}");
        when(draftMapper.selectByOwnerAndKey(8L, "browser-1")).thenReturn(existing);

        assertThrows(ServiceException.class, () -> service.save(
                8L, "teacher", request("browser-1", 2L, "{\"title\":\"旧内容\"}")));
        verify(draftMapper, never()).updateDraftCas(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void casFailureIsReportedAsConcurrentModification() throws Exception
    {
        BizGuideSheetDraft existing = draft(8L, "browser-1", 3L, "{\"title\":\"旧内容\"}");
        when(draftMapper.selectByOwnerAndKey(8L, "browser-1")).thenReturn(existing);
        when(draftMapper.updateDraftCas(eq(8L), eq("browser-1"), eq(3L), any(), any(), any(), any()))
                .thenReturn(0);

        assertThrows(ServiceException.class, () -> service.save(
                8L, "teacher", request("browser-1", 3L, "{\"title\":\"新内容\"}")));
    }

    @Test
    void restoreNeverFallsBackToAnotherOwnersDraft()
    {
        when(draftMapper.selectByOwnerAndKey(8L, "browser-1")).thenReturn(null);

        assertNull(service.restore(8L, "browser-1"));
        verify(draftMapper).selectByOwnerAndKey(8L, "browser-1");
    }

    @Test
    void completionUsesOwnerAndRevisionCas()
    {
        BizGuideSheetDraft existing = draft(8L, "browser-1", 3L, "{\"title\":\"完成\"}");
        when(draftMapper.selectByOwnerAndKey(8L, "browser-1")).thenReturn(existing);
        when(draftMapper.completeDraftCas(eq(8L), eq("browser-1"), eq(3L), eq("teacher"), any()))
                .thenReturn(1);

        GuideSheetDraftVo completed = service.complete(8L, "teacher", "browser-1", 3L);

        assertEquals(4L, completed.getRevision());
        assertEquals("C", completed.getStatus());
    }

    @Test
    void completedDraftCanStartNewEditingSessionWithSameClientKey() throws Exception
    {
        BizGuideSheetDraft existing = draft(8L, "browser-1", 3L, "{\"title\":\"已保存\"}");
        existing.setDraftStatus("C");
        when(draftMapper.selectByOwnerAndKey(8L, "browser-1")).thenReturn(existing);
        when(draftMapper.reopenCompletedDraft(eq(8L), eq("browser-1"), any(), any(),
                eq("teacher"), any())).thenReturn(1);

        GuideSheetDraftVo reopened = service.save(
                8L, "teacher", request("browser-1", 0L, "{\"title\":\"再次编辑\"}"));

        assertEquals(4L, reopened.getRevision());
        assertEquals("D", reopened.getStatus());
    }

    private GuideSheetDraftSaveRequest request(String key, Long revision, String content) throws Exception
    {
        GuideSheetDraftSaveRequest request = new GuideSheetDraftSaveRequest();
        request.setDraftKey(key);
        request.setRevision(revision);
        request.setContent(objectMapper.readTree(content));
        return request;
    }

    private BizGuideSheetDraft draft(Long ownerId, String key, Long revision, String content)
    {
        BizGuideSheetDraft draft = new BizGuideSheetDraft();
        draft.setOwnerId(ownerId);
        draft.setClientDraftKey(key);
        draft.setRevision(revision);
        draft.setContentJson(content);
        draft.setDraftStatus("D");
        return draft;
    }
}
