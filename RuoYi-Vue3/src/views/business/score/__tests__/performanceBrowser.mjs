import { createServer } from 'vite'
import { chromium } from 'playwright'
import assert from 'node:assert/strict'
import fs from 'node:fs/promises'

const pageSource = await fs.readFile('src/views/business/score/index.vue', 'utf8')
const handler = pageSource.slice(pageSource.indexOf('function handlePerformanceSaved('), pageSource.indexOf('\nfunction handleQuery()'))
const server = await createServer({
  server: { host: '127.0.0.1', port: 5198, strictPort: true, open: false },
  plugins: [{
    name: 'review-performance',
    resolveId(id) { if (id === 'virtual:review-entry') return '\0review-entry' },
    load(id) {
      if (id !== '\0review-entry') return
      return `import {createApp,h,ref} from 'vue'; import ElementPlus from 'element-plus'; import 'element-plus/dist/index.css';
        import Performance from '/src/views/business/score/components/PerformanceSection.vue';
        import {createPyodideRunner} from '/src/utils/pyodideRunner.js';
        window.runner=createPyodideRunner();
        const selectedLessonIds=ref([1]),rawData=ref([]),tableData=ref([]);
        const clampClientScore=n=>Math.max(0,Math.min(100,n));
        ${handler}
        createApp({render:()=>h(Performance,{lessonId:1,classCode:'1',entryYear:'2026',onSaved:handlePerformanceSaved})}).use(ElementPlus).mount('#app');`
    },
    configureServer(server) {
      server.middlewares.use('/__performance_review', (_, res) => {
        res.setHeader('Content-Type', 'text/html')
        res.end('<html><head><meta charset="utf-8"></head><body><div id="app"></div><script type="module" src="/@id/virtual:review-entry"></script></body></html>')
      })
    }
  }]
})
let browser
try {
  await server.listen()
  browser = await chromium.launch({ headless: true })
  const page = await browser.newPage({ viewport: { width: 1280, height: 900 } })
  const errors = [], saved = []
  let listCalls = 0
  page.on('pageerror', error => errors.push(error.message))
  await page.route('**/dev-api/**', async route => {
    const url = route.request().url()
    if (url.includes('/classroom-performance/list')) {
      listCalls++
      await route.fulfill({ json: { code: 200, data: Array.from({length:60}, (_, i) => ({ studentId:i+1,studentNo:String(i+1),studentName:`学生${i+1}`,score:0,reason:'' })) } })
    } else if (url.includes('/classroom-performance/batch-save')) {
      saved.push(route.request().postDataJSON())
      await new Promise(resolve => setTimeout(resolve, 600))
      await route.fulfill({ json: { code: 200 } })
    } else await route.abort()
  })
  await page.goto('http://127.0.0.1:5198/__performance_review')
  const row = page.locator('.el-table__body tbody tr').last()
  await row.waitFor()
  await page.evaluate(() => document.fonts.ready)
  // 等表格布局完成后以按钮可见位置作为基线，避免自动点击滚入视口被误判成保存刷新。
  await row.getByRole('button', {name:'加分',exact:true}).scrollIntoViewIfNeeded()
  await page.waitForTimeout(500)
  const scroll = page.locator('.el-table__body-wrapper .el-scrollbar__wrap')
  const before = await scroll.evaluate(el => el.scrollTop)
  const order = await page.locator('.el-table__body tbody tr').allTextContents()
  await row.getByRole('button', {name:'加分',exact:true}).click()
  await page.waitForFunction(() => document.querySelector('.el-table__body tbody tr:last-child .score-input').value === '1')
  await page.waitForTimeout(900)
  await row.getByRole('button', {name:'加分',exact:true}).click({clickCount:2})
  await page.waitForTimeout(2000)
  assert.equal(await row.locator('.score-input').inputValue(), '3')
  assert.equal(saved.at(-1).performances[0].score, 3)
  assert.equal(listCalls, 1)
  assert.equal(await scroll.evaluate(el => el.scrollTop), before)
  assert.deepEqual((await page.locator('.el-table__body tbody tr').allTextContents()).map(t => t.match(/学生\d+/)?.[0]), order.map(t => t.match(/学生\d+/)?.[0]))
  assert.deepEqual(errors, [])
  await page.screenshot({path:'../output/playwright/performance-continuous-score.png'})
  console.log(JSON.stringify({listCalls, saves:saved.length, finalScore:3, scrollTop:before, errors}))
  // 替身解释器只验证真实 Worker 的终止机制，不冒充 pandas/Matplotlib 的真实运行验收。
  await page.route('**/__pyodide/pyodide.mjs', route => route.fulfill({contentType:'text/javascript', body:`
    export async function loadPyodide(){return {
      loadPackage:async()=>{}, FS:{writeFile(){}}, globals:{set(){}},setStdout(){},setStderr(){},
      runPythonAsync:async(code)=>{if(code==='while True: pass'){while(true){}} return ''}
    }}
  `}))
  await page.evaluate(async () => {
    await window.runner.initPyodide(null, '/__pyodide/')
    window.ticks=0
    window.tickTimer=setInterval(()=>window.ticks++,100)
    window.loopResult=window.runner.runCode('while True: pass').then(()=> 'unexpected', error=>error.message)
  })
  await page.waitForTimeout(1000)
  assert.ok(await page.evaluate(()=>window.ticks) > 3)
  const timeoutMessage = await page.evaluate(()=>window.loopResult)
  assert.match(timeoutMessage, /运行超时/)
  await page.evaluate(async () => {
    clearInterval(window.tickTimer)
    await window.runner.initPyodide(null, '/__pyodide/')
    await window.runner.runCode('print(1)')
    window.runner.dispose()
  })
  console.log('Worker死循环隔离、30秒终止和重建验证通过；使用替身解释器')
} finally {
  await browser?.close()
  await server.close()
}
