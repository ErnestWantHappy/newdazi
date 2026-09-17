-- 课程与成绩监管 / 操作题限期批改：执行前只读检查
-- 说明：本脚本不修改任何数据，正式执行 teaching_supervision_v1.sql 前必须保存结果。

SELECT DATABASE() AS current_database, NOW() AS database_now, @@session.time_zone AS session_time_zone;

SELECT COUNT(*) AS lesson_count,
       SUM(dept_id IS NULL) AS lesson_missing_dept,
       SUM(creator_id IS NULL) AS lesson_missing_creator,
       SUM(update_time IS NULL) AS lesson_missing_update_time
FROM biz_lesson;

SELECT lesson_id, lesson_title, creator_id, create_by, create_time
FROM biz_lesson
WHERE dept_id IS NULL
ORDER BY lesson_id;

SELECT COUNT(*) AS answer_duplicate_groups
FROM (
    SELECT student_id, lesson_id, question_id
    FROM biz_student_answer
    GROUP BY student_id, lesson_id, question_id
    HAVING COUNT(*) > 1
) duplicate_group;

SELECT COUNT(*) AS assignment_duplicate_groups
FROM (
    SELECT lesson_id, dept_id, entry_year, class_code
    FROM biz_lesson_assignment
    GROUP BY lesson_id, dept_id, entry_year, class_code
    HAVING COUNT(*) > 1
) duplicate_group;

SELECT COUNT(*) AS assignment_lesson_dept_mismatch
FROM biz_lesson_assignment assignment
INNER JOIN biz_lesson lesson ON lesson.lesson_id = assignment.lesson_id
WHERE assignment.dept_id <> lesson.dept_id;

SELECT COUNT(*) AS existing_deadline_tables
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
      'biz_lesson_class_scope',
      'biz_practical_grading_deadline',
      'biz_practical_grading_deadline_audit'
  );
