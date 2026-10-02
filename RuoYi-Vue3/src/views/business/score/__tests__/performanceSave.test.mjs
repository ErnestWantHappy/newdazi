import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import vm from 'node:vm'

const source = fs.readFileSync(new URL('../components/PerformanceSection.vue', import.meta.url), 'utf8')
function setup(save) {
  const events = [], errors = []
  const context = vm.createContext({
    ref: value => ({ value }), computed: fn => fn, watch() {}, onMounted() {}, onUnmounted() {},
    nextTick: fn => Promise.resolve().then(fn), defineProps: () => ({ lessonId: 1, classCode: '1', entryYear: '2026' }),
    defineEmits: () => (...args) => events.push(args), batchSavePerformance: save,
    getPerformanceList: async () => ({ data: [] }),
    ElMessage: { error: msg => errors.push(msg), info() {}, success() {} },
    console, setTimeout, clearTimeout
  })
  vm.runInContext(source.match(/<script setup>([\s\S]*?)<\/script>/)[1].replace(/^import .*$/gm, ''), context)
  vm.runInContext('tableData.value = [{ studentId: 8, score: 0, reason: "" }]; originalData.value = {8: {score: 0, reason: ""}}', context)
  return { context, events, errors, run: code => vm.runInContext(code, context) }
}

test('请求期间继续加分：先确认发出的值，再保存最新值，名单对象不变', async () => {
  const requests = [], releases = []
  const s = setup(payload => { requests.push(payload); return new Promise(resolve => releases.push(resolve)) })
  const rows = s.run('tableData.value')
  s.run('tableData.value[0].score = 1')
  const pending = s.run('saveSingleRow(tableData.value[0])')
  s.run('tableData.value[0].score = 3; handleDataChange(tableData.value[0])')
  releases.shift()()
  await new Promise(resolve => setImmediate(resolve))
  assert.equal(requests.length, 2)
  assert.equal(requests[0].performances[0].score, 1)
  assert.equal(requests[1].performances[0].score, 3)
  releases.shift()()
  await pending
  assert.equal(s.run('originalData.value[8].score'), 3)
  assert.equal(s.run('tableData.value[0].changed'), false)
  assert.equal(s.run('tableData.value'), rows)
  assert.equal(s.events.length, 2)
})

test('重复保存合并在途请求，失败保留脏状态供重试', async () => {
  let calls = 0, reject
  const s = setup(() => { calls++; return new Promise((_, fail) => { reject = fail }) })
  s.run('tableData.value[0].score = 1')
  const a = s.run('saveSingleRow(tableData.value[0])')
  const b = s.run('saveSingleRow(tableData.value[0])')
  reject(new Error('网络异常'))
  await Promise.all([a, b])
  assert.equal(calls, 1)
  assert.equal(s.run('originalData.value[8].score'), 0)
  assert.equal(s.run('tableData.value[0].changed'), true)
  assert.equal(s.events.length, 0)
})

test('切换班级后旧响应不能污染新班基线', async () => {
  let resolve
  const s = setup(() => new Promise(done => { resolve = done }))
  s.run('tableData.value[0].score = 1')
  const pending = s.run('saveSingleRow(tableData.value[0])')
  await s.run('loadData()')
  resolve()
  await pending
  assert.equal(s.run('Object.keys(originalData.value).length'), 0)
  assert.equal(s.events.length, 0)
})

test('保存事件不再绑定整页查询；局部更新保留原始顺序和人工调整后的作业分', () => {
  const page = fs.readFileSync(new URL('../index.vue', import.meta.url), 'utf8')
  assert.match(page, /@saved="handlePerformanceSaved"/)
  const handler = page.slice(page.indexOf('function handlePerformanceSaved('), page.indexOf('\nfunction handleQuery()'))
  const rows = [{ studentId: 8, scores: [{ lessonId: 1, totalScore: 96, finalScore: 96 }] }, { studentId: 2 }]
  const context = vm.createContext({ selectedLessonIds: {value: [1]}, rawData: {value: rows}, tableData: {value: rows}, clampClientScore: n => Math.max(0, Math.min(100, n)) })
  vm.runInContext(handler + '; handlePerformanceSaved({lessonId:1,performances:[{studentId:8,score:6}]})', context)
  assert.equal(rows[0].finalTotal, 100)
  assert.deepEqual(rows.map(r => r.studentId), [8, 2])
})
