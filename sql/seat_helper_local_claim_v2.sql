-- 目标库需先有机位助手v1三表；备份后执行，发布新后端前完成。历史IP不猜填。
SET @db=DATABASE();
SET @ddl=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='biz_seat_helper_binding' AND column_name='local_ip')=0,
 'ALTER TABLE biz_seat_helper_binding ADD COLUMN local_ip VARCHAR(15) NULL COMMENT ''教师确认时的助手内网IP快照''', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='biz_seat_helper_binding' AND column_name='seat_name')=0,
 'ALTER TABLE biz_seat_helper_binding ADD COLUMN seat_name VARCHAR(40) NULL COMMENT ''教师确认时的机位名称快照''', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
