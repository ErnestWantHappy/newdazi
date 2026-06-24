-- 电子导学单模块建表脚本
-- 版本: v1.0, 日期: 2026-06-06

-- 1. 导学单模板主表
DROP TABLE IF EXISTS `biz_guide_sheet`;
CREATE TABLE `biz_guide_sheet` (
  `sheet_id` bigint NOT NULL AUTO_INCREMENT COMMENT '导学单主键ID',
  `sheet_title` varchar(255) NOT NULL COMMENT '导学单标题',
  `lesson_id` bigint DEFAULT NULL COMMENT '关联课程ID（可选）',
  `creator_id` bigint NOT NULL COMMENT '创建教师ID',
  `dept_id` bigint NOT NULL COMMENT '所属学校ID（多校隔离）',
  `form_json` longtext COMMENT '表单定义JSON',
  `status` char(1) DEFAULT '0' COMMENT '状态：0=草稿，1=已发布，2=已关闭',
  `max_pages` int DEFAULT 0 COMMENT '导学单总页数',
  `teacher_machine_ip` varchar(50) DEFAULT NULL COMMENT '教师机IP地址',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`sheet_id`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_lesson_id` (`lesson_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导学单模板主表';

-- 2. 导学单班级指派表
DROP TABLE IF EXISTS `biz_guide_sheet_assignment`;
CREATE TABLE `biz_guide_sheet_assignment` (
  `assignment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '指派记录ID',
  `sheet_id` bigint NOT NULL COMMENT '导学单ID',
  `entry_year` varchar(255) NOT NULL COMMENT '入学年份',
  `class_code` varchar(255) NOT NULL COMMENT '班级编号',
  `dept_id` bigint NOT NULL COMMENT '所属学校ID（多校隔离）',
  `assign_time` datetime DEFAULT NULL COMMENT '指派时间',
  PRIMARY KEY (`assignment_id`),
  KEY `idx_sheet_id` (`sheet_id`),
  KEY `idx_dept_year_class` (`dept_id`, `entry_year`, `class_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导学单班级指派表';

-- 3. 学生导学单填写记录表
DROP TABLE IF EXISTS `biz_guide_sheet_answer`;
CREATE TABLE `biz_guide_sheet_answer` (
  `answer_id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `sheet_id` bigint NOT NULL COMMENT '导学单ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `lesson_id` bigint DEFAULT NULL COMMENT '关联课程ID',
  `answer_json` longtext COMMENT '学生提交的完整答案JSON',
  `current_page` int DEFAULT 0 COMMENT '当前页码（断点续填）',
  `status` char(1) DEFAULT '0' COMMENT '状态：0=未开始，1=填写中，2=已提交',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`answer_id`),
  KEY `idx_sheet_id` (`sheet_id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_lesson_id` (`lesson_id`),
  UNIQUE KEY `uk_student_sheet` (`student_id`, `sheet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生导学单填写记录表';

-- 4. 导学单多媒体上传记录表
DROP TABLE IF EXISTS `biz_guide_sheet_upload`;
CREATE TABLE `biz_guide_sheet_upload` (
  `upload_id` bigint NOT NULL AUTO_INCREMENT COMMENT '上传记录ID',
  `answer_id` bigint DEFAULT NULL COMMENT '关联填写记录ID',
  `sheet_id` bigint NOT NULL COMMENT '关联导学单ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `question_name` varchar(255) NOT NULL COMMENT '表单题目标识',
  `file_name` varchar(500) NOT NULL COMMENT '原始文件名',
  `file_size` bigint DEFAULT 0 COMMENT '文件大小（字节）',
  `mime_type` varchar(100) DEFAULT NULL COMMENT '文件MIME类型',
  `teacher_machine_ip` varchar(50) NOT NULL COMMENT '存储文件的教师机IP',
  `stored_path` varchar(500) NOT NULL COMMENT '教师机上存储的相对路径',
  `access_url` varchar(500) NOT NULL COMMENT '完整访问URL',
  `upload_time` datetime DEFAULT NULL COMMENT '上传时间',
  PRIMARY KEY (`upload_id`),
  KEY `idx_sheet_id` (`sheet_id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_answer_id` (`answer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导学单多媒体上传记录表';

-- 5. 课堂实时状态表
DROP TABLE IF EXISTS `biz_guide_sheet_progress`;
CREATE TABLE `biz_guide_sheet_progress` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sheet_id` bigint NOT NULL COMMENT '导学单ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `class_code` varchar(255) NOT NULL COMMENT '班级编号',
  `current_page` int DEFAULT 0 COMMENT '当前页码',
  `is_submitted` char(1) DEFAULT 'N' COMMENT '是否已提交（Y/N）',
  `last_heartbeat` datetime NOT NULL COMMENT '最后心跳时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sheet_student` (`sheet_id`, `student_id`),
  KEY `idx_sheet_class` (`sheet_id`, `class_code`),
  KEY `idx_heartbeat` (`last_heartbeat`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课堂实时状态表';
