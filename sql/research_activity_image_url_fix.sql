-- 教研活动富文本图片旧地址修复（幂等）。
-- 旧版把安全配置明确禁止的 /profile/** 直接写入正文，浏览器图片请求无法携带 Authorization。
-- 新版统一走 /common/resource/view，由同源 Cookie 完成只读鉴权；这里只改教研活动专用图片前缀。

UPDATE biz_research_topic
SET content_html = REPLACE(
        REPLACE(content_html,
            '"/dev-api/profile/upload/research-activity/images/',
            '"/dev-api/common/resource/view?resource=/profile/upload/research-activity/images/'),
        '"/prod-api/profile/upload/research-activity/images/',
        '"/prod-api/common/resource/view?resource=/profile/upload/research-activity/images/')
WHERE content_html LIKE '%/profile/upload/research-activity/images/%';

UPDATE biz_research_post
SET content_html = REPLACE(
        REPLACE(content_html,
            '"/dev-api/profile/upload/research-activity/images/',
            '"/dev-api/common/resource/view?resource=/profile/upload/research-activity/images/'),
        '"/prod-api/profile/upload/research-activity/images/',
        '"/prod-api/common/resource/view?resource=/profile/upload/research-activity/images/')
WHERE content_html LIKE '%/profile/upload/research-activity/images/%';

-- 复核：返回 0 才表示旧直连地址已全部清理。
SELECT 'topic_legacy_image_url' AS check_item, COUNT(*) AS remaining
FROM biz_research_topic
WHERE content_html LIKE '%/dev-api/profile/upload/research-activity/images/%'
   OR content_html LIKE '%/prod-api/profile/upload/research-activity/images/%'
UNION ALL
SELECT 'post_legacy_image_url', COUNT(*)
FROM biz_research_post
WHERE content_html LIKE '%/dev-api/profile/upload/research-activity/images/%'
   OR content_html LIKE '%/prod-api/profile/upload/research-activity/images/%';
