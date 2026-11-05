"""2025宁波课例批次：真实教师浏览器验收（桌面 + 390/320 手机宽）。

覆盖：课次课例面板、PDF 连续页图预览、视频在线播放、独立 HTML 沙箱新标签、
手机宽无横向溢出；产物写入 output/playwright/textbook-cases-2025。
"""
import json
import re
from pathlib import Path

import requests
from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/playwright/textbook-cases-2025"
OUT.mkdir(parents=True, exist_ok=True)
BASE = "http://localhost:8080"


def open_panel(page, lesson_no):
    """点开指定课次的课例面板，并确保第一份课例已展开。

    桌面宽屏目录常驻，直接点课次徽标；手机宽目录是浮层，改用阅读器顶部的
    「优秀课例 N」按钮，避免依赖被遮挡的目录项。
    """
    page.wait_for_selector(".case-badge", timeout=40000, state="attached")
    if not page.locator(".case-panel").count():
        badge = page.get_by_role("button", name=re.compile("^第%d课，查看" % lesson_no))
        if badge.count() and badge.first.is_visible():
            badge.first.click()
        else:
            header = page.locator("button", has_text="优秀课例").first
            if header.count() and header.is_visible():
                header.click()
    page.wait_for_selector(".case-panel .resource", timeout=40000, state="attached")
    if not page.locator(".case-panel .resource").first.is_visible():
        page.locator(".case-panel .el-collapse-item__header").first.click()
        page.wait_for_timeout(600)


def main():
    line = next(item for item in (ROOT / "AGENTS.md").read_text(encoding="utf-8").splitlines()
                if "19157727791" in item and "|" in item)
    credentials = re.findall(r"`([^`]+)`", line)
    login = requests.post(BASE + "/login", json={"username": credentials[0], "password": credentials[1]},
                          timeout=30).json()
    if login.get("code") != 200:
        raise RuntimeError("教师登录失败")
    token = login["token"]
    books = requests.get(BASE + "/business/textbook/books",
                         headers={"Authorization": "Bearer " + token}, timeout=20).json()["data"]

    def book_id(grade, semester):
        return next(book["bookId"] for book in books
                    if int(book["grade"]) == grade and str(book["semester"]) == str(semester))

    third_up, fifth_up, seventh_up, eighth_down = (book_id(3, 0), book_id(5, 0),
                                                    book_id(7, 0), book_id(8, 1))
    results = {"steps": [], "failures": []}

    def check(name, ok, detail=""):
        results["steps"].append({"name": name, "ok": bool(ok), "detail": str(detail)})
        if not ok:
            results["failures"].append(name + " " + str(detail))

    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(headless=True)
        context = browser.new_context(viewport={"width": 1500, "height": 900})
        context.add_cookies([{"name": "Admin-Token", "value": token, "url": "http://localhost"}])
        page = context.new_page()
        errors = []
        page.on("pageerror", lambda error: errors.append(str(error)))

        # 1) 三上第12课：两份作者课例 + PDF 连续页图
        page.goto("http://localhost/business/textbook/read/%d?section=lesson-12" % third_up,
                  wait_until="networkidle")
        open_panel(page, 12)
        panel = page.locator(".case-panel").inner_text()
        check("课例面板显示两案", "体验在线应用" in panel and "体验AI在线应用" in panel, panel[:100])
        page.screenshot(path=str(OUT / "01-cases-desktop.png"))
        page.locator(".case-panel button", has_text="预览").first.click()
        page.wait_for_selector(".case-preview-image", timeout=40000)
        page.wait_for_timeout(1200)
        check("PDF 连续页图预览", page.locator(".case-preview-image").count() >= 1,
              page.locator(".case-preview-image").count())
        page.screenshot(path=str(OUT / "02-pdf-preview.png"))

        # 2) 五上第5课：独立网页在线预览（沙箱）
        page.goto("http://localhost/business/textbook/read/%d?section=lesson-5" % fifth_up,
                  wait_until="networkidle")
        open_panel(page, 5)
        open_button = page.locator(".case-panel button", has_text="打开网页").first
        check("出现打开网页按钮", open_button.count() >= 1, open_button.count())
        with page.expect_popup(timeout=40000) as popup_info:
            open_button.click()
        popup = popup_info.value
        popup.wait_for_load_state("load", timeout=40000)
        popup.wait_for_timeout(1500)
        origin = popup.evaluate("() => window.origin")
        cookie_access = popup.evaluate("""() => { try { return String(document.cookie); }
            catch (error) { return 'BLOCKED'; } }""")
        text_length = popup.evaluate("() => document.body.innerText.trim().length")
        check("网页新标签有内容", text_length > 0, "%s 字 %s" % (text_length, popup.title()))
        check("网页在沙箱源中运行", origin == "null", origin)
        check("网页读不到平台 Cookie", cookie_access in ("BLOCKED", ""), cookie_access)
        popup.screenshot(path=str(OUT / "03-html-sandbox.png"))
        popup.close()

        # 3) 七上第10课：视频在线播放
        page.goto("http://localhost/business/textbook/read/%d?section=lesson-10" % seventh_up,
                  wait_until="networkidle")
        open_panel(page, 10)
        if not page.locator(".case-panel .resource", has_text="MP4").first.is_visible():
            page.locator(".case-panel .el-collapse-item__header").first.click()
            page.wait_for_timeout(600)
        video_row = page.locator(".case-panel .resource", has_text="MP4").first
        video_row.locator("button", has_text="预览").click()
        page.wait_for_selector(".media-preview video", timeout=40000)
        page.wait_for_function("() => { const v=document.querySelector('.media-preview video');"
                               " return v && v.readyState >= 2 && v.duration > 0; }", timeout=90000)
        info = page.evaluate("""() => { const v=document.querySelector('.media-preview video');
            v.currentTime = Math.min(3, v.duration / 3);
            return {duration: v.duration, currentTime: v.currentTime, error: v.error ? v.error.code : 0}; }""")
        check("视频可加载并拖动", info["duration"] > 0 and info["error"] == 0, info)
        page.screenshot(path=str(OUT / "04-video.png"))

        # 4) 八下第6课：机器学习课例（含材料包）
        page.goto("http://localhost/business/textbook/read/%d?section=lesson-6" % eighth_down,
                  wait_until="networkidle")
        open_panel(page, 6)
        panel = page.locator(".case-panel").inner_text()
        check("八下机器学习课例含材料包", "配套材料包" in panel, panel[:120])
        page.screenshot(path=str(OUT / "05-machine-learning-cases.png"))

        # 5) 手机宽
        for width, height, label in ((390, 844, "390"), (320, 568, "320")):
            mobile = browser.new_context(viewport={"width": width, "height": height}, is_mobile=True,
                                         has_touch=True)
            mobile.add_cookies([{"name": "Admin-Token", "value": token, "url": "http://localhost"}])
            mobile_page = mobile.new_page()
            mobile_errors = []
            mobile_page.on("pageerror", lambda error: mobile_errors.append(str(error)))
            mobile_page.goto("http://localhost/business/textbook/read/%d?section=lesson-12" % third_up,
                             wait_until="networkidle")
            open_panel(mobile_page, 12)
            overflow = mobile_page.evaluate("() => document.documentElement.scrollWidth - window.innerWidth")
            visible = mobile_page.locator(".case-panel").is_visible()
            mobile_page.screenshot(path=str(OUT / ("06-mobile-%s.png" % label)))
            check("%s宽课例面板可用且无横向溢出" % label, visible and overflow <= 1,
                  "overflow=%s visible=%s" % (overflow, visible))
            check("%s宽无 JS 错误" % label, not mobile_errors, mobile_errors[:2])
            mobile.close()

        check("桌面无 JS 错误", not errors, errors[:3])
        browser.close()

    (OUT / "results.json").write_text(json.dumps(results, ensure_ascii=False, indent=2), encoding="utf-8")
    print("通过 %d 项，失败 %d 项" % (sum(1 for step in results["steps"] if step["ok"]),
                                     len(results["failures"])))
    for failure in results["failures"]:
        print("  失败：" + failure)


if __name__ == "__main__":
    main()
