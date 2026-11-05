"""真实浏览器验证：课例视频播放与拖动、教材打印/下载、课程卡教材绑定入口。

沿用 smoke_cases.py 的登录方式：API 取教师令牌写入 Cookie，开发代理 /dev-api 转发到 8080。
"""
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


def teacher_headers():
    line = next(line for line in (ROOT / "AGENTS.md").read_text(encoding="utf-8").splitlines() if "19157727791" in line and "|" in line)
    credentials = re.findall(r"`([^`]+)`", line)
    login = requests.post(API + "/login", json={"username": credentials[0], "password": credentials[1]}, timeout=30).json()
    if login.get("code") != 200 or not login.get("token"):
        raise RuntimeError("本机教师登录失败")
    return login["token"], {"Authorization": "Bearer " + login["token"]}


def collect(headers):
    summary = requests.get(BASE + "/summary", headers=headers, timeout=30).json()["data"]
    video = None
    tools = []
    for row in summary:
        cases = requests.get(f"{BASE}/book/{row['bookId']}/section/{row['sectionKey']}", headers=headers, timeout=30).json()["data"]
        for case in cases:
            for resource in case["resources"]:
                if resource["category"] == "TOOL":
                    tools.append({"title": resource["title"], "bytes": int(resource["bytes"]),
                                  "hasDescription": bool(resource.get("description")),
                                  "hasRequirements": bool(resource.get("requirements"))})
                if video is None and resource["filename"].lower().endswith(".mp4") and int(resource["bytes"]) > 20 * 1048576:
                    video = {"row": row, "case": case, "resource": resource}
    if video is None:
        raise RuntimeError("没有找到可用于视频验收的课例视频")
    return video, tools


def main():
    token, headers = teacher_headers()
    video, tools = collect(headers)
    assert len(tools) >= 4, f"实验包数量不足：{tools}"
    assert all(item["hasDescription"] and item["hasRequirements"] for item in tools), f"实验包缺少下载说明：{tools}"

    results = {}
    media_responses = []
    errors = []
    http_errors = []
    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(headless=True)
        context = browser.new_context(viewport={"width": 1900, "height": 960}, accept_downloads=True)
        context.add_cookies([{"name": "Admin-Token", "value": token, "url": "http://localhost"}])
        page = context.new_page()
        page.on("pageerror", lambda error: errors.append(str(error)))
        # 网络类 404 由 http_errors 单独统计，避免同一条资源失败重复计入脚本错误。
        page.on("console", lambda message: errors.append("console:" + message.text)
                if message.type == "error" and "Failed to load resource" not in message.text else None)
        page.on("response", lambda response: media_responses.append({
            "status": response.status,
            "range": response.headers.get("content-range", ""),
        }) if "/cases/media/" in response.url else None)
        page.on("response", lambda response: http_errors.append(f"http{response.status}:{response.url}") if response.status >= 400 else None)

        def forward(route):
            route.fulfill(response=route.fetch(url=route.request.url.replace("http://localhost/dev-api", API, 1)))

        page.route("**/dev-api/**", forward)

        # 1) 课程卡「教材」胶囊：只有已绑定课次的卡片才提示"打开本课对应教材"。
        page.goto("http://localhost/teacher-dashboard/index", wait_until="networkidle")
        page.wait_for_selector(".textbook-chip", timeout=30000)
        bound_chip = page.locator('.textbook-chip[title="打开本课对应教材"]')
        assert bound_chip.count() >= 1, "教师首页没有已绑定教材的课程卡"
        page.evaluate("""() => {
            const chip = [...document.querySelectorAll('.textbook-chip')]
                .find(item => item.getAttribute('title') === '打开本课对应教材');
            chip.click();
        }""")
        page.wait_for_url(re.compile(r".*/business/textbook/read/\d+\?.*section=lesson-"), timeout=30000)
        page.wait_for_selector(".page-wrap img", timeout=90000)
        page.wait_for_function("() => [...document.querySelectorAll('.page-wrap img')].some(i => i.naturalWidth > 0)", timeout=90000)
        results["bindingUrl"] = page.url
        page.screenshot(path=str(OUT / "08-binding-entry.png"))

        # 2) 整册 PDF 下载与打印本课。
        with page.expect_download(timeout=300000) as download_event:
            page.get_by_role("button", name=re.compile("下载整册")).click()
        whole = download_event.value
        whole_bytes = Path(whole.path()).read_bytes()
        assert whole_bytes[:4] == b"%PDF", "整册下载不是PDF"
        results["wholeBookPdf"] = {"bytes": len(whole_bytes), "name": whole.suggested_filename}

        with page.expect_popup(timeout=180000) as popup_event:
            page.get_by_role("button", name=re.compile("打印本课")).click()
        popup = popup_event.value
        popup.wait_for_selector(".sheet img", timeout=180000)
        popup.wait_for_function("() => [...document.querySelectorAll('.sheet img')].every(i => i.complete && i.naturalWidth > 0)", timeout=180000)
        results["printPages"] = popup.locator(".sheet img").count()
        popup.close()

        # 3) 课例视频真实播放与拖动进度。
        row = video["row"]
        page.goto(f"http://localhost/business/textbook/read/{row['bookId']}?section={row['sectionKey']}", wait_until="networkidle")
        page.wait_for_selector(".toc-item.is-active .case-badge", timeout=60000)
        page.locator(".toc-item.is-active .case-badge").click()
        page.wait_for_selector(".case-panel .resource", timeout=60000)
        resource_row = page.locator(".case-panel .resource", has_text=video["resource"]["title"]).first
        # 面板默认只展开第一份课例；同课多案可能重名，按作者定位目标课例再展开。
        if not resource_row.is_visible():
            marker = video["case"].get("author") or video["case"]["title"]
            page.locator(".case-panel .el-collapse-item__header", has_text=marker).first.click()
            page.wait_for_timeout(500)
        resource_row.locator("button", has_text="预览").click()
        page.wait_for_selector(".media-preview video", timeout=60000)
        page.wait_for_function("() => document.querySelector('.media-preview video')?.readyState >= 2", timeout=180000)
        page.evaluate("() => { const v = document.querySelector('.media-preview video'); v.muted = true; return v.play(); }")
        page.wait_for_function("() => document.querySelector('.media-preview video').currentTime > 0.5", timeout=60000)
        duration = page.evaluate("() => document.querySelector('.media-preview video').duration")
        target = round(duration / 2, 1)
        page.evaluate("(value) => { document.querySelector('.media-preview video').currentTime = value; }", target)
        page.wait_for_function("(value) => Math.abs(document.querySelector('.media-preview video').currentTime - value) < 2", arg=target, timeout=60000)
        results["video"] = {"file": video["resource"]["filename"], "bytes": int(video["resource"]["bytes"]),
                            "duration": round(duration, 1), "seekTo": target,
                            "currentTime": round(page.evaluate("() => document.querySelector('.media-preview video').currentTime"), 1),
                            "readyState": page.evaluate("() => document.querySelector('.media-preview video').readyState")}
        page.screenshot(path=str(OUT / "09-video-preview.png"))
        results["mediaResponses"] = media_responses[-6:]
        assert any(item["status"] == 206 for item in media_responses), "视频请求没有分段响应"
        # 教师头像文件在本机库有历史缺失，属既有数据问题，与课例功能无关，单独记录不误判为回归。
        unrelated = [item for item in http_errors if "/profile/avatar/" in item]
        results["knownMissingAvatar"] = unrelated
        assert not [item for item in http_errors if item not in unrelated], http_errors
        assert not errors, errors
        browser.close()

    results.update(tools=tools, jsErrors=errors)
    (OUT / "results-media-textbook.json").write_text(json.dumps(results, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"绑定入口直达 {results['bindingUrl']}；整册PDF {results['wholeBookPdf']['bytes']}字节；打印{results['printPages']}页；"
          f"视频{results['video']['duration']}秒拖动到{results['video']['currentTime']}秒；实验包{len(tools)}个均有说明")


if __name__ == "__main__":
    main()
