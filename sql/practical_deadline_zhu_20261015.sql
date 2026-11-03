-- 仅修正丹城二小课程715、2021级4班；发布前先备份期限、审计和目标作品元数据。
-- 新规则：当前有效操作题的非空正式提交，按学生去重达到半班后起算21天。
-- 此脚本不迁移表结构，不改成绩，不自动重算其他课程或人工延期；需与新后端配套。
SET NAMES utf8mb4;
START TRANSACTION;
SET @locked_id = NULL;
SELECT deadline_id INTO @locked_id
FROM biz_practical_grading_deadline
WHERE deadline_id = 1233 AND lesson_id = 715 AND dept_id = 108
  AND entry_year = '2021' AND class_code = '4'
  AND trigger_time = '2026-09-24 14:24:09'
  AND original_deadline_time = '2026-10-15 14:24:09'
  AND current_deadline_time = '2026-10-15 14:24:09'
  AND deadline_days = 21 AND last_adjustment_type IS NULL
  AND initialization_source = 'REALTIME'
  AND NOT EXISTS (SELECT 1 FROM biz_practical_grading_deadline_audit a WHERE a.deadline_id = 1233)
FOR UPDATE;

SET @new_trigger = NULL;
WITH active_student AS (
    SELECT s.student_id
    FROM biz_student s JOIN sys_user u ON u.user_id = s.user_id
    WHERE u.dept_id = 108 AND u.del_flag = '0' AND u.status = '0'
      AND s.entry_year = '2021' AND s.class_code = '4'
), first_practical AS (
    SELECT a.student_id, MIN(a.submit_time) AS first_time
    FROM biz_student_answer a JOIN active_student s ON s.student_id = a.student_id
    JOIN biz_question q ON q.question_id = a.question_id AND q.question_type = 'practical'
    WHERE a.lesson_id = 715 AND a.student_answer IS NOT NULL AND TRIM(a.student_answer) <> ''
      AND a.submit_time IS NOT NULL
      AND EXISTS (SELECT 1 FROM biz_lesson_question lq
                  WHERE lq.lesson_id = a.lesson_id AND lq.question_id = a.question_id)
    GROUP BY a.student_id
), ranked AS (
    SELECT first_time, ROW_NUMBER() OVER (ORDER BY first_time, student_id) AS rn
    FROM first_practical
)
SELECT first_time INTO @new_trigger FROM ranked
WHERE rn = CEIL((SELECT COUNT(*) FROM active_student) / 2.0)
  AND (SELECT COUNT(*) FROM active_student) = 45;

INSERT INTO biz_practical_grading_deadline_audit
    (deadline_id, lesson_id, dept_id, entry_year, class_code, action_type,
     old_deadline_time, new_deadline_time, reason, operator_id, operator_name, operate_time)
SELECT deadline_id, lesson_id, dept_id, entry_year, class_code, 'REOPEN',
       current_deadline_time, DATE_ADD(@new_trigger, INTERVAL 21 DAY),
       '经平台负责人确认，批改起算改为半班非空操作作品提交。原打字触发2026-09-24 14:24:09，原截止2026-10-15 14:24:09；据真实操作提交校正为2026-10-15 14:18:25起算。仅修正期限，不改作品、成绩和版本。',
       0, 'system', NOW()
FROM biz_practical_grading_deadline
WHERE deadline_id = @locked_id AND @new_trigger = '2026-10-15 14:18:25';
SET @audit_rows = ROW_COUNT();

UPDATE biz_practical_grading_deadline
SET trigger_time = @new_trigger, trigger_answered_count = 23, trigger_student_count = 45,
    original_deadline_time = DATE_ADD(@new_trigger, INTERVAL 21 DAY),
    current_deadline_time = DATE_ADD(@new_trigger, INTERVAL 21 DAY),
    initialization_source = 'RULE_CORRECTION', last_adjustment_type = 'REOPEN',
    update_by = 'system', update_time = NOW()
WHERE deadline_id = @locked_id AND @audit_rows = 1
  AND current_deadline_time = '2026-10-15 14:24:09';
SET @updated_rows = ROW_COUNT();
COMMIT;
SELECT @audit_rows AS audit_rows, @updated_rows AS updated_rows, @new_trigger AS verified_trigger;
SELECT deadline_id, trigger_time, current_deadline_time, last_adjustment_type
FROM biz_practical_grading_deadline WHERE deadline_id = 1233;
