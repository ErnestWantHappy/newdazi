-- 130 发布登记（2026-10-30，1.30.63，幂等）
-- release: 20261030_seat_helper_026_v1（仅机位助手下载文件热替换；前后端版本不变）
-- 仅新增/修正一条平台更新记录并置为 PUBLISHED；不修改其他业务数据。
-- 路径中的反斜杠用 CHAR(92) 生成，避免 MySQL 字符串转义吞掉。

INSERT INTO biz_platform_update (version_no, title, content, published_at, status, create_by, create_time)
SELECT
    '1.30.63',
    '机位助手 0.2.6：默认安装到 C 盘',
    CONCAT(
        '机位助手安装包升级到 0.2.6：默认安装目录由 D:', CHAR(92), 'SchoolSeatHelper 改为 C:', CHAR(92), 'SchoolSeatHelper，',
        '方便机房同传和还原时保留助手、配置与自启动项。', CHAR(10),
        '已装旧版的电脑请先退出助手，再重新下载安装 0.2.6；首次启动会自动把 D 盘旧目录中的配置和平台地址迁移到 C 盘，旧目录保留，便于回退。', CHAR(10),
        '网页端已兼容 0.2.3 及以上版本，不需要再次更新网页。'
    ),
    NOW(),
    'PUBLISHED',
    'admin',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.63') AS t
);

-- 兼容重复执行：若已存在但仍是草稿，则补正为 PUBLISHED
UPDATE biz_platform_update
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.63' AND status <> 'PUBLISHED';

-- 修正首次登记时被转义吞掉的反斜杠（仅匹配错误文案，不动正常文案）
UPDATE biz_platform_update
SET content = CONCAT(
    '机位助手安装包升级到 0.2.6：默认安装目录由 D:', CHAR(92), 'SchoolSeatHelper 改为 C:', CHAR(92), 'SchoolSeatHelper，',
    '方便机房同传和还原时保留助手、配置与自启动项。', CHAR(10),
    '已装旧版的电脑请先退出助手，再重新下载安装 0.2.6；首次启动会自动把 D 盘旧目录中的配置和平台地址迁移到 C 盘，旧目录保留，便于回退。', CHAR(10),
    '网页端已兼容 0.2.3 及以上版本，不需要再次更新网页。'
)
WHERE version_no = '1.30.63' AND content LIKE '%D:SchoolSeatHelper%';

-- 验证
SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update
WHERE version_no IN ('1.30.62', '1.30.63')
ORDER BY update_id;
