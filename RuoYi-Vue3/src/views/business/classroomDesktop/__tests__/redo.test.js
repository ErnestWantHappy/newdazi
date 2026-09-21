import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import vm from 'node:vm'
import { indexRedoStudents, hasRedoAnswers, redoContextKey } from '../../score/theoryRedo.js'

const source = readFileSync(new URL('../index.vue', import.meta.url), 'utf8')
function harness(overrides = {}) {
  const requests = [], notices = []
  const c = vm.createContext({
    lessonId: '239', entryYear: '2025', classCode: '1', lessonTitle: '测试课',
    route: { query: { lessonId: '239', entryYear: '2025', classCode: '1' } },
    historical: { value: false }, classRedoSaving: { value: false },
    students: { value: [{ studentId: 10608 }, { studentId: 10609 }] },
    indexRedoStudents, hasRedoAnswers, redoContextKey,
    getTheoryRedoContext: async () => ({ data: { current: true, students: [
      { studentId: 10608, answerCount: 1, revision: 7, typingAnswerCount: 0, typingRevision: 2 },
      { studentId: 10609, answerCount: 0, revision: 0, typingAnswerCount: 1, typingRevision: 3 }
    ] } }),
    ElMessageBox: { confirm: async () => {} },
    ElMessage: Object.fromEntries(['success', 'info', 'error', 'warning'].map(k => [k, m => notices.push(m)])),
    resetTheory: async request => { requests.push(request); return { changedCount: 1 } },
    resetTyping: async request => { requests.push(request); return { changedCount: 1 } },
    load: async () => {}, ...overrides
  })
  vm.runInContext(source.slice(source.indexOf('function currentRedoKey()'), source.indexOf('\nfunction loadDesktop')), c)
  return { c, requests, notices }
}

test('大屏按真实ID和题型筛选，使用各自轮次', async () => {
  const { c, requests } = harness()
  await c.handleClassRedoCommand('theory')
  await c.handleClassRedoCommand('typing')
  assert.equal(JSON.stringify(requests[0].studentRevisions), '{"10608":7}')
  assert.equal(JSON.stringify(requests[1].studentRevisions), '{"10609":3}')
})

test('单人重做支持数组响应且不会误取另一人的轮次', async () => {
  const { c, requests } = harness()
  await c.handleSingleStudentRedo({ studentId: 10608, studentName: '测试学生' }, 'theory')
  assert.equal(requests.length, 1)
  assert.equal(requests[0].studentRevisions[10608], 7)
})

test('零条变更不能显示为全班成功', async () => {
  const { c, notices } = harness({ resetTheory: async () => ({ changedCount: 0 }) })
  await c.handleClassRedoCommand('theory')
  assert.deepEqual(notices, ['没有可重置的答卷，已刷新课堂'])
})

test('确认期间切班不得发起重置，连点不得重复确认', async () => {
  let confirm, calls = 0
  const { c, requests } = harness({ ElMessageBox: { confirm: () => { calls++; return new Promise(r => { confirm = r }) } } })
  const pending = c.handleClassRedoCommand('theory')
  await new Promise(r => setImmediate(r))
  await c.handleClassRedoCommand('theory')
  assert.equal(calls, 1)
  c.route.query.classCode = '2'
  confirm()
  await pending
  assert.equal(requests.length, 0)
  assert.equal(c.classRedoSaving.value, false)
})

test('非当前课程与无相应答卷均不提交', async () => {
  const { c, requests } = harness({ getTheoryRedoContext: async () => ({ data: { current: false, students: [] } }) })
  await c.handleClassRedoCommand('theory')
  assert.equal(requests.length, 0)
  const second = harness()
  await second.c.handleSingleStudentRedo({ studentId: 10608 }, 'typing')
  assert.equal(second.requests.length, 0)
})
