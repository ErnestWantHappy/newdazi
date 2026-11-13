# -*- coding: utf-8 -*-
"""2026-11-13 概览索引扩展后补充只读探查：临时表/内存配置，判断聚合临时表是否溢出磁盘。"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import remote130
from apply_overview_index_score_130 import mysql_script

BODY = """SHOW VARIABLES WHERE Variable_name IN
  ('tmp_table_size','max_heap_table_size','internal_tmp_mem_storage_engine','temptable_max_ram','tmpdir');
SHOW GLOBAL STATUS LIKE 'Created_tmp%';
SELECT @@innodb_buffer_pool_size/1024/1024 AS buffer_pool_mb;
-- 趋势聚合一次，观察本次会话临时表计数差
SHOW SESSION STATUS LIKE 'Created_tmp%';
SELECT COUNT(*) FROM (
  SELECT a.lesson_id, a.student_id, MAX(a.submit_time), SUM(a.score)
  FROM biz_student_answer a
  WHERE a.submit_time IS NOT NULL AND a.submit_time >= date_sub(curdate(), INTERVAL 6 MONTH)
  GROUP BY a.lesson_id, a.student_id, date_format(a.submit_time, '%Y-%m')
) t;
SHOW SESSION STATUS LIKE 'Created_tmp%';"""


def main():
    jump, target, pw = remote130.connect()
    try:
        code, out, err = remote130.sudo(target, pw, mysql_script(BODY), timeout=300)
        print("exit=", code)
        print(out)
        if err.strip():
            print("[stderr]", err[:1500])
    finally:
        target.close()
        jump.close()


if __name__ == "__main__":
    main()
