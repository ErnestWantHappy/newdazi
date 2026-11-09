"""130 教学资源发布：解包校验、执行迁移、切换服务与验收。

分步执行：extract -> sql -> switch -> verify。
所有写操作前已有 20261107_teaching_resources 备份；本脚本只打印非敏感证据。
"""
import argparse
import base64
import json
import sys
from pathlib import Path

import remote130

REL = "/data/apps/xueyeceping/releases/20261107_teaching_resources_v1"
OLD_REL = "/data/apps/xueyeceping/releases/20261107_python_server_run_v3"
OLD_CASES = "/data/apps/xueyeceping/releases/20261104_ningbo2025_cases_v1/resources/cases"
NEW_CASES = REL + "/resources/cases"
SHARED_CASES = "/data/apps/xueyeceping/shared/textbook/cases"
SERVICE = "/etc/systemd/system/xueyeceping-130.service"
NGINX = "/etc/nginx/sites-available/xueyeceping-130"
DRUID = "/data/apps/xueyeceping/releases/20261008_seat_install_v1/config/application-druid.yml"
# tar 由 13:00 最终预览转换后重建；cases.sha256 文件自身哈希为 814d0aa7。
TAR_SHA = "3c2cc2a5013e5779b52aaafc6b9a022e4a8fff2435f7780c305ea6f16fda2424"
OLD_TREE = "f3aebcbe0b428df2951b676f69d3e65de5d227e82cad0b59d827b46e4ec00234"


def shell(client, password, script, timeout=600, sudo=True):
    """整段脚本以 base64 传输，避免多层引号解析问题。"""
    encoded = base64.b64encode(script.encode("utf-8")).decode("ascii")
    command = "echo %s | base64 -d > /tmp/xyc_step.sh && %sbash /tmp/xyc_step.sh; rc=$?; rm -f /tmp/xyc_step.sh; exit $rc" % (
        encoded, ("echo '%s' | sudo -S -p '' " % password) if sudo else "")
    return remote130.run(client, command, timeout=timeout)


def mysql_script(sql):
    return """cfg=%s
user=$(awk '/username:/{print $2; exit}' "$cfg" | tr -d '"')
pass=$(awk '/password:/{print $2; exit}' "$cfg" | tr -d '"')
umask 077
printf '[client]\\nuser=%%s\\npassword=%%s\\nhost=127.0.0.1\\n' "$user" "$pass" > /tmp/.xyc-my.cnf
echo %s | base64 -d > /tmp/xyc.sql
mysql --defaults-extra-file=/tmp/.xyc-my.cnf --default-character-set=utf8mb4 -N -B ry-vue < /tmp/xyc.sql
rc=$?
rm -f /tmp/.xyc-my.cnf /tmp/xyc.sql
exit $rc
""" % (DRUID, base64.b64encode(sql.encode()).decode("ascii"))


CHECK_SQL = """SELECT CONCAT('cases=',COUNT(*)) FROM biz_textbook_case WHERE status='PUBLISHED';
SELECT CONCAT('resources=',COUNT(*)) FROM biz_textbook_case_resource;
SELECT CONCAT('feedback=',COUNT(*)) FROM biz_textbook_case_feedback;
SELECT CONCAT('books=',COUNT(*)) FROM biz_textbook_book_resource;
SELECT CONCAT('guides=',COUNT(*)) FROM biz_guide_grade;
SELECT CONCAT('lessons=',COUNT(*)) FROM biz_guide_lesson;
SELECT CONCAT('origin_',origin,'=',COUNT(*)) FROM biz_textbook_case GROUP BY origin;
SELECT CONCAT('menu=',menu_name) FROM sys_menu WHERE menu_id=26100;
SELECT CONCAT('dup_resource=',COUNT(*)) FROM (SELECT case_id,resource_key FROM biz_textbook_case_resource GROUP BY case_id,resource_key HAVING COUNT(*)>1) t;
"""


def cmd_extract(client, password):
    script = """set -e
cd %(rel)s
test "$(sha256sum cases.tar | cut -d' ' -f1)" = "%(tar_sha)s"
echo TAR_SHA_OK
before=$(find %(old)s -type f -printf '%%P %%s\\n' | sort | sha256sum | cut -d' ' -f1)
echo "old_before=$before"
tar --unlink-first -xf cases.tar -C %(new)s
echo TAR_EXTRACT_OK
cd %(new)s
sha256sum -c %(rel)s/cases.sha256 --quiet
echo CASES_SHA_OK
echo "files=$(find . -type f | wc -l)"
after=$(find %(old)s -type f -printf '%%P %%s\\n' | sort | sha256sum | cut -d' ' -f1)
echo "old_after=$after"
[ "$before" = "$after" ]
echo OLD_TREE_UNCHANGED
""" % {"rel": REL, "tar_sha": TAR_SHA, "old": OLD_CASES, "new": NEW_CASES}
    return shell(client, password, script, timeout=3600)


def cmd_coverage(client, password):
    sql = """SELECT original_path FROM biz_textbook_case_resource WHERE original_path IS NOT NULL
UNION SELECT preview_path FROM biz_textbook_case_resource WHERE preview_path IS NOT NULL
UNION SELECT extra_path FROM biz_textbook_case_resource WHERE extra_path IS NOT NULL
UNION SELECT original_path FROM biz_textbook_book_resource
UNION SELECT preview_path FROM biz_textbook_book_resource WHERE preview_path IS NOT NULL
UNION SELECT pdf_path FROM biz_guide_grade
UNION SELECT usage_path FROM biz_guide_grade WHERE usage_path IS NOT NULL
UNION SELECT original_path FROM biz_guide_lesson WHERE original_path IS NOT NULL;
"""
    encoded = base64.b64encode(mysql_script(sql).encode()).decode()
    command = "echo %s | base64 -d > /tmp/xyc_cov.sh && bash /tmp/xyc_cov.sh" % encoded
    code, out, err = remote130.run(client, command, timeout=300)
    if code != 0 or not out.strip():
        return code or 1, "", "路径查询失败：%s" % (err or out)[-400:]
    paths = [line.strip() for line in out.splitlines() if line.strip()]
    # 1000+ 路径不能塞进命令行，先经 SFTP 落盘再逐行检查。
    sftp = client.open_sftp()
    remote_list = "/tmp/xyc_resource_paths.txt"
    with sftp.open(remote_list, "w") as handle:
        handle.write("\n".join(paths) + "\n")
    sftp.close()
    check = "cd %s\nmissing=0\nwhile IFS= read -r p; do [ -z \"$p\" ] && continue; [ -f \"$p\" ] || { echo MISSING:$p; missing=$((missing+1)); }; done < %s\nrm -f %s\necho checked=%d missing=$missing\n" % (
        NEW_CASES, remote_list, remote_list, len(paths))
    return shell(client, password, check, timeout=600, sudo=False)


def cmd_sql(client, password):
    parts = []
    # 数据 SQL 约 600KB，走 base64 命令行会超出通道上限；改为远端文件重定向执行。
    for name in ("teaching_resources_20261107_v1.sql", "teaching_resources_20261107_data.sql"):
        script = """set -e
cfg=%s
user=$(awk '/username:/{print $2; exit}' "$cfg" | tr -d '"')
pass=$(awk '/password:/{print $2; exit}' "$cfg" | tr -d '"')
umask 077
printf '[client]\\nuser=%%s\\npassword=%%s\\nhost=127.0.0.1\\n' "$user" "$pass" > /tmp/.xyc-my.cnf
mysql --defaults-extra-file=/tmp/.xyc-my.cnf --default-character-set=utf8mb4 ry-vue < %s/sql/%s
rc=$?
rm -f /tmp/.xyc-my.cnf
exit $rc
""" % (DRUID, REL, name)
        code, out, err = shell(client, password, script, timeout=1800)
        parts.append({"file": name, "code": code, "out": out[-800:], "err": err[-500:]})
    code, out, err = shell(client, password, mysql_script(CHECK_SQL), timeout=300)
    parts.append({"file": "post-check", "code": code, "out": out, "err": err[-300:]})
    return parts


def cmd_switch(client, password):
    script = """set -e
stamp=$(date +%%Y%%m%%d_%%H%%M%%S)
sed -i 's#%(old_rel)s#%(rel)s#g' %(service)s
grep -q '%(rel)s' %(service)s
sed -i 's#root %(old_rel)s/frontend;#root %(rel)s/frontend;#' %(nginx)s
grep -q 'root %(rel)s/frontend;' %(nginx)s
ln -s %(new_cases)s %(shared)s.new
mv -Tf %(shared)s.new %(shared)s
readlink %(shared)s
nginx -t
systemctl daemon-reload
systemctl restart xueyeceping-130
systemctl reload nginx
echo SWITCH_OK
""" % {"rel": REL, "old_rel": OLD_REL, "service": SERVICE, "nginx": NGINX,
       "new_cases": NEW_CASES, "shared": SHARED_CASES}
    return shell(client, password, script, timeout=600)


def cmd_health(client, password):
    script = """for i in $(seq 1 30); do
  code=$(curl -s -o /dev/null -w '%%{http_code}' http://127.0.0.1/prod-api/captchaImage)
  [ "$code" = "200" ] && break
  sleep 3
done
echo "captcha=$code"
echo "home=$(curl -s -o /dev/null -w '%%{http_code}' http://127.0.0.1/)"
sha256sum %(rel)s/frontend/index.html
readlink -f %(shared)s
systemctl is-active xueyeceping-130
ss -lntp | grep ':3009'
""" % {"rel": REL, "shared": SHARED_CASES}
    return shell(client, password, script, timeout=300, sudo=False)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("step", choices=("extract", "coverage", "sql", "switch", "health", "check"))
    args = parser.parse_args()
    jump, client, password = remote130.connect()
    try:
        if args.step == "extract":
            result = cmd_extract(client, password)
            print(result[1])
            print(result[2][-500:], file=sys.stderr)
            return result[0]
        if args.step == "coverage":
            result = cmd_coverage(client, password)
            print(result[1])
            print(result[2][-500:], file=sys.stderr)
            return result[0]
        if args.step == "sql":
            json.dump(cmd_sql(client, password), sys.stdout, ensure_ascii=False, indent=1)
            print()
            return 0
        if args.step == "switch":
            result = cmd_switch(client, password)
            print(result[1])
            print(result[2][-800:], file=sys.stderr)
            return result[0]
        if args.step == "health":
            result = cmd_health(client, password)
            print(result[1])
            print(result[2][-300:], file=sys.stderr)
            return result[0]
        if args.step == "check":
            result = shell(client, password, mysql_script(CHECK_SQL), timeout=300)
            print(result[1])
            print(result[2][-300:], file=sys.stderr)
            return result[0]
    finally:
        client.close()
        jump.close()


if __name__ == "__main__":
    raise SystemExit(main())
