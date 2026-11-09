"""在 130 上执行只读 SQL，便于现场核对表结构与数据。"""
import argparse
import base64

import remote130

DB_PREFIX = r"""set -e
cfg=/data/apps/xueyeceping/releases/20261008_seat_install_v1/config/application-druid.yml
u=$(awk '/username:/{print $2; exit}' "$cfg" | tr -d '"')
p=$(awk '/password:/{print $2; exit}' "$cfg" | tr -d '"')
umask 077
printf '[client]\nuser=%s\npassword=%s\nhost=127.0.0.1\n' "$u" "$p" >/tmp/.xyc-my.cnf
trap 'rm -f /tmp/.xyc-my.cnf' EXIT
mysql --defaults-extra-file=/tmp/.xyc-my.cnf --default-character-set=utf8mb4 -N -B ry-vue <<'SQL'
"""


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("sql")
    args = parser.parse_args()
    jump, client, _ = remote130.connect()
    try:
        script = DB_PREFIX + args.sql + "\nSQL\n"
        encoded = base64.b64encode(script.encode("utf-8")).decode("ascii")
        code, out, err = remote130.run(client, "echo %s | base64 -d | bash" % encoded, timeout=300)
        print(out, end="")
        if err:
            print(err, end="")
        return code
    finally:
        client.close()
        jump.close()


if __name__ == "__main__":
    raise SystemExit(main())
