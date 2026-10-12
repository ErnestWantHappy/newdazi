-- 2026-10-13 130 正式 1.30.44 发布登记（LibreOffice 毒文件隔离修复）
-- 目标库：130 正式库 ry-vue（重复执行不会产生重复记录）
-- 版本号说明：上一个已发布版本为 1.30.43（update_id=121），故本轮顺延为 1.30.44
-- 发布方式：新建 release 20261013_lo_crashfix_v3（后端 jar），切换 systemd unit 指向
--           新目录后重启；前端未改动。
-- 回滚：把版本状态改回草稿即可，不删除历史记录：
--   UPDATE biz_platform_update SET status='DRAFT' WHERE version_no='1.30.44';

INSERT INTO `ry-vue`.biz_platform_update (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.44',
       '操作题预览转换稳定性修复',
       '修复部分 Word 文档反复导致预览转换服务中断的问题。此前个别内容异常的文档会让转换进程崩溃，'
       '崩溃后服务未能识别「是这份文件引起的」，于是反复重试、反复崩溃，影响同批次其他学生的预览生成。'
       '现在会自动识别这类文件并单独隔离处理，不再反复冲击共享的转换服务；'
       '被隔离的文件会明确提示「请重新导出后提交」，学生重新导出即为新文件，可正常转换，'
       '原始文件一律保留不会删除。同时修复了隔离结果未能正确记录到数据库、'
       '导致重启后又被重新捞回重试的问题，并恢复被临时暂停的分钟级转换服务自愈巡检。'
       '本次仅调整后端转换与自愈逻辑，页面功能与操作方式无变化。',
       NOW(), 'PUBLISHED', 'claude', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `ry-vue`.biz_platform_update WHERE version_no = '1.30.44');

UPDATE `ry-vue`.biz_platform_update
SET status = 'PUBLISHED',
    published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.44';

SELECT update_id, version_no, status, title, published_at
FROM `ry-vue`.biz_platform_update WHERE version_no = '1.30.44';
