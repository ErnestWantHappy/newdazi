-- 学生答案归属和理论题重复提交保护发布登记（2026-09-05）。
-- 仅写平台更新记录，不变更课程、题目、答案、成绩或作品数据。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.29.3',
  '学生答案归属与理论题重复提交修复',
  '修复教师批改、学情和画像查询将答案 student_id 错连为 user_id 的问题；理论题提交改为终态幂等保护；补充跨模块路由加载遮罩。',
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_platform_update WHERE version_no = '1.29.3'
);

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.29.3';
