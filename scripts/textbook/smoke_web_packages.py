"""对零依赖网页工程做真实启动探活：解包、起服务、访问首页、关闭，不修改原始素材。

只验证本机可启动性；需要第三方依赖或外部令牌的包不在此列。
"""
import json
import os
import shutil
import subprocess
import time
import zipfile
from pathlib import Path

import requests

ROOT = Path(r"d:/dmwprogram/newdazipingtai/newdazi")
OUT = ROOT / "output/textbook-cases"
PACKAGES = OUT / "release-materials/combined/packages"
WORK = OUT / "runtime-check"
PYTHON = r"C:/Users/PC/AppData/Local/Programs/Python/Python310/python.exe"

CASES = [
    {"zip": "s11-dream-wall.zip", "cwd": "", "cmd": [PYTHON, "server.py"], "env": {"WALL_PORT": "8771"}, "url": "http://127.0.0.1:8771/"},
    {"zip": "g6-18-plant-agent.zip", "cwd": "", "cmd": [PYTHON, "serve_classroom.py", "--port", "8871"], "env": {}, "url": "http://127.0.0.1:8871/"},
    {"zip": "w18-dfs-teaching-tool.zip", "cwd": "", "cmd": ["node", "server.js"], "env": {"PORT": "3371"}, "url": "http://127.0.0.1:3371/"},
    {"zip": "s11-film-vote.zip", "cwd": "", "cmd": ["node", "server.mjs"], "env": {"PORT": "8091"}, "url": "http://127.0.0.1:8091/"},
]


def main():
    results = []
    for case in CASES:
        slug = case["zip"].replace(".zip", "")
        folder = WORK / f"probe-{slug}"
        if folder.exists():
            shutil.rmtree(folder, ignore_errors=True)
        folder.mkdir(parents=True)
        with zipfile.ZipFile(PACKAGES / case["zip"]) as archive:
            archive.extractall(folder)
        env = {**os.environ, **case["env"]}
        log_path = WORK / f"probe-{slug}.log"
        with open(log_path, "wb") as log:
            process = subprocess.Popen(case["cmd"], cwd=folder, env=env, stdout=log, stderr=subprocess.STDOUT)
        item = {"zip": case["zip"]}
        try:
            deadline = time.time() + 40
            response = None
            while time.time() < deadline:
                try:
                    response = requests.get(case["url"], timeout=3)
                    if response.status_code == 200:
                        break
                except requests.RequestException:
                    time.sleep(1.5)
            item["status"] = getattr(response, "status_code", None)
            item["bytes"] = len(response.content) if response is not None else 0
            item["ok"] = bool(response is not None and response.status_code == 200)
            if not item["ok"]:
                item["log"] = log_path.read_text(encoding="utf-8", errors="replace")[-400:]
        finally:
            process.terminate()
            try:
                process.wait(timeout=10)
            except subprocess.TimeoutExpired:
                process.kill()
        results.append(item)
        print(case["zip"], "OK" if item["ok"] else "FAILED", item["status"], item["bytes"])

    (OUT / "web-packages-smoke.json").write_text(json.dumps(results, ensure_ascii=False, indent=2), encoding="utf-8")
    failed = [item for item in results if not item["ok"]]
    if failed:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
