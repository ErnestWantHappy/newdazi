-- Python 系统题库 V1 只读审计。
-- 仅包含 SELECT，可在本机开发库和正式库重复执行。
SET NAMES utf8mb4;

SELECT 'SUMMARY' AS section_name,
       (SELECT COUNT(*) FROM biz_question WHERE create_by = 'python-system-v1') AS v1_questions,
       (SELECT COUNT(*) FROM biz_programming_question_config c JOIN biz_question q ON q.question_id = c.question_id WHERE q.create_by = 'python-system-v1') AS configs,
       (SELECT COUNT(*) FROM biz_programming_test_case c JOIN biz_question q ON q.question_id = c.question_id WHERE q.create_by = 'python-system-v1') AS test_cases,
       (SELECT COUNT(*) FROM biz_lesson_question lq JOIN biz_question q ON q.question_id = lq.question_id WHERE q.create_by = 'python-system-v1') AS lesson_links,
       (SELECT COUNT(*) FROM biz_python_practice_plan_question pq JOIN biz_question q ON q.question_id = pq.question_id WHERE q.create_by = 'python-system-v1') AS plan_links,
       (SELECT COUNT(*) FROM biz_python_practice_extension_question eq JOIN biz_question q ON q.question_id = eq.question_id WHERE q.create_by = 'python-system-v1') AS extension_links,
       (SELECT COUNT(*) FROM biz_python_practice_question_snapshot s JOIN biz_question q ON q.question_id = s.question_id WHERE q.create_by = 'python-system-v1') AS snapshots,
       (SELECT COUNT(*) FROM biz_python_practice_draft d JOIN biz_question q ON q.question_id = d.question_id WHERE q.create_by = 'python-system-v1') AS practice_drafts,
       (SELECT COUNT(*) FROM biz_python_practice_submission s JOIN biz_question q ON q.question_id = s.question_id WHERE q.create_by = 'python-system-v1') AS practice_submissions,
       (SELECT COUNT(*) FROM biz_python_practice_progress p JOIN biz_question q ON q.question_id = p.question_id WHERE q.create_by = 'python-system-v1') AS practice_progress,
       (SELECT COUNT(*) FROM biz_programming_submission s JOIN biz_question q ON q.question_id = s.question_id WHERE q.create_by = 'python-system-v1') AS course_submissions,
       (SELECT COUNT(*) FROM biz_student_answer a JOIN biz_question q ON q.question_id = a.question_id WHERE q.create_by = 'python-system-v1') AS student_answers;

SELECT 'LESSON_LINK' AS section_name,
       q.question_id,
       l.lesson_id,
       l.lesson_title,
       l.grade,
       l.semester,
       l.lesson_num,
       COUNT(DISTINCT ps.submission_id) AS course_submission_count,
       COUNT(DISTINCT a.answer_id) AS student_answer_count
FROM biz_question q
JOIN biz_lesson_question lq ON lq.question_id = q.question_id
JOIN biz_lesson l ON l.lesson_id = lq.lesson_id
LEFT JOIN biz_programming_submission ps ON ps.question_id = q.question_id AND ps.lesson_id = l.lesson_id
LEFT JOIN biz_student_answer a ON a.question_id = q.question_id AND a.lesson_id = l.lesson_id
WHERE q.create_by = 'python-system-v1'
GROUP BY q.question_id, l.lesson_id, l.lesson_title, l.grade, l.semester, l.lesson_num
ORDER BY l.lesson_id, q.question_id;

SELECT 'PLAN_LINK' AS section_name,
       q.question_id,
       p.plan_id,
       p.plan_name,
       p.status AS plan_status,
       v.plan_version_id,
       v.version_no,
       v.status AS version_status,
       pq.sort_no
FROM biz_question q
JOIN biz_python_practice_plan_question pq ON pq.question_id = q.question_id
JOIN biz_python_practice_plan_version v ON v.plan_version_id = pq.plan_version_id
JOIN biz_python_practice_plan p ON p.plan_id = v.plan_id
WHERE q.create_by = 'python-system-v1'
ORDER BY p.plan_id, v.version_no, pq.sort_no;

SELECT 'USED_QUESTION' AS section_name,
       q.question_id,
       REPLACE(REPLACE(q.question_content, CHAR(10), ' '), CHAR(13), ' ') AS question_content,
       COUNT(DISTINCT lq.id) AS lesson_link_count,
       COUNT(DISTINCT pq.plan_question_id) AS plan_link_count,
       COUNT(DISTINCT eq.extension_question_id) AS extension_link_count,
       COUNT(DISTINCT ps.submission_id) AS course_submission_count,
       COUNT(DISTINCT a.answer_id) AS student_answer_count,
       COUNT(DISTINCT s.submission_id) AS practice_submission_count,
       COUNT(DISTINCT pr.progress_id) AS practice_progress_count
FROM biz_question q
LEFT JOIN biz_lesson_question lq ON lq.question_id = q.question_id
LEFT JOIN biz_python_practice_plan_question pq ON pq.question_id = q.question_id
LEFT JOIN biz_python_practice_extension_question eq ON eq.question_id = q.question_id
LEFT JOIN biz_programming_submission ps ON ps.question_id = q.question_id
LEFT JOIN biz_student_answer a ON a.question_id = q.question_id
LEFT JOIN biz_python_practice_submission s ON s.question_id = q.question_id
LEFT JOIN biz_python_practice_progress pr ON pr.question_id = q.question_id
WHERE q.create_by = 'python-system-v1'
GROUP BY q.question_id, q.question_content
HAVING lesson_link_count + plan_link_count + extension_link_count + course_submission_count
       + student_answer_count + practice_submission_count + practice_progress_count > 0
ORDER BY q.question_id;

SELECT 'QUALITY' AS section_name,
       COUNT(*) AS question_count,
       COUNT(DISTINCT q.question_content) AS distinct_question_content,
       COUNT(DISTINCT c.starter_code) AS distinct_starter_code,
       COUNT(DISTINCT c.input_description) AS distinct_input_description,
       COUNT(DISTINCT c.output_description) AS distinct_output_description,
       COUNT(DISTINCT c.sample_explanation) AS distinct_sample_explanation,
       SUM(q.is_public NOT IN ('Y', 'N')) AS invalid_question_public_value,
       SUM(q.grade IS NOT NULL OR q.semester IS NOT NULL OR q.lesson_num IS NOT NULL) AS course_metadata_count,
       SUM(q.question_content LIKE '%请根据输入说明编写程序并输出结果%') AS generic_statement_count
FROM biz_question q
JOIN biz_programming_question_config c ON c.question_id = q.question_id
WHERE q.create_by = 'python-system-v1';

SELECT 'CASE_QUALITY' AS section_name,
       COUNT(*) AS test_case_count,
       SUM(c.is_public = '1') AS public_case_count,
       SUM(c.is_public = '0') AS hidden_case_count,
       SUM(c.expected_output LIKE '%?%') AS expected_output_question_mark_count,
       SUM(c.input_text LIKE '%?%') AS input_question_mark_count,
       COUNT(DISTINCT CONCAT(COALESCE(c.input_text, ''), CHAR(0), COALESCE(c.expected_output, ''))) AS distinct_input_output_pairs,
       MIN(per_question.case_count) AS min_cases_per_question,
       MAX(per_question.case_count) AS max_cases_per_question
FROM biz_programming_test_case c
JOIN biz_question q ON q.question_id = c.question_id
JOIN (
    SELECT tc.question_id, COUNT(*) AS case_count
    FROM biz_programming_test_case tc
    JOIN biz_question tq ON tq.question_id = tc.question_id
    WHERE tq.create_by = 'python-system-v1'
    GROUP BY tc.question_id
) per_question ON per_question.question_id = c.question_id
WHERE q.create_by = 'python-system-v1';

SELECT 'BAD_CASE' AS section_name,
       q.question_id,
       c.test_case_id,
       c.case_name,
       c.is_public,
       REPLACE(REPLACE(COALESCE(c.input_text, ''), CHAR(10), '\\n'), CHAR(13), '') AS input_text,
       REPLACE(REPLACE(COALESCE(c.expected_output, ''), CHAR(10), '\\n'), CHAR(13), '') AS expected_output,
       HEX(COALESCE(c.input_text, '')) AS input_hex,
       HEX(COALESCE(c.expected_output, '')) AS expected_output_hex
FROM biz_programming_test_case c
JOIN biz_question q ON q.question_id = c.question_id
WHERE q.create_by = 'python-system-v1'
  AND (c.input_text LIKE '%?%' OR c.expected_output LIKE '%?%')
ORDER BY q.question_id, c.order_num, c.test_case_id;

SELECT 'CURRENT_ASSIGNMENT' AS section_name,
       a.assignment_id,
       a.lesson_id,
       a.dept_id,
       a.entry_year,
       a.class_code,
       a.assign_time
FROM biz_lesson_assignment a
WHERE EXISTS (
    SELECT 1
    FROM biz_lesson_question lq
    JOIN biz_question q ON q.question_id = lq.question_id
    WHERE lq.lesson_id = a.lesson_id
      AND q.create_by = 'python-system-v1'
)
ORDER BY a.lesson_id, a.dept_id, a.entry_year, a.class_code;

SELECT 'CLASS_SCOPE' AS section_name,
       s.scope_id,
       s.lesson_id,
       s.dept_id,
       s.entry_year,
       s.class_code,
       s.current_assigned,
       s.evidence_source,
       s.last_assigned_time
FROM biz_lesson_class_scope s
WHERE EXISTS (
    SELECT 1
    FROM biz_lesson_question lq
    JOIN biz_question q ON q.question_id = lq.question_id
    WHERE lq.lesson_id = s.lesson_id
      AND q.create_by = 'python-system-v1'
)
ORDER BY s.lesson_id, s.dept_id, s.entry_year, s.class_code;

SELECT 'ASSIGNMENT_HISTORY' AS section_name,
       h.history_id,
       h.lesson_id,
       h.next_lesson_id,
       h.dept_id,
       h.entry_year,
       h.class_code,
       h.advance_source,
       h.assigned_time,
       h.advanced_time
FROM biz_lesson_assignment_history h
WHERE EXISTS (
    SELECT 1
    FROM biz_lesson_question lq
    JOIN biz_question q ON q.question_id = lq.question_id
    WHERE lq.lesson_id = h.lesson_id
      AND q.create_by = 'python-system-v1'
)
ORDER BY h.lesson_id, h.history_id;

SELECT 'TABLE_CANDIDATE' AS section_name, table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND (
      table_name LIKE '%lesson%class%'
      OR table_name LIKE '%current%lesson%'
      OR table_name LIKE '%lesson%assign%'
  )
ORDER BY table_name;
