import test from 'node:test'
import assert from 'node:assert/strict'

import {
  BEGINNER_MODULES,
  DEFAULT_STRUCTURE_PRESET_ID,
  LEGACY_STRUCTURE_PRESET_IDS,
  STRUCTURE_PRESETS,
  createBeginnerFormJson,
  createModuleWidget
} from '../utils/presetFactories.js'

test('新手组件库固定为九个教学模块', () => {
  assert.equal(BEGINNER_MODULES.length, 9)
  assert.equal(new Set(BEGINNER_MODULES.map(item => item.type)).size, 9)
})

test('默认结构为空白画布，历史六套结构仅数据兼容', () => {
  assert.equal(DEFAULT_STRUCTURE_PRESET_ID, 'blank')
  assert.equal(LEGACY_STRUCTURE_PRESET_IDS.length, 6)
  assert.ok(STRUCTURE_PRESETS.some(item => item.id === 'blank'))

  const blank = createBeginnerFormJson()
  const homeTab = blank.widgetList[0]
  assert.equal(homeTab.type, 'tab')
  assert.equal(homeTab.options.name, 'HomeTab')
  assert.equal(homeTab.tabs[0].widgetList.length, 0)

  for (const presetId of LEGACY_STRUCTURE_PRESET_IDS) {
    const formJson = createBeginnerFormJson(presetId, {
      topic: '网络安全',
      estimatedMinutes: 20
    })
    const tab = formJson.widgetList[0]
    assert.equal(tab.type, 'tab')
    assert.ok(tab.tabs[0].widgetList.length > 0)
    assert.ok(tab.tabs[0].widgetList.every(widget => widget.options.beginnerModuleType))
  }
})

test('单选题预设只携带教师需要的常用配置', () => {
  const widget = createModuleWidget('singleChoice', { title: '以下哪项更安全？' })
  assert.equal(widget.type, 'radio')
  assert.equal(widget.options.label, '以下哪项更安全？')
  assert.deepEqual(widget.options.optionItems.map(item => item.label), ['选项 A', '选项 B', '选项 C', '选项 D'])
  assert.equal(widget.scoring.score, 10)
  assert.equal(widget.options.required, true)
})

test('课堂反思不会携带隐藏评分配置', () => {
  const widget = createModuleWidget('reflection')
  assert.equal(Object.hasOwn(widget, 'scoring'), false)
})

test('文本型教学任务默认人工评分', () => {
  for (const moduleType of ['preClassCheck', 'shortAnswer']) {
    const widget = createModuleWidget(moduleType)
    assert.equal(widget.scoring.type, 'manual')
    assert.equal(widget.scoring.answer, '')
  }
})
