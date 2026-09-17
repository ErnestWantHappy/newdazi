#!/usr/bin/env python3
"""校验 Python 系统题 V2 JSON，并真实运行每题参考代码的全部测试点。"""

from __future__ import annotations

import argparse
import ast
import difflib
import json
import os
import re
import subprocess
import sys
import tempfile
from pathlib import Path
from typing import Any


if hasattr(sys.stdout, "reconfigure"):
    # Windows 终端编码并不稳定，固定 UTF-8，避免校验报告本身出现中文乱码。
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")


QUESTION_FIELDS = {
    "externalId", "title", "knowledgePoints", "difficulty", "description",
    "inputDescription", "outputDescription", "constraints", "sampleExplanation",
    "noInput", "starterCode", "referenceCode", "timeLimitSeconds",
    "memoryLimitKb", "isPublic", "testCases",
}
CASE_FIELDS = {
    "caseName", "inputText", "expectedOutput", "isPublic", "scoreWeight", "orderNum",
}
BROKEN_MARKERS = ("\ufffd", "锟斤拷", "ï¿½", "??")
BLOCKED_IMPORT_ROOTS = {
    "asyncio", "ctypes", "ftplib", "http", "multiprocessing", "os", "pathlib",
    "shutil", "socket", "subprocess", "telnetlib", "threading", "urllib",
}
BLOCKED_CALLS = {"compile", "eval", "exec", "globals", "input", "locals", "open", "__import__"}
BLOCKED_ATTRIBUTES = {
    "call", "connect", "create_connection", "fork", "kill", "open", "popen",
    "rename", "rmdir", "run", "socket", "spawn", "startfile", "system", "unlink",
    "urlopen",
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="校验 Python 系统题 V2 JSON 题包")
    parser.add_argument("packages", nargs="+", type=Path, help="需要一起校验的 UTF-8 JSON 题包")
    parser.add_argument("--blueprint", type=Path, help="按题号核对标题、知识点和难度的 V2 蓝图 JSON")
    parser.add_argument("--require-complete-blueprint", action="store_true", help="要求本次题包覆盖蓝图中的全部题号")
    parser.add_argument("--report", type=Path, help="将完整 JSON 报告写入指定路径")
    parser.add_argument("--timeout", type=float, default=5.0, help="单个测试点最长运行秒数，默认 5 秒")
    parser.add_argument("--max-output-kb", type=int, default=128, help="单个测试点标准输出上限，默认 128KB")
    return parser.parse_args()


def issue(errors: list[str], external_id: str, message: str) -> None:
    errors.append(f"{external_id or '未知题号'}：{message}")


def is_number(value: Any) -> bool:
    return isinstance(value, (int, float)) and not isinstance(value, bool)


def has_broken_text(value: Any, reject_ascii_question: bool = False) -> bool:
    if not isinstance(value, str):
        return False
    # 问号可能是字符串题的合法数据；只有整段退化为问号时才按编码损坏处理。
    return any(marker in value for marker in BROKEN_MARKERS) or (
        reject_ascii_question and re.fullmatch(r"\?+", value.strip()) is not None
    )


def normalized_output(value: str | None) -> str:
    text = (value or "").replace("\r\n", "\n").replace("\r", "\n")
    lines = [line.rstrip(" \t") for line in text.split("\n")]
    while lines and lines[-1] == "":
        lines.pop()
    return "\n".join(lines)


def similarity_text(value: str) -> str:
    return re.sub(r"[\W\d_]+", "", value.lower(), flags=re.UNICODE)


def validate_reference_code(code: Any, external_id: str, errors: list[str]) -> None:
    if not isinstance(code, str) or not (3 <= len(code) <= 20000):
        issue(errors, external_id, "referenceCode 长度必须为 3～20000 个字符")
        return
    try:
        tree = ast.parse(code, filename=external_id or "reference.py")
    except SyntaxError as exc:
        issue(errors, external_id, f"参考代码语法错误：第 {exc.lineno or '?'} 行 {exc.msg}")
        return
    for node in ast.walk(tree):
        if isinstance(node, (ast.Import, ast.ImportFrom)):
            names = [item.name for item in node.names] if isinstance(node, ast.Import) else [node.module or ""]
            for name in names:
                if name.split(".")[0] in BLOCKED_IMPORT_ROOTS:
                    issue(errors, external_id, f"参考代码禁止导入高风险模块 {name}")
        elif isinstance(node, ast.Call):
            if isinstance(node.func, ast.Name) and node.func.id in BLOCKED_CALLS - {"input"}:
                issue(errors, external_id, f"参考代码禁止调用 {node.func.id}()")
            if isinstance(node.func, ast.Attribute) and node.func.attr in BLOCKED_ATTRIBUTES:
                issue(errors, external_id, f"参考代码禁止调用 .{node.func.attr}()")


def validate_shape(question: Any, errors: list[str]) -> None:
    if not isinstance(question, dict):
        issue(errors, "未知题号", "题目必须是 JSON 对象")
        return
    external_id = question.get("externalId") if isinstance(question.get("externalId"), str) else "未知题号"
    missing = sorted(QUESTION_FIELDS - set(question))
    extra = sorted(set(question) - QUESTION_FIELDS)
    if missing:
        issue(errors, external_id, "缺少字段：" + "、".join(missing))
    if extra:
        issue(errors, external_id, "存在未定义字段：" + "、".join(extra))
    if not re.fullmatch(r"PYV2-\d{3}", str(question.get("externalId", ""))):
        issue(errors, external_id, "externalId 必须符合 PYV2-001 格式")
    for field, minimum, maximum in (
        ("title", 2, 30), ("description", 20, 10000), ("inputDescription", 4, 5000),
        ("outputDescription", 4, 5000), ("constraints", 4, 5000),
    ):
        value = question.get(field)
        if not isinstance(value, str) or not (minimum <= len(value.strip()) <= maximum):
            issue(errors, external_id, f"{field} 长度必须为 {minimum}～{maximum} 个字符")
    if question.get("difficulty") not in {"SIMPLE", "MEDIUM", "HARD"}:
        issue(errors, external_id, "difficulty 只能是 SIMPLE、MEDIUM 或 HARD")
    points = question.get("knowledgePoints")
    if not isinstance(points, list) or not (1 <= len(points) <= 3) or any(not isinstance(x, str) or not (1 <= len(x.strip()) <= 30) for x in points):
        issue(errors, external_id, "knowledgePoints 必须包含 1～3 个长度为 1～30 的字符串")
    elif len(set(points)) != len(points):
        issue(errors, external_id, "knowledgePoints 不能重复")
    if not isinstance(question.get("noInput"), bool):
        issue(errors, external_id, "noInput 必须是布尔值")
    if question.get("isPublic") is not True:
        issue(errors, external_id, "系统题 isPublic 必须为 true")
    if not is_number(question.get("timeLimitSeconds")) or not (0.1 <= question["timeLimitSeconds"] <= 10):
        issue(errors, external_id, "timeLimitSeconds 必须在 0.1～10 之间")
    memory = question.get("memoryLimitKb")
    if not isinstance(memory, int) or isinstance(memory, bool) or not (16384 <= memory <= 524288):
        issue(errors, external_id, "memoryLimitKb 必须是 16384～524288 的整数")
    if not isinstance(question.get("starterCode"), str) or len(question.get("starterCode", "")) > 2000:
        issue(errors, external_id, "starterCode 必须是不超过 2000 个字符的字符串")
    for field in ("title", "description", "inputDescription", "outputDescription", "constraints", "sampleExplanation"):
        if has_broken_text(question.get(field)):
            issue(errors, external_id, f"{field} 检测到疑似乱码或占位符")
    validate_reference_code(question.get("referenceCode"), external_id, errors)

    cases = question.get("testCases")
    if not isinstance(cases, list) or not (6 <= len(cases) <= 20):
        issue(errors, external_id, "testCases 必须包含 6～20 个测试点")
        return
    public_count = 0
    orders: list[int] = []
    total_weight = 0
    for index, case in enumerate(cases, start=1):
        if not isinstance(case, dict):
            issue(errors, external_id, f"第 {index} 个测试点必须是 JSON 对象")
            continue
        missing_case = sorted(CASE_FIELDS - set(case))
        extra_case = sorted(set(case) - CASE_FIELDS)
        if missing_case:
            issue(errors, external_id, f"第 {index} 个测试点缺少字段：{'、'.join(missing_case)}")
        if extra_case:
            issue(errors, external_id, f"第 {index} 个测试点存在未定义字段：{'、'.join(extra_case)}")
        if not isinstance(case.get("caseName"), str) or not (2 <= len(case.get("caseName", "").strip()) <= 50):
            issue(errors, external_id, f"第 {index} 个测试点 caseName 长度必须为 2～50")
        for field in ("inputText", "expectedOutput"):
            value = case.get(field)
            if not isinstance(value, str) or len(value) > 50000:
                issue(errors, external_id, f"第 {index} 个测试点 {field} 必须是不超过 50000 个字符的字符串")
            elif has_broken_text(value, reject_ascii_question=True):
                issue(errors, external_id, f"第 {index} 个测试点 {field} 检测到问号占位或乱码")
        if case.get("expectedOutput") == "":
            issue(errors, external_id, f"第 {index} 个测试点 expectedOutput 不能为空")
        if question.get("noInput") is True and case.get("inputText") != "":
            issue(errors, external_id, f"无输入题第 {index} 个测试点 inputText 必须为空")
        if not isinstance(case.get("isPublic"), bool):
            issue(errors, external_id, f"第 {index} 个测试点 isPublic 必须是布尔值")
        elif case["isPublic"]:
            public_count += 1
        weight = case.get("scoreWeight")
        if not isinstance(weight, int) or isinstance(weight, bool) or not (1 <= weight <= 100):
            issue(errors, external_id, f"第 {index} 个测试点 scoreWeight 必须是 1～100 的整数")
        else:
            total_weight += weight
        order = case.get("orderNum")
        if not isinstance(order, int) or isinstance(order, bool) or not (1 <= order <= 20):
            issue(errors, external_id, f"第 {index} 个测试点 orderNum 必须是 1～20 的整数")
        else:
            orders.append(order)
    if public_count != 2:
        issue(errors, external_id, f"必须恰好有 2 个公开样例，当前为 {public_count} 个")
    if len(cases) - public_count < 4:
        issue(errors, external_id, "至少需要 4 个隐藏测试点")
    if total_weight != 100:
        issue(errors, external_id, f"测试点权重合计必须为 100，当前为 {total_weight}")
    if sorted(orders) != list(range(1, len(cases) + 1)):
        issue(errors, external_id, "orderNum 必须从 1 连续递增且不重复")


def execute_case(question: dict[str, Any], case: dict[str, Any], timeout: float, max_output_bytes: int) -> dict[str, Any]:
    external_id = question["externalId"]
    env = dict(os.environ)
    env["PYTHONIOENCODING"] = "utf-8"
    flags = subprocess.CREATE_NO_WINDOW if os.name == "nt" else 0
    with tempfile.TemporaryDirectory(prefix="python-oj-") as workdir, tempfile.TemporaryFile() as stdout_file, tempfile.TemporaryFile() as stderr_file:
        # Windows 通过 `python -c` 传入中文源码会受活动代码页影响；写入 UTF-8 文件更接近 Judge0 的真实执行方式。
        script_path = Path(workdir) / "main.py"
        script_path.write_text("# -*- coding: utf-8 -*-\n" + question["referenceCode"], encoding="utf-8")
        process = subprocess.Popen(
            # -I 会忽略 PYTHONIOENCODING；显式启用 UTF-8 模式，确保 Windows 重定向输出不回退到 GBK。
            [sys.executable, "-I", "-S", "-X", "utf8", str(script_path)],
            cwd=workdir,
            env=env,
            stdin=subprocess.PIPE,
            stdout=stdout_file,
            stderr=stderr_file,
            creationflags=flags,
        )
        try:
            process.communicate(case["inputText"].encode("utf-8"), timeout=max(0.1, timeout))
        except subprocess.TimeoutExpired:
            process.kill()
            process.wait()
            return {"passed": False, "message": "运行超时"}
        stdout_size = stdout_file.tell()
        stderr_size = stderr_file.tell()
        stdout_file.seek(0)
        stderr_file.seek(0)
        actual = stdout_file.read(min(stdout_size, max_output_bytes + 1)).decode("utf-8", errors="replace")
        stderr = stderr_file.read(min(stderr_size, 8192)).decode("utf-8", errors="replace")
    if stdout_size > max_output_bytes:
        return {"passed": False, "message": f"输出超过 {max_output_bytes // 1024}KB 上限"}
    if process.returncode != 0:
        detail = stderr.strip().splitlines()[-1] if stderr.strip() else f"退出码 {process.returncode}"
        return {"passed": False, "message": "运行错误：" + detail[:300]}
    expected = case["expectedOutput"]
    if normalized_output(expected) != normalized_output(actual):
        return {
            "passed": False,
            "message": "输出不一致",
            "expected": expected[:500],
            "actual": actual[:500],
        }
    return {"passed": True}


def load_packages(paths: list[Path], errors: list[str]) -> list[dict[str, Any]]:
    questions: list[dict[str, Any]] = []
    for path in paths:
        try:
            raw = path.read_text(encoding="utf-8-sig")
            data = json.loads(raw)
        except (OSError, UnicodeError, json.JSONDecodeError) as exc:
            errors.append(f"{path}：无法按 UTF-8 JSON 读取：{exc}")
            continue
        if not isinstance(data, list) or not (1 <= len(data) <= 20):
            errors.append(f"{path}：根节点必须是包含 1～20 道题的数组")
            continue
        questions.extend(data)
    return questions


def load_blueprint(path: Path | None, errors: list[str]) -> dict[str, dict[str, Any]]:
    if path is None:
        return {}
    try:
        data = json.loads(path.read_text(encoding="utf-8-sig"))
    except (OSError, UnicodeError, json.JSONDecodeError) as exc:
        errors.append(f"{path}：无法读取蓝图：{exc}")
        return {}
    if not isinstance(data, list):
        errors.append(f"{path}：蓝图根节点必须是数组")
        return {}
    result: dict[str, dict[str, Any]] = {}
    for index, row in enumerate(data, start=1):
        if not isinstance(row, dict) or not isinstance(row.get("externalId"), str):
            errors.append(f"{path}：蓝图第 {index} 项缺少 externalId")
            continue
        if row["externalId"] in result:
            errors.append(f"{path}：蓝图题号重复 {row['externalId']}")
        result[row["externalId"]] = row
    return result


def main() -> int:
    args = parse_args()
    errors: list[str] = []
    blueprint = load_blueprint(args.blueprint, errors)
    questions = load_packages(args.packages, errors)
    for question in questions:
        validate_shape(question, errors)

    ids: dict[str, int] = {}
    titles: dict[str, int] = {}
    for index, question in enumerate(questions):
        if not isinstance(question, dict):
            continue
        external_id = str(question.get("externalId", ""))
        title = str(question.get("title", "")).strip()
        if external_id in ids:
            issue(errors, external_id, f"与第 {ids[external_id] + 1} 道题的编号重复")
        ids[external_id] = index
        if title in titles:
            issue(errors, external_id, f"与第 {titles[title] + 1} 道题标题重复")
        titles[title] = index
        if blueprint:
            expected = blueprint.get(external_id)
            if expected is None:
                issue(errors, external_id, "题号不在指定 V2 蓝图中")
            else:
                if title != expected.get("title"):
                    issue(errors, external_id, f"标题必须与蓝图一致：{expected.get('title')}")
                if question.get("difficulty") != expected.get("difficulty"):
                    issue(errors, external_id, f"难度必须与蓝图一致：{expected.get('difficulty')}")
                if question.get("knowledgePoints") != expected.get("knowledgePoints"):
                    issue(errors, external_id, "knowledgePoints 必须与蓝图顺序和内容完全一致")

    if blueprint and args.require_complete_blueprint:
        missing_ids = sorted(set(blueprint) - set(ids))
        if missing_ids:
            errors.append("题包未覆盖蓝图题号：" + "、".join(missing_ids))

    for left in range(len(questions)):
        if not isinstance(questions[left], dict):
            continue
        for right in range(left + 1, len(questions)):
            if not isinstance(questions[right], dict):
                continue
            left_text = similarity_text(str(questions[left].get("title", "")) + str(questions[left].get("description", "")))
            right_text = similarity_text(str(questions[right].get("title", "")) + str(questions[right].get("description", "")))
            if min(len(left_text), len(right_text)) >= 20 and difflib.SequenceMatcher(None, left_text, right_text).ratio() >= 0.88:
                issue(errors, str(questions[right].get("externalId", "")), f"与 {questions[left].get('externalId')} 题面高度相似，需人工去重")

    execution: list[dict[str, Any]] = []
    if not errors:
        for question in questions:
            question_result = {"externalId": question["externalId"], "title": question["title"], "passed": 0, "total": len(question["testCases"]), "failures": []}
            for case in sorted(question["testCases"], key=lambda item: item["orderNum"]):
                timeout = min(float(args.timeout), max(0.1, float(question["timeLimitSeconds"]) * 2))
                result = execute_case(question, case, timeout, max(1024, args.max_output_kb * 1024))
                if result["passed"]:
                    question_result["passed"] += 1
                else:
                    result["caseName"] = case["caseName"]
                    question_result["failures"].append(result)
                    issue(errors, question["externalId"], f"{case['caseName']}：{result['message']}")
            execution.append(question_result)

    report = {
        "valid": not errors,
        "packageCount": len(args.packages),
        "blueprint": str(args.blueprint) if args.blueprint else None,
        "blueprintMatchedCount": sum(1 for external_id in ids if external_id in blueprint),
        "questionCount": len(questions),
        "testCaseCount": sum(len(q.get("testCases", [])) for q in questions if isinstance(q, dict)),
        "errors": errors,
        "execution": execution,
    }
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    if errors:
        print(f"校验失败：{len(questions)} 道题，共发现 {len(errors)} 个问题。")
        for error in errors:
            print("- " + error)
        return 1
    print(f"校验通过：{len(questions)} 道题、{report['testCaseCount']} 个测试点，参考代码全部运行一致。")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
