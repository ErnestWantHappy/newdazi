-- 130 发布登记（2026-10-29，1.30.61，幂等）
-- release: 20261029_textbook_seat_v1（后端 + 前端；机位助手仍为 0.2.5）
-- 仅新增一条平台更新记录并置为 PUBLISHED；不修改其他业务数据。

INSERT INTO biz_platform_update (version_no, title, content, published_at, status, create_by, create_time)
SELECT
    '1.30.61',
    '电子教材上线与机位助手识别修复',
    CONCAT(
        '本次更新包含两部分：\n',
        '1. 教师端新增“电子教材”：3–9 年级共 13 册、1301 页，可在书架打开整册、按课次定位，并打印本课；',
        '页面图片经登录鉴权后返回，不提供整册下载。连续多页加载失败时会给出整体提示并可重试；打印准备期间切换页面不再缺图。\n',
        '2. 修复部分电脑已安装机位助手 0.2.5、网页却仍显示“机位未关联”的问题；',
        '已安装 0.2.5 的电脑无需重新下载，刷新页面即可恢复识别。助手长期无法识别浏览器时会提示请老师人工确认机位，答题不受影响。'
    ),
    NOW(),
    'PUBLISHED',
    'admin',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.61') AS t
);

-- 兼容重复执行：若已存在但仍是草稿，则补正为 PUBLISHED
UPDATE biz_platform_update
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.61' AND status <> 'PUBLISHED';

-- 验证
SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update
WHERE version_no IN ('1.30.60', '1.30.61')
ORDER BY update_id;
