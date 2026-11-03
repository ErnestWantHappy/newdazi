-- 教师批改页操作题长图滚动与五星评分稳定性修复，仅登记产品版本，不修改业务数据。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.30.47',
  '教师批改操作题长图滚动与五星评分修复',
  '修复教师批改页操作题长图底部无法滚动的问题；稳定五星评分布局，避免悬浮频闪。',
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.47'
);

UPDATE biz_platform_update
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.47';

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.30.47';
