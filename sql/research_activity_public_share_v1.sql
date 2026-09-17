-- 教研活动通知匿名分享：仅保存令牌哈希，不保存可回推的明文令牌。
SET @db_name = DATABASE();

SET @sql = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE biz_research_topic ADD COLUMN public_share_enabled char(1) NOT NULL DEFAULT ''N'' COMMENT ''是否启用公开通知分享''',
    'SELECT 1') FROM information_schema.columns
    WHERE table_schema=@db_name AND table_name='biz_research_topic' AND column_name='public_share_enabled');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE biz_research_topic ADD COLUMN public_share_token_hash varchar(64) DEFAULT NULL COMMENT ''公开分享令牌SHA-256''',
    'SELECT 1') FROM information_schema.columns
    WHERE table_schema=@db_name AND table_name='biz_research_topic' AND column_name='public_share_token_hash');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE biz_research_topic ADD COLUMN public_share_expire_time datetime DEFAULT NULL COMMENT ''公开分享过期时间''',
    'SELECT 1') FROM information_schema.columns
    WHERE table_schema=@db_name AND table_name='biz_research_topic' AND column_name='public_share_expire_time');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE biz_research_topic ADD UNIQUE KEY uk_research_topic_public_share_token (public_share_token_hash)',
    'SELECT 1') FROM information_schema.statistics
    WHERE table_schema=@db_name AND table_name='biz_research_topic'
      AND index_name='uk_research_topic_public_share_token');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 后检：启用状态必须有令牌哈希，且令牌哈希不得重复。
SELECT COUNT(*) AS invalid_enabled_share_rows
FROM biz_research_topic
WHERE public_share_enabled='Y' AND (public_share_token_hash IS NULL OR public_share_token_hash='');

SELECT public_share_token_hash, COUNT(*) AS duplicate_count
FROM biz_research_topic
WHERE public_share_token_hash IS NOT NULL
GROUP BY public_share_token_hash HAVING COUNT(*) > 1;
