-- 正式上线和真实验证成功后发布更新；同一版本重复执行不新增记录。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.26', '机位助手正式启用与内网HTTP支持',
    '机位助手支持学校现有HTTP平台，正式服务及现有学校已开启，学生可下载新版Windows客户端。管理员按电脑生成一次性注册码并配置机房和机位，教师可确认本班常用机位。助手为可选功能，未安装、离线或设备被撤销时，学生仍可正常登录网页和作答；客户端关联失败会打开普通平台网页。',
    NOW(), 'PUBLISHED', 'codex', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.26');
UPDATE biz_platform_update
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.26' AND status = 'DRAFT';
