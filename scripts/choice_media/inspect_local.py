from migrate_local import connect
db,_=connect()
with db.cursor() as c:
    for table in ['sys_user','biz_student','biz_lesson_assignment']:
        c.execute('SHOW COLUMNS FROM '+table)
        print(table,[(r[0],r[1],r[2],r[4]) for r in c.fetchall()])
db.close()
