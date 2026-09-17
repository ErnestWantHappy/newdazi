-- Python/Judge0 v2：补充学生题目说明字段。
-- 仅增加可空字段，不改变成绩、题型、权限和历史提交数据。
SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'biz_programming_question_config'
              AND column_name = 'input_description'
        ),
        'SELECT 1',
        'ALTER TABLE biz_programming_question_config
            ADD COLUMN input_description TEXT NULL COMMENT ''输入说明'' AFTER starter_code,
            ADD COLUMN output_description TEXT NULL COMMENT ''输出说明'' AFTER input_description,
            ADD COLUMN sample_explanation TEXT NULL COMMENT ''样例解释'' AFTER output_description,
            ADD COLUMN constraints_text TEXT NULL COMMENT ''限制条件'' AFTER sample_explanation,
            ADD COLUMN notes_text TEXT NULL COMMENT ''注意事项'' AFTER constraints_text'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
