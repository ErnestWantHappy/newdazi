-- 画程流程图操作题首期：题目配置、课程快照、学生草稿和不可变提交。
-- 本脚本只建新表，不改存量业务数据；执行前仍须按目标环境完成整库备份。

CREATE TABLE IF NOT EXISTS biz_flowchart_question
(
    question_id       BIGINT       NOT NULL COMMENT '统一题库题目ID',
    schema_version    VARCHAR(20)  NOT NULL DEFAULT '1.0' COMMENT '画程文档规范版本',
    starter_json      LONGTEXT     NOT NULL COMMENT '学生基础图JSON',
    answer_json       LONGTEXT     NOT NULL COMMENT '教师标准答案JSON',
    permissions_json  TEXT         NOT NULL COMMENT '学生题目级编辑权限JSON',
    rules_json        LONGTEXT     NOT NULL COMMENT '结构检查规则JSON',
    config_revision   INT          NOT NULL DEFAULT 1 COMMENT '配置乐观锁修订号',
    create_by         VARCHAR(64)  DEFAULT NULL,
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by         VARCHAR(64)  DEFAULT NULL,
    update_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画程题目配置';

CREATE TABLE IF NOT EXISTS biz_flowchart_lesson_snapshot
(
    snapshot_id       BIGINT       NOT NULL AUTO_INCREMENT,
    lesson_id         BIGINT       NOT NULL COMMENT '课程ID',
    question_id       BIGINT       NOT NULL COMMENT '题目ID',
    source_revision   INT          NOT NULL COMMENT '来源题目配置修订号',
    schema_version    VARCHAR(20)  NOT NULL DEFAULT '1.0',
    starter_json      LONGTEXT     NOT NULL,
    answer_json       LONGTEXT     NOT NULL,
    permissions_json  TEXT         NOT NULL,
    rules_json        LONGTEXT     NOT NULL,
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (snapshot_id),
    UNIQUE KEY uk_flowchart_lesson_question (lesson_id, question_id),
    KEY idx_flowchart_snapshot_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程画程题目快照';

CREATE TABLE IF NOT EXISTS biz_flowchart_draft
(
    draft_id                BIGINT       NOT NULL AUTO_INCREMENT,
    student_id              BIGINT       NOT NULL,
    lesson_id               BIGINT       NOT NULL,
    question_id             BIGINT       NOT NULL,
    schema_version          VARCHAR(20)  NOT NULL DEFAULT '1.0',
    document_json           LONGTEXT     NOT NULL,
    revision                INT          NOT NULL DEFAULT 1 COMMENT '草稿乐观锁修订号',
    base_submission_version INT          DEFAULT NULL COMMENT '补交草稿来源提交版本',
    create_time             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (draft_id),
    UNIQUE KEY uk_flowchart_draft_owner (student_id, lesson_id, question_id),
    KEY idx_flowchart_draft_lesson_question (lesson_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生画程自动保存草稿';

CREATE TABLE IF NOT EXISTS biz_flowchart_submission
(
    submission_id       BIGINT        NOT NULL AUTO_INCREMENT,
    student_id          BIGINT        NOT NULL,
    lesson_id           BIGINT        NOT NULL,
    question_id         BIGINT        NOT NULL,
    version_no          INT           NOT NULL,
    draft_revision      INT           NOT NULL COMMENT '提交来源草稿修订号',
    schema_version      VARCHAR(20)   NOT NULL DEFAULT '1.0',
    document_json       LONGTEXT      NOT NULL,
    rules_snapshot_json LONGTEXT      NOT NULL,
    check_result_json   LONGTEXT      NOT NULL,
    suggested_score     DECIMAL(10,2) DEFAULT NULL COMMENT '结构检查建议分，非正式成绩',
    answer_id           BIGINT        DEFAULT NULL COMMENT '现有学生答案主链记录ID',
    submit_time         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (submission_id),
    UNIQUE KEY uk_flowchart_submission_version (student_id, lesson_id, question_id, version_no),
    UNIQUE KEY uk_flowchart_submission_draft (student_id, lesson_id, question_id, draft_revision),
    KEY idx_flowchart_submission_answer (answer_id),
    KEY idx_flowchart_submission_grading (lesson_id, question_id, student_id, version_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生画程不可变提交版本';

-- 后检
SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('biz_flowchart_question', 'biz_flowchart_lesson_snapshot',
                     'biz_flowchart_draft', 'biz_flowchart_submission')
ORDER BY table_name;

