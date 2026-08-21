-- CryptPad 协作迁移只读前检。仅查询，不执行任何写操作。
SELECT DATABASE() AS current_database;

SELECT COUNT(*) AS room_table_exists
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name = 'biz_collab_room';

SELECT column_name, column_type, is_nullable, column_comment
FROM information_schema.columns
WHERE table_schema = DATABASE() AND table_name = 'biz_collab_room'
ORDER BY ordinal_position;

SELECT index_name, non_unique, column_name, seq_in_index
FROM information_schema.statistics
WHERE table_schema = DATABASE() AND table_name = 'biz_collab_room'
ORDER BY index_name, seq_in_index;

SELECT provider, COUNT(*) AS room_count
FROM biz_collab_room
GROUP BY provider
ORDER BY provider;
