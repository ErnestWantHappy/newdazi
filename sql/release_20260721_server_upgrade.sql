-- 2026-07-21 服务器发布增量入口。
-- 目标基线：10.52.1.123 / ry-vue（2026-07-21 只读核验结果）。
-- 执行方式：先进入本目录，再使用 MySQL 客户端执行本文件；SOURCE 为客户端命令。
-- 强制前置：停止 3009 后端写入、完成全库备份、确认四类重复组均为 0。
-- 不包含 practical_preview_retry_fields.sql 和 typing_answer_dedup_fix.sql：
-- 服务器对应字段、重试索引和学生答案唯一索引均已存在，避免重复非幂等 DDL。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. 区域抽测：服务器是旧版空业务基线，先升级主结构，再补完整性与评卷开关。
SOURCE county_exam_v1.sql;
SOURCE county_exam_integrity_fix.sql;
SOURCE county_exam_grading_enabled.sql;

-- 2. 电子导学单：先建立模板/课程快照/答卷分层，再补新手模式、草稿与约束。
SOURCE guide_sheet_v2_template_binding.sql;
SOURCE guide_sheet_beginner_mode.sql;

-- 3. 课程模式、考勤、自动推进与当前课程唯一约束。
SOURCE lesson_attendance_mode.sql;
SOURCE lesson_auto_advance.sql;
SOURCE lesson_assignment_current_unique_fix.sql;

-- 4. 操作题预览与 LibreOffice 自愈任务。
SOURCE practical_preview_retry_quartz_job.sql;
SOURCE libreoffice_maintenance_quartz_job.sql;
SOURCE libreoffice_health_check_quartz_job.sql;

-- 完成标记只在前述脚本全部成功后写入。
INSERT INTO `biz_guide_sheet_migration`
    (`migration_key`, `migration_status`, `completed_time`)
VALUES
    ('release_20260721_server_upgrade', 'DONE', NOW())
ON DUPLICATE KEY UPDATE
    `migration_status` = VALUES(`migration_status`),
    `completed_time` = VALUES(`completed_time`);

SELECT `migration_key`, `migration_status`, `completed_time`
FROM `biz_guide_sheet_migration`
WHERE `migration_key` IN (
    'guide_sheet_v2_template_binding',
    'release_20260721_server_upgrade'
)
ORDER BY `migration_key`;
