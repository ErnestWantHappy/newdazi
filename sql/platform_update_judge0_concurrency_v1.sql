-- Python 判题 400 人并发能力更新记录（2026-08-23）
-- 目标库：正式库；执行前按 AGENTS.md 完成整库备份。
-- 仅新增面向用户的平台更新记录，不修改业务表结构；按版本号幂等。

INSERT INTO `biz_platform_update`
  (`version_no`, `title`, `content`, `published_at`, `status`, `create_by`, `create_time`)
SELECT
  '1.26.0',
  'Python 判题并发能力提升',
  CONCAT('Python 在线编程判题服务完成并发扩容。\n',
         '支持 400 名学生同时提交代码，提交会可靠进入判题队列并依次返回结果。\n',
         '优化整班集中提交时的排队能力与服务稳定性。'),
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `biz_platform_update` WHERE `version_no` = '1.26.0'
);
