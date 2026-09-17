-- 教师免抽测申请 / 课程真实使用日期 / 课程时间默认值：执行前只读检查
-- 目标库必须是本机 xueyeceping_server_20260729；保存结果并完成整库备份后才能执行正式脚本。

SELECT DATABASE() AS current_database,
       NOW() AS database_now,
       @@session.time_zone AS session_time_zone;

SELECT COUNT(*) AS lesson_count,
       SUM(create_time IS NULL) AS lesson_create_time_null_count,
       SUM(update_time IS NULL) AS lesson_update_time_null_count,
       SUM(create_time IS NULL AND update_time IS NULL) AS lesson_both_time_null_count
FROM biz_lesson;

SELECT column_name, is_nullable, column_default, extra
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'biz_lesson'
  AND column_name IN ('create_time', 'update_time')
ORDER BY ordinal_position;

SELECT COUNT(*) AS existing_exemption_table_count
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
      'biz_exemption_standard',
      'biz_exemption_application',
      'biz_exemption_class_snapshot',
      'biz_exemption_course_snapshot',
      'biz_exemption_attachment'
  );

SELECT COUNT(*) AS existing_exemption_permission_count
FROM sys_menu
WHERE perms IN (
    'business:exemption:apply',
    'business:exemption:review',
    'business:exemption:standard'
);

SELECT table_name, index_name,
       GROUP_CONCAT(column_name ORDER BY seq_in_index) AS index_columns
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name IN (
      'biz_student_answer',
      'biz_lesson_checkin',
      'biz_classroom_performance',
      'biz_lesson_assignment',
      'biz_lesson_assignment_history',
      'biz_lesson_class_scope',
      'biz_teacher_class'
  )
GROUP BY table_name, index_name
ORDER BY table_name, index_name;

SELECT COUNT(*) AS answer_duplicate_groups
FROM (
    SELECT student_id, lesson_id, question_id
    FROM biz_student_answer
    GROUP BY student_id, lesson_id, question_id
    HAVING COUNT(*) > 1
) duplicated;

SELECT COUNT(*) AS scope_duplicate_groups
FROM (
    SELECT lesson_id, dept_id, entry_year, class_code
    FROM biz_lesson_class_scope
    GROUP BY lesson_id, dept_id, entry_year, class_code
    HAVING COUNT(*) > 1
) duplicated;
