# -*- coding: utf-8 -*-
"""2026-11-13 MySQL 内存参数调优（130 正式库，在线生效 + 持久化）。

背景：服务器 32G 内存 / 26.7G 可用 / 16 核，MySQL 却用默认 128M buffer pool、
16M tmp_table_size；概览趋势聚合 117,363 组的临时表溢出磁盘（会话
Created_tmp_disk_tables +1），是 4.4 秒的主要瓶颈。

变更（全部可即时回退，回退值：tmp=16M / heap=16M / pool=128M）：
  A. 动态：tmp_table_size/max_heap_table_size 16M→64M；innodb_buffer_pool_size 128M→2G；
  B. 持久化：/etc/mysql/mysql.conf.d/mysqld.cnf 第二个 [mysqld] 段追加三行（先备份）。
分步验证：调 tmp 前后各跑一次趋势聚合计时，分辨各项贡献；最后服务探活。
"""
import datetime
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import remote130
from apply_overview_index_score_130 import mysql_script, TREND_SQL, SCHOOLS_SQL

STAMP = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
CNF = "/etc/mysql/mysql.conf.d/mysqld.cnf"

BENCH = ("EXPLAIN ANALYZE " + TREND_SQL + ";\n"
         + "EXPLAIN ANALYZE " + SCHOOLS_SQL + ";\n"
         + "SHOW SESSION STATUS LIKE 'Created_tmp_disk_tables';")


def run_step(target, pw, name, script, timeout=600):
    print(f"\n===== {name} =====")
    code, out, err = remote130.sudo(target, pw, script, timeout=timeout)
    print(out[:6000])
    if code != 0:
        print(f"[失败 exit={code}] stderr: {err[:2000]}")
        sys.exit(1)
    return out


def main():
    jump, target, pw = remote130.connect()
    try:
        # 1) 当前值基线
        run_step(target, pw, "step1 调整前参数值",
                 mysql_script("SELECT @@tmp_table_size/1048576 AS tmp_mb, "
                              "@@max_heap_table_size/1048576 AS heap_mb, "
                              "@@innodb_buffer_pool_size/1048576 AS pool_mb;"), 300)

        # 2) 动态调整 tmp 表（立即生效，命中趋势聚合磁盘临时表瓶颈）
        run_step(target, pw, "step2 动态调整 tmp_table_size/max_heap_table_size -> 64M",
                 mysql_script("SET GLOBAL tmp_table_size = 67108864;\n"
                              "SET GLOBAL max_heap_table_size = 67108864;\n"
                              "SELECT @@tmp_table_size/1048576 AS tmp_mb, "
                              "@@max_heap_table_size/1048576 AS heap_mb;"), 300)

        # 3) 调 tmp 后基准（趋势聚合应不再溢出磁盘）
        run_step(target, pw, "step3 tmp=64M 后计时", mysql_script(BENCH), 600)

        # 4) 动态扩 buffer pool（8.0 在线调整，128M→2G）
        run_step(target, pw, "step4 动态调整 innodb_buffer_pool_size -> 2G",
                 mysql_script("SET GLOBAL innodb_buffer_pool_size = 2147483648;\n"
                              "SELECT @@innodb_buffer_pool_size/1048576 AS pool_mb, "
                              "@@innodb_buffer_pool_chunk_size/1048576 AS chunk_mb;"), 600)

        # 5) 最终基准
        run_step(target, pw, "step5 全部调整后计时", mysql_script(BENCH), 600)

        # 6) 持久化：备份后追加配置（幂等：已写则跳过）
        persist = f"""
set -euo pipefail
if grep -q "xueyeceping-perf-20261113" {CNF}; then
  echo "SKIP: 配置已持久化"
else
  cp {CNF} {CNF}.bak.{STAMP}
  cat >> {CNF} <<'EOF'

# xueyeceping-perf-20261113 概览聚合与整站查询内存调优（32G 内存服务器）
# 回退：删除本段并 SET GLOBAL 恢复 16777216/16777216/134217728
tmp_table_size = 64M
max_heap_table_size = 64M
innodb_buffer_pool_size = 2G
EOF
  echo "DONE: 已追加并备份为 {CNF}.bak.{STAMP}"
  tail -8 {CNF}
fi
"""
        run_step(target, pw, "step6 持久化到 mysqld.cnf", persist, 300)

        # 7) 服务探活（MySQL 与应用都不应重启）
        code, out, err = remote130.run(
            target, "systemctl is-active mysql xueyeceping-130; "
                    "systemctl show mysql -p MainPID --value; "
                    "systemctl show xueyeceping-130 -p MainPID -p NRestarts --value")
        print("\n===== 服务状态 =====")
        print(out, err[:500])
    finally:
        target.close()
        jump.close()


if __name__ == "__main__":
    main()
