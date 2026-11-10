"""深度检查压缩包：目录树、魔数、哈希、嵌套包、文档内容与发布风险。"""
from __future__ import annotations

import argparse
import json
import mimetypes
import shutil
import subprocess
import zipfile
from pathlib import Path

from common import (
    ARCHIVE_SUFFIXES, EXECUTABLE_SUFFIXES, PipelineError, archive_tool,
    category_for, contains_personal_info, decode_text, detect_mime,
    decode_zip_name, external_tool_version, read_json, safe_member_path, sha256_bytes,
    sha256_file, url_shortcut_targets, utc_now, write_json, zip_entries,
)


MAX_EXTRACT_BYTES = 2 * 1024 * 1024 * 1024
MAX_COMPRESSION_RATIO = 200


def unsafe_external_names(names: list[str]) -> list[str]:
    """外部解压工具不会替我们校验成员路径；先按 Zip 同一规则拦截。"""
    unsafe: list[str] = []
    for name in names:
        try:
            safe_member_path(name)
        except PipelineError:
            unsafe.append(name)
    return unsafe


def is_within(root: Path, path: Path) -> bool:
    try:
        path.relative_to(root)
        return True
    except ValueError:
        return False


def office_summary(data: bytes, name: str) -> dict:
    suffix = Path(name).suffix.lower()
    result: dict = {}
    if suffix in {".docx", ".pptx", ".xlsx"}:
        with zipfile.ZipFile(__import__("io").BytesIO(data)) as archive:
            names = archive.namelist()
            if suffix == ".docx":
                document = decode_text(archive.read("word/document.xml"))
                import re
                texts = re.findall(r"<w:t[^>]*>(.*?)</w:t>", document)
                result.update({"paragraphCount": document.count("<w:p "), "textLength": sum(len(item) for item in texts),
                               "firstText": "".join(texts[:12])[:300]})
            elif suffix == ".pptx":
                slides = [item for item in names if item.startswith("ppt/slides/slide") and item.endswith(".xml")]
                notes = [item for item in names if item.startswith("ppt/notesSlides/notesSlide") and item.endswith(".xml")]
                media = [item for item in names if item.startswith("ppt/media/")]
                result.update({"slideCount": len(slides), "notesCount": len(notes), "mediaCount": len(media)})
            else:
                workbook = decode_text(archive.read("xl/workbook.xml"))
                import re
                result.update({"sheetNames": re.findall(r'<sheet[^>]+name="([^"]+)"', workbook)})
    elif suffix == ".pdf":
        try:
            from pypdf import PdfReader
            reader = PdfReader(__import__("io").BytesIO(data))
            first = reader.pages[0].extract_text() or "" if reader.pages else ""
            result.update({"pageCount": len(reader.pages), "firstText": first[:300]})
        except Exception:
            result["readError"] = "PDF 内容读取失败，保留原件下载"
    return result


def media_summary(data: bytes, name: str, mime: str) -> dict:
    suffix = Path(name).suffix.lower()
    result: dict = {}
    if mime.startswith("image/"):
        try:
            from PIL import Image
            import io
            with Image.open(io.BytesIO(data)) as image:
                result.update({"width": image.width, "height": image.height, "format": image.format})
        except Exception:
            result["readError"] = "图片尺寸读取失败"
    elif mime.startswith("audio/") or suffix in {".mp3", ".wav", ".m4a", ".ogg"}:
        result.update({"format": suffix.lstrip(".").upper(), "bytes": len(data),
                       "duration": None, "durationNote": "未安装音频时长解析库，保留原文件信息"})
    elif mime.startswith("video/") or suffix in {".mp4", ".webm"}:
        result.update({"format": suffix.lstrip(".").upper(), "bytes": len(data),
                       "duration": None, "durationNote": "未安装视频时长解析库，保留原文件信息"})
    return result


def inspect_zip(path: Path, level: int, tool: str, tool_version: str) -> dict:
    entries = zip_entries(path)
    report = {
        "name": path.name,
        "bytes": path.stat().st_size,
        "sha256": sha256_file(path),
        "tool": tool,
        "toolVersion": tool_version,
        "level": level,
        "encrypted": any(item["encrypted"] for item in entries),
        "truncated": False,
        "entries": [],
        "nested": [],
        "safety": [],
    }
    total = sum(item["bytes"] for item in entries)
    if total > MAX_EXTRACT_BYTES:
        report["safety"].append("解压体积超过 2GiB 上限")
    for item in entries:
        name = item["name"]
        data = item.pop("data", b"")
        item["sha256"] = sha256_bytes(data) if data else sha256_bytes(name.encode("utf-8"))
        try:
            safe_member_path(name)
        except PipelineError:
            report["safety"].append("路径穿越或绝对路径：" + name)
        suffix = Path(name).suffix.lower()
        if item["bytes"] and item["compressedBytes"] and item["bytes"] / item["compressedBytes"] > MAX_COMPRESSION_RATIO:
            report["safety"].append("异常压缩比：" + name)
        if suffix in EXECUTABLE_SUFFIXES:
            report["safety"].append("来源程序或快捷方式：" + name)
        if "__MACOSX" in name or Path(name).name in {".DS_Store"}:
            report["safety"].append("无教学价值环境文件：" + name)
        if suffix in {".url"}:
            item["urlTargets"] = url_shortcut_targets(data)
            if "<blocked>" in item["urlTargets"]:
                report["safety"].append("快捷方式指向内网或本机地址：" + name)
        if suffix in {".docx", ".pptx", ".xlsx", ".pdf"} and data:
            item["content"] = office_summary(data, name)
            if "firstText" in item["content"] and contains_personal_info(item["content"]["firstText"]):
                report["safety"].append("文档内容疑似个人信息：" + name)
        if item["mime"].startswith(("image/", "audio/", "video/")) and data:
            item["content"] = media_summary(data, name, item["mime"])
        if suffix in ARCHIVE_SUFFIXES and data:
            nested_name = path.parent / ("__nested__" + sha256_bytes(data)[:12] + suffix)
            nested_name.write_bytes(data)
            report["nested"].append(inspect_zip(nested_name, level + 1, tool, tool_version)
                                    if suffix == ".zip" else inspect_external(nested_name, level + 1))
            nested_name.unlink(missing_ok=True)
        report["entries"].append(item)
    if any("来源程序或快捷方式" in warning for warning in report["safety"]):
        report["state"] = "PENDING_ARCHIVE"
        report["pendingReason"] = "压缩包包含需本地运行的来源程序或快捷方式，保留原包但不单独上架"
    else:
        report["state"] = "INSPECTED"
    return report


def inspect_external(path: Path, level: int = 0) -> dict:
    tool, tool_version = archive_tool()
    listing = subprocess.run([tool, "-tf", str(path)], capture_output=True, timeout=120)
    if listing.returncode:
        return {"name": path.name, "bytes": path.stat().st_size, "sha256": sha256_file(path),
                "level": level, "state": "PENDING_ARCHIVE", "error": decode_text(listing.stderr)[:300],
                "tool": tool, "toolVersion": tool_version}
    names = [decode_zip_name(name) for name in decode_text(listing.stdout).splitlines() if name.strip()]
    unsafe_names = unsafe_external_names(names)
    if unsafe_names:
        return {"name": path.name, "bytes": path.stat().st_size, "sha256": sha256_file(path),
                "level": level, "state": "PENDING_ARCHIVE",
                "error": "压缩包包含路径穿越或绝对路径，已拒绝解压",
                "tool": tool, "toolVersion": tool_version, "entryCount": len(names),
                "safety": ["路径穿越或绝对路径：" + name for name in unsafe_names],
                "entries": [], "nested": [], "splitFiles": []}
    target = path.parent / ("__inspect__" + sha256_file(path)[:12])
    if target.exists():
        shutil.rmtree(target)
    target.mkdir(exist_ok=True)
    split_root = path.parent / ("__split__" + sha256_file(path)[:12])
    if split_root.exists():
        shutil.rmtree(split_root)
    split_root.mkdir()
    try:
        extract = subprocess.run([tool, "-xf", str(path), "-C", str(target)], capture_output=True, timeout=300)
        if extract.returncode:
            return {"name": path.name, "bytes": path.stat().st_size, "sha256": sha256_file(path),
                    "level": level, "state": "PENDING_ARCHIVE", "error": decode_text(extract.stderr)[:300],
                    "tool": tool, "toolVersion": tool_version}
        entries = []
        extracted_files = []
        safety = []
        target_root = target.resolve()
        for local in sorted(item for item in target.rglob("*") if item.is_file()):
            relative = local.relative_to(target).as_posix()
            if local.is_symlink():
                safety.append("包含符号链接：" + relative)
                continue
            if not is_within(target_root, local.resolve()):
                safety.append("解压路径越界：" + relative)
                continue
            data = local.read_bytes()
            suffix = local.suffix.lower()
            if suffix in EXECUTABLE_SUFFIXES:
                safety.append("来源程序或快捷方式：" + relative)
            if suffix == ".url":
                if "<blocked>" in url_shortcut_targets(data):
                    safety.append("快捷方式指向内网或本机地址：" + relative)
            entries.append({
                "name": relative,
                "bytes": len(data),
                "sha256": sha256_bytes(data),
                "mime": detect_mime(data, relative),
                "content": office_summary(data, relative) if suffix in {".docx", ".pptx", ".xlsx", ".pdf"} else {},
            })
            extracted_files.append({"name": relative, "path": str(local), "data": data})
        result = {"name": path.name, "bytes": path.stat().st_size, "sha256": sha256_file(path),
                "level": level, "state": "PENDING_ARCHIVE" if safety else "INSPECTED",
                "pendingReason": "压缩包包含需人工复核的路径、符号链接或来源程序，保留原包但不单独上架" if safety else "",
                "tool": tool, "toolVersion": tool_version,
                "entryCount": len(names), "entries": entries, "safety": safety,
                "nested": [inspect_external(local, level + 1) for local in target.rglob("*")
                           if local.is_file() and not local.is_symlink()
                           and local.suffix.lower() in {".zip", ".rar", ".7z"}]}
        split_files = []
        for item in extracted_files:
            relative = item["name"]
            suffix = Path(relative).suffix.lower()
            if suffix in EXECUTABLE_SUFFIXES or suffix in ARCHIVE_SUFFIXES or suffix in {".txt", ".url"}:
                continue
            if "__MACOSX" in relative or Path(relative).name in {".DS_Store", ".pyc", ".log", ".tmp"}:
                continue
            if len(item["data"]) > MAX_EXTRACT_BYTES:
                continue
            split_name = relative.replace("/", "__")
            split_files.append({
                "name": relative,
                "bytes": len(item["data"]),
                "sha256": sha256_bytes(item["data"]),
                "mime": detect_mime(item["data"], relative),
                "content": office_summary(item["data"], relative) if suffix in {".docx", ".pptx", ".xlsx", ".pdf"} else {},
                "extractedPath": str((split_root / split_name).resolve()),
            })
            (split_root / split_name).write_bytes(item["data"])
        result["splitFiles"] = split_files
        return result
    finally:
        shutil.rmtree(target, ignore_errors=True)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--run-id", required=True)
    args = parser.parse_args()
    from common import run_dir
    run_path = run_dir(args.run_id)
    assets = read_json(run_path / "reports" / "assets.json")["assets"]
    tool, tool_version = archive_tool()
    report = {"generatedAt": utc_now(), "tool": tool, "toolVersion": tool_version,
              "officeVersion": external_tool_version([
                  str(Path("C:/Program Files/LibreOffice/program/soffice.exe")), "--version"])
              if Path("C:/Program Files/LibreOffice/program/soffice.exe").exists()
              else external_tool_version(["soffice", "--version"]), "archives": []}
    inspected_keys = set()
    for asset in assets:
        path = run_path / asset["stagingPath"]
        if path.suffix.lower() not in ARCHIVE_SUFFIXES:
            continue
        key = (int(asset["postId"]), asset["sha256"])
        if key in inspected_keys:
            continue
        inspected_keys.add(key)
        inspected = inspect_zip(path, 0, tool, tool_version) if path.suffix.lower() == ".zip" else inspect_external(path)
        inspected["postId"] = asset["postId"]
        inspected["stagingPath"] = asset["stagingPath"]
        report["archives"].append(inspected)
    output = run_path / "reports" / "archive-inspection.json"
    write_json(output, report)
    print(json.dumps({"report": str(output), "archives": len(report["archives"]),
                      "pending": sum(1 for item in report["archives"] if item.get("state") == "PENDING_ARCHIVE")},
                     ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
