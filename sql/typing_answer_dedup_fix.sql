-- 学生答案唯一约束脚本（幂等、非破坏性）
-- 同一学生、课程、题目只允许一条记录，提交接口使用原子 upsert。

SELECT student_id, lesson_id, question_id, COUNT(*) AS answer_count,
       GROUP_CONCAT(answer_id ORDER BY answer_id DESC) AS answer_ids
FROM biz_student_answer
GROUP BY student_id, lesson_id, question_id
HAVING COUNT(*) > 1;

DELIMITER $$
DROP PROCEDURE IF EXISTS ensure_student_answer_unique$$
CREATE PROCEDURE ensure_student_answer_unique()
BEGIN
    DECLARE duplicate_count INT DEFAULT 0;
    DECLARE index_count INT DEFAULT 0;

    SELECT COUNT(*) INTO duplicate_count
    FROM (
        SELECT 1
        FROM biz_student_answer
        GROUP BY student_id, lesson_id, question_id
        HAVING COUNT(*) > 1
    ) duplicated_answers;

    IF duplicate_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'biz_student_answer 存在重复答案，请人工确认后再建唯一索引';
    END IF;

    SELECT COUNT(*) INTO index_count
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'biz_student_answer'
      AND INDEX_NAME = 'uk_student_lesson_question';

    IF index_count = 0 THEN
        ALTER TABLE biz_student_answer
            ADD UNIQUE KEY uk_student_lesson_question (student_id, lesson_id, question_id);
    END IF;
END$$
CALL ensure_student_answer_unique()$$
DROP PROCEDURE ensure_student_answer_unique$$
DELIMITER ;
