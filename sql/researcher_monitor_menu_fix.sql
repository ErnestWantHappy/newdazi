-- 教研员系统监控菜单收口 + 系统诊断中心补齐（2026-07-22）
-- 可重复执行；只改 sys_menu / sys_role_menu，不改业务数据。
-- 产品口径：教研员可见 系统诊断中心 / 缓存健康 / 在线用户；隐藏 数据监控、原生服务监控、定时任务、缓存列表。

-- 1. 系统诊断中心（若不存在则创建）
INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 25010, '系统诊断中心', 2, 1, 'diagnosis', 'monitor/diagnosis/index', '', '',
       1, 0, 'C', '0', '0', 'monitor:diagnosis:list', 'monitor', 'admin', NOW(), '面向故障定位的诊断看板'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 25010 OR component = 'monitor/diagnosis/index');

UPDATE sys_menu
SET menu_name = '系统诊断中心',
    parent_id = 2,
    path = 'diagnosis',
    component = 'monitor/diagnosis/index',
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'monitor:diagnosis:list',
    icon = 'monitor'
WHERE menu_id = 25010
   OR component = 'monitor/diagnosis/index';

-- 2. 缓存监控改名缓存健康（若仍是旧名）
UPDATE sys_menu SET menu_name = '缓存健康' WHERE menu_id = 113 AND menu_name IN ('缓存监控', '缓存健康');

-- 3. 系统监控内排序：诊断优先
UPDATE sys_menu SET order_num = 1 WHERE menu_id = 25010 OR component = 'monitor/diagnosis/index';
UPDATE sys_menu SET order_num = 2 WHERE menu_id = 109;
UPDATE sys_menu SET order_num = 3 WHERE menu_id = 113;
UPDATE sys_menu SET order_num = 4 WHERE menu_id = 111;
UPDATE sys_menu SET order_num = 5 WHERE menu_id = 112;
UPDATE sys_menu SET order_num = 6 WHERE menu_id = 110;
UPDATE sys_menu SET order_num = 7 WHERE menu_id = 114;

-- 4. 管理员 + 教研员：平台概览(若存在) + 诊断中心
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON (
      m.menu_id IN (25000, 25010)
      OR m.component IN ('platform/overview/index', 'monitor/diagnosis/index')
  )
WHERE r.role_key IN ('admin', 'researcher')
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
  );

-- 5. 教研员：确保系统监控目录、在线用户、缓存健康
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (2, 109, 113)
WHERE r.role_key = 'researcher'
  AND m.status = '0'
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
  );

-- 6. 教研员：撤掉不适合的原始监控/后台菜单
DELETE rm
FROM sys_role_menu rm
JOIN sys_role r ON rm.role_id = r.role_id
JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE r.role_key = 'researcher'
  AND (
      m.menu_id IN (104, 106, 110, 111, 112, 114, 2042)
      OR m.parent_id IN (104, 106, 110, 111, 112, 114, 2042)
  );
