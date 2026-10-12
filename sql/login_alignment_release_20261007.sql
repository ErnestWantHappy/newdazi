-- 登录页面四模块严格对齐、真实教研大图与密码输入优化正式部署通过验收后登记
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.32', '登录页面网格对齐、真实课堂大图展示与密码输入优化',
    '1. 登录页面布局升级为严格对称的 2×2 扁平 CSS Grid 架构，实现左上教研焦点卡片与右上登录框、左下教研预览与右下开课看板的高度与水平边缘严丝合缝齐平。2. 焦点图直连真实教研与课堂跟帖实况，自动提取象山二中等一线名师上传的现场实拍高清大图进行图文轮播。3. 登录框移除记住密码冗余逻辑，密码框新增明密文切换、实时过滤中文字符防误触及大写锁定（Caps Lock）状态提示，极大提升登录输入体验。',
    NOW(), 'PUBLISHED', 'codex', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.32');
UPDATE biz_platform_update
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.32' AND status = 'DRAFT';
