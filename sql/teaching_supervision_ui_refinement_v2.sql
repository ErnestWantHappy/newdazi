-- 课程监管与免抽测交互收口 v2
-- 仅调整菜单结构，不修改申请、课程、成绩或期限业务数据。

-- 教师申请入口使用可继承侧栏颜色的项目内置图标，避免未选中时仍保持高亮色。
UPDATE sys_menu
SET icon = 'documentation',
    update_by = 'system',
    update_time = NOW()
WHERE perms = 'business:exemption:apply';

INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '免抽测申请审核', 0, 2, 'exemption-review', 'business/exemptionReview/index', '', 'ExemptionReview',
       1, 0, 'C', '0', '0', 'business:exemption:review', 'document', 'system', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE perms = 'business:exemption:review'
);

UPDATE sys_menu
SET menu_name = '免抽测申请审核',
    parent_id = 0,
    order_num = 2,
    path = 'exemption-review',
    component = 'business/exemptionReview/index',
    route_name = 'ExemptionReview',
    menu_type = 'C',
    visible = '0',
    status = '0',
    icon = 'document',
    update_by = 'system',
    update_time = NOW()
WHERE perms = 'business:exemption:review';

UPDATE sys_menu standard_menu
INNER JOIN sys_menu review_menu ON review_menu.perms = 'business:exemption:review'
SET standard_menu.parent_id = review_menu.menu_id,
    standard_menu.order_num = 1,
    standard_menu.update_by = 'system',
    standard_menu.update_time = NOW()
WHERE standard_menu.perms = 'business:exemption:standard';

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT role.role_id, menu.menu_id
FROM sys_role role
INNER JOIN sys_menu menu ON menu.perms IN ('business:exemption:review', 'business:exemption:standard')
WHERE role.role_key = 'researcher';

-- 执行后复核：应返回两行，审核为根级C菜单，标准为其F子权限。
SELECT menu.menu_id, menu.menu_name, menu.parent_id, menu.path, menu.component, menu.menu_type, menu.perms
FROM sys_menu menu
WHERE menu.perms IN ('business:exemption:review', 'business:exemption:standard')
ORDER BY menu.menu_type, menu.menu_id;
