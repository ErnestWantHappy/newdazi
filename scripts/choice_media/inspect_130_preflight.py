"""130 上线前只读补检：课堂空闲、私有图片目录可用性、Nginx 是否公开映射、备份与磁盘。

只发读命令和一次 `test -w` 判定的临时文件写入探测；不建正式目录、不备份、不迁移、不重启。
"""
import json
import re
import shlex
import sys
from datetime import datetime

from migrate_local import ROOT

sys.path.insert(0, str(ROOT / "scripts/deploy"))
import remote130

PROFILE = "/data/upload"
MEDIA_DIR = "/data/upload-question-images"
CONFIG_DIR = "/data/apps/xueyeceping/releases/20261008_seat_install_v1/config"


def main():
    jump, client, password = remote130.connect()
    out = {}

    def read(name, command, sudo=False):
        result = remote130.sudo(client, password, command) if sudo else remote130.run(client, command)
        code, text, error = result
        out[name] = {"exit": code, "stdout": text.strip(), "stderr": error.strip()[:800]}
        return code, text

    try:
        # 1) 课堂与抽测是否空闲：在答课程、近期提交、待批改作业
        sql = """
SELECT 'lessons_in_progress', COUNT(*) FROM biz_lesson WHERE status IN ('1','2','3');
SELECT 'answers_last_30min', COUNT(*) FROM biz_student_answer WHERE create_time >= DATE_SUB(NOW(), INTERVAL 30 MINUTE);
SELECT 'county_exam_running', COUNT(*) FROM biz_county_exam WHERE status IN ('1','2');
SELECT 'lessons_today', COUNT(*) FROM biz_lesson WHERE DATE(create_time)=CURDATE();
"""
        cfg = shlex.quote(f"{CONFIG_DIR}/application-druid.yml")
        command = f"""set -eu
cfg={cfg}
user=$(awk '/username:/{{print $2; exit}}' "$cfg" | tr -d '"')
pass=$(awk '/password:/{{print $2; exit}}' "$cfg" | tr -d '"')
export MYSQL_PWD="$pass"
printf %s {shlex.quote(sql)} | mysql -h 127.0.0.1 -u "$user" --default-character-set=utf8mb4 -N -B ry-vue
"""
        read("idle", command, sudo=True)

        # 2) profile 与私有同级目录
        read("profile_dir", f"ls -ld {shlex.quote(PROFILE)}; stat -c '%U:%G %a %n' {shlex.quote(PROFILE)}")
        read("media_dir", f"ls -ld {shlex.quote(MEDIA_DIR)} 2>&1 || echo NOT_EXISTS")
        read("media_parent", f"stat -c '%U:%G %a %n' {shlex.quote(PROFILE.rsplit('/',1)[0])}; id admin2")
        # 用模板字符串写入探测，末尾删除，不留残留
        probe = (f"d={shlex.quote(MEDIA_DIR)}; "
                 f"if [ -d \"$d\" ]; then t=$(mktemp -p \"$d\" probe.XXXXXX 2>&1) && echo DIR_WRITABLE && rm -f \"$t\"; "
                 f"else echo DIR_MISSING; fi")
        read("media_write_probe", probe)
        read("parent_write_probe", "p=/data; t=$(mktemp -p \"$p\" probe.XXXXXX 2>&1) && echo PARENT_WRITABLE && rm -f \"$t\"")
        read("upload_tree", f"du -sh {shlex.quote(PROFILE)}; find {shlex.quote(PROFILE)} -maxdepth 1 -mindepth 1 -printf '%f\\n' | sort | head -20")

        # 3) Nginx 是否把私有目录暴露到公网
        read("nginx_map", "nginx -T 2>/dev/null | grep -nE 'server_name|listen |location |alias |root '", sudo=True)
        read("nginx_media_ref", f"nginx -T 2>/dev/null | grep -n 'question-images' || echo NO_PUBLIC_MAPPING", sudo=True)

        # 4) 数据库与业务版本现状
        db_sql = """
SELECT 'cols', TABLE_NAME, COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND COLUMN_NAME='media_json' AND TABLE_NAME IN ('biz_question','biz_county_exam_paper_question');
SELECT 'media_tables', TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME LIKE 'biz_question_%';
SELECT 'questions', COUNT(*) FROM biz_question;
SELECT 'county_snapshots', COUNT(*) FROM biz_county_exam_paper_question;
SELECT 'update', update_id, version_no, status FROM biz_platform_update ORDER BY update_id DESC LIMIT 3;
"""
        read("database", f"""set -eu
cfg={cfg}
user=$(awk '/username:/{{print $2; exit}}' "$cfg" | tr -d '"')
pass=$(awk '/password:/{{print $2; exit}}' "$cfg" | tr -d '"')
export MYSQL_PWD="$pass"
printf %s {shlex.quote(db_sql)} | mysql -h 127.0.0.1 -u "$user" --default-character-set=utf8mb4 -N -B ry-vue
""", sudo=True)

        # 5) 备份、磁盘、release 完整性
        read("backups", "ls -1t /data/backups/xueyeceping | head -6; "
                        "f=$(ls -1t /data/backups/xueyeceping/*/database.sql.gz 2>/dev/null | head -1); "
                        "echo \"latest=$f\"; [ -n \"$f\" ] && stat -c 'bytes=%s' \"$f\" && sha256sum \"$f\" && gzip -t \"$f\" && echo GZIP_OK")
        read("disk", "df -h /data; df -i /data")
        read("releases", "ls -1t /data/apps/xueyeceping/releases | head -8")
        read("current", "systemctl show xueyeceping-130 -p ActiveState -p SubState -p MainPID -p NRestarts -p User; "
                        "ss -lnt | grep -E ':(80|3009)\\b'")
        read("hashes", "site=$(grep -rhm1 '^\\s*root ' /etc/nginx/sites-available/xueyeceping-130 | awk '{print $2}' | tr -d ';'); "
                       "echo \"root=$site\"; sha256sum \"$site/index.html\"; "
                       "jar=$(systemctl show xueyeceping-130 -p ExecStart | grep -o '/[^ ]*\\.jar' | head -1); "
                       "echo \"jar=$jar\"; sha256sum \"$jar\"; find \"$site\" -type f | wc -l")
        read("http", "for p in / /prod-api/captchaImage; do curl -s -o /dev/null -w \"$p %{http_code}\\n\" http://127.0.0.1$p; done")

        out["checkedAt"] = datetime.now().isoformat()
        out["productionWrite"] = False
        folder = ROOT / "output/choice-media"
        target = folder / "production-preflight-20261108.json"
        target.write_text(json.dumps(out, ensure_ascii=False, indent=2), encoding="utf-8")
        for name in ["idle", "media_dir", "media_write_probe", "parent_write_probe",
                     "nginx_media_ref", "database", "backups", "disk", "current", "hashes", "http"]:
            item = out[name]
            print(f"### {name} (exit {item['exit']})\n{item['stdout']}\n")
        print("已保存：" + str(target.relative_to(ROOT)))
    finally:
        client.close()
        jump.close()


if __name__ == "__main__":
    main()
