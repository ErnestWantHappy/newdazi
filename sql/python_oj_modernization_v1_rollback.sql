-- Python OJ 化 v1 结构回滚（仅在确认没有新版本数据后执行）。
-- 正式回滚优先恢复迁移前整库备份；本脚本会永久删除新字段数据和逐测试点记录。

DROP TABLE IF EXISTS biz_python_practice_submission_case;

ALTER TABLE biz_python_practice_submission DROP COLUMN custom_input;
ALTER TABLE biz_python_practice_question_snapshot
    DROP COLUMN question_title,
    DROP COLUMN difficulty,
    DROP COLUMN knowledge_points,
    DROP COLUMN no_input;
ALTER TABLE biz_programming_question_config
    DROP INDEX uk_programming_config_external_id,
    DROP COLUMN external_id,
    DROP COLUMN title,
    DROP COLUMN knowledge_points,
    DROP COLUMN no_input,
    DROP COLUMN validation_status,
    DROP COLUMN validated_at,
    DROP COLUMN validated_by,
    DROP COLUMN content_version;
