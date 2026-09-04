-- 电子导学单菜单与角色权限增量。
-- 仅维护 sys_menu / sys_role_menu，可重复执行；不创建旧版导学单业务表。

SET @guide_menu = (
  SELECT menu_id
  FROM sys_menu
  WHERE component = 'business/guideSheet/index'
     OR (path IN ('guide-sheet-list', 'business/guide-sheet-list', '/business/guide-sheet-list')
         AND menu_type IN ('C', 'M'))
  ORDER BY CASE WHEN component = 'business/guideSheet/index' THEN 0 ELSE 1 END, menu_id
  LIMIT 1
);

INSERT INTO sys_menu
  (menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
SELECT '导学单管理',0,7,'business/guide-sheet-list','business/guideSheet/index','GuideSheet',1,0,'C','0','0','business:guideSheet:list','guide-sheet','admin',NOW(),'电子导学单管理'
WHERE @guide_menu IS NULL;

SET @guide_menu = (
  SELECT menu_id
  FROM sys_menu
  WHERE component = 'business/guideSheet/index'
     OR (path IN ('guide-sheet-list', 'business/guide-sheet-list', '/business/guide-sheet-list')
         AND menu_type IN ('C', 'M'))
  ORDER BY CASE WHEN component = 'business/guideSheet/index' THEN 0 ELSE 1 END, menu_id
  LIMIT 1
);

-- 兼容旧分支曾使用的目录菜单，统一为当前 Vue3 可直接访问的页面路由。
UPDATE sys_menu
SET menu_name = '导学单管理',
    parent_id = 0,
    order_num = 7,
    path = 'business/guide-sheet-list',
    component = 'business/guideSheet/index',
    route_name = 'GuideSheet',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'business:guideSheet:list',
    icon = 'guide-sheet'
WHERE menu_id = @guide_menu;

INSERT INTO sys_menu
  (menu_name,parent_id,order_num,path,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT p.menu_name,@guide_menu,p.order_num,'',1,0,'F','0','0',p.perms,'#','admin',NOW()
FROM (
  SELECT '导学单新增' menu_name,1 order_num,'business:guideSheet:add' perms UNION ALL
  SELECT '导学单修改',2,'business:guideSheet:edit' UNION ALL
  SELECT '导学单删除',3,'business:guideSheet:remove' UNION ALL
  SELECT '导学单设计',4,'business:guideSheet:design' UNION ALL
  SELECT '导学单看板',5,'business:guideSheet:dashboard' UNION ALL
  SELECT '导学单导出',6,'business:guideSheet:export'
) p
WHERE @guide_menu IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.perms = p.perms);

INSERT INTO sys_role_menu (role_id,menu_id)
SELECT r.role_id,m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.perms LIKE 'business:guideSheet:%'
WHERE r.role_key IN ('admin','teacher','researcher')
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
  );

INSERT INTO biz_guide_sheet_migration
  (migration_key, migration_status, completed_time)
VALUES
  ('guide_sheet_menu_permissions', 'DONE', NOW())
ON DUPLICATE KEY UPDATE
  migration_status = VALUES(migration_status),
  completed_time = VALUES(completed_time);

SELECT
  (SELECT COUNT(*) FROM sys_menu WHERE perms LIKE 'business:guideSheet:%') AS guide_menu_count,
  (SELECT COUNT(*)
   FROM sys_role_menu rm
   JOIN sys_role r ON r.role_id = rm.role_id
   JOIN sys_menu m ON m.menu_id = rm.menu_id
   WHERE r.role_key = 'teacher' AND m.perms LIKE 'business:guideSheet:%') AS teacher_permission_count;
