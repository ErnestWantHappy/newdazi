-- 仅回滚 2026-08-20 已确认的两项治理：job_id=104 显示名称、sheet_id=1 测试导学单软归档。
-- 执行前必须核对对应备份，禁止扩大到其他任务或导学单。
START TRANSACTION;

UPDATE sys_job
SET job_name = '????????'
WHERE job_id = 104
  AND invoke_target = 'sysPerfEventCleanupTask.cleanupExpiredPerfEvents'
  AND job_name = '系统性能事件清理';

UPDATE biz_guide_sheet
SET del_flag = '0', update_by = 'codex-audit-rollback', update_time = NOW()
WHERE sheet_id = 1
  AND sheet_title = '测试722'
  AND is_public = 'Y'
  AND del_flag = '2';

COMMIT;
