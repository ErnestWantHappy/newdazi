SELECT COUNT(*) AS ai_table_count
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('biz_teacher_ai_config', 'biz_practical_ai_job', 'biz_practical_ai_result');

SELECT COUNT(*) AS invalid_ai_result_count
FROM biz_practical_ai_result result_row
LEFT JOIN biz_practical_submission_version version_row
       ON version_row.version_id = result_row.practical_version_id
LEFT JOIN biz_practical_rubric_snapshot snapshot
       ON snapshot.snapshot_id = result_row.rubric_snapshot_id
WHERE version_row.version_id IS NULL
   OR snapshot.snapshot_id IS NULL
   OR (result_row.result_status = 'SUCCESS'
       AND (result_row.suggested_score IS NULL
            OR result_row.suggested_score < 0
            OR result_row.suggested_score > snapshot.question_score));

SELECT COUNT(*) AS leaked_plain_api_key_count
FROM biz_teacher_ai_config
WHERE api_key_ciphertext NOT LIKE 'v1:%';
