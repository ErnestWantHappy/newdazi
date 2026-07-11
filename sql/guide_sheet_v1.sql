-- 电子导学单 v1：仅创建缺失对象，可重复执行，不删除现有数据。

CREATE TABLE IF NOT EXISTS `biz_guide_sheet` (
  `sheet_id` bigint NOT NULL AUTO_INCREMENT,
  `sheet_title` varchar(255) NOT NULL,
  `lesson_id` bigint DEFAULT NULL,
  `creator_id` bigint NOT NULL,
  `dept_id` bigint NOT NULL,
  `form_json` longtext,
  `status` char(1) NOT NULL DEFAULT '0',
  `max_pages` int NOT NULL DEFAULT 0,
  `teacher_machine_ip` varchar(50) DEFAULT NULL,
  `is_public` char(1) NOT NULL DEFAULT 'Y',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`sheet_id`),
  KEY `idx_guide_sheet_dept` (`dept_id`),
  KEY `idx_guide_sheet_creator` (`creator_id`),
  KEY `idx_guide_sheet_status` (`status`),
  KEY `idx_guide_sheet_lesson` (`lesson_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='电子导学单';

CREATE TABLE IF NOT EXISTS `biz_guide_sheet_assignment` (
  `assignment_id` bigint NOT NULL AUTO_INCREMENT,
  `sheet_id` bigint NOT NULL,
  `entry_year` varchar(4) NOT NULL,
  `class_code` varchar(30) NOT NULL,
  `dept_id` bigint NOT NULL,
  `assign_time` datetime DEFAULT NULL,
  PRIMARY KEY (`assignment_id`),
  UNIQUE KEY `uk_guide_sheet_class` (`sheet_id`,`dept_id`,`entry_year`,`class_code`),
  KEY `idx_guide_sheet_assignment_class` (`dept_id`,`entry_year`,`class_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='导学单班级指派';

CREATE TABLE IF NOT EXISTS `biz_guide_sheet_answer` (
  `answer_id` bigint NOT NULL AUTO_INCREMENT,
  `sheet_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `lesson_id` bigint DEFAULT NULL,
  `answer_json` longtext,
  `current_page` int NOT NULL DEFAULT 0,
  `status` char(1) NOT NULL DEFAULT '0',
  `total_score` int DEFAULT NULL,
  `grading_status` varchar(10) DEFAULT NULL,
  `grading_detail` text,
  `submit_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`answer_id`),
  UNIQUE KEY `uk_guide_sheet_student` (`student_id`,`sheet_id`),
  KEY `idx_guide_sheet_answer_sheet` (`sheet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='导学单答案';

CREATE TABLE IF NOT EXISTS `biz_guide_sheet_upload` (
  `upload_id` bigint NOT NULL AUTO_INCREMENT,
  `answer_id` bigint DEFAULT NULL,
  `sheet_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `question_name` varchar(255) NOT NULL,
  `file_name` varchar(500) NOT NULL,
  `file_size` bigint NOT NULL DEFAULT 0,
  `mime_type` varchar(100) DEFAULT NULL,
  `teacher_machine_ip` varchar(50) NOT NULL,
  `stored_path` varchar(500) NOT NULL,
  `access_url` varchar(500) NOT NULL,
  `upload_time` datetime DEFAULT NULL,
  PRIMARY KEY (`upload_id`),
  KEY `idx_guide_sheet_upload_sheet` (`sheet_id`,`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='导学单上传记录';

CREATE TABLE IF NOT EXISTS `biz_guide_sheet_progress` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sheet_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `class_code` varchar(30) NOT NULL,
  `current_page` int NOT NULL DEFAULT 0,
  `is_submitted` char(1) NOT NULL DEFAULT 'N',
  `last_heartbeat` datetime DEFAULT NULL,
  `progress_detail` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_guide_sheet_progress` (`sheet_id`,`student_id`),
  KEY `idx_guide_sheet_progress_class` (`sheet_id`,`class_code`),
  KEY `idx_guide_sheet_heartbeat` (`last_heartbeat`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='导学单填写进度';

-- 兼容功能分支曾写入表单JSON的明文AI配置。
UPDATE biz_guide_sheet
SET form_json = JSON_REMOVE(form_json, '$._aiApiKey', '$._aiProvider', '$._aiModel', '$._aiCustomUrl')
WHERE JSON_VALID(form_json)
  AND JSON_CONTAINS_PATH(form_json, 'one', '$._aiApiKey', '$._aiProvider', '$._aiModel', '$._aiCustomUrl');

-- 现有业务菜单均为顶级页面，避免依赖并不存在的“教学管理”目录。
SET @guide_menu = (
  SELECT menu_id
  FROM sys_menu
  WHERE component = 'business/guideSheet/index'
     OR (path IN ('guide-sheet-list', 'business/guide-sheet-list', '/business/guide-sheet-list')
         AND menu_type IN ('C', 'M'))
  ORDER BY CASE WHEN component = 'business/guideSheet/index' THEN 0 ELSE 1 END, menu_id
  LIMIT 1
);

INSERT INTO sys_menu
  (menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
SELECT '导学单管理',0,7,'business/guide-sheet-list','business/guideSheet/index','GuideSheet',1,0,'C','0','0','business:guideSheet:list','guide-sheet','admin',NOW(),'电子导学单管理'
WHERE @guide_menu IS NULL;

SET @guide_menu = (
  SELECT menu_id
  FROM sys_menu
  WHERE component = 'business/guideSheet/index'
     OR (path IN ('guide-sheet-list', 'business/guide-sheet-list', '/business/guide-sheet-list')
         AND menu_type IN ('C', 'M'))
  ORDER BY CASE WHEN component = 'business/guideSheet/index' THEN 0 ELSE 1 END, menu_id
  LIMIT 1
);

-- 兼容功能分支曾使用的目录菜单结构，统一为可直接访问的页面菜单。
UPDATE sys_menu
SET menu_name = '导学单管理',
    parent_id = 0,
    order_num = 7,
    path = 'business/guide-sheet-list',
    component = 'business/guideSheet/index',
    route_name = 'GuideSheet',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'business:guideSheet:list',
    icon = 'guide-sheet'
WHERE menu_id = @guide_menu;

INSERT INTO sys_menu
  (menu_name,parent_id,order_num,path,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT p.menu_name,@guide_menu,p.order_num,'',1,0,'F','0','0',p.perms,'#','admin',NOW()
FROM (
  SELECT '导学单新增' menu_name,1 order_num,'business:guideSheet:add' perms UNION ALL
  SELECT '导学单修改',2,'business:guideSheet:edit' UNION ALL
  SELECT '导学单删除',3,'business:guideSheet:remove' UNION ALL
  SELECT '导学单设计',4,'business:guideSheet:design' UNION ALL
  SELECT '导学单看板',5,'business:guideSheet:dashboard' UNION ALL
  SELECT '导学单导出',6,'business:guideSheet:export'
) p
WHERE @guide_menu IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.perms = p.perms);

INSERT INTO sys_role_menu (role_id,menu_id)
SELECT r.role_id,m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.perms LIKE 'business:guideSheet:%'
WHERE r.role_key IN ('admin','teacher','researcher')
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
  );
