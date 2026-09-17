-- ============================================================
-- P2-B3：课程自动推进下一课（幂等）
-- 库：本机 xueyeceping1（执行前确认目标库）
-- 说明：
--   1. 教师可配置是否开启；默认关闭
--   2. 默认阈值 50%、延迟 2 小时；考勤课强制不参与
--   3. 「有成绩」口径与成绩页 scoreReady 一致（请假不计）
--   4. Quartz 每 5 分钟扫描一次
--   5. 统一策略独立持久化；班级达标时间互不影响；推进历史用于短时补交
-- ============================================================

SET @db := DATABASE();

SET @exist_en := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_lesson' AND COLUMN_NAME = 'auto_advance_enabled'
);
SET @sql_en := IF(@exist_en = 0,
  'ALTER TABLE `biz_lesson` ADD COLUMN `auto_advance_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否自动推进下一课 0否1是'' AFTER `teacher_note`',
  'SELECT ''biz_lesson.auto_advance_enabled already exists'' AS info');
PREPARE stmt FROM @sql_en; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist_pct := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_lesson' AND COLUMN_NAME = 'auto_advance_threshold_pct'
);
SET @sql_pct := IF(@exist_pct = 0,
  'ALTER TABLE `biz_lesson` ADD COLUMN `auto_advance_threshold_pct` int NOT NULL DEFAULT 50 COMMENT ''有成绩人数占比阈值%'' AFTER `auto_advance_enabled`',
  'SELECT ''biz_lesson.auto_advance_threshold_pct already exists'' AS info');
PREPARE stmt FROM @sql_pct; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist_delay := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_lesson' AND COLUMN_NAME = 'auto_advance_delay_hours'
);
SET @sql_delay := IF(@exist_delay = 0,
  'ALTER TABLE `biz_lesson` ADD COLUMN `auto_advance_delay_hours` decimal(4,1) NOT NULL DEFAULT 2.0 COMMENT ''达标后延迟小时数'' AFTER `auto_advance_threshold_pct`',
  'SELECT ''biz_lesson.auto_advance_delay_hours already exists'' AS info');
PREPARE stmt FROM @sql_delay; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist_ready := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_lesson' AND COLUMN_NAME = 'auto_advance_ready_time'
);
SET @sql_ready := IF(@exist_ready = 0,
  'ALTER TABLE `biz_lesson` ADD COLUMN `auto_advance_ready_time` datetime NULL DEFAULT NULL COMMENT ''首次达标时间'' AFTER `auto_advance_delay_hours`',
  'SELECT ''biz_lesson.auto_advance_ready_time already exists'' AS info');
PREPARE stmt FROM @sql_ready; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 教师 + 学校唯一的统一策略，不再依赖是否已有常规课。
CREATE TABLE IF NOT EXISTS `biz_lesson_advance_policy` (
  `policy_id` bigint NOT NULL AUTO_INCREMENT COMMENT '策略ID',
  `teacher_id` bigint NOT NULL COMMENT '教师用户ID',
  `dept_id` bigint NOT NULL COMMENT '学校ID',
  `auto_advance_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否自动推进',
  `auto_advance_threshold_pct` int NOT NULL DEFAULT 50 COMMENT '有成绩人数占比阈值%',
  `auto_advance_delay_hours` decimal(4,1) NOT NULL DEFAULT 2.0 COMMENT '达标后延迟小时数',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`policy_id`),
  UNIQUE KEY `uk_lesson_advance_policy_teacher_dept` (`teacher_id`, `dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师统一课堂推进策略';

-- 旧课程若仅保存了 create_by，则按同校唯一用户名补齐创建人，避免找不到同教师下一课。
UPDATE biz_lesson l
INNER JOIN sys_user u
        ON u.user_name = l.create_by
       AND u.dept_id = l.dept_id
SET l.creator_id = u.user_id
WHERE l.creator_id IS NULL
  AND l.create_by IS NOT NULL
  AND l.create_by <> '';

-- 首次升级保留现有教师课程配置；已有策略不覆盖。
INSERT IGNORE INTO biz_lesson_advance_policy
    (teacher_id, dept_id, auto_advance_enabled, auto_advance_threshold_pct,
     auto_advance_delay_hours, create_by, create_time, update_by, update_time)
SELECT l.creator_id,
       l.dept_id,
       IFNULL(l.auto_advance_enabled, 0),
       IFNULL(l.auto_advance_threshold_pct, 50),
       IFNULL(l.auto_advance_delay_hours, 2.0),
       IFNULL(l.create_by, ''),
       NOW(),
       IFNULL(l.update_by, l.create_by),
       NOW()
FROM biz_lesson l
INNER JOIN (
    SELECT creator_id, dept_id, MAX(lesson_id) AS lesson_id
    FROM biz_lesson
    WHERE creator_id IS NOT NULL
      AND dept_id IS NOT NULL
      AND IFNULL(lesson_mode, 'assessment') <> 'attendance'
    GROUP BY creator_id, dept_id
) latest ON latest.lesson_id = l.lesson_id;

SET @exist_assignment_ready := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_lesson_assignment'
    AND COLUMN_NAME = 'auto_advance_ready_time'
);
SET @sql_assignment_ready := IF(@exist_assignment_ready = 0,
  'ALTER TABLE `biz_lesson_assignment` ADD COLUMN `auto_advance_ready_time` datetime NULL DEFAULT NULL COMMENT ''本班首次达标时间'' AFTER `assign_time`',
  'SELECT ''biz_lesson_assignment.auto_advance_ready_time already exists'' AS info');
PREPARE stmt FROM @sql_assignment_ready; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `biz_lesson_assignment_history` (
  `history_id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史ID',
  `assignment_id` bigint NOT NULL COMMENT '原指派ID',
  `lesson_id` bigint NOT NULL COMMENT '推进前课程ID',
  `next_lesson_id` bigint NOT NULL COMMENT '推进后课程ID',
  `entry_year` varchar(16) NOT NULL COMMENT '入学年份',
  `class_code` varchar(32) NOT NULL COMMENT '班级编号',
  `dept_id` bigint NOT NULL COMMENT '学校ID',
  `assigner_id` bigint DEFAULT NULL COMMENT '原指派教师',
  `assigned_time` datetime DEFAULT NULL COMMENT '原指派时间',
  `advanced_by` bigint DEFAULT NULL COMMENT '推进操作者；自动推进为课程创建人',
  `advance_source` varchar(16) NOT NULL COMMENT 'MANUAL/AUTO',
  `advanced_time` datetime NOT NULL COMMENT '推进时间',
  PRIMARY KEY (`history_id`),
  KEY `idx_assignment_history_grace` (`dept_id`, `entry_year`, `class_code`, `lesson_id`, `advanced_time`),
  KEY `idx_assignment_history_assignment` (`assignment_id`, `advanced_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程班级推进历史';

-- Quartz：每 5 分钟扫描自动推进
UPDATE sys_job
SET job_name = '课程自动推进下一课',
    job_group = 'DEFAULT',
    cron_expression = '0 0/5 * * * ?',
    misfire_policy = '3',
    concurrent = '1',
    status = '0',
    update_by = 'admin',
    update_time = NOW(),
    remark = '扫描开启自动推进的测评课；班级有成绩达阈值并过延迟后切到下一课；考勤课不参与'
WHERE invoke_target = 'lessonAutoAdvanceTask.scanAndAdvance';

INSERT INTO sys_job
    (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status,
     create_by, create_time, remark)
SELECT '课程自动推进下一课',
       'DEFAULT',
       'lessonAutoAdvanceTask.scanAndAdvance',
       '0 0/5 * * * ?',
       '3',
       '1',
       '0',
       'admin',
       NOW(),
       '扫描开启自动推进的测评课；班级有成绩达阈值并过延迟后切到下一课；考勤课不参与'
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_job
    WHERE invoke_target = 'lessonAutoAdvanceTask.scanAndAdvance'
);

-- 历史库若重复插入过同一任务，只保留最早一条启用，其他记录保留但停用，避免重复扫描。
UPDATE sys_job j
INNER JOIN (
    SELECT MIN(job_id) AS keep_job_id
    FROM sys_job
    WHERE invoke_target = 'lessonAutoAdvanceTask.scanAndAdvance'
) keep_job ON 1 = 1
SET j.status = '1',
    j.update_by = 'admin',
    j.update_time = NOW(),
    j.remark = CONCAT(IFNULL(j.remark, ''), '；重复任务已停用')
WHERE j.invoke_target = 'lessonAutoAdvanceTask.scanAndAdvance'
  AND j.job_id <> keep_job.keep_job_id
  AND j.status <> '1';

SELECT job_id, job_name, invoke_target, cron_expression, status, concurrent
FROM sys_job
WHERE invoke_target = 'lessonAutoAdvanceTask.scanAndAdvance';
