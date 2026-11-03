-- 历史（legacy）50 班名单冻结。目标库先备份。
-- 背景：历史课的考勤名单和历史题单的学情分母原先按“当前行政班”实时计算，学生转班/转出后会缩水或膨胀。
-- 本脚本把登记为 legacy 的拓展班当前成员一次性写入课程名单与题单投放名单；之后代码只读快照。
-- 幂等：全部 INSERT IGNORE，可重复执行；重复执行不会改动已冻结行。
-- 执行时机：旧 50 班登记为 biz_extension_class(legacy_flag=1) 且历史课/历史题单写入后立即执行一次；
--           越晚执行，中间发生的班级调动越可能被算进历史名单。

-- 1) 历史课考勤名单：按拓展班载体（学校 + 届别 + 班级号）取当前在册学生。
INSERT IGNORE INTO biz_extension_lesson_member(lesson_id,student_id,dept_id,create_time)
SELECT l.lesson_id, s.student_id, e.dept_id, NOW()
FROM biz_lesson l
JOIN biz_extension_class e ON e.extension_class_id = l.extension_class_id
JOIN sys_user u ON u.dept_id = e.dept_id AND u.status = '0' AND u.del_flag = '0'
JOIN biz_student s ON s.user_id = u.user_id
  AND CAST(s.entry_year AS BINARY) = CAST(e.entry_year AS BINARY)
  AND CAST(s.class_code AS BINARY) = CAST(e.class_code AS BINARY)
WHERE l.lesson_scope = 'legacy';

-- 2) 历史题单投放名单：按题单版本的投放班级（plan_class）取当前在册学生。
INSERT IGNORE INTO biz_extension_plan_member(plan_version_id,student_id,dept_id,create_time)
SELECT v.plan_version_id, s.student_id, pc.dept_id, NOW()
FROM biz_python_practice_plan p
JOIN biz_python_practice_plan_version v ON v.plan_id = p.plan_id
  AND v.status = 'PUBLISHED' AND v.version_no = p.current_version_no
JOIN biz_python_practice_plan_class pc ON pc.plan_version_id = v.plan_version_id
JOIN sys_user u ON u.dept_id = pc.dept_id AND u.status = '0' AND u.del_flag = '0'
JOIN biz_student s ON s.user_id = u.user_id
  AND CAST(s.entry_year AS BINARY) = CAST(pc.entry_year AS BINARY)
  AND CAST(s.class_code AS BINARY) = CAST(pc.class_code AS BINARY)
WHERE p.plan_scope = 'legacy';
