-- 仅在正式验收成功后执行发布登记，不修改业务成绩。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.20', '教研多附件与AI批改页面优化',
    '教研课堂反思、活动纪实及课程资源支持多个Word、PDF等附件，支持登录后在线预览，压缩包可下载；编辑时可逐个保留、移除或追加附件。公开通知分享页补齐留言与课程资源正文，附件仍需登录访问。AI批改明确显示失败项数量，区分部分失败与全部失败；顶部筛选与操作分行排列，改善窄屏布局。',
    NOW(), 'PUBLISHED', 'codex', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no='1.30.20');
UPDATE biz_platform_update SET status='PUBLISHED', published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.20' AND status='DRAFT';
