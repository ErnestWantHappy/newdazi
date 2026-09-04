-- 课程入学年份锚定迁移前只读审计（MySQL 8）
-- 必须在执行 lesson_entry_year_anchor.sql 前运行并人工核对结果；本脚本不修改数据。

-- Preflight 1：列出非法届别证据，结果必须为空。
SELECT evidence.lesson_id, evidence.source_name, evidence.entry_year
FROM
(
    SELECT a.lesson_id, 'current_assignment' AS source_name,
           CONVERT(TRIM(a.entry_year) USING utf8mb4) COLLATE utf8mb4_unicode_ci AS entry_year
    FROM biz_lesson_assignment a
    WHERE a.entry_year IS NOT NULL
    UNION ALL
    SELECT h.lesson_id, 'assignment_history' AS source_name,
           CONVERT(TRIM(h.entry_year) USING utf8mb4) COLLATE utf8mb4_unicode_ci
    FROM biz_lesson_assignment_history h
    WHERE h.entry_year IS NOT NULL
    UNION ALL
    SELECT h.next_lesson_id, 'assignment_history_next' AS source_name,
           CONVERT(TRIM(h.entry_year) USING utf8mb4) COLLATE utf8mb4_unicode_ci
    FROM biz_lesson_assignment_history h
    WHERE h.next_lesson_id IS NOT NULL AND h.entry_year IS NOT NULL
    UNION ALL
    SELECT a.lesson_id, 'student_answer' AS source_name,
           CONVERT(TRIM(s.entry_year) USING utf8mb4) COLLATE utf8mb4_unicode_ci
    FROM biz_student_answer a
    INNER JOIN biz_student s ON s.student_id = a.student_id
    WHERE s.entry_year IS NOT NULL
) evidence
WHERE evidence.entry_year = ''
   OR NOT REGEXP_LIKE(evidence.entry_year, '^[0-9]{4}$')
   OR CAST(evidence.entry_year AS UNSIGNED) NOT BETWEEN 1900 AND 2100
ORDER BY evidence.lesson_id, evidence.source_name;

-- Preflight 2：列出多届冲突课程，结果必须为空。
SELECT evidence.lesson_id,
       GROUP_CONCAT(DISTINCT CONCAT(evidence.source_name, ':', evidence.entry_year)
                    ORDER BY evidence.source_name, evidence.entry_year SEPARATOR ', ') AS conflicting_evidence
FROM
(
    SELECT a.lesson_id, 'current_assignment' AS source_name,
           CONVERT(TRIM(a.entry_year) USING utf8mb4) COLLATE utf8mb4_unicode_ci AS entry_year
    FROM biz_lesson_assignment a
    WHERE a.entry_year IS NOT NULL
    UNION ALL
    SELECT h.lesson_id, 'assignment_history' AS source_name,
           CONVERT(TRIM(h.entry_year) USING utf8mb4) COLLATE utf8mb4_unicode_ci
    FROM biz_lesson_assignment_history h
    WHERE h.entry_year IS NOT NULL
    UNION ALL
    SELECT h.next_lesson_id, 'assignment_history_next' AS source_name,
           CONVERT(TRIM(h.entry_year) USING utf8mb4) COLLATE utf8mb4_unicode_ci
    FROM biz_lesson_assignment_history h
    WHERE h.next_lesson_id IS NOT NULL AND h.entry_year IS NOT NULL
    UNION ALL
    SELECT a.lesson_id, 'student_answer' AS source_name,
           CONVERT(TRIM(s.entry_year) USING utf8mb4) COLLATE utf8mb4_unicode_ci
    FROM biz_student_answer a
    INNER JOIN biz_student s ON s.student_id = a.student_id
    WHERE s.entry_year IS NOT NULL
) evidence
GROUP BY evidence.lesson_id
HAVING COUNT(DISTINCT evidence.entry_year) > 1
ORDER BY evidence.lesson_id;

-- Preflight 3：逐条人工审核无业务证据、将使用 2025 学年 grade 基线推断的课程。
-- 需核对 lesson_id、标题、静态 grade、学校、创建人与创建时间；确认无误后才能执行迁移。
SELECT l.lesson_id,
       l.lesson_title,
       l.grade,
       l.dept_id,
       l.create_by,
       l.create_time,
       CASE
           WHEN l.grade BETWEEN 1 AND 6 THEN CAST(2025 - l.grade + 1 AS CHAR)
           WHEN l.grade BETWEEN 7 AND 9 THEN CAST(2025 - (l.grade - 6) + 1 AS CHAR)
           WHEN l.grade BETWEEN 10 AND 12 THEN CAST(2025 - (l.grade - 9) + 1 AS CHAR)
           ELSE NULL
       END AS fallback_entry_year,
       'fallback_2025_grade' AS anchor_source
FROM biz_lesson l
WHERE NOT EXISTS (SELECT 1 FROM biz_lesson_assignment a WHERE a.lesson_id = l.lesson_id)
  AND NOT EXISTS
      (
          SELECT 1
          FROM biz_lesson_assignment_history h
          WHERE h.lesson_id = l.lesson_id OR h.next_lesson_id = l.lesson_id
      )
  AND NOT EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.lesson_id = l.lesson_id)
ORDER BY l.lesson_id;
