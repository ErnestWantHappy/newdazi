package com.ruoyi.system.service;

import java.util.Date;
import java.util.List;
import com.ruoyi.system.domain.SysOperLog;
import com.ruoyi.system.domain.SysPerfEvent;

/**
 * 系统性能事件 服务层
 */
public interface ISysPerfEventService
{
    void recordSlowSql(String mapperId, String sqlText, long durationMs);

    void recordFromOperLog(SysOperLog operLog);

    List<SysPerfEvent> selectRecentEvents(int hours, String eventType);

    int cleanupExpiredEvents(int retentionDays);
}
