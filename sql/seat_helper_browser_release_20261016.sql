-- 仅配套三端正式发布和角色接口验收通过后登记；不修改学生或机位基准。
INSERT INTO biz_platform_update
 (version_no,title,content,published_at,status,create_by,create_time)
SELECT '1.30.52','机位助手静默启动与默认浏览器优先',
 '机位助手升级为0.2.4，开机及重复运行时后台静默启动，支持正式域名和IP入口自动关联。多个浏览器登录不同学生时优先关联Windows默认浏览器中的账号，其他浏览器显示等待，停止检测后自动接替。旧版请先退出托盘，再下载运行新版一次，并由老师核对机房还原与自启动设置。',
 NOW(),'PUBLISHED','AI 发布记录',NOW()
FROM DUAL WHERE NOT EXISTS(SELECT 1 FROM biz_platform_update WHERE version_no='1.30.52');
UPDATE biz_platform_update SET status='PUBLISHED',published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.52' AND title='机位助手静默启动与默认浏览器优先';
SELECT update_id,version_no,status FROM biz_platform_update WHERE version_no='1.30.52';
