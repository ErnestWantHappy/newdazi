import json,re,uuid
import requests
from migrate_local import ROOT,connect
BASE='http://127.0.0.1:18080';OUT=ROOT/'output/choice-media'
def main():
    fixture=json.loads((OUT/'student-session.local.json').read_text());manifest=json.loads((OUT/'review-data.json').read_text());db,_=connect();headers={'Authorization':'Bearer '+fixture['token']};checks=[]
    with db.cursor() as c:
        c.execute('SELECT question_id,student_answer,score,is_correct FROM biz_student_answer WHERE student_id=%s AND lesson_id=%s ORDER BY question_id',(fixture['studentId'],fixture['lessonId']));rows=c.fetchall()
        assert len(rows)==3 and sum(r[2] for r in rows)==20,rows
        assert [r[1] for r in rows]==['B','B','C'];checks.append('真实MySQL答案仍为B/B/C，正确/错误评分10/0/10合计20分')
        c.execute("INSERT INTO biz_county_exam(exam_name,creator_id) VALUES('【图片功能抽测快照验收】',104)");exam=c.lastrowid;manifest.setdefault('examIds',[]).append(exam)
        c.execute('INSERT INTO biz_county_exam_question(exam_id,question_id,question_score,order_num) VALUES(%s,%s,10,1)',(exam,fixture['questionIds'][1]))
        c.execute("INSERT INTO biz_county_exam_paper_question(exam_id,student_id,question_id,question_type,question_content,option_a,option_b,answer,question_score,order_num,media_json) SELECT %s,%s,question_id,question_type,question_content,option_a,option_b,answer,10,1,media_json FROM biz_question WHERE question_id=%s",(exam,fixture['studentId'],fixture['questionIds'][1]))
    db.commit();(OUT/'review-data.json').write_text(json.dumps(manifest));fixture['examId']=exam;(OUT/'student-session.local.json').write_text(json.dumps(fixture))
    image=fixture['imageIds'][0];q=fixture['questionIds'][1]
    read=requests.get(BASE+f'/business/question-images/{image}/content',headers=headers,params={'questionId':q,'examId':exam});assert read.headers['Content-Type'].startswith('image/png');checks.append('学生按个人抽测媒体快照读图')
    denied=requests.get(BASE+f'/business/question-images/{image}/content',headers=headers,params={'questionId':q,'examId':exam+100000}).json();assert denied.get('code')==403;checks.append('换考试ID不能读取他卷图片')
    denied=requests.get(BASE+f'/business/question-images/{image}/content',headers=headers,params={'questionId':q}).json();assert denied.get('code')==403;checks.append('学生不能绕开课程/试卷上下文读私有题库图片')
    account=re.search(r'\| 教研员 \| `([^`]+)` \| `([^`]+)`',(ROOT/'AGENTS.md').read_text(encoding='utf-8'));login=requests.post(BASE+'/login',json={'username':account[1],'password':account[2]}).json()
    assert login.get('code')==200,login.get('msg');(OUT/'researcher-session.local.json').write_text(json.dumps({'token':login['token']}))
    read=requests.get(BASE+f'/business/question-images/{image}/content',headers={'Authorization':'Bearer '+login['token']},params={'questionId':q,'examId':exam});assert read.headers['Content-Type'].startswith('image/png');checks.append('教研员组卷上下文可读取实际引用图片')
    (OUT/'student-api-result.json').write_text(json.dumps({'checks':checks},ensure_ascii=False,indent=2),encoding='utf-8');print(json.dumps({'checks':checks},ensure_ascii=False));db.close()
if __name__=='__main__':main()
