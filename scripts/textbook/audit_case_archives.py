"""只读检查ZIP内容、说明与发布风险，不解压或执行程序，不打印配置正文。"""
import json
import re
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases"


def main():
    data = json.loads((OUT / "inventory.json").read_text(encoding="utf-8"))
    source = Path(data["sourceRoot"])
    report = []
    for path in source.rglob("*.zip"):
        with zipfile.ZipFile(path) as archive:
            names = archive.namelist()
            suspicious = [name for name in names if re.search(r"(?:^|/)(?:\.env|[^/]*\.pem|[^/]*\.(?:db|sqlite3|db-wal))$", name, re.I)]
            traversal = [name for name in names if ".." in Path(name.replace("\\", "/")).parts or name.startswith(("/", "\\"))]
            root_docs = [name for name in names if name.lower().endswith("readme.md") and name.count("/") <= 1]
            snippets = []
            for name in root_docs:
                content = archive.read(name).decode("utf-8", errors="strict")
                snippets.extend(line[:220] for line in content.splitlines() if re.search(r"Windows|Python|安装|启动|requirements|runtime|teacher.txt", line, re.I))
            record = {"source": path.relative_to(source).as_posix(), "entries": len(names),
                "bytes": path.stat().st_size, "suspiciousCount": len(suspicious), "traversalCount": len(traversal),
                "hasRuntime": any("/runtime/" in name for name in names),
                "hasTeacherAddress": any(name.endswith("/teacher.txt") for name in names),
                "readmeInstructions": snippets[:24]}
            report.append(record)
            print(json.dumps(record, ensure_ascii=False), flush=True)
    (OUT / "archive-audit.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")


if __name__ == "__main__":
    main()
