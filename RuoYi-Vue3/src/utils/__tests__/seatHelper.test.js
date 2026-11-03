import test from 'node:test'
import assert from 'node:assert/strict'
import { captureSeatHelperToken, takeSeatHelperToken, clearSeatHelperToken, displaySeatName, classroomIp, probeLocalSeatHelper } from '../seatHelper.js'

test('教师只显示助手内网地址，不能用NAT出口冒充机位', () => {
  const student = { connectionIp: '10.52.30.246' }
  assert.deepEqual(classroomIp(student, { localIp: '192.168.16.143' }), { value: '192.168.16.143', source: '内网 IP' })
  assert.deepEqual(classroomIp(student, { localIp: null }), { value: '', source: '内网 IP' })
  assert.equal(classroomIp({}, undefined).value, '')
})

test('未运行、错误服务和旧版本不能冒充本地助手', async () => {
  assert.equal(await probeLocalSeatHelper(async () => { throw new Error('拒绝连接') }), null)
  assert.equal(await probeLocalSeatHelper(async () => ({ ok: false })), null)
  assert.equal(await probeLocalSeatHelper(async () => ({ ok: true, json: async () => ({ installed: true, version: '0.2.2' }) })), null)
})

test('800ms内结束探测且不发送网页登录凭据', async () => {
  let options
  const identity = { installed: true, version: '0.2.3', localIp: '192.168.1.101', token: 'a'.repeat(64) }
  assert.deepEqual(await probeLocalSeatHelper(async (url, config) => { assert.equal(url, 'http://127.0.0.1:28123/identity'); options = config; return { ok: true, json: async () => identity } }), identity)
  assert.equal(options.credentials, 'omit')
  assert.equal(options.cache, 'no-store')
  const started = Date.now()
  assert.equal(await probeLocalSeatHelper((_url, config) => new Promise((_resolve, reject) => config.signal.addEventListener('abort', () => reject(new Error('超时'))))), null)
  assert.ok(Date.now() - started < 1500)
})

test('隐藏自动电脑编号，同时保留教师自定义的机位名称', () => {
  assert.equal(displaySeatName('电脑-d8c78b7a26e4'), '')
  assert.equal(displaySeatName('A03'), 'A03')
  assert.equal(displaySeatName('电脑-01'), '电脑-01')
  assert.equal(displaySeatName(null), '')
})

test('识别支持浏览器优先级的新助手，并继续兼容0.2.3', async () => {
  const identity = { installed: true, version: '0.2.4' }
  assert.deepEqual(await probeLocalSeatHelper(async () => ({ ok: true, json: async () => identity })), identity)
})

test('单次凭证从路由片段暂存，清除地址且读取后销毁', () => {
  const values = new Map()
  globalThis.sessionStorage = { setItem: (key, value) => values.set(key, value), getItem: key => values.get(key), removeItem: key => values.delete(key) }
  let cleaned
  globalThis.window = { location: { hash: '', pathname: '/student/index', search: '?lessonId=1' }, history: { state: {}, replaceState: (_state, _title, url) => { cleaned = url } } }
  const token = 'a'.repeat(64)
  captureSeatHelperToken(`#seat-helper=${token}`)
  assert.equal(cleaned, '/student/index?lessonId=1')
  assert.equal(takeSeatHelperToken(), token)
  assert.equal(takeSeatHelperToken(), null)
  values.set('seat-helper-pending', JSON.stringify({ token, at: Date.now() - 180000 }))
  assert.equal(takeSeatHelperToken(), token)
  values.set('seat-helper-pending', JSON.stringify({ token, at: Date.now() - 300001 }))
  assert.equal(takeSeatHelperToken(), null)
  captureSeatHelperToken('#seat-helper=invalid')
  assert.equal(values.size, 0)
  captureSeatHelperToken(`#seat-helper=${token}`)
  clearSeatHelperToken()
  assert.equal(takeSeatHelperToken(), null)
  delete globalThis.window
  delete globalThis.sessionStorage
})

