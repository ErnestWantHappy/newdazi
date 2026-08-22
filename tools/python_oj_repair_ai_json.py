#!/usr/bin/env python3
"""修复网页端 AI 把 referenceCode 内部双引号裸放进 JSON 的常见错误。"""

from __future__ import annotations

import argparse
import json
from pathlib import Path


FIELD_PREFIX = '"referenceCode":"'
FIELD_SUFFIX = '","timeLimitSeconds":'


def escape_unescaped_quotes(value: str) -> str:
    """只转义未被反斜杠保护的双引号，保留原有 JSON 转义序列。"""
    result: list[str] = []
    backslash_count = 0
    for char in value:
        if char == '"' and backslash_count % 2 == 0:
            result.append('\\')
        result.append(char)
        if char == '\\':
            backslash_count += 1
        else:
            backslash_count = 0
    return ''.join(result)


def repair_reference_code_fields(raw: str) -> tuple[str, int]:
    cursor = 0
    repaired_count = 0
    chunks: list[str] = []
    while True:
        start = raw.find(FIELD_PREFIX, cursor)
        if start < 0:
            chunks.append(raw[cursor:])
            break
        value_start = start + len(FIELD_PREFIX)
        end = raw.find(FIELD_SUFFIX, value_start)
        if end < 0:
            raise ValueError(f'referenceCode 字段缺少结束标记，位置 {start}')
        escaped = escape_unescaped_quotes(raw[value_start:end])
        # 先让 JSON 解释已有的 \n、\\ 等转义，再统一序列化，避免二次转义代码。
        code = json.loads('"' + escaped + '"')
        chunks.append(raw[cursor:start])
        chunks.append('"referenceCode":' + json.dumps(code, ensure_ascii=False))
        cursor = end + 1
        repaired_count += 1
    return ''.join(chunks), repaired_count


def repair_file(source: Path, output: Path) -> tuple[int, int]:
    raw = source.read_text(encoding='utf-8-sig').strip()
    repaired, repaired_count = repair_reference_code_fields(raw)
    questions = json.loads(repaired)
    if not isinstance(questions, list):
        raise ValueError('题包顶层必须是 JSON 数组')
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(questions, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    return repaired_count, len(questions)


def main() -> int:
    parser = argparse.ArgumentParser(description='修复网页端 AI 生成的 Python OJ 非法 JSON')
    parser.add_argument('source', type=Path)
    parser.add_argument('--output', required=True, type=Path)
    args = parser.parse_args()
    repaired_count, question_count = repair_file(args.source, args.output)
    print(f'已修复 {repaired_count} 个 referenceCode 字段，输出 {question_count} 道题：{args.output}')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
