from playwright.sync_api import sync_playwright
import json

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    context = browser.new_context()
    page = context.new_page()
    
    # Login as admin
    page.goto('http://localhost:8083/login')
    page.wait_for_timeout(2000)
    page.fill('input[placeholder="账号"]', 'admin')
    page.fill('input[placeholder="密码"]', 'admin123')
    page.click('button:has-text("登 录")')
    page.wait_for_timeout(3000)
    
    cookies = context.cookies()
    token = next(c['value'] for c in cookies if c['name'] == 'Admin-Token')
    
    # Get sheet 24 directly
    result = page.evaluate("""
        async (token) => {
            const headers = { 'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json' };
            const resp = await fetch('/dev-api/business/guide-sheet/24', { headers });
            const data = await resp.json();
            return data;
        }
    """, token)
    
    print('API response keys:', list(result.keys()) if result else 'None')
    if 'data' in result:
        sheet = result['data']
        print('Sheet title:', sheet.get('sheetTitle'))
        form_json = sheet.get('formJson')
        if form_json:
            form = json.loads(form_json) if isinstance(form_json, str) else form_json
            print('\nForm JSON length:', len(json.dumps(form)))
            
            def find_scoring(node, depth=0, prefix=''):
                if depth > 15:
                    return
                if isinstance(node, dict):
                    t = node.get('type', '')
                    nid = node.get('id', '')
                    sc = node.get('_scoringConfig')
                    if sc:
                        print(f'{prefix}Field: {nid} (type={t})')
                        print(f'{prefix}  _scoringConfig: {json.dumps(sc, ensure_ascii=False)}')
                    elif t in ('radio', 'checkbox', 'select', 'cascader', 'textarea', 'input', 'color-picker'):
                        pass  # fields without scoring are not graded
                    for k, v in node.items():
                        find_scoring(v, depth+1, prefix+'  ')
                elif isinstance(node, list):
                    for i, item in enumerate(node):
                        find_scoring(item, depth+1, prefix)
            
            find_scoring(form)
        else:
            print('No formJson')
    else:
        print('Result:', result)
    
    browser.close()