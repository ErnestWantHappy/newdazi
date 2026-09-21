-- 仅登记更新草稿，不自动通知教师，不修改学生成绩或表结构。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.14', '批量采用 AI 建议与教师首页修复',
    '修正线上批量采用仍跳过待核对建议的问题，按教师确认的方式写入正式成绩并保留审计；未写入任何成绩时显示提醒。修复从批改页返回教师首页白屏。',
    NOW(), 'DRAFT', 'codex', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.14');
