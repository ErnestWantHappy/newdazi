-- 测试残留软归档回滚脚本。
-- 仅在对应备份和执行记录核对无误后使用；不恢复其他删除态数据。
START TRANSACTION;

UPDATE biz_research_topic
SET del_flag = '0', update_by = 'codex-audit-rollback', update_time = NOW()
WHERE del_flag = '2' AND title LIKE 'QARA0811CONC%';

UPDATE biz_guide_sheet
SET del_flag = '0', update_by = 'codex-audit-rollback', update_time = NOW()
WHERE del_flag = '2' AND is_public = 'Y' AND sheet_title = '测试722';

COMMIT;
