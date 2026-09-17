-- 课程入学年份锚定：应用与数据库联合回滚的数据库步骤（MySQL 8）
-- 执行前必须停止新后端；执行成功并确认字段可空后，才允许启动旧 JAR。
-- 本脚本保留 entry_year 数据与索引，仅恢复旧后端 INSERT 所需的可空兼容性。

DROP PROCEDURE IF EXISTS sp_rollback_lesson_entry_year_anchor_compatibility;
DELIMITER $$

CREATE PROCEDURE sp_rollback_lesson_entry_year_anchor_compatibility()
BEGIN
    DECLARE v_column_count INT DEFAULT 0;

    SELECT COUNT(*)
    INTO v_column_count
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_lesson'
      AND column_name = 'entry_year';

    IF v_column_count = 1 THEN
        ALTER TABLE biz_lesson
            MODIFY COLUMN entry_year VARCHAR(4) NULL COMMENT '课程所属入学年份（稳定届别）' AFTER grade;
    END IF;
END$$

DELIMITER ;

CALL sp_rollback_lesson_entry_year_anchor_compatibility();
DROP PROCEDURE IF EXISTS sp_rollback_lesson_entry_year_anchor_compatibility;

-- 回滚验收：存在该列时必须返回 is_nullable=YES。
SELECT column_name, column_type, is_nullable, column_comment
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'biz_lesson'
  AND column_name = 'entry_year';
