"""2025宁波优秀课例批次：整理、预览转换、合包与增量 SQL 生成（离线，不写数据库）。

阶段：
  plan   规则分类，输出 resource-decisions.json / video-decisions.json，不复制大文件
  stage  按分类复制资源与预览、生成配套材料包，输出 catalog-reviewed-2025.json
  sql    基于已生成清单输出幂等增量 SQL（无 DELETE，不触碰旧批次）
  verify 校验发布副本的大小/SHA 与清单一致

来源目录只读；派生文件写入 output/textbook-cases-2025/stage。
"""
import argparse
import hashlib
import json
import re
import shutil
import subprocess
import sys
import tempfile
import zipfile
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases-2025"
SOURCE_ROOT = Path(r"E:\2025年宁波市义务段“新课程背景下优秀课堂教学案例”（信息科技）")
STAGE = OUT / "stage"
WORK = OUT / "work/extracted"
SOFFICE = r"C:/Program Files/LibreOffice/program/soffice.exe"
CATEGORY_ORDER = {"DESIGN": 0, "SLIDES": 1, "WORKSHEET": 2, "MATERIAL": 3, "TOOL": 4}
VIDEO_LIMIT = 100_000_000          # 本批统一 100MB＝100,000,000 字节
PREVIEW_LIMIT = 100 * 1024 * 1024  # 平台单份预览 PDF 上限（既有 100MiB）
TOOL_LIMIT = 50 * 1024 * 1024      # 单个 Windows 小程序分发上限
DOC_SUFFIX = {".doc", ".docx", ".pdf", ".ppt", ".pptx", ".ppsx", ".pptm", ".wps", ".xlsx", ".enbx"}
# 浏览器不能直接播放的容器：只提供下载，不冒充可在线预览。
DOWNLOAD_ONLY_VIDEO = {".wmv", ".mov", ".avi", ".mkv", ".mpg", ".mpeg"}
CONVERTIBLE = {".doc", ".docx", ".ppt", ".pptx", ".ppsx", ".pptm", ".wps", ".xlsx"}
VIDEO_SUFFIX = {".mp4", ".wmv", ".mov", ".mpg", ".mpeg", ".avi", ".mkv"}
HTML_SUFFIX = {".html", ".htm"}
SKIP_DIRS = {"__pycache__", "node_modules", ".git", ".vite", ".venv", "venv", "env", "_shots"}
# 硬排除：既不作为资源，也不进配套包。
HARD_EXCLUDE = [
    (re.compile(r"课堂实录|课堂录像|教学录像|教学视频|录课|授课录像"), "EXCLUDED_CLASS_RECORDING"),
    (re.compile(r"学生作业|学生学习单收集|学生画图|学生作品|学生代表性"), "EXCLUDED_STUDENT_WORK"),
    (re.compile(r"账号密码|密码\.txt|密码表|班级码|名单|花名册"), "EXCLUDED_PERSONAL_DATA"),
    (re.compile(r"案例征集目录表|优秀课堂教学案例征集"), "EXCLUDED_ADMIN_FORM"),
    (re.compile(r"^~\$|/~\$"), "EXCLUDED_TEMP_FILE"),
]


def digest(path):
    result = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            result.update(chunk)
    return result.hexdigest()


def sql_text(value):
    if not str(value):
        return "''"
    return "(CONVERT(0x" + str(value).encode("utf-8").hex() + " USING utf8mb4) COLLATE utf8mb4_general_ci)"


def load_inventory():
    return json.loads((OUT / "source-inventory.json").read_text(encoding="utf-8"))


def load_archives():
    return json.loads((OUT / "archive-extract.json").read_text(encoding="utf-8"))


def case_files(case, inventory, archives):
    name = case["source"]
    records = [item for item in inventory["files"] if item["caseDirectory"] == name]
    # 册目录下直接存放的压缩包，其本身也会带同名 caseDirectory；这种情况要读解包结果。
    if records and all(item.get("archiveEntry") for item in records):
        records = []
    if records:
        result = []
        for item in records:
            path = SOURCE_ROOT / item["relativePath"]
            relative = (item["relativePath"][len(name) + 1:]
                        if item["relativePath"].startswith(name + "/") else path.name)
            result.append((path, relative, item["bytes"], False))
        return result
    record = next((item for item in archives if item["caseDirectory"] == name), None)
    if record is None:
        raise RuntimeError("找不到课例来源：" + name)
    base = WORK / Path(record["workDirectory"]).name
    return [(base / member["name"], member["name"], member["bytes"], True) for member in record["members"]]


def hard_exclude(case, relative, size):
    for pattern, reason in HARD_EXCLUDE:
        if pattern.search(relative):
            return reason
    parts = Path(relative).parts
    for part in parts:
        if part in SKIP_DIRS:
            return "EXCLUDED_RUNTIME_DIR"
    for skip in case.get("skipDirs", []):
        if skip in parts:
            return "EXCLUDED_LARGE_TOOL"
    suffix = Path(relative).suffix.lower()
    if suffix == ".url":
        return "EXCLUDED_EXTERNAL_LINK"
    if suffix == ".exe" and size > TOOL_LIMIT:
        return "EXCLUDED_LARGE_TOOL"
    if suffix in {".exe", ".dll", ".zip", ".rar", ".7z"} and size > 300 * 1024 * 1024:
        return "EXCLUDED_TOO_LARGE"
    if suffix in VIDEO_SUFFIX:
        decision = case.get("videos", {}).get(relative)
        if decision is None:
            return "PENDING_VIDEO"
        if not decision.get("include"):
            return decision.get("reason") or "EXCLUDED_VIDEO"
        if size > VIDEO_LIMIT:
            return "EXCLUDED_VIDEO_OVERSIZE"
    return ""


def category_for(relative, suffix):
    # 只有文档类后缀才可能成为独立资源；.py/.jpg 即便名字含“学习单”也只进配套包。
    if suffix not in DOC_SUFFIX:
        return ""
    name = Path(relative).name
    if suffix in {".pptx", ".ppt", ".ppsx", ".pptm", ".enbx"}:
        return "SLIDES"
    if re.search(r"学习单|任务单|导学单|导学案|学案|学历案|实验单|活动单|活动记录单|作业|评价表|评价单|检测|练习|题组|记录单|提示单|学习指南|学习手册|课堂测评", name):
        return "WORKSHEET"
    if re.search(r"教学设计|教案|教学案例|说课", name):
        return "DESIGN"
    return "MATERIAL"


def resource_title(category, relative):
    stem = Path(relative).stem.strip()
    if category == "DESIGN":
        return "教学设计"
    if category == "SLIDES":
        return "教学课件"
    return stem[:120]


def convert_preview(path, cache):
    key = hashlib.sha256(str(path).encode("utf-8")).hexdigest()[:24]
    destination = cache / (key + ".pdf")
    if destination.exists() and destination.stat().st_size > 0:
        return destination, ""
    with tempfile.TemporaryDirectory(prefix="nb2025-office-") as profile:
        try:
            completed = subprocess.run([SOFFICE, "-env:UserInstallation=" + Path(profile).as_uri(),
                                        "--headless", "--convert-to", "pdf", "--outdir", str(cache), str(path)],
                                       stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=420)
        except subprocess.TimeoutExpired:
            return None, "转换超时"
    produced = cache / (path.stem + ".pdf")
    if completed.returncode or not produced.exists():
        return None, "转换失败 exit=%s" % completed.returncode
    if produced != destination:
        produced.replace(destination)
    if destination.stat().st_size == 0:
        return None, "空PDF"
    return destination, ""


def compress_preview(path):
    if path.stat().st_size <= PREVIEW_LIMIT:
        return False
    sys.path.insert(0, str(ROOT / "scripts/textbook"))
    from compress_case_previews import rasterize
    rasterize(path, path, 1920, 85)
    return True


def summarize_package(files):
    counts = Counter()
    for relative in files:
        suffix = Path(relative).suffix.lower()
        if suffix in {".html", ".htm"}:
            counts["网页"] += 1
        elif suffix in {".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg"}:
            counts["图片"] += 1
        elif suffix in {".xlsx", ".csv", ".txt"}:
            counts["数据/文本"] += 1
        elif suffix in {".py", ".js", ".sb3", ".drawio"}:
            counts["程序/工程"] += 1
        elif suffix in {".mp4", ".wmv", ".mov"}:
            counts["视频"] += 1
        elif suffix in {".docx", ".doc", ".pdf", ".pptx", ".ppt"}:
            counts["文档"] += 1
        elif suffix in {".exe", ".dll"}:
            counts["Windows 工具"] += 1
        else:
            counts["其他文件"] += 1
    return "、".join("%s %d 个" % (name, count) for name, count in counts.most_common() if count)


def requirement_text(files):
    text = ["解压后按包内「使用说明_平台版.txt」打开"]
    suffixes = {Path(name).suffix.lower() for name in files}
    if suffixes & {".html", ".htm"}:
        text.append("网页子工具需解压后用浏览器打开对应入口")
    if ".py" in suffixes:
        text.append("Python 子工具需本机安装 Python 3.10+")
    if ".exe" in suffixes or ".dll" in suffixes:
        text.append("含 Windows 可执行工具，需在 Windows 电脑运行")
    if ".sb3" in suffixes:
        text.append("Scratch 作品需用 Scratch 或编程平台打开")
    return "；".join(text)


def build_package(case, files, destination, notes):
    total = sum(item[2] for item in files)
    if not files or total < 100 * 1024:
        return None
    if total > 250 * 1024 * 1024:
        notes.append({"case": case["source"], "reason": "素材超过单包上限，已剔除 20MB 以上文件", "bytes": total})
        files = [item for item in files if item[2] < 20 * 1024 * 1024]
        total = sum(item[2] for item in files)
        if total > 250 * 1024 * 1024:
            notes.append({"case": case["source"], "reason": "剔除后仍超限，未生成配套包", "bytes": total})
            return None
    destination.mkdir(parents=True, exist_ok=True)
    archive = destination / (hashlib.sha256(case["source"].encode("utf-8")).hexdigest()[:16] + ".zip")
    description = case.get("packageDescription") or ("本课配套材料包，含" + summarize_package([item[1] for item in files]) + "。")
    requirements = case.get("packageRequirements") or requirement_text([item[1] for item in files])
    guide = ("%s\n作者：%s\n\n内容：%s\n使用条件：%s\n\n说明：本包按作者原始目录结构整理，"
             "未收录课堂实录、学生个人作品与超过 100MB 的视频；原件仍在作者手中。\n"
             % (case["title"], case["author"], description, requirements))
    with zipfile.ZipFile(archive, "w", zipfile.ZIP_DEFLATED, compresslevel=6) as package:
        for path, relative, size, _ in sorted(files, key=lambda item: item[1]):
            package.write(path, relative)
        package.writestr("使用说明_平台版.txt", guide.encode("utf-8"))
    return {"path": archive, "bytes": archive.stat().st_size, "sha256": digest(archive),
            "description": description, "requirements": requirements, "fileCount": len(files),
            "sourceBytes": total}


def pending_video_decisions(cases):
    lines = []
    for case in cases:
        if case.get("status") == "PUBLISHED":
            continue
        for relative, decision in case.get("videos", {}).items():
            lines.append({"case": case["source"], "path": relative,
                          "decision": "PENDING_CASE" if decision.get("include") else decision.get("reason"),
                          "include": False, "note": "课例未上架，视频不发布"})
    return lines


def run_plan(cases, inventory, archives, convert=True, build_packages=True,
             collectors=None, write_outputs=True):
    if collectors is None:
        resource_decisions, video_decisions, html_decisions = [], [], []
    else:
        resource_decisions = collectors.setdefault("resource", [])
        video_decisions = collectors.setdefault("video", [])
        html_decisions = collectors.setdefault("html", [])
    catalog = {"sourceRoot": str(SOURCE_ROOT), "cases": []}
    package_notes = []
    preview_cache = OUT / "work/previews"
    preview_cache.mkdir(parents=True, exist_ok=True)
    for case in cases:
        status = case.get("status", "PUBLISHED")
        if status != "PUBLISHED":
            catalog["cases"].append({key: case.get(key) for key in ("source", "bookCode", "sectionKey", "title",
                                                                   "author", "school", "status", "note")})
            continue
        files = case_files(case, inventory, archives)
        resources, remaining, seen = [], [], {}
        for path, relative, size, from_archive in sorted(files, key=lambda item: item[1]):
            if not path.is_file():
                resource_decisions.append({"case": case["source"], "path": relative,
                                           "decision": "MISSING", "reason": "解包后文件缺失"})
                continue
            suffix = path.suffix.lower()
            # 网页工程内嵌的视频不单列资源，随配套包整体提供。
            if suffix in VIDEO_SUFFIX and relative in case.get("packageVideos", []):
                remaining.append((path, relative, size, from_archive))
                resource_decisions.append({"case": case["source"], "path": relative, "bytes": size,
                                           "decision": "PACKAGE_MEMBER", "reason": "网页工程内嵌视频"})
                continue
            reason = hard_exclude(case, relative, size)
            if reason:
                if suffix in VIDEO_SUFFIX:
                    decision = case.get("videos", {}).get(relative, {})
                    video_decisions.append({"case": case["source"], "path": relative, "bytes": size,
                                            "decision": decision.get("reason") or reason,
                                            "include": False,
                                            "evidence": decision.get("evidence", "规则排除")})
                resource_decisions.append({"case": case["source"], "path": relative, "bytes": size,
                                           "decision": reason})
                continue
            if suffix in VIDEO_SUFFIX:
                checksum = digest(path)
                if checksum in seen:
                    resource_decisions.append({"case": case["source"], "path": relative, "bytes": size,
                                               "decision": "DUPLICATE_FILE", "reason": "与 " + seen[checksum] + " 相同"})
                    video_decisions.append({"case": case["source"], "path": relative, "bytes": size,
                                            "decision": "DUPLICATE_FILE", "include": False,
                                            "evidence": "与 " + seen[checksum] + " 内容相同"})
                    continue
                seen[checksum] = relative
                download_only = suffix in DOWNLOAD_ONLY_VIDEO
                video_decisions.append({"case": case["source"], "path": relative, "bytes": size,
                                        "decision": "INCLUDE_DOWNLOAD_ONLY" if download_only else "INCLUDE",
                                        "include": True,
                                        "evidence": case["videos"].get(relative, {}).get("evidence", ""),
                                        "note": "浏览器不支持该容器，仅提供下载，已列入转码待办" if download_only else ""})
                resources.append({"source": relative, "category": "MATERIAL",
                                  "title": Path(relative).stem[:120], "filename": Path(relative).name,
                                  "kind": "video", "bytes": size, "sha256": checksum, "absolute": str(path),
                                  "preview": "" if download_only else str(path),
                                  "description": "WMV 格式，浏览器不能直接在线播放，请下载后用本地播放器观看。" if download_only else ""})
                continue
            if suffix in HTML_SUFFIX and relative in case.get("tools", {}):
                payload = case["tools"][relative]
                checksum = digest(path)
                resources.append({"source": relative, "category": "TOOL",
                                  "title": payload.get("title", Path(relative).stem), "filename": Path(relative).name,
                                  "kind": "html", "bytes": size, "sha256": checksum, "absolute": str(path),
                                  "description": payload.get("description", ""),
                                  "requirements": payload.get("requirements", "")})
                html_decisions.append({"case": case["source"], "path": relative, "bytes": size,
                                       "category": "HTML_STANDALONE", "decision": "PREVIEW",
                                       "sandbox": "allow-scripts"})
                continue
            category = category_for(relative, suffix)
            if category:
                checksum = digest(path)
                if checksum in seen:
                    resource_decisions.append({"case": case["source"], "path": relative, "bytes": size,
                                               "decision": "DUPLICATE_FILE", "reason": "与 " + seen[checksum] + " 相同"})
                    continue
                seen[checksum] = relative
                resources.append({"source": relative, "category": category,
                                  "title": resource_title(category, relative), "filename": Path(relative).name,
                                  "kind": "document", "bytes": size, "sha256": checksum, "absolute": str(path)})
                resource_decisions.append({"case": case["source"], "path": relative, "bytes": size,
                                           "decision": "RESOURCE", "category": category})
                continue
            remaining.append((path, relative, size, from_archive))
        for resource in resources:
            if resource["kind"] != "document":
                continue
            if not convert:
                resource["preview"] = "pending"
                continue
            source_path = next(item[0] for item in files if item[1] == resource["source"])
            suffix = source_path.suffix.lower()
            if suffix not in CONVERTIBLE | {".pdf"}:
                resource["previewFailed"] = "格式不支持在线预览"
                continue
            preview, error = convert_preview(source_path, preview_cache)
            if error:
                resource["previewFailed"] = error
                continue
            resource["previewCompressed"] = compress_preview(preview)
            resource["preview"] = str(preview)
        package = None
        if not build_packages and remaining:
            for item in remaining:
                resource_decisions.append({"case": case["source"], "path": item[1], "bytes": item[2],
                                           "decision": "PACKAGE_PENDING"})
            catalog["cases"].append({key: case.get(key) for key in ("source", "bookCode", "sectionKey", "title",
                                                                    "author", "school", "note", "sourceKey")}
                                    | {"resources": resources, "package": True})
            continue
        if remaining and case.get("package", True) is not False:
            package = build_package(case, remaining, STAGE / "packages", package_notes)
            if package:
                resources.append({"source": "", "category": "TOOL", "title": "配套材料包",
                                  "filename": case.get("packageName", case["shortTitle"] + "配套材料包.zip"),
                                  "kind": "package", "bytes": package["bytes"], "sha256": package["sha256"],
                                  "description": package["description"], "requirements": package["requirements"],
                                  "path": str(package["path"])})
                for item in remaining:
                    resource_decisions.append({"case": case["source"], "path": item[1], "bytes": item[2],
                                               "decision": "PACKAGE_MEMBER"})
        if not package:
            for item in remaining:
                resource_decisions.append({"case": case["source"], "path": item[1], "bytes": item[2],
                                           "decision": "EXCLUDED_NOT_PACKAGED",
                                           "reason": "非教学材料或未打包"})
        catalog["cases"].append({key: case.get(key) for key in ("source", "bookCode", "sectionKey", "title",
                                                                "author", "school", "note", "sourceKey")}
                                | {"resources": resources, "package": bool(package)})
    video_decisions.extend(pending_video_decisions(cases))
    if write_outputs:
        (OUT / "resource-decisions.json").write_text(json.dumps(resource_decisions, ensure_ascii=False, indent=2),
                                                     encoding="utf-8")
        (OUT / "video-decisions.json").write_text(json.dumps(video_decisions, ensure_ascii=False, indent=2),
                                                  encoding="utf-8")
        (OUT / "html-decisions.json").write_text(json.dumps(html_decisions, ensure_ascii=False, indent=2),
                                                 encoding="utf-8")
        (OUT / "package-notes.json").write_text(json.dumps(package_notes, ensure_ascii=False, indent=2),
                                                encoding="utf-8")
        (OUT / "catalog-draft-2025.json").write_text(json.dumps(catalog, ensure_ascii=False, indent=2),
                                                     encoding="utf-8")
    published = [case for case in catalog["cases"] if case.get("status", "PUBLISHED") == "PUBLISHED"]
    print("课例 %d（上架 %d），资源 %d" % (len(catalog["cases"]), len(published),
                                          sum(len(case.get("resources", [])) for case in published)))
    return catalog


def stage_catalog(catalog, write=True):
    """把清单里的资源与预览复制进发布目录，路径与 sha 一一对应。"""
    files_root = STAGE / "files"
    preview_root = STAGE / "previews"
    manifest, problems, copied = [], [], 0
    for case in catalog["cases"]:
        if case.get("status", "PUBLISHED") != "PUBLISHED":
            continue
        for resource in case["resources"]:
            if resource["kind"] == "package":
                package_path = Path(resource["path"])
                if not package_path.is_file():
                    problems.append({"case": case["source"], "reason": "配套包不存在"})
                    continue
                # 数据库路径相对于 cases 根目录，与旧批次保持一致。
                resource["original_path"] = "packages/" + package_path.name
                resource["stage_path"] = "packages/" + package_path.name
                manifest.append({"case": case["source"], "path": resource["stage_path"],
                                 "bytes": package_path.stat().st_size, "sha256": resource["sha256"]})
                continue
            source = Path(resource["absolute"])
            if not source.is_file():
                problems.append({"case": case["source"], "source": resource["source"], "reason": "来源缺失"})
                continue
            checksum = resource["sha256"]
            target = files_root / checksum[:2] / (checksum + source.suffix.lower())
            target.parent.mkdir(parents=True, exist_ok=True)
            if not target.exists():
                shutil.copyfile(source, target)
                copied += 1
            resource["original_path"] = target.relative_to(files_root).as_posix()
            resource["stage_path"] = target.relative_to(STAGE).as_posix()
            manifest.append({"case": case["source"], "path": resource["stage_path"],
                             "bytes": target.stat().st_size, "sha256": checksum})
            # 视频预览就是原件本身，不能另存成 PDF 预览（避免把视频字节冒充预览件）。
            if resource["kind"] == "video":
                resource["preview_path"] = resource["original_path"] if resource.get("preview") else None
                continue
            preview = resource.get("preview")
            if preview and preview != "pending" and Path(preview).is_file():
                preview_checksum = digest(Path(preview))
                preview_target = preview_root / (preview_checksum + ".pdf")
                preview_target.parent.mkdir(parents=True, exist_ok=True)
                if not preview_target.exists():
                    shutil.copyfile(preview, preview_target)
                resource["preview_path"] = "previews/" + preview_target.name
                resource["stage_preview"] = preview_target.relative_to(STAGE).as_posix()
                manifest.append({"case": case["source"], "path": resource["stage_preview"],
                                 "bytes": preview_target.stat().st_size, "sha256": preview_checksum})
            else:
                resource["preview_path"] = None
    if write:
        (OUT / "catalog-reviewed-2025.json").write_text(json.dumps(catalog, ensure_ascii=False, indent=2),
                                                        encoding="utf-8")
        (OUT / "files-manifest.json").write_text(json.dumps({"files": manifest, "problems": problems},
                                                            ensure_ascii=False, indent=2), encoding="utf-8")
    print("复制 %d 个新文件，清单 %d 项，问题 %d 项" % (copied, len(manifest), len(problems)))
    return catalog


def write_sql(catalog, output):
    statements = ["-- 2025 宁波优秀课例批次：仅本批 source_key 的幂等增量导入。",
                  "-- 不含 DELETE，不触碰旧批次课例与资源；执行前请先做整库备份。",
                  "START TRANSACTION;"]
    cases = [case for case in catalog["cases"] if case.get("status", "PUBLISHED") == "PUBLISHED"]
    for case in cases:
        key = sql_text(case["sourceKey"])
        statements.append(
            "INSERT INTO biz_textbook_case(source_key,book_id,section_key,title,author,school,status) "
            "SELECT %s,b.book_id,s.section_key,%s,%s,%s,'PUBLISHED' FROM biz_textbook_book b "
            "JOIN biz_textbook_section s ON s.book_id=b.book_id "
            "WHERE b.book_code=%s AND s.section_key=%s AND s.kind='LESSON' "
            "ON DUPLICATE KEY UPDATE book_id=VALUES(book_id),section_key=VALUES(section_key),"
            "title=VALUES(title),author=VALUES(author),school=VALUES(school),status=VALUES(status);"
            % (key, sql_text(case["title"]), sql_text(case["author"]), sql_text(case["school"]),
               sql_text(case["bookCode"]), sql_text(case["sectionKey"])))
        order = 0
        for index, resource in enumerate(case["resources"]):
            resource_key = sql_text(hashlib.sha256(
                ("ningbo-2025:%s::%s" % (case["source"], resource["source"] or "package"))
                .encode("utf-8")).hexdigest())
            preview = sql_text(resource["preview_path"]) if resource.get("preview_path") else "NULL"
            sort_no = CATEGORY_ORDER.get(resource["category"], 5) * 1000 + index
            statements.append(
                "INSERT INTO biz_textbook_case_resource(case_id,resource_key,category,title,filename,original_path,"
                "preview_path,bytes,description,requirements,sort_no) "
                "SELECT case_id,%s,%s,%s,%s,%s,%s,%d,%s,%s,%d FROM biz_textbook_case WHERE source_key=%s "
                "ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),"
                "original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),"
                "description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);"
                % (resource_key, sql_text(resource["category"]), sql_text(resource["title"]),
                   sql_text(resource["filename"]), sql_text(resource["original_path"]), preview,
                   resource["bytes"], sql_text(resource.get("description", "")),
                   sql_text(resource.get("requirements", "")), sort_no, key))
            order += 1
    statements.append("COMMIT;")
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text("\n".join(statements) + "\n", encoding="utf-8")
    print("已生成 SQL：%d 课例，%d 资源" % (len(cases), sum(len(case["resources"]) for case in cases)))


def verify_catalog(catalog):
    problems = []
    published = [case for case in catalog["cases"] if case.get("status", "PUBLISHED") == "PUBLISHED"]
    for case in published:
        if not case.get("resources"):
            problems.append({"case": case["source"], "reason": "没有资源"})
        for resource in case["resources"]:
            relative = resource.get("stage_path")
            if not relative:
                problems.append({"case": case["source"], "source": resource["source"], "reason": "未登记发布路径"})
                continue
            path = STAGE / relative
            if not path.is_file():
                problems.append({"case": case["source"], "path": relative, "reason": "发布文件缺失"})
                continue
            if path.stat().st_size != resource["bytes"]:
                problems.append({"case": case["source"], "path": relative, "reason": "字节数不符"})
            if digest(path) != resource["sha256"]:
                problems.append({"case": case["source"], "path": relative, "reason": "SHA256 不符"})
            preview = resource.get("stage_preview")
            if preview and not (STAGE / preview).is_file():
                problems.append({"case": case["source"], "path": preview, "reason": "预览文件缺失"})
            if resource["kind"] == "video" and resource["bytes"] > VIDEO_LIMIT:
                problems.append({"case": case["source"], "path": relative, "reason": "视频超过 100MB"})
    (OUT / "verify-result.json").write_text(json.dumps({"problems": problems, "caseCount": len(published)},
                                                       ensure_ascii=False, indent=2), encoding="utf-8")
    print("校验完成：课例 %d，问题 %d" % (len(published), len(problems)))
    return problems


def assemble():
    """合并分批暂存结果，生成最终清单与决定文件。"""
    batches = sorted((OUT / "work/staged").glob("batch-*.json"), key=lambda path: int(path.stem.split("-")[1]))
    if not batches:
        raise SystemExit("没有分批结果，先执行 stage --offset/--limit")
    cases, resource_decisions, video_decisions, html_decisions = [], [], [], []
    for path in batches:
        data = json.loads(path.read_text(encoding="utf-8"))
        cases.extend(data["cases"])
        resource_decisions.extend(data["resource"])
        video_decisions.extend(data["video"])
        html_decisions.extend(data["html"])
    catalog = {"sourceRoot": str(SOURCE_ROOT), "cases": cases}
    manifest = []
    for case in cases:
        if case.get("status", "PUBLISHED") != "PUBLISHED":
            continue
        for resource in case["resources"]:
            for key in ("stage_path", "stage_preview"):
                relative = resource.get(key)
                if not relative:
                    continue
                path = STAGE / relative
                if path.is_file():
                    manifest.append({"case": case["source"], "path": relative,
                                     "bytes": path.stat().st_size, "sha256": digest(path)})
    (OUT / "catalog-reviewed-2025.json").write_text(json.dumps(catalog, ensure_ascii=False, indent=2),
                                                    encoding="utf-8")
    (OUT / "resource-decisions.json").write_text(json.dumps(resource_decisions, ensure_ascii=False, indent=2),
                                                 encoding="utf-8")
    (OUT / "video-decisions.json").write_text(json.dumps(video_decisions, ensure_ascii=False, indent=2),
                                              encoding="utf-8")
    (OUT / "html-decisions.json").write_text(json.dumps(html_decisions, ensure_ascii=False, indent=2),
                                             encoding="utf-8")
    (OUT / "files-manifest.json").write_text(json.dumps({"files": manifest}, ensure_ascii=False, indent=2),
                                             encoding="utf-8")
    print("合并 %d 批：课例 %d，文件 %d" % (len(batches), len(cases), len(manifest)))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("action", choices=["plan", "stage", "assemble", "sql", "verify"])
    parser.add_argument("--offset", type=int, default=0)
    parser.add_argument("--limit", type=int, default=0)
    parser.add_argument("--sql-output", type=Path, default=ROOT / "sql/textbook_cases_ningbo2025_v1.sql")
    args = parser.parse_args()
    sys.path.insert(0, str(ROOT / "scripts/textbook"))
    from ningbo2025_cases import CASES
    if args.action == "plan":
        run_plan(CASES, load_inventory(), load_archives(), convert=False, build_packages=False)
        return
    if args.action == "assemble":
        assemble()
        return
    if args.action == "stage":
        subset = CASES[args.offset: args.offset + args.limit] if args.limit else CASES[args.offset:]
        collectors = {"resource": [], "video": [], "html": []}
        catalog = run_plan(subset, load_inventory(), load_archives(), convert=True,
                           collectors=collectors, write_outputs=False)
        stage_catalog(catalog, write=False)
        target = OUT / "work/staged" / ("batch-%04d.json" % args.offset)
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(json.dumps({"offset": args.offset, "cases": catalog["cases"],
                                      **collectors}, ensure_ascii=False, indent=2), encoding="utf-8")
        print("批次 %d-%d 完成" % (args.offset, args.offset + len(subset)))
        return
    catalog = json.loads((OUT / "catalog-reviewed-2025.json").read_text(encoding="utf-8"))
    if args.action == "sql":
        write_sql(catalog, args.sql_output)
    else:
        verify_catalog(catalog)


if __name__ == "__main__":
    main()
