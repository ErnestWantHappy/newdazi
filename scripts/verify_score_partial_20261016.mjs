import { createRequire } from 'node:module'
import { pathToFileURL } from 'node:url'
import path from 'node:path'
import fs from 'node:fs/promises'
import assert from 'node:assert/strict'

// 真实页面和图表配模拟接口，不连接业务服务，不写教师或学生成绩。
const root = path.resolve('RuoYi-Vue3')
const require = createRequire(path.join(root, 'package.json'))
const { createServer } = await import(pathToFileURL(require.resolve('vite')).href)
const { default: vue } = await import(pathToFileURL(require.resolve('@vitejs/plugin-vue')).href)
const autoImportModule = await import(pathToFileURL(require.resolve('unplugin-auto-import/vite')).href)
const autoImport = autoImportModule.default.default || autoImportModule.default
const { chromium } = require('playwright')
const output = path.resolve('output/playwright/score-partial-20261016')
await fs.mkdir(output, { recursive: true })
const modules = {
  'score-user': `export default () => ({id:1,token:'mock',roles:['teacher'],name:'验收账号',avatar:''})`,
  'score-request': `export default async config => window.mockRequest(config)`,
  'score-entry': `
    import {createApp} from 'vue';import {createRouter,createMemoryHistory} from 'vue-router';
    import ElementPlus from 'element-plus';import 'element-plus/dist/index.css';
    import * as icons from '@element-plus/icons-vue';import Score from '/src/views/business/score/index.vue';import Student from '/src/views/student/index.vue';
    const student = new URLSearchParams(location.search).get('mode') === 'student';
    window.revision=2;window.saved={score:40,answer:'测试原文',typingSpeed:24,accuracyRate:100,completionRate:100};
    window.studentRows=Array.from({length:45},(_,i)=>({studentId:i+1,studentNo:String(i+1),studentName:'学生'+String(i+1).padStart(2,'0'),classCode:'5',grade:5,scores:[{
      lessonId:537,lessonNum:3,lessonTitle:'成绩口径验收课',typingScore:15,theoryScore:20,practicalScore:null,practicalPending:true,pendingGrading:true,
      totalScore:i===0?35:30+i,finalScore:i===0?40:35+i,performanceScore:5,avgTypingSpeed:24,avgAccuracyRate:98,avgCompletionRate:50,
      ...(i===41?{isAbsent:true,finalScore:null}:{}),...(i===42?{isMissing:true,finalScore:null}:{}),...(i===43?{unsubmitted:true,finalScore:null}:{}),...(i===44?{totalScore:0,finalScore:0,practicalScore:0,practicalPending:false,pendingGrading:false}:{})
    }]}));
    window.mockRequest=async ({url,data})=>{
      if(url.endsWith('/score/classes'))return {data:[{entryYear:'2022',classCode:'5'}]};
      if(url.endsWith('/score/lessons'))return {data:[{lessonId:537,lessonNum:3,lessonTitle:'成绩口径验收课'}]};
      if(url.endsWith('/score/summary'))return {rows:structuredClone(window.studentRows)};
      if(url.endsWith('/score/theory-redo'))return {data:{isCurrent:true,hasTyping:true,students:[]}};
      if(url.endsWith('/score/lesson-gate'))return {data:{isCurrent:true,theoryOpen:true,practicalOpen:true}};
      if(url.endsWith('/student/check'))return {data:{hasExam:false}};
      if(url.endsWith('/current-lesson'))return {hasLesson:true,lessonId:537,lessonTitle:'成绩口径验收课',typingRevision:window.revision,theoryOpen:true,practicalOpen:true,studentInfo:{studentId:1,studentName:'验收学生',entryYear:'2022',classCode:'5',gradeName:'五年级'},questions:[{questionId:1,questionType:'typing',questionContent:'测试原文',questionScore:50,typingDuration:5}],submittedAnswers:window.saved?{1:structuredClone(window.saved)}:{}};
      if(url.endsWith('/submit-answers'))return {totalScore:window.saved?.score??5,typingResults:{1:window.saved??{score:5,answer:'测',typingSpeed:2,accuracyRate:100,completionRate:25}}};
      return {code:200,data:[]};
    };
    const router=createRouter({history:createMemoryHistory(),routes:[{path:'/:pathMatch(.*)*',component:{template:'<div/>'}}]});
    await router.push('/?lessonId=537&entryYear=2022&classCode=5');await router.isReady();
    const app=createApp(student?Student:Score);app.use(router).use(ElementPlus);for(const [name,icon] of Object.entries(icons))app.component(name,icon);
    app.directive('hasPermi',{});app.mount('#app');
  `
}
const server = await createServer({
  configFile: false, root, logLevel: 'error',
  plugins: [{
    name: 'score-harness', enforce: 'pre',
    resolveId(id) { if (modules[id]) return '\0'+id },
    load(id) { return modules[id.slice(1)] },
    transform(code,id) {
      const clean=id.split('?')[0].replaceAll('\\','/')
      if (!id.includes('?') && (clean.endsWith('/views/student/index.vue') || clean.endsWith('/business/score/index.vue'))) {
        // 与本轮无关的弹窗及外部编辑器不进入浏览器验收。
        code=code.replace(/import\s+(\w+)\s+from\s+(['"])([^'"\n]+\.vue)\2;?/g,(whole,name,quote,target)=>
          clean.endsWith('/views/student/index.vue') || !target.includes('/charts/') ? 'const '+name+'={template:"<div/>"};' : whole)
        const expose=clean.endsWith('/views/student/index.vue')
          ? 'window.__student={typingStates,typingMyScore,courseMyScore,fetchData,autoSubmitTyping};'
          : 'window.__score={tableData,handleQuery};window.__echarts=echarts;'
        return code.replace('</script>',expose+'\n</script>')
      }
    },
    configureServer(s) { s.middlewares.use('/score-test',async(_req,res)=>{
      res.setHeader('Content-Type','text/html');res.end(await s.transformIndexHtml('/score-test','<html><meta charset="utf-8"><div id="app"></div><script type="module" src="/@id/score-entry"></script></html>'))
    }) }
  },vue(),autoImport({imports:['vue','vue-router'],dts:false})],
  resolve:{alias:[{find:/^vue$/,replacement:require.resolve('vue/dist/vue.esm-bundler.js')},{find:'@/utils/request',replacement:'score-request'},{find:'@/store/modules/user',replacement:'score-user'},{find:'@',replacement:path.join(root,'src')}]},
  cacheDir:path.join(output,'.vite-cache'),
  optimizeDeps:{entries:[],include:['vue','vue-router','element-plus','echarts','@element-plus/icons-vue']},
  server:{host:'127.0.0.1',port:18126,strictPort:true}
})
const checks=[], errors=[]
let browser
try {
  await server.listen();browser=await chromium.launch({headless:true})
  const page=await browser.newPage({viewport:{width:1440,height:1000}})
  page.on('pageerror',error=>{errors.push(error.message);console.error(error.message)})
  await page.goto('http://127.0.0.1:18126/score-test')
  await page.waitForFunction(()=>window.__score?.tableData.value.length===45)
  await page.waitForFunction(()=>document.querySelectorAll('canvas').length>=1)
  const state=await page.evaluate(()=>({count:window.__score.tableData.value.length,first:window.__score.tableData.value[0],last:window.__score.tableData.value[44]}))
  assert.equal(state.count,45);assert.equal(state.first.filteredTotal,35);assert.equal(state.first.finalTotal,40);assert.equal(state.last.finalTotal,0)
  await page.getByRole('cell',{name:'未提交',exact:true}).first().waitFor()
  checks.push('真实成绩页：45人全部保留，35作业分+5表现分=40，未批操作不阻断')
  // 获取实际ECharts选项，核对画布人数、全量显示与零分/缺考标签。
  const option=await page.evaluate(async()=>{
    const echarts=window.__echarts;
    const dom=Array.from(document.querySelectorAll('[_echarts_instance_]')).find(el=>echarts.getInstanceByDom(el)?.getOption()?.series?.[0]?.type==='bar');
    const chart=echarts.getInstanceByDom(dom), opt=chart.getOption();return {names:opt.xAxis[0].data,zoom:opt.dataZoom[0]?.end,labels:opt.series[0].data.map((value,dataIndex)=>{const label=opt.series[0].label.formatter({value,dataIndex});return typeof label==='string'?label.replaceAll('\n',''):label})};
  })
  assert.equal(option.names.length,45);assert.equal(option.zoom,100);assert.ok(option.labels.includes('请假'));assert.ok(option.labels.includes('缺考'));assert.ok(option.labels.includes('未提交'));assert.ok(option.labels.includes(0))
  checks.push('真实图表：45个横轴项、默认显示100%，请假缺考未提交显示状态，真实0分显示0')
  for(const width of [1440,1920,1024]) {
    await page.setViewportSize({width,height:1000});await page.screenshot({path:path.join(output,'teacher-'+width+'.png'),fullPage:true})
  }
  await page.goto('http://127.0.0.1:18126/score-test?mode=student')
  await page.getByText('本轮最高：40分',{exact:false}).waitFor()
  await page.getByRole('button',{name:'重新打字',exact:true}).click()
  assert.equal(await page.evaluate(()=>window.__student.typingMyScore.value),40)
  assert.equal(await page.evaluate(()=>window.__student.courseMyScore.value),40)
  await page.getByText('本轮最高：40分',{exact:false}).waitFor()
  checks.push('真实学生页：自主重新打字后最高分和课程得分仍为40')
  await page.evaluate(()=>window.__student.autoSubmitTyping(1))
  await page.waitForFunction(()=>window.__student.typingStates.value[1].submitted)
  assert.equal(await page.evaluate(()=>window.__student.typingMyScore.value),40)
  await page.getByText('对应速度 24字/分',{exact:false}).waitFor()
  await page.screenshot({path:path.join(output,'student-best.png'),fullPage:true})
  checks.push('低分重打提交后仍显示最高40及对应速度24，分数统计一致')
  await page.evaluate(async()=>{window.revision=3;window.saved=null;await window.__student.fetchData({silent:true})})
  assert.equal(await page.evaluate(()=>window.__student.typingMyScore.value),null)
  assert.equal(await page.getByText('本轮最高：40分',{exact:false}).count(),0)
  checks.push('教师新轮清分后最高分提示消失，旧分不带入新轮')
  assert.deepEqual(errors,[])
  await fs.writeFile(path.join(output,'verification.json'),JSON.stringify({checks,errors,mocked:true},null,2))
  console.log(JSON.stringify({checks,errors}))
} finally { await browser?.close();await server.close() }
