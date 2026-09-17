-- Python OJ 化 v1：补齐题目元数据、题单快照和独立刷题逐测试点结果。
-- 本脚本不修改或删除 python-system-v1 题目；正式执行前仍须按发布规范整库备份。

DROP PROCEDURE IF EXISTS add_python_oj_column;
DELIMITER $$
CREATE PROCEDURE add_python_oj_column(
    IN target_table VARCHAR(64),
    IN target_column VARCHAR(64),
    IN column_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = target_table
          AND column_name = target_column
    ) THEN
        SET @python_oj_alter_sql = CONCAT(
            'ALTER TABLE `', REPLACE(target_table, '`', '``'),
            '` ADD COLUMN `', REPLACE(target_column, '`', '``'), '` ', column_definition
        );
        PREPARE python_oj_alter_stmt FROM @python_oj_alter_sql;
        EXECUTE python_oj_alter_stmt;
        DEALLOCATE PREPARE python_oj_alter_stmt;
    END IF;
END$$
DELIMITER ;

CALL add_python_oj_column('biz_programming_question_config', 'external_id',
    'VARCHAR(32) NULL COMMENT ''系统题稳定外部题号，教师自建题为空'' AFTER `language_code`');
CALL add_python_oj_column('biz_programming_question_config', 'title',
    'VARCHAR(255) NULL COMMENT ''Python OJ 列表标题'' AFTER `language_code`');
CALL add_python_oj_column('biz_programming_question_config', 'knowledge_points',
    'VARCHAR(500) NULL COMMENT ''逗号分隔的知识点标签'' AFTER `title`');
CALL add_python_oj_column('biz_programming_question_config', 'no_input',
    'CHAR(1) NOT NULL DEFAULT ''0'' COMMENT ''1为无标准输入题'' AFTER `knowledge_points`');
CALL add_python_oj_column('biz_programming_question_config', 'validation_status',
    'VARCHAR(16) NOT NULL DEFAULT ''DRAFT'' COMMENT ''DRAFT/VALIDATING/VALID/INVALID'' AFTER `no_input`');
CALL add_python_oj_column('biz_programming_question_config', 'validated_at',
    'DATETIME NULL COMMENT ''参考代码全用例验证时间'' AFTER `validation_status`');
CALL add_python_oj_column('biz_programming_question_config', 'validated_by',
    'VARCHAR(64) NULL COMMENT ''最近验证人'' AFTER `validated_at`');
CALL add_python_oj_column('biz_programming_question_config', 'content_version',
    'INT NOT NULL DEFAULT 1 COMMENT ''题面或判题内容版本'' AFTER `validated_by`');

DROP PROCEDURE IF EXISTS add_python_oj_index;
DELIMITER $$
CREATE PROCEDURE add_python_oj_index()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'biz_programming_question_config'
          AND index_name = 'uk_programming_config_external_id'
    ) THEN
        ALTER TABLE biz_programming_question_config
            ADD UNIQUE KEY uk_programming_config_external_id (external_id);
    END IF;
END$$
DELIMITER ;
CALL add_python_oj_index();
DROP PROCEDURE IF EXISTS add_python_oj_index;

CALL add_python_oj_column('biz_python_practice_question_snapshot', 'question_title',
    'VARCHAR(255) NULL COMMENT ''发布时题目标题快照'' AFTER `snapshot_hash`');
CALL add_python_oj_column('biz_python_practice_question_snapshot', 'difficulty',
    'VARCHAR(32) NULL COMMENT ''发布时难度快照'' AFTER `question_title`');
CALL add_python_oj_column('biz_python_practice_question_snapshot', 'knowledge_points',
    'VARCHAR(500) NULL COMMENT ''发布时知识点快照'' AFTER `difficulty`');
CALL add_python_oj_column('biz_python_practice_question_snapshot', 'no_input',
    'CHAR(1) NOT NULL DEFAULT ''0'' COMMENT ''发布时无输入标记'' AFTER `knowledge_points`');

CALL add_python_oj_column('biz_python_practice_submission', 'custom_input',
    'MEDIUMTEXT NULL COMMENT ''CUSTOM_RUN 的学生自定义输入'' AFTER `source_code`');

DROP PROCEDURE IF EXISTS add_python_oj_column;

CREATE TABLE IF NOT EXISTS biz_python_practice_submission_case (
    submission_case_id BIGINT NOT NULL AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    snapshot_case_id BIGINT NULL COMMENT 'CUSTOM_RUN 时为空',
    case_name VARCHAR(128) NOT NULL DEFAULT '',
    is_public CHAR(1) NOT NULL DEFAULT '0',
    status_code VARCHAR(32) NOT NULL,
    judge0_status_id INT NULL,
    time_seconds DECIMAL(10,3) NULL,
    memory_kb INT NULL,
    output_text MEDIUMTEXT NULL COMMENT '仅公开样例或自定义运行保存程序输出',
    error_summary VARCHAR(1000) NULL COMMENT '隐藏点只保存脱敏错误摘要',
    order_num INT NOT NULL DEFAULT 0,
    PRIMARY KEY (submission_case_id),
    UNIQUE KEY uk_python_practice_submission_case_order (submission_id, order_num),
    KEY idx_python_practice_submission_case_submission (submission_id, is_public, order_num)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Python刷题逐测试点判题结果';

SELECT
    (SELECT COUNT(*) FROM information_schema.columns
      WHERE table_schema=DATABASE() AND table_name='biz_programming_question_config'
        AND column_name IN ('external_id','title','knowledge_points','no_input','validation_status','validated_at','validated_by','content_version')) AS config_columns,
    (SELECT COUNT(*) FROM information_schema.statistics
      WHERE table_schema=DATABASE() AND table_name='biz_programming_question_config'
        AND index_name='uk_programming_config_external_id') AS external_id_indexes,
    (SELECT COUNT(*) FROM information_schema.columns
      WHERE table_schema=DATABASE() AND table_name='biz_python_practice_question_snapshot'
        AND column_name IN ('question_title','difficulty','knowledge_points','no_input')) AS snapshot_columns,
    (SELECT COUNT(*) FROM information_schema.columns
      WHERE table_schema=DATABASE() AND table_name='biz_python_practice_submission' AND column_name='custom_input') AS custom_input_columns,
    (SELECT COUNT(*) FROM information_schema.tables
      WHERE table_schema=DATABASE() AND table_name='biz_python_practice_submission_case') AS submission_case_tables;
