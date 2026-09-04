-- 教师免抽测申请 / 课程真实使用日期 / 课程时间默认值执行后复核
-- 本脚本只读；重复、孤儿、非法状态和权限异常数量均应为0。

SELECT COUNT(*) AS exemption_table_count
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
      'biz_exemption_standard',
      'biz_exemption_application',
      'biz_exemption_class_snapshot',
      'biz_exemption_course_snapshot',
      'biz_exemption_attachment'
  );

SELECT column_name, is_nullable, column_default, extra
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'biz_lesson'
  AND column_name IN ('create_time', 'update_time')
ORDER BY ordinal_position;

SELECT COUNT(*) AS lesson_count,
       SUM(create_time IS NULL) AS lesson_create_time_null_count,
       SUM(update_time IS NULL) AS lesson_update_time_null_count,
       SUM(create_time IS NULL AND update_time IS NULL) AS lesson_both_time_null_count
FROM biz_lesson;

SELECT COUNT(*) AS duplicate_standard_group_count
FROM (
    SELECT academic_year, semester, grade
    FROM biz_exemption_standard
    GROUP BY academic_year, semester, grade
    HAVING COUNT(*) > 1
) duplicated;

SELECT COUNT(*) AS duplicate_application_group_count
FROM (
    SELECT dept_id, teacher_id, academic_year, semester, grade
    FROM biz_exemption_application
    GROUP BY dept_id, teacher_id, academic_year, semester, grade
    HAVING COUNT(*) > 1
) duplicated;

SELECT COUNT(*) AS invalid_application_status_count
FROM biz_exemption_application
WHERE status NOT IN ('PENDING', 'PASS', 'FAIL');

SELECT COUNT(*) AS orphan_class_snapshot_count
FROM biz_exemption_class_snapshot snapshot
LEFT JOIN biz_exemption_application application
       ON application.application_id = snapshot.application_id
WHERE application.application_id IS NULL;

SELECT COUNT(*) AS orphan_course_snapshot_count
FROM biz_exemption_course_snapshot course
LEFT JOIN biz_exemption_application application
       ON application.application_id = course.application_id
LEFT JOIN biz_exemption_class_snapshot class_snapshot
       ON class_snapshot.class_snapshot_id = course.class_snapshot_id
WHERE application.application_id IS NULL
   OR class_snapshot.class_snapshot_id IS NULL;

SELECT COUNT(*) AS orphan_attachment_count
FROM biz_exemption_attachment attachment
LEFT JOIN biz_exemption_application application
       ON application.application_id = attachment.application_id
WHERE application.application_id IS NULL;

SELECT COUNT(*) AS inconsistent_application_summary_count
FROM biz_exemption_application application
LEFT JOIN (
    SELECT application_id,
           COUNT(*) AS class_count,
           MIN(usage_qualified) AS all_classes_qualified,
           SUM(practical_due_count) AS practical_due_count,
           SUM(practical_graded_count) AS practical_graded_count
    FROM biz_exemption_class_snapshot
    GROUP BY application_id
) snapshot ON snapshot.application_id = application.application_id
WHERE application.class_count <> IFNULL(snapshot.class_count, 0)
   OR application.all_classes_qualified <> IFNULL(snapshot.all_classes_qualified, 0)
   OR application.practical_due_count <> IFNULL(snapshot.practical_due_count, 0)
   OR application.practical_graded_count <> IFNULL(snapshot.practical_graded_count, 0);

SELECT role.role_key, menu.perms, COUNT(*) AS grant_count
FROM sys_role role
INNER JOIN sys_role_menu role_menu ON role_menu.role_id = role.role_id
INNER JOIN sys_menu menu ON menu.menu_id = role_menu.menu_id
WHERE (role.role_key = 'teacher' AND menu.perms = 'business:exemption:apply')
   OR (role.role_key = 'researcher'
       AND menu.perms IN ('business:exemption:review', 'business:exemption:standard'))
GROUP BY role.role_key, menu.perms
ORDER BY role.role_key, menu.perms;

SELECT COUNT(*) AS student_exemption_permission_count
FROM sys_role role
INNER JOIN sys_role_menu role_menu ON role_menu.role_id = role.role_id
INNER JOIN sys_menu menu ON menu.menu_id = role_menu.menu_id
WHERE role.role_key = 'student'
  AND menu.perms IN (
      'business:exemption:apply',
      'business:exemption:review',
      'business:exemption:standard'
  );
