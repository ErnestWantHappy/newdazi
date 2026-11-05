-- 仅在1.30.69正式部署验收后执行，教师端可见更新。
INSERT INTO biz_platform_update(version_no,title,content,status,published_at,create_by,create_time)
SELECT '1.30.69','优秀课例评价与教材阅读体验优化',
'教师可对作者课例推荐或不推荐，支持改票、撤销及匿名文字理由；评价邀请自愿且低频。配套材料按作者课例合包，展示内容与实际使用条件。调整菜单顺序，优化教材与课例预览的缩放、跳页、缩略图及手机布局。',
'PUBLISHED',NOW(),'admin',NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no='1.30.69');
UPDATE biz_platform_update SET status='PUBLISHED',published_at=COALESCE(published_at,NOW()) WHERE version_no='1.30.69';
