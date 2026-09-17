-- 课堂大屏课程聚合查询索引（幂等）。执行前必须完成目标库备份和前检。
DELIMITER $$
DROP PROCEDURE IF EXISTS ensure_classroom_overview_perf_v1$$
CREATE PROCEDURE ensure_classroom_overview_perf_v1()
BEGIN
    DECLARE table_count INT DEFAULT 0;
    DECLARE index_count INT DEFAULT 0;

    SELECT COUNT(*) INTO table_count
      FROM information_schema.tables
     WHERE table_schema = DATABASE()
       AND table_name IN ('biz_student_answer', 'biz_lesson_question');
    IF table_count <> 2 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '课堂大屏索引前检失败：目标表不完整';
    END IF;

    SELECT COUNT(*) INTO index_count
      FROM information_schema.statistics
     WHERE table_schema = DATABASE()
       AND table_name = 'biz_student_answer'
       AND index_name = 'idx_classroom_answer_lesson_student_question';
    IF index_count = 0 THEN
        ALTER TABLE biz_student_answer
          ADD INDEX idx_classroom_answer_lesson_student_question (lesson_id, student_id, question_id, answer_id),
          ALGORITHM=INPLACE, LOCK=NONE;
    END IF;
END$$
CALL ensure_classroom_overview_perf_v1()$$
DROP PROCEDURE ensure_classroom_overview_perf_v1$$
DELIMITER ;

SELECT table_name, index_name,
       GROUP_CONCAT(column_name ORDER BY seq_in_index SEPARATOR ',') AS index_columns
  FROM information_schema.statistics
 WHERE table_schema = DATABASE()
   AND index_name = 'idx_classroom_answer_lesson_student_question'
 GROUP BY table_name, index_name;
