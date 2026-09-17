-- 操作题旧题答案隔离与 AI 用量/费用展示（2026-08-26）。
-- 按版本号幂等；正式库执行前须先完成整库备份。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.26.3',
  '操作题批改状态与 AI 用量展示优化',
  CONCAT('修复课程更换操作题后，旧题答案可能导致班级错误显示“已批改”的问题；旧答案会保留审计归档，不再污染当前课程统计。\n',
         'AI 批改设置新增模型参考价和单份费用估算，处理详情新增累计 token 与预计费用。\n',
         '余额不足、Key 无效和请求限流会显示更明确的处理提示；费用为理论估算，实际以阿里云账单为准。'),
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_platform_update WHERE version_no = '1.26.3'
);

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.26.3';
