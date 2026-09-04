-- 小学实验板 N17 Python 代码模板修正（2026-08-31）。
-- 仅记录前端模板发布，正式库执行前须先完成整库备份。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.27.4',
  '小学实验板 N17 Python 代码模板修正',
  CONCAT('一键复制代码改为 N17 示例格式，增加 utf_8 文件头、npython 导入说明和 oled.print 屏幕显示示例。',
         ' MQTT 账号、课堂口令、Topic 和后端服务保持不变。'),
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_platform_update WHERE version_no = '1.27.4'
);

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.27.4';
