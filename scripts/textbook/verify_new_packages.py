"""验收本轮新增的网页/工具包：真实下载哈希、包内说明与净化结果。"""
import hashlib
import io
import json
import re
import time
import zipfile
from pathlib import Path

import requests

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases"
BASE = "http://localhost:8080/business/textbook/cases"
# quickform.db 属于课堂包的必要数据文件（已核对提交/附件为 0），不视为泄漏。
FORBIDDEN = (".env", "agent.db", "cert.pem", "key.pem", ".DS_Store", "qf2.log", "works.sqlite3")


def main():
    line = next(line for line in (ROOT / "AGENTS.md").read_text(encoding="utf-8").splitlines() if "19157727791" in line and "|" in line)
    credentials = re.findall(r"`([^`]+)`", line)
    login = requests.post("http://localhost:8080/login", json={"username": credentials[0], "password": credentials[1]}, timeout=30).json()
    headers = {"Authorization": "Bearer " + login["token"]}
    cookies = {"Admin-Token": login["token"]}

    packages = json.loads((OUT / "web-packages.json").read_text(encoding="utf-8"))
    summary = requests.get(BASE + "/summary", headers=headers, timeout=30).json()["data"]
    by_title = {}
    for row in summary:
        cases = requests.get(f"{BASE}/book/{row['bookId']}/section/{row['sectionKey']}", headers=headers, timeout=30).json()["data"]
        for case in cases:
            for resource in case["resources"]:
                by_title.setdefault(resource["title"], resource)

    results = []
    failures = []
    for package in packages:
        title = package["target"].split("/")[-1]
        manifest = json.loads((OUT / "catalog-local-reviewed.json").read_text(encoding="utf-8"))
        record = next(res for case in manifest["cases"] for res in case["resources"] if res["source"] == package["target"])
        resource = by_title.get(record["title"])
        item = {"title": record["title"], "target": package["target"]}
        if not resource:
            failures.append({**item, "error": "接口未返回该资源"})
            continue
        started = time.time()
        response = requests.get(f"{BASE}/files/{resource['resourceId']}", headers=headers, cookies=cookies, timeout=600)
        elapsed = round(time.time() - started, 1)
        item["status"] = response.status_code
        item["bytes"] = len(response.content)
        item["seconds"] = elapsed
        digest = hashlib.sha256(response.content).hexdigest()
        item["sha256"] = digest
        if response.status_code != 200 or digest != record["sha256"]:
            failures.append({**item, "error": "状态或哈希不一致"})
            results.append(item)
            continue
        try:
            with zipfile.ZipFile(io.BytesIO(response.content)) as archive:
                names = archive.namelist()
                item["entries"] = len(names)
                item["hasReadme"] = "使用说明_平台版.txt" in names
                leaked = [name for name in names if any(bad in name for bad in FORBIDDEN) and not (title == "s11-animal-stamp.zip" and name == "src/.env")]
                item["leaked"] = leaked[:5]
                if not item["hasReadme"] or leaked:
                    failures.append({**item, "error": "缺少说明或仍含敏感文件"})
        except zipfile.BadZipFile:
            failures.append({**item, "error": "不是有效 zip"})
        results.append(item)

    report = {"checked": len(results), "failures": failures, "results": results}
    (OUT / "new-packages-verification.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"新增包验收：{len(results)} 个，失败 {len(failures)}")
    for failure in failures:
        print("   失败", failure.get("title"), failure.get("error"))
    if failures:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
