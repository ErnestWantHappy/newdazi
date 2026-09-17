-- 教师工具分组文案与帮助中心归属（幂等，仅 130）。
-- 现场已核对：父 26014=教师工具(目录)，子 26031=教师工具(页，path=index)；
-- 26011 免抽测、26024 学生实验工具、26016 平台更新；MAX(menu_id)=26033。
-- 本机菜单 ID 已分叉，禁止在本机库执行。

-- 前检：目标行必须仍是分组后的工具首页，且 26034 未被占用。
SELECT menu_id, parent_id, menu_name, path, component, menu_type
FROM sys_menu WHERE menu_id IN (26014, 26031, 26034);

UPDATE sys_menu
SET menu_name = '教师教学优质资源'
WHERE menu_id = 26031
  AND parent_id = 26014
  AND path = 'index'
  AND component = 'business/teacherTools/index'
  AND menu_name = '教师工具';

INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, remark
) VALUES (
  26034, '帮助中心', 26014, 5, 'help', 'help/index', '', 'TeacherHelpCenter',
  0, 0, 'C', '0', '0', '', 'question',
  'admin', NOW(), '从静态顶级 /help-center 并入教师工具分组，保留原深链接'
)
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name),
  parent_id = VALUES(parent_id),
  order_num = VALUES(order_num),
  path = VALUES(path),
  component = VALUES(component),
  menu_type = 'C',
  visible = '0',
  status = '0';

-- 管理员/教师/教研员与父菜单同一批角色，保持帮助中心原可访问范围。
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT role_id, 26034 FROM sys_role_menu WHERE menu_id = 26014;

-- 后检
SELECT menu_id, parent_id, order_num, menu_name, path, component
FROM sys_menu WHERE parent_id = 26014 AND menu_type = 'C' ORDER BY order_num;
SELECT COUNT(*) AS help_roles FROM sys_role_menu WHERE menu_id = 26034;
