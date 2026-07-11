-- LibreOffice 每日维护清理任务
-- 每天 00:00 停止本应用管理的 OfficeManager，清理残留 soffice 进程，再重启服务池。

UPDATE sys_job
SET job_name = 'LibreOffice每日维护清理',
    job_group = 'DEFAULT',
    cron_expression = '0 0 0 * * ?',
    misfire_policy = '3',
    concurrent = '1',
    status = '0',
    update_by = 'admin',
    update_time = NOW(),
    remark = '每天00:00清理残留LibreOffice进程并重启服务池'
WHERE invoke_target = 'libreOfficeMaintenanceTask.cleanupAndRestart';

INSERT INTO sys_job
    (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status,
     create_by, create_time, remark)
SELECT 'LibreOffice每日维护清理',
       'DEFAULT',
       'libreOfficeMaintenanceTask.cleanupAndRestart',
       '0 0 0 * * ?',
       '3',
       '1',
       '0',
       'admin',
       NOW(),
       '每天00:00清理残留LibreOffice进程并重启服务池'
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_job
    WHERE invoke_target = 'libreOfficeMaintenanceTask.cleanupAndRestart'
);

SELECT job_id, job_name, invoke_target, cron_expression, status
FROM sys_job
WHERE invoke_target = 'libreOfficeMaintenanceTask.cleanupAndRestart';
