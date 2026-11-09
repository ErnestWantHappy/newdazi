"""130 发布前只读侦察：主机、服务、制品、教材目录、数据库与磁盘。"""
import base64
import json
import re
from datetime import datetime
from pathlib import Path

import remote130

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-resources-20261107/remote-recon.json"


def main():
    jump, client, pw = remote130.connect()
    result = {"checkedAt": datetime.now().isoformat(timespec="seconds"), "commands": {}}
    commands = {
        "host": "hostname; date '+%F %T %Z'; uptime",
        "services": "systemctl list-units --type=service --no-pager | grep -iE 'xueyeceping|aiot|nginx' || true",
        "releases": "ls -1 /data/apps/xueyeceping/releases/",
        "releaseArtifacts": "for d in /data/apps/xueyeceping/releases/*/; do f=$(ls $d/backend/*.jar 2>/dev/null|head -1); i=$(ls $d/frontend/index.html 2>/dev/null|head -1); echo \"$d|$f|$(sha256sum $f 2>/dev/null|cut -d' ' -f1)|$i|$(sha256sum $i 2>/dev/null|cut -d' ' -f1)\"; done",
        "serviceUnit": "cat /etc/systemd/system/xueyeceping-130.service",
        "nginxConf": "cat /etc/nginx/sites-available/xueyeceping-130",
        "ports": "ss -lntp | grep -E ':(80|3009|3014)\\b' || true",
        "disk": "df -h / /data; timeout 20 du -sh /data/apps/xueyeceping/shared/textbook 2>/dev/null || echo TEXTBOOK_DU_TIMEOUT",
        "shared": "ls -la /data/apps/xueyeceping/shared/; readlink -f /data/apps/xueyeceping/shared/textbook; ls -la /data/apps/xueyeceping/shared/textbook | head -20",
        "envKeys": "for f in /data/apps/xueyeceping/shared/*.env /data/apps/xueyeceping/shared/config/*.yml; do [ -f \"$f\" ] && echo \"== $f\" && grep -E '^[A-Za-z_]+=' \"$f\" | cut -d= -f1; done",
        "configFiles": "find /data/apps/xueyeceping/releases -maxdepth 3 -name 'application*.yml' -o -maxdepth 3 -name '*.env' 2>/dev/null | sort",
        "running": "pgrep -af 'xueyeceping|ruoyi-admin' || true; pid=$(pgrep -f 'ruoyi-admin.jar' | head -1); [ -n \"$pid\" ] && tr '\\0' '\\n' < /proc/$pid/environ | grep -E 'TEXTBOOK_ROOT|SPRING_PROFILES|SERVER_PORT' || true",
        "currentLinks": "ls -l /data/apps/xueyeceping/current* 2>/dev/null || true; ls -l /etc/nginx/sites-enabled/ | grep xueyeceping || true",
        "textbookCounts": "ls -1 /data/apps/xueyeceping/shared/textbook/cases 2>/dev/null | wc -l; timeout 20 find /data/apps/xueyeceping/shared/textbook/cases -maxdepth 2 -type f 2>/dev/null | wc -l || true",
    }
    for name, command in commands.items():
        print("running", name, flush=True)
        encoded = base64.b64encode(command.encode("utf-8")).decode("ascii")
        code, out, err = remote130.run(client, "echo %s | base64 -d | timeout 60 bash" % encoded, timeout=90)
        result["commands"][name] = {"code": code, "out": out.strip(), "err": err.strip()[:500]}

    # 数据库 URL 只保留库名，密码与账号不出远程机。
    code, out, err = remote130.run(
        client,
        "for f in $(find /data/apps/xueyeceping/releases -maxdepth 3 -name 'application-druid.yml' | sort | tail -1); do echo \"== $f\"; "
        "grep -E 'url:|username:' \"$f\" | sed -E 's/(password|username)[^,]*/\\1: ***/I'; done", timeout=60)
    result["commands"]["database"] = {"code": code, "out": out.strip(), "err": err.strip()[:300]}

    OUT.write_text(json.dumps(result, ensure_ascii=False, indent=1), encoding="utf-8")
    print("written", OUT)
    for name in ("host", "services", "releases", "releaseArtifacts", "ports", "disk", "shared", "running", "textbookCounts", "database"):
        item = result["commands"].get(name, {})
        print("----", name, "code", item.get("code"))
        print(item.get("out", "")[:3000])
    client.close()
    jump.close()


if __name__ == "__main__":
    main()
