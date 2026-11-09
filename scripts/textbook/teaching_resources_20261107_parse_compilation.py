"""解析 2026 整册汇编 PDF 的教学设计区块（只读分析）。

区块以“第N课 《…》教学设计”开头，紧跟“【来源】相对路径”，据此切出页码范围，
并尝试提取标题后的作者/学校行。输出 output/textbook-resources-20261107/compilation-blocks.json。
"""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-resources-20261107" / "compilation-pages"

HEADER = re.compile(r"^第\s*(\d{1,2})\s*课\s*[《“]?([^》”\n]*)》?\s*教学设计")
SOURCE = re.compile(r"^【来源】\s*(.+)$")


def main():
    report = {}
    for path in sorted(OUT.glob("*-matches.json")):
        code = path.name.replace("-matches.json", "")
        pages = json.loads((OUT / (code + ".json")).read_text(encoding="utf-8"))["pages"]
        blocks = []
        current = None
        for page in pages:
            lines = [line.strip() for line in page["text"].splitlines()]
            for index, line in enumerate(lines):
                match = HEADER.match(line)
                if match:
                    if current:
                        current["pageEnd"] = page["page"]
                        blocks.append(current)
                    source = ""
                    for follow in lines[index + 1:index + 4]:
                        found = SOURCE.match(follow)
                        if found:
                            source = found.group(1).strip()
                            break
                    candidate = [item for item in lines[index + 1:index + 8]
                                 if item and not item.startswith("【来源】") and not HEADER.match(item)]
                    current = {
                        "lessonNo": int(match.group(1)),
                        "title": match.group(2).strip(),
                        "pageStart": page["page"],
                        "pageEnd": page["page"],
                        "source": source,
                        "authorLine": candidate[:3],
                    }
        if current:
            current["pageEnd"] = pages[-1]["page"]
            blocks.append(current)
        report[code] = {"pageCount": len(pages), "blocks": blocks}
        print(code, len(blocks), "blocks")
        for block in blocks:
            print("   ", block["lessonNo"], block["title"], block["pageStart"], block["pageEnd"],
                  "|", block["source"][:60], "|", " / ".join(block["authorLine"])[:60])
    (OUT.parent / "compilation-blocks.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=1), encoding="utf-8")


if __name__ == "__main__":
    main()
