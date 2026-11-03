# -*- coding: utf-8 -*-
"""电子教材数据包校验。

校验项：
1. manifest 中每个条目区间连续覆盖 1..total_pages，无重叠、无越界；
2. 1301 张图片全部存在、字节数与 SHA-256 与清单一致；
3. 图片尺寸与清单一致（默认抽查每册前 3 页 + 随机 5 页读像素）；
4. 抽样重渲染（默认每册 2 页），与新生成的 JPEG 哈希比对，确认源 PDF 与制品同源；
5. 课次编号唯一、印刷页区间单调。

用法：
    python scripts/textbook/verify_pack.py
    python scripts/textbook/verify_pack.py --pack output/textbook-pack-20261029 --rerender 3
"""

import argparse
import hashlib
import io
import json
import random
import sys
from pathlib import Path

import fitz
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]


def sha256_bytes(data):
    return hashlib.sha256(data).hexdigest()


def sha256_file(path):
    h = hashlib.sha256()
    with open(path, "rb") as fp:
        for block in iter(lambda: fp.read(1024 * 1024), b""):
            h.update(block)
    return h.hexdigest()


def check_coverage(book, errors):
    sections = sorted(book["sections"], key=lambda s: s["pdf_start"])
    expect = 1
    for sec in sections:
        if sec["pdf_start"] != expect:
            errors.append("%s 区间断裂：期望 %d，实际 %d" % (book["short_title"], expect, sec["pdf_start"]))
        expect = sec["pdf_end"] + 1
    if expect != book["total_pages"] + 1:
        errors.append("%s 覆盖到 %d，全册 %d 页" % (book["short_title"], expect - 1, book["total_pages"]))

    lesson_nos = [s["lesson_no"] for s in sections if s["kind"] == "LESSON"]
    if len(lesson_nos) != len(set(lesson_nos)):
        errors.append("%s 课次编号重复" % book["short_title"])
    if lesson_nos != sorted(lesson_nos):
        errors.append("%s 课次编号未按顺序" % book["short_title"])

    printed = [(s["printed_start"], s["printed_end"]) for s in sections if s.get("printed_start") is not None]
    for i in range(1, len(printed)):
        if printed[i][0] <= printed[i - 1][1]:
            errors.append("%s 印刷页区间重叠：%s 与 %s" % (book["short_title"], printed[i - 1], printed[i]))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--pack", default=str(ROOT / "output" / "textbook-pack-20261029"))
    parser.add_argument("--rerender", type=int, default=2, help="每册抽样重渲染页数")
    args = parser.parse_args()

    pack = Path(args.pack).resolve()
    manifest = json.load(io.open(pack / "manifest.json", "r", encoding="utf-8"))
    errors = []
    warnings = []
    checked = 0
    rerendered = 0

    for book in manifest["books"]:
        check_coverage(book, errors)
        pages = book["pages"]
        if len(pages) != book["total_pages"]:
            errors.append("%s 页记录 %d 条，应为 %d" % (book["short_title"], len(pages), book["total_pages"]))

        for page in pages:
            path = pack / "pages" / page["file"]
            if not path.exists():
                errors.append("%s 缺图 %s" % (book["short_title"], page["file"]))
                continue
            size = path.stat().st_size
            if size != page["bytes"]:
                errors.append("%s %s 字节数不符（清单 %d，实际 %d）" % (book["short_title"], page["file"], page["bytes"], size))
                continue
            if sha256_file(path) != page["sha256"]:
                errors.append("%s %s SHA-256 不符" % (book["short_title"], page["file"]))
            checked += 1

        # 尺寸抽查：前 3 页 + 随机 5 页
        sample = {p["page_no"]: p for p in pages[:3]}
        for page in random.sample(pages, min(5, len(pages))):
            sample[page["page_no"]] = page
        for page in sample.values():
            path = pack / "pages" / page["file"]
            if not path.exists():
                continue
            with Image.open(path) as img:
                if img.size != (page["width"], page["height"]):
                    errors.append("%s %s 尺寸不符（清单 %dx%d，实际 %dx%d）" % (
                        book["short_title"], page["file"], page["width"], page["height"], img.size[0], img.size[1]))

        # 重渲染抽样：确认与源 PDF 同源
        if args.rerender > 0:
            source = Path(book["source_file"])
            if not source.exists():
                # 清单只存文件名，尝试用 volumes.json 还原绝对路径
                volumes = json.load(io.open(ROOT / "scripts" / "textbook" / "volumes.json", "r", encoding="utf-8"))
                for item in volumes["books"]:
                    if item["code"] == book["book_code"]:
                        source = Path(item["source"])
                        break
            if not source.exists():
                warnings.append("%s 源文件不可访问，跳过重渲染" % book["short_title"])
            else:
                doc = fitz.open(str(source))
                # 合订本（七下取自《七年级下册.pdf》的 126–248 页）要按清单里的起始页偏移取样
                src_start = int(book.get("source_page_start") or 1)
                picks = [1, book["total_pages"]] if args.rerender == 1 else [1, max(1, book["total_pages"] // 2)]
                for pdf_no in picks[:args.rerender]:
                    page = doc[src_start - 1 + pdf_no - 1]
                    zoom = manifest["render"]["width"] / page.rect.width
                    pix = page.get_pixmap(matrix=fitz.Matrix(zoom, zoom), alpha=False)
                    data = pix.tobytes("jpeg", jpg_quality=manifest["render"]["quality"])
                    expect = next(p for p in pages if p["page_no"] == pdf_no)
                    if sha256_bytes(data) != expect["sha256"]:
                        errors.append("%s 第 %d 页重渲染哈希不一致" % (book["short_title"], pdf_no))
                    rerendered += 1
                doc.close()

    print("校验目录: %s" % pack)
    print("  册数 %d，页数校验 %d 张，重渲染抽样 %d 页" % (len(manifest["books"]), checked, rerendered))
    for warning in warnings:
        print("  [提示] %s" % warning)
    if errors:
        print("  失败 %d 项：" % len(errors))
        for error in errors[:40]:
            print("   - %s" % error)
        return 1
    print("  结果：全部通过")
    return 0


if __name__ == "__main__":
    sys.exit(main())
