-- 操作题预览失败重试字段补充脚本
-- 1. 增加失败重试元数据
ALTER TABLE biz_student_answer
    ADD COLUMN preview_retry_count INT DEFAULT 0 COMMENT '操作题预览重试次数' AFTER preview_path,
    ADD COLUMN preview_last_retry_time DATETIME NULL COMMENT '最近一次操作题预览重试时间' AFTER preview_retry_count,
    ADD COLUMN preview_error_message VARCHAR(255) NULL COMMENT '操作题预览失败原因' AFTER preview_last_retry_time;

-- 2. 回填历史数据默认值
UPDATE biz_student_answer
SET preview_retry_count = 0
WHERE preview_retry_count IS NULL;

UPDATE biz_student_answer
SET preview_last_retry_time = NULL
WHERE preview_last_retry_time IS NOT NULL
  AND preview_status <> 'failed';

UPDATE biz_student_answer
SET preview_error_message = NULL
WHERE preview_error_message IS NULL;

-- 3. 可选：为自动重试筛选增加辅助索引
CREATE INDEX idx_biz_student_answer_preview_retry
    ON biz_student_answer (preview_status, preview_last_retry_time, preview_retry_count);

-- 4. 系统定时任务建议配置
-- 任务名称：操作题失败文件自动重试
-- 调用目标：practicalPreviewRetryTask.retryFailedStudentAnswerPreviews
-- cron表达式：0 0 * * * ?
