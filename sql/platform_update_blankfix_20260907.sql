-- 仅补齐发布登记，不修改教师、学生、课程或成绩。发布前整库备份见本轮 evidence.json。
SET NAMES utf8mb4;
START TRANSACTION;
INSERT INTO biz_platform_update(version_no,title,content,published_at,status,create_by,create_time)
SELECT '1.30.9','在线人数与登录恢复',
'近5分钟活跃账号去重，支持部门筛选与分页；登录防重复提交，临时网关错误保留会话。课堂表现原因允许留空，学生登录默认进入首页。9月7日16时已部署，本次补齐登记。',
'2026-09-07 16:01:00','DRAFT','codex',NOW()
WHERE NOT EXISTS(SELECT 1 FROM biz_platform_update WHERE version_no='1.30.9');
INSERT INTO biz_platform_update(version_no,title,content,published_at,status,create_by,create_time)
SELECT '1.30.10','白卷批改与评分展示修复',
'统一白卷批改统计口径；有答题记录的白卷可打开批改并显示白卷标记；恢复导出请假标记；流程图评分建议使用流程图版本标签；AI分项非整数评分严格校验。已完成制品校验、备份、正式切换及健康检查。',
'2026-09-07 19:35:43','DRAFT','codex',NOW()
WHERE NOT EXISTS(SELECT 1 FROM biz_platform_update WHERE version_no='1.30.10');
COMMIT;
SELECT update_id,version_no,status FROM biz_platform_update WHERE version_no IN ('1.30.9','1.30.10') ORDER BY update_id;
