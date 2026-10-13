-- 仅在 130 正式 1.30.45 已部署、目标旧预览验收通过后登记；无结构变更。
USE `ry-vue`;
START TRANSACTION;
INSERT INTO biz_platform_update
    (version_no,title,content,published_at,status,create_by,create_time,update_by,update_time)
SELECT '1.30.45','连线题作品预览兼容修复',
       '<p>修复部分 Word 连线题作品的连线在页图和 PDF 预览中不显示的问题，已重新生成经核验的历史受影响预览。</p><p>学生原始作品、作答记录和成绩保持不变。</p>',
       NOW(),'PUBLISHED','codex',NOW(),'codex',NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no='1.30.45');
SELECT update_id,version_no,status,published_at
FROM biz_platform_update WHERE version_no='1.30.45';
COMMIT;
