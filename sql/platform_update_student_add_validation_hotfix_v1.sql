-- 单个新增学生学号校验热修（2026-09-01）。
-- 仅登记正式前端发布记录，不修改业务表结构；执行前须完成相关表备份。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.27.6',
  '单个新增学生学号校验修复',
  CONCAT('修复学生管理中学号 10～99 被前端错误拦截的问题；',
         '01～99 均可录入，00 与 100 仍会被拒绝。'),
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_platform_update WHERE version_no = '1.27.6'
);

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.27.6';
