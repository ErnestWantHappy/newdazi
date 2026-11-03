-- 130 发布登记（2026-11-02，1.30.65，幂等）
-- release: 20261102_extension_dual_role_v1（仅后端热更，替换内嵌业务 JAR）
-- 仅新增一条平台更新记录并置为 PUBLISHED；不修改其他业务数据。

INSERT INTO biz_platform_update (version_no, title, content, published_at, status, create_by, create_time)
SELECT
    '1.30.65',
    '拓展课管理权限调整',
    CONCAT(
        '同时担任教研员的教师，现在也可以创建和管理自己的拓展班。\n',
        '拓展班负责人下拉仍只列纯教师，避免把教研员身份的用户派成负责人；学生和纯教研员没有拓展班管理权限。'
    ),
    NOW(),
    'PUBLISHED',
    'admin',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.65') AS t
);

-- 兼容重复执行：若已存在但仍是草稿，则补正为 PUBLISHED
UPDATE biz_platform_update
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.65' AND status <> 'PUBLISHED';

-- 验证
SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update
WHERE version_no IN ('1.30.64', '1.30.65')
ORDER BY update_id;
