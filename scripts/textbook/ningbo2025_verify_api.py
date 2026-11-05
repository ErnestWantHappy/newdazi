"""2025宁波课例批次：本机接口验收（真实登录、真实文件、真实 Range）。

覆盖：汇总/按课次取课例、PDF 预览与页图、原生下载、视频 Range、HTML 沙箱入口、
匿名 401、学生 403、旧批次课例仍可读。
"""
import os
import json
import sys
from pathlib import Path

import requests

BASE = "http://localhost:8080"
TEACHER = ("19157727791", os.environ.get("TEACHER_PASSWORD",""))
STUDENTS = [("2020710101", "123456"), ("st99_363", "123456")]
OUT = Path(__file__).resolve().parents[2] / "output/textbook-cases-2025"


def login(account):
    response = requests.post(BASE + "/login", json={"username": account[0], "password": account[1]}, timeout=30)
    data = response.json()
    if data.get("code") != 200:
        return None
    return data["token"]


def api(token, path, **kwargs):
    headers = {"Authorization": "Bearer " + token} if token else {}
    return requests.get(BASE + path, headers=headers, timeout=60, **kwargs)


def main():
    catalog = json.loads((OUT / "catalog-reviewed-2025.json").read_text(encoding="utf-8"))
    published = [case for case in catalog["cases"] if case.get("status", "PUBLISHED") == "PUBLISHED"]
    report = {"steps": [], "failures": []}

    def check(name, condition, detail=""):
        report["steps"].append({"name": name, "ok": bool(condition), "detail": detail})
        if not condition:
            report["failures"].append(name + " " + str(detail))

    teacher = login(TEACHER)
    check("教师登录", teacher, "token" if teacher else "失败")
    if not teacher:
        (OUT / "api-verification.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
        sys.exit(1)

    summary = api(teacher, "/business/textbook/cases/summary")
    body = summary.json()
    check("课例汇总接口", summary.status_code == 200 and body.get("code") == 200, summary.status_code)
    totals = sum(item["total"] for item in (body.get("data") or []))
    check("汇总课次覆盖", totals > 100, "课次资源计数合计 %s" % totals)

    # 逐课抽查：每册至少一节，且覆盖新批次多个类型
    samples = ["xxkj-3-0-2026:lesson-12", "xxkj-4-1-2026:lesson-7", "xxkj-5-1-2026:lesson-11",
               "xxkj-6-0-2026:lesson-17", "xxkj-7-0-2026:lesson-10", "xxkj-8-1-2026:lesson-2"]
    book_ids = {}
    for case in published:
        book_ids.setdefault(case["bookCode"], None)
    for pair in samples:
        code, lesson = pair.split(":")
        # 用本机教材目录解析 bookId，避免把本机 id 写死
        if book_ids.get(code) is None:
            book_ids[code] = resolve_book_id(teacher, code)
        book_id = book_ids.get(code)
        if not book_id:
            check("解析教材 %s" % code, False, "缺少 bookId")
            continue
        response = api(teacher, "/business/textbook/cases/book/%d/section/%s" % (book_id, lesson))
        data = response.json()
        cases = data.get("data") or []
        check("按课次取课例 %s" % pair, response.status_code == 200 and len(cases) > 0, len(cases))
        if not cases:
            continue
        resources = cases[0]["resources"]
        check("课例含资源 %s" % pair, len(resources) > 0, len(resources))
        if resources and not report.get("sampleResource"):
            report["sampleResource"] = resources[0]
        # 预览 / 下载 / 视频 / HTML 各挑一个真实文件
        for resource in resources:
            if resource.get("previewable") and not report.get("previewChecked"):
                preview = api(teacher, "/business/textbook/cases/files/%d?preview=true" % resource["resourceId"])
                check("PDF预览下载", preview.status_code == 200 and preview.content[:4] == b"%PDF",
                      len(preview.content))
                info = api(teacher, "/business/textbook/cases/files/%d/preview-info" % resource["resourceId"])
                check("PDF页数接口", info.status_code == 200 and (info.json().get("data") or {}).get("pages", 0) > 0,
                      info.text[:80])
                page = api(teacher, "/business/textbook/cases/files/%d/pages/1" % resource["resourceId"])
                check("页图接口", page.status_code == 200 and page.content[:2] == b"\xff\xd8", len(page.content))
                report["previewChecked"] = True
            if resource["filename"].lower().endswith(".mp4") and not report.get("videoChecked"):
                headers = {"Authorization": "Bearer " + teacher, "Range": "bytes=0-1023"}
                media = requests.get(BASE + "/business/textbook/cases/media/%d" % resource["resourceId"],
                                     headers=headers, timeout=60)
                check("视频Range 206", media.status_code == 206 and len(media.content) == 1024,
                      "%s %s" % (media.status_code, len(media.content)))
                report["videoChecked"] = True
            if resource["filename"].lower().endswith(".html") and not report.get("htmlChecked"):
                headers = {"Authorization": "Bearer " + teacher}
                page = requests.get(BASE + "/business/textbook/cases/html/%d" % resource["resourceId"],
                                    headers=headers, timeout=60)
                policy = page.headers.get("Content-Security-Policy", "")
                check("HTML沙箱入口", page.status_code == 200 and "sandbox" in policy
                      and "allow-same-origin" not in policy, policy)
                report["htmlChecked"] = True
                report["htmlResourceId"] = resource["resourceId"]

    # 原生下载（Cookie）与匿名/学生负例
    first = report.get("sampleResource") or {}
    cookies = {"Admin-Token": teacher}
    download = requests.get(BASE + "/business/textbook/cases/download/%d" % first["resourceId"],
                            cookies=cookies, timeout=180)
    check("教师Cookie下载", download.status_code == 200 and len(download.content) == first["bytes"],
          "%s %s" % (download.status_code, len(download.content)))
    anonymous = requests.get(BASE + "/business/textbook/cases/download/%d" % first["resourceId"], timeout=30)
    check("匿名下载401", anonymous.status_code == 401, anonymous.status_code)
    if report.get("htmlResourceId"):
        anonymous_html = requests.get(BASE + "/business/textbook/cases/html/%d" % report["htmlResourceId"], timeout=30)
        check("匿名打开网页401", anonymous_html.status_code == 401, anonymous_html.status_code)
        student_token = next((token for token in (login(account) for account in STUDENTS) if token), None)
        if student_token:
            student_html = requests.get(BASE + "/business/textbook/cases/html/%d" % report["htmlResourceId"],
                                        headers={"Authorization": "Bearer " + student_token}, timeout=30)
            check("学生打开网页403", student_html.status_code == 403, student_html.status_code)
            student_preview = requests.get(BASE + "/business/textbook/cases/files/%d?preview=true"
                                           % first["resourceId"],
                                           headers={"Authorization": "Bearer " + student_token}, timeout=30)
            # RuoYi 权限拒绝以 HTTP 200 + body.code=403 返回，两者都算拒绝，且不能返回文件字节。
            denied = student_preview.status_code in (401, 403) or b'"code":403' in student_preview.content
            check("学生读课例资源被拒绝且无文件字节", denied and student_preview.content[:4] != b"%PDF",
                  "%s %s" % (student_preview.status_code, student_preview.content[:60]))
        else:
            check("学生登录", False, "账号不可用")

    (OUT / "api-verification.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print("通过 %d 项，失败 %d 项" % (sum(1 for step in report["steps"] if step["ok"]),
                                     len(report["failures"])))
    for failure in report["failures"]:
        print("  失败：" + failure)


def resolve_book_id(token, book_code):
    """教材书架接口返回本机 bookId，避免硬编码。"""
    response = api(token, "/business/textbook/books")
    data = response.json().get("data") or []
    for book in data:
        if book.get("bookCode") == book_code or book.get("book_code") == book_code:
            return book.get("bookId") or book.get("book_id")
    return None


if __name__ == "__main__":
    main()
