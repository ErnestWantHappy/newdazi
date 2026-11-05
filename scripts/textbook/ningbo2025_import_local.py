"""2025宁波课例批次：本机导入（备份→前检→复制资源→执行增量 SQL→后检）。

只允许本机开发库；拒绝在非本机或非约定库上执行。执行前整表备份并计算 SHA256。
"""
import argparse
import datetime
import hashlib
import json
import re
import shutil
import subprocess
from pathlib import Path

import pymysql
import yaml

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases-2025"
STAGE = OUT / "stage"
CATALOG = OUT / "catalog-reviewed-2025.json"
SQL_FILE = ROOT / "sql/textbook_cases_ningbo2025_v1.sql"
TEXTBOOK_ROOT = ROOT / "output/textbook-pack-20261029/pages"
EXPECTED_DB = "xueyeceping_server_20260729"
TABLES = ["biz_textbook_case", "biz_textbook_case_resource",
          "biz_textbook_case_feedback", "biz_textbook_case_invitation"]


def digest(path):
    result = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            result.update(chunk)
    return result.hexdigest()


def connect():
    master = yaml.safe_load((ROOT / "RuoYi-Vue/ruoyi-admin/src/main/resources/application-druid.yml")
                            .read_text(encoding="utf-8"))["spring"]["datasource"]["druid"]["master"]
    match = re.match(r"jdbc:mysql://([^:/]+):(\d+)/([^?]+)", master["url"])
    if not match or match[1] not in {"localhost", "127.0.0.1"} or match[3] != EXPECTED_DB:
        raise SystemExit("拒绝执行：目标不是本机开发库 " + EXPECTED_DB)
    return pymysql.connect(host=match[1], port=int(match[2]), user=master["username"],
                           password=str(master["password"]), database=match[3], charset="utf8mb4",
                           autocommit=False), match[3]


def backup(connection):
    folder = OUT / "backups" / datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
    folder.mkdir(parents=True, exist_ok=True)
    snapshot = {}
    with connection.cursor(pymysql.cursors.DictCursor) as cursor:
        for table in TABLES:
            try:
                cursor.execute("SELECT * FROM " + table)
                snapshot[table] = cursor.fetchall()
            except pymysql.err.ProgrammingError:
                snapshot[table] = None
    target = folder / "case-tables.json"
    target.write_text(json.dumps(snapshot, ensure_ascii=False, indent=2, default=str), encoding="utf-8")
    return target


def precheck(connection, catalog):
    problems = []
    with connection.cursor() as cursor:
        for case in catalog["cases"]:
            if case.get("status", "PUBLISHED") != "PUBLISHED":
                continue
            cursor.execute("SELECT COUNT(*) FROM biz_textbook_section s JOIN biz_textbook_book b "
                           "ON b.book_id=s.book_id WHERE b.book_code=%s AND s.section_key=%s AND s.kind='LESSON'",
                           (case["bookCode"], case["sectionKey"]))
            count = cursor.fetchone()[0]
            if count != 1:
                problems.append("%s -> %s/%s 匹配 %d 条 LESSON" % (case["title"], case["bookCode"],
                                                                    case["sectionKey"], count))
        cursor.execute("SELECT COUNT(*) FROM biz_textbook_case")
        before_cases = cursor.fetchone()[0]
        cursor.execute("SELECT COUNT(*) FROM biz_textbook_case_resource")
        before_resources = cursor.fetchone()[0]
    return problems, before_cases, before_resources


def copy_files(catalog):
    destination = TEXTBOOK_ROOT / "cases"
    destination.mkdir(parents=True, exist_ok=True)
    copied = skipped = 0
    bytes_total = 0
    for case in catalog["cases"]:
        if case.get("status", "PUBLISHED") != "PUBLISHED":
            continue
        for resource in case["resources"]:
            for key in ("original_path", "preview_path"):
                relative = resource.get(key)
                if not relative:
                    continue
                # 视频的 preview_path 就是原件本身，复制一次即可。
                if key == "preview_path" and resource["kind"] == "video":
                    continue
                stage_relative = ("files/" + relative) if key == "original_path" and resource["kind"] != "package" else relative
                source = STAGE / stage_relative
                target = destination / relative
                if not source.is_file():
                    raise SystemExit("暂存文件缺失：" + str(source))
                if target.is_file() and target.stat().st_size == source.stat().st_size:
                    skipped += 1
                    continue
                target.parent.mkdir(parents=True, exist_ok=True)
                shutil.copyfile(source, target)
                copied += 1
                bytes_total += source.stat().st_size
    return copied, skipped, bytes_total


def execute(connection):
    statements = [item.strip() for item in SQL_FILE.read_text(encoding="utf-8").split(";") if item.strip()]
    executed = 0
    with connection.cursor() as cursor:
        for statement in statements:
            if statement.startswith("--"):
                statement = "\n".join(line for line in statement.splitlines() if not line.strip().startswith("--"))
            if not statement.strip():
                continue
            cursor.execute(statement)
            executed += 1
    connection.commit()
    return executed


def postcheck(connection, catalog, before_cases, before_resources):
    expected_cases = sum(1 for case in catalog["cases"] if case.get("status", "PUBLISHED") == "PUBLISHED")
    expected_resources = sum(len(case["resources"]) for case in catalog["cases"]
                             if case.get("status", "PUBLISHED") == "PUBLISHED")
    with connection.cursor() as cursor:
        cursor.execute("SELECT COUNT(*) FROM biz_textbook_case")
        after_cases = cursor.fetchone()[0]
        cursor.execute("SELECT COUNT(*) FROM biz_textbook_case_resource")
        after_resources = cursor.fetchone()[0]
        cursor.execute("SELECT COUNT(*) FROM biz_textbook_case_resource r LEFT JOIN biz_textbook_case c "
                       "ON c.case_id=r.case_id WHERE c.case_id IS NULL")
        orphans = cursor.fetchone()[0]
        cursor.execute("SELECT COUNT(*) FROM (SELECT case_id,resource_key,COUNT(*) c FROM "
                       "biz_textbook_case_resource GROUP BY case_id,resource_key HAVING c>1) t")
        duplicates = cursor.fetchone()[0]
        cursor.execute("SELECT COUNT(*) FROM biz_textbook_case WHERE status='PUBLISHED' AND source_key LIKE 'ningbo-2025:%'")
        batch_cases = cursor.fetchone()[0]
    return {"beforeCases": before_cases, "afterCases": after_cases, "expectedCases": expected_cases,
            "beforeResources": before_resources, "afterResources": after_resources,
            "expectedResources": expected_resources, "orphans": orphans, "duplicates": duplicates,
            "batchCases": batch_cases}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("action", choices=["dry-run", "apply"])
    args = parser.parse_args()
    catalog = json.loads(CATALOG.read_text(encoding="utf-8"))
    connection, database = connect()
    try:
        problems, before_cases, before_resources = precheck(connection, catalog)
        print("前检：课例 %d、资源 %d，映射问题 %d" % (before_cases, before_resources, len(problems)))
        for problem in problems:
            print("  " + problem)
        if problems:
            raise SystemExit("教材映射不唯一，停止导入")
        if args.action == "dry-run":
            copied, skipped, total = 0, 0, 0
            for case in catalog["cases"]:
                if case.get("status", "PUBLISHED") != "PUBLISHED":
                    continue
                for resource in case["resources"]:
                    for key in ("original_path", "preview_path"):
                        if resource.get(key):
                            copied += 1
            print("dry-run：待处理文件 %d（未复制、未写库）" % copied)
            return
        backup_path = backup(connection)
        print("备份：%s %d 字节 sha256=%s" % (backup_path, backup_path.stat().st_size, digest(backup_path)))
        copied, skipped, total = copy_files(catalog)
        print("复制：新增 %d，跳过 %d，%.1f MB" % (copied, skipped, total / 1048576))
        executed = execute(connection)
        print("执行语句 %d 条" % executed)
        result = postcheck(connection, catalog, before_cases, before_resources)
        (OUT / "local-import-result.json").write_text(json.dumps(result, ensure_ascii=False, indent=2),
                                                      encoding="utf-8")
        print("后检：" + json.dumps(result, ensure_ascii=False))
    finally:
        connection.close()


if __name__ == "__main__":
    main()
