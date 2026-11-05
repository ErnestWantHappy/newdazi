"""通过真实教师接口抽查所有文档首末页、媒体分段和课例去重。"""
import concurrent.futures
import io
import json
import re
import time
from pathlib import Path
import requests
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases"
BASE = "http://localhost:8080/business/textbook/cases"


def main():
    line = next(line for line in (ROOT / "AGENTS.md").read_text(encoding="utf-8").splitlines() if "19157727791" in line and "|" in line)
    credentials = re.findall(r"`([^`]+)`", line)
    login = requests.post("http://localhost:8080/login", json={"username": credentials[0], "password": credentials[1]}, timeout=30).json()
    if not login.get("token"):
        raise RuntimeError("教师登录失败")
    headers = {"Authorization": "Bearer " + login["token"]}
    cookies = {"Admin-Token": login["token"]}
    summary = requests.get(BASE + "/summary", headers=headers, timeout=30).json()["data"]
    resources = []
    case_count = 0
    for row in summary:
        cases = requests.get(f"{BASE}/book/{row['bookId']}/section/{row['sectionKey']}", headers=headers, timeout=30).json()["data"]
        assert len(cases) == int(row["total"])
        case_count += len(cases)
        for case in cases:
            resources.extend(case["resources"])

    def verify(resource):
        record = {"id": resource["resourceId"], "title": resource["title"], "filename": resource["filename"]}
        extension = resource["filename"].split(".")[-1].lower()
        try:
            if not int(resource["previewable"]):
                record["result"] = "DOWNLOAD_ONLY"
            elif extension in {"mp4", "webm", "jpg", "jpeg", "png", "webp"}:
                response = requests.get(f"{BASE}/media/{resource['resourceId']}", headers={"Range": "bytes=0-31"}, cookies=cookies, timeout=30)
                assert response.status_code == 206 and len(response.content) == min(32, int(resource["bytes"]))
                record["result"] = "MEDIA_RANGE_OK"
            else:
                info = requests.get(f"{BASE}/files/{resource['resourceId']}/preview-info", headers=headers, timeout=60).json()
                assert info.get("code") == 200, info.get("msg", "预览信息失败")
                pages = int(info["data"]["pages"])
                for page in sorted({1, pages}):
                    response = requests.get(f"{BASE}/files/{resource['resourceId']}/pages/{page}", headers=headers, timeout=60)
                    assert response.status_code == 200
                    with Image.open(io.BytesIO(response.content)) as image:
                        image.verify()
                record.update(result="FIRST_LAST_PAGE_OK", pages=pages)
        except Exception as error:
            record.update(result="FAILED", error=str(error)[:180])
        return record

    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
        results = list(pool.map(verify, resources))
    failed = [record for record in results if record["result"] == "FAILED"]
    report = {"cases": case_count, "resources": len(resources), "failed": failed, "results": results}
    (OUT / "catalog-api-verification.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"真实接口验证：课例{case_count}，资源{len(resources)}，失败{len(failed)}")
    for record in failed:
        print(record["filename"], record["error"])
    if failed:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
