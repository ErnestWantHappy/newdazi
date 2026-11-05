-- 教师课例评价与跨设备邀请限频。新增表可保留，回退旧程序无需删表。
CREATE TABLE IF NOT EXISTS biz_textbook_case_feedback (
  case_id bigint NOT NULL,
  user_id bigint NOT NULL,
  vote tinyint NOT NULL DEFAULT 0 COMMENT '1推荐 -1不推荐 0未评价或撤销',
  reason varchar(500) DEFAULT NULL COMMENT '评价原因/理由',
  last_invited_at datetime DEFAULT NULL,
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (case_id,user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师对作者课例的唯一评价';

SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_textbook_case_feedback' AND COLUMN_NAME = 'reason');
SET @stmt = IF(@col_exists = 0, 'ALTER TABLE biz_textbook_case_feedback ADD COLUMN reason varchar(500) DEFAULT NULL COMMENT ''评价原因/理由'' AFTER vote', 'SELECT 1');
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS biz_textbook_case_invitation (
  user_id bigint NOT NULL,
  last_attempt_at datetime DEFAULT NULL,
  last_invited_at datetime DEFAULT NULL,
  PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师评价邀请的全局频率';
