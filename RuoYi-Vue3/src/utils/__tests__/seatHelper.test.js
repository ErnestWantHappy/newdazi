import test from 'node:test'
import assert from 'node:assert/strict'
import { captureSeatHelperToken, takeSeatHelperToken, clearSeatHelperToken, displaySeatName, classroomIp } from '../seatHelper.js'

test('教师显示助手内网地址，未获取时明确降级为连接地址', () => {
  const student = { connectionIp: '10.52.30.246' }
  assert.deepEqual(classroomIp(student, { localIp: '192.168.16.143' }), { value: '192.168.16.143', source: '内网 IP' })
  assert.deepEqual(classroomIp(student, { localIp: null }), { value: '10.52.30.246', source: '连接 IP' })
  assert.equal(classroomIp({}, undefined).value, '')
})

test('隐藏自动电脑编号，同时保留教师自定义的机位名称', () => {
  assert.equal(displaySeatName('电脑-d8c78b7a26e4'), '')
  assert.equal(displaySeatName('A03'), 'A03')
  assert.equal(displaySeatName('电脑-01'), '电脑-01')
  assert.equal(displaySeatName(null), '')
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

