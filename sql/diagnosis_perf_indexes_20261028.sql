-- 130 诊断性能索引（2026-10-28，幂等）
-- 仅新增二级索引，不修改业务数据；执行前必须完成整库备份并记录 SHA-256。
-- 建议低峰 22:00-06:00 逐条在线执行；出现锁等待立即停止，不要并行。

-- ========== 执行前检查（只读） ==========
-- 1) 表是否存在
SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('biz_student_answer', 'biz_practical_attachment');

-- 2) 目标索引是否已存在
SELECT table_name, index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS columns
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name IN ('biz_student_answer', 'biz_practical_attachment')
  AND index_name IN (
      'idx_answer_preview_path',
      'idx_answer_text_prefix',
      'idx_attachment_preview_path',
      'idx_answer_lesson_student_submit',
      'idx_answer_lesson_student_question_answer'
  )
GROUP BY table_name, index_name;

-- 3) 体积与路径长度（用估算行数，避免大表 COUNT(*)）
SELECT table_name, table_rows
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('biz_student_answer', 'biz_practical_attachment');

-- ========== 幂等新增 ==========
DROP PROCEDURE IF EXISTS ensure_diagnosis_perf_indexes_20261028;
DELIMITER $$
CREATE PROCEDURE ensure_diagnosis_perf_indexes_20261028()
BEGIN
    DECLARE table_count INT DEFAULT 0;
    DECLARE index_count INT DEFAULT 0;

    SELECT COUNT(DISTINCT table_name) INTO table_count
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name IN ('biz_student_answer', 'biz_practical_attachment');
    IF table_count <> 2 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '诊断性能索引前检失败：目标表不完整';
    END IF;

    -- 1) 资源鉴权：学生答案路径
    SELECT COUNT(*) INTO index_count
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_student_answer'
      AND index_name = 'idx_answer_text_prefix';
    IF index_count = 0 THEN
        ALTER TABLE biz_student_answer
            ADD INDEX idx_answer_text_prefix (student_answer(191)),
            ALGORITHM=INPLACE, LOCK=NONE;
    END IF;

    -- 2) 资源鉴权：答案预览路径
    SELECT COUNT(*) INTO index_count
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_student_answer'
      AND index_name = 'idx_answer_preview_path';
    IF index_count = 0 THEN
        ALTER TABLE biz_student_answer
            ADD INDEX idx_answer_preview_path (preview_path(191)),
            ALGORITHM=INPLACE, LOCK=NONE;
    END IF;

    -- 3) 资源鉴权：附件预览路径（resource_path 已有 idx_practical_attachment_path）
    SELECT COUNT(*) INTO index_count
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_practical_attachment'
      AND index_name = 'idx_attachment_preview_path';
    IF index_count = 0 THEN
        ALTER TABLE biz_practical_attachment
            ADD INDEX idx_attachment_preview_path (preview_path(191)),
            ALGORITHM=INPLACE, LOCK=NONE;
    END IF;

    -- 4) 班级统计：MIN(submit_time) 与按课/学生分组
    SELECT COUNT(*) INTO index_count
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_student_answer'
      AND index_name = 'idx_answer_lesson_student_submit';
    IF index_count = 0 THEN
        ALTER TABLE biz_student_answer
            ADD INDEX idx_answer_lesson_student_submit (lesson_id, student_id, submit_time),
            ALGORITHM=INPLACE, LOCK=NONE;
    END IF;

    -- 5) 班级状态：MAX(answer_id) 按课/学生/题分组
    SELECT COUNT(*) INTO index_count
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_student_answer'
      AND index_name = 'idx_answer_lesson_student_question_answer';
    IF index_count = 0 THEN
        ALTER TABLE biz_student_answer
            ADD INDEX idx_answer_lesson_student_question_answer (lesson_id, student_id, question_id, answer_id),
            ALGORITHM=INPLACE, LOCK=NONE;
    END IF;
END$$
DELIMITER ;

CALL ensure_diagnosis_perf_indexes_20261028();
DROP PROCEDURE IF EXISTS ensure_diagnosis_perf_indexes_20261028;

-- ========== 执行后验证 ==========
SELECT table_name, index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS columns
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name IN ('biz_student_answer', 'biz_practical_attachment')
  AND index_name IN (
      'idx_answer_preview_path',
      'idx_answer_text_prefix',
      'idx_attachment_preview_path',
      'idx_answer_lesson_student_submit',
      'idx_answer_lesson_student_question_answer'
  )
GROUP BY table_name, index_name;

-- 资源查询计划（应看到 ref/range，而不是 ALL）
-- EXPLAIN SELECT MAX(answer_id) FROM biz_student_answer WHERE student_answer = '/x';
-- EXPLAIN SELECT MAX(answer_id) FROM biz_student_answer WHERE preview_path = '/x';
-- EXPLAIN SELECT MAX(v.source_answer_id) FROM biz_practical_attachment attachment
--   INNER JOIN biz_practical_submission_version v ON v.version_id = attachment.version_id
--   WHERE v.source_answer_id IS NOT NULL AND attachment.preview_path = '/x';
-- EXPLAIN SELECT MAX(answer_id) FROM biz_student_answer
--   WHERE lesson_id = 1 GROUP BY student_id, lesson_id, question_id;
-- EXPLAIN SELECT MIN(submit_time) FROM biz_student_answer
--   WHERE lesson_id = 1 GROUP BY student_id;

-- ========== 回滚（在线，向后兼容，回退代码时可不删） ==========
-- ALTER TABLE biz_student_answer DROP INDEX idx_answer_lesson_student_question_answer, ALGORITHM=INPLACE, LOCK=NONE;
-- ALTER TABLE biz_student_answer DROP INDEX idx_answer_lesson_student_submit, ALGORITHM=INPLACE, LOCK=NONE;
-- ALTER TABLE biz_practical_attachment DROP INDEX idx_attachment_preview_path, ALGORITHM=INPLACE, LOCK=NONE;
-- ALTER TABLE biz_student_answer DROP INDEX idx_answer_preview_path, ALGORITHM=INPLACE, LOCK=NONE;
-- ALTER TABLE biz_student_answer DROP INDEX idx_answer_text_prefix, ALGORITHM=INPLACE, LOCK=NONE;
