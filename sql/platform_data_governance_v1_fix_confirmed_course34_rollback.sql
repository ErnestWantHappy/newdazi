-- 课程 34 归属修复回滚：仅在确认本次备份对应版本且需要撤销时执行。
START TRANSACTION;
UPDATE biz_lesson
SET dept_id = 169
WHERE lesson_id = 34
  AND dept_id = 139;
COMMIT;
