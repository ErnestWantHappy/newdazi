-- 扩展作品分类，不修改既有题目白名单。压缩包仅保存，不解压。
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema=DATABASE()
 AND table_name='biz_practical_attachment' AND constraint_name='chk_practical_attachment_kind'),
 'ALTER TABLE biz_practical_attachment DROP CHECK chk_practical_attachment_kind', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE biz_practical_attachment ADD CONSTRAINT chk_practical_attachment_kind
 CHECK (file_kind IN ('OFFICE','PDF','IMAGE','LEGACY','TEXT','HTML','ARCHIVE'));
