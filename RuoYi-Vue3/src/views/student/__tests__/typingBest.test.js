import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import vm from 'node:vm'

const source = readFileSync(new URL('../index.vue', import.meta.url), 'utf8')
function harness() {
  let resolveSubmit
  const c = vm.createContext({
    typingQuestions: { value: [{ questionId: 1, typingDuration: 5, questionContent: '测试原文' }] },
    allQuestions: { value: [{ questionId: 1, typingDuration: 5, questionContent: '测试原文' }] },
    typingStates: { value: {} }, answers: { value: {} }, timerIntervals: {},
    submittedAnswers: { value: { 1: { score: 80, answer: '测试', typingSpeed: 24, accuracyRate: 98, completionRate: 50 } } },
    lessonId: { value: 10 }, typingRevision: { value: 2 },
    markQuestionWorking() {}, clearInterval() {}, setInterval: () => 1,
    nextTick: () => {}, ElMessage: { success() {}, error() {} },
    submitAnswersApi: () => new Promise(resolve => { resolveSubmit = resolve })
  })
  vm.runInContext(source.slice(source.indexOf('function initTypingStates()'), source.indexOf('// 手动点击提交')), c)
  // 运行真实聚合表达式，重打过程中最高分仍应计入课程。
  const total = source.slice(source.indexOf('const typingMyScore = computed('), source.indexOf('// 上传配置'))
  c.computed = fn => ({ get value() { return fn() } })
  vm.runInContext(total + '\nglobalThis.total = typingMyScore', c)
  c.initTypingStates()
  return { c, finish: result => resolveSubmit(result) }
}

test('自主重打期间继续保留最高分，刷新恢复最高分对应统计', () => {
  const { c } = harness()
  assert.equal(c.total.value, 80)
  assert.equal(c.typingStates.value[1].speed, 24)
  c.restartTyping(1)
  assert.equal(c.typingStates.value[1].submitted, false)
  assert.equal(c.total.value, 80)
  assert.equal(c.typingStates.value[1].bestSpeed, 24)
})

test('重打低分采用服务端最高记录，多题响应不把总分当单题分', async () => {
  const { c, finish } = harness()
  c.restartTyping(1)
  c.autoSubmitTyping(1)
  finish({ totalScore: 150, typingResults: { 1: { score: 80, answer: '测试', typingSpeed: 24, accuracyRate: 98, completionRate: 50 } } })
  await new Promise(resolve => setImmediate(resolve))
  assert.equal(c.total.value, 80)
  assert.equal(c.typingStates.value[1].bestSpeed, 24)
  c.initTypingStates()
  assert.equal(c.typingStates.value[1].speed, 24)
})

test('教师新轮清分后旧提交响应不能恢复最高分', async () => {
  const { c, finish } = harness()
  c.restartTyping(1)
  c.autoSubmitTyping(1)
  c.typingRevision.value = 3
  c.typingStates.value = {}
  c.submittedAnswers.value = {}
  c.initTypingStates()
  finish({ totalScore: 80 })
  await new Promise(resolve => setImmediate(resolve))
  assert.equal(c.total.value, null)
})
