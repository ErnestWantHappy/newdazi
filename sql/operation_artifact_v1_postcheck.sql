-- 操作题逻辑作品 / 不可变提交版本 / 多附件 v1：执行后只读复核
-- 所有异常计数应为0；历史 LEGACY_UNVERIFIED 是预期状态，不属于异常。

SELECT COUNT(*) AS artifact_table_count
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
      'biz_practical_artifact',
      'biz_practical_submission_version',
      'biz_practical_attachment',
      'biz_practical_question_material'
  );

SELECT COUNT(*) AS artifact_count FROM biz_practical_artifact;
SELECT COUNT(*) AS version_count FROM biz_practical_submission_version;
SELECT COUNT(*) AS attachment_count FROM biz_practical_attachment;
SELECT COUNT(*) AS question_material_count FROM biz_practical_question_material;

SELECT COUNT(*) AS duplicate_artifact_group_count
FROM (
    SELECT context_type, context_id, student_id, question_id
    FROM biz_practical_artifact
    GROUP BY context_type, context_id, student_id, question_id
    HAVING COUNT(*) > 1
) duplicated;

SELECT COUNT(*) AS duplicate_version_group_count
FROM (
    SELECT artifact_id, version_no
    FROM biz_practical_submission_version
    GROUP BY artifact_id, version_no
    HAVING COUNT(*) > 1
) duplicated;

SELECT COUNT(*) AS duplicate_attachment_order_count
FROM (
    SELECT version_id, file_order
    FROM biz_practical_attachment
    GROUP BY version_id, file_order
    HAVING COUNT(*) > 1
) duplicated;

SELECT COUNT(*) AS orphan_current_version_count
FROM biz_practical_artifact artifact
LEFT JOIN biz_practical_submission_version version
       ON version.version_id = artifact.current_version_id
      AND version.artifact_id = artifact.artifact_id
WHERE artifact.current_version_id IS NOT NULL
  AND version.version_id IS NULL;

SELECT COUNT(*) AS orphan_version_count
FROM biz_practical_submission_version version
LEFT JOIN biz_practical_artifact artifact ON artifact.artifact_id = version.artifact_id
WHERE artifact.artifact_id IS NULL;

SELECT COUNT(*) AS orphan_attachment_count
FROM biz_practical_attachment attachment
LEFT JOIN biz_practical_submission_version version ON version.version_id = attachment.version_id
WHERE version.version_id IS NULL;

SELECT COUNT(*) AS invalid_current_version_status_count
FROM biz_practical_artifact artifact
INNER JOIN biz_practical_submission_version version ON version.version_id = artifact.current_version_id
WHERE version.version_status <> 'CURRENT'
   OR version.version_no <> artifact.latest_version_no;

SELECT COUNT(*) AS multiple_current_version_group_count
FROM (
    SELECT artifact_id
    FROM biz_practical_submission_version
    WHERE version_status = 'CURRENT'
    GROUP BY artifact_id
    HAVING COUNT(*) > 1
) duplicated;

SELECT COUNT(*) AS missing_backfill_count
FROM biz_student_answer answer
INNER JOIN biz_question question ON question.question_id = answer.question_id
LEFT JOIN biz_practical_artifact artifact
       ON artifact.context_type = 'LESSON'
      AND artifact.context_id = answer.lesson_id
      AND artifact.student_id = answer.student_id
      AND artifact.question_id = answer.question_id
LEFT JOIN biz_practical_submission_version version ON version.version_id = artifact.current_version_id
LEFT JOIN biz_practical_attachment attachment
       ON attachment.version_id = version.version_id AND attachment.file_order = 0
WHERE question.question_type = 'practical'
  AND answer.student_answer IS NOT NULL
  AND TRIM(answer.student_answer) <> ''
  AND (artifact.artifact_id IS NULL OR version.version_id IS NULL OR attachment.attachment_id IS NULL
       OR answer.practical_artifact_id <> artifact.artifact_id
       OR answer.practical_version_id <> version.version_id);

SELECT COUNT(*) AS invalid_question_policy_count
FROM biz_question
WHERE question_type = 'practical'
  AND (practical_allowed_extensions IS NULL
       OR TRIM(practical_allowed_extensions) = ''
       OR practical_image_max_count NOT BETWEEN 1 AND 10);

SELECT security_status, file_kind, COUNT(*) AS attachment_count
FROM biz_practical_attachment
GROUP BY security_status, file_kind
ORDER BY security_status, file_kind;
