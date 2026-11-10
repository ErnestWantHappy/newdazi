"""从 staging 资源与压缩包检查结果生成待导入资源清单，按内容哈希去重。"""
from __future__ import annotations

import argparse
import json
import re
import shutil
import zipfile
from pathlib import Path

from common import (
    ARCHIVE_SUFFIXES, EXECUTABLE_SUFFIXES, PipelineError, category_for,
    decode_text, decode_zip_name, detect_mime, display_title, read_json, resource_key, run_dir,
    safe_member_path, sha256_bytes, stable_sort_no, utc_now, write_json,
)


MAX_STANDALONE_BYTES = 160 * 1024 * 1024


def course_context(scan_post: dict) -> tuple[str, int | None]:
    return scan_post["courseTitle"].strip("《》"), int(scan_post["lessonNo"] or 0) or None


def readable_title(name: str, course_title: str, lesson_no: int | None, category: str) -> str:
    title = display_title(name, course_title, lesson_no, category)
    stem = Path(name).stem
    if category == "MATERIAL":
        if re.search(r"yt_\d+|复制这个|^[1-9]-", stem):
            return "《%s》课堂演示素材（%s）" % (course_title, stem[:24])
        return "《%s》配套素材（%s）" % (course_title, stem[:40])
    return title


def split_archive(archive_path: Path, inspection: dict, run_path: Path, post_id: int,
                  course_title: str, lesson_no: int | None) -> list[dict]:
    if inspection.get("state") == "PENDING_ARCHIVE":
        return []
    extracted_root = run_path / "extracted" / ("post-%d" % post_id) / Path(inspection["sha256"]).name[:16]
    entries = inspection.get("splitFiles", inspection.get("entries", []))
    result = []
    for item in entries:
        name = item["name"]
        suffix = Path(name).suffix.lower()
        if item.get("isDir") or suffix in EXECUTABLE_SUFFIXES or suffix in ARCHIVE_SUFFIXES or suffix in {".txt", ".url"}:
            continue
        if item.get("mime") == "application/octet-stream" or item["bytes"] > MAX_STANDALONE_BYTES:
            continue
        if any(marker in name for marker in ("__MACOSX", ".DS_Store")) or Path(name).name.startswith("~$"):
            continue
        data = item.get("data", b"")
        extracted_path = item.get("extractedPath")
        if not data and extracted_path and Path(extracted_path).exists():
            data = Path(extracted_path).read_bytes()
        if not data and archive_path.suffix.lower() == ".zip":
            try:
                with zipfile.ZipFile(archive_path) as archive:
                    info = archive.getinfo(item.get("rawName", name))
                    data = archive.read(info)
            except (KeyError, zipfile.BadZipFile):
                continue
        if not data:
            continue
        relative = safe_member_path(name)
        target = extracted_root / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
        digest = sha256_bytes(data)
        category = category_for(name, item.get("mime", "application/octet-stream"))
        result.append({
            "postId": post_id,
            "resourceKey": resource_key(post_id, digest),
            "category": category,
            "title": readable_title(name, course_title, lesson_no, category),
            "filename": Path(name).name,
            "bytes": len(data),
            "sha256": digest,
            "stagingPath": str(target.relative_to(run_path).as_posix()),
            "mime": item.get("mime"),
            "fromArchive": True,
            "archiveName": archive_path.name,
            "sortNo": stable_sort_no(category, len(result)),
            "previewPath": "previews/" + digest + ".pdf" if suffix == ".pdf" else None,
            "description": "",
            "requirements": "",
        })
    return result


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--run-id", required=True)
    args = parser.parse_args()
    from common import OUTPUT_ROOT
    run_path = run_dir(args.run_id)
    scan = {int(item["postId"]): item for item in read_json(OUTPUT_ROOT / ("scan-%s.json" % args.run_id))["posts"]}
    assets = read_json(run_path / "reports" / "assets.json")["assets"]
    inspection = read_json(run_path / "reports" / "archive-inspection.json")
    for stale in (run_path / "extracted").rglob("*"):
        if stale.is_file() and stale.suffix.lower() in {".txt", ".url"}:
            stale.unlink()
    resources: list[dict] = []
    seen: set[tuple[int, str]] = set()
    for asset in assets:
        post_id = int(asset["postId"])
        if post_id not in scan:
            continue
        if Path(asset["name"]).suffix.lower() in {".txt", ".url"}:
            continue
        course_title, lesson_no = course_context(scan[post_id])
        path = run_path / asset["stagingPath"]
        data_head = path.read_bytes()[:4096]
        mime = detect_mime(data_head, path.name)
        category = category_for(path.name, mime)
        if (post_id, asset["sha256"]) in seen:
            continue
        seen.add((post_id, asset["sha256"]))
        resources.append({
            "postId": post_id,
            "resourceKey": resource_key(post_id, asset["sha256"]),
            "category": category,
            "title": readable_title(path.name, course_title, lesson_no, category),
            "filename": path.name,
            "bytes": int(asset["bytes"]),
            "sha256": asset["sha256"],
            "stagingPath": asset["stagingPath"],
            "mime": mime,
            "fromArchive": False,
            "archiveName": path.name if category == "ARCHIVE" else "",
            "sortNo": stable_sort_no(category, len(resources)),
            "previewPath": "previews/" + asset["sha256"] + ".pdf" if mime == "application/pdf" else None,
            "description": "",
            "requirements": "",
        })
        if category == "ARCHIVE":
            report = next((item for item in inspection["archives"]
                           if item["name"] == path.name and item["postId"] == post_id), None)
            if report:
                for split in split_archive(path, report, run_path, post_id, course_title, lesson_no):
                    if (split["postId"], split["sha256"]) in seen:
                        continue
                    seen.add((split["postId"], split["sha256"]))
                    resources.append(split)
    output = run_path / "reports" / "resources.json"
    write_json(output, {"generatedAt": utc_now(), "resources": resources})
    print(json.dumps({"report": str(output), "resources": len(resources),
                      "archives": sum(1 for item in resources if item["category"] == "ARCHIVE")},
                     ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
