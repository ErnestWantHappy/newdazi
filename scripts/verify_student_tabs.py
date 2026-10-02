import os
import time
from playwright.sync_api import sync_playwright

def run():
    print("验证学生端数据分析实验题各 Tab 与交互...")
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(viewport={"width": 1440, "height": 900})
        page = context.new_page()

        # 登录学生
        page.goto("http://localhost/login", timeout=30000)
        page.wait_for_selector('input[placeholder="账号"]', timeout=10000)
        page.fill('input[placeholder="账号"]', '2020710101')
        page.fill('input[placeholder="密码"]', os.environ["LOCAL_STUDENT_PASSWORD"])
        page.click('button:has-text("登 录")')
        page.wait_for_timeout(2000)

        # 进入实验题
        page.goto("http://localhost/student/python-practice", timeout=30000)
        page.wait_for_selector('.data-lab-container', timeout=15000)
        page.wait_for_timeout(3000)

        # 1. 切换到【数据预览】Tab
        print("切换到【数据预览】Tab...")
        page.click('div[role="tab"]:has-text("数据预览")')
        page.wait_for_timeout(1500)
        page.screenshot(path="output/playwright/student_tab_data_preview.png")
        print(">> 数据预览截图已保存: output/playwright/student_tab_data_preview.png")

        # 2. 切换到【任务说明】Tab
        print("切换到【任务说明】Tab...")
        page.click('div[role="tab"]:has-text("任务说明")')
        page.wait_for_timeout(1000)
        page.screenshot(path="output/playwright/student_tab_task_desc.png")
        print(">> 任务说明截图已保存: output/playwright/student_tab_task_desc.png")

        # 3. 切换到【提交记录】Tab
        print("切换到【提交记录】Tab...")
        page.click('div[role="tab"]:has-text("提交记录")')
        page.wait_for_timeout(1500)
        page.screenshot(path="output/playwright/student_tab_submission_history.png")
        print(">> 提交记录截图已保存: output/playwright/student_tab_submission_history.png")

        browser.close()
        print("\n[SUCCESS] 学生端 Tab 冒烟测试全部通过！")

if __name__ == '__main__':
    run()
