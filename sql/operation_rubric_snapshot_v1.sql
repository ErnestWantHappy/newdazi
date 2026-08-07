-- 普通课程操作题评分标准快照 v1
-- MySQL 8；先建快照，再将历史提交版本绑定到按当前证据生成的基线快照。

CREATE TABLE IF NOT EXISTS biz_practical_rubric_snapshot (
    snapshot_id BIGINT NOT NULL AUTO_INCREMENT,
    lesson_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    snapshot_version INT NOT NULL,
    question_content TEXT NOT NULL,
    question_score INT NOT NULL,
    scoring_items_json JSON NOT NULL,
    reference_materials_json JSON NOT NULL,
    created_by_user_id BIGINT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (snapshot_id),
    UNIQUE KEY uk_practical_rubric_version (lesson_id, question_id, snapshot_version),
    KEY idx_practical_rubric_latest (lesson_id, question_id, snapshot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='普通课程操作题评分标准不可变快照';

SET @rubric_snapshot_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE biz_practical_submission_version ADD COLUMN rubric_snapshot_id BIGINT NULL AFTER artifact_id',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_practical_submission_version'
      AND column_name = 'rubric_snapshot_id'
);
PREPARE rubric_snapshot_stmt FROM @rubric_snapshot_sql;
EXECUTE rubric_snapshot_stmt;
DEALLOCATE PREPARE rubric_snapshot_stmt;

SET @rubric_snapshot_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE biz_practical_submission_version ADD INDEX idx_practical_version_rubric (rubric_snapshot_id)',
        'SELECT 1')
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'biz_practical_submission_version'
      AND index_name = 'idx_practical_version_rubric'
);
PREPARE rubric_snapshot_stmt FROM @rubric_snapshot_sql;
EXECUTE rubric_snapshot_stmt;
DEALLOCATE PREPARE rubric_snapshot_stmt;

-- 历史基线只针对已经存在作品版本的课程题目，不改任何成绩。
INSERT INTO biz_practical_rubric_snapshot
    (lesson_id, question_id, snapshot_version, question_content, question_score,
     scoring_items_json, reference_materials_json, created_by_user_id, create_time)
SELECT artifact.context_id,
       artifact.question_id,
       1,
       COALESCE(question.question_content, ''),
       COALESCE(lesson_question.question_score, 0),
       COALESCE((
           SELECT JSON_ARRAYAGG(JSON_OBJECT(
               'itemId', scoring.item_id,
               'questionId', scoring.question_id,
               'itemName', scoring.item_name,
               'itemScore', scoring.item_score,
               'orderNum', scoring.order_num))
           FROM biz_scoring_item scoring
           WHERE scoring.question_id = artifact.question_id
       ), JSON_ARRAY()),
       COALESCE((
           SELECT JSON_ARRAYAGG(JSON_OBJECT(
               'materialId', material.material_id,
               'originalFileName', material.original_file_name,
               'resourcePath', material.resource_path,
               'sha256', material.sha256,
               'fileOrder', material.file_order))
           FROM biz_practical_question_material material
           WHERE material.question_id = artifact.question_id
             AND material.material_type = 'REFERENCE'
       ), JSON_ARRAY()),
       lesson.creator_id,
       NOW()
FROM biz_practical_artifact artifact
INNER JOIN biz_lesson lesson ON lesson.lesson_id = artifact.context_id
INNER JOIN biz_lesson_question lesson_question
        ON lesson_question.lesson_id = artifact.context_id
       AND lesson_question.question_id = artifact.question_id
LEFT JOIN biz_question question ON question.question_id = artifact.question_id
WHERE artifact.context_type = 'LESSON'
GROUP BY artifact.context_id, artifact.question_id, question.question_content,
         lesson_question.question_score, lesson.creator_id
ON DUPLICATE KEY UPDATE snapshot_id = snapshot_id;

-- 三道历史操作题已从课程关系中移除，不能用当前课程关系回填。
-- 下列分值、题干和评分项取自 sys_oper_log 中学生提交前最后一次课程保存记录：
-- lesson 33/question 118（2026-03-04 08:36:01）、lesson 36/question 132（2026-03-09 11:26:42）、
-- lesson 36/question 171（2026-03-11 14:59:13）。固定写入证据值，避免未来题库修改污染历史提交。
INSERT INTO biz_practical_rubric_snapshot
    (lesson_id, question_id, snapshot_version, question_content, question_score,
     scoring_items_json, reference_materials_json, created_by_user_id, create_time)
SELECT legacy.lesson_id,
       legacy.question_id,
       1,
       legacy.question_content,
       legacy.question_score,
       legacy.scoring_items_json,
       COALESCE((
           SELECT JSON_ARRAYAGG(JSON_OBJECT(
               'materialId', material.material_id,
               'originalFileName', material.original_file_name,
               'resourcePath', material.resource_path,
               'sha256', material.sha256,
               'fileOrder', material.file_order))
           FROM biz_practical_question_material material
           WHERE material.question_id = legacy.question_id
             AND material.material_type = 'REFERENCE'
       ), JSON_ARRAY()),
       lesson.creator_id,
       legacy.evidence_time
FROM (
    SELECT 33 AS lesson_id, 118 AS question_id, '六年级' AS question_content, 40 AS question_score,
           JSON_ARRAY(JSON_OBJECT('itemId', 17, 'questionId', 118, 'itemName', '答案是否正确',
                                  'itemScore', 100, 'orderNum', 0)) AS scoring_items_json,
           CAST('2026-03-04 08:36:01' AS DATETIME) AS evidence_time
    UNION ALL
    SELECT 36, 132, '上传生成二维码', 40,
           JSON_ARRAY(JSON_OBJECT('itemId', 27, 'questionId', 132, 'itemName', '上传正确',
                                  'itemScore', 100, 'orderNum', 0)),
           CAST('2026-03-09 11:26:42' AS DATETIME)
    UNION ALL
    SELECT 36, 171, '观看智能浇灌系统和智能通风系统展示视频，选择其一填表：\n', 1,
           JSON_ARRAY(JSON_OBJECT('itemId', 29, 'questionId', 171, 'itemName', '表格内容填写正确',
                                  'itemScore', 100, 'orderNum', 0)),
           CAST('2026-03-11 14:59:13' AS DATETIME)
) legacy
INNER JOIN biz_lesson lesson ON lesson.lesson_id = legacy.lesson_id
WHERE EXISTS (
    SELECT 1
    FROM biz_practical_artifact artifact
    WHERE artifact.context_type = 'LESSON'
      AND artifact.context_id = legacy.lesson_id
      AND artifact.question_id = legacy.question_id
)
ON DUPLICATE KEY UPDATE snapshot_id = snapshot_id;

UPDATE biz_practical_submission_version version_row
INNER JOIN biz_practical_artifact artifact ON artifact.artifact_id = version_row.artifact_id
INNER JOIN biz_practical_rubric_snapshot snapshot
        ON snapshot.lesson_id = artifact.context_id
       AND snapshot.question_id = artifact.question_id
       AND snapshot.snapshot_version = 1
SET version_row.rubric_snapshot_id = snapshot.snapshot_id
WHERE version_row.rubric_snapshot_id IS NULL;
