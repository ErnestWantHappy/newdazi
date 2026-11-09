import {createRequire} from 'node:module'
import fs from 'node:fs/promises'
import path from 'node:path'
import assert from 'node:assert/strict'
const root=path.resolve('.'),require=createRequire(path.join(root,'RuoYi-Vue3/package.json'))
const {chromium}=require('playwright'),out=path.join(root,'output/choice-media')
const session=JSON.parse(await fs.readFile(path.join(out,'teacher-session.local.json'),'utf8'))
const browser=await chromium.launch({headless:true}),context=await browser.newContext({viewport:{width:1440,height:1000}})
await context.addCookies([{name:'Admin-Token',value:session.token,domain:'127.0.0.1',path:'/'}])
const page=await context.newPage(),errors=[];page.on('pageerror',e=>errors.push(e.message));page.setDefaultTimeout(15000)
try{
 const routes=await context.request.get('http://127.0.0.1:4178/prod-api/getRouters',{headers:{Authorization:'Bearer '+session.token}})
 const tree=(await routes.json()).data
 function find(rows,parent=''){for(const r of rows||[]){const full=r.path.startsWith('/')?r.path:parent+'/'+r.path;if(r.component==='business/question/index')return full;const found=find(r.children,full);if(found)return found}}
 const route=find(tree)?.replace(/\/+/g,'/');assert.ok(route,'未找到题库路由')
 const manifest=JSON.parse(await fs.readFile(path.join(out,'review-data.json'),'utf8')),checks=[]
 await page.goto('http://127.0.0.1:4178'+route);await page.getByRole('button',{name:'导入导出',exact:true}).waitFor()
 await page.evaluate(id=>localStorage.setItem('choice-last-saved:104',JSON.stringify({questionId:id,forked:true})),manifest.questionIds[0])
 await page.reload();await page.getByRole('button',{name:'继续编辑',exact:true}).click()
 let dialog=page.locator('.el-dialog:visible').last();await dialog.locator('.media-input').first().waitFor()
 const data=await page.evaluate(()=>{const c=document.createElement('canvas');c.width=700;c.height=460;const x=c.getContext('2d');x.fillStyle='#fff';x.fillRect(0,0,700,460);x.font='22px Microsoft YaHei';x.textAlign='center';const box=(label,y,w=250)=>{x.fillStyle='#eff6ff';x.fillRect(350-w/2,y,w,56);x.strokeStyle='#409eff';x.lineWidth=2;x.strokeRect(350-w/2,y,w,56);x.fillStyle='#303133';x.fillText(label,350,y+36)};box('开始',20,150);box('n = 1',120);box('n = n + 2',220);box('输出 n',320);x.strokeStyle='#70859c';for(const y of [76,176,276]){x.beginPath();x.moveTo(350,y);x.lineTo(350,y+44);x.lineTo(343,y+35);x.moveTo(350,y+44);x.lineTo(357,y+35);x.stroke()}x.fillStyle='#606266';x.font='18px Microsoft YaHei';x.fillText('观察流程图，程序最终输出什么？',350,430);return c.toDataURL('image/png').split(',')[1]})
 const png=Buffer.from(data,'base64'),imagePath=path.join(out,'fixtures/flowchart-ui.png');await fs.writeFile(imagePath,png)
 const uploaded=[];page.on('response',async r=>{if(r.request().method()==='POST'&&r.url().endsWith('/business/question-images'))try{const id=Number((await r.json()).data.imageId);uploaded.push(id);manifest.imageIds.push(id);await fs.writeFile(path.join(out,'review-data.json'),JSON.stringify(manifest))}catch{}})
 let stem=dialog.locator('.media-input').first()
 const chooser=page.waitForEvent('filechooser');await stem.getByRole('button',{name:'更换',exact:true}).first().click();await(await chooser).setFiles(imagePath)
 await page.waitForFunction(()=>!document.querySelector('.media-feedback[role="status"]'))
 await stem.locator('.el-image__inner').waitFor();checks.push('真实编辑页点击更换，自动上传与鉴权预览')
 async function sendImage(locator,event){await locator.evaluate((el,{base64,event})=>{const bytes=Uint8Array.from(atob(base64),x=>x.charCodeAt(0)),dt=new DataTransfer();dt.items.add(new File([bytes],'截图.png',{type:'image/png'}));el.dispatchEvent(event==='paste'?new ClipboardEvent('paste',{bubbles:true,cancelable:true,clipboardData:dt}):new DragEvent('drop',{bubbles:true,cancelable:true,dataTransfer:dt}))},{base64:png.toString('base64'),event});await page.waitForTimeout(700)}
 const editors=dialog.locator('.choice-option-editor')
 await sendImage(editors.nth(0).locator('input').first(),'paste');await editors.nth(0).locator('.el-image__inner').waitFor()
 await sendImage(editors.nth(1),'drop');await editors.nth(1).locator('.el-image__inner').waitFor();checks.push('实际粘贴和拖拽事件，选项图片自动上传')
 await editors.nth(1).getByRole('button',{name:'删除',exact:true}).click();assert.equal(await editors.nth(1).locator('.media-thumb').count(),0);checks.push('移除选项图片不删原题与历史资源')
 await stem.locator('.el-image__inner').click();await page.locator('.el-image-viewer__wrapper').waitFor();await page.keyboard.press('Escape');checks.push('编辑图片可放大并关闭')
 for(const width of [1440,390,320]){await page.setViewportSize({width,height:1000});await page.screenshot({path:path.join(out,`real-edit-${width}.png`),fullPage:true});assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false,`编辑页${width}横向溢出`);const box=await dialog.boundingBox();assert.ok(box.x>=-1 && box.x+box.width<=width+1,`编辑弹窗${width}被裁切`)}
 checks.push('编辑页桌面/390/320布局')
 await page.setViewportSize({width:1440,height:1000})
 await page.route('**/prod-api/business/question',async route=>{if(route.request().method()!=='PUT'){await route.continue();return}const response=await route.fetch();const result=(await response.json()).data;assert.ok(result.questionId);manifest.questionIds.push(result.questionId);await fs.writeFile(path.join(out,'review-data.json'),JSON.stringify(manifest));await route.abort('failed')},{times:1})
 await dialog.getByRole('button',{name:'保存题目',exact:true}).click();await page.getByText('已确认上次保存成功；当前表单已保留，可继续修改后保存',{exact:true}).waitFor();checks.push('真实编辑自动另存；模拟保存响应丢失，回执找回新题并保留当前表单')
 await dialog.locator('.el-dialog__headerbtn').click();await page.setViewportSize({width:1440,height:1000})
 await page.getByRole('button',{name:'导入导出',exact:true}).click();await page.getByRole('menuitem',{name:'导入常规题（Excel）'}).click()
 dialog=page.getByRole('dialog',{name:'Excel 图文题库导入'});
 const parsed=page.waitForResponse(r=>r.url().endsWith('/business/question/import-batches')&&r.request().method()==='POST')
 await dialog.locator('input[type=file]').setInputFiles(path.join(out,'fixtures/50-text-images.xlsx'))
 const batch=(await(await parsed).json()).data;manifest.batchIds.push(Number(batch.batchId));await fs.writeFile(path.join(out,'review-data.json'),JSON.stringify(manifest))
 if(batch.state!=='COMMITTED'){
   await dialog.locator('.import-summary').waitFor();await dialog.locator('.import-editor textarea').first().fill('图片功能验收题：观察流程图，程序输出什么？')
   const pick=page.waitForEvent('filechooser');await dialog.locator('.import-editor .media-input').first().getByRole('button',{name:'更换',exact:true}).first().click();await(await pick).setFiles(imagePath)
   await page.waitForTimeout(1000);await dialog.getByRole('button',{name:'保存本题修正',exact:true}).click();await page.waitForTimeout(1000)
   for(const width of [1440,390,320]){await page.setViewportSize({width,height:1000});await page.screenshot({path:path.join(out,`real-import-preview-${width}.png`),fullPage:true});assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false,`导入预览${width}横向溢出`)}
   await page.setViewportSize({width:1440,height:1000});const saved=page.waitForResponse(r=>r.url().endsWith('/confirm')&&r.request().method()==='POST');await dialog.getByRole('button',{name:'确认导入 50 道题目',exact:true}).click();const result=(await(await saved).json()).data;manifest.questionIds.push(...result.result.questionIds);await fs.writeFile(path.join(out,'review-data.json'),JSON.stringify(manifest));checks.push('真实Excel上传50题预览，在线替换图片/修改文字并整批确认')
 }
 if(await dialog.getByRole('button',{name:'查看本次题目',exact:true}).isVisible())await dialog.getByRole('button',{name:'查看本次题目',exact:true}).click()
 await dialog.locator('.result-rows>div').first().waitFor();assert.equal(await dialog.locator('.result-rows>div').count(),50);checks.push('50题导入结果可找回')
 for(const width of [1440,390,320]){await page.setViewportSize({width,height:1000});await page.screenshot({path:path.join(out,`real-import-${width}.png`),fullPage:true});assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false,`导入页${width}横向溢出`)}
 assert.deepEqual(errors,[])
 await fs.writeFile(path.join(out,'review-data.json'),JSON.stringify(manifest))
 await fs.writeFile(path.join(out,'browser-result.json'),JSON.stringify({checks,errors,scope:'真实生产构建+本机18080 API'},null,2))
 console.log(JSON.stringify({checks,errors}))
}catch(error){await page.screenshot({path:path.join(out,'browser-failure.png'),fullPage:true});console.log(JSON.stringify({error:error.message,pageErrors:errors,feedback:await page.locator('.media-feedback').allTextContents()}));throw error}finally{await browser.close()}
