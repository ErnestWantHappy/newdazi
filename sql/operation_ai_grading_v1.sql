-- 普通课程操作题 AI 建议批改 v1
-- AI 结果只保存为建议，不直接覆盖 biz_student_answer.score。

CREATE TABLE IF NOT EXISTS biz_teacher_ai_config (
    teacher_user_id BIGINT NOT NULL COMMENT '教师用户ID',
    provider_code VARCHAR(32) NOT NULL DEFAULT 'QWEN' COMMENT '模型厂商',
    model_name VARCHAR(80) NOT NULL DEFAULT 'qwen3.7-plus' COMMENT '模型名称',
    endpoint_url VARCHAR(255) NOT NULL COMMENT 'OpenAI兼容接口地址',
    api_key_ciphertext TEXT NOT NULL COMMENT 'AES-GCM密文，禁止返回前端',
    api_key_hint VARCHAR(16) NOT NULL COMMENT '仅展示末四位',
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (teacher_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师个人AI批改配置';

ALTER TABLE biz_teacher_ai_config
    MODIFY COLUMN model_name VARCHAR(80) NOT NULL DEFAULT 'qwen3.7-plus' COMMENT '模型名称';

CREATE TABLE IF NOT EXISTS biz_practical_ai_job (
    job_id BIGINT NOT NULL AUTO_INCREMENT,
    teacher_user_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    entry_year VARCHAR(16) NOT NULL,
    class_code VARCHAR(32) NOT NULL,
    provider_code VARCHAR(32) NOT NULL,
    model_name VARCHAR(80) NOT NULL,
    prompt_version VARCHAR(32) NOT NULL,
    job_status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    total_count INT NOT NULL DEFAULT 0,
    success_count INT NOT NULL DEFAULT 0,
    failed_count INT NOT NULL DEFAULT 0,
    skipped_count INT NOT NULL DEFAULT 0,
    error_message VARCHAR(500) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    start_time DATETIME NULL,
    finish_time DATETIME NULL,
    PRIMARY KEY (job_id),
    KEY idx_practical_ai_job_teacher (teacher_user_id, create_time),
    KEY idx_practical_ai_job_scope (lesson_id, question_id, entry_year, class_code, create_time),
    KEY idx_practical_ai_job_status (job_status, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作题批量AI建议任务';

CREATE TABLE IF NOT EXISTS biz_practical_ai_result (
    result_id BIGINT NOT NULL AUTO_INCREMENT,
    job_id BIGINT NOT NULL,
    answer_id BIGINT NOT NULL,
    practical_version_id BIGINT NOT NULL,
    rubric_snapshot_id BIGINT NOT NULL,
    result_status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    suggested_score INT NULL,
    scoring_details_json JSON NULL,
    evidence_json JSON NULL,
    confidence DECIMAL(5,4) NULL,
    provider_request_id VARCHAR(100) NULL,
    prompt_tokens INT NULL,
    completion_tokens INT NULL,
    error_message VARCHAR(500) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finish_time DATETIME NULL,
    PRIMARY KEY (result_id),
    UNIQUE KEY uk_practical_ai_job_answer (job_id, answer_id),
    KEY idx_practical_ai_result_version (practical_version_id, create_time),
    KEY idx_practical_ai_result_answer (answer_id, create_time),
    KEY idx_practical_ai_result_status (job_id, result_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作题AI评分建议，人工确认前不进入正式成绩';
