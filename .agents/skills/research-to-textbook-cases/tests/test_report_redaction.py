"""报告输出不得含完整私密链接、提取码或凭据值。"""
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts"))
from common import safe_report, redact_text


class ReportRedactionTest(unittest.TestCase):
    def test_redact_cloud_link_and_code(self):
        text = '{"linkUrl":"https://yun.xsedu.net.cn/link/abcdef","extract_code":"a1b2","password":"pw"}'
        result = redact_text(text)
        self.assertNotIn("/link/abcdef", result)
        self.assertNotIn("a1b2", result)
        self.assertNotIn('"pw"', result)

    def test_safe_report_drops_sensitive_keys(self):
        result = safe_report({"linkUrl": "https://yun.xsedu.net.cn/link/x", "extractCode": "abcd", "title": "课件"})
        self.assertEqual(result, {"title": "课件"})

    def test_redact_secret_values(self):
        result = redact_text("YUELING_PASSWORD=plaintext\n")
        self.assertNotIn("plaintext", result)


if __name__ == "__main__":
    unittest.main()
