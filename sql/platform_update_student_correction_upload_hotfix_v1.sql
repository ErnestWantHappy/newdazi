-- 学生批量纠错 Excel 上传热修发布记录（2026-09-02）。
-- 仅登记正式前端发布记录，不修改业务表结构；执行前须完成整库备份。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.28.2',
  '修复学生批量纠错上传',
  '修复学生管理批量纠错上传 Excel 时请求未按文件格式发送，导致系统提示繁忙的问题。',
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_platform_update WHERE version_no = '1.28.2'
);

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.28.2';
