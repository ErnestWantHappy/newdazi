"""发布 1.30.89 批改页窄屏适配到 130：备份、切换前端、验收和回退。"""

from __future__ import annotations

import argparse
import base64
import hashlib
import json
import posixpath
import shlex
import sys
import time
from datetime import datetime
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts" / "deploy"))
import remote130  # noqa: E402


RELEASE = "20261109_grading_mobile_v1"
VERSION = "1.30.89"
NEW_RELEASE = f"/data/apps/xueyeceping/releases/{RELEASE}"
NEW_FRONTEND = f"{NEW_RELEASE}/frontend"
OLD_RELEASE = "/data/apps/xueyeceping/releases/20261109_python_grading_fix_v1"
OLD_JAR = f"{OLD_RELEASE}/ruoyi-admin.jar"
OLD_FRONTEND = f"{OLD_RELEASE}/frontend"
OLD_JAR_SHA = "0cfb023c559ec7e4c6b5490b90405240ccd6aba8dee62e437b4edeccbbcd5c13"
OLD_INDEX_SHA = "27d47323c3216af7a45235a64000c1c55a8823d0d067c793293e00dd3bf623d7"
UNIT_PATH = "/etc/systemd/system/xueyeceping-130.service"
SITE_PATH = "/etc/nginx/sites-available/xueyeceping-130"
ENV_PATH = "/data/apps/xueyeceping/shared/xueyeceping-130.env"
BACKUP_ROOT = "/data/backups/xueyeceping"
ARTIFACT_DIR = ROOT / "output/release-20261109-grading-mobile"
MANIFEST = json.loads((ARTIFACT_DIR / "manifest.json").read_text(encoding="utf-8"))
NEW_TAR_SHA = MANIFEST["files"]["frontend.tar.gz"]["sha256"].lower()
NEW_TAR_BYTES = MANIFEST["files"]["frontend.tar.gz"]["bytes"]
NEW_INDEX_SHA = MANIFEST["frontend"]["indexSha256"].lower()
NEW_FRONTEND_FILES = MANIFEST["frontend"]["fileCount"]


class DeployError(RuntimeError):
    pass


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def run_checked(client, command: str, label: str, timeout: int = 180) -> str:
    code, out, err = remote130.run(client, command, timeout=timeout)
    if code:
        raise DeployError(f"{label}失败，退出码 {code}：{(err or out).strip()[:800]}")
    return out.strip()


def sudo_bash(client, password: str, script: str, label: str, timeout: int = 900) -> str:
    encoded = base64.b64encode(script.encode("utf-8")).decode("ascii")
    code, out, err = remote130.sudo(
        client, password, f"echo {encoded} | base64 -d | bash", timeout=timeout)
    if code:
        raise DeployError(f"{label}失败，退出码 {code}：{(err or out).strip()[:800]}")
    return out.strip()


def db_config_path(client) -> str:
    command = (
        "grep -oE -- '--spring.config.additional-location=[^ ]+' "
        f"{shlex.quote(UNIT_PATH)} | head -1 | cut -d= -f2- "
        "| sed 's/^optional:file://;s/^file://'"
    )
    value = run_checked(client, command, "读取外置配置路径")
    if value.endswith(".yml") or value.endswith(".yaml"):
        return value
    return posixpath.join(value.rstrip("/"), "application-druid.yml")


def db_query(client, password: str, sql: str, label: str, config: str) -> list[list[str]]:
    script = f"""
set -euo pipefail
cfg={shlex.quote(config)}
u=$(awk '/username:/{{print $2; exit}}' "$cfg" | tr -d '"')
p=$(awk '/password:/{{print $2; exit}}' "$cfg" | tr -d '"')
umask 077
cnf=$(mktemp)
trap 'rm -f "$cnf"' EXIT
printf '[client]\\nuser=%s\\npassword=%s\\nhost=127.0.0.1\\n' "$u" "$p" > "$cnf"
mysql --defaults-extra-file="$cnf" --batch --skip-column-names --default-character-set=utf8mb4 ry-vue <<'SQL'
{sql.strip()}
SQL
"""
    output = sudo_bash(client, password, script, label, timeout=300)
    return [line.split("\t") for line in output.splitlines() if line.strip()]


def upload_file(client, local_path: Path, remote_path: str, label: str) -> None:
    local_hash = sha256_file(local_path)
    existing = run_checked(
        client,
        f"sha256sum {shlex.quote(remote_path)} 2>/dev/null | awk '{{print $1}}' || true",
        f"读取{label}远端哈希",
    ).lower()
    if existing == local_hash:
        print(f"{label}已存在且哈希一致，跳过上传")
        return
    part = remote_path + ".part"
    sftp = client.open_sftp()
    last = -1

    def progress(done, total):
        nonlocal last
        bucket = int(done * 10 / total) if total else 10
        if bucket != last:
            last = bucket
            print(f"{label}上传 {bucket * 10}% ({done}/{total})")

    try:
        sftp.put(str(local_path), part, confirm=True, callback=progress)
    finally:
        sftp.close()
    remote_hash = run_checked(
        client, f"sha256sum {shlex.quote(part)} | awk '{{print $1}}'", f"校验{label}"
    ).lower()
    if remote_hash != local_hash:
        raise DeployError(f"{label}上传后哈希不一致")
    run_checked(client, f"mv -f {shlex.quote(part)} {shlex.quote(remote_path)}", f"落位{label}")


def current_release(client, password: str, config: str) -> dict:
    script = f"""
set -euo pipefail
site={shlex.quote(SITE_PATH)}
unit={shlex.quote(UNIT_PATH)}
root=$(grep -hE '^\\s*root\\s+.*frontend;' "$site" | tail -1 | awk '{{print $2}}' | tr -d ';')
jar=$(grep -oE '/data/apps/[^ ]+\\.jar' "$unit" | tail -1)
printf 'ROOT=%s\\nJAR=%s\\n' "$root" "$jar"
sha256sum "$root/index.html" | awk '{{print "INDEX="$1}}'
sha256sum "$jar" | awk '{{print "JAR_SHA="$1}}'
systemctl show xueyeceping-130 -p ActiveState -p SubState -p MainPID -p NRestarts \
  | sed 's/^/SERVICE_/'
"""
    output = run_checked(client, script, "读取当前正式版本")
    values = dict(line.split("=", 1) for line in output.splitlines() if "=" in line)
    rows = db_query(
        client,
        password,
        "SELECT version_no, status FROM biz_platform_update "
        "WHERE version_no IN ('1.30.88','1.30.89') ORDER BY version_no;",
        "读取产品更新状态",
        config,
    )
    return {
        "frontendRoot": values.get("ROOT"),
        "indexSha256": values.get("INDEX"),
        "jar": values.get("JAR"),
        "jarSha256": values.get("JAR_SHA"),
        "service": {
            key.removeprefix("SERVICE_"): value
            for key, value in values.items() if key.startswith("SERVICE_")
        },
        "updates": rows,
    }


def preflight(client, password: str, config: str) -> dict:
    state = current_release(client, password, config)
    if state["frontendRoot"] != OLD_FRONTEND:
        raise DeployError(f"前端根与1.30.88基线不一致：{state['frontendRoot']}")
    if state["indexSha256"] != OLD_INDEX_SHA:
        raise DeployError("线上首页与1.30.88基线不一致")
    if state["jar"] != OLD_JAR or state["jarSha256"] != OLD_JAR_SHA:
        raise DeployError("线上后端JAR与1.30.88基线不一致")
    exists = run_checked(
        client, f"test -e {shlex.quote(NEW_RELEASE)} && echo EXISTS || echo ABSENT", "检查新release目录"
    )
    if exists == "EXISTS":
        raise DeployError("新release目录已存在，禁止覆盖")
    rows = db_query(
        client,
        password,
        """
SELECT 'version89', COUNT(*) FROM biz_platform_update WHERE version_no='1.30.89';
SELECT 'version88', COUNT(*) FROM biz_platform_update WHERE version_no='1.30.88' AND status='PUBLISHED';
SELECT 'answer5', COUNT(*) FROM biz_student_answer WHERE submit_time >= NOW() - INTERVAL 5 MINUTE;
SELECT 'checkin10', COUNT(*) FROM biz_lesson_checkin WHERE checkin_time >= NOW() - INTERVAL 10 MINUTE;
SELECT 'judging', COUNT(*) FROM biz_programming_submission WHERE status_code IN ('WAITING','JUDGING');
""",
        "发布前活动与版本检查",
        config,
    )
    values = {row[0]: row[1] for row in rows if len(row) >= 2}
    print("PREFLIGHT=" + json.dumps(values, ensure_ascii=False))
    if values.get("version89") != "0" or values.get("version88") != "1":
        raise DeployError("产品更新版本状态不符合1.30.89发布前条件")
    if any(values.get(key) != "0" for key in ("answer5", "checkin10", "judging")):
        raise DeployError("检测到课堂活跃或待判题任务，停止发布")
    return {"release": state, "activity": values}


def backup(client, password: str, config: str, baseline: dict) -> dict:
    stamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    backup_dir = f"{BACKUP_ROOT}/{stamp}_{RELEASE}"
    script = f"""
set -euo pipefail
backup={shlex.quote(backup_dir)}
install -d -m 0750 -o admin2 -g admin2 "$backup"
cp -a {shlex.quote(SITE_PATH)} "$backup/nginx.before"
cp -a {shlex.quote(UNIT_PATH)} "$backup/service.before"
cp -a {shlex.quote(ENV_PATH)} "$backup/env.before"
cp -a {shlex.quote(OLD_JAR)} "$backup/jar.before"
cp -a {shlex.quote(OLD_FRONTEND + '/index.html')} "$backup/index.before.html"
cfg={shlex.quote(config)}
u=$(awk '/username:/{{print $2; exit}}' "$cfg" | tr -d '"')
p=$(awk '/password:/{{print $2; exit}}' "$cfg" | tr -d '"')
umask 077
cnf=$(mktemp)
trap 'rm -f "$cnf"' EXIT
printf '[client]\\nuser=%s\\npassword=%s\\nhost=127.0.0.1\\n' "$u" "$p" > "$cnf"
mysqldump --defaults-extra-file="$cnf" --single-transaction --quick --routines --triggers \
  --default-character-set=utf8mb4 ry-vue | gzip -9 > "$backup/database.sql.gz"
gzip -t "$backup/database.sql.gz"
zcat "$backup/database.sql.gz" | tail -1 | grep -q 'Dump completed'
bytes=$(stat -c%s "$backup/database.sql.gz")
sha=$(sha256sum "$backup/database.sql.gz" | awk '{{print $1}}')
printf 'BACKUP_DIR=%s\\nDUMP_BYTES=%s\\nDUMP_SHA256=%s\\n' "$backup" "$bytes" "$sha"
chown -R admin2:admin2 "$backup"
find "$backup" -type d -exec chmod 750 {{}} +
find "$backup" -type f -exec chmod 640 {{}} +
"""
    output = sudo_bash(client, password, script, "整库与配置备份", timeout=1800)
    values = dict(line.split("=", 1) for line in output.splitlines() if "=" in line)
    if len(values.get("DUMP_SHA256", "")) != 64 or int(values.get("DUMP_BYTES", "0")) < 10_000_000:
        raise DeployError("数据库备份校验失败")
    release = {
        "backendJar": OLD_JAR,
        "backendJarSha256": baseline["release"]["jarSha256"],
        "frontendRoot": OLD_FRONTEND,
        "frontendIndexSha256": OLD_INDEX_SHA,
    }
    encoded = base64.b64encode(
        (json.dumps(release, ensure_ascii=False, indent=2) + "\n").encode("utf-8")
    ).decode("ascii")
    sudo_bash(
        client,
        password,
        f"set -e; echo {encoded} | base64 -d > {shlex.quote(backup_dir + '/release.before.json')}; "
        f"chown admin2:admin2 {shlex.quote(backup_dir + '/release.before.json')}; "
        f"chmod 640 {shlex.quote(backup_dir + '/release.before.json')}",
        "记录发布前版本",
    )
    return {
        "dir": values["BACKUP_DIR"],
        "dumpBytes": int(values["DUMP_BYTES"]),
        "dumpSha256": values["DUMP_SHA256"],
    }


def stage(client, password: str) -> dict:
    script = f"""
set -euo pipefail
install -d -m 0755 -o admin2 -g admin2 {shlex.quote(NEW_RELEASE)} {shlex.quote(NEW_FRONTEND)}
"""
    sudo_bash(client, password, script, "创建新release目录")
    upload_file(client, ARTIFACT_DIR / "frontend.tar.gz", f"{NEW_RELEASE}/frontend.tar.gz", "前端归档")
    upload_file(client, ARTIFACT_DIR / "manifest.json", f"{NEW_RELEASE}/manifest.json", "发布清单")
    upload_file(
        client,
        ROOT / "sql/platform_update_1_30_89_20261109.sql",
        f"{NEW_RELEASE}/platform_update_1_30_89_20261109.sql",
        "更新登记SQL",
    )
    script = f"""
set -euo pipefail
cp -a {shlex.quote(OLD_FRONTEND + '/.')} {shlex.quote(NEW_FRONTEND + '/')}
tar --no-same-owner -xzf {shlex.quote(NEW_RELEASE + '/frontend.tar.gz')} -C {shlex.quote(NEW_FRONTEND)}
chown -R admin2:admin2 {shlex.quote(NEW_RELEASE)}
find {shlex.quote(NEW_RELEASE)} -type d -exec chmod 755 {{}} +
find {shlex.quote(NEW_RELEASE)} -type f -exec chmod 644 {{}} +
"""
    sudo_bash(client, password, script, "合并旧静态资源并解包新前端", timeout=900)
    index_sha = run_checked(
        client, f"sha256sum {shlex.quote(NEW_FRONTEND + '/index.html')} | awk '{{print $1}}'",
        "校验新首页",
    ).lower()
    tar_sha = run_checked(
        client, f"sha256sum {shlex.quote(NEW_RELEASE + '/frontend.tar.gz')} | awk '{{print $1}}'",
        "校验前端归档",
    ).lower()
    files = int(run_checked(
        client, f"find {shlex.quote(NEW_FRONTEND)} -type f | wc -l", "统计新前端文件"
    ))
    if index_sha != NEW_INDEX_SHA or tar_sha != NEW_TAR_SHA:
        raise DeployError("新release制品哈希与本地清单不一致")
    if files < NEW_FRONTEND_FILES:
        raise DeployError("新前端文件数少于制品文件数")
    return {"indexSha256": index_sha, "tarSha256": tar_sha, "frontendFiles": files}


def switch(client, password: str) -> None:
    script = f"""
set -euo pipefail
site={shlex.quote(SITE_PATH)}
grep -qF {shlex.quote(OLD_FRONTEND)} "$site"
sed -i {shlex.quote('s#' + OLD_FRONTEND + '#' + NEW_FRONTEND + '#g')} "$site"
grep -qF {shlex.quote(NEW_FRONTEND)} "$site"
nginx -t
systemctl reload nginx
"""
    sudo_bash(client, password, script, "切换前端并平滑重载Nginx", timeout=300)


def service_state(client) -> dict:
    output = run_checked(
        client,
        "systemctl show xueyeceping-130 -p ActiveState -p SubState -p MainPID -p NRestarts",
        "读取后端状态",
    )
    return {
        line.split("=", 1)[0]: line.split("=", 1)[1]
        for line in output.splitlines() if "=" in line
    }


def wait_healthy(client, expected_index: str, old_pid: str, label: str) -> dict:
    deadline = time.time() + 120
    last = None
    while time.time() < deadline:
        state = service_state(client)
        code, out, err = remote130.run(
            client,
            "curl -sS -o /dev/null -w 'home=%{http_code}\\n' http://127.0.0.1/; "
            "curl -sS -o /dev/null -w 'captcha=%{http_code}\\n' http://127.0.0.1/prod-api/captchaImage; "
            "curl -sS http://127.0.0.1/ | sha256sum | awk '{print \"http_index=\"$1}'; "
            f"sha256sum {shlex.quote(NEW_FRONTEND + '/index.html')} | awk '{{print \"index=\"$1}}'; "
            f"sha256sum {shlex.quote(OLD_JAR)} | awk '{{print \"jar=\"$1}}'",
            timeout=60,
        )
        values = dict(line.split("=", 1) for line in out.splitlines() if "=" in line)
        last = {**state, **values}
        ok = (
            code == 0
            and values.get("home") == "200"
            and values.get("captcha") == "200"
            and values.get("http_index") == expected_index
            and values.get("index") == expected_index
            and values.get("jar") == OLD_JAR_SHA
            and state.get("ActiveState") == "active"
            and state.get("SubState") == "running"
            and state.get("NRestarts") == "0"
            and state.get("MainPID") == old_pid
        )
        if ok:
            return last
        time.sleep(3)
    raise DeployError(f"{label}健康检查超时：{last}")


def apply_sql(client, password: str, config: str, path: str, label: str) -> str:
    encoded = base64.b64encode(Path(path).read_bytes()).decode("ascii")
    script = f"""
set -euo pipefail
cfg={shlex.quote(config)}
u=$(awk '/username:/{{print $2; exit}}' "$cfg" | tr -d '"')
p=$(awk '/password:/{{print $2; exit}}' "$cfg" | tr -d '"')
umask 077
cnf=$(mktemp)
sql=$(mktemp)
trap 'rm -f "$cnf" "$sql"' EXIT
printf '[client]\\nuser=%s\\npassword=%s\\nhost=127.0.0.1\\n' "$u" "$p" > "$cnf"
echo {encoded} | base64 -d > "$sql"
mysql --defaults-extra-file="$cnf" --default-character-set=utf8mb4 --show-warnings ry-vue < "$sql"
"""
    return sudo_bash(client, password, script, label, timeout=600)


def register_update(client, password: str, config: str) -> dict:
    output = apply_sql(
        client,
        password,
        config,
        str(ROOT / "sql/platform_update_1_30_89_20261109.sql"),
        "登记产品更新1.30.89",
    )
    print("REGISTER_OUTPUT=" + output.strip()[:800])
    rows = db_query(
        client,
        password,
        "SELECT update_id, version_no, title, status FROM biz_platform_update "
        "WHERE version_no='1.30.89';",
        "复核产品更新",
        config,
    )
    if not rows:
        raise DeployError("1.30.89产品更新未落库")
    row = rows[0]
    result = {"updateId": row[0], "versionNo": row[1], "title": row[2], "status": row[3]}
    if result["status"] != "PUBLISHED":
        raise DeployError("1.30.89产品更新状态不是PUBLISHED")
    return result


def rollback(client, password: str, backup_dir: str, old_pid: str) -> dict:
    script = f"""
set -euo pipefail
backup={shlex.quote(backup_dir)}
install -m 0644 -o root -g root "$backup/nginx.before" {shlex.quote(SITE_PATH)}
nginx -t
systemctl reload nginx
"""
    sudo_bash(client, password, script, "恢复旧Nginx配置", timeout=300)
    return wait_healthy(client, OLD_INDEX_SHA, old_pid, "回退健康检查")


def write_state(name: str, data: dict) -> None:
    path = ARTIFACT_DIR / name
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("STATE=" + str(path))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--mode", choices=["inspect", "deploy", "rollback"], default="inspect")
    parser.add_argument("--backup-dir", default="")
    args = parser.parse_args()
    jump, client, password = remote130.connect()
    try:
        config = db_config_path(client)
        print("CONFIG=" + config)
        state = current_release(client, password, config)
        print("CURRENT=" + json.dumps(state, ensure_ascii=False))
        if args.mode == "inspect":
            return 0
        old_pid = state["service"].get("MainPID", "")
        if args.mode == "rollback":
            if not args.backup_dir:
                raise DeployError("回退必须提供 --backup-dir")
            health = rollback(client, password, args.backup_dir, old_pid)
            db_query(
                client,
                password,
                "UPDATE biz_platform_update SET status='DRAFT', update_time=NOW() "
                "WHERE version_no='1.30.89' AND title='Python 批改页窄屏适配';",
                "回退产品更新状态",
                config,
            )
            write_state("rollback-state.json", {"state": "ROLLED_BACK", "health": health})
            print("ROLLBACK_OK")
            return 0

        baseline = preflight(client, password, config)
        backup_info = backup(client, password, config, baseline)
        print("BACKUP=" + json.dumps(backup_info, ensure_ascii=False))
        staged = stage(client, password)
        print("STAGED=" + json.dumps(staged, ensure_ascii=False))
        switched = False
        try:
            switched = True
            switch(client, password)
            health = wait_healthy(client, NEW_INDEX_SHA, old_pid, "新版本健康检查")
            print("HEALTH=" + json.dumps(health, ensure_ascii=False))
            update = register_update(client, password, config)
            print("UPDATE=" + json.dumps(update, ensure_ascii=False))
            write_state("remote-state.json", {
                "release": RELEASE,
                "version": VERSION,
                "backup": backup_info,
                "baseline": baseline,
                "staged": staged,
                "health": health,
                "productUpdate": update,
                "switchedAt": datetime.now().isoformat(timespec="seconds"),
            })
            print("DEPLOY_OK")
            return 0
        except Exception as error:
            print("DEPLOY_FAILED=" + str(error), file=sys.stderr)
            if switched:
                try:
                    health = rollback(client, password, backup_info["dir"], old_pid)
                    db_query(
                        client,
                        password,
                        "UPDATE biz_platform_update SET status='DRAFT', update_time=NOW() "
                        "WHERE version_no='1.30.89' AND title='Python 批改页窄屏适配';",
                        "回退未完成的产品更新状态",
                        config,
                    )
                    write_state("rollback-state.json", {
                        "state": "AUTO_ROLLED_BACK",
                        "reason": str(error),
                        "health": health,
                    })
                    print("AUTO_ROLLBACK_OK")
                except Exception as rollback_error:
                    print("AUTO_ROLLBACK_FAILED=" + str(rollback_error), file=sys.stderr)
            raise
    finally:
        client.close()
        jump.close()


if __name__ == "__main__":
    raise SystemExit(main())
