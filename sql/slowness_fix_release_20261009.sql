-- 2026-10-09 130 正式 1.30.40 发布登记（首页加载性能与运行稳定性优化）
-- 目标库：130 正式库 ry-vue。幂等：重复执行不会产生重复记录。
-- 发布方式：前端 release 20261009_slowness_fix_v1（零停机切换 Nginx 根），
--           后端 JAR 字节不变，仅 shared env 新增 IOT_MQTT_CLIENT_ID / LOG_PATH 并重启一次。
-- 回滚：将本版本状态改回 DRAFT 即可（不改动历史记录内容）：
--   UPDATE biz_platform_update SET status='DRAFT' WHERE version_no='1.30.40';

INSERT INTO biz_platform_update (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.40',
       '首页加载性能与运行稳定性优化',
       '开启全站静态资源 gzip 压缩与长缓存：入口脚本 3.39MB 降至约 0.94MB、样式表 515KB 降至约 70KB；'
       '登录页 Logo 由 688KB 优化为 62KB WebP，轮播大图改用 WebP（0.8~0.9MB 降至 0.12~0.14MB）。'
       '登录展厅改为首图优先、其余 4 张约 15 秒后加载，降低机房同时开机时的带宽与解码压力。'
       '路由切换遮罩增加 10 秒兜底解除，异常时不再出现整页按钮无法点击。'
       '学生首页退出拦截改为仅拦截确实会丢失的未提交作答，并保存本地草稿快照。'
       '修复物联网 MQTT 客户端与旧环境同名导致的持续断线重连（约 1.3 秒一次）。'
       '修复应用日志目录与轮转，日志不再无限增长；清理 2 条停滞的文档预览转换脏状态。',
       NOW(), 'PUBLISHED', 'codex', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.40');

UPDATE biz_platform_update
SET status = 'PUBLISHED',
    published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.40';

SELECT update_id, version_no, status, title, published_at
FROM biz_platform_update WHERE version_no = '1.30.40';
