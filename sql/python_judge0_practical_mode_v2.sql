-- Python/Judge0 v2：Python 是操作题的作答方式，不新增第四类成绩。
-- 执行前：对目标库完成备份；确认以下查询中的 python 数量与关联配置数量。
-- SELECT question_id, question_type FROM biz_question WHERE question_type = 'python';
-- SELECT COUNT(*) AS programming_config_count FROM biz_programming_question_config;

SET @python_judge0_add_mode_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'biz_question'
              AND column_name = 'practical_mode'
        ),
        'SELECT 1',
        'ALTER TABLE biz_question ADD COLUMN practical_mode VARCHAR(16) NOT NULL DEFAULT ''FILE'' COMMENT ''操作题作答方式：FILE 文件作品，PYTHON 在线 Python 编程'' AFTER practical_allowed_extensions'
    )
);
PREPARE python_judge0_add_mode_stmt FROM @python_judge0_add_mode_sql;
EXECUTE python_judge0_add_mode_stmt;
DEALLOCATE PREPARE python_judge0_add_mode_stmt;

-- 历史文件操作题显式标记 FILE；历史 Python 题改为操作题并保留其 Judge0 配置、测试点和提交历史。
UPDATE biz_question
SET practical_mode = 'FILE'
WHERE question_type = 'practical' AND (practical_mode IS NULL OR practical_mode = '');

UPDATE biz_question
SET question_type = 'practical', practical_mode = 'PYTHON'
WHERE question_type = 'python';

-- 停用旧的第四题型选项，避免教师再创建 question_type=python。
UPDATE sys_dict_data
SET status = '1', update_by = 'system', update_time = NOW(), remark = '已迁移为操作题的 Python 在线编程作答方式'
WHERE dict_type = 'biz_question_type' AND dict_value = 'python' AND status <> '1';

-- 执行后复核：python 题型必须为 0；Python 配置必须对应操作题/PYTHON；普通操作题必须有 FILE。
-- SELECT COUNT(*) AS legacy_python_count FROM biz_question WHERE question_type = 'python';
-- SELECT q.question_id FROM biz_programming_question_config c LEFT JOIN biz_question q ON q.question_id = c.question_id WHERE q.question_id IS NULL OR q.question_type <> 'practical' OR q.practical_mode <> 'PYTHON';
-- SELECT COUNT(*) AS practical_without_mode FROM biz_question WHERE question_type = 'practical' AND (practical_mode IS NULL OR practical_mode = '');
