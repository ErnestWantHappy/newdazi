-- 仅在应用已回滚到不读取 biz_student_task_state 的旧版本后执行。
-- 本表不保存成绩或作品正文，但会丢失“进入/作答中/退回”状态历史。
DROP TABLE IF EXISTS biz_student_task_state;
