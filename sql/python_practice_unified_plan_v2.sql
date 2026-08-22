-- Python 刷题统一题单 v2（方案 1）
-- 目标：取消“年级基础题单 + 班级加练包”，改为“题单版本 + 一个或多个目标班级”。
-- 本脚本不删除旧加练表。若旧加练已有数据，主动中止，由专项迁移处理，避免历史错配。

DELIMITER $$
DROP PROCEDURE IF EXISTS python_practice_unified_v2_guard$$
CREATE PROCEDURE python_practice_unified_v2_guard()
BEGIN
    DECLARE extension_count BIGINT DEFAULT 0;
    SELECT
        (SELECT COUNT(*) FROM biz_python_practice_extension)
      + (SELECT COUNT(*) FROM biz_python_practice_extension_class)
      + (SELECT COUNT(*) FROM biz_python_practice_extension_question)
      INTO extension_count;
    IF extension_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '旧班级加练表存在数据，禁止自动迁移，请先执行专项历史迁移';
    END IF;
END$$
CALL python_practice_unified_v2_guard()$$
DROP PROCEDURE IF EXISTS python_practice_unified_v2_guard$$
DELIMITER ;

CREATE TABLE IF NOT EXISTS biz_python_practice_plan_class (
    plan_class_id BIGINT NOT NULL AUTO_INCREMENT,
    plan_version_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    entry_year VARCHAR(16) NOT NULL,
    class_code VARCHAR(32) NOT NULL,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL,
    PRIMARY KEY (plan_class_id),
    UNIQUE KEY uk_python_practice_plan_class (plan_version_id, dept_id, entry_year, class_code),
    KEY idx_python_practice_plan_class_scope (dept_id, entry_year, class_code, plan_version_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Python练习题单版本目标班级';

-- 统一题单允许同一学校、同一届别创建多份题单，因此移除 V1 的范围唯一约束。
SET @drop_scope_unique := (
    SELECT IF(COUNT(*) > 0,
        'ALTER TABLE biz_python_practice_plan DROP INDEX uk_python_practice_plan_scope',
        'SELECT 1')
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_python_practice_plan'
      AND index_name = 'uk_python_practice_plan_scope'
);
PREPARE stmt FROM @drop_scope_unique;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_scope_lookup := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE biz_python_practice_plan ADD INDEX idx_python_practice_plan_scope (dept_id, entry_year, status)',
        'SELECT 1')
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_python_practice_plan'
      AND index_name = 'idx_python_practice_plan_scope'
);
PREPARE stmt FROM @add_scope_lookup;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- V1 基础题单原本按整届开放；迁移到该校该届实际存在学生的全部班级，保持学生可见范围不变。
INSERT IGNORE INTO biz_python_practice_plan_class
    (plan_version_id, dept_id, entry_year, class_code, create_by, create_time)
SELECT DISTINCT
    v.plan_version_id,
    p.dept_id,
    s.entry_year,
    s.class_code,
    'python-practice-v2-migration',
    NOW()
FROM biz_python_practice_plan p
JOIN biz_python_practice_plan_version v ON v.plan_id = p.plan_id
JOIN sys_user u ON u.dept_id = p.dept_id
JOIN biz_student s ON s.user_id = u.user_id
WHERE CAST(s.entry_year AS BINARY) = CAST(p.entry_year AS BINARY)
  AND s.class_code IS NOT NULL
  AND TRIM(s.class_code) <> '';

-- 没有学生数据的草稿，退而使用创建教师已管理的同届班级，方便继续编辑而不扩大权限。
INSERT IGNORE INTO biz_python_practice_plan_class
    (plan_version_id, dept_id, entry_year, class_code, create_by, create_time)
SELECT DISTINCT
    v.plan_version_id,
    p.dept_id,
    tc.entry_year,
    tc.class_code,
    'python-practice-v2-migration',
    NOW()
FROM biz_python_practice_plan p
JOIN biz_python_practice_plan_version v ON v.plan_id = p.plan_id
JOIN biz_teacher_class tc
  ON tc.user_id = p.creator_id
 AND tc.dept_id = p.dept_id
WHERE CAST(tc.entry_year AS BINARY) = CAST(p.entry_year AS BINARY)
  AND NOT EXISTS (
      SELECT 1
      FROM biz_python_practice_plan_class pc
      WHERE pc.plan_version_id = v.plan_version_id
  );

-- 后检：旧加练必须为空；已发布且仍投放的题单版本必须至少有一个目标班级。
SELECT
    (SELECT COUNT(*) FROM biz_python_practice_plan_class) AS plan_class_count,
    (SELECT COUNT(*)
       FROM biz_python_practice_plan p
       JOIN biz_python_practice_plan_version v
         ON v.plan_id = p.plan_id
        AND v.version_no = p.current_version_no
        AND v.status = 'PUBLISHED'
      WHERE p.status = 'ACTIVE'
        AND NOT EXISTS (
            SELECT 1 FROM biz_python_practice_plan_class pc
            WHERE pc.plan_version_id = v.plan_version_id
        )) AS published_plan_without_class,
    (SELECT COUNT(*) FROM biz_python_practice_extension) AS legacy_extension_count;
