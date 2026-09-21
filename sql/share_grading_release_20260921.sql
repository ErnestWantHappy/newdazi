-- 正式验收成功后登记版本，不修改学生成绩。
INSERT INTO biz_platform_update
    (version_no,title,content,published_at,status,create_by,create_time)
SELECT '1.30.21','分享链接回显与批改切换修复',
    '教研通知新生成的分享链接支持再次打开查看和复制；历史链接需主动重新生成一次后支持回显。修复批改操作题提交并下一位被自动刷新拉回、旧成绩覆盖造成误报的问题；真实重交或其他窗口改分时保留输入并提示重新加载。保留上一版本教研附件预览及AI失败数量显示优化。',
    NOW(),'PUBLISHED','codex',NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no='1.30.21');
UPDATE biz_platform_update SET status='PUBLISHED',published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.21' AND status='DRAFT';
