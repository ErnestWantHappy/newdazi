-- 学情 V3 独立存储，不修改原成绩、评分或 AI 采用记录。执行前备份目标库。
CREATE TABLE IF NOT EXISTS biz_practical_insight_report (
  report_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  teacher_id BIGINT NOT NULL, dept_id BIGINT NOT NULL,
  lesson_id BIGINT NOT NULL, question_id BIGINT NOT NULL,
  entry_year VARCHAR(10) NOT NULL, class_code VARCHAR(20) NOT NULL,
  fingerprint CHAR(64) NOT NULL, status VARCHAR(20) NOT NULL, stage VARCHAR(40), run_token CHAR(36),
  input_json LONGTEXT NOT NULL, report_json LONGTEXT,
  completed INT NOT NULL DEFAULT 0, failed INT NOT NULL DEFAULT 0,
  prompt_tokens BIGINT NOT NULL DEFAULT 0, completion_tokens BIGINT NOT NULL DEFAULT 0,
  error_message VARCHAR(255), created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_insight_input (teacher_id, fingerprint),
  KEY idx_insight_scope (teacher_id, lesson_id, question_id, entry_year, class_code),
  KEY idx_insight_recovery (status, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级学情诊断任务和报告';
CREATE TABLE IF NOT EXISTS biz_practical_insight_paper_map (
  teacher_id BIGINT NOT NULL, fingerprint CHAR(64) NOT NULL,
  paper_json LONGTEXT NOT NULL, created_at DATETIME NOT NULL,
  PRIMARY KEY (teacher_id, fingerprint)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='版本化整卷知识点映射';
CREATE TABLE IF NOT EXISTS biz_practical_insight_student_evidence (
  report_id BIGINT NOT NULL, student_id BIGINT NOT NULL, fingerprint CHAR(64) NOT NULL,
  status VARCHAR(20) NOT NULL, result_json LONGTEXT, updated_at DATETIME NOT NULL,
  PRIMARY KEY (report_id, student_id), KEY idx_insight_evidence (fingerprint)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='逐人作答诊断证据';
