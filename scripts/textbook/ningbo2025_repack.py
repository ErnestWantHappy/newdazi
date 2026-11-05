"""补齐体量很小的配套材料包（原先 <100KB 未成包），并把新包并入清单与发布目录。

只处理 EXCLUDED_NOT_PACKAGED 的文件；不重新分类、不改动已有资源。
"""
import hashlib
import json
import sys
import zipfile
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases-2025"
STAGE = OUT / "stage"
TEXTBOOK_CASES = ROOT / "output/textbook-pack-20261029/pages/cases"
sys.path.insert(0, str(ROOT / "scripts/textbook"))
from ningbo2025_prepare import digest, requirement_text, summarize_package  # noqa: E402


def main():
    catalog = json.loads((OUT / "catalog-reviewed-2025.json").read_text(encoding="utf-8"))
    decisions = json.loads((OUT / "resource-decisions.json").read_text(encoding="utf-8"))
    archives = json.loads((OUT / "archive-extract.json").read_text(encoding="utf-8"))
    inventory = json.loads((OUT / "source-inventory.json").read_text(encoding="utf-8"))
    leftovers = defaultdict(list)
    for item in decisions:
        if item["decision"] == "EXCLUDED_NOT_PACKAGED":
            leftovers[item["case"]].append(item["path"])

    bases = {}
    for case in catalog["cases"]:
        name = case["source"]
        records = [item for item in inventory["files"] if item["caseDirectory"] == name]
        if records and all(item.get("archiveEntry") for item in records):
            records = []
        if records:
            bases[name] = ROOT / "x"  # 占位，下面用相对路径拼
        else:
            record = next((item for item in archives if item["caseDirectory"] == name), None)
            if record:
                bases[name] = ROOT / "output/textbook-cases-2025/work/extracted" / Path(record["workDirectory"]).name
    source_root = Path(inventory["sourceRoot"])

    added = []
    for case in catalog["cases"]:
        name = case["source"]
        if case.get("status", "PUBLISHED") != "PUBLISHED" or not leftovers.get(name):
            continue
        if any(res["kind"] == "package" for res in case["resources"]):
            continue
        base = bases.get(name)
        if base is None:
            continue
        files = []
        for relative in leftovers[name]:
            path = (source_root / name / relative) if base == ROOT / "x" else (base / relative)
            if path.is_file():
                files.append((path, relative))
        if not files:
            continue
        archive = STAGE / "packages" / (hashlib.sha256(name.encode("utf-8")).hexdigest()[:16] + ".zip")
        description = "本课配套材料包，含" + summarize_package([item[1] for item in files]) + "。"
        requirements = requirement_text([item[1] for item in files])
        guide = ("%s\n作者：%s\n\n内容：%s\n使用条件：%s\n\n说明：本包按作者原始目录结构整理，"
                 "未收录课堂实录、学生个人作品与超过 100MB 的视频。\n"
                 % (case["title"], case["author"], description, requirements))
        archive.parent.mkdir(parents=True, exist_ok=True)
        with zipfile.ZipFile(archive, "w", zipfile.ZIP_DEFLATED, compresslevel=6) as package:
            for path, relative in sorted(files, key=lambda item: item[1]):
                package.write(path, relative)
            package.writestr("使用说明_平台版.txt", guide.encode("utf-8"))
        checksum = digest(archive)
        case["resources"].append({
            "source": "", "category": "TOOL", "title": "配套材料包",
            "filename": case["title"][:60] + "_配套材料包.zip",
            "kind": "package", "bytes": archive.stat().st_size, "sha256": checksum,
            "description": description, "requirements": requirements, "path": str(archive)})
        case["package"] = True
        target = TEXTBOOK_CASES / "packages" / archive.name
        target.parent.mkdir(parents=True, exist_ok=True)
        if not target.exists():
            import shutil
            shutil.copyfile(archive, target)
        added.append({"case": name, "package": archive.name, "bytes": archive.stat().st_size,
                      "files": len(files)})

    for case in catalog["cases"]:
        for resource in case.get("resources", []):
            if resource.get("kind") == "package" and not resource.get("original_path"):
                resource["original_path"] = "packages/" + Path(resource["path"]).name
                resource["stage_path"] = "packages/" + Path(resource["path"]).name
    (OUT / "catalog-reviewed-2025.json").write_text(json.dumps(catalog, ensure_ascii=False, indent=2),
                                                    encoding="utf-8")
    (OUT / "repack-result.json").write_text(json.dumps(added, ensure_ascii=False, indent=2), encoding="utf-8")
    print("补齐 %d 份配套包，共 %d 个文件" % (len(added), sum(item["files"] for item in added)))


if __name__ == "__main__":
    main()
