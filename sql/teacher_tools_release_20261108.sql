-- 仅登记教师工具 1.30.80 产品更新，不修改工具、分类、任务、收藏或任何业务数据。
-- 可在同一正式库幂等复跑：版本记录最多一行，并确保最终状态为 PUBLISHED。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time, update_by, update_time)
SELECT
    '1.30.80',
    '教师工具查找、筛选与精选排序修复',
    '教师工具页修复教研精选排序，精选按教研推荐顺序展示；教学任务、来源和访问条件筛选后只显示一份结果，不再重复。来源名称改为“原3005站”“原80根站”，卡片与筛选统一显示“直接打开”。修复键盘收藏时误打开工具、浏览器拦截弹窗仍记最近打开、登录信息未加载时收藏假成功等问题。管理端现在可直接查看已配置的检索别名。',
    NOW(),
    'PUBLISHED',
    'admin',
    NOW(),
    'admin',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.80') existing_version
);

UPDATE biz_platform_update
SET title = '教师工具查找、筛选与精选排序修复',
    content = '教师工具页修复教研精选排序，精选按教研推荐顺序展示；教学任务、来源和访问条件筛选后只显示一份结果，不再重复。来源名称改为“原3005站”“原80根站”，卡片与筛选统一显示“直接打开”。修复键盘收藏时误打开工具、浏览器拦截弹窗仍记最近打开、登录信息未加载时收藏假成功等问题。管理端现在可直接查看已配置的检索别名。',
    status = 'PUBLISHED',
    published_at = COALESCE(published_at, NOW()),
    update_by = 'admin',
    update_time = NOW()
WHERE version_no = '1.30.80';

SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update
WHERE version_no = '1.30.80';
