-- 课程当前指派去重与唯一约束脚本
-- 目的：同一学校、同一入学年份、同一班级只能有一门“当前课程”。

-- 1. 执行前诊断：查看重复指派班级。
SELECT dept_id, entry_year, class_code, COUNT(*) AS assignment_count,
       GROUP_CONCAT(lesson_id ORDER BY assign_time DESC, assignment_id DESC) AS lesson_ids
FROM biz_lesson_assignment
GROUP BY dept_id, entry_year, class_code
HAVING COUNT(*) > 1;

-- 2. 清理重复指派：保留最新 assign_time / assignment_id 的记录。
DELETE la
FROM biz_lesson_assignment la
JOIN (
    SELECT assignment_id
    FROM (
        SELECT assignment_id,
               ROW_NUMBER() OVER (
                   PARTITION BY dept_id, entry_year, class_code
                   ORDER BY assign_time DESC, assignment_id DESC
               ) AS rn
        FROM biz_lesson_assignment
    ) ranked
    WHERE ranked.rn > 1
) duplicate_rows ON duplicate_rows.assignment_id = la.assignment_id;

-- 3. 建立唯一约束，防止后续再次出现同班级多当前课程。
ALTER TABLE biz_lesson_assignment
    ADD UNIQUE KEY uk_lesson_assignment_current_class (dept_id, entry_year, class_code);

-- 4. 执行后复查，应返回空结果。
SELECT dept_id, entry_year, class_code, COUNT(*) AS assignment_count
FROM biz_lesson_assignment
GROUP BY dept_id, entry_year, class_code
HAVING COUNT(*) > 1;
