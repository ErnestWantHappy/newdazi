-- 仅登记本次修复草稿，不弹出发布通知，不修改课堂数据。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.12', '修复掌控板原主题订阅权限',
    '允许班级账号订阅原有 data Topic；移除物联平台自动命令判定、关键词兜底和指定命令界面。实际班级账号订阅由128变为0，跨班订阅仍拒绝。掌控板真机复验待完成。',
    NOW(), 'DRAFT', 'codex', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.12');
