"""2025宁波课例批次：逐个 HTML 检查真实依赖与独立性，不只看扩展名。

- 读取每个 HTML 的文本前 400KB（教学页不会更大），提取：
  script src / link href / img src / iframe src / fetch / XMLHttpRequest / axios /
  import / localStorage / WebSocket / http(s) 绝对地址；
- 逐条判断同目录文件是否存在，输出 HTML_STANDALONE / WITH_ASSETS / SERVICE /
  SOURCE_PROJECT / UNCERTAIN 的初判与证据，供人工复核。
"""
import json
import re
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases-2025"
PATTERN = re.compile(r"""(?:src|href)\s*=\s*["']([^"']+)["']""", re.I)
EXTRA = {
    "fetch": re.compile(r"fetch\s*\(", re.I),
    "xhr": re.compile(r"XMLHttpRequest", re.I),
    "axios": re.compile(r"axios", re.I),
    "module": re.compile(r"type\s*=\s*[\"']module[\"']", re.I),
    "import": re.compile(r"^\s*import\s", re.M),
    "localStorage": re.compile(r"localStorage", re.I),
    "websocket": re.compile(r"WebSocket", re.I),
    "mqtt": re.compile(r"mqtt", re.I),
    "vue": re.compile(r"vue@|vue\.js|Vue\.createApp", re.I),
    "react": re.compile(r"react(-dom)?[./@]|ReactDOM", re.I),
}
LOCAL_SUFFIX = {".css", ".js", ".png", ".jpg", ".jpeg", ".gif", ".svg", ".webp", ".json",
                ".txt", ".csv", ".xlsx", ".mp3", ".mp4", ".wav", ".woff", ".woff2", ".ttf",
                ".html", ".htm", ".ico", ".xml", ".mjs", ".wasm"}


def is_remote(url):
    return url.startswith(("http://", "https://", "//", "data:", "blob:", "javascript:", "mailto:", "#"))


def main():
    inventory = json.loads((OUT / "source-inventory.json").read_text(encoding="utf-8"))
    extracted = json.loads((OUT / "archive-extract.json").read_text(encoding="utf-8"))
    source = Path(inventory["sourceRoot"])
    work = OUT / "work/extracted"
    results = []

    targets = []
    for item in inventory["files"]:
        if item["suffix"] in {".html", ".htm"}:
            targets.append((source / item["relativePath"], item["relativePath"], item["bytes"]))
    for record in extracted:
        base = work / Path(record["workDirectory"]).name
        for member in record["members"]:
            if member["name"].lower().endswith((".html", ".htm")):
                targets.append((base / member["name"], record["relativePath"] + "::" + member["name"], member["bytes"]))

    for path, label, size in targets:
        entry = {"path": label, "bytes": size, "remote": [], "local": [], "missing": [],
                 "flags": [], "category": "", "reason": ""}
        try:
            text = path.read_text(encoding="utf-8", errors="replace")[:400000]
        except Exception as error:
            entry["category"] = "HTML_UNCERTAIN"
            entry["reason"] = "读取失败 " + type(error).__name__
            results.append(entry)
            continue
        refs = set(PATTERN.findall(text))
        for url in sorted(refs):
            if is_remote(url):
                if not url.startswith(("data:", "#", "javascript:", "mailto:")):
                    entry["remote"].append(url)
                continue
            clean = url.split("?")[0].split("#")[0]
            if not clean:
                continue
            candidate = (path.parent / clean).resolve()
            if candidate.exists():
                entry["local"].append(clean)
            else:
                entry["missing"].append(clean)
        for name, pattern in EXTRA.items():
            if pattern.search(text):
                entry["flags"].append(name)
        blocking_missing = [item for item in entry["missing"]
                            if Path(item).suffix.lower() in LOCAL_SUFFIX or not Path(item).suffix]
        if path.parent.joinpath("package.json").exists() or "node_modules" in str(path):
            entry["category"] = "HTML_SOURCE_PROJECT"
            entry["reason"] = "存在 package.json 或 node_modules，属源码工程"
        elif entry["flags"] and set(entry["flags"]) & {"xhr", "axios", "websocket", "mqtt"}:
            entry["category"] = "HTML_SERVICE"
            entry["reason"] = "含接口/实时通信调用，需本地服务或外部接口"
        elif entry["missing"] or entry["local"]:
            entry["category"] = "HTML_WITH_ASSETS"
            entry["reason"] = "依赖同目录文件 %d 个，缺失 %d 个" % (len(entry["local"]), len(blocking_missing))
        elif entry["remote"]:
            entry["category"] = "HTML_UNCERTAIN"
            entry["reason"] = "仅依赖外部网络资源：%s" % ", ".join(entry["remote"][:3])
        else:
            entry["category"] = "HTML_STANDALONE"
            entry["reason"] = "无外部文件/网络依赖"
        results.append(entry)

    grouped = defaultdict(list)
    for entry in results:
        grouped[entry["category"]].append(entry)
    (OUT / "html-audit.json").write_text(json.dumps({"total": len(results), "items": results},
                                                    ensure_ascii=False, indent=2), encoding="utf-8")
    lines = ["%s=%d" % (key, len(value)) for key, value in sorted(grouped.items())]
    lines.append("")
    for entry in results:
        lines.append("%s | %s | %d | %s | remote=%d local=%d missing=%d flags=%s" % (
            entry["category"], entry["path"], entry["bytes"], entry["reason"], len(entry["remote"]),
            len(entry["local"]), len(entry["missing"]), ",".join(entry["flags"])))
    (OUT / "html-audit.txt").write_text("\n".join(lines), encoding="utf-8")
    print("html", len(results), lines[0])


if __name__ == "__main__":
    main()
