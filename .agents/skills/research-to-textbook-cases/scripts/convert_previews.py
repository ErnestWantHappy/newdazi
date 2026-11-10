"""把可安全预览的 Office 文件转为 PDF；使用独立 LibreOffice 用户目录、临时目录和超时。"""
from __future__ import annotations

import argparse
import json
import shutil
import subprocess
import tempfile
from pathlib import Path

from common import read_json, run_dir, sha256_file, utc_now, write_json


CONVERTIBLE = {".doc", ".docx", ".ppt", ".pptx", ".xls", ".xlsx"}
MAX_CONVERT_BYTES = 120 * 1024 * 1024
TIMEOUT_SECONDS = 180


def soffice() -> str:
    candidates = [
        Path("C:/Program Files/LibreOffice/program/soffice.exe"),
        Path("C:/Program Files (x86)/LibreOffice/program/soffice.exe"),
    ]
    for path in candidates:
        if path.exists():
            return str(path)
    found = shutil.which("soffice") or shutil.which("libreoffice")
    if not found:
        raise RuntimeError("未找到 LibreOffice")
    return found


def convert(source: Path, previews: Path, converter: str, work_root: Path) -> dict:
    result = {"source": source.name, "state": "FAILED", "reason": ""}
    if source.stat().st_size > MAX_CONVERT_BYTES:
        result["reason"] = "文件超过 120MiB，标记为下载后查看"
        return result
    with tempfile.TemporaryDirectory(prefix="research-case-lo-", dir=str(work_root)) as temp:
        profile = Path(temp) / "profile"
        output = Path(temp) / "out"
        output.mkdir()
        command = [converter, "-env:UserInstallation=file:///" + profile.as_posix(),
                   "--headless", "--norestore", "--invisible", "--convert-to", "pdf",
                   "--outdir", str(output), str(source)]
        try:
            completed = subprocess.run(command, capture_output=True, timeout=TIMEOUT_SECONDS)
        except subprocess.TimeoutExpired:
            result["reason"] = "转换超时，保留原件下载"
            return result
        if completed.returncode:
            result["reason"] = "转换失败，保留原件下载"
            return result
        candidates = list(output.glob("*.pdf"))
        if not candidates:
            result["reason"] = "未生成 PDF，保留原件下载"
            return result
        digest = sha256_file(candidates[0])
        target = previews / (digest + ".pdf")
        target.parent.mkdir(parents=True, exist_ok=True)
        if not target.exists():
            # Windows 的独立 profile 可能位于系统临时盘；跨盘用复制而不是 rename。
            shutil.copyfile(candidates[0], target)
        result.update({"state": "CONVERTED", "previewPath": "previews/" + target.name,
                       "bytes": target.stat().st_size, "sha256": digest})
    return result


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--run-id", required=True)
    args = parser.parse_args()
    run_path = run_dir(args.run_id)
    resources = read_json(run_path / "reports" / "resources.json")["resources"]
    converter = soffice()
    report = {"generatedAt": utc_now(), "converter": converter, "independentProfile": True,
              "singleProcess": True, "timeoutSeconds": TIMEOUT_SECONDS, "items": []}
    for resource in resources:
        source = run_path / resource["stagingPath"]
        if source.suffix.lower() not in CONVERTIBLE:
            continue
        item = convert(source, run_path / "previews", converter, run_path)
        item["postId"] = resource["postId"]
        item["resourceKey"] = resource["resourceKey"]
        report["items"].append(item)
        if item["state"] == "CONVERTED":
            resource["previewPath"] = item["previewPath"]
            resource["previewBytes"] = item["bytes"]
            resource["previewSha256"] = item["sha256"]
    write_json(run_path / "reports" / "resources.json", {"generatedAt": utc_now(), "resources": resources})
    output = run_path / "reports" / "preview-conversion.json"
    write_json(output, report)
    print(json.dumps({"report": str(output), "converted": sum(1 for item in report["items"] if item["state"] == "CONVERTED"),
                      "failed": sum(1 for item in report["items"] if item["state"] == "FAILED")},
                     ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
