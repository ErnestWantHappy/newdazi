import importlib.util
import pathlib
import unittest


MODULE_PATH = pathlib.Path(__file__).parents[1] / "python_oj_validate_judge0.py"
SPEC = importlib.util.spec_from_file_location("python_oj_validate_judge0", MODULE_PATH)
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


class Judge0OutputNormalizationTest(unittest.TestCase):
    def test_normalizes_newlines_trailing_spaces_and_final_blank_lines(self):
        self.assertEqual("a\nb", MODULE.normalize_output("a  \r\nb\t\r\n\r\n"))

    def test_keeps_leading_spaces_and_inner_blank_lines(self):
        self.assertEqual("  *\n\n***", MODULE.normalize_output("  *\n\n***\n"))


if __name__ == "__main__":
    unittest.main()
