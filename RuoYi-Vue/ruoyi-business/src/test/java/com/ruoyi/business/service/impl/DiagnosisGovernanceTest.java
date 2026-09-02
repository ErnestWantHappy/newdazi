package com.ruoyi.business.service.impl;

import com.ruoyi.common.utils.diagnosis.DiagnosisAdvice;
import com.ruoyi.common.utils.diagnosis.DiagnosisAdvisor;
import com.ruoyi.system.domain.SysOperLog;
import com.ruoyi.system.domain.SysPerfEvent;
import com.ruoyi.system.mapper.SysPerfEventMapper;
import com.ruoyi.system.service.impl.SysPerfEventServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiagnosisGovernanceTest
{
    @Mock
    private SysPerfEventMapper perfEventMapper;

    @InjectMocks
    private SysPerfEventServiceImpl perfEventService;

    @Test
    void expectedBusinessRejectionsAreInformational()
    {
        DiagnosisAdvice threshold = DiagnosisAdvisor.adviseForApiEvent(
                "/business/lesson/auto-advance/manual", "有成绩 0/43 人，需达到 50%", 5, true);
        DiagnosisAdvice repeatedDelete = DiagnosisAdvisor.adviseForApiEvent(
                "/business/lesson/283", "课程不存在", 1, true);

        assertEquals("business", threshold.getCategory());
        assertEquals("info", threshold.getSeverity());
        assertEquals("business", repeatedDelete.getCategory());
        assertEquals("info", repeatedDelete.getSeverity());
    }

    @Test
    void studentImportUsesDedicatedDurationThresholds()
    {
        assertEquals("info", studentImportAdvice(4435).getSeverity());
        assertEquals("warning", studentImportAdvice(12295).getSeverity());
        assertEquals("critical", studentImportAdvice(37316).getSeverity());
    }

    @Test
    void unknownStudentDatabaseFailureRemainsCritical()
    {
        DiagnosisAdvice advice = DiagnosisAdvisor.adviseForApiEvent(
                "/business/student/importData", "数据库连接中断", 20, true);

        assertEquals("system", advice.getCategory());
        assertEquals("critical", advice.getSeverity());
    }

    @Test
    void persistedAndHistoricalEventsUseTheSameDynamicClassification()
    {
        SysOperLog operLog = new SysOperLog();
        operLog.setTitle("课程管理");
        operLog.setOperUrl("/business/lesson/283");
        operLog.setStatus(1);
        operLog.setErrorMsg("课程不存在");
        operLog.setCostTime(1L);

        perfEventService.recordFromOperLog(operLog);

        ArgumentCaptor<SysPerfEvent> eventCaptor = ArgumentCaptor.forClass(SysPerfEvent.class);
        verify(perfEventMapper).insertSysPerfEvent(eventCaptor.capture());
        assertEquals("info", eventCaptor.getValue().getSeverity());
        assertEquals("business", eventCaptor.getValue().getCategory());

        SysPerfEvent historical = new SysPerfEvent();
        historical.setEventType("error_api");
        historical.setSeverity("critical");
        historical.setSourceUrl("/business/lesson/283");
        historical.setErrorMsg("课程不存在");
        historical.setDurationMs(1L);
        when(perfEventMapper.selectRecentEvents(any(), isNull())).thenReturn(Collections.singletonList(historical));

        List<SysPerfEvent> events = perfEventService.selectRecentEvents(24, null);

        assertEquals("info", events.get(0).getSeverity());
        assertEquals("business", events.get(0).getCategory());
    }

    private DiagnosisAdvice studentImportAdvice(long durationMs)
    {
        return DiagnosisAdvisor.adviseForApiEvent(
                "/business/student/importData", null, durationMs, false);
    }
}
