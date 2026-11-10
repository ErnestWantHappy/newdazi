-- 仅登记 1.30.88 产品更新，不修改课程、题库、成绩或其他业务数据。
-- 先确认 1.30.88 未被其他发布占用；脚本可幂等复跑，但不会改写同版本的其他标题。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time, update_by, update_time)
SELECT
    '1.30.88',
    'Python 操作题批改与课例来源修复',
    'Python 操作题批改支持批量采用系统判题最高分：教师可一次完成整班已提交学生的系统分采用，遇到并发重交、退回、档案缺失或分值异常时跳过并在结果中提示人数。批改页展示的代码、判题时间、用时和通过用例严格来自同一条最高分提交；课后或异步提交即使答题表缺少记录，也能正常查看、补建答题记录并完成批改。同时修复课例来源为空时被误标为“优秀课例”的问题，未知来源不再开放旧课例评价入口。本次不改变题库、课程和既有成绩。',
    NOW(),
    'PUBLISHED',
    'admin',
    NOW(),
    'admin',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.88') existing_version
);

UPDATE biz_platform_update
SET content = 'Python 操作题批改支持批量采用系统判题最高分：教师可一次完成整班已提交学生的系统分采用，遇到并发重交、退回、档案缺失或分值异常时跳过并在结果中提示人数。批改页展示的代码、判题时间、用时和通过用例严格来自同一条最高分提交；课后或异步提交即使答题表缺少记录，也能正常查看、补建答题记录并完成批改。同时修复课例来源为空时被误标为“优秀课例”的问题，未知来源不再开放旧课例评价入口。本次不改变题库、课程和既有成绩。',
    status = 'PUBLISHED',
    published_at = COALESCE(published_at, NOW()),
    update_by = 'admin',
    update_time = NOW()
WHERE version_no = '1.30.88'
  AND title = 'Python 操作题批改与课例来源修复';

SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update
WHERE version_no = '1.30.88';
