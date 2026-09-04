import test from 'node:test'
import assert from 'node:assert/strict'

import {
  calculateEntryYearFromGrade,
  calculateGradeNumber,
  createAcademicYearOption,
  resolveAcademicSemester,
  resolveAcademicStartYear
} from '../academicYear.js'

function localDate(year, month, day) {
  return new Date(year, month - 1, day, 12, 0, 0)
}

test('7月20日当天切换新学年', () => {
  assert.equal(resolveAcademicStartYear(localDate(2026, 7, 19)), 2025)
  assert.equal(resolveAcademicStartYear(localDate(2026, 7, 20)), 2026)
  assert.equal(resolveAcademicStartYear(localDate(2026, 7, 21)), 2026)
})

test('学期边界与免抽测统计区间一致', () => {
  assert.equal(resolveAcademicSemester(localDate(2026, 1, 31)), '1')
  assert.equal(resolveAcademicSemester(localDate(2026, 2, 1)), '2')
  assert.equal(resolveAcademicSemester(localDate(2026, 7, 19)), '2')
  assert.equal(resolveAcademicSemester(localDate(2026, 7, 20)), '1')
})

test('初中入学年份与年级在边界日前后保持一致', () => {
  assert.equal(calculateGradeNumber('2024', '2', localDate(2026, 7, 19)), 8)
  assert.equal(calculateGradeNumber('2024', '2', localDate(2026, 7, 20)), 9)
  assert.equal(calculateGradeNumber('2025', '2', localDate(2026, 7, 20)), 8)
  assert.equal(calculateEntryYearFromGrade(9, localDate(2026, 7, 20)), '2024')
  assert.equal(calculateEntryYearFromGrade(8, localDate(2026, 7, 20)), '2025')
})

test('学年查询范围与7月20日切换口径一致', () => {
  assert.deepEqual(createAcademicYearOption(2026), {
    value: '2026',
    label: '2026-2027 学年',
    start: '2026-07-20',
    end: '2027-07-19'
  })
})
