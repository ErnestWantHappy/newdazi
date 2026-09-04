-- 回滚难度迁移：保留字段但恢复为中等，避免破坏后续题目查询。
UPDATE biz_question SET difficulty='MEDIUM' WHERE question_type='practical' AND practical_mode='PYTHON';
