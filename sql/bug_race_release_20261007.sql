-- 并发与状态同步问题修复通过正式验收后登记。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.36', '并发操作与页面状态同步修复',
    '修复区域抽测快速切换答卷、筛选和学生明细时的旧数据覆盖；修复成绩分析切换条件后的状态残留；修复Python题单切题、草稿保存与判题结果错位；整班PDF拆分改为按原件排队，减少不同原件互相等待。',
    NOW(), 'PUBLISHED', 'codex', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.36');
UPDATE biz_platform_update
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.36' AND status = 'DRAFT';
