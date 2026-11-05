-- 教师优秀课例上线验收后登记，不修改学生作业或成绩，可重复执行。
INSERT INTO biz_platform_update (version_no,title,content,published_at,status,create_by,create_time)
SELECT '1.30.68','电子教材新增优秀课例资源',
 CONCAT(
  '电子教材收录52份优秀课例、201份教学资源。书架与课次目录显示资源标记，点击对应课次即可查看不同教师的课例。\n',
  '教学设计、课件支持在线预览与原件下载，图片和适合在线播放的视频可以直接查看。\n',
  '实验工具和配套网页提供下载包，下载前可查看内容、用途及运行条件。部分工具含原作者自愿共享的模型Key，失效后可替换为自己的。'
 ),NOW(),'PUBLISHED','admin',NOW()
FROM DUAL WHERE NOT EXISTS (
 SELECT 1 FROM (SELECT version_no FROM biz_platform_update WHERE version_no='1.30.68') existing_version
);
UPDATE biz_platform_update SET status='PUBLISHED',published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.68' AND status<>'PUBLISHED';
SELECT update_id,version_no,status,published_at FROM biz_platform_update WHERE version_no='1.30.68';
