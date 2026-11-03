#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""只读测量：找出「页框外有内容」的操作题预览 PDF，生成重算清单。

为什么需要它：v4 只对新的转换生效，旧 success 行的页图仍停在 page-jpeg-v3 目录。
全表重置会让几千份 OFFICE 一起重跑 LibreOffice，打满 soffice 进程池；
所以先只测量出真正越界的附件，只重算这些。

判定规则与 PracticalPageRenderer 一致：把内容包围盒（文字、路径、图像）与 PDF 的
CropBox 比较，内容超出就视为越界。本脚本用 PyMuPDF 近似测量，仅用于挑选候选；
最终以渲染器实际行为为准，重算后仍需人工抽查页图。

用法：
    # 1. 先导出清单（任一方式，得到 attachment_id 与 preview_path）
    #    在 130 上执行前检 SQL 的同类查询，导出 JSON 数组：
    #    [{"attachment_id":42835,"preview_path":"/profile/upload/.../x.pdf","file_kind":"OFFICE"}, ...]
    #
    # 2. 把 PDF 拉到本机同一目录结构下（或直接在服务器上跑本脚本）
    #
    # 3. 测量
    python -I scripts/measure_page_overflow.py --manifest manifest.json \
        --profile-root D:/profile-dump --out overflow.json

输出 overflow.json 含 attachment_ids 与可直接粘贴进 SQL 的 @ids 字符串。
本脚本只读，不修改任何 PDF、不连数据库、不写成绩。
"""

import argparse
import json
import os
import re
import sys

try:
    import fitz  # PyMuPDF
except ImportError:
    sys.exit("需要 PyMuPDF：pip install pymupdf")

# 与 PracticalPageRenderer 保持一致的容差与余量口径
TOLERANCE = 0.5
INK_MARGIN_RATIO = 0.01


def content_bounds(page):
    """近似测量一页内容的包围盒，使用页面用户空间坐标（原点左下，与 PDF 一致）。

    PyMuPDF 的坐标原点在左上，这里统一换算回 PDF 用户空间，避免与渲染器判断相反。
    """
    height = page.rect.height
    min_x = min_y = float("inf")
    max_x = max_y = float("-inf")

    def add(x, y):
        nonlocal min_x, min_y, max_x, max_y
        min_x = min(min_x, x)
        min_y = min(min_y, y)
        max_x = max(max_x, x)
        max_y = max(max_y, y)

    # 文字块
    for block in page.get_text("blocks"):
        x0, y0, x1, y1 = block[:4]
        add(x0, height - y1)   # 左上角 → 用户空间左下角
        add(x1, height - y0)

    # 矢量图形
    for drawing in page.get_drawings():
        rect = drawing.get("rect")
        if rect is None:
            continue
        add(rect.x0, height - rect.y1)
        add(rect.x1, height - rect.y0)

    # 位图：只统计页面可见范围内的依据是图像矩阵，取 bbox 近似
    for info in page.get_image_info():
        bbox = info.get("bbox")
        if not bbox:
            continue
        x0, y0, x1, y1 = bbox
        add(x0, height - y1)
        add(x1, height - y0)

    if min_x > max_x:
        return None
    return min_x, min_y, max_x, max_y


def overflows(page):
    """内容是否超出 CropBox；返回 (是否越界, 超出量, 边界)。"""
    bounds = content_bounds(page)
    if bounds is None:
        return False, 0.0, None
    crop = page.cropbox
    min_x, min_y, max_x, max_y = bounds

    # 允许 TOLERANCE 的浮点误差，与渲染器 inside() 一致，避免正常文档被误判
    overflow = max(
        crop.x0 - min_x,
        crop.y0 - min_y,
        max_x - crop.x1,
        max_y - crop.y1,
    )
    return overflow > TOLERANCE, max(0.0, overflow), {
        "content": [round(v, 2) for v in (min_x, min_y, max_x, max_y)],
        "crop": [round(v, 2) for v in (crop.x0, crop.y0, crop.x1, crop.y1)],
        "overflow_pt": round(max(0.0, overflow), 2),
    }


def normalize(path, root):
    """把 /profile/... 资源地址映射到本机拉取目录。"""
    relative = re.sub(r"^/profile", "", path)
    return os.path.join(root, relative.lstrip("/\\"))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--manifest", required=True, help="含 attachment_id/preview_path 的 JSON 数组")
    parser.add_argument("--profile-root", required=True, help="previewPath 对应的本机根目录")
    parser.add_argument("--out", required=True, help="输出 overflow.json")
    args = parser.parse_args()

    with open(args.manifest, encoding="utf-8") as handle:
        entries = json.load(handle)

    overflowing = []
    unreadable = []
    scanned = 0

    for entry in entries:
        path = entry.get("preview_path") or ""
        if not path.lower().endswith(".pdf"):
            continue
        local = normalize(path, args.profile_root)
        try:
            document = fitz.open(local)
        except Exception as error:  # 文件缺失或加密：记录并跳过，不算越界
            unreadable.append({"attachment_id": entry.get("attachment_id"), "error": str(error)})
            continue
        scanned += 1
        try:
            worst = 0.0
            detail = None
            pages = 0
            for index in range(document.page_count):
                hit, amount, info = overflows(document.load_page(index))
                if hit:
                    pages += 1
                    if amount > worst:
                        worst = amount
                        detail = {"page": index + 1, **(info or {})}
            if pages > 0:
                overflowing.append({
                    "attachment_id": entry.get("attachment_id"),
                    "file_kind": entry.get("file_kind"),
                    "sha256": entry.get("sha256"),
                    "preview_path": path,
                    "overflow_pages": pages,
                    "page_count": document.page_count,
                    "max_overflow_pt": round(worst, 2),
                    "worst": detail,
                })
        finally:
            document.close()

    ids = [str(item["attachment_id"]) for item in overflowing if item["attachment_id"] is not None]
    report = {
        "scanned": scanned,
        "overflowing": len(overflowing),
        "unreadable": len(unreadable),
        "attachment_ids": ids,
        "ids_literal": ",".join(ids) if ids else "0",
        "items": sorted(overflowing, key=lambda item: -item["max_overflow_pt"]),
        "unreadable_items": unreadable,
    }

    with open(args.out, "w", encoding="utf-8") as handle:
        json.dump(report, handle, ensure_ascii=False, indent=1)

    print(f"已测量 {scanned} 份预览 PDF，越界 {len(overflowing)} 份，无法读取 {len(unreadable)} 份")
    print("把下面这串填进 sql/page_overflow_reframe_20261014.sql 的 @ids：")
    print(report["ids_literal"])


if __name__ == "__main__":
    main()
