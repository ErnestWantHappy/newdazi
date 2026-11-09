"""教师工具发布前只读侦察：服务、制品、数据库结构与数据现状。"""
import base64
import json
from datetime import datetime
from pathlib import Path

import remote130

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/teacher-tools-20261108/remote-recon.json"

DB_PREFIX = r"""set -e
cfg=/data/apps/xueyeceping/releases/20261008_seat_install_v1/config/application-druid.yml
u=$(awk '/username:/{print $2; exit}' "$cfg" | tr -d '"')
p=$(awk '/password:/{print $2; exit}' "$cfg" | tr -d '"')
umask 077
printf '[client]\nuser=%s\npassword=%s\nhost=127.0.0.1\n' "$u" "$p" >/tmp/.xyc-my.cnf
trap 'rm -f /tmp/.xyc-my.cnf' EXIT
mysql --defaults-extra-file=/tmp/.xyc-my.cnf --default-character-set=utf8mb4 -N -B ry-vue <<'SQL'
"""

DB_SUFFIX = "\nSQL\n"


def db_command(sql):
    script = DB_PREFIX + sql + DB_SUFFIX
    encoded = base64.b64encode(script.encode("utf-8")).decode("ascii")
    return "echo %s | base64 -d | bash" % encoded


def main():
    jump, client, password = remote130.connect()
    result = {"checkedAt": datetime.now().isoformat(timespec="seconds"), "commands": {}}
    commands = {
        "host": "hostname; date '+%F %T %Z'; uptime; who",
        "services": "systemctl list-units --type=service --no-pager | grep -iE 'xueyeceping|aiot|nginx' || true",
        "status": "systemctl status xueyeceping-130 --no-pager -n 5 || true",
        "releases": "ls -1dt /data/apps/xueyeceping/releases/*/ | head -12",
        "releaseArtifacts": "for d in $(ls -dt /data/apps/xueyeceping/releases/*/ | head -5); do f=$(ls $d/backend/*.jar 2>/dev/null|head -1); i=$(ls $d/frontend/index.html 2>/dev/null|head -1); echo \"$d|$(stat -c '%n %s %y' \"$f\" 2>/dev/null)|$(stat -c '%n %s %y' \"$i\" 2>/dev/null)\"; done",
        "serviceUnit": "cat /etc/systemd/system/xueyeceping-130.service",
        "nginxConf": "cat /etc/nginx/sites-available/xueyeceping-130",
        "ports": "ss -lntp | grep -E ':(80|3009|3014)\\b' || true",
        "disk": "df -h / /data; free -h",
        "running": "pgrep -af 'ruoyi-admin.jar' || true; pid=$(pgrep -f 'ruoyi-admin.jar' | head -1); [ -n \"$pid\" ] && echo PID=$pid && tr '\\0' '\\n' < /proc/$pid/environ | grep -E 'TEXTBOOK_ROOT|SPRING_PROFILES|SERVER_PORT' || true",
        "currentArtifact": "jar=$(grep -oE '/data/apps/[^ ]+\\.jar' /etc/systemd/system/xueyeceping-130.service | head -1); echo \"jar=$jar\"; [ -f \"$jar\" ] && sha256sum \"$jar\"; root=$(awk '/root .*frontend;/{print $2; exit}' /etc/nginx/sites-available/xueyeceping-130); echo \"root=$root\"; [ -f \"$root/index.html\" ] && sha256sum \"$root/index.html\"",
        "currentLinks": "ls -l /data/apps/xueyeceping/current* 2>/dev/null || true; ls -l /etc/nginx/sites-enabled/ | grep xueyeceping || true",
        "configFiles": "find /data/apps/xueyeceping/releases -maxdepth 3 -name 'application*.yml' 2>/dev/null | sort",
        "dbConfig": "cfg=/data/apps/xueyeceping/releases/20261008_seat_install_v1/config/application-druid.yml; sed -n '/url:/p;/username:/p' \"$cfg\" | sed -E 's#(jdbc:[^?]+).*#\\1***#; s/(username:).*/\\1 ***/'",
    }
    commands["sqlColumnsTool"] = db_command("""
SHOW COLUMNS FROM biz_teacher_tool;
SELECT CONCAT('task_table=', COUNT(*)) FROM information_schema.tables
 WHERE table_schema=DATABASE() AND table_name='biz_teacher_tool_task';
SELECT CONCAT('term_table=', COUNT(*)) FROM information_schema.tables
 WHERE table_schema=DATABASE() AND table_name='biz_teacher_tool_term';
SELECT CONCAT('tools=', COUNT(*)) FROM biz_teacher_tool WHERE del_flag='0';
SELECT CONCAT('categories=', COUNT(*)) FROM biz_teacher_tool_category WHERE del_flag='0';
SELECT CONCAT('categories_enabled=', COUNT(*)) FROM biz_teacher_tool_category WHERE del_flag='0' AND status='0';
SELECT CONCAT('recommended=', COUNT(*)) FROM biz_teacher_tool WHERE del_flag='0' AND recommended=1;
""")
    commands["sqlCounts"] = db_command("""
SELECT CONCAT('rel_rows=', COUNT(*)) FROM biz_teacher_tool_category;
SELECT CONCAT('menu=', menu_name, '/', perms, '/', status) FROM sys_menu
 WHERE perms LIKE 'business:teacher-tools%' OR path LIKE 'teacher-tools%' ORDER BY menu_id;
SELECT CONCAT('update=', version, '/', status, '/', title) FROM biz_platform_update ORDER BY id DESC LIMIT 5;
""")
    commands["sqlDup"] = db_command("""
SELECT CONCAT('dup_rel=', COUNT(*)) FROM
 (SELECT tool_id,category_id FROM biz_teacher_tool_category GROUP BY tool_id,category_id HAVING COUNT(*)>1) t;
""")
    for name, command in commands.items():
        print("running", name, flush=True)
        code, out, err = remote130.run(client, command, timeout=120)
        result["commands"][name] = {"code": code, "out": out.strip(), "err": err.strip()[:800]}

    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(result, ensure_ascii=False, indent=1), encoding="utf-8")
    print("written", OUT)
    for name in ("host", "services", "status", "releaseArtifacts", "serviceUnit", "nginxConf", "ports", "disk", "running", "dbConfig", "sqlColumnsTool", "sqlCounts", "sqlDup"):
        item = result["commands"].get(name, {})
        print("----", name, "code", item.get("code"))
        print(item.get("out", "")[:8000])
        if item.get("err"):
            print("STDERR:", item["err"][:1000])
    client.close()
    jump.close()


if __name__ == "__main__":
    main()
