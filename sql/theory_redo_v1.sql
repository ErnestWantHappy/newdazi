-- 普通课程理论重做历史；本机或正式环境分别备份后执行，前后端需配套更新。
CREATE TABLE IF NOT EXISTS biz_theory_redo_history (
    redo_id BIGINT NOT NULL AUTO_INCREMENT,
    dept_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    entry_year VARCHAR(16) NOT NULL,
    class_code VARCHAR(32) NOT NULL,
    teacher_id BIGINT NOT NULL,
    previous_revision BIGINT NOT NULL DEFAULT 0,
    snapshot_json LONGTEXT NOT NULL COMMENT '原理论答案、分数和题目快照，不进入当前统计',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (redo_id),
    UNIQUE KEY uk_theory_round (student_id, lesson_id, previous_revision),
    KEY idx_theory_lesson (lesson_id, student_id, redo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='普通课程理论重做审计';
