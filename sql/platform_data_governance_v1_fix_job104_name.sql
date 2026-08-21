-- 将已确认用途的性能事件清理任务恢复为可读名称。
-- 前置条件：已备份 sys_job.job_id=104，且调用目标保持不变。
UPDATE sys_job
SET job_name = '系统性能事件清理'
WHERE job_id = 104
  AND invoke_target = 'sysPerfEventCleanupTask.cleanupExpiredPerfEvents'
  AND (job_name LIKE '%?%' OR job_name LIKE '%�%');

SELECT job_id, job_name, invoke_target, status
FROM sys_job
WHERE job_id = 104;
