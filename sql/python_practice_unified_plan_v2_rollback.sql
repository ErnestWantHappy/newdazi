-- Python 刷题统一题单 v2 回滚（仅限尚未创建同范围多题单、且未产生 v2 班级配置时）
-- 业务上线并产生新数据后应优先切回旧 release，不应直接执行本脚本。

DELIMITER $$
DROP PROCEDURE IF EXISTS python_practice_unified_v2_rollback_guard$$
CREATE PROCEDURE python_practice_unified_v2_rollback_guard()
BEGIN
    DECLARE duplicate_scope_count BIGINT DEFAULT 0;
    SELECT COUNT(*) INTO duplicate_scope_count
    FROM (
        SELECT dept_id, grade, semester, entry_year
        FROM biz_python_practice_plan
        GROUP BY dept_id, grade, semester, entry_year
        HAVING COUNT(*) > 1
    ) duplicated;
    IF duplicate_scope_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '已存在同范围多题单，禁止自动回滚唯一索引';
    END IF;
END$$
CALL python_practice_unified_v2_rollback_guard()$$
DROP PROCEDURE IF EXISTS python_practice_unified_v2_rollback_guard$$
DELIMITER ;

DROP TABLE IF EXISTS biz_python_practice_plan_class;

SET @drop_scope_lookup := (
    SELECT IF(COUNT(*) > 0,
        'ALTER TABLE biz_python_practice_plan DROP INDEX idx_python_practice_plan_scope',
        'SELECT 1')
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_python_practice_plan'
      AND index_name = 'idx_python_practice_plan_scope'
);
PREPARE stmt FROM @drop_scope_lookup;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @restore_scope_unique := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE biz_python_practice_plan ADD UNIQUE INDEX uk_python_practice_plan_scope (dept_id, grade, semester, entry_year)',
        'SELECT 1')
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_python_practice_plan'
      AND index_name = 'uk_python_practice_plan_scope'
);
PREPARE stmt FROM @restore_scope_unique;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
