package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.mapper.TextbookCaseFeedbackMapper;
import com.ruoyi.common.exception.ServiceException;

class TextbookCaseFeedbackServiceTest
{
    private TextbookCaseFeedbackMapper mapper;
    private FixedDrawService service;
    private Map<String, Object> global;
    private static class FixedDrawService extends TextbookCaseFeedbackService
    {
        boolean selected = true;
        @Override protected boolean draw() { return selected; }
    }

    @BeforeEach void setup()
    {
        mapper = mock(TextbookCaseFeedbackMapper.class);
        service = new FixedDrawService();
        ReflectionTestUtils.setField(service, "mapper", mapper);
        when(mapper.published(1L)).thenReturn(1);
        global = new HashMap<>();
        when(mapper.lockInvitation(2L)).thenReturn(global);
    }

    @Test void unpublishedAndInvalidVotesDoNotWrite()
    {
        assertThrows(ServiceException.class, () -> service.vote(3L, 2L, 1));
        assertThrows(ServiceException.class, () -> service.vote(1L, 2L, 2));
        assertThrows(ServiceException.class, () -> service.invitation(3L, 2L));
        verify(mapper, never()).vote(anyLong(), anyLong(), anyInt(), any());
    }

    @Test void changesAndWithdrawalUseSameTeacherCaseKey()
    {
        for (int vote : new int[] {1, -1, 0}) service.vote(1L, 2L, vote);
        verify(mapper).vote(1L, 2L, 1, null);
        verify(mapper).vote(1L, 2L, -1, null);
        verify(mapper).vote(1L, 2L, 0, null);
    }

    @Test void voteWithReasonSavesTrimmedText()
    {
        service.vote(1L, 2L, 1, "  很棒的课例设计！  ");
        verify(mapper).vote(1L, 2L, 1, "很棒的课例设计！");

        // 超过500字抛出异常
        StringBuilder longReason = new StringBuilder();
        for (int i = 0; i < 501; i++) longReason.append("字");
        assertThrows(ServiceException.class, () -> service.vote(1L, 2L, 1, longReason.toString()));
    }

    @Test void blankReasonMeansExplicitClearWhileNullPreserves()
    {
        service.vote(1L, 2L, 1, "   ");
        verify(mapper).vote(1L, 2L, 1, "");
        service.vote(1L, 2L, -1);
        verify(mapper).vote(1L, 2L, -1, null);
    }

    @Test void commentsReturnsMapperList()
    {
        List<Map<String, Object>> mockList = Collections.singletonList(Collections.singletonMap("reason", "好"));
        when(mapper.comments(1L)).thenReturn(mockList);
        assertEquals(mockList, service.comments(1L));
        assertThrows(ServiceException.class, () -> service.comments(999L));
    }

    @Test void selectedInvitationRecordsBothLimits()
    {
        assertTrue(service.invitation(1L, 2L));
        verify(mapper).attempted(2L, true);
        verify(mapper).invited(1L, 2L);
    }

    @Test void randomMissDoesNotMarkAsInvited()
    {
        service.selected = false;
        assertFalse(service.invitation(1L, 2L));
        verify(mapper).attempted(2L, false);
        verify(mapper, never()).invited(anyLong(), anyLong());
    }

    @Test void dailyLimitAppliesAcrossCases()
    {
        global.put("last_invited_at", new Date());
        assertFalse(service.invitation(1L, 2L));
        verify(mapper, never()).attempted(anyLong(), anyBoolean());
    }

    @Test void repeatedRefreshCannotRerollImmediately()
    {
        global.put("last_attempt_at", Date.from(Instant.now().minus(5, ChronoUnit.MINUTES)));
        assertFalse(service.invitation(1L, 2L));
    }

    @Test void alreadyVotedDoesNotInvite()
    {
        when(mapper.feedback(1L, 2L)).thenReturn(Collections.singletonMap("vote", -1));
        assertFalse(service.invitation(1L, 2L));
    }

    @Test void dismissedOrWithdrawnCaseWaitsFourteenDays()
    {
        Map<String, Object> row = new HashMap<>();
        row.put("vote", 0);
        row.put("last_invited_at", Date.from(Instant.now().minus(13, ChronoUnit.DAYS)));
        when(mapper.feedback(1L, 2L)).thenReturn(row);
        assertFalse(service.invitation(1L, 2L));
        row.put("last_invited_at", Date.from(Instant.now().minus(15, ChronoUnit.DAYS)));
        assertTrue(service.invitation(1L, 2L));
    }
}
