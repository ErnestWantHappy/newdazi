-- ======================================================================
-- 初中物联网方案 A 班级分组与 6 位课堂口令增量迁移（MySQL 8，幂等）
-- 包含：
--   1. biz_iot_class_config: 班级级账号、加密口令、每组人数与分组快照版本
--   2. biz_iot_group_student: 学生与小组关系快照
--   3. biz_iot_group 增强: 支持 group_no 与 topic 冗余
-- ======================================================================

-- 1. 班级实验配置表
CREATE TABLE IF NOT EXISTS biz_iot_class_config (
  config_id BIGINT NOT NULL AUTO_INCREMENT,
  experiment_id BIGINT NOT NULL COMMENT '实验ID',
  dept_id BIGINT NOT NULL COMMENT '学校ID',
  entry_year VARCHAR(16) NOT NULL COMMENT '届别/入学年份',
  class_code VARCHAR(32) NOT NULL COMMENT '班级编号',
  group_size INT NOT NULL DEFAULT 4 COMMENT '每组人数',
  mqtt_username VARCHAR(128) NOT NULL COMMENT '班级MQTT账号',
  passcode_ciphertext VARCHAR(255) DEFAULT NULL COMMENT '6位课堂口令密文(AES-GCM)',
  passcode_hash VARCHAR(255) DEFAULT NULL COMMENT '6位课堂口令哈希',
  passcode_version INT NOT NULL DEFAULT 1 COMMENT '口令版本号',
  group_version INT NOT NULL DEFAULT 1 COMMENT '分组版本号',
  passcode_updated_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '口令最后轮换时间',
  grouped_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '分组生成时间',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态(0正常 1停用)',
  create_by VARCHAR(64) DEFAULT NULL COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (config_id),
  UNIQUE KEY uk_iot_class_exp (experiment_id, entry_year, class_code),
  KEY idx_iot_class_dept (dept_id),
  CONSTRAINT fk_iot_class_config_exp FOREIGN KEY (experiment_id) REFERENCES biz_iot_experiment (experiment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联网实验班级配置与口令快照';

-- 2. 学生分组快照表
CREATE TABLE IF NOT EXISTS biz_iot_group_student (
  group_student_id BIGINT NOT NULL AUTO_INCREMENT,
  group_id BIGINT NOT NULL COMMENT '小组ID',
  experiment_id BIGINT NOT NULL COMMENT '实验ID',
  student_id BIGINT NOT NULL COMMENT '学生ID',
  student_no VARCHAR(32) NOT NULL COMMENT '学号快照',
  student_name VARCHAR(64) NOT NULL COMMENT '姓名快照',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '组内排序序号',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (group_student_id),
  UNIQUE KEY uk_iot_exp_student (experiment_id, student_id),
  KEY idx_iot_gs_group (group_id),
  KEY idx_iot_gs_exp_no (experiment_id, student_no),
  CONSTRAINT fk_iot_gs_group FOREIGN KEY (group_id) REFERENCES biz_iot_group (group_id),
  CONSTRAINT fk_iot_gs_exp FOREIGN KEY (experiment_id) REFERENCES biz_iot_experiment (experiment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联网实验学生分组快照';

-- 3. 增强 biz_iot_group 表（幂等检查添加 group_no 与 topic 列）
SET @dbname = DATABASE();
SET @tablename = 'biz_iot_group';
SET @columnname = 'group_no';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',
  'ALTER TABLE biz_iot_group ADD COLUMN group_no INT NOT NULL DEFAULT 1 AFTER group_code'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @columnname = 'topic';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',
  'ALTER TABLE biz_iot_group ADD COLUMN topic VARCHAR(255) DEFAULT NULL AFTER group_name'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @columnname = 'last_seen_at';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',
  'ALTER TABLE biz_iot_group ADD COLUMN last_seen_at DATETIME DEFAULT NULL COMMENT \'最后活跃时间\' AFTER topic'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

