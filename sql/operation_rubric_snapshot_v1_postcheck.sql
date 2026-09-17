SELECT COUNT(*) AS rubric_table_count
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name = 'biz_practical_rubric_snapshot';

SELECT COUNT(*) AS rubric_column_count
FROM information_schema.columns
WHERE table_schema = DATABASE() AND table_name = 'biz_practical_submission_version'
  AND column_name = 'rubric_snapshot_id';

SELECT COUNT(*) AS rubric_snapshot_count FROM biz_practical_rubric_snapshot;

SELECT COUNT(*) AS unbound_practical_version_count
FROM biz_practical_submission_version version_row
INNER JOIN biz_practical_artifact artifact ON artifact.artifact_id = version_row.artifact_id
WHERE artifact.context_type = 'LESSON' AND version_row.rubric_snapshot_id IS NULL;

SELECT COUNT(*) AS invalid_rubric_snapshot_count
FROM biz_practical_rubric_snapshot
WHERE question_score < 0
   OR scoring_items_json IS NULL OR JSON_TYPE(scoring_items_json) != 'ARRAY'
   OR reference_materials_json IS NULL OR JSON_TYPE(reference_materials_json) != 'ARRAY';

SELECT COUNT(*) AS orphan_rubric_binding_count
FROM biz_practical_submission_version version_row
LEFT JOIN biz_practical_rubric_snapshot snapshot
       ON snapshot.snapshot_id = version_row.rubric_snapshot_id
WHERE version_row.rubric_snapshot_id IS NOT NULL AND snapshot.snapshot_id IS NULL;
