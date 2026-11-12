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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    void accessBoundaryRejectionsRemainVisibleAsSecurityWarnings()
    {
        DiagnosisAdvice advice = DiagnosisAdvisor.adviseForApiEvent(
                "/business/lesson/283", "无权限查看其他教师的课程", 1, true);

        assertEquals("security", advice.getCategory());
        assertEquals("warning", advice.getSeverity());
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

    @Test
    void historicalSlowSqlWithoutUserDoesNotAssertBackgroundExecution()
    {
        SysPerfEvent historical = new SysPerfEvent();
        historical.setEventType("slow_sql");
        historical.setSqlText("select * from biz_student_answer");
        historical.setDurationMs(3500L);
        historical.setSeverity("info");
        historical.setAdvice("后台任务触发，不占用用户请求");
        when(perfEventMapper.selectRecentEvents(any(), isNull())).thenReturn(Collections.singletonList(historical));

        SysPerfEvent event = perfEventService.selectRecentEvents(24, null).get(0);

        assertEquals("critical", event.getSeverity());
        assertEquals("performance", event.getCategory());
        assertTrue(event.getAdvice().contains("执行来源未记录"));
        assertFalse(event.getAdvice().contains("后台任务触发"));
        assertFalse(event.getAdvice().contains("不占用用户请求"));
    }

    @Test
    void checkinTableAloneDoesNotIdentifyLoginPreviewOrBackgroundExecution()
    {
        DiagnosisAdvice advice = DiagnosisAdvisor.adviseForSlowSql(slowSqlRow(
                "select * from biz_lesson_checkin where student_id = ?", 1700, 0));

        assertEquals("warning", advice.getSeverity());
        assertTrue(advice.getAdvice().contains("执行来源未记录"));
        assertFalse(advice.getAdvice().contains("开课看板"));
        assertFalse(advice.getAdvice().contains("后台预刷新"));
        assertFalse(advice.getAdvice().contains("不占用用户请求"));
    }

    @Test
    void exactLoginPreviewMapperExplainsPurposeWithoutClaimingExecutionSource()
    {
        Map<String, Object> row = slowSqlRow("select * from biz_lesson_checkin", 1700, 0);
        row.put("sourceName", "com.ruoyi.business.mapper.LoginPreviewMapper.selectLessons");
        row.put("operName", "teacher");

        DiagnosisAdvice advice = DiagnosisAdvisor.adviseForSlowSql(row);

        assertEquals("warning", advice.getSeverity());
        assertTrue(advice.getAdvice().contains("登录页开课看板"));
        assertTrue(advice.getAdvice().contains("用途不能证明"));
        assertFalse(advice.getAdvice().contains("执行来源未记录"));
        assertFalse(advice.getAdvice().contains("不占用用户请求"));
        row.put("sourceName", "com.ruoyi.business.mapper.LoginPreviewMapper.selectLessons_COUNT");
        assertFalse(DiagnosisAdvisor.adviseForSlowSql(row).getAdvice().contains("开课看板"));
    }

    @Test
    void criticalThresholdsTakePriorityOverBusinessPurpose()
    {
        for (String sql : Arrays.asList("select * from biz_lesson_checkin",
                "select sum(score) as paper_score from biz_student_answer"))
        {
            for (long[] metrics : new long[][] { { 3000, 0 }, { 1700, 1 } })
            {
                Map<String, Object> row = slowSqlRow(sql, metrics[0], metrics[1]);
                row.put("mapperId", "com.ruoyi.business.mapper.LoginPreviewMapper.selectLessons");
                DiagnosisAdvice advice = DiagnosisAdvisor.adviseForSlowSql(row);

                assertEquals("critical", advice.getSeverity());
                assertTrue(advice.getAdvice().contains("请优先"));
                assertTrue(advice.getAdvice().contains("登录页开课看板"));
                assertFalse(advice.getAdvice().contains("再排查"));
            }
        }
        assertEquals("warning", DiagnosisAdvisor.adviseForSlowSql(
                slowSqlRow("select 1", 2999, 0)).getSeverity());
    }

    @Test
    void overviewAdviceDescribesCacheAndColdStartWaitAtBothSeverityLevels()
    {
        for (long duration : new long[] { 1700, 4400 })
        {
            DiagnosisAdvice advice = DiagnosisAdvisor.adviseForSlowSql(slowSqlRow(
                    "select sum(score) as paper_score from biz_student_answer", duration, 0));

            assertEquals(duration >= 3000 ? "critical" : "warning", advice.getSeverity());
            assertTrue(advice.getAdvice().contains("5 分钟快照缓存"));
            assertTrue(advice.getAdvice().contains("返回旧值并在后台刷新"));
            assertTrue(advice.getAdvice().contains("冷启动无快照时请求最多等待15秒共享构建结果"));
        }
    }

    @Test
    void historicalLoginPreviewEventRetainsPurposeAndCriticalSeverity()
    {
        SysPerfEvent historical = new SysPerfEvent();
        historical.setEventType("slow_sql");
        historical.setSourceName("com.ruoyi.business.mapper.LoginPreviewMapper.selectLessons");
        historical.setSqlText("select * from biz_lesson_checkin");
        historical.setDurationMs(3000L);
        when(perfEventMapper.selectRecentEvents(any(), isNull())).thenReturn(Collections.singletonList(historical));

        SysPerfEvent event = perfEventService.selectRecentEvents(24, null).get(0);

        assertEquals("critical", event.getSeverity());
        assertTrue(event.getAdvice().contains("登录页开课看板"));
        assertTrue(event.getAdvice().contains("执行来源未记录"));
        assertTrue(event.getAdvice().contains("请优先"));
    }

    @Test
    void slowSqlTruncatesLongOperNameBeforePersistence()
    {
        String username = String.join("", Collections.nCopies(80, "用"));
        perfEventService.recordSlowSql("example.Mapper.select", "select 1", 1500, username);

        ArgumentCaptor<SysPerfEvent> captor = ArgumentCaptor.forClass(SysPerfEvent.class);
        verify(perfEventMapper).insertSysPerfEvent(captor.capture());
        assertEquals(username.substring(0, 64), captor.getValue().getOperName());
        assertEquals("slow_sql", captor.getValue().getEventType());
        assertEquals(0L, perfEventService.getInsertFailCount());
    }

    @Test
    void slowAndErrorApiTruncateLongOperNameBeforePersistence()
    {
        String username = String.join("", Collections.nCopies(80, "u"));
        SysOperLog operLog = new SysOperLog();
        operLog.setOperName(username);
        operLog.setOperUrl("/business/example");
        operLog.setCostTime(1500L);
        operLog.setStatus(0);
        perfEventService.recordFromOperLog(operLog);
        operLog.setStatus(1);
        operLog.setCostTime(10L);
        operLog.setErrorMsg("数据库连接中断");
        perfEventService.recordFromOperLog(operLog);

        ArgumentCaptor<SysPerfEvent> captor = ArgumentCaptor.forClass(SysPerfEvent.class);
        org.mockito.Mockito.verify(perfEventMapper, org.mockito.Mockito.times(2))
                .insertSysPerfEvent(captor.capture());
        assertEquals("slow_api", captor.getAllValues().get(0).getEventType());
        assertEquals("error_api", captor.getAllValues().get(1).getEventType());
        for (SysPerfEvent event : captor.getAllValues())
        {
            assertEquals(username.substring(0, 64), event.getOperName());
        }
        assertEquals(username, operLog.getOperName());
    }

    @Test
    void whitespaceOperNameRemainsUnknownWhenPersistedAndDisplayed()
    {
        perfEventService.recordSlowSql("example.Mapper.select", "select 1", 1500, "   ");
        ArgumentCaptor<SysPerfEvent> captor = ArgumentCaptor.forClass(SysPerfEvent.class);
        verify(perfEventMapper).insertSysPerfEvent(captor.capture());
        assertNull(captor.getValue().getOperName());
        when(perfEventMapper.selectRecentEvents(any(), isNull()))
                .thenReturn(Collections.singletonList(captor.getValue()));

        assertTrue(perfEventService.selectRecentEvents(24, null).get(0).getAdvice().contains("执行来源未记录"));
    }

    private Map<String, Object> slowSqlRow(String sql, long maxTimespan, long runningCount)
    {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("sql", sql);
        row.put("maxTimespan", maxTimespan);
        row.put("runningCount", runningCount);
        return row;
    }

    private DiagnosisAdvice studentImportAdvice(long durationMs)
    {
        return DiagnosisAdvisor.adviseForApiEvent(
                "/business/student/importData", null, durationMs, false);
    }
}
