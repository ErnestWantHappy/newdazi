-- 仅登记 1.30.84 产品更新，不修改课程、题库、成绩或其他业务数据。
-- 可在正式库幂等复跑：版本记录最多一行，并确保最终状态为 PUBLISHED。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time, update_by, update_time)
SELECT
    '1.30.84',
    '品牌标识与登录体验优化',
    '浏览器标题统一为“信息科技学业测评平台”。登录页、教师首页和学生首页的 Logo 均可点击查看透明底大图；实际登录成功首次进入首页时，Logo 会先居中停留约 1 秒，再缩小到左上角，刷新页面或站内跳转不会重复播放。系统开启“减少动态效果”时会自动跳过动画。本次仅调整前端展示，不改变账号、课程、题目、成绩或权限规则。',
    NOW(),
    'PUBLISHED',
    'admin',
    NOW(),
    'admin',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.84') existing_version
);

UPDATE biz_platform_update
SET title = '品牌标识与登录体验优化',
    content = '浏览器标题统一为“信息科技学业测评平台”。登录页、教师首页和学生首页的 Logo 均可点击查看透明底大图；实际登录成功首次进入首页时，Logo 会先居中停留约 1 秒，再缩小到左上角，刷新页面或站内跳转不会重复播放。系统开启“减少动态效果”时会自动跳过动画。本次仅调整前端展示，不改变账号、课程、题目、成绩或权限规则。',
    status = 'PUBLISHED',
    published_at = COALESCE(published_at, NOW()),
    update_by = 'admin',
    update_time = NOW()
WHERE version_no = '1.30.84';

SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update
WHERE version_no = '1.30.84';
