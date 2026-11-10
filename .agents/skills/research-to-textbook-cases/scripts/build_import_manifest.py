"""生成按来源帖子幂等的导入清单、SQL 草稿与中文报告；不写正式库。"""
from __future__ import annotations

import argparse
import json
from collections import defaultdict
from pathlib import Path

from common import (
    OUTPUT_ROOT, PipelineError, display_title, read_json, resource_key,
    run_dir, safe_report, sha256_file, source_key, stable_sort_no, utc_now,
    write_json,
)


DDL = """-- 教研活动课例来源字段：幂等增量，旧课例保持空值。
SET @db_name = DATABASE();
SET @sql = (SELECT IF(COUNT(*)=0,
 'ALTER TABLE biz_textbook_case ADD COLUMN research_topic_id BIGINT NULL COMMENT ''来源教研活动ID'' AFTER origin', 'SELECT 1')
 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='biz_textbook_case' AND column_name='research_topic_id');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*)=0,
 'ALTER TABLE biz_textbook_case ADD COLUMN research_topic_title VARCHAR(255) NOT NULL DEFAULT '''' COMMENT ''来源教研活动标题快照'' AFTER research_topic_id', 'SELECT 1')
 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='biz_textbook_case' AND column_name='research_topic_title');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*)=0,
 'ALTER TABLE biz_textbook_case ADD COLUMN research_post_id BIGINT NULL COMMENT ''来源教研帖子ID'' AFTER research_topic_title', 'SELECT 1')
 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='biz_textbook_case' AND column_name='research_post_id');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*)=0,
 'ALTER TABLE biz_textbook_case ADD UNIQUE KEY uk_research_post_id (research_post_id)', 'SELECT 1')
 FROM information_schema.statistics WHERE table_schema=@db_name AND table_name='biz_textbook_case' AND index_name='uk_research_post_id');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
"""


def sql_literal(value) -> str:
    if value is None:
        return "NULL"
    if isinstance(value, bool):
        return "1" if value else "0"
    if isinstance(value, (int, float)):
        return str(value)
    escaped = str(value).replace("\\", "\\\\").replace("'", "''")
    return "'" + escaped + "'"


def case_sql(case: dict) -> list[str]:
    return [
        "INSERT INTO biz_textbook_case"
        "(source_key,book_id,section_key,title,author,school,origin,status,sort_no,"
        "research_topic_id,research_topic_title,research_post_id) VALUES"
        "(%s,%s,%s,%s,%s,%s,'RESEARCH_ACTIVITY','DRAFT',%s,%s,%s,%s) "
        "ON DUPLICATE KEY UPDATE book_id=VALUES(book_id),section_key=VALUES(section_key),"
        "title=VALUES(title),author=VALUES(author),school=VALUES(school),"
        "origin=VALUES(origin),research_topic_id=VALUES(research_topic_id),"
        "research_topic_title=VALUES(research_topic_title),research_post_id=VALUES(research_post_id);" %
        tuple(sql_literal(case[key]) for key in ("sourceKey", "bookId", "sectionKey", "title", "author", "school",
                                                "sortNo", "topicId", "topicTitle", "postId"))
    ]


def resource_sql(resource: dict) -> str:
    preview = resource.get("previewPath")
    return ("INSERT INTO biz_textbook_case_resource"
            "(case_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no) "
            "SELECT case_id,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s FROM biz_textbook_case WHERE source_key=%s "
            "ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),"
            "original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),"
            "description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);" %
            tuple(sql_literal(value) for value in (
                resource["resourceKey"], resource["category"], resource["title"], resource["filename"],
                resource["originalPath"], preview, resource["bytes"], resource.get("description", ""),
                resource.get("requirements", ""), resource["sortNo"], resource["sourceKey"])))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--run-id", required=True)
    args = parser.parse_args()
    run_path = run_dir(args.run_id)
    scan = {int(item["postId"]): item for item in read_json(OUTPUT_ROOT / ("scan-%s.json" % args.run_id))["posts"]}
    sections = {int(item["postId"]): item for item in read_json(OUTPUT_ROOT / ("sections-%s.json" % args.run_id))["items"]}
    assets = read_json(run_path / "reports" / "assets.json")["assets"]
    resources = read_json(run_path / "reports" / "resources.json")["resources"]
    summaries = {item["archiveName"]: item["requirements"] for item in read_json(run_path / "reports" / "archive-summaries.json")["summaries"]}
    inspection = read_json(run_path / "reports" / "archive-inspection.json")
    previews = read_json(run_path / "reports" / "preview-conversion.json")
    # 来源程序和 .url 保留在原包、不单独上架即可；只有无法安全解析或真正危险的条目才阻断。
    blockers = [item for item in inspection["archives"]
                if item.get("state") == "PENDING_ARCHIVE" and not item.get("pendingReason", "").startswith("压缩包包含需本地运行")
                or any("路径" in warning or "压缩比" in warning or "个人信息" in warning or "内网" in warning
                       for warning in item.get("safety", []))]
    by_post: dict[int, list[dict]] = defaultdict(list)
    for resource in resources:
        by_post[int(resource["postId"])].append(resource)
    cases = []
    draft_sql = [DDL, "START TRANSACTION;"]
    for post_id, resolved in sections.items():
        if resolved["state"] != "RESOLVED":
            continue
        post = scan[post_id]
        case = {
            "postId": post_id,
            "sourceKey": source_key(post_id),
            "topicId": int(post["topicId"]),
            "topicTitle": post["topicTitle"],
            "bookId": int(resolved["resolved"]["bookId"]),
            "sectionKey": resolved["resolved"]["sectionKey"],
            "title": post["courseTitle"].strip("《》"),
            "author": post["author"],
            "school": post["school"],
            "sortNo": post_id,
            "origin": "RESEARCH_ACTIVITY",
            "status": "DRAFT",
        }
        cases.append(case)
        draft_sql.extend(case_sql(case))
        for resource in sorted(by_post[post_id], key=lambda item: item["sortNo"]):
            resource["sourceKey"] = case["sourceKey"]
            resource["originalPath"] = resource["sha256"][:2] + "/" + resource["sha256"] + Path(resource["filename"]).suffix
            if resource["category"] == "ARCHIVE":
                resource["requirements"] = summaries.get(resource["filename"], resource.get("requirements", ""))
            draft_sql.append(resource_sql(resource))
    draft_sql.append("COMMIT;")
    sql_path = run_path / "reports" / "import-draft.sql"
    sql_path.write_text("\n".join(draft_sql) + "\n", encoding="utf-8")
    manifest = {
        "runId": args.run_id,
        "generatedAt": utc_now(),
        "state": "BLOCKED" if blockers else "READY_FOR_APPLY",
        "dryRun": True,
        "sourceFingerprints": {str(key): value["postUpdatedAt"] for key, value in scan.items()},
        "cases": cases,
        "resources": safe_report(resources),
        "assets": safe_report(assets),
        "archiveInspection": {
            "generatedAt": inspection.get("generatedAt"),
            "tool": inspection.get("tool"),
            "toolVersion": inspection.get("toolVersion"),
            "archives": [{
                "name": archive.get("name"),
                "bytes": archive.get("bytes"),
                "sha256": archive.get("sha256"),
                "state": archive.get("state"),
                "pendingReason": archive.get("pendingReason"),
                "entryCount": archive.get("entryCount", len(archive.get("entries", []))),
                "safety": archive.get("safety", []),
                "urlTargets": [entry.get("urlTargets") for entry in archive.get("entries", []) if entry.get("urlTargets")],
            } for archive in inspection.get("archives", [])],
        },
        "previews": safe_report(previews),
        "sqlDraft": "reports/import-draft.sql",
        "blockers": safe_report(blockers),
        "applyPolicy": "仅 apply 命令在用户确认的低峰窗口、备份及前检通过后执行",
    }
    write_json(run_path / "manifest.json", manifest)
    report = {
        "runId": args.run_id,
        "state": manifest["state"],
        "cases": [{"postId": item["postId"], "title": item["title"], "author": item["author"],
                   "school": item["school"], "topicTitle": item["topicTitle"]} for item in cases],
        "resources": len(resources),
        "previews": sum(1 for item in previews["items"] if item["state"] == "CONVERTED"),
        "archiveBlockers": [item["name"] for item in blockers],
        "zhaoQi": "post 60 已纳入整理，成功解析课例与资源" if any(item["postId"] == 60 for item in cases) else "post 60 未在本次导入清单中",
    }
    write_json(run_path / "reports" / "chinese-report.json", report)
    print(json.dumps({"manifest": str(run_path / "manifest.json"), "state": manifest["state"],
                      "sqlDraft": str(sql_path), "report": str(run_path / "reports" / "chinese-report.json")},
                     ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
