-- 彻底清除按钮权限 v1 回滚（仅本地/130）。
DELETE FROM sys_role_menu WHERE menu_id IN (26032, 26033);
DELETE FROM sys_menu WHERE menu_id IN (26032, 26033);
