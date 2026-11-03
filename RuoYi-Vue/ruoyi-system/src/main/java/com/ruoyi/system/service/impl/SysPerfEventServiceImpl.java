package com.ruoyi.system.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.diagnosis.DiagnosisAdvice;
import com.ruoyi.common.utils.diagnosis.DiagnosisAdvisor;
import com.ruoyi.common.utils.sql.SqlBusinessDescriber;
import com.ruoyi.system.domain.SysOperLog;
import com.ruoyi.system.domain.SysPerfEvent;
import com.ruoyi.system.mapper.SysPerfEventMapper;
import com.ruoyi.system.service.ISysPerfEventService;

/**
 * 系统性能事件 服务层处理
 */
@Service
public class SysPerfEventServiceImpl implements ISysPerfEventService
{
    private static final long SLOW_API_THRESHOLD_MS = 1000L;

    private static final long SELF_TEST_TIMEOUT_MS = 3000L;

    private final AtomicLong slowSqlRecordCount = new AtomicLong();

    private final AtomicLong insertFailCount = new AtomicLong();

    private volatile String lastInsertError;

    private volatile Date lastInsertFailTime;

    @Autowired
    private SysPerfEventMapper perfEventMapper;

    @Override
    public void recordSlowSql(String mapperId, String sqlText, long durationMs)
    {
        if (durationMs < SLOW_API_THRESHOLD_MS || StringUtils.isEmpty(sqlText))
        {
            return;
        }
        slowSqlRecordCount.incrementAndGet();
        SqlBusinessDescriber.SqlDescription desc = SqlBusinessDescriber.describe(mapperId, sqlText);
        SysPerfEvent event = new SysPerfEvent();
        event.setEventType("slow_sql");
        event.setSeverity(durationMs >= 3000 ? "critical" : "warning");
        event.setTitle(desc.getTitle());
        event.setDescription(desc.getDescription());
        event.setSourceName(StringUtils.isNotEmpty(mapperId) ? mapperId : null);
        event.setSqlText(truncate(sqlText, 4000));
        event.setSqlHash(hashSql(sqlText));
        event.setDurationMs(durationMs);
        event.setOccurTime(new Date());
        safeInsert(event);
    }

    @Override
    public void recordFromOperLog(SysOperLog operLog)
    {
        if (operLog == null)
        {
            return;
        }
        boolean isError = operLog.getStatus() != null && operLog.getStatus() == 1;
        long costTime = operLog.getCostTime() == null ? 0L : operLog.getCostTime();
        boolean isSlow = costTime >= SLOW_API_THRESHOLD_MS;
        if (!isError && !isSlow)
        {
            return;
        }
        SysPerfEvent event = new SysPerfEvent();
        event.setEventType(isError ? "error_api" : "slow_api");
        DiagnosisAdvice advice = DiagnosisAdvisor.adviseForApiEvent(
                operLog.getOperUrl(), operLog.getErrorMsg(), costTime, isError);
        event.setSeverity(advice.getSeverity());
        event.setCategory(advice.getCategory());
        event.setAdvice(advice.getAdvice());
        event.setTitle(StringUtils.isNotEmpty(operLog.getTitle()) ? operLog.getTitle() : "接口请求");
        event.setDescription(buildApiDescription(operLog, isError, isSlow));
        event.setSourceName(operLog.getTitle());
        event.setSourceUrl(operLog.getOperUrl());
        event.setDurationMs(costTime);
        event.setErrorMsg(truncate(operLog.getErrorMsg(), 2000));
        event.setOperName(operLog.getOperName());
        event.setDeptName(operLog.getDeptName());
        event.setOccurTime(operLog.getOperTime() != null ? operLog.getOperTime() : new Date());
        safeInsert(event);
    }

    @Override
    public List<SysPerfEvent> selectRecentEvents(int hours, String eventType)
    {
        int safeHours = Math.min(Math.max(hours, 1), 168);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -safeHours);
        List<SysPerfEvent> events = perfEventMapper.selectRecentEvents(
                calendar.getTime(), StringUtils.isEmpty(eventType) ? null : eventType);
        for (SysPerfEvent event : events)
        {
            enrichEvent(event);
        }
        return events;
    }

    @Override
    public int cleanupExpiredEvents(int retentionDays)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -Math.max(retentionDays, 1));
        return perfEventMapper.deleteBefore(calendar.getTime());
    }

    private String buildApiDescription(SysOperLog operLog, boolean isError, boolean isSlow)
    {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotEmpty(operLog.getOperUrl()))
        {
            builder.append("接口 ").append(operLog.getOperUrl());
        }
        if (isSlow)
        {
            builder.append("，耗时 ").append(operLog.getCostTime()).append(" ms");
        }
        if (isError)
        {
            builder.append("，执行异常");
        }
        if (StringUtils.isNotEmpty(operLog.getOperName()))
        {
            builder.append("，操作用户 ").append(operLog.getOperName());
        }
        return builder.toString();
    }

    private void safeInsert(SysPerfEvent event)
    {
        try
        {
            perfEventMapper.insertSysPerfEvent(event);
        }
        catch (Exception e)
        {
            // 表未初始化或写入失败时不阻断主流程，但必须留下可观测计数，便于诊断页定位。
            insertFailCount.incrementAndGet();
            lastInsertError = truncate(e.getMessage(), 500);
            lastInsertFailTime = new Date();
        }
    }

    private void enrichEvent(SysPerfEvent event)
    {
        if (event == null)
        {
            return;
        }
        if ("error_api".equals(event.getEventType()) || "slow_api".equals(event.getEventType()))
        {
            boolean errorEvent = "error_api".equals(event.getEventType());
            DiagnosisAdvice advice = DiagnosisAdvisor.adviseForApiEvent(
                    event.getSourceUrl(), event.getErrorMsg(),
                    event.getDurationMs() == null ? 0L : event.getDurationMs(), errorEvent);
            // 历史事件也动态重算，发布后无需改表或批量更新旧记录。
            event.setSeverity(advice.getSeverity());
            event.setCategory(advice.getCategory());
            event.setAdvice(advice.getAdvice());
            return;
        }
        event.setCategory("performance");
        if (StringUtils.isEmpty(event.getAdvice()))
        {
            event.setAdvice("结合 SQL、发生时间和关联接口确认是否持续影响业务");
        }
    }

    @Override
    public long getSlowSqlRecordCount()
    {
        return slowSqlRecordCount.get();
    }

    @Override
    public long getInsertFailCount()
    {
        return insertFailCount.get();
    }

    @Override
    public String getLastInsertError()
    {
        return lastInsertError;
    }

    @Override
    public Date getLastInsertFailTime()
    {
        return lastInsertFailTime;
    }

    @Override
    public Map<String, Object> runSlowSqlSelfTest()
    {
        long before = slowSqlRecordCount.get();
        long start = System.currentTimeMillis();
        Long eventId = null;
        try
        {
            // 必须走 MyBatis，JdbcTemplate 不经过 SlowSqlInterceptor，无法验证持久化链路。
            perfEventMapper.selfTestSlowQuery();
            long deadline = start + SELF_TEST_TIMEOUT_MS;
            while (System.currentTimeMillis() < deadline)
            {
                eventId = perfEventMapper.selectSelfTestEventId();
                if (eventId != null)
                {
                    break;
                }
                Thread.sleep(100L);
            }
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
        catch (Exception e)
        {
            lastInsertError = truncate(e.getMessage(), 500);
            lastInsertFailTime = new Date();
        }
        boolean intercepted = slowSqlRecordCount.get() > before;
        boolean persisted = eventId != null;
        // 事件已落库说明拦截器必然已经执行；异步计数存在毫秒级先后差，不能反过来否定 persisted。
        intercepted = intercepted || persisted;
        String hint;
        if (persisted)
        {
            hint = "slow_sql 拦截、异步写入和入库链路正常";
        }
        else if (intercepted)
        {
            hint = "已进入拦截器但未写入 sys_perf_event：检查表结构、字段长度和异步线程日志";
        }
        else
        {
            hint = "未进入 SlowSqlInterceptor：确认部署 JAR 已注册 MyBatis 插件且 SQL 实际耗时至少 1000ms";
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("intercepted", intercepted);
        result.put("persisted", persisted);
        result.put("eventId", eventId);
        result.put("elapsedMs", System.currentTimeMillis() - start);
        result.put("hint", hint);
        return result;
    }

    private String truncate(String value, int maxLength)
    {
        if (value == null)
        {
            return null;
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    private String hashSql(String sqlText)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalizeSql(sqlText).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash)
            {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private String normalizeSql(String sqlText)
    {
        return sqlText.replaceAll("\\s+", " ").trim().toLowerCase();
    }
}
