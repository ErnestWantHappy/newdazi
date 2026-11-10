"""教材课次解析必须唯一，标题不符或候选歧义时转 PENDING。"""
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts"))
from resolve_textbook_section import section_for


SECTIONS = {
    "xxkj-4-0-2026": [{"bookId": 3, "bookTitle": "四上", "sectionKey": "lesson-17", "title": "返回成语现场"}],
    "xxkj-8-1-2026": [{"bookId": 12, "bookTitle": "八下", "sectionKey": "lesson-2", "title": "语音合成技术"}],
}


class SectionMappingTest(unittest.TestCase):
    def test_resolve_unique_lesson(self):
        post = {"grade": 4, "semester": "1", "lessonNo": 17, "courseTitle": "《返回成语现场》"}
        resolved, state = section_for(post, SECTIONS)
        self.assertEqual(state, "RESOLVED")
        self.assertEqual(resolved["sectionKey"], "lesson-17")

    def test_title_mismatch_pending(self):
        post = {"grade": 4, "semester": "1", "lessonNo": 17, "courseTitle": "不相关课程"}
        resolved, state = section_for(post, SECTIONS)
        self.assertIsNone(resolved)
        self.assertTrue(state.startswith("PENDING"))

    def test_duplicate_candidate_pending(self):
        sections = {"xxkj-4-0-2026": SECTIONS["xxkj-4-0-2026"] * 2}
        post = {"grade": 4, "semester": "1", "lessonNo": 17, "courseTitle": "返回成语现场"}
        resolved, state = section_for(post, sections)
        self.assertIsNone(resolved)
        self.assertTrue(state.startswith("PENDING"))


if __name__ == "__main__":
    unittest.main()
