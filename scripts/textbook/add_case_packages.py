"""把审核通过的实验包/网页工具登记进本机清单，并记录不收或待净化的原因。

必须在 review_prepared_catalog.py 之后执行：该脚本会重建清单，本轮新增项需重新并入。
只读E盘来源，发布副本与净化包写到 release-materials/combined/packages/。
"""
import hashlib
import json
import re
import zipfile
from pathlib import Path

from import_cases import digest

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases"
PREPARED = OUT / "release-materials/combined"
PACKAGES = PREPARED / "packages"

# 目录打包时的排除项：运行时依赖、版本库、编辑器备份、课堂数据库与真实配置。
SKIP_DIRS = {"node_modules", ".git", "__pycache__", ".workbuddy", ".wpp_backup", ".idea", ".vscode", "test"}
SKIP_FILES = re.compile(r"(?:^|/)(?:\.DS_Store|\.env|desktop\.ini|thumbs\.db)$|\.(?:db|db-wal|db-shm|sqlite|sqlite3|bak\d*|log)$", re.I)

INCLUDE = [
    {
        "source": "七年级/七年级下册/象山县-第5课《人工智能物联网》/源代码.zip",
        "file": True, "category": "TOOL", "title": "物联网实验源代码",
        "description": "本课物联网实验的完整工程（Vue3 前端、FastAPI 后端、SQLite 记录），含县平台身份、小组实验记录、文本模型训练与 MQTT 语音控灯示例。适合教师二次修改或在校内搭建实验环境。",
        "requirements": "Python 3.11 与 Node.js 20；需按 .env.example 填写县平台地址与 MQTT 账号后运行。示例配置为占位值，不含真实凭据。",
    },
    {
        "source": "四年级/四年级下册/慈溪市-第17课《手写数字的识别与应用》/第17课《手写数字的识别与应用》配套资源.zip",
        "file": True, "category": "TOOL", "title": "手写数字识别训练平台",
        "description": "本课配套训练平台（Windows 可执行程序与运行库），支持班级分组、学生训练、互评与结果导出。包内数据库为空库，不含学生数据。",
        "requirements": "Windows 10/11 64 位；解压后按包内说明启动，数据保存在本地 SQLite。若安全软件拦截可执行文件，需加入信任目录。",
    },
    {
        "source": "九年级/象山县 郑琦 -九年级-第13课《推荐系统探究》/电子实验单",
        "file": False, "category": "TOOL", "title": "读书节推荐系统电子实验单",
        "description": "本课课堂实验网站，含教师大屏、学生端、推荐算法实验步骤与证据记录。已移除上一届课堂数据库与编辑器备份，首次启动为空课堂。",
        "requirements": "Windows；解压后双击 start.bat 启动（自带 Node 运行时，无需安装）。学生机需与教师机同一局域网，端口以启动窗口提示为准。",
    },
    {
        "source": "七年级/七年级上册/江北区-第6课《互联网接入》/七上第6课《互联网接入》互动课件",
        "file": False, "category": "MATERIAL", "title": "互联网接入互动课件",
        "description": "本课互动网页课件，用设备图片演示上网方式与网络连接过程，可直接在浏览器操作。",
        "requirements": "解压后双击 index.html，用 Chrome/Edge 等现代浏览器打开；无需联网。",
    },
]

QUARANTINE = [
    # 课堂包、两个 RAR 与六下16 学习单网站已在 build_web_packages.py 中净化收录，
    # 这里只保留仍不随课例分发或需要额外说明的来源。
    {"source": "七年级/七年级下册/象山县-第19课《人工智能的技术基础》(1)/城市大脑/node-v24.21.0-x64.msi",
     "reason": "Node.js 安装包，不是课例素材；由教师在需要时自行安装"},
    {"source": "三年级/三年级下册/前湾新区-第11课《资源智能生成》-张楚人/学生评价平台/1.先安装nodejs.msi",
     "reason": "Node.js 安装包，不随课例分发"},
    {"source": "九年级/象山县 郑琦 -九年级-第13课《推荐系统探究》/电子实验单/runtime/node/node.exe",
     "reason": "运行时已随净化包内保留，不单独作为资源登记"},
    {"source": "五年级/五年级上册/金阳小学-第11课《认识决策树》-杨晶茹/全校学生报名数据_1500条.xlsx",
     "reason": "含1500条学生姓名与体能达标记录，属个人数据，不得随课例分发"},
    {"source": "五年级/五年级上册/金阳小学-第11课《认识决策树》-杨晶茹/全校学生数据_1000条模拟数据.xlsx",
     "reason": "虽标注模拟，仍含学生姓名列，按个人数据保守处理，不随课例分发"},
    {"source": "七年级/七年级上册/江北区-第19课《智能体的搭建与应用》/学生素材/2更丰富的智能体/app",
     "reason": "Vite+React 源码工程，需 npm 安装与构建才能运行；同课已收录可运行的 Python 智能体平台，源码不单独分发"},
]

# 学生练习用数据与说明性文档：原扫描未收录xlsx，或与已收录件同哈希/同名被去重。
EXTRA = [
    {"source": "七年级/七年级下册/象山县-第17课《二分搜索算法》-叶晓静/学生用文件/数据1：冷冻厂生产间各工位产量.xlsx",
     "title": "二分搜索练习数据一（冷冻厂产量）",
     "description": "本课二分查找活动的学生数据表，含1000条工位产量记录。",
     "requirements": "用 Excel 或 WPS 打开；无需联网。"},
    {"source": "七年级/七年级下册/象山县-第17课《二分搜索算法》-叶晓静/学生用文件/数据2：网上超市各类商品销售量.xlsx",
     "title": "二分搜索练习数据二（商品销售量）",
     "description": "本课二分查找活动的学生数据表，含14类商品销售量。",
     "requirements": "用 Excel 或 WPS 打开；无需联网。"},
    {"source": "五年级/五年级下册/奉化区-第18课《用神经网络进行预测》/测试数据/训练数据_500名学生.xlsx",
     "title": "神经网络训练数据（500条）",
     "description": "身高预测实验的训练集，只含身高与性别字段，不含姓名等个人标识。",
     "requirements": "用 Excel 或 WPS 打开；与训练数据（5条）配合演示。"},
    {"source": "五年级/五年级下册/奉化区-第18课《用神经网络进行预测》/测试数据/训练数据_5名学生.xlsx",
     "title": "神经网络训练数据（5条）",
     "description": "身高预测实验的小样本训练集，便于课堂演示训练过程。",
     "requirements": "用 Excel 或 WPS 打开。"},
    {"source": "五年级/五年级下册/奉化区-第18课《用神经网络进行预测》/测试数据/预测验证数据_2名学生.xlsx",
     "title": "神经网络预测验证数据（2条）",
     "description": "身高预测实验的验证样本，用于对比模型预测与真实值。",
     "requirements": "用 Excel 或 WPS 打开。"},
    {"source": "六年级/六年级上册/第18课《对话机器人》/学生素材/语料库模板.xlsx",
     "title": "对话机器人语料库模板",
     "description": "问答语料模板，学生按“问题、关键词、答案”三列补充本校本班语料。",
     "requirements": "用 Excel 或 WPS 填写；导入对话机器人工具前保持三列结构。"},
    {"source": "四年级/四年级上册/北仑区-第17课《返回成语现场》(1)/学生文件/【学习工具】/《返回成语现场》操作指南_20261020_105354.docx",
     "title": "《返回成语现场》操作指南",
     "description": "学生使用本课成语学习工具的分步操作说明。",
     "requirements": "用 Word 或 WPS 打开。"},
]


def add_zip(source, target, prefix):
    count = 0
    with zipfile.ZipFile(target, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
        for path in sorted(source.rglob("*")):
            if not path.is_file():
                continue
            parts = path.relative_to(source).parts
            if any(part in SKIP_DIRS for part in parts[:-1]):
                continue
            relative = path.relative_to(source).as_posix()
            if SKIP_FILES.search(relative):
                continue
            archive.write(path, prefix + "/" + relative)
            count += 1
    return count


def main():
    manifest_path = OUT / "catalog-local-reviewed.json"
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    source = Path(json.loads((OUT / "inventory.json").read_text(encoding="utf-8"))["sourceRoot"])
    PACKAGES.mkdir(parents=True, exist_ok=True)

    applied = []
    for decision in INCLUDE:
        origin = source / decision["source"]
        if not origin.exists():
            raise RuntimeError("来源不存在：" + decision["source"])
        name = Path(decision["source"]).stem + (".zip" if not decision["file"] else Path(decision["source"]).suffix)
        name = name.replace(" ", "")
        target = PACKAGES / name
        if decision["file"]:
            target.write_bytes(origin.read_bytes())
            entries = None
        else:
            entries = add_zip(origin, target, Path(decision["source"]).name)
        record = {key: value for key, value in decision.items() if key not in {"source", "file"}}
        record.update(source="packages/" + target.name, sha256=digest(target), preview="", previewStatus="READY")
        if entries is not None:
            record["entries"] = entries
        applied.append({"source": decision["source"], "target": record["source"], "bytes": target.stat().st_size,
                        "sha256": record["sha256"], "entries": entries, "title": record["title"]})

        directory = Path(decision["source"]).parent.as_posix()
        case = next((item for item in manifest["cases"] if item["sourceDirectory"] == directory), None)
        if case is None:
            raise RuntimeError("清单中没有对应课例：" + directory)
        case["resources"] = [item for item in case["resources"] if item["source"] != record["source"]]
        case["resources"].append(record)

    extra_root = PACKAGES / "extras"
    for item in EXTRA:
        origin = source / item["source"]
        if not origin.is_file():
            raise RuntimeError("来源不存在：" + item["source"])
        target = extra_root / (digest(origin)[:8] + "_" + origin.name)
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(origin.read_bytes())
        record = {"source": "packages/extras/" + target.name, "sha256": digest(target), "preview": "",
                  "previewStatus": "READY", "category": "MATERIAL", "title": item["title"],
                  "description": item["description"], "requirements": item["requirements"]}
        case = next((entry for entry in manifest["cases"]
                     if item["source"].startswith(entry["sourceDirectory"] + "/")), None)
        if case is None:
            raise RuntimeError("清单中没有对应课例：" + item["source"])
        case["resources"] = [entry for entry in case["resources"] if entry["source"] != record["source"]]
        case["resources"].append(record)
        applied.append({"source": item["source"], "target": record["source"], "bytes": target.stat().st_size,
                        "sha256": record["sha256"], "title": record["title"]})

    manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")
    (OUT / "package-decisions.json").write_text(json.dumps(
        {"included": applied, "quarantine": QUARANTINE}, ensure_ascii=False, indent=2), encoding="utf-8")
    for item in applied:
        print(f"已登记 {item['title']} {item['bytes']/1048576:.1f}MiB → {item['target']}")
    print(f"本轮收录{len(applied)}个包，隔离/待处理{len(QUARANTINE)}项；尚未写库")


if __name__ == "__main__":
    main()
