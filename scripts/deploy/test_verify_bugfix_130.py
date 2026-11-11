"""线上验收脚本离线测试：不读真实凭据、不连接网络。"""
import contextlib
import importlib.util
import io
import json
import tempfile
import unittest
from pathlib import Path
from unittest.mock import Mock, patch

spec = importlib.util.spec_from_file_location("verify_bugfix", Path(__file__).with_name("verify_bugfix_130.py"))
v = importlib.util.module_from_spec(spec)
spec.loader.exec_module(v)
PRIVATE = "private-marker-must-not-leak"


class FakeApi:
    def __init__(self, cases=None, health=None, published=True, owner=True):
        self.calls = []
        self.cases = cases if cases is not None else [{"statusCode": "ACCEPTED", "expectedOutput": PRIVATE}]
        self.health = health or (200, {"code": 401, "msg": PRIVATE})
        self.published = published
        self.owner = owner

    def call(self, method, path, data=None, token=None):
        self.calls.append((method, path, data))
        if path.endswith("/health"):
            return self.health
        if path == "/login":
            return 200, {"code": 200, "token": PRIVATE}
        if path == "/getInfo":
            return 200, {"code": 200, "roles": ["teacher", "researcher"], "user": PRIVATE}
        if path.startswith("/business/platform-update/list"):
            return 200, {"code": 200, "rows": [{"versionNo": "1.30.96", "status": "PUBLISHED" if self.published else "DRAFT", "content": PRIVATE}]}
        if path == "/business/programming/question/3337":
            return 200, {"code": 200 if self.owner else 403, "data": {"cpuTimeLimit": 2}}
        if path == "/business/question/3337":
            return 200, {"code": 200, "data": {"answer": 'a=input("Name:")\nprint(a)'}}
        if path.endswith("/generate-outputs"):
            return 200, {"code": 200, "data": {"cases": self.cases}, "msg": PRIVATE}
        raise AssertionError("unexpected API")


def run(api):
    return v.run_verification(api, credentials=lambda: (PRIVATE, PRIVATE))


class VerificationTests(unittest.TestCase):
    def test_import_does_not_load_credentials_or_network(self):
        with patch.object(v.urllib.request, "build_opener", side_effect=AssertionError("network initialized")):
            module = importlib.util.module_from_spec(spec)
            spec.loader.exec_module(module)

    def test_normal_existing_question_reports_missing_input_limit(self):
        api = FakeApi()
        report = run(api)
        self.assertEqual(report["result"], "PASS_WITH_LIMITATIONS")
        self.assertEqual(report["checks"]["insufficientInput"]["status"], "NOT_TESTED")
        self.assertNotIn(PRIVATE, json.dumps(report))
        posts = [(m, p, data) for m, p, data in api.calls if m == "POST"]
        self.assertEqual(len(posts), 2)
        self.assertEqual(posts[1], ("POST", "/business/programming/question/3337/generate-outputs", None))
        self.assertFalse(any(m in ("PUT", "DELETE", "PATCH") for m, _, _ in api.calls))

    def test_existing_insufficient_input_is_observed_without_override(self):
        api = FakeApi(cases=[{"statusCode": "RUNTIME_ERROR", "errorSummary": "参考代码还在等待输入：该测试点给的输入行数不够，请补足输入后再生成"}])
        report = run(api)
        self.assertEqual(report["result"], "PASS")
        self.assertEqual(report["checks"]["insufficientInput"]["reason"], "OBSERVED_IN_EXISTING_STORED_CASE")

    def test_nul_rejected_without_saving_raw_text(self):
        report = run(FakeApi(cases=[{"statusCode": "ACCEPTED", "expectedOutput": PRIVATE + "\0"}]))
        self.assertEqual(report["result"], "FAIL")
        self.assertFalse(report["checks"]["generateOutputs"]["noNul"])
        self.assertNotIn(PRIVATE, json.dumps(report))

    def test_denial_requires_auth_code_and_no_data(self):
        for status, body in ((500, {"code": 500}), (200, {"code": 200, "data": {"host": PRIVATE}}), (403, {"code": 403, "data": {"token": PRIVATE}}), (302, {})):
            self.assertEqual(v.denied_result(status, body)["status"], "FAIL")
        self.assertEqual(v.denied_result(403, {})["status"], "PASS")
        self.assertEqual(v.denied_result(200, {"code": 401})["status"], "PASS")

    def test_draft_update_and_no_permission_do_not_pass(self):
        self.assertEqual(run(FakeApi(published=False))["result"], "FAIL")
        api = FakeApi(owner=False)
        report = run(api)
        self.assertEqual(report["checks"]["generateOutputs"]["status"], "BLOCKED")
        self.assertFalse(any(path.endswith("/generate-outputs") for _, path, _ in api.calls))

    def test_request_whitelist_prevents_writes_and_overrides(self):
        api = v.Api()
        with patch.object(api.opener, "open", side_effect=AssertionError("must not connect")):
            for method, path, data in (("PUT", "/business/programming/question/3337", {}), ("POST", "/business/question", {}), ("POST", "/business/programming/question/3337/validate", None), ("POST", "/business/programming/question/3337/generate-outputs", {"stdin": ""})):
                with self.assertRaises(v.VerificationError):
                    api.call(method, path, data)

    def test_main_sanitizes_errors_and_writes_only_safe_report(self):
        with tempfile.TemporaryDirectory() as temporary:
            path = Path(temporary) / "online-api-verification.json"
            captured = io.StringIO()
            with patch.object(v, "REPORT", path), patch.object(v, "run_verification", side_effect=RuntimeError(PRIVATE)), patch.object(v, "Api", return_value=Mock()), contextlib.redirect_stdout(captured):
                self.assertEqual(v.main([]), 1)
            self.assertNotIn(PRIVATE, captured.getvalue() + path.read_text(encoding="utf-8"))
            self.assertEqual(json.loads(path.read_text())["result"], "FAIL")

    def test_reuse_existing_auth_entry_without_reading_secrets(self):
        source = v.AUTH_ENTRY.read_text(encoding="utf-8")
        self.assertIn("def creds():", source)
        self.assertIn("contexts/secrets.local.md", source)
        with patch.object(v.importlib.util, "module_from_spec") as factory:
            module = Mock()
            module.creds.return_value = (PRIVATE, PRIVATE)
            factory.return_value = module
            with patch.object(v.importlib.util, "spec_from_file_location") as loader:
                self.assertEqual(v.teacher_credentials(), (PRIVATE, PRIVATE))
                loader.return_value.loader.exec_module.assert_called_once_with(module)


if __name__ == "__main__":
    unittest.main(verbosity=2)
