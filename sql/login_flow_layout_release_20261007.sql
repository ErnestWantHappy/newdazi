-- 平台更新记录：1.30.34 登录页面流式自适应、开课监管看板与教研预览位置优化及Logo放大升级
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.34', '登录页流式自适应、开课监管看板与教研预览位置优化及Logo放大',
    '1. 区域学校开课动态与监管看板移动至左下，享有更充裕的横向宽屏流式空间，开课明细与批改进度舒展呈现；2. 最新教研活动预览移动至右下，与登录卡片构成紧凑对称的信息布局；3. 页面采用Flex流式与自适应视口比例，容器宽度扩增至min(1560px, 94vw)，完美适配宽屏、笔记本及移动端；4. 顶部Logo尺寸放大至58px并微调标题层次，浏览器Tab栏Favicon全面生效帆船专属徽标。',
    NOW(), 'PUBLISHED', 'codex', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.34');
UPDATE biz_platform_update
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.34' AND status = 'DRAFT';
