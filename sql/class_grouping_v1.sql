-- P2 通用班级分组、课时快照、教师桌面布局
CREATE TABLE IF NOT EXISTS biz_class_group_scheme (
  scheme_id BIGINT NOT NULL AUTO_INCREMENT,
  dept_id BIGINT NOT NULL,
  entry_year VARCHAR(16) NOT NULL,
  class_code VARCHAR(16) NOT NULL,
  scheme_name VARCHAR(100) NOT NULL,
  scheme_version INT NOT NULL DEFAULT 1,
  status CHAR(1) NOT NULL DEFAULT '0',
  creator_user_id BIGINT NOT NULL,
  create_time DATETIME NULL,
  update_time DATETIME NULL,
  PRIMARY KEY (scheme_id),
  UNIQUE KEY uk_group_scheme (dept_id,entry_year,class_code,scheme_name,scheme_version),
  KEY idx_group_scheme_class (dept_id,entry_year,class_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS biz_class_group (
  group_id BIGINT NOT NULL AUTO_INCREMENT,
  scheme_id BIGINT NOT NULL,
  group_no INT NOT NULL,
  group_name VARCHAR(100) NOT NULL,
  color VARCHAR(20) NULL,
  sort_no INT NOT NULL DEFAULT 0,
  leader_student_id BIGINT NULL,
  PRIMARY KEY (group_id),
  UNIQUE KEY uk_group_no (scheme_id,group_no),
  KEY idx_group_scheme (scheme_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS biz_class_group_member (
  scheme_id BIGINT NOT NULL,
  group_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  PRIMARY KEY (scheme_id,student_id),
  UNIQUE KEY uk_group_member (group_id,student_id),
  KEY idx_group_member_group (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS biz_lesson_group_snapshot (
  snapshot_id BIGINT NOT NULL AUTO_INCREMENT,
  lesson_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  entry_year VARCHAR(16) NOT NULL,
  class_code VARCHAR(16) NOT NULL,
  source_scheme_id BIGINT NULL,
  source_scheme_version INT NULL,
  snapshot_hash VARCHAR(64) NULL,
  frozen_time DATETIME NOT NULL,
  PRIMARY KEY (snapshot_id),
  UNIQUE KEY uk_lesson_snapshot_class (lesson_id,dept_id,entry_year,class_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS biz_lesson_group_snapshot_group (
  snapshot_group_id BIGINT NOT NULL AUTO_INCREMENT,
  snapshot_id BIGINT NOT NULL,
  group_no INT NOT NULL,
  group_name VARCHAR(100) NOT NULL,
  color VARCHAR(20) NULL,
  sort_no INT NOT NULL DEFAULT 0,
  leader_student_id BIGINT NULL,
  PRIMARY KEY (snapshot_group_id),
  UNIQUE KEY uk_snapshot_group_no (snapshot_id,group_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS biz_lesson_group_snapshot_member (
  snapshot_id BIGINT NOT NULL,
  snapshot_group_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  PRIMARY KEY (snapshot_id,student_id),
  KEY idx_snapshot_member_group (snapshot_group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS biz_classroom_layout (
  layout_id BIGINT NOT NULL AUTO_INCREMENT,
  teacher_user_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  entry_year VARCHAR(16) NOT NULL,
  class_code VARCHAR(16) NOT NULL,
  columns_count INT NOT NULL DEFAULT 6,
  layout_version INT NOT NULL DEFAULT 1,
  update_time DATETIME NULL,
  PRIMARY KEY (layout_id),
  UNIQUE KEY uk_classroom_layout (teacher_user_id,dept_id,entry_year,class_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS biz_classroom_layout_item (
  layout_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  grid_row INT NOT NULL DEFAULT 0,
  grid_col INT NOT NULL DEFAULT 0,
  sort_no INT NOT NULL DEFAULT 0,
  PRIMARY KEY (layout_id,student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
