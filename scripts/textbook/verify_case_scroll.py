"""连续预览：真实滚轮/手机触摸、跳页、混合页面与长文档懒加载。"""
import argparse
import json
import re
from pathlib import Path
from urllib.parse import urlsplit, parse_qs
import requests
from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / 'output/playwright/textbook-case-scroll'
OUT.mkdir(parents=True, exist_ok=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--production', action='store_true')
    args = parser.parse_args()
    base = 'http://10.52.1.130' if args.production else 'http://localhost'
    api = base + '/prod-api' if args.production else 'http://localhost:8080'
    line = next(line for line in (ROOT / 'AGENTS.md').read_text(encoding='utf-8').splitlines() if '19157727791' in line and '|' in line)
    values = re.findall(r'`([^`]+)`', line)
    token = requests.post(api + '/login', json={'username': values[0], 'password': values[1]}, timeout=30).json()['token']
    headers = {'Authorization': 'Bearer ' + token}
    summary = requests.get(api + '/business/textbook/cases/summary', headers=headers, timeout=30).json()['data']
    selected = None
    for row in summary:
        cases = requests.get(api + f"/business/textbook/cases/book/{row['bookId']}/section/{row['sectionKey']}", headers=headers, timeout=30).json()['data']
        for case in cases:
            slides = next((r for r in case['resources'] if r['category'] == 'SLIDES' and int(r['previewable'])), None)
            if slides:
                selected = (row, slides); break
        if selected: break
    assert selected
    row, slides = selected
    results = {}
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        for width, height in [(1500, 900), (1366, 600), (390, 844), (320, 568)]:
            ctx = browser.new_context(viewport={'width': width, 'height': height}, has_touch=width < 600)
            ctx.add_cookies([{'name': 'Admin-Token', 'value': token, 'url': base}])
            page = ctx.new_page(); errors, full_requests = [], []
            state = {'synthetic': False, 'failPage': None}
            page.on('pageerror', lambda e: errors.append(str(e)))
            def intercept(route):
                url = urlsplit(route.request.url)
                match = re.search(r'/cases/files/\d+/pages/(\d+)$', url.path)
                is_thumb = parse_qs(url.query).get('thumbnail') == ['true']
                if match and not is_thumb: full_requests.append(int(match[1]))
                if '/cases/' in url.path and route.request.method in {'POST', 'PUT'}:
                    route.fulfill(json={'code': 200, 'data': {'show': False}})
                elif state['synthetic'] and url.path.endswith('/preview-info'):
                    route.fulfill(json={'code': 200, 'data': {'pages': 200}})
                elif state['synthetic'] and match:
                    number = int(match[1])
                    if state['failPage'] == number:
                        route.fulfill(json={'code': 500, 'msg': '模拟该页加载失败'})
                    else:
                        w, h = (900, 1600) if number == 150 else (1200, 675)
                        route.fulfill(content_type='image/svg+xml', body=f'<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}"><rect width="100%" height="100%" fill="white"/><text x="30" y="80" font-size="48">Page {number}</text></svg>')
                else:
                    target = route.request.url.replace(base + ('/prod-api' if args.production else '/dev-api'), api, 1)
                    try:
                        route.fulfill(response=route.fetch(url=target))
                    except Exception:
                        try: route.abort()
                        except Exception: pass
            page.route('**/' + ('prod-api' if args.production else 'dev-api') + '/**', intercept)
            target = base + f"/business/textbook/read/{row['bookId']}?section={row['sectionKey']}"
            page.goto(target, wait_until='networkidle')
            resource = page.locator('.case-panel .resource').filter(has=page.get_by_text(slides['title'], exact=True)).first
            resource.get_by_text('预览', exact=True).click()
            first = page.locator('[data-preview-page="1"] .case-preview-image')
            first.wait_for(timeout=60000)
            canvas = page.locator('.preview-canvas')
            jump = page.locator('.preview-toolbar .el-input-number input')
            if width > 600:
                canvas.hover(); page.mouse.wheel(0, 700)
            else:
                box = canvas.bounding_box()
                session = ctx.new_cdp_session(page)
                x, start, end = box['x'] + box['width']/2, box['y'] + box['height']*0.8, box['y'] + 20
                session.send('Input.dispatchTouchEvent', {'type': 'touchStart', 'touchPoints': [{'x': x, 'y': start}]})
                for i in range(1, 9):
                    session.send('Input.dispatchTouchEvent', {'type': 'touchMove', 'touchPoints': [{'x': x, 'y': start+(end-start)*i/8}]})
                    page.wait_for_timeout(30)
                session.send('Input.dispatchTouchEvent', {'type': 'touchEnd', 'touchPoints': []})
            page.wait_for_function('Number(document.querySelector(".preview-toolbar .el-input-number input").value)>1')
            current = int(jump.input_value())
            page.locator(f'[data-preview-page="{current}"] .case-preview-image').wait_for(timeout=60000)
            assert canvas.evaluate('el=>el.scrollTop') > 0
            page.get_by_role('button', name='缩略图', exact=True).click()
            page.locator('.thumbnail-rail button[data-page="3"]').click()
            page.wait_for_function('document.querySelector(".preview-toolbar .el-input-number input").value==="3"')
            page.locator('[data-preview-page="3"] .case-preview-image').wait_for(timeout=60000)
            assert page.locator('.thumbnail-rail button.selected').get_attribute('data-page') == '3'
            # 上滚、跳页和缩放不重新回到第一页。
            jump.fill('1'); jump.press('Enter'); first.wait_for(timeout=60000)
            assert jump.input_value() == '1'
            page.screenshot(path=str(OUT / f'{"production" if args.production else "local"}-{width}x{height}.png'))
            results[str(width)] = {'wheelOrTouchPage': current, 'thumbnailAndJump': True, 'peakImages': page.locator('.case-preview-image').count()}
            if not args.production and width == 1500:
                page.locator('.case-preview-dialog .el-dialog__headerbtn').click()
                page.wait_for_timeout(400)
                state['synthetic'] = True; full_requests.clear()
                resource.get_by_text('预览', exact=True).click(); first.wait_for(timeout=15000)
                page.wait_for_timeout(500)
                assert len(full_requests) < 10, full_requests
                state['failPage'] = 150
                jump.fill('150'); jump.press('Enter')
                failure = page.locator('[data-preview-page="150"] [role=alert]')
                failure.wait_for(timeout=15000)
                state['failPage'] = None
                failure.get_by_text('重试本页', exact=True).click()
                page.locator('[data-preview-page="150"] .case-preview-image').wait_for(timeout=15000)
                assert jump.input_value() == '150'
                assert page.locator('.case-preview-image').count() < 10
                page.get_by_role('button', name='放大预览', exact=True).click()
                page.wait_for_timeout(300)
                assert jump.input_value() == '150'
                results['longDocument'] = {'pages': 200, 'loadedPages': full_requests, 'boundedImages': page.locator('.case-preview-image').count(), 'mixedOrientationRetry': True}
            assert not errors, errors
            page.unroute_all(behavior='ignoreErrors')
            ctx.close()
        browser.close()
    (OUT / ('production-result.json' if args.production else 'local-result.json')).write_text(json.dumps(results, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps(results, ensure_ascii=False))


if __name__ == '__main__': main()
