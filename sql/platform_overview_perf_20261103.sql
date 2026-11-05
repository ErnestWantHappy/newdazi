-- 2026-11-03 平台概览慢接口优化：上线前后检（只读，幂等，不改任何结构）
--
-- 背景：/business/platformOverview/summary 的三条统计 SQL 原本都带
--   INNER JOIN (SELECT MAX(answer_id) FROM biz_student_answer
--               GROUP BY student_id, lesson_id, question_id) latest ON latest.answer_id = a.answer_id
-- 该派生表每次都要把整张答题表分组物化一遍（130 实测单条 7.1 秒，一次请求里执行三次）。
-- biz_student_answer 已有唯一键 uk_student_lesson_question(student_id, lesson_id, question_id)，
-- 「学生 + 课程 + 题目」物理上只有一行，因此这层自连接是冗余的，新 SQL 直接去掉它。
--
-- 新 SQL 的正确性依赖该唯一键：若它不在，同一题目可能残留多行，去掉自连接会把多行分数重复累加。
-- 因此发布前必须确认下面三项检查全部通过，否则先执行 sql/typing_answer_dedup_fix.sql 补齐唯一键。

-- 检查 1：唯一键是否存在（期望 1 行，列顺序 student_id, lesson_id, question_id）
SELECT index_name,
       GROUP_CONCAT(column_name ORDER BY seq_in_index) AS index_columns,
       non_unique
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name = 'biz_student_answer'
  AND index_name = 'uk_student_lesson_question'
GROUP BY index_name, non_unique;

-- 检查 2：是否真的没有重复组（期望 dup_groups = 0）
SELECT COUNT(*) AS dup_groups
FROM (
    SELECT student_id, lesson_id, question_id
    FROM biz_student_answer
    GROUP BY student_id, lesson_id, question_id
    HAVING COUNT(*) > 1
) d;

-- 检查 3：近 6 个月趋势走覆盖索引而不是全表（期望 key = idx_answer_submit_lesson_student 或同等时间索引）
EXPLAIN FORMAT=TRADITIONAL
SELECT date_format(papers.last_submit, '%m月') AS name, COUNT(1) AS paperCount,
       round(AVG(papers.paper_score), 1) AS avgScore
FROM (
    SELECT a.lesson_id, a.student_id, MAX(a.submit_time) AS last_submit, SUM(a.score) AS paper_score
    FROM biz_student_answer a
    WHERE a.submit_time IS NOT NULL AND a.submit_time >= date_sub(curdate(), INTERVAL 6 MONTH)
    GROUP BY a.lesson_id, a.student_id, date_format(a.submit_time, '%Y-%m')
) papers
GROUP BY date_format(papers.last_submit, '%Y-%m'), date_format(papers.last_submit, '%m月')
ORDER BY date_format(papers.last_submit, '%Y-%m');

-- ========== 可选：让趋势查询彻底免回表（本机实测 205ms → 157ms） ==========
-- 现有 idx_answer_submit_lesson_student(submit_time, lesson_id, student_id) 不含 score，
-- 带 sum(score) 的合并查询会退化为全表扫描。把 score 追加到索引末尾即可 index-only。
-- 索引名不变，前缀列不变，旧查询和唯一性判断都不受影响；答题表写入每行只多 4 字节。
-- 表行数越大收益越明显；若线上该表已超过百万行，建议低峰执行并先整库备份。
--
-- ALTER TABLE biz_student_answer
--     DROP INDEX idx_answer_submit_lesson_student,
--     ADD INDEX idx_answer_submit_lesson_student (submit_time, lesson_id, student_id, score),
--     ALGORITHM=INPLACE, LOCK=NONE;
--
-- 回滚：
-- ALTER TABLE biz_student_answer
--     DROP INDEX idx_answer_submit_lesson_student,
--     ADD INDEX idx_answer_submit_lesson_student (submit_time, lesson_id, student_id),
--     ALGORITHM=INPLACE, LOCK=NONE;

-- 回滚说明：本次代码改动不新增、不删除索引，回退代码即可恢复原行为；
-- 线上若已按 sql/biz_student_answer_latest_index_20261009.sql 建过 idx_answer_latest，
-- 新 SQL 不再使用它，可保留（不影响正确性），确认写放大压力后再决定是否删除。
