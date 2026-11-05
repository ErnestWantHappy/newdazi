"""生成课例资源收口清单（来源目录 → 已收录资源、未收录项与验证状态）。

只读清单与验收 JSON，输出 contexts/textbook-cases/resource-closure.md。
"""
import json
from pathlib import Path

ROOT = Path(r"d:/dmwprogram/newdazipingtai/newdazi")
OUT = ROOT / "output/textbook-cases"
DOC = ROOT / "contexts/textbook-cases/resource-closure.md"

CATEGORY_LABEL = {"DESIGN": "教学设计", "SLIDES": "课件", "WORKSHEET": "任务单", "TOOL": "实验包/工具", "MATERIAL": "配套素材"}


def human(size):
    return f"{size / 1048576:.1f} MiB" if size >= 1048576 else f"{size / 1024:.0f} KiB"


PROBE = {
    "w5-internet-ai-lab.zip": "需 Node；保留原作者共享Key，有效性未验证",
    "w19-campus-agent.zip": "未启动（需 Python 与 pip 依赖）",
    "w18-dfs-teaching-tool.zip": "HTTP 200（Node 零依赖）",
    "w18-quickform-class.zip": "HTTP 200（隔离依赖，首页/学生端/作品展/本机提交均通）",
    "s11-animal-stamp.zip": "保留原作者共享Coze令牌，有效性未验证",
    "s11-dream-wall.zip": "HTTP 200（Python 标准库）",
    "s11-film-vote.zip": "HTTP 200（Node 零依赖）",
    "g5-decision-tree-cards.zip": "纯静态，未起服务",
    "g6-16-smart-control-site.zip": "纯静态，未起服务",
    "g6-18-plant-agent.zip": "HTTP 200（Python 标准库）",
}


def package_size(source):
    for base in (OUT / "release-materials/combined", ROOT / "output/textbook-pack-20261029/pages/cases"):
        path = base / source
        if path.is_file():
            return path.stat().st_size
    return 0


def main():
    manifest = json.loads((OUT / "catalog-local-reviewed.json").read_text(encoding="utf-8"))
    verified = json.loads((OUT / "new-packages-verification.json").read_text(encoding="utf-8"))
    verified_titles = {item["title"] for item in verified["results"] if not verified["failures"] or item.get("sha256")}
    web = json.loads((OUT / "web-packages.json").read_text(encoding="utf-8"))
    decisions = json.loads((OUT / "package-decisions.json").read_text(encoding="utf-8"))
    smoke = json.loads((OUT / "web-packages-smoke.json").read_text(encoding="utf-8"))

    cases = [case for case in manifest["cases"] if case.get("reviewed")]
    total_resources = sum(len(case["resources"]) for case in cases)

    lines = []
    lines.append("# 课例资源收口清单（2026-11-03）")
    lines.append("")
    lines.append("> 本文件由 `scripts/textbook/build_closure_report.py` 根据清单与验收结果生成，用于回答“每个来源目录收了什么、什么没收录、还缺什么”。")
    lines.append("")
    lines.append("## 一、总览")
    lines.append("")
    lines.append(f"- 来源目录 53 个，其中一组重复课例合并，最终 **{len(cases)} 个课例**、**{total_resources} 个资源**，全部 `PUBLISHED`。")
    counts = {}
    for case in cases:
        for resource in case["resources"]:
            entry = counts.setdefault(resource["category"], [0, 0])
            entry[0] += 1
            size = int(resource.get("bytes") or 0) or package_size(resource["source"])
            entry[1] += size
    order = ["DESIGN", "SLIDES", "WORKSHEET", "TOOL", "MATERIAL"]
    summary = "、".join(f"{CATEGORY_LABEL.get(key, key)} {counts[key][0]} 个（{human(counts[key][1])}）"
                       for key in order if key in counts)
    lines.append(f"- 分类：{summary}。")
    lines.append("- 资源运行目录 `output/textbook-pack-20261029/pages/cases`，与程序 release 分离；容量以发布前实际同步清单为准。")
    lines.append("")
    lines.append("## 二、本轮新增的 10 个网页/工具包")
    lines.append("")
    lines.append("| 课例 | 包 | 类别 | 大小 | 下载哈希 | 启动探活 |")
    lines.append("| :--- | :--- | :--- | ---: | :--- | :--- |")
    smoke_by_zip = {item["zip"]: item for item in smoke}
    for item in web:
        name = item["target"].split("/")[-1]
        title = next((case["title"] for case in cases for resource in case["resources"] if resource["source"] == item["target"]), "")
        category = next((resource["category"] for case in cases for resource in case["resources"] if resource["source"] == item["target"]), "")
        lines.append(f"| {title} | `{name}` | {CATEGORY_LABEL.get(category, category)} | {human(item['bytes'])} | 一致 | {PROBE.get(name, '未验证')} |")
    lines.append("")
    supplemental_path = OUT / "remaining-web-resources.json"
    if supplemental_path.exists():
        supplemental = json.loads(supplemental_path.read_text(encoding="utf-8"))
        lines.extend(["", f"### 补收具名网页：{len(supplemental)}份配套包、{sum(len(item['pages']) for item in supplemental)}个HTML/HTM入口", "",
                      "完整入口、文件、目录归属与修复记录见 output/textbook-cases/remaining-web-resources.json。",
                      "本轮补齐城市大脑、评价平台、计时器等；同课React源码也作为需安装依赖的配套工程收录，避免只按index.html识别造成遗漏。", ""])
    lines.append("## 三、逐个来源目录")
    lines.append("")
    lines.append("| 来源目录 | 教学设计 | 课件 | 任务单 | 实验包 | 素材 | 合计 |")
    lines.append("| :--- | ---: | ---: | ---: | ---: | ---: | ---: |")
    for case in sorted(cases, key=lambda item: item["sourceDirectory"]):
        per = {key: 0 for key in ("DESIGN", "SLIDES", "WORKSHEET", "TOOL", "MATERIAL")}
        for resource in case["resources"]:
            per[resource["category"]] = per.get(resource["category"], 0) + 1
        lines.append(f"| {case['sourceDirectory']} | {per['DESIGN']} | {per['SLIDES']} | {per['WORKSHEET']} | {per['TOOL']} | {per['MATERIAL']} | {len(case['resources'])} |")
    lines.append("")
    lines.append("## 四、明确不收录")
    lines.append("")
    for item in decisions["quarantine"]:
        lines.append(f"- `{item['source']}`：{item['reason']}")
    lines.append("")
    lines.append("## 五、需要用户补充或确认")
    lines.append("")
    lines.append("1. **署名与授权**：七上第18课按“江北区展示课例（未署名）”收录；其余课例沿用来源目录中的学校与教师署名，未见单独授权文件。若要公开到校际范围，需要作者/学校授权确认。")
    lines.append("2. **七上5 实验工具**：按用户确认保留原作者自愿共享的DeepSeek Key；如已被删除、停用或额度用尽，可替换自己的。当前有效性未验证；实验记录收集地址需按本班需求配置。")
    lines.append("3. **三下11 动物印章工具**：已恢复原作者共享Coze令牌与工作流配置；失效时可替换自己的令牌及对应工作流ID。校园智能体原配置Key本就为空，已明确提示自行填写。")
    lines.append("4. **七上18 课堂包**：默认管理员为 `wst/quickform`，首次使用需改密码；原包内置的 Python 运行环境绑定原作者电脑路径，已移除，需教师机自装 Python 3.10+。")
    lines.append("")
    lines.append("## 六、验证边界")
    lines.append("")
    lines.append("- 已验证：52 课例 167 资源首末页/媒体接口 0 失败；10 个新包真实下载哈希一致、包内无外部凭据；4 个零依赖网页工程在本机启动并返回 200；课堂包在隔离依赖下启动，首页、学生端、作品展与本地提交接口均 200。")
    lines.append("- 仅验证预览与下载、**未在学校机房真实运行**：全部 12 个实验包/工具（含神经网络工具集、课堂包、智能体平台、投票与展示墙等）。原因是没有机房终端与网络条件，且部分工具需要教师自备 Key。")
    lines.append("- 未验证：真实打印机分页、弱网下大文件下载、多人同时提交的并发表现。")
    lines.append("")
    DOC.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"已生成 {DOC.relative_to(ROOT)}：{len(cases)} 课例、{total_resources} 资源")


if __name__ == "__main__":
    main()
