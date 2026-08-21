-- =============================================================
-- 课程级物联网开关迁移 v1 后检脚本（2026-08-21，只读）
-- 用途：迁移执行后核对字段、回填结果与一致性。
-- =============================================================

-- 1) 字段存在性（应返回 1）
SELECT COUNT(*) AS iot_enabled_column_exists
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'biz_lesson'
  AND column_name = 'iot_enabled';

-- 2) 已开启课程数
SELECT COUNT(*) AS lessons_with_iot_enabled FROM biz_lesson WHERE iot_enabled = 1;

-- 3) 一致性：存在物联网实验但开关未开启的课程（回填后应为 0）
SELECT COUNT(*) AS experiment_lessons_not_enabled
FROM biz_iot_experiment e
LEFT JOIN biz_lesson l ON l.lesson_id = e.lesson_id
WHERE l.lesson_id IS NULL OR l.iot_enabled = 0;

-- 4) 一致性：开关开启但尚无实验的课程（允许存在，教师刚开启尚未建实验）
SELECT l.lesson_id, l.lesson_title, l.dept_id
FROM biz_lesson l
WHERE l.iot_enabled = 1
  AND NOT EXISTS (SELECT 1 FROM biz_iot_experiment e WHERE e.lesson_id = l.lesson_id)
ORDER BY l.lesson_id;

-- 5) 已开启课程与实验清单（人工核对）
SELECT l.lesson_id, l.lesson_title, l.dept_id, e.experiment_id, e.title AS experiment_title
FROM biz_lesson l
JOIN biz_iot_experiment e ON e.lesson_id = l.lesson_id
WHERE l.iot_enabled = 1
ORDER BY l.lesson_id;
