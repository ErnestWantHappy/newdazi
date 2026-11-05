"""只读核对课例来源里的压缩包/安装包，标记凭据、数据库与运行时，供收录决策。

只读条目名与统计，不解压到磁盘、不执行其中任何程序、不打印配置正文。
"""
import json
import re
import tarfile
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases"
SOURCE = Path(json.loads((OUT / "inventory.json").read_text(encoding="utf-8"))["sourceRoot"])

ENV_PATTERN = re.compile(r"(?:^|/)\.env(?:\.[^/]*)?$", re.I)
DB_PATTERN = re.compile(r"\.(?:db|sqlite|sqlite3|db-wal|db-shm)$", re.I)
KEY_PATTERN = re.compile(r"\.(?:pem|key|pfx|p12|jks)$", re.I)
SECRET_WORD = re.compile(r"(?:secret|token|password|apikey|api_key|credential)", re.I)
STUDENT_WORD = re.compile(r"(?:学生|名单|花名册|成绩|姓名|学号)")


def flags(names):
    lowered = [name.lower() for name in names]
    return {
        "envFiles": [name for name in names if ENV_PATTERN.search(name)],
        "dbFiles": [name for name in names if DB_PATTERN.search(name)],
        "keyFiles": [name for name in names if KEY_PATTERN.search(name)],
        "logFiles": sum(1 for name in lowered if name.endswith(".log")),
        "backupFiles": sum(1 for name in lowered if ".bak" in name or "_backup" in name or "wppsnapshot" in name),
        "runtimeBins": sum(1 for name in lowered if name.endswith((".exe", ".dll"))),
        "nodeModules": any("node_modules/" in name for name in lowered),
        "traversal": sum(1 for name in names if ".." in Path(name.replace("\\", "/")).parts or name.startswith(("/", "\\"))),
        "studentNamed": [name for name in names if STUDENT_WORD.search(name)][:10],
        "secretNamed": [name for name in names if SECRET_WORD.search(name)][:10],
    }


def describe(path, names, total_bytes):
    top = sorted({name.split("/")[0] for name in names if name.strip("/")})
    readmes = [name for name in names if re.search(r"(?:^|/)(?:readme|说明)[^/]*$", name, re.I)]
    record = {"source": path.relative_to(SOURCE).as_posix(), "bytes": path.stat().st_size,
              "entries": len(names), "uncompressedBytes": total_bytes, "topLevel": top[:12],
              "readme": readmes[:5]}
    record.update(flags(names))
    return record


def main():
    included = json.loads((OUT / "catalog-local-reviewed.json").read_text(encoding="utf-8"))
    included_sources = {resource["source"] for case in included["cases"] for resource in case["resources"]}
    report = []
    for path in sorted(SOURCE.rglob("*")):
        if not path.is_file() or path.suffix.lower() not in {".zip", ".tar", ".rar", ".7z", ".exe", ".msi"}:
            continue
        relative = path.relative_to(SOURCE).as_posix()
        record = {"source": relative, "bytes": path.stat().st_size, "kind": path.suffix.lower().lstrip("."),
                  "included": relative in included_sources}
        try:
            if path.suffix.lower() == ".zip":
                with zipfile.ZipFile(path) as archive:
                    infos = archive.infolist()
                    record.update(describe(path, [info.filename for info in infos], sum(info.file_size for info in infos)))
            elif path.suffix.lower() == ".tar":
                with tarfile.open(path) as archive:
                    members = archive.getmembers()
                    record.update(describe(path, [member.name for member in members], sum(member.size for member in members)))
            else:
                # rar/7z 与安装包不做二进制解析，只登记体积与类型，留给人工决定。
                record["note"] = "安装包" if path.suffix.lower() in {".exe", ".msi"} else "需人工解压核对"
        except Exception as error:
            record["note"] = type(error).__name__
        report.append(record)
        print(f"{record['bytes']/1048576:8.1f}MiB {record['kind']:4s} included={record['included']} {relative}")

    (OUT / "package-audit.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    risky = [record for record in report if record.get("envFiles") or record.get("dbFiles") or record.get("keyFiles")]
    print(f"共{len(report)}个包，含凭据/数据库特征{len(risky)}个：")
    for record in risky:
        print(" ", record["source"], "env", len(record.get("envFiles", [])), "db", len(record.get("dbFiles", [])),
              "key", len(record.get("keyFiles", [])))


if __name__ == "__main__":
    main()
