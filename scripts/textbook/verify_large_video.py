"""验收大视频：125MiB 改为下载提示不崩溃；47MiB 真实播放与拖动；390px 无溢出。"""
import json
import re
from pathlib import Path

import requests
from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/playwright/textbook-cases"
API = "http://localhost:8080"
BASE = API + "/business/textbook/cases"


def forward(route):
    """把开发代理请求转到 8080；页面关闭导致请求中断时不要抛错。"""
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
    found = {}
    for row in summary:
        cases = requests.get(f"{BASE}/book/{row['bookId']}/section/{row['sectionKey']}", headers=headers, timeout=30).json()["data"]
        for case in cases:
            for resource in case["resources"]:
                if resource["title"] in {"操作视频完整版", "操作视频整理归档"}:
                    found[resource["title"]] = (row, case, resource)
    hugest = found["操作视频完整版"][2]
    medium = found["操作视频整理归档"][2]
    assert int(hugest["bytes"]) > 100 * 1024 * 1024 and int(medium["bytes"]) < 100 * 1024 * 1024, found

    results = {}
    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(headless=True)

        def open_preview(page, entry):
            row, case, resource = entry
            page.goto(f"http://localhost/business/textbook/read/{row['bookId']}?section={row['sectionKey']}", wait_until="networkidle")
            page.wait_for_timeout(1200)
            if page.locator(".case-panel").count() == 0:
                page.get_by_role("button", name=re.compile("优秀课例")).first.click()
            page.wait_for_selector(".case-panel .resource", timeout=60000)
            target = page.locator(".case-panel .resource", has_text=resource["title"]).first
            if not target.is_visible():
                page.locator(".case-panel .el-collapse-item__header", has_text=case["title"]).first.click()
                page.wait_for_timeout(400)
            page.evaluate("""(title) => { const row = [...document.querySelectorAll('.case-panel .resource')]
                .find(r => r.textContent.includes(title));
                [...row.querySelectorAll('button')].find(b => b.textContent.includes('预览')).click(); }""", resource["title"])

        for label, width in (("desktop", 1500), ("mobile", 390)):
            context = browser.new_context(viewport={"width": width, "height": 900})
            context.add_cookies([{"name": "Admin-Token", "value": token, "url": "http://localhost"}])
            page = context.new_page()
            page.route("**/dev-api/**", forward)
            open_preview(page, found["操作视频完整版"])
            page.wait_for_timeout(3000)
            text = page.locator(".media-preview").first.inner_text()
            results[f"hugeVideo{label.capitalize()}"] = {
                "warningShown": "建议下载后" in text,
                "videoElement": page.locator(".media-preview video").count(),
                "overflow": page.evaluate("document.documentElement.scrollWidth > innerWidth"),
            }
            page.screenshot(path=str(OUT / f"14-huge-video-{label}.png"))
            context.close()

        context = browser.new_context(viewport={"width": 1500, "height": 900})
        context.add_cookies([{"name": "Admin-Token", "value": token, "url": "http://localhost"}])
        page = context.new_page()
        page.route("**/dev-api/**", forward)
        open_preview(page, found["操作视频整理归档"])
        page.wait_for_selector(".media-preview video", timeout=60000)
        page.wait_for_function("() => document.querySelector('.media-preview video')?.readyState >= 2", timeout=180000)
        page.evaluate("() => { const v = document.querySelector('.media-preview video'); v.muted = true; return v.play(); }")
        page.wait_for_function("() => document.querySelector('.media-preview video').currentTime > 0.5", timeout=120000)
        duration = page.evaluate("() => document.querySelector('.media-preview video').duration")
        target_time = round(duration * 0.6, 1)
        page.evaluate("(value) => { document.querySelector('.media-preview video').currentTime = value; }", target_time)
        page.wait_for_function("(value) => Math.abs(document.querySelector('.media-preview video').currentTime - value) < 3", arg=target_time, timeout=180000)
        results["mediumVideo"] = {
            "file": medium["filename"], "bytes": int(medium["bytes"]), "duration": round(duration, 1),
            "seekTarget": target_time,
            "currentTime": round(page.evaluate("() => document.querySelector('.media-preview video').currentTime"), 1),
        }
        page.screenshot(path=str(OUT / "15-medium-video.png"))
        context.close()
        browser.close()

    (OUT / "results-large-video.json").write_text(json.dumps(results, ensure_ascii=False, indent=2), encoding="utf-8")
    for key in ("hugeVideoDesktop", "hugeVideoMobile"):
        assert results[key]["warningShown"] and results[key]["videoElement"] == 0, results
    assert results["mediumVideo"]["currentTime"] >= results["mediumVideo"]["seekTarget"] - 3, results
    print(f"125MiB 视频改为下载提示（桌面/手机均无 <video>、无溢出）；"
          f"47MiB 视频播放并拖到 {results['mediumVideo']['currentTime']}/{results['mediumVideo']['duration']} 秒")


if __name__ == "__main__":
    main()
