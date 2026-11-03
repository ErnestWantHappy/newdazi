package com.ruoyi.system.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
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

    /** 慢 SQL 真正进入 recordSlowSql 的次数，用于自检拦截器是否生效。 */
    long getSlowSqlRecordCount();

    /** 性能事件写库失败次数与最后一次错误，避免 safeInsert 静默吞掉问题。 */
    long getInsertFailCount();

    String getLastInsertError();

    Date getLastInsertFailTime();

    /** 执行一次带标记的慢查询，验证拦截、异步和入库整条链路。 */
    Map<String, Object> runSlowSqlSelfTest();
}
