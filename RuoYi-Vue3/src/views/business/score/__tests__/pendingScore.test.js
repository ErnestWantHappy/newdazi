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
const pending = { lessonId: 2, practicalScore: null, totalScore: null, finalScore: null, practicalPending: true, pendingGrading: true }
test('待批改不拉低已评分课程均分，表现分独立保留', () => {
  const row = summarize([{ lessonId: 1, practicalScore: 80, totalScore: 80, finalScore: 80 }, { ...pending, performanceScore: 10 }])
  assert.equal(row.avgPractical, 80)
  assert.equal(row.filteredAverage, 80)
  assert.equal(row.totalPerformance, 5)
})
test('只有待批改时保持空值，真实零分参与平均', () => {
  assert.equal(summarize([pending]).filteredTotal, null)
  assert.equal(summarize([pending]).avgPractical, null)
  const row = summarize([{ lessonId: 1, practicalScore: 80, totalScore: 80 }, { lessonId: 2, practicalScore: 0, totalScore: 0 }])
  assert.equal(row.avgPractical, 40)
  assert.equal(row.filteredAverage, 40)
})
test('人工总分覆盖不伪造操作题评分', () => {
  const row = summarize([{ ...pending, pendingGrading: false, totalScore: 70, finalScore: 75 }])
  assert.equal(row.avgPractical, null)
  assert.equal(row.filteredTotal, 70)
  assert.equal(row.finalTotal, 75)
})
