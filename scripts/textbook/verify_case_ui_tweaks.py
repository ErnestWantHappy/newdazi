"""验收两项界面小修：面板内行内下载提示、宽屏预览放大有效。"""
import json
import re
from pathlib import Path

import requests
from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/playwright/textbook-cases"
API = "http://localhost:8080"
BASE = API + "/business/textbook/cases"
TARGET = {"book": 12, "section": "lesson-18"}


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

    results = {}
    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(headless=True)

        def open_panel(page):
            page.goto(f"http://localhost/business/textbook/read/{TARGET['book']}?section={TARGET['section']}", wait_until="networkidle")
            page.wait_for_timeout(1200)
            if page.locator(".case-panel").count() == 0:
                page.get_by_role("button", name=re.compile("优秀课例")).first.click()
            page.wait_for_selector(".case-panel .resource", timeout=60000)

        # 1) 桌面：下载后出现面板内提示，不再弹全局消息。
        context = browser.new_context(viewport={"width": 1500, "height": 900}, accept_downloads=True)
        context.add_cookies([{"name": "Admin-Token", "value": token, "url": "http://localhost"}])
        page = context.new_page()
        page.route("**/dev-api/**", forward)
        open_panel(page)
        page.locator(".case-panel .resource button", has_text=re.compile("下载")).first.click()
        page.wait_for_selector(".download-hint", timeout=15000)
        results["desktopHint"] = {
            "text": page.locator(".download-hint").inner_text()[:40],
            "globalMessage": page.locator(".el-message").count(),
        }
        page.screenshot(path=str(OUT / "16-download-hint-desktop.png"))
        context.close()

        # 2) 手机：提示位于面板头部下方，不遮挡标题。
        context = browser.new_context(viewport={"width": 390, "height": 844}, accept_downloads=True)
        context.add_cookies([{"name": "Admin-Token", "value": token, "url": "http://localhost"}])
        page = context.new_page()
        page.route("**/dev-api/**", forward)
        open_panel(page)
        page.locator(".case-panel .resource button", has_text=re.compile("下载")).first.click()
        page.wait_for_selector(".download-hint", timeout=15000)
        header = page.locator(".case-panel header").bounding_box()
        hint = page.locator(".download-hint").bounding_box()
        results["mobileHint"] = {
            "belowHeader": hint["y"] >= header["y"] + header["height"] - 1,
            "insideViewport": hint["x"] >= 0 and hint["x"] + hint["width"] <= 390,
            "overflow": page.evaluate("document.documentElement.scrollWidth > innerWidth"),
        }
        page.screenshot(path=str(OUT / "17-download-hint-mobile.png"))
        context.close()

        # 3) 宽屏：预览弹层点「放大」后图片宽度必须变大。
        context = browser.new_context(viewport={"width": 1800, "height": 950})
        context.add_cookies([{"name": "Admin-Token", "value": token, "url": "http://localhost"}])
        page = context.new_page()
        page.route("**/dev-api/**", forward)
        open_panel(page)
        page.locator(".case-panel .resource button", has_text="预览").first.click()
        page.wait_for_selector(".case-preview-image", timeout=90000)
        page.wait_for_function("() => document.querySelector('.case-preview-image')?.complete", timeout=90000)
        before = page.evaluate("() => Math.round(document.querySelector('.case-preview-image').getBoundingClientRect().width)")
        page.locator(".preview-toolbar button", has_text="放大").click()
        page.wait_for_timeout(800)
        after = page.evaluate("() => Math.round(document.querySelector('.case-preview-image').getBoundingClientRect().width)")
        results["zoom"] = {"before": before, "after": after}
        page.screenshot(path=str(OUT / "18-preview-zoomed.png"))
        context.close()
        browser.close()

    (OUT / "results-ui-tweaks.json").write_text(json.dumps(results, ensure_ascii=False, indent=2), encoding="utf-8")
    assert results["desktopHint"]["globalMessage"] == 0, results
    assert results["mobileHint"]["belowHeader"] and results["mobileHint"]["insideViewport"], results
    assert results["zoom"]["after"] > results["zoom"]["before"] + 20, results
    print(f"行内提示正常（全局消息 0）；手机提示位于标题下方且不溢出；"
          f"放大 {results['zoom']['before']}px → {results['zoom']['after']}px")


if __name__ == "__main__":
    main()
