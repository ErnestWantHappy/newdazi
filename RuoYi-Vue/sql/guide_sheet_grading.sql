-- 导学单自动评分功能 DB 迁移脚本
-- Phase 1: biz_guide_sheet_answer 表增加评分字段

ALTER TABLE biz_guide_sheet_answer
  ADD COLUMN total_score INT DEFAULT NULL COMMENT '总分',
  ADD COLUMN grading_status VARCHAR(10) DEFAULT NULL COMMENT '评分状态: pending/auto/partial/manual',
  ADD COLUMN grading_detail TEXT DEFAULT NULL COMMENT '评分明细 JSON: [{fieldKey, fieldTitle, score, maxScore, matchType, desc}]';

-- Phase 2 预留: 独立评分规则表（后续需要时执行）
-- CREATE TABLE biz_guide_sheet_grading (
--   grading_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
--   sheet_id       BIGINT NOT NULL COMMENT '导学单ID',
--   field_key      VARCHAR(100) NOT NULL COMMENT '对应表单字段key',
--   field_title    VARCHAR(200) COMMENT '字段标题',
--   score          INT DEFAULT 0 COMMENT '分值',
--   answer_type    VARCHAR(20) DEFAULT 'exact' COMMENT '评分类型: exact/contains/regex/manual',
--   correct_answer TEXT COMMENT '正确答案',
--   partial_score  INT DEFAULT 0 COMMENT '部分得分',
--   sort_order     INT DEFAULT 0 COMMENT '排序',
--   UNIQUE KEY uk_sheet_field (sheet_id, field_key)
-- ) COMMENT='导学单评分规则表';