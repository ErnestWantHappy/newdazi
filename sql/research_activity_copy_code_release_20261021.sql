-- 仅在配套发布验收成功后登记；不修改学生作品或成绩，可重复执行。
INSERT INTO biz_platform_update
 (version_no,title,content,published_at,status,create_by,create_time)
SELECT '1.30.54','教研活动云盘提取码直观展示与兼容复制修复',
 '优化教研活动资源卡片体验：1. 兼容内网及专网 HTTP 环境下的提取码复制功能，自动降级兜底，解决复制无效报错；2. 云盘资料条目直观展示提取码明文标签（支持双击直接选中），杜绝信息遮挡；3. 优化移动端卡片自适应折行排版。',
 NOW(),'PUBLISHED','AI 发布记录',NOW()
FROM DUAL WHERE NOT EXISTS(SELECT 1 FROM biz_platform_update WHERE version_no='1.30.54');
UPDATE biz_platform_update SET status='PUBLISHED',published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.54' AND title='教研活动云盘提取码直观展示与兼容复制修复';
SELECT update_id,version_no,status FROM biz_platform_update WHERE version_no='1.30.54';
