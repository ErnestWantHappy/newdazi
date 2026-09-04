#!/usr/bin/env python3
"""从主提示词和 120 题蓝图生成可直接粘贴到网页端 AI 的单批提示词。"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
CONTEXT_DIR = ROOT / "contexts" / "python-judge0"
PROMPT_FILE = CONTEXT_DIR / "python-system-question-generation-prompt.md"
BLUEPRINT_FILE = CONTEXT_DIR / "python-system-question-blueprint-v2.json"


def main() -> int:
    parser = argparse.ArgumentParser(description="生成 Python V2 系统题单批网页端提示词")
    parser.add_argument("--batch", type=int, required=True, choices=range(1, 7), metavar="1-6")
    parser.add_argument("--output", type=Path, help="写入 UTF-8 文本文件；省略时打印到终端")
    args = parser.parse_args()

    prompt_text = PROMPT_FILE.read_text(encoding="utf-8")
    start_marker = "## 主提示词"
    end_marker = "## 每批追加参数模板"
    start = prompt_text.index(start_marker) + len(start_marker)
    end = prompt_text.index(end_marker)
    main_prompt = prompt_text[start:end].strip()

    blueprint = json.loads(BLUEPRINT_FILE.read_text(encoding="utf-8"))
    if len(blueprint) != 120:
        raise RuntimeError("V2 蓝图必须恰好包含 120 道题")
    offset = (args.batch - 1) * 20
    batch_rows = blueprint[offset: offset + 20]
    first_id = batch_rows[0]["externalId"]
    last_id = batch_rows[-1]["externalId"]
    result = (
        main_prompt
        + "\n\n以下是本批不可修改的题目蓝图：\n\n"
        + json.dumps(batch_rows, ensure_ascii=False, indent=2)
        + f"\n\n本批编号范围：{first_id} 至 {last_id}"
        + "\n本批数量：20 道"
        + "\n允许的前置知识：只使用蓝图题目之前已经出现的 Python 基础知识。"
        + "\n最终只返回 20 个完整题目对象组成的合法 JSON 数组，不要 Markdown 围栏或解释。\n"
    )
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(result, encoding="utf-8")
        print(f"已生成第 {args.batch} 批提示词：{args.output}")
    else:
        print(result, end="")
    return 0


if __name__ == "__main__":
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    raise SystemExit(main())
