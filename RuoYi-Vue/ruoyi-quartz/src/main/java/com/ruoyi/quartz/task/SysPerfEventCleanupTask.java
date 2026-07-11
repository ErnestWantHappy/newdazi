package com.ruoyi.quartz.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.system.service.ISysPerfEventService;

/**
 * 性能事件清理定时任务
 */
@Component("sysPerfEventCleanupTask")
public class SysPerfEventCleanupTask
{
    @Autowired
    private ISysPerfEventService perfEventService;

    public String cleanupExpiredPerfEvents()
    {
        int deleted = perfEventService.cleanupExpiredEvents(7);
        return String.format("性能事件清理完成，删除 %s 条过期记录", deleted);
    }
}
