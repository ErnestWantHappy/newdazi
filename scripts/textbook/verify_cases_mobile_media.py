"""390px 手机视口核对课例实验包说明与视频预览是否可用、是否横向溢出。"""
import json
import re
from pathlib import Path

import requests
from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/playwright/textbook-cases"
API = "http://localhost:8080"
BASE = API + "/business/textbook/cases"
TOOL_TITLE = "读书节推荐系统电子实验单"


def main():
    line = next(line for line in (ROOT / "AGENTS.md").read_text(encoding="utf-8").splitlines() if "19157727791" in line and "|" in line)
    credentials = re.findall(r"`([^`]+)`", line)
    login = requests.post(API + "/login", json={"username": credentials[0], "password": credentials[1]}, timeout=30).json()
    headers = {"Authorization": "Bearer " + login["token"]}

    summary = requests.get(BASE + "/summary", headers=headers, timeout=30).json()["data"]
    tool = None
    video = None
    for row in summary:
        cases = requests.get(f"{BASE}/book/{row['bookId']}/section/{row['sectionKey']}", headers=headers, timeout=30).json()["data"]
        for case in cases:
            for resource in case["resources"]:
                if resource["title"] == TOOL_TITLE:
                    tool = (row, case, resource)
                if video is None and resource["filename"].lower().endswith(".mp4") and int(resource["bytes"]) > 20 * 1048576:
                    video = (row, case, resource)
    assert tool and video, "缺少实验包或视频资源"

    results = {}
    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(headless=True)
        context = browser.new_context(viewport={"width": 390, "height": 844}, accept_downloads=True)
        context.add_cookies([{"name": "Admin-Token", "value": login["token"], "url": "http://localhost"}])
        page = context.new_page()
        errors = []
        page.on("pageerror", lambda error: errors.append(str(error)))

        def forward(route):
            route.fulfill(response=route.fetch(url=route.request.url.replace("http://localhost/dev-api", API, 1)))

        page.route("**/dev-api/**", forward)

        def open_cases(entry):
            row, case, resource = entry
            page.goto(f"http://localhost/business/textbook/read/{row['bookId']}?section={row['sectionKey']}", wait_until="networkidle")
            # 手机端目录收成浮层；有课例的课次会自动展开面板，只有未展开时才点顶栏按钮。
            page.wait_for_timeout(1200)
            if page.locator(".case-panel").count() == 0:
                page.get_by_role("button", name=re.compile("优秀课例")).first.click()
            page.wait_for_selector(".case-panel .resource", timeout=60000)
            target = page.locator(".case-panel .resource", has_text=resource["title"]).first
            if not target.is_visible():
                page.locator(".case-panel .el-collapse-item__header", has_text=case["author"] or case["title"]).first.click()
                page.wait_for_timeout(400)
            return target

        row_locator = open_cases(tool)
        assert "使用条件" in row_locator.inner_text(), "手机端实验包缺少使用条件"
        results["mobileToolNoOverflow"] = page.evaluate("document.documentElement.scrollWidth <= innerWidth")
        page.screenshot(path=str(OUT / "11-mobile-tool.png"))

        open_cases(video).locator("button", has_text="预览").click()
        page.wait_for_selector(".media-preview video", timeout=60000)
        page.wait_for_function("() => document.querySelector('.media-preview video')?.readyState >= 2", timeout=120000)
        box = page.evaluate("""() => {
            const video = document.querySelector('.media-preview video');
            return {width: Math.round(video.getBoundingClientRect().width), controls: video.controls,
                    viewport: innerWidth};
        }""")
        results["mobileVideo"] = {**box, "noOverflow": page.evaluate("document.documentElement.scrollWidth <= innerWidth")}
        assert box["width"] <= box["viewport"] and box["controls"], box
        page.screenshot(path=str(OUT / "12-mobile-video.png"))
        assert not errors, errors
        browser.close()

    (OUT / "results-mobile-media.json").write_text(json.dumps(results, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"手机端实验包无横向溢出={results['mobileToolNoOverflow']}；视频宽{results['mobileVideo']['width']}px"
          f"/视口{results['mobileVideo']['viewport']}px，控件可用，无溢出={results['mobileVideo']['noOverflow']}")


if __name__ == "__main__":
    main()
