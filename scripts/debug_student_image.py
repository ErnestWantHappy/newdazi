import os
from playwright.sync_api import sync_playwright

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    page = browser.new_page()
    page.on('response', lambda resp: print('资源请求状态:', resp.status) if 'resource' in resp.url or 'charts' in resp.url or 'datalab' in resp.url else None)
    page.goto('http://localhost/login')
    page.fill('input[placeholder="账号"]', '2020710101')
    page.fill('input[placeholder="密码"]', os.environ["LOCAL_STUDENT_PASSWORD"])
    page.click('button:has-text("登 录")')
    page.wait_for_timeout(2000)
    page.goto('http://localhost/student/python-practice')
    page.wait_for_selector('.data-lab-container')
    page.wait_for_timeout(2000)
    page.click('div[role="tab"]:has-text("提交记录")')
    page.wait_for_timeout(3000)
    browser.close()
