-- =======================================================
-- 数据分析实验题（DATA_LAB）增量 SQL 脚本 v1
-- 创建日期: 2026-09-21
-- 说明: 用于支持 Python 读取 Excel（Pandas + Matplotlib）的实验题型
-- =======================================================

-- 1. 实验题配置表（与 biz_question 一对一关联）
CREATE TABLE IF NOT EXISTS `biz_data_lab_config` (
  `question_id`     BIGINT NOT NULL COMMENT '关联 biz_question.question_id',
  `experiment_name` VARCHAR(200) DEFAULT NULL COMMENT '实验名称（展示用，如为空则取 question_content 标题）',
  `description`     TEXT DEFAULT NULL COMMENT '实验说明/任务要求',
  `starter_code`    TEXT DEFAULT NULL COMMENT '初始代码模板',
  `excel_file_url`  VARCHAR(500) DEFAULT NULL COMMENT 'Excel 文件在服务器的相对存储路径',
  `excel_file_name` VARCHAR(200) DEFAULT NULL COMMENT '原始文件名（Python 代码中 read_excel 默认使用的文件名）',
  `excel_file_size` BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
  `max_score`       INT DEFAULT 100 COMMENT '满分值',
  `create_by`       VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time`     DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by`       VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time`     DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据分析实验题配置表';

-- 2. 学生实验提交记录表（同一学生/题目/题单来源保持唯一更新）
CREATE TABLE IF NOT EXISTS `biz_data_lab_submission` (
  `submission_id`   BIGINT NOT NULL AUTO_INCREMENT COMMENT '提交ID',
  `student_id`      BIGINT NOT NULL COMMENT '学生ID（sys_user.user_id）',
  `question_id`     BIGINT NOT NULL COMMENT '实验题ID',
  `source_type`     VARCHAR(20) NOT NULL COMMENT '来源类型，须与题单授权协议一致',
  `source_id`       BIGINT NOT NULL COMMENT '明确的来源版本ID，不允许NULL绕过唯一约束',
  `source_code`     MEDIUMTEXT DEFAULT NULL COMMENT '学生提交的代码',
  `chart_image_url` VARCHAR(500) DEFAULT NULL COMMENT '生成的图表截图相对存储路径',
  `output_text`     MEDIUMTEXT DEFAULT NULL COMMENT 'print() 文本输出',
  `score`           INT DEFAULT NULL COMMENT '教师打分（NULL表示待批改）',
  `score_comment`   VARCHAR(500) DEFAULT NULL COMMENT '教师评语',
  `scored_by`       BIGINT DEFAULT NULL COMMENT '评分教师ID',
  `scored_at`       DATETIME DEFAULT NULL COMMENT '评分时间',
  `submitted_at`    DATETIME DEFAULT NULL COMMENT '提交时间',
  `revision`        BIGINT NOT NULL DEFAULT 1 COMMENT '重交版本，评分须匹配',
  `max_score`       INT NOT NULL DEFAULT 100 COMMENT '提交对应快照满分',
  PRIMARY KEY (`submission_id`),
  UNIQUE KEY `uk_student_question_source` (`student_id`, `question_id`, `source_type`, `source_id`),
  KEY `idx_question_source` (`question_id`, `source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据分析实验提交记录表';
