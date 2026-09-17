-- 仅清理 2026-08-11 WPS 生产激活验收生成的匿名诊断探针。
-- room_id IS NULL 是额外保护，真实协作房间事件不会被删除。
SELECT COUNT(*) AS probe_rows_before
FROM biz_collab_callback_event
WHERE room_id IS NULL
  AND public_file_id IN ('probe-restored', 'probe-after-config', 'codex-config-probe');

DELETE FROM biz_collab_callback_event
WHERE room_id IS NULL
  AND public_file_id IN ('probe-restored', 'probe-after-config', 'codex-config-probe');

SELECT ROW_COUNT() AS probe_rows_deleted;

SELECT COUNT(*) AS probe_rows_after
FROM biz_collab_callback_event
WHERE room_id IS NULL
  AND public_file_id IN ('probe-restored', 'probe-after-config', 'codex-config-probe');
