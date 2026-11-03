-- 用户要求撤销本次单份改分时执行；恢复待批并递增状态版本，避免旧页面覆盖新状态。
-- 若期间已经重批或重交，基线检查失败后停止，禁止整库回灌。
USE `ry-vue`;
SET SESSION sql_mode = 'STRICT_ALL_TABLES';
CREATE TEMPORARY TABLE score_fix_rollback_guard (ok INT NOT NULL);
START TRANSACTION;
SELECT answer_id FROM biz_student_answer WHERE answer_id=395663 FOR UPDATE;
SELECT state_id FROM biz_student_task_state WHERE state_id=436383 FOR UPDATE;
INSERT INTO score_fix_rollback_guard SELECT IF(COUNT(*)=1,1,NULL)
FROM biz_student_answer a JOIN biz_student_task_state st
ON st.student_id=a.student_id AND st.lesson_id=a.lesson_id AND st.question_id=a.question_id
WHERE a.answer_id=395663 AND a.student_id=21383 AND a.lesson_id=408 AND a.question_id=3335
AND a.score=20 AND a.practical_version_id=39706 AND a.submit_time='2026-09-30 13:52:11'
AND st.state_id=436383 AND st.task_state='GRADED' AND st.state_version=6;
UPDATE biz_student_answer SET score=NULL WHERE answer_id=395663 AND score=20 AND practical_version_id=39706;
INSERT INTO score_fix_rollback_guard VALUES (IF(ROW_COUNT()=1,1,NULL));
UPDATE biz_student_task_state SET task_state='SUBMITTED',state_version=state_version+1,changed_at=NOW(),update_time=NOW()
WHERE state_id=436383 AND task_state='GRADED' AND state_version=6;
INSERT INTO score_fix_rollback_guard VALUES (IF(ROW_COUNT()=1,1,NULL));
COMMIT;
