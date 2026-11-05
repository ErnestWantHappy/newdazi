"""补收所有具名HTML/HTM及配套文件，保留共享模型配置与原始目录结构。"""
import html
import json
import re
import zipfile
from pathlib import Path
from urllib.parse import quote, unquote, urlsplit
from import_cases import digest

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases"
SKIP_PARTS = {"node_modules", ".git", "__MACOSX", "venv", ".venv", "runtime", "__pycache__", ".workbuddy", ".wpp_backup", ".pytest_cache", ".vite"}
SKIP_EXTENSIONS = {".doc", ".docx", ".ppt", ".pptx", ".pdf", ".xls", ".xlsx", ".zip", ".rar", ".tar", ".7z", ".exe", ".msi", ".db", ".sqlite", ".sqlite3", ".pyc", ".log", ".err", ".tmp", ".pem", ".key", ".pfx", ".lnk"}
STATE_NAMES = {"submissions.json", "store.json", "sessions.json", "votes.json"}
SHARED_KEY_NOTE = "部分工具包含原作者自愿共享的API Key；若已被删除、停用或额度用尽，可按原工具配置位置替换为自己的。平台未调用共享Key验证有效性。"


def main():
    manifest_path = OUT / "catalog-local-reviewed.json"
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    source = Path(json.loads((OUT / "inventory.json").read_text(encoding="utf-8"))["sourceRoot"])
    web = json.loads((OUT / "web-packages.json").read_text(encoding="utf-8"))
    decisions = json.loads((OUT / "package-decisions.json").read_text(encoding="utf-8"))
    covered = [entry["origin"].rstrip("/") for entry in web if entry["kind"] == "dir"]
    covered += [entry["source"].rstrip("/") for entry in decisions["included"] if not entry["target"].startswith("packages/lesson-web/") and (source / entry["source"]).is_dir()]
    destination = Path(manifest["sourceRoot"]) / "packages/lesson-web"
    destination.mkdir(parents=True, exist_ok=True)
    reports = []
    for case in manifest["cases"]:
        if not case.get("reviewed"):
            continue
        base = source / case["sourceDirectory"]

        def eligible(path):
            relative = path.relative_to(base)
            original = path.relative_to(source).as_posix()
            return (path.is_file() and not SKIP_PARTS.intersection(relative.parts)
                    and not any(original == root or original.startswith(root + "/") for root in covered))

        pages = sorted(path for path in base.rglob("*") if path.suffix.lower() in {".html", ".htm"} and eligible(path))
        if not pages:
            continue
        files = {}
        omitted = []
        for path in sorted(base.rglob("*")):
            if not eligible(path):
                continue
            relative = path.relative_to(base).as_posix()
            if (path.suffix.lower() in SKIP_EXTENSIONS or path.name in STATE_NAMES
                    or re.search(r"\.(?:bak\d*|db-wal|db-shm)$", path.name, re.I)
                    or "uploads" in path.relative_to(base).parts
                    or relative.startswith("学生评价平台/data/")
                    or path.name in {".DS_Store", "Thumbs.db", "desktop.ini"}):
                omitted.append(relative)
                continue
            files[relative] = path
        # 明确引用的本地素材必须保留；个人数据表/历史记录不自动带入。
        missing = []
        for page in pages:
            content = page.read_text(encoding="utf-8-sig")
            for reference in re.findall(r'(?:src|href)\s*=\s*["\']([^"\']+)["\']', content, re.I):
                uri = urlsplit(reference)
                if uri.scheme or uri.netloc or not uri.path or uri.path.startswith("/") or any(c in uri.path for c in "${}"):
                    continue
                target = (page.parent / unquote(uri.path)).resolve()
                if not target.is_relative_to(base.resolve()):
                    missing.append({"page": page.relative_to(base).as_posix(), "reason": "引用超出本课目录"})
                    continue
                if target.is_file():
                    relative = target.relative_to(base).as_posix()
                    if target.suffix.lower() not in SKIP_EXTENSIONS and eligible(target):
                        files[relative] = target
                elif Path(uri.path).suffix.lower() in {".js", ".css", ".png", ".jpg", ".jpeg", ".gif", ".svg", ".mp3", ".mp4", ".webp", ".woff", ".woff2", ".ttf", ".wasm", ".json", ".下载"}:
                    missing.append({"page": page.relative_to(base).as_posix(), "reference": uri.path, "reason": "来源中缺失"})
        projects = []
        roots = set()
        for page in pages:
            for parent in [page.parent, *page.parent.parents]:
                if not parent.is_relative_to(base):
                    break
                if any((parent / name).is_file() for name in ("server.js", "server.mjs", "server.py", "package.json")):
                    roots.add(parent)
                    break
        for parent in sorted(roots):
            directory = parent.relative_to(base).as_posix()
            package_path = parent / "package.json"
            if package_path.is_file():
                package = json.loads(package_path.read_text(encoding="utf-8-sig"))
                scripts = package.get("scripts", {})
                command = "npm install，然后 npm run dev" if "dev" in scripts else "npm install，然后 npm start" if "start" in scripts else "按原README安装和构建"
                projects.append(f"进入 {directory}：{command}（需要Node.js，首次安装依赖需联网）。")
            elif (parent / "server.js").is_file():
                projects.append(f"进入 {directory}：node server.js（需要Node.js）。访问启动窗口给出的地址，不直接双击教师端/学生端页面。")
            elif (parent / "server.mjs").is_file():
                projects.append(f"进入 {directory}：node server.mjs（需要Node.js）。")
            else:
                projects.append(f"进入 {directory}：python server.py（需要Python，其他条件见原说明）。")
        names = [page.relative_to(base).as_posix() for page in pages]
        overrides = {}
        repairs = []
        if case["bookCode"] == "xxkj-5-1-2026" and case["sectionKey"] == "lesson-18":
            library = ROOT / "RuoYi-Vue3/node_modules/xlsx/dist/xlsx.full.min.js"
            license_file = ROOT / "RuoYi-Vue3/node_modules/xlsx/LICENSE"
            if not library.is_file() or not license_file.is_file():
                raise RuntimeError("缺少既有XLSX库或许可文件，不能生成完整网页包")
            files["在线神经网络实验室_files/xlsx.full.min.js"] = library
            files["第三方许可_XLSX.txt"] = license_file
            original = (base / "在线神经网络实验室.html").read_text(encoding="utf-8-sig")
            overrides["在线神经网络实验室.html"] = original.replace("xlsx.full.min.js.下载", "xlsx.full.min.js").encode("utf-8")
            missing = [item for item in missing if "xlsx.full.min.js" not in item.get("reference", "")]
            repairs.append("补齐原网页缺失的XLSX脚本，复用本项目已有版本并附许可；修正发布副本脚本引用")
        if case["bookCode"] == "xxkj-6-0-2026" and case["sectionKey"] == "lesson-15":
            original_image = base / "教学材料/学生文件/images/10.png"
            if original_image.is_file():
                files["教学材料/images/10.png"] = original_image
                missing = [item for item in missing if item.get("reference") != "images/10.png"]
                repairs.append("从同课学生素材补齐教师演示页引用的太阳能小车图片")
        description = f"本课配套网页学单、演示和实验工具，共{len(pages)}个网页入口。包含：" + "、".join(path.stem for path in pages[:6]) + ("等。" if len(pages) > 6 else "。")
        requirements = ("本包包含本地服务工程。" + " ".join(projects) if projects else "静态网页可解压后双击打开，使用Chrome或Edge。部分页面配合同课实验工具或课堂收集服务使用，具体见包内原说明。")
        requirements += " " + SHARED_KEY_NOTE
        readme = (f"{case['title']}配套网页与实验材料\n\n{description}\n\n使用方式\n{requirements}\n\n"
                  "原目录结构已保留。原运行记录、缓存和安装包不随本包附带。涉及课堂记录的收集地址，请按自己的课堂需要核对和配置。\n\n网页入口\n" + "\n".join(names))
        if missing:
            readme += "\n\n来源文件本身存在以下未找到的引用，相关步骤需按原工具说明补齐\n" + "\n".join(f"{item['page']}：{item.get('reference',item['reason'])}" for item in missing)
        if repairs:
            readme += "\n\n平台整理记录\n" + "\n".join(repairs)
        target = destination / (case["sourceKey"] + ".zip")
        temporary = target.with_suffix(".part")
        with zipfile.ZipFile(temporary, "w", zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
            for relative, path in files.items():
                if relative in overrides:
                    archive.writestr(relative, overrides[relative])
                else:
                    archive.write(path, relative)
            archive.writestr("使用说明_平台版.txt", readme)
            links = "\n".join(f'<li><a href="{html.escape(quote(name, safe="/"))}">{html.escape(name)}</a></li>' for name in names)
            archive.writestr("网页入口清单.html", f'<!doctype html><meta charset="utf-8"><title>本课网页入口</title><h1>{html.escape(case["title"])}</h1><p>{html.escape(requirements)}</p><ul>{links}</ul>')
        temporary.replace(target)
        key = target.relative_to(Path(manifest["sourceRoot"])).as_posix()
        volume = case["sourceDirectory"].split("/")[1] if case["bookCode"].split("-")[1] != "9" else "九年级全一册"
        filename = re.sub(r'[<>:"/\\|?*]', "_", f"{volume}_第{case['sectionKey'].split('-')[-1]}课_{case['title']}_{case['author']}_配套网页.zip")
        resource = {"source": key, "sha256": digest(target), "filename": filename, "preview": "", "category": "TOOL" if projects else "MATERIAL",
                    "title": "配套网页与实验材料", "description": description, "requirements": requirements}
        case["resources"] = [item for item in case["resources"] if item["source"] != key] + [resource]
        reports.append({"sourceDirectory": case["sourceDirectory"], "sourceKey": case["sourceKey"], "package": key, "pages": names,
                        "files": list(files), "omitted": omitted, "missingReferences": missing, "projects": projects, "repairs": repairs,
                        "sha256": resource["sha256"], "bytes": target.stat().st_size})
        print(f"补收 {case['title']} {case['author']}：{len(pages)}网页/{len(files)}文件，{target.stat().st_size//1024}KiB")
    manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")
    (OUT / "remaining-web-resources.json").write_text(json.dumps(reports, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"共补收{len(reports)}份配套材料，{sum(len(item['pages']) for item in reports)}个网页入口，尚未写库")


if __name__ == "__main__":
    main()
