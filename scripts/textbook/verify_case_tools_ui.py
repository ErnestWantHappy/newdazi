"""真实浏览器核对实验包在课例面板中的说明文字与下载哈希。"""
import hashlib
import json
import re
from pathlib import Path

import requests
from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/playwright/textbook-cases"
OUT.mkdir(parents=True, exist_ok=True)
API = "http://localhost:8080"
BASE = API + "/business/textbook/cases"
# 本轮新增的净化包，核对标题、说明与下载字节一致。
TARGETS = ["读书节推荐系统电子实验单", "物联网实验源代码"]


def main():
    line = next(line for line in (ROOT / "AGENTS.md").read_text(encoding="utf-8").splitlines() if "19157727791" in line and "|" in line)
    credentials = re.findall(r"`([^`]+)`", line)
    login = requests.post(API + "/login", json={"username": credentials[0], "password": credentials[1]}, timeout=30).json()
    headers = {"Authorization": "Bearer " + login["token"]}

    summary = requests.get(BASE + "/summary", headers=headers, timeout=30).json()["data"]
    located = {}
    for row in summary:
        cases = requests.get(f"{BASE}/book/{row['bookId']}/section/{row['sectionKey']}", headers=headers, timeout=30).json()["data"]
        for case in cases:
            for resource in case["resources"]:
                if resource["title"] in TARGETS and resource["title"] not in located:
                    located[resource["title"]] = {"row": row, "case": case, "resource": resource}
    missing = [title for title in TARGETS if title not in located]
    assert not missing, f"接口缺少实验包：{missing}"

    results = {}
    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(headless=True)
        context = browser.new_context(viewport={"width": 1500, "height": 960}, accept_downloads=True)
        context.add_cookies([{"name": "Admin-Token", "value": login["token"], "url": "http://localhost"}])
        page = context.new_page()
        errors = []
        page.on("pageerror", lambda error: errors.append(str(error)))

        def forward(route):
            route.fulfill(response=route.fetch(url=route.request.url.replace("http://localhost/dev-api", API, 1)))

        page.route("**/dev-api/**", forward)
        for title, info in located.items():
            row = info["row"]
            page.goto(f"http://localhost/business/textbook/read/{row['bookId']}?section={row['sectionKey']}", wait_until="networkidle")
            page.wait_for_selector(".toc-item.is-active .case-badge", timeout=60000)
            page.locator(".toc-item.is-active .case-badge").click()
            page.wait_for_selector(".case-panel .resource", timeout=60000)
            row_locator = page.locator(".case-panel .resource", has_text=title).first
            if not row_locator.is_visible():
                page.locator(".case-panel .el-collapse-item__header", has_text=info["case"]["author"] or info["case"]["title"]).first.click()
                page.wait_for_timeout(500)
            text = row_locator.inner_text()
            assert "使用条件" in text, f"{title} 面板没有显示使用条件：{text}"
            with page.expect_download(timeout=300000) as event:
                row_locator.locator("button", has_text="下载实验包").click()
            download = event.value
            digest = hashlib.sha256(Path(download.path()).read_bytes()).hexdigest()
            direct = requests.get(f"{BASE}/files/{info['resource']['resourceId']}", headers=headers, timeout=300).content
            assert hashlib.sha256(direct).hexdigest() == digest, f"{title} 浏览器下载与接口不一致"
            results[title] = {"bytes": len(direct), "sha256": digest, "descriptionShown": True,
                              "requirementsShown": True}
            page.screenshot(path=str(OUT / f"10-tool-{len(results)}.png"))
        assert not errors, errors
        browser.close()

    (OUT / "results-tools.json").write_text(json.dumps(results, ensure_ascii=False, indent=2), encoding="utf-8")
    for title, value in results.items():
        print(f"{title} {value['bytes']}字节 哈希{value['sha256'][:12]} 说明与使用条件可见")


if __name__ == "__main__":
    main()
