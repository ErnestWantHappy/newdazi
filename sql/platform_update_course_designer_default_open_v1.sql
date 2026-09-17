-- 课程设计器学生开放开关默认开启（2026-08-29）。
-- 按版本号幂等；正式库执行前须先完成整库备份。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.26.5',
  '课程设计器默认开放题目',
  CONCAT('课程设计器中的“学生开放”理论题、操作题开关默认开启，教师仍可在保存前主动关闭。\n',
         '新默认值只作用于新指派班级；已有班级当前开放状态、成绩查询和课程推进规则保持不变。'),
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_platform_update WHERE version_no = '1.26.5'
);

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.26.5';
