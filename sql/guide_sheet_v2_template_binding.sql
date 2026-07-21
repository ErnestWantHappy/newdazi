-- 电子导学单 v2：模板、课程绑定快照与学生答卷分层迁移。
-- 执行要求：先停止旧版服务写入并备份数据库，再执行本脚本，最后部署新版后端与前端。
-- 本脚本仅首次删除五张旧导学单表，不修改普通课程、普通答卷或区域抽测数据。

CREATE TABLE IF NOT EXISTS `biz_guide_sheet_migration` (
  `migration_key` varchar(100) NOT NULL,
  `migration_status` varchar(20) NOT NULL,
  `completed_time` datetime DEFAULT NULL,
  PRIMARY KEY (`migration_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='导学单迁移执行标记';

SET @guide_sheet_v2_key = 'guide_sheet_v2_template_binding';
SET @guide_sheet_v2_done = (
  SELECT COUNT(*)
  FROM `biz_guide_sheet_migration`
  WHERE `migration_key` = CONVERT(@guide_sheet_v2_key USING utf8mb4) COLLATE utf8mb4_general_ci
    AND `migration_status` = 'DONE'
);

-- MySQL DDL 会隐式提交，完成标记用于避免脚本重跑时删除 v2 业务数据。
SET @guide_sheet_v2_sql = IF(
  @guide_sheet_v2_done > 0,
  'SELECT 1',
  'DROP TABLE IF EXISTS `biz_guide_sheet_upload`'
);
PREPARE guide_sheet_v2_stmt FROM @guide_sheet_v2_sql;
EXECUTE guide_sheet_v2_stmt;
DEALLOCATE PREPARE guide_sheet_v2_stmt;

SET @guide_sheet_v2_sql = IF(
  @guide_sheet_v2_done > 0,
  'SELECT 1',
  'DROP TABLE IF EXISTS `biz_guide_sheet_progress`'
);
PREPARE guide_sheet_v2_stmt FROM @guide_sheet_v2_sql;
EXECUTE guide_sheet_v2_stmt;
DEALLOCATE PREPARE guide_sheet_v2_stmt;

SET @guide_sheet_v2_sql = IF(
  @guide_sheet_v2_done > 0,
  'SELECT 1',
  'DROP TABLE IF EXISTS `biz_guide_sheet_answer`'
);
PREPARE guide_sheet_v2_stmt FROM @guide_sheet_v2_sql;
EXECUTE guide_sheet_v2_stmt;
DEALLOCATE PREPARE guide_sheet_v2_stmt;

-- 中断后重跑必须同时移除残余绑定，否则会保留指向新建模板表的旧快照。
SET @guide_sheet_v2_sql = IF(
  @guide_sheet_v2_done > 0,
  'SELECT 1',
  'DROP TABLE IF EXISTS `biz_lesson_guide_sheet_binding`'
);
PREPARE guide_sheet_v2_stmt FROM @guide_sheet_v2_sql;
EXECUTE guide_sheet_v2_stmt;
DEALLOCATE PREPARE guide_sheet_v2_stmt;

SET @guide_sheet_v2_sql = IF(
  @guide_sheet_v2_done > 0,
  'SELECT 1',
  'DROP TABLE IF EXISTS `biz_guide_sheet_assignment`'
);
PREPARE guide_sheet_v2_stmt FROM @guide_sheet_v2_sql;
EXECUTE guide_sheet_v2_stmt;
DEALLOCATE PREPARE guide_sheet_v2_stmt;

SET @guide_sheet_v2_sql = IF(
  @guide_sheet_v2_done > 0,
  'SELECT 1',
  'DROP TABLE IF EXISTS `biz_guide_sheet`'
);
PREPARE guide_sheet_v2_stmt FROM @guide_sheet_v2_sql;
EXECUTE guide_sheet_v2_stmt;
DEALLOCATE PREPARE guide_sheet_v2_stmt;

CREATE TABLE IF NOT EXISTS `biz_guide_sheet` (
  `sheet_id` bigint NOT NULL AUTO_INCREMENT COMMENT '导学单模板ID',
  `sheet_title` varchar(255) NOT NULL COMMENT '导学单标题',
  `grade` int NOT NULL COMMENT '年级',
  `semester` char(1) NOT NULL COMMENT '学期：0上册，1下册',
  `lesson_num` int NOT NULL COMMENT '第几课',
  `creator_id` bigint NOT NULL COMMENT '创建教师ID',
  `dept_id` bigint NOT NULL COMMENT '创建教师所属学校ID',
  `county_dept_id` bigint NOT NULL COMMENT '创建时所属县域根部门ID',
  `form_json` longtext COMMENT 'VForm3完整表单及评分配置JSON',
  `version_no` int NOT NULL DEFAULT 1 COMMENT '模板版本号',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '归档标志：0正常，2归档',
  `max_pages` int NOT NULL DEFAULT 0 COMMENT '导学单总页数',
  `teacher_machine_ip` varchar(50) DEFAULT NULL COMMENT '教师机文件服务地址',
  `is_public` char(1) NOT NULL DEFAULT 'N' COMMENT '是否县域公开：Y公开，N私有',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`sheet_id`),
  KEY `idx_guide_sheet_public_library`
    (`county_dept_id`,`del_flag`,`is_public`,`grade`,`semester`,`lesson_num`,`update_time`),
  KEY `idx_guide_sheet_creator_library`
    (`creator_id`,`del_flag`,`grade`,`semester`,`lesson_num`,`update_time`),
  CONSTRAINT `chk_guide_sheet_public` CHECK (`is_public` IN ('Y','N')),
  CONSTRAINT `chk_guide_sheet_archive` CHECK (`del_flag` IN ('0','2')),
  CONSTRAINT `chk_guide_sheet_version` CHECK (`version_no` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='电子导学单模板';

CREATE TABLE IF NOT EXISTS `biz_lesson_guide_sheet_binding` (
  `binding_id` bigint NOT NULL AUTO_INCREMENT COMMENT '课程导学单绑定ID',
  `lesson_id` bigint NOT NULL COMMENT '课程ID',
  `source_sheet_id` bigint NOT NULL COMMENT '来源模板ID',
  `source_version` int NOT NULL COMMENT '来源模板版本号',
  `snapshot_title` varchar(255) NOT NULL COMMENT '标题快照',
  `snapshot_grade` int NOT NULL COMMENT '年级快照',
  `snapshot_semester` char(1) NOT NULL COMMENT '学期快照',
  `snapshot_lesson_num` int NOT NULL COMMENT '课次快照',
  `snapshot_form_json` longtext NOT NULL COMMENT 'VForm3表单及评分配置快照',
  `snapshot_max_pages` int NOT NULL DEFAULT 0 COMMENT '总页数快照',
  `snapshot_teacher_machine_ip` varchar(50) DEFAULT NULL COMMENT '教师机文件服务地址快照',
  `is_current` char(1) NOT NULL DEFAULT 'Y' COMMENT '是否当前绑定',
  `enabled` char(1) NOT NULL DEFAULT 'Y' COMMENT '课程是否开启导学单',
  `creator_id` bigint NOT NULL COMMENT '建立绑定的教师ID',
  `archived_time` datetime DEFAULT NULL COMMENT '被替换时间',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  `current_lesson_id` bigint GENERATED ALWAYS AS (
    CASE WHEN `is_current` = 'Y' THEN `lesson_id` ELSE NULL END
  ) STORED COMMENT '当前绑定课程唯一键',
  PRIMARY KEY (`binding_id`),
  UNIQUE KEY `uk_lesson_guide_sheet_current` (`current_lesson_id`),
  KEY `idx_lesson_guide_sheet_lookup` (`lesson_id`,`is_current`,`enabled`),
  KEY `idx_lesson_guide_sheet_source` (`source_sheet_id`,`source_version`),
  CONSTRAINT `chk_lesson_guide_sheet_current` CHECK (`is_current` IN ('Y','N')),
  CONSTRAINT `chk_lesson_guide_sheet_enabled` CHECK (`enabled` IN ('Y','N')),
  CONSTRAINT `chk_lesson_guide_sheet_version` CHECK (`source_version` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课程导学单不可变快照绑定';

CREATE TABLE IF NOT EXISTS `biz_guide_sheet_answer` (
  `answer_id` bigint NOT NULL AUTO_INCREMENT COMMENT '导学单答卷ID',
  `binding_id` bigint NOT NULL COMMENT '课程导学单绑定ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `lesson_id` bigint NOT NULL COMMENT '课程ID审计快照',
  `source_sheet_id` bigint NOT NULL COMMENT '来源模板ID审计快照',
  `answer_json` longtext COMMENT '学生答案JSON',
  `current_page` int NOT NULL DEFAULT 0 COMMENT '当前页码',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态：0未开始，1填写中，2已提交',
  `auto_score` int DEFAULT NULL COMMENT '自动评分',
  `manual_adjustment` int NOT NULL DEFAULT 0 COMMENT '人工调整分',
  `total_score` int DEFAULT NULL COMMENT '最终得分',
  `grading_status` varchar(20) DEFAULT NULL COMMENT '评分状态',
  `grading_detail` longtext COMMENT '评分明细JSON',
  `submit_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`answer_id`),
  UNIQUE KEY `uk_guide_sheet_answer_binding_student` (`binding_id`,`student_id`),
  KEY `idx_guide_sheet_answer_dashboard` (`binding_id`,`status`,`submit_time`),
  KEY `idx_guide_sheet_answer_student` (`student_id`,`binding_id`),
  KEY `idx_guide_sheet_answer_lesson` (`lesson_id`,`student_id`),
  CONSTRAINT `chk_guide_sheet_answer_status` CHECK (`status` IN ('0','1','2'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课程导学单学生答卷';

CREATE TABLE IF NOT EXISTS `biz_guide_sheet_progress` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '进度ID',
  `binding_id` bigint NOT NULL COMMENT '课程导学单绑定ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `dept_id` bigint NOT NULL COMMENT '作答时学校ID',
  `entry_year` varchar(4) NOT NULL COMMENT '作答时入学年份',
  `class_code` varchar(30) NOT NULL COMMENT '作答时班级编号',
  `current_page` int NOT NULL DEFAULT 0 COMMENT '当前页码',
  `is_submitted` char(1) NOT NULL DEFAULT 'N' COMMENT '是否已提交',
  `last_heartbeat` datetime DEFAULT NULL COMMENT '最后心跳时间',
  `progress_detail` longtext COMMENT '进度详情JSON',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_guide_sheet_progress_binding_student` (`binding_id`,`student_id`),
  KEY `idx_guide_sheet_progress_class`
    (`binding_id`,`dept_id`,`entry_year`,`class_code`,`is_submitted`),
  KEY `idx_guide_sheet_progress_heartbeat` (`binding_id`,`last_heartbeat`),
  CONSTRAINT `chk_guide_sheet_progress_submitted` CHECK (`is_submitted` IN ('Y','N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课程导学单填写进度';

CREATE TABLE IF NOT EXISTS `biz_guide_sheet_upload` (
  `upload_id` bigint NOT NULL AUTO_INCREMENT COMMENT '上传记录ID',
  `answer_id` bigint DEFAULT NULL COMMENT '答卷ID',
  `binding_id` bigint NOT NULL COMMENT '课程导学单绑定ID',
  `source_sheet_id` bigint NOT NULL COMMENT '来源模板ID审计快照',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `question_name` varchar(255) NOT NULL COMMENT 'VForm3题目标识',
  `file_name` varchar(500) NOT NULL COMMENT '原始文件名',
  `file_size` bigint NOT NULL DEFAULT 0 COMMENT '文件大小',
  `mime_type` varchar(100) DEFAULT NULL COMMENT 'MIME类型',
  `teacher_machine_ip` varchar(50) DEFAULT NULL COMMENT '教师机文件服务地址',
  `stored_path` varchar(500) NOT NULL COMMENT '存储相对路径',
  `access_url` varchar(500) NOT NULL COMMENT '访问地址',
  `upload_time` datetime DEFAULT NULL,
  PRIMARY KEY (`upload_id`),
  KEY `idx_guide_sheet_upload_binding_student` (`binding_id`,`student_id`),
  KEY `idx_guide_sheet_upload_answer` (`answer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课程导学单上传记录';

INSERT INTO `biz_guide_sheet_migration`
  (`migration_key`, `migration_status`, `completed_time`)
VALUES
  (@guide_sheet_v2_key, 'DONE', NOW())
ON DUPLICATE KEY UPDATE
  `completed_time` = IF(`migration_status` = 'DONE', `completed_time`, VALUES(`completed_time`)),
  `migration_status` = 'DONE';

-- 执行后应仅出现模板、绑定、答卷、进度、上传及迁移标记表，不应再有旧指派表。
SELECT `table_name`
FROM `information_schema`.`tables`
WHERE `table_schema` = DATABASE()
  AND (`table_name` LIKE 'biz_guide_sheet%' OR `table_name` = 'biz_lesson_guide_sheet_binding')
ORDER BY `table_name`;
