-- 教师菜单二级分组 v1（幂等，仅 130 演练库执行：130 库由 123 备份恢复，菜单 ID 与线上一致）。
-- 禁止在 123 生产执行；禁止在本机 dev 库执行（本机菜单 ID 已分叉，26026/26027 等另有所指）。
-- 目标顺序：教师首页 → 题库管理 → 成绩查询(组) → 学生管理 → 班级管理 → 教师工具(组) → 教研活动 → 导学单管理 → Python刷题。
-- 成绩查询组：成绩查询页 + 学生个人成绩画像；教师工具组：工具首页 + 免抽测申请 + 学生实验工具 + 平台更新。
-- 帮助中心为静态路由 /help-center（admin/teacher/researcher），不在菜单表，不动。
-- Python刷题保持顶级（需求未指定归属，不得静默并入他组）。
-- 旧深链接 /score、/teacher-tools 改为分组父项（目录），页体下沉一级：
--   /score/query、/score/student-profile、/teacher-tools/index、/teacher-tools/teacher-exemption 等。
-- 行为变化：侧边栏需多点一次展开；权限字符串全部保留，学生/教研员角色未动。

-- 1) 成绩查询 2042 改为目录
UPDATE sys_menu SET menu_type='M', component=NULL, order_num=2, remark='F04 分组：目录，页体见子菜单 26030'
WHERE menu_id=2042;

-- 2) 新增 成绩查询页 子菜单（沿用原 2042 的组件与权限）
-- 新 ID 取 26030/26031：123 当前最大 26025；本机库 26026/26027 已被其他按钮占用，避开。
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, icon,
  is_frame, is_cache, menu_type, visible, status, create_by, create_time)
VALUES (26030, '成绩查询', 2042, 1, 'query', 'business/score/index', 'business:score:list', 'job',
  1, 0, 'C', '0', '0', 'system', NOW())
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name), parent_id=VALUES(parent_id), order_num=VALUES(order_num),
  path=VALUES(path), component=VALUES(component), perms=VALUES(perms), menu_type='C', visible='0', status='0';

-- 3) 画像并入成绩查询组
UPDATE sys_menu SET parent_id=2042, order_num=2 WHERE menu_id=2044;

UPDATE sys_menu SET menu_type='M', component=NULL, order_num=5, remark='F04 分组：目录，页体见子菜单 26031'
WHERE menu_id=26014;

-- 5) 新增 工具首页 子菜单（沿用原 26014 的组件与权限）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, icon,
  is_frame, is_cache, menu_type, visible, status, route_name, create_by, create_time)
VALUES (26031, '教师工具', 26014, 1, 'index', 'business/teacherTools/index', 'business:teacherTool:list', 'tool',
  1, 0, 'C', '0', '0', 'TeacherToolIndex', 'system', NOW())
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name), parent_id=VALUES(parent_id), order_num=VALUES(order_num),
  path=VALUES(path), component=VALUES(component), perms=VALUES(perms), menu_type='C', visible='0', status='0';

-- 6) 免抽测/实验工具/平台更新并入教师工具组（按钮级子项随父移动）
UPDATE sys_menu SET parent_id=26014, order_num=2 WHERE menu_id=26011;
UPDATE sys_menu SET parent_id=26014, order_num=3 WHERE menu_id=26024;
UPDATE sys_menu SET parent_id=26014, order_num=4 WHERE menu_id=26016;

-- 7) 顶级排序
UPDATE sys_menu SET order_num=0 WHERE menu_id=2037;
UPDATE sys_menu SET order_num=1 WHERE menu_id=2031;
UPDATE sys_menu SET order_num=3 WHERE menu_id=2018;
UPDATE sys_menu SET order_num=4 WHERE menu_id=2038;
UPDATE sys_menu SET order_num=6 WHERE menu_id=26000;
UPDATE sys_menu SET order_num=7 WHERE menu_id=2048;
UPDATE sys_menu SET order_num=8 WHERE menu_id=26020;

-- 8) 原页面下沉后继承原有访问角色，避免教研员已有工具权限随目录化丢失。
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT role_id, 26030 FROM sys_role_menu WHERE menu_id=2042;
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT role_id, 26031 FROM sys_role_menu WHERE menu_id=26014;

-- 复核：教师顶级应为 9 项；分组子项应为 2+4；权限字符串无 NULL 新增（画像本就为 NULL，保持）。
SELECT 'teacher_top' AS check_item, COUNT(*) AS cnt FROM sys_menu m
  INNER JOIN sys_role_menu rm ON m.menu_id=rm.menu_id
  WHERE rm.role_id=100 AND m.parent_id=0 AND m.visible='0' AND m.menu_type IN ('M','C');
SELECT 'score_kids' AS check_item, COUNT(*) AS cnt FROM sys_menu WHERE parent_id=2042 AND menu_type='C';
SELECT 'tool_kids' AS check_item, COUNT(*) AS cnt FROM sys_menu WHERE parent_id=26014 AND menu_type='C';
