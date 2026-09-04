-- 成绩列表自然数值排序热修发布记录（2026-09-01）。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.28.1',
  '修复成绩列表学号排序',
  '修复 Element Plus 表格排序回调参数使用错误，成绩查询、课堂表现、排行榜和理论详情的学号/班级排序恢复为自然数值顺序。',
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_platform_update WHERE version_no = '1.28.1'
);

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.28.1';
