"""启动本机测试后端，安全读取主密钥，设置 TEXTBOOK_ROOT，监听 8080 并记录 PID。"""
import os
import re
import socket
import subprocess
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def check_port(port=8080):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.settimeout(0.5)
        return s.connect_ex(('127.0.0.1', port)) == 0


def main():
    if check_port(8080):
        raise RuntimeError("端口 8080 已被占用，请先停止冲突进程")

    # 安全读取主密钥
    secrets_file = ROOT / "contexts/secrets.local.md"
    master_key = None
    if secrets_file.is_file():
        for line in secrets_file.read_text(encoding="utf-8").splitlines():
            m = re.search(r"PRACTICAL_AI_MASTER_KEY\s*[:：=]\s*`?([^`\s]+)", line)
            if m:
                master_key = m.group(1).strip()
                break

    if not master_key:
        raise RuntimeError("未在 contexts/secrets.local.md 中找到 PRACTICAL_AI_MASTER_KEY")

    env = os.environ.copy()
    env["PRACTICAL_AI_MASTER_KEY"] = master_key
    env["TEXTBOOK_ROOT"] = str((ROOT / "output/textbook-pack-20261029/pages").resolve())

    jar_path = (ROOT / "RuoYi-Vue/ruoyi-admin/target/ruoyi-admin.jar").resolve()
    java_bin = Path("C:/Users/PC/.jdks/corretto-1.8.0_422/bin/java.exe")
    if not java_bin.is_file():
        java_bin = "java"

    stdout_log = (ROOT / "output/textbook-cases/backend.stdout.log").open("wb")
    stderr_log = (ROOT / "output/textbook-cases/backend.stderr.log").open("wb")

    cmd = [
        str(java_bin),
        "-Xms512m",
        "-Xmx2048m",
        "-Dserver.port=8080",
        "-Dspring.quartz.auto-startup=false",
        "-jar",
        str(jar_path)
    ]

    print(f"正在启动后端进程: port=8080, jar={jar_path.name} ...")
    DETACHED_PROCESS = 0x00000008
    CREATE_NEW_PROCESS_GROUP = 0x00000200
    proc = subprocess.Popen(
        cmd,
        cwd=str(ROOT),
        env=env,
        stdout=stdout_log,
        stderr=stderr_log,
        creationflags=DETACHED_PROCESS | CREATE_NEW_PROCESS_GROUP if os.name == 'nt' else 0
    )

    pid_file = ROOT / "output/textbook-cases/backend.pid"
    pid_file.write_text(str(proc.pid), encoding="utf-8")
    print(f"后端进程已启动，PID={proc.pid}，正在等待端口 8080 监听...")

    # 等待端口监听，最多 45 秒
    started = False
    for i in range(45):
        time.sleep(1)
        if check_port(8080):
            started = True
            break
        if proc.poll() is not None:
            raise RuntimeError(f"后端进程异常退出，exitCode={proc.returncode}，请检查 backend.stderr.log")

    if not started:
        raise RuntimeError("后端启动超时 (45秒内未监听 8080)")

    print(f"后端启动成功！PID={proc.pid}，已监听 http://localhost:8080")


if __name__ == "__main__":
    main()
