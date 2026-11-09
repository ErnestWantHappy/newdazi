"""仅对已配置的本机开发库备份并执行选择题图片增量，不允许远程库。"""
import argparse
import hashlib
import json
import re
from datetime import datetime
from pathlib import Path
import pymysql
import yaml

ROOT = Path(__file__).resolve().parents[2]

def connect():
    config = yaml.safe_load((ROOT / 'RuoYi-Vue/ruoyi-admin/src/main/resources/application-druid.yml').read_text(encoding='utf-8'))
    master = config['spring']['datasource']['druid']['master']
    match = re.match(r'jdbc:mysql://([^:/]+):(\d+)/([^?]+)', master['url'])
    if not match or match[1] not in {'localhost', '127.0.0.1'} or match[3] != 'xueyeceping_server_20260729':
        raise RuntimeError('拒绝操作非约定本机开发库')
    return pymysql.connect(host=match[1], port=int(match[2]), user=master['username'], password=str(master['password']), database=match[3], charset='utf8mb4'), match[3]

def main():
    args = argparse.ArgumentParser()
    args.add_argument('--apply', action='store_true')
    options = args.parse_args()
    connection, database = connect()
    tables = ['biz_question', 'biz_county_exam_paper_question', 'biz_question_image', 'biz_question_save_receipt', 'biz_question_import_batch', 'biz_question_import_item']
    try:
        with connection.cursor() as cursor:
            baseline = {}
            existing = []
            for table in tables:
                cursor.execute('SHOW TABLES LIKE %s', (table,))
                if cursor.fetchone():
                    existing.append(table)
                    cursor.execute(f'SELECT COUNT(*) FROM `{table}`')
                    baseline[table] = cursor.fetchone()[0]
            if not options.apply:
                print(json.dumps({'database': database, 'counts': baseline, 'write': False}, ensure_ascii=False))
                return
            folder = ROOT / 'output/choice-media/backups' / datetime.now().strftime('%Y%m%d-%H%M%S')
            folder.mkdir(parents=True, exist_ok=False)
            target = folder / 'before.sql'
            # 本轮只增加列和表，仍保留两张原表及已有本功能表的完整快照。
            connection.rollback()
            cursor.execute('SET TRANSACTION ISOLATION LEVEL REPEATABLE READ')
            cursor.execute('START TRANSACTION WITH CONSISTENT SNAPSHOT')
            with target.open('w', encoding='utf-8', newline='\n') as stream:
                stream.write('SET NAMES utf8mb4;\n')
                for table in existing:
                    cursor.execute(f'SHOW CREATE TABLE `{table}`')
                    stream.write(cursor.fetchone()[1] + ';\n')
                    cursor.execute(f'SELECT * FROM `{table}`')
                    columns = ','.join('`'+c[0]+'`' for c in cursor.description)
                    for row in cursor:
                        stream.write(f'INSERT INTO `{table}` ({columns}) VALUES (' + ','.join(connection.escape(v) for v in row) + ');\n')
            connection.rollback()
            digest = hashlib.sha256(target.read_bytes()).hexdigest()
            sql = (ROOT / 'sql/choice_question_media_v1.sql').read_text(encoding='utf-8')
            sql = '\n'.join(line for line in sql.splitlines() if not line.lstrip().startswith('--'))
            for statement in sql.split(';'):
                if statement.strip(): cursor.execute(statement)
            connection.commit()
            for table in baseline:
                cursor.execute(f'SELECT COUNT(*) FROM `{table}`')
                assert cursor.fetchone()[0] == baseline[table], '迁移前后行数改变'
            cursor.execute("SELECT COUNT(*) FROM biz_question WHERE media_json IS NOT NULL")
            nonnull = cursor.fetchone()[0]
            result = {'database': database, 'baseline': baseline, 'backup': str(target.relative_to(ROOT)), 'sha256': digest, 'bytes': target.stat().st_size, 'questionMediaNonNull': nonnull, 'status': 'APPLIED'}
            (folder / 'manifest.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
            print(json.dumps(result, ensure_ascii=False))
    finally:
        connection.close()

if __name__ == '__main__': main()
