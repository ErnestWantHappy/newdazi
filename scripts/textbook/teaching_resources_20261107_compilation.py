"""提取 2026 整册教学设计 PDF 的逐页文本，定位每课起始页（只读来源，输出分析文件）。

输出 output/textbook-resources-20261107/compilation-pages/<book>.json：
  pages: [{page, text}]
  matches: [{lessonNo, title, page, score}]
"""
import json
import re
from pathlib import Path

import fitz

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-resources-20261107"
SOURCE = Path(r"E:\11-7\新省版义务段信息科技新教材教学设计（宁波版）\2026新教材教学设计（宁波版）PDF")
TOC = ROOT / "scripts/textbook/toc"

BOOK_FILE = {
    "xxkj-3-0-2026": "2026义务教育信息科技三年级上册教学设计.pdf",
    "xxkj-3-1-2026": "2026义务教育信息科技三年级下册教学设计.pdf",
    "xxkj-4-0-2026": "2026义务教育信息科技四年级上册教学设计.pdf",
    "xxkj-4-1-2026": "2026义务教育信息科技四年级下册教学设计.pdf",
    "xxkj-5-0-2026": "2026义务教育信息科技五年级上册教学设计.pdf",
    "xxkj-5-1-2026": "2026义务教育信息科技五年级下册教学设计.pdf",
    "xxkj-6-0-2026": "2026义务教育信息科技六年级上册教学设计.pdf",
    "xxkj-6-1-2026": "2026义务教育信息科技六年级下册教学设计.pdf",
    "xxkj-7-0-2026": "2026义务教育信息科技七年级上册教学设计.pdf",
    "xxkj-7-1-2026": "2026义务教育信息科技七年级下册教学设计.pdf",
    "xxkj-8-0-2026": "2026义务教育信息科技八年级上册教学设计.pdf",
    "xxkj-8-1-2026": "2026义务教育信息科技八年级下册教学设计.pdf",
    "xxkj-9-9-2026": "2026义务教育信息科技九年级教学设计.pdf",
}


def normalize(text):
    return re.sub(r"\s+", "", text)


def main():
    target = OUT / "compilation-pages"
    target.mkdir(parents=True, exist_ok=True)
    summary = {}
    for code, filename in BOOK_FILE.items():
        path = SOURCE / filename
        toc = json.loads((TOC / (code + ".json")).read_text(encoding="utf-8"))
        document = fitz.open(path)
        pages = []
        for index in range(document.page_count):
            pages.append({"page": index + 1, "text": document[index].get_text("text")})
        document.close()
        record = {"bookCode": code, "file": filename, "pageCount": len(pages), "pages": pages}
        (target / (code + ".json")).write_text(json.dumps(record, ensure_ascii=False), encoding="utf-8")
        # 简单定位：在前 12 页目录之后，从第 8 页开始找“第N课”或课名出现的位置
        matches = []
        for unit in toc["units"]:
            for lesson in unit["lessons"]:
                title = normalize(lesson["title"])
                hit = []
                for page in pages:
                    text = normalize(page["text"])
                    if title and title in text:
                        hit.append(page["page"])
                matches.append({"lessonNo": lesson["no"], "title": lesson["title"],
                                "pages": hit[:8], "first": hit[0] if hit else None})
        record_out = {"bookCode": code, "pageCount": len(pages), "matches": matches}
        (target / (code + "-matches.json")).write_text(
            json.dumps(record_out, ensure_ascii=False, indent=1), encoding="utf-8")
        summary[code] = {"pageCount": len(pages),
                         "located": sum(1 for item in matches if item["first"]),
                         "total": len(matches)}
        print(code, summary[code])
    (OUT / "compilation-summary.json").write_text(
        json.dumps(summary, ensure_ascii=False, indent=1), encoding="utf-8")


if __name__ == "__main__":
    main()
