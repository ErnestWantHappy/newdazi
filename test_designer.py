from playwright.sync_api import sync_playwright
import time

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    page = browser.new_page()

    # Collect console errors
    errors = []
    page.on("console", lambda msg: errors.append(f"[{msg.type}] {msg.text}") if msg.type in ("error", "warning") else None)

    # Step 1: Login
    print("=== Step 1: Login ===")
    page.goto('http://localhost:8082/')
    page.wait_for_load_state('networkidle')
    page.wait_for_timeout(2000)

    # Check if we're on login page
    content = page.content()
    if 'login' in content.lower() or '登录' in content:
        page.fill('input[placeholder*="账号"]', 'admin')
        page.fill('input[placeholder*="密码"]', 'admin123')
        page.click('button:has-text("登录")')
        page.wait_for_load_state('networkidle')
        page.wait_for_timeout(3000)
        print("  Logged in")
    else:
        print("  Already logged in or different page")

    # Step 2: Navigate to guide sheet list
    print("\n=== Step 2: Navigate to guide sheet list ===")
    page.goto('http://localhost:8082/#/business/guide-sheet-list')
    page.wait_for_load_state('networkidle')
    page.wait_for_timeout(3000)
    page.screenshot(path='/tmp/test_list.png', full_page=True)
    print("  Screenshot saved: /tmp/test_list.png")

    # Step 3: Check page for guide sheet list
    print("\n=== Step 3: Check page content ===")
    content = page.content()
    if '导学单' in content or 'guide' in content.lower():
        print("  Page contains guide sheet content")
    else:
        print("  WARNING: Page may not have loaded correctly")

    # Step 4: Navigate to designer page
    print("\n=== Step 4: Navigate to designer ===")
    page.goto('http://localhost:8082/#/business/guide-sheet')
    page.wait_for_load_state('networkidle')
    page.wait_for_timeout(5000)
    page.screenshot(path='/tmp/test_designer.png', full_page=True)
    print("  Screenshot saved: /tmp/test_designer.png")

    # Check designer content
    content = page.content()
    if '导学单标题' in content:
        print("  Designer page loaded: title section found")
    if '评分配置' in content:
        print("  Designer page loaded: scoring section found")
    if '表单设计器' in content or 'v-form-designer' in content.lower():
        print("  Designer page loaded: form designer found")
    if '关联课程' in content:
        print("  Course selector found")

    # Step 5: Check for the scoring card
    print("\n=== Step 5: Check scoring card ===")
    if '评分配置' in content:
        print("  Scoring card is present")
        if '启用自动评分' in content:
            print("  Scoring switch is present")
        if '刷新字段' in content:
            print("  Refresh button is present")
        if 'AI评分' in content or 'ai' in content.lower():
            print("  AI scoring option is present")
    else:
        print("  WARNING: Scoring card not found")

    # Step 6: Check for removed elements
    print("\n=== Step 6: Verify removed elements ===")
    if '教师机IP' in content:
        print("  ERROR: teacherMachineIp still present!")
    else:
        print("  teacherMachineIp removed - OK")
    if '页面预览' in content:
        print("  ERROR: '页面预览' button still present!")
    else:
        print("  '页面预览' button removed - OK")

    # Step 7: Print console errors
    print("\n=== Step 7: Console errors/warnings ===")
    if errors:
        for e in errors:
            print(f"  {e}")
    else:
        print("  No console errors or warnings")

    # Step 8: Try clicking the scoring switch
    print("\n=== Step 8: Test scoring switch ===")
    try:
        switch = page.locator('text=启用自动评分').first
        if switch.is_visible():
            switch.click()
            page.wait_for_timeout(1000)
            # Check if API key input appears
            content = page.content()
            if 'API-Key' in content or 'api' in content.lower():
                print("  API Key input appeared after enabling scoring - OK")
            else:
                print("  WARNING: API Key input not visible after enabling scoring")
            page.screenshot(path='/tmp/test_scoring_enabled.png', full_page=True)
            print("  Screenshot saved: /tmp/test_scoring_enabled.png")
    except Exception as e:
        print(f"  Could not click scoring switch: {e}")

    browser.close()
    print("\n=== Test Complete ===")