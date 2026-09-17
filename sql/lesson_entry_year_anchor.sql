-- 课程入学年份锚定迁移（MySQL 8）
--
-- 背景：biz_lesson.grade 是课程创建时的静态内容年级快照，不能作为课程所属届别的稳定标识。
-- 本迁移新增 biz_lesson.entry_year，并按以下优先级一次性回填：
--   1. 当前课程指派 biz_lesson_assignment
--   2. 课程推进历史 biz_lesson_assignment_history
--   3. 学生答案关联学生的入学年份
--   4. 完全没有业务证据时，按切换前最后正常学年 2025 和原 grade 推算
--
-- 安全约束：
--   * 任一课程只要在上述证据中出现多个入学年份，立即 SIGNAL，中止回填。
--   * 已有 entry_year 与业务证据不一致时立即 SIGNAL，不覆盖已有值。
--   * 不修改 biz_lesson.grade。

DROP PROCEDURE IF EXISTS sp_migrate_lesson_entry_year_anchor;
DELIMITER $$

CREATE PROCEDURE sp_migrate_lesson_entry_year_anchor()
BEGIN
    DECLARE v_conflict_count BIGINT DEFAULT 0;
    DECLARE v_invalid_evidence_count BIGINT DEFAULT 0;
    DECLARE v_column_count INT DEFAULT 0;
    DECLARE v_column_type VARCHAR(64) DEFAULT NULL;
    DECLARE v_column_length BIGINT DEFAULT NULL;
    DECLARE v_column_nullable VARCHAR(3) DEFAULT NULL;
    DECLARE v_existing_invalid_count BIGINT DEFAULT 0;
    DECLARE v_existing_mismatch_count BIGINT DEFAULT 0;
    DECLARE v_candidate_invalid_count BIGINT DEFAULT 0;
    DECLARE v_postflight_invalid_count BIGINT DEFAULT 0;
    DECLARE v_backfilled_rows BIGINT DEFAULT 0;
    DECLARE v_total_lessons BIGINT DEFAULT 0;
    DECLARE v_index_name_count INT DEFAULT 0;
    DECLARE v_index_columns VARCHAR(255) DEFAULT NULL;
    DECLARE v_index_non_unique INT DEFAULT NULL;
    DECLARE v_exact_index_count INT DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        DROP TEMPORARY TABLE IF EXISTS tmp_lesson_entry_year_candidate;
        DROP TEMPORARY TABLE IF EXISTS tmp_lesson_entry_year_evidence;
        RESIGNAL;
    END;

    DROP TEMPORARY TABLE IF EXISTS tmp_lesson_entry_year_candidate;
    DROP TEMPORARY TABLE IF EXISTS tmp_lesson_entry_year_evidence;

    CREATE TEMPORARY TABLE tmp_lesson_entry_year_evidence
    (
        lesson_id BIGINT NOT NULL,
        source_priority TINYINT NOT NULL,
        source_name VARCHAR(32) NOT NULL,
        entry_year VARCHAR(64) NOT NULL,
        KEY idx_tmp_lesson_entry_year (lesson_id, source_priority, entry_year)
    ) ENGINE = InnoDB;

    -- 当前指派是最直接的课程届别证据；同一届多个班级会被 DISTINCT 合并。
    INSERT INTO tmp_lesson_entry_year_evidence (lesson_id, source_priority, source_name, entry_year)
    SELECT DISTINCT l.lesson_id, 1, 'current_assignment', TRIM(a.entry_year)
    FROM biz_lesson l
    INNER JOIN biz_lesson_assignment a ON a.lesson_id = l.lesson_id
    WHERE a.entry_year IS NOT NULL;

    -- 推进历史中的旧课程和下一课程都属于该历史记录对应的同一届学生。
    INSERT INTO tmp_lesson_entry_year_evidence (lesson_id, source_priority, source_name, entry_year)
    SELECT DISTINCT l.lesson_id, 2, 'assignment_history', TRIM(h.entry_year)
    FROM biz_lesson l
    INNER JOIN biz_lesson_assignment_history h ON h.lesson_id = l.lesson_id
    WHERE h.entry_year IS NOT NULL;

    INSERT INTO tmp_lesson_entry_year_evidence (lesson_id, source_priority, source_name, entry_year)
    SELECT DISTINCT l.lesson_id, 2, 'assignment_history', TRIM(h.entry_year)
    FROM biz_lesson l
    INNER JOIN biz_lesson_assignment_history h ON h.next_lesson_id = l.lesson_id
    WHERE h.entry_year IS NOT NULL;

    -- 答案只能通过学生主表确定届别，不能从账号或作答时间猜测。
    INSERT INTO tmp_lesson_entry_year_evidence (lesson_id, source_priority, source_name, entry_year)
    SELECT DISTINCT l.lesson_id, 3, 'student_answer', TRIM(s.entry_year)
    FROM biz_lesson l
    INNER JOIN biz_student_answer a ON a.lesson_id = l.lesson_id
    INNER JOIN biz_student s ON s.student_id = a.student_id
    WHERE s.entry_year IS NOT NULL;

    -- 先拦截空值、非四位年份或异常年份，避免脏证据被截断后写入 varchar(4)。
    SELECT COUNT(*)
    INTO v_invalid_evidence_count
    FROM tmp_lesson_entry_year_evidence
    WHERE entry_year = ''
       OR NOT REGEXP_LIKE(entry_year, '^[0-9]{4}$')
       OR CAST(entry_year AS UNSIGNED) NOT BETWEEN 1900 AND 2100;

    IF v_invalid_evidence_count > 0 THEN
        SELECT lesson_id, source_name, entry_year
        FROM tmp_lesson_entry_year_evidence
        WHERE entry_year = ''
           OR NOT REGEXP_LIKE(entry_year, '^[0-9]{4}$')
           OR CAST(entry_year AS UNSIGNED) NOT BETWEEN 1900 AND 2100
        ORDER BY lesson_id, source_priority
        LIMIT 50;
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'lesson entry_year migration aborted: invalid entry-year evidence';
    END IF;

    -- 跨来源也必须一致：当前指派虽然优先，但不能掩盖历史或答案显示的多届复用。
    SELECT COUNT(*)
    INTO v_conflict_count
    FROM
    (
        SELECT lesson_id
        FROM tmp_lesson_entry_year_evidence
        GROUP BY lesson_id
        HAVING COUNT(DISTINCT entry_year) > 1
    ) conflict_lessons;

    IF v_conflict_count > 0 THEN
        SELECT lesson_id,
               GROUP_CONCAT(DISTINCT CONCAT(source_name, ':', entry_year)
                            ORDER BY source_priority, entry_year SEPARATOR ', ') AS conflicting_evidence
        FROM tmp_lesson_entry_year_evidence
        GROUP BY lesson_id
        HAVING COUNT(DISTINCT entry_year) > 1
        ORDER BY lesson_id
        LIMIT 50;
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'lesson entry_year migration aborted: multi-cohort lesson evidence detected';
    END IF;

    SELECT COUNT(*), MAX(data_type), MAX(character_maximum_length), MAX(is_nullable)
    INTO v_column_count, v_column_type, v_column_length, v_column_nullable
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_lesson'
      AND column_name = 'entry_year';

    IF v_column_count = 0 THEN
        ALTER TABLE biz_lesson
            ADD COLUMN entry_year VARCHAR(4) NULL COMMENT '课程所属入学年份（稳定届别）' AFTER grade;
        SET v_column_nullable = 'YES';
    ELSEIF v_column_type <> 'varchar' OR v_column_length <> 4 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'lesson entry_year migration aborted: existing column is not varchar(4)';
    END IF;

    -- 支持安全重跑：已有合法锚点保留，但必须与现有业务证据一致。
    SELECT COUNT(*)
    INTO v_existing_invalid_count
    FROM biz_lesson
    WHERE entry_year IS NOT NULL
      AND (TRIM(entry_year) = ''
           OR NOT REGEXP_LIKE(TRIM(entry_year), '^[0-9]{4}$')
           OR CAST(TRIM(entry_year) AS UNSIGNED) NOT BETWEEN 1900 AND 2100);

    IF v_existing_invalid_count > 0 THEN
        SELECT lesson_id, grade, entry_year
        FROM biz_lesson
        WHERE entry_year IS NOT NULL
          AND (TRIM(entry_year) = ''
               OR NOT REGEXP_LIKE(TRIM(entry_year), '^[0-9]{4}$')
               OR CAST(TRIM(entry_year) AS UNSIGNED) NOT BETWEEN 1900 AND 2100)
        ORDER BY lesson_id
        LIMIT 50;
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'lesson entry_year migration aborted: existing anchor is invalid';
    END IF;

    SELECT COUNT(*)
    INTO v_existing_mismatch_count
    FROM biz_lesson l
    INNER JOIN
    (
        SELECT lesson_id, MIN(entry_year) AS evidence_entry_year
        FROM tmp_lesson_entry_year_evidence
        GROUP BY lesson_id
    ) e ON e.lesson_id = l.lesson_id
    WHERE l.entry_year IS NOT NULL
      AND TRIM(l.entry_year) <> ''
      AND TRIM(l.entry_year) <> e.evidence_entry_year;

    IF v_existing_mismatch_count > 0 THEN
        SELECT l.lesson_id, l.entry_year AS existing_entry_year,
               e.evidence_entry_year
        FROM biz_lesson l
        INNER JOIN
        (
            SELECT lesson_id, MIN(entry_year) AS evidence_entry_year
            FROM tmp_lesson_entry_year_evidence
            GROUP BY lesson_id
        ) e ON e.lesson_id = l.lesson_id
        WHERE l.entry_year IS NOT NULL
          AND TRIM(l.entry_year) <> ''
          AND TRIM(l.entry_year) <> e.evidence_entry_year
        ORDER BY l.lesson_id
        LIMIT 50;
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'lesson entry_year migration aborted: existing anchor conflicts with business evidence';
    END IF;

    CREATE TEMPORARY TABLE tmp_lesson_entry_year_candidate
    (
        lesson_id BIGINT NOT NULL PRIMARY KEY,
        entry_year VARCHAR(4) NULL,
        anchor_source VARCHAR(32) NOT NULL
    ) ENGINE = InnoDB;

    INSERT INTO tmp_lesson_entry_year_candidate (lesson_id, entry_year, anchor_source)
    SELECT l.lesson_id,
           CASE
               WHEN l.entry_year IS NOT NULL AND TRIM(l.entry_year) <> '' THEN TRIM(l.entry_year)
               WHEN MAX(CASE WHEN e.source_priority = 1 THEN e.entry_year END) IS NOT NULL
                   THEN MAX(CASE WHEN e.source_priority = 1 THEN e.entry_year END)
               WHEN MAX(CASE WHEN e.source_priority = 2 THEN e.entry_year END) IS NOT NULL
                   THEN MAX(CASE WHEN e.source_priority = 2 THEN e.entry_year END)
               WHEN MAX(CASE WHEN e.source_priority = 3 THEN e.entry_year END) IS NOT NULL
                   THEN MAX(CASE WHEN e.source_priority = 3 THEN e.entry_year END)
               WHEN l.grade BETWEEN 1 AND 6
                   THEN CAST(2025 - l.grade + 1 AS CHAR)
               WHEN l.grade BETWEEN 7 AND 9
                   THEN CAST(2025 - (l.grade - 6) + 1 AS CHAR)
               WHEN l.grade BETWEEN 10 AND 12
                   THEN CAST(2025 - (l.grade - 9) + 1 AS CHAR)
               ELSE NULL
           END AS entry_year,
           CASE
               WHEN l.entry_year IS NOT NULL AND TRIM(l.entry_year) <> '' THEN 'existing_anchor'
               WHEN MAX(CASE WHEN e.source_priority = 1 THEN e.entry_year END) IS NOT NULL
                   THEN 'current_assignment'
               WHEN MAX(CASE WHEN e.source_priority = 2 THEN e.entry_year END) IS NOT NULL
                   THEN 'assignment_history'
               WHEN MAX(CASE WHEN e.source_priority = 3 THEN e.entry_year END) IS NOT NULL
                   THEN 'student_answer'
               ELSE 'fallback_2025_grade'
           END AS anchor_source
    FROM biz_lesson l
    LEFT JOIN tmp_lesson_entry_year_evidence e ON e.lesson_id = l.lesson_id
    GROUP BY l.lesson_id, l.entry_year, l.grade;

    SELECT COUNT(*)
    INTO v_candidate_invalid_count
    FROM tmp_lesson_entry_year_candidate
    WHERE entry_year IS NULL
       OR NOT REGEXP_LIKE(entry_year, '^[0-9]{4}$')
       OR CAST(entry_year AS UNSIGNED) NOT BETWEEN 1900 AND 2100;

    IF v_candidate_invalid_count > 0 THEN
        SELECT c.lesson_id, l.grade, c.entry_year, c.anchor_source
        FROM tmp_lesson_entry_year_candidate c
        INNER JOIN biz_lesson l ON l.lesson_id = c.lesson_id
        WHERE c.entry_year IS NULL
           OR NOT REGEXP_LIKE(c.entry_year, '^[0-9]{4}$')
           OR CAST(c.entry_year AS UNSIGNED) NOT BETWEEN 1900 AND 2100
        ORDER BY c.lesson_id
        LIMIT 50;
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'lesson entry_year migration aborted: some lessons cannot be anchored safely';
    END IF;

    UPDATE biz_lesson l
    INNER JOIN tmp_lesson_entry_year_candidate c ON c.lesson_id = l.lesson_id
    SET l.entry_year = c.entry_year
    WHERE l.entry_year IS NULL OR TRIM(l.entry_year) = '';
    SET v_backfilled_rows = ROW_COUNT();

    SELECT COUNT(*)
    INTO v_postflight_invalid_count
    FROM biz_lesson
    WHERE entry_year IS NULL
       OR TRIM(entry_year) = ''
       OR NOT REGEXP_LIKE(TRIM(entry_year), '^[0-9]{4}$')
       OR CAST(TRIM(entry_year) AS UNSIGNED) NOT BETWEEN 1900 AND 2100;

    IF v_postflight_invalid_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'lesson entry_year migration aborted: postflight validation failed';
    END IF;

    SELECT is_nullable
    INTO v_column_nullable
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_lesson'
      AND column_name = 'entry_year';

    IF v_column_nullable = 'YES' THEN
        ALTER TABLE biz_lesson
            MODIFY COLUMN entry_year VARCHAR(4) NOT NULL COMMENT '课程所属入学年份（稳定届别）' AFTER grade;
    END IF;

    -- 若同名索引已存在，必须确认定义正确；若已有其它同定义普通索引则不重复创建。
    SELECT COUNT(*),
           GROUP_CONCAT(column_name ORDER BY seq_in_index SEPARATOR ','),
           MIN(non_unique)
    INTO v_index_name_count, v_index_columns, v_index_non_unique
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_lesson'
      AND index_name = 'idx_biz_lesson_dept_entry_year';

    IF v_index_name_count > 0 THEN
        IF v_index_columns <> 'dept_id,entry_year' OR v_index_non_unique <> 1 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'lesson entry_year migration aborted: index name exists with an unexpected definition';
        END IF;
    ELSE
        SELECT COUNT(*)
        INTO v_exact_index_count
        FROM
        (
            SELECT index_name
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'biz_lesson'
              AND non_unique = 1
            GROUP BY index_name
            HAVING GROUP_CONCAT(column_name ORDER BY seq_in_index SEPARATOR ',') = 'dept_id,entry_year'
        ) matching_indexes;

        IF v_exact_index_count = 0 THEN
            ALTER TABLE biz_lesson
                ADD INDEX idx_biz_lesson_dept_entry_year (dept_id, entry_year);
        END IF;
    END IF;

    SELECT COUNT(*) INTO v_total_lessons FROM biz_lesson;

    SELECT v_total_lessons AS total_lessons,
           v_backfilled_rows AS backfilled_rows,
           0 AS invalid_anchor_rows,
           'PASS' AS migration_status;

    SELECT anchor_source, COUNT(*) AS lesson_count
    FROM tmp_lesson_entry_year_candidate
    GROUP BY anchor_source
    ORDER BY anchor_source;

    DROP TEMPORARY TABLE IF EXISTS tmp_lesson_entry_year_candidate;
    DROP TEMPORARY TABLE IF EXISTS tmp_lesson_entry_year_evidence;
END$$

DELIMITER ;

CALL sp_migrate_lesson_entry_year_anchor();
DROP PROCEDURE IF EXISTS sp_migrate_lesson_entry_year_anchor;

-- Postflight 1：字段必须为 varchar(4) NOT NULL。
SELECT column_name, column_type, is_nullable, column_comment
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'biz_lesson'
  AND column_name = 'entry_year';

-- Postflight 2：全表不得存在空值或非法年份。
SELECT COUNT(*) AS total_lessons,
       SUM(entry_year IS NULL OR TRIM(entry_year) = '') AS null_or_blank_rows,
       SUM(NOT REGEXP_LIKE(TRIM(entry_year), '^[0-9]{4}$')
           OR CAST(TRIM(entry_year) AS UNSIGNED) NOT BETWEEN 1900 AND 2100) AS invalid_year_rows
FROM biz_lesson;

-- Postflight 3：确认 (dept_id, entry_year) 普通联合索引存在。
SELECT index_name, non_unique,
       GROUP_CONCAT(column_name ORDER BY seq_in_index SEPARATOR ',') AS index_columns
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name = 'biz_lesson'
GROUP BY index_name, non_unique
HAVING index_columns = 'dept_id,entry_year';

-- Postflight 4：复核业务证据仍然不存在多届冲突。
SELECT COUNT(*) AS multi_cohort_conflict_lessons
FROM
(
    SELECT lesson_id
    FROM
    (
        SELECT a.lesson_id,
               CONVERT(TRIM(a.entry_year) USING utf8mb4) COLLATE utf8mb4_unicode_ci AS entry_year
        FROM biz_lesson_assignment a
        WHERE a.entry_year IS NOT NULL
        UNION ALL
        SELECT h.lesson_id,
               CONVERT(TRIM(h.entry_year) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        FROM biz_lesson_assignment_history h
        WHERE h.entry_year IS NOT NULL
        UNION ALL
        SELECT h.next_lesson_id,
               CONVERT(TRIM(h.entry_year) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        FROM biz_lesson_assignment_history h
        WHERE h.next_lesson_id IS NOT NULL AND h.entry_year IS NOT NULL
        UNION ALL
        SELECT a.lesson_id,
               CONVERT(TRIM(s.entry_year) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        FROM biz_student_answer a
        INNER JOIN biz_student s ON s.student_id = a.student_id
        WHERE s.entry_year IS NOT NULL
    ) all_evidence
    GROUP BY lesson_id
    HAVING COUNT(DISTINCT entry_year) > 1
) conflicts;

-- Postflight 5：稳定锚点必须与唯一业务证据一致。
SELECT COUNT(*) AS anchor_evidence_mismatch_lessons
FROM biz_lesson l
INNER JOIN
(
    SELECT lesson_id, MIN(entry_year) AS evidence_entry_year
    FROM
    (
        SELECT a.lesson_id,
               CONVERT(TRIM(a.entry_year) USING utf8mb4) COLLATE utf8mb4_unicode_ci AS entry_year
        FROM biz_lesson_assignment a
        WHERE a.entry_year IS NOT NULL
        UNION ALL
        SELECT h.lesson_id,
               CONVERT(TRIM(h.entry_year) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        FROM biz_lesson_assignment_history h
        WHERE h.entry_year IS NOT NULL
        UNION ALL
        SELECT h.next_lesson_id,
               CONVERT(TRIM(h.entry_year) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        FROM biz_lesson_assignment_history h
        WHERE h.next_lesson_id IS NOT NULL AND h.entry_year IS NOT NULL
        UNION ALL
        SELECT a.lesson_id,
               CONVERT(TRIM(s.entry_year) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        FROM biz_student_answer a
        INNER JOIN biz_student s ON s.student_id = a.student_id
        WHERE s.entry_year IS NOT NULL
    ) all_evidence
    GROUP BY lesson_id
    HAVING COUNT(DISTINCT entry_year) = 1
) evidence ON evidence.lesson_id = l.lesson_id
WHERE l.entry_year <> evidence.evidence_entry_year;

-- Postflight 6：按届别查看课程数量，便于与教师首页分组数量交叉核对。
SELECT entry_year, COUNT(*) AS lesson_count
FROM biz_lesson
GROUP BY entry_year
ORDER BY entry_year DESC;
