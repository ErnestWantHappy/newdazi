-- 学生批量导入治理：为有效账号批量查重补联合索引。
-- 执行前必须确认目标库、完成整库备份，并先查看下方重复组结果。

SELECT user_name, COUNT(*) AS active_count
FROM sys_user
WHERE del_flag = '0' AND user_name IS NOT NULL AND user_name <> ''
GROUP BY user_name
HAVING COUNT(*) > 1
ORDER BY active_count DESC, user_name;

SET @student_import_index_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND index_name = 'idx_sys_user_name_del_flag'
);
SET @student_import_index_sql = IF(
    @student_import_index_exists = 0,
    'CREATE INDEX idx_sys_user_name_del_flag ON sys_user(user_name, del_flag)',
    'SELECT ''idx_sys_user_name_del_flag already exists'' AS migration_info'
);
PREPARE student_import_index_stmt FROM @student_import_index_sql;
EXECUTE student_import_index_stmt;
DEALLOCATE PREPARE student_import_index_stmt;

SELECT index_name, non_unique, seq_in_index, column_name
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name = 'sys_user'
  AND index_name = 'idx_sys_user_name_del_flag'
ORDER BY seq_in_index;

EXPLAIN SELECT user_id, user_name
FROM sys_user
WHERE user_name = '__student_import_index_probe__' AND del_flag = '0'
LIMIT 1;
