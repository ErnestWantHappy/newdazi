import importlib.util
import sys
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]


def load_tool(name: str):
    path = ROOT / "tools" / f"{name}.py"
    spec = importlib.util.spec_from_file_location(name, path)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    sys.modules[name] = module
    spec.loader.exec_module(module)
    return module


builder = load_tool("python_oj_build_import_sql")
validator = load_tool("python_oj_validate")


def sample_question():
    weights = [17, 17, 17, 17, 17, 15]
    return {
        "externalId": "PYV2-001",
        "title": "固定输出练习",
        "knowledgePoints": ["标准输出"],
        "difficulty": "SIMPLE",
        "description": "请编写程序，按照题目要求输出指定的一行文字。",
        "inputDescription": "本题没有输入。",
        "outputDescription": "输出指定的一行文字。",
        "constraints": "输出内容必须与题面完全一致。",
        "sampleExplanation": "直接输出即可。",
        "noInput": True,
        "starterCode": "",
        "referenceCode": "print('hello')",
        "timeLimitSeconds": 2.0,
        "memoryLimitKb": 131072,
        "isPublic": True,
        "testCases": [
            {
                "caseName": f"测试点 {index}",
                "inputText": "",
                "expectedOutput": "hello",
                "isPublic": index <= 2,
                "scoreWeight": weights[index - 1],
                "orderNum": index,
            }
            for index in range(1, 7)
        ],
    }


class PythonOjToolTest(unittest.TestCase):
    def test_utf8_sql_uses_hex_for_chinese(self):
        encoded = builder.utf8_sql("中文题面")
        self.assertNotIn("中文题面", encoded)
        self.assertIn("CONVERT(0xE4B8ADE69687E9A298E99DA2 USING utf8mb4)", encoded)

    def test_generated_sql_has_transaction_conflict_guard_and_post_checks(self):
        question = sample_question()
        sql = builder.build_sql([question], [Path(__file__)])
        self.assertIn("START TRANSACTION;", sql)
        self.assertIn("ROLLBACK;", sql)
        self.assertIn("COMMIT;", sql)
        self.assertIn("external_id", sql)
        self.assertIn("V2 外部题号已被非系统题占用", sql)
        self.assertIn("V2 公开/隐藏测试点或权重后检失败", sql)
        self.assertIn(builder.utf8_sql(question["title"]), sql)

    def test_validator_rejects_empty_expected_output(self):
        question = sample_question()
        question["testCases"][0]["expectedOutput"] = ""
        errors = []
        validator.validate_shape(question, errors)
        self.assertTrue(any("expectedOutput 不能为空" in error for error in errors))


if __name__ == "__main__":
    unittest.main()
