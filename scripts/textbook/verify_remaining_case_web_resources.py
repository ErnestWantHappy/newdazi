"""验收补收网页的实际下载、入口覆盖、关键本地服务和教师桌面/窄屏入口。"""
import functools
import hashlib
import http.server
import json
import os
import re
import shutil
import socket
import subprocess
import tempfile
import threading
import time
import zipfile
from pathlib import Path
from urllib.parse import quote, urlsplit
import requests
from playwright.sync_api import sync_playwright
from add_remaining_case_web_resources import SKIP_PARTS

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases"
SHOTS = ROOT / "output/playwright/textbook-cases"


def free_port():
    with socket.socket() as stream:
        stream.bind(("127.0.0.1", 0))
        return stream.getsockname()[1]


class QuietHandler(http.server.SimpleHTTPRequestHandler):
    def log_message(self, *args):
        pass


def main():
    records = json.loads((OUT / "remaining-web-resources.json").read_text(encoding="utf-8"))
    manifest = json.loads((OUT / "catalog-local-reviewed.json").read_text(encoding="utf-8"))
    cases_by_source = {case["sourceKey"]: case for case in manifest["cases"]}
    source = Path(json.loads((OUT / "inventory.json").read_text(encoding="utf-8"))["sourceRoot"])
    old_web = json.loads((OUT / "web-packages.json").read_text(encoding="utf-8"))
    decisions = json.loads((OUT / "package-decisions.json").read_text(encoding="utf-8"))
    origins = [item["origin"] for item in old_web if item["kind"] == "dir"]
    origins += [item["source"] for item in decisions["included"] if not item["target"].startswith("packages/lesson-web/") and (source / item["source"]).is_dir()]
    supplemental = {item["sourceDirectory"] + "/" + page for item in records for page in item["pages"]}
    missing = []
    html_count = 0
    for case in manifest["cases"]:
        if not case.get("reviewed"):
            continue
        for path in (source / case["sourceDirectory"]).rglob("*"):
            if not path.is_file() or path.suffix.lower() not in {".html", ".htm"} or SKIP_PARTS.intersection(path.parts):
                continue
            html_count += 1
            relative = path.relative_to(source).as_posix()
            if relative not in supplemental and not any(relative.startswith(origin.rstrip("/") + "/") for origin in origins):
                missing.append(relative)
    assert not missing, "仍有具名网页漏收：" + str(missing)
    line = next(line for line in (ROOT / "AGENTS.md").read_text(encoding="utf-8").splitlines() if "19157727791" in line and "|" in line)
    credentials = re.findall(r"`([^`]+)`", line)
    auth = requests.post("http://localhost:8080/login", json={"username": credentials[0], "password": credentials[1]}, timeout=30).json()
    assert auth.get("token"), "教师登录失败"
    headers = {"Authorization": "Bearer " + auth["token"]}
    books = requests.get("http://localhost:8080/business/textbook/books", headers=headers, timeout=30).json()["data"]
    book_ids = {book["bookCode"]: book["bookId"] for book in books}
    downloaded = []
    resources = {}
    for item in records:
        case = cases_by_source[item["sourceKey"]]
        book_id = book_ids[case["bookCode"]]
        url = f"http://localhost:8080/business/textbook/cases/book/{book_id}/section/{case['sectionKey']}"
        payload = requests.get(url, headers=headers, timeout=30).json()["data"]
        owner = next(owner for owner in payload if owner["author"] == case["author"])
        resource = next(resource for resource in owner["resources"] if resource["title"] == "配套网页与实验材料")
        resources[item["sourceKey"]] = resource
        response = requests.get(f"http://localhost:8080/business/textbook/cases/download/{resource['resourceId']}", headers=headers, stream=True, timeout=120)
        assert response.status_code == 200
        checksum = hashlib.sha256()
        for chunk in response.iter_content(1024 * 1024):
            checksum.update(chunk)
        assert checksum.hexdigest() == item["sha256"], "新增包下载哈希不一致"
        assert "配套网页" in resource["filename"] and not re.fullmatch(r"[a-f0-9]{32}\.zip", resource["filename"])
        with zipfile.ZipFile(Path(manifest["sourceRoot"]) / item["package"]) as archive:
            assert all(page in archive.namelist() for page in item["pages"])
            assert archive.testzip() is None
        downloaded.append({"sourceKey": item["sourceKey"], "resourceId": resource["resourceId"], "sha256": item["sha256"], "pages": len(item["pages"])})
    probes = []
    runtime = OUT / "runtime-remaining"
    runtime.mkdir(exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="verify-", dir=runtime) as directory:
        folder = Path(directory)
        assert folder.resolve().is_relative_to(runtime.resolve())
        chosen = {}
        for item in records:
            if any(key in item["sourceDirectory"] for key in ["技术基础", "张楚人", "王诗莹", "用神经网络进行预测", "在线购物中的推荐算法"]):
                target = folder / item["sourceKey"]
                with zipfile.ZipFile(Path(manifest["sourceRoot"]) / item["package"]) as archive:
                    assert not any(".." in Path(name).parts or name.startswith("/") for name in archive.namelist())
                    archive.extractall(target)
                chosen[item["sourceKey"]] = (item, target)
        server = http.server.ThreadingHTTPServer(("127.0.0.1", 0), functools.partial(QuietHandler, directory=str(folder)))
        server_thread = threading.Thread(target=server.serve_forever, daemon=True)
        server_thread.start()
        try:
            with sync_playwright() as playwright:
                browser = playwright.chromium.launch(headless=True)
                context = browser.new_context(viewport={"width": 1500, "height": 950})
                context.route("**/*", lambda route: route.continue_() if urlsplit(route.request.url).hostname in {"localhost", "127.0.0.1"} else route.abort())
                for key, (item, target) in chosen.items():
                    if any(name in item["sourceDirectory"] for name in ["技术基础", "张楚人"]):
                        project = target / ("城市大脑" if "技术基础" in item["sourceDirectory"] else "学生评价平台")
                        port = free_port()
                        script = project / "server.js"
                        content = script.read_text(encoding="utf-8-sig")
                        # 仅验收临时副本改端口，避免占用原项目或他人服务。
                        content = re.sub(r"const PORT\s*=\s*\d+\s*;", f"const PORT = {port};", content, count=1)
                        script.write_text(content, encoding="utf-8")
                        process = subprocess.Popen([shutil.which("node.exe") or "node", "server.js"], cwd=project,
                            env={**os.environ, "PORT": str(port)}, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
                        try:
                            url = f"http://127.0.0.1:{port}"
                            for _ in range(40):
                                try:
                                    response = requests.get(url + ("/student.html" if "技术基础" in item["sourceDirectory"] else "/login.html"), timeout=2)
                                    if response.status_code == 200:
                                        break
                                except requests.RequestException:
                                    pass
                                time.sleep(0.25)
                            else:
                                raise AssertionError("补收工程未能启动")
                            page = context.new_page()
                            page.goto(response.url, wait_until="networkidle")
                            assert len(page.locator("body").inner_text()) > 20
                            page.screenshot(path=str(SHOTS / ("remaining-city.png" if "技术基础" in item["sourceDirectory"] else "remaining-evaluation.png")))
                            page.close()
                            probes.append({"sourceKey": key, "server": True, "status": 200})
                        finally:
                            process.terminate()
                            try:
                                process.wait(timeout=10)
                            except subprocess.TimeoutExpired:
                                process.kill()
                                process.wait()
                    else:
                        page = context.new_page()
                        entry = next((name for name in item["pages"] if "课堂计时器" in name), item["pages"][0])
                        url = f"http://127.0.0.1:{server.server_port}/{key}/{quote(entry, safe='/')}"
                        page.goto(url, wait_until="networkidle")
                        assert len(page.locator("body").inner_text()) > 20
                        if "用神经网络进行预测" in item["sourceDirectory"]:
                            assert page.evaluate("typeof XLSX !== 'undefined'"), "XLSX脚本未加载"
                        if "在线购物中的推荐算法" in item["sourceDirectory"]:
                            assert requests.get(f"http://127.0.0.1:{server.server_port}/{key}/" + quote("教学材料/images/10.png", safe="/"), timeout=10).status_code == 200
                        page.close()
                        probes.append({"sourceKey": key, "static": True, "status": 200})
                context.close()
                context = browser.new_context(viewport={"width": 1500, "height": 950})
                context.add_cookies([{"name": "Admin-Token", "value": auth["token"], "url": "http://localhost"}])
                page = context.new_page()
                city = next(item for item in records if "技术基础" in item["sourceDirectory"])
                case = cases_by_source[city["sourceKey"]]
                page.goto(f"http://localhost/business/textbook/read/{book_ids[case['bookCode']]}?section={case['sectionKey']}", wait_until="networkidle")
                package_row = page.locator(".case-panel .resource", has=page.locator("strong", has_text="配套网页与实验材料"))
                package_row.wait_for()
                page.screenshot(path=str(SHOTS / "remaining-platform-desktop.png"))
                with page.expect_download() as event:
                    package_row.get_by_role("button", name="下载实验包").click()
                assert "配套网页" in event.value.suggested_filename
                page.set_viewport_size({"width": 390, "height": 844})
                assert page.evaluate("document.documentElement.scrollWidth <= innerWidth")
                page.screenshot(path=str(SHOTS / "remaining-platform-mobile.png"))
                browser.close()
        finally:
            server.shutdown()
            server.server_close()
            server_thread.join()
    result = {"packages": len(downloaded), "htmlEntries": sum(item["pages"] for item in downloaded), "allSourceHtml": html_count, "uncovered": missing, "downloaded": downloaded, "probes": probes,
              "desktop": True, "mobile390": True, "externalModelCalled": False}
    (OUT / "remaining-web-verification.json").write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"补收验收通过：{len(downloaded)}包/{result['htmlEntries']}网页，五个关键工程、教师桌面及390px入口通过")


if __name__ == "__main__":
    main()
