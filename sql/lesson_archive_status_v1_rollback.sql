-- 回滚仅删除归档字段；不会恢复已经做过的归档选择。
SET @has_lesson_status := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'biz_lesson'
    AND COLUMN_NAME = 'status'
);

SET @drop_lesson_status := IF(
  @has_lesson_status > 0,
  'ALTER TABLE biz_lesson DROP COLUMN status',
  'SELECT ''biz_lesson.status does not exist'''
);

PREPARE stmt FROM @drop_lesson_status;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
