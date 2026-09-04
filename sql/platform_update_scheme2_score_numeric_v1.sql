-- 方案二与成绩查询数值排序发布记录（2026-09-01）。
-- 仅在不存在同版本记录时写入，避免重复登记。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.28.0',
  '方案二安全处理与成绩查询数值排序',
  CONCAT('落实课程删除、归档与学生停用/纠错分离；增加课程归档状态迁移；',
         '成绩查询、课堂表现、排行榜、导学单及班级列表按学号等字段自然数值排序；',
         '管理员班级列表仅统计正常学生。'),
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_platform_update WHERE version_no = '1.28.0'
);

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.28.0';
