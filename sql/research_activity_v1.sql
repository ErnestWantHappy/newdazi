-- ============================================================
-- 教研活动 v1（幂等增量）
-- 目标：单一活动信息流、三类留言、结构化课程资源、定向站内通知
-- 注意：不迁移旧论坛；不使用 sys_notice；不授权 student
-- ============================================================

CREATE TABLE IF NOT EXISTS `biz_research_topic` (
  `topic_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主题ID',
  `topic_type` varchar(16) NOT NULL COMMENT 'NOTICE活动通知/SHARE交流分享',
  `title` varchar(200) NOT NULL COMMENT '主题标题',
  `content_html` longtext NOT NULL COMMENT '清洗后的富文本',
  `content_text` text NOT NULL COMMENT '清洗后的纯文本',
  `notice_level` char(1) NOT NULL DEFAULT '0' COMMENT '0非通知主题1站内通知2历史兼容',
  `notice_scope` char(1) NOT NULL DEFAULT '0' COMMENT '0无1学段2指定教师',
  `notice_stages` varchar(20) DEFAULT NULL COMMENT '通知学段代码逗号串',
  `activity_time` datetime DEFAULT NULL COMMENT '活动时间，为空时按未读状态提醒',
  `is_pinned` char(1) NOT NULL DEFAULT 'N' COMMENT '是否置顶N/Y',
  `view_count` bigint NOT NULL DEFAULT 0 COMMENT '浏览数',
  `reply_count` bigint NOT NULL DEFAULT 0 COMMENT '有效留言数',
  `download_count` bigint NOT NULL DEFAULT 0 COMMENT '资源访问总数',
  `last_activity_time` datetime NOT NULL COMMENT '最后互动时间',
  `creator_id` bigint NOT NULL COMMENT '发布账号ID',
  `dept_id` bigint DEFAULT NULL COMMENT '发布时学校/部门快照',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志0正常2删除',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`topic_id`),
  KEY `idx_research_topic_feed` (`del_flag`,`is_pinned`,`last_activity_time`),
  KEY `idx_research_topic_creator` (`creator_id`,`del_flag`,`create_time`),
  KEY `idx_research_topic_type` (`topic_type`,`del_flag`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教研活动主题';

CREATE TABLE IF NOT EXISTS `biz_research_post` (
  `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT '留言ID',
  `topic_id` bigint NOT NULL COMMENT '所属主题ID',
  `post_type` varchar(16) NOT NULL COMMENT 'COMMENT普通/MOMENT纪实/RESOURCE课程资源',
  `content_html` longtext NOT NULL COMMENT '清洗后的正文或课后反思',
  `content_text` text NOT NULL COMMENT '清洗后的纯文本',
  `school_type` char(1) DEFAULT NULL COMMENT '学段1小学2初中3高中',
  `grade` tinyint DEFAULT NULL COMMENT '绝对年级1-12',
  `semester` char(1) DEFAULT NULL COMMENT '1上学期2下学期',
  `lesson_kind` char(1) DEFAULT NULL COMMENT 'N数字课S专题课R复习课',
  `lesson_no` smallint DEFAULT NULL COMMENT '数字课次',
  `course_title` varchar(200) DEFAULT NULL COMMENT '课程标题',
  `is_pinned` char(1) NOT NULL DEFAULT 'N' COMMENT '是否置顶N/Y',
  `author_id` bigint NOT NULL COMMENT '留言账号ID',
  `dept_id` bigint DEFAULT NULL COMMENT '发布时学校/部门快照',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志0正常2删除',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`post_id`),
  KEY `idx_research_post_topic` (`topic_id`,`del_flag`,`is_pinned`,`create_time`),
  KEY `idx_research_post_filter` (`del_flag`,`post_type`,`school_type`,`grade`,`semester`,`lesson_kind`,`lesson_no`,`update_time`),
  KEY `idx_research_post_author` (`author_id`,`del_flag`,`update_time`),
  KEY `idx_research_post_course_title` (`course_title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教研活动留言';

CREATE TABLE IF NOT EXISTS `biz_research_resource` (
  `resource_id` bigint NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `post_id` bigint NOT NULL COMMENT '课程资源留言ID',
  `resource_type` char(1) NOT NULL COMMENT 'F主课件/L云盘链接',
  `resource_name` varchar(255) NOT NULL COMMENT '资源展示名称',
  `original_file_name` varchar(255) DEFAULT NULL COMMENT '文件原名',
  `stored_path` varchar(500) DEFAULT NULL COMMENT '私有目录安全相对路径',
  `file_size` bigint DEFAULT NULL COMMENT '文件字节数',
  `mime_type` varchar(100) DEFAULT NULL COMMENT '文件MIME',
  `link_url` varchar(1000) DEFAULT NULL COMMENT 'HTTP(S)云盘地址',
  `extract_code` varchar(64) DEFAULT NULL COMMENT '提取码',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间，空为永久',
  `description` varchar(500) DEFAULT NULL COMMENT '资源说明',
  `access_count` bigint NOT NULL DEFAULT 0 COMMENT '访问次数',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志0正常2删除',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`resource_id`),
  KEY `idx_research_resource_post` (`post_id`,`del_flag`,`resource_type`,`sort_order`),
  KEY `idx_research_resource_expire` (`resource_type`,`expire_time`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教研活动课程资源项';

CREATE TABLE IF NOT EXISTS `biz_research_notice_recipient` (
  `recipient_id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知接收记录ID',
  `topic_id` bigint NOT NULL COMMENT '主题ID',
  `user_id` bigint NOT NULL COMMENT '具体教师账号ID',
  `source_type` char(1) NOT NULL COMMENT 'S按学段/U指定教师',
  `source_value` varchar(32) DEFAULT NULL COMMENT '学段代码快照',
  `notice_level` char(1) NOT NULL COMMENT '1站内通知2历史兼容',
  `read_flag` char(1) NOT NULL DEFAULT 'N' COMMENT 'N未读Y已读',
  `read_time` datetime DEFAULT NULL COMMENT '已读时间',
  `notify_time` datetime NOT NULL COMMENT '最近通知时间',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`recipient_id`),
  UNIQUE KEY `uk_research_notice_topic_user` (`topic_id`,`user_id`),
  KEY `idx_research_notice_unread` (`user_id`,`read_flag`,`notice_level`,`notify_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教研活动通知接收人快照';

-- 一级菜单本身承载 list 权限，其余 7 个权限点为按钮菜单。
INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon,
   create_by, create_time, update_by, update_time, remark)
SELECT 26000, '教研活动', 0, 8, 'research-activity', 'business/researchActivity/index', '', 'ResearchActivity',
       1, 0, 'C', '0', '0', 'business:researchActivity:list', 'education',
       'admin', NOW(), '', NULL, '教师、教研员和管理员的单一教研活动信息流'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu
  WHERE menu_id = 26000 OR path = 'research-activity' OR component = 'business/researchActivity/index'
);

SET @research_menu_id := (
  SELECT menu_id FROM sys_menu
  WHERE menu_id = 26000 OR component = 'business/researchActivity/index'
  ORDER BY (menu_id = 26000) DESC, menu_id ASC LIMIT 1
);

UPDATE sys_menu
SET menu_name='教研活动', parent_id=0, order_num=8, path='research-activity',
    component='business/researchActivity/index', route_name='ResearchActivity',
    is_frame=1, is_cache=0, menu_type='C', visible='0', status='0',
    perms='business:researchActivity:list', icon='education', update_by='admin', update_time=NOW()
WHERE menu_id=@research_menu_id;

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon,
   create_by, create_time, update_by, update_time, remark)
SELECT 26001, '新增教研内容', @research_menu_id, 1, '', NULL, '', '', 1, 0, 'F', '0', '0',
       'business:researchActivity:add', '#', 'admin', NOW(), '', NULL, '发布主题和三类留言'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='business:researchActivity:add');

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon,
   create_by, create_time, update_by, update_time, remark)
SELECT 26002, '编辑本人内容', @research_menu_id, 2, '', NULL, '', '', 1, 0, 'F', '0', '0',
       'business:researchActivity:edit', '#', 'admin', NOW(), '', NULL, '只能编辑本人正文'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='business:researchActivity:edit');

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon,
   create_by, create_time, update_by, update_time, remark)
SELECT 26003, '删除本人内容', @research_menu_id, 3, '', NULL, '', '', 1, 0, 'F', '0', '0',
       'business:researchActivity:remove', '#', 'admin', NOW(), '', NULL, '作者软删除本人内容'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='business:researchActivity:remove');

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon,
   create_by, create_time, update_by, update_time, remark)
SELECT 26004, '访问教研资源', @research_menu_id, 4, '', NULL, '', '', 1, 0, 'F', '0', '0',
       'business:researchActivity:download', '#', 'admin', NOW(), '', NULL, '受控下载和云盘访问'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='business:researchActivity:download');

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon,
   create_by, create_time, update_by, update_time, remark)
SELECT 26005, '发送活动通知', @research_menu_id, 5, '', NULL, '', '', 1, 0, 'F', '0', '0',
       'business:researchActivity:notify', '#', 'admin', NOW(), '', NULL, '仅管理员和教研员'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='business:researchActivity:notify');

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon,
   create_by, create_time, update_by, update_time, remark)
SELECT 26006, '置顶教研内容', @research_menu_id, 6, '', NULL, '', '', 1, 0, 'F', '0', '0',
       'business:researchActivity:pin', '#', 'admin', NOW(), '', NULL, '仅管理员和教研员'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='business:researchActivity:pin');

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon,
   create_by, create_time, update_by, update_time, remark)
SELECT 26007, '管理教研内容', @research_menu_id, 7, '', NULL, '', '', 1, 0, 'F', '0', '0',
       'business:researchActivity:manage', '#', 'admin', NOW(), '', NULL, '隐藏和恢复他人内容'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='business:researchActivity:manage');

-- 历史重复执行后仍将功能按钮收口到唯一一级菜单。
UPDATE sys_menu
SET parent_id=@research_menu_id, status='0'
WHERE perms IN (
  'business:researchActivity:add','business:researchActivity:edit',
  'business:researchActivity:remove','business:researchActivity:download',
  'business:researchActivity:notify','business:researchActivity:pin',
  'business:researchActivity:manage'
);

-- admin / teacher / researcher：菜单 + 基础操作。
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.perms IN (
  'business:researchActivity:list','business:researchActivity:add',
  'business:researchActivity:edit','business:researchActivity:remove',
  'business:researchActivity:download'
)
WHERE r.role_key IN ('admin','teacher','researcher')
  AND r.status='0' AND r.del_flag='0'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=m.menu_id
  );

-- notify / pin / manage 只授予 admin / researcher。
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.perms IN (
  'business:researchActivity:notify','business:researchActivity:pin','business:researchActivity:manage'
)
WHERE r.role_key IN ('admin','researcher')
  AND r.status='0' AND r.del_flag='0'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=m.menu_id
  );

-- 学生不得继承或残留任何教研活动权限。
DELETE rm
FROM sys_role_menu rm
JOIN sys_role r ON r.role_id=rm.role_id
JOIN sys_menu m ON m.menu_id=rm.menu_id
WHERE r.role_key='student'
  AND m.perms LIKE 'business:researchActivity:%';

-- ======================= 复核查询 =======================
SELECT COUNT(*) AS research_table_count
FROM information_schema.tables
WHERE table_schema=DATABASE()
  AND table_name IN (
    'biz_research_topic','biz_research_post',
    'biz_research_resource','biz_research_notice_recipient'
  );

SELECT table_name, index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS index_columns
FROM information_schema.statistics
WHERE table_schema=DATABASE() AND table_name LIKE 'biz_research_%'
GROUP BY table_name, index_name
ORDER BY table_name, index_name;

SELECT perms, COUNT(*) AS menu_count
FROM sys_menu
WHERE perms LIKE 'business:researchActivity:%'
GROUP BY perms
ORDER BY perms;

SELECT r.role_key, COUNT(m.menu_id) AS permission_count,
       GROUP_CONCAT(m.perms ORDER BY m.perms) AS permissions
FROM sys_role r
LEFT JOIN sys_role_menu rm ON rm.role_id=r.role_id
LEFT JOIN sys_menu m ON m.menu_id=rm.menu_id AND m.perms LIKE 'business:researchActivity:%'
WHERE r.role_key IN ('admin','teacher','researcher','student')
GROUP BY r.role_key
ORDER BY r.role_key;

SELECT topic_id, user_id, COUNT(*) AS duplicate_count
FROM biz_research_notice_recipient
GROUP BY topic_id, user_id
HAVING COUNT(*) > 1;
