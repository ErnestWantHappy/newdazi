-- 课程归档状态：0=正常使用，1=已归档。
-- 归档只让课程退出教师日常首页和学生当前任务，不删除课程及任何历史数据。

SET @has_lesson_status := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'biz_lesson'
    AND COLUMN_NAME = 'status'
);

SET @add_lesson_status := IF(
  @has_lesson_status = 0,
  'ALTER TABLE biz_lesson ADD COLUMN status char(1) NOT NULL DEFAULT ''0'' COMMENT ''使用状态（0正常 1归档）'' AFTER dept_id',
  'SELECT ''biz_lesson.status already exists'''
);

PREPARE stmt FROM @add_lesson_status;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE biz_lesson SET status = '0' WHERE status IS NULL OR status = '';

