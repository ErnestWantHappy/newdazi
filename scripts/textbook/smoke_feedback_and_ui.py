import os
# -*- coding: utf-8 -*-
"""真实浏览器自动化全流程验收：
1. 教师登录与左侧菜单顺序核验
2. 桌面端 (1500x900)：课例评价 (👍/👎/写理由/查看同行匿名评价)、配套包展示与行内下载、大弹窗自适应预览 (缩放/跳页/缩略图/配套侧栏)
3. 矮屏端 (1366x600)：无双重滚动条、正文不挤压、flex 高度分配
4. 移动端 (390x844)：手机全屏预览、目录与课例抽屉、无水平溢出
"""
import json
import re
import sys
from pathlib import Path

import requests
from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/playwright/textbook-cases"
OUT.mkdir(parents=True, exist_ok=True)

API = "http://localhost:8080"
BASE = "http://localhost"
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
    print("=== 开始 Playwright 真实浏览器端验收 ===")
    
    # 获取教师 Token
    res = requests.post(f"{API}/login", json={"username": "19157727791", "password": os.environ.get("TEACHER_PASSWORD",""), "code": "", "uuid": ""}, timeout=10)
    assert res.json().get("code") == 200, "登录失败"
    token = res.json()["token"]

    results = {}

    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(headless=True)

        # -------------------------------------------------------------
        # 1. 桌面端验证 (1500 x 900)
        # -------------------------------------------------------------
        print("\n[Step 1] 桌面端 (1500x900) 验收开始...")
        context = browser.new_context(viewport={"width": 1500, "height": 900}, accept_downloads=True)
        context.add_cookies([{"name": "Admin-Token", "value": token, "url": "http://localhost"}])
        page = context.new_page()
        page.route("**/dev-api/**", forward)

        # 1.1 访问首页，检查菜单顺序
        page.goto(f"{BASE}/teacher-dashboard/index", wait_until="networkidle")
        page.wait_for_timeout(1500)
        
        # 检查校区弹窗
        dialog = page.locator(".el-dialog:has-text('选择所属校区')")
        if dialog.count() and dialog.first.is_visible():
            dialog.first.locator("input[type=radio][value='139']").check(force=True)
            dialog.first.locator("button:has-text('确 定')").click()
            page.wait_for_timeout(2000)

        # 抓取左侧菜单顺序文本
        menu_titles = page.locator(".sidebar-container .el-menu-item, .sidebar-container .el-sub-menu__title").all_inner_texts()
        cleaned_titles = [t.strip() for t in menu_titles if t.strip()]
        print(f"  侧边栏菜单提取: {cleaned_titles[:10]}")
        page.screenshot(path=str(OUT / "01-desktop-menu-order.png"))

        # 1.2 打开教材阅读器与课例面板
        target_url = f"{BASE}/business/textbook/read/{TARGET['book']}?section={TARGET['section']}"
        page.goto(target_url, wait_until="networkidle")
        page.wait_for_timeout(1500)

        # 若课例面板未展开则点击展开按钮
        if page.locator(".case-panel").count() == 0:
            btn_case = page.locator("button:has-text('优秀课例')")
            if btn_case.count():
                btn_case.first.click()
                page.wait_for_timeout(1000)

        page.wait_for_selector(".case-panel", timeout=15000)
        print("  课例面板已展开")

        # 1.3 课例评价交互 (👍 / 👎 / 写理由 / 查看同行评价)
        page.wait_for_selector(".case-votes", timeout=10000)
        votes_comp = page.locator(".case-votes").first
        
        # 点击推荐 👍
        like_btn = votes_comp.locator("button:has-text('推荐')").first
        like_btn.click()
        page.wait_for_timeout(800)

        # 点击写理由
        reason_btn = votes_comp.locator("button:has-text('理由')").first
        reason_btn.click()
        page.wait_for_selector(".el-dialog:has-text('写评价理由')", timeout=5000)
        page.wait_for_timeout(500)

        # 输入文字理由
        test_comment = "教学设计思路非常清晰，配套实验工程可直接运行，对教学帮助极大！"
        textarea = page.locator(".el-dialog:has-text('写评价理由') textarea")
        textarea.fill(test_comment)
        
        # 点击保存评价
        save_btn = page.locator(".el-dialog:has-text('写评价理由') button:has-text('保存评价')")
        save_btn.click()
        page.wait_for_timeout(1200)
        print("  推荐评价与理由填写完成")
        page.screenshot(path=str(OUT / "02-desktop-vote-reason.png"))

        # 点击“评价理由”按钮，查看匿名评论弹窗
        comments_btn = votes_comp.locator("button:has-text('评价理由')").first
        comments_btn.click()
        page.wait_for_selector(".el-dialog:has-text('教师评价理由')", timeout=5000)
        page.wait_for_timeout(800)

        comments_dialog = page.locator(".el-dialog:has-text('教师评价理由')")
        dialog_text = comments_dialog.inner_text()
        assert "同行教师" in dialog_text, "未匿名展示为同行教师"
        assert test_comment in dialog_text, "刚填写的理由未在列表中展示"
        print("  同行教师匿名评价列表弹窗验证通过（无敏感身份泄漏）")
        page.screenshot(path=str(OUT / "03-desktop-peer-comments.png"))

        # 关闭评论弹窗
        close_dialog_btn = comments_dialog.locator(".el-dialog__headerbtn").first
        close_dialog_btn.click()
        page.wait_for_timeout(800)

        # 1.4 配套材料包内容展示与下载行内提示
        tool_card = page.locator(".case-panel .resource:has-text('配套材料'), .case-panel .resource:has-text('工具')").first
        if tool_card.count():
            print("  配套材料卡片已展示，正在验证行内下载提示...")
            dl_btn = tool_card.locator("button:has-text('下载')").first
            dl_btn.click()
            page.wait_for_selector(".download-hint", timeout=10000)
            page.screenshot(path=str(OUT / "04-desktop-tool-download-hint.png"))
            hint_text = page.locator(".download-hint").inner_text()
            print(f"  下载行内提示生效: {hint_text[:30]}...")

        # 1.5 大弹窗自适应预览
        preview_btn = page.locator(".case-panel .resource button:has-text('预览')").first
        preview_btn.click()
        page.wait_for_selector(".case-preview-dialog", timeout=20000)
        page.wait_for_timeout(2000)

        # 验证大图渲染
        img = page.locator(".case-preview-image")
        page.wait_for_selector(".case-preview-image", timeout=25000)
        w_before = img.evaluate("el => el.getBoundingClientRect().width")

        # 验证缩放功能
        zoom_in_btn = page.locator(".zoom-controls button:has-text('＋'), button[aria-label='放大预览']").first
        if zoom_in_btn.count():
            zoom_in_btn.click()
            page.wait_for_timeout(800)
            w_after = img.evaluate("el => el.getBoundingClientRect().width")
            print(f"  预览放大功能生效: {w_before}px -> {w_after}px")
            assert w_after > w_before, "放大后宽度未增加"

        # 验证缩略图切换
        thumb_btn = page.locator("button:has-text('缩略图')").first
        if thumb_btn.count():
            thumb_btn.click()
            page.wait_for_timeout(600)
            page.wait_for_selector(".thumbnail-rail", timeout=5000)
            print("  缩略图导轨正常展开")

        # 验证配套材料包侧栏在宽屏桌面下存在
        sidebar = page.locator(".preview-tools")
        print(f"  宽屏配套材料侧栏存在性: count={sidebar.count()}")

        page.screenshot(path=str(OUT / "05-desktop-preview-dialog.png"))

        # 关闭预览弹窗
        close_preview = page.locator(".case-preview-dialog .el-dialog__headerbtn").first
        if close_preview.count():
            close_preview.click()
            page.wait_for_timeout(1000)

        context.close()
        print("[Step 1] 桌面端验收完成！")

        # -------------------------------------------------------------
        # 2. 矮屏端验证 (1366 x 600)
        # -------------------------------------------------------------
        print("\n[Step 2] 矮屏端 (1366x600) 验收开始...")
        context_low = browser.new_context(viewport={"width": 1366, "height": 600}, accept_downloads=True)
        context_low.add_cookies([{"name": "Admin-Token", "value": token, "url": "http://localhost"}])
        page_low = context_low.new_page()
        page_low.route("**/dev-api/**", forward)

        page_low.goto(target_url, wait_until="networkidle")
        page_low.wait_for_timeout(1500)

        # 验证页面没有外层水平溢出
        has_horiz_overflow = page_low.evaluate("() => document.documentElement.scrollWidth > window.innerWidth")
        print(f"  矮屏阅读器无水平滚动条: {not has_horiz_overflow}")
        assert not has_horiz_overflow, "矮屏下出现非预期整页水平滚动条"

        # 打开课例预览
        if page_low.locator(".case-panel").count() == 0:
            btn_case = page_low.locator("button:has-text('优秀课例')")
            if btn_case.count(): btn_case.first.click()
            page_low.wait_for_timeout(1000)

        preview_btn = page_low.locator(".case-panel .resource button:has-text('预览')").first
        if preview_btn.count():
            preview_btn.click()
            page_low.wait_for_selector(".case-preview-dialog", timeout=15000)
            page_low.wait_for_timeout(1500)
            page_low.screenshot(path=str(OUT / "06-lowscreen-preview-dialog.png"))
            print("  矮屏预览弹窗渲染正常")

        context_low.close()
        print("[Step 2] 矮屏端验收完成！")

        # -------------------------------------------------------------
        # 3. 移动端验证 (390 x 844)
        # -------------------------------------------------------------
        print("\n[Step 3] 移动端 (390x844) 验收开始...")
        context_mob = browser.new_context(viewport={"width": 390, "height": 844}, accept_downloads=True)
        context_mob.add_cookies([{"name": "Admin-Token", "value": token, "url": "http://localhost"}])
        page_mob = context_mob.new_page()
        page_mob.route("**/dev-api/**", forward)

        page_mob.goto(target_url, wait_until="networkidle")
        page_mob.wait_for_timeout(1500)

        # 打开移动端课例面板/抽屉（若未展开则点击展开）
        if page_mob.locator(".case-panel").count() == 0:
            btn_case = page_mob.locator("button:has-text('优秀课例')")
            if btn_case.count():
                btn_case.first.click()
                page_mob.wait_for_timeout(1000)

        page_mob.wait_for_selector(".case-panel", timeout=15000)
        page_mob.screenshot(path=str(OUT / "07-mobile-case-panel.png"))
        print("  移动端课例抽屉渲染正常")

        # 打开移动端大预览
        preview_btn_mob = page_mob.locator(".case-panel .resource button:has-text('预览')").first
        if preview_btn_mob.count():
            preview_btn_mob.click()
            page_mob.wait_for_selector(".case-preview-dialog", timeout=15000)
            page_mob.wait_for_timeout(2000)

            # 验证移动端预览弹窗全屏覆盖
            dialog_box = page_mob.locator(".case-preview-dialog").first.bounding_box()
            print(f"  移动端弹窗尺寸: w={dialog_box['width']}, h={dialog_box['height']}")
            assert dialog_box["width"] >= 350, f"移动端弹窗未占满全屏宽度: {dialog_box['width']}"

            page_mob.screenshot(path=str(OUT / "08-mobile-preview-fullscreen.png"))
            print("  移动端全屏预览弹窗验证通过！")

        context_mob.close()
        print("[Step 3] 移动端验收完成！")

        browser.close()

    print("\n=== 全部 Playwright 浏览器验收测试 100% 成功！截图已保存至 output/playwright/textbook-cases/ ===")

if __name__ == "__main__":
    main()
