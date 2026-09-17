-- 课程 Python 自定义输入运行：仅保存调试输入，不计分、不写课程答案。
-- 可重复执行；正式环境必须先备份，再执行并按末尾查询后检。

DROP PROCEDURE IF EXISTS add_course_python_custom_input;
DELIMITER $$
CREATE PROCEDURE add_course_python_custom_input()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'biz_programming_submission'
          AND COLUMN_NAME = 'custom_input'
    ) THEN
        ALTER TABLE biz_programming_submission
            ADD COLUMN custom_input MEDIUMTEXT NULL
            COMMENT 'CUSTOM_RUN 的学生自定义输入'
            AFTER source_code;
    END IF;
END$$
DELIMITER ;

CALL add_course_python_custom_input();
DROP PROCEDURE IF EXISTS add_course_python_custom_input;

SELECT COUNT(*) AS custom_input_column_count
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'biz_programming_submission'
  AND COLUMN_NAME = 'custom_input';
