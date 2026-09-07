-- 1.30.2 教师效率小包发布记录（2026-09-05）。
-- 仅登记平台更新，不变更业务表结构；按版本号幂等。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.30.2',
  '教师效率小包：毕业年级折叠与协作成员可查',
  '教师首页已毕业年级默认折叠；学生协作卡区分本组与全班；协作编辑器在线成员可点击查看权限与状态。后端453单测通过，无增量SQL。',
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.2');

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.30.2';
