"""本机开发数据库迁移：全库mysqldump备份 + 执行feedback与菜单排序SQL"""
import datetime
import hashlib
import os
import re
import subprocess
from pathlib import Path
import pymysql
import yaml

ROOT = Path(__file__).resolve().parents[2]


def main():
    config = yaml.safe_load((ROOT / "RuoYi-Vue/ruoyi-admin/src/main/resources/application-druid.yml").read_text(encoding="utf-8"))
    master = config["spring"]["datasource"]["druid"]["master"]
    match = re.match(r"jdbc:mysql://([^:/]+):(\d+)/([^?]+)", master["url"])
    if not match or match[1] not in {"localhost", "127.0.0.1"} or match[3] != "xueyeceping_server_20260729":
        raise RuntimeError("仅允许在本机开发库 xueyeceping_server_20260729 执行")

    # 1. 备份
    backup_dir = ROOT / "output/textbook-cases/backups" / datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
    backup_dir.mkdir(parents=True, exist_ok=True)
    dump_file = backup_dir / "before_feedback_menu_migration.sql"
    env = os.environ.copy()
    env["MYSQL_PWD"] = str(master["password"])

    print(f"正在备份本机数据库 {match[3]} 到 {dump_file} ...")
    result = subprocess.run([
        r"D:/MySQL/MySQL Server 8.0/bin/mysqldump.exe",
        f"--host={match[1]}",
        f"--port={match[2]}",
        f"--user={master['username']}",
        "--single-transaction",
        "--routines",
        "--triggers",
        "--set-gtid-purged=OFF",
        match[3]
    ], stdout=dump_file.open("wb"), stderr=subprocess.PIPE, env=env)

    if result.returncode != 0 or dump_file.stat().st_size < 1_000_000:
        raise RuntimeError(f"mysqldump 备份失败: exit={result.returncode}, size={dump_file.stat().st_size}, err={result.stderr.decode('utf-8', errors='ignore')}")

    checksum = hashlib.sha256(dump_file.read_bytes()).hexdigest()
    (backup_dir / "SHA256SUMS").write_text(f"{checksum}  {dump_file.name}\n", encoding="utf-8")
    print(f"备份成功！大小={dump_file.stat().st_size} 字节，SHA256={checksum}")

    # 2. 执行 SQL
    conn = pymysql.connect(host=match[1], port=int(match[2]), user=master["username"],
                           password=str(master["password"]), database=match[3], charset="utf8mb4")
    try:
        with conn.cursor() as cursor:
            # 执行 feedback DDL
            feedback_sql = (ROOT / "sql/textbook_case_feedback_v1.sql").read_text(encoding="utf-8")
            for stmt in feedback_sql.split(";"):
                stmt = stmt.strip()
                if stmt:
                    cursor.execute(stmt)
            print("已执行 sql/textbook_case_feedback_v1.sql")

            # 执行 menu 排序 SQL
            menu_sql = (ROOT / "sql/menu_order_textbook_v1.sql").read_text(encoding="utf-8")
            for stmt in menu_sql.split(";"):
                stmt = stmt.strip()
                if stmt:
                    cursor.execute(stmt)
            print("已执行 sql/menu_order_textbook_v1.sql")

            # 验证两张反馈表
            cursor.execute("SHOW TABLES LIKE 'biz_textbook_case_feedback'")
            if not cursor.fetchone():
                raise RuntimeError("biz_textbook_case_feedback 未创建成功")
            cursor.execute("SHOW TABLES LIKE 'biz_textbook_case_invitation'")
            if not cursor.fetchone():
                raise RuntimeError("biz_textbook_case_invitation 未创建成功")

            # 验证菜单顺序
            cursor.execute("SELECT menu_id, menu_name, order_num FROM sys_menu WHERE menu_id IN (2031, 26100, 2048, 2042, 26000, 2018)")
            menu_results = {row[0]: (row[1], row[2]) for row in cursor.fetchall()}
            print("菜单验证:")
            for mid, (name, order) in menu_results.items():
                print(f"  menu_id={mid}: {name}, order_num={order}")

            # 检查排序规则:
            # 1. 26100 紧接 2031 之后 (70, 80)
            assert menu_results[2031][1] < menu_results[26100][1], "电子教材与优秀课例未能排在题库管理之后"
            # 2. 2042 紧接 2048 之后 (90, 100)
            assert menu_results[2048][1] < menu_results[2042][1], "成绩查询未能排在导学单管理之后"
            # 3. 26000 紧靠 2018 之前 (30, 40)
            assert menu_results[26000][1] < menu_results[2018][1], "教研活动未能排在学生管理之前"

        conn.commit()
        print("所有迁移执行并验证成功！")
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


if __name__ == "__main__":
    main()
