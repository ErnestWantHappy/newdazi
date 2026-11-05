"""按真实教材种子目录生成全量候选与署名校对报告，不自动上架。"""
import json
import re
import zipfile
import xml.etree.ElementTree as ET
from pathlib import Path
import fitz

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases"
GRADE = {"三": 3, "四": 4, "五": 5, "六": 6, "七": 7, "八": 8, "九": 9}


def text(path):
    try:
        if path.suffix.lower() == ".pdf":
            with fitz.open(path) as document:
                return " ".join(document[i].get_text() for i in range(min(2, len(document))))
        with zipfile.ZipFile(path) as archive:
            if path.suffix.lower() == ".docx":
                names = ["word/document.xml"]
            else:
                names = sorted((n for n in archive.namelist() if re.fullmatch(r"ppt/slides/slide\d+\.xml", n)), key=lambda n: int(re.search(r"slide(\d+)", n)[1]))[:3]
            return " ".join(node.text or "" for name in names for node in ET.fromstring(archive.read(name)).iter() if node.tag.endswith("}t"))
    except (OSError, ValueError, zipfile.BadZipFile, ET.ParseError):
        return ""


def main():
    catalog = {}
    book = None
    for line in (ROOT / "sql/textbook_seed_20261029.sql").read_text(encoding="utf-8").splitlines():
        match = re.search(r"VALUES \('(xxkj-[^']+)'", line)
        if match:
            book = match[1]
        match = re.search(r"'(lesson-\d+)', 'LESSON', \d+, '([^']+)'", line)
        if match:
            catalog[(book, match[1])] = match[2]
    data = json.loads((OUT / "inventory.json").read_text(encoding="utf-8"))
    report = []
    signature_groups = {}
    for index, case in enumerate(data["cases"], 1):
        directory = Path(case["sourceDirectory"])
        grade = GRADE[directory.parts[0][0]]
        semester = "9" if grade == 9 else "0" if "上册" in case["sourceDirectory"] else "1"
        book = f"xxkj-{grade}-{semester}-2026"
        match = re.search(r"第(\d+)课", directory.name)
        number = match[1] if match else {"开关量的生成": "7", "认识智能体": "18", "互联网与人工智能": "5"}.get(next((name for name in ["开关量的生成", "认识智能体", "互联网与人工智能"] if name in directory.name), ""))
        section = f"lesson-{number}" if number else ""
        case.update(bookCode=book, sectionKey=section)
        case["title"] = catalog.get((book, section), directory.name)
        case["mappingVerified"] = (book, section) in catalog
        material = []
        signatures = []
        for resource in case["resources"]:
            path = Path(data["sourceRoot"]) / resource["source"]
            if len(path.relative_to(Path(data["sourceRoot"]) / directory).parts) > 3:
                continue
            if path.suffix.lower() not in {".docx", ".pptx", ".pdf"}:
                continue
            content = re.sub(r"\s+", " ", text(path))[:2500]
            category = "SLIDES" if path.suffix.lower() == ".pptx" or "PPT" in path.stem.upper() or "课件" in path.stem else "DESIGN" if "设计" in path.stem or "教学设计" in content[:500] else "WORKSHEET"
            resource.update(category=category, title={"DESIGN": "教学设计", "SLIDES": "教学课件", "WORKSHEET": "任务单"}[category])
            signatures.append(resource["sha256"])
            # 仅展示文档开头，供署名核对，排除数据集与运行配置。
            material.append(f"  {path.name} [{category}] {content[:240]}")
        signature = tuple(sorted(signatures))
        if signature and signature in signature_groups:
            case["duplicateCandidate"] = signature_groups[signature]
        else:
            signature_groups[signature] = case["sourceKey"]
        report.append(f"{index}. {case['sourceDirectory']}\n  对应 {book}/{section} {case['title']}\n" + "\n".join(material))
    (OUT / "catalog-candidates.json").write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
    (OUT / "catalog-review.txt").write_text("\n\n".join(report), encoding="utf-8")
    print(f"已匹配{sum(c['mappingVerified'] for c in data['cases'])}/{len(data['cases'])}目录；署名与去重报告已生成，未上架")


if __name__ == "__main__":
    main()
