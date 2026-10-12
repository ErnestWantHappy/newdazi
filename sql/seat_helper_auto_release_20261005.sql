-- 正式部署和验收成功后登记，重复执行不新增同版本。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.27', '机位助手直接运行与自动关联',
    '学生下载新版机位助手后直接运行，无需填写接口地址、注册码或安装目录。助手自动获取内网IP和电脑信息，并通过原网页登录关联学校与账号；教师可在课堂机位明细中设置机房和机位，确认本班常用机位。重复关联保留电脑记录与教师命名，已撤销电脑不会自动恢复。助手未安装或故障不影响正常登录、上课和作答。',
    NOW(), 'PUBLISHED', 'codex', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.27');
UPDATE biz_platform_update
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.27' AND status = 'DRAFT';
