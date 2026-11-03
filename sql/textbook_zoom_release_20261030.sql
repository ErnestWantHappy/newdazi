-- 130 发布登记（2026-10-30，1.30.62，幂等）
-- release: 20261030_textbook_zoom_v1（仅前端；后端保持 1.30.61）
-- 仅新增一条平台更新记录并置为 PUBLISHED；不修改其他业务数据。

INSERT INTO biz_platform_update (version_no, title, content, published_at, status, create_by, create_time)
SELECT
    '1.30.62',
    '电子教材阅读缩放',
    CONCAT(
        '电子教材阅读器新增缩放：默认“适应整页”，打开时一屏可看完整一页；',
        '可切换“适应宽度”，或选择 50%–200% 的固定比例，选择会记住。\n',
        '手机/窄屏默认收起目录，展开目录时以浮层显示，不再挤占正文。',
        '“打印本课”仍按整页输出，不受阅读缩放影响。'
    ),
    NOW(),
    'PUBLISHED',
    'admin',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.62') AS t
);

-- 兼容重复执行：若已存在但仍是草稿，则补正为 PUBLISHED
UPDATE biz_platform_update
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.62' AND status <> 'PUBLISHED';

-- 验证
SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update
WHERE version_no IN ('1.30.61', '1.30.62')
ORDER BY update_id;
