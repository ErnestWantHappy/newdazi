-- 先备份目标库，再执行；旧链接继续有效，但无法从历史哈希恢复明文。
SET @db_name = DATABASE();
SET @column_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema=@db_name AND table_name='biz_research_topic'
      AND column_name='public_share_token_ciphertext'
);
SET @ddl = IF(@column_exists=0,
    'ALTER TABLE biz_research_topic ADD COLUMN public_share_token_ciphertext varchar(512) DEFAULT NULL COMMENT ''公开分享令牌AES-GCM密文'' AFTER public_share_token_hash',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
