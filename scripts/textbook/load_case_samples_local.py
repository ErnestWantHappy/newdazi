"""仅向本机开发基线导入已核对的三份文档样板，不包含实验程序。"""
import json
import argparse
import datetime
import re
from pathlib import Path
import pymysql
import yaml
from import_cases import stage

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--manifest", type=Path, default=OUT / "samples-local-reviewed.json")
    args = parser.parse_args()
    master = yaml.safe_load((ROOT / "RuoYi-Vue/ruoyi-admin/src/main/resources/application-druid.yml").read_text(encoding="utf-8"))["spring"]["datasource"]["druid"]["master"]
    match = re.match(r"jdbc:mysql://([^:/]+):(\d+)/([^?]+)", master["url"])
    if not match or match[1] not in {"localhost", "127.0.0.1"} or match[3] != "xueyeceping_server_20260729":
        raise RuntimeError("拒绝非本机开发基线")
    if not list((OUT / "backups").glob("*/SHA256SUMS")):
        raise RuntimeError("必须先执行本机备份迁移入口")
    manifest = args.manifest
    data = json.loads(manifest.read_text(encoding="utf-8"))
    connection = pymysql.connect(host=match[1], port=int(match[2]), user=master["username"],
                                 password=str(master["password"]), database=match[3], charset="utf8mb4")
    try:
        with connection.cursor() as cursor:
            snapshot = {}
            for table in ("biz_textbook_case", "biz_textbook_case_resource"):
                cursor.execute("SELECT * FROM " + table)
                columns = [column[0] for column in cursor.description]
                snapshot[table] = [dict(zip(columns, row)) for row in cursor.fetchall()]
            backup = OUT / "backups" / ("cases-" + datetime.datetime.now().strftime("%Y%m%d-%H%M%S") + ".json")
            backup.write_text(json.dumps(snapshot, ensure_ascii=False, indent=2), encoding="utf-8")
            for case in data["cases"]:
                if not case.get("reviewed"):
                    continue
                cursor.execute("SELECT COUNT(*) FROM biz_textbook_section s JOIN biz_textbook_book b ON b.book_id=s.book_id WHERE b.book_code=%s AND s.section_key=%s AND s.kind='LESSON'", (case["bookCode"], case["sectionKey"]))
                if cursor.fetchone()[0] != 1:
                    raise RuntimeError("教材匹配不唯一，未导入")
        destination = ROOT / "output/textbook-pack-20261029/pages/cases"
        sql_path = OUT / "samples-local.sql"
        stage(manifest, destination, sql_path)
        with connection.cursor() as cursor:
            for statement in sql_path.read_text(encoding="utf-8").split(";"):
                if statement.strip():
                    cursor.execute(statement)
            cursor.execute("SELECT COUNT(*) FROM biz_textbook_case")
            cases = cursor.fetchone()[0]
            cursor.execute("SELECT COUNT(*) FROM biz_textbook_case_resource")
            print(f"本机样板导入完成：课例{cases}，资源{cursor.fetchone()[0]}。未连接正式环境。")
        connection.commit()
    except Exception:
        connection.rollback()
        raise
    finally:
        connection.close()


if __name__ == "__main__":
    main()
