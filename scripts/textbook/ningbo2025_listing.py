"""2025宁波课例批次：把每个候选课例的文件树导出为人工审核用的紧凑文本。

输出 output/textbook-cases-2025/review-listing.txt，含：
- 候选目录、文件总数与字节；
- 文档/HTML/音视频/压缩包逐条列出，图片过多时按目录汇总；
- 运行库与工程依赖只统计数量，逐条明细留在 source-inventory.json。
"""
import json
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases-2025"
DOCUMENTS = {".docx", ".doc", ".pdf", ".pptx", ".ppt", ".ppsx", ".pptm", ".wps", ".xlsx",
             ".html", ".htm", ".zip", ".rar", ".7z", ".sb3", ".csv", ".txt", ".url",
             ".mp4", ".wmv", ".mov", ".mp3", ".wav"}
IMAGES = {".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg", ".psd", ".drawio", ".enbx"}
RUNTIME = {".dll", ".pyd", ".pth", ".pyc", ".lib", ".exe", ".cat", ".tm", ".enc", ".tcl",
           ".msg", ".pdiparams", ".pdmodel", ".ib", ".hex", ".vc", ".mp", ".nmake", "._pth",
           ".py", ".js", ".css", ".woff", ".woff2", ".ttf", ".eot", ".sh", ".bat", ".json",
           ".info", ".terms", ".hd", ".fprg", ".pos", ".c", ".cpp", ".h", ".swf"}


def main():
    data = json.loads((OUT / "source-inventory.json").read_text(encoding="utf-8"))
    extracted = json.loads((OUT / "archive-extract.json").read_text(encoding="utf-8"))
    by_case = {}
    for item in data["files"]:
        by_case.setdefault(item["caseDirectory"], []).append(item)
    lines = []
    for case_name in sorted(by_case):
        files = by_case[case_name]
        if case_name == "(汇总表)":
            lines.append("### " + case_name + " files=%d" % len(files))
            continue
        runtime = Counter(item["suffix"] for item in files if item["suffix"] in RUNTIME)
        documents = [item for item in files if item["suffix"] in DOCUMENTS]
        images = [item for item in files if item["suffix"] in IMAGES]
        lines.append("### %s | files=%d bytes=%d | 运行库%d | 图片%d" % (
            case_name, len(files), sum(item["bytes"] for item in files), sum(runtime.values()), len(images)))
        for item in sorted(documents, key=lambda entry: entry["relativePath"]):
            relative = item["relativePath"]
            prefix = case_name + "/"
            short = relative[len(prefix):] if relative.startswith(prefix) else relative
            lines.append("   %s | %d" % (short, item["bytes"]))
        if len(images) <= 12:
            for item in sorted(images, key=lambda entry: entry["relativePath"]):
                relative = item["relativePath"]
                prefix = case_name + "/"
                short = relative[len(prefix):] if relative.startswith(prefix) else relative
                lines.append("   %s | %d" % (short, item["bytes"]))
        else:
            groups = defaultdict(list)
            for item in images:
                relative = item["relativePath"]
                prefix = case_name + "/"
                short = relative[len(prefix):] if relative.startswith(prefix) else relative
                groups["/".join(short.split("/")[:-1])].append(item["bytes"])
            for group in sorted(groups):
                lines.append("   [图片] %s/ ×%d 共%d字节" % (group or ".", len(groups[group]), sum(groups[group])))
        if runtime:
            lines.append("   (运行库/依赖) " + ", ".join("%s×%d" % (k, v) for k, v in runtime.most_common(8)))
    for record in extracted:
        if record["caseDirectory"] in by_case:
            continue
        lines.append("### [压缩包] %s | 解出files=%d bytes=%d" % (
            record["relativePath"], record["memberCount"], record["memberBytes"]))
        for member in sorted(record["members"], key=lambda entry: entry["name"]):
            suffix = Path(member["name"]).suffix.lower()
            if suffix in DOCUMENTS:
                lines.append("   %s | %d" % (member["name"], member["bytes"]))
    (OUT / "review-listing.txt").write_text("\n".join(lines), encoding="utf-8")
    print("candidates", len(by_case), "lines", len(lines))


if __name__ == "__main__":
    main()
