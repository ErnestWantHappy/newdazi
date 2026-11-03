# -*- coding: utf-8 -*-
"""电子教材教师端冒烟：登录 -> 首页课程卡胶囊 -> 书架 -> 阅读器 -> 手选课次 -> 打印预览。

产物：output/playwright/20261029-textbook-smoke/*.png
运行前需本机后端 8080、前端 dev 80 已启动。
"""
import json
import sys
from pathlib import Path

from playwright.sync_api import sync_playwright

BASE = "http://localhost"
OUT = Path(__file__).resolve().parents[2] / "output" / "playwright" / "20261029-textbook-smoke"
OUT.mkdir(parents=True, exist_ok=True)

results = []


def record(step, ok, detail=""):
    results.append({"step": step, "ok": bool(ok), "detail": str(detail)[:300]})
    print(f"[{'PASS' if ok else 'FAIL'}] {step} {detail}"[:400])


def main():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        ctx = browser.new_context(viewport={"width": 1440, "height": 900}, device_scale_factor=1)
        page = ctx.new_page()
        page.goto(f"{BASE}/login", wait_until="networkidle")

        # 登录
        page.fill("input[placeholder='请输入您的账号']", "19157727791")
        page.fill("input[placeholder='请输入您的密码']", "zdx5201ZDX!")
        page.click("button:has-text('登 录')")
        page.wait_for_timeout(2500)

        # 首次登录可能出现“选择所属校区”
        dialog = page.locator(".el-dialog:has-text('选择所属校区')")
        if dialog.count() and dialog.first.is_visible():
            dialog.first.locator("input[type=radio][value='139']").check(force=True)
            dialog.first.locator("button:has-text('确 定')").click()
            page.wait_for_timeout(2500)
        record("登录并进入教师首页", "teacher-dashboard" in page.url or "/index" in page.url, page.url)

        page.goto(f"{BASE}/teacher-dashboard/index", wait_until="networkidle")
        page.wait_for_selector(".lesson-folder", timeout=20000)
        chips = page.locator(".textbook-chip")
        record("课程卡出现教材胶囊", chips.count() > 0, f"chip={chips.count()}")
        page.screenshot(path=str(OUT / "01-teacher-dashboard-chip.png"))

        # 侧边菜单入口
        sidebar = page.locator(".sidebar-container a[href='/textbook']")
        record("侧边菜单存在 /textbook", sidebar.count() == 1, f"count={sidebar.count()}")

        # 书架
        page.goto(f"{BASE}/textbook", wait_until="networkidle")
        page.wait_for_selector(".book-card", timeout=20000)
        page.wait_for_timeout(1200)
        books = page.locator(".book-card:not(.is-pending)")
        pending = page.locator(".book-card.is-pending")
        covers = page.evaluate(
            "() => Array.from(document.querySelectorAll('.book-cover img')).map(i => [i.naturalWidth, i.naturalHeight])"
        )
        record("书架 13 册、0 张灰卡", books.count() == 13 and pending.count() == 0,
               f"books={books.count()} pending={pending.count()}")
        record("封面图按鉴权 blob 渲染", len(covers) == 13 and all(w == 1400 for w, h in covers), covers[:2])
        page.screenshot(path=str(OUT / "02-textbook-shelf.png"))

        # 阅读器：进入七上
        page.locator(".book-card", has_text="七上").first.click()
        page.wait_for_selector(".page-wrap", timeout=20000)
        page.wait_for_timeout(1200)
        record("打开七上阅读器", "/business/textbook/read/6" in page.url, page.url)
        record("进入时不预选课次", page.locator(".pick-hint").count() == 1,
               page.locator(".section-select").inner_text().strip())
        record("总页数 125", page.locator(".page-total").inner_text().strip() == "/ 125",
               page.locator(".page-total").inner_text().strip())
        page.screenshot(path=str(OUT / "03-reader-no-preselect.png"))

        # 手选第 1 课
        page.locator(".section-select").click()
        page.wait_for_selector(".el-select-dropdown__item", timeout=5000)
        page.locator(".el-select-dropdown__item", has_text="第1课 计算机网络").first.click()
        # 平滑滚动 + 懒加载图回填尺寸需要约 2 秒，等页码稳定到 11 再断言
        try:
            page.wait_for_function(
                "() => (document.querySelector('.foot-info')?.innerText || '').includes('第 11 页')",
                timeout=15000,
            )
        except Exception:
            pass
        foot = page.locator(".foot-info").inner_text().replace("\n", " ")
        record("手选课次后定位到本课", "第 11 页" in foot and "第1课" in foot, foot)
        page.screenshot(path=str(OUT / "04-reader-lesson-selected.png"))

        # 打印本课：拦截新窗口，校验生成的打印页
        with ctx.expect_page(timeout=15000) as new_page_info:
            page.locator("button:has-text('打印本课')").click()
        print_page = new_page_info.value
        try:
            print_page.wait_for_selector(".sheet img", timeout=20000)
        except Exception:
            pass
        sheets = print_page.locator(".sheet img")
        head = print_page.locator(".head").inner_text() if print_page.locator(".head").count() else ""
        record("打印本课生成 4 页", sheets.count() == 4, f"sheets={sheets.count()} head={head}")
        print_page.screenshot(path=str(OUT / "05-print-window.png"))
        print_page.close()

        # 滚动到中部，确认懒加载与页码联动
        page.evaluate(
            "() => { const w = document.querySelector('.page-wrap[data-page-no=\"60\"]');"
            " const p = document.querySelector('.pages');"
            " if (w) p.scrollTop += w.getBoundingClientRect().top - p.getBoundingClientRect().top; }"
        )
        page.wait_for_timeout(1200)
        try:
            page.wait_for_function(
                "() => (document.querySelector('.foot-info')?.innerText || '').includes('第 60 页')",
                timeout=15000,
            )
        except Exception:
            pass
        foot2 = page.locator(".foot-info").inner_text().replace("\n", " ")
        record("滚动后页码跟随", "第 60 页" in foot2, foot2)
        page.screenshot(path=str(OUT / "06-reader-scrolled.png"))

        # 课程卡胶囊跳转：六上
        page.goto(f"{BASE}/teacher-dashboard/index", wait_until="networkidle")
        page.wait_for_selector(".lesson-folder", timeout=20000)
        page.wait_for_timeout(800)
        page.locator(".lesson-folder", has_text="AI_AUDIT_课程二次编辑").first.locator(".textbook-chip").click()
        page.wait_for_selector(".page-wrap", timeout=20000)
        page.wait_for_timeout(800)
        record("课程卡胶囊直达对应册", "/business/textbook/read/4" in page.url,
               f"{page.url} | {page.locator('.book-title').inner_text()}")
        page.screenshot(path=str(OUT / "07-chip-to-book.png"))

        browser.close()

    ok = all(r["ok"] for r in results)
    (OUT / "result.json").write_text(json.dumps(results, ensure_ascii=False, indent=2), encoding="utf-8")
    print("SUMMARY", "ALL PASS" if ok else "HAS FAILURE")
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
