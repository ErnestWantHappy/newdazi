-- 成绩班级选择弹窗统计查询优化索引
-- 执行前请先用 SHOW INDEX 确认线上是否已有同名或同列索引，避免重复建索引。

ALTER TABLE `biz_student_answer`
ADD INDEX `idx_bsa_lesson_student_question_answer` (`lesson_id`, `student_id`, `question_id`, `answer_id`);

ALTER TABLE `biz_student`
ADD INDEX `idx_bs_year_class_user` (`entry_year`, `class_code`, `user_id`);

ALTER TABLE `biz_lesson_assignment`
ADD INDEX `idx_bla_lesson_dept_year_class` (`lesson_id`, `dept_id`, `entry_year`, `class_code`);

ALTER TABLE `biz_classroom_performance`
ADD INDEX `idx_bcp_lesson_dept_student_absent` (`lesson_id`, `dept_id`, `student_id`, `is_absent`);
