"""导入后只读复核课例数量、来源字段、资源文件、重复与孤儿记录。"""
from __future__ import annotations

import argparse
import json
from pathlib import Path

from common import PipelineError, mysql_readonly, parse_tsv, read_json, run_dir, utc_now, write_json


def sql(manifest: dict) -> str:
    keys = ",".join("'%s'" % case["sourceKey"] for case in manifest["cases"])
    post_ids = ",".join(str(int(case["postId"])) for case in manifest["cases"])
    return r"""
SELECT 'case_count', COUNT(*) FROM biz_textbook_case WHERE source_key IN (%s);
SELECT 'duplicate_source', COUNT(*) FROM (SELECT source_key FROM biz_textbook_case
 WHERE source_key IN (%s) GROUP BY source_key HAVING COUNT(*)>1) x;
SELECT 'duplicate_post', COUNT(*) FROM (SELECT research_post_id FROM biz_textbook_case
 WHERE research_post_id IN (%s) GROUP BY research_post_id HAVING COUNT(*)>1) x;
SELECT 'case_field', c.case_id, c.source_key, c.status, c.origin, c.title, c.author, c.school,
       c.research_topic_id, c.research_topic_title, c.research_post_id, b.book_code, s.section_key, s.title
FROM biz_textbook_case c
JOIN biz_textbook_book b ON b.book_id=c.book_id
JOIN biz_textbook_section s ON s.book_id=c.book_id AND s.section_key=c.section_key AND s.kind='LESSON'
WHERE c.source_key IN (%s) ORDER BY c.case_id;
SELECT 'resource_count', COUNT(*) FROM biz_textbook_case_resource r
JOIN biz_textbook_case c ON c.case_id=r.case_id WHERE c.source_key IN (%s);
SELECT 'orphan_resource', COUNT(*) FROM biz_textbook_case_resource r
LEFT JOIN biz_textbook_case c ON c.case_id=r.case_id WHERE c.case_id IS NULL;
SELECT 'duplicate_resource', COUNT(*) FROM (SELECT case_id,resource_key FROM biz_textbook_case_resource
 GROUP BY case_id,resource_key HAVING COUNT(*)>1) x;
SELECT 'profile_path', COUNT(*) FROM biz_textbook_case_resource
WHERE original_path LIKE '/profile%%' OR preview_path LIKE '/profile%%';
SELECT 'resource_file', r.resource_id, r.original_path, r.preview_path, r.bytes, r.category, r.requirements
FROM biz_textbook_case_resource r JOIN biz_textbook_case c ON c.case_id=r.case_id
WHERE c.source_key IN (%s) ORDER BY r.case_id,r.sort_no,r.resource_id;
""" % (keys, keys, post_ids, keys, keys, keys)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--manifest", required=True)
    args = parser.parse_args()
    manifest = read_json(Path(args.manifest).resolve())
    run_path = run_dir(manifest["runId"])
    rows = parse_tsv(mysql_readonly(sql(manifest)))
    values: dict[str, list[list[str]]] = {}
    for row in rows:
        values.setdefault(row[0], []).append(row[1:])
    checks = {
        "caseCount": int(values.get("case_count", [["0"]])[0][0]),
        "expectedCases": len(manifest["cases"]),
        "duplicateSource": int(values.get("duplicate_source", [["0"]])[0][0]),
        "duplicatePost": int(values.get("duplicate_post", [["0"]])[0][0]),
        "resourceCount": int(values.get("resource_count", [["0"]])[0][0]),
        "expectedResources": len(manifest["resources"]),
        "orphanResources": int(values.get("orphan_resource", [["0"]])[0][0]),
        "duplicateResources": int(values.get("duplicate_resource", [["0"]])[0][0]),
        "profilePaths": int(values.get("profile_path", [["0"]])[0][0]),
    }
    failures = []
    if checks["caseCount"] != checks["expectedCases"]:
        failures.append("课例数量不符")
    if checks["resourceCount"] != checks["expectedResources"]:
        failures.append("资源数量不符")
    for key in ("duplicateSource", "duplicatePost", "orphanResources", "duplicateResources", "profilePaths"):
        if checks[key] != 0:
            failures.append("%s=%d" % (key, checks[key]))
    for case in manifest["cases"]:
        match = next((row for row in values.get("case_field", []) if row[1] == case["sourceKey"]), None)
        if not match:
            failures.append("课例缺失：" + case["sourceKey"])
            continue
        expected_topic = case["topicTitle"]
        if match[8] != expected_topic:
            failures.append("活动标题快照不一致：" + case["sourceKey"])
        if match[3] != "RESEARCH_ACTIVITY":
            failures.append("来源枚举错误：" + case["sourceKey"])
    result = {"runId": manifest["runId"], "verifiedAt": utc_now(), "checks": checks,
              "caseFields": values.get("case_field", []), "resourceFiles": values.get("resource_file", []),
              "failures": failures, "state": "PASS" if not failures else "FAIL"}
    write_json(run_path / "reports" / "verify-result.json", result)
    print(json.dumps(result, ensure_ascii=False, indent=2))
    return 0 if not failures else 1


if __name__ == "__main__":
    raise SystemExit(main())
