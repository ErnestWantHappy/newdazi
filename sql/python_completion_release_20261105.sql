-- 仅登记已验收版本；无业务表结构或成绩数据变更。
INSERT INTO biz_platform_update (version_no,title,content,published_at,status,create_by,create_time)
SELECT '1.30.73','修复课程推进后的 Python 判题状态',
 '修复教师推进课程后，尚未结束的 Python 判题记录可能一直显示判题中的问题。判题结果仍正常保存，已结束课程的成绩不会被迟到结果改写。',
 NOW(),'PUBLISHED','admin',NOW()
FROM DUAL WHERE NOT EXISTS (
 SELECT 1 FROM (SELECT version_no FROM biz_platform_update WHERE version_no='1.30.73') existing_version
);
UPDATE biz_platform_update SET status='PUBLISHED',published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.73' AND status<>'PUBLISHED';
SELECT update_id,version_no,status FROM biz_platform_update WHERE version_no='1.30.73';
