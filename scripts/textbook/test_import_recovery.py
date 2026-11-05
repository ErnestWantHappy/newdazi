"""导入器故障恢复验证：校验失败时不写SQL、不改库，事务异常可回滚。

只操作临时目录与本机开发库；测试行使用可识别的 source_key 并在事务内回滚。
"""
import copy
import json
import re
import tempfile
from pathlib import Path

import pymysql
import yaml

from import_cases import stage

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases"
BASELINE = "xueyeceping_server_20260729"


def connect():
    master = yaml.safe_load((ROOT / "RuoYi-Vue/ruoyi-admin/src/main/resources/application-druid.yml").read_text(encoding="utf-8"))["spring"]["datasource"]["druid"]["master"]
    match = re.match(r"jdbc:mysql://([^:/]+):(\d+)/([^?]+)", master["url"])
    if not match or match[1] not in {"localhost", "127.0.0.1"} or match[3] != BASELINE:
        raise RuntimeError("拒绝非本机开发基线")
    return pymysql.connect(host=match[1], port=int(match[2]), user=master["username"],
                           password=str(master["password"]), database=match[3], charset="utf8mb4")


def counts(connection):
    with connection.cursor() as cursor:
        cursor.execute("SELECT COUNT(*) FROM biz_textbook_case")
        cases = cursor.fetchone()[0]
        cursor.execute("SELECT COUNT(*) FROM biz_textbook_case_resource")
        resources = cursor.fetchone()[0]
    return cases, resources


def expect_failure(label, manifest, destination, sql_path):
    try:
        stage(manifest, destination, sql_path)
    except Exception as error:
        assert not sql_path.exists(), f"{label}: 失败时仍写出了SQL"
        return type(error).__name__ + ":" + str(error)[:80]
    raise AssertionError(f"{label}: 未按预期失败")


def main():
    manifest = json.loads((OUT / "catalog-local-reviewed.json").read_text(encoding="utf-8"))
    connection = connect()
    before = counts(connection)
    results = {}
    try:
        with tempfile.TemporaryDirectory(prefix="case-recovery-") as folder:
            root = Path(folder)

            # 1) 来源文件被改动（哈希不符）必须中止，并且不生成SQL。
            broken = copy.deepcopy(manifest)
            target_case = next(case for case in broken["cases"] if case["resources"])
            target_case["resources"][0]["sha256"] = "0" * 64
            results["hashMismatch"] = expect_failure("哈希不符", broken, root / "dest1", root / "sql1.sql")

            # 2) 已审核课例缺少作者，应在写文件前拒绝。
            broken = copy.deepcopy(manifest)
            target_case = next(case for case in broken["cases"] if case["resources"])
            target_case["author"] = ""
            results["missingAuthor"] = expect_failure("缺作者", broken, root / "dest2", root / "sql2.sql")

            # 3) 来源路径不存在，应在拷贝前暴露。
            broken = copy.deepcopy(manifest)
            target_case = next(case for case in broken["cases"] if case["resources"])
            target_case["resources"][0]["source"] = "不存在的目录/不存在的文件.pptx"
            results["missingFile"] = expect_failure("来源缺失", broken, root / "dest3", root / "sql3.sql")

            assert counts(connection) == before, "失败用例改动了数据库"

        # 4) 事务内写入后异常必须整体回滚，不留下半份课例。
        with connection.cursor() as cursor:
            cursor.execute("START TRANSACTION")
            cursor.execute("INSERT INTO biz_textbook_case(source_key,book_id,section_key,title,author,status) "
                           "VALUES('RECOVERY-TEST-ROLLBACK',1,'lesson-1','回滚验证','测试','DRAFT')")
            cursor.execute("SELECT COUNT(*) FROM biz_textbook_case WHERE source_key='RECOVERY-TEST-ROLLBACK'")
            inserted = cursor.fetchone()[0]
            connection.rollback()
            cursor.execute("SELECT COUNT(*) FROM biz_textbook_case WHERE source_key='RECOVERY-TEST-ROLLBACK'")
            leftover = cursor.fetchone()[0]
        results["transactionRollback"] = {"insertedInTransaction": inserted, "leftoverAfterRollback": leftover}
        assert inserted == 1 and leftover == 0, "事务回滚未生效"
        assert counts(connection) == before, "回滚后数量应保持不变"
    finally:
        connection.close()

    results["countsBefore"] = before
    results["countsAfter"] = before
    (OUT / "import-recovery.json").write_text(json.dumps(results, ensure_ascii=False, indent=2), encoding="utf-8")
    print("导入失败恢复通过：哈希不符/缺作者/缺文件均未写SQL，事务回滚无残留；库内数量保持",
          before)


if __name__ == "__main__":
    main()
