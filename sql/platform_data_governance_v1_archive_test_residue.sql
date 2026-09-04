-- 测试残留软归档脚本。
-- 必须先执行 platform_data_governance_v1_preflight.sql、完成备份和人工确认。
-- 只改变父记录删除标记，不删除主题回复、导学单答案或成绩数据。
START TRANSACTION;

UPDATE biz_research_topic
SET del_flag = '2', update_by = 'codex-audit', update_time = NOW()
WHERE del_flag = '0' AND title LIKE 'QARA0811CONC%';

UPDATE biz_guide_sheet
SET del_flag = '2', update_by = 'codex-audit', update_time = NOW()
WHERE del_flag = '0' AND is_public = 'Y' AND sheet_title = '测试722';

COMMIT;

SELECT COUNT(*) AS active_research_test_topics
FROM biz_research_topic
WHERE del_flag = '0' AND title LIKE 'QARA0811CONC%';

SELECT COUNT(*) AS active_public_test_sheets
FROM biz_guide_sheet
WHERE del_flag = '0' AND is_public = 'Y' AND sheet_title = '测试722';
