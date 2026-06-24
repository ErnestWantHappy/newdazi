-- 电子导学单——菜单权限注册脚本
-- 执行前请确认 parent_id 和 role_id 匹配实际环境

-- 需要在「教学管理」下新增「导学单管理」一级菜单
-- 假定教学管理的 menu_id 可通过查询获取
-- SELECT menu_id FROM sys_menu WHERE menu_name = '教学管理' AND parent_id = 0;

SET @parent_menu = (SELECT menu_id FROM sys_menu WHERE menu_name = '教学管理' AND parent_id = 0 LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('导学单管理', @parent_menu, 5, '/business/guide-sheet-list', NULL, NULL, NULL, 1, 0, 'M', '0', '0', NULL, 'guide-sheet', 'admin', NOW(), NULL, NULL, '');

SET @guide_menu = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('导学单查询', @guide_menu, 1, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'business:guideSheet:list', '#', 'admin', NOW(), NULL, NULL, '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('导学单新增', @guide_menu, 2, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'business:guideSheet:add', '#', 'admin', NOW(), NULL, NULL, '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('导学单修改', @guide_menu, 3, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'business:guideSheet:edit', '#', 'admin', NOW(), NULL, NULL, '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('导学单删除', @guide_menu, 4, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'business:guideSheet:remove', '#', 'admin', NOW(), NULL, NULL, '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('导学单设计', @guide_menu, 5, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'business:guideSheet:design', '#', 'admin', NOW(), NULL, NULL, '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('导学单看板', @guide_menu, 6, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'business:guideSheet:dashboard', '#', 'admin', NOW(), NULL, NULL, '');

-- 分配角色权限：将导学单管理菜单分配给教师角色(100)和管理员角色(1)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE m.menu_id IN (
    @guide_menu,
    @guide_menu + 1, @guide_menu + 2, @guide_menu + 3,
    @guide_menu + 4, @guide_menu + 5, @guide_menu + 6
)
AND r.role_id IN (1, 100)
AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
);
