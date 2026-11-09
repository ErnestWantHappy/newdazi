"""从教学设计文档正文提取作者与学校（只读来源）。

支持 .docx（直接读 XML）、.doc（LibreOffice 转文本，兼容 GBK）、.pdf（PyMuPDF）。
结果缓存到 output/textbook-resources-20261107/author-cache.json。
"""
import hashlib
import json
import re
import subprocess
import zipfile
from pathlib import Path

import fitz

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-resources-20261107"
CACHE = OUT / "author-cache.json"
SOFFICE = r"C:/Program Files/LibreOffice/program/soffice.exe"
WORK = OUT / "work" / "author-txt"

SCHOOL = re.compile(r"[\u4e00-\u9fa5]{2,14}(?:小学|中学|学校|书院|校区|实验学校|中心学校|初级中学|附属学校|附校|二小|一小|中心校)")
NAME = re.compile(r"^[\u4e00-\u9fa5]{2,4}(?:[、，, ][\u4e00-\u9fa5]{2,4})*$")


def digest(path):
    return hashlib.sha256(str(path).encode("utf-8")).hexdigest()[:24]


def docx_text(path):
    with zipfile.ZipFile(path) as stream:
        xml = stream.read("word/document.xml").decode("utf-8", "ignore")
    xml = re.sub(r"</w:p>", "\n", xml)
    xml = re.sub(r"<w:tab[^>]*/>", " ", xml)
    return re.sub(r"<[^>]+>", "", xml)


def decode_file(path):
    raw = path.read_bytes()
    for encoding in ("utf-8-sig", "utf-8", "gb18030"):
        try:
            return raw.decode(encoding)
        except UnicodeDecodeError:
            continue
    return raw.decode("utf-8", "ignore")


def convert_text(path):
    WORK.mkdir(parents=True, exist_ok=True)
    profile = OUT / "work" / "soffice-profile-author"
    subprocess.run([SOFFICE, "-env:UserInstallation=" + profile.as_uri(),
                    "--headless", "--convert-to", "txt:Text", "--outdir", str(WORK), str(path)],
                   stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=180)
    for candidate in (WORK / (path.stem + ".txt"), WORK / (digest(path) + ".txt")):
        if candidate.exists():
            return decode_file(candidate)
    return ""


def pdf_text(path):
    document = fitz.open(path)
    text = "\n".join(document[index].get_text("text") for index in range(min(8, document.page_count)))
    document.close()
    return text


def read_text(path):
    suffix = path.suffix.lower()
    if suffix == ".docx":
        return docx_text(path)
    if suffix == ".pdf":
        return pdf_text(path)
    if suffix == ".doc":
        return convert_text(path)
    return ""


def strip_prefix(text):
    return re.sub(r"^[\s：:，,。；;、\-—_（(【\[]+", "", text or "")


def extract(text):
    lines = [line.strip() for line in text.splitlines() if line.strip()]
    head = lines[:45]
    for line in head[:15]:
        if len(line) > 90 or not ("姓名" in line or "所在单位" in line):
            continue
        school = SCHOOL.search(line)
        name = re.search(r"姓名[：: ]*([^\s，,。；;（）()]{2,20})", line)
        if name or school:
            return {"author": name.group(1).replace(" ", "") if name else "",
                    "school": school.group(0) if school else "", "evidence": line}
    for line in head[:25]:
        if len(line) > 60 or re.search(r"[，。；：:！？]", line):
            continue
        # 先把“（翔云校区）”这类补充说明去掉，避免学校名被切成两段
        cleaned = re.sub(r"[（(][^）)]*[）)]", " ", line)
        school = SCHOOL.search(cleaned)
        if not school:
            continue
        rest = strip_prefix(cleaned.replace(school.group(0), " "))
        rest = re.sub(r"(姓名|所在单位|学校|老师|教师)", "", rest).strip(" ：:—-　")
        match = re.match(r"^([\u4e00-\u9fa5]{2,4}(?:[、，,\s][\u4e00-\u9fa5]{2,4})*)", rest)
        if match and not any(word in match.group(1) for word in ("校区", "学校", "小学", "中学")):
            return {"author": match.group(1), "school": school.group(0), "evidence": line}
    for line in head[1:15]:
        candidate = strip_prefix(line)
        if NAME.match(candidate) and len(candidate) <= 8:
            return {"author": candidate, "school": "", "evidence": line}
    return {"author": "", "school": "", "evidence": ""}


def main():
    cache = json.loads(CACHE.read_text(encoding="utf-8")) if CACHE.exists() else {}
    targets = json.loads((OUT / "author-targets.json").read_text(encoding="utf-8"))
    for item in targets:
        path = Path(item)
        key = str(path)
        if key in cache and cache[key].get("bytes") == path.stat().st_size:
            continue
        try:
            text = read_text(path)
            result = extract(text)
            result["bytes"] = path.stat().st_size
            result["head"] = [line for line in text.splitlines() if line.strip()][:8]
        except Exception as error:  # 单个文档失败不能中断整批
            result = {"author": "", "school": "", "evidence": "", "error": str(error),
                      "bytes": path.stat().st_size, "head": []}
        cache[key] = result
    CACHE.write_text(json.dumps(cache, ensure_ascii=False, indent=1), encoding="utf-8")
    print("cached", len(cache), "missing", sum(1 for value in cache.values() if not value.get("author")))


if __name__ == "__main__":
    main()
