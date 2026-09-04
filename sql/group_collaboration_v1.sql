-- P3 非计分小组在线协作、操作轨迹与版本差异。
-- 不修改个人答案、成绩和既有全班协作房间；旧房间不写入本脚本新建的映射表即可继续兼容。
CREATE TABLE IF NOT EXISTS biz_collab_activity (
  activity_id BIGINT NOT NULL AUTO_INCREMENT,
  lesson_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  entry_year VARCHAR(16) NOT NULL,
  class_code VARCHAR(16) NOT NULL,
  snapshot_id BIGINT NOT NULL,
  activity_title VARCHAR(120) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
  frozen_time DATETIME NULL,
  creator_user_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (activity_id),
  KEY idx_collab_activity_class (lesson_id, dept_id, entry_year, class_code, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='非计分小组协作活动';

CREATE TABLE IF NOT EXISTS biz_collab_task_version (
  task_version_id BIGINT NOT NULL AUTO_INCREMENT,
  activity_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  source_material_id BIGINT NOT NULL,
  version_name VARCHAR(120) NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (task_version_id),
  KEY idx_collab_task_activity (activity_id, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协作活动起始文件版本';

CREATE TABLE IF NOT EXISTS biz_collab_group_task (
  activity_id BIGINT NOT NULL,
  snapshot_group_id BIGINT NOT NULL,
  task_version_id BIGINT NOT NULL,
  room_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (activity_id, snapshot_group_id),
  UNIQUE KEY uk_collab_group_room (room_id),
  KEY idx_collab_group_task_version (task_version_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课时小组到协作文档映射';

CREATE TABLE IF NOT EXISTS biz_collab_operation_event (
  event_id BIGINT NOT NULL AUTO_INCREMENT,
  room_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  student_id BIGINT NULL,
  event_type VARCHAR(24) NOT NULL,
  event_detail VARCHAR(500) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (event_id),
  KEY idx_collab_event_room_time (room_id, create_time),
  KEY idx_collab_event_student_time (student_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协作进入、心跳和保存触发轨迹';

CREATE TABLE IF NOT EXISTS biz_collab_revision_diff (
  revision_id BIGINT NOT NULL,
  diff_status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  summary_text VARCHAR(2000) NULL,
  error_message VARCHAR(500) NULL,
  processed_time DATETIME NULL,
  PRIMARY KEY (revision_id),
  KEY idx_collab_diff_status (diff_status, processed_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协作相邻版本差异摘要';
