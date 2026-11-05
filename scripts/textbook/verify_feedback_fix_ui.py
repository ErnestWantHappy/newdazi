"""异常与多尺寸回归，写请求拦截模拟，不改变真实教师评价。"""
import copy
import json
import re
from pathlib import Path
import requests
from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / 'output/playwright/textbook-feedback-fix'
OUT.mkdir(parents=True, exist_ok=True)


def main():
    line = next(line for line in (ROOT / 'AGENTS.md').read_text(encoding='utf-8').splitlines() if '19157727791' in line and '|' in line)
    values = re.findall(r'`([^`]+)`', line)
    token = requests.post('http://localhost:8080/login', json={'username': values[0], 'password': values[1]}, timeout=30).json()['token']
    rows = requests.get('http://localhost:8080/business/textbook/cases/book/12/section/lesson-18', headers={'Authorization': 'Bearer '+token}, timeout=30).json()['data']
    state = {'vote': 1, 'reason': '原有理由', 'failVote': False, 'failPreview': False}
    seen, errors, result = [], [], {}
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        ctx = browser.new_context(viewport={'width': 1500, 'height': 900})
        ctx.add_cookies([{'name': 'Admin-Token', 'value': token, 'url': 'http://localhost'}])
        page = ctx.new_page()
        page.on('pageerror', lambda error: errors.append(str(error)))
        def route_api(route):
            request = route.request
            url = request.url
            if request.method in {'PUT', 'POST', 'DELETE', 'PATCH'} and '/textbook/cases/' in url:
                seen.append({'url': url, 'body': request.post_data_json})
                if url.endswith('/invitation'):
                    route.fulfill(json={'code': 200, 'data': {'show': False}})
                elif state['failVote']:
                    route.fulfill(json={'code': 500, 'msg': '模拟网络保存失败'})
                else:
                    body = request.post_data_json
                    state['vote'] = body['vote']
                    if not state['vote']: state['reason'] = None
                    elif 'reason' in body: state['reason'] = (body['reason'] or '').strip() or None
                    data = {'myVote': state['vote'], 'likes': int(state['vote'] == 1), 'dislikes': int(state['vote'] == -1)}
                    if state['reason'] is not None: data['myReason'] = state['reason']
                    route.fulfill(json={'code': 200, 'data': data})
            elif '/cases/book/12/section/lesson-18' in url:
                data = copy.deepcopy(rows)
                data[0].update({'myVote': state['vote'], 'myReason': state['reason']})
                route.fulfill(json={'code': 200, 'data': data})
            elif '/preview-info' in url and state['failPreview']:
                route.fulfill(json={'code': 500, 'msg': '模拟预览失败'})
            else:
                route.fulfill(response=route.fetch(url=url.replace('http://localhost/dev-api', 'http://localhost:8080', 1)))
        page.route('**/dev-api/**', route_api)
        target = 'http://localhost/business/textbook/read/12?section=lesson-18'
        page.goto(target, wait_until='networkidle')
        sidebar = [title.strip() for title in page.locator('.sidebar-container .el-menu-item:visible, .sidebar-container .el-sub-menu__title:visible').all_inner_texts()]
        assert sidebar.index('电子教材与优秀课例') == sidebar.index('题库管理') + 1
        assert sidebar.index('成绩查询') == sidebar.index('导学单管理') + 1
        assert sidebar.index('教研活动') == sidebar.index('学生管理') - 1
        result['actualSidebarOrder'] = sidebar
        votes = page.locator('.case-panel .case-votes').first
        votes.locator('button.chosen').click()
        page.wait_for_timeout(250)
        assert votes.get_by_text('写理由', exact=True).count() == 1
        votes.get_by_text('推荐', exact=True).first.click()
        page.wait_for_timeout(250)
        assert 'reason' not in seen[-1]['body'] and state['reason'] is None
        result['withdrawDoesNotReviveReason'] = True
        votes.get_by_text('写理由', exact=True).click()
        dialog = page.locator('.el-dialog').filter(has=page.get_by_text('写评价理由', exact=True))
        dialog.locator('textarea').fill('保存失败仍保留的内容')
        state['failVote'] = True
        dialog.get_by_text('保存评价', exact=True).click()
        page.wait_for_timeout(400)
        assert dialog.is_visible() and dialog.locator('textarea').input_value() == '保存失败仍保留的内容'
        assert dialog.locator('[role=alert]').count() == 1
        state['failVote'] = False
        dialog.get_by_text('保存评价', exact=True).click()
        dialog.wait_for(state='hidden')
        result['failedSaveKeepsDraftAndRetrySucceeds'] = True
        votes.get_by_text('修改理由', exact=True).click()
        dialog.locator('textarea').fill('')
        dialog.get_by_text('保存评价', exact=True).click()
        dialog.wait_for(state='hidden')
        assert state['reason'] is None
        assert votes.get_by_text('写理由', exact=True).count() == 1
        result['explicitClearReason'] = True
        # 未评价状态下，失败预览即使聚焦且时间流逝也不能邀请。
        votes.locator('button.chosen').click()
        page.wait_for_timeout(250)
        state['failPreview'] = True
        def open_preview():
            page.locator('.case-panel .resource').filter(has=page.get_by_text('预览', exact=True)).first.get_by_text('预览', exact=True).click()
        open_preview()
        page.wait_for_selector('.case-preview [role=alert]')
        page.evaluate('window.dispatchEvent(new Event("blur"));window.dispatchEvent(new Event("focus"));window.__originalNow=performance.now.bind(performance);Object.defineProperty(performance,"now",{configurable:true,value:()=>window.__originalNow()+31000})')
        before = len([r for r in seen if r['url'].endswith('/invitation')])
        page.locator('.case-preview-dialog .el-dialog__headerbtn').click()
        page.wait_for_timeout(400)
        assert len([r for r in seen if r['url'].endswith('/invitation')]) == before
        page.evaluate('Object.defineProperty(performance,"now",{configurable:true,value:window.__originalNow})')
        result['failedPreviewDoesNotInvite'] = True
        state['failPreview'] = False
        open_preview()
        page.wait_for_selector('.case-preview-image')
        page.evaluate('window.dispatchEvent(new Event("blur"));window.dispatchEvent(new Event("focus"));Object.defineProperty(performance,"now",{configurable:true,value:()=>window.__originalNow()+31000})')
        page.locator('.case-preview-dialog .el-dialog__headerbtn').click()
        page.wait_for_timeout(400)
        assert len([r for r in seen if r['url'].endswith('/invitation')]) == before + 1
        page.evaluate('Object.defineProperty(performance,"now",{configurable:true,value:window.__originalNow})')
        result['successfulActivePreviewCanInvite'] = True
        result['layouts'] = []
        for width, height in [(1500, 900), (1366, 600), (390, 844), (320, 568)]:
            page.set_viewport_size({'width': width, 'height': height})
            page.reload(wait_until='networkidle')
            open_preview()
            page.wait_for_selector('.case-preview-image')
            page.wait_for_timeout(400)
            rect = page.locator('.case-preview-dialog').bounding_box()
            if width <= 600:
                assert abs(rect['y']) < 1 and abs(rect['x']) < 1
                assert abs(rect['width'] - width) < 1 and abs(rect['height'] - height) < 1
            assert not page.evaluate('document.documentElement.scrollWidth>innerWidth')
            result['layouts'].append({'viewport': [width, height], 'dialog': rect})
            page.screenshot(path=str(OUT / f'preview-{width}x{height}.png'))
        assert not errors, errors
        result['pageErrors'] = errors
        ctx.close(); browser.close()
    (OUT / 'result.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps(result, ensure_ascii=False, indent=2))


if __name__ == '__main__': main()
