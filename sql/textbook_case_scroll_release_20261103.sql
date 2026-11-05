-- 连续滚动预览验收后登记，无业务结构迁移。
INSERT INTO biz_platform_update(version_no,title,content,published_at,status,create_by,create_time)
SELECT '1.30.70','优秀课例支持上下连续滚动预览',
'教学设计和教学课件支持滚轮、触摸上下连续阅读，当前页码和缩略图随滚动同步；保留跳页和缩放。长文档只加载可见页及邻近页，单页失败可就地重试。',
NOW(),'PUBLISHED','admin',NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT version_no FROM biz_platform_update WHERE version_no='1.30.70') existing_version);
UPDATE biz_platform_update SET status='PUBLISHED',published_at=COALESCE(published_at,NOW()) WHERE version_no='1.30.70' AND status<>'PUBLISHED';
