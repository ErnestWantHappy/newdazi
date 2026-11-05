"""合并生成 package-decisions.json：已登记实验包 + 本轮网页/压缩包 + 仍未收录清单。

只读脚本，不打包不写库；用于让决策清单与实际收录保持一致。
"""
import importlib.util
import json
from pathlib import Path

ROOT = Path(r"d:/dmwprogram/newdazipingtai/newdazi")
OUT = ROOT / "output/textbook-cases"


def load_module():
    spec = importlib.util.spec_from_file_location("add_case_packages", ROOT / "scripts/textbook/add_case_packages.py")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def main():
    decisions_path = OUT / "package-decisions.json"
    current = json.loads(decisions_path.read_text(encoding="utf-8"))
    web = json.loads((OUT / "web-packages.json").read_text(encoding="utf-8"))
    module = load_module()

    included = {item["target"]: item for item in current["included"]}
    for item in web:
        included[item["target"]] = {
            "source": item["origin"], "target": item["target"], "bytes": item["bytes"],
            "sha256": item["sha256"], "entries": item["files"], "title": item["target"].split("/")[-1].replace(".zip", ""),
        }

    supplemental_path = OUT / "remaining-web-resources.json"
    supplemental = json.loads(supplemental_path.read_text(encoding="utf-8")) if supplemental_path.exists() else []
    for item in supplemental:
        included[item["package"]] = {"source": item["sourceDirectory"], "target": item["package"], "bytes": item["bytes"],
            "sha256": item["sha256"], "entries": len(item["files"]), "title": "配套网页与实验材料"}
    origins = {item["origin"] for item in web}
    covered_files = {item["sourceDirectory"] + "/" + path for item in supplemental for path in item["files"]}
    quarantine = {item["source"]: item for item in [*module.QUARANTINE, *current["quarantine"]]}
    quarantine = [item for name, item in quarantine.items() if name not in origins and name not in covered_files
                  and not any(path.startswith(name.rstrip("/") + "/") for path in covered_files)]

    merged = {"included": list(included.values()), "quarantine": quarantine}
    decisions_path.write_text(json.dumps(merged, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"已收录 {len(included)} 项，隔离/不收录 {len(quarantine)} 项")


if __name__ == "__main__":
    main()
