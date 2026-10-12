-- 发布时冻结个人反馈，重新生成报告不会自动覆盖学生已经看到的内容。
CREATE TABLE IF NOT EXISTS biz_student_feedback_publication (
 publication_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 report_id BIGINT NOT NULL, content_hash CHAR(64) NOT NULL, teacher_id BIGINT NOT NULL, dept_id BIGINT NOT NULL,
 lesson_id BIGINT NOT NULL, question_id BIGINT NOT NULL,
 entry_year VARCHAR(10) NOT NULL, class_code VARCHAR(20) NOT NULL,
 published_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uk_feedback_report_content(report_id,content_hash),
 KEY idx_feedback_scope(dept_id,lesson_id,question_id,entry_year,class_code,publication_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 兼容同一候选版本的重复迁移；旧发布内容仍保留，不自动替换学生反馈。
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
 AND table_name='biz_student_feedback_publication' AND column_name='content_hash'), 'SELECT 1',
 'ALTER TABLE biz_student_feedback_publication ADD COLUMN content_hash CHAR(64) NULL AFTER report_id');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
UPDATE biz_student_feedback_publication SET content_hash=SHA2(CONCAT('legacy:',publication_id),256) WHERE content_hash IS NULL;
ALTER TABLE biz_student_feedback_publication MODIFY content_hash CHAR(64) NOT NULL;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE()
 AND table_name='biz_student_feedback_publication' AND index_name='uk_feedback_report'),
 'ALTER TABLE biz_student_feedback_publication DROP INDEX uk_feedback_report', 'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE()
 AND table_name='biz_student_feedback_publication' AND index_name='uk_feedback_report_content'), 'SELECT 1',
 'ALTER TABLE biz_student_feedback_publication ADD UNIQUE KEY uk_feedback_report_content(report_id,content_hash)');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
CREATE TABLE IF NOT EXISTS biz_student_feedback_snapshot (
 publication_id BIGINT NOT NULL, student_id BIGINT NOT NULL,
 source_version_id BIGINT NULL, payload_json JSON NOT NULL,
 PRIMARY KEY(publication_id,student_id), KEY idx_feedback_student(student_id,publication_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
