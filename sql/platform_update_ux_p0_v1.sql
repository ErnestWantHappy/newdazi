-- 1.30.1 P0体验修复发布记录（2026-09-05，仅前端）。
-- 仅登记平台更新，不变更业务表结构与业务数据；按版本号幂等。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.30.1',
  'P0体验修复：趋势图纵轴与批改五星切换',
  '平台概览趋势图纵轴万位数被截断改用万单位；批改页数字/五星二选一可切换（偏好本地记忆），去掉五星辅助评分文案与悬浮跳动。无增量SQL。',
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.1');

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.30.1';
