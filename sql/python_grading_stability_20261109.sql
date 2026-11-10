-- Python 批改最高分查询索引；仅增量添加，可重复执行。
-- 目标：按学生、课程、题目、提交类型定位有效正式提交，再按分数/判题时间倒序取第一条。
SET @db_name = DATABASE();
SET @sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE biz_programming_submission ADD KEY idx_programming_submission_best (student_id, lesson_id, question_id, submission_kind, score, judged_at, submission_id)',
    'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = @db_name
    AND table_name = 'biz_programming_submission'
    AND index_name = 'idx_programming_submission_best'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS columns
FROM information_schema.statistics
WHERE table_schema = @db_name
  AND table_name = 'biz_programming_submission'
  AND index_name = 'idx_programming_submission_best'
GROUP BY index_name;
