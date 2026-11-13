# -*- coding: utf-8 -*-
"""2026-11-13 概览索引扩展执行脚本（130 正式库 ry-vue）。

按 sql/platform_overview_index_score_20261113.sql 的口径在 130 上执行：
  step1 前检（索引现状/唯一键）+ 基线 EXPLAIN ANALYZE 两条聚合 SQL；
  step2 整库备份（gzip + SHA-256）到 /data/backups/xueyeceping/<时间戳>_overview_index_score_v1/；
  step3 幂等 DDL：idx_answer_submit_lesson_student 追加 score 列；
  step4 后检：索引列、EXPLAIN（range+Using index）、EXPLAIN ANALYZE 对比、服务探活。
任一步失败立即停止，不做后续步骤；凭据从服务器外置配置现取，绝不回显。
用法：python scripts/deploy/apply_overview_index_score_130.py
"""
import datetime
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import remote130

STAMP = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
BACKUP_DIR = f"/data/backups/xueyeceping/{STAMP}_overview_index_score_v1"

# 与 PlatformOverviewController 常量一致（只加 EXPLAIN ANALYZE，不改口径）
TREND_SQL = """select date_format(papers.last_submit, '%m月') as name, count(1) as paperCount, round(avg(papers.paper_score), 1) as avgScore
from (
  select a.lesson_id, a.student_id, max(a.submit_time) as last_submit, sum(a.score) as paper_score
  from biz_student_answer a
  where a.submit_time is not null and a.submit_time >= date_sub(curdate(), interval 6 month)
  group by a.lesson_id, a.student_id, date_format(a.submit_time, '%Y-%m')
) papers
group by date_format(papers.last_submit, '%Y-%m'), date_format(papers.last_submit, '%m月')
order by date_format(papers.last_submit, '%Y-%m')"""

SCHOOLS_SQL = """select d.dept_name, count(distinct papers.student_id) as active_student_count, round(avg(papers.paper_score), 1) as avg_score
from (
  select a.student_id, u.dept_id, sum(a.score) as paper_score
  from biz_student_answer a
  inner join biz_student s on a.student_id = s.student_id
  inner join sys_user u on s.user_id = u.user_id
  where a.submit_time >= date_sub(curdate(), interval 90 day) and a.score is not null
  group by a.lesson_id, a.student_id, u.dept_id
) papers
inner join sys_dept d on papers.dept_id = d.dept_id
group by d.dept_id, d.dept_name order by active_student_count desc, avg_score desc limit 8"""

PRECHECK_SQL = """SELECT index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS cols
FROM information_schema.statistics
WHERE table_schema = DATABASE() AND table_name = 'biz_student_answer'
  AND index_name IN ('idx_answer_submit_lesson_student', 'uk_student_lesson_question')
GROUP BY index_name;"""

DDL_PROC = r"""
DROP PROCEDURE IF EXISTS ensure_overview_index_score_20261113;
DELIMITER $$
CREATE PROCEDURE ensure_overview_index_score_20261113()
BEGIN
    DECLARE covered_count INT DEFAULT 0;
    DECLARE old_count INT DEFAULT 0;
    DECLARE uk_count INT DEFAULT 0;
    SELECT COUNT(*) INTO uk_count FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'biz_student_answer'
      AND index_name = 'uk_student_lesson_question';
    IF uk_count = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'precheck failed: uk_student_lesson_question missing';
    END IF;
    SELECT COUNT(*) INTO covered_count FROM (
        SELECT GROUP_CONCAT(column_name ORDER BY seq_in_index) AS cols
        FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'biz_student_answer'
        GROUP BY index_name
    ) i WHERE i.cols = 'submit_time,lesson_id,student_id,score';
    IF covered_count > 0 THEN
        SELECT 'SKIP: covered index exists' AS result;
    ELSE
        SELECT COUNT(*) INTO old_count FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'biz_student_answer'
          AND index_name = 'idx_answer_submit_lesson_student';
        IF old_count > 0 THEN
            ALTER TABLE biz_student_answer
                DROP INDEX idx_answer_submit_lesson_student,
                ADD INDEX idx_answer_submit_lesson_student (submit_time, lesson_id, student_id, score),
                ALGORITHM=INPLACE, LOCK=NONE;
        ELSE
            ALTER TABLE biz_student_answer
                ADD INDEX idx_answer_submit_lesson_student (submit_time, lesson_id, student_id, score),
                ALGORITHM=INPLACE, LOCK=NONE;
        END IF;
        SELECT 'DONE: index extended to 4 cols' AS result;
    END IF;
END$$
DELIMITER ;
CALL ensure_overview_index_score_20261113();
DROP PROCEDURE IF EXISTS ensure_overview_index_score_20261113;"""

POSTCHECK_SQL = """SELECT index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS cols
FROM information_schema.statistics
WHERE table_schema = DATABASE() AND table_name = 'biz_student_answer'
  AND index_name = 'idx_answer_submit_lesson_student'
GROUP BY index_name;"""


def mysql_script(body, timeout=600):
    """返回远端 sudo bash 脚本：现取凭据 → 临时 cnf → mysql heredoc。"""
    return f"""
set -euo pipefail
cfg=""
for f in /data/apps/xueyeceping/shared/config/application-druid.yml \
         /data/apps/xueyeceping/shared/20261008_seat_install_v1/config/application-druid.yml; do
  if [ -f "$f" ]; then cfg=$f; break; fi
done
[ -n "$cfg" ] || {{ echo "DRUID_CONFIG_NOT_FOUND"; exit 2; }}
u=$(awk '/username:/{{print $2; exit}}' "$cfg" | tr -d '"')
p=$(awk '/password:/{{print $2; exit}}' "$cfg" | tr -d '"')
umask 077
cnf=$(mktemp)
trap 'rm -f "$cnf"' EXIT
printf '[client]\\nuser=%s\\npassword=%s\\nhost=127.0.0.1\\n' "$u" "$p" > "$cnf"
mysql --defaults-extra-file="$cnf" --default-character-set=utf8mb4 ry-vue <<'SQL'
{body}
SQL
"""


def step(target, pw, name, script, timeout=600):
    print(f"\n===== {name} =====")
    code, out, err = remote130.sudo(target, pw, script, timeout=timeout)
    print(out)
    if code != 0:
        print(f"[失败 exit={code}] stderr: {err[:3000]}")
        sys.exit(1)
    return out


def main():
    jump, target, pw = remote130.connect()
    try:
        # step1 前检 + 基线 EXPLAIN ANALYZE（真实执行计时，与诊断 4.4s 对照）
        baseline = mysql_script(
            PRECHECK_SQL + "\n" + "EXPLAIN ANALYZE " + TREND_SQL + ";\n"
            + "EXPLAIN ANALYZE " + SCHOOLS_SQL + ";")
        step(target, pw, "step1 前检与基线计时", baseline, timeout=600)

        # step2 整库备份 + SHA-256
        backup = f"""
set -euo pipefail
cfg=/data/apps/xueyeceping/shared/config/application-druid.yml
[ -f "$cfg" ] || cfg=/data/apps/xueyeceping/shared/20261008_seat_install_v1/config/application-druid.yml
u=$(awk '/username:/{{print $2; exit}}' "$cfg" | tr -d '"')
p=$(awk '/password:/{{print $2; exit}}' "$cfg" | tr -d '"')
umask 077
cnf=$(mktemp)
trap 'rm -f "$cnf"' EXIT
printf '[client]\\nuser=%s\\npassword=%s\\nhost=127.0.0.1\\n' "$u" "$p" > "$cnf"
mkdir -p {BACKUP_DIR}
mysqldump --defaults-extra-file="$cnf" --single-transaction --quick --routines --triggers \
  --default-character-set=utf8mb4 ry-vue | gzip -9 > {BACKUP_DIR}/database.sql.gz
ls -l {BACKUP_DIR}/database.sql.gz
sha256sum {BACKUP_DIR}/database.sql.gz
"""
        step(target, pw, f"step2 整库备份 -> {BACKUP_DIR}", backup, timeout=900)

        # step3 幂等 DDL
        step(target, pw, "step3 索引扩展 DDL", mysql_script(DDL_PROC), timeout=900)

        # step4 后检：索引列 + 执行计划 + 计时 + 服务状态
        post = mysql_script(
            POSTCHECK_SQL + "\n" + "EXPLAIN " + TREND_SQL + ";\n"
            + "EXPLAIN ANALYZE " + TREND_SQL + ";\n"
            + "EXPLAIN ANALYZE " + SCHOOLS_SQL + ";")
        step(target, pw, "step4 后检与计时", post, timeout=600)
        code, out, err = remote130.run(target, "systemctl is-active xueyeceping-130; systemctl show xueyeceping-130 -p MainPID -p NRestarts")
        print("\n===== 服务状态 =====")
        print(out, err)
        print(f"\n备份目录：{BACKUP_DIR}")
    finally:
        target.close()
        jump.close()


if __name__ == "__main__":
    main()
