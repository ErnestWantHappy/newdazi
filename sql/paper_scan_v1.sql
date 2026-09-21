-- 已废弃（2026-09-17）：方案 A 复用原作品与成绩表，本脚本仅保留历史，禁止作为新功能迁移执行。
-- 纸质扫描 P0 历史设计：独立中间层，不改任何核心表。
-- 正式成绩仍只写 biz_student_answer.score，扫描侧用 final_answer_id 回指。
-- 全部幂等：重复执行不报错、不产生重复结构。

CREATE TABLE IF NOT EXISTS biz_scan_batch (
    batch_id BIGINT NOT NULL AUTO_INCREMENT,
    lesson_id BIGINT NOT NULL COMMENT '课程ID',
    dept_id BIGINT NOT NULL COMMENT '学校ID(sys_dept)',
    entry_year VARCHAR(16) NOT NULL COMMENT '入学年份',
    class_code VARCHAR(32) NOT NULL COMMENT '班级编号01-99',
    scanner_user_id BIGINT NOT NULL COMMENT '上传教师用户ID',
    upload_mode VARCHAR(24) NOT NULL DEFAULT 'MULTI_PDF' COMMENT 'MULTI_PDF多份PDF/CLASS_PDF整班单PDF',
    batch_status VARCHAR(24) NOT NULL DEFAULT 'UPLOADED' COMMENT 'UPLOADED/PARTIAL/COMPLETED/FAILED',
    page_count INT NOT NULL DEFAULT 0 COMMENT '已页化总页数',
    request_id VARCHAR(64) NOT NULL COMMENT '批次幂等键，前端生成',
    error_message VARCHAR(500) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (batch_id),
    UNIQUE KEY uk_scan_batch_request (request_id),
    KEY idx_scan_batch_scope (lesson_id, dept_id, entry_year, class_code, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='纸质扫描批次，一次上传任务';

CREATE TABLE IF NOT EXISTS biz_scan_source_file (
    file_id BIGINT NOT NULL AUTO_INCREMENT,
    batch_id BIGINT NOT NULL COMMENT '所属批次',
    original_file_name VARCHAR(255) NOT NULL COMMENT '原始文件名，仅展示不参与身份匹配',
    resource_path VARCHAR(500) NOT NULL COMMENT '私存原文件路径',
    file_size BIGINT NOT NULL DEFAULT 0,
    sha256 VARCHAR(64) NOT NULL COMMENT '原文件去重',
    upload_status VARCHAR(24) NOT NULL DEFAULT 'SUCCESS' COMMENT 'SUCCESS/FAILED',
    error_message VARCHAR(500) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (file_id),
    UNIQUE KEY uk_scan_source_sha (sha256),
    KEY idx_scan_source_batch (batch_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='扫描原文件，一文件一任务，失败可单独重试';

CREATE TABLE IF NOT EXISTS biz_scan_page (
    page_id BIGINT NOT NULL AUTO_INCREMENT,
    batch_id BIGINT NOT NULL COMMENT '所属批次',
    file_id BIGINT NOT NULL COMMENT '所属原文件',
    group_no INT NOT NULL COMMENT '一份卷分组号：多PDF按文件递增，整班PDF按身份锚点切分',
    page_no INT NOT NULL COMMENT '文件内页序号，从1开始',
    image_path VARCHAR(500) NOT NULL COMMENT '规范化页图路径(page-jpeg-v1)',
    sha256 VARCHAR(64) NOT NULL COMMENT '页图去重，重复上传直接复用识别结果',
    student_id BIGINT NULL COMMENT '确认后的学生ID(biz_student)，未确认可空',
    match_status VARCHAR(24) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING待识别/MATCHED自动匹配/CONFIRMED人工确认/REVIEW待人工/REJECTED作废',
    match_confidence DECIMAL(5,4) NULL COMMENT '身份识别置信度',
    match_detail_json JSON NULL COMMENT '候选学号姓名与证据，不进正式成绩',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (page_id),
    UNIQUE KEY uk_scan_page_sha (sha256),
    KEY idx_scan_page_batch (batch_id, group_no, page_no),
    KEY idx_scan_page_student (batch_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='扫描页：身份归属待确认态，可人工合并拆分换人';

CREATE TABLE IF NOT EXISTS biz_scan_question_result (
    result_id BIGINT NOT NULL AUTO_INCREMENT,
    batch_id BIGINT NOT NULL COMMENT '所属批次',
    group_no INT NOT NULL COMMENT '一份卷分组号',
    student_id BIGINT NULL COMMENT '确认后的学生ID，未确认可空',
    question_id BIGINT NOT NULL COMMENT '题目ID(biz_question)',
    recognized_answer TEXT NULL COMMENT 'Vision结构化答案：学生写了什么',
    recognition_confidence DECIMAL(5,4) NULL,
    recognition_evidence_json JSON NULL COMMENT '页码与题块描述等证据',
    grading_method VARCHAR(16) NULL COMMENT 'RULE规则/AI模型',
    suggested_score INT NULL COMMENT '初批草稿，确认前不进正式成绩',
    scoring_detail_json JSON NULL,
    needs_review TINYINT(1) NOT NULL DEFAULT 0 COMMENT '1需教师复核',
    result_status VARCHAR(24) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/RECOGNIZING/SUCCESS/FAILED',
    final_answer_id BIGINT NULL COMMENT '采用后回指biz_student_answer.answer_id',
    attempt_count INT NOT NULL DEFAULT 0,
    error_message VARCHAR(500) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (result_id),
    UNIQUE KEY uk_scan_question_group (batch_id, group_no, question_id),
    KEY idx_scan_question_student (batch_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='扫描答题结果：识别层与判分层统一行，草稿态';

CREATE TABLE IF NOT EXISTS biz_paper_insight (
    insight_id BIGINT NOT NULL AUTO_INCREMENT,
    lesson_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    entry_year VARCHAR(16) NOT NULL,
    class_code VARCHAR(32) NOT NULL,
    confirmed_count INT NOT NULL DEFAULT 0 COMMENT '参与统计的已确认人数，变化才重算AI建议',
    stats_json JSON NULL COMMENT '确定性统计：人数/均分/分段/逐题口径',
    ai_advice_json JSON NULL COMMENT 'AI学情建议缓存：薄弱点/高频错误/讲评优先级',
    create_by BIGINT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (insight_id),
    UNIQUE KEY uk_paper_insight_scope (lesson_id, dept_id, entry_year, class_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级纸质学情：统计实时算，AI建议按确认数缓存';
-- 已废弃（2026-09-17）：方案 A 复用原作品、AI 建议和正式成绩表，无需执行本脚本。
-- 已执行过的环境保留历史表，不在本轮删除表或数据。
