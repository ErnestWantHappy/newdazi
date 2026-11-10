"""低峰窗口专用：备份、上传文件、执行幂等 SQL、按 source_key 置为 DRAFT/PUBLISHED。"""
from __future__ import annotations

import argparse
import base64
import hashlib
import json
import posixpath
import shlex
from datetime import datetime
from pathlib import Path
from typing import Any

from common import (
    PipelineError, TEXTBOOK_CASE_ROOT, mysql_readonly, parse_tsv, read_json,
    redact_text, remote_connect, remote_run, run_dir, sha256_file, source_key,
    utc_now, write_json,
)


SERVICE_UNIT = "/etc/systemd/system/xueyeceping-130.service"


def normalize_config_path(value: str) -> str:
    """把 systemd 的 additional-location 参数规范成 application-druid.yml 路径。"""
    first = (value or "").split(",")[0].strip()
    first = first.removeprefix("optional:file:").removeprefix("file:")
    if not first:
        raise PipelineError("无法从 xueyeceping-130.service 读取外置配置目录")
    if first.endswith(".yml") or first.endswith(".yaml"):
        return first
    return posixpath.join(first.rstrip("/"), "application-druid.yml")


def resolve_config(client: Any) -> str:
    command = (
        "grep -oE -- '--spring.config.additional-location=[^ ]+' %s "
        "| head -1 | cut -d= -f2-"
    ) % shlex.quote(SERVICE_UNIT)
    code, out, err = remote_run(client, command, timeout=30)
    if code or not out.strip():
        raise PipelineError("读取 130 服务外置配置失败：" + redact_text(err.strip()))
    return normalize_config_path(out.strip())


def backup_script(backup_dir: str, config_path: str) -> str:
    return """set -e
install -d -m 0750 /data/apps/xueyeceping/shared/textbook/cases/.staging
chown admin2:admin2 /data/apps/xueyeceping/shared/textbook/cases/.staging
cfg=%s
u=$(awk '/username:/{print $2; exit}' "$cfg" | tr -d '"')
p=$(awk '/password:/{print $2; exit}' "$cfg" | tr -d '"')
umask 077
printf '[client]\\nuser=%%s\\npassword=%%s\\nhost=127.0.0.1\\n' "$u" "$p" >/tmp/.xyc-research-backup.cnf
trap 'rm -f /tmp/.xyc-research-backup.cnf' EXIT
install -d -m 0750 %s
mysqldump --defaults-extra-file=/tmp/.xyc-research-backup.cnf --single-transaction --quick \\
  --default-character-set=utf8mb4 ry-vue | gzip > %s/database.sql.gz
sha256sum %s/database.sql.gz
""" % (shlex.quote(config_path), shlex.quote(backup_dir), shlex.quote(backup_dir), shlex.quote(backup_dir))


def preflight_sql(post_ids: list[int]) -> str:
    values = ",".join(str(value) for value in post_ids)
    return r"""
SELECT 'case_columns', COUNT(*) FROM information_schema.columns
WHERE table_schema=DATABASE() AND table_name='biz_textbook_case'
  AND column_name IN ('research_topic_id','research_topic_title','research_post_id');
SELECT 'source_conflict', COUNT(*) FROM biz_textbook_case
WHERE source_key IN (%s);
SELECT 'baseline_cases', COUNT(*) FROM biz_textbook_case;
SELECT 'baseline_resources', COUNT(*) FROM biz_textbook_case_resource;
SELECT 'post_fingerprint', p.post_id, DATE_FORMAT(COALESCE(p.update_time,p.create_time), '%%Y-%%m-%%d %%H:%%i:%%s')
FROM biz_research_post p WHERE p.post_id IN (%s) ORDER BY p.post_id;
SELECT 'class_activity', SUM(submit_time >= DATE_SUB(NOW(), INTERVAL 20 MINUTE))
FROM biz_student_answer;
""" % (",".join("'research-post-%d'" % value for value in post_ids), values)


def upload_files(client: Any, password: str, manifest: dict, run_path: Path, staging_root: str) -> list[dict]:
    import remote130
    create_staging = (
        "install -d -m 0750 -o admin2 -g admin2 %s %s"
        % (shlex.quote(posixpath.dirname(staging_root.rstrip("/"))), shlex.quote(staging_root))
    )
    code, _, err = remote130.sudo(client, password, create_staging, timeout=60)
    if code:
        raise PipelineError("创建服务器临时目录失败：" + redact_text(err.strip()))
    sftp = client.open_sftp()
    uploaded = []
    try:
        for resource in manifest["resources"]:
            local = run_path / resource["stagingPath"]
            digest = resource["sha256"]
            suffix = Path(resource["filename"]).suffix
            relative = digest[:2] + "/" + digest + suffix
            remote = posixpath.join(staging_root, relative)
            parent = posixpath.dirname(remote)
            try:
                sftp.stat(parent)
            except IOError:
                pieces = parent.replace(staging_root, "").strip("/").split("/")
                current = staging_root.rstrip("/")
                for piece in pieces:
                    current += "/" + piece
                    try:
                        sftp.stat(current)
                    except IOError:
                        sftp.mkdir(current)
            temp = remote + ".part"
            sftp.put(str(local), temp)
            sftp.rename(temp, remote)
            sftp.chmod(remote, 0o640)
            uploaded.append({"resourceKey": resource["resourceKey"], "originalPath": relative,
                             "bytes": local.stat().st_size, "sha256": digest})
            preview = resource.get("previewPath")
            if preview:
                preview_local = run_path / preview
                preview_remote = posixpath.join(staging_root, preview)
                preview_parent = posixpath.dirname(preview_remote)
                try:
                    sftp.stat(preview_parent)
                except IOError:
                    sftp.mkdir(preview_parent)
                sftp.put(str(preview_local), preview_remote + ".part")
                sftp.rename(preview_remote + ".part", preview_remote)
                sftp.chmod(preview_remote, 0o640)
    finally:
        sftp.close()
    return uploaded


def server_hash_check(client: Any, staging_root: str, resources: list[dict]) -> str:
    lines = []
    for resource in resources:
        relative = resource["sha256"][:2] + "/" + resource["sha256"] + Path(resource["filename"]).suffix
        lines.append("%s  %s" % (resource["sha256"], posixpath.join(staging_root, relative)))
        if resource.get("previewSha256") and resource.get("previewPath"):
            lines.append("%s  %s" % (resource["previewSha256"], posixpath.join(staging_root, resource["previewPath"])))
    encoded = base64.b64encode(("\n".join(lines) + "\n").encode("utf-8")).decode("ascii")
    script = "set -e; echo %s | base64 -d | sha256sum -c --status; echo HASH_OK" % encoded
    code, out, err = remote_run(client, script, timeout=300)
    if code or "HASH_OK" not in out:
        raise PipelineError("服务器文件哈希复核失败：" + redact_text(err.strip()))
    return out.strip()


def execute_sql(client: Any, sql: str, label: str, config_path: str) -> None:
    script = r"""set -e
cfg=%s
u=$(awk '/username:/{print $2; exit}' "$cfg" | tr -d '"')
p=$(awk '/password:/{print $2; exit}' "$cfg" | tr -d '"')
umask 077
printf '[client]\nuser=%%s\npassword=%%s\nhost=127.0.0.1\n' "$u" "$p" >/tmp/.xyc-research-apply.cnf
trap 'rm -f /tmp/.xyc-research-apply.cnf /tmp/.xyc-research-apply.sql' EXIT
cat > /tmp/.xyc-research-apply.sql <<'SQL'
%s
SQL
mysql --defaults-extra-file=/tmp/.xyc-research-apply.cnf --default-character-set=utf8mb4 --show-warnings ry-vue < /tmp/.xyc-research-apply.sql
""" % (shlex.quote(config_path), sql)
    code, out, err = remote_run(client, script, timeout=300)
    if code:
        raise PipelineError("%s 执行失败：%s" % (label, redact_text(err.strip())))


def remote_file_exists(client: Any, path: str) -> bool:
    code, _, _ = remote_run(client, "test -f %s" % shlex.quote(path), timeout=30)
    return code == 0


def rollback_moves(client: Any, moved: list[tuple[str, str]]) -> None:
    """SQL 或发布失败时，把本次新落位的文件退回 staging，避免留下不可见正式文件。"""
    if not moved:
        return
    lines = ["mv -f %s %s" % (shlex.quote(destination), shlex.quote(source))
             for destination, source in moved]
    code, _, err = remote_run(client, "set -e\n" + "\n".join(lines), timeout=300)
    if code:
        raise PipelineError("文件回退失败：" + redact_text(err.strip()))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--manifest", required=True)
    parser.add_argument("--confirm-low-peak-window", action="store_true")
    parser.add_argument("--publish", action="store_true", help="仅在验收通过后调用；默认保持 DRAFT")
    args = parser.parse_args()
    manifest_path = Path(args.manifest).resolve()
    manifest = read_json(manifest_path)
    run_path = run_dir(manifest["runId"])
    if manifest.get("state") != "READY_FOR_APPLY":
        raise PipelineError("manifest 未达到 READY_FOR_APPLY，禁止写正式库")
    if not args.confirm_low_peak_window:
        raise PipelineError("必须显式传入 --confirm-low-peak-window 才允许 apply")
    for post_id, updated_at in manifest["sourceFingerprints"].items():
        fingerprint_sql = (
            "SELECT 'post_fingerprint', p.post_id, "
            "DATE_FORMAT(COALESCE(p.update_time,p.create_time), '%%Y-%%m-%%d %%H:%%i:%%s') "
            "FROM biz_research_post p WHERE p.post_id=%s;" % int(post_id)
        )
        current = dict((row[1], row[2]) for row in parse_tsv(mysql_readonly(fingerprint_sql))
                       if row[0] == "post_fingerprint")
        if current.get(str(post_id)) != updated_at:
            raise PipelineError("来源帖子 %s 已变化，必须重新生成 manifest" % post_id)
    post_ids = [int(case["postId"]) for case in manifest["cases"]]
    preflight = parse_tsv(mysql_readonly(preflight_sql(post_ids)))
    activity = next((int(row[1] or 0) for row in preflight if row[0] == "class_activity"), 0)
    if activity > 0:
        raise PipelineError("近 20 分钟仍有 %d 条提交时间更新，不是确认的低峰窗口" % activity)
    baseline_cases = next((int(row[1]) for row in preflight if row[0] == "baseline_cases"), None)
    baseline_resources = next((int(row[1]) for row in preflight if row[0] == "baseline_resources"), None)
    if baseline_cases is None or baseline_resources is None:
        raise PipelineError("无法读取旧课例基线")

    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    backup_dir = "/data/backups/xueyeceping/%s_research_cases_%s" % (timestamp, manifest["runId"])
    case_root = TEXTBOOK_CASE_ROOT.rstrip("/")
    staging_root = case_root + "/.staging/" + manifest["runId"]
    jump, client, password = remote_connect()
    moved_files: list[tuple[str, str]] = []
    result = {"runId": manifest["runId"], "appliedAt": utc_now(), "backupDir": backup_dir,
              "stagingRoot": staging_root, "baseline": {"cases": baseline_cases, "resources": baseline_resources},
              "publish": args.publish}
    try:
        import remote130
        config_path = resolve_config(client)
        result["configPath"] = config_path
        code, out, err = remote130.sudo(client, password, backup_script(backup_dir, config_path), timeout=900)
        if code:
            raise PipelineError("整库备份失败：" + redact_text(err.strip()))
        result["backupSha256"] = out.strip().split()[0]
        execute_sql(client, "SELECT 1;", "数据库连通性", config_path)
        uploaded = upload_files(client, password, manifest, run_path, staging_root)
        result["uploadedFiles"] = len(uploaded)
        result["serverHashCheck"] = server_hash_check(client, staging_root, manifest["resources"])
        move_lines = []
        for resource in manifest["resources"]:
            source = posixpath.join(staging_root, posixpath.dirname(resource["originalPath"]),
                                    posixpath.basename(resource["originalPath"]))
            destination_dir = posixpath.join(case_root, posixpath.dirname(resource["originalPath"]))
            destination = posixpath.join(destination_dir, posixpath.basename(resource["originalPath"]))
            if not remote_file_exists(client, destination):
                move_lines.append("install -d -m 0750 %s" % shlex.quote(destination_dir))
                move_lines.append("mv -f %s %s" % (shlex.quote(source), shlex.quote(destination)))
                move_lines.append("chmod 0640 %s" % shlex.quote(destination))
                moved_files.append((destination, source))
            preview = resource.get("previewPath")
            if preview:
                preview_source = posixpath.join(staging_root, preview)
                preview_dir = posixpath.join(case_root, posixpath.dirname(preview))
                preview_destination = posixpath.join(preview_dir, posixpath.basename(preview))
                if not remote_file_exists(client, preview_destination):
                    move_lines.append("install -d -m 0750 %s" % shlex.quote(preview_dir))
                    move_lines.append("mv -f %s %s" % (shlex.quote(preview_source), shlex.quote(preview_destination)))
                    move_lines.append("chmod 0640 %s" % shlex.quote(preview_destination))
                    moved_files.append((preview_destination, preview_source))
        for resource in manifest["resources"]:
            destination = posixpath.join(case_root, resource["originalPath"])
            move_lines.append("test -f %s" % shlex.quote(destination))
            preview = resource.get("previewPath")
            if preview:
                move_lines.append("test -f %s" % shlex.quote(posixpath.join(case_root, preview)))
        code, out, err = remote_run(client, "set -e\n" + "\n".join(move_lines), timeout=300)
        if code:
            raise PipelineError("文件从 staging 落位失败：" + redact_text(err.strip()))
        result["serverFinalHashCheck"] = server_hash_check(client, case_root, manifest["resources"])
        sql = (run_path / manifest["sqlDraft"]).read_text(encoding="utf-8")
        execute_sql(client, sql, "来源字段与 DRAFT 导入", config_path)
        if args.publish:
            keys = ",".join("'%s'" % case["sourceKey"] for case in manifest["cases"])
            execute_sql(client, "UPDATE biz_textbook_case SET status='PUBLISHED' WHERE source_key IN (%s);" % keys,
                        "课例发布", config_path)
        result["state"] = "APPLIED_PUBLISHED" if args.publish else "APPLIED_DRAFT"
        result["movedFiles"] = len(moved_files)
    except Exception as error:
        try:
            rollback_moves(client, moved_files)
            result["fileRollback"] = "COMPLETED"
        except Exception as rollback_error:
            result["fileRollback"] = "FAILED"
            result["fileRollbackError"] = redact_text(str(rollback_error))
        result["state"] = "FAILED"
        result["error"] = redact_text(str(error))
        write_json(run_path / "reports" / "apply-result.json", result)
        raise
    finally:
        client.close()
        jump.close()
    write_json(run_path / "reports" / "apply-result.json", result)
    print(json.dumps(result, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
