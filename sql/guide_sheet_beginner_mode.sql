-- 电子导学单新手模式：独立草稿、学生草稿版本和上传幂等键。
-- 执行前必须停止导学单写入；本脚本不会删除或重建任何 v2 业务表。

SET @before_lesson_count = (SELECT COUNT(*) FROM `biz_lesson`);
SET @before_lesson_min = (SELECT MIN(`lesson_id`) FROM `biz_lesson`);
SET @before_lesson_max = (SELECT MAX(`lesson_id`) FROM `biz_lesson`);
SET @before_assignment_count = (SELECT COUNT(*) FROM `biz_lesson_assignment`);
SET @before_assignment_min = (SELECT MIN(`assignment_id`) FROM `biz_lesson_assignment`);
SET @before_assignment_max = (SELECT MAX(`assignment_id`) FROM `biz_lesson_assignment`);
SET @before_lesson_question_count = (SELECT COUNT(*) FROM `biz_lesson_question`);
SET @before_lesson_question_min = (SELECT MIN(`id`) FROM `biz_lesson_question`);
SET @before_lesson_question_max = (SELECT MAX(`id`) FROM `biz_lesson_question`);
SET @before_student_answer_count = (SELECT COUNT(*) FROM `biz_student_answer`);
SET @before_student_answer_min = (SELECT MIN(`answer_id`) FROM `biz_student_answer`);
SET @before_student_answer_max = (SELECT MAX(`answer_id`) FROM `biz_student_answer`);
SET @before_county_exam_count = (SELECT COUNT(*) FROM `biz_county_exam`);
SET @before_county_exam_min = (SELECT MIN(`exam_id`) FROM `biz_county_exam`);
SET @before_county_exam_max = (SELECT MAX(`exam_id`) FROM `biz_county_exam`);
SET @before_county_answer_count = (SELECT COUNT(*) FROM `biz_county_exam_answer`);
SET @before_county_answer_min = (SELECT MIN(`answer_id`) FROM `biz_county_exam_answer`);
SET @before_county_answer_max = (SELECT MAX(`answer_id`) FROM `biz_county_exam_answer`);

SELECT 'BEFORE' AS `stage`, 'biz_lesson' AS `table_name`,
       @before_lesson_count AS `row_count`, @before_lesson_min AS `min_id`, @before_lesson_max AS `max_id`
UNION ALL SELECT 'BEFORE', 'biz_lesson_assignment', @before_assignment_count, @before_assignment_min, @before_assignment_max
UNION ALL SELECT 'BEFORE', 'biz_lesson_question', @before_lesson_question_count, @before_lesson_question_min, @before_lesson_question_max
UNION ALL SELECT 'BEFORE', 'biz_student_answer', @before_student_answer_count, @before_student_answer_min, @before_student_answer_max
UNION ALL SELECT 'BEFORE', 'biz_county_exam', @before_county_exam_count, @before_county_exam_min, @before_county_exam_max
UNION ALL SELECT 'BEFORE', 'biz_county_exam_answer', @before_county_answer_count, @before_county_answer_min, @before_county_answer_max;

CREATE TABLE IF NOT EXISTS `biz_guide_sheet_draft` (
  `draft_id` bigint NOT NULL AUTO_INCREMENT COMMENT '草稿ID',
  `owner_id` bigint NOT NULL COMMENT '草稿所属教师ID',
  `client_draft_key` varchar(64) NOT NULL COMMENT '客户端幂等草稿键',
  `sheet_id` bigint DEFAULT NULL COMMENT '已有模板ID，新建时为空',
  `content_json` longtext NOT NULL COMMENT '向导信息及兼容VForm3表单JSON',
  `revision` bigint unsigned NOT NULL DEFAULT 1 COMMENT '自动保存单调版本号',
  `draft_status` char(1) NOT NULL DEFAULT 'D' COMMENT '状态：D编辑中，C已完成',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  `completed_time` datetime DEFAULT NULL,
  PRIMARY KEY (`draft_id`),
  UNIQUE KEY `uk_guide_sheet_draft_owner_key` (`owner_id`,`client_draft_key`),
  KEY `idx_guide_sheet_draft_owner_status` (`owner_id`,`draft_status`,`update_time`),
  KEY `idx_guide_sheet_draft_sheet` (`sheet_id`),
  CONSTRAINT `chk_guide_sheet_draft_status` CHECK (`draft_status` IN ('D','C')),
  CONSTRAINT `chk_guide_sheet_draft_revision` CHECK (`revision` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='电子导学单编辑草稿';

SET @guide_sheet_sql = IF(
  (SELECT COUNT(*) FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE()
     AND `table_name` = 'biz_guide_sheet_answer'
     AND `column_name` = 'draft_revision') = 0,
  'ALTER TABLE `biz_guide_sheet_answer` ADD COLUMN `draft_revision` bigint unsigned NOT NULL DEFAULT 0 COMMENT ''学生草稿客户端单调版本号'' AFTER `current_page`',
  'SELECT 1'
);
PREPARE guide_sheet_stmt FROM @guide_sheet_sql;
EXECUTE guide_sheet_stmt;
DEALLOCATE PREPARE guide_sheet_stmt;

SET @guide_sheet_sql = IF(
  (SELECT COUNT(*) FROM `information_schema`.`columns`
   WHERE `table_schema` = DATABASE()
     AND `table_name` = 'biz_guide_sheet_upload'
     AND `column_name` = 'client_upload_id') = 0,
  'ALTER TABLE `biz_guide_sheet_upload` ADD COLUMN `client_upload_id` varchar(64) DEFAULT NULL COMMENT ''客户端上传幂等键'' AFTER `student_id`',
  'SELECT 1'
);
PREPARE guide_sheet_stmt FROM @guide_sheet_sql;
EXECUTE guide_sheet_stmt;
DEALLOCATE PREPARE guide_sheet_stmt;

SET @guide_sheet_sql = IF(
  (SELECT COUNT(*) FROM `information_schema`.`statistics`
   WHERE `table_schema` = DATABASE()
     AND `table_name` = 'biz_guide_sheet_upload'
     AND `index_name` = 'uk_guide_sheet_upload_idempotent') = 0,
  'ALTER TABLE `biz_guide_sheet_upload` ADD UNIQUE KEY `uk_guide_sheet_upload_idempotent` (`binding_id`,`student_id`,`client_upload_id`)',
  'SELECT 1'
);
PREPARE guide_sheet_stmt FROM @guide_sheet_sql;
EXECUTE guide_sheet_stmt;
DEALLOCATE PREPARE guide_sheet_stmt;

-- 历史操作日志只保留操作事实，不继续保存表单正文、评分配置或旧版 AI 密钥。
UPDATE `sys_oper_log`
SET `oper_param` = CASE
      WHEN `oper_param` LIKE '%formJson%'
        OR `oper_param` LIKE '%answerJson%'
        OR `oper_param` LIKE '%_aiApiKey%'
      THEN '{"redacted":"电子导学单正文已脱敏"}'
      ELSE `oper_param`
    END,
    `json_result` = CASE
      WHEN `json_result` LIKE '%formJson%'
        OR `json_result` LIKE '%answerJson%'
        OR `json_result` LIKE '%_aiApiKey%'
      THEN '{"code":200,"redacted":"电子导学单正文已脱敏"}'
      ELSE `json_result`
    END
WHERE `title` LIKE '%导学单%'
  AND (`oper_param` LIKE '%formJson%'
    OR `oper_param` LIKE '%answerJson%'
    OR `oper_param` LIKE '%_aiApiKey%'
    OR `json_result` LIKE '%formJson%'
    OR `json_result` LIKE '%answerJson%'
    OR `json_result` LIKE '%_aiApiKey%');

SET @after_lesson_count = (SELECT COUNT(*) FROM `biz_lesson`);
SET @after_lesson_min = (SELECT MIN(`lesson_id`) FROM `biz_lesson`);
SET @after_lesson_max = (SELECT MAX(`lesson_id`) FROM `biz_lesson`);
SET @after_assignment_count = (SELECT COUNT(*) FROM `biz_lesson_assignment`);
SET @after_assignment_min = (SELECT MIN(`assignment_id`) FROM `biz_lesson_assignment`);
SET @after_assignment_max = (SELECT MAX(`assignment_id`) FROM `biz_lesson_assignment`);
SET @after_lesson_question_count = (SELECT COUNT(*) FROM `biz_lesson_question`);
SET @after_lesson_question_min = (SELECT MIN(`id`) FROM `biz_lesson_question`);
SET @after_lesson_question_max = (SELECT MAX(`id`) FROM `biz_lesson_question`);
SET @after_student_answer_count = (SELECT COUNT(*) FROM `biz_student_answer`);
SET @after_student_answer_min = (SELECT MIN(`answer_id`) FROM `biz_student_answer`);
SET @after_student_answer_max = (SELECT MAX(`answer_id`) FROM `biz_student_answer`);
SET @after_county_exam_count = (SELECT COUNT(*) FROM `biz_county_exam`);
SET @after_county_exam_min = (SELECT MIN(`exam_id`) FROM `biz_county_exam`);
SET @after_county_exam_max = (SELECT MAX(`exam_id`) FROM `biz_county_exam`);
SET @after_county_answer_count = (SELECT COUNT(*) FROM `biz_county_exam_answer`);
SET @after_county_answer_min = (SELECT MIN(`answer_id`) FROM `biz_county_exam_answer`);
SET @after_county_answer_max = (SELECT MAX(`answer_id`) FROM `biz_county_exam_answer`);

SELECT 'AFTER' AS `stage`, 'biz_lesson' AS `table_name`,
       @after_lesson_count AS `row_count`, @after_lesson_min AS `min_id`, @after_lesson_max AS `max_id`,
       @after_lesson_count - @before_lesson_count AS `count_delta`
UNION ALL SELECT 'AFTER', 'biz_lesson_assignment', @after_assignment_count, @after_assignment_min, @after_assignment_max,
                 @after_assignment_count - @before_assignment_count
UNION ALL SELECT 'AFTER', 'biz_lesson_question', @after_lesson_question_count, @after_lesson_question_min, @after_lesson_question_max,
                 @after_lesson_question_count - @before_lesson_question_count
UNION ALL SELECT 'AFTER', 'biz_student_answer', @after_student_answer_count, @after_student_answer_min, @after_student_answer_max,
                 @after_student_answer_count - @before_student_answer_count
UNION ALL SELECT 'AFTER', 'biz_county_exam', @after_county_exam_count, @after_county_exam_min, @after_county_exam_max,
                 @after_county_exam_count - @before_county_exam_count
UNION ALL SELECT 'AFTER', 'biz_county_exam_answer', @after_county_answer_count, @after_county_answer_min, @after_county_answer_max,
                 @after_county_answer_count - @before_county_answer_count;

SET @orphan_guide_sheet_binding_count = (
  SELECT COUNT(*)
  FROM `biz_lesson_guide_sheet_binding` `binding`
  LEFT JOIN `biz_lesson` `lesson` ON `lesson`.`lesson_id` = `binding`.`lesson_id`
  WHERE `lesson`.`lesson_id` IS NULL
);

SET @guide_sheet_lesson_fk_name_count = (
  SELECT COUNT(*)
  FROM `information_schema`.`table_constraints`
  WHERE `constraint_schema` = DATABASE()
    AND `table_name` = 'biz_lesson_guide_sheet_binding'
    AND `constraint_name` = 'fk_lesson_guide_sheet_binding_lesson'
    AND `constraint_type` = 'FOREIGN KEY'
);

SET @guide_sheet_lesson_fk_valid_count = (
  SELECT COUNT(*)
  FROM `information_schema`.`key_column_usage` `usage`
  INNER JOIN `information_schema`.`referential_constraints` `reference`
    ON `reference`.`constraint_schema` = `usage`.`constraint_schema`
   AND `reference`.`constraint_name` = `usage`.`constraint_name`
   AND `reference`.`table_name` = `usage`.`table_name`
  WHERE `usage`.`constraint_schema` = DATABASE()
    AND `usage`.`table_name` = 'biz_lesson_guide_sheet_binding'
    AND `usage`.`constraint_name` = 'fk_lesson_guide_sheet_binding_lesson'
    AND `usage`.`column_name` = 'lesson_id'
    AND `usage`.`referenced_table_name` = 'biz_lesson'
    AND `usage`.`referenced_column_name` = 'lesson_id'
    AND `reference`.`delete_rule` = 'RESTRICT'
    AND `reference`.`update_rule` = 'RESTRICT'
);

SELECT 'ORPHAN_BINDING_CHECK' AS `stage`,
       'biz_lesson_guide_sheet_binding' AS `table_name`,
       @orphan_guide_sheet_binding_count AS `orphan_count`;

-- 维护窗口内受保护表不应发生变化；发现差异必须中止部署并先查明原因。
DROP PROCEDURE IF EXISTS `assert_guide_sheet_beginner_protected_tables`;
DELIMITER $$
CREATE PROCEDURE `assert_guide_sheet_beginner_protected_tables`()
BEGIN
  IF @orphan_guide_sheet_binding_count > 0 THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = '存在找不到课程的导学单绑定，请先修复孤儿数据再添加课程外键';
  END IF;

  IF @guide_sheet_lesson_fk_name_count > 0
     AND @guide_sheet_lesson_fk_valid_count = 0 THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = '课程导学单外键定义与预期不一致，请核对后再继续迁移';
  END IF;

  IF NOT (@after_lesson_count <=> @before_lesson_count)
     OR NOT (@after_lesson_min <=> @before_lesson_min)
     OR NOT (@after_lesson_max <=> @before_lesson_max)
     OR NOT (@after_assignment_count <=> @before_assignment_count)
     OR NOT (@after_assignment_min <=> @before_assignment_min)
     OR NOT (@after_assignment_max <=> @before_assignment_max)
     OR NOT (@after_lesson_question_count <=> @before_lesson_question_count)
     OR NOT (@after_lesson_question_min <=> @before_lesson_question_min)
     OR NOT (@after_lesson_question_max <=> @before_lesson_question_max)
     OR NOT (@after_student_answer_count <=> @before_student_answer_count)
     OR NOT (@after_student_answer_min <=> @before_student_answer_min)
     OR NOT (@after_student_answer_max <=> @before_student_answer_max)
     OR NOT (@after_county_exam_count <=> @before_county_exam_count)
     OR NOT (@after_county_exam_min <=> @before_county_exam_min)
     OR NOT (@after_county_exam_max <=> @before_county_exam_max)
     OR NOT (@after_county_answer_count <=> @before_county_answer_count)
     OR NOT (@after_county_answer_min <=> @before_county_answer_min)
     OR NOT (@after_county_answer_max <=> @before_county_answer_max) THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = '电子导学单新手迁移中受保护表发生变化，请停止部署并核对 BEFORE/AFTER 结果';
  END IF;
END$$
DELIMITER ;

CALL `assert_guide_sheet_beginner_protected_tables`();
DROP PROCEDURE IF EXISTS `assert_guide_sheet_beginner_protected_tables`;

-- 数据库外键用于封住业务历史检查完成后并发创建绑定导致的课程误删窗口。
SET @guide_sheet_sql = IF(
  @guide_sheet_lesson_fk_name_count = 0,
  'ALTER TABLE `biz_lesson_guide_sheet_binding` ADD CONSTRAINT `fk_lesson_guide_sheet_binding_lesson` FOREIGN KEY (`lesson_id`) REFERENCES `biz_lesson` (`lesson_id`) ON DELETE RESTRICT ON UPDATE RESTRICT',
  'SELECT 1'
);
PREPARE guide_sheet_stmt FROM @guide_sheet_sql;
EXECUTE guide_sheet_stmt;
DEALLOCATE PREPARE guide_sheet_stmt;

SELECT `table_name`, `index_name`, `non_unique`, `column_name`, `seq_in_index`
FROM `information_schema`.`statistics`
WHERE `table_schema` = DATABASE()
  AND `table_name` IN ('biz_guide_sheet_draft','biz_guide_sheet_answer','biz_guide_sheet_upload')
ORDER BY `table_name`, `index_name`, `seq_in_index`;

SELECT `constraint_name`, `table_name`, `referenced_table_name`,
       `delete_rule`, `update_rule`
FROM `information_schema`.`referential_constraints`
WHERE `constraint_schema` = DATABASE()
  AND `constraint_name` = 'fk_lesson_guide_sheet_binding_lesson';
