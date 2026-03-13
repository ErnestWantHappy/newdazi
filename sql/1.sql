-- ------------------------------------------------------------------
-- 1. 为 `biz_lesson` 表增加 `dept_id` 字段并刷新历史数据
-- ------------------------------------------------------------------
ALTER TABLE `biz_lesson` 
ADD COLUMN `dept_id` bigint(20) NULL DEFAULT NULL COMMENT '所属学校ID' AFTER `creator_id`;
-- 根据创建教师归属的学校，修复历史记录的 dept_id
UPDATE `biz_lesson` bl 
INNER JOIN `sys_user` su ON bl.creator_id = su.user_id 
SET bl.dept_id = su.dept_id 
WHERE bl.dept_id IS NULL;
-- ------------------------------------------------------------------
-- 2. 为 `biz_lesson_assignment` 表增加 `dept_id` 字段并刷新历史数据
-- ------------------------------------------------------------------
ALTER TABLE `biz_lesson_assignment` 
ADD COLUMN `dept_id` bigint(20) NULL DEFAULT NULL COMMENT '所属学校ID' AFTER `assigner_id`;
-- 根据指派教师归属的学校，修复历史记录的 dept_id
UPDATE `biz_lesson_assignment` bla 
INNER JOIN `sys_user` su ON bla.assigner_id = su.user_id 
SET bla.dept_id = su.dept_id 
WHERE bla.dept_id IS NULL;
-- ------------------------------------------------------------------
-- 3. 为 `biz_classroom_performance` 表增加 `dept_id` 字段并刷新历史数据
-- ------------------------------------------------------------------
ALTER TABLE `biz_classroom_performance` 
ADD COLUMN `dept_id` bigint(20) NULL DEFAULT NULL COMMENT '所属学校ID' AFTER `student_id`;
-- 根据打分记录对应的学生所在学校，修复历史记录的 dept_id
UPDATE `biz_classroom_performance` bcp 
INNER JOIN `sys_user` su ON bcp.student_id = su.user_id 
SET bcp.dept_id = su.dept_id 
WHERE bcp.dept_id IS NULL;