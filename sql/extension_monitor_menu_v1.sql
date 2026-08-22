-- 扩展机一体化健康看板菜单（2026-08-21）
-- 可重复执行；只改 sys_menu / sys_role_menu，不改业务数据。
-- 产品口径：管理员与教研员在「系统监控」下新增“扩展服务监控”，
-- 与系统诊断中心(25010)并列，一屏掌握 129 扩展机 Judge0/CryptPad/EMQX/MQTT 状态。

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 25011, '扩展服务监控', 2, 2, 'extension', 'monitor/extension/index', '', '',
       1, 0, 'C', '0', '0', 'monitor:extension:list', 'server', 'admin', NOW(), '129扩展机Judge0/CryptPad/EMQX健康大盘'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 25011 OR component = 'monitor/extension/index');

UPDATE sys_menu
SET menu_name = '扩展服务监控',
    parent_id = 2,
    order_num = 2,
    path = 'extension',
    component = 'monitor/extension/index',
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'monitor:extension:list',
    icon = 'server'
WHERE menu_id = 25011
   OR component = 'monitor/extension/index';

-- 管理员 + 教研员授权
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON (m.menu_id = 25011 OR m.component = 'monitor/extension/index')
WHERE r.role_key IN ('admin', 'researcher')
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
  );
