-- 操作题 AI 批改 v4：允许教师显式覆盖已有正式成绩，并逐份保留覆盖前后审计。
-- 先执行 operation_ai_grading_v1～v3；本脚本只新增审计表，可重复执行。

CREATE TABLE IF NOT EXISTS biz_practical_ai_apply_audit (
    audit_id BIGINT NOT NULL AUTO_INCREMENT,
    job_id BIGINT NOT NULL COMMENT 'AI 批改任务',
    result_id BIGINT NOT NULL COMMENT '本次采用的 AI 建议',
    answer_id BIGINT NOT NULL COMMENT '被修改的正式答卷',
    practical_version_id BIGINT NOT NULL COMMENT '采用时校验的不可变提交版本',
    apply_mode VARCHAR(24) NOT NULL COMMENT 'FILL_UNGRADED 或 OVERWRITE_ALL',
    old_score INT NULL COMMENT '采用前正式总分，空表示原来未评分',
    new_score INT NOT NULL COMMENT '采用后的正式总分',
    old_scoring_details_json JSON NOT NULL COMMENT '采用前分项得分快照',
    new_scoring_details_json JSON NOT NULL COMMENT '采用后分项得分快照',
    operator_user_id BIGINT NOT NULL COMMENT '执行采用的教师或管理员',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (audit_id),
    KEY idx_ai_apply_audit_job (job_id, audit_id),
    KEY idx_ai_apply_audit_answer (answer_id, audit_id),
    KEY idx_ai_apply_audit_result (result_id, audit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI建议写入正式成绩逐份审计';

SELECT 'operation_ai_grading_v4' AS migration,
       (SELECT COUNT(*) FROM information_schema.tables
        WHERE table_schema=DATABASE() AND table_name='biz_practical_ai_apply_audit') AS audit_table,
       (SELECT COUNT(*) FROM information_schema.columns
        WHERE table_schema=DATABASE() AND table_name='biz_practical_ai_apply_audit') AS audit_columns;
