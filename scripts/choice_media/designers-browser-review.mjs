import {createRequire} from 'node:module'
import fs from 'node:fs/promises'
import path from 'node:path'
import assert from 'node:assert/strict'
const root=path.resolve('.'),require=createRequire(path.join(root,'RuoYi-Vue3/package.json')),{chromium}=require('playwright'),out=path.join(root,'output/choice-media'),fixture=JSON.parse(await fs.readFile(path.join(out,'student-session.local.json'),'utf8')),browser=await chromium.launch({headless:true}),checks=[],errors=[]
try{
 for(const [role,url,name] of [['teacher',`/business/lesson-auth/designer/${fixture.lessonId}`,'lesson'],['researcher',`/business/county-exam-designer/${fixture.examId}`,'exam']]){
   const session=JSON.parse(await fs.readFile(path.join(out,`${role}-session.local.json`),'utf8')),context=await browser.newContext({viewport:{width:1440,height:1000}});await context.addCookies([{name:'Admin-Token',value:session.token,domain:'127.0.0.1',path:'/'}]);const page=await context.newPage();page.setDefaultTimeout(15000);page.on('pageerror',e=>errors.push(e.message))
   try{await page.goto('http://127.0.0.1:4178'+url);await page.getByRole('button',{name:'预览图文',exact:true}).first().waitFor();await page.getByRole('button',{name:'预览图文',exact:true}).first().click();const dialog=page.getByRole('dialog',{name:'图文题目预览'});await dialog.locator('.el-image__inner').first().waitFor();checks.push(`${name}真实组卷页面缩略入口与完整图文预览`)
   for(const width of [1440,390,320]){await page.setViewportSize({width,height:1000});await page.screenshot({path:path.join(out,`real-${name}-preview-${width}.png`),fullPage:true});const box=await dialog.boundingBox();assert.ok(box.x>=-1 && box.x+box.width<=width+1,`${name}弹窗被裁切`)}
   }catch(error){await page.screenshot({path:path.join(out,`${name}-failure.png`),fullPage:true});throw error}finally{await context.close()}
 }
 assert.deepEqual(errors,[]);await fs.writeFile(path.join(out,'designers-browser-result.json'),JSON.stringify({checks,errors},null,2));console.log(JSON.stringify({checks,errors}))
}finally{await browser.close()}
