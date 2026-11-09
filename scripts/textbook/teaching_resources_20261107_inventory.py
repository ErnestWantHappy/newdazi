"""2026-11-07 教学资源批次：只读盘点来源目录并生成全量清单（不写数据库）。

来源：
  E:\\11-7\\新省版义务段信息科技新教材教学设计（宁波版）
  E:\\11-7\\义务教育信息科技课程教学指南及配套课件

输出 output/textbook-resources-20261107/source-inventory.json，字段包含相对路径、
大小、SHA-256、扩展名和顶层分类。原始目录只读，本脚本不复制、不修改任何来源文件。
"""
import hashlib
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-resources-20261107"
SOURCES = {
    "ningbo": Path(r"E:\11-7\新省版义务段信息科技新教材教学设计（宁波版）"),
    "guide": Path(r"E:\11-7\义务教育信息科技课程教学指南及配套课件"),
}


def digest(path):
    result = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            result.update(chunk)
    return result.hexdigest()


def walk(key, base):
    records = []
    for path in sorted(base.rglob("*")):
        if not path.is_file():
            continue
        relative = path.relative_to(base).as_posix()
        records.append({
            "source": key,
            "relativePath": relative,
            "bytes": path.stat().st_size,
            "sha256": digest(path),
            "suffix": path.suffix.lower(),
            "depth": relative.count("/"),
        })
    return records


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    report = {}
    for key, base in SOURCES.items():
        if not base.is_dir():
            raise SystemExit("来源目录不存在：" + str(base))
        records = walk(key, base)
        report[key] = {
            "root": str(base),
            "files": len(records),
            "bytes": sum(item["bytes"] for item in records),
            "records": records,
        }
        print("%s: %d files, %d bytes" % (key, len(records), report[key]["bytes"]))
    target = OUT / "source-inventory.json"
    target.write_text(json.dumps(report, ensure_ascii=False, indent=1), encoding="utf-8")
    print("written", target)
    return 0


if __name__ == "__main__":
    sys.exit(main())
