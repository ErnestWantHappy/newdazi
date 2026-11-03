-- 130 发布登记（2026-11-02，1.30.64，幂等）
-- release: 20261102_extension_textbook_v1（后端 + 前端）
-- 仅新增一条平台更新记录并置为 PUBLISHED；不修改其他业务数据。

INSERT INTO biz_platform_update (version_no, title, content, published_at, status, create_by, create_time)
SELECT
    '1.30.64',
    '拓展课空间上线与课程教材绑定',
    CONCAT(
        '本次更新包含三部分：\n',
        '1. 新增“拓展课”空间：教师可建拓展班、维护名单、按班上课与签到、发布拓展题单并查看学情；',
        '学生登录后可选择“常规课 / 拓展课”，选择跟着账号保存，换电脑也不会丢。历史拓展名单按发布时快照冻结，学生移出后历史学情分母不变。\n',
        '2. 课程设计可绑定电子教材的课次，课堂上可直接打开对应课次；电子教材新增“保存本课 PDF / 下载整册”，',
        '修复了部分整册 PDF 少页、多人同时导出时卡住的问题，缓存目录也会自动清理。\n',
        '3. 修复拓展题单在部分学校名单排序规则下报错（1267）的问题；学生登录不再因拓展接口异常而失败，',
        '入口异常时自动回到常规课首页。'
    ),
    NOW(),
    'PUBLISHED',
    'admin',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.64') AS t
);

-- 兼容重复执行：若已存在但仍是草稿，则补正为 PUBLISHED
UPDATE biz_platform_update
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.64' AND status <> 'PUBLISHED';

-- 验证
SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update
WHERE version_no IN ('1.30.63', '1.30.64')
ORDER BY update_id;
