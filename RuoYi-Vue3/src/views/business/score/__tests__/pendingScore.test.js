import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import vm from 'node:vm'
const source = readFileSync(new URL('../index.vue', import.meta.url), 'utf8')
function summarize(scores) {
  const c = vm.createContext({ selectedLessonIds: { value: scores.map(s => s.lessonId) }, queryParams: { value: { entryYear: '2025' } },
    rawData: { value: [{ studentId: 1, studentNo: '1', scores }] }, tableData: { value: [] },
    roundOne: n => Math.round(n * 10) / 10, naturalCodeCompare: () => 0 })
  vm.runInContext(source.slice(source.indexOf('function processData()'), source.indexOf('/** 学号/班级号自然序')), c)
  c.processData()
  return c.tableData.value[0]
}
const pending = { lessonId: 2, typingScore: 15, theoryScore: 20, practicalScore: null, totalScore: 35, finalScore: 35, practicalPending: true, pendingGrading: true }
test('操作待批仍累计打字和理论，参与课程均分', () => {
  const row = summarize([{ lessonId: 1, practicalScore: 80, totalScore: 80, finalScore: 80 }, { ...pending, performanceScore: 10 }])
  assert.equal(row.avgPractical, 80)
  assert.equal(row.filteredAverage, 57.5)
  assert.equal(row.filteredTotal, 57.5)
  assert.equal(row.totalPerformance, 5)
})
test('操作待批保留空值，总分先展示已有成绩，真实零分参与平均', () => {
  assert.equal(summarize([pending]).filteredTotal, 35)
  assert.equal(summarize([pending]).avgPractical, null)
  const row = summarize([{ lessonId: 1, practicalScore: 80, totalScore: 80 }, { lessonId: 2, practicalScore: 0, totalScore: 0 }])
  assert.equal(row.avgPractical, 40)
  assert.equal(row.filteredAverage, 40)
})

test('部分操作已批仍进入操作平均，待批提醒保持', () => {
  const row = summarize([{ ...pending, practicalScore: 10, totalScore: 45, finalScore: 45 }])
  assert.equal(row.avgPractical, 10)
  assert.equal(row.finalTotal, 45)
  assert.equal(row.scoreStatus, '操作待批')
})

test('全班名单保留缺考请假未提交，空值不冒充真实0分', () => {
  for (const [flag, status] of [['isAbsent', '请假'], ['isMissing', '缺考'], ['unsubmitted', '未提交']]) {
    const row = summarize([{ ...pending, [flag]: true }])
    assert.equal(row.finalTotal, null)
    assert.equal(row.scoreStatus, status)
  }
  assert.equal(summarize([{ ...pending, totalScore: 0, finalScore: 0 }]).finalTotal, 0)
})
test('人工总分覆盖不伪造操作题评分', () => {
  const row = summarize([{ ...pending, pendingGrading: false, totalScore: 70, finalScore: 75 }])
  assert.equal(row.avgPractical, null)
  assert.equal(row.filteredTotal, 70)
  assert.equal(row.finalTotal, 75)
})
