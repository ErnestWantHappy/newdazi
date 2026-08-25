-- Judge0 400 人排队容量验收夹具。
-- 仅创建 create_by=ACCJ400 的隔离题单，供测试学生执行 CUSTOM_RUN；不写成绩与进度。

SET @acc_source_snapshot_id := (
    SELECT snapshot_id
    FROM biz_python_practice_question_snapshot
    WHERE source_type = 'BASE_VERSION' AND language_code = 'python'
    ORDER BY snapshot_id DESC
    LIMIT 1
);

INSERT INTO biz_python_practice_plan
    (dept_id, grade, semester, entry_year, plan_name, status, current_version_no,
     creator_id, create_by, create_time, update_by, update_time)
VALUES
    (139, '9', '1', '2020', 'ACCJ400 判题容量验收临时题单', 'ACTIVE', 1,
     104, 'ACCJ400', NOW(), 'ACCJ400', NOW());

SET @acc_plan_id := LAST_INSERT_ID();

INSERT INTO biz_python_practice_plan_version
    (plan_id, version_no, status, published_time, creator_id, create_by, create_time, update_by, update_time)
VALUES
    (@acc_plan_id, 1, 'PUBLISHED', NOW(), 104, 'ACCJ400', NOW(), 'ACCJ400', NOW());

SET @acc_version_id := LAST_INSERT_ID();

INSERT INTO biz_python_practice_question_snapshot
    (source_type, source_id, question_id, snapshot_hash, question_title, difficulty,
     knowledge_points, no_input, question_content, input_description, output_description,
     sample_explanation, constraints_text, notes_text, starter_code, language_code,
     time_limit_seconds, memory_limit_kb, max_processes, max_file_size_kb, max_output_kb,
     create_by, create_time)
SELECT
    'BASE_VERSION', @acc_version_id, question_id, REPLACE(UUID(), '-', ''),
    CONCAT(question_title, '（容量验收）'), difficulty, knowledge_points, no_input,
    question_content, input_description, output_description, sample_explanation,
    constraints_text, notes_text, starter_code, language_code, time_limit_seconds,
    memory_limit_kb, max_processes, max_file_size_kb, max_output_kb, 'ACCJ400', NOW()
FROM biz_python_practice_question_snapshot
WHERE snapshot_id = @acc_source_snapshot_id;

SET @acc_snapshot_id := LAST_INSERT_ID();

INSERT INTO biz_python_practice_plan_question
    (plan_version_id, question_id, snapshot_id, sort_no, stage, required_flag)
SELECT @acc_version_id, question_id, @acc_snapshot_id, 1, 'BEGINNER', '1'
FROM biz_python_practice_question_snapshot
WHERE snapshot_id = @acc_snapshot_id;

INSERT INTO biz_python_practice_plan_class
    (plan_version_id, dept_id, entry_year, class_code, create_by, create_time)
VALUES
    (@acc_version_id, 139, '2020', '1', 'ACCJ400', NOW());

SELECT @acc_plan_id AS plan_id,
       @acc_version_id AS plan_version_id,
       question_id,
       @acc_snapshot_id AS snapshot_id
FROM biz_python_practice_question_snapshot
WHERE snapshot_id = @acc_snapshot_id;

