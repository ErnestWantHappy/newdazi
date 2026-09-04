-- 教研通知公开分享页面与复制热修发布记录（2026-09-02）。
-- 仅登记前端热修，不修改业务表结构；执行前须完成平台更新表备份。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.28.5',
  '教研通知公开分享页面与复制修复',
  '修复公开分享链接页面空白，以及正式 HTTP 地址下点击复制失败的问题；后端接口和数据库结构不变。',
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_platform_update WHERE version_no = '1.28.5'
);

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.28.5';
