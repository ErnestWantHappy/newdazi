-- 平台更新记录：1.30.33 全站统一全新高清Logo与Favicon焕新升级
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.33', '全站统一全新高清Logo与Favicon焕新升级',
    '1. 平台全局统一替换为课题申报专属徽标Logo，涵盖登录页顶栏、管理后台侧边栏、学生端三大核心顶栏；2. 浏览器Favicon全面同步生成16/32/48/64多尺寸高清ICO图标；3. 登录页全新帆船徽标配备柔和微光发散滤镜，与四模块现代布局深度融合；4. 生产环境已全面编译并部署上线。',
    NOW(), 'PUBLISHED', 'codex', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.33');
UPDATE biz_platform_update
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.33' AND status = 'DRAFT';
