-- ============================================================
-- 考勤课（P0-4 B1+B2）幂等增量
-- 库：本机 xueyeceping1（执行前确认目标库）
-- 说明：
--   1. biz_lesson.lesson_mode = assessment|attendance（默认 assessment）
--   2. teacher_note：教师给学生看的课堂说明
--   3. biz_lesson_checkin：学生签到记录（不计作业分）
-- ============================================================

-- 1) 课程用途字段
SET @db := DATABASE();

SET @exist_mode := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_lesson' AND COLUMN_NAME = 'lesson_mode'
);
SET @sql_mode := IF(@exist_mode = 0,
  'ALTER TABLE `biz_lesson` ADD COLUMN `lesson_mode` varchar(20) NOT NULL DEFAULT ''assessment'' COMMENT ''课程用途: assessment测评/attendance考勤'' AFTER `random_judgment_count`',
  'SELECT ''biz_lesson.lesson_mode already exists'' AS info');
PREPARE stmt FROM @sql_mode; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist_note := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_lesson' AND COLUMN_NAME = 'teacher_note'
);
SET @sql_note := IF(@exist_note = 0,
  'ALTER TABLE `biz_lesson` ADD COLUMN `teacher_note` varchar(500) NULL DEFAULT NULL COMMENT ''教师课堂说明（学生可见）'' AFTER `lesson_mode`',
  'SELECT ''biz_lesson.teacher_note already exists'' AS info');
PREPARE stmt FROM @sql_note; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2) 签到表
CREATE TABLE IF NOT EXISTS `biz_lesson_checkin` (
  `checkin_id`   bigint       NOT NULL AUTO_INCREMENT COMMENT '签到ID',
  `lesson_id`    bigint       NOT NULL COMMENT '课程ID',
  `student_id`   bigint       NOT NULL COMMENT '学生ID',
  `dept_id`      bigint       DEFAULT NULL COMMENT '学校ID',
  `checkin_time` datetime     NOT NULL COMMENT '签到时间',
  `create_by`    varchar(64)  DEFAULT '' COMMENT '创建者',
  `create_time`  datetime     DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`checkin_id`),
  UNIQUE KEY `uk_lesson_student` (`lesson_id`, `student_id`),
  KEY `idx_lesson_checkin_time` (`lesson_id`, `checkin_time`),
  KEY `idx_student_checkin` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课堂考勤签到';

-- 3) 历史数据默认测评课
UPDATE `biz_lesson`
SET `lesson_mode` = 'assessment'
WHERE `lesson_mode` IS NULL OR `lesson_mode` = '';
