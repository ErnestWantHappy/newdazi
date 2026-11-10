-- 仅登记 1.30.92 Python批改教师确认制与运行查看，不修改课程、题库、成绩或权限。
-- 脚本幂等；只在同版本标题一致时更新，避免覆盖其他发布。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time, update_by, update_time)
SELECT
    '1.30.92',
    'Python 批改教师确认制与运行查看',
    'Python 操作题批改增加运行查看：老师可选择学生最高分提交，输入测试数据后直接查看程序输出；运行仅作评分参考，不生成判题记录、不影响成绩。批改页开放五星和数字两种打分方式。系统判题分改为仅供参考，学生提交后不再自动写正式成绩，老师五星、数字打分或批量采用系统分后才计入成绩，避免0个用例通过被误显示为已批0分。',
    NOW(),
    'PUBLISHED',
    'admin',
    NOW(),
    'admin',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.92') existing_version
);

UPDATE biz_platform_update
SET content = 'Python 操作题批改增加运行查看：老师可选择学生最高分提交，输入测试数据后直接查看程序输出；运行仅作评分参考，不生成判题记录、不影响成绩。批改页开放五星和数字两种打分方式。系统判题分改为仅供参考，学生提交后不再自动写正式成绩，老师五星、数字打分或批量采用系统分后才计入成绩，避免0个用例通过被误显示为已批0分。',
    status = 'PUBLISHED',
    published_at = COALESCE(published_at, NOW()),
    update_by = 'admin',
    update_time = NOW()
WHERE version_no = '1.30.92'
  AND title = 'Python 批改教师确认制与运行查看';

SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update
WHERE version_no = '1.30.92';
