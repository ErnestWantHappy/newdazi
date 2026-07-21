-- 课程当前指派唯一约束脚本（幂等、非破坏性）
-- 目的：同一学校、同一入学年份、同一班级只能有一门“当前课程”。
-- 若存在重复，本脚本明确失败，不自动删除或猜测应保留的课程。

-- 1. 执行前诊断：查看重复指派班级。
SELECT dept_id, entry_year, class_code, COUNT(*) AS assignment_count,
       GROUP_CONCAT(lesson_id ORDER BY assign_time DESC, assignment_id DESC) AS lesson_ids
FROM biz_lesson_assignment
GROUP BY dept_id, entry_year, class_code
HAVING COUNT(*) > 1;

DELIMITER $$
DROP PROCEDURE IF EXISTS ensure_lesson_assignment_current_unique$$
CREATE PROCEDURE ensure_lesson_assignment_current_unique()
BEGIN
    DECLARE duplicate_count INT DEFAULT 0;
    DECLARE index_count INT DEFAULT 0;

    SELECT COUNT(*) INTO duplicate_count
    FROM (
        SELECT 1
        FROM biz_lesson_assignment
        GROUP BY dept_id, entry_year, class_code
        HAVING COUNT(*) > 1
    ) duplicated_classes;

    IF duplicate_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'biz_lesson_assignment 存在重复当前课，请人工确认后再建唯一索引';
    END IF;

    SELECT COUNT(*) INTO index_count
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'biz_lesson_assignment'
      AND INDEX_NAME = 'uk_lesson_assignment_current_class';

    IF index_count = 0 THEN
        ALTER TABLE biz_lesson_assignment
            ADD UNIQUE KEY uk_lesson_assignment_current_class (dept_id, entry_year, class_code);
    END IF;
END$$
CALL ensure_lesson_assignment_current_unique()$$
DROP PROCEDURE ensure_lesson_assignment_current_unique$$
DELIMITER ;

-- 执行后复查，应返回空结果。
SELECT dept_id, entry_year, class_code, COUNT(*) AS assignment_count
FROM biz_lesson_assignment
GROUP BY dept_id, entry_year, class_code
HAVING COUNT(*) > 1;
