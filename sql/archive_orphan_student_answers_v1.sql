-- 历史孤儿答案治理：先原样归档，再从在线答案表移除。
-- 回滚时仅在父记录已恢复后，将归档表同名字段 INSERT IGNORE 回 biz_student_answer。

CREATE TABLE IF NOT EXISTS biz_student_answer_orphan_archive LIKE biz_student_answer;

CREATE TABLE IF NOT EXISTS biz_student_answer_orphan_archive_meta
(
    answer_id      BIGINT       NOT NULL COMMENT '原答案ID',
    orphan_reason  VARCHAR(64)  NOT NULL COMMENT '孤儿原因',
    archived_at    DATETIME     NOT NULL COMMENT '归档时间',
    archive_batch  VARCHAR(64)  NOT NULL COMMENT '归档批次',
    PRIMARY KEY (answer_id),
    KEY idx_archive_batch (archive_batch)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='历史孤儿答案归档元数据';

DROP PROCEDURE IF EXISTS migrate_orphan_student_answers_v1;

DELIMITER //
CREATE PROCEDURE migrate_orphan_student_answers_v1()
BEGIN
    DECLARE target_count INT DEFAULT 0;
    DECLARE archived_target_count INT DEFAULT 0;
    DECLARE deleted_count INT DEFAULT 0;

    START TRANSACTION;

    SELECT COUNT(*) INTO target_count
    FROM biz_student_answer a
    LEFT JOIN biz_student s ON s.student_id = a.student_id
    LEFT JOIN biz_lesson l ON l.lesson_id = a.lesson_id
    WHERE s.student_id IS NULL OR l.lesson_id IS NULL;

    INSERT IGNORE INTO biz_student_answer_orphan_archive
    SELECT a.*
    FROM biz_student_answer a
    LEFT JOIN biz_student s ON s.student_id = a.student_id
    LEFT JOIN biz_lesson l ON l.lesson_id = a.lesson_id
    WHERE s.student_id IS NULL OR l.lesson_id IS NULL;

    SELECT COUNT(*) INTO archived_target_count
    FROM biz_student_answer a
    INNER JOIN biz_student_answer_orphan_archive archive_row
        ON archive_row.answer_id = a.answer_id
    LEFT JOIN biz_student s ON s.student_id = a.student_id
    LEFT JOIN biz_lesson l ON l.lesson_id = a.lesson_id
    WHERE s.student_id IS NULL OR l.lesson_id IS NULL;

    IF archived_target_count <> target_count THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '孤儿答案未完整归档，已取消治理';
    END IF;

    INSERT IGNORE INTO biz_student_answer_orphan_archive_meta
        (answer_id, orphan_reason, archived_at, archive_batch)
    SELECT a.answer_id,
           CASE
               WHEN s.student_id IS NULL AND l.lesson_id IS NULL THEN 'missing_student_and_lesson'
               WHEN s.student_id IS NULL THEN 'missing_student'
               ELSE 'missing_lesson'
           END,
           NOW(),
           'archive_orphan_student_answers_v1'
    FROM biz_student_answer a
    LEFT JOIN biz_student s ON s.student_id = a.student_id
    LEFT JOIN biz_lesson l ON l.lesson_id = a.lesson_id
    WHERE s.student_id IS NULL OR l.lesson_id IS NULL;

    DELETE a
    FROM biz_student_answer a
    INNER JOIN biz_student_answer_orphan_archive archive_row
        ON archive_row.answer_id = a.answer_id
    LEFT JOIN biz_student s ON s.student_id = a.student_id
    LEFT JOIN biz_lesson l ON l.lesson_id = a.lesson_id
    WHERE s.student_id IS NULL OR l.lesson_id IS NULL;

    SET deleted_count = ROW_COUNT();
    IF deleted_count <> target_count THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '孤儿答案删除数量不一致，已回滚';
    END IF;

    COMMIT;

    SELECT target_count AS target_count,
           archived_target_count AS archived_count,
           deleted_count AS deleted_count;
END//
DELIMITER ;

CALL migrate_orphan_student_answers_v1();
DROP PROCEDURE migrate_orphan_student_answers_v1;

SELECT COUNT(*) AS remaining_missing_student
FROM biz_student_answer a
LEFT JOIN biz_student s ON s.student_id = a.student_id
WHERE s.student_id IS NULL;

SELECT COUNT(*) AS remaining_missing_lesson
FROM biz_student_answer a
LEFT JOIN biz_lesson l ON l.lesson_id = a.lesson_id
WHERE l.lesson_id IS NULL;
