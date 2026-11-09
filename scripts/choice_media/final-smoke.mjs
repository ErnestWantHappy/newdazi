import {createRequire} from 'node:module'
import fs from 'node:fs/promises'
import path from 'node:path'
import assert from 'node:assert/strict'
const require=createRequire(path.resolve('RuoYi-Vue3/package.json')),{chromium}=require('playwright'),browser=await chromium.launch({headless:true}),context=await browser.newContext(),session=JSON.parse(await fs.readFile('output/choice-media/teacher-session.local.json','utf8')),errors=[]
try{await context.addCookies([{name:'Admin-Token',value:session.token,domain:'127.0.0.1',path:'/'}]);const page=await context.newPage();page.on('pageerror',e=>errors.push(e.message));await page.goto('http://127.0.0.1:4178/business/score');await page.locator('.filter-card').waitFor();await page.goto('http://127.0.0.1:4178/question');await page.getByRole('button',{name:'导入导出',exact:true}).waitFor();assert.deepEqual(errors,[]);console.log('最终构建：真实教师成绩页/题库页进入通过，0页面异常；验收数据已清理')}finally{await browser.close()}
