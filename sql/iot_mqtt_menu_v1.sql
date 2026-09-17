-- 物联网教师数据页菜单（可重复执行；权限仍由后端 @PreAuthorize 决定）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
  visible, status, perms, icon, create_by, create_time)
SELECT '物联网实验', 0, 6, 'iot', 'business/iot/index', 1, 0, 'C', '0', '0',
       'business:iot:query', 'monitor', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'business:iot:query');

SET @iot_menu_id := (SELECT menu_id FROM sys_menu WHERE perms = 'business:iot:query' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
  visible, status, perms, icon, create_by, create_time)
SELECT '物联网实验查询', @iot_menu_id, 1, '#', '', 1, 0, 'F', '1', '0',
       'business:iot:query', '#', 'system', NOW()
WHERE @iot_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'business:iot:query' AND menu_type = 'F');

-- 菜单写入不会自动授予角色；教师需要入口和查询权限，学生不授予该模块权限。
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.perms = 'business:iot:query'
WHERE r.role_key IN ('admin', 'teacher', 'researcher')
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm
    WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
  );
