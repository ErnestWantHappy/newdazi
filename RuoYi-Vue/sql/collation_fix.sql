-- 电子导学单 — 数据库 COLLATE 统一修复脚本
-- 日期: 2026-07-08
-- 用途: 统一所有业务表的字符集和排序规则，移除 SQL 中的强制 COLLATE 子句
-- 注意: 建议在维护窗口执行，大表可能锁表

-- 1. 统一 biz_guide_sheet_assignment
ALTER TABLE biz_guide_sheet_assignment
  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 2. 统一 biz_student（如果 collation 不一致）
ALTER TABLE biz_student
  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 3. 统一 biz_guide_sheet_progress
ALTER TABLE biz_guide_sheet_progress
  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 4. 验证修复后的 collation
SELECT TABLE_NAME, TABLE_COLLATION
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'ry-vue'
  AND TABLE_NAME IN ('biz_guide_sheet_assignment', 'biz_student', 'biz_guide_sheet_progress');
