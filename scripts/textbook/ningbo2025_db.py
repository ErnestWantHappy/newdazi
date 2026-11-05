"""2025宁波课例批次：只读数据库辅助（教材目录、基线快照）。不打印凭据。

用法：
  python scripts/textbook/ningbo2025_db.py textbook <输出json>
  python scripts/textbook/ningbo2025_db.py baseline <输出json>
"""
import json
import re
import sys
from pathlib import Path

import pymysql
import yaml

ROOT = Path(__file__).resolve().parents[2]
CONFIG = ROOT / "RuoYi-Vue/ruoyi-admin/src/main/resources/application-druid.yml"


def connect():
    master = yaml.safe_load(CONFIG.read_text(encoding="utf-8"))["spring"]["datasource"]["druid"]["master"]
    match = re.match(r"jdbc:mysql://([^:/]+):(\d+)/([^?]+)", master["url"])
    if not match:
        raise RuntimeError("无法解析数据库地址")
    return pymysql.connect(host=match[1], port=int(match[2]), user=master["username"],
                           password=str(master["password"]), database=match[3], charset="utf8mb4"), match[3]


def textbook(connection):
    with connection.cursor(pymysql.cursors.DictCursor) as cursor:
        cursor.execute("SELECT book_id,book_code,title,short_title,grade,semester,status,sort_no FROM biz_textbook_book ORDER BY sort_no")
        books = cursor.fetchall()
        cursor.execute("SELECT s.section_id,s.book_id,s.section_key,s.kind,s.lesson_no,s.title,s.sort_no "
                       "FROM biz_textbook_section s JOIN biz_textbook_book b ON b.book_id=s.book_id "
                       "ORDER BY b.sort_no,s.sort_no")
        sections = cursor.fetchall()
    by_book = {}
    for section in sections:
        by_book.setdefault(section["book_id"], []).append({
            "sectionKey": section["section_key"], "kind": section["kind"],
            "lessonNo": section["lesson_no"], "title": section["title"]})
    return [{
        "bookId": book["book_id"], "bookCode": book["book_code"], "title": book["title"],
        "shortTitle": book["short_title"], "grade": book["grade"], "semester": book["semester"],
        "status": book["status"],
        "lessons": [item for item in by_book.get(book["book_id"], []) if item["kind"] == "LESSON"],
        "otherSections": [item for item in by_book.get(book["book_id"], []) if item["kind"] != "LESSON"],
    } for book in books]


def baseline(connection):
    with connection.cursor(pymysql.cursors.DictCursor) as cursor:
        cursor.execute("SELECT COUNT(*) c FROM biz_textbook_case")
        cases = cursor.fetchone()["c"]
        cursor.execute("SELECT COUNT(*) c FROM biz_textbook_case_resource")
        resources = cursor.fetchone()["c"]
        cursor.execute("SELECT case_id,source_key,book_id,section_key,title,author,school,status FROM biz_textbook_case ORDER BY case_id")
        case_rows = cursor.fetchall()
        cursor.execute("SELECT resource_id,case_id,resource_key,category,title,filename,original_path,preview_path,bytes "
                       "FROM biz_textbook_case_resource ORDER BY resource_id")
        resource_rows = cursor.fetchall()
        cursor.execute("SELECT MAX(resource_id) m FROM biz_textbook_case_resource")
        max_resource = cursor.fetchone()["m"]
        cursor.execute("SHOW TABLES LIKE 'biz_textbook_case_feedback'")
        feedback_table = bool(cursor.fetchall())
        feedback = []
        if feedback_table:
            cursor.execute("SELECT COUNT(*) c FROM biz_textbook_case_feedback")
            feedback.append(cursor.fetchone()["c"])
        cursor.execute("SHOW TABLES LIKE 'biz_textbook_case_invitation'")
        invitation_supported = bool(cursor.fetchall())
    return {"caseCount": cases, "resourceCount": resources, "maxResourceId": max_resource,
            "cases": case_rows, "resources": resource_rows,
            "feedbackCount": feedback[0] if feedback else None,
            "invitationTable": invitation_supported}


def main():
    action = sys.argv[1]
    connection, database = connect()
    try:
        if action == "textbook":
            data = textbook(connection)
        elif action == "baseline":
            data = baseline(connection)
        else:
            raise SystemExit("未知子命令")
        data = {"database": database, "items": data} if action == "textbook" else {"database": database, **data}
        Path(sys.argv[2]).parent.mkdir(parents=True, exist_ok=True)
        Path(sys.argv[2]).write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
        print("已写出 " + sys.argv[2])
    finally:
        connection.close()


if __name__ == "__main__":
    main()
