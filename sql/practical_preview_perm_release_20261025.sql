-- 仅在配套发布验收成功后登记；不修改学生作品或成绩，可重复执行。
INSERT INTO biz_platform_update
 (version_no,title,content,published_at,status,create_by,create_time)
SELECT '1.30.55','学生作品网页/文本预览与下载权限修复',
 '修复部分学生账号（历史导入、未绑定学生角色）在本课程提交 HTML/TXT 作品后，预览或下载时提示“没有权限”的问题。作品附件读取改为与其它学生端接口一致的学生身份校验，仍按作品归属和班级范围控制访问；教师批改与学生本人预览、下载恢复正常。',
 NOW(),'PUBLISHED','AI 发布记录',NOW()
FROM DUAL WHERE NOT EXISTS(SELECT 1 FROM biz_platform_update WHERE version_no='1.30.55');
UPDATE biz_platform_update SET status='PUBLISHED',published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.55' AND title='学生作品网页/文本预览与下载权限修复';
SELECT update_id,version_no,status FROM biz_platform_update WHERE version_no='1.30.55';
