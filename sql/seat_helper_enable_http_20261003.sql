-- 用户已授权正式启用：覆盖当前有效小学、初中和高中，助手仍为可选功能。
-- 仅写助手配置；不修改学生、成绩、指派或长期机位。新学校仍由管理员配置。
INSERT INTO biz_seat_helper_school (dept_id, enabled, platform_url)
SELECT dept_id, 1, 'http://10.52.1.130'
FROM sys_dept
WHERE status = '0' AND del_flag = '0' AND school_type IN ('1', '2', '3')
ON DUPLICATE KEY UPDATE enabled = 1, updated_at = NOW();
