-- CryptPad 在线协作 Provider 增量迁移 v1。
-- 只扩展既有协作房间表，WPS 房间、版本、票据和历史回滚材料不删除。

SET @cryptpad_column_exists := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'biz_collab_room'
    AND column_name = 'provider_session_key'
);
SET @cryptpad_sql := IF(
  @cryptpad_column_exists = 0,
  'ALTER TABLE `biz_collab_room` ADD COLUMN `provider_session_key` varchar(512) NULL COMMENT ''编辑器会话密钥密文'' AFTER `provider`',
  'SELECT 1'
);
PREPARE cryptpad_stmt FROM @cryptpad_sql;
EXECUTE cryptpad_stmt;
DEALLOCATE PREPARE cryptpad_stmt;

SET @cryptpad_provider_index_exists := (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'biz_collab_room'
    AND index_name = 'idx_collab_room_provider'
);
SET @cryptpad_sql := IF(
  @cryptpad_provider_index_exists = 0,
  'ALTER TABLE `biz_collab_room` ADD KEY `idx_collab_room_provider` (`provider`,`status`)',
  'SELECT 1'
);
PREPARE cryptpad_stmt FROM @cryptpad_sql;
EXECUTE cryptpad_stmt;
DEALLOCATE PREPARE cryptpad_stmt;

UPDATE `biz_collab_room`
SET `provider` = 'CRYPTPAD'
WHERE `provider` IS NULL OR `provider` = '';

SELECT `provider`, COUNT(*) AS `room_count`
FROM `biz_collab_room`
GROUP BY `provider`
ORDER BY `provider`;

SELECT COUNT(*) AS `cryptpad_rooms_missing_key`
FROM `biz_collab_room`
WHERE `provider` = 'CRYPTPAD'
  AND (`provider_session_key` IS NULL OR `provider_session_key` = '');
