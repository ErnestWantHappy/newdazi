-- 平台更新记录：教师、教研员和管理员只读；仅管理员可维护与发布。
-- 执行目标：本机开发库或正式库。正式执行前按 AGENTS.md 先备份相关数据。

CREATE TABLE IF NOT EXISTS `biz_platform_update` (
  `update_id` bigint NOT NULL AUTO_INCREMENT COMMENT '更新记录ID',
  `version_no` varchar(30) NOT NULL COMMENT '平台版本号，如1.0.0',
  `title` varchar(100) NOT NULL COMMENT '面向用户的更新标题',
  `content` text NOT NULL COMMENT '更新说明，纯文本按换行显示',
  `published_at` datetime NOT NULL COMMENT '实际发布时间',
  `status` varchar(16) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT草稿 PUBLISHED已发布 WITHDRAWN已撤回',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`update_id`),
  KEY `idx_platform_update_public` (`status`, `published_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台更新与维护记录';

-- 从 Git 历史整理的已实现记录。日期只使用真实提交时间；文字面向教师和教研员，避免技术内部细节。
INSERT INTO `biz_platform_update`
  (`version_no`, `title`, `content`, `published_at`, `status`, `create_by`, `create_time`)
SELECT * FROM (
  SELECT '1.0.0' AS version_no, '教学基础功能上线' AS title,
    '完成试题、作业、课程与学生管理基础流程。\n教师首页可汇总查看自己的教学任务。',
    '2025-08-31 14:49:00' AS published_at, 'PUBLISHED' AS status, 'AI 发布记录' AS create_by, '2025-08-31 14:49:00' AS create_time
  UNION ALL SELECT '1.1.0', '多学校教师使用优化',
    '支持同一位教师在多个学校开展教学工作。\n学校切换后的课程和学生数据保持按学校隔离。',
    '2025-10-08 16:15:00', 'PUBLISHED', 'AI 发布记录', '2025-10-08 16:15:00'
  UNION ALL SELECT '1.2.0', '教研活动与学年课程优化',
    '新增教研活动相关能力，并优化学年课程的归属与展示。\n修复多学校通知范围问题。',
    '2026-07-22 18:19:00', 'PUBLISHED', 'AI 发布记录', '2026-07-22 18:19:00'
  UNION ALL SELECT '1.3.0', '教学监管与免抽测能力完善',
    '完善教学监管、免抽测审核与交卷稳定性。\n教师首页增加批改提醒，便于及时完成操作题批改。',
    '2026-08-03 20:10:00', 'PUBLISHED', 'AI 发布记录', '2026-08-03 20:10:00'
  UNION ALL SELECT '1.4.0', '操作题多格式作品与 AI 辅助批改上线',
    '操作题支持更多作品格式预览与提交。\n新增 AI 辅助批改的可观察、可恢复处理能力，教师可继续人工核对结果。',
    '2026-08-09 13:04:00', 'PUBLISHED', 'AI 发布记录', '2026-08-09 13:04:00'
  UNION ALL SELECT '1.5.0', '教师首页与批改体验优化',
    '教师首页按开设年级分栏，常用信息加载更快。\n优化批改进度和截止状态的显示，支持人工复核 AI 给出的分数。',
    '2026-08-09 17:22:00', 'PUBLISHED', 'AI 发布记录', '2026-08-09 17:22:00'
  UNION ALL SELECT '1.6.0', '在线协作能力接入',
    '平台接入在线协作文档能力。\n完善协作房间状态处理，支持教学场景下的文档协作。',
    '2026-08-17 12:53:00', 'PUBLISHED', 'AI 发布记录', '2026-08-17 12:53:00'
  UNION ALL SELECT '1.7.0', 'Python 编程作答体验优化',
    '课程内 Python 编程题支持保存草稿、运行示例、提交判题和历史记录。\n修复学生端编程题显示和草稿保存的体验问题。',
    '2026-08-18 22:43:00', 'PUBLISHED', 'AI 发布记录', '2026-08-18 22:43:00'
) AS history
WHERE NOT EXISTS (
  SELECT 1 FROM `biz_platform_update` existing
  WHERE existing.`version_no` = history.`version_no`
);

-- 独立菜单和功能权限，页面由 Vue3 动态路由加载；学生没有入口。
INSERT INTO `sys_menu`
  (`menu_name`,`parent_id`,`order_num`,`path`,`component`,`route_name`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`remark`)
SELECT '平台更新', 0, 99, 'platform-update', 'business/platformUpdate/index', 'PlatformUpdate', 1, 0, 'C', '0', '0',
       'business:platformUpdate:list', 'notification', 'admin', NOW(), '教师、教研员查看平台更新记录'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'business:platformUpdate:list' AND `menu_type` = 'C');

SET @platform_update_menu_id := (
  SELECT `menu_id` FROM `sys_menu`
  WHERE `perms` = 'business:platformUpdate:list' AND `menu_type` = 'C'
  ORDER BY `menu_id` LIMIT 1
);

INSERT INTO `sys_menu` (`menu_name`,`parent_id`,`order_num`,`path`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`)
SELECT source.menu_name, @platform_update_menu_id, source.order_num, '', 1, 0, 'F', '1', '0', source.perms, '#', 'admin', NOW()
FROM (
  SELECT '平台更新新增' AS menu_name, 1 AS order_num, 'business:platformUpdate:add' AS perms
  UNION ALL SELECT '平台更新修改', 2, 'business:platformUpdate:edit'
  UNION ALL SELECT '平台更新发布', 3, 'business:platformUpdate:publish'
) source
WHERE @platform_update_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` menu WHERE menu.`perms` = source.perms);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT role.`role_id`, menu.`menu_id`
FROM `sys_role` role
JOIN `sys_menu` menu ON menu.`perms` = 'business:platformUpdate:list'
WHERE role.`role_key` IN ('admin', 'teacher', 'researcher')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` role_menu
    WHERE role_menu.`role_id` = role.`role_id` AND role_menu.`menu_id` = menu.`menu_id`
  );

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT role.`role_id`, menu.`menu_id`
FROM `sys_role` role
JOIN `sys_menu` menu ON menu.`perms` IN ('business:platformUpdate:add', 'business:platformUpdate:edit', 'business:platformUpdate:publish')
WHERE role.`role_key` = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` role_menu
    WHERE role_menu.`role_id` = role.`role_id` AND role_menu.`menu_id` = menu.`menu_id`
  );

-- 后检：应至少有 8 条历史已发布记录、1 个可见菜单和 3 个管理员功能权限。
SELECT
  (SELECT COUNT(*) FROM `biz_platform_update` WHERE `status` = 'PUBLISHED') AS published_update_count,
  (SELECT COUNT(*) FROM `sys_menu` WHERE `perms` = 'business:platformUpdate:list') AS list_menu_count,
  (SELECT COUNT(*) FROM `sys_menu` WHERE `perms` LIKE 'business:platformUpdate:%') AS permission_menu_count;
