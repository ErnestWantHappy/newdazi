-- 彻底清除按钮权限 v1（幂等，仅 130 演练库随菜单 SQL 执行；禁止在 123 生产执行）。
-- 新权限默认只给管理员角色；教师需单独授权（本轮不在生产授权）。
-- 130 测试库曾临时给教师角色授权以验证接口，正式库恢复时不要带过去。

-- 课程彻底清除按钮（挂在课程删除同组下）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, perms, menu_type,
  visible, status, create_by, create_time)
VALUES (26032, '课程彻底清除', 2006, 6, '#', 'business:lesson:purge', 'F',
  '0', '0', 'system', NOW())
ON DUPLICATE KEY UPDATE perms=VALUES(perms), menu_type='F', visible='0', status='0';

-- 学生彻底清除按钮（挂在学生管理下）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, perms, menu_type,
  visible, status, create_by, create_time)
VALUES (26033, '学生彻底清除', 2018, 7, '#', 'business:student:purge', 'F',
  '0', '0', 'system', NOW())
ON DUPLICATE KEY UPDATE perms=VALUES(perms), menu_type='F', visible='0', status='0';

-- 仅管理员角色
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 26032), (1, 26033);

-- 复核
SELECT 'purge_buttons' AS check_item, COUNT(*) AS cnt FROM sys_menu WHERE menu_id IN (26032, 26033);
