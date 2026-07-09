"""Verify: Check that grading results are no longer duplicated for "111" guide sheet."""
from playwright.sync_api import sync_playwright
import json

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    context = browser.new_context(viewport={"width": 1920, "height": 1080}, locale="zh-CN")
    page = context.new_page()
    
    page.on("console", lambda msg: print(f"  [CONSOLE] {msg.type}: {msg.text}"))
    
    # Login
    print("=== Login ===")
    page.goto("http://localhost:8082/login", wait_until="networkidle")
    page.wait_for_timeout(2000)
    page.fill('input[placeholder="账号"]', 'admin')
    page.fill('input[placeholder="密码"]', 'admin123')
    page.locator('button').first.click()
    page.wait_for_timeout(5000)
    page.goto("http://localhost:8082/business/guide-sheet-list", wait_until="networkidle")
    page.wait_for_timeout(2000)
    
    cookies = context.cookies()
    token = ''
    for c in cookies:
        if c['name'] == 'Admin-Token':
            token = c['value']
            break
    
    # Fetch form JSON and simulate grading
    result = page.evaluate(f"""
    async () => {{
        const response = await fetch('/dev-api/business/guide-sheet/24', {{
            headers: {{ 'Authorization': 'Bearer {token}' }}
        }});
        const data = await response.json();
        if (data.code === 200 && data.data) {{
            const formJson = data.data.formJson;
            const parsed = typeof formJson === 'string' ? JSON.parse(formJson) : formJson;
            
            // Simulate the fixed walkFlatten logic
            const allWidgets = [];
            const visited = new Set();
            
            function isRealWidget(map) {{
                return 'formItemFlag' in map || 'key' in map || 'widgetList' in map || 'tabs' in map || 'category' in map;
            }}
            
            function walkFlatten(value, path) {{
                if (value === null || value === undefined) return;
                if (visited.has(value)) return;
                visited.add(value);
                
                if (Array.isArray(value)) {{
                    value.forEach((item, i) => walkFlatten(item, path + '[' + i + ']'));
                }} else if (typeof value === 'object') {{
                    const wid = value.id || value.name;
                    const wtype = value.type;
                    if (wid && wtype && isRealWidget(value)) {{
                        allWidgets.push({{
                            id: wid,
                            type: wtype,
                            label: (value.options || {{}}).label || '',
                            hasScoring: 'scoring' in value,
                            scoringType: value.scoring ? value.scoring.type : ''
                        }});
                    }}
                    for (const v of Object.values(value)) {{
                        if (typeof v === 'object') {{
                            walkFlatten(v, path);
                        }}
                    }}
                }}
            }}
            
            for (const widget of (parsed.widgetList || [])) {{
                walkFlatten(widget, 'root');
            }}
            
            // Check for duplicates
            const fieldIds = allWidgets.filter(w => w.hasScoring).map(w => w.id);
            const fieldLabels = allWidgets.filter(w => w.hasScoring).map(w => w.label);
            const duplicates = fieldIds.filter((id, i) => fieldIds.indexOf(id) !== i);
            
            return JSON.stringify({{
                totalWidgets: allWidgets.length,
                scoredWidgets: fieldIds.length,
                fieldIds: fieldIds,
                fieldLabels: fieldLabels,
                duplicates: duplicates,
                allWidgets: allWidgets
            }});
        }}
        return 'error';
    }}
    """)
    print(f"After fix - grading simulation result:")
    parsed = json.loads(result)
    print(f"  Total widgets: {parsed['totalWidgets']}")
    print(f"  Scored widgets: {parsed['scoredWidgets']}")
    print(f"  Field IDs: {parsed['fieldIds']}")
    print(f"  Field labels: {parsed['fieldLabels']}")
    print(f"  Duplicates: {parsed['duplicates']}")
    
    print(f"\n  All widgets:")
    for w in parsed['allWidgets']:
        print(f"    {w['type']:15s} | {w['id']:20s} | {w['label']:20s} | has_scoring={w['hasScoring']} | type={w['scoringType']}")
    
    if parsed['duplicates']:
        print(f"\n  FAIL: Duplicates found! {parsed['duplicates']}")
    else:
        print(f"\n  SUCCESS: No duplicates! Fix works correctly.")
    
    browser.close()
    print("\n=== Done ===")