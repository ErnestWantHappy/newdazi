"""只读核对压缩包内的配置与数据库，判断是否含真实凭据或学生数据。

为避免二次泄露：只记录键名、是否为空/占位、表名与行数，绝不输出取值正文。
"""
import json
import re
import sqlite3
import tarfile
import tempfile
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases"
SOURCE = Path(json.loads((OUT / "inventory.json").read_text(encoding="utf-8"))["sourceRoot"])
# 已发布副本（清理过 teacher.txt 的神经网络工具包）也要复核。
INCLUDED_COPY = OUT / "release-materials/神经网络实验工具集-课前配置版.zip"

PLACEHOLDER = re.compile(r"^(?:|your|xxx|changeme|todo|placeholder|<.*>|\*+|test)$", re.I)


def read_member(archive, name):
    with archive.open(name) as stream:
        return stream.read(65536).decode("utf-8", errors="replace")


def env_report(content):
    keys = []
    risky = []
    for line in content.splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        key = key.strip().lower()
        value = value.strip().strip('"').strip("'")
        keys.append(key)
        if value and not PLACEHOLDER.match(value) and re.search(r"key|secret|token|password|passwd|pwd", key):
            risky.append(key)
    return {"keys": sorted(set(keys))[:24], "nonEmptySecretKeys": sorted(set(risky))[:12]}


def code_report(content):
    hits = re.findall(r"^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=\s*[\"']([^\"']{12,})[\"']", content, re.M)
    return {"literalAssignments": len(hits), "names": sorted({name for name, _ in hits})[:12]}


def sqlite_report(path):
    connection = sqlite3.connect("file:" + str(path).replace("\\", "/") + "?mode=ro&immutable=1", uri=True)
    try:
        tables = connection.execute("SELECT name FROM sqlite_master WHERE type='table'").fetchall()
        detail = []
        for (name,) in tables[:12]:
            count = connection.execute('SELECT COUNT(*) FROM "' + name.replace('"', '""') + '"').fetchone()[0]
            columns = [row[1] for row in connection.execute('PRAGMA table_info("' + name.replace('"', '""') + '")')]
            detail.append({"table": name, "rows": count,
                           "studentLikeColumns": [column for column in columns if re.search(r"姓名|学号|学生|name|student", column, re.I)][:6]})
        return detail
    finally:
        connection.close()


def audit_zip(path, entries, result):
    with zipfile.ZipFile(path) as archive:
        names = set(archive.namelist())
        envs = [name for name in names if re.search(r"(?:^|/)\.env(?:\.example)?$", name, re.I)]
        dbs = [name for name in names if name.lower().endswith((".db", ".sqlite", ".sqlite3"))]
        codes = [name for name in names if re.search(r"(?:secret|config)\.py$", name, re.I)]
        result["env"] = {}
        for name in envs[:3]:
            result["env"][name] = env_report(read_member(archive, name))
        result["code"] = {}
        for name in codes[:3]:
            result["code"][name] = code_report(read_member(archive, name))
        result["databases"] = {}
        for name in dbs[:3]:
            with tempfile.TemporaryDirectory(prefix="case-db-") as folder:
                target = Path(folder) / "sample.db"
                target.write_bytes(archive.read(name))
                try:
                    result["databases"][name] = sqlite_report(target)
                except Exception as error:
                    result["databases"][name] = type(error).__name__


def audit_tar(path, result):
    with tarfile.open(path) as archive:
        members = [member for member in archive.getmembers() if member.isfile()]
        names = {member.name for member in members}
        envs = [name for name in names if re.search(r"(?:^|/)\.env(?:\.example)?$", name, re.I)]
        dbs = [name for name in names if name.lower().endswith((".db", ".sqlite"))]
        result["env"] = {}
        for name in envs[:3]:
            stream = archive.extractfile(name)
            if stream:
                result["env"][name] = env_report(stream.read(65536).decode("utf-8", errors="replace"))
        result["databases"] = {}
        for name in dbs[:3]:
            stream = archive.extractfile(name)
            if not stream:
                continue
            with tempfile.TemporaryDirectory(prefix="case-db-") as folder:
                target = Path(folder) / "sample.db"
                target.write_bytes(stream.read())
                try:
                    result["databases"][name] = sqlite_report(target)
                except Exception as error:
                    result["databases"][name] = type(error).__name__


def main():
    targets = ["七年级/七年级上册/江北区-第18课《生成式人工智能》/课堂包.tar",
               "七年级/七年级下册/象山县-第5课《人工智能物联网》/源代码.zip",
               "四年级/四年级下册/慈溪市-第17课《手写数字的识别与应用》/第17课《手写数字的识别与应用》配套资源.zip"]
    report = []
    for relative in targets:
        path = SOURCE / relative
        if not path.is_file():
            continue
        record = {"source": relative, "bytes": path.stat().st_size}
        try:
            if path.suffix.lower() == ".tar":
                audit_tar(path, record)
            else:
                audit_zip(path, None, record)
        except Exception as error:
            record["error"] = type(error).__name__
        report.append(record)
        print(json.dumps({"source": relative,
                          "envKeys": {name: value["keys"] for name, value in record.get("env", {}).items()},
                          "riskySecretKeys": {name: value["nonEmptySecretKeys"] for name, value in record.get("env", {}).items()},
                          "code": record.get("code"),
                          "databases": record.get("databases")}, ensure_ascii=False), flush=True)

    if INCLUDED_COPY.is_file():
        with zipfile.ZipFile(INCLUDED_COPY) as archive:
            names = archive.namelist()
            record = {"source": "已发布副本/" + INCLUDED_COPY.name,
                      "teacherTxt": [name for name in names if name.lower().endswith("teacher.txt")],
                      "tokenFiles": [name for name in names if re.search(r"aau_token|\.npmrc$|pip\.conf$", name, re.I)],
                      "envFiles": [name for name in names if re.search(r"(?:^|/)\.env", name, re.I)],
                      "dbFiles": [name for name in names if name.lower().endswith((".db", ".sqlite"))]}
            for name in record["tokenFiles"][:2]:
                record.setdefault("tokenSamples", {})[name] = len(read_member(archive, name).strip())
            report.append(record)
            print(json.dumps(record, ensure_ascii=False), flush=True)

    (OUT / "package-secrets.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")


if __name__ == "__main__":
    main()
