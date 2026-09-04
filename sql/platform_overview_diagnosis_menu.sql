-- 平台概览与系统诊断中心菜单调整
-- 说明：只调整 sys_menu/sys_role_menu，可重复执行；不会修改业务数据。

-- 1. 新增“平台概览”一级菜单：首页入口不再使用同名二级菜单
INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 25000, '平台概览', 0, 0, 'platform/overview', 'platform/overview/index', '', '',
       1, 0, 'C', '0', '0', 'business:platformOverview:list', 'dashboard', 'admin', NOW(), '平台数据概览'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 25000 OR component = 'platform/overview/index');

UPDATE sys_menu
SET menu_name = '平台概览',
    parent_id = 0,
    order_num = 0,
    path = 'platform/overview',
    component = 'platform/overview/index',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'business:platformOverview:list',
    icon = 'dashboard',
    remark = '平台数据概览'
WHERE menu_id = 25000;

DELETE FROM sys_role_menu WHERE menu_id = 25001;
DELETE FROM sys_menu WHERE menu_id = 25001;

-- 2. 新增“系统诊断中心”，保留 Druid/服务/缓存等高级监控页面
INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 25010, '系统诊断中心', 2, 1, 'diagnosis', 'monitor/diagnosis/index', '', '',
       1, 0, 'C', '0', '0', 'monitor:diagnosis:list', 'monitor', 'admin', NOW(), '面向故障定位的诊断看板'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 25010 OR component = 'monitor/diagnosis/index');

-- 3. 菜单命名更贴近业务含义
UPDATE sys_menu SET menu_name = '缓存健康' WHERE menu_id = 113 AND menu_name = '缓存监控';

-- 4. 系统监控排序：诊断中心优先，原始工具后置
UPDATE sys_menu SET order_num = 1 WHERE menu_id = 25010;
UPDATE sys_menu SET order_num = 2 WHERE menu_id = 109;
UPDATE sys_menu SET order_num = 3 WHERE menu_id = 111;
UPDATE sys_menu SET order_num = 4 WHERE menu_id = 112;
UPDATE sys_menu SET order_num = 5 WHERE menu_id = 113;
UPDATE sys_menu SET order_num = 6 WHERE menu_id = 110;
UPDATE sys_menu SET order_num = 7 WHERE menu_id = 114;

-- 5. 授权给管理员和教研员；管理员即使没有 role_menu 也通常拥有全部权限，此处保持显式授权。
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (25000, 25010)
WHERE r.role_key IN ('admin', 'researcher')
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
  );

-- 6. 教研员端隐藏不适合日常使用的原始后台菜单。
-- 口径（2026-07-22）：诊断中心 + 缓存健康 + 在线用户；隐藏数据监控/原生服务监控/定时任务/缓存列表等。
DELETE rm
FROM sys_role_menu rm
JOIN sys_role r ON rm.role_id = r.role_id
JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE r.role_key = 'researcher'
  AND (
      m.menu_id IN (104, 106, 110, 111, 112, 114, 2042)
      OR m.parent_id IN (104, 106, 110, 111, 112, 114, 2042)
  );

-- 7. 确保教研员仍有系统监控目录、在线用户、缓存健康、诊断中心（幂等）。
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (2, 109, 113, 25010)
WHERE r.role_key = 'researcher'
  AND m.status = '0'
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
  );
