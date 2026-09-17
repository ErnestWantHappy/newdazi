-- 修复：教师前端不展示管理员课程列表，但教师角色残留了已停用菜单的授权关系。
-- 不启用该管理菜单，只删除 teacher 角色的无效关联，保持实际 403 权限边界不变。
-- 幂等：可重复执行。
-- 回滚：INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
--       SELECT r.role_id, m.menu_id FROM sys_role r JOIN sys_menu m ON m.perms='business:lesson:list'
--       WHERE r.role_key='teacher' AND m.status='1';

DELETE role_menu
FROM sys_role_menu role_menu
INNER JOIN sys_role role ON role.role_id = role_menu.role_id
INNER JOIN sys_menu menu ON menu.menu_id = role_menu.menu_id
WHERE role.role_key = 'teacher'
  AND menu.perms = 'business:lesson:list'
  AND menu.status = '1';
