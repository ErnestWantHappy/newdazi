"""把课例里的网页工程、两个 RAR 与课堂包 tar 建成可发布副本并登记进清单。

规则：原始 E 盘只读；所有净化在 output/textbook-cases/staging 副本上进行；
每个包内附「使用说明_平台版.txt」，与平台显示的用途/条件保持一致。
只处理本机，不触碰数据库，写库由 load_case_samples_local.py 负责。
"""
import json
import re
import shutil
import sqlite3
import subprocess
import tarfile
import time
import zipfile
from fnmatch import fnmatch
from pathlib import Path

ROOT = Path(r"d:/dmwprogram/newdazipingtai/newdazi")
OUT = ROOT / "output/textbook-cases"
SOURCE = Path(json.loads((OUT / "inventory.json").read_text(encoding="utf-8"))["sourceRoot"])
STAGING = OUT / "staging"
PACKAGES = OUT / "release-materials/combined/packages"
MANIFEST_PATH = OUT / "catalog-local-reviewed.json"
TAR = r"C:\Windows\System32\tar.exe"
PACKAGE_DIR_PREFIX = "packages"

IGNORE_PARTS = {".git", ".svn", "__pycache__", ".pytest_cache", "node_modules/.cache"}
IGNORE_NAMES = {".DS_Store", "Thumbs.db", "desktop.ini"}

PACKAGES_SPEC = [
    {
        "slug": "w5-internet-ai-lab",
        "case": "七年级/七年级上册/《互联网与人工智能》-王纪宇",
        "source": "七年级/七年级上册/《互联网与人工智能》-王纪宇/七上第5课/七上第5课/实验工具+实验记录/app",
        "kind": "dir",
        "category": "TOOL",
        "title": "互联网与人工智能 实验工具（搜索与推荐）",
        "description": "用途：七上第5课学生实验网页，含传统搜索、智能搜索、个性化推荐三个实验页和实验记录提交。包含：index.html、server.js（本地代理与静态托管）、package-lock.json。启动：需 Node.js 18+，在 app 目录执行 node server.js 后打开 http://localhost:3000。环境：Windows/国产系统均可，联网才能实时抓取搜索结果。",
        "requirements": "需 Node.js 18+，联网使用。内含原作者自愿共享的 DeepSeek API Key；若无法使用，可能已被原作者删除、停用或额度用尽，可在 index.html 的 DEEPSEEK_API_KEY 处替换为自己的。实验记录如需保存到本班，请另行配置自己的收集地址。",
        "remove": ["Dockerfile"],
        "transforms": {
            "index.html": [
                (re.compile(r"const FORM_API = '[^']*';"), "const FORM_API = ''; // 平台版已清空，请填本班收集地址"),
                (re.compile(r"const FORM_API_2 = '[^']*';"), "const FORM_API_2 = ''; // 平台版已清空，请填本班收集地址"),
            ]
        },
    },
    {
        "slug": "w19-campus-agent",
        "case": "七年级/七年级上册/江北区-第19课《智能体的搭建与应用》",
        "source": "七年级/七年级上册/江北区-第19课《智能体的搭建与应用》/智能体.rar",
        "kind": "rar",
        "stripRoot": "智能体",
        "category": "TOOL",
        "title": "智能体的搭建与应用 校园智能体平台",
        "description": "用途：学生四步创建智能体并互相试用，教师审核作品。包含：FastAPI 后端（main.py、database.py、rag.py）、原生前端、四个场景模板与示例知识库、安装与启动脚本。启动：先双击「安装依赖.bat」，再双击「启动.bat」，浏览器打开 http://127.0.0.1:8000。环境：Windows + Python 3.10+；可选联网调用大模型。",
        "requirements": "需 Python 3.10+ 并安装依赖，首次启动自动建库。原作者配置中的模型 Key 为空；如需调用模型，请在 backend/config.json 填写自己的 Key 与对应服务地址。未配置模型时可用知识库片段演示。原课堂数据库与自签名证书已移除，启动方式为 HTTP。",
        "remove": ["backend/agent.db*", "backend/cert.pem", "backend/key.pem", "backend/__pycache__/*"],
        "transforms": {
            "启动.bat": [
                ("python -m uvicorn main:app --host 0.0.0.0 --port 8000 --ssl-keyfile key.pem --ssl-certfile cert.pem",
                 "python -m uvicorn main:app --host 0.0.0.0 --port 8000"),
            ]
        },
    },
    {
        "slug": "w18-dfs-teaching-tool",
        "case": "七年级/七年级下册/慈溪市-第18课《深度优先搜索算法》",
        "source": "七年级/七年级下册/慈溪市-第18课《深度优先搜索算法》/教学工具 源程序代码.rar",
        "kind": "rar",
        "stripRoot": "教学工具",
        "category": "TOOL",
        "title": "深度优先搜索 教学工具（学生端与教师端）",
        "description": "用途：深搜公开课配套互动工具，学生端闯关提交、教师端实时看板并可按关卡开放。包含：server.js（零依赖 Node 服务）、public 下学生端/教师端页面与脚本。启动：安装 Node.js 后在目录内执行 node server.js，打开 http://localhost:3000。环境：Node.js 18+，局域网内学生可访问教师机 IP。",
        "requirements": "需 Node.js 18+；不联网也能完成闯关与看板。AI 问答面板为可选项，需在启动前设置 AI_API_KEY（可选 AI_API_BASE、AI_MODEL），未设置时其余功能正常。",
    },
    {
        "slug": "w18-quickform-class",
        "case": "七年级/七年级上册/江北区-第18课《生成式人工智能》",
        "source": "七年级/七年级上册/江北区-第18课《生成式人工智能》/课堂包.tar",
        "kind": "tar",
        "stripRoot": "课堂包",
        "category": "TOOL",
        "title": "生成式人工智能 AI讲宁波 课堂包",
        "description": "用途：七上第18课课堂提交与作品展，学生在互动页提交提示词与图片，教师机实时展示。包含：QuickForm 教师版本地服务（Flask+SQLite）、学生互动页、教师作品展页面与启动脚本。启动：首次先安装 Python 依赖（python -m pip install -r requirements.txt），再双击「启动QuickForm服务.bat」，按窗口提示打开教师端与学生端地址（默认 5001 端口）。环境：Windows 教师机需 Python 3.10+，学生机同一局域网。",
        "requirements": "（1）教师机需安装 Python 3.10+ 并在首次使用时联网安装依赖；（2）需放行 5001 端口；（3）平台版已清空上届学生提交、附件与日志，保留 5 个任务与默认管理员 wst/quickform（首次登录请改密码）；（4）原包内置的运行环境绑定原作者电脑路径，换机无法启动，已从本副本移除；（5）服务仅限校园内网，请勿映射公网。",
        "remove": ["_shots/*", "qf2.log", ".DS_Store", "quickform/QuickForm 2.5/.env",
                   "quickform/QuickForm 2.5/__pycache__/*", "quickform/QuickForm 2.5/backups/*",
                   "quickform/QuickForm 2.5/static/uploads/*", "quickform/QuickForm 2.5/venv/*"],
        "transforms": {
            "学生端-互动课堂.html": [
                (re.compile(r"const POST_URL = 'https://quickform\.cn/api/[^']+';"),
                 "// 平台版：原外部收集地址已移除，改为提交到本机 QuickForm 同一任务。\nlet POST_URL = '';"),
                (re.compile(r"(const API_BASE = [\s\S]*?;\n)"),
                 "\\1POST_URL = API_BASE + '/api/fhXTZJnVyZJ';\n"),
            ],
            "quickform/QuickForm 2.5/static/student.html": [
                (re.compile(r"const POST_URL = 'https://quickform\.cn/api/[^']+';"),
                 "// 平台版：原外部收集地址已移除，改为提交到本机 QuickForm 同一任务。\nlet POST_URL = '';"),
                (re.compile(r"(const API_BASE = [\s\S]*?;\n)"),
                 "\\1POST_URL = API_BASE + '/api/fhXTZJnVyZJ';\n"),
            ],
            "使用说明.md": [
                ("# 宁波旅游推文作品展 · 课堂包使用说明",
                 "# 宁波旅游推文作品展 · 课堂包使用说明\n\n"
                 "> **平台版说明**：本副本不附带原作者电脑上的 Python 运行环境。首次使用请先安装 Python 3.10+，"
                 "在 `quickform\\QuickForm 2.5` 目录执行 `python -m pip install -r requirements.txt`，再双击启动脚本。"
                 "学生提交改存本机，不再发往外部收集地址。详见同级 `使用说明_平台版.txt`。"),
            ],
        },
        "sqlite": {
            "path": "quickform/QuickForm 2.5/data/quickform.db",
            "deleteRows": ["submission", "attachment"],
            "vacuum": True,
        },
    },
    {
        "slug": "s11-animal-stamp",
        "case": "三年级/三年级下册/前湾新区-第11课《资源智能生成》-卢旭浩",
        "source": "三年级/三年级下册/前湾新区-第11课《资源智能生成》-卢旭浩/源程序代码/aiagent",
        "kind": "dir",
        "category": "TOOL",
        "title": "资源智能生成 动物印章生成工具（源码）",
        "description": "用途：三下第11课资源智能生成的演示工具，输入动物特征调用 Coze 工作流生成印章风格图片。包含：app.py（Flask 接口）、index.html 前端页面、依赖说明。启动：安装依赖后在 src 目录执行 python app.py，打开 http://127.0.0.1:5000。环境：Python 3.10+，联网访问 Coze。",
        "requirements": "需 Python 3.10+ 并安装 flask、flask-cors、cozepy、python-dotenv，联网使用。src/.env 保留原作者自愿共享的 Coze API 令牌与工作流配置；若无法生成图片，令牌可能已失效，可替换为自己的 COZE_API_TOKEN，并按需填写对应 WORKFLOW_ID。未验证共享令牌当前有效性。",
        "remove": ["venv/*", "src/__pycache__/*"],
    },
    {
        "slug": "s11-dream-wall",
        "case": "三年级/三年级下册/前湾新区-第11课《资源智能生成》-岑姝锌",
        "source": "三年级/三年级下册/前湾新区-第11课《资源智能生成》-岑姝锌/梦想校园_作品展示墙",
        "kind": "dir",
        "category": "TOOL",
        "title": "梦想校园 班级作品展示墙",
        "description": "用途：全班作品征集与展示，学生提交文字和图片，教室大屏滚动展示。包含：server.py（仅 Python 标准库）、index.html、启动脚本与使用说明。启动：双击 START_WALL.bat，教师机打开 http://127.0.0.1:8765，学生机访问教师机 IP 的同一端口。环境：Python 3.8+（无需第三方库），局域网。",
        "requirements": "需安装 Python 3；不联网也能用。作品保存在本地 data 目录；平台版已清空上届作品数据库与上传图片，教师 PIN 可用环境变量 WALL_TEACHER_PIN 设置。",
        "remove": ["data/works.sqlite3", "data/uploads", "__pycache__"],
    },
    {
        "slug": "s11-film-vote",
        "case": "三年级/三年级下册/前湾新区-第11课《资源智能生成》-陈凯",
        "source": "三年级/三年级下册/前湾新区-第11课《资源智能生成》-陈凯/校园电影节局域网投票网站",
        "kind": "dir",
        "category": "TOOL",
        "title": "校园电影节 局域网投票网站",
        "description": "用途：校园电影节作品投票，学生端投票、教师端实时统计与结果展示。包含：server.mjs（零依赖 Node 服务）、public 下学生与管理员页面、启动脚本与说明。启动：安装 Node.js 后双击「启动网站.bat」，按窗口显示的地址访问。环境：Node.js 18+，局域网；无需数据库。",
        "requirements": "需 Node.js 18+；不联网也能投票。管理员口令与投票数据由服务端本地保存，正式使用前请按 README 修改默认口令。",
    },
    {
        "slug": "g5-decision-tree-cards",
        "case": "五年级/五年级上册/岔路小学-第11课《认识决策树》-田甜",
        "source": "五年级/五年级上册/岔路小学-第11课《认识决策树》-田甜/认识决策树-学单",
        "kind": "dir",
        "category": "MATERIAL",
        "title": "认识决策树 网页学单（任务1-5+评价+教师大屏）",
        "description": "用途：五上第11课网页学单，含任务1至任务5、自评页与教师数据大屏。包含：8 个 HTML 页面与配套样式脚本。启动：解压后用浏览器打开 index.html。环境：任意现代浏览器，无需服务器。",
        "requirements": "无需安装与联网；学生作答保存在本机浏览器，换机器不会同步。教师大屏页需与学单在同一目录打开。",
    },
    {
        "slug": "g6-16-smart-control-site",
        "case": "六年级/六年级下册/余姚市-第16课《认识智能控制系统》",
        "source": "六年级/六年级下册/余姚市-第16课《认识智能控制系统》/余姚市-六年级下册-第16课认识智能控制系统-学习单网站-教师课件",
        "kind": "dir",
        "category": "MATERIAL",
        "title": "认识智能控制系统 学习单网站（含番茄实验室）",
        "description": "用途：六下第16课学习单网站，含 5 个活动页、番茄实验室互动页、学生端页面与教师网页课件。包含：HTML 页面、样式脚本、本地视频与图片素材（约 68MB）。启动：解压后用浏览器打开「教师网页课件.html」或 student/index.html。环境：现代浏览器，无需服务器。",
        "requirements": "无需安装与联网；包体较大，解压需预留约 70MB。视频为本地文件，浏览器需允许自动播放或手动点击播放。",
    },
    {
        "slug": "g6-18-plant-agent",
        "case": "六年级/六年级下册/余姚市-认识智能体-余姚市梨洲小学-李炯荣(1)",
        "source": "六年级/六年级下册/余姚市-认识智能体-余姚市梨洲小学-李炯荣(1)/导入视频、网页素材、网页学单及部署包",
        "kind": "dir",
        "category": "TOOL",
        "title": "认识智能体 校园植物养护专家 网页学单与课堂服务",
        "description": "用途：六下《认识智能体》课堂网页学单，含导入、探索、创建、评测四个环节与教师星星看板。包含：serve_classroom.py（仅 Python 标准库）、课堂网页与脚本、AI 设置页、45 秒导入视频。启动：教师机双击「启动全班课堂.bat」，默认 http://127.0.0.1:8868，学生机用教师机 IP 访问。环境：教师机需 Python 3，学生机仅需浏览器。",
        "requirements": "需 Python 3（无需第三方库）；不联网也能用预设回答与朗读。真实 AI 需在教师机「打开AI接入设置.bat」中填自己的 Key，仅教师机 localhost 可设置。平台版已清空上届课堂数据与缓存。",
        "remove": ["classroom_data/*", "__pycache__/*"],
    },
]


def digest(path):
    import hashlib
    hasher = hashlib.sha256()
    with open(path, "rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            hasher.update(chunk)
    return hasher.hexdigest()


def copy_tree(source, target):
    # 依赖目录与虚拟环境在登记前就会排除，避免无谓地复制数百 MB。
    heavy = ("venv", ".venv", "node_modules", "__pycache__", ".git")
    shutil.copytree(source, target, ignore=shutil.ignore_patterns(*IGNORE_NAMES, *heavy), dirs_exist_ok=True)


def extract(kind, archive, target):
    target.mkdir(parents=True, exist_ok=True)
    if kind == "rar":
        result = subprocess.run([TAR, "-xf", str(archive), "-C", str(target)], capture_output=True)
        if result.returncode:
            raise RuntimeError("RAR 解压失败：" + result.stderr.decode("utf-8", "replace")[:200])
    elif kind == "tar":
        with tarfile.open(archive, encoding="utf-8") as handle:
            handle.extractall(target)


def prune(root, patterns):
    for pattern in patterns:
        for path in list(root.glob(pattern)):
            if path.is_dir():
                shutil.rmtree(path)
            elif path.exists():
                path.unlink()


def transform(root, transforms):
    for relative, rules in transforms.items():
        path = root / relative
        if not path.is_file():
            raise RuntimeError(f"待改写文件缺失：{relative}")
        text = path.read_text(encoding="utf-8", errors="surrogateescape")
        for pattern, replacement in rules:
            if hasattr(pattern, "sub"):
                text = pattern.sub(replacement, text)
            else:
                if pattern not in text:
                    raise RuntimeError(f"{relative} 未找到待替换内容：{pattern[:40]}")
                text = text.replace(pattern, replacement)
        path.write_text(text, encoding="utf-8", errors="surrogateescape")


def sanitize_sqlite(root, spec):
    path = root / spec["path"]
    # 自动提交模式：VACUUM 不能处在事务中，也不能有未关闭的游标。
    connection = sqlite3.connect(path, isolation_level=None)
    try:
        connection.execute("PRAGMA secure_delete=ON")
        counts = {}
        for table in spec.get("deleteRows", []):
            deleted = connection.execute(f'DELETE FROM "{table}"')
            counts[table] = deleted.rowcount
            deleted.close()
        connection.execute("PRAGMA wal_checkpoint(TRUNCATE)")
        if spec.get("vacuum"):
            connection.execute("VACUUM")
    finally:
        connection.close()
    return counts


def write_readme(root, spec):
    text = (
        f"{spec['title']}\n\n"
        f"用途与包含：{spec['description']}\n\n"
        f"使用条件：{spec['requirements']}\n\n"
        "本副本由平台整理。若包含原作者自愿共享的 API Key，会按原样保留；"
        "共享 Key 可能被删除、停用或额度用尽，如无法使用可按上文说明替换为自己的。"
        "平台未验证共享 Key 当前有效性。\n"
    )
    (root / "使用说明_平台版.txt").write_text(text, encoding="utf-8")


def normalize_shared_agent_config(root):
    path = root / "backend/config.json"
    content = path.read_text(encoding="utf-8-sig")
    config = json.loads((root / "backend/config.example.json").read_text(encoding="utf-8-sig"))
    # 原文含JSON不支持的行尾注释，只读取有明确引号边界的模型字段，不执行原配置。
    for section in ("llm", "asr"):
        block = re.search(r'"' + section + r'"\s*:\s*\{([^}]+)\}', content, re.S)
        if not block:
            continue
        for field in ("api_key", "base_url", "model", "resource_id"):
            match = re.search(r'"' + field + r'"\s*:\s*("(?:\\.|[^"\\])*")', block[1])
            if match:
                config.setdefault(section, {})[field] = json.loads(match[1])
    path.write_text(json.dumps(config, ensure_ascii=False, indent=2), encoding="utf-8")


def build_zip(root, target, excludes):
    with zipfile.ZipFile(target, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
        for path in sorted(root.rglob("*")):
            if not path.is_file():
                continue
            if any(part in IGNORE_PARTS for part in path.parts):
                continue
            if path.name in IGNORE_NAMES:
                continue
            relative = path.relative_to(root).as_posix()
            if any(fnmatch(relative, pattern) for pattern in excludes):
                continue
            archive.write(path, relative)


def main():
    PACKAGES.mkdir(parents=True, exist_ok=True)
    STAGING.mkdir(parents=True, exist_ok=True)
    run_id = time.strftime("%H%M%S")
    manifest = json.loads(MANIFEST_PATH.read_text(encoding="utf-8"))
    applied = []
    for spec in PACKAGES_SPEC:
        # 每次用独立暂存目录，避免批量删除海量文件触发安全限制。
        staging = STAGING / f"{run_id}-{spec['slug']}"
        staging.mkdir(parents=True)
        origin = SOURCE / spec["source"]
        if not origin.exists():
            raise RuntimeError("来源不存在：" + spec["source"])
        if spec["kind"] == "dir":
            copy_tree(origin, staging)
            root = staging
        else:
            extract(spec["kind"], origin, staging)
            root = staging / spec["stripRoot"] if spec.get("stripRoot") else staging
            if not root.is_dir():
                raise RuntimeError("压缩包缺少顶层目录：" + spec["stripRoot"])
        excludes = list(spec.get("remove", []))
        if spec.get("sqlite"):
            excludes += [spec["sqlite"]["path"] + "-wal", spec["sqlite"]["path"] + "-shm"]
        if spec.get("transforms"):
            transform(root, spec["transforms"])
        if spec["slug"] == "w19-campus-agent":
            normalize_shared_agent_config(root)
        sqlite_counts = sanitize_sqlite(root, spec["sqlite"]) if spec.get("sqlite") else {}
        write_readme(root, spec)

        target = PACKAGES / (spec["slug"] + ".zip")
        build_zip(root, target, excludes)
        files = [path for path in root.rglob("*") if path.is_file()
                 and not any(fnmatch(path.relative_to(root).as_posix(), pattern) for pattern in excludes)]
        entry = {
            "source": f"{PACKAGE_DIR_PREFIX}/{target.name}",
            "sha256": digest(target),
            "preview": "",
            "previewStatus": "READY",
            "category": spec["category"],
            "title": spec["title"],
            "description": spec["description"],
            "requirements": spec["requirements"],
        }
        case = next((item for item in manifest["cases"] if item["sourceDirectory"] == spec["case"]), None)
        if case is None:
            raise RuntimeError("清单中没有该课例：" + spec["case"])
        case["resources"] = [item for item in case["resources"] if item["source"] != entry["source"]]
        case["resources"].append(entry)
        applied.append({
            "slug": spec["slug"], "case": spec["case"], "origin": spec["source"], "kind": spec["kind"],
            "target": entry["source"], "bytes": target.stat().st_size, "sha256": entry["sha256"],
            "files": len(files), "uncompressedBytes": sum(path.stat().st_size for path in files),
            "sqliteDeleted": sqlite_counts, "removed": spec.get("remove", []),
        })
        print(f"打包 {spec['title']} → {target.name} {target.stat().st_size/1048576:.1f}MiB（{len(files)} 文件）")

    MANIFEST_PATH.write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")
    (OUT / "web-packages.json").write_text(json.dumps(applied, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"共生成 {len(applied)} 个网页/工具包，已写入 catalog-local-reviewed.json；尚未写库")


if __name__ == "__main__":
    main()
