# -*- coding: utf-8 -*-
"""本机应用高中教材与课程标准：先备份，页图/课标就绪后再提交数据库并复核。"""

import datetime
import hashlib
import io
import os
import re
import shutil
import subprocess
from pathlib import Path

import fitz
import pymysql
import yaml
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
STAGE = ROOT / "output" / "high-school-textbook-pack-20261109"
SQL = ROOT / "sql" / "high_school_textbooks_20261109.sql"
TARGET_ROOT = ROOT / "output" / "textbook-pack-20261029" / "pages"
BOOK_CODES = (("xxkj-hs-b1-2019", 193), ("xxkj-hs-b2-2019", 161), ("xxkj-hs-e1-2019", 185))


def sha256_file(path):
    digest = hashlib.sha256()
    with path.open("rb") as fp:
        for block in iter(lambda: fp.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def cover_bytes(source):
    with Image.open(source) as image:
        ratio = 360 / image.width
        resized = image.resize((360, max(1, round(image.height * ratio))))
        buffer = io.BytesIO()
        resized.convert("RGB").save(buffer, format="JPEG", quality=85)
        return buffer.getvalue()


def sync_file(source, target):
    """按内容哈希幂等同步单文件，失败时保留旧文件而不是覆盖到一半。"""
    digest = sha256_file(source)
    target.parent.mkdir(parents=True, exist_ok=True)
    if target.exists() and sha256_file(target) == digest:
        return False
    temp = target.with_name(target.name + ".part")
    shutil.copy2(source, temp)
    if sha256_file(temp) != digest:
        temp.unlink(missing_ok=True)
        raise RuntimeError(f"文件同步哈希不一致: {target}")
    os.replace(temp, target)
    return True


def sync_directory(source, target):
    """资源先就绪；同名目录已存在时逐文件校验补齐，不把重跑当成致命错误。"""
    if not source.is_dir():
        raise RuntimeError(f"源资源目录不存在: {source}")
    files = [item for item in source.rglob("*") if item.is_file()]
    if not files:
        raise RuntimeError(f"源资源目录为空: {source}")
    copied = 0
    for item in files:
        relative = item.relative_to(source)
        copied += int(sync_file(item, target / relative))
    return len(files), copied


def write_bytes_atomic(target, content):
    target.parent.mkdir(parents=True, exist_ok=True)
    temp = target.with_name(target.name + ".part")
    temp.write_bytes(content)
    os.replace(temp, target)


def main():
    config = yaml.safe_load((ROOT / "RuoYi-Vue/ruoyi-admin/src/main/resources/application-druid.yml").read_text(encoding="utf-8"))
    master = config["spring"]["datasource"]["druid"]["master"]
    match = re.match(r"jdbc:mysql://([^:/]+):(\d+)/([^?]+)", master["url"])
    if not match or match[1] not in {"localhost", "127.0.0.1"} or match[3] != "xueyeceping_server_20260729":
        raise RuntimeError("仅允许本机开发库 xueyeceping_server_20260729")
    backup_dir = STAGE / "backups" / datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
    backup_dir.mkdir(parents=True, exist_ok=True)
    dump = backup_dir / "before_high_school_textbooks.sql"
    env = os.environ.copy()
    env["MYSQL_PWD"] = str(master["password"])
    result = subprocess.run([r"D:/MySQL/MySQL Server 8.0/bin/mysqldump.exe", f"--host={match[1]}", f"--port={match[2]}",
                             f"--user={master['username']}", "--single-transaction", "--routines", "--triggers",
                             "--set-gtid-purged=OFF", match[3]], stdout=dump.open("wb"), stderr=subprocess.PIPE, env=env)
    if result.returncode or dump.stat().st_size < 1_000_000:
        raise RuntimeError(f"备份失败 exit={result.returncode}, size={dump.stat().st_size}, err={result.stderr.decode('utf-8', errors='ignore')}")
    print(f"备份 {dump} {dump.stat().st_size}B SHA256={sha256_file(dump)}")

    # 先把页图和课程标准同步到 TEXTBOOK_ROOT，再提交数据库；中断后重跑会复用已校验文件。
    page_files = 0
    for code, expected in BOOK_CODES:
        count, copied = sync_directory(STAGE / "pages" / code, TARGET_ROOT / code)
        if count != expected:
            raise RuntimeError(f"{code} 源页图 {count} != {expected}")
        page_files += count
        print(f"pages {code}: {count} files, copied {copied}")
    standards_target = TARGET_ROOT / "cases" / "standards"
    standard_files = sorted((STAGE / "cases" / "standards").glob("*.pdf"))
    if len(standard_files) != 2:
        raise RuntimeError(f"课程标准源文件应为 2 份，实际 {len(standard_files)}")
    for source in standard_files:
        sync_file(source, standards_target / source.name)
    print(f"standards: {len(standard_files)} files")

    conn = pymysql.connect(host=match[1], port=int(match[2]), user=master["username"], password=str(master["password"]), database=match[3], charset="utf8mb4")
    try:
        with conn.cursor() as cursor:
            for statement in SQL.read_text(encoding="utf-8").split(";"):
                if statement.strip():
                    cursor.execute(statement)
            cursor.execute("SELECT book_code,total_pages FROM biz_textbook_book WHERE book_code IN %s", (tuple(code for code, _ in BOOK_CODES),))
            books = dict(cursor.fetchall())
            if len(books) != 3 or any(books[code] != pages for code, pages in BOOK_CODES):
                raise RuntimeError(f"教材入库复核失败: {books}")
            for code, expected in BOOK_CODES:
                cursor.execute("SELECT COUNT(*) FROM biz_textbook_page p JOIN biz_textbook_book b ON b.book_id=p.book_id WHERE b.book_code=%s", (code,))
                count = cursor.fetchone()[0]
                if count != expected:
                    raise RuntimeError(f"{code} 页记录 {count} != {expected}")
            cursor.execute("SELECT standard_key,bytes FROM biz_textbook_standard WHERE status='0' ORDER BY sort_no")
            standards = cursor.fetchall()
            if len(standards) != 2:
                raise RuntimeError(f"课程标准复核失败: {standards}")
            cursor.execute("SELECT b.book_code,b.grade,b.semester,b.page_offset,b.total_pages FROM biz_textbook_book b WHERE b.book_code IN %s", (tuple(code for code, _ in BOOK_CODES),))
            if len(cursor.fetchall()) != 3:
                raise RuntimeError("高中教材元数据不完整")
            cursor.execute("SELECT book_id,book_code FROM biz_textbook_book WHERE book_code IN %s ORDER BY sort_no", (tuple(code for code, _ in BOOK_CODES),))
            for book_id, code in cursor.fetchall():
                target = TARGET_ROOT / "covers" / f"{book_id}.jpg"
                write_bytes_atomic(target, cover_bytes(STAGE / "pages" / code / "p001.jpg"))
                print(f"cover {code}: {target} {target.stat().st_size}B")
        # 所有页图、课标和封面均已就绪后才提交数据库。
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()
    print(f"本机数据库与资源应用完成：pages={page_files}, standards={len(standard_files)}")


if __name__ == "__main__":
    main()
