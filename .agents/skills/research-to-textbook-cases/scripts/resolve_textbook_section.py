"""按年级、学期、教材版本、课次和目录标题解析唯一教材课次。"""
from __future__ import annotations

import argparse
import json
import re

from common import OUTPUT_ROOT, read_json, utc_now, write_json


BOOK_CODE = {
    (4, "1"): "xxkj-4-0-2026",
    (8, "2"): "xxkj-8-1-2026",
}


def normalize_title(value: str) -> str:
    return re.sub(r"[\s《》（）()]+", "", value or "")


def title_matches(course_title: str, section_title: str) -> bool:
    course = normalize_title(course_title)
    section = normalize_title(section_title)
    return bool(course and section and (course in section or section in course))


def section_for(post: dict, sections: dict[str, list[dict]]) -> tuple[dict | None, str]:
    grade = int(post["grade"])
    semester = str(post["semester"])
    book_code = BOOK_CODE.get((grade, semester))
    if not book_code:
        return None, "PENDING: 年级/学期没有已确认教材版本"
    lesson_no = int(post["lessonNo"])
    if lesson_no <= 0:
        return None, "PENDING: 课次缺失"
    candidates = [item for item in sections.get(book_code, [])
                  if item["sectionKey"] == "lesson-%d" % lesson_no]
    if len(candidates) != 1:
        return None, "PENDING: 课次候选不是唯一结果"
    section = candidates[0]
    if not title_matches(post["courseTitle"], section["title"]):
        return None, "PENDING: 课程标题与教材目录标题不一致"
    return {
        "bookCode": book_code,
        "bookId": int(section["bookId"]),
        "bookTitle": section["bookTitle"],
        "sectionKey": section["sectionKey"],
        "sectionTitle": section["title"],
    }, "RESOLVED"


def load_sections() -> dict[str, list[dict]]:
    from common import mysql_readonly, parse_json_lines
    sql = r"""
    SELECT JSON_OBJECT(
      'bookCode', b.book_code,
      'bookId', b.book_id,
      'bookTitle', b.title,
      'sectionKey', s.section_key,
      'title', s.title
    )
    FROM biz_textbook_book b
    JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.kind='LESSON'
    WHERE b.book_code IN ('xxkj-4-0-2026','xxkj-8-1-2026') AND b.status='0'
    ORDER BY b.book_code, s.section_key;
    """
    result: dict[str, list[dict]] = {}
    for item in parse_json_lines(mysql_readonly(sql)):
        result.setdefault(item["bookCode"], []).append(item)
    return result


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--run-id", required=True)
    parser.add_argument("--sections-json", help="单测或离线复核用；默认从正式库读取")
    args = parser.parse_args()

    scan = read_json(OUTPUT_ROOT / ("scan-%s.json" % args.run_id))
    sections = read_json(Path(args.sections_json)) if args.sections_json else load_sections()
    resolved = []
    for post in scan["posts"]:
        if post["state"] != "CANDIDATE":
            resolved.append({"postId": post["postId"], "state": post["state"], "reason": post["reason"]})
            continue
        section, state = section_for(post, sections)
        entry = {
            "postId": post["postId"],
            "state": state,
            "courseTitle": post["courseTitle"],
            "resolved": section,
            "postUpdatedAt": post["postUpdatedAt"],
        }
        if section is None:
            entry["reason"] = state
        resolved.append(entry)
    output = OUTPUT_ROOT / ("sections-%s.json" % args.run_id)
    write_json(output, {"generatedAt": utc_now(), "items": resolved})
    print(json.dumps({"report": str(output),
                      "resolved": [item["postId"] for item in resolved if item["state"] == "RESOLVED"]},
                     ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    from pathlib import Path
    raise SystemExit(main())
