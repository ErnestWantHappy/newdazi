-- =====================================================================
-- iot_message_device_fk_v1.sql（幂等）
-- 背景：biz_iot_message.device_id 带外键指向 biz_iot_device(device_id)，
--       但小组级消息以哨兵值 device_id=0 入库，且设备表可为空，
--       导致每条消息插入报 ERROR 1452，异常从 MQTT 回调抛出，
--       paho 视为致命错误断线重连——教师端收不到任何数据。
-- 处理：删除该外键约束（保留列与索引），恢复消息落库链路。
-- 回滚：sql/iot_message_device_fk_v1_rollback.sql
-- =====================================================================
SET @fk_exists := (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'biz_iot_message'
      AND CONSTRAINT_NAME = 'fk_iot_message_device'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
SET @ddl := IF(@fk_exists > 0,
    'ALTER TABLE biz_iot_message DROP FOREIGN KEY fk_iot_message_device',
    'SELECT ''FK already absent, skip'' AS msg');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
