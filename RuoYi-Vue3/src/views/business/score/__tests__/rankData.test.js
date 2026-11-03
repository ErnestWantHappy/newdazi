import test from 'node:test'
import assert from 'node:assert/strict'
import { buildRankData } from '../components/charts/rankData.js'

test('全班45人含操作待批、请假缺考、真实0分全部保留', () => {
  const students = Array.from({ length: 45 }, (_, i) => ({ studentId: i, studentName: `学生${i}`, finalTotal: i < 40 ? i : null, scoreStatus: i < 40 ? '操作待批' : '请假' }))
  const ranked = buildRankData(students)
  assert.equal(ranked.length, 45)
  assert.equal(ranked[0].chartScore, 39)
  assert.equal(ranked[39].chartScore, 0)
  assert.equal(ranked[40].chartScore, null)
  assert.equal(students[0].studentId, 0)
})
