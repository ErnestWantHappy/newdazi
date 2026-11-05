import assert from 'node:assert/strict'
import test from 'node:test'
import {
  MODULE_SCOPES,
  PRACTICAL_MODES,
  WORKBENCH_DEFAULTS,
  answerLabel,
  buildAllListQuery,
  buildModuleQuery,
  cardActions,
  cardTitle,
  choiceOptions,
  createModuleState,
  hasMoreAfter,
  mergeQuestionPage,
  practicalModeGroupKey,
  primaryAction,
  scopeOf,
  supportsGradeScope,
} from '../questionWorkbench.js'

const emptyFilters = { questionContent: null, grade: null, semester: null, lessonNum: null, isPublic: null, createBy: null, difficulty: null }

test('默认加载条数集中配置为打字8、选择6、判断6、操作6', () => {
  assert.deepEqual({ ...WORKBENCH_DEFAULTS }, { typing: 8, choice: 6, judgment: 6, practical: 6 })
  const page = buildModuleQuery(emptyFilters, scopeOf('typing'))
  assert.equal(page.pageSize, WORKBENCH_DEFAULTS.typing)
  assert.equal(page.pageNum, 1)
})

test('操作题四种方式顺序固定且文件作品为默认', () => {
  assert.deepEqual(PRACTICAL_MODES.map(item => item.key), ['FILE', 'PYTHON', 'DATA_LAB', 'FLOWCHART'])
  assert.equal(scopeOf('FILE').practicalMode, 'FILE')
  assert.equal(scopeOf('missing').key, 'typing')
})

test('模块查询带对应题型与操作方式，且每次从第一页重建', () => {
  const choice = buildModuleQuery({ ...emptyFilters, questionContent: '算法', isPublic: 'Y' }, scopeOf('choice'), 1, 6)
  assert.equal(choice.questionType, 'choice')
  assert.equal(choice.practicalMode, null)
  assert.equal(choice.questionContent, '算法')
  assert.equal(choice.isPublic, 'Y')

  const flow = buildModuleQuery(emptyFilters, scopeOf('FLOWCHART'), 2, 6)
  assert.equal(flow.questionType, 'practical')
  assert.equal(flow.practicalMode, 'FLOWCHART')
  assert.equal(flow.pageNum, 2)
})

test('文件作品明确传 FILE，由服务端兼容历史空值而非查询所有操作题', () => {
  const file = buildModuleQuery(emptyFilters, scopeOf('FILE'))
  assert.equal(file.practicalMode, 'FILE')
  assert.equal(MODULE_SCOPES.FILE.includeLegacyMode, true)
})

test('年级、学期、课次只传给支持它们的题型', () => {
  const filters = { ...emptyFilters, grade: '7', semester: '1', lessonNum: '3', difficulty: 'HARD' }
  assert.equal(buildModuleQuery(filters, scopeOf('typing')).grade, '7')
  assert.equal(buildModuleQuery(filters, scopeOf('FILE')).lessonNum, '3')
  const python = buildModuleQuery(filters, scopeOf('PYTHON'))
  assert.equal(python.grade, null)
  assert.equal(python.semester, null)
  assert.equal(python.lessonNum, null)
  // Python 难度只作用于 Python 模块
  assert.equal(python.difficulty, 'HARD')
  assert.equal(buildModuleQuery(filters, scopeOf('choice')).difficulty, null)
  const dataLab = buildModuleQuery(filters, scopeOf('DATA_LAB'))
  assert.equal(dataLab.grade, null)
  assert.equal(dataLab.difficulty, null)
})

test('支持年级范围的判断与题型一致', () => {
  assert.equal(supportsGradeScope({ questionType: 'choice' }), true)
  assert.equal(supportsGradeScope({ questionType: 'practical', practicalMode: 'PYTHON' }), false)
  assert.equal(supportsGradeScope({ questionType: 'practical', practicalMode: 'DATA_LAB' }), false)
  assert.equal(supportsGradeScope({ questionType: 'practical', practicalMode: 'FLOWCHART' }), true)
  assert.equal(supportsGradeScope(null), false)
})

test('查看全部复用筛选并把 Python 走回原题库视图', () => {
  const query = buildAllListQuery({ ...emptyFilters, questionContent: '和' }, scopeOf('PYTHON'), 2, 10)
  assert.equal(query.bankView, 'PYTHON')
  assert.equal(query.questionType, 'practical')
  assert.equal(query.practicalMode, 'PYTHON')
  assert.equal(query.pageNum, 2)
  assert.equal(buildAllListQuery(emptyFilters, scopeOf('choice')).bankView, undefined)
})

test('卡片标题与摘要来自真实字段', () => {
  assert.equal(cardTitle({ practicalMode: 'PYTHON', programmingTitle: '两数之和', questionContent: '读入…' }), '两数之和')
  assert.equal(cardTitle({ questionType: 'typing', questionContent: '  走进数字生活  ' }), '走进数字生活')
  assert.equal(cardTitle({ questionType: 'choice', questionContent: '' }), '未填写题目内容')
  assert.deepEqual(choiceOptions({ optionA: '键盘', optionB: '', optionC: '显示器' }), [{ key: 'A', text: '键盘' }, { key: 'C', text: '显示器' }])
  assert.equal(answerLabel({ questionType: 'judgment', answer: 'T' }), '答案 正确')
  assert.equal(answerLabel({ questionType: 'typing' }), '')
})

test('按钮按权限与归属动态显示：他人公开题只能查看', () => {
  const all = ['business:question:query', 'business:question:edit', 'business:question:remove']
  const own = { questionId: 1, questionType: 'choice', creatorId: 7 }
  const others = { questionId: 2, questionType: 'choice', creatorId: 8 }
  assert.deepEqual(cardActions(own, { permissions: all, userId: 7 }), ['view', 'edit', 'remove'])
  assert.deepEqual(cardActions(others, { permissions: all, userId: 7 }), ['view'])
  // 只有编辑权限不能删除
  assert.deepEqual(cardActions(own, { permissions: ['business:question:edit'], userId: 7 }), ['edit'])
  // 没有查看权限且不是自己的题，不留任何按钮
  assert.deepEqual(cardActions(others, { permissions: [], userId: 7 }), [])
  // 管理员按归属规则管理他人的题
  assert.deepEqual(cardActions(others, { permissions: all, userId: 1, isAdmin: true }), ['view', 'edit', 'remove'])
  assert.deepEqual(cardActions(others, { permissions: ['*:*:*'], userId: 1, isAdmin: true }), ['view', 'edit', 'remove'])
  assert.deepEqual(cardActions(own, { permissions: ['business:question:remove'], userId: 7 }), ['remove'])
})

test('操作题预览只在确有预览资源或流程图时出现', () => {
  const base = { questionType: 'practical', creatorId: 7 }
  assert.deepEqual(cardActions({ ...base, practicalMode: 'FILE' }, { permissions: ['business:question:query'], userId: 7 }), [])
  assert.deepEqual(cardActions({ ...base, practicalMode: 'FILE', previewPath: 'a/b.png' }, { userId: 7 }), ['preview'])
  assert.deepEqual(cardActions({ ...base, practicalMode: 'FLOWCHART' }, { userId: 7 }), ['preview'])
  // 操作题不能走普通题详情
  assert.deepEqual(cardActions({ ...base, practicalMode: 'PYTHON' }, { permissions: ['business:question:query'], userId: 7 }), [])
  assert.equal(primaryAction(['preview']), 'preview')
  assert.equal(primaryAction(['view', 'edit']), 'view')
  assert.equal(primaryAction([]), '')
})

test('模块状态独立，加载更多的页码与去重独立维护', () => {
  const typing = createModuleState('typing')
  const choice = createModuleState('choice')
  typing.pageNum = 2
  assert.equal(choice.pageNum, 1)
  assert.equal(typing.loaded, false)

  const page1 = [{ questionId: 1 }, { questionId: 2 }]
  const page2 = [{ questionId: 2 }, { questionId: 3 }]
  const merged = mergeQuestionPage(page1, page2, 3)
  assert.deepEqual(merged.items.map(item => item.questionId), [1, 2, 3])
  assert.equal(merged.hasMore, false)
  assert.equal(mergeQuestionPage(page1, page2, 5).hasMore, true)
  assert.equal(hasMoreAfter({ total: 6 }, 6), false)
  assert.equal(hasMoreAfter({ total: 6 }, 2), true)
})

test('历史操作方式为空时归入文件作品', () => {
  assert.equal(practicalModeGroupKey({ practicalMode: null }), 'FILE')
  assert.equal(practicalModeGroupKey({ practicalMode: 'PYTHON' }), 'PYTHON')
})
