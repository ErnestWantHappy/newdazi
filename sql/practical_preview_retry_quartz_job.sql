-- 操作题预览失败自动重试定时任务
-- 若已存在同名调用目标，则修正为每小时执行并启用。

UPDATE sys_job
SET job_name = '操作题失败文件自动重试',
    job_group = 'DEFAULT',
    cron_expression = '0 0 * * * ?',
    misfire_policy = '3',
    concurrent = '1',
    status = '0',
    update_by = 'admin',
    update_time = NOW(),
    remark = '每小时重试操作题 DOC/DOCX 预览转换失败或卡住的记录'
WHERE invoke_target = 'practicalPreviewRetryTask.retryFailedStudentAnswerPreviews';

INSERT INTO sys_job
    (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status,
     create_by, create_time, remark)
SELECT '操作题失败文件自动重试',
       'DEFAULT',
       'practicalPreviewRetryTask.retryFailedStudentAnswerPreviews',
       '0 0 * * * ?',
       '3',
       '1',
       '0',
       'admin',
       NOW(),
       '每小时重试操作题 DOC/DOCX 预览转换失败或卡住的记录'
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_job
    WHERE invoke_target = 'practicalPreviewRetryTask.retryFailedStudentAnswerPreviews'
);

SELECT job_id, job_name, invoke_target, cron_expression, status
FROM sys_job
WHERE invoke_target = 'practicalPreviewRetryTask.retryFailedStudentAnswerPreviews';
