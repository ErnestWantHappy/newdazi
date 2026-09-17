-- 小学实验板学生端 Python 入口默认显示（2026-08-31）。
-- 按版本号幂等；正式库执行前须先完成整库备份。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.27.3',
  '小学实验板 Python 一键复制入口',
  CONCAT('学生物联网页面默认展示小学实验板 Python 代码入口，提供一键复制本人小组代码。\n',
         '初中掌控板 Mind+ 连接参数继续保留为兼容入口；不改变后端 MQTT 账号、Topic、权限和数据库结构。'),
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_platform_update WHERE version_no = '1.27.3'
);

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.27.3';
