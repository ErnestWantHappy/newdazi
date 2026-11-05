"""只读核对每份课例来源目录的全部文件与已收录资源，输出覆盖与例外清单。

用途：确认实验包/HTML工程/压缩包/素材是否漏收，并列出未收录原因；
不读取压缩包内容，不改来源目录，也不打印任何凭据。
"""
import json
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases"
SKIP = {"__MACOSX", "node_modules", ".git", "__pycache__", ".vite"}
# 已在清单里被明确排除的临时文件与系统文件。
NOISE_SUFFIX = {".tmp", ".lnk", ".ds_store", ".ini"}


def main():
    manifest = json.loads((OUT / "catalog-local-reviewed.json").read_text(encoding="utf-8"))
    inventory = json.loads((OUT / "inventory.json").read_text(encoding="utf-8"))
    # 清单的 sourceRoot 是本地暂存目录，审计必须回到E盘原始来源。
    source = Path(inventory["sourceRoot"])
    if not source.is_dir():
        raise RuntimeError("原始来源目录不可读：" + str(source))
    included = {resource["source"] for case in manifest["cases"] for resource in case["resources"]}
    # 已收录资源来自本地暂存副本，来源路径需要还原成原始来源。
    included_original = set()
    for case in manifest["cases"]:
        for resource in case["resources"]:
            value = resource["source"]
            if value.startswith("prepared/"):
                included_original.add(case["sourceDirectory"] + "/" + value)
            else:
                included_original.add(value)

    extension_stats = defaultdict(lambda: {"count": 0, "bytes": 0, "included": 0})
    per_case = []
    for case in manifest["cases"]:
        directory = source / case["sourceDirectory"]
        if not directory.is_dir():
            continue
        missing = []
        for path in sorted(directory.rglob("*")):
            if not path.is_file() or SKIP.intersection(path.relative_to(directory).parts):
                continue
            suffix = path.suffix.lower()
            relative = path.relative_to(source).as_posix()
            size = path.stat().st_size
            stats = extension_stats[suffix or "(无扩展名)"]
            stats["count"] += 1
            stats["bytes"] += size
            is_included = relative in included_original
            if is_included:
                stats["included"] += 1
                continue
            if suffix in NOISE_SUFFIX:
                reason = "系统/临时文件"
            elif suffix in {".jpg", ".jpeg", ".png", ".webp"}:
                reason = "图片未在课例根目录或属插图素材"
            elif suffix in {".mp4", ".webm", ".mov", ".avi"}:
                reason = "视频层级超过3层或非本课演示"
            elif suffix in {".zip", ".rar", ".7z"}:
                reason = "压缩包待审核收录"
            elif suffix in {".html", ".htm", ".js", ".css", ".py", ".ipynb", ".json", ".md", ".txt", ".url"}:
                reason = "工程/源码文件待审核收录"
            elif suffix in {".doc", ".docx", ".ppt", ".pptx", ".pdf", ".xlsx", ".xls"}:
                reason = "文档层级超过3层或为附属材料"
            else:
                reason = "其他格式待审核"
            missing.append({"source": relative, "bytes": size, "reason": reason})
        per_case.append({"sourceDirectory": case["sourceDirectory"], "author": case["author"],
                         "included": sum(1 for resource in case["resources"]),
                         "notIncluded": len(missing), "notIncludedBytes": sum(item["bytes"] for item in missing),
                         "items": missing})

    report = {
        "sourceRoot": str(source),
        "extensionStats": {key: value for key, value in sorted(extension_stats.items(), key=lambda pair: -pair[1]["bytes"])},
        "cases": per_case,
    }
    (OUT / "asset-coverage.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")

    print("扩展名 总数 已收录 体积MiB")
    for suffix, stats in list(report["extensionStats"].items())[:14]:
        print(f"{suffix:10s} {stats['count']:5d} {stats['included']:5d} {stats['bytes']/1048576:10.1f}")
    heavy = sorted(per_case, key=lambda item: -item["notIncludedBytes"])[:8]
    for item in heavy:
        print(f"{item['notIncludedBytes']/1048576:8.1f}MiB 未收录 {item['notIncluded']:3d} 件 {item['sourceDirectory']}")
    kinds = Counter(Path(entry["source"]).suffix.lower() for case in per_case for entry in case["items"])
    print("未收录类型", dict(kinds.most_common(12)))


if __name__ == "__main__":
    main()
