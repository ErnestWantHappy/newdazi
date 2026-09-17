-- 操作题 AI 批改 v3：可视化阶段、心跳、安全事件与服务重启自动接续。
-- 先执行 operation_ai_grading_v1.sql、operation_ai_grading_v2.sql；本脚本可重复执行。

DROP PROCEDURE IF EXISTS migrate_operation_ai_grading_v3;
DELIMITER $$
CREATE PROCEDURE migrate_operation_ai_grading_v3()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_job' AND column_name='preparation_status') THEN
        ALTER TABLE biz_practical_ai_job ADD COLUMN preparation_status VARCHAR(24) NOT NULL DEFAULT 'PENDING'
            COMMENT '公共对照材料准备状态' AFTER starter_materials_json;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_job' AND column_name='comparison_pages_json') THEN
        ALTER TABLE biz_practical_ai_job ADD COLUMN comparison_pages_json JSON NULL
            COMMENT '已准备的对照页图路径与标签' AFTER preparation_status;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_job' AND column_name='current_result_id') THEN
        ALTER TABLE biz_practical_ai_job ADD COLUMN current_result_id BIGINT NULL
            COMMENT '当前处理结果' AFTER comparison_pages_json;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_job' AND column_name='heartbeat_time') THEN
        ALTER TABLE biz_practical_ai_job ADD COLUMN heartbeat_time DATETIME NULL
            COMMENT '最近阶段心跳' AFTER current_result_id;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_result' AND column_name='processing_stage') THEN
        ALTER TABLE biz_practical_ai_result ADD COLUMN processing_stage VARCHAR(32) NOT NULL DEFAULT 'WAITING'
            COMMENT '当前可视化处理阶段' AFTER result_status;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_result' AND column_name='processing_started_time') THEN
        ALTER TABLE biz_practical_ai_result ADD COLUMN processing_started_time DATETIME NULL AFTER processing_stage;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_result' AND column_name='stage_updated_time') THEN
        ALTER TABLE biz_practical_ai_result ADD COLUMN stage_updated_time DATETIME NULL AFTER processing_started_time;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_result' AND column_name='duration_ms') THEN
        ALTER TABLE biz_practical_ai_result ADD COLUMN duration_ms BIGINT NULL AFTER stage_updated_time;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_result' AND column_name='attempt_count') THEN
        ALTER TABLE biz_practical_ai_result ADD COLUMN attempt_count INT NOT NULL DEFAULT 0 AFTER duration_ms;
    END IF;
END$$
DELIMITER ;
CALL migrate_operation_ai_grading_v3();
DROP PROCEDURE migrate_operation_ai_grading_v3;

CREATE TABLE IF NOT EXISTS biz_practical_ai_event (
    event_id BIGINT NOT NULL AUTO_INCREMENT,
    job_id BIGINT NOT NULL,
    result_id BIGINT NULL,
    event_level VARCHAR(12) NOT NULL DEFAULT 'INFO',
    event_stage VARCHAR(32) NOT NULL,
    event_message VARCHAR(500) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (event_id),
    KEY idx_practical_ai_event_job (job_id, event_id),
    KEY idx_practical_ai_event_result (result_id, event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师可见的操作题AI任务安全事件';

-- 旧结果只补齐展示阶段；不伪造单份耗时，也不改写执行状态。
UPDATE biz_practical_ai_result
SET processing_stage=CASE result_status
    WHEN 'SUCCESS' THEN 'COMPLETED'
    WHEN 'FAILED' THEN 'FAILED'
    WHEN 'CANCELLED' THEN 'CANCELLED'
    ELSE 'WAITING' END,
    stage_updated_time=COALESCE(stage_updated_time,finish_time,create_time)
WHERE result_status IN ('SUCCESS','FAILED','CANCELLED') AND processing_stage='WAITING';

UPDATE biz_practical_ai_job
SET heartbeat_time=COALESCE(heartbeat_time,finish_time,start_time,create_time),
    preparation_status=CASE
        WHEN comparison_pages_json IS NOT NULL THEN 'READY'
        WHEN job_status IN ('COMPLETED','PARTIAL_FAILED','FAILED','CANCELLED') THEN 'LEGACY'
        ELSE preparation_status END;

INSERT INTO biz_practical_ai_event(job_id,result_id,event_level,event_stage,event_message,create_time)
SELECT job.job_id,NULL,'INFO','LEGACY_MIGRATED','该任务在处理详情功能上线前创建，逐阶段历史日志无法回溯；结果与失败原因仍可查看',NOW()
FROM biz_practical_ai_job job
WHERE NOT EXISTS (SELECT 1 FROM biz_practical_ai_event event
                  WHERE event.job_id=job.job_id AND event.event_stage='LEGACY_MIGRATED');

SELECT 'operation_ai_grading_v3' AS migration,
       (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_job' AND column_name IN
        ('preparation_status','comparison_pages_json','current_result_id','heartbeat_time')) AS job_columns,
       (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_result' AND column_name IN
        ('processing_stage','processing_started_time','stage_updated_time','duration_ms','attempt_count')) AS result_columns,
       (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_event') AS event_tables;
