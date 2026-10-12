-- 登录页面全新四模块升级与统一Logo正式部署通过验收后登记；无结构迁移或成绩修改。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.31', '登录页面全新四模块升级与统一Logo',
    '登录页面全面重塑为四模块全新布局，左上角设置统一系统Logo与优雅偏距，主界面呈现最新教研活动5图焦点轮播大卡片与教研活动预览，右侧布局用户登录框及区域学校开课动态与监管看板。全平台统一采用全新高清Logo，视觉基调重构为温润柔和的天霁晨曦浅色微质感，支持活动首图动态智能联动与高质感风采展示。',
    NOW(), 'PUBLISHED', 'codex', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.31');
UPDATE biz_platform_update
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.31' AND status = 'DRAFT';
