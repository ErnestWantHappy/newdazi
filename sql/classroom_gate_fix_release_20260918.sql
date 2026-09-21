-- 仅登记修复草稿，不调整任何班级的开放状态或学生成绩。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.15', '课堂题目开关刷新显示修复',
    '修复所有教师课堂大屏在定时刷新后将已开启题目误显示为关闭的问题，并防止旧刷新结果覆盖刚保存的状态。自动及手动课程推进沿用默认开启理论题和操作题的规则。',
    NOW(), 'DRAFT', 'codex', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.15');
