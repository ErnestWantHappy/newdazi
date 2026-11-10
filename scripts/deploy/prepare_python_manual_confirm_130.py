"""生成 1.30.92 发布制品：后端 JAR、前端归档、发布清单和更新 SQL。"""

from __future__ import annotations

import hashlib
import json
import shutil
import tarfile
from datetime import datetime, timedelta, timezone
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
RELEASE = "20261110_python_manual_confirm_v1"
JAR = ROOT / "RuoYi-Vue/ruoyi-admin/target/ruoyi-admin.jar"
DIST = ROOT / "RuoYi-Vue3/dist"
OUT = ROOT / "output/release-20261110-python-manual-confirm"
SQL_NAME = "platform_update_1_30_92_20261110.sql"
SQL = ROOT / "sql" / SQL_NAME


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def reset_output() -> None:
    output_root = (ROOT / "output").resolve()
    resolved = OUT.resolve()
    if resolved.parent != output_root:
        raise RuntimeError(f"发布目录越界：{resolved}")
    if OUT.exists():
        shutil.rmtree(OUT)
    OUT.mkdir(parents=True)


def write_frontend_archive(path: Path) -> int:
    files = sorted(item for item in DIST.rglob("*") if item.is_file())
    if not files:
        raise RuntimeError("前端 dist 为空，请先执行生产构建")
    with tarfile.open(path, "w:gz", compresslevel=9, format=tarfile.PAX_FORMAT) as archive:
        for item in files:
            info = archive.gettarinfo(str(item), arcname=item.relative_to(DIST).as_posix())
            info.mode = 0o644
            with item.open("rb") as stream:
                archive.addfile(info, stream)
    return len(files)


def main() -> int:
    if not JAR.is_file() or not (DIST / "index.html").is_file() or not SQL.is_file():
        raise RuntimeError("JAR、前端首页或更新SQL不存在")
    reset_output()
    jar_path = OUT / "ruoyi-admin.jar"
    frontend_path = OUT / "frontend.tar.gz"
    sql_path = OUT / SQL_NAME
    shutil.copy2(JAR, jar_path)
    shutil.copy2(SQL, sql_path)
    file_count = write_frontend_archive(frontend_path)
    files = {
        jar_path.name: {"bytes": jar_path.stat().st_size, "sha256": sha256_file(jar_path)},
        frontend_path.name: {
            "bytes": frontend_path.stat().st_size,
            "sha256": sha256_file(frontend_path),
        },
        sql_path.name: {"bytes": sql_path.stat().st_size, "sha256": sha256_file(sql_path)},
    }
    manifest = {
        "release": RELEASE,
        "baseRelease": "20261110_python_grading_snapshot_v1",
        "createdAt": datetime.now(timezone(timedelta(hours=8))).isoformat(timespec="seconds"),
        "scope": "Python批改教师确认制、五星打分与运行查看",
        "databaseChanges": True,
        "sql": [SQL_NAME],
        "backendJar": {"name": jar_path.name, **files[jar_path.name]},
        "frontend": {
            "archive": frontend_path.name,
            "fileCount": file_count,
            "indexSha256": sha256_file(DIST / "index.html"),
            "archiveBytes": files[frontend_path.name]["bytes"],
            "archiveSha256": files[frontend_path.name]["sha256"],
        },
        "files": files,
    }
    (OUT / "manifest.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    (OUT / "SHA256SUMS").write_text(
        "".join(f"{item['sha256']}  {name}\n" for name, item in sorted(files.items())),
        encoding="utf-8", newline="\n")
    print(json.dumps(manifest, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
