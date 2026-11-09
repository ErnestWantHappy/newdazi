"""新增独立验收学生/班级/课程，不更改任何现有账号、指派或成绩。"""
import io,json,re,uuid
import requests
from PIL import Image,ImageDraw,ImageFont
from migrate_local import ROOT,connect
BASE='http://127.0.0.1:18080';OUT=ROOT/'output/choice-media'
def main():
    manifest=json.loads((OUT/'review-data.json').read_text());session=requests.Session();session.headers['Authorization']='Bearer '+json.loads((OUT/'teacher-session.local.json').read_text())['token']
    def record():(OUT/'review-data.json').write_text(json.dumps(manifest),encoding='utf-8')
    def call(path,**kwargs):
        r=session.post(BASE+path,timeout=60,**kwargs).json();assert r.get('code')==200,r;return r['data']
    image_ids=[]
    font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',24)
    for n in range(1,5):
        image=Image.new('RGB',(420,400),'white');draw=ImageDraw.Draw(image)
        for y,text in [(20,'开始'),(110,'n = '+str(n)),(200,'n = n + 2'),(290,'输出 n')]:
            draw.rounded_rectangle((70,y,350,y+55),radius=6,fill='#eff6ff',outline='#409eff',width=2);draw.text((210,y+27),text,font=font,anchor='mm',fill='#303133')
            if y<290:draw.line((210,y+56,210,y+90),fill='#73869f',width=2);draw.polygon([(210,y+90),(204,y+82),(216,y+82)],fill='#73869f')
        out=io.BytesIO();image.save(out,'PNG');(OUT/f'fixtures/option-flow-{n}.png').write_bytes(out.getvalue())
        data=call('/business/question-images',files={'file':(f'流程图{n}.png',out.getvalue(),'image/png')});image_ids.append(data['imageId']);manifest['imageIds'].append(int(data['imageId']));record()
    base={'questionType':'choice','optionA':'1','optionB':'3','optionC':'5','optionD':'7','answer':'B','grade':6,'semester':'0','isPublic':'N'}
    q1={**base,'questionContent':'【图片功能学生验收】观察流程图，程序最终输出什么？','media':{'schemaVersion':1,'stem':[{'imageId':image_ids[0]}]}}
    q2={**base,'questionContent':'【图片功能学生验收】哪幅流程图最终输出3？','optionA':'','optionB':'流程图二','optionC':'','optionD':'流程图四','answer':'A','media':{'schemaVersion':1,'stem':[],'options':{k:[{'imageId':image_ids[i]}] for i,k in enumerate('ABCD')}}}
    q3={**base,'questionContent':'【图片功能学生验收】已有纯文字题：计算机的输入设备是？','optionA':'显示器','optionB':'音箱','optionC':'键盘','optionD':'打印机','answer':'C'}
    ids=[]
    for q in [q1,q2,q3]:
        result=call('/business/question',json={**q,'saveRequestId':uuid.uuid4().hex});ids.append(result['questionId']);manifest['questionIds'].append(result['questionId']);record()
    db,_=connect();username='cmr_'+uuid.uuid4().hex[:12]
    with db.cursor() as c:
        c.execute("SELECT dept_id,password FROM sys_user WHERE user_id=104");dept,password=c.fetchone()
        c.execute("SELECT count(*) FROM biz_lesson_assignment WHERE dept_id=%s AND entry_year='2099' AND class_code='97'",(dept,));assert c.fetchone()[0]==0
        c.execute("INSERT INTO sys_user(dept_id,user_name,nick_name,password,create_by) VALUES(%s,%s,'图片功能验收学生',%s,'choice-media-review')",(dept,username,password));user=c.lastrowid;manifest.setdefault('userIds',[]).append(user);record()
        c.execute("INSERT INTO sys_user_role(user_id,role_id) SELECT %s,role_id FROM sys_role WHERE role_key='student'",(user,))
        c.execute("INSERT INTO biz_student(user_id,student_no,entry_year,class_code) VALUES(%s,%s,'2099','97')",(user,username));student=c.lastrowid;manifest.setdefault('studentIds',[]).append(student);record()
        c.execute("INSERT INTO biz_lesson(lesson_title,entry_year,creator_id,dept_id) VALUES('【图片功能学生验收】图文选择题','2099',104,%s)",(dept,));lesson=c.lastrowid;manifest['lessonIds'].append(lesson);record()
        for i,qid in enumerate(ids):c.execute('INSERT INTO biz_lesson_question(lesson_id,question_id,question_score,order_num) VALUES(%s,%s,10,%s)',(lesson,qid,i+1))
        c.execute("INSERT INTO biz_lesson_assignment(lesson_id,entry_year,class_code,assigner_id,dept_id,theory_open) VALUES(%s,'2099','97',104,%s,1)",(lesson,dept))
    db.commit();db.close()
    account=re.search(r'\| 教师 \| `([^`]+)` \| `([^`]+)`',(ROOT/'AGENTS.md').read_text(encoding='utf-8'))
    login=requests.post(BASE+'/login',json={'username':username,'password':account[2]},timeout=30).json();assert login.get('code')==200,login.get('msg')
    student_session=requests.Session();student_session.headers['Authorization']='Bearer '+login['token']
    lesson_data=student_session.get(BASE+'/business/student-home/current-lesson').json();assert lesson_data.get('hasLesson') and len(lesson_data.get('questions',[]))==3,lesson_data
    assert all('answer' not in q for q in lesson_data['questions'])
    (OUT/'student-session.local.json').write_text(json.dumps({'token':login['token'],'userId':user,'studentId':student,'lessonId':lesson,'questionIds':ids,'imageIds':image_ids}),encoding='utf-8')
    print(json.dumps({'studentFixture':True,'newUserId':user,'lessonId':lesson,'questionCount':3,'oldAccountsChanged':0},ensure_ascii=False))
if __name__=='__main__':main()
