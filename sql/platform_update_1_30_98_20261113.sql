-- 仅登记 1.30.98 产品更新，不修改课程、成绩、权限或表结构。
-- 同版本同标题幂等更新；不同标题保留，发布脚本将拒绝冲突。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time, update_by, update_time)
SELECT
    '1.30.98',
    '扩展服务监控 129 硬件探针稳定性修复',
    '修复扩展服务监控页 129 硬件信息间歇显示“暂缺”的问题：探针改为优先展示最近一次成功数据并在后台刷新，129 经 123 跳板响应较慢时页面不再闪回缺失；首次打开页面时显示“正在探测”而不是误报暂缺；同一时间只执行一个探针进程，减少重复 SSH 与等待。',
    NOW(), 'PUBLISHED', 'admin', NOW(), 'admin', NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.98') existing_version
);

UPDATE biz_platform_update
SET content = '修复扩展服务监控页 129 硬件信息间歇显示“暂缺”的问题：探针改为优先展示最近一次成功数据并在后台刷新，129 经 123 跳板响应较慢时页面不再闪回缺失；首次打开页面时显示“正在探测”而不是误报暂缺；同一时间只执行一个探针进程，减少重复 SSH 与等待。',
    status = 'PUBLISHED',
    published_at = COALESCE(published_at, NOW()),
    update_by = 'admin', update_time = NOW()
WHERE version_no = '1.30.98'
  AND title = '扩展服务监控 129 硬件探针稳定性修复';

SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update WHERE version_no = '1.30.98';
