-- 操作题逻辑作品 / 不可变提交版本 / 多附件 v1
-- MySQL 8；可重复执行；不移动、不删除历史文件，不改写历史提交时间和正式成绩。

CREATE TABLE IF NOT EXISTS biz_practical_artifact (
    artifact_id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '逻辑作品主键',
    context_type         VARCHAR(20)  NOT NULL COMMENT 'LESSON/COUNTY_EXAM',
    context_id           BIGINT       NOT NULL COMMENT '课程ID或抽测ID',
    student_id           BIGINT       NOT NULL COMMENT '学生ID',
    question_id          BIGINT       NOT NULL COMMENT '操作题ID',
    current_version_id   BIGINT       NULL COMMENT '当前提交版本ID',
    latest_version_no    INT          NOT NULL DEFAULT 0 COMMENT '最新版本号',
    lock_version         INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    create_time          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time          DATETIME     NULL,
    PRIMARY KEY (artifact_id),
    UNIQUE KEY uk_practical_artifact_context (context_type, context_id, student_id, question_id),
    KEY idx_practical_artifact_current_version (current_version_id),
    CONSTRAINT chk_practical_artifact_context CHECK (context_type IN ('LESSON', 'COUNTY_EXAM'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作题逻辑作品';

CREATE TABLE IF NOT EXISTS biz_practical_submission_version (
    version_id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '提交版本主键',
    artifact_id            BIGINT       NOT NULL COMMENT '逻辑作品ID',
    version_no             INT          NOT NULL COMMENT '从1递增的版本号',
    source_answer_id       BIGINT       NULL COMMENT '兼容层答题记录ID',
    version_status         VARCHAR(20)  NOT NULL COMMENT 'CURRENT/SUPERSEDED/DELETED',
    score_status           VARCHAR(20)  NOT NULL DEFAULT 'UNGRADED' COMMENT 'UNGRADED/GRADED/INVALIDATED',
    score_snapshot         INT          NULL COMMENT '该版本失效前的正式总分快照',
    scoring_details_json   JSON         NULL COMMENT '该版本失效前的分项成绩快照',
    submitted_by_user_id   BIGINT       NULL COMMENT '提交账号ID；历史回填可空',
    submit_time            DATETIME     NOT NULL COMMENT '提交时间',
    invalidated_time       DATETIME     NULL COMMENT '被新版本替代或删除的时间',
    create_time            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (version_id),
    UNIQUE KEY uk_practical_version_no (artifact_id, version_no),
    KEY idx_practical_version_answer (source_answer_id),
    KEY idx_practical_version_status (artifact_id, version_status, submit_time),
    CONSTRAINT chk_practical_version_status CHECK (version_status IN ('CURRENT', 'SUPERSEDED', 'DELETED')),
    CONSTRAINT chk_practical_score_status CHECK (score_status IN ('UNGRADED', 'GRADED', 'INVALIDATED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作题不可变提交版本';

CREATE TABLE IF NOT EXISTS biz_practical_attachment (
    attachment_id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '作品附件主键',
    version_id             BIGINT        NOT NULL COMMENT '提交版本ID',
    file_order             INT           NOT NULL COMMENT '作品内顺序，从0开始',
    file_kind              VARCHAR(20)   NOT NULL COMMENT 'OFFICE/PDF/IMAGE/LEGACY',
    original_file_name     VARCHAR(255)  NOT NULL COMMENT '原始文件名',
    resource_path          VARCHAR(500)  NOT NULL COMMENT '受保护资源路径',
    file_extension         VARCHAR(16)   NOT NULL COMMENT '小写扩展名',
    mime_type              VARCHAR(120)  NULL COMMENT '上传MIME',
    file_size              BIGINT        NULL COMMENT '文件字节数',
    sha256                 CHAR(64)      NULL COMMENT '文件SHA-256',
    security_status        VARCHAR(24)   NOT NULL DEFAULT 'VERIFIED' COMMENT 'VERIFIED/LEGACY_UNVERIFIED/REJECTED',
    preview_status         VARCHAR(20)   NULL COMMENT 'pending/converting/success/failed',
    preview_path           VARCHAR(500)  NULL COMMENT 'PDF或图片预览资源路径',
    preview_retry_count    INT           NOT NULL DEFAULT 0,
    preview_last_retry_time DATETIME     NULL,
    preview_error_message  VARCHAR(255)  NULL,
    create_time            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time            DATETIME      NULL,
    PRIMARY KEY (attachment_id),
    UNIQUE KEY uk_practical_attachment_order (version_id, file_order),
    KEY idx_practical_attachment_path (resource_path(191)),
    KEY idx_practical_attachment_preview (preview_status, preview_last_retry_time, preview_retry_count),
    CONSTRAINT chk_practical_attachment_kind CHECK (file_kind IN ('OFFICE', 'PDF', 'IMAGE', 'LEGACY')),
    CONSTRAINT chk_practical_attachment_security CHECK (security_status IN ('VERIFIED', 'LEGACY_UNVERIFIED', 'REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作题提交版本附件';

CREATE TABLE IF NOT EXISTS biz_practical_question_material (
    material_id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '题目材料主键',
    question_id            BIGINT        NOT NULL COMMENT '操作题ID',
    material_type          VARCHAR(20)   NOT NULL COMMENT 'STARTER/RESOURCE/REFERENCE',
    file_order             INT           NOT NULL DEFAULT 0 COMMENT '同类材料顺序',
    original_file_name     VARCHAR(255)  NOT NULL COMMENT '原始文件名',
    resource_path          VARCHAR(500)  NOT NULL COMMENT '资源路径',
    file_extension         VARCHAR(16)   NOT NULL COMMENT '小写扩展名',
    mime_type              VARCHAR(120)  NULL,
    file_size              BIGINT        NULL,
    sha256                 CHAR(64)      NULL,
    create_by              VARCHAR(64)   NULL,
    create_time            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (material_id),
    UNIQUE KEY uk_practical_material_order (question_id, material_type, file_order),
    KEY idx_practical_material_path (resource_path(191)),
    CONSTRAINT chk_practical_material_type CHECK (material_type IN ('STARTER', 'RESOURCE', 'REFERENCE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作题起始文件、补充资源与参考材料';

SET @operation_artifact_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE biz_question ADD COLUMN practical_allowed_extensions VARCHAR(255) NULL COMMENT ''操作题允许提交扩展名CSV'' AFTER preview_status',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_question'
      AND column_name = 'practical_allowed_extensions'
);
PREPARE operation_artifact_stmt FROM @operation_artifact_sql;
EXECUTE operation_artifact_stmt;
DEALLOCATE PREPARE operation_artifact_stmt;

SET @operation_artifact_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE biz_question ADD COLUMN practical_image_max_count TINYINT NOT NULL DEFAULT 10 COMMENT ''图片作品最多张数'' AFTER practical_allowed_extensions',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_question'
      AND column_name = 'practical_image_max_count'
);
PREPARE operation_artifact_stmt FROM @operation_artifact_sql;
EXECUTE operation_artifact_stmt;
DEALLOCATE PREPARE operation_artifact_stmt;

SET @operation_artifact_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE biz_student_answer ADD COLUMN practical_artifact_id BIGINT NULL COMMENT ''逻辑作品ID'' AFTER preview_error_message',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_student_answer'
      AND column_name = 'practical_artifact_id'
);
PREPARE operation_artifact_stmt FROM @operation_artifact_sql;
EXECUTE operation_artifact_stmt;
DEALLOCATE PREPARE operation_artifact_stmt;

SET @operation_artifact_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE biz_student_answer ADD COLUMN practical_version_id BIGINT NULL COMMENT ''当前提交版本ID'' AFTER practical_artifact_id',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_student_answer'
      AND column_name = 'practical_version_id'
);
PREPARE operation_artifact_stmt FROM @operation_artifact_sql;
EXECUTE operation_artifact_stmt;
DEALLOCATE PREPARE operation_artifact_stmt;

SET @operation_artifact_index_exists = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'biz_student_answer'
      AND index_name = 'idx_student_answer_practical_version'
);
SET @operation_artifact_sql = IF(@operation_artifact_index_exists = 0,
    'ALTER TABLE biz_student_answer ADD INDEX idx_student_answer_practical_version (practical_version_id)',
    'SELECT 1');
PREPARE operation_artifact_stmt FROM @operation_artifact_sql;
EXECUTE operation_artifact_stmt;
DEALLOCATE PREPARE operation_artifact_stmt;

UPDATE biz_question
SET practical_allowed_extensions = 'doc,docx,pdf,ppt,pptx,xls,xlsx,jpg,jpeg,png'
WHERE question_type = 'practical'
  AND (practical_allowed_extensions IS NULL OR TRIM(practical_allowed_extensions) = '');

UPDATE biz_question
SET practical_image_max_count = 10
WHERE question_type = 'practical'
  AND (practical_image_max_count IS NULL OR practical_image_max_count < 1 OR practical_image_max_count > 10);

-- 历史题目单一 file_path 视为起始文件；保留原字段供旧应用读取。
INSERT IGNORE INTO biz_practical_question_material
    (question_id, material_type, file_order, original_file_name, resource_path,
     file_extension, mime_type, file_size, sha256, create_by, create_time)
SELECT q.question_id,
       'STARTER',
       0,
       SUBSTRING_INDEX(REPLACE(q.file_path, '\\', '/'), '/', -1),
       REPLACE(q.file_path, '\\', '/'),
       LOWER(SUBSTRING_INDEX(q.file_path, '.', -1)),
       NULL,
       NULL,
       NULL,
       q.create_by,
       COALESCE(q.create_time, NOW())
FROM biz_question q
WHERE q.question_type = 'practical'
  AND q.file_path IS NOT NULL
  AND TRIM(q.file_path) <> '';

-- 每条历史普通课程操作题答案建立一个逻辑作品；不处理空答案。
INSERT IGNORE INTO biz_practical_artifact
    (context_type, context_id, student_id, question_id, latest_version_no, create_time, update_time)
SELECT 'LESSON', a.lesson_id, a.student_id, a.question_id, 1,
       COALESCE(a.submit_time, NOW()), COALESCE(a.submit_time, NOW())
FROM biz_student_answer a
INNER JOIN biz_question q ON q.question_id = a.question_id
WHERE q.question_type = 'practical'
  AND a.student_answer IS NOT NULL
  AND TRIM(a.student_answer) <> '';

INSERT IGNORE INTO biz_practical_submission_version
    (artifact_id, version_no, source_answer_id, version_status, score_status,
     score_snapshot, scoring_details_json, submit_time, create_time)
SELECT artifact.artifact_id,
       1,
       answer.answer_id,
       'CURRENT',
       IF(answer.score IS NULL, 'UNGRADED', 'GRADED'),
       answer.score,
       (SELECT JSON_ARRAYAGG(JSON_OBJECT('itemId', detail.item_id, 'score', detail.score))
        FROM biz_scoring_detail detail
        WHERE detail.answer_id = answer.answer_id),
       COALESCE(answer.submit_time, NOW()),
       COALESCE(answer.submit_time, NOW())
FROM biz_practical_artifact artifact
INNER JOIN biz_student_answer answer
        ON artifact.context_type = 'LESSON'
       AND artifact.context_id = answer.lesson_id
       AND artifact.student_id = answer.student_id
       AND artifact.question_id = answer.question_id
WHERE answer.student_answer IS NOT NULL
  AND TRIM(answer.student_answer) <> '';

UPDATE biz_practical_artifact artifact
INNER JOIN biz_practical_submission_version version
        ON version.artifact_id = artifact.artifact_id
       AND version.version_no = artifact.latest_version_no
SET artifact.current_version_id = version.version_id
WHERE artifact.current_version_id IS NULL;

INSERT IGNORE INTO biz_practical_attachment
    (version_id, file_order, file_kind, original_file_name, resource_path,
     file_extension, mime_type, file_size, sha256, security_status,
     preview_status, preview_path, preview_retry_count, preview_last_retry_time,
     preview_error_message, create_time, update_time)
SELECT version.version_id,
       0,
       CASE
         WHEN LOWER(SUBSTRING_INDEX(answer.student_answer, '.', -1)) IN ('jpg', 'jpeg', 'png') THEN 'IMAGE'
         WHEN LOWER(SUBSTRING_INDEX(answer.student_answer, '.', -1)) = 'pdf' THEN 'PDF'
         WHEN LOWER(SUBSTRING_INDEX(answer.student_answer, '.', -1)) IN ('doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx') THEN 'OFFICE'
         ELSE 'LEGACY'
       END,
       SUBSTRING_INDEX(REPLACE(answer.student_answer, '\\', '/'), '/', -1),
       REPLACE(answer.student_answer, '\\', '/'),
       LOWER(SUBSTRING_INDEX(answer.student_answer, '.', -1)),
       NULL,
       NULL,
       NULL,
       'LEGACY_UNVERIFIED',
       answer.preview_status,
       answer.preview_path,
       COALESCE(answer.preview_retry_count, 0),
       answer.preview_last_retry_time,
       answer.preview_error_message,
       COALESCE(answer.submit_time, NOW()),
       COALESCE(answer.submit_time, NOW())
FROM biz_practical_submission_version version
INNER JOIN biz_student_answer answer ON answer.answer_id = version.source_answer_id
WHERE version.version_no = 1;

UPDATE biz_student_answer answer
INNER JOIN biz_practical_artifact artifact
        ON artifact.context_type = 'LESSON'
       AND artifact.context_id = answer.lesson_id
       AND artifact.student_id = answer.student_id
       AND artifact.question_id = answer.question_id
SET answer.practical_artifact_id = artifact.artifact_id,
    answer.practical_version_id = artifact.current_version_id
WHERE answer.student_answer IS NOT NULL
  AND TRIM(answer.student_answer) <> ''
  AND (answer.practical_artifact_id IS NULL OR answer.practical_version_id IS NULL);

-- 回滚说明（不自动执行）：
-- 1. 应用可切回旧 jar/dist；旧应用继续读取 student_answer 和原 preview_* 字段。
-- 2. 数据库优先恢复执行前整库备份。新增表只承载副本和新版本，历史源文件没有移动。
-- 3. 若已经产生新版本，不得直接 DROP 新表；应先导出新版本附件并确认已回写兼容字段。
-- 4. 仅在确认没有新版本数据时，才能删除两个新增列、两个新增题目配置列和四张新表。
