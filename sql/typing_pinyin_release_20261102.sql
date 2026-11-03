-- 130 正式发布登记：2026-11-02，1.30.67（仅前端）；验收通过后执行，可重复执行。
-- 仅维护本版本的平台更新记录，不修改课程、学生、作业或成绩。
INSERT INTO biz_platform_update (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.67', '学生打字练习支持显示拼音',
    CONCAT(
        '学生端打字原文新增“显示拼音 / 隐藏拼音”按钮，拼音显示在汉字上方，默认隐藏，再次点击可收起。\n',
        '每道打字题独立控制；开始练习前和练习中都可切换，切换不改变答案、不暂停计时，也不会触发提交。\n',
        '拼音只作阅读提示，不进入答案、字数统计或评分；刷新页面、切换课程或重新进入页面后恢复默认隐藏。'
    ), NOW(), 'PUBLISHED', 'admin', NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT version_no FROM biz_platform_update WHERE version_no='1.30.67') AS existing_version
);
UPDATE biz_platform_update
SET status='PUBLISHED', published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.67' AND status<>'PUBLISHED';
SELECT update_id,version_no,title,status,published_at FROM biz_platform_update WHERE version_no='1.30.67';
