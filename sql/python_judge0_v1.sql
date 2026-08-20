-- Python 在线编程与 Judge0 CE v1：仅增加编程题扩展，不创建新的用户、课程或成绩主表。
-- v2 起 Python 改为操作题的作答方式，见 python_judge0_practical_mode_v2.sql。

CREATE TABLE IF NOT EXISTS biz_programming_question_config (
    question_id BIGINT NOT NULL COMMENT 'biz_question.question_id，v2 起题型为 practical 且 practical_mode=PYTHON',
    language_code VARCHAR(32) NOT NULL DEFAULT 'python' COMMENT '第一阶段固定 python',
    starter_code MEDIUMTEXT NULL COMMENT '学生初始代码',
    time_limit_seconds DECIMAL(5,2) NOT NULL DEFAULT 2.00 COMMENT '单测试点 CPU 时限',
    memory_limit_kb INT NOT NULL DEFAULT 131072 COMMENT '单测试点内存上限',
    max_processes SMALLINT NOT NULL DEFAULT 8 COMMENT '进程/线程上限',
    max_file_size_kb INT NOT NULL DEFAULT 1024 COMMENT '输出和临时文件之外的文件上限',
    max_output_kb INT NOT NULL DEFAULT 64 COMMENT '标准输出上限',
    enabled CHAR(1) NOT NULL DEFAULT '1' COMMENT '1可判题，0保留题目但暂停提交',
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NULL,
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NULL,
    PRIMARY KEY (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Python 编程题资源与判题限制配置';

-- 已存在的第一版表可能仍是默认 10；只修正默认值，不改动已有题目配置。
SET @python_judge0_fix_default_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'biz_programming_question_config'
              AND column_name = 'max_processes'
              AND COALESCE(column_default, '') <> '8'
        ),
        'ALTER TABLE biz_programming_question_config ALTER COLUMN max_processes SET DEFAULT 8',
        'SELECT 1'
    )
);
PREPARE python_judge0_fix_default_stmt FROM @python_judge0_fix_default_sql;
EXECUTE python_judge0_fix_default_stmt;
DEALLOCATE PREPARE python_judge0_fix_default_stmt;

CREATE TABLE IF NOT EXISTS biz_programming_test_case (
    test_case_id BIGINT NOT NULL AUTO_INCREMENT,
    question_id BIGINT NOT NULL,
    case_name VARCHAR(128) NOT NULL DEFAULT '',
    input_text MEDIUMTEXT NULL,
    expected_output MEDIUMTEXT NOT NULL,
    is_public CHAR(1) NOT NULL DEFAULT '0' COMMENT '1公开测试点，0隐藏测试点',
    score_weight DECIMAL(10,2) NOT NULL DEFAULT 1.00,
    order_num INT NOT NULL DEFAULT 0,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NULL,
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NULL,
    PRIMARY KEY (test_case_id),
    UNIQUE KEY uk_programming_case_order (question_id, order_num),
    KEY idx_programming_case_question (question_id, is_public, order_num)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Python 编程题公开和隐藏测试点';

CREATE TABLE IF NOT EXISTS biz_programming_draft (
    draft_id BIGINT NOT NULL AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    source_code MEDIUMTEXT NOT NULL,
    update_time DATETIME NOT NULL,
    PRIMARY KEY (draft_id),
    UNIQUE KEY uk_programming_draft_scope (student_id, lesson_id, question_id),
    KEY idx_programming_draft_lesson (lesson_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生 Python 代码草稿';

CREATE TABLE IF NOT EXISTS biz_programming_submission (
    submission_id BIGINT NOT NULL AUTO_INCREMENT,
    submission_key VARCHAR(64) NOT NULL COMMENT '客户端幂等键',
    student_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    source_code MEDIUMTEXT NOT NULL,
    submission_kind VARCHAR(16) NOT NULL COMMENT 'RUN 或 SUBMIT',
    status_code VARCHAR(32) NOT NULL COMMENT 'WAITING/JUDGING/ACCEPTED/PARTIAL/WRONG_ANSWER/SYNTAX_ERROR/RUNTIME_ERROR/TIME_LIMIT/MEMORY_LIMIT/SERVICE_ERROR/CANCELLED',
    status_message VARCHAR(255) NOT NULL DEFAULT '',
    score INT NULL COMMENT '仅正式提交终态写入的本题得分',
    passed_case_count INT NOT NULL DEFAULT 0,
    total_case_count INT NOT NULL DEFAULT 0,
    judge0_token VARCHAR(64) NULL,
    judge0_status_id INT NULL,
    time_seconds DECIMAL(10,3) NULL,
    memory_kb INT NULL,
    error_summary VARCHAR(1000) NULL COMMENT '不保存隐藏测试输入/输出',
    request_ip VARCHAR(128) NULL,
    submitted_at DATETIME NOT NULL,
    judged_at DATETIME NULL,
    cancelled_at DATETIME NULL,
    PRIMARY KEY (submission_id),
    UNIQUE KEY uk_programming_submission_key (student_id, lesson_id, question_id, submission_key),
    KEY idx_programming_submission_scope (student_id, lesson_id, question_id, submission_id DESC),
    KEY idx_programming_submission_recovery (status_code, submitted_at),
    KEY idx_programming_submission_class (lesson_id, question_id, status_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Python 运行与正式提交历史';

CREATE TABLE IF NOT EXISTS biz_programming_submission_case (
    submission_case_id BIGINT NOT NULL AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    test_case_id BIGINT NOT NULL,
    is_public CHAR(1) NOT NULL DEFAULT '0',
    status_code VARCHAR(32) NOT NULL,
    judge0_status_id INT NULL,
    time_seconds DECIMAL(10,3) NULL,
    memory_kb INT NULL,
    output_text MEDIUMTEXT NULL COMMENT '仅公开测试点可保存输出',
    error_summary VARCHAR(1000) NULL COMMENT '隐藏点不保存输入、期望输出或程序输出',
    PRIMARY KEY (submission_case_id),
    UNIQUE KEY uk_programming_submission_case (submission_id, test_case_id),
    KEY idx_programming_submission_case_submission (submission_id, is_public)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Python 单测试点判题历史';

-- 只新增 Python 枚举，不修改旧题型数据；菜单复用现有题库管理权限。
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, 'Python编程题', 'python', 'biz_question_type', '', 'primary', 'N', '0', 'admin', NOW(), 'Judge0 CE 自动判题'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_data WHERE dict_type = 'biz_question_type' AND dict_value = 'python'
);
