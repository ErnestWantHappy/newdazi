# -*- coding: utf-8 -*-
"""高中教材与课程标准导入包生成脚本。

只读取 G 盘原始 PDF，输出到独立 stage 目录；不直接写数据库、不修改正式资源。
高中教材沿用现有四张教材表，课程标准使用独立表，避免把标准文件伪装成课次。
"""

import hashlib
import json
import shutil
from pathlib import Path

import fitz
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
STAGE = ROOT / "output" / "high-school-textbook-pack-20261109"

BOOKS = [
    {
        "code": "xxkj-hs-b1-2019", "grade": 10, "semester": "9", "sort_no": 80,
        "title": "普通高中信息技术 必修1 数据与计算", "short_title": "高中必修1",
        "source": r"G:\教师工作资料\信息科技新教材的pdf\高中教材\【浙教版】必修1_数据与计算.pdf",
        "chapters": [
            ("第一章 数据与信息", 8, 10, [
                ("1.1 感知数据", 11, 14), ("1.2 数据、信息与知识", 15, 19),
                ("1.3 数据采集与编码", 20, 31), ("1.4 数据管理与安全", 32, 34),
                ("1.5 数据与大数据", 35, 38)], 39, 43),
            ("第二章 算法与问题解决", 44, 46, [
                ("2.1 算法的概念及描述", 47, 59), ("2.2 算法的控制结构", 60, 64),
                ("2.3 用算法解决问题的过程", 65, 69)], 70, 73),
            ("第三章 算法的程序实现", 74, 76, [
                ("3.1 用计算机编程解决问题的一般过程", 77, 78),
                ("3.2 Python语言程序设计", 79, 102),
                ("3.3 简单算法及其程序实现", 103, 115)], 116, 120),
            ("第四章 数据处理与应用", 121, 122, [
                ("4.1 常用表格数据的处理", 123, 129), ("4.2 大数据处理", 130, 157),
                ("4.3 大数据典型应用", 158, 161)], 162, 165),
            ("第五章 人工智能及应用", 166, 168, [
                ("5.1 人工智能的产生与发展", 169, 178), ("5.2 人工智能的应用", 179, 182),
                ("5.3 人工智能对社会的影响", 183, 188)], 189, 192),
        ],
        "appendix": ("附录 ASCII码表", 193),
    },
    {
        "code": "xxkj-hs-b2-2019", "grade": 11, "semester": "9", "sort_no": 81,
        "title": "普通高中信息技术 必修2 信息系统与社会", "short_title": "高中必修2",
        "source": r"G:\教师工作资料\信息科技新教材的pdf\高中教材\【浙教版】高中必修2+信息系统与社会信息技术电子课本批注版.pdf",
        "chapters": [
            ("第一章 信息系统概述", 8, 10, [
                ("1.1 信息技术与信息系统", 11, 17), ("1.2 信息系统的组成与功能", 18, 21),
                ("1.3 信息系统的应用", 22, 27), ("1.4 信息社会及其发展", 28, 33)], 34, 37),
            ("第二章 信息系统的支撑技术", 38, 40, [
                ("2.1 计算机硬件", 41, 49), ("2.2 计算机软件", 50, 59),
                ("2.3 移动终端", 60, 66), ("2.4 传感与控制", 67, 75),
                ("2.5 网络系统", 76, 88), ("2.6 网络应用软件开发", 89, 99)], 100, 103),
            ("第三章 信息系统安全", 104, 106, [
                ("3.1 信息安全与保护", 107, 116), ("3.2 信息系统安全与防护", 117, 131)], 132, 135),
            ("第四章 信息系统的搭建实例", 136, 138, [
                ("4.1 搭建信息系统的前期准备", 139, 145), ("4.2 搭建信息系统", 146, 155),
                ("4.3 完善信息系统", 156, 158)], 159, 161),
        ],
    },
    {
        "code": "xxkj-hs-e1-2019", "grade": 12, "semester": "9", "sort_no": 82,
        "title": "普通高中信息技术 选择性必修1 数据与数据结构", "short_title": "高中选择性必修1",
        "source": r"G:\教师工作资料\信息科技新教材的pdf\高中教材\【浙教版】选修1_数据与数据结构.pdf",
        "chapters": [
            ("第一章 数据与数据的组织", 8, 10, [
                ("1.1 数据", 11, 15), ("1.2 数据的组织", 16, 31)], 32, 37),
            ("第二章 数组与链表", 38, 40, [
                ("2.1 数组", 41, 56), ("2.2 链表", 57, 63)], 64, 67),
            ("第三章 字符串、队列和栈", 68, 70, [
                ("3.1 字符串", 71, 80), ("3.2 队列", 81, 88), ("3.3 栈", 89, 97)], 98, 101),
            ("第四章 树", 102, 104, [
                ("4.1 树与二叉树", 105, 112), ("4.2 二叉树的基本操作", 113, 119),
                ("4.3 抽象数据类型", 120, 125)], 126, 129),
            ("第五章 数据结构与算法", 130, 132, [
                ("5.1 数据结构与算法效率", 133, 135), ("5.2 迭代与递归", 136, 145),
                ("5.3 数据排序", 146, 153), ("5.4 数据查找", 154, 165)], 166, 169),
            ("第六章 大数据时代数据的组织", 170, 172, [
                ("6.1 实时查询系统中数据的组织", 173, 179),
                ("6.2 POI数据的组织与应用", 180, 183)], 184, 185),
        ],
    },
]

STANDARDS = [
    {"key": "it-2022", "title": "义务教育信息科技课程标准（2022年版）", "audience": "义务教育", "sort_no": 10,
     "source": r"G:\教师工作资料\信息科技新教材的pdf\信息科技课程标准2022.pdf"},
    {"key": "hs-it-2025", "title": "普通高中信息科技课程标准（2017年版2025年修订）", "audience": "普通高中", "sort_no": 20,
     "source": r"G:\教师工作资料\信息科技新教材的pdf\高中教材\普通高中信息科技课程标准（2017年版2025年修订）.pdf"},
]


def sha256_file(path):
    digest = hashlib.sha256()
    with path.open("rb") as fp:
        for block in iter(lambda: fp.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def sql_value(value):
    if value is None:
        return "NULL"
    if isinstance(value, int):
        return str(value)
    return "'" + str(value).replace("\\", "\\\\").replace("'", "''") + "'"


def validate_book(book, page_count):
    cursor = 1
    # 三本高中教材的封面、版权、前言和目录均占 PDF 前 7 页。
    cursor = 8
    if page_count < cursor:
        raise RuntimeError(f"{book['code']} PDF页数不足")
    for title, intro_start, intro_end, lessons, summary_start, summary_end in book["chapters"]:
        if intro_start != cursor or intro_end < intro_start:
            raise RuntimeError(f"{book['code']} {title} 导入区间无效，期望起点 {cursor}")
        cursor = intro_end + 1
        for lesson_title, start, end in lessons:
            if start != cursor or end < start:
                raise RuntimeError(f"{book['code']} {lesson_title} 区间无效，期望起点 {cursor}")
            cursor = end + 1
        if summary_start != cursor or summary_end < summary_start:
            raise RuntimeError(f"{book['code']} {title} 小结区间无效，期望起点 {cursor}")
        cursor = summary_end + 1
    if "appendix" in book:
        title, start = book["appendix"]
        if start != cursor:
            raise RuntimeError(f"{book['code']} {title} 起点应为 {cursor}，实际 {start}")
        cursor = start + 1
    if cursor != page_count + 1:
        raise RuntimeError(f"{book['code']} 只覆盖到 {cursor - 1} 页，PDF共 {page_count} 页")


def render_book(book):
    source = Path(book["source"])
    document = fitz.open(source)
    validate_book(book, document.page_count)
    target_dir = STAGE / "pages" / book["code"]
    target_dir.mkdir(parents=True, exist_ok=True)
    pages = []
    for page_no in range(1, document.page_count + 1):
        target = target_dir / f"p{page_no:03d}.jpg"
        page = document[page_no - 1]
        zoom = 1400 / page.rect.width
        pixmap = page.get_pixmap(matrix=fitz.Matrix(zoom, zoom), alpha=False)
        target.write_bytes(pixmap.tobytes("jpeg", jpg_quality=75))
        with Image.open(target) as image:
            width, height = image.size
        pages.append({"pageNo": page_no, "printedNo": page_no - 7 if page_no >= 8 else None,
                      "path": f"{book['code']}/p{page_no:03d}.jpg", "width": width, "height": height,
                      "bytes": target.stat().st_size, "sha256": sha256_file(target)})
    document.close()
    return pages


def build_seed_sql(manifest):
    lines = ["-- 高中教材与课程标准（2026-11-09）", "-- 高中教材写入现有四张教材表；课程标准写独立表。脚本幂等，可重复执行。", "SET NAMES utf8mb4;", "",
             "CREATE TABLE IF NOT EXISTS `biz_textbook_standard` (",
             "  `standard_id` BIGINT NOT NULL AUTO_INCREMENT, `standard_key` VARCHAR(50) NOT NULL COMMENT '稳定编码',",
             "  `title` VARCHAR(160) NOT NULL COMMENT '标准名称', `audience` VARCHAR(30) NOT NULL COMMENT '适用学段',",
             "  `filename` VARCHAR(255) NOT NULL COMMENT '下载文件名', `original_path` VARCHAR(500) NOT NULL COMMENT 'TEXTBOOK_ROOT/cases相对路径',",
             "  `preview_path` VARCHAR(500) DEFAULT NULL COMMENT '预览PDF相对路径', `bytes` BIGINT NOT NULL DEFAULT 0, `sha256` CHAR(64) DEFAULT NULL,",
             "  `sort_no` INT NOT NULL DEFAULT 0, `status` CHAR(1) NOT NULL DEFAULT '0', `create_time` DATETIME DEFAULT NULL, `update_time` DATETIME DEFAULT NULL,",
             "  PRIMARY KEY (`standard_id`), UNIQUE KEY `uk_textbook_standard_key` (`standard_key`)",
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='信息科技课程标准';", ""]
    for item in manifest["standards"]:
        lines.append("INSERT INTO biz_textbook_standard (standard_key,title,audience,filename,original_path,preview_path,bytes,sha256,sort_no,status,create_time) VALUES "
                     f"({sql_value(item['key'])},{sql_value(item['title'])},{sql_value(item['audience'])},{sql_value(item['filename'])},{sql_value(item['path'])},"
                     f"{sql_value(item['path'])},{item['bytes']},{sql_value(item['sha256'])},{item['sortNo']},'0',NOW()) "
                     "ON DUPLICATE KEY UPDATE title=VALUES(title),audience=VALUES(audience),filename=VALUES(filename),original_path=VALUES(original_path),"
                     "preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),sort_no=VALUES(sort_no),status='0',update_time=NOW();")
    for book in manifest["books"]:
        lines += ["", f"-- ===== {book['shortTitle']} {book['title']} ====="]
        lines.append("INSERT INTO biz_textbook_book (book_code,title,short_title,grade,semester,publisher,edition,total_pages,page_offset,status,sort_no,source_file,source_sha256,imported_at) VALUES "
                     f"({sql_value(book['code'])},{sql_value(book['title'])},{sql_value(book['shortTitle'])},{book['grade']},{sql_value(book['semester'])},{sql_value('浙江教育出版社')},"
                     f"{sql_value('2019')},{book['totalPages']},7,'0',{book['sortNo']},{sql_value(book['sourceFile'])},{sql_value(book['sourceSha256'])},NOW()) "
                     "ON DUPLICATE KEY UPDATE title=VALUES(title),short_title=VALUES(short_title),grade=VALUES(grade),semester=VALUES(semester),publisher=VALUES(publisher),edition=VALUES(edition),"
                     "total_pages=VALUES(total_pages),page_offset=VALUES(page_offset),status='0',sort_no=VALUES(sort_no),source_file=VALUES(source_file),source_sha256=VALUES(source_sha256),imported_at=NOW();")
        lines.append(f"SET @book_id := (SELECT book_id FROM biz_textbook_book WHERE book_code={sql_value(book['code'])});")
        for unit_no, chapter in enumerate(book["chapters"], 1):
            lines.append(f"INSERT INTO biz_textbook_unit (book_id,unit_no,title,sort_no) VALUES (@book_id,{unit_no},{sql_value(chapter['title'])},{unit_no}) ON DUPLICATE KEY UPDATE title=VALUES(title),sort_no=VALUES(sort_no);")
        for section in book["sections"]:
            unit_expr = "NULL" if section["unitNo"] is None else f"(SELECT unit_id FROM biz_textbook_unit WHERE book_id=@book_id AND unit_no={section['unitNo']})"
            lines.append("INSERT INTO biz_textbook_section (book_id,unit_id,section_key,kind,lesson_no,title,printed_start,printed_end,pdf_start,pdf_end,sort_no) VALUES "
                         f"(@book_id,{unit_expr},{sql_value(section['key'])},{sql_value(section['kind'])},{sql_value(section['lessonNo'])},{sql_value(section['title'])},"
                         f"{sql_value(section['printedStart'])},{sql_value(section['printedEnd'])},{section['pdfStart']},{section['pdfEnd']},{section['sortNo']}) "
                         "ON DUPLICATE KEY UPDATE unit_id=VALUES(unit_id),kind=VALUES(kind),lesson_no=VALUES(lesson_no),title=VALUES(title),printed_start=VALUES(printed_start),"
                         "printed_end=VALUES(printed_end),pdf_start=VALUES(pdf_start),pdf_end=VALUES(pdf_end),sort_no=VALUES(sort_no);")
        for page in book["pages"]:
            lines.append("INSERT INTO biz_textbook_page (book_id,page_no,printed_no,image_path,width,height,bytes,sha256) VALUES "
                         f"(@book_id,{page['pageNo']},{sql_value(page['printedNo'])},{sql_value(page['path'])},{page['width']},{page['height']},{page['bytes']},{sql_value(page['sha256'])}) "
                         "ON DUPLICATE KEY UPDATE printed_no=VALUES(printed_no),image_path=VALUES(image_path),width=VALUES(width),height=VALUES(height),bytes=VALUES(bytes),sha256=VALUES(sha256);")
    return "\n".join(lines) + "\n"


def main():
    if STAGE.exists():
        shutil.rmtree(STAGE)
    (STAGE / "pages").mkdir(parents=True)
    manifest = {"books": [], "standards": []}
    for standard in STANDARDS:
        source = Path(standard["source"])
        digest = sha256_file(source)
        target_dir = STAGE / "cases" / "standards"
        target_dir.mkdir(parents=True, exist_ok=True)
        target = target_dir / f"{digest}.pdf"
        shutil.copy2(source, target)
        manifest["standards"].append({"key": standard["key"], "title": standard["title"], "audience": standard["audience"],
                                      "sortNo": standard["sort_no"], "filename": source.name, "path": f"standards/{digest}.pdf",
                                      "bytes": target.stat().st_size, "sha256": digest})
        print(f"[standard] {standard['title']} {target.stat().st_size} bytes")
    for config in BOOKS:
        source = Path(config["source"])
        pages = render_book(config)
        sections, lesson_no, sort_no = [], 0, 0
        sections.append({"key": "front", "kind": "FRONT", "unitNo": None, "lessonNo": None, "title": "封面 · 前言 · 目录",
                         "printedStart": None, "printedEnd": None, "pdfStart": 1, "pdfEnd": 7, "sortNo": sort_no})
        for unit_no, (title, intro_start, intro_end, lessons, summary_start, summary_end) in enumerate(config["chapters"], 1):
            sort_no += 1
            sections.append({"key": f"unit-{unit_no}-intro", "kind": "UNIT_INTRO", "unitNo": unit_no, "lessonNo": None, "title": "本章导入",
                             "printedStart": None, "printedEnd": None, "pdfStart": intro_start, "pdfEnd": intro_end, "sortNo": sort_no})
            for lesson_title, start, end in lessons:
                sort_no += 1
                lesson_no += 1
                sections.append({"key": f"lesson-{lesson_no}", "kind": "LESSON", "unitNo": unit_no, "lessonNo": lesson_no, "title": lesson_title,
                                 "printedStart": start - 7, "printedEnd": end - 7, "pdfStart": start, "pdfEnd": end, "sortNo": sort_no})
            sort_no += 1
            sections.append({"key": f"summary-{unit_no}", "kind": "SUMMARY", "unitNo": unit_no, "lessonNo": None, "title": "本章巩固与项目挑战",
                             "printedStart": None, "printedEnd": None, "pdfStart": summary_start, "pdfEnd": summary_end, "sortNo": sort_no})
        if "appendix" in config:
            title, start = config["appendix"]
            sections.append({"key": "appendix", "kind": "APPENDIX", "unitNo": None, "lessonNo": None, "title": title,
                             "printedStart": None, "printedEnd": None, "pdfStart": start, "pdfEnd": len(pages), "sortNo": sort_no + 1})
        manifest["books"].append({"code": config["code"], "title": config["title"], "shortTitle": config["short_title"],
                                  "grade": config["grade"], "semester": config["semester"], "sortNo": config["sort_no"], "totalPages": len(pages),
                                  "sourceFile": source.name, "sourceSha256": sha256_file(source),
                                  "chapters": [{"title": item[0]} for item in config["chapters"]], "sections": sections, "pages": pages})
        print(f"[book] {config['short_title']} {len(pages)} pages")
    with (STAGE / "manifest.json").open("w", encoding="utf-8", newline="\n") as fp:
        json.dump(manifest, fp, ensure_ascii=False, indent=1)
    sql_path = ROOT / "sql" / "high_school_textbooks_20261109.sql"
    sql_path.write_text(build_seed_sql(manifest), encoding="utf-8", newline="\n")
    print(f"SQL: {sql_path}")
    print(f"STAGE: {STAGE}")


if __name__ == "__main__":
    main()
