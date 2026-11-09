"""2026-11-07 宁波版教学设计批次：生成课例/资源整理清单（只读来源，不复制、不写库）。

以现行教材课次为准，逐课判断来源；卷根单文件按课名匹配课次（兼容七下 11–16 课
目录编号与现行教材相差一课的情况），二级目录按课例目录归属，含作者子目录时拆分。

输出 output/textbook-resources-20261107/case-draft.json 与 coverage.md。
"""
import hashlib
import json
import re
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-resources-20261107"
TOC = ROOT / "scripts/textbook/toc"

VOLUME_DIRS = {
    "三上（前湾高新）": "xxkj-3-0-2026",
    "三下（高新前湾）": "xxkj-3-1-2026",
    "四上（北仑）": "xxkj-4-0-2026",
    "四下（镇海）": "xxkj-4-1-2026",
    "五上（海曙）": "xxkj-5-0-2026",
    "五下（江北）": "xxkj-5-1-2026",
    "六上（慈溪）": "xxkj-6-0-2026",
    "六下（余姚）": "xxkj-6-1-2026",
    "七上（奉化）": "xxkj-7-0-2026",
    "七下（鄞州）": "xxkj-7-1-2026",
    "八上": "xxkj-8-0-2026",
    "八下（宁海）": "xxkj-8-1-2026",
    "九年级": "xxkj-9-9-2026",
}

DOC_SUFFIX = {".doc", ".docx", ".pdf", ".ppt", ".pptx", ".ppsx", ".pptm", ".wps", ".xls", ".xlsx"}
VIDEO_SUFFIX = {".mp4", ".wmv", ".mov", ".mpg", ".mpeg", ".avi", ".mkv"}
HTML_SUFFIX = {".html", ".htm"}
IMAGE_SUFFIX = {".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp"}
AUDIO_SUFFIX = {".mp3", ".wav", ".m4a", ".aac", ".wma"}
ARCHIVE_SUFFIX = {".zip", ".rar", ".7z", ".tar", ".gz"}
SKIP_DIRS = {"__pycache__", "node_modules", ".git", ".vite", ".venv", "venv", "env", "_shots", ".idea", ".vscode"}
VIDEO_LIMIT = 100_000_000
TOOL_LIMIT = 50 * 1024 * 1024

DESIGN = re.compile(r"教学设计|教案|设计案例|设计案列|教学案例|案例设计|设计$")
SLIDES = re.compile(r"课件|PPT|演示文稿|教学ppt", re.I)
WORKSHEET = re.compile(r"任务单|学习单|导学案|导学单|学案|学习包|作业练习|随堂练习|实验报告|实验记录|记录单|评价单|评价表|课堂记录|学生学习材料|学习任务单|实验单")
MATERIAL = re.compile(r"资料|素材|阅读|说明|手册|表格|数据|模板|学习资源|教学辅助|板书|逐字稿|答案|语料")
RECORDING = re.compile(r"课堂实录|课堂录像|教学录像|教学视频|录课|授课录像|完整版")
PERSONAL = re.compile(r"名单|花名册|学生信息|账号密码|密码表|班级码|个人信息")
STUDENT_WORK = re.compile(r"学生作品|学生作业|学生代表性|学生学习单收集")
TEMP = re.compile(r"^~\$|/~\$|\.tmp$|Thumbs\.db$|\.DS_Store$|desktop\.ini$")
TITLE_NOISE = re.compile(r"^(?:[^-—–]{2,8}[区县市])-|^[^-—–]{2,8}[区县市]—")
AUTHOR_SUFFIX = re.compile(r"[-—–]\s*([一-龥]{2,4}(?:[、，,\s][一-龥]{2,4})*)\s*$")
AUTHOR_FOLDER = re.compile(r"[-—–]\s*[一-龥]{2,4}(?:[、，,\s][一-龥]{2,4})*\s*$")


def digest(path):
    result = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            result.update(chunk)
    return result.hexdigest()


def normalize(text):
    return re.sub(r"[\s《》“”\"'‘’（）()【】\[\]、，,。．.·:：;；!！?？—–\-_]", "", text or "")


def number_in(name):
    patterns = [re.compile(r"第\s*(\d{1,2})\s*课"), re.compile(r"^(\d{1,2})\s*[\.．、\s《【\[]"),
                re.compile(r"^(\d{1,2})(?=[^\d])")]
    for pattern in patterns:
        match = pattern.search(name)
        if match:
            value = int(match.group(1))
            if 1 <= value <= 40:
                return value
    return None


def title_candidate(name):
    text = Path(name).stem
    text = re.sub(r"^\d{1,2}\s*[\.．、\s]+", "", text)
    text = re.sub(r"^第\s*\d{1,2}\s*课\s*", "", text)
    text = TITLE_NOISE.sub("", text)
    text = re.sub(r"[-—–]\s*[一-龥]{2,8}(?:小学|中学|学校|书院|校区)[-—–]?[一-龥]{0,8}$", "", text)
    text = AUTHOR_SUFFIX.sub("", text)
    text = re.sub(r"[（(][^）)]*[）)]", "", text)
    text = re.sub(r"(教学设计|教学案例|设计案例|设计案列|设计案例|课件|教案|教学PPT|同步练习|学习单|任务单|导学单|导学案|学习资料|配套资源)$", "", text, flags=re.I)
    return text.strip(" -—_《》")


def resolve_lesson(name, toc):
    candidate = normalize(title_candidate(name))
    if candidate:
        exact = [no for no, lesson in toc.items() if normalize(lesson["title"]) == candidate]
        if len(exact) == 1:
            return exact[0], "TITLE_EXACT"
        contains = [no for no, lesson in toc.items()
                    if len(candidate) >= 4 and (candidate in normalize(lesson["title"])
                                                or normalize(lesson["title"]) in candidate)]
        if len(contains) == 1:
            return contains[0], "TITLE_CONTAINS"
    no = number_in(name)
    if no in toc:
        return no, "NUMBER"
    return None, "UNRESOLVED"


def load_toc():
    books = {}
    for path in TOC.glob("*.json"):
        data = json.loads(path.read_text(encoding="utf-8"))
        lessons = {}
        for unit in data["units"]:
            for lesson in unit["lessons"]:
                lessons[lesson["no"]] = {"title": lesson["title"],
                                         "sectionKey": "lesson-%d" % lesson["no"],
                                         "unit": unit["title"]}
        books[data["book_code"]] = {"toc": data, "lessons": lessons}
    return books


def load_blocks():
    data = json.loads((OUT / "compilation-blocks.json").read_text(encoding="utf-8"))
    result = {}
    for code, record in data.items():
        cleaned = []
        for block in record["blocks"]:
            if cleaned and cleaned[-1]["lessonNo"] == block["lessonNo"] \
                    and not block["source"] and cleaned[-1]["pageEnd"] >= block["pageStart"] - 1:
                cleaned[-1]["pageEnd"] = max(cleaned[-1]["pageEnd"], block["pageEnd"])
                continue
            cleaned.append(dict(block))
        result[code] = cleaned
    return result


def school_in(text):
    match = re.search(r"[一-龥]{2,20}(?:小学|中学|学校|书院|校区|实验学校|中心学校|初级中学|附属学校)", text or "")
    return match.group(0).strip() if match else ""


def person_in(text):
    match = re.search(r"姓名[：: ]*([^\s，,。；;（）()]{2,20})", text or "")
    if match:
        return match.group(1).strip()
    return ""


def author_from_name(name):
    """只从“（作者）”“——作者”“-作者”这类明确位置取人名，避免把课名当作者。"""
    stem = Path(name).stem
    candidates = []
    for match in re.finditer(r"[（(]([^（）()]{2,30})[）)]", stem):
        candidates.append(match.group(1))
    for match in re.finditer(r"[—–]{1,2}\s*([^—–《》]{2,30})", stem):
        candidates.append(match.group(1))
    for match in re.finditer(r"[-]\s*([^-\s《》]{2,30})\s*$", stem):
        candidates.append(match.group(1))
    for match in re.finditer(r"[-—–]\s*([一-龥]{2,4}(?:\s+[一-龥]{2,4})?)\s*$", stem):
        candidates.append(match.group(1))
    for candidate in candidates:
        candidate = candidate.strip()
        school = school_in(candidate)
        if school:
            rest = candidate.replace(school, "").strip(" ：:—-　")
            if 2 <= len(rest) <= 12:
                return rest, school
        names = re.split(r"[、，,\s]+", candidate)
        # 连写的多字人名（如“徐凯王炳科”）没有分隔符，按整体保留，不做臆测拆分
        limit = 6 if len(names) == 1 else 4
        if 1 <= len(names) <= 3 and all(2 <= len(part) <= limit for part in names) \
                and not any(word in candidate for word in ("教学", "设计", "课件", "学习", "资源", "案例",
                                                           "视频", "说明", "单元", "年级", "上册", "下册")):
            return candidate, ""
    return "", ""


def author_from_lines(lines):
    for line in lines:
        if "姓名" in line or "所在单位" in line:
            return person_in(line), school_in(line)
    for line in lines[:20]:
        if len(line) > 40 or re.search(r"[，。；：:！？]", line):
            continue
        school = school_in(line)
        if not school:
            continue
        rest = line.replace(school, "").strip(" ：:—-　")
        rest = re.sub(r"(姓名|所在单位|学校|老师|教师)", "", rest).strip(" ：:—-　")
        match = re.match(r"^([一-龥]{2,4}(?:[、，,\s][一-龥]{2,4})*)", rest)
        if match:
            return match.group(1), school
    for line in lines[1:12]:
        if re.fullmatch(r"[一-龥]{2,4}(?:[、，,\s][一-龥]{2,4})*", line):
            return line, ""
    return "", ""


def match_block(blocks, source_rel, lesson, lesson_title):
    target = normalize(source_rel)
    for block in blocks:
        src = normalize(block["source"])
        if src and (target.endswith(src) or src.endswith(target)):
            author, school = author_from_lines(block["authorLine"])
            return block, author, school, True
    title = normalize(lesson_title)
    for block in blocks:
        if title and normalize(block["title"]) == title:
            author, school = author_from_lines(block["authorLine"])
            return block, author, school, False
    for block in blocks:
        if block["lessonNo"] == lesson:
            author, school = author_from_lines(block["authorLine"])
            return block, author, school, False
    return None, "", "", False


def classify(relative, size):
    name = Path(relative).name
    suffix = Path(relative).suffix.lower()
    for part in Path(relative).parts:
        if part in SKIP_DIRS:
            return "EXCLUDED_RUNTIME_DIR"
    if TEMP.search(relative):
        return "EXCLUDED_TEMP_FILE"
    if suffix == ".url":
        return "EXCLUDED_EXTERNAL_LINK"
    if suffix == ".exe" and size > TOOL_LIMIT:
        return "EXCLUDED_LARGE_TOOL"
    if suffix in VIDEO_SUFFIX:
        if RECORDING.search(name) or (suffix == ".mp4" and re.search(r"课堂|教学", name) and size > VIDEO_LIMIT):
            return "EXCLUDED_CLASS_RECORDING"
        if STUDENT_WORK.search(name):
            return "EXCLUDED_STUDENT_WORK"
        if PERSONAL.search(name):
            return "EXCLUDED_PERSONAL_DATA"
        if size > VIDEO_LIMIT:
            return "EXCLUDED_VIDEO_OVERSIZE"
        return "VIDEO"
    if suffix in ARCHIVE_SUFFIX:
        return "ARCHIVE"
    if suffix in HTML_SUFFIX:
        return "HTML"
    if suffix in IMAGE_SUFFIX:
        return "IMAGE"
    if suffix in AUDIO_SUFFIX:
        return "AUDIO"
    if suffix in DOC_SUFFIX:
        if WORKSHEET.search(name):
            return "WORKSHEET"
        if suffix in {".ppt", ".pptx", ".ppsx", ".pptm"} or SLIDES.search(name):
            return "SLIDES"
        if DESIGN.search(name):
            return "DESIGN"
        if MATERIAL.search(name) or suffix in {".xls", ".xlsx"}:
            return "MATERIAL"
        return "DOCUMENT"
    if suffix in {".py", ".js", ".css", ".json", ".tsx", ".jsx", ".txt", ".md", ".svg"}:
        return "CODE"
    return "OTHER"


def load_author_cache():
    path = OUT / "author-cache.json"
    return json.loads(path.read_text(encoding="utf-8")) if path.exists() else {}


def load_existing_names():
    """旧批次已上架课例的目录名→作者，作为未署名文档的最后兜底（只复用署名，不改旧数据）。"""
    path = ROOT / "output/textbook-cases/catalog-local-reviewed.json"
    result = {}
    if not path.exists():
        return result
    data = json.loads(path.read_text(encoding="utf-8"))
    for case in data.get("cases", []):
        directory = case.get("sourceDirectory") or ""
        if not directory:
            continue
        name = directory.split("/")[-1]
        result.setdefault(name, (case.get("author") or "", case.get("school") or ""))
    return result


def load_existing_hashes():
    """旧课例资源文件的 SHA-256 →（课例ID、作者、学校），用于跨批次内容复用。"""
    path = OUT / "existing-resource-paths.tsv"
    result = {}
    if not path.exists():
        return result
    for line in path.read_text(encoding="utf-8").splitlines():
        parts = line.split("\t")
        if len(parts) < 6:
            continue
        case_id, author, school, original_path, book_code, section_key = parts[:6]
        match = re.search(r"([0-9a-f]{64})", original_path or "")
        if match:
            result.setdefault(match.group(1), (int(case_id), author, school))
    return result


def primary_design(files):
    designs = [item for item in files if item["kind"] == "DESIGN"]
    if not designs:
        designs = [item for item in files if item["kind"] == "DOCUMENT"
                   and item["suffix"] in {".doc", ".docx", ".pdf"}]
    if not designs:
        return None
    designs.sort(key=lambda item: (0 if item["suffix"] == ".docx" else 1 if item["suffix"] == ".doc" else 2,
                                   item["bytes"]))
    return designs[0]


def build():
    inventory = json.loads((OUT / "source-inventory.json").read_text(encoding="utf-8"))
    records = [r for r in inventory["ningbo"]["records"]
               if r["relativePath"].split("/")[0] in VOLUME_DIRS]
    books = load_toc()
    blocks = load_blocks()
    cache = load_author_cache()
    existing_names = load_existing_names()
    existing_hashes = load_existing_hashes()
    tree = defaultdict(list)
    for record in records:
        tree[record["relativePath"].split("/")[0]].append(record)

    cases = []
    exclusions = []
    coverage = {}
    for volume_dir, book_code in VOLUME_DIRS.items():
        items = tree.get(volume_dir, [])
        toc = books[book_code]["lessons"]
        volume_blocks = blocks.get(book_code, [])
        covered = set()
        seen_hashes = {}

        def add_case(source_name, group, lesson_meta, no, block_author="", block_school="", block_exact=False):
            files = []
            for record in sorted(group, key=lambda r: r["relativePath"]):
                relative = record["relativePath"][len(volume_dir) + 1:]
                kind = classify(relative, record["bytes"])
                if kind.startswith("EXCLUDED"):
                    exclusions.append({"relativePath": record["relativePath"], "reason": kind})
                    continue
                files.append({"relativePath": record["relativePath"], "casePath": relative,
                              "bytes": record["bytes"], "sha256": record["sha256"],
                              "suffix": record["suffix"], "kind": kind})
            if not files:
                exclusions.append({"relativePath": group[0]["relativePath"],
                                   "reason": "EXCLUDED_EMPTY_AFTER_RULES"})
                return
            design = primary_design(files)
            author, school = author_from_name(source_name)
            if not author and design:
                info = cache.get(str(Path(inventory["ningbo"]["root"]) / design["relativePath"]), {})
                if info.get("author"):
                    author, school = info["author"], info.get("school") or school
            if not author and block_author:
                author, school = block_author, block_school or school
            if block_exact and not school and block_school:
                school = block_school
            if not author and source_name in existing_names:
                author, school = existing_names[source_name]
            # 设计文件哈希已存在于旧批次资源：按内容认定同一课例，复用旧作者归属
            existing_case = None
            if design and design["sha256"] in existing_hashes:
                existing_case = existing_hashes[design["sha256"]][0]
                if not author:
                    author, school = existing_hashes[design["sha256"]][1], existing_hashes[design["sha256"]][2] or school
            title = lesson_meta["title"]
            basis = json.dumps([book_code, lesson_meta["sectionKey"], author,
                                sorted(item["sha256"] for item in files)], ensure_ascii=False)
            source_key = "nb2026" + hashlib.sha1(basis.encode("utf-8")).hexdigest()[:32]
            # 同一册同一课同内容重复文件（如三下第3课的根目录与文件夹）只保留资料更全的一份
            signature = (book_code, lesson_meta["sectionKey"],
                         design["sha256"] if design else "", normalize(author))
            if signature in seen_hashes and seen_hashes[signature] >= len(files):
                exclusions.append({"relativePath": group[0]["relativePath"],
                                   "reason": "DUPLICATE_IN_BATCH"})
                return
            seen_hashes[signature] = max(seen_hashes.get(signature, 0), len(files))
            cases.append({
                "batch": "nb2026", "sourceKey": source_key, "bookCode": book_code,
                "sectionKey": lesson_meta["sectionKey"], "lessonNo": no,
                "lessonTitle": title, "unit": lesson_meta["unit"], "title": title,
                "author": author or "", "school": school or "", "origin": "NINGBO",
                "existingCaseId": existing_case,
                "sourceName": source_name, "sourceDir": volume_dir + "/" + source_name,
                "compilationSource": "",
                "compilationPages": None, "files": files,
                "counts": dict(Counter(item["kind"] for item in files)),
                "notes": [],
            })
            covered.add(no)

        # 1) 二级目录（可能是单课例目录，也可能是作者子目录）
        first_level = defaultdict(list)
        for record in items:
            parts = record["relativePath"].split("/")
            if len(parts) >= 3:
                first_level[parts[1]].append(record)
        for folder, folder_items in sorted(first_level.items()):
            if folder.startswith("原教材"):
                for record in folder_items:
                    exclusions.append({"relativePath": record["relativePath"],
                                       "reason": "EXCLUDED_OLD_EDITION"})
                continue
            no, method = resolve_lesson(folder, toc)
            if no is None:
                for record in folder_items:
                    exclusions.append({"relativePath": record["relativePath"],
                                       "reason": "PENDING_LESSON_MAPPING"})
                continue
            sub = defaultdict(list)
            for record in folder_items:
                pieces = record["relativePath"].split("/")
                if len(pieces) >= 4 and resolve_lesson(pieces[2], toc)[0] == no:
                    sub[pieces[2]].append(record)
            # 只有“同课多作者/多县区”目录才拆分；课件、素材、实验工具等资产目录仍归属本课例。
            split_ok = len(sub) >= 2 or (len(sub) == 1 and AUTHOR_FOLDER.search(next(iter(sub))))
            if not split_ok:
                sub = {folder: folder_items}
            for sub_name, sub_items in sorted(sub.items()):
                source_rel = sub_items[0]["relativePath"][len(volume_dir) + 1:]
                block, block_author, block_school, block_exact = match_block(
                    volume_blocks, source_rel, no, toc[no]["title"])
                add_case(sub_name, sub_items, toc[no], no, block_author, block_school, block_exact)

        # 2) 卷根文件（按课名/课号归属；同一课同作者多格式只算一个课例）
        roots = [record for record in items if len(record["relativePath"].split("/")) == 2]
        root_groups = defaultdict(list)
        for record in roots:
            name = record["relativePath"].split("/")[1]
            no, method = resolve_lesson(name, toc)
            if no is None:
                reason = "EXCLUDED_OLD_EDITION" if name.startswith("原教材") else "PENDING_LESSON_MAPPING"
                exclusions.append({"relativePath": record["relativePath"], "reason": reason})
                continue
            root_groups[(no, name)].append(record)
        for (no, name), group in sorted(root_groups.items()):
            record = group[0]
            block, block_author, block_school, block_exact = match_block(
                volume_blocks, record["relativePath"][len(volume_dir) + 1:], no, toc[no]["title"])
            add_case(name, group, toc[no], no, block_author, block_school, block_exact)

        coverage[book_code] = {
            "volume": volume_dir, "total": len(toc), "covered": sorted(covered),
            "compilationLessons": sorted({block["lessonNo"] for block in volume_blocks}),
        }

    cases = merge_cases(cases)
    payload = {"sourceRoot": inventory["ningbo"]["root"], "cases": cases,
               "exclusions": exclusions, "coverage": coverage}
    (OUT / "case-draft.json").write_text(json.dumps(payload, ensure_ascii=False, indent=1), encoding="utf-8")
    write_coverage(coverage, cases, exclusions)
    print("cases", len(cases), "exclusions", len(exclusions))
    print("by book", Counter(case["bookCode"] for case in cases))
    for code, item in coverage.items():
        missing = sorted(set(range(1, item["total"] + 1)) - set(item["covered"]))
        print(code, "lessons", item["total"], "covered", len(item["covered"]), "missing", missing)


def merge_cases(cases):
    """同册同课同作者的多份来源合并为一个课例，文件去重后作为版本资源保留。"""
    groups = defaultdict(list)
    singles = []
    for case in cases:
        author = normalize(case["author"])
        if author:
            groups[(case["bookCode"], case["sectionKey"], author)].append(case)
        else:
            singles.append(case)
    merged = list(singles)
    for key, group in groups.items():
        if len(group) == 1:
            merged.append(group[0])
            continue
        group.sort(key=lambda item: len(item["files"]), reverse=True)
        base = dict(group[0])
        files = {}
        sources = []
        for item in group:
            sources.append(item["sourceName"])
            item["existingCaseId"] and base.__setitem__("existingCaseId", item["existingCaseId"])
            for record in item["files"]:
                files.setdefault(record["sha256"], record)
        base["files"] = sorted(files.values(), key=lambda record: record["casePath"])
        base["sourceNames"] = sources
        base["counts"] = dict(Counter(item["kind"] for item in base["files"]))
        base["notes"] = base.get("notes", []) + ["已合并同一作者同课的多份来源"]
        merged.append(base)
    merged.sort(key=lambda item: (item["bookCode"], item["lessonNo"], item["author"]))
    return merged


def write_coverage(coverage, cases, exclusions):
    lines = ["# 2026 宁波版教学设计逐册覆盖（自动草稿）", ""]
    for code, item in sorted(coverage.items()):
        toc = json.loads((TOC / (code + ".json")).read_text(encoding="utf-8"))
        titles = {}
        for unit in toc["units"]:
            for lesson in unit["lessons"]:
                titles[lesson["no"]] = lesson["title"]
        lines.append("## %s（%s）" % (code, item["volume"]))
        lines.append("")
        lines.append("| 课次 | 来源判定 | 课例数 |")
        lines.append("| --- | --- | ---: |")
        for no in range(1, item["total"] + 1):
            key = "lesson-%d" % no
            count = sum(1 for case in cases if case["bookCode"] == code and case["sectionKey"] == key)
            if count:
                state = "逐课设计"
            elif no in item["compilationLessons"]:
                state = "汇编待提取"
            else:
                state = "无来源"
            lines.append("| %d. %s | %s | %d |" % (no, titles.get(no, ""), state, count))
        lines.append("")
    lines.append("## 排除统计")
    lines.append("")
    for reason, count in Counter(item["reason"] for item in exclusions).most_common():
        lines.append("- %s: %d" % (reason, count))
    (OUT / "coverage.md").write_text("\n".join(lines), encoding="utf-8")


if __name__ == "__main__":
    build()
