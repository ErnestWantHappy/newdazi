-- 用户授权的单份作业纠正：徐翎轩，赖鑫卢老师，1.计算机网络，20/30分。
-- 仅用于130正式ry-vue；执行前必须完成数据库备份。任何基线不符均报错并回滚。
USE `ry-vue`;
SET SESSION sql_mode = 'STRICT_ALL_TABLES';
CREATE TEMPORARY TABLE score_fix_guard (ok INT NOT NULL);
START TRANSACTION;
SELECT answer_id FROM biz_student_answer WHERE answer_id=395663 FOR UPDATE;
SELECT state_id FROM biz_student_task_state WHERE state_id=436383 FOR UPDATE;
INSERT INTO score_fix_guard SELECT IF(COUNT(*)=1,1,NULL)
FROM biz_student_answer a
JOIN biz_student s ON s.student_id=a.student_id
JOIN sys_user u ON u.user_id=s.user_id
JOIN biz_lesson l ON l.lesson_id=a.lesson_id
JOIN biz_lesson_question lq ON lq.lesson_id=a.lesson_id AND lq.question_id=a.question_id
JOIN biz_student_task_state st ON st.student_id=a.student_id AND st.lesson_id=a.lesson_id AND st.question_id=a.question_id
WHERE a.answer_id=395663 AND a.student_id=21383 AND a.lesson_id=408 AND a.question_id=3335
AND a.score IS NULL AND a.practical_version_id=39706 AND a.submit_time='2026-09-30 13:52:11'
AND u.user_name='2026721615' AND u.nick_name='徐翎轩' AND u.dept_id=169 AND u.del_flag='0'
AND s.entry_year='2026' AND s.class_code='16' AND l.creator_id=10582 AND l.dept_id=169
AND lq.question_score=30 AND st.state_id=436383 AND st.task_state='SUBMITTED' AND st.state_version=5;
INSERT INTO score_fix_guard SELECT IF(COUNT(*)=0,1,NULL) FROM biz_scoring_detail WHERE answer_id=395663;
UPDATE biz_student_answer SET score=20 WHERE answer_id=395663 AND score IS NULL AND practical_version_id=39706;
INSERT INTO score_fix_guard VALUES (IF(ROW_COUNT()=1,1,NULL));
UPDATE biz_student_task_state SET task_state='GRADED',state_version=state_version+1,changed_at=NOW(),update_time=NOW()
WHERE state_id=436383 AND task_state='SUBMITTED' AND state_version=5;
INSERT INTO score_fix_guard VALUES (IF(ROW_COUNT()=1,1,NULL));
COMMIT;
SELECT 'score_fix_committed',answer_id,score,practical_version_id FROM biz_student_answer WHERE answer_id=395663;
