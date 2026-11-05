import assert from 'node:assert/strict'
import test from 'node:test'
import vm from 'node:vm'
import { buildModuleQuery, scopeOf } from '../questionWorkbench.js'
import { read, scriptOf } from './helpers/workbenchSource.js'

const page = 'src/views/business/question/index.vue'
const source = read(page)
const script = scriptOf(page)

test('工作台按“打字题 → 选择/判断并列 → 操作题”固定顺序排列', () => {
  const typing = source.indexOf('title="打字题"')
  const choice = source.indexOf('title="选择题"')
  const judgment = source.indexOf('title="判断题"')
  const practical = source.indexOf('title="操作题"')
  assert.ok(typing > 0 && choice > typing && judgment > choice && practical > judgment, '模块顺序不符合固定布局')
  // 选择与判断放在同一个半宽容器里，窄屏再由 CSS 转单列
  const pair = source.indexOf('<div class="qw__pair">')
  assert.ok(pair > typing && pair < practical, '选择/判断未并列在同一行容器')
  assert.ok(source.includes('@media (max-width: 768px)'), '缺少窄屏单列规则')
})

test('默认加载数来自集中配置，页面里不再写死 8/6', () => {
  assert.ok(script.includes('WORKBENCH_DEFAULTS'), '页面未引用默认条数配置')
  assert.ok(!/pageSize:\s*8\b|pageSize:\s*6\b/.test(script), '页面里出现了散落的默认条数')
})

test('筛选默认展开，年级/学期/课次不进入 Python 与数据分析实验', () => {
  assert.ok(script.includes('const filtersOpen = ref(true)'), '初次进入筛选应默认展开')
  assert.ok(script.includes('supportsGradeScope'), '未按题型判断年级范围支持')
})

test('七个新增入口都带入对应题型或操作方式', () => {
  assert.ok(/@add="handleAddFrom\('typing'\)"/.test(source))
  assert.ok(/@add="handleAddFrom\('choice'\)"/.test(source))
  assert.ok(/@add="handleAddFrom\('judgment'\)"/.test(source))
  assert.ok(/emit\('add', item\.key\)/.test(read('src/views/business/question/components/PracticalModePicker.vue')), '操作方式新增未带上方式')
  // DATA_LAB 直接打开专用编辑器，不进普通表单
  assert.ok(/practicalMode === "DATA_LAB"[\s\S]{0,120}dataLabEditor\.value\?\.open\(\)/.test(script), '数据分析实验未走专用编辑器')
  // 每次新增都先 reset 再设类型，不受上一次编辑影响
  const beginAdd = script.slice(script.indexOf('function beginAdd('), script.indexOf('/** 修改按钮操作 */'))
  assert.ok(beginAdd.indexOf('reset()') < beginAdd.indexOf('form.value.questionType = questionType'), '新增时未先重置再设题型')
})

test('查看/预览/编辑/删除按权限与归属动态判断，不留统一按钮排', () => {
  assert.ok(script.includes('cardActions(row, {'), '卡片按钮未走权限判断')
  assert.ok(!/v-hasPermi=\[\s*'business:question:(edit|remove)'\s*\]\s*\n?\s*>\s*(修改|删除)/.test(source), '模板仍硬编码编辑/删除按钮')
})

test('保存与删除后刷新四个模块，并提示新题可能被筛选隐藏', () => {
  assert.ok(/function refreshAll\(\)[\s\S]{0,300}loadModule\("typing"\)[\s\S]{0,120}loadModule\("choice"\)[\s\S]{0,120}loadModule\("judgment"\)[\s\S]{0,120}loadModule\("practical"\)/.test(script), '刷新未覆盖全部模块')
  assert.ok(script.includes('newQuestionFilterHint'), '缺少新题不匹配筛选时的提示')
})

test('多阶段保存失败重试不重复建题', () => {
  assert.ok(script.includes('createdQuestionId'), '缺少已建主记录的复用标记')
  assert.ok(/createdQuestionId\.value !== null|createdQuestionId\.value != null/.test(script), '重试时未复用已建记录')
})

test('页面保持单一根节点，编辑器留在根容器内', () => {
  const template = source.slice(0, source.indexOf('<script'))
  const rootMatches = template.match(/^<template>\s*\n\s*<div /m)
  assert.ok(rootMatches, '模板根节点不是单个 div')
  const dataLab = template.indexOf('<DataLabEditor')
  const rootEnd = template.lastIndexOf('</div>')
  assert.ok(dataLab > 0 && dataLab < rootEnd, 'DataLabEditor 不在根容器内，会破坏切页动画')
})

test('原编辑器与导入导出能力仍在页面中', () => {
  for (const text of [
    'FlowchartQuestionDesigner',
    'FlowchartQuestionPreviewDialog',
    'DataLabEditor',
    'pythonImportOpen',
    'handlePythonWorkbook',
    'importTemplate',
    'handleExport',
    'practicalExtensionOptions',
  ]) {
    assert.ok(script.includes(text), `缺少原有能力：${text}`)
  }
  // PY 文件格式支持不能被回退
  assert.ok(script.includes('"py"') || script.includes("'py'"), 'PY 扩展名支持丢失')
})

// 执行页面中的真实函数，覆盖静态字符串断言无法发现的变量遮蔽和错误动作。
function functionSource(name, nextName) {
  const start = script.indexOf(`function ${name}(`)
  return script.slice(start, script.indexOf(`function ${nextName}(`, start))
}

test('三个操作题保存回调能切换方式、关闭弹窗并刷新完整列表', () => {
  for (const mode of ['FILE', 'PYTHON', 'FLOWCHART']) {
    const calls = []
    const ctx = { practicalMode: { value: 'FILE' }, viewingAll: { value: true }, activeScopeKey: { value: 'typing' }, allQuery: { pageNum: 3 }, open: { value: true }, scopeOf,
      refreshAll: () => calls.push('refresh'), loadAllList: () => calls.push('list') }
    vm.runInNewContext(`${functionSource('afterQuestionSaved', 'newQuestionFilterHint')} afterQuestionSaved('practical', '${mode}')`, ctx)
    assert.equal(ctx.practicalMode.value, mode)
    assert.equal(ctx.activeScopeKey.value, mode)
    assert.equal(ctx.open.value, false)
    assert.equal(ctx.allQuery.pageNum, 1)
    assert.deepEqual(calls, ['refresh', 'list'])
  }
})

test('方式计数逐类取真实 total，不重复累计全操作题', async () => {
  const queries = []
  const ctx = { modeTotals: { counts: null, requestId: 0 }, PRACTICAL_MODE_KEYS: ['FILE', 'PYTHON', 'DATA_LAB', 'FLOWCHART'], appliedFilters: { value: {} }, buildModuleQuery, scopeOf,
    listQuestion: async query => { queries.push(query); return { total: ({ FILE: 100, PYTHON: 20, DATA_LAB: 5, FLOWCHART: 7 })[query.practicalMode] ?? 132 } } }
  const start = script.indexOf('async function loadPracticalModeTotals(')
  await vm.runInNewContext(`${script.slice(start, script.indexOf('const practicalCounts =', start))} loadPracticalModeTotals()`, ctx)
  assert.equal(ctx.modeTotals.counts.FILE, 100)
  assert.equal(queries.length, 4)
  assert.ok(queries.every(query => query.practicalMode))
})

test('批量编辑与删除各自鉴权，退出无写操作', () => {
  const bulk = script.slice(script.indexOf('function exitSelection('), script.indexOf('function enterSelection('))
  for (const permission of ['edit', 'remove', 'none']) {
    const calls = []
    const rows = [{ questionId: 1 }, { questionId: 2 }]
    const ctx = { selectedIds: { value: [1] }, selectedQuestions: { value: [rows[0]] }, allChecked: { value: true }, selectionMode: { value: true }, canManageSelected: { value: true }, allState: { items: rows },
      hasPermi: code => code === `business:question:${permission}`, canManageQuestion: () => true,
      handleUpdate: row => calls.push(['edit', row.questionId]), handleDelete: row => calls.push(['remove', Array.from(row.questionIds)]) }
    vm.createContext(ctx)
    vm.runInContext(bulk, ctx)
    vm.runInContext('editSelected()', ctx)
    assert.equal(calls.length, permission === 'edit' ? 1 : 0)
    calls.length = 0
    ctx.selectedIds.value = [1, 2]
    vm.runInContext('deleteSelected()', ctx)
    assert.equal(calls.length, permission === 'remove' ? 1 : 0)
    calls.length = 0
    vm.runInContext('exitSelection()', ctx)
    assert.equal(calls.length, 0)
    assert.equal(ctx.selectedIds.value.length, 0)
    assert.equal(ctx.selectionMode.value, false)
  }
})
