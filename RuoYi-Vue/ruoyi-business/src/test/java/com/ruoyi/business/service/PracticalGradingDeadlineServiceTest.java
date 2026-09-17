package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.business.domain.BizPracticalGradingDeadline;
import com.ruoyi.business.domain.vo.PracticalGradingStatusVo;
import com.ruoyi.business.mapper.PracticalGradingDeadlineMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.service.ISysConfigService;

@ExtendWith(MockitoExtension.class)
class PracticalGradingDeadlineServiceTest
{
    @Mock private PracticalGradingDeadlineMapper deadlineMapper;
    @Mock private ISysConfigService configService;
    @InjectMocks private PracticalGradingDeadlineService service;

    @BeforeEach
    void defaultConfig()
    {
        lenient().when(configService.selectConfigByKey(PracticalGradingDeadlineService.DEADLINE_DAYS_KEY))
                .thenReturn("21");
    }

    @Test
    void zeroDenominatorDoesNotTrigger()
    {
        when(deadlineMapper.selectClassMetrics(1L, 10L, "2025", "1"))
                .thenReturn(metrics(true, 0, 0, 0, 0));
        PracticalGradingStatusVo status = service.getStatus(1L, 10L, "2025", "1", true);
        assertEquals("NOT_TRIGGERED", status.getStatusCode());
        verify(deadlineMapper, never()).insertDeadlineIgnore(any());
    }

    @Test
    void noPracticalQuestionDoesNotTrigger()
    {
        when(deadlineMapper.selectClassMetrics(1L, 10L, "2025", "1"))
                .thenReturn(metrics(false, 10, 10, 0, 0));
        PracticalGradingStatusVo status = service.getStatus(1L, 10L, "2025", "1", true);
        assertEquals("NO_PRACTICAL", status.getStatusCode());
        verify(deadlineMapper, never()).insertDeadlineIgnore(any());
    }

    @Test
    void evenClassTriggersAtHalf()
    {
        Date threshold = new Date(1_000_000L);
        Map<String, Object> metrics = metrics(true, 10, 5, 3, 0);
        metrics.put("thresholdTime", threshold);
        BizPracticalGradingDeadline created = deadline(threshold, daysAfter(threshold, 21), null);
        when(deadlineMapper.selectClassMetrics(1L, 10L, "2025", "1")).thenReturn(metrics);
        when(deadlineMapper.selectDeadline(1L, 10L, "2025", "1")).thenReturn(null, created);

        service.getStatus(1L, 10L, "2025", "1", true);

        ArgumentCaptor<BizPracticalGradingDeadline> captor =
                ArgumentCaptor.forClass(BizPracticalGradingDeadline.class);
        verify(deadlineMapper).insertDeadlineIgnore(captor.capture());
        assertEquals(5, captor.getValue().getTriggerAnsweredCount());
        assertEquals(10, captor.getValue().getTriggerStudentCount());
        assertEquals(threshold, captor.getValue().getTriggerTime());
    }

    @Test
    void oddClassTriggersAtCeilingHalf()
    {
        Date threshold = new Date();
        Map<String, Object> metrics = metrics(true, 5, 3, 1, 0);
        metrics.put("thresholdTime", threshold);
        when(deadlineMapper.selectClassMetrics(1L, 10L, "2025", "1")).thenReturn(metrics);
        when(deadlineMapper.selectDeadline(1L, 10L, "2025", "1"))
                .thenReturn(null, deadline(threshold, daysAfter(threshold, 21), null));
        service.getStatus(1L, 10L, "2025", "1", true);
        verify(deadlineMapper).insertDeadlineIgnore(any());
    }

    @Test
    void oddClassDoesNotTriggerBelowCeilingHalf()
    {
        when(deadlineMapper.selectClassMetrics(1L, 10L, "2025", "1"))
                .thenReturn(metrics(true, 5, 2, 0, 0));
        service.getStatus(1L, 10L, "2025", "1", true);
        verify(deadlineMapper, never()).insertDeadlineIgnore(any());
    }

    @Test
    void zeroScoreCountsAsGradedAndCanComplete()
    {
        Date now = new Date();
        when(deadlineMapper.selectClassMetrics(1L, 10L, "2025", "1"))
                .thenReturn(metrics(true, 10, 6, 4, 4));
        when(deadlineMapper.selectDeadline(1L, 10L, "2025", "1"))
                .thenReturn(deadline(now, daysAfter(now, 10), null));
        PracticalGradingStatusVo status = service.getStatus(1L, 10L, "2025", "1", false);
        assertEquals("COMPLETED", status.getStatusCode());
        assertEquals(4, status.getGradedCount());
    }

    @Test
    void overdueDeadlineLocksGrading()
    {
        Date now = new Date();
        when(deadlineMapper.selectClassMetrics(1L, 10L, "2025", "1"))
                .thenReturn(metrics(true, 10, 6, 4, 2));
        when(deadlineMapper.selectDeadline(1L, 10L, "2025", "1"))
                .thenReturn(deadline(now, new Date(now.getTime() - 1_000L), null));
        PracticalGradingStatusVo status = service.getStatus(1L, 10L, "2025", "1", false);
        assertEquals("OVERDUE", status.getStatusCode());
        assertFalse(status.isCanGrade());
    }

    @Test
    void dueWithin72HoursIsDueSoon()
    {
        Date now = new Date();
        when(deadlineMapper.selectClassMetrics(1L, 10L, "2025", "1"))
                .thenReturn(metrics(true, 10, 6, 4, 2));
        when(deadlineMapper.selectDeadline(1L, 10L, "2025", "1"))
                .thenReturn(deadline(now, new Date(now.getTime() + TimeUnit.HOURS.toMillis(48)), null));
        assertEquals("DUE_SOON", service.getStatus(1L, 10L, "2025", "1", false).getStatusCode());
    }

    @Test
    void completedStatusTakesPriorityBeforeReopenedLabel()
    {
        Date now = new Date();
        when(deadlineMapper.selectClassMetrics(1L, 10L, "2025", "1"))
                .thenReturn(metrics(true, 10, 6, 4, 4));
        when(deadlineMapper.selectDeadline(1L, 10L, "2025", "1"))
                .thenReturn(deadline(now, daysAfter(now, 3), "REOPEN"));
        assertEquals("COMPLETED", service.getStatus(1L, 10L, "2025", "1", false).getStatusCode());
    }

    @Test
    void completedStatusRemainsVisibleAfterDeadlineButEditingStaysLocked()
    {
        Date now = new Date();
        when(deadlineMapper.selectClassMetrics(1L, 10L, "2025", "1"))
                .thenReturn(metrics(true, 10, 6, 4, 4));
        when(deadlineMapper.selectDeadline(1L, 10L, "2025", "1"))
                .thenReturn(deadline(now, new Date(now.getTime() - 1_000L), null));

        PracticalGradingStatusVo status = service.getStatus(1L, 10L, "2025", "1", false);

        assertEquals("COMPLETED", status.getStatusCode());
        assertFalse(status.isCanGrade());
    }

    @Test
    void answerBelongingToOverdueClassIsRejected()
    {
        Map<String, Object> key = new HashMap<>();
        key.put("lessonId", 1L);
        key.put("deptId", 10L);
        key.put("entryYear", "2025");
        key.put("classCode", "1");
        when(deadlineMapper.selectClassKeyByAnswerId(99L)).thenReturn(key);
        when(deadlineMapper.selectClassMetrics(1L, 10L, "2025", "1"))
                .thenReturn(metrics(true, 10, 6, 1, 0));
        when(deadlineMapper.selectDeadline(1L, 10L, "2025", "1"))
                .thenReturn(deadline(new Date(), new Date(System.currentTimeMillis() - 1_000L), null));
        assertThrows(ServiceException.class, () -> service.assertCanGrade(99L));
    }

    @Test
    void malformedConfigFallsBackTo21Days()
    {
        when(configService.selectConfigByKey(PracticalGradingDeadlineService.DEADLINE_DAYS_KEY))
                .thenReturn("invalid");
        assertEquals(21, service.getDeadlineDays());
    }

    @Test
    void configRangeIsValidated()
    {
        assertThrows(ServiceException.class, () -> service.updateDeadlineDays(0, "researcher"));
        assertThrows(ServiceException.class, () -> service.updateDeadlineDays(366, "researcher"));
    }

    @Test
    void extensionMustMoveDeadlineForward()
    {
        Date now = new Date();
        BizPracticalGradingDeadline current = deadline(now, daysAfter(now, 3), null);
        when(deadlineMapper.selectDeadlineById(1L)).thenReturn(current);
        assertThrows(ServiceException.class, () -> service.adjustDeadline(
                1L, daysAfter(now, 2), "原因", 8L, "researcher"));
        verify(deadlineMapper, never()).updateCurrentDeadline(any(), any(), any(), any(), any());
    }

    @Test
    void extensionWritesDeadlineAndAuditAtomically()
    {
        Date now = new Date();
        Date oldDeadline = daysAfter(now, 3);
        Date newDeadline = daysAfter(now, 5);
        BizPracticalGradingDeadline current = deadline(now, oldDeadline, null);
        when(deadlineMapper.selectDeadlineById(1L)).thenReturn(current);
        when(deadlineMapper.updateCurrentDeadline(1L, oldDeadline, newDeadline, "EXTEND", "researcher"))
                .thenReturn(1);
        when(deadlineMapper.selectClassMetrics(1L, 10L, "2025", "1"))
                .thenReturn(metrics(true, 10, 6, 2, 1));
        when(deadlineMapper.selectDeadline(1L, 10L, "2025", "1")).thenReturn(current);

        service.adjustDeadline(1L, newDeadline, "教学安排变化", 8L, "researcher");

        verify(deadlineMapper).insertDeadlineAudit(eq(current), eq("EXTEND"), eq(oldDeadline),
                eq(newDeadline), eq("教学安排变化"), eq(8L), eq("researcher"));
    }

    private static Map<String, Object> metrics(boolean practical, int total, int answered,
                                               int due, int graded)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("hasPractical", practical ? 1 : 0);
        map.put("totalStudentCount", total);
        map.put("answeredStudentCount", answered);
        map.put("dueCount", due);
        map.put("gradedCount", graded);
        return map;
    }

    private static BizPracticalGradingDeadline deadline(Date trigger, Date due, String adjustment)
    {
        BizPracticalGradingDeadline row = new BizPracticalGradingDeadline();
        row.setDeadlineId(1L);
        row.setLessonId(1L);
        row.setDeptId(10L);
        row.setEntryYear("2025");
        row.setClassCode("1");
        row.setTriggerTime(trigger);
        row.setCurrentDeadlineTime(due);
        row.setOriginalDeadlineTime(due);
        row.setLastAdjustmentType(adjustment);
        return row;
    }

    private static Date daysAfter(Date date, int days)
    {
        return new Date(date.getTime() + TimeUnit.DAYS.toMillis(days));
    }
}
