-- 教师首页共享课程权限提示与学生端导航吸顶（2026-08-27）。
-- 按版本号幂等；正式库执行前须先完成整库备份。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.26.4',
  '共享课程权限提示与学生端导航优化',
  CONCAT('教师首页会明确标识共享课程及其创建教师，共享课程不再显示无权限的设计和删除入口。\n',
         '课程删除会先校验管理权限，再检查学生作答或导学单历史，避免出现误导性报错。\n',
         '学生端顶部导航在页面滚动时持续置顶，原有课程、实验工具和个人功能保持可用。'),
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_platform_update WHERE version_no = '1.26.4'
);

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.26.4';
