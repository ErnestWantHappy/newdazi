-- 仅登记 1.30.91 课例配套材料包说明精简，不修改课程、题库、成绩或权限。
-- 脚本幂等；只在同版本标题一致时更新，避免覆盖其他发布。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time, update_by, update_time)
SELECT
    '1.30.91',
    '课例配套材料包说明精简',
    '精简顾意丽《返回成语现场》配套材料包的教师使用说明：不再罗列哈希、完整目录和内部程序清单，只说明包内有什么、如何启动以及教学设计、课件在资源列表中的位置。后续课例导入也按同一简短口径生成说明，技术检查明细仍保留在后台报告中。本次不改课程、题库、成绩和权限规则。',
    NOW(),
    'PUBLISHED',
    'admin',
    NOW(),
    'admin',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.91') existing_version
);

UPDATE biz_platform_update
SET content = '精简顾意丽《返回成语现场》配套材料包的教师使用说明：不再罗列哈希、完整目录和内部程序清单，只说明包内有什么、如何启动以及教学设计、课件在资源列表中的位置。后续课例导入也按同一简短口径生成说明，技术检查明细仍保留在后台报告中。本次不改课程、题库、成绩和权限规则。',
    status = 'PUBLISHED',
    published_at = COALESCE(published_at, NOW()),
    update_by = 'admin',
    update_time = NOW()
WHERE version_no = '1.30.91'
  AND title = '课例配套材料包说明精简';

SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update
WHERE version_no = '1.30.91';
