import os
# -*- coding: utf-8 -*-
import sys
import time
from playwright.sync_api import sync_playwright

def run():
    print("启动 Playwright 验证教师端题单设计器与作业批改抽屉...")
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(viewport={"width": 1440, "height": 900})
        page = context.new_page()

        page.goto("http://localhost/login", timeout=30000)
        page.fill('input[placeholder="账号"]', '19157727791')
        page.fill('input[placeholder="密码"]', os.environ["LOCAL_TEACHER_PASSWORD"])
        page.click('button:has-text("登 录")')
        page.wait_for_timeout(2000)

        page.goto("http://localhost/python-practice", timeout=30000)
        page.wait_for_timeout(3000)

        # 点击表格中第一行的“设计题单”
        design_btn = page.locator('text="设计题单"').first
        assert design_btn.is_visible(), '未找到设计题单按钮'
        if design_btn.is_visible():
            design_btn.click()
            page.wait_for_timeout(3000)

            shot_designer = "output/playwright/teacher_designer_view.png"
            page.screenshot(path=shot_designer)
            print(f">> 教师端题单设计器截图已保存: {shot_designer}")

            # 寻找“批改作业”按钮
            grading_btn = page.locator('text="批改作业"').first
            if grading_btn.is_visible():
                grading_btn.click()
                page.wait_for_timeout(3000)
                shot_grading = "output/playwright/teacher_grading_drawer.png"
                page.screenshot(path=shot_grading)
                print(f">> 实验作业批改抽屉截图已保存: {shot_grading}")
            else:
                raise AssertionError('未找到批改作业按钮')

        browser.close()
        print("\n[SUCCESS] 教师端验证完成！")

if __name__ == '__main__':
    run()
