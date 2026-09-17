-- Python 刷题独立业务域 v1
-- 本脚本不修改课程、biz_student_answer 或既有 biz_programming_* 表。
-- 执行前备份目标库；发布前由服务层再次校验题目重复、教师班级范围和唯一已发布版本。

CREATE TABLE IF NOT EXISTS biz_python_practice_plan (
    plan_id BIGINT NOT NULL AUTO_INCREMENT,
    dept_id BIGINT NOT NULL,
    grade BIGINT NOT NULL,
    semester CHAR(1) NOT NULL,
    entry_year VARCHAR(16) NOT NULL,
    plan_name VARCHAR(128) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    current_version_no INT NOT NULL DEFAULT 0,
    creator_id BIGINT NOT NULL,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL,
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL,
    PRIMARY KEY (plan_id),
    UNIQUE KEY uk_python_practice_plan_scope (dept_id, grade, semester, entry_year),
    KEY idx_python_practice_plan_status (dept_id, entry_year, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Python刷题年级基础题单';

CREATE TABLE IF NOT EXISTS biz_python_practice_plan_version (
    plan_version_id BIGINT NOT NULL AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    version_no INT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    published_time DATETIME NULL,
    creator_id BIGINT NOT NULL,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL,
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL,
    PRIMARY KEY (plan_version_id),
    UNIQUE KEY uk_python_practice_plan_version (plan_id, version_no),
    KEY idx_python_practice_plan_version_status (plan_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Python刷题基础题单版本';

CREATE TABLE IF NOT EXISTS biz_python_practice_question_snapshot (
    snapshot_id BIGINT NOT NULL AUTO_INCREMENT,
    source_type VARCHAR(16) NOT NULL COMMENT 'BASE_VERSION或EXTENSION',
    source_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    snapshot_hash VARCHAR(64) NOT NULL DEFAULT '',
    question_title VARCHAR(255) NULL,
    difficulty VARCHAR(32) NULL,
    knowledge_points VARCHAR(500) NULL,
    no_input CHAR(1) NOT NULL DEFAULT '0',
    question_content MEDIUMTEXT NOT NULL,
    input_description MEDIUMTEXT NULL,
    output_description MEDIUMTEXT NULL,
    sample_explanation MEDIUMTEXT NULL,
    constraints_text MEDIUMTEXT NULL,
    notes_text MEDIUMTEXT NULL,
    starter_code MEDIUMTEXT NULL,
    language_code VARCHAR(32) NOT NULL DEFAULT 'python',
    time_limit_seconds DECIMAL(5,2) NOT NULL DEFAULT 2.00,
    memory_limit_kb INT NOT NULL DEFAULT 131072,
    max_processes SMALLINT NOT NULL DEFAULT 8,
    max_file_size_kb INT NOT NULL DEFAULT 1024,
    max_output_kb INT NOT NULL DEFAULT 64,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL,
    PRIMARY KEY (snapshot_id),
    UNIQUE KEY uk_python_practice_snapshot_source_question (source_type, source_id, question_id),
    KEY idx_python_practice_snapshot_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Python刷题题面和判题规则快照';

CREATE TABLE IF NOT EXISTS biz_python_practice_snapshot_case (
    snapshot_case_id BIGINT NOT NULL AUTO_INCREMENT,
    snapshot_id BIGINT NOT NULL,
    case_name VARCHAR(128) NOT NULL DEFAULT '',
    input_text MEDIUMTEXT NULL,
    expected_output MEDIUMTEXT NOT NULL,
    is_public CHAR(1) NOT NULL DEFAULT '0',
    score_weight DECIMAL(10,2) NOT NULL DEFAULT 1.00,
    order_num INT NOT NULL DEFAULT 0,
    PRIMARY KEY (snapshot_case_id),
    UNIQUE KEY uk_python_practice_snapshot_case_order (snapshot_id, order_num),
    KEY idx_python_practice_snapshot_case_public (snapshot_id, is_public, order_num)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Python刷题快照测试点';

CREATE TABLE IF NOT EXISTS biz_python_practice_plan_question (
    plan_question_id BIGINT NOT NULL AUTO_INCREMENT,
    plan_version_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    snapshot_id BIGINT NOT NULL,
    sort_no INT NOT NULL,
    stage VARCHAR(32) NOT NULL DEFAULT 'BEGINNER',
    required_flag CHAR(1) NOT NULL DEFAULT '1',
    PRIMARY KEY (plan_question_id),
    UNIQUE KEY uk_python_practice_plan_question (plan_version_id, question_id),
    UNIQUE KEY uk_python_practice_plan_sort (plan_version_id, sort_no),
    KEY idx_python_practice_plan_question_snapshot (snapshot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Python刷题基础题单题目';

CREATE TABLE IF NOT EXISTS biz_python_practice_extension (
    extension_id BIGINT NOT NULL AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    extension_name VARCHAR(128) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    creator_id BIGINT NOT NULL,
    published_time DATETIME NULL,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL,
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL,
    PRIMARY KEY (extension_id),
    KEY idx_python_practice_extension_plan_status (plan_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Python刷题班级加练包';

CREATE TABLE IF NOT EXISTS biz_python_practice_extension_class (
    extension_class_id BIGINT NOT NULL AUTO_INCREMENT,
    extension_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    entry_year VARCHAR(16) NOT NULL,
    class_code VARCHAR(32) NOT NULL,
    PRIMARY KEY (extension_class_id),
    UNIQUE KEY uk_python_practice_extension_class (extension_id, dept_id, entry_year, class_code),
    KEY idx_python_practice_extension_class_scope (dept_id, entry_year, class_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Python刷题加练包班级范围';

CREATE TABLE IF NOT EXISTS biz_python_practice_extension_question (
    extension_question_id BIGINT NOT NULL AUTO_INCREMENT,
    extension_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    snapshot_id BIGINT NOT NULL,
    sort_no INT NOT NULL,
    PRIMARY KEY (extension_question_id),
    UNIQUE KEY uk_python_practice_extension_question (extension_id, question_id),
    UNIQUE KEY uk_python_practice_extension_sort (extension_id, sort_no),
    KEY idx_python_practice_extension_question_snapshot (snapshot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Python刷题加练包题目';

CREATE TABLE IF NOT EXISTS biz_python_practice_draft (
    draft_id BIGINT NOT NULL AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    source_type VARCHAR(16) NOT NULL,
    source_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    snapshot_id BIGINT NOT NULL,
    source_code MEDIUMTEXT NOT NULL,
    update_time DATETIME NOT NULL,
    PRIMARY KEY (draft_id),
    UNIQUE KEY uk_python_practice_draft_scope (student_id, source_type, source_id, question_id),
    KEY idx_python_practice_draft_student (student_id, update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Python刷题学生草稿';

CREATE TABLE IF NOT EXISTS biz_python_practice_submission (
    submission_id BIGINT NOT NULL AUTO_INCREMENT,
    submission_key VARCHAR(64) NOT NULL,
    student_id BIGINT NOT NULL,
    source_type VARCHAR(16) NOT NULL,
    source_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    snapshot_id BIGINT NOT NULL,
    source_code MEDIUMTEXT NOT NULL,
    custom_input MEDIUMTEXT NULL COMMENT 'CUSTOM_RUN 的学生自定义输入',
    submit_type VARCHAR(16) NOT NULL COMMENT 'RUN、SUBMIT或CUSTOM_RUN',
    status_code VARCHAR(32) NOT NULL DEFAULT 'WAITING',
    status_message VARCHAR(255) NOT NULL DEFAULT '',
    score INT NULL,
    passed_case_count INT NOT NULL DEFAULT 0,
    total_case_count INT NOT NULL DEFAULT 0,
    judge_summary VARCHAR(1000) NULL,
    submitted_at DATETIME NOT NULL,
    judged_at DATETIME NULL,
    cancelled_at DATETIME NULL,
    PRIMARY KEY (submission_id),
    UNIQUE KEY uk_python_practice_submission_key (student_id, source_type, source_id, question_id, submission_key),
    KEY idx_python_practice_submission_student (student_id, submitted_at),
    KEY idx_python_practice_submission_scope (source_type, source_id, question_id, status_code),
    KEY idx_python_practice_submission_recovery (status_code, submitted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Python刷题提交历史';

CREATE TABLE IF NOT EXISTS biz_python_practice_submission_case (
    submission_case_id BIGINT NOT NULL AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    snapshot_case_id BIGINT NULL,
    case_name VARCHAR(128) NOT NULL DEFAULT '',
    is_public CHAR(1) NOT NULL DEFAULT '0',
    status_code VARCHAR(32) NOT NULL,
    judge0_status_id INT NULL,
    time_seconds DECIMAL(10,3) NULL,
    memory_kb INT NULL,
    output_text MEDIUMTEXT NULL,
    error_summary VARCHAR(1000) NULL,
    order_num INT NOT NULL DEFAULT 0,
    PRIMARY KEY (submission_case_id),
    UNIQUE KEY uk_python_practice_submission_case_order (submission_id, order_num),
    KEY idx_python_practice_submission_case_submission (submission_id, is_public, order_num)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Python刷题逐测试点判题结果';

CREATE TABLE IF NOT EXISTS biz_python_practice_progress (
    progress_id BIGINT NOT NULL AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    source_type VARCHAR(16) NOT NULL,
    source_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    best_score INT NOT NULL DEFAULT 0,
    passed_flag CHAR(1) NOT NULL DEFAULT '0',
    submit_count INT NOT NULL DEFAULT 0,
    first_pass_time DATETIME NULL,
    last_practice_time DATETIME NULL,
    PRIMARY KEY (progress_id),
    UNIQUE KEY uk_python_practice_progress_scope (student_id, source_type, source_id, question_id),
    KEY idx_python_practice_progress_source (source_type, source_id, question_id),
    KEY idx_python_practice_progress_student (student_id, last_practice_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Python刷题学生进度';

-- 教师端唯一一级菜单；按钮权限由后端注解和服务层范围校验共同保证。
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type,
  visible, status, perms, icon, create_by, create_time, remark)
SELECT 'Python刷题', 0, 8, 'python-practice', 'business/pythonPractice/index', 'PythonPractice', 1, 0, 'C',
  '0', '0', 'business:pythonPractice:query', 'code', 'system', NOW(), '独立 Python 刷题，不计入课程成绩'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'business:pythonPractice:query' AND menu_type = 'C');

SET @python_practice_menu_id := (SELECT menu_id FROM sys_menu WHERE perms = 'business:pythonPractice:query' AND menu_type = 'C' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
  visible, status, perms, icon, create_by, create_time)
SELECT p.menu_name, @python_practice_menu_id, p.order_num, '#', '', 1, 0, 'F', '1', '0', p.perms, '#', 'system', NOW()
FROM (
  SELECT 'Python刷题配置' menu_name, 1 order_num, 'business:pythonPractice:edit' perms UNION ALL
  SELECT 'Python刷题发布', 2, 'business:pythonPractice:publish' UNION ALL
  SELECT 'Python刷题学情', 3, 'business:pythonPractice:analytics'
) p
WHERE @python_practice_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.perms = p.perms);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.perms IN (
  'business:pythonPractice:query', 'business:pythonPractice:edit',
  'business:pythonPractice:publish', 'business:pythonPractice:analytics'
)
WHERE r.role_key IN ('admin', 'teacher', 'researcher')
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id);

SELECT
  (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name LIKE 'biz_python_practice_%') AS practice_table_count,
  (SELECT COUNT(*) FROM sys_menu WHERE perms LIKE 'business:pythonPractice:%') AS practice_menu_count;
