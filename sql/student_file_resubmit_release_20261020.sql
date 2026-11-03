-- 仅在配套发布验收成功后登记；不修改学生作品或成绩，可重复执行。
INSERT INTO biz_platform_update
 (version_no,title,content,published_at,status,create_by,create_time)
SELECT '1.30.53','学生文件作业支持自主重交',
 '当前课文件作业已有分数也可自行重新提交，无需先请老师退回。选择新文件后确认提交，成功前保留原作品和分数；新作品提交成功后转为待批改，原作品和成绩留档。取消或提交失败不会清分。教师退回功能保留，已结束课程及其他题型规则保持。',
 NOW(),'PUBLISHED','AI 发布记录',NOW()
FROM DUAL WHERE NOT EXISTS(SELECT 1 FROM biz_platform_update WHERE version_no='1.30.53');
UPDATE biz_platform_update SET status='PUBLISHED',published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.53' AND title='学生文件作业支持自主重交';
SELECT update_id,version_no,status FROM biz_platform_update WHERE version_no='1.30.53';
