-- 仅登记题库前端预览更新，不修改题目、学生或成绩。
INSERT INTO biz_platform_update (version_no,title,content,published_at,status,create_by,create_time)
SELECT '1.30.74','题库教师姓名与题目预览优化',
 '题库显示出题教师姓名，打字题正文不再重复。Python题支持弹窗查看题目详情、起始代码、参考答案及测试输入输出；流程图分学生起始图和参考答案图预览。参考答案沿用原有查看权限。',
 NOW(),'PUBLISHED','admin',NOW()
FROM DUAL WHERE NOT EXISTS (
 SELECT 1 FROM (SELECT version_no FROM biz_platform_update WHERE version_no='1.30.74') existing_version
);
UPDATE biz_platform_update SET status='PUBLISHED',published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.74' AND status<>'PUBLISHED';
SELECT update_id,version_no,status FROM biz_platform_update WHERE version_no='1.30.74';
