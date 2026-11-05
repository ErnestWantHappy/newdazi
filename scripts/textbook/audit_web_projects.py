"""只读盘点各课例所有HTML/HTM入口，包含具名网页，供按目录打包决策。

输出：每个工程所属课例、相对路径、文件数、体积、是否已是已收录资源、是否含依赖/数据目录。
"""
import json
from pathlib import Path

ROOT = Path(r"d:/dmwprogram/newdazipingtai/newdazi")
OUT = ROOT / "output/textbook-cases"
SOURCE = Path(json.loads((OUT / "inventory.json").read_text(encoding="utf-8"))["sourceRoot"])
MANIFEST = json.loads((OUT / "catalog-local-reviewed.json").read_text(encoding="utf-8"))
NOISE = {"node_modules", ".git", "__pycache__", "venv", ".venv", "runtime", "__MACOSX", ".idea", ".vscode", "test"}
LIMITS = {"files": 400, "bytes": 60 * 1048576}


def main():
    included_paths = {}
    for case in MANIFEST["cases"]:
        directory = case["sourceDirectory"]
        for resource in case["resources"]:
            included_paths.setdefault(directory, set()).add(resource["source"].split("/")[0])

    projects = []
    folders = sorted({path.parent for path in SOURCE.rglob("*") if path.is_file() and path.suffix.lower() in {".html", ".htm"}})
    for folder in folders:
        if any(part in NOISE for part in folder.relative_to(SOURCE).parts):
            continue
        relative = folder.relative_to(SOURCE).as_posix()
        parts = relative.split("/")
        case_dir = next((case["sourceDirectory"] for case in MANIFEST["cases"] if relative == case["sourceDirectory"] or relative.startswith(case["sourceDirectory"] + "/")), None)
        if case_dir is None:
            continue
        files = [path for path in folder.rglob("*") if path.is_file() and not any(p in NOISE for p in path.relative_to(folder).parts)]
        size = sum(path.stat().st_size for path in files)
        projects.append({
            "case": case_dir,
            "project": relative,
            "name": folder.name,
            "depth": len(parts) - 3,
            "files": len(files),
            "bytes": size,
            "hasEntry": (folder / "index.html").is_file(),
            "entries": [path.name for path in folder.iterdir() if path.is_file() and path.suffix.lower() in {".html", ".htm"}],
            "nested": sorted({path.relative_to(folder).parts[0] for path in files if len(path.relative_to(folder).parts) > 1})[:6],
            "alreadyIncluded": folder.name in included_paths.get(case_dir, set())
            or any(part in included_paths.get(case_dir, set()) for part in parts[3:4]),
            "overLimit": len(files) > LIMITS["files"] or size > LIMITS["bytes"],
        })

    projects.sort(key=lambda item: (item["case"], item["project"]))
    (OUT / "web-projects.json").write_text(json.dumps(projects, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"共 {len(projects)} 个网页工程，涉及 {len({item['case'] for item in projects})} 个课例")
    for item in projects:
        flag = "已收录" if item["alreadyIncluded"] else ("超限" if item["overLimit"] else "待定")
        print(f"[{flag}] {item['files']:>4}文件 {item['bytes']/1048576:>7.1f}MiB {item['project']}")


if __name__ == "__main__":
    main()
