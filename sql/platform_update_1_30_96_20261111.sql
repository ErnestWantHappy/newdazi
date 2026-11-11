-- 仅登记 1.30.96 产品更新，不修改课程、成绩、权限或表结构。
-- 同版本同标题幂等更新；不同标题保留，发布脚本将拒绝冲突。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time, update_by, update_time)
SELECT
    '1.30.96',
    'Python 运行与批改多项问题修复',
    '修复了多项课堂使用中的问题：旧版浏览器运行 Python 时直接按回车不再卡住；切换题目后运行按钮不再被锁；教师给最后一名学生打分后不再提示“成绩已更新”；用参考代码生成期望输出时，若测试点输入不够会明确提示；区域抽测超时后上传作品会正确自动交卷，同时打开也不会偶发报错；长考试结束前上传的作品不会失效；自动推进课程会跳过已归档的课；历史拓展班题单可以正常查看学情；带过拓展班的老师申请免抽测不再被误判不达标；AI 批改卡住的任务可自动恢复；课例评价首次投票的原因能正确保存；监管学生明细与班级统计按同一学期范围对齐。',
    NOW(), 'PUBLISHED', 'admin', NOW(), 'admin', NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.96') existing_version
);

UPDATE biz_platform_update
SET content = '修复了多项课堂使用中的问题：旧版浏览器运行 Python 时直接按回车不再卡住；切换题目后运行按钮不再被锁；教师给最后一名学生打分后不再提示“成绩已更新”；用参考代码生成期望输出时，若测试点输入不够会明确提示；区域抽测超时后上传作品会正确自动交卷，同时打开也不会偶发报错；长考试结束前上传的作品不会失效；自动推进课程会跳过已归档的课；历史拓展班题单可以正常查看学情；带过拓展班的老师申请免抽测不再被误判不达标；AI 批改卡住的任务可自动恢复；课例评价首次投票的原因能正确保存；监管学生明细与班级统计按同一学期范围对齐。',
    status = 'PUBLISHED',
    published_at = COALESCE(published_at, NOW()),
    update_by = 'admin', update_time = NOW()
WHERE version_no = '1.30.96'
  AND title = 'Python 运行与批改多项问题修复';

SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update WHERE version_no = '1.30.96';
