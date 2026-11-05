"""用真实MySQL执行Mapper SQL，仅操作会话临时表并回滚。"""
import json
import re
from pathlib import Path
import pymysql
import yaml

ROOT = Path(__file__).resolve().parents[2]


def main():
    master = yaml.safe_load((ROOT / 'RuoYi-Vue/ruoyi-admin/src/main/resources/application-druid.yml').read_text(encoding='utf-8'))['spring']['datasource']['druid']['master']
    match = re.match(r'jdbc:mysql://([^:/]+):(\d+)/([^?]+)', master['url'])
    assert match[1] in {'localhost', '127.0.0.1'}
    connection = pymysql.connect(host=match[1], port=int(match[2]), user=master['username'], password=str(master['password']), database=match[3], charset='utf8mb4')
    source = (ROOT / 'RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/mapper/TextbookCaseFeedbackMapper.java').read_text(encoding='utf-8')
    def annotation(method, kind):
        prefix = source.split(method)[0].rsplit('@' + kind + '(', 1)[1]
        return ''.join(re.findall(r'"([^"\n]*)"', prefix)).replace('biz_textbook_case_feedback', 'tmp_case_feedback_regression')
    select_sql = annotation('Map<String, Object> summary', 'Select')
    insert_sql = annotation('int vote(', 'Insert')
    def execute(cursor, sql, values):
        names = re.findall(r'#\{(\w+)\}', sql)
        cursor.execute(re.sub(r'#\{\w+\}', '%s', sql), [values[name] for name in names])
    try:
        with connection.cursor(pymysql.cursors.DictCursor) as cursor:
            cursor.execute('CREATE TEMPORARY TABLE tmp_case_feedback_regression LIKE biz_textbook_case_feedback')
            def vote(user, value, reason=None):
                execute(cursor, insert_sql, {'caseId': 1, 'userId': user, 'vote': value, 'reason': reason})
            def summary(user):
                execute(cursor, select_sql, {'caseId': 1, 'userId': user})
                return cursor.fetchone()
            vote(11, -1, '旧理由'); vote(22, 1)
            assert summary(11)['myVote'] == -1
            for _ in range(12): vote(11, -1)
            assert summary(11)['dislikes'] == 1
            assert summary(11)['myReason'] == '旧理由'
            vote(11, -1, '')
            assert summary(11)['myReason'] is None
            vote(11, 1, '新理由'); vote(11, 0)
            assert summary(11)['myVote'] == 0 and summary(11)['myReason'] is None
            vote(11, 1)
            assert summary(11)['myReason'] is None
            print(json.dumps({'mysqlMapperRegression': 'PASS', 'scenarios': ['多人踩票', '12次重复不多计', '快捷改票保留理由', '清空理由', '撤销后重新评价无旧理由']} , ensure_ascii=False))
        connection.rollback()
    finally: connection.close()


if __name__ == '__main__': main()
