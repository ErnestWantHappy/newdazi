"""根据暂存清单生成可审计、幂等的导入 SQL。

用法：python -X utf8 scripts/textbook/teaching_resources_20261107_sql.py
输出：sql/teaching_resources_20261107_data.sql
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-resources-20261107"
TARGET = ROOT / "sql/teaching_resources_20261107_data.sql"
BATCH = "nb2026_20261107"


def q(value):
    if value is None:
        return "NULL"
    return "'" + str(value).replace("\\", "\\\\").replace("'", "''").replace("\n", "\\n").replace("\r", "") + "'"


def num(value, default=0):
    try:
        return int(value)
    except (TypeError, ValueError):
        return default


def main():
    manifest = json.loads((OUT / "resource-manifest.json").read_text(encoding="utf-8"))
    # 复用旧课例时，清单里的 sourceKey 是本批次键；跨库定位必须用旧课例自身稳定的 source_key。
    existing_keys = json.loads((OUT / "existing-case-source-keys.json").read_text(encoding="utf-8"))
    missing_previews = []
    lines = [
        "-- 电子教材与教学资源 v1 数据导入（2026-11-07，自动生成）",
        "-- 依赖 teaching_resources_20261107_v1.sql 先建表。幂等：按 source_key / resource_key / grade / lesson_no 更新。",
        "-- 不修改旧课例的作者、身份、评价和既有资源。",
        "",
        "SET NAMES utf8mb4;",
        "START TRANSACTION;",
        "",
    ]

    section_order = {}
    for case in manifest["cases"]:
        key = (case["bookCode"], case["sectionKey"])
        section_order[key] = section_order.get(key, 0) + 1
        sort_no = 100 + section_order[key]
        source_key = case["sourceKey"]
        # 课例 ID 随各环境导入历史而不同，跨库发布必须按稳定 source_key 定位；
        # 复用旧课例时也不能写死本机 case_id，否则资源会挂到线上同号的其他课例。
        existing_case_id = case.get("existingCaseId")
        if existing_case_id:
            case_filter = f"c.source_key={q(existing_keys[str(existing_case_id)])}"
        else:
            case_filter = f"c.source_key={q(source_key)}"
        if not existing_case_id:
            lines.append(
                "INSERT INTO biz_textbook_case "
                "(source_key,book_id,section_key,title,author,school,origin,status,sort_no)\n"
                "SELECT {source},{book}.book_id,{section},{title},{author},{school},'NINGBO2026','PUBLISHED',{sort}\n"
                "FROM biz_textbook_book {book}\n"
                "JOIN biz_textbook_section s ON s.book_id={book}.book_id AND s.section_key={section} AND s.kind='LESSON'\n"
                "WHERE {book}.book_code={code}\n"
                "ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),"
                "origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);".format(
                    source=q(source_key), book="b", section=q(case["sectionKey"]),
                    title=q(case["title"]), author=q(case.get("author") or ""),
                    school=q(case.get("school") or ""), sort=sort_no, code=q(case["bookCode"])))
            case_filter = f"c.source_key={q(source_key)}"

        for index, resource in enumerate(case["resources"], 1):
            preview = resource.get("previewPath")
            if preview and preview.startswith("previews/"):
                # 转换失败的预览文件不写库，避免教师点开空白。
                import_path = OUT / "stage/cases" / preview
                if not import_path.exists():
                    missing_previews.append(str(import_path))
                    preview = None
            extra = resource.get("extra") or {}
            lines.append(
                "INSERT INTO biz_textbook_case_resource "
                "(case_id,resource_key,category,title,filename,original_path,preview_path,"
                "extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)\n"
                "SELECT c.case_id,{key},{cat},{title},{filename},{original},{preview},"
                "{extra_name},{extra_path},{extra_bytes},{bytes},{description},{requirements},{sort}\n"
                "FROM biz_textbook_case c WHERE {case_filter}\n"
                "ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),"
                "original_path=VALUES(original_path),preview_path=VALUES(preview_path),"
                "extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),"
                "bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);".format(
                    key=q(resource["key"]), cat=q(resource["category"]), title=q(resource["title"]),
                    filename=q(resource["filename"]), original=q(resource["originalPath"]),
                    preview=q(preview), extra_name=q(extra.get("filename")),
                    extra_path=q(extra.get("originalPath")), extra_bytes=num(extra.get("bytes")),
                    bytes=num(resource.get("bytes")), description=q(resource.get("description")),
                    requirements=q(resource.get("requirements")), sort=index, case_filter=case_filter))
        lines.append("")

    for book in manifest["bookResources"]:
        lines.append(
            "INSERT INTO biz_textbook_book_resource "
            "(book_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)\n"
            "SELECT b.book_id,{key},{cat},{title},{filename},{original},{preview},{bytes},{description},NULL,10\n"
            "FROM biz_textbook_book b WHERE b.book_code={code}\n"
            "ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),"
            "original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),"
            "description=VALUES(description),sort_no=VALUES(sort_no);".format(
                key=q(book["key"]), cat=q(book["category"]), title=q(book["title"]),
                filename=q(book["filename"]), original=q(book["originalPath"]),
                preview=q(book.get("previewPath")), bytes=num(book.get("bytes")),
                description=q(book.get("description")), code=q(book["bookCode"])))
    lines.append("")

    usage = manifest.get("usage") or {}
    for guide in manifest["guides"]:
        grade = int(guide["grade"])
        note = "指南 PDF 在线阅读或下载；配套课件按课次编号排序。"
        if grade == 5:
            note += "第28课《认识决策树算法》来源资料包未提供课件，页面标注“暂未提供”。"
        lines.append(
            "INSERT INTO biz_guide_grade "
            "(grade,title,pdf_filename,pdf_path,pdf_bytes,pdf_sha256,usage_filename,usage_path,usage_bytes,note,sort_no,status)\n"
            "VALUES ({grade},{title},{pdf_name},{pdf_path},{pdf_bytes},{pdf_sha},{usage_name},{usage_path},{usage_bytes},{note},{sort},'0')\n"
            "ON DUPLICATE KEY UPDATE title=VALUES(title),pdf_filename=VALUES(pdf_filename),pdf_path=VALUES(pdf_path),"
            "pdf_bytes=VALUES(pdf_bytes),pdf_sha256=VALUES(pdf_sha256),usage_filename=VALUES(usage_filename),"
            "usage_path=VALUES(usage_path),usage_bytes=VALUES(usage_bytes),note=VALUES(note),sort_no=VALUES(sort_no),status='0';".format(
                grade=grade, title=q(guide["title"]), pdf_name=q(guide["pdf"]["filename"]),
                pdf_path=q(guide["pdf"]["originalPath"]), pdf_bytes=num(guide["pdf"]["bytes"]),
                pdf_sha=q(guide["pdf"].get("sha256")), usage_name=q(usage.get("filename")),
                usage_path=q(usage.get("originalPath")), usage_bytes=num(usage.get("bytes")),
                note=q(note), sort=grade))
        for lesson in sorted(guide["lessons"], key=lambda row: int(row["lessonNo"])):
            preview = lesson.get("previewPath")
            if preview:
                import_path = OUT / "stage/cases" / preview
                if not import_path.exists():
                    missing_previews.append(str(import_path))
                    preview = None
            lines.append(
                "INSERT INTO biz_guide_lesson "
                "(guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)\n"
                "SELECT g.guide_id,{no},{title},{filename},{original},{preview},{bytes},{sha},{available},{note},{no}\n"
                "FROM biz_guide_grade g WHERE g.grade={grade}\n"
                "ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),"
                "preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),"
                "available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);".format(
                    no=int(lesson["lessonNo"]), title=q(lesson["title"]), filename=q(lesson["filename"]),
                    original=q(lesson["originalPath"]), preview=q(preview), bytes=num(lesson["bytes"]),
                    sha=q(lesson.get("sha256")), available=int(lesson["available"]),
                    note=q(lesson.get("note")), grade=grade))
        lines.append("")

    lines.extend([
        "COMMIT;",
        "",
        "-- ------------------------- 导入后复核 -------------------------",
        "SELECT origin,COUNT(*) AS cases FROM biz_textbook_case WHERE status='PUBLISHED' GROUP BY origin;",
        "SELECT COUNT(*) AS book_resources FROM biz_textbook_book_resource;",
        "SELECT g.grade,COUNT(l.lesson_id) AS lessons,SUM(l.available=1) AS available,SUM(l.available=0) AS missing",
        "FROM biz_guide_grade g LEFT JOIN biz_guide_lesson l ON l.guide_id=g.guide_id GROUP BY g.grade ORDER BY g.grade;",
        "SELECT COUNT(*) AS duplicate_case_resources FROM (",
        "  SELECT case_id,resource_key FROM biz_textbook_case_resource GROUP BY case_id,resource_key HAVING COUNT(*)>1",
        ") t;",
        "",
    ])
    TARGET.write_text("\n".join(lines), encoding="utf-8")
    print("written", TARGET, "cases", len(manifest["cases"]),
          "resources", sum(len(c["resources"]) for c in manifest["cases"]),
          "guides", len(manifest["guides"]), "missing previews", len(missing_previews))
    if missing_previews:
        (OUT / "sql-missing-previews.json").write_text(
            json.dumps(missing_previews, ensure_ascii=False, indent=1), encoding="utf-8")


if __name__ == "__main__":
    main()
