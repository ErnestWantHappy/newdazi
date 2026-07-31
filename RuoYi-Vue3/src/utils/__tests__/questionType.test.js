import assert from 'node:assert/strict'
import test from 'node:test'
import { questionTypeLabel } from '../questionType.js'

test('known question types use unified Chinese labels', () => {
  assert.equal(questionTypeLabel('choice'), '选择题')
  assert.equal(questionTypeLabel('judgment'), '判断题')
  assert.equal(questionTypeLabel('typing'), '打字题')
  assert.equal(questionTypeLabel('practical'), '操作题')
})

test('unknown and empty question types use safe Chinese fallback', () => {
  assert.equal(questionTypeLabel('essay'), '其他题型')
  assert.equal(questionTypeLabel(undefined), '其他题型')
  assert.equal(questionTypeLabel(''), '其他题型')
})
