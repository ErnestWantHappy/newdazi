"""本机真实评价API回归，先保存两行状态，结束后精确恢复。"""
import concurrent.futures
import json
import re
from pathlib import Path
import pymysql
import requests
import yaml

ROOT = Path(__file__).resolve().parents[2]
API = 'http://localhost:8080'


def main():
    line = next(line for line in (ROOT / 'AGENTS.md').read_text(encoding='utf-8').splitlines() if '19157727791' in line and '|' in line)
    credentials = re.findall(r'`([^`]+)`', line)
    token = requests.post(API + '/login', json={'username': credentials[0], 'password': credentials[1]}, timeout=30).json()['token']
    headers = {'Authorization': 'Bearer ' + token}
    uid = requests.get(API + '/getInfo', headers=headers, timeout=30).json()['user']['userId']
    master = yaml.safe_load((ROOT / 'RuoYi-Vue/ruoyi-admin/src/main/resources/application-druid.yml').read_text(encoding='utf-8'))['spring']['datasource']['druid']['master']
    match = re.match(r'jdbc:mysql://([^:/]+):(\d+)/([^?]+)', master['url'])
    assert match[1] in {'localhost', '127.0.0.1'}
    db = pymysql.connect(host=match[1], port=int(match[2]), user=master['username'], password=str(master['password']), database=match[3], charset='utf8mb4')
    cases = requests.get(API + '/business/textbook/cases/book/12/section/lesson-18', headers=headers, timeout=30).json()['data']
    cid = cases[0]['caseId']
    snapshots = []
    with db.cursor(pymysql.cursors.DictCursor) as cursor:
        for table, where, params in [('biz_textbook_case_feedback', 'case_id=%s AND user_id=%s', (cid, uid)), ('biz_textbook_case_invitation', 'user_id=%s', (uid,))]:
            cursor.execute('SELECT * FROM ' + table + ' WHERE ' + where, params)
            snapshots.append((table, where, params, cursor.fetchone()))
    db.commit()
    def vote(value, **extra):
        response = requests.put(API + f'/business/textbook/cases/{cid}/feedback', headers=headers, json={'vote': value, **extra}, timeout=30).json()
        assert response.get('code') == 200, {'code': response.get('code'), 'message': response.get('msg')}
        return response['data']
    try:
        assert vote(1, reason='API回归临时理由')['myVote'] == 1
        with concurrent.futures.ThreadPoolExecutor(max_workers=6) as executor:
            rows = list(executor.map(lambda _: vote(-1), range(12)))
        assert all(row['myVote'] == -1 for row in rows), [row['myVote'] for row in rows]
        assert vote(-1, reason='').get('myReason') is None
        vote(1, reason='撤销前临时理由'); vote(0)
        assert vote(1).get('myReason') is None
        rejected = requests.put(API + f'/business/textbook/cases/{cid}/feedback', headers=headers, json={'vote': 1, 'reason': '字' * 501}, timeout=30).json()
        assert rejected.get('code') != 200
        with db.cursor() as cursor:
            cursor.execute('SELECT COUNT(*) FROM biz_textbook_case_feedback WHERE case_id=%s AND user_id=%s', (cid, uid))
            assert cursor.fetchone()[0] == 1
        report = {'realApiRegression': 'PASS', 'concurrentRepeats': 12, 'scenarios': ['赞踩改票', '显式清空理由', '撤销重评无理由', '501字拒绝', '唯一行'], 'rowsRestored': True}
    finally:
        with db.cursor() as cursor:
            for table, where, params, row in snapshots:
                cursor.execute('DELETE FROM ' + table + ' WHERE ' + where, params)
                if row:
                    columns = list(row)
                    cursor.execute('INSERT INTO ' + table + '(' + ','.join(columns) + ') VALUES(' + ','.join(['%s'] * len(columns)) + ')', [row[col] for col in columns])
        db.commit(); db.close()
    print(json.dumps(report, ensure_ascii=False))


if __name__ == '__main__': main()
