"""生成本批资源闭环表（每个候选目录的去向与理由）。"""
import json
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases-2025"
DOC = ROOT / "contexts/textbook-cases-2025"


def main():
    catalog = json.loads((OUT / "catalog-reviewed-2025.json").read_text(encoding="utf-8"))
    decisions = json.loads((OUT / "resource-decisions.json").read_text(encoding="utf-8"))
    videos = json.loads((OUT / "video-decisions.json").read_text(encoding="utf-8"))
    by_case = {}
    for item in decisions:
        by_case.setdefault(item["case"], Counter())[item["decision"]] += 1
    video_case = {}
    for item in videos:
        video_case.setdefault(item["case"], []).append(item)

    lines = ["# 2025 宁波优秀课例：资源闭环表", "",
             "> 每个候选目录/压缩包的去向、教材课次与理由。原始来源 2172 个文件、19,941,483,508 字节；",
             "> 本表不把 2172 当发布资源数。发布资源以 `catalog-reviewed-2025.json` 为准。", ""]
    published = [case for case in catalog["cases"] if case.get("status", "PUBLISHED") == "PUBLISHED"]
    pending = [case for case in catalog["cases"] if case.get("status") == "PENDING"]
    duplicate = [case for case in catalog["cases"] if case.get("status") == "DUPLICATE_EXISTING"]
    total_resources = sum(len(case["resources"]) for case in published)
    lines.append("- 候选来源：%d 个（目录 + 册目录下压缩包）" % len(catalog["cases"]))
    lines.append("- 已上架课例：%d 份，资源 %d 项（含 %d 份配套材料包）" % (
        len(published), total_resources,
        sum(1 for case in published for res in case["resources"] if res["kind"] == "package")))
    lines.append("- 待确认（PENDING）：%d 份；重复来源（DUPLICATE_EXISTING）：%d 份" % (len(pending), len(duplicate)))
    lines.append("")
    lines.append("## 已上架课例")
    lines.append("")
    lines.append("| # | 来源目录 | 教材 | 课次 | 课名 | 作者 | 学校 | 资源 | 配套包 | 排除/合并 |")
    lines.append("| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |")
    for index, case in enumerate(sorted(published, key=lambda item: (item["bookCode"], item["sectionKey"])), 1):
        counts = Counter(res["category"] for res in case["resources"])
        package = next((res for res in case["resources"] if res["kind"] == "package"), None)
        excluded = by_case.get(case["source"], Counter())
        skip = "、".join("%s×%d" % (key, value) for key, value in sorted(excluded.items())
                        if key.startswith("EXCLUDED") or key in ("DUPLICATE_FILE",))
        lines.append("| %d | %s | %s | %s | %s | %s | %s | %s | %s | %s |" % (
            index, case["source"], case["bookCode"], case["sectionKey"], case["title"], case["author"],
            case["school"],
            "、".join("%s%d" % (key, counts[key]) for key in ("DESIGN", "SLIDES", "WORKSHEET", "MATERIAL", "TOOL")
                     if counts[key]),
            ("%.1fMB" % (package["bytes"] / 1048576)) if package else "—",
            skip or "—"))
    lines.append("")
    lines.append("## 待确认（暂不上架）")
    lines.append("")
    for case in pending:
        lines.append("- **%s**｜%s｜%s：%s" % (case["author"], case["title"], case["source"], case.get("note", "")))
    lines.append("")
    lines.append("## 重复来源（不另建卡片）")
    lines.append("")
    for case in duplicate:
        lines.append("- **%s**｜%s：%s" % (case["author"], case["title"], case.get("note", "")))
    lines.append("")
    lines.append("## 视频逐条去向")
    lines.append("")
    lines.append("> 本批上限 100MB＝100,000,000 字节；课堂实录/教学录像一律不收录，不论大小。")
    lines.append("")
    for case in catalog["cases"]:
        items = video_case.get(case["source"], [])
        if not items:
            continue
        lines.append("### " + case["source"])
        for item in items:
            lines.append("- %s：%s（%.1fMB）%s" % (item["path"], item["decision"], item.get("bytes", 0) / 1048576,
                                                 "；" + item.get("evidence", "") if item.get("evidence") else ""))
        lines.append("")
    DOC.mkdir(parents=True, exist_ok=True)
    (DOC / "resource-closure.md").write_text("\n".join(lines), encoding="utf-8")
    print("闭环表行数", len(lines))


if __name__ == "__main__":
    main()
