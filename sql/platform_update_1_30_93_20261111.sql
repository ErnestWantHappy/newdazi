-- 仅登记 1.30.93 教材资源面板拖拽调宽与课例标题左对齐，不修改课程、题库、成绩或权限。
-- 脚本幂等；只在同版本标题一致时更新，避免覆盖其他发布。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time, update_by, update_time)
SELECT
    '1.30.93',
    '教材资源面板可拖宽与课例标题左对齐',
    '教材阅读页右侧教学资源面板支持手动拖拽调宽，宽度限制 290 至 720 像素并按浏览器记住；优秀课例和教研活动课例的标题统一左对齐，不再出现逐个课程缩进。手机和窄屏继续使用浮层布局，不显示拖拽条，页面不会横向溢出。',
    NOW(),
    'PUBLISHED',
    'admin',
    NOW(),
    'admin',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.93') existing_version
);

UPDATE biz_platform_update
SET content = '教材阅读页右侧教学资源面板支持手动拖拽调宽，宽度限制 290 至 720 像素并按浏览器记住；优秀课例和教研活动课例的标题统一左对齐，不再出现逐个课程缩进。手机和窄屏继续使用浮层布局，不显示拖拽条，页面不会横向溢出。',
    status = 'PUBLISHED',
    published_at = COALESCE(published_at, NOW()),
    update_by = 'admin',
    update_time = NOW()
WHERE version_no = '1.30.93'
  AND title = '教材资源面板可拖宽与课例标题左对齐';

SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update
WHERE version_no = '1.30.93';
