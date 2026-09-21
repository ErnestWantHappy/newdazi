import test from 'node:test'
import assert from 'node:assert/strict'
import vm from 'node:vm'
import { readFileSync } from 'node:fs'

const source = readFileSync(new URL('../session.js', import.meta.url), 'utf8')
  .split(/\r?\n/).filter(line => !line.trimStart().startsWith('import ')).join('\n')
  .replaceAll('export ', '').replace('import.meta.env.VITE_APP_BASE_API', "'/api'")
function harness(get) {
  const state = { token: 'session-a', dialogs: 0, warnings: 0, calls: 0, now: 1000000 }
  const context = vm.createContext({
    axios: { create: () => ({ get: (...args) => { state.calls++; return get(...args) } }) },
    getToken: () => state.token,
    ElMessageBox: { confirm: () => { state.dialogs++; return Promise.reject('cancel') } },
    ElNotification: { warning: () => { state.warnings++ } },
    Date: { now: () => state.now }, document: { hidden: false }
  })
  vm.runInContext(source, context)
  return { context, state }
}
test('普通错误文字包含401或认证失败不能认定会话过期', () => {
  const { context: c } = harness(async () => ({}))
  assert.equal(c.isSessionExpiredError(new Error('题目401保存失败，认证失败')), false)
  assert.equal(c.isSessionExpiredError({ response: { status: 401 } }), true)
  assert.equal(c.isSessionExpiredError({ code: 401 }), true)
})
test('暂时故障非模态提示且保留令牌', async () => {
  const { context: c, state } = harness(async () => { throw { response: { status: 503 } } })
  await c.handleSessionExpired()
  state.now += 6000
  await c.handleSessionExpired()
  assert.equal(state.dialogs, 0); assert.equal(state.warnings, 1)
  assert.equal(state.token, 'session-a')
})
test('并发401只复核一次，取消真实过期提示后不重复弹', async () => {
  let resolve
  const { context: c, state } = harness(() => new Promise(r => { resolve = r }))
  const pending = c.handleSessionExpired()
  c.handleSessionExpired(); c.handleSessionExpired()
  assert.equal(state.calls, 1)
  resolve({ data: { code: 401 } }); await pending
  state.now += 60000
  await c.handleSessionExpired()
  assert.equal(state.dialogs, 1); assert.equal(state.calls, 1)
})
test('复核成功不会弹窗，旧令牌的迟到401不能影响新登录', async () => {
  const success = harness(async () => ({ data: { code: 200 } }))
  await success.context.handleSessionExpired()
  assert.equal(success.state.dialogs, 0)
  let resolve
  const { context: c, state } = harness(() => new Promise(r => { resolve = r }))
  const pending = c.handleSessionExpired()
  state.token = 'session-b'; resolve({ data: { code: 401 } }); await pending
  assert.equal(state.dialogs, 0)
})
test('心跳恢复后后续真实过期仍能提示', async () => {
  let code = 401
  const { context: c, state } = harness(async () => ({ data: { code } }))
  await c.handleSessionExpired(); await Promise.resolve()
  code = 200; await c.runSessionHeartbeat()
  code = 401; state.now += 60000
  await c.handleSessionExpired()
  assert.equal(state.dialogs, 2)
})
