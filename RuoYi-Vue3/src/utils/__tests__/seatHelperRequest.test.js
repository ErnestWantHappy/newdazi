import test from 'node:test'
import assert from 'node:assert/strict'
import vm from 'node:vm'
import { readFileSync } from 'node:fs'

// 验证真实拦截器：助手的静默选项不能改变普通业务提示和登录过期处理。
const file = readFileSync(new URL('../request.js', import.meta.url), 'utf8')
const source = file.slice(file.indexOf('const service = axios.create'), file.indexOf('export { isRelogin }')).replace('import.meta.env.VITE_APP_BASE_API', "'/api'")
function harness() {
  const state = { messages: 0, expired: 0 }
  let success, failure, prepare
  const context = vm.createContext({
    axios: { create: () => ({ interceptors: { request: { use: f => { prepare = f } }, response: { use: (s, f) => { success = s; failure = f } } } }), isCancel: () => false },
    ElMessage: () => state.messages++, ElNotification: { error: () => state.messages++ },
    errorCode: { default: '失败' }, isSessionExpiredCode: code => code === 401,
    isSessionExpiredError: e => e.response?.status === 401, handleSessionExpired: () => state.expired++,
    refreshAuthorizationHeader: headers => ({ ...headers, Authorization: 'Bearer current' }),
    console: { log() {}, warn() {} },
    cache: { session: { getJSON() {}, setJSON() {} } }
  })
  vm.runInContext(source, context)
  return { state, success, failure, prepare }
}
test('静默认领抑制业务错误弹窗，普通业务仍提示', async () => {
  for (const code of [500, 601, 403]) {
    const h = harness()
    await assert.rejects(h.success({ request: {}, config: { silentError: true }, data: { code, msg: '失败' } }))
    assert.equal(h.state.messages, 0)
    await assert.rejects(h.success({ request: {}, config: {}, data: { code, msg: '失败' } }))
    assert.equal(h.state.messages, 1)
  }
})
test('助手网络失败只返回错误，普通业务仍提示接口异常', async () => {
  const h = harness()
  await assert.rejects(h.failure({ config: { silentError: true }, message: 'Network Error' }))
  assert.equal(h.state.messages, 0)
  await assert.rejects(h.failure({ config: {}, message: 'Network Error' }))
  assert.equal(h.state.messages, 1)
})
test('静默选项不绕过真正的登录过期处理', async () => {
  const h = harness()
  await assert.rejects(h.success({ request: {}, config: { silentError: true }, data: { code: 401 } }))
  await assert.rejects(h.failure({ config: { silentError: true }, response: { status: 401 } }))
  assert.equal(h.state.expired, 2)
})
test('换账号的迟到请求保留捕获时的身份，不串用新令牌', () => {
  const h = harness()
  const config = h.prepare({ method: 'get', headers: { isToken: false, Authorization: 'Bearer previous' } })
  assert.equal(config.headers.Authorization, 'Bearer previous')
  assert.equal(h.prepare({ method: 'get', headers: {} }).headers.Authorization, 'Bearer current')
})
