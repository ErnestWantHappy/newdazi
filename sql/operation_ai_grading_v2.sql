-- 操作题 AI 批改 v2：范围确认、任务恢复、教师参考答案快照与安全批量采用。
-- 先执行 operation_ai_grading_v1.sql；本脚本可重复执行。

CREATE TABLE IF NOT EXISTS biz_teacher_practical_reference_answer (
    reference_id BIGINT NOT NULL AUTO_INCREMENT,
    teacher_user_id BIGINT NOT NULL COMMENT '上传教师',
    dept_id BIGINT NOT NULL COMMENT '学校',
    lesson_id BIGINT NOT NULL COMMENT '课程',
    question_id BIGINT NOT NULL COMMENT '操作题',
    original_file_name VARCHAR(255) NOT NULL,
    resource_path VARCHAR(500) NOT NULL,
    file_extension VARCHAR(16) NOT NULL,
    mime_type VARCHAR(120) NULL,
    file_size BIGINT NOT NULL,
    sha256 CHAR(64) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (reference_id),
    UNIQUE KEY uk_teacher_practical_reference_scope (teacher_user_id, dept_id, lesson_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师课程级操作题AI参考答案';

DROP PROCEDURE IF EXISTS migrate_operation_ai_grading_v2;
DELIMITER $$
CREATE PROCEDURE migrate_operation_ai_grading_v2()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_job' AND column_name='scope_mode') THEN
        ALTER TABLE biz_practical_ai_job ADD COLUMN scope_mode VARCHAR(24) NULL COMMENT 'UNGRADED_ONLY或ALL_SUBMITTED' AFTER prompt_version;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_job' AND column_name='reference_answer_json') THEN
        ALTER TABLE biz_practical_ai_job ADD COLUMN reference_answer_json JSON NULL COMMENT '本任务教师参考答案不可变快照' AFTER scope_mode;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_job' AND column_name='starter_materials_json') THEN
        ALTER TABLE biz_practical_ai_job ADD COLUMN starter_materials_json JSON NULL COMMENT '本任务空白起始材料不可变快照' AFTER reference_answer_json;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_result' AND column_name='apply_status') THEN
        ALTER TABLE biz_practical_ai_result ADD COLUMN apply_status VARCHAR(32) NOT NULL DEFAULT 'NOT_APPLIED' COMMENT '建议采用状态' AFTER error_message;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_result' AND column_name='applied_by_user_id') THEN
        ALTER TABLE biz_practical_ai_result ADD COLUMN applied_by_user_id BIGINT NULL AFTER apply_status;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_result' AND column_name='applied_time') THEN
        ALTER TABLE biz_practical_ai_result ADD COLUMN applied_time DATETIME NULL AFTER applied_by_user_id;
    END IF;
END$$
DELIMITER ;
CALL migrate_operation_ai_grading_v2();
DROP PROCEDURE migrate_operation_ai_grading_v2;

SELECT 'operation_ai_grading_v2' AS migration,
       (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_job' AND column_name IN ('scope_mode','reference_answer_json','starter_materials_json')) AS job_columns,
       (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_result' AND column_name IN ('apply_status','applied_by_user_id','applied_time')) AS result_columns;
