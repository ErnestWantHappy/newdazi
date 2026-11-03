import { createRequire } from 'node:module'
import { pathToFileURL } from 'node:url'
import path from 'node:path'
import fs from 'node:fs/promises'
import assert from 'node:assert/strict'

// 真实教师首页与推进组件配模拟接口，避免推进在用班级。
const root = path.resolve('RuoYi-Vue3')
const require = createRequire(path.join(root, 'package.json'))
const { createServer } = await import(pathToFileURL(require.resolve('vite')).href)
const { default: vue } = await import(pathToFileURL(require.resolve('@vitejs/plugin-vue')).href)
const { chromium } = require('playwright')
const output = path.resolve('output/playwright/course-advance-20261020')
await fs.mkdir(output, { recursive: true })
const modules = {
  'advance-request': 'export default config => window.mockRequest(config)',
  'advance-entry': `
    import {createApp} from 'vue';import {createRouter,createMemoryHistory} from 'vue-router';
    import ElementPlus from 'element-plus';import 'element-plus/dist/index.css';
    import * as icons from '@element-plus/icons-vue';import Teacher from '/src/views/business/teacher/index.vue';
    window.scenario='mixed';window.requests=[];window.moved=false;window.dashboardReads=0;
    window.delayed={};
    const groups=[{entryYear:'2022',gradeId:5,gradeName:'五年级',allClassesInGrade:['1','2','3','4'],lessons:[
      {lessonId:608,lessonNum:2,lessonTitle:'体验算法控制',grade:5,lessonMode:'assessment',assignedClasses:['1','2','3','4'],canDesign:true,canDelete:false},
      {lessonId:990674,lessonNum:4,lessonTitle:'双分支结构',grade:5,lessonMode:'assessment',assignedClasses:[],canDesign:true,canDelete:false}
    ]}];
    window.mockRequest=async ({url,data})=>{
      if(url.endsWith('/dashboard-data')){window.dashboardReads++;return {data:groups};}
      if(url.endsWith('/advance-policy')){
        if(window.scenario==='policy-error')throw new Error('模拟读取失败');
        return {data:{autoAdvanceEnabled:false,autoAdvanceThresholdPct:50,autoAdvanceDelayHours:2}};
      }
      if(url.endsWith('/advance-preview')){
        const scenario=window.scenario;
        if(scenario==='preview-error')throw new Error('模拟网络失败');
        if(scenario==='delayed')await new Promise(resolve=>window.delayed[data.classCodes.join(',')]=resolve);
        return {data:{rows:data.classCodes.map(code=>({
          classCode:code,currentLessonId:window.moved&&code==='1'?990674:608,
          currentLessonTitle:window.moved&&code==='1'?'第4课 双分支结构':'第2课 体验算法控制',
          nextLessonId:code==='3'?null:990674,nextLessonTitle:code==='3'?'暂无下一课，请先备课':'第4课 双分支结构',
          canAdvance:!window.moved&&(code==='1'||code==='4')&&scenario!=='blocked',
          readyCount:code==='2'?19:code==='1'&&window.moved?0:39,totalCount:40,requiredCount:20,thresholdPct:50,
          reason:code==='3'?'没有可推进的下一课，请先备好同年级下一课':window.moved?'有成绩0/40人，需达到50%':'有成绩19/40人，需达到50%',
          autoStatus:code==='4'?'已达标，正在等待':'已关闭，不会自动推进',autoDueTime:code==='4'?Date.now()+3600000:null
        }))}};
      }
      if(url.endsWith('/manual-advance')){
        window.requests.push(data);
        if(window.scenario==='submit-error')throw new Error('模拟响应中断');
        window.moved=true;
        return {data:{advanced:1,failed:1,details:[
          {classCode:'1',success:true,message:'从「体验算法控制」→「双分支结构」'},
          {classCode:'4',success:false,message:'当前课程已变化，本次未推进；请刷新状态后核对'}
        ]}};
      }
      return {code:200,data:{hasTask:false}};
    };
    const router=createRouter({history:createMemoryHistory(),routes:[{path:'/:pathMatch(.*)*',component:{template:'<div/>'}}]});
    await router.push('/');await router.isReady();
    const app=createApp(Teacher);app.use(router).use(ElementPlus);for(const [name,icon] of Object.entries(icons))app.component(name,icon);
    app.directive('hasPermi',{});app.mount('#app');
  `
}
const server = await createServer({
  configFile: false, root, logLevel: 'error',
  plugins: [{
    name: 'advance-harness', enforce: 'pre',
    resolveId(id) { if (modules[id]) return '\0' + id },
    load(id) { return modules[id.slice(1)] },
    transform(code, id) {
      if (id.split('?')[0].replaceAll('\\', '/').endsWith('/business/teacher/index.vue') && !id.includes('?')) {
        return code.replace(/import ResearchNotificationBar[^\n]+/, 'const ResearchNotificationBar={template:"<div/>"}')
          .replace(/import ClassSelectionDialog[^\n]+/, 'const ClassSelectionDialog={template:"<div/>"}')
      }
    },
    configureServer(s) { s.middlewares.use('/advance-test', async (_req, res) => {
      res.setHeader('Content-Type', 'text/html')
      res.end(await s.transformIndexHtml('/advance-test', '<html><meta charset="utf-8"><div id="app"></div><script type="module" src="/@id/advance-entry"></script></html>'))
    }) }
  }, vue()],
  resolve: { alias: [{find:/^vue$/,replacement:require.resolve('vue/dist/vue.esm-bundler.js')}, {find:'@/utils/request',replacement:'advance-request'}, {find:'@',replacement:path.join(root,'src')}] },
  cacheDir: path.join(output,'.vite-cache'),
  optimizeDeps: {entries:[],include:['vue','vue-router','element-plus','@element-plus/icons-vue']},
  server: {host:'127.0.0.1',port:18129,strictPort:true}
})
const checks=[], errors=[]
let browser
try {
  await server.listen()
  browser=await chromium.launch({headless:true})
  const page=await browser.newPage({viewport:{width:1440,height:1000}})
  page.on('pageerror',error=>errors.push(error.message))
  const dialog=page.getByRole('dialog',{name:'课堂推进',exact:true})
  const submit=dialog.getByRole('button',{name:/推进 \d+ 个达标班级/})
  async function open(scenario='mixed') {
    await page.goto('http://127.0.0.1:18129/advance-test')
    await page.getByText('体验算法控制',{exact:true}).waitFor()
    await page.evaluate(s=>window.scenario=s,scenario)
    await page.getByRole('button',{name:'手动一键课堂推进',exact:true}).click()
    await dialog.waitFor()
    await page.waitForFunction(()=>!document.querySelector('.lesson-advance-dialog .el-loading-mask'))
  }
  await open()
  assert.equal(await dialog.locator('.advance-class').count(),4)
  await dialog.getByText('暂无下一课，请先备课').waitFor()
  await dialog.getByText(/预计.*附近执行/).waitFor()
  assert.equal(await submit.isEnabled(),true)
  checks.push('真实教师首页入口：四班预检、课次缺号、人数门槛、无下一课、自动关闭与预计时间可见')
  await page.screenshot({path:path.join(output,'desktop-preview.png'),fullPage:true})
  await submit.click()
  await page.getByRole('button',{name:'返回核对',exact:true}).click()
  assert.equal(await page.evaluate(()=>window.requests.length),0)
  await submit.click()
  await page.getByRole('button',{name:'推进达标班级',exact:true}).click()
  await page.getByRole('dialog',{name:'确认课堂推进',exact:true}).waitFor({state:'hidden'})
  await dialog.getByText('已推进 1 个班级，1 个未推进',{exact:true}).waitFor()
  await dialog.getByText('本轮已结束，请先刷新状态',{exact:true}).waitFor()
  assert.equal(await submit.isEnabled(),false)
  const sent=await page.evaluate(()=>window.requests[0])
  assert.deepEqual(sent.classCodes,['1','4'])
  assert.deepEqual(sent.expectedLessonIds,{'1':608,'4':608})
  assert.ok(await page.evaluate(()=>window.dashboardReads)>=2)
  await dialog.locator('.advance-class').first().getByText('第4课 双分支结构').first().waitFor()
  checks.push('确认取消不写入；只发送达标班级及课程快照；部分成功结果保留、首页和预检刷新、重复点击被禁止')
  await page.screenshot({path:path.join(output,'desktop-result.png'),fullPage:true})

  await open('preview-error')
  await dialog.getByText(/未能读取推进状态/).waitFor()
  assert.equal(await submit.isEnabled(),false)
  await page.evaluate(()=>window.scenario='mixed')
  await dialog.getByRole('button',{name:'刷新状态',exact:true}).click()
  await submit.waitFor({state:'visible'})
  await page.waitForFunction(()=>document.querySelectorAll('.advance-class').length===4)
  assert.equal(await submit.isEnabled(),true)
  checks.push('预检失败就地说明、禁止提交，刷新恢复且班级选择保持')

  await page.evaluate(()=>window.scenario='delayed')
  await dialog.getByRole('button',{name:'刷新状态',exact:true}).click()
  await dialog.getByText('正在核对各班当前课程和推进条件…',{exact:true}).waitFor()
  assert.equal(await submit.isEnabled(),false)
  await page.waitForFunction(()=>typeof window.delayed['1,2,3,4']==='function')
  await dialog.locator('.advance-filters .el-select').nth(1).locator('.el-tag__close').first().click()
  await page.waitForFunction(()=>typeof window.delayed['2,3,4']==='function')
  await page.evaluate(()=>window.delayed['2,3,4']())
  await page.waitForFunction(()=>document.querySelectorAll('.advance-class').length===3)
  await page.evaluate(()=>window.delayed['1,2,3,4']())
  await page.waitForTimeout(150)
  assert.deepEqual(await dialog.locator('.advance-class-title strong').allTextContents(),['2班','3班','4班'])
  checks.push('慢请求显示核对中且禁用提交，切换班级后迟到响应不能覆盖新选择')

  await open('submit-error')
  await submit.click()
  await page.getByRole('button',{name:'推进达标班级',exact:true}).click()
  await page.getByRole('dialog',{name:'确认课堂推进',exact:true}).waitFor({state:'hidden'})
  await dialog.getByText(/未能确认本次推进结果/).waitFor()
  assert.equal(await submit.isEnabled(),false)
  assert.equal(await page.evaluate(()=>window.requests.length),1)
  checks.push('执行响应中断提示可能部分成功，不假报全部失败，不自动重试')

  await open('blocked')
  assert.equal(await submit.isEnabled(),false)
  checks.push('无可推进班级时主按钮禁用，阻断原因可见')

  await page.setViewportSize({width:390,height:844})
  await open()
  const mobile=await dialog.boundingBox()
  assert.ok(mobile.x>=0&&mobile.x+mobile.width<=391)
  assert.equal(await dialog.evaluate(node=>node.scrollWidth<=node.clientWidth+1),true)
  await submit.scrollIntoViewIfNeeded()
  assert.equal(await submit.isVisible(),true)
  await page.screenshot({path:path.join(output,'mobile-preview.png'),fullPage:true})
  await submit.click()
  await page.getByRole('button',{name:'推进达标班级',exact:true}).click()
  await page.getByRole('dialog',{name:'确认课堂推进',exact:true}).waitFor({state:'hidden'})
  await dialog.getByText('已推进 1 个班级，1 个未推进',{exact:true}).waitFor()
  const visibleResult=await dialog.locator('.advance-results').evaluate(node=>{
    const box=node.getBoundingClientRect(), body=node.closest('.el-dialog__body').getBoundingClientRect()
    return box.top>=body.top-1&&box.bottom<=body.bottom+1
  })
  assert.equal(visibleResult,true)
  await page.screenshot({path:path.join(output,'mobile-result.png'),fullPage:true})
  await dialog.getByRole('button',{name:'关闭',exact:true}).click()
  await dialog.waitFor({state:'hidden'})
  checks.push('390px手机：弹窗无横向溢出，主操作可点，结果可读，弹窗可关闭')

  await page.setViewportSize({width:1440,height:1000})
  await page.evaluate(()=>window.scenario='policy-error')
  await page.getByRole('button',{name:'设置',exact:true}).click()
  const settings=page.getByRole('dialog',{name:'课程推进设置',exact:true})
  await settings.getByText(/未能读取真实设置/).waitFor()
  assert.equal(await settings.getByRole('button',{name:'保存',exact:true}).isEnabled(),false)
  checks.push('自动设置读取失败不显示虚假的默认关闭，不允许覆盖保存')
  assert.deepEqual(errors,[])
  await fs.writeFile(path.join(output,'result.json'),JSON.stringify({checks,errors},null,2))
  console.log(JSON.stringify({passed:checks.length,checks,errors},null,2))
} finally {
  await browser?.close()
  await server.close()
}
