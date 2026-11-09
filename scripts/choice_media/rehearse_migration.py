"""在新建的本机临时库上演练选择题图片增量：导入备份、首跑、重跑、回退条件、旧数据比对。

只允许 localhost，且库名必须带 rehearsal 前缀，避免误连开发库或正式库。
"""
import argparse
import hashlib
import json
import re
import shutil
import subprocess
from pathlib import Path

import pymysql
import yaml

ROOT = Path(__file__).resolve().parents[2]
LEGACY_TABLES = ['biz_question', 'biz_county_exam_paper_question']
MEDIA_TABLES = ['biz_question_image', 'biz_question_save_receipt', 'biz_question_import_batch', 'biz_question_import_item']
EXPECTED_COLUMNS = {
    'biz_question': ['media_json'],
    'biz_county_exam_paper_question': ['media_json'],
}
EXPECTED_KEYS = {
    'biz_question_image': ['PRIMARY', 'uk_question_image_path', 'idx_question_image_owner', 'idx_question_image_cleanup'],
    'biz_question_save_receipt': ['PRIMARY', 'uk_question_save_request'],
    'biz_question_import_batch': ['PRIMARY', 'uk_question_import_request', 'idx_question_import_owner', 'idx_question_import_hash'],
    'biz_question_import_item': ['PRIMARY', 'uk_question_import_source', 'idx_question_import_items'],
}


def server_config():
    config = yaml.safe_load((ROOT / 'RuoYi-Vue/ruoyi-admin/src/main/resources/application-druid.yml').read_text(encoding='utf-8'))
    master = config['spring']['datasource']['druid']['master']
    match = re.match(r'jdbc:mysql://([^:/]+):(\d+)/', master['url'])
    if not match or match[1] not in {'localhost', '127.0.0.1'}:
        raise RuntimeError('拒绝连接非本机 MySQL')
    return {'host': match[1], 'port': int(match[2]), 'user': master['username'], 'password': str(master['password'])}


def statements(sql_text):
    body = '\n'.join(line for line in sql_text.splitlines() if not line.lstrip().startswith('--'))
    return [part.strip() for part in body.split(';') if part.strip()]


def table_digest(cursor, table, column_list=None):
    """按主键排序对全表算摘要，用于判断迁移是否改动了旧数据。

    必须按迁移前的列清单比对：新增的 media_json 列会让 SELECT * 结果多一个 NULL，
    直接比整行会造成“数据被改写”的假警报。
    """
    cursor.execute(f'SHOW KEYS FROM `{table}` WHERE Key_name = \'PRIMARY\'')
    keys = [row[4] for row in cursor.fetchall()]
    order = ','.join(f'`{key}`' for key in keys) if keys else '1'
    projection = ','.join(f'`{name}`' for name in column_list) if column_list else '*'
    cursor.execute(f'SELECT {projection} FROM `{table}` ORDER BY {order}')
    digest = hashlib.sha256()
    rows = 0
    for row in cursor.fetchall():
        digest.update(repr(row).encode('utf-8'))
        rows += 1
    return rows, digest.hexdigest()


def columns(cursor, table):
    cursor.execute('SELECT column_name, is_nullable, column_type FROM information_schema.columns'
                   ' WHERE table_schema=DATABASE() AND table_name=%s ORDER BY ordinal_position', (table,))
    return cursor.fetchall()


def keys(cursor, table):
    cursor.execute('SHOW KEYS FROM `%s`' % table)
    return [row[2] for row in cursor.fetchall()]


def existing_tables(cursor):
    cursor.execute("SELECT table_name FROM information_schema.tables WHERE table_schema=DATABASE()")
    return {row[0] for row in cursor.fetchall()}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--backup', default='output/choice-media/backups/20261106-155601/before.sql')
    parser.add_argument('--keep', action='store_true', help='保留临时库以便人工查看')
    options = parser.parse_args()

    backup = ROOT / options.backup
    if not backup.is_file():
        raise SystemExit(f'备份文件不存在：{backup}')
    backup_sha = hashlib.sha256(backup.read_bytes()).hexdigest()

    server = server_config()
    database = f'choice_media_rehearsal_{hashlib.sha1(backup_sha.encode()).hexdigest()[:10]}'
    if 'rehearsal' not in database:
        raise SystemExit('临时库名异常，拒绝执行')

    admin = pymysql.connect(host=server['host'], port=server['port'], user=server['user'],
                            password=server['password'], charset='utf8mb4', autocommit=True)
    report = {'database': database, 'backup': str(backup.relative_to(ROOT)), 'backupSha256': backup_sha,
              'backupBytes': backup.stat().st_size, 'steps': []}
    try:
        with admin.cursor() as cursor:
            cursor.execute(f'DROP DATABASE IF EXISTS `{database}`')
            cursor.execute(f'CREATE DATABASE `{database}` DEFAULT CHARSET utf8mb4')

        connection = pymysql.connect(host=server['host'], port=server['port'], user=server['user'],
                                     password=server['password'], database=database, charset='utf8mb4',
                                     autocommit=True)
        try:
            with connection.cursor() as cursor:
                # 1) 旧库快照的大字段可能含分号，交给 MySQL 客户端整体导入，避免手工拆分语句
                client = shutil.which('mysql')
                if not client:
                    raise SystemExit('未找到 mysql 客户端，无法导入备份')
                with backup.open('rb') as stream:
                    imported = subprocess.run(
                        [client, f'--host={server["host"]}', f'--port={server["port"]}',
                         f'--user={server["user"]}', f'--password={server["password"]}',
                         '--default-character-set=utf8mb4', '--batch', database],
                        stdin=stream, capture_output=True)
                if imported.returncode != 0:
                    raise SystemExit('备份导入失败：' + imported.stderr.decode('utf-8', 'replace')[:500])
                baseline_columns = {table: [row[0] for row in columns(cursor, table)] for table in LEGACY_TABLES}
                baseline = {table: table_digest(cursor, table, baseline_columns[table]) for table in LEGACY_TABLES}
                report['legacyBefore'] = {table: {'rows': value[0], 'digest': value[1],
                                                 'columns': baseline_columns[table]} for table, value in baseline.items()}
                report['steps'].append({'step': 'import_backup', 'ok': True,
                                        'tables': sorted(existing_tables(cursor))})

                sql = (ROOT / 'sql/choice_question_media_v1.sql').read_text(encoding='utf-8')
                # 2) 首次执行
                for statement in statements(sql):
                    cursor.execute(statement)
                first = {'legacy': {}, 'columns': {}, 'keys': {}, 'rows': {}}
                for table in LEGACY_TABLES:
                    rows, digest = table_digest(cursor, table, baseline_columns[table])
                    first['legacy'][table] = {'rows': rows, 'digest': digest}
                    first['columns'][table] = [row[0] for row in columns(cursor, table) if row[0] == 'media_json']
                    cursor.execute(f'SELECT COUNT(*) FROM `{table}` WHERE media_json IS NOT NULL')
                    first.setdefault('nonNull', {})[table] = cursor.fetchone()[0]
                for table in MEDIA_TABLES:
                    first['keys'][table] = keys(cursor, table)
                    cursor.execute(f'SELECT COUNT(*) FROM `{table}`')
                    first['rows'][table] = cursor.fetchone()[0]
                report['firstRun'] = first
                report['steps'].append({'step': 'apply_first', 'ok': True})

                # 3) 重复执行确认幂等
                for statement in statements(sql):
                    cursor.execute(statement)
                second = {'legacy': {}, 'keys': {}, 'rows': {}}
                for table in LEGACY_TABLES:
                    second['legacy'][table] = table_digest(cursor, table, baseline_columns[table])
                for table in MEDIA_TABLES:
                    second['keys'][table] = keys(cursor, table)
                    cursor.execute(f'SELECT COUNT(*) FROM `{table}`')
                    second['rows'][table] = cursor.fetchone()[0]
                report['secondRun'] = second
                report['steps'].append({'step': 'apply_second_idempotent', 'ok': True})

                checks = []
                for table, expected in EXPECTED_COLUMNS.items():
                    got = [row[0] for row in columns(cursor, table) if row[0] in expected]
                    checks.append({'check': f'column:{table}', 'expected': expected, 'actual': got, 'ok': got == expected})
                for table, expected in EXPECTED_KEYS.items():
                    got = sorted(set(keys(cursor, table)))
                    checks.append({'check': f'keys:{table}', 'expected': sorted(set(expected)),
                                   'actual': got, 'ok': got == sorted(set(expected))})
                for table in LEGACY_TABLES:
                    checks.append({'check': f'legacy_unchanged:{table}',
                                   'expected': baseline[table], 'actual': second['legacy'][table],
                                   'ok': baseline[table] == second['legacy'][table]})
                idempotent = all(second['keys'][t] == first['keys'][t] and
                                 second['rows'][t] == first['rows'][t] for t in MEDIA_TABLES)
                idempotent = idempotent and all(second['legacy'][t][0] == first['legacy'][t]['rows']
                                                for t in LEGACY_TABLES)
                checks.append({'check': 'idempotent_second_run', 'expected': True,
                               'actual': idempotent, 'ok': idempotent})
                report['checks'] = checks
                report['steps'].append({'step': 'assert_schema', 'ok': all(c['ok'] for c in checks)})

                # 4) 回退条件演练：四表空且两列全 NULL 时应可安全移除
                removable = True
                for table in MEDIA_TABLES:
                    cursor.execute(f'SELECT COUNT(*) FROM `{table}`')
                    if cursor.fetchone()[0] != 0:
                        removable = False
                for table in LEGACY_TABLES:
                    cursor.execute(f'SELECT COUNT(*) FROM `{table}` WHERE media_json IS NOT NULL')
                    if cursor.fetchone()[0] != 0:
                        removable = False
                report['rollbackPrecondition'] = {'removable': removable}
                if removable:
                    for table in MEDIA_TABLES:
                        cursor.execute(f'DROP TABLE `{table}`')
                    for table in LEGACY_TABLES:
                        cursor.execute(f'ALTER TABLE `{table}` DROP COLUMN `media_json`')
                    after = {}
                    for table in LEGACY_TABLES:
                        rows, digest = table_digest(cursor, table, baseline_columns[table])
                        after[table] = {'rows': rows, 'digest': digest,
                                        'hasMediaJson': any(row[0] == 'media_json' for row in columns(cursor, table))}
                    report['rollbackResult'] = {
                        'tables': after,
                        'mediaTablesRemaining': [t for t in MEDIA_TABLES if t in existing_tables(cursor)],
                        'legacyIntact': all(after[t] == {'rows': baseline[t][0], 'digest': baseline[t][1],
                                                         'hasMediaJson': False} for t in LEGACY_TABLES),
                    }
                    report['steps'].append({'step': 'rollback_rehearsal',
                                            'ok': report['rollbackResult']['legacyIntact']
                                                  and not report['rollbackResult']['mediaTablesRemaining']})
                    # 5) 回退后再次前滚，确认可重复发布
                    for statement in statements(sql):
                        cursor.execute(statement)
                    refwd = all(any(row[0] == 'media_json' for row in columns(cursor, t)) for t in LEGACY_TABLES)
                    report['steps'].append({'step': 'forward_again_after_rollback', 'ok': refwd})
            report['status'] = 'REHEARSED'
        finally:
            connection.close()
    finally:
        if not options.keep:
            with admin.cursor() as cursor:
                cursor.execute(f'DROP DATABASE IF EXISTS `{database}`')
            report['tempDatabaseDropped'] = True
        admin.close()

    target = ROOT / 'output/choice-media' / 'migration-rehearsal-20261108.json'
    target.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
    failed = [step['step'] for step in report['steps'] if not step.get('ok')]
    print(json.dumps({'status': report['status'], 'failedSteps': failed,
                      'checks': len(report.get('checks', [])),
                      'rollbackPrecondition': report.get('rollbackPrecondition'),
                      'report': str(target.relative_to(ROOT))}, ensure_ascii=False))


if __name__ == '__main__':
    main()
