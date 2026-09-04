-- 教研通知图片保存与公开分享显示热修发布记录（2026-09-04）。
-- 本脚本仅登记平台更新，不修改业务表结构或通知正文。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.29.1',
  '教研通知图片保存与分享显示修复',
  '修复图片作为编辑器最后一步操作时未进入通知正文，以及公开分享页无法识别当前资源访问地址的问题；不修改已有通知正文。',
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_platform_update WHERE version_no = '1.29.1'
);

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.29.1';
