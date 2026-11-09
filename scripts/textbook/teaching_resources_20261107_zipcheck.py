"""核对顶层同名 ZIP 与已解压目录的条目差异（只读）。

输出每个 ZIP 独有、磁盘独有的条目清单，用于判断是否还有未解压的独有资料。
"""
import json
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-resources-20261107"
PAIRS = [
    (Path(r"E:\11-7\义务教育信息科技课程教学指南及配套课件.zip"),
     Path(r"E:\11-7\义务教育信息科技课程教学指南及配套课件")),
    (Path(r"E:\11-7\新省版义务段信息科技新教材教学设计（宁波版）.zip"),
     Path(r"E:\11-7\新省版义务段信息科技新教材教学设计（宁波版）")),
]


def main():
    report = {}
    for archive, folder in PAIRS:
        names = set()
        with zipfile.ZipFile(archive) as stream:
            for info in stream.infolist():
                if info.is_dir():
                    continue
                names.add((info.filename.replace("\\", "/"), info.file_size))
        disk = set()
        for path in folder.rglob("*"):
            if path.is_file():
                disk.add((path.relative_to(folder).as_posix(), path.stat().st_size))
        tops = {name.split("/")[0] for name, _ in names}
        stripped = names
        if len(tops) == 1:
            # 解压目录本身不带顶层文件夹，压缩包带同名顶层目录时统一剥掉再比较。
            prefix = next(iter(tops)) + "/"
            if all(name.startswith(prefix) for name, _ in names):
                stripped = {(name[len(prefix):], size) for name, size in names}
        only_zip = sorted(stripped - disk)
        only_disk = sorted(disk - stripped)
        report[archive.name] = {
            "zipEntries": len(names),
            "diskFiles": len(disk),
            "onlyZip": [{"name": n, "bytes": s} for n, s in only_zip[:200]],
            "onlyZipCount": len(only_zip),
            "onlyDiskCount": len(only_disk),
            "onlyDiskSample": [{"name": n, "bytes": s} for n, s in only_disk[:20]],
        }
        print(archive.name, "zip", len(names), "disk", len(disk),
              "onlyZip", len(only_zip), "onlyDisk", len(only_disk))
        for item in only_zip[:10]:
            print("  ZIP-ONLY", item)
    target = OUT / "zip-diff.json"
    target.write_text(json.dumps(report, ensure_ascii=False, indent=1), encoding="utf-8")
    print("written", target)


if __name__ == "__main__":
    main()
