-- 修复 Python 系统题导入时被错误字符集替换为 ? 的起始代码。
-- 同步修复已创建题单的快照；只处理系统题，且仅覆盖仍含 ? 的记录。
SET NAMES utf8mb4;

UPDATE biz_programming_question_config c
JOIN biz_question q ON q.question_id = c.question_id
SET c.starter_code = '# 请根据题目要求编写程序\n',
    c.update_by = 'python-system-v1',
    c.update_time = NOW()
WHERE q.create_by = 'python-system-v1'
  AND q.question_id BETWEEN 1755 AND 1834
  AND c.starter_code LIKE '%?%';

UPDATE biz_python_practice_question_snapshot s
JOIN biz_question q ON q.question_id = s.question_id
SET s.starter_code = '# 请根据题目要求编写程序\n'
WHERE q.create_by = 'python-system-v1'
  AND q.question_id BETWEEN 1755 AND 1834
  AND s.starter_code LIKE '%?%';

SELECT
  (SELECT COUNT(*)
   FROM biz_programming_question_config c
   JOIN biz_question q ON q.question_id = c.question_id
   WHERE q.create_by = 'python-system-v1'
     AND q.question_id BETWEEN 1755 AND 1834
     AND c.starter_code LIKE '%?%') AS remaining_config_with_question_mark,
  (SELECT COUNT(*)
   FROM biz_python_practice_question_snapshot s
   JOIN biz_question q ON q.question_id = s.question_id
   WHERE q.create_by = 'python-system-v1'
     AND q.question_id BETWEEN 1755 AND 1834
     AND s.starter_code LIKE '%?%') AS remaining_snapshot_with_question_mark;
