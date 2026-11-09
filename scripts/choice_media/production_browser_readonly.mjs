import { createRequire } from 'node:module'
import fs from 'node:fs/promises'
import path from 'node:path'
import assert from 'node:assert/strict'

const root = path.resolve('.')
const require = createRequire(path.join(root, 'RuoYi-Vue3/package.json'))
const { chromium } = require('playwright')
const base = 'http://10.52.1.130'
const out = path.join(root, 'output/choice-media')
const { token } = JSON.parse(await fs.readFile(path.join(root, 'output/playwright/teacher-tools-teacher-state.json'), 'utf8'))
const browser = await chromium.launch({ headless: true })
const context = await browser.newContext({ viewport: { width: 1440, height: 1000 } })
const result = { checkedAt: new Date().toISOString(), productionWrite: false, blockedMutations: [], errors: [] }
try {
  const headers = { Authorization: 'Bearer ' + token }
  const identity = await context.request.get(base + '/prod-api/getInfo', { headers })
  assert.equal((await identity.json()).code, 200, '正式教师登录态无效，停止只读浏览器检查')
  const routers = await context.request.get(base + '/prod-api/getRouters', { headers })
  const tree = (await routers.json()).data
  function find(rows, parent = '') {
    for (const row of rows || []) {
      const full = row.path.startsWith('/') ? row.path : parent + '/' + row.path
      if (row.component === 'business/question/index') return full.replace(/\/+/g, '/')
      const found = find(row.children, full)
      if (found) return found
    }
  }
  const route = find(tree)
  assert.ok(route, '未找到教师题库路由')
  await context.addCookies([{ name: 'Admin-Token', value: token, url: base }])
  await context.route('**/prod-api/**', async requestRoute => {
    const request = requestRoute.request()
    if (!['GET', 'HEAD', 'OPTIONS'].includes(request.method())) {
      result.blockedMutations.push({ method: request.method(), path: new URL(request.url()).pathname })
      await requestRoute.abort()
      return
    }
    await requestRoute.continue()
  })
  const page = await context.newPage()
  page.on('pageerror', error => result.errors.push(error.message))
  await page.goto(base + route)
  await page.getByRole('button', { name: '导入导出', exact: true }).waitFor()
  result.buttons = await page.getByRole('button').allTextContents()
  const choiceSection = page.locator('.qw-section').filter({ hasText: '选择题' })
  let add = choiceSection.getByRole('button', { name: /新增/ }).first()
  if (!(await add.count())) add = page.getByRole('button', { name: /新增选择题/ }).first()
  if (!(await add.count())) {
    const section = page.locator('section').filter({ has: page.getByRole('heading', { name: '选择题', exact: true }) })
    add = section.getByRole('button', { name: /新增/ }).first()
  }
  assert.ok(await add.count(), '未找到选择题新增入口')
  await add.click()
  const dialog = page.locator('.el-dialog:visible').last()
  await dialog.waitFor()
  result.mediaInputsVisible = await dialog.locator('.media-input').count()
  result.stemLabelVisible = await dialog.getByText('题干图片', { exact: true }).count()
  await dialog.screenshot({ path: path.join(out, 'production-choice-entry-readonly.png') })
  await dialog.locator('.el-dialog__headerbtn').click()
  await page.getByRole('button', { name: '导入导出', exact: true }).click()
  await page.getByRole('menuitem', { name: '导入常规题（Excel）' }).click()
  result.imageImportDialogVisible = await page.getByRole('dialog', { name: 'Excel 图文题库导入' }).isVisible()
  result.route = route
  assert.deepEqual(result.blockedMutations, [], '页面检查意外触发写请求，已拦截')
  await fs.writeFile(path.join(out, 'production-browser-readonly-20261108.json'), JSON.stringify(result, null, 2))
  console.log(JSON.stringify(result))
} finally {
  await browser.close()
}
