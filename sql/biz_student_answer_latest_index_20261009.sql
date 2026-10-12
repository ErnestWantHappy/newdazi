-- 2026-10-09 修复「最新作答派生表」慢查询（130 正式）
-- 背景：批改/课堂监控类查询普遍使用
--   INNER JOIN (SELECT MAX(answer_id) FROM biz_student_answer
--               GROUP BY student_id, lesson_id, question_id) latest ON ...
-- 现有唯一索引 uk_student_lesson_question(student_id, lesson_id, question_id) 不含 answer_id，
-- 取 MAX(answer_id) 需回表读取聚簇索引（含 student_answer TEXT），退化为全表扫描，实测约 1.4s。
-- 本脚本新增覆盖索引，使该分组可 index-only 完成；幂等，可重复执行。
--
-- 回滚（仅在确认不需要时执行）：
--   ALTER TABLE biz_student_answer DROP INDEX idx_answer_latest;

SET @exists := (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = 'ry-vue'
    AND table_name = 'biz_student_answer'
    AND index_name = 'idx_answer_latest'
);
SET @ddl := IF(@exists = 0,
  'ALTER TABLE biz_student_answer ADD INDEX idx_answer_latest (student_id, lesson_id, question_id, answer_id)',
  'SELECT ''idx_answer_latest 已存在，跳过'' AS note');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 后检：确认索引存在
SELECT index_name, seq_in_index, column_name
FROM information_schema.statistics
WHERE table_schema = 'ry-vue' AND table_name = 'biz_student_answer' AND index_name = 'idx_answer_latest'
ORDER BY seq_in_index;
