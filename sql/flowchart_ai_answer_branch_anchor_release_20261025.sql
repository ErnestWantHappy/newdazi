-- 仅在 1.30.59 判断框分支端口优化验收成功后登记；不修改学生作品或成绩，可重复执行。
INSERT INTO biz_platform_update
 (version_no,title,content,published_at,status,create_by,create_time)
SELECT '1.30.59','流程图判断分支左右端口优化',
 '修复拍照生成流程图时判断框“是/否”两条分支从同一底部端口分出、挤在一起的问题。现在判断框的多条分支按目标位置分别从左、右锚点引出（最左走左侧、最右走右侧），再纵向汇入后续步骤，更接近课本的画法，连线更清楚。',
 NOW(),'PUBLISHED','AI 发布记录',NOW()
FROM DUAL WHERE NOT EXISTS(SELECT 1 FROM biz_platform_update WHERE version_no='1.30.59');
UPDATE biz_platform_update SET status='PUBLISHED',published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.59' AND title='流程图判断分支左右端口优化';
SELECT update_id,version_no,status FROM biz_platform_update WHERE version_no='1.30.59';
