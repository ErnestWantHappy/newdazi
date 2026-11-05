// 等级/赋分共用的排名工具：页面与导出必须同源
import test from 'node:test'
import assert from 'node:assert/strict'
import { buildGradeMaps } from '../gradeRanking.js'

const students = [
  { studentId: 1, total: 300 },
  { studentId: 2, total: 200 },
  { studentId: 3, total: 200 },
  { studentId: 4, total: 100 },
  { studentId: 5, total: 0 }
]

test('等级按总量排序，0 分不参与评级', () => {
  const { grades } = buildGradeMaps(students.filter(s => s.total > 0), { excellent: 25, good: 40, pass: 30 }, s => s.total)
  assert.equal(grades.size, 4)
  assert.equal(grades.get(1), '优秀')
  // 4 人 × 25/40/30 → 优秀1、良好2、及格1，末位落及格档（与页面原有规则一致）
  assert.equal(grades.get(4), '及格')
  assert.equal(grades.get(5), undefined)
})

test('并列总量名次相同、赋分相同', () => {
  const { scores } = buildGradeMaps(students.filter(s => s.total > 0), { excellent: 25, good: 40, pass: 30 }, s => s.total)
  assert.equal(scores.get(2), scores.get(3))
  assert.ok(scores.get(1) > scores.get(2))
})

test('单人时赋分为 100，空名单不报错', () => {
  assert.equal(buildGradeMaps([{ studentId: 9, total: 60 }], {}, s => s.total).scores.get(9), 100)
  const empty = buildGradeMaps([], {}, s => s.total)
  assert.equal(empty.grades.size, 0)
  assert.equal(empty.scores.size, 0)
})

test('比例缺省为 25/40/30', () => {
  const many = Array.from({ length: 10 }, (_, i) => ({ studentId: i, total: 100 - i }))
  const { grades } = buildGradeMaps(many, {}, s => s.total)
  assert.equal(grades.get(0), '优秀')
  // 10 人 × 25/40/30 → 优秀3、良好4、及格3，末位正好进及格档
  assert.equal(grades.get(9), '及格')
  assert.equal(grades.get(6), '良好')
})
