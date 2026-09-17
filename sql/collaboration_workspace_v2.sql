-- 课程协作与课堂固定组隔离；旧房间、密钥和作品保持不变。执行前须整库备份。
-- 执行要求：mysql 客户端必须加 --default-character-set=utf8mb4，否则末尾中文条件行报 1267 排序规则混合错误。
SET @db = DATABASE();
SET @ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='biz_lesson_group_snapshot' AND column_name='round_no')=0,
 'ALTER TABLE biz_lesson_group_snapshot ADD COLUMN round_no INT NOT NULL DEFAULT 1 COMMENT ''分组存档版本''', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=@db AND table_name='biz_lesson_group_snapshot' AND index_name='uk_collab_snapshot_round')=0,
 'ALTER TABLE biz_lesson_group_snapshot ADD UNIQUE KEY uk_collab_snapshot_round (lesson_id,dept_id,entry_year,class_code,round_no)', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=@db AND table_name='biz_lesson_group_snapshot' AND index_name='uk_lesson_snapshot_class')>0,
 'ALTER TABLE biz_lesson_group_snapshot DROP INDEX uk_lesson_snapshot_class', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='biz_collab_activity' AND column_name='request_id')=0,
 'ALTER TABLE biz_collab_activity ADD COLUMN request_id VARCHAR(36) NULL, ADD COLUMN request_hash VARCHAR(64) NULL, ADD UNIQUE KEY uk_collab_request (request_id)', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='biz_class_group_scheme' AND column_name='scheme_scope')=0,
 'ALTER TABLE biz_class_group_scheme ADD COLUMN scheme_scope VARCHAR(16) NOT NULL DEFAULT ''FIXED'' COMMENT ''固定组或历史协作来源''', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
-- 正式前检已确认仅此方案由旧协作自动生成并被活动引用；保留数据，仅排除出固定组入口。
UPDATE biz_class_group_scheme s SET s.scheme_scope='COLLAB_LEGACY'
WHERE s.scheme_id=2 AND s.dept_id=139 AND s.entry_year='2022' AND s.class_code='5'
  AND s.scheme_name='协作自动分组' AND s.create_time='2026-09-06 14:47:23'
  AND EXISTS (SELECT 1 FROM biz_lesson_group_snapshot sn JOIN biz_collab_activity a ON a.snapshot_id=sn.snapshot_id WHERE sn.source_scheme_id=s.scheme_id);
