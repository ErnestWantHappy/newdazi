-- =====================================================
-- P6: 分项评分体系 - 数据库表创建脚本 (Revision 2)
-- Remove lesson_id from biz_scoring_item as items are now bound to question definition
-- =====================================================

DROP TABLE IF EXISTS biz_scoring_item;

CREATE TABLE biz_scoring_item (
    item_id         BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '评分项ID',
    question_id     BIGINT       NOT NULL COMMENT '题目ID',
    item_name       VARCHAR(100) NOT NULL COMMENT '评分项名称',
    item_score      INT          NOT NULL DEFAULT 10 COMMENT '该项满分',
    order_num       INT          DEFAULT 0 COMMENT '排序',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作题评分项定义';

-- biz_scoring_detail remains the same, but let's ensure it exists
CREATE TABLE IF NOT EXISTS biz_scoring_detail (
    detail_id       BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    answer_id       BIGINT       NOT NULL COMMENT '答题记录ID（关联biz_student_answer.answer_id）',
    item_id         BIGINT       NOT NULL COMMENT '评分项ID（关联biz_scoring_item.item_id）',
    score           INT          NOT NULL DEFAULT 0 COMMENT '得分',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_answer_item (answer_id, item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作题分项得分记录';
