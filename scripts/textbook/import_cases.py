"""优秀课例离线整理。scan 只读来源，stage 仅导入已审核清单并生成 SQL。"""
import argparse
import hashlib
import json
import shutil
from pathlib import Path

SKIP = {"__MACOSX", "node_modules", ".git", "__pycache__", ".vite"}


def digest(path):
    result = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            result.update(chunk)
    return result.hexdigest()


def scan(source, output):
    directories = []
    for grade in sorted(source.iterdir()):
        if not grade.is_dir():
            continue
        for child in sorted(grade.iterdir()):
            if not child.is_dir():
                continue
            directories.extend(sorted(child.iterdir()) if child.name.endswith("册") else [child])
    cases = []
    for directory in directories:
        if not directory.is_dir():
            continue
        files = []
        for path in sorted(directory.rglob("*")):
            if not path.is_file() or SKIP.intersection(path.relative_to(directory).parts):
                continue
            # 清单只录候选教学文件。工程整体在人工审核后登记为实验包。
            if path.suffix.lower() not in {".docx", ".doc", ".pdf", ".pptx", ".ppt", ".zip", ".rar", ".7z", ".mp4", ".txt", ".url"}:
                continue
            files.append({"source": path.relative_to(source).as_posix(), "bytes": path.stat().st_size,
                          "sha256": digest(path), "category": "", "title": "", "preview": ""})
        relative = directory.relative_to(source).as_posix()
        cases.append({"sourceKey": hashlib.sha256(relative.encode()).hexdigest()[:32], "sourceDirectory": relative,
                      "bookCode": "", "sectionKey": "", "title": directory.name, "author": "", "school": "",
                      "reviewed": False, "status": "DRAFT", "resources": files})
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps({"sourceRoot": str(source), "cases": cases}, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"已登记 {len(cases)} 个来源目录；全部默认待审核")


def sql_text(value):
    # 十六进制 UTF-8 常量避免 SQL mode 对引号/反斜线转义行为的差异。
    return "(CONVERT(0x" + str(value).encode("utf-8").hex() + " USING utf8mb4) COLLATE utf8mb4_general_ci)" if str(value) else "''"


def stage(manifest, destination, sql_output):
    data = json.loads(manifest.read_text(encoding="utf-8"))
    source = Path(data["sourceRoot"]).resolve(strict=True)
    destination = destination.resolve()
    if destination.is_relative_to(source):
        raise ValueError("发布目录不能位于原始素材目录内")
    statements = ["START TRANSACTION;"]
    count = 0
    for case in data["cases"]:
        if not case.get("reviewed"):
            continue
        if not case["bookCode"] or not case["sectionKey"] or not case["author"]:
            raise ValueError("已审核课例必须有教材课次和作者")
        if case["status"] not in {"DRAFT", "PUBLISHED"}:
            raise ValueError("课例状态无效")
        key = sql_text(case["sourceKey"])
        statements.append("INSERT INTO biz_textbook_case(source_key,book_id,section_key,title,author,school,status) "
                          f"SELECT {key},b.book_id,s.section_key,{sql_text(case['title'])},{sql_text(case['author'])},"
                          f"{sql_text(case['school'])},{sql_text(case['status'])} FROM biz_textbook_book b "
                          "JOIN biz_textbook_section s ON s.book_id=b.book_id "
                          f"WHERE b.book_code={sql_text(case['bookCode'])} AND s.section_key={sql_text(case['sectionKey'])} AND s.kind='LESSON' "
                          "ON DUPLICATE KEY UPDATE book_id=VALUES(book_id),section_key=VALUES(section_key),title=VALUES(title),author=VALUES(author),school=VALUES(school),status=VALUES(status);")
        statements.append(f"SET @case_id=(SELECT case_id FROM biz_textbook_case WHERE source_key={key});")
        resource_keys = []
        for index, resource in enumerate(case["resources"]):
            if not resource.get("category"):
                continue
            path = (source / resource["source"]).resolve(strict=True)
            if not path.is_relative_to(source):
                raise ValueError("来源文件越界")
            checksum = digest(path)
            filename = resource.get("filename", path.name)
            if not isinstance(filename, str) or not filename or len(filename) > 255 or any(character in filename for character in "\r\n\0/\\"):
                raise ValueError("下载文件名无效")
            resource_key = hashlib.sha256(resource["source"].encode()).hexdigest()
            resource_keys.append(sql_text(resource_key))
            if checksum != resource["sha256"]:
                raise ValueError("来源文件已变化，请重新审核")
            target = Path(checksum[:2]) / (checksum + path.suffix.lower())
            final = destination / target
            final.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(path, final)
            preview_sql = "NULL"
            sort_no = {"DESIGN": 0, "SLIDES": 1, "WORKSHEET": 2, "MATERIAL": 3, "TOOL": 4}.get(resource["category"], 5) * 1000 + index
            if resource.get("preview"):
                preview = Path(resource["preview"]).resolve(strict=True)
                if preview.suffix.lower() not in {".pdf", ".jpg", ".jpeg", ".png", ".webp", ".mp4", ".webm"}:
                    raise ValueError("预览件须为PDF或支持的图片/视频")
                preview_checksum = digest(preview)
                preview_target = target if preview_checksum == checksum and preview.suffix.lower() == path.suffix.lower() else Path("previews") / (preview_checksum + preview.suffix.lower())
                if preview_target != target:
                    (destination / preview_target).parent.mkdir(parents=True, exist_ok=True)
                    shutil.copyfile(preview, destination / preview_target)
                preview_sql = sql_text(preview_target.as_posix())
            statements.append("INSERT INTO biz_textbook_case_resource(case_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no) "
                              f"VALUES(@case_id,{sql_text(resource_key)},{sql_text(resource['category'])},{sql_text(resource['title'])},"
                              f"{sql_text(filename)},{sql_text(target.as_posix())},{preview_sql},{path.stat().st_size},"
                              f"{sql_text(resource.get('description',''))},{sql_text(resource.get('requirements',''))},{sort_no}) "
                              "ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),"
                              "original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),"
                              "description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);")
        retained = ','.join(resource_keys) or "''"
        statements.append(f"DELETE FROM biz_textbook_case_resource WHERE case_id=@case_id AND resource_key NOT IN ({retained});")
        count += 1
    statements.append("COMMIT;")
    sql_output.parent.mkdir(parents=True, exist_ok=True)
    sql_output.write_text("\n".join(statements) + "\n", encoding="utf-8")
    print(f"已生成 {count} 份审核课例的文件和 SQL，尚未执行数据库写入")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    sub = parser.add_subparsers(dest="action", required=True)
    scan_parser = sub.add_parser("scan")
    scan_parser.add_argument("source", type=Path)
    scan_parser.add_argument("output", type=Path)
    stage_parser = sub.add_parser("stage")
    stage_parser.add_argument("manifest", type=Path)
    stage_parser.add_argument("destination", type=Path)
    stage_parser.add_argument("sql_output", type=Path)
    args = parser.parse_args()
    if args.action == "scan":
        scan(args.source, args.output)
    else:
        stage(args.manifest, args.destination, args.sql_output)
