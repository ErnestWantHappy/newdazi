-- 正式师生验收成功后登记发布；不修改学生作品、成绩或历史状态。
INSERT INTO biz_platform_update
    (version_no,title,content,published_at,status,create_by,create_time)
SELECT '1.30.24','课堂作品更新与反馈体验改进',
    '未批改的文件和流程图作品可更新，0分仍锁定，教师退回后保留原分直到重交成功。修复删除作品后首页、大屏、批改、反馈和成绩状态不同步；流程图草稿按账号隔离，只读展示正式作品。修复连续上传额度及切班慢请求、切校注册码串页。新增TXT、单文件HTML及ZIP/RAR/7Z作品支持，需题目明确允许，新格式由教师人工评分，附件显示大小，超长文件名明确提示改名。学生可查看本人作答与教师显式发布的个人学习反馈，刷新失败可重试。可选机位助手默认关闭，新增跨页面续期和客户端普通网页降级、重新配置。',
    NOW(),'PUBLISHED','codex',NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no='1.30.24');
UPDATE biz_platform_update SET status='PUBLISHED',published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.24' AND status='DRAFT';
