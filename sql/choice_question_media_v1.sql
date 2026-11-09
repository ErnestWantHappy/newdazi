-- 选择题图片增量；只新增可空列和本功能表，不改写旧题、答案或成绩。
SET @media_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='biz_question' AND column_name='media_json')=0,'ALTER TABLE biz_question ADD COLUMN media_json TEXT NULL COMMENT ''题干与选项图片引用''','SELECT 1');
PREPARE media_stmt FROM @media_ddl;
EXECUTE media_stmt;
DEALLOCATE PREPARE media_stmt;
SET @media_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='biz_county_exam_paper_question' AND column_name='media_json')=0,'ALTER TABLE biz_county_exam_paper_question ADD COLUMN media_json TEXT NULL COMMENT ''题目图片引用快照''','SELECT 1');
PREPARE media_stmt FROM @media_ddl;
EXECUTE media_stmt;
DEALLOCATE PREPARE media_stmt;
CREATE TABLE IF NOT EXISTS biz_question_image (
 image_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 uploader_id BIGINT NOT NULL,
 resource_path VARCHAR(500) NOT NULL,
 mime_type VARCHAR(64) NOT NULL,
 byte_size BIGINT NOT NULL,
 width INT NOT NULL,
 height INT NOT NULL,
 sha256 CHAR(64) NOT NULL,
 state VARCHAR(16) NOT NULL DEFAULT 'TEMP',
 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 bound_time DATETIME NULL,
 UNIQUE KEY uk_question_image_path(resource_path),
 KEY idx_question_image_owner(uploader_id,state,create_time),
 KEY idx_question_image_cleanup(state,create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目图片元数据，已绑定原图保留';
CREATE TABLE IF NOT EXISTS biz_question_save_receipt (
 receipt_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 owner_id BIGINT NOT NULL,
 request_id VARCHAR(64) NOT NULL,
 payload_hash CHAR(64) NOT NULL,
 source_question_id BIGINT NULL,
 question_id BIGINT NULL,
 save_mode VARCHAR(16) NULL,
 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uk_question_save_request(owner_id,request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目保存幂等回执';
CREATE TABLE IF NOT EXISTS biz_question_import_batch (
 batch_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 owner_id BIGINT NOT NULL,
 request_id VARCHAR(64) NOT NULL,
 file_hash CHAR(64) NOT NULL,
 original_name VARCHAR(255) NOT NULL,
 state VARCHAR(16) NOT NULL,
 revision BIGINT NOT NULL DEFAULT 1,
 question_count INT NOT NULL DEFAULT 0,
 image_count INT NOT NULL DEFAULT 0,
 excluded_count INT NOT NULL DEFAULT 0,
 issues_json MEDIUMTEXT NULL,
 result_json MEDIUMTEXT NULL,
 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uk_question_import_request(owner_id,request_id),
 KEY idx_question_import_owner(owner_id,create_time),
 KEY idx_question_import_hash(owner_id,file_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库图文导入预览与结果';
CREATE TABLE IF NOT EXISTS biz_question_import_item (
 item_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 batch_id BIGINT NOT NULL,
 source_key VARCHAR(128) NOT NULL,
 sheet_name VARCHAR(128) NOT NULL,
 source_row INT NOT NULL,
 draft_json MEDIUMTEXT NOT NULL,
 issues_json TEXT NULL,
 included TINYINT NOT NULL DEFAULT 1,
 question_id BIGINT NULL,
 UNIQUE KEY uk_question_import_source(batch_id,source_key),
 KEY idx_question_import_items(batch_id,item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='逐题图文草稿，不代表正式题库';
