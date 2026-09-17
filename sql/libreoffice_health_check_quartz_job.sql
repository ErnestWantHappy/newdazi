-- LibreOffice 分钟级健康巡检与自愈任务（方案 B）
-- 每分钟探测进程池；不健康且冷却结束后 cleanup+重启，并联动捞回卡住预览。
-- 日级 libreOfficeMaintenanceTask.cleanupAndRestart 仍保留作兜底。
-- 幂等：已存在则更新 cron/状态，不存在则插入。

UPDATE sys_job
SET job_name = 'LibreOffice分钟级健康自愈',
    job_group = 'DEFAULT',
    cron_expression = '0 0/1 * * * ?',
    misfire_policy = '3',
    concurrent = '1',
    status = '0',
    update_by = 'admin',
    update_time = NOW(),
    remark = '每分钟健康检查；不健康时清理重启Office池并捞回卡住预览，冷却期内跳过'
WHERE invoke_target = 'libreOfficeMaintenanceTask.healthCheckAndRecover';

INSERT INTO sys_job
    (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status,
     create_by, create_time, remark)
SELECT 'LibreOffice分钟级健康自愈',
       'DEFAULT',
       'libreOfficeMaintenanceTask.healthCheckAndRecover',
       '0 0/1 * * * ?',
       '3',
       '1',
       '0',
       'admin',
       NOW(),
       '每分钟健康检查；不健康时清理重启Office池并捞回卡住预览，冷却期内跳过'
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_job
    WHERE invoke_target = 'libreOfficeMaintenanceTask.healthCheckAndRecover'
);

-- 确认日级维护仍存在（不改其 cron，仅保证有记录）
UPDATE sys_job
SET job_name = 'LibreOffice每日维护清理',
    remark = '每天00:00全量清理残留LibreOffice进程并重启服务池（兜底，非高峰自愈主路径）'
WHERE invoke_target = 'libreOfficeMaintenanceTask.cleanupAndRestart';

SELECT job_id, job_name, invoke_target, cron_expression, status, remark
FROM sys_job
WHERE invoke_target IN (
    'libreOfficeMaintenanceTask.healthCheckAndRecover',
    'libreOfficeMaintenanceTask.cleanupAndRestart'
)
ORDER BY invoke_target;
