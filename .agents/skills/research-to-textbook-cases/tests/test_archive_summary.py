"""教师可见说明必须短、能操作，不泄露技术清单或内部路径。"""
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts"))
from build_archive_summary import MAX_REQUIREMENTS, summarize


def entry(name):
    return {"name": name, "isDir": False}


class ArchiveSummaryTest(unittest.TestCase):
    def test_local_classroom_tool_is_short_and_actionable(self):
        report = {
            "postId": 70,
            "name": "10.20公开课（用这个）.rar",
            "bytes": 120132700,
            "sha256": "c" * 64,
            "entries": [
                entry("10.20公开课（用这个）/启动课堂工具.bat"),
                entry("10.20公开课（用这个）/app.py"),
                entry("10.20公开课（用这个）/templates/index.html"),
                entry("10.20公开课（用这个）/data/groups.json"),
                entry("10.20公开课（用这个）/static/generated/a.png"),
                entry("10.20公开课（用这个）/static/uploads/a.mp3"),
            ],
            "safety": ["来源程序或快捷方式：10.20公开课（用这个）/app.py"],
        }
        resources = [
            {"postId": 70, "category": "ARCHIVE", "title": "配套材料包", "filename": report["name"]},
            {"postId": 70, "category": "DESIGN", "title": "《返回成语现场》教学设计"},
            {"postId": 70, "category": "SLIDES", "title": "《返回成语现场》课件"},
        ]
        text = summarize(report, resources)
        self.assertIn("启动课堂工具.bat", text)
        self.assertIn("教师端", text)
        self.assertIn("资源列表", text)
        self.assertNotIn("SHA-256", text)
        self.assertNotIn("目录概览", text)
        self.assertNotIn("static/generated", text)
        self.assertNotIn("app.py", text)
        self.assertLessEqual(len(text), MAX_REQUIREMENTS)

    def test_document_archive_mentions_contents_not_missing_placeholders(self):
        report = {
            "postId": 15,
            "name": "6-语音合成.zip",
            "bytes": 1,
            "sha256": "d" * 64,
            "entries": [
                entry("教学设计.docx"),
                entry("语音合成.pptx"),
                entry("学生任务单.docx"),
                entry("语音合成教师端.url"),
            ],
            "safety": [],
        }
        text = summarize(report, [])
        self.assertIn("教学设计", text)
        self.assertIn("课件", text)
        self.assertIn("学生任务单", text)
        self.assertIn("网页快捷方式", text)
        self.assertNotIn("未检出", text)
        self.assertNotIn("SHA-256", text)
        self.assertLessEqual(len(text), MAX_REQUIREMENTS)

    def test_personal_info_warning_is_teacher_facing(self):
        report = {
            "postId": 1,
            "name": "资料.zip",
            "bytes": 1,
            "sha256": "e" * 64,
            "entries": [entry("教学设计.docx")],
            "safety": ["文档内容疑似个人信息：名单.docx"],
        }
        text = summarize(report, [])
        self.assertIn("学生信息", text)
        self.assertNotIn("名单.docx", text)
        self.assertLessEqual(len(text), MAX_REQUIREMENTS)

    def test_date_folders_do_not_trigger_personal_info_warning(self):
        report = {
            "postId": 1,
            "name": "课堂记录.zip",
            "bytes": 1,
            "sha256": "f" * 64,
            "entries": [entry("memory/2026-10-14.md")],
            "safety": [],
        }
        text = summarize(report, [])
        self.assertNotIn("学生信息", text)


if __name__ == "__main__":
    unittest.main()
