-- 回滚教师工具子菜单改名与帮助中心并入（仅 130）。
DELETE FROM sys_role_menu WHERE menu_id = 26034;
DELETE FROM sys_menu WHERE menu_id = 26034;
UPDATE sys_menu
SET menu_name = '教师工具'
WHERE menu_id = 26031
  AND parent_id = 26014
  AND path = 'index'
  AND component = 'business/teacherTools/index';
