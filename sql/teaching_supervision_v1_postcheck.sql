-- 课程与成绩监管 / 操作题限期批改 v1 执行后复核
-- 本脚本只读；所有异常数量均应为 0，数量快照用于与迁移前后对比。

SELECT COUNT(*) AS scope_count,
       SUM(current_assigned = 1) AS current_scope_count
FROM biz_lesson_class_scope;

SELECT initialization_source, COUNT(*) AS deadline_count
FROM biz_practical_grading_deadline
GROUP BY initialization_source
ORDER BY initialization_source;

SELECT COUNT(*) AS duplicate_scope_group_count
FROM (
    SELECT lesson_id, dept_id, entry_year, class_code
    FROM biz_lesson_class_scope
    GROUP BY lesson_id, dept_id, entry_year, class_code
    HAVING COUNT(*) > 1
) duplicated;

SELECT COUNT(*) AS duplicate_deadline_group_count
FROM (
    SELECT lesson_id, dept_id, entry_year, class_code
    FROM biz_practical_grading_deadline
    GROUP BY lesson_id, dept_id, entry_year, class_code
    HAVING COUNT(*) > 1
) duplicated;

SELECT COUNT(*) AS zero_denominator_deadline_count
FROM biz_practical_grading_deadline
WHERE trigger_student_count <= 0;

SELECT COUNT(*) AS non_practical_deadline_count
FROM biz_practical_grading_deadline deadline
WHERE NOT EXISTS (
    SELECT 1
    FROM biz_lesson_question lesson_question
    INNER JOIN biz_question question
            ON question.question_id = lesson_question.question_id
    WHERE lesson_question.lesson_id = deadline.lesson_id
      AND question.question_type = 'practical'
);

SELECT COUNT(*) AS orphan_scope_count
FROM biz_lesson_class_scope scope
LEFT JOIN biz_lesson lesson
       ON lesson.lesson_id = scope.lesson_id
      AND lesson.dept_id = scope.dept_id
WHERE lesson.lesson_id IS NULL;

SELECT COUNT(*) AS researcher_permission_count
FROM sys_role role
INNER JOIN sys_role_menu role_menu ON role_menu.role_id = role.role_id
INNER JOIN sys_menu menu ON menu.menu_id = role_menu.menu_id
WHERE role.role_key = 'researcher'
  AND (
      menu.menu_id = 2046
      OR menu.perms IN (
          'business:teachingSupervision:export',
          'business:practicalDeadline:config',
          'business:practicalDeadline:adjust'
      )
  );

SELECT COUNT(*) AS enabled_compensation_job_count
FROM sys_job
WHERE invoke_target = 'practicalGradingDeadlineTask.reconcileTriggers'
  AND status = '0'
  AND concurrent = '1';

-- 历史异常只列清单，不由本迁移自动改写。
SELECT lesson.lesson_id, lesson.lesson_title, lesson.dept_id, lesson.creator_id
FROM biz_lesson lesson
WHERE lesson.dept_id IS NULL OR lesson.creator_id IS NULL
ORDER BY lesson.lesson_id;

SELECT assignment.assignment_id, assignment.lesson_id,
       lesson.dept_id AS lesson_dept_id,
       assignment.dept_id AS assignment_dept_id,
       assignment.entry_year, assignment.class_code
FROM biz_lesson_assignment assignment
INNER JOIN biz_lesson lesson ON lesson.lesson_id = assignment.lesson_id
WHERE lesson.dept_id <> assignment.dept_id
ORDER BY assignment.assignment_id;
