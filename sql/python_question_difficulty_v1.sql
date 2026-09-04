-- Python 题目难度：幂等迁移。执行前请先备份正式库。
-- 使用存储过程做字段存在性判断，兼容正式库当前 MySQL 版本，避免重复执行时重复列错误。
DROP PROCEDURE IF EXISTS add_python_question_difficulty;
DELIMITER //
CREATE PROCEDURE add_python_question_difficulty()
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_question'
      AND column_name = 'difficulty'
  ) THEN
    ALTER TABLE biz_question
      ADD COLUMN difficulty varchar(16) NOT NULL DEFAULT 'MEDIUM'
      COMMENT 'Python题目难度 SIMPLE/MEDIUM/HARD' AFTER grade;
  END IF;
END//
DELIMITER ;
CALL add_python_question_difficulty();
DROP PROCEDURE IF EXISTS add_python_question_difficulty;

-- 已按 81 道题的实际题面复核。不能按题号连续分段：例如 1834 是“两门课程总分”，仍属基础题。
-- 简单：基础输入输出、运算和分支；中等：循环、字符串、列表及常用函数/字典；
-- 困难：二维结构、游程编码、集合/排序等综合处理。
UPDATE biz_question
SET difficulty = 'MEDIUM'
WHERE question_type='practical' AND practical_mode='PYTHON';

UPDATE biz_question
SET difficulty = 'SIMPLE'
WHERE question_type='practical' AND practical_mode='PYTHON'
  AND (question_id BETWEEN 1754 AND 1779 OR question_id = 1834);

UPDATE biz_question
SET difficulty = 'HARD'
WHERE question_type='practical' AND practical_mode='PYTHON'
  AND (question_id BETWEEN 1810 AND 1814
       OR question_id BETWEEN 1820 AND 1823
       OR question_id BETWEEN 1828 AND 1833);
