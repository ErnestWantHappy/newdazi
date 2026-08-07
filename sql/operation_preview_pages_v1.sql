-- 操作题统一页图预览 v1
-- MySQL 8；可重复执行；只增加规范化渲染元数据，不移动原文件、不改成绩。

SET @operation_preview_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE biz_practical_attachment ADD COLUMN normalized_status VARCHAR(20) NOT NULL DEFAULT ''pending'' COMMENT ''pending/converting/success/failed'' AFTER preview_error_message',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_practical_attachment'
      AND column_name = 'normalized_status'
);
PREPARE operation_preview_stmt FROM @operation_preview_sql;
EXECUTE operation_preview_stmt;
DEALLOCATE PREPARE operation_preview_stmt;

SET @operation_preview_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE biz_practical_attachment ADD COLUMN normalized_pages_json JSON NULL COMMENT ''有序页图资源路径'' AFTER normalized_status',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_practical_attachment'
      AND column_name = 'normalized_pages_json'
);
PREPARE operation_preview_stmt FROM @operation_preview_sql;
EXECUTE operation_preview_stmt;
DEALLOCATE PREPARE operation_preview_stmt;

SET @operation_preview_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE biz_practical_attachment ADD COLUMN renderer_version VARCHAR(32) NULL COMMENT ''规范化渲染器版本'' AFTER normalized_pages_json',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_practical_attachment'
      AND column_name = 'renderer_version'
);
PREPARE operation_preview_stmt FROM @operation_preview_sql;
EXECUTE operation_preview_stmt;
DEALLOCATE PREPARE operation_preview_stmt;

SET @operation_preview_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE biz_practical_attachment ADD COLUMN normalized_retry_count INT NOT NULL DEFAULT 0 AFTER renderer_version',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_practical_attachment'
      AND column_name = 'normalized_retry_count'
);
PREPARE operation_preview_stmt FROM @operation_preview_sql;
EXECUTE operation_preview_stmt;
DEALLOCATE PREPARE operation_preview_stmt;

SET @operation_preview_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE biz_practical_attachment ADD COLUMN normalized_last_retry_time DATETIME NULL AFTER normalized_retry_count',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_practical_attachment'
      AND column_name = 'normalized_last_retry_time'
);
PREPARE operation_preview_stmt FROM @operation_preview_sql;
EXECUTE operation_preview_stmt;
DEALLOCATE PREPARE operation_preview_stmt;

SET @operation_preview_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE biz_practical_attachment ADD COLUMN normalized_error_message VARCHAR(255) NULL AFTER normalized_last_retry_time',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_practical_attachment'
      AND column_name = 'normalized_error_message'
);
PREPARE operation_preview_stmt FROM @operation_preview_sql;
EXECUTE operation_preview_stmt;
DEALLOCATE PREPARE operation_preview_stmt;

SET @operation_preview_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE biz_practical_attachment ADD INDEX idx_practical_normalized (normalized_status, normalized_last_retry_time, normalized_retry_count)',
        'SELECT 1')
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'biz_practical_attachment'
      AND index_name = 'idx_practical_normalized'
);
PREPARE operation_preview_stmt FROM @operation_preview_sql;
EXECUTE operation_preview_stmt;
DEALLOCATE PREPARE operation_preview_stmt;

-- 历史图片已经是单页视觉输入；无需改写文件即可安全标记为规范化成功。
UPDATE biz_practical_attachment
SET normalized_status = 'success',
    normalized_pages_json = JSON_ARRAY(resource_path),
    renderer_version = 'source-image-v1',
    normalized_error_message = NULL
WHERE file_kind = 'IMAGE'
  AND normalized_status = 'pending';

-- 区域抽测暂沿用单文件答卷，但复用同一页图状态与渲染器。
SET @operation_preview_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE biz_county_exam_answer ADD COLUMN normalized_status VARCHAR(20) NULL COMMENT ''pending/converting/success/failed'' AFTER preview_error_message',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_county_exam_answer'
      AND column_name = 'normalized_status'
);
PREPARE operation_preview_stmt FROM @operation_preview_sql;
EXECUTE operation_preview_stmt;
DEALLOCATE PREPARE operation_preview_stmt;

SET @operation_preview_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE biz_county_exam_answer ADD COLUMN normalized_pages_json JSON NULL AFTER normalized_status',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_county_exam_answer'
      AND column_name = 'normalized_pages_json'
);
PREPARE operation_preview_stmt FROM @operation_preview_sql;
EXECUTE operation_preview_stmt;
DEALLOCATE PREPARE operation_preview_stmt;

SET @operation_preview_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE biz_county_exam_answer ADD COLUMN renderer_version VARCHAR(32) NULL AFTER normalized_pages_json',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_county_exam_answer'
      AND column_name = 'renderer_version'
);
PREPARE operation_preview_stmt FROM @operation_preview_sql;
EXECUTE operation_preview_stmt;
DEALLOCATE PREPARE operation_preview_stmt;

SET @operation_preview_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE biz_county_exam_answer ADD COLUMN normalized_retry_count INT NOT NULL DEFAULT 0 AFTER renderer_version',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_county_exam_answer'
      AND column_name = 'normalized_retry_count'
);
PREPARE operation_preview_stmt FROM @operation_preview_sql;
EXECUTE operation_preview_stmt;
DEALLOCATE PREPARE operation_preview_stmt;

SET @operation_preview_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE biz_county_exam_answer ADD COLUMN normalized_last_retry_time DATETIME NULL AFTER normalized_retry_count',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_county_exam_answer'
      AND column_name = 'normalized_last_retry_time'
);
PREPARE operation_preview_stmt FROM @operation_preview_sql;
EXECUTE operation_preview_stmt;
DEALLOCATE PREPARE operation_preview_stmt;

SET @operation_preview_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE biz_county_exam_answer ADD COLUMN normalized_error_message VARCHAR(255) NULL AFTER normalized_last_retry_time',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_county_exam_answer'
      AND column_name = 'normalized_error_message'
);
PREPARE operation_preview_stmt FROM @operation_preview_sql;
EXECUTE operation_preview_stmt;
DEALLOCATE PREPARE operation_preview_stmt;

SET @operation_preview_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE biz_county_exam_answer ADD INDEX idx_county_normalized (normalized_status, normalized_last_retry_time, normalized_retry_count)',
        'SELECT 1')
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'biz_county_exam_answer'
      AND index_name = 'idx_county_normalized'
);
PREPARE operation_preview_stmt FROM @operation_preview_sql;
EXECUTE operation_preview_stmt;
DEALLOCATE PREPARE operation_preview_stmt;

UPDATE biz_county_exam_answer a
INNER JOIN biz_county_exam_paper_question pq
        ON pq.exam_id = a.exam_id AND pq.student_id = a.student_id AND pq.question_id = a.question_id
SET a.normalized_status = 'pending',
    a.normalized_retry_count = 0
WHERE pq.question_type = 'practical'
  AND a.student_answer IS NOT NULL AND a.student_answer != ''
  AND a.normalized_status IS NULL;
