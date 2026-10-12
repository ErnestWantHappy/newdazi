import { test } from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import vm from 'node:vm'

const page = fs.readFileSync(new URL('../pythonPractice.vue', import.meta.url), 'utf8')
const functions = page.slice(page.indexOf('async function saveDraft('), page.indexOf('async function goRelative('))
const selectFunction = page.slice(page.indexOf('async function selectQuestion('), page.indexOf('function onDataLabSubmitted('))
const deferred = () => {
  let resolve
  const promise = new Promise(done => { resolve = done })
  return { promise, resolve }
}
const ref = value => ({ value })
const tick = () => new Promise(resolve => setImmediate(resolve))

function setup() {
  const saves = []
  const submits = []
  const reads = []
  const timers = []
  const item = { source_type: 'PLAN', source_id: 1, question_id: 1 }
  const ctx = {
    current: ref(item), activeKey: ref('PLAN-1-1'), sourceCode: ref('first'), customInput: ref(''),
    saving: ref(false), running: ref(false), customRunning: ref(false), submitting: ref(false), questionLoading: ref(false),
    draftState: ref('草稿待保存'), isJudging: ref(false), history: ref([]), selectedHistory: ref(null),
    consoleTab: ref('custom'), questions: ref([]), keyOf: row => `${row.source_type}-${row.source_id}-${row.question_id}`,
    normalizeSubmission: row => row, ElMessage: { error() {}, info() {}, warning() {} },
    saveStudentPracticeDraft: payload => { const request = deferred(); saves.push({ payload, ...request }); return request.promise },
    submitStudentPractice: payload => { const request = deferred(); submits.push({ payload, ...request }); return request.promise },
    getStudentPracticeQuestion: payload => { const request = deferred(); reads.push({ payload, ...request }); return request.promise },
    getStudentPracticeOverview: async () => ({ data: { questions: [] } }),
    setTimeout: callback => { timers.push(callback); return timers.length }, clearTimeout() {}
  }
  vm.createContext(ctx)
  vm.runInContext('let savePromise = Promise.resolve(); let pollTimer; let pollAttempts = 0; let selectionPending = false;\n' + functions, ctx)
  return { ctx, saves, submits, reads, timers, item }
}

test('保存中的再次编辑按顺序写入，旧快照不标为已保存', async () => {
  const { ctx, saves, item } = setup()
  const first = ctx.saveDraft(item, 'first')
  ctx.sourceCode.value = 'second'
  const second = ctx.saveDraft(item, 'second')
  await tick()
  assert.equal(saves.length, 1)
  saves[0].resolve()
  await first
  assert.equal(ctx.draftState.value, '草稿待保存')
  await tick()
  assert.equal(saves[1].payload.sourceCode, 'second')
  saves[1].resolve()
  await second
  assert.equal(ctx.draftState.value, '草稿已保存')
})

test('切题前发起的运行始终提交原题，响应不写入新题', async () => {
  const { ctx, saves, submits } = setup()
  const running = ctx.runSamples()
  await tick()
  saves[0].resolve()
  await tick()
  assert.equal(submits[0].payload.questionId, 1)
  ctx.current.value = { source_type: 'PLAN', source_id: 1, question_id: 2 }
  ctx.activeKey.value = 'PLAN-1-2'
  ctx.history.value = [{ submission_id: 99 }]
  submits[0].resolve({ data: { submission_id: 1, status_code: 'WAITING' } })
  await running
  assert.equal(ctx.history.value[0].submission_id, 99)
})

test('切题后迟到的轮询结果不能覆盖新题记录', async () => {
  const { ctx, timers, reads } = setup()
  ctx.poll()
  timers[0]()
  assert.equal(reads[0].payload.questionId, 1)
  ctx.activeKey.value = 'PLAN-1-2'
  ctx.history.value = [{ submission_id: 99 }]
  reads[0].resolve({ data: { history: [{ submission_id: 1 }] } })
  await tick()
  assert.equal(ctx.history.value[0].submission_id, 99)
})

test('题目加载失败后保持原题，允许再次进入目标题', async () => {
  const { ctx } = setup()
  const target = { source_type: 'PLAN', source_id: 1, question_id: 2 }
  let attempts = 0
  ctx.draftState.value = '草稿已保存'
  ctx.questionError = ref('')
  ctx.getStudentPracticeQuestion = async () => {
    if (++attempts === 1) throw new Error('network')
    return { data: { ...target, practical_mode: 'DATA_LAB' } }
  }
  ctx.isDataLab = { value: true }
  ctx.editorView = null
  vm.runInContext('let editorView;\n' + selectFunction, ctx)
  await ctx.selectQuestion(target)
  assert.equal(ctx.activeKey.value, 'PLAN-1-1')
  assert.equal(ctx.current.value.question_id, 1)
  await ctx.selectQuestion(target)
  assert.equal(attempts, 2)
  assert.equal(ctx.activeKey.value, 'PLAN-1-2')
  assert.equal(ctx.current.value.question_id, 2)
})

test('切题前草稿保存失败时退出加载态并留在原题', async () => {
  const { ctx } = setup()
  const target = { source_type: 'PLAN', source_id: 1, question_id: 2 }
  ctx.questionError = ref('')
  ctx.getStudentPracticeQuestion = () => { throw new Error('不应加载目标题') }
  vm.runInContext('let editorView;\n' + selectFunction, ctx)
  ctx.saveDraft = async () => { throw new Error('network') }
  await ctx.selectQuestion(target)
  assert.equal(ctx.questionLoading.value, false)
  assert.equal(ctx.activeKey.value, 'PLAN-1-1')
  assert.equal(ctx.current.value.question_id, 1)
})
