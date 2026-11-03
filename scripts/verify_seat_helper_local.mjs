import { createRequire } from 'node:module'
import { pathToFileURL } from 'node:url'
import path from 'node:path'
import fs from 'node:fs/promises'
import assert from 'node:assert/strict'

// 挂载真实Vue组件，所有课堂写接口均模拟，不改真实学生或基准。
const root = path.resolve('RuoYi-Vue3'), require = createRequire(path.join(root, 'package.json'))
const { createServer } = await import(pathToFileURL(require.resolve('vite')))
const vue = (await import(pathToFileURL(require.resolve('@vitejs/plugin-vue')))).default
const { chromium } = require('playwright')
const output = path.resolve('output/playwright/seat-local-20261016')
await fs.mkdir(output, { recursive: true })
const modules = {
  'seat-user': `import { reactive } from 'vue'; export const user=reactive({id:1,token:'session-a'}); export default ()=>user;`,
  'seat-request': `export default config=>window.harnessRequest(config);`,
  'seat-download': `export default {zip(){throw Error('验收不下载真实程序')}};`,
  'seat-entry': `
    import {createApp,ref} from 'vue'; import ElementPlus from 'element-plus'; import 'element-plus/dist/index.css';
    import {user} from 'seat-user';
    import {provideStudentSeatSession} from '/src/components/SeatHelper/studentSession.js';
    import Student from '/src/components/SeatHelper/StudentSeatHelper.vue';
    import Panel from '/src/components/SeatHelper/ClassroomSeatPanel.vue';
    import {classroomIp,seatStateLabels} from '/src/utils/seatHelper.js';
    window.calls=[];window.installed=false;window.failClaim=false;window.browserDeferred=false;window.deferred=[];window.delayClaims=false;window.schoolEnabled=true;
    window.savedRoom='';window.rows=[{studentId:1,deviceId:'d',localIp:'192.168.16.101',state:'UNBOUND'},{studentId:2,deviceId:'e',localIp:'192.168.16.102',state:'UNBOUND'},{studentId:3,state:'UNIDENTIFIED'}];
    let claimed='';window.claims=[];
    window.harnessRequest=async c=>{
      window.calls.push(JSON.parse(JSON.stringify(c)));
      if(c.url.endsWith('claim-local')){
        if(window.browserDeferred)return {data:{enabled:true,associated:false,browserDeferred:true,preferredBrowser:true}};
        if(window.failClaim)throw Error('暂不可用');
        if(window.delayClaims)await new Promise(resolve=>window.deferred.push(resolve));
        claimed=c.headers.Authorization;window.claims.push(claimed);return {data:{enabled:true,associated:true,online:true,localIp:'192.168.16.101',seatName:'101机'}};
      }
      if(c.url.endsWith('student/status'))return {data:{enabled:window.schoolEnabled,associated:claimed===c.headers.Authorization,online:true,localIp:'192.168.16.101',seatName:'101机'}};
      if(c.url.endsWith('classroom/confirm')){
        window.savedRoom=c.data.roomName;return {data:{confirmed:c.data.students.length}};
      }
      return {data:{enabled:true,roomName:c.params?.roomName||window.savedRoom,rooms:window.savedRoom?[window.savedRoom]:[],rows:structuredClone(window.rows)}};
    };
    const nativeFetch=window.fetch.bind(window);
    window.fetch=(url,options)=>url==='http://127.0.0.1:28123/identity' ? window.installed ? Promise.resolve({ok:true,json:async()=>({installed:true,version:'0.2.4',token:'a'.repeat(64),localIp:'192.168.16.101'})}) : Promise.reject(Error('助手未运行')) : nativeFetch(url,options);
    const app=createApp({components:{Student,Panel},setup(){provideStudentSeatSession();const panel=ref(),rows=ref([]),scope=ref({lesson:10,clazz:'1'});window.changeUser=(id,token)=>{user.id=id;user.token=token};window.updateStudent=id=>panel.value.confirmStudent(id);window.changeClass=()=>{scope.value={lesson:11,clazz:'2'}};return {panel,rows,scope,classroomIp,seatStateLabels};},template:
      ${JSON.stringify('<main style="padding:30px"><Student/><hr/><Panel ref="panel" :lesson-id="scope.lesson" entry-year="2026" :class-code="scope.clazz" @updated="rows=$event"/><div v-for="r in rows" :key="r.studentId" class="card" style="margin:16px">{{r.studentId}} · {{classroomIp({connectionIp: &quot;10.52.30.250&quot;},r).value || &quot;未连助手&quot;}} · {{seatStateLabels[r.state]}}</div></main>')}
    });
    app.use(ElementPlus);app.mount('#app');
  `
}
await fs.writeFile(path.join(output, 'harness-source.mjs'), modules['seat-entry'])
const server = await createServer({
  cacheDir: path.join(output, '.vite-cache'),
  configFile: false, root, logLevel: 'error', plugins: [vue(), {
    name: 'seat-test-harness', resolveId(id) { if (modules[id]) return '\0' + id },
    load(id) { return modules[id.slice(1)] },
    configureServer(s) { s.middlewares.use('/seat-test', async (_req, res) => {
      res.setHeader('Content-Type', 'text/html'); res.end(await s.transformIndexHtml('/seat-test', '<html><div id="app"></div><script type="module" src="/@id/seat-entry"></script></html>'))
    }) }
  }], resolve: { alias: [{ find: /^vue$/, replacement: require.resolve('vue/dist/vue.esm-bundler.js') }, { find: '@/utils/request', replacement: 'seat-request' }, { find: '@/plugins/download', replacement: 'seat-download' }, { find: '@/store/modules/user', replacement: 'seat-user' }, { find: '@', replacement: path.join(root, 'src') }] },
  optimizeDeps: { entries: [], include: ['vue', 'element-plus'] },
  server: { host: '127.0.0.1', port: 18123, strictPort: true }
})
const results = [], errors = []
let browser
try {
  await server.listen(); browser = await chromium.launch({ headless: true })
  const page = await browser.newPage({ viewport: { width: 1366, height: 768 } })
  page.on('pageerror', e => { errors.push(e.message); console.error(e.message) })
  page.on('console', m => { if(m.type()==='error') console.error(m.text()) })
  await page.goto('http://127.0.0.1:18123/seat-test')
  await page.getByText('下载机位助手', { exact: true }).waitFor()
  assert.match(await page.locator('.card').last().innerText(), /未连助手/)
  assert.ok(!(await page.locator('main').innerText()).includes('10.52.30.250'))
  results.push('未运行时保留下载按钮，未识别卡片不显示网关IP')
  await page.screenshot({ path: path.join(output, 'uninstalled.png') })
  await page.evaluate(() => { window.installed = true })
  await page.locator('.seat-status-pill.is-online').waitFor({ timeout: 6000 })
  assert.equal(await page.getByText('下载机位助手', { exact: true }).count(), 0)
  assert.match(await page.locator('.seat-status-pill').innerText(), /192.168.16.101/)
  results.push('运行后无需刷新自动变绿并隐藏下载按钮')
  await page.evaluate(() => window.changeUser(2, 'session-b'))
  await page.waitForFunction(() => window.claims.includes('Bearer session-b'))
  await page.locator('.seat-status-pill.is-online').waitFor()
  assert.equal(await page.getByText('下载机位助手', { exact: true }).count(), 0)
  results.push('同一电脑切换学生静默重新认领')
  const input = page.locator('.class-seat-panel input')
  await input.fill('创新机房1'); await input.press('Enter')
  const save = page.getByRole('button', { name: /一键保存为常用机位/ })
  await page.waitForFunction(() => !!document.querySelector('.class-seat-panel .el-button--primary:not(.is-disabled)'))
  await save.click(); await page.waitForFunction(() => window.savedRoom === '创新机房1')
  const confirmed = await page.evaluate(() => window.calls.find(c => c.url.endsWith('classroom/confirm')))
  assert.equal(confirmed.data.students.length, 2); assert.equal(confirmed.data.roomName, '创新机房1')
  assert.equal(await page.getByText('机位明细', { exact: true }).count(), 0)
  results.push('输入机房后一键保存已识别学生，排除未连助手者')
  await page.evaluate(() => { window.rows[0].state = 'IP_MISMATCH'; window.rows[0].localIp = '192.168.16.115' })
  await page.getByText(/1 · 192.168.16.115 · 异机登录/).waitFor({ timeout: 7000 })
  await page.evaluate(() => window.updateStudent(1))
  const single = await page.evaluate(() => window.calls.filter(c => c.url.endsWith('classroom/confirm')).at(-1))
  assert.equal(single.data.students.length, 1); assert.equal(single.data.students[0].localIp, '192.168.16.115')
  results.push('异机IP显示告警，单人更新只提交该学生')
  await page.screenshot({ path: path.join(output, 'associated-alert.png') })
  await page.evaluate(() => window.changeClass()); await page.waitForFunction(() => window.calls.some(c => c.params?.lessonId === 11))
  await page.waitForFunction(() => document.querySelector('.class-seat-panel .el-select')?.textContent?.includes('创新机房1'))
  results.push('切换课堂后由后端恢复班级常用机房')
  await page.evaluate(() => { window.delayClaims = true; window.changeUser(3, 'session-c') })
  await page.waitForFunction(() => window.deferred.length === 1)
  await page.evaluate(() => window.changeUser(4, 'session-d'))
  await page.waitForFunction(() => window.deferred.length === 2)
  await page.evaluate(() => { window.delayClaims = false; window.deferred.shift()(); window.deferred.shift()() })
  await page.locator('.seat-status-pill.is-online').waitFor()
  const oldStatus = await page.evaluate(() => window.calls.filter(c => c.url.endsWith('student/status') && c.headers.Authorization === 'Bearer session-c'))
  assert.equal(oldStatus.length, 1)
  results.push('身份切换后旧异步认领结果不续期或污染新账号')
  await page.waitForFunction(() => document.querySelectorAll('.el-message').length === 0)
  await page.evaluate(() => { window.failClaim = true; window.changeUser(5, 'session-e') })
  await page.getByText('机位信息暂不可用，答题可以继续。', { exact: true }).waitFor()
  assert.equal(await page.locator('.el-message').count(), 0)
  results.push('后台认领失败保留内联提示，不弹业务窗口')
  const claimsBeforeDisabled = await page.evaluate(() => window.calls.filter(c => c.url.endsWith('claim-local')).length)
  await page.evaluate(() => { window.schoolEnabled = false; window.changeUser(6, 'session-f') })
  await page.waitForFunction(() => window.calls.some(c => c.headers?.Authorization === 'Bearer session-f'))
  await page.waitForFunction(() => !document.querySelector('.student-seat-helper'))
  assert.equal(await page.evaluate(() => window.calls.filter(c => c.url.endsWith('claim-local')).length), claimsBeforeDisabled)
  results.push('学校关闭助手时不认领、不显示误导状态')
  await page.evaluate(() => { window.schoolEnabled = true; window.failClaim = false; window.changeUser(7, 'session-g') })
  await page.locator('.seat-status-pill.is-online').waitFor()
  await page.evaluate(() => { window.browserDeferred = true; window.changeUser(9, 'session-secondary') })
  await page.getByText('当前机位优先关联默认浏览器中的账号', { exact: true }).waitFor()
  assert.equal(await page.getByText('下载机位助手', { exact: true }).count(), 0)
  assert.equal(await page.locator('.seat-status-pill').count(), 0)
  assert.equal(await page.locator('.el-message').count(), 0)
  results.push('默认浏览器优先时其他账号只显示等待，不下载、不显示旧IP、不弹窗')
  await page.screenshot({ path: path.join(output, 'default-browser-priority.png') })
  await page.evaluate(() => { window.browserDeferred = false; window.changeUser(7, 'session-g') })
  await page.locator('.seat-status-pill.is-online').waitFor()
  await page.evaluate(() => { window.installed = false; window.changeUser(8, 'session-h') })
  await page.getByText('下载机位助手', { exact: true }).waitFor()
  assert.equal(await page.locator('.seat-status-pill').count(), 0)
  results.push('助手消失后不展示旧IP，恢复下载入口')
  assert.deepEqual(errors, [])
  await fs.writeFile(path.join(output, 'result.json'), JSON.stringify({ passed: results.length, results, pageErrors: errors, realBusinessWrites: 0 }, null, 2))
  console.log(JSON.stringify({ passed: results.length, results, pageErrors: errors }))
} finally { await browser?.close(); await server.close() }
