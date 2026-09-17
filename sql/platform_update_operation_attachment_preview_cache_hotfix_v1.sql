-- 操作题附件预览状态缓存热修发布记录（2026-09-01）。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.27.7',
  '修复操作题附件预览状态误报',
  CONCAT('修复操作题页图缓存复用时跳过当前附件 PDF 预览及旧答题状态回写的问题；',
         '已补齐受影响作品的预览状态，不影响原文件、答案和成绩。'),
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_platform_update WHERE version_no = '1.27.7'
);

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.27.7';
