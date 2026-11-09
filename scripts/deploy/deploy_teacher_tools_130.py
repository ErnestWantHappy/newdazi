"""教师工具 1.30.80 的 130 安全发布脚本。

只发布本次隔离构建的后端 JAR 与 teacher_tools_search_v2.sql，不切换前端，
不上传工作区其他未发布改动。数据库先整库备份，后端切换失败时自动恢复旧 unit。
"""
import argparse
import base64
import hashlib
import json
import posixpath
import re
import shlex
import shutil
import sys
import time
from datetime import datetime
from pathlib import Path

import remote130


ROOT = Path(__file__).resolve().parents[2]
RELEASE_NAME = "20261108_teacher_tools_v1"
REMOTE_APP_ROOT = "/data/apps/xueyeceping"
REMOTE_RELEASE = posixpath.join(REMOTE_APP_ROOT, "releases", RELEASE_NAME)
REMOTE_BACKUP_ROOT = "/data/backups/xueyeceping"
UNIT_PATH = "/etc/systemd/system/xueyeceping-130.service"
DB_NAME = "ry-vue"
DB_CONFIG = (
    "/data/apps/xueyeceping/releases/20261008_seat_install_v1/"
    "config/application-druid.yml"
)
LOCAL_JAR = (
    ROOT
    / "output/release-worktrees/20261108_teacher_tools_v1"
    / "RuoYi-Vue/ruoyi-admin/target/ruoyi-admin.jar"
)
LOCAL_SQL = ROOT / "sql/teacher_tools_search_v2.sql"
ARTIFACT_DIR = ROOT / "output/release-artifacts" / RELEASE_NAME
EXPECTED_TASK_CODES = {
    "programming",
    "experiment",
    "resources",
    "material",
    "class-affairs",
}


class DeployError(RuntimeError):
    pass


def sha256_file(path):
    digest = hashlib.sha256()
    with Path(path).open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def run_checked(client, command, label, timeout=300, quiet=False):
    code, out, err = remote130.run(client, command, timeout=timeout)
    safe_out = out.strip()
    safe_err = err.strip()
    if safe_out and not quiet:
        print(safe_out)
    if safe_err:
        print(safe_err, file=sys.stderr)
    if code != 0:
        raise DeployError("%s 失败，退出码 %s" % (label, code))
    return safe_out


def sudo_checked(client, password, command, label, timeout=300):
    code, out, err = remote130.sudo(client, password, command, timeout=timeout)
    safe_out = out.strip()
    safe_err = err.strip()
    if safe_out:
        print(safe_out)
    if safe_err:
        print(safe_err, file=sys.stderr)
    if code != 0:
        raise DeployError("%s 失败，退出码 %s" % (label, code))
    return safe_out


def run_base64_checked(client, script, label, timeout=300, password=None):
    encoded = base64.b64encode(script.encode("utf-8")).decode("ascii")
    command = "echo %s | base64 -d | bash" % encoded
    if password is None:
        return run_checked(client, command, label, timeout=timeout)
    return sudo_checked(client, password, command, label, timeout=timeout)


def db_prefix():
    return r"""
set -euo pipefail
cfg=%s
u=$(awk '/username:/{print $2; exit}' "$cfg" | tr -d '"')
p=$(awk '/password:/{print $2; exit}' "$cfg" | tr -d '"')
umask 077
cnf=$(mktemp)
trap 'rm -f "$cnf"' EXIT
printf '[client]\nuser=%%s\npassword=%%s\nhost=127.0.0.1\n' "$u" "$p" > "$cnf"
mysql --defaults-extra-file="$cnf" --default-character-set=utf8mb4 -N -B %s <<'SQL'
""" % (shlex.quote(DB_CONFIG), shlex.quote(DB_NAME))


def db_query(client, sql, label):
    script = db_prefix() + sql.strip() + "\nSQL\n"
    encoded = base64.b64encode(script.encode("utf-8")).decode("ascii")
    out = run_checked(client, "echo %s | base64 -d | bash" % encoded, label, quiet=True)
    return [line.split("\t") for line in out.splitlines() if line.strip()]


def inspect(client, password):
    commands = {
        "时间/主机": "hostname; date '+%F %T %Z'; uptime",
        "服务": (
            "systemctl show xueyeceping-130 -p ActiveState -p SubState "
            "-p MainPID -p NRestarts -p WorkingDirectory -p ExecStart; "
            "systemctl status xueyeceping-130 --no-pager -n 3 || true"
        ),
        "当前制品": (
            "jar=$(grep -oE '/data/apps/[^ ]+\\.jar' %s | head -1); "
            "echo \"jar=$jar\"; [ -f \"$jar\" ] && sha256sum \"$jar\"; "
            "root=$(awk '/root .*frontend;/{print $2; exit}' "
            "/etc/nginx/sites-available/xueyeceping-130); "
            "root=${root%%;}; "
            "echo \"frontend=$root\"; [ -f \"$root/index.html\" ] && "
            "sha256sum \"$root/index.html\""
        ) % shlex.quote(UNIT_PATH),
        "旧release结构": (
            "find /data/apps/xueyeceping/releases/20261107_teaching_resources_v1 "
            "-maxdepth 2 -printf '%y %m %u:%g %p\\n' | sort | head -40"
        ),
        "备份/磁盘": (
            "ls -ld /data/backups /data/backups/xueyeceping 2>/dev/null || true; "
            "df -h / /data; free -h"
        ),
        "已有本次目录": (
            "if [ -e %s ]; then find %s -maxdepth 2 -printf '%%y %%p\\n' | head; "
            "else echo 'NOT_EXISTS'; fi"
        )
        % (shlex.quote(REMOTE_RELEASE), shlex.quote(REMOTE_RELEASE)),
        "数据库基础": (
            "SELECT 'tools', COUNT(*) FROM biz_teacher_tool WHERE del_flag='0'\n"
            "UNION ALL SELECT 'categories', COUNT(*) FROM biz_teacher_tool_category "
            "WHERE del_flag='0'\n"
            "UNION ALL SELECT 'recommended', COUNT(*) FROM biz_teacher_tool "
            "WHERE del_flag='0' AND is_recommended='Y'\n"
            "UNION ALL SELECT 'recommended_without_enabled_category', COUNT(*) "
            "FROM biz_teacher_tool t WHERE t.del_flag='0' AND t.is_recommended='Y' "
            "AND NOT EXISTS (SELECT 1 FROM biz_teacher_tool_category_rel r "
            "JOIN biz_teacher_tool_category c ON c.category_id=r.category_id "
            "WHERE r.tool_id=t.tool_id AND c.status='0' AND c.del_flag='0')\n"
            "UNION ALL SELECT 'rel_rows', COUNT(*) FROM biz_teacher_tool_category_rel;"
        ),
    }
    for name, command in commands.items():
        print("----", name)
        if name == "数据库基础":
            for row in db_query(client, command, name):
                print("\t".join(row))
        else:
            run_checked(client, command, name, timeout=120)


def remote_backup(client, password, backup_dir):
    sudo_checked(
        client,
        password,
        "install -d -m 0700 -o admin2 -g admin2 %s" % shlex.quote(backup_dir),
        "创建备份目录",
    )
    dump_file = posixpath.join(backup_dir, "database.sql.gz")
    dump_sha_file = dump_file + ".sha256"
    script = r"""
set -euo pipefail
cfg=%s
u=$(awk '/username:/{print $2; exit}' "$cfg" | tr -d '"')
p=$(awk '/password:/{print $2; exit}' "$cfg" | tr -d '"')
out=%s
umask 077
cnf=$(mktemp)
trap 'rm -f "$cnf"' EXIT
printf '[client]\nuser=%%s\npassword=%%s\nhost=127.0.0.1\n' "$u" "$p" > "$cnf"
mysqldump --defaults-extra-file="$cnf" --single-transaction --quick \
  --routines --triggers --events --default-character-set=utf8mb4 %s \
  | gzip -1 > "$out"
gzip -t "$out"
sha256sum "$out" | tee %s
stat -c '%%n %%s bytes' "$out"
""" % (
        shlex.quote(DB_CONFIG),
        shlex.quote(dump_file),
        shlex.quote(DB_NAME),
        shlex.quote(dump_sha_file),
    )
    run_base64_checked(client, script, "整库备份", timeout=1800)
    unit_backup = posixpath.join(backup_dir, "xueyeceping-130.service.before")
    sudo_checked(
        client,
        password,
        "cp -a %s %s" % (shlex.quote(UNIT_PATH), shlex.quote(unit_backup)),
        "备份 systemd unit",
    )
    info = run_checked(
        client,
        "sha256sum %s %s; cat %s"
        % (shlex.quote(dump_file), shlex.quote(unit_backup), shlex.quote(unit_backup)),
        "读取备份信息",
    )
    dump_hash = run_checked(
        client, "sha256sum %s | awk '{print $1}'" % shlex.quote(dump_file), "备份哈希"
    ).strip()
    if not re.fullmatch(r"[0-9a-f]{64}", dump_hash):
        raise DeployError("整库备份哈希异常")
    return {
        "dir": backup_dir,
        "dump": dump_file,
        "dump_sha256": dump_hash,
        "unit": unit_backup,
        "info": info,
    }


def upload_file(client, local_path, remote_path, label):
    local_size = Path(local_path).stat().st_size
    local_hash = sha256_file(local_path)
    # 续发时远端往往已是同一份文件（上次失败在切换阶段），哈希一致就跳过重复上传。
    existing_hash = run_checked(
        client,
        "sha256sum %s 2>/dev/null | awk '{print $1}'" % shlex.quote(remote_path),
        label + "现有哈希",
        quiet=True,
    ).strip()
    if existing_hash.lower() == local_hash.lower():
        print("%s 远端已是最新，跳过上传" % label)
        return {"size": local_size, "sha256": local_hash}
    remote_tmp = remote_path + ".part"
    sftp = client.open_sftp()
    last_bucket = {"value": -1}

    def progress(done, total):
        bucket = int(done * 10 / total) if total else 10
        if bucket != last_bucket["value"]:
            last_bucket["value"] = bucket
            print("%s 上传 %d%% (%d/%d bytes)" % (label, bucket * 10, done, total))

    try:
        sftp.put(str(local_path), remote_tmp, confirm=True, callback=progress)
        # 续发时目标文件已存在；优先用 POSIX rename 原子覆盖，服务器不支持时
        # 再退化为“先删旧文件再改名”，避免 SFTP v3 对已存在目标直接报 Failure。
        try:
            sftp.posix_rename(remote_tmp, remote_path)
        except (IOError, OSError, AttributeError):
            try:
                sftp.remove(remote_path)
            except (IOError, OSError):
                pass
            sftp.rename(remote_tmp, remote_path)
    finally:
        sftp.close()
    remote_size = int(
        run_checked(client, "stat -c %%s %s" % shlex.quote(remote_path), label + "大小").strip()
    )
    remote_hash = run_checked(
        client, "sha256sum %s | awk '{print $1}'" % shlex.quote(remote_path), label + "哈希"
    ).strip()
    if remote_size != local_size or remote_hash != local_hash:
        raise DeployError(
            "%s 校验失败：本地/远端大小或哈希不一致" % label
        )
    return {"size": local_size, "sha256": local_hash}


def prepare_artifacts():
    if not LOCAL_JAR.is_file():
        raise DeployError("缺少后端 JAR：%s" % LOCAL_JAR)
    if not LOCAL_SQL.is_file():
        raise DeployError("缺少增量 SQL：%s" % LOCAL_SQL)
    ARTIFACT_DIR.mkdir(parents=True, exist_ok=True)
    manifest = {
        "release": RELEASE_NAME,
        "createdAt": datetime.now().astimezone().isoformat(timespec="seconds"),
        "backendJar": LOCAL_JAR.name,
        "backendJarSha256": sha256_file(LOCAL_JAR),
        "backendJarBytes": LOCAL_JAR.stat().st_size,
        "sql": LOCAL_SQL.name,
        "sqlSha256": sha256_file(LOCAL_SQL),
        "frontend": "unchanged",
        "frontendRoot": "/data/apps/xueyeceping/releases/20261108_teaching_label_v1/frontend",
        "scope": "teacher-tools only",
    }
    (ARTIFACT_DIR / "manifest.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    shutil.copyfile(LOCAL_SQL, ARTIFACT_DIR / LOCAL_SQL.name)
    return manifest


def precheck_sql(client):
    sql = """
SELECT 'base_tables', COUNT(*) FROM information_schema.tables
 WHERE table_schema=DATABASE()
   AND table_name IN ('biz_teacher_tool','biz_teacher_tool_category','biz_teacher_tool_category_rel');
SELECT 'new_columns', COUNT(*) FROM information_schema.columns
 WHERE table_schema=DATABASE() AND table_name='biz_teacher_tool'
   AND column_name IN ('aliases','task_tags');
SELECT 'new_tables', COUNT(*) FROM information_schema.tables
 WHERE table_schema=DATABASE()
   AND table_name IN ('biz_teacher_tool_task','biz_teacher_tool_term');
SELECT 'tools', COUNT(*) FROM biz_teacher_tool WHERE del_flag='0';
SELECT 'recommended_without_enabled_category', COUNT(*)
 FROM biz_teacher_tool t
 WHERE t.del_flag='0' AND t.is_recommended='Y'
   AND NOT EXISTS (
     SELECT 1 FROM biz_teacher_tool_category_rel r
     JOIN biz_teacher_tool_category c ON c.category_id=r.category_id
     WHERE r.tool_id=t.tool_id AND c.status='0' AND c.del_flag='0');
SELECT 'duplicate_relations', COUNT(*) FROM (
 SELECT tool_id, category_id FROM biz_teacher_tool_category_rel
 GROUP BY tool_id, category_id HAVING COUNT(*) > 1) d;
SELECT 'duplicate_terms', 0;
"""
    rows = db_query(client, sql, "SQL 前检")
    values = {row[0]: row[1] for row in rows if len(row) >= 2}
    for key, value in values.items():
        print("%s=%s" % (key, value))
    if values.get("base_tables") != "3":
        raise DeployError("原表不完整，停止执行 SQL")
    if values.get("recommended_without_enabled_category") != "0":
        raise DeployError("存在精选工具没有启用分类，停止执行 SQL")
    if values.get("duplicate_relations") != "0":
        raise DeployError("工具分类关系存在重复，停止执行 SQL")
    return values


def apply_sql(client, remote_sql):
    # mysql 的 SOURCE 只用于客户端，不能放进 heredoc，因此直接走文件重定向。
    script = r"""
set -euo pipefail
cfg=%s
u=$(awk '/username:/{print $2; exit}' "$cfg" | tr -d '"')
p=$(awk '/password:/{print $2; exit}' "$cfg" | tr -d '"')
umask 077
cnf=$(mktemp)
trap 'rm -f "$cnf"' EXIT
printf '[client]\nuser=%%s\npassword=%%s\nhost=127.0.0.1\n' "$u" "$p" > "$cnf"
mysql --defaults-extra-file="$cnf" --default-character-set=utf8mb4 --show-warnings %s < %s
""" % (
        shlex.quote(DB_CONFIG),
        shlex.quote(DB_NAME),
        shlex.quote(remote_sql),
    )
    run_base64_checked(client, script, "执行增量 SQL", timeout=900)


def postcheck_sql(client):
    sql = """
SELECT 'columns', GROUP_CONCAT(column_name ORDER BY column_name)
 FROM information_schema.columns
 WHERE table_schema=DATABASE() AND table_name='biz_teacher_tool'
   AND column_name IN ('aliases','task_tags');
SELECT 'tables', GROUP_CONCAT(table_name ORDER BY table_name)
 FROM information_schema.tables
 WHERE table_schema=DATABASE()
   AND table_name IN ('biz_teacher_tool_task','biz_teacher_tool_term');
SELECT 'tasks', COUNT(*) FROM biz_teacher_tool_task WHERE del_flag='0' AND status='0';
SELECT 'terms', COUNT(*) FROM biz_teacher_tool_term;
SELECT 'tools_with_aliases', COUNT(*) FROM biz_teacher_tool
 WHERE del_flag='0' AND aliases IS NOT NULL AND aliases <> '';
SELECT 'tools_with_tasks', COUNT(*) FROM biz_teacher_tool
 WHERE del_flag='0' AND task_tags IS NOT NULL AND task_tags <> '';
SELECT 'duplicate_terms', COUNT(*) FROM (
 SELECT term, tool_id FROM biz_teacher_tool_term
 GROUP BY term, tool_id HAVING COUNT(*) > 1) d;
SELECT 'duplicate_relations', COUNT(*) FROM (
 SELECT tool_id, category_id FROM biz_teacher_tool_category_rel
 GROUP BY tool_id, category_id HAVING COUNT(*) > 1) d;
SELECT 'recommended_without_enabled_category', COUNT(*)
 FROM biz_teacher_tool t
 WHERE t.del_flag='0' AND t.is_recommended='Y'
   AND NOT EXISTS (
     SELECT 1 FROM biz_teacher_tool_category_rel r
     JOIN biz_teacher_tool_category c ON c.category_id=r.category_id
     WHERE r.tool_id=t.tool_id AND c.status='0' AND c.del_flag='0');
SELECT 'task', task_code, COUNT(DISTINCT t.tool_id)
 FROM biz_teacher_tool_task k
 LEFT JOIN biz_teacher_tool t ON FIND_IN_SET(k.task_code, t.task_tags) > 0
   AND t.status='0' AND t.del_flag='0'
 WHERE k.del_flag='0' AND k.status='0'
 GROUP BY task_code ORDER BY task_code;
"""
    rows = db_query(client, sql, "SQL 后检")
    values = {}
    task_counts = {}
    for row in rows:
        if len(row) >= 2 and row[0] not in values:
            values[row[0]] = row[1]
        if len(row) >= 3 and row[0] == "task":
            task_counts[row[0] + ":" + row[1]] = int(row[2])
    print("后检汇总：", json.dumps(values, ensure_ascii=False))
    print("任务覆盖：", json.dumps(task_counts, ensure_ascii=False, sort_keys=True))
    if values.get("columns") != "aliases,task_tags":
        raise DeployError("aliases/task_tags 列未按预期创建")
    if values.get("tables") != "biz_teacher_tool_task,biz_teacher_tool_term":
        raise DeployError("任务/词条表未按预期创建")
    if values.get("tasks") != "5":
        raise DeployError("教学任务不是 5 项")
    if int(values.get("terms", "0")) <= 0:
        raise DeployError("别名词条为空")
    if int(values.get("tools_with_aliases", "0")) <= 0:
        raise DeployError("aliases 列未回填")
    if int(values.get("tools_with_tasks", "0")) <= 0:
        raise DeployError("task_tags 列未回填")
    if values.get("duplicate_terms") != "0":
        raise DeployError("别名词条有重复")
    if values.get("duplicate_relations") != "0":
        raise DeployError("工具分类关系有重复")
    if values.get("recommended_without_enabled_category") != "0":
        raise DeployError("精选工具存在无启用分类")
    actual_codes = {key.split(":", 1)[1] for key in task_counts}
    if actual_codes != EXPECTED_TASK_CODES:
        raise DeployError("教学任务编码不完整：%s" % sorted(actual_codes))
    empty_tasks = {key: count for key, count in task_counts.items() if count <= 0}
    if empty_tasks:
        raise DeployError("存在空教学任务：%s" % empty_tasks)
    return values


def capture_runtime(client):
    out = run_checked(
        client,
        "systemctl show xueyeceping-130 -p ActiveState -p SubState -p MainPID "
        "-p NRestarts -p WorkingDirectory -p ExecStart; "
        "grep -oE '/data/apps/[^ ]+\\.jar' %s | head -1"
        % shlex.quote(UNIT_PATH),
        "读取当前运行状态",
    )
    workdir_match = re.search(r"^WorkingDirectory=(.*)$", out, re.M)
    exec_match = re.search(r"^ExecStart=.*?(/data/apps/[^ ]+\.jar)", out, re.M)
    if not workdir_match or not exec_match:
        raise DeployError("无法解析当前 systemd unit")
    jar = exec_match.group(1).strip()
    jar_sha256 = run_checked(
        client, "sha256sum %s | awk '{print $1}'" % shlex.quote(jar), "旧 JAR 哈希"
    ).strip()
    unit_sha256 = run_checked(
        client, "sha256sum %s | awk '{print $1}'" % shlex.quote(UNIT_PATH), "旧 unit 哈希"
    ).strip()
    return {
        "raw": out,
        "workdir": workdir_match.group(1).strip(),
        "jar": jar,
        "jar_sha256": jar_sha256,
        "unit_sha256": unit_sha256,
    }


def switch_backend(client, password, backup, previous, manifest):
    unit_text = run_checked(
        client, "cat %s" % shlex.quote(UNIT_PATH), "读取 systemd unit"
    )
    old_jar = previous["jar"]
    old_workdir = previous["workdir"]
    new_jar = posixpath.join(REMOTE_RELEASE, "ruoyi-admin.jar")
    new_workdir = posixpath.join(REMOTE_RELEASE, "backend")
    if old_jar not in unit_text:
        raise DeployError("当前 unit 中未找到旧 JAR 路径")
    if old_workdir not in unit_text:
        raise DeployError("当前 unit 中未找到旧工作目录")
    new_unit = unit_text.replace(old_jar, new_jar).replace(old_workdir, new_workdir)
    if new_jar not in new_unit or new_workdir not in new_unit:
        raise DeployError("新 unit 未写入目标路径")
    encoded = base64.b64encode(new_unit.encode("utf-8")).decode("ascii")
    script = r"""
set -euo pipefail
tmp=$(mktemp)
trap 'rm -f "$tmp"' EXIT
echo %s | base64 -d > "$tmp"
install -m 0644 "$tmp" %s
systemctl daemon-reload
""" % (encoded, shlex.quote(UNIT_PATH))
    run_base64_checked(client, script, "写入新 systemd unit", password=password)
    old_pid = re.search(r"^MainPID=(\d+)$", previous["raw"], re.M)
    old_pid_value = int(old_pid.group(1)) if old_pid else 0
    run_base64_checked(
        client,
        "set -e\nsystemctl restart xueyeceping-130\n",
        "重启后端服务",
        password=password,
    )

    deadline = time.time() + 150
    last = ""
    while time.time() < deadline:
        code, out, err = remote130.run(
            client,
            "systemctl show xueyeceping-130 -p ActiveState -p SubState -p MainPID "
            "-p NRestarts -p ExecStart; "
            "printf '\\nCAPTCHA='; curl -sS -o /dev/null -w '%{http_code}' "
            "--max-time 5 http://127.0.0.1:3009/captchaImage || true; "
            "printf '\\nNGINX='; curl -sS -o /dev/null -w '%{http_code}' "
            "--max-time 5 -H 'Host: xxkj.xsedu.net.cn' "
            "http://127.0.0.1/prod-api/captchaImage || true; "
            "printf '\\nROOT='; curl -sS -o /dev/null -w '%{http_code}' "
            "--max-time 5 -H 'Host: xxkj.xsedu.net.cn' http://127.0.0.1/ || true",
            timeout=30,
        )
        last = (out or "") + (err or "")
        state_match = re.search(r"^ActiveState=(\S+)$", last, re.M)
        pid_match = re.search(r"^MainPID=(\d+)$", last, re.M)
        active = state_match.group(1) if state_match else ""
        pid = int(pid_match.group(1)) if pid_match else 0
        if (
            active == "active"
            and pid > 0
            and pid != old_pid_value
            and new_jar in last
            and "CAPTCHA=200" in last
            and "NGINX=200" in last
            and "ROOT=200" in last
        ):
            print(last.strip())
            return {
                "pid": pid,
                "health": "200/200/200",
                "jar": new_jar,
                "unit": UNIT_PATH,
            }
        time.sleep(3)
    print(last.strip(), file=sys.stderr)
    raise DeployError("后端启动或探活超时")


def rollback_backend(client, password, backup):
    print("开始恢复旧 systemd unit", flush=True)
    run_base64_checked(
        client,
        "set -e\ninstall -m 0644 %s %s\nsystemctl daemon-reload\nsystemctl restart xueyeceping-130\n"
        % (shlex.quote(backup["unit"]), shlex.quote(UNIT_PATH)),
        "恢复旧 unit",
        password=password,
    )
    deadline = time.time() + 150
    last = ""
    while time.time() < deadline:
        code, out, err = remote130.run(
            client,
            "systemctl show xueyeceping-130 -p ActiveState -p SubState -p MainPID; "
            "printf '\\nCAPTCHA='; curl -sS -o /dev/null -w '%{http_code}' "
            "--max-time 5 http://127.0.0.1:3009/captchaImage || true",
            timeout=30,
        )
        last = (out or "") + (err or "")
        if "ActiveState=active" in last and "CAPTCHA=200" in last:
            print(last.strip())
            return
        time.sleep(3)
    raise DeployError("回滚后探活仍失败，请人工处理：" + last)


def write_manifest(client, manifest):
    remote_manifest = posixpath.join(REMOTE_RELEASE, "manifest.json")
    local_manifest = ARTIFACT_DIR / "manifest.json"
    upload_file(client, local_manifest, remote_manifest, "manifest.json")


def deploy(client, password, manifest):
    previous = capture_runtime(client)
    print("当前 JAR：", previous["jar"])
    print("当前工作目录：", previous["workdir"])
    precheck_sql(client)
    existing = run_checked(
        client,
        "if [ -e %s ]; then echo EXISTS; else echo MISSING; fi"
        % shlex.quote(REMOTE_RELEASE),
        "检查目标 release",
    )
    if existing.endswith("EXISTS"):
        # 上一次发布若在切换阶段失败，release 目录里已经是同一份构建产物。
        # 只有 JAR 哈希与本次清单一致时才允许续发，避免覆盖其他版本的目录。
        remote_jar = posixpath.join(REMOTE_RELEASE, "ruoyi-admin.jar")
        code, out, err = remote130.run(
            client,
            "sha256sum %s 2>/dev/null | awk '{print $1}'" % shlex.quote(remote_jar),
            timeout=60,
        )
        remote_hash = (out or "").strip().lower()
        if code != 0 or remote_hash != manifest["backendJarSha256"].lower():
            raise DeployError("目标 release 已存在且 JAR 不一致，拒绝覆盖：%s" % REMOTE_RELEASE)
        print("目标 release 已存在且 JAR 一致，继续续发：", REMOTE_RELEASE)
    missing = run_checked(
        client,
        "for p in /data/apps/xueyeceping/releases /data/backups; do "
        "[ -d \"$p\" ] || echo MISSING:$p; done",
        "检查远端目录",
    )
    if missing.strip():
        raise DeployError("远端目录缺失：" + missing.strip())

    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    backup_dir = posixpath.join(
        REMOTE_BACKUP_ROOT, "%s_%s" % (timestamp, RELEASE_NAME)
    )
    print("整库备份目录：", backup_dir)
    backup = remote_backup(client, password, backup_dir)
    run_checked(
        client,
        "mkdir -p %s %s"
        % (
            shlex.quote(REMOTE_RELEASE),
            shlex.quote(posixpath.join(REMOTE_RELEASE, "backend")),
        ),
        "创建 release 目录",
    )
    switched = False
    try:
        jar_info = upload_file(
            client, LOCAL_JAR, posixpath.join(REMOTE_RELEASE, "ruoyi-admin.jar"), "后端 JAR"
        )
        if jar_info["sha256"].lower() != manifest["backendJarSha256"].lower():
            raise DeployError("远端 JAR 哈希与本地清单不一致")
        sql_info = upload_file(
            client,
            ARTIFACT_DIR / LOCAL_SQL.name,
            posixpath.join(REMOTE_RELEASE, LOCAL_SQL.name),
            "增量 SQL",
        )
        if sql_info["sha256"].lower() != manifest["sqlSha256"].lower():
            raise DeployError("远端 SQL 哈希与本地清单不一致")
        write_manifest(client, manifest)
        apply_sql(client, posixpath.join(REMOTE_RELEASE, LOCAL_SQL.name))
        postcheck_sql(client)
        switched = True
        runtime = switch_backend(client, password, backup, previous, manifest)
    except Exception:
        print("发布流程失败。", file=sys.stderr)
        if switched:
            try:
                rollback_backend(client, password, backup)
            except Exception as rollback_error:
                print("自动回滚失败：%s" % rollback_error, file=sys.stderr)
        raise
    return {
        "release": RELEASE_NAME,
        "releaseDir": REMOTE_RELEASE,
        "backupDir": backup_dir,
        "backupSha256": backup["dump_sha256"],
        "jarSha256": jar_info["sha256"],
        "sqlSha256": sql_info["sha256"],
        "runtime": runtime,
        "previous": previous,
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("action", choices=["inspect", "deploy"])
    args = parser.parse_args()
    jump, client, password = remote130.connect()
    try:
        if args.action == "inspect":
            inspect(client, password)
            return 0
        manifest = prepare_artifacts()
        result = deploy(client, password, manifest)
        (ARTIFACT_DIR / "deploy-result.json").write_text(
            json.dumps(result, ensure_ascii=False, indent=2) + "\n",
            encoding="utf-8",
        )
        print(json.dumps(result, ensure_ascii=False, indent=2))
        return 0
    except DeployError as exc:
        print("DEPLOY_ERROR:", exc, file=sys.stderr)
        return 1
    finally:
        client.close()
        jump.close()


if __name__ == "__main__":
    raise SystemExit(main())
