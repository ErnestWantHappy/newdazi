-- 生产高并发只读性能索引（幂等）
-- 仅增加二级索引，不修改业务数据；正式执行前必须完成整库备份和 SHA-256 校验。

DELIMITER $$
DROP PROCEDURE IF EXISTS ensure_high_concurrency_read_indexes$$
CREATE PROCEDURE ensure_high_concurrency_read_indexes()
BEGIN
    DECLARE table_count INT DEFAULT 0;
    DECLARE index_count INT DEFAULT 0;

    SELECT COUNT(DISTINCT table_name) INTO table_count
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name IN (
          'biz_student_answer', 'biz_student', 'sys_user',
          'biz_lesson_question', 'biz_classroom_performance', 'biz_lesson_checkin'
      );
    IF table_count <> 6 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '高并发读索引前检失败：目标表不完整';
    END IF;

    SELECT COUNT(*) INTO index_count
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_student_answer'
      AND index_name = 'idx_answer_submit_lesson_student';
    IF index_count = 0 THEN
        ALTER TABLE biz_student_answer
            ADD INDEX idx_answer_submit_lesson_student (submit_time, lesson_id, student_id),
            ALGORITHM=INPLACE, LOCK=NONE;
    END IF;

    SELECT COUNT(*) INTO index_count
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_student_answer'
      AND index_name = 'idx_answer_lesson_student';
    IF index_count = 0 THEN
        ALTER TABLE biz_student_answer
            ADD INDEX idx_answer_lesson_student (lesson_id, student_id),
            ALGORITHM=INPLACE, LOCK=NONE;
    END IF;

    SELECT COUNT(*) INTO index_count
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_student'
      AND index_name = 'idx_student_class_user';
    IF index_count = 0 THEN
        ALTER TABLE biz_student
            ADD INDEX idx_student_class_user (entry_year, class_code, user_id),
            ALGORITHM=INPLACE, LOCK=NONE;
    END IF;

    SELECT COUNT(*) INTO index_count
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND index_name = 'idx_user_dept_status';
    IF index_count = 0 THEN
        ALTER TABLE sys_user
            ADD INDEX idx_user_dept_status (dept_id, status, del_flag, user_id),
            ALGORITHM=INPLACE, LOCK=NONE;
    END IF;

    SELECT COUNT(*) INTO index_count
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_lesson_question'
      AND index_name = 'idx_lesson_question_lesson';
    IF index_count = 0 THEN
        ALTER TABLE biz_lesson_question
            ADD INDEX idx_lesson_question_lesson (lesson_id, question_id),
            ALGORITHM=INPLACE, LOCK=NONE;
    END IF;

    SELECT COUNT(*) INTO index_count
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_classroom_performance'
      AND index_name = 'idx_performance_time_lesson_student';
    IF index_count = 0 THEN
        ALTER TABLE biz_classroom_performance
            ADD INDEX idx_performance_time_lesson_student (create_time, lesson_id, student_id),
            ALGORITHM=INPLACE, LOCK=NONE;
    END IF;

    SELECT COUNT(*) INTO index_count
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_lesson_checkin'
      AND index_name = 'idx_checkin_time_lesson_student';
    IF index_count = 0 THEN
        ALTER TABLE biz_lesson_checkin
            ADD INDEX idx_checkin_time_lesson_student (checkin_time, lesson_id, student_id),
            ALGORITHM=INPLACE, LOCK=NONE;
    END IF;
END$$
CALL ensure_high_concurrency_read_indexes()$$
DROP PROCEDURE ensure_high_concurrency_read_indexes$$
DELIMITER ;

-- 后检必须返回 7 个索引、20 个字段序号；重复执行结果保持不变。
SELECT table_name,
       index_name,
       GROUP_CONCAT(column_name ORDER BY seq_in_index SEPARATOR ',') AS index_columns
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND index_name IN (
      'idx_answer_submit_lesson_student',
      'idx_answer_lesson_student',
      'idx_student_class_user',
      'idx_user_dept_status',
      'idx_lesson_question_lesson',
      'idx_performance_time_lesson_student',
      'idx_checkin_time_lesson_student'
  )
GROUP BY table_name, index_name
ORDER BY table_name, index_name;
