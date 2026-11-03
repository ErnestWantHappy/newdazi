-- 仅在 1.30.57 排版修复验收成功后登记；不修改学生作品或成绩，可重复执行。
INSERT INTO biz_platform_update
 (version_no,title,content,published_at,status,create_by,create_time)
SELECT '1.30.57','流程图标准答案AI排版修复',
 '修复画流程图题拍照生成标准答案时，含循环/回边的算法流程图（如二分查找）被挤成一行、连线混乱的问题。现在按流程步骤纵向分层排版，判断分支左右并列，结束节点沉底，生成后可直接在画布上核对微调。',
 NOW(),'PUBLISHED','AI 发布记录',NOW()
FROM DUAL WHERE NOT EXISTS(SELECT 1 FROM biz_platform_update WHERE version_no='1.30.57');
UPDATE biz_platform_update SET status='PUBLISHED',published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.57' AND title='流程图标准答案AI排版修复';
SELECT update_id,version_no,status FROM biz_platform_update WHERE version_no='1.30.57';
