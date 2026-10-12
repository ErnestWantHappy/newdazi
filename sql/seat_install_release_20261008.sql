-- 课堂机位显示与助手安装修复通过正式验收后登记。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.37', '课堂机位显示与助手安装体验优化',
    '课堂监控优先显示电脑内网IP，保留学生姓名，隐藏自动电脑编号；机位助手升级0.2.1，双击后优先安装D盘，明确显示实际安装位置、自启动结果和关联状态。机房首次安装后仍需老师保存还原基线并重启核验。',
    NOW(), 'PUBLISHED', 'codex', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.37');
UPDATE biz_platform_update
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.37' AND status = 'DRAFT';
