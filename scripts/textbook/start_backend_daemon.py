"""Daemon 进程：前台阻塞运行 Spring Boot 后端，作为长期后台服务保持运行。"""
import os
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]

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

cmd = [
    str(java_bin),
    "-Xms512m",
    "-Xmx2048m",
    "-Dserver.port=8080",
    "-Dspring.quartz.auto-startup=false",
    "-jar",
    str(jar_path)
]

print(f"启动守护进程: {cmd}")
pid_file = ROOT / "output/textbook-cases/backend.pid"
pid_file.write_text(str(os.getpid()), encoding="utf-8")

# 阻塞运行，保持进程存活
subprocess.run(cmd, cwd=str(ROOT), env=env)
