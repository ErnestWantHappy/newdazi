# -*- coding: utf-8 -*-
"""电子教材导入管线。

职责：
1. 读取 volumes.json + toc/<book_code>.json，校验课次区间完整、无重叠、无越界；
2. 用 PyMuPDF 把每页渲染成 1400px 宽的 JPEG（默认质量 75）；
3. 生成 manifest.json（册/单元/条目/1301 页元数据与哈希）、toc-review.md、SHA256SUMS；
4. 生成幂等种子 SQL（sql/textbook_seed_<pack>.sql）。

用法：
    python scripts/textbook/import_textbook.py
    python scripts/textbook/import_textbook.py --only xxkj-7-0-2026
    python scripts/textbook/import_textbook.py --skip-render   # 只重建清单/SQL/校对表
"""

import argparse
import datetime as dt
import hashlib
import io
import json
import os
import re
import sys
from pathlib import Path

try:
    import fitz  # PyMuPDF
except ImportError:
    sys.exit("缺少 PyMuPDF，请先执行: pip install pymupdf")

try:
    from PIL import Image
except ImportError:
    Image = None

ROOT = Path(__file__).resolve().parents[2]
BASE = Path(__file__).resolve().parent


def load_json(path):
    with io.open(path, "r", encoding="utf-8") as fp:
        return json.load(fp)


def sha256_file(path, chunk=1024 * 1024):
    h = hashlib.sha256()
    with open(path, "rb") as fp:
        while True:
            block = fp.read(chunk)
            if not block:
                break
            h.update(block)
    return h.hexdigest()


def norm(text):
    text = text.replace("\u3000", " ").replace("\xa0", " ")
    text = re.sub(r"\s+", "", text)
    for a, b in zip("１２３４５６７８９０", "1234567890"):
        text = text.replace(a, b)
    return text.replace("（", "(").replace("）", ")")


def offset_for(pdf_no, book):
    off = int(book["page_offset"])
    for br in sorted(book.get("offset_breaks") or [], key=lambda x: x["from_pdf"]):
        if pdf_no >= int(br["from_pdf"]):
            off = int(br["offset"])
    return off


def printed_no(pdf_no, book):
    if pdf_no <= int(book["front"]["pdf_end"]):
        return None
    return pdf_no - offset_for(pdf_no, book)


def flatten_sections(book):
    """按页面顺序返回全部条目，用于连续性校验与建库。"""
    rows = []
    front = book["front"]
    rows.append({
        "section_key": "front", "kind": "FRONT", "unit_no": None, "lesson_no": None,
        "title": front["title"], "printed_start": None, "printed_end": None,
        "pdf_start": int(front["pdf_start"]), "pdf_end": int(front["pdf_end"]),
    })
    for unit in book["units"]:
        no = int(unit["unit_no"])
        if unit.get("intro"):
            intro = unit["intro"]
            rows.append({
                "section_key": "unit-%d-intro" % no, "kind": "UNIT_INTRO", "unit_no": no,
                "lesson_no": None, "title": intro.get("title") or "单元导入",
                "printed_start": intro.get("printed_start"), "printed_end": intro.get("printed_end"),
                "pdf_start": int(intro["pdf_start"]), "pdf_end": int(intro["pdf_end"]),
            })
        for les in unit["lessons"]:
            rows.append({
                "section_key": "lesson-%d" % int(les["no"]), "kind": "LESSON", "unit_no": no,
                "lesson_no": int(les["no"]), "title": les["title"],
                "printed_start": les.get("printed_start"), "printed_end": les.get("printed_end"),
                "pdf_start": int(les["pdf_start"]), "pdf_end": int(les["pdf_end"]),
            })
        if unit.get("summary"):
            summary = unit["summary"]
            rows.append({
                "section_key": "summary-%d" % no, "kind": "SUMMARY", "unit_no": no,
                "lesson_no": None, "title": summary.get("title") or "单元小结与反思",
                "printed_start": summary.get("printed_start"), "printed_end": summary.get("printed_end"),
                "pdf_start": int(summary["pdf_start"]), "pdf_end": int(summary["pdf_end"]),
            })
    if book.get("appendix"):
        app = book["appendix"]
        rows.append({
            "section_key": "appendix", "kind": "APPENDIX", "unit_no": None, "lesson_no": None,
            "title": app["title"], "printed_start": None, "printed_end": None,
            "pdf_start": int(app["pdf_start"]), "pdf_end": int(app["pdf_end"]),
        })
    rows.sort(key=lambda r: r["pdf_start"])
    return rows


def validate_book(book, pdf_pages):
    errors = []
    total = int(book["total_pages"])
    if total != pdf_pages:
        errors.append("total_pages=%d 与 PDF 实际 %d 页不一致" % (total, pdf_pages))

    rows = flatten_sections(book)
    expect = 1
    for row in rows:
        if row["pdf_start"] > row["pdf_end"]:
            errors.append("%s 起始页大于结束页" % row["section_key"])
        if row["pdf_start"] != expect:
            errors.append("%s 起点应为 %d，实际 %d（存在缺口或重叠）" % (row["section_key"], expect, row["pdf_start"]))
        expect = row["pdf_end"] + 1
    if expect != total + 1:
        errors.append("条目只覆盖到第 %d 页，全册共 %d 页" % (expect - 1, total))

    seen = set()
    for unit in book["units"]:
        for les in unit["lessons"]:
            no = int(les["no"])
            if no in seen:
                errors.append("课次 %d 重复" % no)
            seen.add(no)
    return rows, errors


def check_titles(doc, book, rows, src_start=1):
    """有文本层的册，校验每课起始页确实出现课名；无文本层直接跳过。"""
    warnings = []
    sample = norm(doc[src_start - 1 + rows[0]["pdf_start"] - 1].get_text())
    if not sample:
        return warnings, False
    for row in rows:
        if row["kind"] not in ("LESSON", "SUMMARY"):
            continue
        text = norm(doc[src_start - 1 + row["pdf_start"] - 1].get_text())[:320]
        key = norm(row["title"])[:8]
        if key and key not in text:
            warnings.append("%s 起始页未匹配到课名《%s》" % (row["section_key"], row["title"]))
    return warnings, True


def render_book(book, source, pages_dir, width, quality, skip_render, validate_only=False):
    doc = fitz.open(source)
    # 合订本（如《七年级下册.pdf》里前半是七上）只取指定页段，并按 1..N 重新编号
    src_start = int(book.get("source_page_start") or 1)
    src_end = int(book.get("source_page_end") or doc.page_count)
    page_count = src_end - src_start + 1
    rows, errors = validate_book(book, page_count)
    title_warnings, has_text = check_titles(doc, book, rows, src_start)
    if validate_only:
        doc.close()
        return rows, [], errors, title_warnings, has_text

    pages_dir.mkdir(parents=True, exist_ok=True)
    pages = []
    for pdf_no in range(1, page_count + 1):
        target = pages_dir / ("p%03d.jpg" % pdf_no)
        if skip_render and target.exists():
            if Image is None:
                raise RuntimeError("--skip-render 需要 Pillow 读取图片尺寸")
            with Image.open(target) as img:
                w, h = img.size
        else:
            page = doc[src_start - 1 + pdf_no - 1]
            zoom = width / page.rect.width
            pix = page.get_pixmap(matrix=fitz.Matrix(zoom, zoom), alpha=False)
            data = pix.tobytes("jpeg", jpg_quality=quality)
            target.write_bytes(data)
            w, h = pix.width, pix.height
        size = target.stat().st_size
        pages.append({
            "page_no": pdf_no,
            "file": "%s/p%03d.jpg" % (book["book_code"], pdf_no),
            "width": w,
            "height": h,
            "bytes": size,
            "sha256": sha256_file(target),
            "printed_no": printed_no(pdf_no, book),
        })
    doc.close()
    return rows, pages, errors, title_warnings, has_text


def sql_str(value):
    if value is None:
        return "NULL"
    return "'" + str(value).replace("\\", "\\\\").replace("'", "''") + "'"


def build_seed_sql(manifest, pack):
    lines = [
        "-- 电子教材种子数据（%s 生成，幂等可重复执行）" % pack,
        "-- 依赖 sql/textbook_v1.sql 已先建表",
        "SET NAMES utf8mb4;",
        "",
    ]
    for book in manifest["books"]:
        code = book["book_code"]
        lines.append("-- ===== %s %s =====" % (book["short_title"], book["title"]))
        lines.append(
            "INSERT INTO biz_textbook_book (book_code, title, short_title, grade, semester, publisher, edition, "
            "total_pages, page_offset, status, sort_no, source_file, source_sha256, imported_at) VALUES ("
            "%s, %s, %s, %d, %s, %s, %s, %d, %d, '0', %d, %s, %s, NOW()) "
            "ON DUPLICATE KEY UPDATE title=VALUES(title), short_title=VALUES(short_title), grade=VALUES(grade), "
            "semester=VALUES(semester), publisher=VALUES(publisher), edition=VALUES(edition), "
            "total_pages=VALUES(total_pages), page_offset=VALUES(page_offset), status=VALUES(status), "
            "sort_no=VALUES(sort_no), source_file=VALUES(source_file), source_sha256=VALUES(source_sha256), "
            "imported_at=VALUES(imported_at);" % (
                sql_str(code), sql_str(book["title"]), sql_str(book["short_title"]),
                int(book["grade"]), sql_str(book["semester"]), sql_str(book["publisher"]),
                sql_str(book["edition"]), int(book["total_pages"]), int(book["page_offset"]),
                int(book["sort_no"]), sql_str(book["source_file"]), sql_str(book["source_sha256"]),
            )
        )
        lines.append("SET @book_id := (SELECT book_id FROM biz_textbook_book WHERE book_code=%s);" % sql_str(code))

        for unit in book["units"]:
            lines.append(
                "INSERT INTO biz_textbook_unit (book_id, unit_no, title, sort_no) VALUES "
                "(@book_id, %d, %s, %d) ON DUPLICATE KEY UPDATE title=VALUES(title), sort_no=VALUES(sort_no);" % (
                    int(unit["unit_no"]), sql_str(unit["title"]), int(unit["unit_no"]),
                )
            )
        lines.append("")

        for row in book["sections"]:
            unit_expr = "NULL"
            if row["unit_no"] is not None:
                unit_expr = "(SELECT unit_id FROM biz_textbook_unit WHERE book_id=@book_id AND unit_no=%d)" % int(row["unit_no"])
            lines.append(
                "INSERT INTO biz_textbook_section (book_id, unit_id, section_key, kind, lesson_no, title, "
                "printed_start, printed_end, pdf_start, pdf_end, sort_no) VALUES ("
                "@book_id, %s, %s, %s, %s, %s, %s, %s, %d, %d, %d) "
                "ON DUPLICATE KEY UPDATE unit_id=VALUES(unit_id), kind=VALUES(kind), lesson_no=VALUES(lesson_no), "
                "title=VALUES(title), printed_start=VALUES(printed_start), printed_end=VALUES(printed_end), "
                "pdf_start=VALUES(pdf_start), pdf_end=VALUES(pdf_end), sort_no=VALUES(sort_no);" % (
                    unit_expr, sql_str(row["section_key"]), sql_str(row["kind"]),
                    "NULL" if row["lesson_no"] is None else int(row["lesson_no"]),
                    sql_str(row["title"]),
                    "NULL" if row["printed_start"] is None else int(row["printed_start"]),
                    "NULL" if row["printed_end"] is None else int(row["printed_end"]),
                    int(row["pdf_start"]), int(row["pdf_end"]), int(row["sort_no"]),
                )
            )
        lines.append("")

        for page in book["pages"]:
            lines.append(
                "INSERT INTO biz_textbook_page (book_id, page_no, printed_no, image_path, width, height, bytes, sha256) "
                "VALUES (@book_id, %d, %s, %s, %d, %d, %d, %s) ON DUPLICATE KEY UPDATE "
                "printed_no=VALUES(printed_no), image_path=VALUES(image_path), width=VALUES(width), "
                "height=VALUES(height), bytes=VALUES(bytes), sha256=VALUES(sha256);" % (
                    int(page["page_no"]),
                    "NULL" if page["printed_no"] is None else int(page["printed_no"]),
                    sql_str(page["file"]), int(page["width"]), int(page["height"]),
                    int(page["bytes"]), sql_str(page["sha256"]),
                )
            )
        lines.append("")
    return "\n".join(lines) + "\n"


def fmt_mb(num):
    return "%.1f MB" % (num / 1048576.0)


def build_review(manifest, pack):
    out = []
    out.append("# 电子教材目录校对表（%s）" % pack)
    out.append("")
    out.append("> 由 `scripts/textbook/import_textbook.py` 自动生成。请重点抽查「印刷页」与「PDF 页」是否对应，"
               "确认后再执行 `sql/textbook_seed_%s.sql` 入库。" % pack)
    out.append("")
    total_pages = sum(b["total_pages"] for b in manifest["books"])
    total_bytes = sum(p["bytes"] for b in manifest["books"] for p in b["pages"])
    total_lessons = sum(1 for b in manifest["books"] for s in b["sections"] if s["kind"] == "LESSON")
    out.append("| 项目 | 数值 |")
    out.append("| :-- | --: |")
    out.append("| 册数 | %d |" % len(manifest["books"]))
    out.append("| 总页数 | %d |" % total_pages)
    out.append("| 课次总数 | %d |" % total_lessons)
    out.append("| 图片总体积 | %s |" % fmt_mb(total_bytes))
    out.append("| 渲染参数 | 宽 %dpx / JPEG q%d |" % (manifest["render"]["width"], manifest["render"]["quality"]))
    out.append("")

    out.append("## 一、册次总览")
    out.append("")
    out.append("| 册 | 书名 | 页数 | 单元 | 课次 | 体积 | 源文件 |")
    out.append("| :-- | :-- | --: | --: | --: | --: | :-- |")
    for book in manifest["books"]:
        lessons = sum(1 for s in book["sections"] if s["kind"] == "LESSON")
        size = sum(p["bytes"] for p in book["pages"])
        out.append("| %s | %s | %d | %d | %d | %s | %s |" % (
            book["short_title"], book["title"], book["total_pages"], len(book["units"]),
            lessons, fmt_mb(size), book["source_file"],
        ))
    if manifest.get("missing"):
        out.append("")
        out.append("未上架：%s。" % "、".join(m["short_title"] for m in manifest["missing"]))
    out.append("")

    out.append("## 二、逐册目录")
    for book in manifest["books"]:
        out.append("")
        out.append("### %s · %s" % (book["short_title"], book["title"]))
        out.append("")
        out.append("- 册编码：`%s`；PDF 共 %d 页；印刷页偏移 %d。" % (
            book["book_code"], book["total_pages"], book["page_offset"]))
        out.append("- 源文件：`%s`（%s，SHA-256 `%s`）" % (
            book["source_file"], fmt_mb(book["source_bytes"]), book["source_sha256"][:16] + "…"))
        for note in book.get("notes") or []:
            out.append("- ⚠️ %s" % note)
        out.append("")
        out.append("| 单元 | 类型 | 课次 | 标题 | 印刷页 | PDF 页 | 页数 |")
        out.append("| :-- | :-- | :-- | :-- | :-- | :-- | --: |")
        for row in book["sections"]:
            kind_map = {"FRONT": "前置", "UNIT_INTRO": "单元导入", "LESSON": "课", "SUMMARY": "单元小结", "APPENDIX": "附录"}
            unit_title = ""
            for unit in book["units"]:
                if int(unit["unit_no"]) == row.get("unit_no"):
                    unit_title = unit["title"]
                    break
            printed = "—"
            if row.get("printed_start") is not None:
                printed = "%d–%d" % (row["printed_start"], row["printed_end"])
            out.append("| %s | %s | %s | %s | %s | %d–%d | %d |" % (
                unit_title, kind_map.get(row["kind"], row["kind"]),
                "—" if row.get("lesson_no") is None else "第%d课" % row["lesson_no"],
                row["title"], printed, row["pdf_start"], row["pdf_end"],
                row["pdf_end"] - row["pdf_start"] + 1,
            ))
    out.append("")
    return "\n".join(out)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--out", default=None)
    parser.add_argument("--width", type=int, default=None)
    parser.add_argument("--quality", type=int, default=None)
    parser.add_argument("--only", default=None, help="只处理指定册编码，逗号分隔")
    parser.add_argument("--skip-render", action="store_true")
    parser.add_argument("--validate-only", action="store_true", help="只校验目录区间，不渲染图片、不写制品")
    args = parser.parse_args()

    pack = load_json(BASE / "volumes.json")
    pack_version = pack["pack_version"]
    width = args.width or int(pack["render"]["width"])
    quality = args.quality or int(pack["render"]["quality"])
    out_dir = Path(args.out) if args.out else ROOT / "output" / ("textbook-pack-%s" % pack_version)
    out_dir = out_dir.resolve()
    sql_path = ROOT / "sql" / ("textbook_seed_%s.sql" % pack_version)
    only = set(args.only.split(",")) if args.only else None

    manifest = {
        "pack_version": pack_version,
        "generated_at": dt.datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "render": {"width": width, "quality": quality, "format": "jpeg"},
        "books": [],
        "missing": pack.get("missing") or [],
    }

    failures = []
    for item in pack["books"]:
        code = item["code"]
        if only and code not in only:
            continue
        toc = load_json(BASE / item["toc"])
        source = Path(item["source"])
        print("[%s] 读取 %s" % (code, source))
        if not source.exists():
            failures.append("%s 源文件不存在: %s" % (code, source))
            continue

        rows, pages, errors, warnings, has_text = render_book(
            toc, source, out_dir / "pages" / code, width, quality, args.skip_render,
            validate_only=args.validate_only,
        )
        if errors:
            failures.extend("%s: %s" % (code, e) for e in errors)
            continue
        if warnings:
            for warning in warnings:
                print("  [警告] %s" % warning)
        if args.validate_only:
            print("  目录校验通过：%d 页，%d 个单元" % (toc["total_pages"], len(toc["units"])))
            continue

        for idx, row in enumerate(rows):
            row["sort_no"] = idx
        book = {
            "book_code": code,
            "stage": item["stage"],
            "sort_no": int(item["sort_no"]),
            "title": toc["title"],
            "short_title": toc["short_title"],
            "grade": int(toc["grade"]),
            "semester": str(toc["semester"]),
            "publisher": toc.get("publisher") or "浙江教育出版社",
            "edition": toc.get("edition") or "2026",
            "total_pages": int(toc["total_pages"]),
            "page_offset": int(toc["page_offset"]),
            "offset_breaks": toc.get("offset_breaks") or [],
            "front": toc["front"],
            "units": toc["units"],
            "appendix": toc.get("appendix"),
            "notes": toc.get("notes") or [],
            "source_file": source.name,
            "source_bytes": source.stat().st_size,
            "source_sha256": sha256_file(source),
            "source_page_start": int(toc.get("source_page_start") or 1),
            "source_page_end": int(toc.get("source_page_end") or 0) or None,
            "has_text_layer": has_text,
            "sections": rows,
            "pages": pages,
        }
        manifest["books"].append(book)
        size = sum(p["bytes"] for p in pages)
        print("  完成 %d 页，%s" % (len(pages), fmt_mb(size)))

    if failures:
        print("\n校验失败，未生成清单与 SQL：")
        for failure in failures:
            print("  - %s" % failure)
        return 2

    if args.validate_only:
        print("\n全部 %d 册目录校验通过（未渲染、未写制品）。" % len(pack["books"]))
        return 0

    manifest["books"].sort(key=lambda b: b["sort_no"])
    out_dir.mkdir(parents=True, exist_ok=True)
    with io.open(out_dir / "manifest.json", "w", encoding="utf-8") as fp:
        json.dump(manifest, fp, ensure_ascii=False, indent=1)

    sums = []
    for book in manifest["books"]:
        for page in book["pages"]:
            sums.append("%s  pages/%s" % (page["sha256"], page["file"]))
    with io.open(out_dir / "SHA256SUMS", "w", encoding="utf-8", newline="\n") as fp:
        fp.write("\n".join(sums) + "\n")

    with io.open(out_dir / "toc-review.md", "w", encoding="utf-8") as fp:
        fp.write(build_review(manifest, pack_version))

    sql_path.parent.mkdir(parents=True, exist_ok=True)
    with io.open(sql_path, "w", encoding="utf-8", newline="\n") as fp:
        fp.write(build_seed_sql(manifest, pack_version))

    total_bytes = sum(p["bytes"] for b in manifest["books"] for p in b["pages"])
    total_pages = sum(b["total_pages"] for b in manifest["books"])
    print("\n制品目录: %s" % out_dir)
    print("  册数 %d，页数 %d，图片 %s" % (len(manifest["books"]), total_pages, fmt_mb(total_bytes)))
    print("  清单: manifest.json / toc-review.md / SHA256SUMS")
    print("  种子 SQL: %s" % sql_path)
    return 0


if __name__ == "__main__":
    sys.exit(main())
