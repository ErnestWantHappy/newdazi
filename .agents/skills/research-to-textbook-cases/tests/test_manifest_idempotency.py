"""来源帖子幂等键稳定，不因课程标题或教师同名变化。"""
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts"))
from common import resource_key, source_key


class ManifestIdempotencyTest(unittest.TestCase):
    def test_source_key_is_post_id(self):
        self.assertEqual(source_key(15), "research-post-15")
        self.assertEqual(source_key(70), "research-post-70")

    def test_same_title_different_post_has_distinct_resource_key(self):
        digest = "a" * 64
        self.assertNotEqual(resource_key(60, digest), resource_key(70, digest))

    def test_repeated_key_is_stable(self):
        self.assertEqual(resource_key(70, "b" * 64), resource_key(70, "b" * 64))


if __name__ == "__main__":
    unittest.main()
