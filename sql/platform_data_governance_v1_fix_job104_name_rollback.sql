-- 回滚 job_id=104 的名称；执行前请从备份文件确认原始值。
UPDATE sys_job
SET job_name = '????????'
WHERE job_id = 104
  AND invoke_target = 'sysPerfEventCleanupTask.cleanupExpiredPerfEvents';
