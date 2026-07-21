import test from 'node:test'
import assert from 'node:assert/strict'

import { normalizeStudentFormData } from '../utils/studentFormState.js'

function createFormJson(defaultValue = null) {
  return {
    widgetList: [{
      type: 'tab',
      category: 'container',
      tabs: [{
        type: 'tab-pane',
        category: 'container',
        widgetList: [{
          type: 'textarea',
          formItemFlag: true,
          options: { name: 'reflection', defaultValue: null }
        }, {
          type: 'checkbox',
          formItemFlag: true,
          options: {
            name: 'securityPractices',
            defaultValue,
            optionItems: [
              { label: '使用强密码', value: 'A' },
              { label: '开启双重验证', value: 'B' }
            ]
          }
        }]
      }]
    }]
  }
}

test('多选题的 null 草稿恢复为可写入的数组模型', () => {
  const source = {
    reflection: '我学会了保护个人信息。',
    securityPractices: null,
    selfAssessment: 3
  }

  const normalized = normalizeStudentFormData(createFormJson(), source)

  assert.deepEqual(normalized.securityPractices, [])
  normalized.securityPractices.push('A', 'B')
  assert.deepEqual(normalized.securityPractices, ['A', 'B'])
  assert.equal(normalized.reflection, source.reflection)
  assert.equal(normalized.selfAssessment, 3)
  assert.equal(source.securityPractices, null)
})

test('刷新时保留已保存的多选答案', () => {
  const source = { securityPractices: ['A', 'B'] }

  const normalized = normalizeStudentFormData(createFormJson(), source)

  assert.deepEqual(normalized.securityPractices, ['A', 'B'])
  assert.notEqual(normalized.securityPractices, source.securityPractices)
})

test('无草稿时使用多选题的数组默认值', () => {
  const normalized = normalizeStudentFormData(createFormJson(['B']), {})

  assert.deepEqual(normalized.securityPractices, ['B'])
})

test('旧模板的多选标量值转换为数组且不丢失', () => {
  const normalized = normalizeStudentFormData(createFormJson(), {
    securityPractices: 'A'
  })

  assert.deepEqual(normalized.securityPractices, ['A'])
})

test('刷新时清除历史认证失败遗留的伪成功文件', () => {
  const formJson = createFormJson()
  formJson.widgetList[0].tabs[0].widgetList.push({
    type: 'file-upload',
    formItemFlag: true,
    options: { name: 'studentWork', defaultValue: null }
  })
  const valid = { name: 'report.pdf', url: '/business/guide-sheet/uploads/1/content' }
  const invalid = {
    name: 'logo.png',
    status: 'success',
    response: { code: 401, msg: '认证失败' }
  }

  const normalized = normalizeStudentFormData(formJson, {
    studentWork: [valid, invalid]
  })

  assert.deepEqual(normalized.studentWork, [valid])
})
