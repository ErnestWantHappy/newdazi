#!/usr/bin/env python3
"""经固定指纹跳板使用正式 Judge0，验证 Python V3 题包的全部测试点。"""

from __future__ import annotations

import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "output"))

from migration_resume import Session  # noqa: E402


REMOTE_PAYLOAD = "/tmp/python-zero-basis-v3-cases.json"
REMOTE_RUNNER = "/tmp/python-zero-basis-v3-judge0.py"
OUTPUT = ROOT / "output/python-zero-basis-v3/judge0-report.json"


RUNNER = r'''#!/usr/bin/env python3
import base64
import concurrent.futures
import json
import time
import urllib.error
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

ENV_PATH = Path('/data/apps/xueyeceping/shared/xueyeceping-130.env')
PAYLOAD_PATH = Path('/tmp/python-zero-basis-v3-cases.json')


def load_env():
    values = {}
    for raw in ENV_PATH.read_text(encoding='utf-8').splitlines():
        line = raw.strip()
        if not line or line.startswith('#') or '=' not in line:
            continue
        key, value = line.split('=', 1)
        key = key.strip()
        if key.startswith('export '):
            key = key[7:].strip()
        values[key] = value.strip().strip('"').strip("'")
    required = ('JUDGE0_BASE_URL', 'JUDGE0_AUTH_TOKEN')
    if any(not values.get(key) for key in required):
        raise RuntimeError('Judge0 服务或认证令牌未配置')
    return values


ENV = load_env()
BASE = ENV['JUDGE0_BASE_URL'].rstrip('/')
AUTH_HEADER = ENV.get('JUDGE0_AUTH_HEADER', 'X-Judge0-Token')
AUTH_TOKEN = ENV['JUDGE0_AUTH_TOKEN']
LANGUAGE_ID = int(ENV.get('JUDGE0_PYTHON_LANGUAGE_ID', '71'))


def encoded(value):
    return base64.b64encode((value or '').encode('utf-8')).decode('ascii')


def request_json(url, *, body=None, attempts=4):
    for attempt in range(attempts):
        request = urllib.request.Request(url, data=body, method='POST' if body is not None else 'GET')
        request.add_header(AUTH_HEADER, AUTH_TOKEN)
        if body is not None:
            request.add_header('Content-Type', 'application/json')
        try:
            with urllib.request.urlopen(request, timeout=12) as response:
                return json.loads(response.read().decode('utf-8'))
        except urllib.error.HTTPError as exc:
            if exc.code not in (429, 500, 502, 503, 504) or attempt + 1 == attempts:
                raise
        except (TimeoutError, urllib.error.URLError):
            if attempt + 1 == attempts:
                raise
        time.sleep(0.5 * (attempt + 1))


def validate_case(item):
    body = json.dumps({
        'source_code': encoded(item['sourceCode']),
        'language_id': LANGUAGE_ID,
        'stdin': encoded(item['inputText']),
        'expected_output': encoded(item['expectedOutput']),
        'cpu_time_limit': item['timeLimitSeconds'],
        'cpu_extra_time': 0.5,
        'wall_time_limit': item['timeLimitSeconds'] + 1,
        'memory_limit': item['memoryLimitKb'],
        'max_processes_and_or_threads': 8,
        'max_file_size': 1024,
        'max_output_size': 64,
        'enable_network': False,
    }, ensure_ascii=False).encode('utf-8')
    response = request_json(BASE + '/submissions?base64_encoded=true&wait=false', body=body)
    token = response.get('token')
    if not token:
        raise RuntimeError('Judge0 未返回提交令牌')
    result = None
    for _ in range(30):
        time.sleep(0.5)
        result = request_json(BASE + '/submissions/' + token + '?base64_encoded=true')
        status_id = (result.get('status') or {}).get('id')
        if status_id not in (1, 2):
            break
    status = result.get('status') or {} if result else {}
    return {
        'externalId': item['externalId'],
        'caseName': item['caseName'],
        'statusId': status.get('id'),
        'status': status.get('description'),
        'timeSeconds': result.get('time') if result else None,
        'memoryKb': result.get('memory') if result else None,
    }


def main():
    cases = json.loads(PAYLOAD_PATH.read_text(encoding='utf-8'))
    results = []
    failures = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=8) as pool:
        future_map = {pool.submit(validate_case, item): item for item in cases}
        for future in concurrent.futures.as_completed(future_map):
            item = future_map[future]
            try:
                result = future.result()
            except Exception as exc:
                result = {
                    'externalId': item['externalId'],
                    'caseName': item['caseName'],
                    'statusId': None,
                    'status': type(exc).__name__ + ': ' + str(exc)[:180],
                    'timeSeconds': None,
                    'memoryKb': None,
                }
            results.append(result)
            if result['statusId'] != 3:
                failures.append(result)
    results.sort(key=lambda row: (row['externalId'], row['caseName']))
    report = {
        'validatedAt': datetime.now(timezone.utc).isoformat(),
        'questionCount': len({item['externalId'] for item in cases}),
        'testCaseCount': len(cases),
        'acceptedCount': len(cases) - len(failures),
        'failedCount': len(failures),
        'allAccepted': not failures,
        'maxTimeSeconds': max((float(row['timeSeconds']) for row in results if row['timeSeconds'] is not None), default=None),
        'maxMemoryKb': max((int(row['memoryKb']) for row in results if row['memoryKb'] is not None), default=None),
        'failures': failures,
    }
    print(json.dumps(report, ensure_ascii=False))
    return 0 if not failures else 1


raise SystemExit(main())
'''


def build_cases() -> list[dict]:
    cases = []
    for batch in range(1, 6):
        path = ROOT / f"contexts/python-judge0/python-zero-basis-v3/batch-{batch:02d}.json"
        for question in json.loads(path.read_text(encoding="utf-8")):
            for case in question["testCases"]:
                cases.append(
                    {
                        "externalId": question["externalId"],
                        "caseName": case["caseName"],
                        "sourceCode": question["referenceCode"],
                        "inputText": case["inputText"],
                        "expectedOutput": case["expectedOutput"],
                        "timeLimitSeconds": question["timeLimitSeconds"],
                        "memoryLimitKb": question["memoryLimitKb"],
                    }
                )
    return cases


def main() -> int:
    payload = json.dumps(build_cases(), ensure_ascii=False).encode("utf-8")
    with Session() as session:
        code, host, _ = session.run("hostname -I", timeout=30)
        if code != 0 or "10.52.1.130" not in host.split():
            raise RuntimeError("目标主机不是 10.52.1.130，停止 Judge0 验证")
        with session.sftp.open(REMOTE_PAYLOAD, "wb") as handle:
            handle.write(payload)
        with session.sftp.open(REMOTE_RUNNER, "wb") as handle:
            handle.write(RUNNER.encode("utf-8"))
        session.sftp.chmod(REMOTE_RUNNER, 0o700)
        try:
            code, stdout, stderr = session.run(
                f"sudo -S -p '' python3 {REMOTE_RUNNER}",
                stdin=session.password + "\n",
                timeout=900,
            )
        finally:
            session.run(f"rm -f {REMOTE_PAYLOAD} {REMOTE_RUNNER}", timeout=30)
    lines = [line for line in stdout.splitlines() if line.strip()]
    if not lines:
        raise RuntimeError("Judge0 验证没有返回报告：" + stderr[:1200])
    report = json.loads(lines[-1])
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(
        f"Judge0 验证：{report['acceptedCount']}/{report['testCaseCount']} Accepted，"
        f"失败 {report['failedCount']}。"
    )
    return 0 if code == 0 and report["allAccepted"] else 1


if __name__ == "__main__":
    raise SystemExit(main())
