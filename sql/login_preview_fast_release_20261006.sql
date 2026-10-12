-- 性能修复正式部署并通过验收后登记；无结构迁移或成绩修改。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.30', '登录页学校动态加载提速',
    '优化登录页区域学校开课动态的查询和缓存，后台提前更新概览，打开页面及缓存刷新期间可以更快看到学校动态。继续展示近90天真实开课、参与人数和批改概况；活动内容、学生明细及完整成绩仍需登录并按原有权限查看。',
    NOW(), 'PUBLISHED', 'codex', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.30');
UPDATE biz_platform_update
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.30' AND status = 'DRAFT';
