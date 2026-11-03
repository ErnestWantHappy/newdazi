-- 仅重算反馈课程715、2021级4班的40份当前PPTX预览；先备份原件/PDF/页图及附件元数据。
-- 只改变派生预览状态；不改原件、答案、成绩、作品版本。旧值时间门禁防止成功后复跑再次重置。
SET NAMES utf8mb4;
START TRANSACTION;
UPDATE biz_practical_attachment att
SET normalized_status='failed', preview_status='pending',
    normalized_last_retry_time=DATE_SUB(NOW(),INTERVAL 2 HOUR),
    normalized_error_message='PPTX保存路径兼容升级，等待定向重算', update_time=NOW()
WHERE att.attachment_id IN (49728,49730,49732,49733,49737,49738,49739,49740,49741,49743,49745,49747,49749,49750,49752,49754,49755,49756,49757,49758,49759,49761,49767,49768,49769,49801,49805,49806,49807,49808,49809,49810,49811,49812,49823,49833,49837,49848,49875,49881)
  AND att.file_kind='OFFICE' AND att.file_extension='pptx'
  AND att.normalized_status='success' AND att.preview_status='success'
  AND att.normalized_retry_count<3
  AND COALESCE(att.normalized_last_retry_time,att.create_time)<'2026-10-15 19:23:34'
  AND EXISTS (SELECT 1 FROM biz_student_answer a
              JOIN biz_student s ON s.student_id=a.student_id JOIN sys_user u ON u.user_id=s.user_id
              WHERE a.practical_version_id=att.version_id AND a.lesson_id=715 AND a.question_id=4418
                AND u.dept_id=108 AND s.entry_year='2021' AND s.class_code='4');
SELECT ROW_COUNT() AS reset_count;
COMMIT;
SELECT normalized_status,preview_status,COUNT(*) FROM biz_practical_attachment
WHERE attachment_id IN (49728,49730,49732,49733,49737,49738,49739,49740,49741,49743,49745,49747,49749,49750,49752,49754,49755,49756,49757,49758,49759,49761,49767,49768,49769,49801,49805,49806,49807,49808,49809,49810,49811,49812,49823,49833,49837,49848,49875,49881) GROUP BY normalized_status,preview_status;
