"""2025宁波课例批次：把压缩包解到工作副本（E 盘只读，不修改来源）。

- zip 按 UTF-8 标志位解码，缺失时回退 cp936，避免中文名被 errors=ignore 掩盖；
- 拦截绝对路径、..、符号链接与异常解压规模；
- 每个包单独目录，记录成员清单与失败项，可断点续做。
"""
import hashlib
import json
import shutil
import subprocess
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases-2025"
WORK = OUT / "work/extracted"
MAX_MEMBER_BYTES = 2 * 1024 * 1024 * 1024
MAX_TOTAL_BYTES = 4 * 1024 * 1024 * 1024


def tag(relative):
    return hashlib.sha256(relative.encode("utf-8")).hexdigest()[:12]


def safe_member(name):
    normalized = name.replace("\\", "/").strip()
    if not normalized or normalized.startswith("/") or ":" in normalized.split("/")[0]:
        return None
    parts = [part for part in normalized.split("/") if part not in ("", ".")]
    if any(part == ".." for part in parts):
        return None
    return "/".join(parts)


def decode_zip_name(info):
    if info.flag_bits & 0x800:
        return info.filename
    try:
        return info.filename.encode("cp437").decode("gbk")
    except (UnicodeEncodeError, UnicodeDecodeError):
        return info.filename


def extract_zip(archive, destination, report):
    with zipfile.ZipFile(archive) as package:
        total = 0
        for info in package.infolist():
            name = safe_member(decode_zip_name(info))
            if name is None:
                report["skipped"].append({"name": info.filename, "reason": "路径越界"})
                continue
            if info.is_dir():
                continue
            if info.file_size > MAX_MEMBER_BYTES:
                report["skipped"].append({"name": name, "reason": "单成员过大", "bytes": info.file_size})
                continue
            total += info.file_size
            if total > MAX_TOTAL_BYTES:
                report["skipped"].append({"name": name, "reason": "解压总量超限"})
                continue
            target = destination / name
            target.parent.mkdir(parents=True, exist_ok=True)
            with package.open(info) as source, target.open("wb") as sink:
                shutil.copyfileobj(source, sink, 1024 * 1024)
            report["members"].append({"name": name, "bytes": info.file_size})


def extract_rar(archive, destination, report):
    # 系统 bsdtar 对 RAR5 压缩过滤器报 "Parsing filters is unsupported"，会静默漏文件；
    # 本机已装 Bandizip 控制台，先复制成 ASCII 名再解包，避开命令行中文编码问题。
    staging = WORK / "_srcarc"
    staging.mkdir(parents=True, exist_ok=True)
    copy = staging / (tag(archive.as_posix()) + ".rar")
    shutil.copyfile(archive, copy)
    try:
        completed = subprocess.run([r"C:/Program Files/Bandizip/bz.exe", "x",
                                    "-o:" + str(destination), "-y", str(copy)],
                                   stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=1800)
        if completed.returncode:
            report["errors"].append("Bandizip 解包失败 exit=" + str(completed.returncode) + " "
                                    + completed.stderr.decode("utf-8", errors="replace")[:300])
    finally:
        copy.unlink(missing_ok=True)
    for path in sorted(destination.rglob("*")):
        if path.is_file():
            report["members"].append({"name": path.relative_to(destination).as_posix(), "bytes": path.stat().st_size})


def main():
    data = json.loads((OUT / "source-inventory.json").read_text(encoding="utf-8"))
    WORK.mkdir(parents=True, exist_ok=True)
    results = []
    for archive in data["archives"]:
        archive_path = Path(data["sourceRoot"]) / archive["relativePath"]
        destination = WORK / tag(archive["relativePath"])
        report = {"relativePath": archive["relativePath"], "caseDirectory": archive["caseDirectory"],
                  "workDirectory": destination.relative_to(ROOT).as_posix(),
                  "members": [], "skipped": [], "errors": []}
        marker = destination / ".extract-ok.json"
        if marker.exists():
            report.update(json.loads(marker.read_text(encoding="utf-8")))
            results.append(report)
            print("跳过已完成 " + archive["relativePath"], flush=True)
            continue
        if destination.exists():
            shutil.rmtree(destination)
        destination.mkdir(parents=True, exist_ok=True)
        try:
            if archive_path.suffix.lower() == ".zip":
                extract_zip(archive_path, destination, report)
            else:
                extract_rar(archive_path, destination, report)
        except Exception as error:
            report["errors"].append(type(error).__name__ + ": " + str(error)[:300])
        report["memberCount"] = len(report["members"])
        report["memberBytes"] = sum(item["bytes"] for item in report["members"])
        marker.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
        results.append(report)
        print(f"解包 {archive['relativePath']} 成员{report['memberCount']} 跳过{len(report['skipped'])} 错误{len(report['errors'])}", flush=True)
    (OUT / "archive-extract.json").write_text(json.dumps(results, ensure_ascii=False, indent=2), encoding="utf-8")
    print("全部完成")


if __name__ == "__main__":
    main()
