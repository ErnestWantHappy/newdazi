-- 130：朱仁敏老师课程714及990576、2022级2班，71份当前PPTX派生预览重算。
-- 前提：20261020_ppt_class2_remaining_v1 完整库/原件/PDF/页图备份已校验；不改原件、成绩、提交时间或版本指针。
-- 旧更新时间门禁保证本次成功后再次执行为0行。
SET NAMES utf8mb4;
START TRANSACTION;
UPDATE biz_practical_attachment att
SET normalized_status='failed', preview_status='pending',
 normalized_last_retry_time=DATE_SUB(NOW(),INTERVAL 2 HOUR),
 normalized_error_message='PPTX旧预览定向重算20261020',update_time=NOW()
WHERE att.attachment_id IN (38086,38087,38092,38095,38097,38101,38102,38103,38104,38106,38107,38108,38109,38111,38113,38114,38115,38116,38117,38118,38119,38120,38121,38123,38125,38130,38131,38134,38135,38141,38142,38146,38147,38148,45343,45344,45345,45346,45347,45348,45349,45350,45351,45352,45353,45355,45356,45357,45358,45359,45360,45361,45362,45363,45364,45365,45366,45367,45368,45369,45370,45371,45372,45373,45375,45376,45377,45379,45380,45381,45382)
 AND att.file_kind='OFFICE' AND att.file_extension='pptx'
 AND att.normalized_status='success' AND att.preview_status='success'
 AND att.normalized_retry_count<3 AND att.update_time<'2026-10-15 19:23:34'
 AND EXISTS (SELECT 1 FROM biz_student_answer a JOIN biz_student s ON s.student_id=a.student_id JOIN sys_user u ON u.user_id=s.user_id
 WHERE a.practical_version_id=att.version_id AND ((a.lesson_id=714 AND a.question_id=4416) OR (a.lesson_id=990576 AND a.question_id=990705))
 AND u.dept_id=108 AND s.entry_year='2022' AND s.class_code='2');
SELECT ROW_COUNT() AS reset_count;
COMMIT;
