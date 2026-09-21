-- 已核实上线的五个版本补为教师可见，保留原版本日期和内容。
-- 执行前备份biz_platform_update；不批量发布其他历史草稿。
START TRANSACTION;
UPDATE biz_platform_update
SET status = 'PUBLISHED', update_by = 'codex', update_time = NOW()
WHERE status = 'DRAFT'
  AND ((update_id = 92 AND version_no = '1.30.12')
    OR (update_id = 93 AND version_no = '1.30.13')
    OR (update_id = 94 AND version_no = '1.30.14')
    OR (update_id = 95 AND version_no = '1.30.15')
    OR (update_id = 96 AND version_no = '1.30.16'));
COMMIT;
