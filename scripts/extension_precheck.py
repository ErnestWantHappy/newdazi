"""拓展班迁移前只读核对；同名候选不能替代人工身份确认。"""
import json
import re
from datetime import datetime
from pathlib import Path

import pymysql


ROOT = Path(__file__).resolve().parents[1]


def main():
    config = (ROOT / 'RuoYi-Vue/ruoyi-admin/src/main/resources/application-druid.yml').read_text(encoding='utf-8')
    master = config.split('master:', 1)[1].split('slave:', 1)[0]
    values = dict(re.findall(r'^\s*(url|username|password):\s*(.*?)\s*$', master, re.M))
    url = re.fullmatch(r'jdbc:mysql://(localhost|127\.0\.0\.1):(\d+)/([^?]+)\?.*', values['url'])
    if not url:
        raise RuntimeError('仅允许连接明确配置的本机 MySQL，未执行查询')
    connection = pymysql.connect(host=url[1], port=int(url[2]), user=values['username'].strip('\"\''),
                                 password=values['password'].strip('\"\''), database=url[3],
                                 charset='utf8mb4', cursorclass=pymysql.cursors.DictCursor,
                                 connect_timeout=5, read_timeout=30, autocommit=False)
    report = {'database': url[3], 'checked_at': datetime.now().isoformat(),
              'warning': '同名候选未经人工确认；本脚本不修改数据、不冻结账号'}
    try:
        with connection.cursor() as cursor:
            cursor.execute('SET TRANSACTION READ ONLY')
            cursor.execute('START TRANSACTION WITH CONSISTENT SNAPSHOT')

            def query(sql, args=None):
                cursor.execute(sql, args)
                return cursor.fetchall()

            report['schema'] = query("SELECT table_name,column_name,column_type FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name IN ('biz_student','biz_lesson','biz_lesson_assignment','biz_lesson_checkin','biz_python_practice_plan','biz_python_practice_plan_class','biz_teacher_class','biz_extension_class','biz_extension_lesson_member','biz_extension_plan_member') ORDER BY table_name,ordinal_position")
            report['legacy_classes'] = query("""
                SELECT u.dept_id,s.entry_year,s.class_code,COUNT(*) student_count,
                       SUM(u.status='0' AND u.del_flag='0') enabled_count
                FROM biz_student s JOIN sys_user u ON u.user_id=s.user_id
                WHERE s.class_code='50' GROUP BY u.dept_id,s.entry_year,s.class_code
                ORDER BY u.dept_id,s.entry_year
            """)
            report['legacy_students'] = query("""
                SELECT s.student_id,s.user_id,s.student_no,s.entry_year,s.class_code,
                       u.dept_id,u.user_name,u.nick_name,u.status,u.del_flag
                FROM biz_student s JOIN sys_user u ON u.user_id=s.user_id
                WHERE s.class_code='50' ORDER BY u.dept_id,s.entry_year,s.student_id
            """)
            report['same_name_candidates'] = query("""
                SELECT old.student_id old_student_id,s.student_id candidate_student_id,
                       s.user_id candidate_user_id,s.student_no,s.entry_year,s.class_code,
                       u.user_name,u.nick_name,u.dept_id
                FROM biz_student old JOIN sys_user ou ON ou.user_id=old.user_id
                JOIN sys_user u ON u.dept_id=ou.dept_id AND u.nick_name=ou.nick_name
                    AND u.status='0' AND u.del_flag='0'
                JOIN biz_student s ON s.user_id=u.user_id AND s.class_code<>'50'
                WHERE old.class_code='50' ORDER BY old.student_id,s.student_id
            """)
            report['legacy_assignments'] = query("SELECT * FROM biz_lesson_assignment WHERE class_code='50'")
            report['legacy_plan_classes'] = query("SELECT * FROM biz_python_practice_plan_class WHERE class_code='50'")
            report['legacy_teachers'] = query("SELECT * FROM biz_teacher_class WHERE class_code='50'")
            # 防止“050”或“50班”等历史写法被精确匹配漏掉，候选仍不能自动迁移。
            report['class_code_variants'] = {}
            for table in ('biz_student', 'biz_teacher_class', 'biz_lesson_assignment', 'biz_python_practice_plan_class'):
                report['class_code_variants'][table] = query(
                    'SELECT class_code,COUNT(*) row_count FROM ' + table +
                    " WHERE TRIM(class_code) REGEXP '^0*50([^0-9]|$)' GROUP BY class_code")
            report['legacy_checkins'] = query("""
                SELECT u.dept_id,s.entry_year,c.lesson_id,COUNT(*) checkin_count
                FROM biz_lesson_checkin c JOIN biz_student s ON s.student_id=c.student_id
                JOIN sys_user u ON u.user_id=s.user_id WHERE s.class_code='50'
                GROUP BY u.dept_id,s.entry_year,c.lesson_id
            """)
            report['attendance_lessons'] = query("SELECT lesson_id,lesson_title,creator_id,dept_id,entry_year,lesson_mode FROM biz_lesson WHERE lesson_mode='attendance'")
            # 投放名单表（0900_ai_ci）与拓展班表（general_ci）曾因直接 JOIN 字符串列报 1267，教师题单列表/详情全部 500。
            # 代码已改 CAST AS BINARY；这里按实际执行的 SQL 形状复核一次，仍然失败就拒绝发布。
            # information_schema 在不同 MySQL 版本下返回的列名大小写不一致，按小写匹配取表名集合。
            tables = {str(value) for row in report['schema'] for key, value in row.items()
                      if key.lower() == 'table_name'}
            report['plan_class_join'] = {'skipped': True}
            if 'biz_python_practice_plan_class' in tables and 'biz_extension_class' in tables:
                join_sql = ("SELECT COUNT(*) matched FROM biz_python_practice_plan_class pc "
                            "LEFT JOIN biz_extension_class e ON e.dept_id=pc.dept_id "
                            "AND CAST(e.entry_year AS BINARY)=CAST(pc.entry_year AS BINARY) "
                            "AND CAST(e.class_code AS BINARY)=CAST(pc.class_code AS BINARY)")
                try:
                    report['plan_class_join'] = {'ok': True, 'matched': query(join_sql)[0]['matched']}
                except Exception as exc:
                    report['plan_class_join'] = {'ok': False, 'error': str(exc)}
                try:
                    query("SELECT COUNT(*) FROM biz_python_practice_plan_class pc LEFT JOIN biz_extension_class e "
                          "ON e.dept_id=pc.dept_id AND e.entry_year=pc.entry_year AND e.class_code=pc.class_code")
                    report['plan_class_raw_join'] = {'ok': True}
                except Exception as exc:
                    # 已知可复现的排序规则混用；代码已规避，只作为环境提示记录。
                    report['plan_class_raw_join'] = {'ok': False, 'error': str(exc)}
            # 历史名单已改为只读冻结快照：登记旧 50 班后若没跑 sql/extension_legacy_snapshot_v1.sql，
            # 历史考勤与题单学情会显示 0 人。这里只提示缺口，不自动写入。
            report['legacy_snapshot_backlog'] = {'skipped': True}
            if {'biz_extension_lesson_member', 'biz_extension_plan_member'} <= tables:
                try:
                    report['legacy_snapshot_backlog'] = query("""
                        SELECT
                          (SELECT COUNT(*) FROM biz_lesson l WHERE l.lesson_scope='legacy'
                             AND NOT EXISTS(SELECT 1 FROM biz_extension_lesson_member m WHERE m.lesson_id=l.lesson_id)) lessons_without_members,
                          (SELECT COUNT(*) FROM biz_python_practice_plan p
                             JOIN biz_python_practice_plan_version v ON v.plan_id=p.plan_id
                               AND v.status='PUBLISHED' AND v.version_no=p.current_version_no
                            WHERE p.plan_scope='legacy'
                              AND NOT EXISTS(SELECT 1 FROM biz_extension_plan_member m WHERE m.plan_version_id=v.plan_version_id)) plans_without_members
                    """)[0]
                except Exception as exc:
                    report['legacy_snapshot_backlog'] = {'ok': False, 'error': str(exc)}
        connection.rollback()
    finally:
        connection.close()
    output = ROOT / 'output/extension-precheck'
    output.mkdir(parents=True, exist_ok=True)
    target = output / (datetime.now().strftime('%Y%m%d-%H%M%S') + '.json')
    target.write_text(json.dumps(report, ensure_ascii=False, indent=2, default=str), encoding='utf-8')
    candidates = {}
    for row in report['same_name_candidates']:
        candidates.setdefault(row['old_student_id'], []).append(row)
    print(json.dumps({'database': report['database'], 'legacy_classes': report['legacy_classes'],
                      'legacy_student_count': len(report['legacy_students']),
                      'no_candidate': sum(not candidates.get(s['student_id']) for s in report['legacy_students']),
                      'multiple_candidates': sum(len(candidates.get(s['student_id'], [])) > 1 for s in report['legacy_students']),
                      'assignment_count': len(report['legacy_assignments']),
                      'plan_class_count': len(report['legacy_plan_classes']),
                      'checkin_lesson_count': len(report['legacy_checkins']),
                      'class_code_variants': report['class_code_variants'],
                      'plan_class_join': report['plan_class_join'],
                      'legacy_snapshot_backlog': report['legacy_snapshot_backlog'],
                      'report': str(target)}, ensure_ascii=False, indent=2))
    # 题单列表/详情依赖这条 JOIN；它跑不通时发布后教师端直接 500，必须先运维排序规则。
    if report['plan_class_join'].get('ok') is False:
        raise SystemExit('拓展题单投放名单 JOIN 失败（排序规则不兼容），已拒绝发布：'
                         + report['plan_class_join']['error'])


if __name__ == '__main__':
    main()
