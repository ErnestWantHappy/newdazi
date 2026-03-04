-- ============================================================
-- 数据库迁移脚本
-- 从本地 dazi8.sql 迁移到服务器 fuwuqi.sql (10.52.1.123)
-- 生成日期: 2026-02-05
-- 目标数据库: ry-vue
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 第一部分：新增表结构
-- ============================================================

-- ----------------------------
-- 新增表: biz_classroom_performance (课堂表现记录表)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `biz_classroom_performance`  (
  `performance_id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `lesson_id` bigint NOT NULL COMMENT '课程ID',
  `score` int NOT NULL DEFAULT 0 COMMENT '平时分（-10到+10）',
  `reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '加分/扣分原因',
  `teacher_id` bigint NOT NULL COMMENT '打分教师ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`performance_id`) USING BTREE,
  UNIQUE INDEX `uk_student_lesson`(`student_id` ASC, `lesson_id` ASC) USING BTREE,
  INDEX `idx_lesson`(`lesson_id` ASC) USING BTREE,
  INDEX `idx_teacher`(`teacher_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课堂表现记录表' ROW_FORMAT = Dynamic;

-- ============================================================
-- 第二部分：修改现有表结构
-- ============================================================

-- ----------------------------
-- 为 biz_student 表添加 remark 备注字段
-- 注意：如果字段已存在会报错，可忽略该错误继续执行
-- ----------------------------
-- 方式1：直接添加（如果字段已存在会报错 "Duplicate column name"，可忽略）
ALTER TABLE `biz_student` 
ADD COLUMN `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '教师备注' AFTER `class_code`;

-- ============================================================
-- 第三部分：新增菜单配置 (sys_menu)
-- 使用 INSERT IGNORE 避免重复插入
-- ============================================================

-- 2044: 学生个人成绩画像
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (2044, '学生个人成绩画像', 0, 6, 'student-profile', 'business/student-profile/index', NULL, '', 1, 0, 'C', '0', '0', NULL, 'user', 'admin', '2026-02-03 10:42:49', '', NULL, '');

-- 2045: 学校统计
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (2045, '学校统计', 0, 2, 'schoolStats', 'business/schoolStats/index', NULL, '', 1, 0, 'C', '0', '0', 'business:schoolStats:list', '#', 'laoda', '2026-02-03 19:05:03', '', NULL, '');

-- 2046: 学校成绩查询
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (2046, '学校成绩查询', 0, 3, 'schoolScore', 'business/schoolScore/index', NULL, '', 1, 0, 'C', '0', '0', 'business:schoolScore:stats', '#', 'admin', '2026-02-03 19:35:43', '', NULL, '');

-- ============================================================
-- 第四部分：角色菜单权限分配 (sys_role_menu)
-- 为管理员角色(role_id=1)分配新菜单权限
-- ============================================================

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 2044);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 2045);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 2046);

-- 如果教师角色也需要这些菜单权限，请取消下面注释：
-- INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2044);
-- INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2045);
-- INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2046);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 执行说明:
-- 1. 在 Navicat 中连接到服务器 10.52.1.123
-- 2. 选择数据库 ry-vue
-- 3. 打开新的查询窗口
-- 4. 复制粘贴此脚本内容
-- 5. 按 F5 或点击"运行"执行
-- 6. 检查执行结果，确认无错误
-- ============================================================
