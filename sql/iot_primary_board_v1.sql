-- 小学信息科技实验板正式接入 v1（MySQL 8，幂等）
-- 仅增加 Broker 精确权限同步状态；现有班级账号、分组、消息表全部复用。

SET @dbname = DATABASE();
SET @tablename = 'biz_iot_class_config';

SET @columnname = 'broker_sync_status';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  'ALTER TABLE biz_iot_class_config ADD COLUMN broker_sync_status VARCHAR(16) NOT NULL DEFAULT ''PENDING'' COMMENT ''Broker同步状态(PENDING/SYNCED/FAILED)'' AFTER grouped_at'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @columnname = 'broker_synced_at';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  'ALTER TABLE biz_iot_class_config ADD COLUMN broker_synced_at DATETIME NULL COMMENT ''Broker最近成功同步时间'' AFTER broker_sync_status'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @columnname = 'broker_sync_error';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  'ALTER TABLE biz_iot_class_config ADD COLUMN broker_sync_error VARCHAR(500) NULL COMMENT ''脱敏后的Broker同步错误'' AFTER broker_synced_at'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 历史班级配置必须经过精确 ACL 回填后才能重新分发，不能把旧的宽泛权限误标为成功。
UPDATE biz_iot_class_config
SET broker_sync_status = 'PENDING', broker_synced_at = NULL, broker_sync_error = NULL
WHERE broker_sync_status IS NULL OR broker_sync_status NOT IN ('PENDING', 'SYNCED', 'FAILED');

