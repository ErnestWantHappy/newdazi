"""130只读前检：不上传、不建目录、不备份、不迁移、不重启。"""
import hashlib
import json
import re
import shlex
import sys
from datetime import datetime

from migrate_local import ROOT

sys.path.insert(0, str(ROOT / "scripts/deploy"))
import remote130


def main():
    jump, client, password = remote130.connect()
    output = {}

    def read(name, command, sudo=False):
        result = remote130.sudo(client, password, command) if sudo else remote130.run(client, command)
        code, text, error = result
        if code:
            raise RuntimeError(f"{name}检查失败，退出码{code}")
        output[name] = text.strip()
        return text

    try:
        service = read("service", "systemctl show xueyeceping-130 -p ActiveState -p SubState -p MainPID -p NRestarts -p User -p WorkingDirectory -p ExecStart")
        nginx = read("nginx", "cat /etc/nginx/sites-available/xueyeceping-130")
        frontend = re.search(r"^\s*root\s+([^;]+);", nginx, re.M).group(1)
        backend = re.search(r"-jar\s+(\S+\.jar)", service).group(1)
        read("hashes", f"sha256sum {shlex.quote(frontend + '/index.html')} {shlex.quote(backend)}")
        read("disk", "df -h /data; df -i /data")
        read("backups", "find /data/backups/xueyeceping -maxdepth 2 -type f -name 'database.sql*' -printf '%TY-%Tm-%Td %TH:%TM %s %p\\n' | sort | tail -5")
        read("ports", "ss -lnt | grep -E ':(80|3009)\\b'")
        read("http", "for path in / /prod-api/captchaImage; do curl -s -o /dev/null -w \"$path %{http_code}\\n\" http://127.0.0.1$path; done")
        sql = """
SELECT DATABASE();
SELECT TABLE_NAME,COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN ('biz_question','biz_county_exam_paper_question') AND COLUMN_NAME='media_json';
SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN ('biz_question_image','biz_question_save_receipt','biz_question_import_batch','biz_question_import_item');
SELECT update_id,version_no,status FROM biz_platform_update ORDER BY update_id DESC LIMIT 3;
"""
        config = "/data/apps/xueyeceping/releases/20261008_seat_install_v1/config/application-druid.yml"
        command = f"""set -eu
cfg={shlex.quote(config)}
user=$(awk '/username:/{{print $2; exit}}' "$cfg" | tr -d '"')
pass=$(awk '/password:/{{print $2; exit}}' "$cfg" | tr -d '"')
export MYSQL_PWD="$pass"
printf %s {shlex.quote(sql)} | mysql -h 127.0.0.1 -u "$user" --default-character-set=utf8mb4 -N -B ry-vue
"""
        read("database", command, sudo=True)
        profile = read("profile", "grep -h -A2 'profile:' /data/apps/xueyeceping/releases/20261008_seat_install_v1/config/application.yml")
        read("nginx_private_check", "nginx -T 2>/dev/null | grep -nE 'profile|uploadPath|question-images|alias|root '", sudo=True)
        output["checkedAt"] = datetime.now().isoformat()
        output["productionWrite"] = False
        folder = ROOT / "output/choice-media"
        (folder / "production-readonly-20261108.json").write_text(
            json.dumps(output, ensure_ascii=False, indent=2), encoding="utf-8"
        )
        for name in ["service", "hashes", "disk", "database", "profile", "http"]:
            print(name + ":\n" + output[name])
        print("只读前检已保存；课堂空闲、目录写权限与迁移演练尚待补验。")
    finally:
        client.close()
        jump.close()


if __name__ == "__main__":
    main()
