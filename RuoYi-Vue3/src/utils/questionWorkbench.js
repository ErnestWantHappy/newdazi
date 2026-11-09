/**
 * 题库工作台的纯函数配置与摘要逻辑。
 * 单独成文件是为了让“默认条数、查询范围、卡片文案、权限判断”可被 node --test 直接验证，
 * 不把易回归的业务口径散落在模板的魔法数字里。
 */

// 工作台各模块首次加载条数；模板只读配置，不再写 8/6 字样。
export const WORKBENCH_DEFAULTS = Object.freeze({
  typing: 8,
  choice: 6,
  judgment: 6,
  practical: 6,
})

// 操作题四种作答方式；顺序即页面顺序，FILE 为默认方式。
export const PRACTICAL_MODES = Object.freeze([
  { key: 'FILE', label: '文件作品', hint: '文档、演示、表格、图片等' },
  { key: 'PYTHON', label: 'Python 在线编程', hint: '代码编写与自动判题' },
  { key: 'DATA_LAB', label: '数据分析实验', hint: '数据处理与分析任务' },
  { key: 'FLOWCHART', label: '画程流程图', hint: '绘制流程与逻辑表达' },
])

export function practicalModeMeta(mode) {
  return PRACTICAL_MODES.find(item => item.key === mode) || PRACTICAL_MODES[0]
}

/**
 * 年级、学期、课次只对普通题生效。
 * Python 题是通用技能题，后端保存时会清空这些字段；DATA_LAB 是独立编辑器也不绑定它们，
 * 所以这两类不能把它们带进查询，否则会被筛成空列表。
 */
export function supportsGradeScope(row) {
  if (!row) return false
  if (row.questionType !== 'practical') return true
  return row.practicalMode !== 'PYTHON' && row.practicalMode !== 'DATA_LAB'
}

/**
 * 工作台每个模块的列表范围。
 * 文件作品沿用历史口径：操作方式为空的历史题也算文件题，不能漏掉旧题。
 */
export const MODULE_SCOPES = Object.freeze({
  typing: { key: 'typing', title: '打字题', questionType: 'typing', keepsGradeScope: true, grid: 'simple' },
  choice: { key: 'choice', title: '选择题', questionType: 'choice', keepsGradeScope: true, grid: 'simple' },
  judgment: { key: 'judgment', title: '判断题', questionType: 'judgment', keepsGradeScope: true, grid: 'simple' },
  FILE: { key: 'FILE', title: '文件作品', questionType: 'practical', practicalMode: 'FILE', includeLegacyMode: true, keepsGradeScope: true, grid: 'practical' },
  PYTHON: { key: 'PYTHON', title: 'Python 在线编程', questionType: 'practical', practicalMode: 'PYTHON', keepsGradeScope: false, grid: 'practical' },
  DATA_LAB: { key: 'DATA_LAB', title: '数据分析实验', questionType: 'practical', practicalMode: 'DATA_LAB', keepsGradeScope: false, grid: 'practical' },
  FLOWCHART: { key: 'FLOWCHART', title: '画程流程图', questionType: 'practical', practicalMode: 'FLOWCHART', keepsGradeScope: true, grid: 'practical' },
  practical: { key: 'practical', title: '操作题', questionType: 'practical', keepsGradeScope: true, grid: 'practical' },
})

export function scopeOf(key) {
  return MODULE_SCOPES[key] || MODULE_SCOPES.typing
}

/**
 * 组装某个模块的列表查询参数。
 * 每次都由“已应用筛选 + 模块范围”重建，避免把旧页码、旧类型带进请求。
 */
export function buildModuleQuery(filters, scope, pageNum = 1, pageSize = WORKBENCH_DEFAULTS.typing) {
  const query = {
    pageNum,
    pageSize,
    questionContent: filters.questionContent || null,
    isPublic: filters.isPublic || null,
    createBy: filters.createBy || null,
    grade: filters.grade || null,
    semester: filters.semester || null,
    lessonNum: filters.lessonNum || null,
  }
  if (scope.questionType) query.questionType = scope.questionType
  else query.questionType = null
  // FILE 的历史空值兼容由服务端筛选，不能取消条件后混入其他操作题。
  if (scope.practicalMode) query.practicalMode = scope.practicalMode
  else query.practicalMode = null
  // 难度字段只有 Python 题使用，放进其他模块会把它们全部筛空。
  if (scope.practicalMode === 'PYTHON' && filters.difficulty) query.difficulty = filters.difficulty
  else query.difficulty = null
  if (!scope.keepsGradeScope) {
    query.grade = null
    query.semester = null
    query.lessonNum = null
  }
  return query
}

/** “查看全部”复用后端既有 bankView 语义，Python 题库仍走 PYTHON 视图。 */
export function buildAllListQuery(filters, scope, pageNum = 1, pageSize = 10) {
  const query = buildModuleQuery(filters, scope, pageNum, pageSize)
  if (scope.practicalMode === 'PYTHON') query.bankView = 'PYTHON'
  return query
}

export function practicalModeGroupKey(row) {
  return row?.practicalMode || 'FILE'
}

/** 按操作方式统计真实题量；用于四种方式入口上的数字。 */
export function countByPracticalMode(rows) {
  const counts = { FILE: 0, PYTHON: 0, DATA_LAB: 0, FLOWCHART: 0 }
  for (const row of rows || []) {
    const key = practicalModeGroupKey(row)
    counts[key] = (counts[key] || 0) + 1
  }
  return counts
}

function normalize(text) {
  return String(text ?? '').replace(/\s+/g, ' ').trim()
}

/**
 * 卡片正文：Python 用题目标题，其余用题干。
 * 不新增标题字段，也不改写题目内容。
 */
export function cardTitle(row) {
  if (!row) return ''
  if (row.practicalMode === 'PYTHON' && row.programmingTitle) return normalize(row.programmingTitle)
  return normalize(row.questionContent) || (row.media?.stem?.length ? '图片选择题' : '未填写题目内容')
}

export function cardExcerpt(row, limit = 80) {
  const text = normalize(row?.questionContent || '')
  if (!text) return ''
  return text.length > limit ? `${text.slice(0, limit)}…` : text
}

export function choiceOptions(row) {
  return ['A', 'B', 'C', 'D']
    .map(key => ({ key, text: normalize(row?.[`option${key}`]) }))
    .map(item => (row?.media?.options?.[item.key] || []).length > 0 ? { ...item, hasImage: true } : item)
    .filter(item => item.text || item.hasImage)
}

/** 选择/判断的正确答案只做文字展示，避免在工作台上误触作答。 */
export function answerLabel(row) {
  if (row?.questionType === 'choice') return row.answer ? `答案 ${row.answer}` : ''
  if (row?.questionType === 'judgment') {
    if (row.answer === 'T') return '答案 正确'
    if (row.answer === 'F') return '答案 错误'
    return ''
  }
  return ''
}

export function gradeScopeText(row, gradeDict = [], semesterDict = []) {
  if (!supportsGradeScope(row)) return ''
  const grade = (gradeDict || []).find(item => String(item.value) === String(row.grade))?.label
  const semester = (semesterDict || []).find(item => item.value === row.semester)?.label
  const lesson = row.lessonNum ? `第 ${row.lessonNum} 课` : ''
  return [grade, semester, lesson].filter(Boolean).join(' · ')
}

/**
 * 卡片上可见的操作按钮。
 * 权限（按钮级）、归属（创建者或管理员）与服务端最终校验三层一致：
 * 有编辑权限不代表能删别人的题，普通教师只能管理自己创建的题目。
 */
export function cardActions(row, { permissions = [], userId = null, isAdmin = false } = {}) {
  const has = perm => permissions.includes('*:*:*') || permissions.includes(perm)
  const owned = isAdmin || String(row?.creatorId ?? '') === String(userId ?? '')
  const actions = []
  if (row?.questionType === 'practical') {
    if (row.practicalMode === 'PYTHON' && has('business:question:query')) actions.push('preview')
    // 流程图可走只读预览；文件作品只有转换出预览页才有预览入口，不造假入口。
    else if (row.practicalMode === 'FLOWCHART' || row.previewPath) actions.push('preview')
  } else if (has('business:question:query')) {
    actions.push('view')
  }
  if (owned && has('business:question:edit')) actions.push('edit')
  if (owned && has('business:question:remove')) actions.push('remove')
  return actions
}

/** 查询失败时显示“未知”，不能把失败当成 0 题。 */
export function totalText(state) {
  if (!state || state.error) return state?.loaded ? '题量未知' : ''
  return `${state.total} 题`
}

/** 加载更多后是否仍有剩余，按服务端 total 与已加载条数判断。 */
export function hasMoreAfter(state, loadedCount) {
  return (state?.total || 0) > loadedCount
}

/** 主操作：普通题查看详情，操作题按可用条件预览。 */
export function primaryAction(actions) {
  if (actions?.includes('view')) return 'view'
  return actions?.includes('preview') ? 'preview' : ''
}

/** 每个模块各自的追加状态，筛选变化时必须整体重置。 */
export function createModuleState(scopeKey) {
  return {
    scope: scopeOf(scopeKey),
    items: [],
    pageNum: 1,
    total: 0,
    loaded: false,
    loading: false,
    error: false,
    loadingMore: false,
    loadMoreError: false,
    hasMore: false,
    requestId: 0,
  }
}

/**
 * 合并“加载更多”结果：按 questionId 去重，防止刷新与追加交错时重复渲染同一题。
 * 返回的 hasMore 同时参考服务端 total 与去重后的实际条数，避免重复页导致死循环。
 */
export function mergeQuestionPage(current, next, total) {
  const merged = [...(current || [])]
  for (const row of next || []) {
    if (!merged.some(item => item.questionId === row.questionId)) merged.push(row)
  }
  return { items: merged, hasMore: Number(total || 0) > merged.length }
}
