import assert from 'node:assert/strict'
import test from 'node:test'

import { sortDashboardStudents } from '../utils/dashboardStudentList.js'

test('看板列表保留已提交、填写中和未开始学生', () => {
  const rows = [
    { studentId: 3, classCode: '1', studentNo: '3', currentPage: 0, isSubmitted: 'N' },
    { studentId: 2, classCode: '1', studentNo: '2', currentPage: 1, isSubmitted: 'N' },
    { studentId: 1, classCode: '1', studentNo: '1', currentPage: 2, isSubmitted: 'Y' }
  ]

  const result = sortDashboardStudents(rows)

  assert.equal(result.length, 3)
  assert.deepEqual(result.map(item => item.studentId), [1, 2, 3])
})

test('全部班级按班级分组且不修改接口原数组', () => {
  const rows = [
    { studentId: 4, classCode: '2', studentNo: '1', currentPage: 1, isSubmitted: 'N' },
    { studentId: 2, classCode: '1', studentNo: '2', currentPage: 0, isSubmitted: 'N' }
  ]

  const result = sortDashboardStudents(rows)

  assert.deepEqual(result.map(item => item.studentId), [2, 4])
  assert.deepEqual(rows.map(item => item.studentId), [4, 2])
})
