-- 恢复画程流程图前端资源（仅静态前端，不改后端和业务数据）。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.28.6',
  '恢复画程流程图题库功能',
  '修复 1.28.5 前端热修覆盖流程图资源的问题，恢复题库流程图操作题新增、编辑和学生作答入口。',
  NOW(), 'PUBLISHED', 'AI 发布记录', NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.28.6');

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.28.6';
