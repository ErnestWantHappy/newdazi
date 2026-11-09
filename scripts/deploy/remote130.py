"""通过 123 跳板连接 130 的公共助手。凭据只从 secrets.local.md 读取，绝不打印。"""
import base64
import hashlib
import re
from pathlib import Path

import paramiko

ROOT = Path(__file__).resolve().parents[2]
SECRETS = ROOT / "contexts/secrets.local.md"
FP123 = "uXOH9wM5raU+ngVjFjZr3GtCK4rTL27ydlXCI3qR5yU"
FP130 = "DiFXqb+9fyOV/iZWLVSKjOe7UCPyX8lreNgjd20QfOI"


class Pinned(paramiko.MissingHostKeyPolicy):
    def __init__(self, expected):
        self.expected = expected

    def missing_host_key(self, client, hostname, key):
        actual = base64.b64encode(hashlib.sha256(key.asbytes()).digest()).decode().rstrip("=")
        if actual != self.expected:
            raise RuntimeError("ssh fingerprint mismatch for " + hostname)
        client.get_host_keys().add(hostname, key.get_name(), key)


def secrets():
    text = SECRETS.read_text(encoding="utf-8")
    pw123 = re.search(r"内网服务器 10\.52\.1\.123.*?Windows 密码 \| `([^`]+)`", text, re.S)
    pw130 = re.search(r"Ubuntu 扩展服务器 10\.52\.1\.130.*?SSH 密码：`([^`]+)`", text, re.S)
    if not pw123 or not pw130:
        raise RuntimeError("未能在 secrets.local.md 中定位 123/130 凭据")
    return pw123.group(1), pw130.group(1)


def connect():
    pw123, pw130 = secrets()
    jump = paramiko.SSHClient()
    jump.set_missing_host_key_policy(Pinned(FP123))
    jump.connect("10.52.1.123", username="Administrator", password=pw123,
                 look_for_keys=False, allow_agent=False, timeout=25)
    channel = jump.get_transport().open_channel("direct-tcpip", ("10.52.1.130", 22), ("127.0.0.1", 0))
    target = paramiko.SSHClient()
    target.set_missing_host_key_policy(Pinned(FP130))
    target.connect("10.52.1.130", username="admin2", password=pw130, sock=channel,
                   look_for_keys=False, allow_agent=False, timeout=25)
    return jump, target, pw130


def run(client, command, timeout=120):
    stdin, stdout, stderr = client.exec_command(command, timeout=timeout)
    code = stdout.channel.recv_exit_status()
    return code, stdout.read().decode("utf-8", "replace"), stderr.read().decode("utf-8", "replace")


def sudo(client, password, command, timeout=300):
    quoted = "'" + command.replace("'", "'\"'\"'") + "'"
    return run(client, "echo '%s' | sudo -S -p '' bash -lc %s" % (password, quoted), timeout)
