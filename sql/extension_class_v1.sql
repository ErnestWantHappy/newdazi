-- 拓展课双空间。目标库先备份；不包含旧50班登记或账号冻结，旧身份须另行逐人确认。
CREATE TABLE IF NOT EXISTS biz_extension_class (
 extension_class_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 dept_id BIGINT NOT NULL, entry_year VARCHAR(4) NOT NULL, class_code VARCHAR(50) NOT NULL,
 class_name VARCHAR(100) NOT NULL, owner_user_id BIGINT NOT NULL,
 status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE', legacy_flag TINYINT NOT NULL DEFAULT 0,
 remark VARCHAR(500), create_by VARCHAR(64), create_time DATETIME NOT NULL,
 update_by VARCHAR(64), update_time DATETIME,
 UNIQUE KEY uk_extension_carrier(dept_id,entry_year,class_code),
 UNIQUE KEY uk_extension_name(dept_id,class_name), KEY idx_extension_owner(dept_id,owner_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS biz_extension_class_member (
 member_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 extension_class_id BIGINT NOT NULL, student_id BIGINT NOT NULL, dept_id BIGINT NOT NULL,
 join_time DATETIME NOT NULL, create_by VARCHAR(64), create_time DATETIME NOT NULL,
 UNIQUE KEY uk_extension_student(student_id), KEY idx_extension_members(extension_class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS biz_extension_lesson_member (
 lesson_id BIGINT NOT NULL, student_id BIGINT NOT NULL, dept_id BIGINT NOT NULL,
 create_time DATETIME NOT NULL, PRIMARY KEY(lesson_id,student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
-- 发布时冻结题单目标名单，移出/归档后历史学情仍保留原分母。
CREATE TABLE IF NOT EXISTS biz_extension_plan_member (
 plan_version_id BIGINT NOT NULL, student_id BIGINT NOT NULL, dept_id BIGINT NOT NULL,
 create_time DATETIME NOT NULL, PRIMARY KEY(plan_version_id,student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET @db=DATABASE();
SET @ddl=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='biz_lesson' AND column_name='lesson_scope')=0,'ALTER TABLE biz_lesson ADD lesson_scope VARCHAR(16) NOT NULL DEFAULT ''regular'', ADD INDEX idx_lesson_scope(lesson_scope,dept_id,creator_id)','SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='biz_lesson' AND column_name='extension_class_id')=0,'ALTER TABLE biz_lesson ADD extension_class_id BIGINT NULL, ADD INDEX idx_lesson_extension(extension_class_id)','SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='biz_lesson' AND column_name='lesson_date')=0,'ALTER TABLE biz_lesson ADD lesson_date DATE NULL','SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='biz_python_practice_plan' AND column_name='plan_scope')=0,'ALTER TABLE biz_python_practice_plan ADD plan_scope VARCHAR(16) NOT NULL DEFAULT ''regular''','SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='biz_python_practice_plan' AND column_name='extension_class_id')=0,'ALTER TABLE biz_python_practice_plan ADD extension_class_id BIGINT NULL, ADD INDEX idx_plan_extension(dept_id,plan_scope,extension_class_id)','SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='biz_student' AND column_name='space_pref')=0,'ALTER TABLE biz_student ADD space_pref VARCHAR(16) NOT NULL DEFAULT ''regular''','SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
-- 默认值不代表学生亲自选过；选择状态跟账号存储，首次加入也不会因换电脑丢失。
SET @ddl=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='biz_student' AND column_name='space_selected')=0,'ALTER TABLE biz_student ADD space_selected TINYINT NOT NULL DEFAULT 0','SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
