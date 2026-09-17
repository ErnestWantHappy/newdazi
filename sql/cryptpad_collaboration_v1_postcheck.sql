-- CryptPad 协作迁移只读后检。须在人工执行迁移并确认事务结果后运行。
SELECT DATABASE() AS current_database;

SELECT COUNT(*) AS provider_session_key_column_exists
FROM information_schema.columns
WHERE table_schema = DATABASE() AND table_name = 'biz_collab_room'
  AND column_name = 'provider_session_key';

SELECT COUNT(*) AS provider_index_exists
FROM information_schema.statistics
WHERE table_schema = DATABASE() AND table_name = 'biz_collab_room'
  AND index_name = 'idx_collab_room_provider';

SELECT provider, COUNT(*) AS room_count
FROM biz_collab_room
GROUP BY provider
ORDER BY provider;

SELECT COUNT(*) AS cryptpad_rooms_missing_key
FROM biz_collab_room
WHERE provider = 'CRYPTPAD'
  AND (provider_session_key IS NULL OR provider_session_key = '');
