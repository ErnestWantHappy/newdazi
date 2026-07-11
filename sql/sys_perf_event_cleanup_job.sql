-- 性能事件清理定时任务：每天凌晨 2 点删除 7 天前的记录

UPDATE sys_job
SET job_name = '性能事件过期清理',
    job_group = 'DEFAULT',
    cron_expression = '0 0 2 * * ?',
    misfire_policy = '3',
    concurrent = '1',
    status = '0',
    update_by = 'admin',
    update_time = NOW(),
    remark = '每天清理 7 天前的 sys_perf_event 性能事件'
WHERE invoke_target = 'sysPerfEventCleanupTask.cleanupExpiredPerfEvents';

INSERT INTO sys_job
    (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status,
     create_by, create_time, remark)
SELECT '性能事件过期清理',
       'DEFAULT',
       'sysPerfEventCleanupTask.cleanupExpiredPerfEvents',
       '0 0 2 * * ?',
       '3',
       '1',
       '0',
       'admin',
       NOW(),
       '每天清理 7 天前的 sys_perf_event 性能事件'
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_job
    WHERE invoke_target = 'sysPerfEventCleanupTask.cleanupExpiredPerfEvents'
);

SELECT job_id, job_name, invoke_target, cron_expression, status
FROM sys_job
WHERE invoke_target = 'sysPerfEventCleanupTask.cleanupExpiredPerfEvents';
