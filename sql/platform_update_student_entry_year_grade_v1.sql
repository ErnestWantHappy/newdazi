-- 学生管理入学年份年级备注发布记录（2026-09-02）。
-- 仅登记前端显示优化，不修改学生数据或表结构。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.28.3',
  '学生入学年份显示当前年级',
  '学生管理按当前校区学部自动在入学年份后显示当前年级，小学显示x年级，初中显示初x，高中显示高x；保存和筛选值仍为原入学年份。',
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_platform_update WHERE version_no = '1.28.3'
);

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.28.3';
