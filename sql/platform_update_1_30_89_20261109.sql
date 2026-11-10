-- 仅登记 1.30.89 窄屏批改页修复，不修改题库、课程或成绩。
-- 脚本幂等；只在同版本标题一致时更新，避免覆盖其他发布。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time, update_by, update_time)
SELECT
    '1.30.89',
    'Python 批改页窄屏适配',
    '修复手机窄屏打开操作题批改页时，学生名单、代码预览和打分区被强行横向压缩的问题。窄屏现在按“选择学生 → 查看作品 → 打分”纵向排列，Python 系统判题信息改为上下排列，代码与评分按钮不再被挤成窄条。桌面端原有三栏批改布局保持不变，本次不改动课程、题库、成绩和权限规则。',
    NOW(),
    'PUBLISHED',
    'admin',
    NOW(),
    'admin',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.89') existing_version
);

UPDATE biz_platform_update
SET content = '修复手机窄屏打开操作题批改页时，学生名单、代码预览和打分区被强行横向压缩的问题。窄屏现在按“选择学生 → 查看作品 → 打分”纵向排列，Python 系统判题信息改为上下排列，代码与评分按钮不再被挤成窄条。桌面端原有三栏批改布局保持不变，本次不改动课程、题库、成绩和权限规则。',
    status = 'PUBLISHED',
    published_at = COALESCE(published_at, NOW()),
    update_by = 'admin',
    update_time = NOW()
WHERE version_no = '1.30.89'
  AND title = 'Python 批改页窄屏适配';

SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update
WHERE version_no = '1.30.89';
