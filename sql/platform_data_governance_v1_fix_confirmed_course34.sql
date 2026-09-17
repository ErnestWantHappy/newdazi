-- 课程 34 历史归属修复：仅适用于已完成逐条人工确认的本机基线库。
-- 证据：课程创建者、4 条指派记录及指派人均属于 dept_id=139；课程标题为小学三年级内容。
-- 执行前必须完成相关表备份和前检；脚本不处理课程 1、区域抽测或时间缺失记录。
START TRANSACTION;

UPDATE biz_lesson
SET dept_id = 139
WHERE lesson_id = 34
  AND dept_id = 169
  AND EXISTS (
      SELECT 1
      FROM biz_lesson_assignment a
      WHERE a.lesson_id = 34
      GROUP BY a.lesson_id
      HAVING COUNT(*) = 4
         AND SUM(a.dept_id = 139) = 4
  );

-- 只有恰好完成预期映射时才提交，避免误伤其它课程。
SET @lesson34_changed := ROW_COUNT();
SELECT @lesson34_changed AS changed_rows;
SELECT lesson_id, dept_id
FROM biz_lesson
WHERE lesson_id = 34;
SELECT assignment_id, lesson_id, dept_id
FROM biz_lesson_assignment
WHERE lesson_id = 34
ORDER BY assignment_id;

COMMIT;
