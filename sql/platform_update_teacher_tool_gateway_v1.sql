-- 教师工具统一入口与服务稳定性修复（2026-08-31）。
-- 按版本号幂等；正式库执行前须先完成整库备份。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.27.1',
  '教师工具统一入口与服务稳定性提升',
  CONCAT('服务器内置的邮件、小学实验、网络仿真、物联网数据演示和图像识别工具统一改为 80 端口路径入口，教师无需再记忆或依赖独立端口。\n',
         '物联网数据演示已纳入自动启动与异常重启管理。\n',
         '服务器统一使用 D 盘 Nginx 提供 80、3010、3012 服务，避免两套 Nginx 同时监听造成配置不一致。'),
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_platform_update WHERE version_no = '1.27.1'
);

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.27.1';
