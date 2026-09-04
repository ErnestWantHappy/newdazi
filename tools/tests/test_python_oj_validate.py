import unittest

from tools.python_oj_validate import has_broken_text, validate_reference_code


class PythonOjValidateTest(unittest.TestCase):
    def test_question_mark_inside_real_text_is_allowed(self):
        self.assertFalse(has_broken_text('a! b? c.', reject_ascii_question=True))
        self.assertTrue(has_broken_text('?', reject_ascii_question=True))

    def test_collection_remove_is_not_treated_as_file_deletion(self):
        errors = []
        validate_reference_code('items = {1}\nitems.remove(1)\nprint(items)', 'PYV2-999', errors)
        self.assertEqual([], errors)


if __name__ == '__main__':
    unittest.main()
