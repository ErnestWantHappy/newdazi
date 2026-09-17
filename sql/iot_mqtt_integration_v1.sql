-- 初中物联网 P2 最小接入（MySQL 8，幂等）
-- 只保存平台业务凭据哈希；Broker 管理凭据由部署环境注入，不进入本表。

CREATE TABLE IF NOT EXISTS biz_iot_experiment (
  experiment_id BIGINT NOT NULL AUTO_INCREMENT,
  lesson_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  activity_code VARCHAR(64) NOT NULL,
  title VARCHAR(128) NOT NULL,
  description VARCHAR(1000) DEFAULT NULL,
  topic_prefix VARCHAR(192) NOT NULL,
  status CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) DEFAULT NULL,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (experiment_id),
  UNIQUE KEY uk_iot_experiment_lesson_activity (lesson_id, activity_code),
  KEY idx_iot_experiment_dept (dept_id),
  CONSTRAINT fk_iot_experiment_lesson FOREIGN KEY (lesson_id) REFERENCES biz_lesson (lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联网课堂实验';

CREATE TABLE IF NOT EXISTS biz_iot_group (
  group_id BIGINT NOT NULL AUTO_INCREMENT,
  experiment_id BIGINT NOT NULL,
  entry_year VARCHAR(16) NOT NULL,
  class_code VARCHAR(32) NOT NULL,
  group_code VARCHAR(32) NOT NULL,
  group_name VARCHAR(64) NOT NULL,
  status CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (group_id),
  UNIQUE KEY uk_iot_group_scope (experiment_id, entry_year, class_code, group_code),
  KEY idx_iot_group_experiment (experiment_id),
  CONSTRAINT fk_iot_group_experiment FOREIGN KEY (experiment_id) REFERENCES biz_iot_experiment (experiment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联网实验小组';

CREATE TABLE IF NOT EXISTS biz_iot_device (
  device_id BIGINT NOT NULL AUTO_INCREMENT,
  group_id BIGINT NOT NULL,
  device_code VARCHAR(64) NOT NULL,
  device_name VARCHAR(128) NOT NULL,
  broker_username VARCHAR(128) DEFAULT NULL,
  credential_hash VARCHAR(255) DEFAULT NULL,
  credential_expires_at DATETIME DEFAULT NULL,
  status CHAR(1) NOT NULL DEFAULT '0',
  last_seen_at DATETIME DEFAULT NULL,
  create_by VARCHAR(64) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (device_id),
  UNIQUE KEY uk_iot_device_group_code (group_id, device_code),
  KEY idx_iot_device_username (broker_username),
  CONSTRAINT fk_iot_device_group FOREIGN KEY (group_id) REFERENCES biz_iot_group (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联网实验设备';

CREATE TABLE IF NOT EXISTS biz_iot_message (
  message_id BIGINT NOT NULL AUTO_INCREMENT,
  experiment_id BIGINT NOT NULL,
  group_id BIGINT NOT NULL,
  device_id BIGINT NOT NULL,
  topic VARCHAR(255) NOT NULL,
  payload_type VARCHAR(16) NOT NULL,
  payload_text VARCHAR(16000) NOT NULL,
  payload_number DECIMAL(20,6) DEFAULT NULL,
  source_ip VARCHAR(64) DEFAULT NULL,
  qos TINYINT DEFAULT NULL,
  retained TINYINT NOT NULL DEFAULT 0,
  received_at DATETIME(3) NOT NULL,
  PRIMARY KEY (message_id),
  KEY idx_iot_message_experiment_time (experiment_id, received_at),
  KEY idx_iot_message_device_time (device_id, received_at),
  CONSTRAINT fk_iot_message_experiment FOREIGN KEY (experiment_id) REFERENCES biz_iot_experiment (experiment_id),
  CONSTRAINT fk_iot_message_group FOREIGN KEY (group_id) REFERENCES biz_iot_group (group_id),
  CONSTRAINT fk_iot_message_device FOREIGN KEY (device_id) REFERENCES biz_iot_device (device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联网消息存档';

CREATE TABLE IF NOT EXISTS biz_iot_event (
  event_id BIGINT NOT NULL AUTO_INCREMENT,
  experiment_id BIGINT DEFAULT NULL,
  group_id BIGINT DEFAULT NULL,
  device_id BIGINT DEFAULT NULL,
  event_type VARCHAR(32) NOT NULL,
  diagnostic_stage VARCHAR(32) NOT NULL,
  detail VARCHAR(1000) NOT NULL,
  payload_digest CHAR(64) DEFAULT NULL,
  occurred_at DATETIME(3) NOT NULL,
  PRIMARY KEY (event_id),
  KEY idx_iot_event_experiment_time (experiment_id, occurred_at),
  KEY idx_iot_event_device_time (device_id, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物联网连接与诊断事件';
