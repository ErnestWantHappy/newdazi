import {createRequire} from 'node:module'
import path from 'node:path'
import {pathToFileURL} from 'node:url'
import assert from 'node:assert/strict'
const require=createRequire(path.resolve('RuoYi-Vue3/package.json')),{chromium}=require('playwright'),browser=await chromium.launch({headless:true}),page=await browser.newPage({viewport:{width:1440,height:1000}}),errors=[];page.on('pageerror',e=>errors.push(e.message))
try{await page.goto(pathToFileURL(path.resolve('docs/choice-question-images-ui/implemented/index.html')).href);for(let i=0;i<5;i++){await page.locator('nav button').nth(i).click();await page.waitForFunction(()=>{const image=document.querySelector('#shot');return image.complete&&image.naturalWidth>0})}await page.setViewportSize({width:390,height:900});assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false);assert.deepEqual(errors,[]);console.log('真实截图入口：五页切换、原图加载、390px布局通过；0页面异常')}finally{await browser.close()}
