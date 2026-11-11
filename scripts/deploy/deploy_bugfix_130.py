"""1.30.96 独立发布入口；inspect 无需本机制品，check/deploy 先验证清单。

制品沿用上一版 manifest 格式，files 必须包含 jar、frontend.tar.gz 和本次 SQL。
SQL 制品须与仓库 sql/ 原文一致。失败自动恢复配置，不恢复业务数据库。
"""
from __future__ import annotations

import argparse
import base64
import hashlib
import shutil
import tempfile
import json
import posixpath
import re
import shlex
import sys
import tarfile
import time
from datetime import datetime
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
RELEASE = "20261111_bugfix_v1"
VERSION = "1.30.96"
OLD_VERSION = "1.30.95"
FRONTEND_VERSION = "1.30.94"
TITLE = "Python 运行与批改多项问题修复"
NEW_RELEASE = f"/data/apps/xueyeceping/releases/{RELEASE}"
NEW_JAR = f"{NEW_RELEASE}/ruoyi-admin.jar"
NEW_BACKEND = f"{NEW_RELEASE}/backend"
NEW_FRONTEND = f"{NEW_RELEASE}/frontend"
OLD_FRONTEND = "/data/apps/xueyeceping/releases/20261111_python_interactive_v1/frontend"
OLD_JAR = "/data/apps/xueyeceping/releases/20261111_typing_quote_v1/ruoyi-admin.jar"
OLD_BACKEND = "/data/apps/xueyeceping/releases/20261111_typing_quote_v1/backend"
OLD_JAR_SHA = "741b595902488bd6446c74a5a8fbae1828694e4f49e22fa4cad538034f5d58b8"
OLD_INDEX_SHA = "4239bdf9fb27d1fd1c509d070653826db0bd92d09395f41541a309ff3e85cd6d"
UNIT_PATH = "/etc/systemd/system/xueyeceping-130.service"
SITE_PATH = "/etc/nginx/sites-available/xueyeceping-130"
ENV_PATH = "/data/apps/xueyeceping/shared/xueyeceping-130.env"
BACKUP_ROOT = "/data/backups/xueyeceping"
ARTIFACT_DIR = ROOT / "output/release-20261111-bugfix-130"
SQL_NAME = "platform_update_1_30_96_20261111.sql"
SQL_PATH = ROOT / "sql" / SQL_NAME
remote130 = None


class DeployError(RuntimeError):
    pass


def sha256_file(path):
    h = hashlib.sha256()
    with Path(path).open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            h.update(block)
    return h.hexdigest()


def validate_artifacts(directory=None):
    directory = Path(directory or ARTIFACT_DIR)
    path = directory / "manifest.json"
    if not path.is_file():
        raise DeployError(f"缺少制品清单：{path}；请先准备 jar、frontend.tar.gz、SQL 和 manifest.json")
    try:
        manifest = json.loads(path.read_text(encoding="utf-8"))
        if manifest["release"] != RELEASE or manifest["version"] != VERSION:
            raise DeployError("清单 release/version 与本次发布不符")
        if manifest["sql"] != [SQL_NAME] or set(manifest["files"]) != {"ruoyi-admin.jar", "frontend.tar.gz", SQL_NAME}:
            raise DeployError("清单只能包含本次 jar、前端归档和唯一产品更新 SQL")
        for name, entry in manifest["files"].items():
            target = directory / name
            if not target.is_file() or target.is_symlink():
                raise DeployError(f"缺少制品或制品为链接：{target}")
            if type(entry["bytes"]) is not int or entry["bytes"] <= 0:
                raise DeployError(f"制品大小必须为正整数：{name}")
            if not re.fullmatch(r"[0-9a-f]{64}", entry["sha256"]):
                raise DeployError(f"SHA-256 格式错误：{name}")
            if target.stat().st_size != entry["bytes"] or sha256_file(target) != entry["sha256"]:
                raise DeployError(f"制品大小或 SHA-256 不一致：{name}")
        if (directory / SQL_NAME).read_bytes() != SQL_PATH.read_bytes():
            raise DeployError("SQL 制品与仓库唯一允许的 SQL 不一致")
        frontend = manifest["frontend"]
        if frontend["archive"] != "frontend.tar.gz" or frontend["archiveBytes"] != manifest["files"]["frontend.tar.gz"]["bytes"] or frontend["archiveSha256"] != manifest["files"]["frontend.tar.gz"]["sha256"]:
            raise DeployError("前端归档重复字段不一致")
        if type(frontend["fileCount"]) is not int or frontend["fileCount"] <= 0:
            raise DeployError("前端文件数无效")
        with tarfile.open(directory / "frontend.tar.gz", "r:gz") as archive:
            names, count, index_sha = set(), 0, None
            for member in archive:
                name = member.name.removeprefix("./")
                if name in ("", ".") and member.isdir():
                    continue
                if name.startswith("/") or ".." in name.split("/") or "\\" in name or not (member.isfile() or member.isdir()) or name in names:
                    raise DeployError("前端归档包含不安全或重复路径")
                names.add(name)
                if member.isfile():
                    count += 1
                    if name == "index.html":
                        index_sha = hashlib.sha256(archive.extractfile(member).read()).hexdigest()
            if count != frontend["fileCount"] or index_sha != frontend["indexSha256"] or not index_sha:
                raise DeployError("前端归档文件数或首页 SHA-256 不一致")
        return manifest
    except (OSError, ValueError, KeyError, TypeError, tarfile.TarError) as error:
        raise DeployError("制品清单或归档不完整/格式错误，请重新生成并核对") from error


def package_artifacts(jar=None, dist=None, directory=None):
    jar = Path(jar or ROOT / "RuoYi-Vue/ruoyi-admin/target/ruoyi-admin.jar")
    dist = Path(dist or ROOT / "RuoYi-Vue3/dist")
    directory = Path(directory or ARTIFACT_DIR)
    if not jar.is_file() or jar.is_symlink() or not (dist / "index.html").is_file() or not SQL_PATH.is_file():
        raise DeployError("打包输入缺失：请先完成后端打包和前端构建，并保留仓库产品更新 SQL")
    entries = sorted(dist.rglob("*"))
    if any(p.relative_to(dist).parts[0].lower() in ("output", "node_modules", ".git") for p in entries):
        raise DeployError("dist 内含 output/node_modules/.git，拒绝夹带开发产物")
    if any(p.is_symlink() or not (p.is_file() or p.is_dir()) for p in entries):
        raise DeployError("dist 中含链接或特殊文件，拒绝归档")
    if directory.resolve().is_relative_to(dist.resolve()):
        raise DeployError("制品目录不得位于 dist 内")
    directory.parent.mkdir(parents=True, exist_ok=True)
    # 先在隔离临时目录完整生成并验证，失败不破坏现有制品。
    with tempfile.TemporaryDirectory(prefix="bugfix-package-", dir=directory.parent) as temp:
        output = Path(temp)
        shutil.copyfile(jar, output / "ruoyi-admin.jar")
        shutil.copyfile(SQL_PATH, output / SQL_NAME)
        with tarfile.open(output / "frontend.tar.gz", "w:gz") as archive:
            for path in entries:
                if path.is_file():
                    archive.add(path, arcname=path.relative_to(dist).as_posix(), recursive=False)
        files = {name: {"bytes": (output / name).stat().st_size, "sha256": sha256_file(output / name)} for name in ("ruoyi-admin.jar", "frontend.tar.gz", SQL_NAME)}
        frontend = {"archive": "frontend.tar.gz", "archiveBytes": files["frontend.tar.gz"]["bytes"], "archiveSha256": files["frontend.tar.gz"]["sha256"], "indexSha256": sha256_file(dist / "index.html"), "fileCount": sum(p.is_file() for p in entries)}
        manifest = {"release": RELEASE, "version": VERSION, "baseRelease": "20261111_typing_quote_v1 + 20261111_python_interactive_v1", "createdAt": datetime.now().isoformat(timespec="seconds"), "scope": TITLE, "databaseChanges": False, "sql": [SQL_NAME], "files": files, "frontend": frontend}
        (output / "manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        validate_artifacts(output)
        if directory.exists():
            raise DeployError("制品目录已存在，拒绝覆盖；请核对并另行保存旧制品后再打包")
        shutil.copytree(output, directory)
    return manifest


def run_checked(client, command, label, timeout=180, password=None):
    if password is None:
        code, out, _ = remote130.run(client, command, timeout=timeout)
    else:
        code, out, _ = remote130.sudo(client, password, command, timeout=timeout)
    if code:
        # 远程错误可能包含认证信息，禁止回显原始 stdout/stderr。
        raise DeployError(f"{label}失败，退出码 {code}（原始远程输出已隐藏）")
    return out.strip()


def sudo_bash(client, password, script, label, timeout=900):
    encoded = base64.b64encode(script.encode()).decode("ascii")
    return run_checked(client, f"echo {encoded} | base64 -d | bash", label, timeout, password)


def db_config_path(client):
    command = "grep -oE -- '--spring.config.additional-location=[^ ]+' " + shlex.quote(UNIT_PATH) + " | head -1 | cut -d= -f2- | sed 's/^optional:file://;s/^file://'"
    value = run_checked(client, command, "读取外置配置路径")
    if not value.startswith("/"):
        raise DeployError("无法定位正式外置配置")
    return value if value.endswith((".yml", ".yaml")) else posixpath.join(value.rstrip("/"), "application-druid.yml")


def db_query(client, password, sql, config):
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
{sql}
SQL
"""
    out = sudo_bash(client, password, script, "数据库操作", 600)
    return [line.split("\t") for line in out.splitlines() if line.strip()]


def current_release(client):
    script = f"""
set -euo pipefail
root=$(grep -hE '^\\s*root\\s+.*frontend;' {shlex.quote(SITE_PATH)} | tail -1 | awk '{{print $2}}' | tr -d ';')
jar=$(grep -oE '/data/apps/[^ ]+\\.jar' {shlex.quote(UNIT_PATH)} | tail -1)
printf 'frontendRoot=%s\\njar=%s\\n' "$root" "$jar"
sha256sum "$root/index.html" | awk '{{print "indexSha256="$1}}'
sha256sum "$jar" | awk '{{print "jarSha256="$1}}'
systemctl show xueyeceping-130 -p ActiveState -p SubState -p MainPID -p NRestarts
"""
    return dict(line.split("=", 1) for line in run_checked(client, script, "读取正式版本").splitlines() if "=" in line)


def validate_baseline(state):
    for key, expected in {"frontendRoot": OLD_FRONTEND, "jar": OLD_JAR, "indexSha256": OLD_INDEX_SHA, "jarSha256": OLD_JAR_SHA}.items():
        if state.get(key) != expected:
            raise DeployError(f"线上 {key} 偏离后端 {OLD_VERSION}/前端 {FRONTEND_VERSION} 基线，停止发布")
    if state.get("ActiveState") != "active" or state.get("SubState") != "running" or state.get("NRestarts") != "0" or not state.get("MainPID", "0").isdigit() or int(state["MainPID"]) == 0:
        raise DeployError("正式后端服务状态异常")


def preflight(client, password, config, require_absent=True):
    state = current_release(client)
    validate_baseline(state)
    if require_absent:
        run_checked(client, f"test ! -e {shlex.quote(NEW_RELEASE)} && test ! -L {shlex.quote(NEW_RELEASE)}", "新release已存在，禁止覆盖")
    sudo_bash(client, password, "nginx -t", "Nginx配置检查")
    rows = db_query(client, password, f"""
SELECT 'old', COUNT(*) FROM biz_platform_update WHERE version_no='{OLD_VERSION}' AND status='PUBLISHED';
SELECT 'new_conflict', COUNT(*) FROM biz_platform_update WHERE version_no='{VERSION}' AND title<>'{TITLE}';
SELECT 'new_count', COUNT(*) FROM biz_platform_update WHERE version_no='{VERSION}';
SELECT 'answer5', COUNT(*) FROM biz_student_answer WHERE submit_time>=NOW()-INTERVAL 5 MINUTE;
SELECT 'checkin10', COUNT(*) FROM biz_lesson_checkin WHERE checkin_time>=NOW()-INTERVAL 10 MINUTE;
SELECT 'judging', COUNT(*) FROM biz_programming_submission WHERE status_code IN ('WAITING','JUDGING');
SELECT 'ai', COUNT(*) FROM biz_practical_ai_job WHERE job_status IN ('PENDING','RUNNING','CANCEL_REQUESTED');
SELECT 'index_exists', COUNT(DISTINCT index_name) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='biz_programming_submission' AND index_name='idx_programming_submission_best';
""", config)
    values = {row[0]: row[1] for row in rows}
    if values.get("old") != "1" or values.get("new_conflict") != "0" or values.get("new_count") not in ("0", "1") or values.get("index_exists") != "1":
        # 按索引名计数，避免复合索引列数影响门禁。
        raise DeployError("版本/既有索引门禁未通过")
    if any(values.get(k) != "0" for k in ("answer5", "checkin10", "judging", "ai")):
        raise DeployError("存在课堂活动或后台任务，停止发布")
    return {"release": state, "activity": values}


def remote_python(client, password, source, label):
    encoded = base64.b64encode(source.encode()).decode("ascii")
    return sudo_bash(client, password, f"echo {encoded} | base64 -d | python3 -I", label)


def backup(client, password, config, baseline, manifest):
    directory = f"{BACKUP_ROOT}/{datetime.now():%Y%m%d_%H%M%S}_{RELEASE}"
    script = f"""
set -euo pipefail
umask 077
backup={shlex.quote(directory)}
mkdir "$backup"
cp -a {shlex.quote(UNIT_PATH)} "$backup/service.before"
cp -a {shlex.quote(SITE_PATH)} "$backup/nginx.before"
cp -a {shlex.quote(ENV_PATH)} "$backup/env.before"
cp -a {shlex.quote(OLD_JAR)} "$backup/jar.before"
cp -a {shlex.quote(OLD_FRONTEND + '/index.html')} "$backup/index.before.html"
cfg={shlex.quote(config)}
u=$(awk '/username:/{{print $2; exit}}' "$cfg" | tr -d '"')
p=$(awk '/password:/{{print $2; exit}}' "$cfg" | tr -d '"')
cnf=$(mktemp)
trap 'rm -f "$cnf"' EXIT
printf '[client]\\nuser=%s\\npassword=%s\\nhost=127.0.0.1\\n' "$u" "$p" > "$cnf"
mysqldump --defaults-extra-file="$cnf" --single-transaction --quick --routines --triggers --default-character-set=utf8mb4 ry-vue | gzip -9 > "$backup/database.sql.gz"
gzip -t "$backup/database.sql.gz"
zcat "$backup/database.sql.gz" | tail -1 | grep -q 'Dump completed'
"""
    sudo_bash(client, password, script, "整库与配置备份", 1800)
    metadata = {"release": RELEASE, "version": VERSION, "backupDir": directory, "baseline": baseline["release"], "target": {"jar": NEW_JAR, "frontendRoot": NEW_FRONTEND, "jarSha256": manifest["files"]["ruoyi-admin.jar"]["sha256"], "indexSha256": manifest["frontend"]["indexSha256"]}}
    source = f"""
import hashlib,json,pathlib
p=pathlib.Path({directory!r})
m=json.loads({json.dumps(metadata)!r})
m['files']={{}}
for name in ('service.before','nginx.before','env.before','jar.before','index.before.html','database.sql.gz'):
    f=p/name
    h=hashlib.sha256()
    with f.open('rb') as s:
        for b in iter(lambda:s.read(1048576),b''): h.update(b)
    m['files'][name]={{'bytes':f.stat().st_size,'sha256':h.hexdigest()}}
assert m['files']['database.sql.gz']['bytes']>10000000
(p/'release.before.json').write_text(json.dumps(m,indent=2)+'\\n',encoding='utf-8')
print(json.dumps({{'dir':str(p),'dump':m['files']['database.sql.gz']}}))
"""
    return json.loads(remote_python(client, password, source, "记录备份归属与哈希"))


def upload_file(client, local_path, remote_path):
    sftp = client.open_sftp()
    try:
        with sftp.open(remote_path, "wx") as target, Path(local_path).open("rb") as source:
            for block in iter(lambda: source.read(1024 * 1024), b""):
                target.write(block)
    finally:
        sftp.close()
    sha = run_checked(client, f"sha256sum {shlex.quote(remote_path)} | cut -d' ' -f1", "上传哈希复核")
    size = run_checked(client, f"stat -c%s {shlex.quote(remote_path)}", "上传大小复核")
    if sha != sha256_file(local_path) or int(size) != Path(local_path).stat().st_size:
        raise DeployError("远端制品大小或SHA-256不一致")


def stage(client, password, manifest):
    # 原子 mkdir 再次防止 check 后目录被其他发布占用。
    sudo_bash(client, password, f"set -e; mkdir {shlex.quote(NEW_RELEASE)}; chown admin2:admin2 {shlex.quote(NEW_RELEASE)}", "独占创建新release")
    for name in ("ruoyi-admin.jar", "frontend.tar.gz", "manifest.json", SQL_NAME):
        upload_file(client, ARTIFACT_DIR / name, f"{NEW_RELEASE}/{name}")
    sudo_bash(client, password, f"""
set -euo pipefail
mkdir {shlex.quote(NEW_BACKEND)} {shlex.quote(NEW_FRONTEND)}
cp -a {shlex.quote(OLD_BACKEND + '/.')} {shlex.quote(NEW_BACKEND + '/')}
cp -a {shlex.quote(OLD_FRONTEND + '/.')} {shlex.quote(NEW_FRONTEND + '/')}
tar --no-same-owner -xzf {shlex.quote(NEW_RELEASE + '/frontend.tar.gz')} -C {shlex.quote(NEW_FRONTEND)}
chown -R admin2:admin2 {shlex.quote(NEW_RELEASE)}
find {shlex.quote(NEW_RELEASE)} -type d -exec chmod 755 {{}} +
find {shlex.quote(NEW_RELEASE)} -type f -exec chmod 644 {{}} +
test "$(sha256sum {shlex.quote(NEW_FRONTEND + '/index.html')} | cut -d' ' -f1)" = {shlex.quote(manifest['frontend']['indexSha256'])}
""", "合并静态资源并验证首页")


def switch(client, password):
    sudo_bash(client, password, f"""
set -euo pipefail
grep -qF {shlex.quote(OLD_JAR)} {shlex.quote(UNIT_PATH)}
grep -qF {shlex.quote(OLD_FRONTEND)} {shlex.quote(SITE_PATH)}
sed -i {shlex.quote('s#' + OLD_BACKEND + '#' + NEW_BACKEND + '#g;s#' + OLD_JAR + '#' + NEW_JAR + '#g')} {shlex.quote(UNIT_PATH)}
sed -i {shlex.quote('s#' + OLD_FRONTEND + '#' + NEW_FRONTEND + '#g')} {shlex.quote(SITE_PATH)}
nginx -t
systemctl daemon-reload
systemctl restart xueyeceping-130
systemctl reload nginx
""", "切换前后端并重启", 300)


def wait_healthy(client, target, old_pid):
    deadline = time.monotonic() + 180
    while time.monotonic() < deadline:
        state = current_release(client)
        code, out, _ = remote130.run(client, "set -e; curl --max-time 10 -fsS http://127.0.0.1/ | sha256sum | cut -d' ' -f1; curl --max-time 10 -fsS -o /dev/null http://127.0.0.1/prod-api/captchaImage", timeout=30)
        if code == 0 and all(state.get(k) == v for k, v in target.items()) and out.strip() == target["indexSha256"] and state.get("ActiveState") == "active" and state.get("SubState") == "running" and state.get("NRestarts") == "0" and state.get("MainPID") not in ("0", old_pid):
            return state
        time.sleep(3)
    raise DeployError("首页/验证码/后端哈希或进程健康检查超时")


def validate_rollback(metadata, current, files, manual=True):
    if metadata.get("release") != RELEASE or metadata.get("version") != VERSION:
        raise DeployError("备份不属于本release")
    validate_baseline(metadata.get("baseline", {}))
    if metadata.get("target", {}).get("jar") != NEW_JAR or metadata.get("target", {}).get("frontendRoot") != NEW_FRONTEND:
        raise DeployError("备份目标路径与本release不匹配")
    if set(files) != {"service.before", "nginx.before", "env.before", "jar.before", "index.before.html", "database.sql.gz"}:
        raise DeployError("备份文件集合不完整")
    if files != metadata.get("files") or files.get("jar.before", {}).get("sha256") != OLD_JAR_SHA or files.get("index.before.html", {}).get("sha256") != OLD_INDEX_SHA:
        raise DeployError("备份文件与记录/基线不匹配")
    for key, expected in {"jar": NEW_JAR, "frontendRoot": NEW_FRONTEND}.items():
        actual = current.get(key)
        if manual and actual != expected:
            raise DeployError("当前不是本release，拒绝手动回滚其他版本")
        if not manual and actual not in (expected, metadata["baseline"][key]):
            raise DeployError("自动回滚发现其他发布已接管，拒绝覆盖")
    for key in ("jarSha256", "indexSha256"):
        pathkey = "jar" if key == "jarSha256" else "frontendRoot"
        expected = metadata["target"][key] if current[pathkey] == metadata["target"][pathkey] else metadata["baseline"][key]
        if current.get(key) != expected:
            raise DeployError("当前版本哈希漂移，拒绝回滚")


def rollback(client, password, directory, manual=True):
    if not re.fullmatch(re.escape(BACKUP_ROOT) + r"/\d{8}_\d{6}_" + re.escape(RELEASE), directory):
        raise DeployError("备份路径必须是本release在正式备份根下的时间戳目录")
    current = current_release(client)
    source = f"""
import hashlib,json,pathlib
p=pathlib.Path({directory!r})
assert str(p.resolve())==str(p) and not p.is_symlink()
m=json.loads((p/'release.before.json').read_text(encoding='utf-8'))
assert m['backupDir']==str(p)
facts={{}}
for name in ('service.before','nginx.before','env.before','jar.before','index.before.html','database.sql.gz'):
    f=p/name
    assert f.is_file() and not f.is_symlink()
    h=hashlib.sha256()
    with f.open('rb') as s:
        for b in iter(lambda:s.read(1048576),b''):h.update(b)
    facts[name]={{'bytes':f.stat().st_size,'sha256':h.hexdigest()}}
assert {OLD_JAR!r} in (p/'service.before').read_text()
assert {OLD_BACKEND!r} in (p/'service.before').read_text()
assert {OLD_FRONTEND!r} in (p/'nginx.before').read_text()
print(json.dumps({{'metadata':m,'files':facts}}))
"""
    evidence = json.loads(remote_python(client, password, source, "核对备份真实归属与完整性"))
    validate_rollback(evidence["metadata"], current, evidence["files"], manual)
    # 哈希覆盖完整dump，但回滚只恢复应用配置，保留发布期间的新成绩。
    sudo_bash(client, password, f"""
set -euo pipefail
cmp -s {shlex.quote(directory + '/env.before')} {shlex.quote(ENV_PATH)}
install -m 0644 -o root -g root {shlex.quote(directory + '/service.before')} {shlex.quote(UNIT_PATH)}
install -m 0644 -o root -g root {shlex.quote(directory + '/nginx.before')} {shlex.quote(SITE_PATH)}
nginx -t
systemctl daemon-reload
systemctl restart xueyeceping-130
systemctl reload nginx
""", "恢复已核验的旧配置", 300)
    return wait_healthy(client, {"jar": OLD_JAR, "frontendRoot": OLD_FRONTEND, "jarSha256": OLD_JAR_SHA, "indexSha256": OLD_INDEX_SHA}, current["MainPID"])


def register_update(client, password, config):
    # 仅执行已通过大小/哈希检查且等于仓库原文的唯一 SQL。
    db_query(client, password, SQL_PATH.read_text(encoding="utf-8"), config)
    rows = db_query(client, password, f"SELECT update_id,version_no,title,status FROM biz_platform_update WHERE version_no='{VERSION}';", config)
    if len(rows) != 1 or rows[0][1:] != [VERSION, TITLE, "PUBLISHED"]:
        raise DeployError("产品更新登记冲突或状态不是PUBLISHED")
    return {"updateId": rows[0][0], "version": VERSION, "status": "PUBLISHED"}


def draft_update(client, password, config):
    db_query(client, password, f"UPDATE biz_platform_update SET status='DRAFT',update_time=NOW() WHERE version_no='{VERSION}' AND title='{TITLE}';", config)


def write_state(name, data):
    ARTIFACT_DIR.mkdir(parents=True, exist_ok=True)
    (ARTIFACT_DIR / name).write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main(argv=None):
    global remote130
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--mode", choices=["inspect", "check", "deploy", "rollback", "package"], default="inspect")
    parser.add_argument("--backup-dir", default="")
    args = parser.parse_args(argv)
    if args.mode == "package":
        print("PACKAGE_OK=" + json.dumps(package_artifacts(), ensure_ascii=False))
        return 0
    manifest = validate_artifacts() if args.mode in ("check", "deploy") else None
    if args.mode == "rollback" and not args.backup_dir:
        raise DeployError("回退必须提供 --backup-dir")
    if remote130 is None:
        sys.path.insert(0, str(ROOT / "scripts/deploy"))
        import remote130 as transport
        remote130 = transport
    jump, client, password = remote130.connect()
    try:
        if args.mode == "inspect":
            print("CURRENT=" + json.dumps(current_release(client), ensure_ascii=False))
            return 0
        config = db_config_path(client)
        if args.mode == "rollback":
            health = rollback(client, password, args.backup_dir)
            draft_update(client, password, config)
            write_state("rollback-state.json", {"state": "ROLLED_BACK", "health": health})
            print("ROLLBACK_OK")
            return 0
        baseline = preflight(client, password, config)
        if args.mode == "check":
            print("CHECK_OK=" + json.dumps(baseline, ensure_ascii=False))
            return 0
        info = backup(client, password, config, baseline, manifest)
        print("BACKUP=" + json.dumps(info))
        stage(client, password, manifest)
        # 上传期间可能有课堂活动或版本漂移，切换前再次核对。
        preflight(client, password, config, require_absent=False)
        sudo_bash(client, password, f"cmp -s {shlex.quote(info['dir'] + '/env.before')} {shlex.quote(ENV_PATH)}", "核对环境未漂移")
        try:
            switch(client, password)
            target = {"jar": NEW_JAR, "frontendRoot": NEW_FRONTEND, "jarSha256": manifest["files"]["ruoyi-admin.jar"]["sha256"], "indexSha256": manifest["frontend"]["indexSha256"]}
            health = wait_healthy(client, target, baseline["release"]["MainPID"])
            update = register_update(client, password, config)
            write_state("remote-state.json", {"release": RELEASE, "version": VERSION, "backup": info, "health": health, "productUpdate": update})
            print("DEPLOY_OK=" + json.dumps(update, ensure_ascii=False))
            return 0
        except Exception:
            print("DEPLOY_FAILED（原始异常隐藏）", file=sys.stderr)
            try:
                health = rollback(client, password, info["dir"], manual=False)
                draft_update(client, password, config)
                write_state("rollback-state.json", {"state": "AUTO_ROLLED_BACK", "health": health})
                print("AUTO_ROLLBACK_OK")
            except Exception:
                raise DeployError("发布失败且自动回滚未完成，请核对当前版本与备份；禁止宣称上线") from None
            raise DeployError("发布失败，已自动回滚") from None
    finally:
        client.close()
        jump.close()


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except DeployError as error:
        print(str(error), file=sys.stderr)
        raise SystemExit(1)
    except Exception:
        print("发布操作失败（认证及原始远程异常已隐藏）", file=sys.stderr)
        raise SystemExit(1)
