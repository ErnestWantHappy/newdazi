-- 仅登记 1.30.94 Python 在线运行与判题全链路统一，不修改课程、题库、成绩或权限。
-- 脚本幂等；只在同版本标题一致时更新，避免覆盖其他发布。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time, update_by, update_time)
SELECT
    '1.30.94',
    'Python 在线运行与判题全链路统一',
    '教师批改页“运行查看”、学生做题与刷题、作品 .py 运行统一为同一套运行与判题：打开即运行，程序提示语按顺序显示，输入框紧跟提示语；课程与刷题按测试点权重计分，等待输入不再误判为通过。教师建题可直接写 input("提示文字")，验证失败会逐测试点显示期望与实际输出，并可一键按参考代码生成期望输出（需确认后保存）。高版本浏览器本机运行，其他浏览器自动切换到服务器运行，已填内容不丢失。历史题目与已有成绩保持兼容，不修改历史数据。',
    NOW(),
    'PUBLISHED',
    'admin',
    NOW(),
    'admin',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.94') existing_version
);

UPDATE biz_platform_update
SET content = '教师批改页“运行查看”、学生做题与刷题、作品 .py 运行统一为同一套运行与判题：打开即运行，程序提示语按顺序显示，输入框紧跟提示语；课程与刷题按测试点权重计分，等待输入不再误判为通过。教师建题可直接写 input("提示文字")，验证失败会逐测试点显示期望与实际输出，并可一键按参考代码生成期望输出（需确认后保存）。高版本浏览器本机运行，其他浏览器自动切换到服务器运行，已填内容不丢失。历史题目与已有成绩保持兼容，不修改历史数据。',
    status = 'PUBLISHED',
    published_at = COALESCE(published_at, NOW()),
    update_by = 'admin',
    update_time = NOW()
WHERE version_no = '1.30.94'
  AND title = 'Python 在线运行与判题全链路统一';

SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update
WHERE version_no = '1.30.94';
