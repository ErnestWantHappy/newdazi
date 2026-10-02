import os
import sys
import time
from playwright.sync_api import sync_playwright

def run():
    print("启动 Playwright 进行本地真实浏览器冒烟测试...")
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(viewport={"width": 1440, "height": 900})
        page = context.new_page()

        # ==========================================
        # 1. 学生端验证
        # ==========================================
        print("步骤 1: 访问本地登录页 http://localhost/login")
        page.goto("http://localhost/login", timeout=30000)
        page.wait_for_selector('input[placeholder="账号"]', timeout=10000)

        print("步骤 2: 输入学生账号 2020710101 登录")
        page.fill('input[placeholder="账号"]', '2020710101')
        page.fill('input[placeholder="密码"]', os.environ["LOCAL_STUDENT_PASSWORD"])
        page.click('button:has-text("登 录")')

        # 等待登录成功跳转
        page.wait_for_timeout(2000)
        print("步骤 3: 跳转至学生 Python 练习页面 http://localhost/student/python-practice")
        page.goto("http://localhost/student/python-practice", timeout=30000)

        # 等待实验题渲染
        try:
            page.wait_for_selector('.data-lab-container', timeout=15000)
            print(">> 成功检测到数据分析实验题专属组件 (.data-lab-container)")
        except Exception as e:
            raise AssertionError('实验题组件未出现，不能判定验收通过') from e

        # 等待一下界面渲染完全
        page.wait_for_timeout(3000)
        shot_path_student = "output/playwright/student_datalab_practice.png"
        page.screenshot(path=shot_path_student)
        print(f">> 学生端实验题页面截图已保存: {shot_path_student}")

        # ==========================================
        # 2. 教师端验证
        # ==========================================
        print("\n步骤 4: 清除会话并登录教师账号 19157727791")
        context.clear_cookies()
        page.goto("http://localhost/login", timeout=30000)
        page.wait_for_selector('input[placeholder="账号"]', timeout=10000)

        page.fill('input[placeholder="账号"]', '19157727791')
        page.fill('input[placeholder="密码"]', os.environ["LOCAL_TEACHER_PASSWORD"])
        page.click('button:has-text("登 录")')

        page.wait_for_timeout(2000)
        print("步骤 5: 跳转至教师端题单管理页面 http://localhost/python-practice")
        page.goto("http://localhost/python-practice", timeout=30000)
        page.wait_for_timeout(3000)

        shot_path_teacher = "output/playwright/teacher_python_practice.png"
        page.screenshot(path=shot_path_teacher)
        print(f">> 教师端题单管理页面截图已保存: {shot_path_teacher}")

        browser.close()
        print("\n[SUCCESS] 本地真实浏览器冒烟测试圆满完成！")

if __name__ == '__main__':
    run()
