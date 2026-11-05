"""逐包检查入口、依赖和共享模型配置，生成每份作者课例唯一配套包。

只读取审核后的发布副本，不改原始来源，不执行工具或调用共享 Key。
报告只记录配置所在文件，不输出配置值。默认只审计，--build 才生成新清单。
"""
import argparse
from collections import Counter
import datetime
import html
import json
import re
import shutil
import zipfile
from pathlib import Path, PurePosixPath
from urllib.parse import quote
from import_cases import digest

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases"
BASE = OUT / "catalog-local-reviewed.json"
SKIP = {".vite", "__pycache__", ".git", "node_modules", ".DS_Store"}
RUNTIME_PARTS = {"runtime", "Lib", "site-packages", "pip", "setuptools", "venv", ".venv", "Include", "Scripts", "_vendor"}
GENERATED = {"使用说明_平台版.txt", "网页入口清单.html", "打开材料目录.html"}
CODE = {".html", ".htm", ".js", ".ts", ".tsx", ".jsx", ".py", ".json", ".env", ".bat"}
SHARED = re.compile(r"(?:sk-[A-Za-z0-9_-]{20,}|pat_[A-Za-z0-9_-]{25,}|pat\.[A-Za-z0-9_.-]{25,})")
KEY_SLOT = re.compile(r"(?:DEEPSEEK_API_KEY|COZE_API_TOKEN|AI_API_KEY|OPENAI_API_KEY|apiKey\s*[:=]\s*[`'\"](?:\s*|sk-[a-zA-Z0-9_-]*|YOUR_[A-Za-z0-9_]+)[`'\"])", re.I)


def is_runtime_file(name):
    return bool(RUNTIME_PARTS.intersection(PurePosixPath(name).parts))


def is_meaningful_page(name):
    p = Path(name)
    if p.suffix.lower() not in {".html", ".htm"} or p.name in GENERATED:
        return False
    if is_runtime_file(name):
        return False
    # 排除编译/缓存哈希页面（如 0d2eb3878d.html）和内部模板
    if re.match(r"^[0-9a-fA-F]{8,}$", p.stem):
        return False
    if "templates" in PurePosixPath(name).parts:
        return False
    return True


def check_network(name, text):
    if is_runtime_file(name):
        return False
    # 剔除 XML 命名空间、微格式 Schema、localhost 和注释
    cleaned = re.sub(r"https?://(?:www\.)?w3\.org[^\s\"'>]*", "", text)
    cleaned = re.sub(r"https?://schemas\.[^\s\"'>]*", "", cleaned)
    cleaned = re.sub(r"https?://(?:localhost|127\.0\.0\.1)[^\s\"'>]*", "", cleaned)
    cleaned = re.sub(r"<!--[\s\S]*?-->", "", cleaned)
    # 不能按 // 截行，https:// 本身就包含双斜线；优先识别真实资源引用和调用。
    return bool(re.search(r'''(?:src|href)\s*=\s*["']https?://[^"']+''', cleaned, re.I)
                or re.search(r'''(?:fetch|axios\.(?:get|post)|requests\.(?:get|post))\s*\(\s*["'`]https?://''', cleaned)
                or re.search(r'''(?:url|baseURL|endpoint|API_URL)\s*[:=]\s*["'`]https?://''', cleaned, re.I))


def check_local_fetch(name, text):
    if is_runtime_file(name):
        return False
    # 支持反引号、引号相对路径及 API 路由调用
    return bool(re.search(r"fetch\(\s*[`'\"](?:\.?\.?/|/[^/]|api/|[^h`'\"]*\.(?:json|csv|txt))", text)
                or re.search(r"axios\.(?:get|post|request)\(\s*[`'\"](?:\.?\.?/|/[^/]|api/)", text)
                or re.search(r"\$\.(?:ajax|get|getJSON)\(\s*[`'\"](?:\.?\.?/|/[^/]|api/)", text))


def inspect(path):
    with zipfile.ZipFile(path) as archive:
        all_entries = [entry for entry in archive.infolist() if not entry.is_dir()
                       and not SKIP.intersection(PurePosixPath(entry.filename).parts)]
        names = [entry.filename for entry in all_entries]
        has_runtime = any(is_runtime_file(name) for name in names)

        # 教师代码与资料排除运行库和 vendored 依赖
        teacher_entries = [entry for entry in all_entries if not is_runtime_file(entry.filename)]
        teacher_names = [entry.filename for entry in teacher_entries]

        pages = [name for name in teacher_names if is_meaningful_page(name)]
        counts = Counter(Path(name).suffix.lower() for name in teacher_names)
        texts, projects, shared, key_slots, network, local_fetch, camera = {}, [], [], [], [], [], []

        for entry in teacher_entries:
            name = entry.filename
            if (Path(name).suffix.lower() not in CODE and Path(name).name != ".env") or entry.file_size > 2_000_000:
                continue
            if name.endswith(".min.js") or Path(name).name in GENERATED:
                continue
            try:
                text = archive.read(entry).decode("utf-8-sig")
            except UnicodeDecodeError:
                continue
            texts[name] = text
            if SHARED.search(text): shared.append(name)
            if KEY_SLOT.search(text): key_slots.append(name)
            if check_network(name, text): network.append(name)
            if check_local_fetch(name, text): local_fetch.append(name)
            if "getUserMedia" in text: camera.append(name)
            if Path(name).name == "package.json":
                try:
                    spec = json.loads(text)
                    scripts = spec.get("scripts", {})
                    script = next((key for key in ("dev", "start", "serve") if key in scripts), None)
                    if script:
                        projects.append({"directory": str(PurePosixPath(name).parent), "script": script,
                                         "dependencies": bool(spec.get("dependencies") or spec.get("devDependencies"))})
                except Exception:
                    pass

        return {"files": names, "teacherFiles": teacher_names, "pages": pages,
                "counts": dict(counts), "projects": projects, "hasRuntime": has_runtime,
                "sharedKeyFiles": shared, "keySlotFiles": key_slots, "networkFiles": network,
                "localFetchFiles": local_fetch, "cameraFiles": camera, "texts": texts}


def describe(audit):
    parts = []
    if audit["pages"]:
        titles = list(dict.fromkeys(Path(name).stem for name in audit["pages"]))
        shown = '、'.join(titles[:4])
        parts.append(f"网页{len(audit['pages'])}个（{shown}{'等' if len(titles) > 4 else ''}）")
    counts = audit["counts"]
    if audit.get("hasRuntime"):
        parts.append("内置运行环境与启动脚本")
    for extensions, label in [([".py"], "Python脚本"), ([".png", ".jpg", ".jpeg", ".svg", ".webp"], "图片"),
                              ([".mp4", ".webm"], "视频"), ([".xlsx", ".xls", ".csv"], "数据表")]:
        number = sum(counts.get(ext, 0) for ext in extensions)
        if number: parts.append(f"{label}{number}份")
    if counts.get(".exe"): parts.append("Windows启动程序")
    if counts.get(".bat") and not audit.get("hasRuntime"): parts.append("启动脚本")
    if audit["projects"]: parts.append("网页应用源码")
    return '；'.join(parts) or "实验程序与原使用说明"


def requirements(resource, audit):
    # 旧独立工具已有逐项审核说明；补收网页包原先使用统一模板，必须按实物重建。
    if "/lesson-web/" not in resource["source"]:
        result = resource.get("requirements", "").strip()
    else:
        notes = []
        if audit["projects"]:
            for project in audit["projects"]:
                install = "先 npm install，再 " if project["dependencies"] else ""
                command = "npm start" if project["script"] == "start" else "npm run " + project["script"]
                notes.append(f"{project['directory']}：需Node.js，{install}{command}，按终端地址用Chrome或Edge访问。")
        elif any(Path(name).name == "server.js" for name in audit["teacherFiles"]):
            directory = str(PurePosixPath(next(name for name in audit["teacherFiles"] if Path(name).name == "server.js")).parent)
            notes.append(f"需Node.js，在{directory}目录运行 node server.js，按终端地址访问，不直接双击服务页面。")
        elif any(Path(name).name in ("server.py", "app.py", "main.py") for name in audit["teacherFiles"]):
            main_file = next(Path(name).name for name in audit["teacherFiles"] if Path(name).name in ("server.py", "app.py", "main.py"))
            notes.append(f"需Python，运行 python {main_file} 启动服务，按终端地址访问，不直接双击页面。")
        elif audit["localFetchFiles"]:
            notes.append("网页读取本地数据或调用同课服务，请通过本地HTTP服务使用；如无专用服务，可在解压目录运行 python -m http.server 8000（需Python），用Chrome或Edge访问。")
        elif audit["pages"]:
            notes.append("静态网页，解压后使用Chrome或Edge双击打开HTML文件。")
        if audit["counts"].get(".py") and not any("Python" in item for item in notes):
            notes.append("另含Python实验脚本，需Python环境；设备连接与扩展库按对应脚本及原课例说明准备。")
        if audit["networkFiles"]: notes.append("部分页面引用在线资源或服务，相关功能需联网。")
        if audit["cameraFiles"]: notes.append("摄像头功能需授权；若双击无法授权，请通过localhost本地服务打开。")
        result = "\n".join(notes)
    # 共享 Key 必须有实际非占位字面量证据，不能因为出现配置变量名就宣称已提供。
    if audit["sharedKeyFiles"] and "共享" not in result:
        result += "\n包含原作者资源共享的API Key，失效时需替换自己的；有效性未验证。"
    elif audit["keySlotFiles"] and not audit["sharedKeyFiles"] and not re.search(r"Key|令牌|API", result, re.I):
        result += "\n含模型接入配置，调用模型时按工具说明填写自己的API Key；未配置不代表AI功能可用。"
    return result


def build(data, source, groups, reports):
    destination = source / "packages/consolidated-v2"
    destination.mkdir(parents=True, exist_ok=True)
    for case, resources, audits in groups:
        target = destination / (case["sourceKey"] + ".zip")
        temporary = target.with_suffix(".part")
        sections, links, index_rows = [], [], []
        with zipfile.ZipFile(temporary, "w", zipfile.ZIP_DEFLATED, compresslevel=6) as output:
            for i, (resource, audit) in enumerate(zip(resources, audits), 1):
                label = resource["title"] if resource["title"] != "配套网页与实验材料" else "网页与学生材料"
                folder = re.sub(r'[<>:"/\\|?*]', '_', f"{i:02d}_{label}")
                note = requirements(resource, audit)
                sections.append(f"【{label}】\n包含：{describe(audit)}\n使用条件：{note}")
                with zipfile.ZipFile(source / resource["source"]) as original:
                    for entry in original.infolist():
                        name = PurePosixPath(entry.filename)
                        if entry.is_dir() or SKIP.intersection(name.parts) or name.name in GENERATED:
                            continue
                        if name.is_absolute() or ".." in name.parts or '\\' in entry.filename:
                            raise ValueError("压缩包成员路径不安全")
                        # 流式复制避免大包整体进入内存；完整保留各子工程相对目录。
                        with original.open(entry) as incoming, output.open(folder + "/" + entry.filename, "w", force_zip64=True) as outgoing:
                            shutil.copyfileobj(incoming, outgoing, 1024 * 1024)
                output.writestr(folder + "/使用说明_平台版.txt", sections[-1] + "\n")
                for page in audit["pages"]:
                    links.append((folder + "/" + page, label + " / " + Path(page).stem))
                index_rows.append(f"<h2>{html.escape(label)}</h2><p>{html.escape(describe(audit))}</p><pre>{html.escape(note)}</pre>")
            readme = f"{case['title']} · {case['author']}\n\n" + "\n\n".join(sections)
            output.writestr("使用说明_平台版.txt", readme + "\n\n网页入口见《打开材料目录.html》，服务型工程需先启动，目录中的链接不替代启动程序。\n")
            entries = ''.join(f'<li><a href="{html.escape(quote(path, safe="/"))}">{html.escape(label)}</a></li>' for path, label in links)
            output.writestr("打开材料目录.html", '<!doctype html><html lang="zh-CN"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>配套材料目录</title><style>body{max-width:900px;margin:24px auto;padding:0 16px;font:16px/1.7 sans-serif}pre{white-space:pre-wrap;overflow-wrap:anywhere}a{overflow-wrap:anywhere}</style><h1>' + html.escape(case["title"]) + '</h1><p>先查看下方使用条件，服务型工具必须先启动。</p>' + ''.join(index_rows) + '<h2>网页入口</h2><ul>' + entries + '</ul></html>')
        temporary.replace(target)
        summary = "\n".join(f"{resource['title'] if resource['title'] != '配套网页与实验材料' else '网页与学生材料'}：{describe(audit)}" for resource, audit in zip(resources, audits))
        conditions = "\n".join((f"{i}. " if len(resources) > 1 else "") + requirements(resource, audit) for i, (resource, audit) in enumerate(zip(resources, audits), 1))
        filename = re.sub(r'[<>:"/\\|?*]', '_', f"{case['bookCode']}_{case['title']}_{case['author']}_配套材料包.zip")
        merged = {"source": target.relative_to(source).as_posix(), "sha256": digest(target), "filename": filename,
                  "preview": "", "category": "TOOL", "title": "配套材料包", "description": "包含：\n" + summary,
                  "requirements": conditions}
        case["resources"] = [resource for resource in case["resources"] if resource not in resources] + [merged]
        reports.append({"case": case["title"], "author": case["author"], "sourceKey": case["sourceKey"],
                        "mergedFrom": [r["source"] for r in resources], "package": merged["source"],
                        "description": merged["description"], "requirements": conditions, "sha256": merged["sha256"]})
    (OUT / "catalog-feedback-v2.json").write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
    (OUT / "package-consolidation-v2.json").write_text(json.dumps(reports, ensure_ascii=False, indent=2), encoding="utf-8")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--build", action="store_true")
    args = parser.parse_args()
    data = json.loads(BASE.read_text(encoding="utf-8"))
    source = Path(data["sourceRoot"])
    groups, audits = [], []
    for case in data["cases"]:
        resources = [r for r in case["resources"] if r["source"].lower().endswith(".zip")]
        if not resources: continue
        checks = [inspect(source / r["source"]) for r in resources]
        groups.append((case, resources, checks))
        for resource, check in zip(resources, checks):
            audits.append({"case": case["title"], "author": case["author"], "source": resource["source"],
                           "contains": describe(check), "requirements": requirements(resource, check),
                           **{k: v for k, v in check.items() if k not in {"texts", "files", "teacherFiles", "counts", "pages"}}})
    (OUT / "package-audit-v2.json").write_text(json.dumps(audits, ensure_ascii=False, indent=2), encoding="utf-8")
    for row in audits:
        print(f"{row['case']} / {row['author']}：{row['contains']}；共享配置文件{len(row['sharedKeyFiles'])}处")
    if args.build:
        build(data, source, groups, [])
        print(f"已生成{len(groups)}份配套材料包与独立v2清单；原清单、原包、来源均未改动。")


if __name__ == "__main__":
    main()
