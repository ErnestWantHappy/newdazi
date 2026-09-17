import json
import unittest

from tools.python_oj_repair_ai_json import repair_reference_code_fields


class PythonOjRepairAiJsonTest(unittest.TestCase):
    def test_repairs_bare_quotes_without_changing_newlines(self):
        raw = '[{"referenceCode":"print("YES")\\nprint(f"{1 + 1}")","timeLimitSeconds":2.0}]'
        repaired, count = repair_reference_code_fields(raw)
        data = json.loads(repaired)
        self.assertEqual(1, count)
        self.assertEqual('print("YES")\nprint(f"{1 + 1}")', data[0]['referenceCode'])

    def test_keeps_already_escaped_quotes_valid(self):
        raw = '[{"referenceCode":"print(\\"YES\\")","timeLimitSeconds":2.0}]'
        repaired, count = repair_reference_code_fields(raw)
        data = json.loads(repaired)
        self.assertEqual(1, count)
        self.assertEqual('print("YES")', data[0]['referenceCode'])


if __name__ == '__main__':
    unittest.main()
