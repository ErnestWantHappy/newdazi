"""核对新收录包在课例面板中的用途/条件展示（桌面与 390px）。"""
import json
import re
from pathlib import Path

import requests
from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/playwright/textbook-cases"
API = "http://localhost:8080"
BASE = API + "/business/textbook/cases"
TITLE = "深度优先搜索 教学工具（学生端与教师端）"


def forward(route):
    try:
        route.fulfill(response=route.fetch(url=route.request.url.replace("http://localhost/dev-api", API, 1)))
    except Exception:
        try:
            route.abort()
        except Exception:
            pass


def main():
    line = next(line for line in (ROOT / "AGENTS.md").read_text(encoding="utf-8").splitlines() if "19157727791" in line and "|" in line)
    credentials = re.findall(r"`([^`]+)`", line)
    token = requests.post(API + "/login", json={"username": credentials[0], "password": credentials[1]}, timeout=30).json()["token"]
    headers = {"Authorization": "Bearer " + token}

    summary = requests.get(BASE + "/summary", headers=headers, timeout=30).json()["data"]
    located = None
    for row in summary:
        cases = requests.get(f"{BASE}/book/{row['bookId']}/section/{row['sectionKey']}", headers=headers, timeout=30).json()["data"]
        for case in cases:
            for resource in case["resources"]:
                if resource["title"] == TITLE:
                    located = (row, case, resource)
    assert located, "接口未返回新包"

    results = {}
    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(headless=True)
        for label, width in (("desktop", 1500), ("mobile", 390)):
            context = browser.new_context(viewport={"width": width, "height": 900})
            context.add_cookies([{"name": "Admin-Token", "value": token, "url": "http://localhost"}])
            page = context.new_page()
            page.route("**/dev-api/**", forward)
            row, case, resource = located
            page.goto(f"http://localhost/business/textbook/read/{row['bookId']}?section={row['sectionKey']}", wait_until="networkidle")
            page.wait_for_timeout(1200)
            if page.locator(".case-panel").count() == 0:
                page.get_by_role("button", name=re.compile("优秀课例")).first.click()
            page.wait_for_selector(".case-panel .resource", timeout=60000)
            target = page.locator(".case-panel .resource", has_text=TITLE).first
            if not target.is_visible():
                page.locator(".case-panel .el-collapse-item__header", has_text=case["title"]).first.click()
                page.wait_for_timeout(400)
            text = target.inner_text()
            results[label] = {
                "hasUsage": "用途" in text,
                "hasRequirement": "使用条件" in text,
                "hasDownload": "下载实验包" in text,
                "overflow": page.evaluate("document.documentElement.scrollWidth > innerWidth"),
            }
            page.screenshot(path=str(OUT / f"19-new-package-{label}.png"))
            context.close()
        browser.close()

    (OUT / "results-new-packages-ui.json").write_text(json.dumps(results, ensure_ascii=False, indent=2), encoding="utf-8")
    for label, value in results.items():
        assert value["hasUsage"] and value["hasRequirement"] and value["hasDownload"], results
        assert value["overflow"] is False, results
    print("新包面板：桌面/390px 均显示用途、使用条件与下载按钮，无横向溢出")


if __name__ == "__main__":
    main()
