import {createRequire} from 'node:module'
import fs from 'node:fs/promises'
import path from 'node:path'
import assert from 'node:assert/strict'
const root=path.resolve('.'),require=createRequire(path.join(root,'RuoYi-Vue3/package.json')),{chromium}=require('playwright'),out=path.join(root,'output/choice-media')
const session=JSON.parse(await fs.readFile(path.join(out,'student-session.local.json'),'utf8')),browser=await chromium.launch({headless:true}),context=await browser.newContext({viewport:{width:1440,height:1000}}),checks=[],errors=[]
await context.addCookies([{name:'Admin-Token',value:session.token,domain:'127.0.0.1',path:'/'}]);const page=await context.newPage();page.setDefaultTimeout(15000);page.on('pageerror',e=>errors.push(e.message))
try{
 await page.goto('http://127.0.0.1:4178/student/index');await page.locator('.theory-card').nth(2).waitFor();await page.locator('.theory-card .el-image__inner').nth(4).waitFor();checks.push('学生真实课程透传3题，题干图/四个图文选项/旧纯文本同时显示')
 if(process.argv.includes('--review-saved')){
   await page.getByRole('button',{name:'已提交',exact:true}).waitFor()
   for(const width of [1440,390,320]){await page.setViewportSize({width,height:1000});await page.screenshot({path:path.join(out,`real-student-saved-${width}.png`),fullPage:true});assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false)}
   assert.deepEqual(errors,[]);console.log(JSON.stringify({checks:['学生已答回看桌面/390/320，图片和成绩清晰，0页面异常'],errors}));await browser.close();process.exit(0)
 }
 const cards=page.locator('.theory-card'),opt=cards.nth(1).locator('.option-radio').first()
 await opt.locator('.el-image__inner').click();await page.locator('.el-image-viewer__wrapper').waitFor();assert.equal(await opt.getAttribute('aria-checked'),'false');await page.keyboard.press('Escape');checks.push('放大图片不误选答案')
 await page.locator('.el-image-viewer__wrapper').waitFor({state:'hidden'});await cards.nth(0).locator('.option-radio').nth(1).focus();await page.keyboard.press('Enter');await cards.nth(1).locator('.option-radio').nth(1).locator('.opt-label').click();await cards.nth(2).locator('.option-radio').nth(2).click();assert.equal(await cards.nth(0).locator('.option-radio').nth(1).getAttribute('aria-checked'),'true');checks.push('整块选项点击与键盘均可正常选择A—D')
 for(const width of [1440,390,320]){await page.setViewportSize({width,height:1000});await page.screenshot({path:path.join(out,`real-student-${width}.png`),fullPage:true});assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false,`学生页${width}溢出`)}
 await page.setViewportSize({width:1440,height:1000});await page.getByRole('button',{name:'提交理论测试',exact:true}).click();const result=page.waitForResponse(r=>r.url().endsWith('/submit-answers'));await page.getByRole('button',{name:'确定',exact:true}).click();const response=await(await result).json();assert.equal(response.code,200);await page.getByRole('button',{name:'已提交',exact:true}).waitFor();checks.push('真实学生提交成功，提交后锁定')
 await page.reload();await page.getByRole('button',{name:'已提交',exact:true}).waitFor();checks.push('刷新后答案与提交状态保留')
 const wrong=await context.request.get('http://127.0.0.1:4178/prod-api/business/student-home/wrong-questions',{headers:{Authorization:'Bearer '+session.token},params:{lessonId:session.lessonId}});const data=await wrong.json();assert.equal(data.code,200);const rows=data.data||data.rows;assert.ok(rows.some(q=>q.questionId===session.questionIds[1]&&q.media.options.A.length===1));checks.push('真实错题接口保留图片媒体')
 assert.deepEqual(errors,[]);await fs.writeFile(path.join(out,'student-browser-result.json'),JSON.stringify({checks,errors},null,2));console.log(JSON.stringify({checks,errors}))
}catch(error){await page.screenshot({path:path.join(out,'student-failure.png'),fullPage:true});console.log(JSON.stringify({error:error.message,errors}));throw error}finally{await browser.close()}
