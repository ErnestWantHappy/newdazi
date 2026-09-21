-- 恢复被教学监管迁移替换的打字统计权限，不改变监管入口和其他角色授权。
-- 执行前备份 sys_menu/sys_role_menu；执行后原账号需重新登录刷新权限。
START TRANSACTION;

INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '打字统计', parent.menu_id, 4, '#', '', '', '',
       1, 0, 'F', '0', '0', 'business:schoolScore:stats', '#', 'system', NOW()
FROM sys_menu parent
WHERE parent.menu_id = 2046
  AND parent.component = 'business/schoolScore/index'
  AND parent.perms = 'business:teachingSupervision:view'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'business:schoolScore:stats');

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT role.role_id, menu.menu_id
FROM sys_role role
JOIN sys_role_menu parent_grant ON parent_grant.role_id = role.role_id AND parent_grant.menu_id = 2046
JOIN sys_menu menu ON menu.perms = 'business:schoolScore:stats' AND menu.status = '0'
WHERE role.role_key = 'researcher' AND role.status = '0' AND role.del_flag = '0'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu existing
                  WHERE existing.role_id = role.role_id AND existing.menu_id = menu.menu_id);

COMMIT;

-- 后检应只有一个权限点，且仅向原有监管入口的教研员补授权。
SELECT menu.menu_id, menu.perms, role.role_key
FROM sys_menu menu
LEFT JOIN sys_role_menu grant_row ON grant_row.menu_id = menu.menu_id
LEFT JOIN sys_role role ON role.role_id = grant_row.role_id
WHERE menu.perms = 'business:schoolScore:stats';
