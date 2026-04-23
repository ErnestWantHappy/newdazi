-- 打字题重复提交修复脚本
-- 执行顺序：先备份 -> 清理重复 -> 补唯一索引

-- 1. 备份当前答题表（请按需要修改备份表名）
CREATE TABLE biz_student_answer_backup_20260409 AS
SELECT *
FROM biz_student_answer;

-- 2. 删除重复记录：同一学生、同一课程、同一题目只保留 answer_id 最大的一条
DELETE a
FROM biz_student_answer a
INNER JOIN biz_student_answer b
    ON a.student_id = b.student_id
   AND a.lesson_id = b.lesson_id
   AND a.question_id = b.question_id
   AND a.answer_id < b.answer_id;

-- 3. 为后续提交增加唯一约束
ALTER TABLE biz_student_answer
ADD UNIQUE KEY uk_student_lesson_question (student_id, lesson_id, question_id);
