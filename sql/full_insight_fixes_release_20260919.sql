-- 只登记更新草稿，不改动正式成绩，不自动发布更新通知。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.16', '课堂问题修复与班级 AI 学情诊断',
    '优化临时会话失败处理；区分批改页全题型人数、操作题份数和当前题人数，修复未交学生导航；保留流程图连线路径；题库支持完整只读详情；恢复教研员打字均速统计。上线班级 AI 学情诊断 V3.3.1，展示知识点、具体错误及原卷证据，参考争议单独提示，诊断不自动改动正式成绩。',
    NOW(), 'DRAFT', 'codex', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.16');
