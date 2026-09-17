-- Python 在线编程与文件操作题可在同一课程共存；本脚本只读校验。
-- 前三项发布前与发布后均必须为 0；最后一项用于审计历史孤儿关联，必须人工确认范围。
SELECT COUNT(*) AS practical_mode_missing
FROM biz_question
WHERE question_type = 'practical'
  AND (practical_mode IS NULL OR practical_mode NOT IN ('FILE', 'PYTHON'));

SELECT COUNT(*) AS python_config_missing
FROM biz_question q
LEFT JOIN biz_programming_question_config c ON c.question_id = q.question_id AND c.enabled = '1'
WHERE q.question_type = 'practical'
  AND q.practical_mode = 'PYTHON'
  AND c.question_id IS NULL;

SELECT q.question_id
FROM biz_question q
LEFT JOIN biz_programming_test_case tc ON tc.question_id = q.question_id
WHERE q.question_type = 'practical'
  AND q.practical_mode = 'PYTHON'
GROUP BY q.question_id
HAVING COUNT(tc.test_case_id) = 0 OR SUM(tc.is_public = '0') = 0;

SELECT lq.lesson_id, lq.question_id AS orphan_question_id
FROM biz_lesson_question lq
LEFT JOIN biz_question q ON q.question_id = lq.question_id
WHERE q.question_id IS NULL;
