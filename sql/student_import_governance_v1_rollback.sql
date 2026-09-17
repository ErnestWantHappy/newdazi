-- 仅回滚学生导入查重索引；不修改任何用户、角色或学生业务数据。

SET @student_import_index_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND index_name = 'idx_sys_user_name_del_flag'
);
SET @student_import_index_sql = IF(
    @student_import_index_exists > 0,
    'DROP INDEX idx_sys_user_name_del_flag ON sys_user',
    'SELECT ''idx_sys_user_name_del_flag does not exist'' AS rollback_info'
);
PREPARE student_import_index_stmt FROM @student_import_index_sql;
EXECUTE student_import_index_stmt;
DEALLOCATE PREPARE student_import_index_stmt;
