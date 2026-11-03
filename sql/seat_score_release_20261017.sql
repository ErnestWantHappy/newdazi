-- 机位与成绩修复配套发布成功后登记；不更新学生答案、成绩或机位基准。
INSERT INTO biz_platform_update
 (version_no,title,content,published_at,status,create_by,create_time)
SELECT '1.30.52','机位静默关联与成绩统计优化',
 '机位助手升级为0.2.4，开机与重复运行静默，支持正式域名及IP入口，多浏览器优先关联Windows默认浏览器中的账号。成绩图表保留全体学生及状态，操作题未批时先累计已有评分，页面与导出同步；学生自主重新打字保留本轮最高分及对应指标，教师打字重做才清分开启新轮。旧助手请先退出托盘，再下载运行新版一次。',
 NOW(),'PUBLISHED','AI 发布记录',NOW()
FROM DUAL WHERE NOT EXISTS(SELECT 1 FROM biz_platform_update WHERE version_no='1.30.52');
UPDATE biz_platform_update SET status='PUBLISHED',published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.52' AND title='机位静默关联与成绩统计优化';
SELECT update_id,version_no,status FROM biz_platform_update WHERE version_no='1.30.52';
