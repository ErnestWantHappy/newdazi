-- 仅登记 1.30.97 产品更新，不修改课程、成绩、权限或表结构。
-- 同版本同标题幂等更新；不同标题保留，发布脚本将拒绝冲突。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time, update_by, update_time)
SELECT
    '1.30.97',
    '诊断中心升级与平台概览提速',
    '平台概览统计改为后台自动刷新并显示数据生成时间，教研员打开概览不再长时间等待；系统诊断中心完成改版，内网环境下复制诊断报告、复制慢 SQL 等按钮恢复正常；诊断时间线新增来源标注，便于区分后台刷新与用户请求；修复仪表盘超长小数等显示细节。',
    NOW(), 'PUBLISHED', 'admin', NOW(), 'admin', NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.97') existing_version
);

UPDATE biz_platform_update
SET content = '平台概览统计改为后台自动刷新并显示数据生成时间，教研员打开概览不再长时间等待；系统诊断中心完成改版，内网环境下复制诊断报告、复制慢 SQL 等按钮恢复正常；诊断时间线新增来源标注，便于区分后台刷新与用户请求；修复仪表盘超长小数等显示细节。',
    status = 'PUBLISHED',
    published_at = COALESCE(published_at, NOW()),
    update_by = 'admin', update_time = NOW()
WHERE version_no = '1.30.97'
  AND title = '诊断中心升级与平台概览提速';

SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update WHERE version_no = '1.30.97';
