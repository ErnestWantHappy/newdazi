"""生成简短、面向教师的压缩包使用说明。

技术检查结果保留在 archive-inspection.json 和 manifest 中；教师页面只展示
“包内有什么、怎么打开”，不罗列哈希、完整路径、程序清单和安全扫描明细。
"""
from __future__ import annotations

import argparse
import json
import re
from pathlib import PurePosixPath

from common import read_json, run_dir, utc_now, write_json

# 与优秀课例的既有说明保持同一量级，避免长清单挤占阅读器版面。
MAX_REQUIREMENTS = 180
PERSONAL_FILENAME = re.compile(
    r"(?<!\d)(?:1[3-9]\d{9}|\d{17}[\dXx])(?!\d)|(?:学号|身份证)[:：]\s*\S{4,}"
)


def entry_names(report: dict) -> list[str]:
    return [item["name"] for item in report.get("entries", []) if not item.get("isDir")]


def base_name(name: str) -> str:
    return PurePosixPath(name.replace("\\", "/")).name


def has_suffix(names: list[str], suffixes: set[str]) -> bool:
    return any(PurePosixPath(name).suffix.lower() in suffixes for name in names)


def matches_name(names: list[str], pattern: str) -> bool:
    return any(re.search(pattern, base_name(name), re.I) for name in names)


def startup_file(names: list[str]) -> str:
    candidates = [
        base_name(name) for name in names
        if re.search(r"(启动|运行).*\.(bat|cmd|exe)$", base_name(name), re.I)
    ]
    return sorted(candidates, key=lambda item: (0 if "启动" in item else 1, len(item)))[0] if candidates else ""


def archive_types(names: list[str]) -> dict[str, bool]:
    return {
        "design": has_suffix(names, {".doc", ".docx", ".pdf"})
        and matches_name(names, r"设计|反思|说课|教案"),
        "slides": has_suffix(names, {".ppt", ".pptx"}),
        "worksheet": matches_name(names, r"任务单|学习单|练习|导学"),
        "images": has_suffix(names, {".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp"}),
        "audio": has_suffix(names, {".mp3", ".wav", ".m4a", ".ogg", ".aac"}),
        "video": has_suffix(names, {".mp4", ".mov", ".webm", ".avi", ".mkv"}),
        "url": has_suffix(names, {".url"}),
    }


def external_types(resources: list[dict], archive_name: str) -> list[str]:
    """识别同一课例资源列表中的独立资料，供压缩包说明做简短指引。"""
    labels: list[str] = []
    for item in resources:
        if item.get("category") == "ARCHIVE" or item.get("filename") == archive_name:
            continue
        title = str(item.get("title") or item.get("filename") or "")
        if re.search(r"设计|反思|说课|教案", title) and "教学设计" not in labels:
            labels.append("教学设计")
        elif re.search(r"课件", title) and "课件" not in labels:
            labels.append("课件")
        elif re.search(r"任务单|学习单", title) and "学生任务单" not in labels:
            labels.append("学生任务单")
    return labels


def summarize(report: dict, resources: list[dict]) -> str:
    names = entry_names(report)
    types = archive_types(names)
    startup = startup_file(names)
    has_tool = bool(startup) or (
        any(base_name(name).lower() in {"app.py", "server.py", "main.py"} for name in names)
        and any("/templates/" in name.replace("\\", "/") for name in names)
    )

    if has_tool:
        content = "本地课堂工具，含课堂运行程序、分组数据和课堂素材。"
    else:
        labels = []
        if types["design"]:
            labels.append("教学设计与反思")
        if types["slides"]:
            labels.append("课件")
        if types["worksheet"]:
            labels.append("学生任务单")
        if types["images"]:
            labels.append("图片素材")
        if types["audio"]:
            labels.append("音频素材")
        if types["video"]:
            labels.append("视频素材")
        content = ("含%s。" % "、".join(labels)) if labels else "课堂配套资料。"

    if startup:
        usage = (
            "解压后双击“%s”，浏览器会自动打开；教师端地址见启动窗口。"
            "首次运行需联网并按提示安装依赖。" % startup
        )
    elif has_tool:
        usage = "解压后按包内说明启动本地服务，在浏览器中使用。"
    elif types["design"] or types["slides"] or types["worksheet"]:
        usage = "解压后按包内文件直接使用；需要完整素材时使用原压缩包。"
    else:
        usage = "解压后按包内文件说明使用。"

    extras = []
    external = external_types(resources, report["name"])
    if external and not (types["design"] or types["slides"] or types["worksheet"]):
        extras.append("%s见资源列表。" % "、".join(external[:3]))
    if types["url"]:
        extras.append("包内网页快捷方式仅保留在原包，未单独上架。")
    if any("个人信息" in warning for warning in report.get("safety", [])) or any(
        PERSONAL_FILENAME.search(base_name(name)) for name in names
    ):
        extras.append("涉及学生信息的材料仅保留在原包，请教师本地查看。")

    text = content + usage + "".join(extras)
    if len(text) > MAX_REQUIREMENTS:
        text = text[:MAX_REQUIREMENTS - 1].rstrip("，；。 ") + "。"
    return text


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--run-id", required=True)
    args = parser.parse_args()
    run_path = run_dir(args.run_id)
    inspection = read_json(run_path / "reports" / "archive-inspection.json")
    resources = read_json(run_path / "reports" / "resources.json")["resources"]
    summaries = []
    for report in inspection["archives"]:
        matched = [
            item for item in resources
            if int(item.get("postId", -1)) == int(report["postId"])
        ]
        summaries.append({
            "postId": report["postId"],
            "archiveName": report["name"],
            "sha256": report["sha256"],
            "requirements": summarize(report, matched),
        })
    output = run_path / "reports" / "archive-summaries.json"
    write_json(output, {"generatedAt": utc_now(), "summaries": summaries})
    print(json.dumps({"report": str(output), "summaries": len(summaries)}, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
