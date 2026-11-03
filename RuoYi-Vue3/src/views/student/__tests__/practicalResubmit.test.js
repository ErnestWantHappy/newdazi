import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import vm from 'node:vm'

const source = readFileSync(new URL('../index.vue', import.meta.url), 'utf8')
function harness(score = 68) {
  const old = { answer: '/old.pdf', score, taskState: 'GRADED', artifact: { versionId: 5 } }
  const calls = [], messages = []
  const c = vm.createContext({
    practicalDrafts: { value: { 3: [{ uid: 1, uploadToken: 'new' }] } },
    getPracticalDrafts: () => c.practicalDrafts.value[3],
    submittingPracticalQuestionId: { value: null }, practicalUploadStates: { value: {} },
    practicalUploadEpoch: 1, lessonId: { value: 2 }, practicalScores: { value: { 3: score } },
    practicalArtifacts: { value: { 3: old.artifact } }, submittedAnswers: { value: { 3: old } },
    practicalFileLists: { value: { 3: [{ uid: 1 }] } }, uploadingQuestionId: { value: null },
    ElMessageBox: { confirm: async () => {} },
    ElMessage: Object.fromEntries(['success', 'error', 'warning'].map(type => [type, text => messages.push({ type, text })])),
    submitPracticalArtifact: async request => {
      calls.push(request)
      return { data: { versionId: 7, attachments: [{ resourcePath: '/new.pdf', previewStatus: 'success' }] } }
    },
    syncPracticalSubmission: (id, answer) => { c.practicalScores.value[id] = answer.score; c.practicalArtifacts.value[id] = answer.artifact },
    refreshPracticalSubmission: async () => c.submittedAnswers.value[3],
    schedulePracticalPreviewPolling() {},
  })
  vm.runInContext(source.slice(source.indexOf('async function commitPracticalArtifact('), source.indexOf('function handleUploadError(')), c)
  return { c, calls, messages }
}
test('已有非零分或零分均可重交，成功才清分并切换版本', async () => {
  for (const score of [68, 0]) {
    const { c, calls } = harness(score)
    await c.commitPracticalArtifact({ questionId: 3 })
    assert.equal(calls[0].expectedVersionId, 5)
    assert.equal(c.practicalScores.value[3], null)
    assert.equal(c.practicalArtifacts.value[3].versionId, 7)
    assert.equal(c.submittedAnswers.value[3].taskState, 'SUBMITTED')
    assert.equal(c.submittingPracticalQuestionId.value, null)
  }
})
test('取消保留旧作品和原分，不提交请求', async () => {
  const { c, calls } = harness()
  c.ElMessageBox.confirm = async () => { throw 'cancel' }
  await c.commitPracticalArtifact({ questionId: 3 })
  assert.equal(calls.length, 0)
  assert.equal(c.practicalScores.value[3], 68)
  assert.equal(c.practicalArtifacts.value[3].versionId, 5)
})
test('提交失败保留原分和暂存文件，可以重试', async () => {
  const { c } = harness()
  c.submitPracticalArtifact = async () => { throw new Error('网络失败') }
  await assert.rejects(c.commitPracticalArtifact({ questionId: 3 }))
  assert.equal(c.practicalScores.value[3], 68)
  assert.equal(c.practicalArtifacts.value[3].versionId, 5)
  assert.equal(c.practicalDrafts.value[3].length, 1)
})
test('确认期间切换课程，不向新课误提交', async () => {
  const { c, calls } = harness()
  c.ElMessageBox.confirm = async () => { c.lessonId.value = 99; c.practicalUploadEpoch++ }
  await c.commitPracticalArtifact({ questionId: 3 })
  assert.equal(calls.length, 0)
})
test('提交成功但刷新失败，保留新版本和待批状态，不提示提交失败', async () => {
  const { c, messages } = harness()
  c.refreshPracticalSubmission = async () => { throw new Error('刷新失败') }
  await c.commitPracticalArtifact({ questionId: 3 })
  assert.equal(c.practicalScores.value[3], null)
  assert.equal(c.practicalArtifacts.value[3].versionId, 7)
  assert.equal(messages.at(-1).type, 'warning')
  assert.match(messages.at(-1).text, /新作品已提交/)
})
test('未评分首次提交不增加确认步骤', async () => {
  const { c, calls } = harness(null)
  c.ElMessageBox.confirm = () => assert.fail('未评分无需清分确认')
  await c.commitPracticalArtifact({ questionId: 3 })
  assert.equal(calls.length, 1)
})
