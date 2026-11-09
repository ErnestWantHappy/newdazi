-- 仅登记产品更新，不修改题目、学生、成绩或任何业务数据。
INSERT INTO biz_platform_update (version_no,title,content,published_at,status,create_by,create_time)
SELECT '1.30.75','课堂 Python 示例展示与兼容运行',
 '课堂 Python 题在代码框上方重新显示示例输入和预期输出，学生可以对照样例理解题目。新版本浏览器保持原有的逐条输入交互；浏览器版本较低时自动改为服务器按示例运行，并提示可升级浏览器，不影响保存草稿和交作业。',
 NOW(),'PUBLISHED','admin',NOW()
FROM DUAL WHERE NOT EXISTS (
 SELECT 1 FROM (SELECT version_no FROM biz_platform_update WHERE version_no='1.30.75') existing_version
);
UPDATE biz_platform_update SET status='PUBLISHED',published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.75' AND status<>'PUBLISHED';
SELECT update_id,version_no,status FROM biz_platform_update WHERE version_no='1.30.75';
