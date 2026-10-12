import { test } from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import vm from 'node:vm'

const page = fs.readFileSync(new URL('../grading.vue', import.meta.url), 'utf8')
const functions = page.slice(page.indexOf('async function loadTasks('), page.indexOf('function prevTask('))
const deferred = () => {
  let resolve
  const promise = new Promise(done => { resolve = done })
  return { promise, resolve }
}
const ref = value => ({ value })
const tick = () => new Promise(resolve => setImmediate(resolve))

function setup() {
  const lists = []
  const details = []
  const grades = []
  const ctx = {
    taskRequestVersion: 0, detailRequestVersion: 0,
    loading: ref(false), detailLoading: ref(false), submitting: ref(false),
    tasks: ref([]), detail: ref(null), currentAnswerId: ref(null), scoringItems: ref([]),
    itemScores: {}, manualScore: ref(0), useItemScoring: ref(false), query: { gradingStatus: '0' },
    focusScoreInput() {}, ElMessage: { warning() {}, success() {} },
    getCountyExamGradingTasks: () => { const request = deferred(); lists.push(request); return request.promise },
    getCountyExamGradingAnswer: id => { const request = deferred(); details.push({ id, ...request }); return request.promise },
    gradeCountyExamAnswer: payload => { grades.push(payload); return Promise.resolve() }
  }
  ctx.currentIndex = { get value() { return ctx.tasks.value.findIndex(row => row.answerId === ctx.currentAnswerId.value) } }
  ctx.maxScore = { get value() { return ctx.detail.value?.questionScore || 0 } }
  ctx.totalItemScore = { value: 0 }
  vm.createContext(ctx)
  vm.runInContext('let taskRequestVersion = 0; let detailRequestVersion = 0;\n' + functions, ctx)
  return { ctx, lists, details, grades }
}

test('快速切换答卷时旧详情不能覆盖当前答卷，也不能提交旧评分', async () => {
  const { ctx, lists, details, grades } = setup()
  const first = ctx.selectTask(1)
  const second = ctx.selectTask(2)
  details[1].resolve({ data: { answerId: 2, questionScore: 10, score: 3 } })
  await second
  details[0].resolve({ data: { answerId: 1, questionScore: 10, score: 8 } })
  await first
  assert.equal(ctx.detail.value.answerId, 2)
  ctx.manualScore.value = 4
  const grading = ctx.submitGrade()
  await tick()
  assert.equal(grades[0].answerId, 2)
  lists[0].resolve({ data: [] })
  await grading
})

test('快速切换筛选时迟到的旧任务列表不会覆盖新列表', async () => {
  const { ctx, lists, details } = setup()
  const oldLoad = ctx.loadTasks(false)
  ctx.query.gradingStatus = '1'
  const newLoad = ctx.loadTasks(false)
  lists[1].resolve({ data: [{ answerId: 2, gradingStatus: '1' }] })
  await tick()
  details[0].resolve({ data: { answerId: 2, questionScore: 10 } })
  await newLoad
  lists[0].resolve({ data: [{ answerId: 1, gradingStatus: '0' }] })
  await oldLoad
  assert.equal(ctx.tasks.value[0].answerId, 2)
  assert.equal(ctx.detail.value.answerId, 2)
})
