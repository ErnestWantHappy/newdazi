-- Python 刷题收口 v3：物理删除旧 V1 题与归档题单，并统一 Python 题元数据。
-- MySQL 8；执行前必须整库备份。删除的数据只能通过备份恢复。

SET NAMES utf8mb4;
DROP PROCEDURE IF EXISTS polish_python_practice_v3;
DELIMITER $$
CREATE PROCEDURE polish_python_practice_v3()
BEGIN
    DECLARE v_count INT DEFAULT 0;
    DECLARE v_creator_id BIGINT DEFAULT NULL;
    DECLARE v_creator_username VARCHAR(64) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    SELECT COUNT(*) INTO v_count
    FROM sys_user
    WHERE nick_name=CONVERT(0xE98391E4B89CE697AD USING utf8mb4) COLLATE utf8mb4_general_ci
      AND status='0' AND del_flag='0';
    IF v_count <> 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='郑东旭有效账号不是唯一一条，拒绝执行';
    END IF;
    SELECT user_id,user_name INTO v_creator_id,v_creator_username
    FROM sys_user
    WHERE nick_name=CONVERT(0xE98391E4B89CE697AD USING utf8mb4) COLLATE utf8mb4_general_ci
      AND status='0' AND del_flag='0'
    LIMIT 1;

    -- 正在判题的记录由异步任务持有，先拒绝迁移，避免清理后又被回写。
    SELECT COUNT(*) INTO v_count
    FROM biz_programming_submission s
    JOIN biz_question q ON q.question_id=s.question_id
    WHERE q.create_by='python-system-v1' AND s.status_code IN ('WAITING','JUDGING');
    IF v_count <> 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='旧 V1 题仍有课程判题任务运行，拒绝执行';
    END IF;
    SELECT COUNT(*) INTO v_count
    FROM biz_python_practice_submission s
    JOIN biz_question q ON q.question_id=s.question_id
    WHERE q.create_by='python-system-v1' AND s.status_code IN ('WAITING','JUDGING');
    IF v_count <> 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='旧 V1 题仍有刷题判题任务运行，拒绝执行';
    END IF;

    -- 这些作品链路若出现引用，必须专项级联，不能只删题目制造孤儿。
    SELECT COUNT(*) INTO v_count
    FROM biz_practical_submission_version v
    JOIN biz_student_answer a ON a.answer_id=v.source_answer_id
    JOIN biz_question q ON q.question_id=a.question_id
    WHERE q.create_by='python-system-v1';
    IF v_count <> 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='旧 V1 题存在操作题作品版本，拒绝执行';
    END IF;
    SELECT COUNT(*) INTO v_count
    FROM biz_practical_ai_result r
    JOIN biz_student_answer a ON a.answer_id=r.answer_id
    JOIN biz_question q ON q.question_id=a.question_id
    WHERE q.create_by='python-system-v1';
    IF v_count <> 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='旧 V1 题存在 AI 批改结果，拒绝执行';
    END IF;

    START TRANSACTION;

    DROP TEMPORARY TABLE IF EXISTS tmp_python_v1_question;
    CREATE TEMPORARY TABLE tmp_python_v1_question (question_id BIGINT PRIMARY KEY)
    SELECT question_id FROM biz_question WHERE create_by='python-system-v1';

    DROP TEMPORARY TABLE IF EXISTS tmp_python_v1_answer;
    CREATE TEMPORARY TABLE tmp_python_v1_answer (answer_id BIGINT PRIMARY KEY)
    SELECT a.answer_id FROM biz_student_answer a
    JOIN tmp_python_v1_question q ON q.question_id=a.question_id;

    -- 归档题单不再保留：先固定整条关系链的主键，再按叶子到根物理删除。
    DROP TEMPORARY TABLE IF EXISTS tmp_archived_plan;
    CREATE TEMPORARY TABLE tmp_archived_plan (plan_id BIGINT PRIMARY KEY)
    SELECT plan_id FROM biz_python_practice_plan WHERE status='ARCHIVED';
    DROP TEMPORARY TABLE IF EXISTS tmp_archived_version;
    CREATE TEMPORARY TABLE tmp_archived_version (plan_version_id BIGINT PRIMARY KEY)
    SELECT v.plan_version_id FROM biz_python_practice_plan_version v
    JOIN tmp_archived_plan p ON p.plan_id=v.plan_id;
    DROP TEMPORARY TABLE IF EXISTS tmp_archived_extension;
    CREATE TEMPORARY TABLE tmp_archived_extension (extension_id BIGINT PRIMARY KEY)
    SELECT e.extension_id FROM biz_python_practice_extension e
    JOIN tmp_archived_plan p ON p.plan_id=e.plan_id;
    DROP TEMPORARY TABLE IF EXISTS tmp_archived_snapshot;
    CREATE TEMPORARY TABLE tmp_archived_snapshot (snapshot_id BIGINT PRIMARY KEY)
    SELECT s.snapshot_id FROM biz_python_practice_question_snapshot s
    WHERE (s.source_type='BASE_VERSION' AND s.source_id IN (SELECT plan_version_id FROM tmp_archived_version))
       OR (s.source_type='EXTENSION' AND s.source_id IN (SELECT extension_id FROM tmp_archived_extension));
    DROP TEMPORARY TABLE IF EXISTS tmp_archived_submission;
    CREATE TEMPORARY TABLE tmp_archived_submission (submission_id BIGINT PRIMARY KEY)
    SELECT s.submission_id FROM biz_python_practice_submission s
    WHERE (s.source_type='BASE_VERSION' AND s.source_id IN (SELECT plan_version_id FROM tmp_archived_version))
       OR (s.source_type='EXTENSION' AND s.source_id IN (SELECT extension_id FROM tmp_archived_extension));

    DELETE FROM biz_python_practice_submission_case WHERE submission_id IN (SELECT submission_id FROM tmp_archived_submission);
    DELETE FROM biz_python_practice_submission WHERE submission_id IN (SELECT submission_id FROM tmp_archived_submission);
    DELETE FROM biz_python_practice_draft
    WHERE (source_type='BASE_VERSION' AND source_id IN (SELECT plan_version_id FROM tmp_archived_version))
       OR (source_type='EXTENSION' AND source_id IN (SELECT extension_id FROM tmp_archived_extension));
    DELETE FROM biz_python_practice_progress
    WHERE (source_type='BASE_VERSION' AND source_id IN (SELECT plan_version_id FROM tmp_archived_version))
       OR (source_type='EXTENSION' AND source_id IN (SELECT extension_id FROM tmp_archived_extension));
    DELETE FROM biz_python_practice_plan_question WHERE plan_version_id IN (SELECT plan_version_id FROM tmp_archived_version);
    DELETE FROM biz_python_practice_extension_question WHERE extension_id IN (SELECT extension_id FROM tmp_archived_extension);
    DELETE FROM biz_python_practice_extension_class WHERE extension_id IN (SELECT extension_id FROM tmp_archived_extension);
    DELETE FROM biz_python_practice_plan_class WHERE plan_version_id IN (SELECT plan_version_id FROM tmp_archived_version);
    DELETE FROM biz_python_practice_snapshot_case WHERE snapshot_id IN (SELECT snapshot_id FROM tmp_archived_snapshot);
    DELETE FROM biz_python_practice_question_snapshot WHERE snapshot_id IN (SELECT snapshot_id FROM tmp_archived_snapshot);
    DELETE FROM biz_python_practice_plan_version WHERE plan_version_id IN (SELECT plan_version_id FROM tmp_archived_version);
    DELETE FROM biz_python_practice_extension WHERE extension_id IN (SELECT extension_id FROM tmp_archived_extension);
    DELETE FROM biz_python_practice_plan WHERE plan_id IN (SELECT plan_id FROM tmp_archived_plan);

    -- 删除旧 V1 题在课程和独立刷题域中的全部使用记录。
    DROP TEMPORARY TABLE IF EXISTS tmp_v1_course_submission;
    CREATE TEMPORARY TABLE tmp_v1_course_submission (submission_id BIGINT PRIMARY KEY)
    SELECT s.submission_id FROM biz_programming_submission s
    JOIN tmp_python_v1_question q ON q.question_id=s.question_id;
    DELETE FROM biz_programming_submission_case WHERE submission_id IN (SELECT submission_id FROM tmp_v1_course_submission);
    DELETE FROM biz_programming_submission WHERE submission_id IN (SELECT submission_id FROM tmp_v1_course_submission);
    DELETE d FROM biz_programming_draft d JOIN tmp_python_v1_question q ON q.question_id=d.question_id;

    DROP TEMPORARY TABLE IF EXISTS tmp_v1_practice_submission;
    CREATE TEMPORARY TABLE tmp_v1_practice_submission (submission_id BIGINT PRIMARY KEY)
    SELECT s.submission_id FROM biz_python_practice_submission s
    JOIN tmp_python_v1_question q ON q.question_id=s.question_id;
    DELETE FROM biz_python_practice_submission_case WHERE submission_id IN (SELECT submission_id FROM tmp_v1_practice_submission);
    DELETE FROM biz_python_practice_submission WHERE submission_id IN (SELECT submission_id FROM tmp_v1_practice_submission);
    DELETE d FROM biz_python_practice_draft d JOIN tmp_python_v1_question q ON q.question_id=d.question_id;
    DELETE p FROM biz_python_practice_progress p JOIN tmp_python_v1_question q ON q.question_id=p.question_id;
    DELETE e FROM biz_python_practice_extension_question e JOIN tmp_python_v1_question q ON q.question_id=e.question_id;
    DELETE p FROM biz_python_practice_plan_question p JOIN tmp_python_v1_question q ON q.question_id=p.question_id;

    DROP TEMPORARY TABLE IF EXISTS tmp_v1_snapshot;
    CREATE TEMPORARY TABLE tmp_v1_snapshot (snapshot_id BIGINT PRIMARY KEY)
    SELECT s.snapshot_id FROM biz_python_practice_question_snapshot s
    JOIN tmp_python_v1_question q ON q.question_id=s.question_id;
    DELETE FROM biz_python_practice_snapshot_case WHERE snapshot_id IN (SELECT snapshot_id FROM tmp_v1_snapshot);
    DELETE FROM biz_python_practice_question_snapshot WHERE snapshot_id IN (SELECT snapshot_id FROM tmp_v1_snapshot);

    DELETE d FROM biz_scoring_detail d JOIN tmp_python_v1_answer a ON a.answer_id=d.answer_id;
    DELETE a FROM biz_practical_ai_apply_audit a JOIN tmp_python_v1_answer old_a ON old_a.answer_id=a.answer_id;
    DELETE r FROM biz_practical_rubric_snapshot r JOIN tmp_python_v1_question q ON q.question_id=r.question_id;
    DELETE a FROM biz_student_answer a JOIN tmp_python_v1_answer old_a ON old_a.answer_id=a.answer_id;
    DELETE lq FROM biz_lesson_question lq JOIN tmp_python_v1_question q ON q.question_id=lq.question_id;
    DELETE s FROM biz_scoring_item s JOIN tmp_python_v1_question q ON q.question_id=s.question_id;
    DELETE m FROM biz_practical_question_material m JOIN tmp_python_v1_question q ON q.question_id=m.question_id;
    DELETE r FROM biz_teacher_practical_reference_answer r JOIN tmp_python_v1_question q ON q.question_id=r.question_id;
    DELETE x FROM biz_county_exam_scoring_item x JOIN tmp_python_v1_question q ON q.question_id=x.question_id;
    DELETE x FROM biz_county_exam_paper_question x JOIN tmp_python_v1_question q ON q.question_id=x.question_id;
    DELETE x FROM biz_county_exam_question x JOIN tmp_python_v1_question q ON q.question_id=x.question_id;
    DELETE t FROM biz_programming_test_case t JOIN tmp_python_v1_question q ON q.question_id=t.question_id;
    DELETE c FROM biz_programming_question_config c JOIN tmp_python_v1_question q ON q.question_id=c.question_id;
    DELETE b FROM biz_question b JOIN tmp_python_v1_question q ON q.question_id=b.question_id;

    -- Python 是跨课程复用技能题，课程归属字段必须为空；创建人统一显示教师本人。
    UPDATE biz_question
    SET grade=NULL,semester=NULL,lesson_num=NULL,
        creator_id=v_creator_id,create_by=v_creator_username,
        update_by=v_creator_username,update_time=NOW()
    WHERE question_type='practical' AND practical_mode='PYTHON';

    SELECT COUNT(*) INTO v_count FROM biz_question WHERE create_by='python-system-v1';
    IF v_count <> 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='旧 V1 题后检未清零，事务已回滚';
    END IF;
    SELECT COUNT(*) INTO v_count FROM biz_python_practice_plan WHERE status='ARCHIVED';
    IF v_count <> 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='归档题单后检未清零，事务已回滚';
    END IF;
    SELECT COUNT(*) INTO v_count
    FROM biz_question
    WHERE question_type='practical' AND practical_mode='PYTHON'
      AND (grade IS NOT NULL OR semester IS NOT NULL OR lesson_num IS NOT NULL
           OR creator_id<>v_creator_id
           OR CAST(create_by AS BINARY)<>CAST(v_creator_username AS BINARY));
    IF v_count <> 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Python 题元数据统一失败，事务已回滚';
    END IF;
    SELECT COUNT(*) INTO v_count
    FROM biz_programming_question_config c
    JOIN biz_question q ON q.question_id=c.question_id
    WHERE c.create_by='python-system-v2' AND q.question_type='practical' AND q.practical_mode='PYTHON';
    IF v_count <> 120 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='V2 系统题数量不是 120，事务已回滚';
    END IF;
    SELECT COUNT(*) INTO v_count
    FROM biz_programming_test_case t
    JOIN biz_programming_question_config c ON c.question_id=t.question_id
    WHERE c.create_by='python-system-v2';
    IF v_count <> 720 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='V2 测试点数量不是 720，事务已回滚';
    END IF;

    COMMIT;
END$$
DELIMITER ;
CALL polish_python_practice_v3();
DROP PROCEDURE IF EXISTS polish_python_practice_v3;

SELECT COUNT(*) AS old_v1_question_count FROM biz_question WHERE create_by='python-system-v1';
SELECT COUNT(*) AS archived_plan_count FROM biz_python_practice_plan WHERE status='ARCHIVED';
SELECT COUNT(*) AS python_question_count,
       SUM(grade IS NOT NULL OR semester IS NOT NULL OR lesson_num IS NOT NULL) AS invalid_course_metadata,
       SUM(creator_id<>(SELECT user_id FROM sys_user WHERE nick_name=CONVERT(0xE98391E4B89CE697AD USING utf8mb4) COLLATE utf8mb4_general_ci AND status='0' AND del_flag='0' LIMIT 1)) AS invalid_creator
FROM biz_question WHERE question_type='practical' AND practical_mode='PYTHON';
SELECT COUNT(*) AS v2_question_count FROM biz_programming_question_config WHERE create_by='python-system-v2';
SELECT COUNT(*) AS v2_test_case_count
FROM biz_programming_test_case t JOIN biz_programming_question_config c ON c.question_id=t.question_id
WHERE c.create_by='python-system-v2';
