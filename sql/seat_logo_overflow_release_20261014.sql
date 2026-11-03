-- 仅登记已验收的后端与助手更新，不修改成绩、作答或数据库结构。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.49', '机位助手平台图标与操作题页图修复',
  '机位助手更新为0.2.2，程序、桌面快捷方式、托盘和状态窗口统一使用平台Logo；操作题预览将画在页框外的内容纳入页图，保留学生原件与评分。助手需退出旧版后重新下载运行，已有登录优化保持。',
  NOW(), 'PUBLISHED', 'AI 发布记录', NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.49');

UPDATE biz_platform_update
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.49' AND title = '机位助手平台图标与操作题页图修复';

SELECT update_id, version_no, status
FROM biz_platform_update WHERE version_no = '1.30.49';
