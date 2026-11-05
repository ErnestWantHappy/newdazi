"""写库前核对新包内容：净化结果、数据库行数、关键改写是否生效。

只读 zip 与包内副本，不执行任何包内程序。
"""
import io
import json
import re
import sqlite3
import zipfile
from pathlib import Path

ROOT = Path(r"d:/dmwprogram/newdazipingtai/newdazi")
OUT = ROOT / "output/textbook-cases"
PACKAGES = OUT / "release-materials/combined/packages"

# quickform.db 不是禁用项：它必须存在，但提交/附件行必须为 0（下方单独核对）。
FORBIDDEN_NAMES = {".env", "agent.db", "cert.pem", "key.pem", ".DS_Store", "qf2.log", "works.sqlite3"}
FORBIDDEN_PARTS = ("__pycache__", "classroom_data", "static/uploads", "backups/")


def main():
    report = {}
    for path in sorted(PACKAGES.glob("*.zip")):
        with zipfile.ZipFile(path) as archive:
            names = archive.namelist()
            leaked = sorted({
                name for name in names
                if name.rsplit("/", 1)[-1] in FORBIDDEN_NAMES and not name.endswith(".example") and not (path.name == "s11-animal-stamp.zip" and name == "src/.env")
                or any(part in name for part in FORBIDDEN_PARTS)
            })
            info = {"entries": len(names), "leaked": leaked[:8],
                    "hasReadme": "使用说明_平台版.txt" in names,
                    "bytes": path.stat().st_size}
            if path.name == "w5-internet-ai-lab.zip":
                text = archive.read("index.html").decode("utf-8", "ignore")
                info["hasSharedDeepseekKey"] = bool(re.search(r"DEEPSEEK_API_KEY = '[^']+';", text))
                info["emptyFormApi"] = "const FORM_API = '';" in text and "const FORM_API_2 = '';" in text
                info["hasNodeServer"] = "server.js" in names
            if path.name == "w19-campus-agent.zip":
                bat = archive.read("启动.bat").decode("utf-8", "ignore")
                info["httpStart"] = "--ssl-keyfile" not in bat
                info["hasExampleConfig"] = "backend/config.example.json" in names
            if path.name == "w18-quickform-class.zip":
                db_bytes = archive.read("quickform/QuickForm 2.5/data/quickform.db")
                temp_db = OUT / "tmp-quickform-check.db"
                temp_db.write_bytes(db_bytes)
                connection = sqlite3.connect(temp_db)
                counts = {}
                for table in ("submission", "attachment", "user", "task", "app_setting"):
                    counts[table] = connection.execute(f'select count(*) from "{table}"').fetchone()[0]
                connection.close()
                info["dbCounts"] = counts
                info["hasVenv"] = any(name.startswith("quickform/QuickForm 2.5/venv/Scripts/python.exe") for name in names)
            report[path.name] = info

    (OUT / "web-packages-validation.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    # 平台说明只要求本轮新建的包；早期实验包由另一条流水线登记。
    new_targets = {item["target"].split("/")[-1] for item in json.loads((OUT / "web-packages.json").read_text(encoding="utf-8"))}
    problems = []
    for name, info in report.items():
        if info["leaked"]:
            problems.append(f"{name} 仍含：{info['leaked']}")
        if name in new_targets and not info["hasReadme"]:
            problems.append(f"{name} 缺少平台说明")
    quick = report.get("w18-quickform-class.zip", {}).get("dbCounts", {})
    if quick and (quick.get("submission") or quick.get("attachment")):
        problems.append("课堂包数据库仍有提交或附件")
    if quick and not (quick.get("user") and quick.get("task")):
        problems.append("课堂包数据库缺少默认管理员或任务")
    if not report.get("w5-internet-ai-lab.zip", {}).get("hasSharedDeepseekKey"):
        problems.append("七上5 原作者自愿共享的Key未保留")
    if not report.get("w19-campus-agent.zip", {}).get("httpStart"):
        problems.append("智能体启动脚本仍依赖已删除证书")
    print(f"核对 {len(report)} 个包；问题 {len(problems)} 项")
    for line in problems:
        print("   ", line)
    print(json.dumps(report.get("w18-quickform-class.zip", {}), ensure_ascii=False))
    if problems:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
