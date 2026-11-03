-- 配套三端上线验收通过后登记；不修改学生作品、成绩或常用机位。
INSERT INTO biz_platform_update
 (version_no,title,content,published_at,status,create_by,create_time)
SELECT '1.30.50','机位助手自动认领与课堂异机提醒',
 '机位助手升级为0.2.3，同一电脑换学生登录可自动重新关联；课堂大屏仅显示助手获取的内网IP，未识别显示未连助手。教师可直接设置机房、一键保存常用机位，并对异机登录进行提醒和单人更新。旧助手需退出托盘后重新下载运行一次；机位变化提示需教师核实，不自动认定盗号或抄袭。',
 NOW(),'PUBLISHED','AI 发布记录',NOW()
FROM DUAL WHERE NOT EXISTS(SELECT 1 FROM biz_platform_update WHERE version_no='1.30.50');
UPDATE biz_platform_update SET status='PUBLISHED',published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.50' AND title='机位助手自动认领与课堂异机提醒';
SELECT update_id,version_no,status FROM biz_platform_update WHERE version_no='1.30.50';
