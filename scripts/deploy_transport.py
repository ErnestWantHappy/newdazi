"""固定主机指纹、双流读取和硬超时，避免 SSH 卡死或凭据进入命令行。"""
import base64
import hashlib
import re
import time
from pathlib import Path
import paramiko

REPO = Path(__file__).resolve().parents[1]
HOST = '10.52.1.123'
FINGERPRINT = 'uXOH9wM5raU+ngVjFjZr3GtCK4rTL27ydlXCI3qR5yU'


def secret(pattern):
    match = re.search(pattern, (REPO / 'contexts/secrets.local.md').read_text(encoding='utf-8'), re.S)
    if not match:
        raise RuntimeError('私密配置缺少所需凭据，未尝试猜测')
    return match.group(1)


class PinnedPolicy(paramiko.MissingHostKeyPolicy):
    def missing_host_key(self, client, hostname, key):
        actual = base64.b64encode(hashlib.sha256(key.asbytes()).digest()).decode().rstrip('=')
        if actual != FINGERPRINT:
            raise RuntimeError('服务器 SSH 指纹不匹配')


class Remote:
    def __enter__(self):
        self.client = paramiko.SSHClient()
        self.client.set_missing_host_key_policy(PinnedPolicy())
        self.client.connect(HOST, username='Administrator',
                            password=secret(r'内网服务器 10\.52\.1\.123.*?Windows 密码 \| `([^`]+)`'),
                            look_for_keys=False, allow_agent=False, timeout=12,
                            auth_timeout=12, banner_timeout=12)
        self.sftp = self.client.open_sftp()
        self.sftp.get_channel().settimeout(30)
        return self

    def __exit__(self, *args):
        self.sftp.close()
        self.client.close()

    def ps(self, script, timeout=45):
        prefix = "$ProgressPreference='SilentlyContinue';$ErrorActionPreference='Stop';[Console]::OutputEncoding=[Text.UTF8Encoding]::new($false);"
        encoded = base64.b64encode((prefix + script).encode('utf-16le')).decode()
        channel = self.client.get_transport().open_session(timeout=12)
        channel.exec_command('powershell.exe -NoLogo -NoProfile -NonInteractive -EncodedCommand ' + encoded)
        end = time.monotonic() + timeout
        out, err = bytearray(), bytearray()
        try:
            while True:
                while channel.recv_ready():
                    out.extend(channel.recv(65536))
                while channel.recv_stderr_ready():
                    err.extend(channel.recv_stderr(65536))
                if channel.exit_status_ready() and not channel.recv_ready() and not channel.recv_stderr_ready():
                    break
                if time.monotonic() > end:
                    raise TimeoutError('远程调用超时；先查询任务状态，不自动重复执行')
                time.sleep(.05)
            if channel.recv_exit_status() != 0:
                # 原始异常可能包含外置配置和口令，不把远端 stderr 直接带入聊天。
                raise RuntimeError('远程脚本失败，请检查该任务的脱敏状态文件')
            return out.decode('utf-8', errors='replace').strip()
        finally:
            channel.close()

    def read(self, path):
        with self.sftp.open(path, 'rb') as f:
            return f.read()

    def write(self, path, data):
        with self.sftp.open(path, 'wb') as f:
            f.write(data)
