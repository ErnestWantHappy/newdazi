-- 130 全量发布登记（2026-10-28，1.30.60，幂等）
-- release: 20261028_fullstack_v1（后端 + 前端 + 机位助手 0.2.5）
-- 仅新增一条平台更新记录并置为 PUBLISHED；不修改其他业务数据。

INSERT INTO biz_platform_update (version_no, title, content, published_at, status, create_by, create_time)
SELECT
    '1.30.60',
    '导学单作答与机位登录顺序修复（含机位助手 0.2.5）',
    CONCAT(
        '本次更新包含四部分：\n',
        '1. 电子导学单：学生作答过程中每 2 秒自动保存，保存失败会把内容暂存在本机并可一键重试；',
        '必答项没填时会自动跳到那一页并指出是哪一题；评分结果改为“已得 X / Y 分”，待老师批改的题目不再显示成红色的 0 分；', 
        '教师看板的“正确率”改为按满分计算（此前满分 45 分会显示成 45%）。\n',
        '2. 模板库预览：预览别人的公共导学单时改为只渲染学生可见内容，不再执行模板内的脚本。\n',
        '3. 机位助手：修复同一台电脑换账号登录后，座位卡片仍显示上一位同学电脑地址的问题；',
        '同一座位以最新一次登录为准，旧登录不再续期。未安装或未连接助手时不再显示空的机位块。\n',
        '4. 机位助手升级到 0.2.5：请在学生端“下载机位助手”重新下载安装，覆盖旧版即可。\n',
        '另修复诊断中心慢 SQL 采样缺失、资源鉴权与班级统计查询变慢等性能问题。'
    ),
    NOW(),
    'PUBLISHED',
    'admin',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.60') AS t
);

-- 兼容重复执行：若已存在但仍是草稿，则补正为 PUBLISHED
UPDATE biz_platform_update
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.60' AND status <> 'PUBLISHED';

-- 验证
SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update
WHERE version_no IN ('1.30.59', '1.30.60')
ORDER BY update_id;
