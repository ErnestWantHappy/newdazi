"""130 课例配套材料包说明精简：整库备份、幂等更新与只读复核。

只改 resource_id=2314 的教师可见说明，并登记 1.30.91 产品更新；
不改课程、题库、成绩、文件或权限。默认只读，写库必须显式 --confirm。
"""
import argparse
import base64
import json
import posixpath
import re
import shlex
import sys
from datetime import datetime
from pathlib import Path

import remote130

ROOT = Path(__file__).resolve().parents[2]
RELEASE = "20261110_case_summary_v1"
VERSION = "1.30.91"
UNIT_PATH = "/etc/systemd/system/xueyeceping-130.service"
SITE_PATH = "/etc/nginx/sites-available/xueyeceping-130"
ENV_PATH = "/data/apps/xueyeceping/shared/xueyeceping-130.env"
BACKUP_ROOT = "/data/backups/xueyeceping"
ARTIFACT_DIR = ROOT / "output" / "release-20261110-case-summary"
RESOURCE_SQL = ROOT / "sql" / "research_case_2314_short_summary_20261110.sql"
UPDATE_SQL = ROOT / "sql" / "platform_update_1_30_91_20261110.sql"
EXPECTED_TEXT = (
    "本包为“返回成语现场”课堂工具，含成语连环画创作与作品展示。"
    "解压后双击“启动课堂工具.bat”，浏览器打开学生端；教师端地址见启动窗口，"
    "需联网使用AI出图。教学设计和课件见资源列表。"
)


class DeployError(RuntimeError):
    pass


def run_checked(client, command: str, label: str, timeout: int = 180) -> str:
    code, out, err = remote130.run(client, command, timeout=timeout)
    if code:
        raise DeployError("%s失败，退出码 %s：%s" % (label, code, (err or out).strip()[:800]))
    return out.strip()


def sudo_bash(client, password: str, script: str, label: str, timeout: int = 900) -> str:
    encoded = base64.b64encode(script.encode("utf-8")).decode("ascii")
    code, out, err = remote130.sudo(
        client, password, "echo %s | base64 -d | bash" % encoded, timeout=timeout)
    if code:
        raise DeployError("%s失败，退出码 %s：%s" % (label, code, (err or out).strip()[:800]))
    return out.strip()


def db_config_path(client) -> str:
    command = (
        "grep -oE -- '--spring.config.additional-location=[^ ]+' "
        "%s | head -1 | cut -d= -f2- | sed 's/^optional:file://;s/^file://'" % shlex.quote(UNIT_PATH)
    )
    value = run_checked(client, command, "读取外置配置路径").strip()
    if value.endswith((".yml", ".yaml")):
        return value
    return posixpath.join(value.rstrip("/"), "application-druid.yml")


def db_query(client, password: str, sql: str, label: str, config: str) -> list[list[str]]:
    script = """
set -euo pipefail
cfg=%s
u=$(awk '/username:/{print $2; exit}' "$cfg" | tr -d '"')
p=$(awk '/password:/{print $2; exit}' "$cfg" | tr -d '"')
umask 077
cnf=$(mktemp)
trap 'rm -f "$cnf"' EXIT
printf '[client]\\nuser=%%s\\npassword=%%s\\nhost=127.0.0.1\\n' "$u" "$p" > "$cnf"
mysql --defaults-extra-file="$cnf" --batch --skip-column-names --default-character-set=utf8mb4 ry-vue <<'SQL'
%s
SQL
""" % (shlex.quote(config), sql.strip())
    output = sudo_bash(client, password, script, label, timeout=300)
    return [line.split("\t") for line in output.splitlines() if line]


def current_state(client, password: str, config: str) -> dict:
    rows = db_query(
        client,
        password,
        """
SELECT r.resource_id, r.case_id, r.title, c.author, c.status,
       CHAR_LENGTH(COALESCE(r.description, '')), CHAR_LENGTH(COALESCE(r.requirements, '')),
       LEFT(COALESCE(r.requirements, ''), 80)
FROM biz_textbook_case_resource r
JOIN biz_textbook_case c ON c.case_id = r.case_id
WHERE r.resource_id = 2314;
SELECT COUNT(*) FROM biz_textbook_case WHERE status = 'PUBLISHED';
SELECT COUNT(*) FROM biz_textbook_case_resource;
SELECT update_id, version_no, status FROM biz_platform_update WHERE version_no = '1.30.91';
""",
        "读取资源与发布状态",
        config,
    )
    resource = rows[0] if rows else []
    return {
        "resource": {
            "resourceId": int(resource[0]) if resource else None,
            "caseId": int(resource[1]) if resource else None,
            "title": resource[2] if resource else "",
            "author": resource[3] if resource else "",
            "caseStatus": resource[4] if resource else "",
            "descriptionLength": int(resource[5]) if resource else 0,
            "requirementsLength": int(resource[6]) if resource else 0,
            "requirementsPreview": resource[7] if len(resource) > 7 else "",
        },
        "publishedCases": int(rows[1][0]) if len(rows) > 1 else 0,
        "resources": int(rows[2][0]) if len(rows) > 2 else 0,
        "update": {
            "updateId": int(rows[3][0]) if len(rows) > 3 else None,
            "version": rows[3][1] if len(rows) > 3 else "",
            "status": rows[3][2] if len(rows) > 3 else "",
        },
    }


def service_health(client) -> dict:
    script = (
        "printf 'backend=%s\\n' \"$(systemctl is-active xueyeceping-130)\"; "
        "printf 'nginx=%s\\n' \"$(systemctl is-active nginx)\"; "
        "printf 'home=%s\\n' \"$(curl -sS -o /dev/null -w '%{http_code}' http://127.0.0.1/)\"; "
        "printf 'captcha=%s\\n' \"$(curl -sS -o /dev/null -w '%{http_code}' http://127.0.0.1/prod-api/captchaImage)\""
    )
    output = run_checked(client, script, "检查正式服务")
    return dict(line.split("=", 1) for line in output.splitlines() if "=" in line)


def backup(client, password: str, config: str) -> dict:
    stamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    backup_dir = "%s/%s_%s" % (BACKUP_ROOT, stamp, RELEASE)
    script = """
set -euo pipefail
backup=%s
install -d -m 0750 -o admin2 -g admin2 "$backup"
if [ -f %s ]; then cp -a %s "$backup/service.before"; fi
if [ -f %s ]; then cp -a %s "$backup/nginx.before"; fi
if [ -f %s ]; then cp -a %s "$backup/env.before"; fi
cfg=%s
u=$(awk '/username:/{print $2; exit}' "$cfg" | tr -d '"')
p=$(awk '/password:/{print $2; exit}' "$cfg" | tr -d '"')
umask 077
cnf=$(mktemp)
trap 'rm -f "$cnf"' EXIT
printf '[client]\\nuser=%%s\\npassword=%%s\\nhost=127.0.0.1\\n' "$u" "$p" > "$cnf"
mysql --defaults-extra-file="$cnf" --batch --skip-column-names --default-character-set=utf8mb4 \
  ry-vue -e "SELECT HEX(requirements) FROM biz_textbook_case_resource WHERE resource_id=2314" \
  > "$backup/resource-2314-before.hex"
mysqldump --defaults-extra-file="$cnf" --single-transaction --quick --routines --triggers \
  --default-character-set=utf8mb4 ry-vue | gzip -9 > "$backup/database.sql.gz"
gzip -t "$backup/database.sql.gz"
zcat "$backup/database.sql.gz" | tail -1 | grep -q 'Dump completed'
bytes=$(stat -c%%s "$backup/database.sql.gz")
sha=$(sha256sum "$backup/database.sql.gz" | awk '{print $1}')
printf 'BACKUP_DIR=%%s\\nDUMP_BYTES=%%s\\nDUMP_SHA256=%%s\\n' "$backup" "$bytes" "$sha"
chown -R admin2:admin2 "$backup"
find "$backup" -type d -exec chmod 750 {} +
find "$backup" -type f -exec chmod 640 {} +
""" % (
        shlex.quote(backup_dir),
        shlex.quote(UNIT_PATH), shlex.quote(UNIT_PATH),
        shlex.quote(SITE_PATH), shlex.quote(SITE_PATH),
        shlex.quote(ENV_PATH), shlex.quote(ENV_PATH),
        shlex.quote(config),
    )
    output = sudo_bash(client, password, script, "整库与配置备份", timeout=1800)
    values = dict(line.split("=", 1) for line in output.splitlines() if "=" in line)
    if len(values.get("DUMP_SHA256", "")) != 64 or int(values.get("DUMP_BYTES", "0")) < 10_000_000:
        raise DeployError("数据库备份校验失败")
    return {
        "dir": values["BACKUP_DIR"],
        "bytes": int(values["DUMP_BYTES"]),
        "sha256": values["DUMP_SHA256"],
    }


def apply_sql(client, password: str, config: str) -> dict:
    sql = RESOURCE_SQL.read_text(encoding="utf-8") + "\n" + UPDATE_SQL.read_text(encoding="utf-8")
    encoded = base64.b64encode(sql.encode("utf-8")).decode("ascii")
    script = """
set -euo pipefail
cfg=%s
u=$(awk '/username:/{print $2; exit}' "$cfg" | tr -d '"')
p=$(awk '/password:/{print $2; exit}' "$cfg" | tr -d '"')
umask 077
cnf=$(mktemp)
sqlfile=$(mktemp)
trap 'rm -f "$cnf" "$sqlfile"' EXIT
printf '[client]\\nuser=%%s\\npassword=%%s\\nhost=127.0.0.1\\n' "$u" "$p" > "$cnf"
printf '%%s' %s | base64 -d > "$sqlfile"
mysql --defaults-extra-file="$cnf" --default-character-set=utf8mb4 ry-vue < "$sqlfile"
""" % (shlex.quote(config), encoded)
    sudo_bash(client, password, script, "执行资源说明与产品更新 SQL", timeout=600)
    return verify_state(client, password, config)


def verify_state(client, password: str, config: str) -> dict:
    check_sql = """
SELECT requirements FROM biz_textbook_case_resource WHERE resource_id=2314;
SELECT COUNT(*) FROM biz_textbook_case_resource WHERE resource_id=2314 AND requirements=%s;
SELECT update_id, version_no, title, status FROM biz_platform_update WHERE version_no='1.30.91';
""" % ("'" + EXPECTED_TEXT.replace("'", "''") + "'")
    rows = db_query(
        client,
        password,
        check_sql,
        "复核资源说明与产品更新",
        config,
    )
    actual = rows[0][0] if rows else ""
    matched = int(rows[1][0]) if len(rows) > 1 else 0
    update = rows[2] if len(rows) > 2 else []
    if actual != EXPECTED_TEXT or matched != 1:
        raise DeployError("资源 2314 的说明未按预期更新")
    if len(update) < 4 or update[3] != "PUBLISHED":
        raise DeployError("产品更新 1.30.91 未处于 PUBLISHED")
    return {
        "requirements": actual,
        "requirementsLength": len(actual),
        "updateId": int(update[0]),
        "version": update[1],
        "title": update[2],
        "status": update[3],
    }


def rollback(client, password: str, config: str, backup_dir: str) -> dict:
    if not re.match(r"^/data/backups/xueyeceping/[0-9]{8}_[0-9]{6}_%s$" % re.escape(RELEASE), backup_dir):
        raise DeployError("回退备份目录不在允许范围")
    sftp = client.open_sftp()
    try:
        with sftp.open(posixpath.join(backup_dir, "resource-2314-before.hex"), "r") as handle:
            old_hex = handle.read().decode("ascii").strip()
    finally:
        sftp.close()
    if not old_hex or not re.fullmatch(r"[0-9A-Fa-f]+", old_hex):
        raise DeployError("备份中的旧说明无效")
    sql = """
UPDATE biz_textbook_case_resource
SET requirements=CONVERT(UNHEX('%s') USING utf8mb4)
WHERE resource_id=2314;
UPDATE biz_platform_update
SET status='DRAFT', update_time=NOW()
WHERE version_no='1.30.91' AND title='课例配套材料包说明精简';
""" % old_hex
    encoded = base64.b64encode(sql.encode("utf-8")).decode("ascii")
    script = """
set -euo pipefail
cfg=%s
u=$(awk '/username:/{print $2; exit}' "$cfg" | tr -d '"')
p=$(awk '/password:/{print $2; exit}' "$cfg" | tr -d '"')
umask 077
cnf=$(mktemp)
sqlfile=$(mktemp)
trap 'rm -f "$cnf" "$sqlfile"' EXIT
printf '[client]\\nuser=%%s\\npassword=%%s\\nhost=127.0.0.1\\n' "$u" "$p" > "$cnf"
printf '%%s' %s | base64 -d > "$sqlfile"
mysql --defaults-extra-file="$cnf" --default-character-set=utf8mb4 ry-vue < "$sqlfile"
""" % (shlex.quote(config), encoded)
    sudo_bash(client, password, script, "回退资源说明与产品更新", timeout=300)
    state = current_state(client, password, config)
    if state["resource"]["requirementsLength"] != 1045:
        raise DeployError("回退后旧说明长度不符合预期")
    return state


def write_state(name: str, data: dict) -> None:
    ARTIFACT_DIR.mkdir(parents=True, exist_ok=True)
    path = ARTIFACT_DIR / name
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("STATE=" + str(path))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--mode", choices=["inspect", "verify", "apply", "rollback"], default="inspect")
    parser.add_argument("--confirm", default="")
    parser.add_argument("--backup-dir", default="")
    args = parser.parse_args()
    jump, client, password = remote130.connect()
    try:
        config = db_config_path(client)
        print("CONFIG=" + config)
        before = current_state(client, password, config)
        health = service_health(client)
        print("BEFORE=" + json.dumps(before, ensure_ascii=False))
        print("HEALTH=" + json.dumps(health, ensure_ascii=False))
        if args.mode == "inspect":
            return 0
        if args.mode == "verify":
            state = verify_state(client, password, config)
            write_state("verify-state.json", {"state": "VERIFIED", "resource": state})
            print("VERIFY=" + json.dumps(state, ensure_ascii=False))
            print("VERIFY_OK")
            return 0
        if args.mode == "rollback":
            if not args.backup_dir:
                raise DeployError("回退必须提供 --backup-dir")
            state = rollback(client, password, config, args.backup_dir)
            write_state("rollback-state.json", {"state": "ROLLED_BACK", "resource": state})
            print("ROLLBACK_OK")
            return 0
        if args.confirm != RELEASE:
            raise DeployError("写库必须提供 --confirm %s" % RELEASE)
        resource = before["resource"]
        if resource["resourceId"] != 2314 or resource["author"] != "顾意丽" or resource["caseStatus"] != "PUBLISHED":
            raise DeployError("资源 2314 的作者或发布状态不符合预期")
        if before["update"]["version"]:
            state = verify_state(client, password, config)
            write_state("remote-state.json", {
                "release": RELEASE,
                "version": VERSION,
                "resumed": True,
                "backupDir": args.backup_dir,
                "before": before,
                "after": state,
                "health": health,
            })
            print("ALREADY_APPLIED=" + json.dumps(state, ensure_ascii=False))
            print("APPLY_OK_IDEMPOTENT")
            return 0
        if resource["requirementsLength"] <= 500:
            raise DeployError("当前说明已不是待修复的长版本，停止重复写库")
        if health.get("backend") != "active" or health.get("nginx") != "active":
            raise DeployError("正式服务未处于 active")
        backup_info = backup(client, password, config)
        print("BACKUP=" + json.dumps(backup_info, ensure_ascii=False))
        after = apply_sql(client, password, config)
        final = current_state(client, password, config)
        if final["resources"] != before["resources"] or final["publishedCases"] != before["publishedCases"]:
            raise DeployError("更新前后课例或资源总数发生变化")
        health_after = service_health(client)
        state = {
            "release": RELEASE,
            "version": VERSION,
            "backup": backup_info,
            "before": before,
            "after": after,
            "final": final,
            "healthBefore": health,
            "healthAfter": health_after,
            "appliedAt": datetime.now().isoformat(timespec="seconds"),
        }
        write_state("remote-state.json", state)
        print("AFTER=" + json.dumps(after, ensure_ascii=False))
        print("FINAL=" + json.dumps(final, ensure_ascii=False))
        print("APPLY_OK")
        return 0
    finally:
        client.close()
        jump.close()


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except DeployError as error:
        print("ERROR=" + str(error), file=sys.stderr)
        raise SystemExit(1)
