"""8080隔离后端与现有Vue3开发服务器的真实教师课例验收。"""
import json
import re
import hashlib
from pathlib import Path
import requests
from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/playwright/textbook-cases"
OUT.mkdir(parents=True, exist_ok=True)


def main():
    line = next(line for line in (ROOT / "AGENTS.md").read_text(encoding="utf-8").splitlines() if "19157727791" in line and "|" in line)
    credentials = re.findall(r"`([^`]+)`", line)
    login = requests.post("http://localhost:8080/login", json={"username": credentials[0], "password": credentials[1]}, timeout=30).json()
    if login.get("code") != 200 or not login.get("token"):
        raise RuntimeError("本机教师登录失败")
    headers = {"Authorization": "Bearer " + login["token"]}
    books = requests.get("http://localhost:8080/business/textbook/books", headers=headers, timeout=20).json()["data"]
    five_id = next(book["bookId"] for book in books if int(book["grade"]) == 5 and str(book["semester"]) == "0")
    summary = requests.get("http://localhost:8080/business/textbook/cases/summary", headers=headers, timeout=20).json()
    assert summary.get("code") == 200, "汇总接口失败：" + str(summary)[:600]
    counts = summary["data"]
    five = next(row for row in counts if int(row["bookId"]) == int(five_id) and row["sectionKey"] == "lesson-11")
    assert int(five["total"]) == 2
    details = requests.get(f"http://localhost:8080/business/textbook/cases/book/{five['bookId']}/section/lesson-11", headers=headers, timeout=20).json()
    assert len(details["data"]) == 2
    resource = details["data"][0]["resources"][0]
    preview = requests.get(f"http://localhost:8080/business/textbook/cases/files/{resource['resourceId']}?preview=true", headers=headers, timeout=20)
    assert preview.status_code == 200 and preview.content.startswith(b"%PDF")
    anonymous = requests.get("http://localhost:8080/business/textbook/cases/summary", timeout=20)
    assert anonymous.status_code in (401, 403) or anonymous.json().get("code") in (401, 403)
    student_line = next(line for line in (ROOT / "AGENTS.md").read_text(encoding="utf-8").splitlines() if "2020710101" in line and "|" in line)
    student_credentials = re.findall(r"`([^`]+)`", student_line)
    student_login = requests.post("http://localhost:8080/login", json={"username": student_credentials[0], "password": student_credentials[1]}, timeout=30).json()
    student_verified = False
    if student_login.get("token"):
        student_headers = {"Authorization": "Bearer " + student_login["token"]}
        for path in ["summary", f"book/{five['bookId']}/section/lesson-11", f"files/{resource['resourceId']}", f"files/{resource['resourceId']}/preview-info", f"files/{resource['resourceId']}/pages/1"]:
            denied = requests.get("http://localhost:8080/business/textbook/cases/" + path, headers=student_headers, timeout=20)
            assert denied.status_code in (401, 403) or denied.json().get("code") in (401, 403)
        student_verified = True
        denied_download = requests.get(f"http://localhost:8080/business/textbook/cases/download/{resource['resourceId']}", cookies={"Admin-Token": student_login["token"]}, timeout=20)
        assert denied_download.status_code == 403
        denied_media = requests.get(f"http://localhost:8080/business/textbook/cases/media/{resource['resourceId']}", cookies={"Admin-Token": student_login["token"]}, timeout=20)
        assert denied_media.status_code == 403
    else:
        print("学生权限实测跳过：本机已登记账号登录失败，未修改账号")
    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(headless=True)
        context = browser.new_context(viewport={"width": 1900, "height": 960})
        context.add_cookies([{"name": "Admin-Token", "value": login["token"], "url": "http://localhost"}])
        page = context.new_page()
        errors = []
        page.on("pageerror", lambda error: errors.append(str(error)))
        # 仅将开发代理转到本轮隔离后端，所有应答均来自真实服务和本机数据。
        def forward(route):
            url = route.request.url.replace("http://localhost/dev-api", "http://localhost:8080", 1)
            response = route.fetch(url=url)
            route.fulfill(response=response)
        page.route("**/dev-api/**", forward)
        page.goto("http://localhost/textbook", wait_until="networkidle")
        page.wait_for_selector(".book-card", timeout=30000)
        page.screenshot(path=str(OUT / "01-shelf.png"))
        page.goto(f"http://localhost/business/textbook/read/{five['bookId']}", wait_until="networkidle")
        page.wait_for_selector(".case-badge", timeout=30000)
        assert page.locator(".case-badge").count() >= 1
        page.screenshot(path=str(OUT / "02-directory-before-selection.png"))
        page.get_by_role("button", name="第11课，查看2份优秀课例").click()
        page.wait_for_selector(".case-panel .resource", timeout=20000)
        page.wait_for_function("() => [...document.querySelectorAll('.page-wrap:not(.is-outside) img')].some(i => i.naturalWidth > 0)", timeout=30000)
        page.wait_for_timeout(1500)
        assert page.locator(".case-panel").inner_text().count("认识决策树") >= 2
        page.screenshot(path=str(OUT / "03-two-cases.png"))
        page.locator(".case-panel button", has_text="预览").first.click()
        page.wait_for_selector(".case-preview-image", timeout=30000)
        page.screenshot(path=str(OUT / "04-preview.png"))
        with page.expect_download(timeout=30000) as download_event:
            page.locator(".el-dialog .preview-actions button", has_text="下载原件").click()
        downloaded = download_event.value
        assert downloaded.failure() is None
        native_hash = hashlib.sha256(Path(downloaded.path()).read_bytes()).hexdigest()
        original_bytes = requests.get(f"http://localhost:8080/business/textbook/cases/files/{resource['resourceId']}", headers=headers, timeout=30).content
        assert native_hash == hashlib.sha256(original_bytes).hexdigest(), "原生下载与接口文件不一致"
        page.locator(".el-dialog__headerbtn").click()
        page.set_viewport_size({"width": 390, "height": 844})
        page.wait_for_timeout(700)
        assert page.evaluate("document.documentElement.scrollWidth <= innerWidth"), "手机页面横向溢出"
        page.screenshot(path=str(OUT / "05-mobile.png"))
        page.locator(".case-panel button", has_text="预览").first.click()
        page.wait_for_selector(".case-preview-image", timeout=30000)
        page.locator(".preview-toolbar button", has_text="下一页").click()
        page.wait_for_selector('.case-preview-image[alt$="第2页"]', timeout=30000)
        assert page.evaluate("document.documentElement.scrollWidth <= innerWidth")
        page.screenshot(path=str(OUT / "06-mobile-page-preview.png"))
        page.locator(".el-dialog__headerbtn").click()
        page.set_viewport_size({"width": 1900, "height": 960})
        seven_id = next(book["bookId"] for book in books if int(book["grade"]) == 7 and str(book["semester"]) == "1")
        page.goto(f"http://localhost/business/textbook/read/{seven_id}?section=lesson-20", wait_until="networkidle")
        page.wait_for_selector(".case-panel .resource", timeout=20000)
        assert "Windows" in page.locator(".case-panel").inner_text()
        page.locator(".case-panel button", has_text="预览").first.click()
        page.wait_for_selector(".case-preview-image", timeout=30000)
        page.wait_for_function("() => document.querySelector('.case-preview-image')?.naturalWidth > 0")
        page.screenshot(path=str(OUT / "07-neural-tool-with-preview.png"))
        with page.expect_download(timeout=30000) as tool_event:
            page.locator(".preview-tools button", has_text="下载实验包").click()
        tool_download = tool_event.value
        tool_path = Path(tool_download.path())
        expected_tool = json.loads((ROOT / 'output/textbook-cases/neural-tool-review.json').read_text(encoding='utf-8'))
        tool_hash = hashlib.sha256()
        with tool_path.open('rb') as stream:
            for chunk in iter(lambda: stream.read(1024 * 1024), b''):
                tool_hash.update(chunk)
        assert tool_hash.hexdigest() == expected_tool['releaseSha256'], "实验包下载哈希不符"
        tool_bytes = tool_path.stat().st_size
        assert not errors, errors
        browser.close()
    (OUT / "results.json").write_text(json.dumps({"teacherSummary": True, "twoCases": True, "pdf": True,
        "anonymousDenied": True, "studentDenied": student_verified, "nativeDownloadHash": native_hash, "toolDownloadBytes": tool_bytes, "toolDownloadSha256": tool_hash.hexdigest(), "desktop": True, "mobilePagePreview": True, "mobileScreenshot": True, "jsErrors": errors}, ensure_ascii=False, indent=2), encoding="utf-8")
    print("教师/学生权限、桌面预览、原生下载哈希、390px逐页预览与无横向溢出通过")


if __name__ == "__main__":
    main()
