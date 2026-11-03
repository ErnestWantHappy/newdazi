-- 仅登记已验收的前端产品更新，不修改学生作答、成绩或数据库结构。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.48', '登录输入流畅性与资源加载优化',
  '缩小登录首屏资源，导学单组件按需加载；输入账号、密码时暂停轮播并延后展厅更新，减少文字迟显；保留选校、验证码及原角色入口。',
  NOW(), 'PUBLISHED', 'AI 发布记录', NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.48');

UPDATE biz_platform_update
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.48' AND title = '登录输入流畅性与资源加载优化';

SELECT update_id, version_no, title, status
FROM biz_platform_update WHERE version_no = '1.30.48';
