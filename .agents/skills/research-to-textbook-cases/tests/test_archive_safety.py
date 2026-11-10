"""压缩包路径、魔数、快捷方式与安全分类的最小回归。"""
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts"))
from common import (PipelineError, category_for, decode_zip_name, detect_mime,
                    safe_member_path, url_shortcut_targets)
from inspect_archive import unsafe_external_names


class ArchiveSafetyTest(unittest.TestCase):
    def test_reject_traversal_and_absolute_paths(self):
        for name in ("../escape.txt", "a/../../escape.txt", "/etc/passwd", "C:/Windows/a.exe"):
            with self.assertRaises(PipelineError):
                safe_member_path(name)

    def test_accept_relative_member(self):
        self.assertEqual(safe_member_path("课件/第1课.pptx").as_posix(), "课件/第1课.pptx")

    def test_external_archive_rejects_unsafe_member_before_extract(self):
        self.assertEqual(
            unsafe_external_names(["课件/第1课.pptx", "../escape.txt", "/etc/passwd"]),
            ["../escape.txt", "/etc/passwd"],
        )

    def test_decode_gbk_zip_name(self):
        raw = "丹城中学语音合成.pptx".encode("gbk").decode("cp437")
        self.assertEqual(decode_zip_name(raw), "丹城中学语音合成.pptx")

    def test_magic_over_extension(self):
        self.assertEqual(detect_mime(b"PK\x03\x04data", "伪装.txt"), "application/zip")
        self.assertEqual(detect_mime(b"%PDF-1.7", "伪装.docx"), "application/pdf")

    def test_archive_category(self):
        self.assertEqual(category_for("材料.rar", "application/octet-stream"), "ARCHIVE")
        self.assertEqual(category_for("课件.pptx", "application/zip+ooxml"), "SLIDES")

    def test_url_shortcut_blocks_private_targets(self):
        data = b"[InternetShortcut]\r\nURL=http://127.0.0.1/tool\r\nURL=https://example.org/tool\r\n"
        self.assertEqual(url_shortcut_targets(data), ["<blocked>", "https://example.org/tool"])


if __name__ == "__main__":
    unittest.main()
