"""精确清理本轮选择题图片验收数据，不按 owner 批量删除整表。

清理范围只来自 output/choice-media/cleanup-scope-20261108.json 的显式 ID；
执行前逐项核对归属、日期、引用和旧数据不变，任何一项不符即停止。
"""
import argparse
import gzip
import hashlib
import json
import re
from datetime import datetime
from pathlib import Path

import yaml

from migrate_local import ROOT, connect

OUT = ROOT / 'output/choice-media'
SCOPE_FILE = OUT / 'cleanup-scope-20261108.json'
BEFORE = OUT / 'backups/20261106-155601/before.sql'
BEFORE_SHA256 = 'c3e32c6c5dcb20b68be919ed13d82c10de5916e68f87de42ad1777850464b432'
ORIGINAL_COUNTS = {'biz_question': 1738, 'biz_county_exam_paper_question': 4187}
MEDIA_TABLES = ['biz_question_image', 'biz_question_save_receipt',
                'biz_question_import_batch', 'biz_question_import_item']
REVIEW_DAY = '2026-11-08'
TEACHER_LOGIN = '19157727791'
STUDENT_ALLOWED_TABLES = {'biz_student', 'biz_student_answer',
                          'biz_student_task_state', 'biz_county_exam_paper_question'}


def marks(values):
    return ','.join(['%s'] * len(values))


def media_root():
    config = yaml.safe_load(
        (ROOT / 'RuoYi-Vue/ruoyi-admin/src/main/resources/application.yml').read_text(encoding='utf-8'))
    profile = Path(str(config['ruoyi']['profile']))
    if not profile.is_absolute():
        profile = (ROOT / 'RuoYi-Vue' / profile).resolve()
    return (profile.parent / (profile.name + '-question-images')).resolve()


def load_scope():
    scope = json.loads(SCOPE_FILE.read_text(encoding='utf-8'))
    for key in ['questionIds', 'batchIds', 'receiptIds', 'imageIds',
                'lessonIds', 'userIds', 'studentIds', 'examIds']:
        values = scope.get(key) or []
        assert values, f'清理清单缺少 {key}'
        assert len(values) == len(set(values)), f'清理清单 {key} 有重复'
    assert scope['database'] == 'xueyeceping_server_20260729'
    return scope


def verify_baseline(cursor):
    assert hashlib.sha256(BEFORE.read_bytes()).hexdigest() == BEFORE_SHA256, '迁移前备份哈希变化'
    baseline, columns = {}, {}
    pattern = re.compile(r'^INSERT INTO `(biz_question|biz_county_exam_paper_question)` \((.*?)\) VALUES \((\d+),')
    for line in BEFORE.read_text(encoding='utf-8').splitlines():
        match = pattern.match(line)
        if match:
            baseline.setdefault(match[1], {})[int(match[3])] = line
            columns[match[1]] = match[2]
    unchanged = {}
    for table, old in baseline.items():
        key = 'question_id' if table == 'biz_question' else 'paper_question_id'
        cursor.execute(f'SELECT {columns[table]} FROM `{table}` WHERE `{key}`<=%s', (max(old),))
        rows = {int(r[0]): f'INSERT INTO `{table}` ({columns[table]}) VALUES (' +
                          ','.join(cursor.connection.escape(v) for v in r) + ');'
                for r in cursor.fetchall()}
        differences = [item for item, line in old.items() if rows.get(item) != line]
        assert not differences, f'旧数据发生变化，拒绝清理：{table} {differences[:8]}'
        assert len(old) == ORIGINAL_COUNTS[table], f'{table} 原行数不是 {ORIGINAL_COUNTS[table]}'
        unchanged[table] = len(old)
    return unchanged


def verify_scope(cursor, scope):
    questions = scope['questionIds']
    lessons = scope['lessonIds']
    exams = scope['examIds']
    images = scope['imageIds']
    users = scope['userIds']
    students = scope['studentIds']

    cursor.execute(f"SELECT question_id,create_by,DATE(create_time) FROM biz_question "
                   f"WHERE question_id IN ({marks(questions)})", questions)
    rows = cursor.fetchall()
    assert len(rows) == len(questions), '清单题目数量与库中不一致'
    assert {int(r[0]) for r in rows} == set(questions)
    assert all(r[1] == TEACHER_LOGIN and str(r[2]) == REVIEW_DAY for r in rows), '出现非本轮题目'
    cursor.execute("SELECT question_id FROM biz_question WHERE question_id BETWEEN %s AND %s",
                   (min(questions), max(questions)))
    assert {int(r[0]) for r in cursor.fetchall()} == set(questions), '题号区间内出现清单外题目'

    cursor.execute(f"SELECT lesson_id,lesson_title,creator_id FROM biz_lesson "
                   f"WHERE lesson_id IN ({marks(lessons)})", lessons)
    rows = cursor.fetchall()
    assert {int(r[0]) for r in rows} == set(lessons)
    assert all(int(r[2]) == 104 and ('【图片功能' in r[1]) for r in rows), '验收课程归属不符'

    cursor.execute(f"SELECT exam_id,exam_name,creator_id FROM biz_county_exam "
                   f"WHERE exam_id IN ({marks(exams)})", exams)
    rows = cursor.fetchall()
    assert {int(r[0]) for r in rows} == set(exams)
    assert all(int(r[2]) == 104 and r[1] == '【图片功能抽测快照验收】' for r in rows), '验收抽测归属不符'

    cursor.execute(f"SELECT user_id,user_name,create_by FROM sys_user "
                   f"WHERE user_id IN ({marks(users)})", users)
    rows = cursor.fetchall()
    assert {int(r[0]) for r in rows} == set(users)
    assert all(r[2] == 'choice-media-review' and r[1].startswith('cmr_') for r in rows), '验收账号归属不符'
    cursor.execute(f"SELECT student_id,user_id,entry_year,class_code FROM biz_student "
                   f"WHERE student_id IN ({marks(students)})", students)
    rows = cursor.fetchall()
    assert {int(r[0]) for r in rows} == set(students)
    assert all(int(r[1]) in set(users) and r[2] == '2099' and r[3] == '97' for r in rows), '验收学生归属不符'

    cursor.execute(f"SELECT image_id,resource_path,uploader_id,DATE(create_time) "
                   f"FROM biz_question_image WHERE image_id IN ({marks(images)})", images)
    image_rows = cursor.fetchall()
    assert {int(r[0]) for r in image_rows} == set(images), '清单图片数量与库中不一致'
    assert all(int(r[2]) == 104 and str(r[3]) == REVIEW_DAY for r in image_rows), '出现非本轮图片'
    cursor.execute('SELECT COUNT(*) FROM biz_question_image')
    assert cursor.fetchone()[0] == len(images), '图片表存在清单外记录'

    cursor.execute(f"SELECT receipt_id,owner_id,question_id FROM biz_question_save_receipt "
                   f"WHERE receipt_id IN ({marks(scope['receiptIds'])})", scope['receiptIds'])
    rows = cursor.fetchall()
    assert {int(r[0]) for r in rows} == set(scope['receiptIds'])
    assert all(int(r[1]) == 104 and int(r[2]) in set(questions) for r in rows), '保存回执归属不符'
    cursor.execute('SELECT receipt_id FROM biz_question_save_receipt WHERE owner_id=104')
    assert {int(r[0]) for r in cursor.fetchall()} == set(scope['receiptIds']), '教师 104 存在清单外回执'

    cursor.execute(f"SELECT batch_id,owner_id FROM biz_question_import_batch "
                   f"WHERE batch_id IN ({marks(scope['batchIds'])})", scope['batchIds'])
    rows = cursor.fetchall()
    assert {int(r[0]) for r in rows} == set(scope['batchIds'])
    assert all(int(r[1]) == 104 for r in rows), '导入批次归属不符'
    cursor.execute('SELECT batch_id FROM biz_question_import_batch WHERE owner_id=104')
    assert {int(r[0]) for r in cursor.fetchall()} == set(scope['batchIds']), '教师 104 存在清单外批次'

    cursor.execute(f"SELECT COUNT(*) FROM biz_lesson_question WHERE question_id IN ({marks(questions)}) "
                   f"AND lesson_id NOT IN ({marks(lessons)})", questions + lessons)
    assert cursor.fetchone()[0] == 0, '验收题被清单外课程引用'
    cursor.execute(f"SELECT COUNT(*) FROM biz_county_exam_question WHERE question_id IN ({marks(questions)}) "
                   f"AND exam_id NOT IN ({marks(exams)})", questions + exams)
    assert cursor.fetchone()[0] == 0, '验收题被清单外抽测引用'
    for image_id in images:
        pattern = '%"imageId":"' + str(image_id) + '"%'
        cursor.execute(f"SELECT COUNT(*) FROM biz_question WHERE media_json LIKE %s "
                       f"AND question_id NOT IN ({marks(questions)})", [pattern] + questions)
        assert cursor.fetchone()[0] == 0, f'图片 {image_id} 仍被清单外题目引用'
        cursor.execute(f"SELECT COUNT(*) FROM biz_county_exam_paper_question WHERE media_json LIKE %s "
                       f"AND exam_id NOT IN ({marks(exams)})", [pattern] + exams)
        assert cursor.fetchone()[0] == 0, f'图片 {image_id} 仍被清单外抽测引用'

    cursor.execute("SELECT TABLE_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() "
                   "AND COLUMN_NAME='student_id' AND TABLE_NAME LIKE 'biz_%' AND TABLE_NAME<>'biz_student'")
    used = {}
    for (table,) in cursor.fetchall():
        cursor.execute(f'SELECT COUNT(*) FROM `{table}` WHERE student_id IN ({marks(students)})', students)
        count = cursor.fetchone()[0]
        if count:
            used[table] = count
    extra = set(used) - STUDENT_ALLOWED_TABLES
    assert not extra, f'验收学生在白名单外表有数据：{sorted(extra)}'

    root = media_root()
    for image_id, resource, _, _ in image_rows:
        target = (root / resource).resolve()
        assert root in target.parents and target.is_file(), f'图片文件缺失或越界：{image_id} {resource}'

    expected = {
        ('biz_student_answer', 'student_id'): used.get('biz_student_answer', 0),
        ('biz_student_task_state', 'student_id'): used.get('biz_student_task_state', 0),
        ('biz_county_exam_paper_question', 'exam_id'): None,
        ('biz_county_exam_question', 'exam_id'): None,
        ('biz_county_exam', 'exam_id'): None,
        ('biz_lesson_question', 'lesson_id'): None,
        ('biz_lesson_assignment', 'lesson_id'): None,
        ('biz_lesson', 'lesson_id'): None,
        ('biz_question_import_item', 'batch_id'): None,
        ('biz_question_import_batch', 'batch_id'): None,
        ('biz_question_save_receipt', 'receipt_id'): None,
        ('biz_question', 'question_id'): None,
        ('biz_question_image', 'image_id'): None,
        ('sys_user_role', 'user_id'): None,
        ('biz_student', 'user_id'): None,
        ('sys_user', 'user_id'): None,
    }
    expected_args = {
        ('biz_county_exam_paper_question', 'exam_id'): exams,
        ('biz_county_exam_question', 'exam_id'): exams,
        ('biz_county_exam', 'exam_id'): exams,
        ('biz_lesson_question', 'lesson_id'): lessons,
        ('biz_lesson_assignment', 'lesson_id'): lessons,
        ('biz_lesson', 'lesson_id'): lessons,
        ('biz_question_import_item', 'batch_id'): scope['batchIds'],
        ('biz_question_import_batch', 'batch_id'): scope['batchIds'],
        ('biz_question_save_receipt', 'receipt_id'): scope['receiptIds'],
        ('biz_question', 'question_id'): questions,
        ('biz_question_image', 'image_id'): images,
        ('sys_user_role', 'user_id'): users,
        ('biz_student', 'user_id'): users,
        ('sys_user', 'user_id'): users,
    }
    for key, args in expected_args.items():
        table, column = key
        cursor.execute(f'SELECT COUNT(*) FROM `{table}` WHERE `{column}` IN ({marks(args)})', args)
        expected[key] = cursor.fetchone()[0]
    for key, count in expected.items():
        assert count is not None and count > 0, f'预期删除行为零，拒绝执行：{key}'
    return expected, used, image_rows


def dump_rows(cursor, table, column, ids):
    cursor.execute(f'SELECT * FROM `{table}` WHERE `{column}` IN ({marks(ids)})', ids)
    names = [item[0] for item in cursor.description]
    return [dict(zip(names, row)) for row in cursor.fetchall()]


def main():
    args = argparse.ArgumentParser()
    args.add_argument('--apply', action='store_true')
    options = args.parse_args()
    scope = load_scope()
    db, database = connect()
    try:
        with db.cursor() as cursor:
            unchanged = verify_baseline(cursor)
            expected, student_usage, image_rows = verify_scope(cursor, scope)
            plan = {
                'database': database,
                'scope': str(SCOPE_FILE.relative_to(ROOT)),
                'scopeSha256': hashlib.sha256(SCOPE_FILE.read_bytes()).hexdigest(),
                'originalUnchanged': unchanged,
                'toDelete': {f'{table}.{column}': count for (table, column), count in expected.items()},
                'studentUsage': student_usage,
                'mediaRoot': str(media_root()),
            }
            if not options.apply:
                print(json.dumps(plan, ensure_ascii=False, indent=2))
                return

            stamp = datetime.now().strftime('%Y%m%d-%H%M%S')
            backup = OUT / f'cleanup-deleted-{stamp}.json.gz'
            deleted = {
                'biz_question': dump_rows(cursor, 'biz_question', 'question_id', scope['questionIds']),
                'biz_question_image': dump_rows(cursor, 'biz_question_image', 'image_id', scope['imageIds']),
                'biz_question_save_receipt': dump_rows(cursor, 'biz_question_save_receipt', 'receipt_id', scope['receiptIds']),
                'biz_question_import_batch': dump_rows(cursor, 'biz_question_import_batch', 'batch_id', scope['batchIds']),
                'biz_question_import_item': dump_rows(cursor, 'biz_question_import_item', 'batch_id', scope['batchIds']),
                'biz_lesson_question': dump_rows(cursor, 'biz_lesson_question', 'lesson_id', scope['lessonIds']),
                'biz_lesson_assignment': dump_rows(cursor, 'biz_lesson_assignment', 'lesson_id', scope['lessonIds']),
                'biz_lesson': dump_rows(cursor, 'biz_lesson', 'lesson_id', scope['lessonIds']),
                'biz_county_exam_paper_question': dump_rows(cursor, 'biz_county_exam_paper_question', 'exam_id', scope['examIds']),
                'biz_county_exam_question': dump_rows(cursor, 'biz_county_exam_question', 'exam_id', scope['examIds']),
                'biz_county_exam': dump_rows(cursor, 'biz_county_exam', 'exam_id', scope['examIds']),
                'biz_student_answer': dump_rows(cursor, 'biz_student_answer', 'student_id', scope['studentIds']),
                'biz_student_task_state': dump_rows(cursor, 'biz_student_task_state', 'student_id', scope['studentIds']),
                'biz_student': dump_rows(cursor, 'biz_student', 'user_id', scope['userIds']),
                'sys_user': dump_rows(cursor, 'sys_user', 'user_id', scope['userIds']),
                'sys_user_role': dump_rows(cursor, 'sys_user_role', 'user_id', scope['userIds']),
            }
            with gzip.open(backup, 'wt', encoding='utf-8') as stream:
                json.dump(deleted, stream, ensure_ascii=False, default=str)

            def remove(table, column, ids):
                cursor.execute(f'DELETE FROM `{table}` WHERE `{column}` IN ({marks(ids)})', ids)
                actual = cursor.rowcount
                assert actual == expected[(table, column)], \
                    f'删除行数不符 {table}.{column}: {actual} != {expected[(table, column)]}'
                return actual

            try:
                removed = {}
                for table in ['biz_student_answer', 'biz_student_task_state']:
                    removed[f'{table}.student_id'] = remove(table, 'student_id', scope['studentIds'])
                for table in ['biz_county_exam_paper_question', 'biz_county_exam_question', 'biz_county_exam']:
                    removed[f'{table}.exam_id'] = remove(table, 'exam_id', scope['examIds'])
                for table in ['biz_lesson_question', 'biz_lesson_assignment', 'biz_lesson']:
                    removed[f'{table}.lesson_id'] = remove(table, 'lesson_id', scope['lessonIds'])
                removed['biz_question_import_item.batch_id'] = remove(
                    'biz_question_import_item', 'batch_id', scope['batchIds'])
                removed['biz_question_import_batch.batch_id'] = remove(
                    'biz_question_import_batch', 'batch_id', scope['batchIds'])
                removed['biz_question_save_receipt.receipt_id'] = remove(
                    'biz_question_save_receipt', 'receipt_id', scope['receiptIds'])
                removed['biz_question.question_id'] = remove(
                    'biz_question', 'question_id', scope['questionIds'])
                removed['biz_question_image.image_id'] = remove(
                    'biz_question_image', 'image_id', scope['imageIds'])
                removed['sys_user_role.user_id'] = remove('sys_user_role', 'user_id', scope['userIds'])
                removed['biz_student.user_id'] = remove('biz_student', 'user_id', scope['userIds'])
                removed['sys_user.user_id'] = remove('sys_user', 'user_id', scope['userIds'])
                db.commit()
            except Exception:
                db.rollback()
                raise

            root = media_root()
            removed_files = []
            for image_id, resource, _, _ in image_rows:
                target = (root / resource).resolve()
                assert root in target.parents
                target.unlink()
                removed_files.append(str(target))

            after = {}
            for table, expected_count in ORIGINAL_COUNTS.items():
                cursor.execute(f'SELECT COUNT(*) FROM `{table}`')
                after[table] = cursor.fetchone()[0]
                assert after[table] == expected_count, f'{table} 清理后行数不是 {expected_count}'
            for table in MEDIA_TABLES:
                cursor.execute(f'SELECT COUNT(*) FROM `{table}`')
                after[table] = cursor.fetchone()[0]
                assert after[table] == 0, f'{table} 清理后未归零'
            assert len(set(removed_files)) == len(image_rows)

            result = {
                'status': 'CLEANED',
                'database': database,
                'scopeSha256': plan['scopeSha256'],
                'originalUnchanged': unchanged,
                'removed': removed,
                'removedFiles': len(removed_files),
                'backup': str(backup.relative_to(ROOT)),
                'backupSha256': hashlib.sha256(backup.read_bytes()).hexdigest(),
                'after': after,
            }
            (OUT / 'cleanup-result.json').write_text(
                json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
            print(json.dumps(result, ensure_ascii=False, indent=2))
    finally:
        db.close()


if __name__ == '__main__':
    main()
