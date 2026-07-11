-- 区域抽测 v1 数据库一次性基线升级脚本
-- 执行前请先备份数据库。
-- 注意：本文件包含 ADD COLUMN / ADD INDEX，不能重复整段执行。
-- 如果你已经执行过旧版区域抽测 SQL，请不要再次全量执行本文件，应只按缺失字段/索引逐条补齐。
-- 本轮新增 grading_enabled 字段；已执行过本脚本的环境请执行 county_exam_grading_enabled.sql。

ALTER TABLE `biz_county_exam`
  ADD COLUMN `random_choice_count` int NULL DEFAULT 0 COMMENT '随机抽取选择题数量' AFTER `shuffle_mode`,
  ADD COLUMN `random_judgment_count` int NULL DEFAULT 0 COMMENT '随机抽取判断题数量' AFTER `random_choice_count`,
  ADD COLUMN `total_score` int NULL DEFAULT 0 COMMENT '试卷总分' AFTER `random_judgment_count`,
  ADD COLUMN `duration_minutes` int NULL DEFAULT 40 COMMENT '作答时长(分钟)' AFTER `total_score`,
  ADD COLUMN `grading_enabled` char(1) NOT NULL DEFAULT '0' COMMENT '评卷入口是否开启(0否 1是)' AFTER `status`,
  ADD COLUMN `open_time` datetime NULL DEFAULT NULL COMMENT '开启时间' AFTER `update_time`;

ALTER TABLE `biz_county_exam_class`
  ADD UNIQUE INDEX `uk_county_exam_dept` (`exam_id`, `dept_id`);

ALTER TABLE `biz_county_exam_answer`
  ADD COLUMN `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间' AFTER `file_path`,
  ADD COLUMN `typing_speed` int NULL DEFAULT NULL COMMENT '打字速度' AFTER `submit_time`,
  ADD COLUMN `accuracy_rate` decimal(7, 4) NULL DEFAULT NULL COMMENT '打字正确率(%)' AFTER `typing_speed`,
  ADD COLUMN `completion_rate` decimal(7, 4) NULL DEFAULT NULL COMMENT '打字完成率(%)' AFTER `accuracy_rate`,
  ADD COLUMN `preview_status` varchar(20) NULL DEFAULT NULL COMMENT '预览状态' AFTER `completion_rate`,
  ADD COLUMN `preview_path` varchar(500) NULL DEFAULT NULL COMMENT '预览文件路径' AFTER `preview_status`,
  ADD COLUMN `preview_retry_count` int NULL DEFAULT 0 COMMENT '预览重试次数' AFTER `preview_path`,
  ADD COLUMN `preview_last_retry_time` datetime NULL DEFAULT NULL COMMENT '最后重试时间' AFTER `preview_retry_count`,
  ADD COLUMN `preview_error_message` varchar(255) NULL DEFAULT NULL COMMENT '预览错误信息' AFTER `preview_last_retry_time`,
  ADD UNIQUE INDEX `uk_county_exam_answer` (`exam_id`, `student_id`, `question_id`);

ALTER TABLE `biz_county_exam_student`
  ADD COLUMN `start_time` datetime NULL DEFAULT NULL COMMENT '个人开始作答时间' AFTER `status`,
  ADD COLUMN `deadline_time` datetime NULL DEFAULT NULL COMMENT '个人作答截止时间' AFTER `start_time`,
  ADD COLUMN `auto_submit` char(1) NULL DEFAULT '0' COMMENT '是否超时自动提交' AFTER `deadline_time`;

ALTER TABLE `biz_county_exam_grader`
  ADD COLUMN `question_id` bigint NULL DEFAULT NULL COMMENT '操作题ID' AFTER `exam_id`,
  DROP INDEX `uk_exam_grader`,
  ADD UNIQUE INDEX `uk_county_exam_grader_question` (`exam_id`, `question_id`, `grader_id`);

CREATE TABLE IF NOT EXISTS `biz_county_exam_paper_question` (
  `paper_question_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` bigint NOT NULL COMMENT '区域抽测ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `question_type` varchar(20) NOT NULL COMMENT '题目类型',
  `question_content` text NULL COMMENT '题干快照',
  `option_a` varchar(1000) NULL DEFAULT NULL COMMENT '选项A快照',
  `option_b` varchar(1000) NULL DEFAULT NULL COMMENT '选项B快照',
  `option_c` varchar(1000) NULL DEFAULT NULL COMMENT '选项C快照',
  `option_d` varchar(1000) NULL DEFAULT NULL COMMENT '选项D快照',
  `answer` varchar(1000) NULL DEFAULT NULL COMMENT '标准答案快照',
  `analysis` text NULL COMMENT '解析快照',
  `question_score` int NULL DEFAULT 0 COMMENT '分值快照',
  `order_num` int NULL DEFAULT 0 COMMENT '个人试卷排序',
  `typing_duration` int NULL DEFAULT NULL COMMENT '打字时长',
  `word_count` int NULL DEFAULT NULL COMMENT '字数',
  `file_path` varchar(500) NULL DEFAULT NULL COMMENT '操作题素材',
  `preview_path` varchar(500) NULL DEFAULT NULL COMMENT '素材预览',
  PRIMARY KEY (`paper_question_id`),
  UNIQUE INDEX `uk_county_exam_paper_question` (`exam_id`, `student_id`, `question_id`),
  INDEX `idx_county_exam_paper_student` (`exam_id`, `student_id`, `order_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区域抽测学生试卷题目快照';

CREATE TABLE IF NOT EXISTS `biz_county_exam_scoring_item` (
  `item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` bigint NOT NULL COMMENT '区域抽测ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `source_item_id` bigint NULL DEFAULT NULL COMMENT '原评分项ID',
  `item_name` varchar(200) NOT NULL COMMENT '评分项名称快照',
  `item_score` int NULL DEFAULT 0 COMMENT '评分项分值快照',
  `order_num` int NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`item_id`),
  INDEX `idx_county_exam_scoring_item` (`exam_id`, `question_id`, `order_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区域抽测操作题评分项快照';

CREATE TABLE IF NOT EXISTS `biz_county_exam_grading_detail` (
  `detail_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `answer_id` bigint NOT NULL COMMENT '答题ID',
  `item_id` bigint NOT NULL COMMENT '评分项快照ID',
  `score` int NULL DEFAULT 0 COMMENT '得分',
  PRIMARY KEY (`detail_id`),
  UNIQUE INDEX `uk_county_exam_grading_detail` (`answer_id`, `item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区域抽测操作题分项评分';

INSERT INTO `sys_menu`
  (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '区域抽测', 0, 6, 'county-exam', 'business/countyExam/index', NULL, 'CountyExam', 1, 0, 'C', '0', '0', 'business:countyExam:list', 'education', 'admin', sysdate(), '区域抽测工作台'
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` WHERE `path` = 'county-exam' AND `component` = 'business/countyExam/index'
);

-- 管理接口允许管理员和教研员，菜单授权需与后端角色规则一致。
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.role_id, m.menu_id
FROM `sys_role` r
JOIN `sys_menu` m ON m.perms = 'business:countyExam:list'
WHERE r.role_key IN ('admin', 'researcher')
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu` rm
    WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
  );
