INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.38', '学生登录输入响应优化',
       '登录页延迟加载非关键展厅接口并启用轮播图片懒加载，减少账号密码输入阶段的主线程和网络竞争，提升学生登录输入响应速度。',
       NOW(), 'PUBLISHED', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.38');
UPDATE biz_platform_update
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.38';
