import { createRequire } from 'node:module'
import { spawn } from 'node:child_process'
import { createServer } from 'node:http'
import { createHash, randomBytes } from 'node:crypto'
import fs from 'node:fs/promises'
import path from 'node:path'
import assert from 'node:assert/strict'

const root = process.cwd(), require = createRequire(path.join(root, 'RuoYi-Vue3/package.json'))
const { chromium } = require('playwright')
const output = path.join(root, 'output/seat-browser-native')
await fs.mkdir(output, { recursive: true })
const ready = path.join(output, 'ready.json'), stop = path.join(output, 'stop')
await fs.rm(ready, { force: true }); await fs.rm(stop, { force: true })
const calls = [], browsers = [], results = []
const server = createServer(async (req, res) => {
  if (req.url.endsWith('/device/open')) {
    let body = ''; for await (const chunk of req) body += chunk
    const data = JSON.parse(body); calls.push({ browserKey: data.browserKey, preferredBrowser: data.preferredBrowser })
    res.setHeader('content-type', 'application/json')
    res.end(JSON.stringify({ code: 200, data: { url: `http://127.0.0.1:18124/student/index#seat-helper=${randomBytes(32).toString('hex')}` } }))
  } else { res.setHeader('content-type', 'text/html'); res.end('<!doctype html><title>机位助手独立验收</title>') }
})
await new Promise(resolve => server.listen(18124, '127.0.0.1', resolve))
const child = spawn(path.join(root, 'output/review-repair-client-tests/SeatHelperBrowserProbe.exe'), [ready, stop], { windowsHide: true, stdio: ['ignore', 'pipe', 'pipe'] })
let childError = ''; child.stderr.on('data', data => { childError += data })
async function until(check, message) {
  const deadline = Date.now() + 15000
  while (Date.now() < deadline) { if (await check()) return; await new Promise(resolve => setTimeout(resolve, 100)) }
  throw Error(message + childError)
}
try {
  await until(async () => !!(await fs.stat(ready).catch(() => null)), '本地测试助手未启动')
  const settings = JSON.parse((await fs.readFile(ready, 'utf8')).replace(/^\uFEFF/, ''))
  for (const executablePath of ['C:/Program Files/Google/Chrome/Application/chrome.exe', 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe']) {
    const browser = await chromium.launch({ executablePath, headless: true }); browsers.push(browser)
    const page = await browser.newPage(); await page.goto('http://127.0.0.1:18124')
    const probe = () => page.evaluate(async () => {
      const response = await fetch('http://127.0.0.1:28123/identity', { cache: 'no-store', credentials: 'omit' })
      return response.json()
    })
    const identity = await probe(); assert.equal(identity.version, '0.2.4')
    const normalized = path.win32.normalize(executablePath).toLowerCase()
    const expectedKey = createHash('sha256').update(normalized).digest('hex')
    await until(async () => { const value = await probe(); return !!value.token }, '浏览器未取得自己的单次凭证')
    const observed = calls.find(call => call.browserKey === expectedKey)
    assert.ok(observed, '未准确识别浏览器程序')
    assert.equal(observed.preferredBrowser, normalized === settings.defaultBrowser.toLowerCase())
    const origins = []
    for (const origin of ['http://10.52.1.130', 'http://xxkj.xsedu.net.cn']) {
      await page.goto(origin + '/login')
      const actual = await probe().catch(error => ({ error: error.message }))
      assert.equal(new URL(page.url()).origin, origin, '入口发生跨来源跳转，不能算原域名通过')
      assert.equal(actual.version, '0.2.4', `${origin}无法探测本机助手：${actual.error || ''}`)
      origins.push({ origin, localHelperReachable: actual.version === '0.2.4', ...(actual.error ? { error: actual.error } : {}) })
    }
    results.push({ browser: path.basename(executablePath), preferred: observed.preferredBrowser, nativeProcessMatched: true, origins })
  }
  assert.equal(new Set(calls.map(call => call.browserKey)).size, 2)
  await fs.writeFile(path.join(output, 'result.json'), JSON.stringify({ passed: 2, browsers: results, realBusinessWrites: 0 }, null, 2))
  console.log(JSON.stringify({ passed: 2, browsers: results, realBusinessWrites: 0 }))
} finally {
  await fs.writeFile(stop, '')
  await Promise.allSettled(browsers.map(browser => browser.close()))
  await until(() => child.exitCode !== null, '测试助手未正常退出')
  await new Promise(resolve => server.close(resolve))
}
