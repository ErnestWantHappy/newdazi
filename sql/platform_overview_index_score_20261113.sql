-- 2026-11-13 平台概览索引扩展：idx_answer_submit_lesson_student 追加 score 列（幂等，130 正式库 ry-vue）
--
-- 背景（2026-11-13 线上只读探查，recon_overview_index_130.py）：
--   biz_student_answer 共 672,225 行，近 6 个月提交 589,491 行（87.7%）。
--   平台概览两条聚合（6 个月趋势 / 90 天学校活跃）内层带 sum(score)，三列时间索引
--   (submit_time, lesson_id, student_id) 不含 score，范围扫描需逐行回表，87.7% 的选择率
--   下优化器直接放弃索引走全表扫描（EXPLAIN type=ALL, key=NULL, rows=636293），
--   130 单条 4.4 秒，快照后台全量构建 9~10 秒。
--   该方向在 sql/platform_overview_perf_20261103.sql 中已预留为可选项，本轮正式执行。
--
-- 变更：把 score 追加到索引末尾 → (submit_time, lesson_id, student_id, score)，
--   聚合所需 4 列全部在索引内，index-only 范围扫描；索引名不变、前缀列不变，
--   其余查询不受影响。score 为可空 int，每行写放大 +4 字节。
-- 无代码变更、无业务口径变化、无需重启应用；回滚即恢复三列形态。
--
-- 执行要求：先整库备份（/data/backups/xueyeceping/<时间戳>_overview_index_score_v1/，记录 SHA-256），
--   建议 22:00-06:00 低峰；67 万行表 INPLACE 重建预计秒级~十秒级，LOCK=NONE 不阻塞读写。

-- ========== 执行前检查（只读） ==========
-- 1) 目标索引与唯一键现状（期望 idx_answer_submit_lesson_student 列 = submit_time,lesson_id,student_id；
--    uk_student_lesson_question 列 = student_id,lesson_id,question_id，概览口径正确性依赖它）
SELECT index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS columns
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name = 'biz_student_answer'
  AND index_name IN ('idx_answer_submit_lesson_student', 'uk_student_lesson_question')
GROUP BY index_name;

-- 2) 是否已有人建过同列序四列索引（存在则本脚本应跳过）
SELECT index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS columns
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name = 'biz_student_answer'
GROUP BY index_name
HAVING columns IN ('submit_time,lesson_id,student_id,score');

-- ========== 幂等变更 ==========
DROP PROCEDURE IF EXISTS ensure_overview_index_score_20261113;
DELIMITER $$
CREATE PROCEDURE ensure_overview_index_score_20261113()
BEGIN
    DECLARE covered_count INT DEFAULT 0;
    DECLARE old_count INT DEFAULT 0;
    DECLARE uk_count INT DEFAULT 0;

    -- 正确性依赖：概览聚合把 sum(score) 的 NULL 传播当口径，前提是「学生+课程+题目」唯一
    SELECT COUNT(*) INTO uk_count
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'biz_student_answer'
      AND index_name = 'uk_student_lesson_question';
    IF uk_count = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '前检失败：uk_student_lesson_question 不存在，先核对 sql/typing_answer_dedup_fix.sql';
    END IF;

    -- 已有同列序四列索引则跳过（无论索引名）
    SELECT COUNT(*) INTO covered_count
    FROM (
        SELECT GROUP_CONCAT(column_name ORDER BY seq_in_index) AS cols
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'biz_student_answer'
        GROUP BY index_name
    ) i
    WHERE i.cols = 'submit_time,lesson_id,student_id,score';
    IF covered_count > 0 THEN
        SELECT 'SKIP: 四列索引已存在' AS result;
    ELSE
        SELECT COUNT(*) INTO old_count
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'biz_student_answer'
          AND index_name = 'idx_answer_submit_lesson_student';
        IF old_count > 0 THEN
            -- DROP+ADD 同一条 ALTER，原子完成，不出现中间态
            ALTER TABLE biz_student_answer
                DROP INDEX idx_answer_submit_lesson_student,
                ADD INDEX idx_answer_submit_lesson_student (submit_time, lesson_id, student_id, score),
                ALGORITHM=INPLACE, LOCK=NONE;
        ELSE
            ALTER TABLE biz_student_answer
                ADD INDEX idx_answer_submit_lesson_student (submit_time, lesson_id, student_id, score),
                ALGORITHM=INPLACE, LOCK=NONE;
        END IF;
        SELECT 'DONE: 索引已扩展为四列' AS result;
    END IF;
END$$
DELIMITER ;

CALL ensure_overview_index_score_20261113();
DROP PROCEDURE IF EXISTS ensure_overview_index_score_20261113;

-- ========== 执行后验证 ==========
SELECT index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS columns
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name = 'biz_student_answer'
  AND index_name = 'idx_answer_submit_lesson_student'
GROUP BY index_name;
-- 期望：submit_time,lesson_id,student_id,score

-- 趋势聚合内层（id=2, table=a）应变为 type=range、key=idx_answer_submit_lesson_student、Extra 含 Using index
EXPLAIN select date_format(papers.last_submit, '%m月') as name, count(1) as paperCount,
               round(avg(papers.paper_score), 1) as avgScore
from (
    select a.lesson_id, a.student_id, max(a.submit_time) as last_submit, sum(a.score) as paper_score
    from biz_student_answer a
    where a.submit_time is not null and a.submit_time >= date_sub(curdate(), interval 6 month)
    group by a.lesson_id, a.student_id, date_format(a.submit_time, '%Y-%m')
) papers
group by date_format(papers.last_submit, '%Y-%m'), date_format(papers.last_submit, '%m月')
order by date_format(papers.last_submit, '%Y-%m');

-- ========== 回滚（在线，恢复三列形态） ==========
-- ALTER TABLE biz_student_answer
--     DROP INDEX idx_answer_submit_lesson_student,
--     ADD INDEX idx_answer_submit_lesson_student (submit_time, lesson_id, student_id),
--     ALGORITHM=INPLACE, LOCK=NONE;
