-- 小学信息科技实验板正式接入 v1 回滚（会丢失 Broker 同步状态，仅在应用回滚后人工执行）

ALTER TABLE biz_iot_class_config DROP COLUMN broker_sync_error;
ALTER TABLE biz_iot_class_config DROP COLUMN broker_synced_at;
ALTER TABLE biz_iot_class_config DROP COLUMN broker_sync_status;

