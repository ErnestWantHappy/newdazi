-- 课程与电子教材课次绑定（2026-10-30）
-- 目标库先备份，再执行；幂等，可重复跑。新增两列均为可空，历史课程保持未绑定。
SET @db=DATABASE();
SET @ddl=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='biz_lesson' AND column_name='textbook_book_id')=0,
 'ALTER TABLE biz_lesson ADD COLUMN textbook_book_id BIGINT NULL COMMENT ''本课对应教材册ID；为空表示老师未指定''', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='biz_lesson' AND column_name='textbook_section_key')=0,
 'ALTER TABLE biz_lesson ADD COLUMN textbook_section_key VARCHAR(32) NULL COMMENT ''本课对应教材课次稳定键，如 lesson-6''', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 后检：两列存在且历史课程未绑定
SELECT column_name, column_type, is_nullable
FROM information_schema.columns
WHERE table_schema=DATABASE() AND table_name='biz_lesson'
  AND column_name IN ('textbook_book_id','textbook_section_key');
SELECT COUNT(*) AS bound_lessons
FROM biz_lesson
WHERE textbook_book_id IS NOT NULL OR textbook_section_key IS NOT NULL;
