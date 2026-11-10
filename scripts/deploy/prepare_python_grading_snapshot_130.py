"""生成 1.30.90 发布制品：后端 JAR、前端归档、发布清单和更新 SQL。"""

from __future__ import annotations

import hashlib
import json
import shutil
import tarfile
from datetime import datetime, timedelta, timezone
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
RELEASE = "20261110_python_grading_snapshot_v1"
JAR = ROOT / "RuoYi-Vue/ruoyi-admin/target/ruoyi-admin.jar"
DIST = ROOT / "RuoYi-Vue3/dist"
OUT = ROOT / "output/release-20261110-python-grading-snapshot"
SQL_NAME = "platform_update_1_30_90_20261110.sql"
SQL = ROOT / "sql" / SQL_NAME


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def reset_output() -> None:
    resolved_root = ROOT.resolve()
    resolved_out = OUT.resolve()
    if resolved_out.parent != (resolved_root / "output").resolve():
        raise RuntimeError(f"发布目录越界：{resolved_out}")
    if OUT.exists():
        shutil.rmtree(OUT)
    OUT.mkdir(parents=True)


def write_frontend_archive(path: Path) -> int:
    files = sorted(item for item in DIST.rglob("*") if item.is_file())
    if not files:
        raise RuntimeError("前端 dist 为空，请先执行生产构建")
    with tarfile.open(path, "w:gz", compresslevel=9, format=tarfile.PAX_FORMAT) as archive:
        for item in files:
            relative = item.relative_to(DIST).as_posix()
            info = archive.gettarinfo(str(item), arcname=relative)
            info.mode = 0o644
            with item.open("rb") as stream:
                archive.addfile(info, stream)
    return len(files)


def main() -> int:
    if not JAR.is_file():
        raise RuntimeError(f"后端 JAR 不存在：{JAR}")
    if not (DIST / "index.html").is_file():
        raise RuntimeError(f"前端首页不存在：{DIST / 'index.html'}")
    if not SQL.is_file():
        raise RuntimeError(f"更新 SQL 不存在：{SQL}")

    reset_output()
    artifact_jar = OUT / "ruoyi-admin.jar"
    artifact_tar = OUT / "frontend.tar.gz"
    artifact_sql = OUT / SQL_NAME
    shutil.copy2(JAR, artifact_jar)
    shutil.copy2(SQL, artifact_sql)
    file_count = write_frontend_archive(artifact_tar)

    index_sha = sha256_file(DIST / "index.html")
    files = {
        artifact_jar.name: {
            "bytes": artifact_jar.stat().st_size,
            "sha256": sha256_file(artifact_jar),
        },
        artifact_tar.name: {
            "bytes": artifact_tar.stat().st_size,
            "sha256": sha256_file(artifact_tar),
        },
        artifact_sql.name: {
            "bytes": artifact_sql.stat().st_size,
            "sha256": sha256_file(artifact_sql),
        },
    }
    created_at = datetime.now(timezone(timedelta(hours=8))).isoformat(timespec="seconds")
    manifest = {
        "release": RELEASE,
        "baseRelease": "20261109_python_grading_fix_v1 + 20261109_grading_mobile_v1",
        "createdAt": created_at,
        "scope": "Python 批量采用页面快照并发保护、批改页窄屏布局与系统判题最高分稳定化",
        "databaseChanges": True,
        "sql": [SQL_NAME],
        "backendJar": {
            "name": artifact_jar.name,
            **files[artifact_jar.name],
        },
        "frontend": {
            "archive": artifact_tar.name,
            "fileCount": file_count,
            "indexSha256": index_sha,
            "archiveBytes": files[artifact_tar.name]["bytes"],
            "archiveSha256": files[artifact_tar.name]["sha256"],
        },
        "files": files,
    }
    (OUT / "manifest.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    sums = "".join(f"{item['sha256']}  {name}\n" for name, item in sorted(files.items()))
    (OUT / "SHA256SUMS").write_text(sums, encoding="utf-8", newline="\n")
    print(json.dumps(manifest, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
