"""把超过预览上限的PDF预览压成轻量图页版本，原始课件与来源文件保持只读。

背景：LibreOffice 把高分辨率截图逐页重复嵌入，个别课件导出的 PDF 超过 100MiB，
后端为避免内存与耗时失控拒绝预览。逐页重采样成 JPEG 页面后体积降到几 MB，
前端本来就是按页取图，观感一致；原件仍是原 PPTX/PDF，不受影响。
"""
import argparse
import hashlib
import json
import os
from pathlib import Path

import fitz

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases"
PREVIEWS = OUT / "previews"


def digest(path):
    result = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            result.update(chunk)
    return result.hexdigest()


def rasterize(source, destination, max_pixels, quality):
    """按页重采样为 JPEG 页面，返回页数。页数不一致就不替换，避免截断内容。"""
    with fitz.open(source) as document:
        pages = len(document)
        if not pages:
            raise RuntimeError("PDF为空")
        output = fitz.open()
        temporary = destination.with_name(destination.name + ".compressing")
        try:
            for index in range(pages):
                page = document[index]
                rect = page.rect
                if rect.width <= 0 or rect.height <= 0:
                    raise RuntimeError("页面尺寸异常")
                # 只降不升：原本就小的页面保持分辨率，避免文字被插值放大。
                zoom = min(max_pixels / rect.width, max_pixels / rect.height, 2.5)
                zoom = max(zoom, 0.2)
                pixmap = page.get_pixmap(matrix=fitz.Matrix(zoom, zoom), alpha=False)
                image = pixmap.tobytes("jpg", jpg_quality=quality)
                target = output.new_page(width=rect.width, height=rect.height)
                target.insert_image(rect, stream=image)
            output.save(temporary, garbage=4, deflate=True)
        finally:
            output.close()
    with fitz.open(temporary) as check:
        if len(check) != pages:
            temporary.unlink(missing_ok=True)
            raise RuntimeError("重采样页数不一致")
    os.replace(temporary, destination)
    return pages


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--manifest", type=Path, default=OUT / "catalog-local-reviewed.json")
    parser.add_argument("--threshold-mib", type=float, default=100.0)
    parser.add_argument("--max-pixels", type=int, default=1920)
    parser.add_argument("--quality", type=int, default=85)
    parser.add_argument("--report", type=Path, default=OUT / "preview-compression.json")
    args = parser.parse_args()

    threshold = int(args.threshold_mib * 1024 * 1024)
    data = json.loads(args.manifest.read_text(encoding="utf-8"))
    items = []
    for case in data["cases"]:
        for resource in case["resources"]:
            preview = resource.get("preview") or ""
            if not preview.lower().endswith(".pdf"):
                continue
            path = Path(preview)
            # 只处理工作副本，绝不改写 E 盘只读来源或原件。
            if not path.is_file() or PREVIEWS not in path.resolve().parents:
                continue
            before = path.stat().st_size
            record = {"case": case["title"], "author": case["author"], "source": resource["source"],
                      "preview": str(path), "beforeBytes": before, "beforeSha256": digest(path)}
            if before <= threshold:
                record.update(compressed=False, reason="未超过阈值")
                items.append(record)
                continue
            pages = rasterize(path, path, args.max_pixels, args.quality)
            after = path.stat().st_size
            record.update(compressed=True, pages=pages, afterBytes=after, afterSha256=digest(path))
            items.append(record)
            print(f"已压缩 {case['title']} {path.name} {before/1048576:.1f}→{after/1048576:.1f}MiB {pages}页", flush=True)

    report = {"thresholdMiB": args.threshold_mib, "maxPixels": args.max_pixels,
              "quality": args.quality, "items": items}
    args.report.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    changed = [item for item in items if item["compressed"]]
    print(f"共检查{len(items)}个PDF预览，压缩{len(changed)}个；原件与来源未改动")


if __name__ == "__main__":
    main()
