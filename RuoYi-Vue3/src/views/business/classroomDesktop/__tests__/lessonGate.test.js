import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import vm from 'node:vm'

const source = readFileSync(fileURLToPath(new URL('../index.vue', import.meta.url)), 'utf8')
const start = source.indexOf('let gateRequestVersion = 0')
const end = source.indexOf('\nfunction loadDesktop', start)

function harness(getLessonGate, setLessonGate = async () => {}) {
  const context = vm.createContext({
    lessonId: 574, entryYear: '2022', classCode: '3',
    historical: { value: false }, gateSaving: { value: '' },
    gateTheoryOpen: { value: false }, gatePracticalOpen: { value: false },
    getLessonGate, setLessonGate,
    ElMessage: { success() {}, error() {}, warning() {} }
  })
  vm.runInContext(source.slice(start, end), context)
  return context
}

test('顶层接口状态经过多次轮询仍保持开启，也能同步真实关闭', async () => {
  let state = { theoryOpen: true, practicalOpen: true }
  const c = harness(async () => ({ code: 200, ...state }))
  for (let i = 0; i < 3; i++) await c.fetchLessonGateState()
  assert.equal(c.gateTheoryOpen.value, true)
  assert.equal(c.gatePracticalOpen.value, true)
  state = { theoryOpen: false, practicalOpen: false }
  await c.fetchLessonGateState()
  assert.equal(c.gatePracticalOpen.value, false)
})

test('兼容 data 包装的状态响应', async () => {
  const c = harness(async () => ({ data: { theoryOpen: true, practicalOpen: false } }))
  await c.fetchLessonGateState()
  assert.equal(c.gateTheoryOpen.value, true)
  assert.equal(c.gatePracticalOpen.value, false)
})

test('保存前发出的延迟轮询不能撤销刚开启的开关', async () => {
  let resolveRead
  const c = harness(() => new Promise(resolve => { resolveRead = resolve }))
  const read = c.fetchLessonGateState()
  c.gatePracticalOpen.value = true
  await c.handleGateToggle('practical', true)
  resolveRead({ practicalOpen: false })
  await read
  assert.equal(c.gatePracticalOpen.value, true)
})

test('保存进行中不发轮询，保存失败恢复原状态', async () => {
  let calls = 0
  const c = harness(async () => { calls++; return {} }, async () => { throw Error('failed') })
  c.gateSaving.value = 'theory'
  await c.fetchLessonGateState()
  assert.equal(calls, 0)
  c.gateSaving.value = ''
  c.gateTheoryOpen.value = true
  await c.handleGateToggle('theory', true)
  assert.equal(c.gateTheoryOpen.value, false)
})
