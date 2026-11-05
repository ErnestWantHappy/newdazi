"""只读：用系统 tar.exe 列出两个 RAR 的条目，确认是否可读。"""
import subprocess
import sys
from pathlib import Path

ROOT = Path(r"E:/2026年宁波市义教信息科技学科新教材优秀课例展示")
TARGETS = [
    "七年级/七年级上册/江北区-第19课《智能体的搭建与应用》/智能体.rar",
    "七年级/七年级下册/慈溪市-第18课《深度优先搜索算法》/教学工具 源程序代码.rar",
]
TAR = r"C:\Windows\System32\tar.exe"

for relative in TARGETS:
    path = ROOT / relative
    print("==", relative, path.stat().st_size if path.exists() else "缺失")
    proc = subprocess.run([TAR, "-tf", str(path)], capture_output=True)
    out = proc.stdout.decode("utf-8", "replace")
    err = proc.stderr.decode("utf-8", "replace")
    lines = [line for line in out.splitlines() if line.strip()]
    print("exit", proc.returncode, "entries", len(lines))
    for line in lines[:12]:
        print("   ", line)
    if err.strip():
        print("stderr", err[:200])
