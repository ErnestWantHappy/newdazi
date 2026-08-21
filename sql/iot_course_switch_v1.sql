-- =============================================================
-- 课程级物联网开关迁移 v1（2026-08-21）
-- 用途：biz_lesson 增加 iot_enabled 字段；为已有物联网实验的课程回填开关=1。
-- 背景：物联入口原来全局显示（教师首页非考勤课、学生首页无条件），
--       本次改为与在线协作一致的课程级开关：教师在课程设计器中开启，
--       教师首页课程卡片与学生首页才显示「物联」入口。
-- 目标库：本机开发库 / 正式库（以目标环境 application-druid 外置配置为准）。
-- 说明：可重复执行（幂等）；旧代码忽略该字段，不强制重启；建议与新前后端版本一起发布。
-- 前置：执行前备份目标库（至少 biz_lesson 表）。
-- =============================================================

-- 1) 加列（information_schema 判断，避免重复执行报错）
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_lesson'
      AND column_name = 'iot_enabled'
);
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE biz_lesson ADD COLUMN iot_enabled tinyint(1) NOT NULL DEFAULT 0 COMMENT ''课程级物联网开关：1=开启，教师/学生首页显示物联入口'' AFTER auto_advance_ready_time',
    'SELECT ''biz_lesson.iot_enabled 已存在，跳过加列''');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) 回填：已创建过物联网实验的课程默认视为已开启，保持既有教学事实（如郑东旭已开课的班级）。
UPDATE biz_lesson l
SET l.iot_enabled = 1
WHERE l.iot_enabled = 0
  AND EXISTS (
      SELECT 1 FROM biz_iot_experiment e WHERE e.lesson_id = l.lesson_id
  );

-- 3) 后检统计（执行后人工核对）
SELECT COUNT(*) AS lessons_with_iot_enabled FROM biz_lesson WHERE iot_enabled = 1;

SELECT l.lesson_id, l.lesson_title, l.dept_id, e.experiment_id, e.title AS experiment_title
FROM biz_lesson l
JOIN biz_iot_experiment e ON e.lesson_id = l.lesson_id
WHERE l.iot_enabled = 1
ORDER BY l.lesson_id;
