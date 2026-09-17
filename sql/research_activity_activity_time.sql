-- 教研活动通知增加可选活动时间；脚本可重复执行。
SET @activity_time_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'biz_research_topic'
      AND COLUMN_NAME = 'activity_time'
);

SET @activity_time_sql := IF(
    @activity_time_exists = 0,
    'ALTER TABLE biz_research_topic ADD COLUMN activity_time datetime NULL COMMENT ''活动时间，为空时按未读状态提醒'' AFTER notice_stages',
    'SELECT 1'
);

PREPARE activity_time_stmt FROM @activity_time_sql;
EXECUTE activity_time_stmt;
DEALLOCATE PREPARE activity_time_stmt;
