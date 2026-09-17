-- 学生导入班级号范围扩展至 01～99（2026-08-31）。
-- 仅记录前后端规则修复，正式库执行前须先完成整库备份。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.27.5',
  '学生导入班级号扩展至 01～99',
  CONCAT('学生新增、Excel 导入及上传前校验统一允许班级号 01～99，支持 11 班及以上；',
         '00、100 及 601/602 等带年级的三位数仍会被拒绝。'),
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_platform_update WHERE version_no = '1.27.5'
);

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.27.5';
