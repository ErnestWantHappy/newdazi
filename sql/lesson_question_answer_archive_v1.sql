-- 课程换题答案归档 v1。
-- 运行时代码会在课程保存事务内归档未来被移除题目的答案；本脚本只治理已确认的课程 279、题目 1882/1883。
-- 执行前必须完成目标库备份。本脚本可重复执行。

CREATE TABLE IF NOT EXISTS biz_student_answer_orphan_archive LIKE biz_student_answer;

CREATE TABLE IF NOT EXISTS biz_student_answer_orphan_archive_meta
(
    answer_id      BIGINT       NOT NULL COMMENT '原答案ID',
    orphan_reason  VARCHAR(64)  NOT NULL COMMENT '归档原因',
    archived_at    DATETIME     NOT NULL COMMENT '归档时间',
    archive_batch  VARCHAR(64)  NOT NULL COMMENT '归档批次',
    PRIMARY KEY (answer_id),
    KEY idx_archive_batch (archive_batch)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='历史孤儿答案归档元数据';

DROP PROCEDURE IF EXISTS migrate_lesson_question_answer_archive_v1;
DELIMITER $$
CREATE PROCEDURE migrate_lesson_question_answer_archive_v1()
BEGIN
    DECLARE target_count INT DEFAULT 0;
    DECLARE archived_count INT DEFAULT 0;
    DECLARE metadata_count INT DEFAULT 0;
    DECLARE deleted_count INT DEFAULT 0;

    -- 归档表可能早于多格式作品字段创建，需要先补齐当前运行时代码使用的字段。
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_student_answer_orphan_archive' AND column_name='practical_artifact_id') THEN
        ALTER TABLE biz_student_answer_orphan_archive
            ADD COLUMN practical_artifact_id BIGINT NULL COMMENT '逻辑作品ID';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_student_answer_orphan_archive' AND column_name='practical_version_id') THEN
        ALTER TABLE biz_student_answer_orphan_archive
            ADD COLUMN practical_version_id BIGINT NULL COMMENT '当前提交版本ID';
    END IF;

    START TRANSACTION;

    SELECT COUNT(*) INTO target_count
    FROM biz_student_answer answer
    WHERE answer.lesson_id = 279
      AND answer.question_id IN (1882, 1883)
      AND NOT EXISTS (
          SELECT 1 FROM biz_lesson_question current_lq
          WHERE current_lq.lesson_id = answer.lesson_id
            AND current_lq.question_id = answer.question_id
      );

    INSERT IGNORE INTO biz_student_answer_orphan_archive
        (answer_id, student_id, lesson_id, question_id, student_answer, is_correct, score,
         answer_time, submit_time, typing_speed, accuracy_rate, completion_rate,
         preview_status, preview_path, preview_retry_count, preview_last_retry_time,
         preview_error_message, practical_artifact_id, practical_version_id)
    SELECT answer.answer_id, answer.student_id, answer.lesson_id, answer.question_id,
           answer.student_answer, answer.is_correct, answer.score, answer.answer_time,
           answer.submit_time, answer.typing_speed, answer.accuracy_rate,
           answer.completion_rate, answer.preview_status, answer.preview_path,
           answer.preview_retry_count, answer.preview_last_retry_time,
           answer.preview_error_message, answer.practical_artifact_id,
           answer.practical_version_id
    FROM biz_student_answer answer
    WHERE answer.lesson_id = 279
      AND answer.question_id IN (1882, 1883)
      AND NOT EXISTS (
          SELECT 1 FROM biz_lesson_question current_lq
          WHERE current_lq.lesson_id = answer.lesson_id
            AND current_lq.question_id = answer.question_id
      );

    SELECT COUNT(*) INTO archived_count
    FROM biz_student_answer answer
    INNER JOIN biz_student_answer_orphan_archive archive_row
        ON archive_row.answer_id = answer.answer_id
    WHERE answer.lesson_id = 279
      AND answer.question_id IN (1882, 1883)
      AND NOT EXISTS (
          SELECT 1 FROM biz_lesson_question current_lq
          WHERE current_lq.lesson_id = answer.lesson_id
            AND current_lq.question_id = answer.question_id
      );
    IF archived_count <> target_count THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '课程279旧题答案未完整归档，已取消治理';
    END IF;

    INSERT IGNORE INTO biz_student_answer_orphan_archive_meta
        (answer_id, orphan_reason, archived_at, archive_batch)
    SELECT answer.answer_id, 'lesson_question_removed', NOW(), 'lesson_279_removed_questions_v1'
    FROM biz_student_answer answer
    INNER JOIN biz_student_answer_orphan_archive archive_row
        ON archive_row.answer_id = answer.answer_id
    WHERE answer.lesson_id = 279
      AND answer.question_id IN (1882, 1883)
      AND NOT EXISTS (
          SELECT 1 FROM biz_lesson_question current_lq
          WHERE current_lq.lesson_id = answer.lesson_id
            AND current_lq.question_id = answer.question_id
      );

    SELECT COUNT(*) INTO metadata_count
    FROM biz_student_answer answer
    INNER JOIN biz_student_answer_orphan_archive archive_row
        ON archive_row.answer_id = answer.answer_id
    INNER JOIN biz_student_answer_orphan_archive_meta archive_meta
        ON archive_meta.answer_id = answer.answer_id
    WHERE answer.lesson_id = 279
      AND answer.question_id IN (1882, 1883)
      AND NOT EXISTS (
          SELECT 1 FROM biz_lesson_question current_lq
          WHERE current_lq.lesson_id = answer.lesson_id
            AND current_lq.question_id = answer.question_id
      );
    IF metadata_count <> target_count THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '课程279旧题答案归档元数据不完整，已取消治理';
    END IF;

    DELETE answer
    FROM biz_student_answer answer
    INNER JOIN biz_student_answer_orphan_archive archive_row
        ON archive_row.answer_id = answer.answer_id
    INNER JOIN biz_student_answer_orphan_archive_meta archive_meta
        ON archive_meta.answer_id = answer.answer_id
    WHERE answer.lesson_id = 279
      AND answer.question_id IN (1882, 1883)
      AND NOT EXISTS (
          SELECT 1 FROM biz_lesson_question current_lq
          WHERE current_lq.lesson_id = answer.lesson_id
            AND current_lq.question_id = answer.question_id
      );
    SET deleted_count = ROW_COUNT();
    IF deleted_count <> target_count THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '课程279旧题在线答案清理数量不一致，已回滚';
    END IF;

    COMMIT;
    SELECT target_count AS target_count, archived_count AS archived_count,
           metadata_count AS metadata_count, deleted_count AS deleted_count;
END$$
DELIMITER ;

CALL migrate_lesson_question_answer_archive_v1();
DROP PROCEDURE migrate_lesson_question_answer_archive_v1;

SELECT COUNT(*) AS remaining_live_removed_answers
FROM biz_student_answer answer
WHERE answer.lesson_id = 279
  AND answer.question_id IN (1882, 1883)
  AND NOT EXISTS (
      SELECT 1 FROM biz_lesson_question current_lq
      WHERE current_lq.lesson_id = answer.lesson_id
        AND current_lq.question_id = answer.question_id
  );

SELECT COUNT(*) AS archived_lesson_279_removed_answers
FROM biz_student_answer_orphan_archive archive_row
INNER JOIN biz_student_answer_orphan_archive_meta archive_meta
    ON archive_meta.answer_id = archive_row.answer_id
WHERE archive_row.lesson_id = 279
  AND archive_row.question_id IN (1882, 1883)
  AND archive_meta.orphan_reason = 'lesson_question_removed';
