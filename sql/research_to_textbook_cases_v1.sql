-- 教研活动课例来源字段：幂等增量，旧课例保持空值。
-- 执行前必须整库备份；低峰窗口由 apply 流程复核课堂活动与源帖子指纹。
SET NAMES utf8mb4;
SET @db_name = DATABASE();

SET @sql = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE biz_textbook_case ADD COLUMN research_topic_id BIGINT NULL COMMENT ''来源教研活动ID'' AFTER origin',
  'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db_name AND table_name = 'biz_textbook_case' AND column_name = 'research_topic_id');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE biz_textbook_case ADD COLUMN research_topic_title VARCHAR(255) NOT NULL DEFAULT '''' COMMENT ''来源教研活动标题快照'' AFTER research_topic_id',
  'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db_name AND table_name = 'biz_textbook_case' AND column_name = 'research_topic_title');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE biz_textbook_case ADD COLUMN research_post_id BIGINT NULL COMMENT ''来源教研帖子ID'' AFTER research_topic_title',
  'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db_name AND table_name = 'biz_textbook_case' AND column_name = 'research_post_id');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE biz_textbook_case ADD UNIQUE KEY uk_research_post_id (research_post_id)',
  'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = @db_name AND table_name = 'biz_textbook_case' AND index_name = 'uk_research_post_id');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 前检：三列与唯一索引存在；已有来源帖子不应重复。
SELECT column_name FROM information_schema.columns
WHERE table_schema=@db_name AND table_name='biz_textbook_case'
  AND column_name IN ('research_topic_id','research_topic_title','research_post_id')
ORDER BY column_name;
SELECT index_name FROM information_schema.statistics
WHERE table_schema=@db_name AND table_name='biz_textbook_case' AND index_name='uk_research_post_id';
SELECT research_post_id, COUNT(*) FROM biz_textbook_case
WHERE research_post_id IS NOT NULL GROUP BY research_post_id HAVING COUNT(*) > 1;
