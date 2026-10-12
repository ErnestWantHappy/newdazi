-- 新作品提交成功前保留原成绩；重交时把原成绩归档到对应流程图版本。
-- 仅增加历史字段，应用回退时保留字段和数据，不覆盖学生成绩。
SET @schema_name = DATABASE();
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name
    AND table_name='biz_flowchart_submission' AND column_name='score_snapshot'),
    'SELECT 1', 'ALTER TABLE biz_flowchart_submission ADD COLUMN score_snapshot INT NULL');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name
    AND table_name='biz_flowchart_submission' AND column_name='scoring_details_json'),
    'SELECT 1', 'ALTER TABLE biz_flowchart_submission ADD COLUMN scoring_details_json LONGTEXT NULL');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
