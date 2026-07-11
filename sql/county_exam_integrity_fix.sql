-- 区域抽测完整性增量升级脚本。
-- 可重复执行；执行前仍建议备份目标数据库。

-- 百分数按 0-100 保存，decimal(6,4) 无法容纳 100.0000。
SET @county_exam_sql = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'biz_county_exam_answer'
        AND column_name = 'accuracy_rate'
        AND column_type = 'decimal(7,4)'
    ),
    'SELECT 1',
    'ALTER TABLE `biz_county_exam_answer` MODIFY COLUMN `accuracy_rate` decimal(7,4) NULL DEFAULT NULL COMMENT ''打字正确率(%)'''
  )
);
PREPARE county_exam_stmt FROM @county_exam_sql;
EXECUTE county_exam_stmt;
DEALLOCATE PREPARE county_exam_stmt;

SET @county_exam_sql = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'biz_county_exam_answer'
        AND column_name = 'completion_rate'
        AND column_type = 'decimal(7,4)'
    ),
    'SELECT 1',
    'ALTER TABLE `biz_county_exam_answer` MODIFY COLUMN `completion_rate` decimal(7,4) NULL DEFAULT NULL COMMENT ''打字完成率(%)'''
  )
);
PREPARE county_exam_stmt FROM @county_exam_sql;
EXECUTE county_exam_stmt;
DEALLOCATE PREPARE county_exam_stmt;

-- 按题评卷允许同一教师承担多道操作题，唯一键必须包含 question_id。
SET @county_exam_index_columns = (
  SELECT GROUP_CONCAT(column_name ORDER BY seq_in_index SEPARATOR ',')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'biz_county_exam_grader'
    AND index_name = 'uk_county_exam_grader_question'
);
SET @county_exam_sql = IF(
  @county_exam_index_columns IS NOT NULL
    AND @county_exam_index_columns != 'exam_id,question_id,grader_id',
  'ALTER TABLE `biz_county_exam_grader` DROP INDEX `uk_county_exam_grader_question`',
  'SELECT 1'
);
PREPARE county_exam_stmt FROM @county_exam_sql;
EXECUTE county_exam_stmt;
DEALLOCATE PREPARE county_exam_stmt;

SET @county_exam_sql = (
  SELECT IF(
    EXISTS (
      SELECT index_name
      FROM information_schema.statistics
      WHERE table_schema = DATABASE()
        AND table_name = 'biz_county_exam_grader'
        AND index_name = 'uk_county_exam_grader_question'
        AND non_unique = 0
      GROUP BY index_name
      HAVING GROUP_CONCAT(column_name ORDER BY seq_in_index SEPARATOR ',') = 'exam_id,question_id,grader_id'
    ),
    'SELECT 1',
    'ALTER TABLE `biz_county_exam_grader` ADD UNIQUE INDEX `uk_county_exam_grader_question` (`exam_id`, `question_id`, `grader_id`)'
  )
);
PREPARE county_exam_stmt FROM @county_exam_sql;
EXECUTE county_exam_stmt;
DEALLOCATE PREPARE county_exam_stmt;

SET @county_exam_sql = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.statistics
      WHERE table_schema = DATABASE()
        AND table_name = 'biz_county_exam_grader'
        AND index_name = 'idx_county_exam_grader_question'
    ),
    'ALTER TABLE `biz_county_exam_grader` DROP INDEX `idx_county_exam_grader_question`',
    'SELECT 1'
  )
);
PREPARE county_exam_stmt FROM @county_exam_sql;
EXECUTE county_exam_stmt;
DEALLOCATE PREPARE county_exam_stmt;

SET @county_exam_sql = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.statistics
      WHERE table_schema = DATABASE()
        AND table_name = 'biz_county_exam_grader'
        AND index_name = 'uk_exam_grader'
    ),
    'ALTER TABLE `biz_county_exam_grader` DROP INDEX `uk_exam_grader`',
    'SELECT 1'
  )
);
PREPARE county_exam_stmt FROM @county_exam_sql;
EXECUTE county_exam_stmt;
DEALLOCATE PREPARE county_exam_stmt;
