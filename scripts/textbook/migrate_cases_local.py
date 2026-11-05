"""仅本机课例迁移：整库备份成功并计算哈希后才执行新增两表DDL。"""
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
        raise RuntimeError("拒绝非本机开发基线")
    backup = ROOT / "output/textbook-cases/backups" / datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
    backup.mkdir(parents=True)
    dump = backup / "before.sql"
    environment = os.environ.copy()
    environment["MYSQL_PWD"] = str(master["password"])
    with dump.open("wb") as stream:
        result = subprocess.run([r"D:/MySQL/MySQL Server 8.0/bin/mysqldump.exe", "--host=" + match[1],
                                 "--port=" + match[2], "--user=" + str(master["username"]),
                                 "--single-transaction", "--routines", "--triggers", "--set-gtid-purged=OFF", match[3]],
                                stdout=stream, stderr=subprocess.PIPE, env=environment)
    if result.returncode or dump.stat().st_size < 1000000:
        raise RuntimeError("备份失败，未执行迁移")
    checksum = hashlib.sha256(dump.read_bytes()).hexdigest()
    (backup / "SHA256SUMS").write_text(checksum + "  before.sql\n", encoding="utf-8")
    connection = pymysql.connect(host=match[1], port=int(match[2]), user=master["username"],
                                 password=str(master["password"]), database=match[3], charset="utf8mb4")
    try:
        sql = (ROOT / "sql/textbook_cases_v1.sql").read_text(encoding="utf-8")
        with connection.cursor() as cursor:
            for statement in sql.split(";"):
                if statement.strip():
                    cursor.execute(statement)
            cursor.execute("SELECT COUNT(*) FROM biz_textbook_case")
            count = cursor.fetchone()[0]
        connection.commit()
        print(f"本机迁移通过；现有课例={count}；备份={dump.relative_to(ROOT)}；SHA256={checksum}")
    finally:
        connection.close()


if __name__ == "__main__":
    main()
