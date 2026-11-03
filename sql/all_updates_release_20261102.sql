-- 130 正式发布登记：2026-11-02，1.30.66；验收通过后执行，可重复执行。
-- 仅维护本版本的平台更新记录，不修改课程、学生、作业或成绩。
INSERT INTO biz_platform_update (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.66', '课堂 Python 交互运行与教材、拓展课体验优化',
    CONCAT(
        '课堂 Python：点击运行后按提示输入，回车查看结果；支持多次输入、停止运行和错误重试，精简题目说明与样例区域，保留草稿和交作业。\n',
        '课堂监控：修复教师跨校任教、两校存在同届同班号时无法加载学生列表的问题。\n',
        '拓展课：完善教师首页和班级管理入口，兼任教研员的教师也能正常进入自己的拓展班，首页支持展开和收起。\n',
        '电子教材：书架封面改用清晰的小图，减少加载流量；保留课程绑定、学生教材入口和教师整册／本课 PDF 下载。\n',
        '登录页：减少开课概览重复计算，降低后台负担；课堂作业和成绩仍以登录后的实时页面为准。'
    ), NOW(), 'PUBLISHED', 'admin', NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT version_no FROM biz_platform_update WHERE version_no='1.30.66') AS existing_version
);
UPDATE biz_platform_update
SET status='PUBLISHED', published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.66' AND status<>'PUBLISHED';
SELECT update_id,version_no,title,status,published_at FROM biz_platform_update WHERE version_no='1.30.66';
