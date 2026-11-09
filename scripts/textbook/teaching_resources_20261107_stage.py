"""2026 宁波版教学设计 + 课程指南资源暂存（可重复执行）。

用法：
  python -X utf8 scripts/textbook/teaching_resources_20261107_stage.py plan
  python -X utf8 scripts/textbook/teaching_resources_20261107_stage.py convert

plan    ：生成 resource-manifest.json，复制原件、生成配套材料包（不转换预览）。
convert ：用 LibreOffice 批量把 Word/PPT/Excel 转成 PDF 预览（断点续跑）。

来源目录只读；所有产物写到 output/textbook-resources-20261107/stage。
"""
import argparse
import hashlib
import json
import re
import shutil
import subprocess
import sys
import zipfile
from collections import Counter, defaultdict
from datetime import datetime
from pathlib import Path

import fitz
from resource_package_hygiene import clean_zip

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-resources-20261107"
STAGE = OUT / "stage"
CASES = STAGE / "cases"
PREVIEWS = CASES / "previews"
WORK = OUT / "work"
SOFFICE = r"C:/Program Files/LibreOffice/program/soffice.exe"
VIDEO_LIMIT = 100_000_000
DOC_PREVIEW_SUFFIX = {".doc", ".docx", ".ppt", ".pptx", ".ppsx", ".pptm", ".xls", ".xlsx", ".wps"}
PACKAGE_KINDS = {"IMAGE", "HTML", "CODE", "OTHER", "ARCHIVE", "MATERIAL", "AUDIO"}
STANDALONE_KINDS = {"DESIGN", "SLIDES", "WORKSHEET", "VIDEO"}

BOOK_PDF = {
    "xxkj-3-0-2026": "2026义务教育信息科技三年级上册教学设计.pdf",
    "xxkj-3-1-2026": "2026义务教育信息科技三年级下册教学设计.pdf",
    "xxkj-4-0-2026": "2026义务教育信息科技四年级上册教学设计.pdf",
    "xxkj-4-1-2026": "2026义务教育信息科技四年级下册教学设计.pdf",
    "xxkj-5-0-2026": "2026义务教育信息科技五年级上册教学设计.pdf",
    "xxkj-5-1-2026": "2026义务教育信息科技五年级下册教学设计.pdf",
    "xxkj-6-0-2026": "2026义务教育信息科技六年级上册教学设计.pdf",
    "xxkj-6-1-2026": "2026义务教育信息科技六年级下册教学设计.pdf",
    "xxkj-7-0-2026": "2026义务教育信息科技七年级上册教学设计.pdf",
    "xxkj-7-1-2026": "2026义务教育信息科技七年级下册教学设计.pdf",
    "xxkj-8-0-2026": "2026义务教育信息科技八年级上册教学设计.pdf",
    "xxkj-8-1-2026": "2026义务教育信息科技八年级下册教学设计.pdf",
    "xxkj-9-9-2026": "2026义务教育信息科技九年级教学设计.pdf",
}

GUIDE_GRADES = [
    (3, "3 在线学习与生活 三年级全一册"),
    (4, "4 数据与编码 四年级全一册"),
    (5, "5 身边的算法 五年级全一册"),
    (6, "6 过程与控制 六年级全一册"),
    (7, "7 互联网应用与创新 七年级全一册"),
    (8, "8 物联网实践与探索 八年级全一册"),
]
GUIDE_MISSING = {5: {28: "认识决策树算法"}}


def sha256_file(path):
    digest = hashlib.sha256()
    with open(path, "rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def json_read(path, default=None):
    if not path.exists():
        return default
    return json.loads(path.read_text(encoding="utf-8"))


def json_write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=1), encoding="utf-8")


def safe_name(text, limit=100):
    text = re.sub(r'[\\/:*?"<>|\r\n\t]+', " ", text or "")
    text = re.sub(r"\s+", " ", text).strip(" .")
    return text[:limit] or "配套材料"


def stage_original(source, sha, suffix):
    """按内容哈希落盘，已存在且大小一致就跳过，支持断点续跑。"""
    target = CASES / sha[:2] / (sha + suffix.lower())
    if target.exists() and target.stat().st_size == source.stat().st_size:
        return target
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(source, target)
    if target.stat().st_size != source.stat().st_size:
        raise RuntimeError("复制后大小不一致：" + str(source))
    return target


def normalize_design(name):
    stem = Path(name).stem.lower()
    stem = re.sub(r"[（(].*?[）)]", "", stem)
    stem = re.sub(r"[\s_\-—–·.、，,]+", "", stem)
    stem = re.sub(r"(教学设计|教学课例|定稿|修改稿|修改|最终版|最终|终稿|v\d+|版本\d*)+$", "", stem)
    return stem


def is_standalone_html(path):
    """只把无本地/远程引用的单文件网页放进沙箱预览，其余整包下载。"""
    try:
        if path.stat().st_size > 16 * 1024 * 1024:
            return False
        text = path.read_text(encoding="utf-8", errors="ignore")
    except OSError:
        return False
    text = re.sub(r"<!--.*?-->", " ", text, flags=re.S)
    patterns = [
        r"""<script[^>]+src\s*=""",
        r"""<link[^>]+href\s*=""",
        r"""<img[^>]+src\s*=""",
        r"""<iframe[^>]+src\s*=""",
        r"""<audio[^>]+src\s*=""",
        r"""<video[^>]+src\s*=""",
        r"""url\(\s*['"]?[^)'"]+""",
    ]
    for pattern in patterns:
        for match in re.finditer(pattern, text, flags=re.I):
            fragment = text[match.start():match.start() + 300]
            value = re.search(r"""=\s*['"]([^'"]+)""", fragment)
            target = value.group(1).strip() if value else ""
            if not target or target.startswith("#") or target.startswith("data:") or target.startswith("javascript:"):
                continue
            return False
    if re.search(r"""https?://[^\s"'<>]+""", text, flags=re.I):
        return False
    return True


def package_requirements(files):
    names = {Path(item["casePath"]).name.lower() for item in files}
    paths = " ".join(item["casePath"].lower() for item in files)
    hints = []
    if "package.json" in names:
        hints.append("需 Node.js 环境：解压后在项目目录执行 npm install，再按 README 或 package.json 启动。")
    if "requirements.txt" in names:
        hints.append("需 Python 3 环境：解压后安装 requirements.txt 中的依赖再运行。")
    if any(name.endswith((".html", ".htm")) for name in names):
        hints.append("解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。")
    if any(name.endswith((".exe", ".msi", ".bat")) for name in names):
        hints.append("含 Windows 程序或脚本，解压后按包内说明运行；请先确认来源可信。")
    if "node_modules" in paths:
        hints.append("包含依赖目录 node_modules，可离线运行。")
    if not hints:
        hints.append("解压后按文件名和包内 README 使用。")
    return "\n".join(hints)


def make_package(case, group_name, files, source_root, package_seq):
    """整理教学材料并净化嵌套包，运行数据不能随教学源码分发。"""
    label = group_name if group_name and group_name != "__root__" else "配套材料"
    title = f"{case['lessonTitle']}-{label}"
    filename = safe_name(f"第{case['lessonNo']}课-{case['lessonTitle']}-{case.get('author') or '未署名'}-{label}", 90) + ".zip"
    digest = hashlib.sha256()
    entries = []
    for item in sorted(files, key=lambda row: row["casePath"]):
        digest.update((item["sha256"] + "|" + item["casePath"] + "\n").encode("utf-8"))
    # 修改清理规则后不能复用旧缓存包。
    digest.update(b"package-hygiene-v1")
    package_sha = digest.hexdigest()
    target = CASES / package_sha[:2] / (package_sha + ".zip")
    manifest_entry = {
        "key": "pkg-" + package_sha,
        "category": "MATERIAL",
        "title": title,
        "filename": filename,
        "originalPath": f"{package_sha[:2]}/{package_sha}.zip",
        "previewPath": None,
        "htmlPreview": False,
        "bytes": 0,
        "sha256": package_sha,
        "description": f"共 {len(files)} 个文件，来源目录：{case.get('sourceDir') or ''}",
        "requirements": package_requirements(files),
        "packagedFiles": [item["casePath"] for item in files],
    }
    if target.exists():
        manifest_entry["bytes"] = target.stat().st_size
        return manifest_entry
    target.parent.mkdir(parents=True, exist_ok=True)
    readme = (
        f"{title}\r\n\r\n"
        f"课次：第{case['lessonNo']}课 {case['lessonTitle']}\r\n"
        f"作者：{case.get('author') or '未署名'}\r\n"
        f"学校：{case.get('school') or ''}\r\n"
        f"来源：{case.get('sourceDir') or ''}\r\n\r\n"
        "包内文件：\r\n" + "\r\n".join(f"- {item['casePath']}" for item in sorted(files, key=lambda row: row["casePath"])) +
        "\r\n\r\n使用条件：\r\n" + package_requirements(files) + "\r\n"
    )
    tmp = target.with_suffix(".zip.part")
    with zipfile.ZipFile(tmp, "w", allowZip64=True) as archive:
        archive.writestr("README.txt", readme.encode("utf-8-sig"))
        for item in sorted(files, key=lambda row: row["casePath"]):
            source = source_root / item["relativePath"]
            if not source.is_file():
                raise RuntimeError("打包源文件缺失：" + str(source))
            suffix = source.suffix.lower()
            method = zipfile.ZIP_STORED if suffix in {".jpg", ".jpeg", ".png", ".webp", ".avif", ".gif", ".mp4", ".webm", ".zip", ".rar", ".7z", ".mp3", ".wav"} else zipfile.ZIP_DEFLATED
            archive.write(source, arcname=item["casePath"], compress_type=method)
    cleaned, removed = clean_zip(tmp.read_bytes())
    if removed:
        tmp.write_bytes(cleaned)
        manifest_entry["description"] += f"；已清理 {len(removed)} 项运行环境或本地数据"
        manifest_entry["requirements"] = "解压后按原项目说明安装依赖，建立自己的本地数据库与配置。"
    tmp.replace(target)
    manifest_entry["bytes"] = target.stat().st_size
    return manifest_entry


def case_resources(case, source_root, existing_by_case, existing_titles, plan):
    """返回 （资源列表，文件处置列表）。旧课例按哈希和标题去重。"""
    existing_id = case.get("existingCaseId")
    old_hashes = existing_by_case.get(existing_id, set()) if existing_id else set()
    old_titles = existing_titles.get(existing_id, set()) if existing_id else set()
    resources, dispositions = [], []
    used_package_names = Counter()
    has_design = any(item["kind"] == "DESIGN" for item in case["files"])
    document_count = sum(1 for item in case["files"] if item["kind"] == "DOCUMENT")

    standalone = []
    groups = defaultdict(list)
    for item in case["files"]:
        kind = item["kind"]
        suffix = item["suffix"].lower()
        if kind in STANDALONE_KINDS or (kind in ("MATERIAL", "DOCUMENT") and suffix in DOC_PREVIEW_SUFFIX):
            standalone.append(item)
        elif kind == "HTML" and "/" not in item["casePath"]:
            standalone.append(item)
        elif kind == "VIDEO":
            standalone.append(item)
        elif kind == "ARCHIVE" and "/" not in item["casePath"]:
            standalone.append(item)
        elif kind == "IMAGE" and "/" not in item["casePath"] and len([row for row in case["files"] if row["kind"] == "IMAGE" and "/" not in row["casePath"]]) == 1:
            standalone.append(item)
        else:
            key = item["casePath"].split("/")[0] if "/" in item["casePath"] else "__root__"
            groups[key].append(item)

    # 同一教学设计的 Word/PDF 合并为一条资源，PDF 优先，Word 作为附加原件。
    design_groups = defaultdict(list)
    rest = []
    for item in standalone:
        if item["kind"] == "DESIGN":
            design_groups[normalize_design(item["casePath"])].append(item)
        else:
            rest.append(item)
    ordered = []
    for key, items in design_groups.items():
        pdfs = [row for row in items if row["suffix"].lower() == ".pdf"]
        words = [row for row in items if row["suffix"].lower() != ".pdf"]
        primary = (pdfs or words)[0]
        extra = next((row for row in words if row is not primary), None)
        ordered.append((primary, extra))
    for item in rest:
        ordered.append((item, None))

    for item, extra in ordered:
        source = source_root / item["relativePath"]
        sha = item["sha256"]
        suffix = item["suffix"].lower() or ".bin"
        category = item["kind"]
        if category == "DOCUMENT" and not has_design and document_count == 1:
            # 文件名没写“教学设计”但整课只有这一份文档时，按教学设计展示。
            category = "DESIGN"
        elif category in ("MATERIAL", "DOCUMENT", "OTHER"):
            category = "MATERIAL"
        if category == "HTML":
            category = "MATERIAL"
        title = Path(item["casePath"]).stem
        filename = Path(item["casePath"]).name
        description = f"来源：{item['relativePath']}"
        requirements = ""
        html_preview = False
        preview_path = None

        # 同名文件可能包含修订内容，只以原件哈希确认重复。
        if existing_id and sha in old_hashes:
            dispositions.append({"path": item["relativePath"], "disposition": "DUPLICATE_REUSED",
                                 "target": f"case:{existing_id}", "reason": "旧课例已有相同原件哈希"})
            if extra:
                dispositions.append({"path": extra["relativePath"], "disposition": "DUPLICATE_REUSED",
                                     "target": f"case:{existing_id}", "reason": "同一教学设计已由旧课例收录"})
            continue

        original = stage_original(source, sha, suffix)
        if suffix == ".pdf":
            preview_path = f"{sha[:2]}/{sha}.pdf"
        elif suffix in DOC_PREVIEW_SUFFIX:
            preview_path = f"previews/{sha}.pdf"
        elif item["kind"] in ("VIDEO", "IMAGE"):
            preview_path = f"{sha[:2]}/{sha}{suffix}"
        elif item["kind"] == "HTML":
            html_preview = is_standalone_html(source)
            if not html_preview:
                # 有依赖的网页不能单文件预览，整包下载。
                key = item["casePath"].split("/")[0] if "/" in item["casePath"] else "__root__"
                groups[key].append(item)
                dispositions.append({"path": item["relativePath"], "disposition": "IN_PACKAGE",
                                     "target": key, "reason": "网页依赖本地资源，整包提供"})
                continue

        resource = {
            "key": sha,
            "category": category,
            "title": title,
            "filename": filename,
            "originalPath": f"{sha[:2]}/{sha}{suffix}",
            "previewPath": preview_path,
            "htmlPreview": html_preview,
            "bytes": item["bytes"],
            "sha256": sha,
            "description": description,
            "requirements": requirements,
            "sourcePath": item["relativePath"],
        }
        if extra:
            extra_source = source_root / extra["relativePath"]
            extra_stage = stage_original(extra_source, extra["sha256"], extra["suffix"].lower())
            resource["extra"] = {
                "filename": Path(extra["casePath"]).name,
                "originalPath": f"{extra['sha256'][:2]}/{extra['sha256']}{extra['suffix'].lower()}",
                "bytes": extra["bytes"],
                "sha256": extra["sha256"],
                "sourcePath": extra["relativePath"],
            }
            dispositions.append({"path": extra["relativePath"], "disposition": "MERGED_EXTRA",
                                 "target": item["relativePath"], "reason": "同一教学设计的另一格式"})
        resources.append(resource)
        dispositions.append({"path": item["relativePath"], "disposition": "PUBLISHED",
                             "target": item["casePath"], "reason": ""})

    for group_name, files in groups.items():
        package = make_package(case, group_name, files, source_root, 0)
        resources.append(package)
        for item in files:
            dispositions.append({"path": item["relativePath"], "disposition": "IN_PACKAGE",
                                 "target": package["filename"], "reason": "项目/素材整包提供"})
        key = group_name
        used_package_names[key] += 1

    return resources, dispositions


def build_book_resources(ningbo_root):
    pdf_dir = ningbo_root / "2026新教材教学设计（宁波版）PDF"
    resources = []
    reviewed = json_read(Path(__file__).with_name("teaching_resources_20261108_coverage.json"), {})
    for book_code, filename in BOOK_PDF.items():
        source = pdf_dir / filename
        if not source.is_file():
            continue
        sha = sha256_file(source)
        suffix = source.suffix.lower()
        staged = stage_original(source, sha, suffix)
        coverage = reviewed.get(book_code)
        # 覆盖课次经过正文核对，原件变动时必须重审，不能继续套用旧说明。
        if not coverage or coverage["sha256"] != sha:
            raise ValueError(f"{filename} 的来源或覆盖范围尚未审核，请先核对正文并更新覆盖记录")
        resources.append({
            "bookCode": book_code,
            "key": "book-design-" + book_code,
            "category": "DESIGN",
            "title": "教学设计汇编",
            "filename": filename,
            "originalPath": f"{sha[:2]}/{sha}.pdf",
            "previewPath": f"{sha[:2]}/{sha}.pdf",
            "bytes": source.stat().st_size,
            "sha256": sha,
            "description": coverage["description"],
            "sourcePath": str(source),
        })
    return resources


def parse_guide_title(filename):
    stem = Path(filename).stem
    stem = re.sub(r"_\d{10,}$", "", stem)
    stem = re.sub(r"[（(]\d+[）)]$", "", stem)
    match = re.match(r"第\s*(\d+)\s*课\s*(.+)$", stem)
    if not match:
        return None, stem
    return int(match.group(1)), match.group(2).strip()


def build_guides(guide_root, usage_stage):
    guides = []
    usage_source = None
    for grade, folder_name in GUIDE_GRADES:
        folder = guide_root / folder_name
        if not folder.is_dir():
            continue
        pdfs = [p for p in folder.glob("*.pdf") if "使用说明" not in p.name]
        usage = [p for p in folder.glob("*使用说明*.pdf")]
        if usage and usage_source is None:
            usage_source = usage[0]
        if not pdfs:
            continue
        guide_pdf = sorted(pdfs, key=lambda p: p.stat().st_size, reverse=True)[0]
        sha = sha256_file(guide_pdf)
        stage_original(guide_pdf, sha, ".pdf")
        lessons = {}
        for pptx in folder.glob("*.pptx"):
            no, title = parse_guide_title(pptx.name)
            if no is None:
                continue
            lessons[no] = (title, pptx)
        lesson_rows = []
        for no in range(1, 31):
            missing = GUIDE_MISSING.get(grade, {}).get(no)
            if no in lessons:
                title, source = lessons[no]
                lesson_sha = sha256_file(source)
                stage_original(source, lesson_sha, ".pptx")
                lesson_rows.append({
                    "lessonNo": no,
                    "title": title,
                    "filename": source.name,
                    "originalPath": f"{lesson_sha[:2]}/{lesson_sha}.pptx",
                    "previewPath": f"previews/{lesson_sha}.pdf",
                    "bytes": source.stat().st_size,
                    "sha256": lesson_sha,
                    "available": 1,
                    "note": "",
                    "sourcePath": str(source),
                })
            elif missing:
                lesson_rows.append({"lessonNo": no, "title": missing, "filename": "", "originalPath": "",
                                    "previewPath": None, "bytes": 0, "sha256": "",
                                    "available": 0, "note": "来源资料包未提供本课课件；指南目录列有本课，暂未提供。",
                                    "sourcePath": ""})
            else:
                lesson_rows.append({"lessonNo": no, "title": "", "filename": "", "originalPath": "",
                                    "previewPath": None, "bytes": 0, "sha256": "",
                                    "available": 0, "note": "来源资料包未提供本课课件。", "sourcePath": ""})
        guides.append({
            "grade": grade,
            "title": folder_name.split(" ", 1)[1] if " " in folder_name else folder_name,
            "folder": str(folder),
            "pdf": {
                "filename": guide_pdf.name,
                "originalPath": f"{sha[:2]}/{sha}.pdf",
                "previewPath": f"{sha[:2]}/{sha}.pdf",
                "bytes": guide_pdf.stat().st_size,
                "sha256": sha,
                "sourcePath": str(guide_pdf),
            },
            "lessons": lesson_rows,
        })
    usage = None
    if usage_stage:
        usage = {"filename": usage_stage.name, "bytes": usage_stage.stat().st_size,
                 "originalPath": f"{sha256_file(usage_stage)[:2]}/{sha256_file(usage_stage)}.pdf"}
    return guides, usage


def build_manifest():
    inventory = json_read(OUT / "source-inventory.json")
    ningbo_root = Path(inventory["ningbo"]["root"])
    guide_root = Path(inventory["guide"]["root"])
    draft = json_read(OUT / "case-draft.json")
    existing = json_read(OUT / "existing-resources.json", [])

    existing_by_case = defaultdict(set)
    existing_titles = defaultdict(set)
    for row in existing:
        match = re.search(r"([0-9a-f]{64})", row.get("originalPath") or "")
        if match:
            existing_by_case[row["caseId"]].add(match.group(1))
        existing_titles[row["caseId"]].add(normalize_design(row.get("filename") or ""))
        if row.get("title"):
            existing_titles[row["caseId"]].add(normalize_design(row["title"]))

    case_records, dispositions = [], []
    for case in draft["cases"]:
        resources, dispo = case_resources(case, ningbo_root, existing_by_case, existing_titles, None)
        dispositions.extend({"case": case["sourceKey"], **item} for item in dispo)
        if not resources:
            continue
        case_records.append({
            "sourceKey": case["sourceKey"],
            "existingCaseId": case.get("existingCaseId"),
            "bookCode": case["bookCode"],
            "sectionKey": case["sectionKey"],
            "lessonNo": case["lessonNo"],
            "lessonTitle": case["lessonTitle"],
            "title": case["title"],
            "author": case["author"],
            "school": case["school"],
            "origin": "NINGBO2026",
            "sourceDir": case.get("sourceDir") or "",
            "sourceName": case.get("sourceName") or "",
            "resources": resources,
        })

    books = build_book_resources(ningbo_root)
    usage_stage = None
    first_usage = next(iter(guide_root.glob("*/义务教育信息科技课程教学指南和配套课件使用说明.pdf")), None)
    if first_usage:
        usage_sha = sha256_file(first_usage)
        usage_stage = stage_original(first_usage, usage_sha, ".pdf")
    guides, usage = build_guides(guide_root, usage_stage)

    manifest = {
        "builtAt": datetime.now().isoformat(timespec="seconds"),
        "batch": "nb2026_20261107",
        "root": str(ningbo_root),
        "guideRoot": str(guide_root),
        "cases": case_records,
        "bookResources": books,
        "guides": guides,
        "usage": usage,
        "dispositions": dispositions,
    }
    json_write(OUT / "resource-manifest.json", manifest)
    # 逐文件处置清单，便于审计“没有任何资料被静默丢弃”。
    json_write(OUT / "file-dispositions.json", dispositions)
    print("cases", len(case_records), "resources", sum(len(c["resources"]) for c in case_records))
    print("book resources", len(books), "guides", len(guides),
          "guide lessons", sum(len(g["lessons"]) for g in guides))
    print("dispositions", Counter(item["disposition"] for item in dispositions))
    return manifest


def convert_previews(manifest):
    pending = []
    for case in manifest["cases"]:
        for resource in case["resources"]:
            if not resource.get("previewPath") or not resource.get("sourcePath"):
                continue
            if not resource["previewPath"].startswith("previews/"):
                continue
            if (CASES / resource["previewPath"]).exists():
                continue
            source = Path(resource["sourcePath"])
            if not source.is_absolute():
                source = Path(manifest["root"]) / source
            if source.suffix.lower() not in DOC_PREVIEW_SUFFIX:
                continue
            pending.append((source, CASES / resource["previewPath"]))
    for guide in manifest["guides"]:
        for lesson in guide["lessons"]:
            if lesson["available"] and lesson["previewPath"] and lesson["sourcePath"]:
                target = CASES / lesson["previewPath"]
                if not target.exists():
                    pending.append((Path(lesson["sourcePath"]), target))
    # 去重：同一内容只转一次
    unique = {}
    for source, target in pending:
        unique[str(target)] = (source, target)
    pending = list(unique.values())
    print("pending previews", len(pending))
    batches, used = [], []
    for source, target in pending:
        placed = False
        for index, batch in enumerate(batches):
            if len(batch) >= 18:
                continue
            if source.stem in used[index]:
                continue
            batch.append((source, target))
            used[index].add(source.stem)
            placed = True
            break
        if not placed:
            batches.append([(source, target)])
            used.append({source.stem})
    print("soffice batches", len(batches))
    failures = []
    for index, batch in enumerate(batches, 1):
        work = WORK / f"convert-{index:03d}"
        if work.exists():
            shutil.rmtree(work)
        work.mkdir(parents=True, exist_ok=True)
        mapping = {}
        for source, target in batch:
            copied = work / (hashlib.sha256(str(source).encode("utf-8")).hexdigest()[:16] + source.suffix.lower())
            shutil.copy2(source, copied)
            mapping[copied.stem] = target
        profile = OUT / "work" / "soffice-profile-convert"
        command = [SOFFICE, "-env:UserInstallation=" + profile.as_uri(), "--headless",
                   "--convert-to", "pdf", "--outdir", str(work)]
        command.extend(str(path) for path in sorted(work.iterdir()) if path.suffix.lower() in DOC_PREVIEW_SUFFIX)
        try:
            result = subprocess.run(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=1800)
        except subprocess.TimeoutExpired:
            failures.extend(str(source) for source, _ in batch)
            print("batch timeout", index, flush=True)
            continue
        for stem, target in mapping.items():
            produced = work / (stem + ".pdf")
            if not produced.exists() or produced.stat().st_size < 1024:
                failures.append(str(target))
                continue
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.move(str(produced), str(target))
        print(f"batch {index}/{len(batches)} done, failures so far {len(failures)}", flush=True)
        shutil.rmtree(work, ignore_errors=True)
    (OUT / "preview-failures.json").write_text(json.dumps(failures, ensure_ascii=False, indent=1), encoding="utf-8")
    print("preview failures", len(failures))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("mode", choices=["plan", "convert"])
    args = parser.parse_args()
    CASES.mkdir(parents=True, exist_ok=True)
    PREVIEWS.mkdir(parents=True, exist_ok=True)
    if args.mode == "plan":
        build_manifest()
    else:
        manifest = json_read(OUT / "resource-manifest.json")
        convert_previews(manifest)


if __name__ == "__main__":
    main()
