"""2025宁波课例批次：来源全量只读盘点（不含内容判定）。

- 逐文件记录相对路径、字节、mtime、归属候选目录、扩展名；
- 只读打开，不写 E 盘；
- 压缩包成员单独登记，避免与外层文件重复计数；
- 大文件默认不计算 SHA（发布时再按需计算并记录原因）。
"""
import argparse
import hashlib
import json
import os
import zipfile
from pathlib import Path

SOURCE = Path(r"E:\2025年宁波市义务段“新课程背景下优秀课堂教学案例”（信息科技）")
BOOK_DIRS = {
    "小学/三年级上册": "xxkj-3-0-2026", "小学/三年级下册": "xxkj-3-1-2026",
    "小学/四年级上册": "xxkj-4-0-2026", "小学/四年级下册": "xxkj-4-1-2026",
    "小学/五年级上册": "xxkj-5-0-2026", "小学/五年级下册": "xxkj-5-1-2026",
    "小学/六年级上册": "xxkj-6-0-2026", "小学/六年级下册": "xxkj-6-1-2026",
    "初中/七上": "xxkj-7-0-2026", "初中/七下": "xxkj-7-1-2026",
    "初中/八上": "xxkj-8-0-2026", "初中/八下": "xxkj-8-1-2026",
}
# 这些后缀属于作者运行库/工程依赖，不逐文件登记为教学资源。
RUNTIME_HINTS = {".dll", ".pyd", ".pth", ".pyc", ".lib", ".exe", ".cat", ".tm", ".enc", ".tcl",
                 ".msg", ".pdiparams", ".pdmodel", ".ib", ".hex", ".vc", ".mp", ".nmake",
                 "._pth", ".py", ".js", ".css", ".woff", ".woff2", ".ttf", ".eot", ".sh",
                 ".bat", ".json", ".info", ".terms", ".hd", ".drawio", ".fprg", ".pos",
                 ".c", ".cpp", ".h"}


def digest(path):
    result = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            result.update(chunk)
    return result.hexdigest()


def members(archive):
    """只列成员名和声明大小，不解压。"""
    result = []
    try:
        if archive.suffix.lower() == ".zip":
            with zipfile.ZipFile(archive) as package:
                for info in package.infolist():
                    result.append({"name": info.filename, "bytes": info.file_size,
                                   "isDirectory": info.is_dir()})
        else:
            import subprocess
            completed = subprocess.run(["tar.exe", "-tvf", str(archive)],
                                       stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=120)
            if completed.returncode == 0:
                text = completed.stdout.decode("utf-8", errors="replace")
                for line in text.splitlines():
                    parts = line.split(None, 8)
                    if len(parts) >= 9:
                        result.append({"name": parts[8], "bytes": int(parts[4]) if parts[4].isdigit() else 0,
                                       "isDirectory": line.startswith("d")})
            else:
                result.append({"name": "", "bytes": 0, "isDirectory": False,
                               "error": completed.stderr.decode("utf-8", errors="replace")[:200]})
    except Exception as error:  # 压缩包损坏或编码异常也要留证据
        result.append({"name": "", "bytes": 0, "isDirectory": False,
                       "error": type(error).__name__ + ": " + str(error)[:200]})
    return result


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", type=Path, default=Path("output/textbook-cases-2025/source-inventory.json"))
    args = parser.parse_args()
    records = []
    archives = []
    for book_dir, book_code in BOOK_DIRS.items():
        root = SOURCE / book_dir
        if not root.is_dir():
            continue
        # 册目录下直接存放的压缩包也各算一个候选课例，不能漏。
        candidates = [(p, (Path(book_dir) / p.name).as_posix()) for p in sorted(root.iterdir()) if p.is_dir()]
        candidates += [(p, (Path(book_dir) / p.stem).as_posix()) for p in sorted(root.iterdir())
                       if p.is_file() and p.suffix.lower() in {".zip", ".rar", ".7z"}]
        for case_dir, case_name in candidates:
            paths = [case_dir] if case_dir.is_file() else sorted(case_dir.rglob("*"))
            for path in paths:
                if not path.is_file():
                    continue
                relative = path.relative_to(SOURCE).as_posix()
                suffix = path.suffix.lower()
                records.append({
                    "relativePath": relative,
                    "caseDirectory": case_name,
                    "bookCode": book_code,
                    "suffix": suffix or "(无扩展名)",
                    "bytes": path.stat().st_size,
                    "mtime": int(path.stat().st_mtime),
                    "archiveEntry": case_dir.is_file(),
                    "archiveMember": False,
                })
                if suffix in {".zip", ".rar", ".7z"}:
                    archives.append({"relativePath": relative, "caseDirectory": case_name,
                                     "members": members(path)})
    # 两份汇总表只作署名/课名核对依据，不作为教学材料上架。
    summaries = [p for group in SOURCE.iterdir() if group.is_dir()
                 for p in group.iterdir() if p.is_file() and p.suffix.lower() == ".xlsx"]
    for path in sorted(summaries):
        relative = path.relative_to(SOURCE).as_posix()
        records.append({"relativePath": relative, "caseDirectory": "(汇总表)", "bookCode": "",
                        "suffix": ".xlsx", "bytes": path.stat().st_size,
                        "mtime": int(path.stat().st_mtime), "archiveEntry": False, "archiveMember": False})
    data = {"sourceRoot": str(SOURCE), "fileCount": len(records),
            "totalBytes": sum(item["bytes"] for item in records),
            "files": records, "archives": archives}
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"盘点完成：{len(records)} 个文件，{data['totalBytes']} 字节，{len(archives)} 个压缩包")


if __name__ == "__main__":
    main()
