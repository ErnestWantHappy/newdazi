-- =====================================================================
-- iot_message_device_fk_v1_rollback.sql
-- 回滚：恢复 biz_iot_message.device_id 对 biz_iot_device 的外键。
-- 注意：若库中已存在 device_id=0 的消息（小组级消息哨兵值），
--       直接加回外键会失败；执行前须先确认无 device_id=0 数据，
--       或接受该约束语义已不适用的事实。
-- =====================================================================
SET @fk_exists := (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'biz_iot_message'
      AND CONSTRAINT_NAME = 'fk_iot_message_device'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
SET @zero_rows := (SELECT COUNT(*) FROM biz_iot_message WHERE device_id = 0);
SET @ddl := IF(@fk_exists = 0 AND @zero_rows = 0,
    'ALTER TABLE biz_iot_message ADD CONSTRAINT fk_iot_message_device FOREIGN KEY (device_id) REFERENCES biz_iot_device (device_id)',
    'SELECT ''Skip: FK exists or device_id=0 rows present'' AS msg');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
