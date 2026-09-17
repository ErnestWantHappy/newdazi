-- 操作题 AI 用量与费用估算 v1。
-- 单价单位统一为“元/千 token”；金额仅为理论估算，实际以阿里云账单为准。
-- 执行前必须完成目标库备份。本脚本可重复执行，重复执行不会覆盖管理员已调整的价格。

CREATE TABLE IF NOT EXISTS biz_ai_model_price
(
    provider_code       VARCHAR(32)   NOT NULL COMMENT '模型厂商',
    model_name          VARCHAR(80)   NOT NULL COMMENT '请求模型名',
    display_name        VARCHAR(100)  NOT NULL COMMENT '界面名称',
    input_price_per_1k  DECIMAL(12,6) NOT NULL COMMENT '输入价格，元/千token',
    output_price_per_1k DECIMAL(12,6) NOT NULL COMMENT '输出价格，元/千token',
    price_status        VARCHAR(24)   NOT NULL DEFAULT 'REFERENCE' COMMENT 'REFERENCE/TO_CONFIRM/CONFIRMED',
    price_note          VARCHAR(255)  NULL COMMENT '价格来源与提示',
    update_by           VARCHAR(64)   NULL,
    create_time         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (provider_code, model_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型按量计费参考价';

INSERT IGNORE INTO biz_ai_model_price
    (provider_code, model_name, display_name, input_price_per_1k,
     output_price_per_1k, price_status, price_note, update_by)
VALUES
    ('QWEN', 'qwen-vl-max', 'Qwen-VL-Max', 0.003000, 0.009000,
     'REFERENCE', '历史公开参考价，以阿里云官网为准', 'migration'),
    ('QWEN', 'qwen-vl-plus', 'Qwen-VL-Plus', 0.001500, 0.004500,
     'REFERENCE', '历史公开参考价，以阿里云官网为准', 'migration'),
    ('QWEN', 'qwen-plus', 'Qwen-Plus', 0.000800, 0.002000,
     'REFERENCE', '历史公开参考价，以阿里云官网为准', 'migration'),
    ('QWEN', 'qwen3.7-plus', 'Qwen3.7-Plus', 0.003000, 0.009000,
     'TO_CONFIRM', '按 Qwen-VL-Max 档暂估，模型名称与正式单价待确认', 'migration'),
    ('QWEN', 'qwen3.6-flash', 'Qwen3.6-Flash', 0.001500, 0.004500,
     'TO_CONFIRM', '按 Qwen-VL-Plus 档暂估，模型名称与正式单价待确认', 'migration');

DROP PROCEDURE IF EXISTS migrate_operation_ai_usage_pricing_v1;
DELIMITER $$
CREATE PROCEDURE migrate_operation_ai_usage_pricing_v1()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_job' AND column_name='input_price_per_1k') THEN
        ALTER TABLE biz_practical_ai_job ADD COLUMN input_price_per_1k DECIMAL(12,6) NULL
            COMMENT '任务创建时输入参考价，元/千token' AFTER model_name;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_job' AND column_name='output_price_per_1k') THEN
        ALTER TABLE biz_practical_ai_job ADD COLUMN output_price_per_1k DECIMAL(12,6) NULL
            COMMENT '任务创建时输出参考价，元/千token' AFTER input_price_per_1k;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_job' AND column_name='price_status') THEN
        ALTER TABLE biz_practical_ai_job ADD COLUMN price_status VARCHAR(24) NULL
            COMMENT '任务价格快照状态' AFTER output_price_per_1k;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_job' AND column_name='price_note') THEN
        ALTER TABLE biz_practical_ai_job ADD COLUMN price_note VARCHAR(255) NULL
            COMMENT '任务价格快照说明' AFTER price_status;
    END IF;
END$$
DELIMITER ;

CALL migrate_operation_ai_usage_pricing_v1();
DROP PROCEDURE migrate_operation_ai_usage_pricing_v1;

SELECT (SELECT COUNT(*) FROM biz_ai_model_price WHERE provider_code='QWEN') AS qwen_price_rows,
       (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE()
        AND table_name='biz_practical_ai_job'
        AND column_name IN ('input_price_per_1k','output_price_per_1k','price_status','price_note')) AS job_price_columns;
