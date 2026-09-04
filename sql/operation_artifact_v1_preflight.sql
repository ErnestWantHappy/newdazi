-- 操作题逻辑作品 / 不可变提交版本 / 多附件 v1：执行前只读检查
-- 执行正式迁移前必须保存本脚本结果并完成目标库整库备份。

SELECT DATABASE() AS current_database,
       NOW() AS database_now,
       VERSION() AS mysql_version;

SELECT COUNT(*) AS existing_artifact_table_count
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
      'biz_practical_artifact',
      'biz_practical_submission_version',
      'biz_practical_attachment',
      'biz_practical_question_material'
  );

SELECT table_name, column_name, column_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND ((table_name = 'biz_question'
        AND column_name IN ('practical_allowed_extensions', 'practical_image_max_count'))
    OR (table_name = 'biz_student_answer'
        AND column_name IN ('practical_artifact_id', 'practical_version_id')))
ORDER BY table_name, ordinal_position;

SELECT COUNT(*) AS practical_answer_count,
       SUM(a.student_answer IS NOT NULL AND TRIM(a.student_answer) <> '') AS nonempty_practical_answer_count,
       SUM(a.score IS NOT NULL) AS graded_practical_answer_count
FROM biz_student_answer a
INNER JOIN biz_question q ON q.question_id = a.question_id
WHERE q.question_type = 'practical';

SELECT LOWER(SUBSTRING_INDEX(a.student_answer, '.', -1)) AS extension,
       COUNT(*) AS answer_count
FROM biz_student_answer a
INNER JOIN biz_question q ON q.question_id = a.question_id
WHERE q.question_type = 'practical'
  AND a.student_answer IS NOT NULL
  AND TRIM(a.student_answer) <> ''
GROUP BY LOWER(SUBSTRING_INDEX(a.student_answer, '.', -1))
ORDER BY answer_count DESC, extension;

SELECT COUNT(*) AS answer_duplicate_group_count
FROM (
    SELECT student_id, lesson_id, question_id
    FROM biz_student_answer
    GROUP BY student_id, lesson_id, question_id
    HAVING COUNT(*) > 1
) duplicated;

SELECT COUNT(*) AS practical_scoring_detail_count
FROM biz_scoring_detail detail
INNER JOIN biz_student_answer answer ON answer.answer_id = detail.answer_id
INNER JOIN biz_question question ON question.question_id = answer.question_id
WHERE question.question_type = 'practical';

SELECT COUNT(*) AS practical_question_count,
       SUM(file_path IS NOT NULL AND TRIM(file_path) <> '') AS practical_question_with_legacy_file_count
FROM biz_question
WHERE question_type = 'practical';
