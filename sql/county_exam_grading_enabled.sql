-- 区域抽测评卷入口开关字段
-- 可重复执行：仅在字段缺失时新增，默认关闭。

SET @column_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'biz_county_exam'
    AND COLUMN_NAME = 'grading_enabled'
);

SET @ddl := IF(
  @column_exists = 0,
  'ALTER TABLE `biz_county_exam` ADD COLUMN `grading_enabled` char(1) NOT NULL DEFAULT ''0'' COMMENT ''评卷入口是否开启(0否 1是)'' AFTER `status`',
  'SELECT ''biz_county_exam.grading_enabled already exists'''
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `biz_county_exam`
SET `grading_enabled` = '0'
WHERE `grading_enabled` IS NULL OR `status` = '3';
