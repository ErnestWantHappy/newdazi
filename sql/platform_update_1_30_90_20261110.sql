-- 仅登记 1.30.90 批量采用并发保护，不修改课程、题库、成绩或其他业务数据。
-- 脚本幂等；仅在同版本标题一致时更新，避免覆盖其他发布。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time, update_by, update_time)
SELECT
    '1.30.90',
    'Python 批量采用并发保护',
    '补强 Python 操作题批量采用系统分的并发一致性：教师打开批改页后，如果学生在另一窗口重交作品，或其他教师已经人工改分，该学生会被安全跳过并在结果中提示，不再用页面旧的系统分覆盖新成绩。正常批次仍可一次采用；本次不改动课程、题库、既有成绩和权限规则。',
    NOW(),
    'PUBLISHED',
    'admin',
    NOW(),
    'admin',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.90') existing_version
);

UPDATE biz_platform_update
SET content = '补强 Python 操作题批量采用系统分的并发一致性：教师打开批改页后，如果学生在另一窗口重交作品，或其他教师已经人工改分，该学生会被安全跳过并在结果中提示，不再用页面旧的系统分覆盖新成绩。正常批次仍可一次采用；本次不改动课程、题库、既有成绩和权限规则。',
    status = 'PUBLISHED',
    published_at = COALESCE(published_at, NOW()),
    update_by = 'admin',
    update_time = NOW()
WHERE version_no = '1.30.90'
  AND title = 'Python 批量采用并发保护';

SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update
WHERE version_no = '1.30.90';
