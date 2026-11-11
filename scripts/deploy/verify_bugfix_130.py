"""130 只读 API 验收。仅登录、GET 和无落库 generate-outputs，不保存原始响应。

复用 1.30.94 已验证的教师凭据读取入口 creds()；不复用其会打印响应的 main。
当前 generate-outputs 无请求 DTO，只接受 questionId，不能覆写 stdin/测试点。
"""
from __future__ import annotations

import argparse
import importlib.util
import json
import re
import urllib.error
import urllib.request
from datetime import datetime
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
BASE = "http://10.52.1.130/prod-api"
AUTH_ENTRY = ROOT / "output/release-20261111-python-interactive-130/api_check_130.py"
REPORT = ROOT / "output/bugfix-1.30.96/online-api-verification.json"
VERSION = "1.30.96"


class VerificationError(RuntimeError):
    pass


def teacher_credentials():
    # 现有入口只在调用 creds 时读 secrets，导入不会登录或打印凭据。
    if not AUTH_ENTRY.is_file():
        raise VerificationError("缺少已核验的教师认证入口")
    spec = importlib.util.spec_from_file_location("verified_130_auth", AUTH_ENTRY)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module.creds()


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


class Api:
    def __init__(self):
        self.opener = urllib.request.build_opener(urllib.request.ProxyHandler({}), NoRedirect())

    def call(self, method, path, data=None, token=None):
        allowed = (
            (method == "POST" and path == "/login")
            or (method == "GET" and path in ("/getInfo", "/business/collaboration/health", "/business/platform-update/list?keyword=1.30.96&pageNum=1&pageSize=100"))
            or (method == "GET" and re.fullmatch(r"/business/(?:programming/question|question)/[1-9][0-9]*", path))
            or (method == "POST" and re.fullmatch(r"/business/programming/question/[1-9][0-9]*/generate-outputs", path) and data is None)
        )
        if not allowed:
            raise VerificationError("验收请求不在只读白名单内")
        body = json.dumps(data).encode("utf-8") if data is not None else None
        request = urllib.request.Request(BASE + path, body, method=method)
        request.add_header("Content-Type", "application/json;charset=utf-8")
        if token:
            request.add_header("Authorization", "Bearer " + token)
        try:
            response = self.opener.open(request, timeout=120)
        except urllib.error.HTTPError as error:
            response = error
        except Exception:
            raise VerificationError("请求失败，原始网络异常已隐藏") from None
        with response:
            status = response.code
            raw = response.read(4 * 1024 * 1024 + 1)
        try:
            if len(raw) > 4 * 1024 * 1024:
                raise ValueError()
            payload = json.loads(raw.decode("utf-8"))
            if not isinstance(payload, dict):
                raise ValueError()
        except (ValueError, UnicodeError):
            payload = {}
        return status, payload


def business_code(body):
    value = body.get("code")
    return value if type(value) is int else None


def response_fact(status, body):
    return {"httpStatus": status, "businessCode": business_code(body)}


def contains_nul(value):
    if isinstance(value, str):
        return "\x00" in value
    if isinstance(value, dict):
        return any(contains_nul(k) or contains_nul(v) for k, v in value.items())
    if isinstance(value, list):
        return any(contains_nul(v) for v in value)
    return False


def login(api, credentials):
    user, password = credentials()
    status, body = api.call("POST", "/login", {"username": user, "password": password})
    nested = body.get("data") if isinstance(body.get("data"), dict) else {}
    token = body.get("token") or nested.get("token")
    if status != 200 or business_code(body) != 200 or not isinstance(token, str) or not token:
        raise VerificationError("教师登录未成功，原始认证响应已隐藏")
    return token


def denied_result(status, body):
    # 500/空响应不能视作鉴权通过，必须得到明确的401/403且无业务数据。
    denied = status in (401, 403) or business_code(body) in (401, 403)
    safe = body.get("data") in (None, {}, [])
    return {"status": "PASS" if denied and safe else "FAIL", **response_fact(status, body), "hasBusinessData": not safe}


def insufficient_case(item):
    return (isinstance(item, dict) and item.get("statusCode") == "RUNTIME_ERROR"
            and isinstance(item.get("errorSummary"), str)
            and "等待输入" in item["errorSummary"] and "输入行数不够" in item["errorSummary"]
            and "\x00" not in item["errorSummary"])


def run_verification(api, question_id=3337, credentials=teacher_credentials):
    checks = {}
    status, body = api.call("GET", "/business/collaboration/health")
    checks["anonymousHealth"] = denied_result(status, body)
    token = login(api, credentials)
    status, identity = api.call("GET", "/getInfo", token=token)
    if status != 200 or business_code(identity) != 200 or "teacher" not in identity.get("roles", []):
        raise VerificationError("认证身份未确认为真实教师；不使用管理员替代")
    checks["teacherIdentity"] = {"status": "PASS"}
    status, updates = api.call("GET", "/business/platform-update/list?keyword=1.30.96&pageNum=1&pageSize=100", token=token)
    rows = updates.get("rows") if isinstance(updates.get("rows"), list) else []
    matches = [r for r in rows if isinstance(r, dict) and r.get("versionNo") == VERSION]
    checks["publishedUpdate"] = {"status": "PASS" if status == 200 and business_code(updates) == 200 and len(matches) == 1 and matches[0].get("status") == "PUBLISHED" else "FAIL", **response_fact(status, updates), "version": VERSION, "matchingRows": len(matches), "published": len(matches) == 1 and matches[0].get("status") == "PUBLISHED"}
    # 编辑配置 GET 会执行创建者/管理员校验；仅query/preview权限不足以证明可生成。
    status, config = api.call("GET", f"/business/programming/question/{question_id}", token=token)
    if status != 200 or business_code(config) != 200 or not isinstance(config.get("data"), dict):
        checks["generateOutputs"] = {"status": "BLOCKED", "reason": "QUESTION_CONFIG_ACCESS_UNAVAILABLE", **response_fact(status, config)}
    else:
        status, question = api.call("GET", f"/business/question/{question_id}", token=token)
        detail = question.get("data") if isinstance(question.get("data"), dict) else {}
        source = detail.get("answer")
        if status != 200 or business_code(question) != 200 or not isinstance(source, str) or not re.search(r"\binput\s*\(", source):
            checks["generateOutputs"] = {"status": "BLOCKED", "reason": "NO_ACCESSIBLE_INPUT_REFERENCE", **response_fact(status, question)}
        else:
            # 不发送stdin：真实控制器根本没有@RequestBody，发送也不会生效。
            status, result = api.call("POST", f"/business/programming/question/{question_id}/generate-outputs", token=token)
            data = result.get("data") if isinstance(result.get("data"), dict) else {}
            cases = data.get("cases") if isinstance(data.get("cases"), list) else []
            valid_cases = bool(cases) and all(isinstance(c, dict) and (c.get("statusCode") == "ACCEPTED" or insufficient_case(c)) for c in cases)
            no_nul = not contains_nul(result)
            checks["generateOutputs"] = {"status": "PASS" if status == 200 and business_code(result) == 200 and valid_cases and no_nul else "FAIL", **response_fact(status, result), "caseCount": len(cases), "noNul": no_nul, "requestOverrides": False}
            observed = any(isinstance(c, dict) and c.get("statusCode") == "RUNTIME_ERROR" and isinstance(c.get("errorSummary"), str) and "等待输入" in c["errorSummary"] and "输入行数不够" in c["errorSummary"] and "\x00" not in c["errorSummary"] for c in cases)
            if observed and checks["generateOutputs"]["status"] == "PASS":
                checks["insufficientInput"] = {"status": "PASS", "reason": "OBSERVED_IN_EXISTING_STORED_CASE", "runtimeError": True, "chineseReason": True, "noNul": True}
    checks.setdefault("insufficientInput", {"status": "NOT_TESTED", "reason": "ENDPOINT_ACCEPTS_ONLY_QUESTION_ID_NO_STDIN_OVERRIDE", "detail": "接口不接受stdin或测试点覆写；未制造不足输入，不能宣称该专项线上通过"})
    checks["studentHealth"] = {"status": "SKIPPED", "reason": "NO_VERIFIED_PRODUCTION_STUDENT_CREDENTIAL_ENTRY"}
    required = ("anonymousHealth", "teacherIdentity", "publishedUpdate", "generateOutputs")
    passed = all(checks[k]["status"] == "PASS" for k in required)
    return {"version": VERSION, "checkedAt": datetime.now().isoformat(timespec="seconds"), "result": "PASS_WITH_LIMITATIONS" if passed and checks["insufficientInput"]["status"] != "PASS" else ("PASS" if passed else "FAIL"), "businessDataWrites": False, "checks": checks}


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--question-id", type=int, default=3337, help="教师拥有且参考代码含input的已有题目；不修改该题")
    args = parser.parse_args(argv)
    if args.question_id <= 0:
        parser.error("question-id必须为正整数")
    try:
        report = run_verification(Api(), args.question_id)
    except Exception:
        # 认证或响应解析异常也不保存异常原文，避免Cookie/Token/账号泄漏。
        report = {"version": VERSION, "result": "FAIL", "reason": "VERIFICATION_ABORTED_RAW_ERROR_HIDDEN", "businessDataWrites": False}
    REPORT.parent.mkdir(parents=True, exist_ok=True)
    REPORT.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("验收结果：" + report["result"])
    print("非敏感报告：" + str(REPORT))
    return 1 if report["result"] == "FAIL" else 0


if __name__ == "__main__":
    raise SystemExit(main())
