-- 教师菜单二级分组 v1 回滚（仅本地/130）。
DELETE FROM sys_role_menu WHERE role_id=100 AND menu_id IN (26030, 26031);
DELETE FROM sys_menu WHERE menu_id IN (26030, 26031);
UPDATE sys_menu SET parent_id=0, order_num=6 WHERE menu_id=2044;
UPDATE sys_menu SET menu_type='C', component='business/score/index', order_num=5 WHERE menu_id=2042;
UPDATE sys_menu SET parent_id=0, order_num=1 WHERE menu_id IN (26011, 26024);
UPDATE sys_menu SET parent_id=0, order_num=99 WHERE menu_id=26016;
UPDATE sys_menu SET menu_type='C', component='business/teacherTools/index', order_num=0 WHERE menu_id=26014;
UPDATE sys_menu SET order_num=1 WHERE menu_id IN (2018, 2031);
UPDATE sys_menu SET order_num=5 WHERE menu_id=2038;
UPDATE sys_menu SET order_num=8 WHERE menu_id IN (26000, 26020);
UPDATE sys_menu SET order_num=7 WHERE menu_id=2048;
