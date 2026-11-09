-- 菜单排序调整：
-- 1. 电子教材与教学资源紧接题库管理之后
-- 2. 成绩查询紧接导学单管理之后
-- 3. 教研活动紧靠学生管理之前
-- 包含原顺序备份与幂等更新。

CREATE TABLE IF NOT EXISTS sys_menu_order_backup_20261103 (
  menu_id bigint NOT NULL PRIMARY KEY,
  menu_name varchar(50) NOT NULL,
  order_num int NOT NULL
);

INSERT IGNORE INTO sys_menu_order_backup_20261103 (menu_id, menu_name, order_num)
SELECT menu_id, menu_name, order_num FROM sys_menu WHERE parent_id = 0;

-- 顺序调整
UPDATE sys_menu SET order_num = 10 WHERE menu_id = 2037; -- 教师首页
UPDATE sys_menu SET order_num = 20 WHERE menu_id = 26014; -- 教师工具
UPDATE sys_menu SET order_num = 30 WHERE menu_id = 26000; -- 教研活动 (紧靠学生管理之前)
UPDATE sys_menu SET order_num = 40 WHERE menu_id = 2018; -- 学生管理
UPDATE sys_menu SET order_num = 50 WHERE menu_id = 2038; -- 班级管理
UPDATE sys_menu SET order_num = 60 WHERE menu_id = 2006; -- 课程管理
UPDATE sys_menu SET order_num = 70 WHERE menu_id = 2031; -- 题库管理
UPDATE sys_menu SET order_num = 80, menu_name = '电子教材与教学资源' WHERE menu_id = 26100; -- 电子教材与教学资源 (紧接题库管理之后)
UPDATE sys_menu SET order_num = 90 WHERE menu_id = 2048; -- 导学单管理
UPDATE sys_menu SET order_num = 100 WHERE menu_id = 2042; -- 成绩查询 (紧接导学单管理之后)
UPDATE sys_menu SET order_num = 110 WHERE menu_id = 2044; -- 学生个人成绩画像
UPDATE sys_menu SET order_num = 120 WHERE menu_id = 26016; -- 物联网实验
UPDATE sys_menu SET order_num = 130 WHERE menu_id = 26018; -- Python刷题
UPDATE sys_menu SET order_num = 140 WHERE menu_id = 26011; -- 免抽测申请
UPDATE sys_menu SET order_num = 150 WHERE menu_id = 26012; -- 免抽测申请审核
UPDATE sys_menu SET order_num = 160 WHERE menu_id = 26022; -- 学生实验工具
UPDATE sys_menu SET order_num = 170 WHERE menu_id = 2046; -- 课程与成绩监管
UPDATE sys_menu SET order_num = 180 WHERE menu_id = 2045; -- 学校统计
UPDATE sys_menu SET order_num = 190 WHERE menu_id = 2047; -- 区域抽测
UPDATE sys_menu SET order_num = 200 WHERE menu_id = 1; -- 系统管理
UPDATE sys_menu SET order_num = 210 WHERE menu_id = 2; -- 系统监控
UPDATE sys_menu SET order_num = 220 WHERE menu_id = 3; -- 系统工具
UPDATE sys_menu SET order_num = 230 WHERE menu_id = 4; -- 若依官网
UPDATE sys_menu SET order_num = 990 WHERE menu_id = 26024; -- 平台更新
