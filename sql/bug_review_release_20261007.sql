-- 三份 AI 报告核验修复通过正式验收后登记。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.35', '登录、课堂作品与机位助手问题修复',
    '修复登录图片兜底、选校刷新及跨标签状态；修复退回作品重交、成绩人数、流程图版本与草稿冲突；改进机位确认、轮询和异常状态；统一匿名教研图片有效性校验，并修复文件下载与预览兼容问题。',
    NOW(), 'PUBLISHED', 'codex', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.35');
UPDATE biz_platform_update
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.35' AND status = 'DRAFT';
