# -*- coding: utf-8 -*-
"""2026-11-13 概览索引扩展前只读探查（130 正式库 ry-vue）。

目的：为 sql/platform_overview_index_score_20261113.sql 的索引扩展
（idx_answer_submit_lesson_student 追加 score 列）收集事实：
  1) MySQL 版本与 biz_student_answer 规模；
  2) 目标索引现状、score 列类型、唯一键 uk_student_lesson_question；
  3) 趋势/学校活跃两条聚合 SQL 的当前 EXPLAIN（扩展前基线）。
只读，不做任何变更；凭据只从服务器外置配置现取，绝不回显。
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import remote130

# 与 PlatformOverviewController 常量保持一致（提交前勿改动 SQL 口径）
TREND_SQL = r"""EXPLAIN
select date_format(papers.last_submit, '%m月') as name, count(1) as paperCount, round(avg(papers.paper_score), 1) as avgScore
from (
  select a.lesson_id, a.student_id, max(a.submit_time) as last_submit, sum(a.score) as paper_score
  from biz_student_answer a
  where a.submit_time is not null and a.submit_time >= date_sub(curdate(), interval 6 month)
  group by a.lesson_id, a.student_id, date_format(a.submit_time, '%Y-%m')
) papers
group by date_format(papers.last_submit, '%Y-%m'), date_format(papers.last_submit, '%m月')
order by date_format(papers.last_submit, '%Y-%m')\G"""

SCHOOLS_SQL = r"""EXPLAIN
select d.dept_name, count(distinct papers.student_id) as active_student_count, round(avg(papers.paper_score), 1) as avg_score
from (
  select a.student_id, u.dept_id, sum(a.score) as paper_score
  from biz_student_answer a
  inner join biz_student s on a.student_id = s.student_id
  inner join sys_user u on s.user_id = u.user_id
  where a.submit_time >= date_sub(curdate(), interval 90 day) and a.score is not null
  group by a.lesson_id, a.student_id, u.dept_id
) papers
inner join sys_dept d on papers.dept_id = d.dept_id
group by d.dept_id, d.dept_name order by active_student_count desc, avg_score desc limit 8\G"""

FACT_SQL = r"""SELECT VERSION();
SELECT COUNT(*) AS answer_rows FROM biz_student_answer;
SELECT index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS cols, non_unique
FROM information_schema.statistics
WHERE table_schema = DATABASE() AND table_name = 'biz_student_answer'
GROUP BY index_name, non_unique ORDER BY index_name;
SELECT column_name, column_type, is_nullable FROM information_schema.columns
WHERE table_schema = DATABASE() AND table_name = 'biz_student_answer' AND column_name IN ('score', 'submit_time');
SELECT COUNT(*) AS recent_6m_rows FROM biz_student_answer
WHERE submit_time >= date_sub(curdate(), interval 6 month);"""


def main():
    jump, target, pw = remote130.connect()
    try:
        # 与 deploy 脚本同款：外置配置现取凭据，临时 cnf 用后即删
        script = r"""
set -euo pipefail
cfg=""
for f in /data/apps/xueyeceping/shared/20261008_seat_install_v1/config/application-druid.yml \
         /data/apps/xueyeceping/shared/config/application-druid.yml; do
  if [ -f "$f" ]; then cfg=$f; break; fi
done
[ -n "$cfg" ] || { echo "DRUID_CONFIG_NOT_FOUND"; exit 2; }
echo "CONFIG=$cfg"
u=$(awk '/username:/{print $2; exit}' "$cfg" | tr -d '"')
p=$(awk '/password:/{print $2; exit}' "$cfg" | tr -d '"')
umask 077
cnf=$(mktemp)
trap 'rm -f "$cnf"' EXIT
printf '[client]\nuser=%s\npassword=%s\nhost=127.0.0.1\n' "$u" "$p" > "$cnf"
mysql --defaults-extra-file="$cnf" --default-character-set=utf8mb4 ry-vue <<'SQL'
__SQL__
SQL
"""
        body = FACT_SQL + "\n" + TREND_SQL + "\n" + SCHOOLS_SQL
        code, out, err = remote130.sudo(target, pw, script.replace("__SQL__", body), timeout=300)
        print("exit=", code)
        print(out)
        if err.strip():
            print("[stderr]", err[:2000])
    finally:
        target.close()
        jump.close()


if __name__ == "__main__":
    main()
