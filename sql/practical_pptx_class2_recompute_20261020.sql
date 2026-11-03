-- 130：朱仁敏老师课程990614、2022级2班，37份当前PPTX派生预览重算。
-- 前提：20261020_ppt_class2_recompute_v1 完整库/原件/PDF/页图备份已校验；不改原件、成绩、提交时间或版本指针。
-- 旧更新时间门禁保证本次成功后再次执行为0行。
SET NAMES utf8mb4;
START TRANSACTION;
UPDATE biz_practical_attachment att
SET normalized_status='failed', preview_status='pending',
 normalized_last_retry_time=DATE_SUB(NOW(),INTERVAL 2 HOUR),
 normalized_error_message='PPTX旧预览定向重算20261020',update_time=NOW()
WHERE att.attachment_id IN (48473,48474,48475,48476,48477,48478,48479,48480,48481,48482,48484,48485,48486,48487,48488,48489,48490,48491,48495,48496,48497,48498,48499,48500,48501,48502,48503,48504,48505,48506,48507,48508,48509,48510,48511,48512,48513)
 AND att.file_kind='OFFICE' AND att.file_extension='pptx'
 AND att.normalized_status='success' AND att.preview_status='success'
 AND att.normalized_retry_count<3 AND att.update_time<'2026-10-15 19:23:34'
 AND EXISTS (SELECT 1 FROM biz_student_answer a JOIN biz_student s ON s.student_id=a.student_id JOIN sys_user u ON u.user_id=s.user_id
 WHERE a.practical_version_id=att.version_id AND a.lesson_id=990614 AND a.question_id=990831
 AND u.dept_id=108 AND s.entry_year='2022' AND s.class_code='2');
SELECT ROW_COUNT() AS reset_count;
COMMIT;
