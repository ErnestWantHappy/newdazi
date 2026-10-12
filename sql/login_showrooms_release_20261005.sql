-- 登录展厅正式发布并通过验收后登记；不包含表结构或业务数据迁移。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.28', '全景登录页与教研开课概览',
    '登录页采用全景轮播背景与深蓝磨砂双展厅，登录框水平居中，仅保留账号、密码和登录按钮。页面展示最新教研活动及区域学校开课的真实概览；活动内容、资源、学生明细和完整成绩继续要求登录及原有权限。保留原三张背景、素养标签和版权信息，优化手机布局；服务器开启验证码时，点击登录后在弹窗中验证。',
    NOW(), 'PUBLISHED', 'codex', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.28');
UPDATE biz_platform_update
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.28' AND status = 'DRAFT';
