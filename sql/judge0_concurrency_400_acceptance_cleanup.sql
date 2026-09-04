-- 精确清理 Judge0 400 人容量验收数据。
-- 删除范围只由 ACCJ400 标记的临时题单派生，不触碰真实教学题单、成绩或进度。

DELETE c
FROM biz_python_practice_submission_case c
JOIN biz_python_practice_submission s ON s.submission_id = c.submission_id
JOIN biz_python_practice_plan_version v ON v.plan_version_id = s.source_id
JOIN biz_python_practice_plan p ON p.plan_id = v.plan_id
WHERE s.source_type = 'BASE_VERSION'
  AND p.create_by = 'ACCJ400'
  AND p.plan_name = 'ACCJ400 判题容量验收临时题单';

DELETE s
FROM biz_python_practice_submission s
JOIN biz_python_practice_plan_version v ON v.plan_version_id = s.source_id
JOIN biz_python_practice_plan p ON p.plan_id = v.plan_id
WHERE s.source_type = 'BASE_VERSION'
  AND p.create_by = 'ACCJ400'
  AND p.plan_name = 'ACCJ400 判题容量验收临时题单';

DELETE d
FROM biz_python_practice_draft d
JOIN biz_python_practice_plan_version v ON v.plan_version_id = d.source_id
JOIN biz_python_practice_plan p ON p.plan_id = v.plan_id
WHERE d.source_type = 'BASE_VERSION'
  AND p.create_by = 'ACCJ400'
  AND p.plan_name = 'ACCJ400 判题容量验收临时题单';

DELETE pr
FROM biz_python_practice_progress pr
JOIN biz_python_practice_plan_version v ON v.plan_version_id = pr.source_id
JOIN biz_python_practice_plan p ON p.plan_id = v.plan_id
WHERE pr.source_type = 'BASE_VERSION'
  AND p.create_by = 'ACCJ400'
  AND p.plan_name = 'ACCJ400 判题容量验收临时题单';

DELETE sc
FROM biz_python_practice_snapshot_case sc
JOIN biz_python_practice_question_snapshot s ON s.snapshot_id = sc.snapshot_id
JOIN biz_python_practice_plan_version v ON v.plan_version_id = s.source_id
JOIN biz_python_practice_plan p ON p.plan_id = v.plan_id
WHERE s.source_type = 'BASE_VERSION'
  AND p.create_by = 'ACCJ400'
  AND p.plan_name = 'ACCJ400 判题容量验收临时题单';

DELETE pq
FROM biz_python_practice_plan_question pq
JOIN biz_python_practice_plan_version v ON v.plan_version_id = pq.plan_version_id
JOIN biz_python_practice_plan p ON p.plan_id = v.plan_id
WHERE p.create_by = 'ACCJ400'
  AND p.plan_name = 'ACCJ400 判题容量验收临时题单';

DELETE pc
FROM biz_python_practice_plan_class pc
JOIN biz_python_practice_plan_version v ON v.plan_version_id = pc.plan_version_id
JOIN biz_python_practice_plan p ON p.plan_id = v.plan_id
WHERE p.create_by = 'ACCJ400'
  AND p.plan_name = 'ACCJ400 判题容量验收临时题单';

DELETE s
FROM biz_python_practice_question_snapshot s
JOIN biz_python_practice_plan_version v ON v.plan_version_id = s.source_id
JOIN biz_python_practice_plan p ON p.plan_id = v.plan_id
WHERE s.source_type = 'BASE_VERSION'
  AND p.create_by = 'ACCJ400'
  AND p.plan_name = 'ACCJ400 判题容量验收临时题单';

DELETE v
FROM biz_python_practice_plan_version v
JOIN biz_python_practice_plan p ON p.plan_id = v.plan_id
WHERE p.create_by = 'ACCJ400'
  AND p.plan_name = 'ACCJ400 判题容量验收临时题单';

DELETE FROM biz_python_practice_plan
WHERE create_by = 'ACCJ400'
  AND plan_name = 'ACCJ400 判题容量验收临时题单';

SELECT
    (SELECT COUNT(*) FROM biz_python_practice_plan WHERE create_by = 'ACCJ400') AS fixture_plan_count,
    (SELECT COUNT(*) FROM biz_python_practice_submission WHERE source_code LIKE '# ACCJ400-%') AS fixture_submission_count;

