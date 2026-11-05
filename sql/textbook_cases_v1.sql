-- 教师优秀课例由维护人员审核后导入，草稿不进入教师查询。
CREATE TABLE IF NOT EXISTS biz_textbook_case (
  case_id BIGINT NOT NULL AUTO_INCREMENT,
  source_key VARCHAR(100) NOT NULL,
  book_id BIGINT NOT NULL,
  section_key VARCHAR(64) NOT NULL,
  title VARCHAR(200) NOT NULL,
  author VARCHAR(100) NOT NULL DEFAULT '',
  school VARCHAR(200) NOT NULL DEFAULT '',
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  sort_no INT NOT NULL DEFAULT 0,
  PRIMARY KEY (case_id), UNIQUE KEY uk_source (source_key),
  KEY idx_section (book_id, section_key, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS biz_textbook_case_resource (
  resource_id BIGINT NOT NULL AUTO_INCREMENT,
  case_id BIGINT NOT NULL,
  resource_key VARCHAR(100) NOT NULL,
  category VARCHAR(32) NOT NULL,
  title VARCHAR(200) NOT NULL,
  filename VARCHAR(255) NOT NULL,
  original_path VARCHAR(500) NOT NULL,
  preview_path VARCHAR(500) DEFAULT NULL,
  bytes BIGINT NOT NULL DEFAULT 0,
  description TEXT,
  requirements TEXT,
  sort_no INT NOT NULL DEFAULT 0,
  PRIMARY KEY (resource_id), UNIQUE KEY uk_resource (case_id,resource_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
