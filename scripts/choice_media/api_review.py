"""真实本机API验收；只创建带专用前缀的测试数据，清理限定在本轮清单内。"""
import concurrent.futures
import io
import json
import time
import uuid
import re
from pathlib import Path
import requests
from PIL import Image
from migrate_local import ROOT, connect

BASE='http://127.0.0.1:18080'
OUT=ROOT/'output/choice-media'

def token_from_state(file):
    state=json.loads(file.read_text(encoding='utf-8'))
    return next(c['value'] for c in state['cookies'] if c['name']=='Admin-Token')

def main():
    state=ROOT/'output/playwright/review-fix-teacher-state.json'
    session=requests.Session();session.headers['Authorization']='Bearer '+token_from_state(state)
    response=session.get(BASE+'/getInfo').json()
    if response.get('code')!=200:
        account=re.search(r'\| 教师 \| `([^`]+)` \| `([^`]+)`', (ROOT/'AGENTS.md').read_text(encoding='utf-8'))
        login=requests.post(BASE+'/login',json={'username':account[1],'password':account[2]},timeout=30).json()
        if login.get('code')!=200:raise RuntimeError('本机登录失败：'+login.get('msg','未知原因'))
        session.headers['Authorization']='Bearer '+login['token']
        (OUT/'teacher-session.local.json').write_text(json.dumps({'token':login['token']}),encoding='utf-8')
        response=session.get(BASE+'/getInfo').json()
    print(json.dumps({'auth':True,'userId':response['user']['userId'],'roles':response.get('roles')},ensure_ascii=False))
    checks=[];manifest=json.loads((OUT/'review-data.json').read_text()) if (OUT/'review-data.json').exists() else {'questionIds':[],'batchIds':[],'imageIds':[],'lessonIds':[]}
    def record(): (OUT/'review-data.json').write_text(json.dumps(manifest),encoding='utf-8')
    def call(method,path,expected=200,**kwargs):
        r=session.request(method,BASE+path,timeout=180,**kwargs)
        data=r.json();assert data.get('code')==expected,(path,data);return data
    def key():return uuid.uuid4().hex
    def upload_image(color):
        out=io.BytesIO();Image.new('RGB',(600,360),color).save(out,'PNG')
        data=call('POST','/business/question-images',files={'file':('流程图.png',out.getvalue(),'image/png')})['data']
        manifest['imageIds'].append(int(data['imageId']));record();return data['imageId']
    def media(id):return {'schemaVersion':1,'stem':[{'imageId':id}],'options':{'A':[],'B':[],'C':[],'D':[]}}
    image=upload_image('#337ecc')
    q={'questionType':'choice','questionContent':'【图片功能API验收】观察流程图','optionA':'1','optionB':'2','optionC':'3','optionD':'4','answer':'B','grade':6,'semester':'0','isPublic':'N','media':media(image),'saveRequestId':key()}
    created=call('POST','/business/question',json=q)['data'];qid=created['questionId'];manifest['questionIds'].append(qid);record()
    repeat=call('POST','/business/question',json=q)['data'];assert repeat['questionId']==qid;checks.append('新增响应重放不重复建题')
    detail=call('GET',f'/business/question/{qid}')['data'];assert detail['media']['stem'][0]['imageId']==image
    read=session.get(BASE+f'/business/question-images/{image}/content',params={'questionId':qid});assert read.headers['Content-Type'].startswith('image/png');assert read.content.startswith(b'\x89PNG');checks.append('鉴权图片真实可读')
    anon=requests.get(BASE+f'/business/question-images/{image}/content');assert anon.status_code==401 or anon.json().get('code')==401;checks.append('匿名不能读取图片')
    db,_=connect()
    with db.cursor() as c:
        c.execute('INSERT INTO biz_lesson(lesson_title,entry_year,creator_id,dept_id) VALUES(%s,%s,%s,%s)',('【图片功能API验收】历史保护','2099',response['user']['userId'],response['user'].get('deptId')));lesson=c.lastrowid
        manifest['lessonIds'].append(lesson);record()
        c.execute('INSERT INTO biz_lesson_question(lesson_id,question_id,question_score,order_num) VALUES(%s,%s,10,1)',(lesson,qid))
    db.commit()
    changed={**detail,'questionContent':'【图片功能API验收】修改后新题','expectedSourceFingerprint':detail['sourceFingerprint'],'saveRequestId':key()}
    fork=call('PUT','/business/question',json=changed)['data'];assert fork['saveMode']=='FORKED' and fork['questionId']!=qid
    manifest['questionIds'].append(fork['questionId']);record()
    assert call('PUT','/business/question',json=changed)['data']==fork
    assert call('GET',f'/business/question/{qid}')['data']['questionContent']==q['questionContent']
    checks.append('历史课程引用不变，编辑自动另存且重试只创建一次')
    denied=call('DELETE',f'/business/question/{qid}',expected=500);assert '历史' in denied.get('msg','') or '课程' in denied.get('msg','');checks.append('被课程使用的含图题禁止删除')
    pure={**q,'questionContent':'','optionA':'','answer':'A','media':{'schemaVersion':1,'stem':[{'imageId':image}],'options':{'A':[{'imageId':image}],'B':[],'C':[],'D':[]}},'saveRequestId':key()}
    pureid=call('POST','/business/question',json=pure)['data']['questionId'];manifest['questionIds'].append(pureid);record();checks.append('纯图片题干和纯图片选项可保存')
    fixture=(OUT/'fixtures/50-text-images.xlsx').read_bytes()
    request_id=key();preview=call('POST','/business/question/import-batches',files={'file':('50道图文题.xlsx',fixture)},data={'requestId':request_id,'allowDuplicate':'true'})['data']
    bid=preview['batchId'];manifest['batchIds'].append(int(bid));record()
    assert preview['questionCount']==50 and preview['imageCount']==76 and preview['errorCount']==0,(preview['questionCount'],preview['imageCount'],[i['errors'] for i in preview['items'] if i['errors']])
    assert call('POST','/business/question/import-batches',files={'file':('50道图文题.xlsx',fixture)},data={'requestId':request_id,'allowDuplicate':'true'})['data']['batchId']==bid
    checks.append('50题76张图片一次识别，预览上传重放不重复建批次')
    with db.cursor() as c:
        c.execute('SELECT COUNT(*) FROM biz_question');before=c.fetchone()[0]
    # 第一行正常，第二行改用独立图后使其暂时失效，逼真验证写入一部分后仍整批回滚。
    second=preview['items'][1];other=upload_image('#67c23a');draft=second['draft']['question'];draft['media']=media(other)
    patched=call('PATCH',f'/business/question/import-batches/{bid}/items/{second["itemId"]}',json={'expectedRevision':preview['revision'],'question':draft})['data']
    with db.cursor() as c:c.execute("UPDATE biz_question_image SET state='EXPIRED' WHERE image_id=%s",(other,))
    db.commit()
    failed=call('POST',f'/business/question/import-batches/{bid}/confirm',expected=500,json={'expectedRevision':patched['revision']})
    db.rollback()
    with db.cursor() as c:
        c.execute('SELECT COUNT(*) FROM biz_question');assert c.fetchone()[0]==before,'失败留下半批题目'
        c.execute('SELECT COUNT(*) FROM biz_question_import_item WHERE batch_id=%s AND question_id IS NOT NULL',(bid,));assert c.fetchone()[0]==0
        c.execute("UPDATE biz_question_image SET state='TEMP' WHERE image_id=%s",(other,))
    db.commit();checks.append('第二题图片失效时整批回滚，0道半成品')
    def confirm():return call('POST',f'/business/question/import-batches/{bid}/confirm',json={'expectedRevision':patched['revision']})['data']
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:results=list(pool.map(lambda _:confirm(),range(2)))
    assert results[0]['result']==results[1]['result'];ids=results[0]['result']['questionIds'];assert len(ids)==50
    manifest['questionIds']+=ids;record();checks.append('并发确认只导入50题，回执一致')
    duplicate=call('POST','/business/question/import-batches',files={'file':('50道图文题.xlsx',fixture)},data={'requestId':key()})['data'];assert duplicate['alreadyImported'] and duplicate['batchId']==bid;checks.append('同文件重复上传定位原结果')
    exported=session.post(BASE+'/business/question/export',data={'questionContent':'【图片功能API验收】'},timeout=90)
    assert exported.content[:2]==b'PK',exported.text[:300];(OUT/'fixtures/export-roundtrip.xlsx').write_bytes(exported.content)
    p=call('POST','/business/question/import-batches',files={'file':('重新导入.xlsx',exported.content)},data={'requestId':key()})['data'];manifest['batchIds'].append(int(p['batchId']));record();assert p['imageCount']>=2 and p['errorCount']==0;checks.append('图文导出再导入，纯图片与比例保留且无定位异常')
    with db.cursor() as c:
        c.execute('SELECT image_id FROM biz_question_image WHERE uploader_id=%s AND image_id IN (SELECT image_id FROM biz_question_image)',(response['user']['userId'],))
    db.close()
    (OUT/'api-result.json').write_text(json.dumps({'checks':checks,'passed':len(checks),'scope':'18080真实API与本机MySQL，测试数据清单独立保存'},ensure_ascii=False,indent=2),encoding='utf-8')
    print(json.dumps({'checks':checks,'passed':len(checks)},ensure_ascii=False))

if __name__=='__main__':main()
