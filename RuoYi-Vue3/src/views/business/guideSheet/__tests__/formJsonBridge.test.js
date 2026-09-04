import test from 'node:test'
import assert from 'node:assert/strict'

import {
  createBeginnerItemsFromLines,
  createSafePreviewFormJson,
  hasRenderableWidgets,
  parseFormJsonSafely,
  readBeginnerDocument,
  splitSuggestionLines,
  writeBeginnerDocument
} from '../utils/formJsonBridge.js'
import { createBeginnerFormJson } from '../utils/presetFactories.js'

test('损坏数据进入可恢复状态且不会抛出异常', () => {
  const result = parseFormJsonSafely('{broken')
  assert.equal(result.ok, false)
  assert.equal(result.formJson, null)
  assert.ok(result.raw.includes('broken'))
})

test('未知高级组件往返转换后保持原数据和顺序', () => {
  const formJson = createBeginnerFormJson('before-class')
  const widgets = formJson.widgetList[0].tabs[0].widgetList
  const unknown = {
    id: 'advanced-grid-1',
    type: 'grid',
    category: 'container',
    cols: [{ span: 12, widgetList: [] }],
    options: { name: 'advancedGrid', customClass: 'teacher-layout' }
  }
  widgets.splice(1, 0, unknown)

  const document = readBeginnerDocument(formJson)
  assert.equal(document.items[1].advanced, true)
  assert.equal(document.advancedCount, 1)

  document.items[0].title = '更新后的学习目标'
  const roundTripped = writeBeginnerDocument(formJson, document)
  const resultWidgets = roundTripped.widgetList[0].tabs[0].widgetList
  assert.deepEqual(resultWidgets[1], unknown)
  assert.equal(resultWidgets[0].options.label, '更新后的学习目标')
})

test('旧模板中的常见题型可进入新手模式继续编辑', () => {
  const legacy = {
    widgetList: [{
      id: 'legacy-radio',
      type: 'radio',
      options: {
        name: 'question1',
        label: '旧单选题',
        required: true,
        optionItems: [{ label: '是', value: 'yes' }, { label: '否', value: 'no' }]
      }
    }]
  }
  const document = readBeginnerDocument(legacy)
  assert.equal(document.items[0].advanced, false)
  assert.equal(document.items[0].moduleType, 'singleChoice')
  assert.equal(document.items[0].title, '旧单选题')
})

test('旧模板空组件评分可从根快照恢复到新手模式', () => {
  const legacy = {
    widgetList: [{
      id: 'legacy-radio',
      type: 'radio',
      options: {
        name: 'question1',
        label: '旧根快照题',
        optionItems: [{ label: '是', value: 'yes' }, { label: '否', value: 'no' }]
      },
      scoring: {}
    }],
    _scoringConfig: {
      旧根快照题: {
        score: 12,
        answer: 'yes',
        type: 'exact',
        explanation: '历史答案解析'
      }
    }
  }

  const document = readBeginnerDocument(legacy)

  assert.equal(document.items[0].score, 12)
  assert.equal(document.items[0].correctAnswer, 'yes')
  assert.equal(document.items[0].explanation, '历史答案解析')
})

test('旧文本题未配置评分方式时回写为人工评分', () => {
  const legacy = {
    widgetList: [{
      id: 'legacy-textarea',
      type: 'textarea',
      options: { name: 'question1', label: '说说你的理解', required: true }
    }]
  }

  const document = readBeginnerDocument(legacy)
  const roundTripped = writeBeginnerDocument(legacy, document)

  assert.equal(roundTripped.widgetList[0].scoring.type, 'manual')
  assert.equal(roundTripped.widgetList[0].scoring.answer, '')
})

test('新手修改选择题后根快照与组件评分保持一致', () => {
  const legacy = {
    widgetList: [{
      id: 'single-choice',
      type: 'radio',
      options: {
        name: 'singleChoice',
        label: '旧单选题',
        beginnerModuleType: 'singleChoice',
        beginnerExplanation: '旧单选解析',
        optionItems: [{ label: '选项 A', value: 'A' }, { label: '选项 B', value: 'B' }]
      },
      scoring: { score: 5, answer: 'A', type: 'exact', explanation: '旧单选解析' }
    }, {
      id: 'multiple-choice',
      type: 'checkbox',
      options: {
        name: 'multipleChoice',
        label: '旧多选题',
        beginnerModuleType: 'multipleChoice',
        beginnerExplanation: '旧多选解析',
        optionItems: [
          { label: '选项 A', value: 'A' },
          { label: '选项 B', value: 'B' },
          { label: '选项 C', value: 'C' }
        ]
      },
      scoring: { score: 10, answer: 'A,B', type: 'exact', explanation: '旧多选解析' }
    }, {
      id: 'advanced-select',
      type: 'select',
      options: { name: 'advancedSelect', label: '高级题' }
    }],
    _scoringConfig: {
      旧单选题: { score: 5, answer: 'A', type: 'exact', explanation: '旧单选解析' },
      旧多选题: { score: 10, answer: 'A,B', type: 'exact', explanation: '旧多选解析' },
      高级题: { score: 30, answer: 'legacy', type: 'exact' }
    }
  }

  const document = readBeginnerDocument(legacy)
  Object.assign(document.items[0], {
    title: '更新单选题',
    correctAnswer: 'B',
    score: 15,
    explanation: '选择 B 的原因'
  })
  Object.assign(document.items[1], {
    title: '更新多选题',
    correctAnswer: ['A', 'C'],
    score: 20,
    explanation: 'A、C 都符合条件'
  })

  const roundTripped = writeBeginnerDocument(legacy, document)
  const [singleChoice, multipleChoice] = roundTripped.widgetList

  assert.deepEqual(singleChoice.scoring, {
    score: 15,
    answer: 'B',
    type: 'exact',
    explanation: '选择 B 的原因'
  })
  assert.deepEqual(roundTripped._scoringConfig['更新单选题'], singleChoice.scoring)
  assert.deepEqual(multipleChoice.scoring, {
    score: 20,
    answer: 'A,C',
    type: 'exact',
    explanation: 'A、C 都符合条件'
  })
  assert.deepEqual(roundTripped._scoringConfig['更新多选题'], multipleChoice.scoring)
  assert.equal(Object.hasOwn(roundTripped._scoringConfig, '旧单选题'), false)
  assert.equal(Object.hasOwn(roundTripped._scoringConfig, '旧多选题'), false)
  assert.deepEqual(roundTripped._scoringConfig['高级题'], legacy._scoringConfig['高级题'])
})

test('预览副本移除事件代码且不修改正式表单', () => {
  const original = createBeginnerFormJson('class-task')
  const widget = original.widgetList[0].tabs[0].widgetList[0]
  original.formConfig.functions = 'window.danger = true'
  widget.options.onMounted = 'window.danger = true'
  widget.options.remoteURL = 'https://example.invalid/data'

  const preview = createSafePreviewFormJson(original)
  const previewWidget = preview.widgetList[0].tabs[0].widgetList[0]
  assert.equal(preview.formConfig.functions, '')
  assert.equal(previewWidget.options.onMounted, '')
  assert.equal(previewWidget.options.remoteURL, '')
  assert.equal(widget.options.onMounted, 'window.danger = true')
})

test('学生预览副本不包含评分答案和解析', () => {
  const original = createBeginnerFormJson('after-class')
  const document = readBeginnerDocument(original)
  const choice = document.items.find(item => item.moduleType === 'singleChoice')
  Object.assign(choice, {
    correctAnswer: 'B',
    score: 25,
    explanation: '仅教师可见的答案解析'
  })
  const saved = writeBeginnerDocument(original, document)

  const preview = createSafePreviewFormJson(saved)
  const previewText = JSON.stringify(preview)

  assert.equal(Object.hasOwn(preview, '_scoringConfig'), false)
  assert.equal(previewText.includes('"scoring"'), false)
  assert.equal(previewText.includes('仅教师可见的答案解析'), false)
  assert.equal(saved._scoringConfig[choice.title].answer, 'B')
})

test('空容器不是可保存的教学内容', () => {
  assert.equal(hasRenderableWidgets({ widgetList: [] }), false)
  assert.equal(hasRenderableWidgets({
    widgetList: [{ type: 'tab', category: 'container', tabs: [{ type: 'tab-pane', widgetList: [] }] }]
  }), false)
  assert.equal(hasRenderableWidgets(createBeginnerFormJson('blank')), false)
  assert.equal(hasRenderableWidgets(createBeginnerFormJson('class-task')), true)
})

test('AI 多行建议按非空行生成独立教学模块', () => {
  const source = '1. 先判断已有知识\n\n- 再说明选择理由\r\n3、最后写下疑问'
  assert.deepEqual(splitSuggestionLines(source), ['先判断已有知识', '再说明选择理由', '最后写下疑问'])
  const items = createBeginnerItemsFromLines('preClassCheck', source)
  assert.equal(items.length, 3)
  assert.ok(items.every(item => item.moduleType === 'preClassCheck'))
  assert.deepEqual(items.map(item => item.title), ['先判断已有知识', '再说明选择理由', '最后写下疑问'])
})

test('预览移除所有VForm事件扩展入口', () => {
  const source = {
    widgetList: [{
      id: 'advanced-events',
      type: 'input',
      options: {
        name: 'question',
        onAppendButtonClick: 'steal()',
        onSubFormRowAdd: 'steal()',
        onClose: 'steal()'
      }
    }],
    formConfig: { onCustomLifecycle: 'steal()' }
  }

  const preview = createSafePreviewFormJson(source)
  const previewText = JSON.stringify(preview)

  assert.equal(previewText.includes('steal()'), false)
  assert.equal(source.widgetList[0].options.onAppendButtonClick, 'steal()')
})
