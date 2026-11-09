"""验收收口只读核对，并注销已删除的临时测试学生登录态。"""
import json
import requests
from migrate_local import ROOT,connect
OUT=ROOT/'output/choice-media'
db,_=connect();fixture=json.loads((OUT/'student-session.local.json').read_text())
with db.cursor() as c:
    c.execute('SELECT COUNT(*) FROM sys_user WHERE user_id=%s',(fixture['userId'],));assert c.fetchone()[0]==0
    c.execute('SELECT COUNT(*) FROM biz_student WHERE student_id=%s',(fixture['studentId'],));assert c.fetchone()[0]==0
    counts={}
    for table in ['biz_question','biz_county_exam_paper_question','biz_question_image','biz_question_save_receipt','biz_question_import_batch','biz_question_import_item']:
        c.execute('SELECT COUNT(*) FROM '+table);counts[table]=c.fetchone()[0]
assert counts=={'biz_question':1738,'biz_county_exam_paper_question':4187,'biz_question_image':0,'biz_question_save_receipt':0,'biz_question_import_batch':0,'biz_question_import_item':0}
result=requests.post('http://127.0.0.1:18080/logout',headers={'Authorization':'Bearer '+fixture['token']},timeout=20).json();assert result.get('code')==200
for i in range(1,6):assert len(list((ROOT/'docs/choice-question-images-ui/implemented').glob(str(i)+'-*.png')))==1
(OUT/'final-state.json').write_text(json.dumps({'counts':counts,'testStudentSession':'LOGGED_OUT','deployed':False},indent=2))
print(json.dumps({'counts':counts,'testStudentSession':'LOGGED_OUT','deployed':False}));db.close()
