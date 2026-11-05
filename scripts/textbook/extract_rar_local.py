"""只读处理两个 RAR：用系统 tar.exe 列目录并解到工作区临时目录核对。

不执行包内程序；输出只包含条目名、大小与敏感文件类别，不打印凭据值。
"""
import shutil
import subprocess
from pathlib import Path

ROOT = Path(r"E:/2026年宁波市义教信息科技学科新教材优秀课例展示")
WORK = Path(r"d:/dmwprogram/newdazipingtai/newdazi/output/textbook-cases/rar-work")
TAR = r"C:\Windows\System32\tar.exe"
TARGETS = {
    "agent": "七年级/七年级上册/江北区-第19课《智能体的搭建与应用》/智能体.rar",
    "dfs": "七年级/七年级下册/慈溪市-第18课《深度优先搜索算法》/教学工具 源程序代码.rar",
}


def decode(raw):
    for encoding in ("utf-8", "gbk"):
        try:
            return raw.decode(encoding)
        except UnicodeDecodeError:
            continue
    return raw.decode("utf-8", "replace")


def main():
    if WORK.exists():
        shutil.rmtree(WORK)
    for slug, relative in TARGETS.items():
        archive = ROOT / relative
        listing = subprocess.run([TAR, "-tf", str(archive)], capture_output=True)
        names = [line for line in decode(listing.stdout).splitlines() if line.strip()]
        unsafe = [name for name in names if name.startswith(("/", "\\")) or ".." in Path(name).parts]
        print(f"[{slug}] 条目 {len(names)}，路径穿越 {len(unsafe)}")
        destination = WORK / slug
        destination.mkdir(parents=True)
        extract = subprocess.run([TAR, "-xf", str(archive), "-C", str(destination)], capture_output=True)
        print(f"[{slug}] 解压退出码 {extract.returncode}")
        files = sorted(path for path in destination.rglob("*") if path.is_file())
        total = sum(path.stat().st_size for path in files)
        print(f"[{slug}] 解出文件 {len(files)}，合计 {total/1048576:.1f}MiB")
        for path in files:
            print("   ", path.relative_to(destination).as_posix(), path.stat().st_size)
        if unsafe:
            raise RuntimeError(f"{slug} 存在可疑路径：{unsafe[:3]}")


if __name__ == "__main__":
    main()
