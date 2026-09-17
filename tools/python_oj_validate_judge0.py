#!/usr/bin/env python3
"""通过真实 Judge0 批量验证 Python V2 题包，不在参数或报告中保存认证令牌。"""

from __future__ import annotations

import argparse
import json
import os
import time
import urllib.error
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path
from typing import Any


FINISHED_STATUS_IDS = set(range(3, 15))


def normalize_output(value: Any) -> str:
    text = "" if value is None else str(value)
    lines = text.replace("\r\n", "\n").replace("\r", "\n").split("\n")
    lines = [line.rstrip(" \t") for line in lines]
    while lines and lines[-1] == "":
        lines.pop()
    return "\n".join(lines)


def request_json(url: str, token: str, method: str = "GET", body: dict[str, Any] | None = None) -> dict[str, Any]:
    data = None if body is None else json.dumps(body, ensure_ascii=False).encode("utf-8")
    request = urllib.request.Request(url, data=data, method=method)
    request.add_header("X-Judge0-Token", token)
    request.add_header("Content-Type", "application/json; charset=utf-8")
    with urllib.request.urlopen(request, timeout=15) as response:
        return json.loads(response.read().decode("utf-8"))


def validate_case(base_url: str, token: str, question: dict[str, Any], case: dict[str, Any], max_polls: int) -> dict[str, Any]:
    payload = {
        "source_code": question["referenceCode"],
        "language_id": 71,
        "stdin": case.get("inputText") or "",
        "expected_output": case.get("expectedOutput") or "",
        "cpu_time_limit": float(question.get("timeLimitSeconds") or 2),
        "cpu_extra_time": 0.5,
        "wall_time_limit": float(question.get("timeLimitSeconds") or 2) + 1,
        "memory_limit": int(question.get("memoryLimitKb") or 131072),
        "max_processes_and_or_threads": int(question.get("maxProcesses") or 8),
        "max_file_size": int(question.get("maxFileSizeKb") or 1024),
        "max_output_size": int(question.get("maxOutputKb") or 64),
        "enable_network": False,
    }
    submitted = request_json(f"{base_url}/submissions?base64_encoded=false&wait=false", token, "POST", payload)
    judge_token = submitted.get("token")
    if not judge_token:
        raise RuntimeError("Judge0 未返回提交令牌")
    result: dict[str, Any] = {}
    for _ in range(max_polls):
        time.sleep(0.25)
        result = request_json(f"{base_url}/submissions/{judge_token}?base64_encoded=false", token)
        status_id = int((result.get("status") or {}).get("id") or 0)
        if status_id in FINISHED_STATUS_IDS:
            break
    else:
        raise RuntimeError("Judge0 轮询超时")
    actual = normalize_output(result.get("stdout"))
    expected = normalize_output(case.get("expectedOutput"))
    status_id = int((result.get("status") or {}).get("id") or 0)
    passed = status_id == 3 and actual == expected
    return {
        "externalId": question.get("externalId"),
        "caseName": case.get("caseName"),
        "passed": passed,
        "statusId": status_id,
        "status": (result.get("status") or {}).get("description"),
        "time": result.get("time"),
        "memoryKb": result.get("memory"),
        "expected": None if passed else expected,
        "actual": None if passed else actual,
        "stderr": None if passed else (result.get("compile_output") or result.get("stderr") or result.get("message")),
    }


def main() -> int:
    parser = argparse.ArgumentParser(description="使用真实 Judge0 验证 Python V2 系统题包")
    parser.add_argument("packages", nargs="+", help="UTF-8 JSON 题包")
    parser.add_argument("--base-url", default=os.environ.get("JUDGE0_BASE_URL", ""))
    parser.add_argument("--report", required=True)
    parser.add_argument("--workers", type=int, default=8)
    parser.add_argument("--max-polls", type=int, default=48)
    args = parser.parse_args()
    token = os.environ.get("JUDGE0_AUTH_TOKEN", "").strip()
    base_url = args.base_url.rstrip("/")
    if not token or not base_url:
        parser.error("必须通过 JUDGE0_AUTH_TOKEN 和 --base-url/JUDGE0_BASE_URL 提供连接配置")

    questions: list[dict[str, Any]] = []
    for package in args.packages:
        questions.extend(json.loads(Path(package).read_text(encoding="utf-8")))
    jobs = [(question, case) for question in questions for case in question.get("testCases", [])]
    results: list[dict[str, Any]] = []
    with ThreadPoolExecutor(max_workers=max(1, min(args.workers, 16))) as executor:
        futures = [executor.submit(validate_case, base_url, token, question, case, args.max_polls) for question, case in jobs]
        for future in as_completed(futures):
            try:
                results.append(future.result())
            except (urllib.error.URLError, RuntimeError, ValueError) as error:
                results.append({"passed": False, "error": str(error)})

    failures = [item for item in results if not item.get("passed")]
    report = {
        "questionCount": len(questions),
        "caseCount": len(jobs),
        "passedCount": len(results) - len(failures),
        "failureCount": len(failures),
        "failures": failures,
    }
    Path(args.report).write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    if failures:
        print(f"真实 Judge0 校验失败：{len(failures)}/{len(jobs)} 个测试点未通过。")
        return 1
    print(f"真实 Judge0 校验通过：{len(questions)} 道题、{len(jobs)} 个测试点。")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
