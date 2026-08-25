-- =============================================================
-- 学生实验工具 + 题目开放开关 迁移 v1（2026-08-22）
-- 用途：
--   1) 建 biz_student_tool / biz_student_tool_scope（常驻工具+按年级/班级范围）；
--   2) 建 biz_lesson_tool（本节课工具，随课程走）；
--   3) biz_lesson_assignment 增加 theory_open / practical_open（题目开放开关，班级x当前课程）；
--   4) 菜单：学生实验工具（目录）+ 工具管理（教师）。
-- 目标库：本机开发库 / 正式库（以目标环境 application-druid 外置配置为准）。
-- 说明：全部幂等可重复执行；旧代码忽略新列，建议与新前后端版本一起发布。
-- 前置：执行前备份目标库。
-- =============================================================

-- 1) 常驻工具表
CREATE TABLE IF NOT EXISTS `biz_student_tool` (
  `tool_id` bigint NOT NULL AUTO_INCREMENT COMMENT '工具ID',
  `tool_name` varchar(100) NOT NULL COMMENT '工具名称（学生端显示）',
  `tool_url` varchar(500) NOT NULL COMMENT '工具网址',
  `tool_desc` varchar(255) DEFAULT NULL COMMENT '简要说明（可选）',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序（小在前）',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '启用 1=启用 0=停用',
  `dept_id` bigint DEFAULT NULL COMMENT '学校ID（数据隔离，空=平台级）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`tool_id`),
  KEY `idx_student_tool_dept` (`dept_id`, `enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生常驻工具';

-- 2) 常驻工具适用范围：一行=（工具, 入学年份级, 班级）；class_code 为空表示整个年级生效
CREATE TABLE IF NOT EXISTS `biz_student_tool_scope` (
  `scope_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `tool_id` bigint NOT NULL COMMENT '工具ID',
  `entry_year` varchar(20) NOT NULL COMMENT '入学年份/级',
  `class_code` varchar(20) DEFAULT NULL COMMENT '班级号，NULL/空=全年级生效',
  PRIMARY KEY (`scope_id`),
  KEY `idx_student_tool_scope_tool` (`tool_id`),
  KEY `idx_student_tool_scope_scope` (`entry_year`, `class_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生常驻工具适用范围';

-- 3) 本节课工具：随课程走
CREATE TABLE IF NOT EXISTS `biz_lesson_tool` (
  `tool_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `lesson_id` bigint NOT NULL COMMENT '课程ID',
  `tool_name` varchar(100) NOT NULL COMMENT '工具名称',
  `tool_url` varchar(500) NOT NULL COMMENT '工具网址',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序（小在前）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`tool_id`),
  KEY `idx_lesson_tool_lesson` (`lesson_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生本节课工具（随课程）';

-- 4) 题目开放开关列（幂等加列）
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_lesson_assignment'
      AND column_name = 'theory_open'
);
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE biz_lesson_assignment ADD COLUMN theory_open tinyint(1) NOT NULL DEFAULT 0 COMMENT ''理论测试题开放开关 1=开放（推进课程自动复位）'' AFTER auto_advance_ready_time',
    'SELECT ''biz_lesson_assignment.theory_open 已存在，跳过''');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_lesson_assignment'
      AND column_name = 'practical_open'
);
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE biz_lesson_assignment ADD COLUMN practical_open tinyint(1) NOT NULL DEFAULT 0 COMMENT ''操作题开放开关 1=开放（推进课程自动复位）'' AFTER theory_open',
    'SELECT ''biz_lesson_assignment.practical_open 已存在，跳过''');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5) 菜单：学生实验工具（一级目录，教师可见）+ 工具管理（功能权限）
INSERT INTO `sys_menu`
  (`menu_name`,`parent_id`,`order_num`,`path`,`component`,`query`,`route_name`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`)
SELECT '学生实验工具', 0, 1, 'student-tool', 'business/studentTool/index', '', 'StudentTool', 1, 0, 'C', '0', '0',
       'business:studentTool:list', 'link', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE perms='business:studentTool:list');

SET @student_tool_menu_id := (SELECT menu_id FROM sys_menu WHERE perms='business:studentTool:list' ORDER BY menu_id LIMIT 1);

-- 工具管理（教师可用）与管理（教师可用，隐藏按钮能力）
INSERT INTO `sys_menu`
  (`menu_name`,`parent_id`,`order_num`,`path`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`)
SELECT '学生实验工具管理', @student_tool_menu_id, 1, '#', 1, 0, 'F', '1', '0',
       'business:studentTool:manage', '#', 'system', NOW()
WHERE @student_tool_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE perms='business:studentTool:manage');

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.role_id, @student_tool_menu_id FROM `sys_role` r
WHERE r.role_key IN ('admin', 'teacher')
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id=r.role_id AND rm.menu_id=@student_tool_menu_id);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.role_id, m.menu_id FROM `sys_role` r
JOIN `sys_menu` m ON m.perms='business:studentTool:manage'
WHERE r.role_key IN ('admin', 'teacher')
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id=r.role_id AND rm.menu_id=m.menu_id);

-- 学生端不参与本菜单
DELETE rm FROM `sys_role_menu` rm
JOIN `sys_role` r ON r.role_id=rm.role_id AND r.role_key='student'
WHERE rm.menu_id IN (@student_tool_menu_id, (SELECT m.menu_id FROM sys_menu m WHERE m.perms='business:studentTool:manage'));

-- 6) 后检统计
SELECT
  (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='biz_lesson_assignment' AND column_name IN ('theory_open','practical_open')) AS gate_columns,
  (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name IN ('biz_student_tool','biz_student_tool_scope','biz_lesson_tool')) AS tool_tables,
  (SELECT COUNT(*) FROM sys_menu WHERE perms LIKE 'business:studentTool:%') AS student_tool_menus;