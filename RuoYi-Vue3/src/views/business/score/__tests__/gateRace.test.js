import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import vm from 'node:vm'
import { ref, computed, watch } from 'vue'
import { redoContextKey } from '../theoryRedo.js'

const source = readFileSync(new URL('../index.vue', import.meta.url), 'utf8')
function harness() {
  const reads = [], writes = []
  const c = vm.createContext({
    watch, gateContext: ref(null), gateSaving: ref(''), loading: ref(false),
    selectedLessonIds: ref([239]), queryParams: ref({ entryYear: '2025', classCode: '1' }),
    getLessonGate: () => new Promise(resolve => reads.push(resolve)),
    setLessonGate: () => new Promise(resolve => writes.push(resolve)),
    proxy: { $modal: { msgSuccess() {} } }
  })
  c.redoKey = computed(() => redoContextKey(c.selectedLessonIds.value, c.queryParams.value.entryYear, c.queryParams.value.classCode))
  vm.runInContext(source.slice(source.indexOf('let gateRequestVersion = 0'), source.indexOf('// 计算属性：是否为年级概览模式')), c)
  return { c, reads, writes }
}

test('切班立即使旧读取失效，即使还没发起新查询', async () => {
  const { c, reads } = harness()
  const old = c.loadGateContext()
  c.queryParams.value.classCode = '2'
  reads[0]({ isCurrent: true, theoryOpen: false })
  await old
  assert.equal(c.gateContext.value, null)
})

test('清除课程后不能被旧响应恢复开关', async () => {
  const { c, reads } = harness()
  const old = c.loadGateContext()
  c.selectedLessonIds.value = []
  await c.loadGateContext()
  reads[0]({ isCurrent: true })
  await old
  assert.equal(c.gateContext.value, null)
})

test('旧班保存不能覆盖新班状态或清除新保存的忙碌标记', async () => {
  const { c, writes } = harness()
  c.gateContext.value = { key: c.redoKey.value, isCurrent: true }
  const first = c.toggleGate('theory', true)
  c.queryParams.value.classCode = '2'
  c.gateContext.value = { key: c.redoKey.value, isCurrent: true, theoryOpen: false }
  const second = c.toggleGate('practical', true)
  writes[0]({ theoryOpen: true, practicalOpen: false })
  await first
  assert.equal(c.gateContext.value.theoryOpen, false)
  assert.equal(c.gateSaving.value, 'practical')
  writes[1]({ theoryOpen: false, practicalOpen: true })
  await second
  assert.equal(c.gateContext.value.practicalOpen, true)
  assert.equal(c.gateSaving.value, '')
})
