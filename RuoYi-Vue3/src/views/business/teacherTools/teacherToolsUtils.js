// 教师工具页的纯函数：搜索、筛选、个人常用与最近打开。
// 这些函数不依赖 Vue，便于用 node --test 直接验证排序与去重口径。

export function splitTags(tags) {
  return String(tags || '')
    // 与后端保存时的分隔符保持一致：中英文逗号、顿号、分号都切分。
    .split(/[,，、;；]/)
    .map(item => item.trim())
    .filter(Boolean)
}

const CATEGORY_ICON_MAP = {
  'primary-school': 'education',
  'grade-seven': 'guide',
  'grade-eight': 'skill',
  'zj-discipline': 'international',
  'student-no-login': 'peoples',
  'ai-websites': 'message',
  'programming-learning': 'code',
  'office-materials': 'documentation',
  'teacher-websites': 'education',
  'homeroom-tools': 'people',
  'cross-subject': 'tree',
  'regional-platforms': 'build',
  recommended: 'star'
}

const VALID_CATEGORY_ICONS = new Set(Object.values(CATEGORY_ICON_MAP))

const TOOL_ICON_RULES = [
  { icon: 'pdf', pattern: /pdf/ },
  { icon: 'excel', pattern: /excel|表格|成绩|报表/ },
  { icon: 'email', pattern: /邮件|邮箱|email/ },
  { icon: 'input', pattern: /打字|键盘|录入|输入练习/ },
  { icon: 'language', pattern: /音乐|音频|语音|配音|朗读|英语|单词|k歌/ },
  { icon: 'tree', pattern: /流程图|流程绘制|血液循环/ },
  { icon: 'code', pattern: /python|编程|代码|算法|web前端|micro ?python|图形化/ },
  { icon: 'server', pattern: /物联网|网络搭建|数据中台|服务器|云服务|dify|api/ },
  { icon: 'icon', pattern: /图片|图像|绘图|图标|二维码|gif|视频|素材|设计/ },
  { icon: 'documentation', pattern: /文档|写作|演示文稿|ppt|模板|资料|备课/ },
  { icon: 'upload', pattern: /云盘|文件传输|上传|分享/ },
  { icon: 'chart', pattern: /评价|测评|监测|考试|学情|统计|质量/ },
  { icon: 'people', pattern: /班级|班主任|点名|学生管理|座位/ },
  { icon: 'education', pattern: /学习|课程|教程|教育|培训|研修|图书馆/ },
  { icon: 'build', pattern: /学校|校园|校本|象山|区域|教务/ },
  { icon: 'message', pattern: /ai|人工智能|智能|模型|deepseek|豆包|kimi|通义|问答|对话|机器人/ },
  { icon: 'search', pattern: /搜索|检索|导航/ }
]

const ICON_TONE_MAP = {
  pdf: 'rose', excel: 'green', email: 'cyan', input: 'orange', language: 'purple', tree: 'cyan',
  code: 'blue', server: 'green', icon: 'purple', documentation: 'blue', upload: 'cyan', chart: 'orange',
  people: 'rose', peoples: 'rose', education: 'blue', build: 'orange', message: 'purple', search: 'cyan',
  international: 'green', guide: 'cyan', skill: 'orange', star: 'orange', tool: 'blue', download: 'blue'
}

export const ACCESS_TYPE_LABELS = {
  DIRECT: '直接打开',
  LOGIN_REQUIRED: '需登录',
  INTRANET_ONLY: '限内网',
  DOWNLOAD: '下载工具'
}

export const SOURCE_TYPE_LABELS = {
  LOCAL_3005: '原3005站',
  LOCAL_80: '原80根站',
  ZJ_DISCIPLINE: '省学科平台',
  MANUAL: '平台自建'
}

export function resolveTeacherToolCategoryIcon(category) {
  const configured = String(category?.icon || '').trim()
  if (VALID_CATEGORY_ICONS.has(configured)) return configured
  return CATEGORY_ICON_MAP[category?.categoryCode] || 'tool'
}

export function resolveTeacherToolIcon(tool, categoryCode) {
  const searchable = [tool?.title, tool?.description, tool?.tags].join(' ').toLowerCase()
  const matched = TOOL_ICON_RULES.find(rule => rule.pattern.test(searchable))
  if (matched) return matched.icon
  if (tool?.accessType === 'DOWNLOAD') return 'download'
  return CATEGORY_ICON_MAP[categoryCode] || 'tool'
}

export function resolveTeacherToolTone(tool, categoryCode) {
  return ICON_TONE_MAP[resolveTeacherToolIcon(tool, categoryCode)] || 'blue'
}

/** 访问条件只按真实 accessType 说明，不推断免登录或免费。 */
export function resolveAccessLabel(tool) {
  return ACCESS_TYPE_LABELS[tool?.accessType] || ''
}

function normalizeText(value) {
  return String(value || '').toLowerCase().replace(/\s+/g, '')
}

// 目录侧 toolId 可能是数字或字符串：统一转成字符串再做 Map 键，避免 1 与 '1' 变成两条记录。
function normalizeKey(value) {
  if (value == null || value === '') return ''
  return String(value)
}

/**
 * 把目录接口返回的“工具×分类”行合并成每个工具一条。
 * 同一工具出现在推荐、多个分类时只保留一份，分类名与任务段合并去重，
 * 保证搜索结果与全部工具的数量是一致的“工具个数”而不是“出现次数”。
 */
export function mergeCatalogTools(catalog) {
  const byId = new Map()
  const add = (tool, extra) => {
    if (!tool || tool.toolId == null) return
    const key = normalizeKey(tool.toolId)
    const existing = byId.get(key)
    if (existing) {
      extra.categories.forEach(name => existing.categories.includes(name) || existing.categories.push(name))
      extra.categoryIds.forEach(id => existing.categoryIds.includes(id) || existing.categoryIds.push(id))
      // 推荐可能后于分类到达：补上真正的推荐顺序，不能沿用分类分支的兜底最大值。
      if (extra.recommended) {
        existing.recommended = true
        const order = Number(tool.recommendOrder)
        existing.recommendedOrder = Number.isFinite(order) ? order : 0
      }
      // 同一工具各分类行任务段本应一致；保留首次非空，避免末行覆盖。
      if (!existing.taskTags && extra.taskTags) existing.taskTags = extra.taskTags
      return
    }
    byId.set(key, {
      ...tool,
      // 任务编码统一小写：后端按小写存储与比对，前端保留大小写会导致筛选对不上。
      taskTags: String(extra.taskTags || tool.taskTags || '').toLowerCase(),
      recommended: extra.recommended,
      categories: [...extra.categories],
      categoryIds: [...extra.categoryIds],
      recommendedOrder: extra.recommended ? (Number(tool.recommendOrder) || 0) : Number.MAX_SAFE_INTEGER
    })
  }
  ;(catalog?.categories || []).forEach(category => {
    ;(category.tools || []).forEach(tool => add(tool, {
      // 后端目录行内附带 categoryNameText（全量归属，顿号分隔）；兼容旧字段名。
      // categoryNames 可能是旧数组：数组统一按元素处理，避免 String() 成逗号串后剩下逗号。
      // 空字符串别名在这里过滤：''.split('、') 会得到 ['']，不拦会污染合并后的类别名。
      categories: [tool.categoryNameText ?? tool.categoryNames ?? category.categoryName].flatMap(name => Array.isArray(name) ? name.map(item => String(item || '').trim()).filter(Boolean) : String(name || '').split('、').map(item => item.trim()).filter(Boolean)),
      categoryIds: [category.categoryId],
      taskTags: tool.taskTags
    }))
  })
  ;(catalog?.recommended || []).forEach(tool => add(tool, { categories: [], categoryIds: [], recommended: true }))
  const terms = new Map()
  ;(catalog?.terms || []).forEach(term => {
    if (term?.toolId == null) return
    // term 明细与工具侧 toolId 类型可能不一致：统一用字符串键，避免别名挂到空集合上。
    const termKey = normalizeKey(term.toolId)
    const termText = String(term.term || '').trim()
    if (!termText) return
    const list = terms.get(termKey) || []
    list.push(termText)
    terms.set(termKey, list)
  })
  const tools = [...byId.values()].map(tool => ({
    ...tool,
    aliasList: terms.get(normalizeKey(tool.toolId)) || [],
    // 任务编码统一小写，与后端存储口径一致，避免 “Material” 选不中 “material”。
    taskCodeList: splitTags(String(tool.taskTags || '').replace(/;/g, ',')).map(code => code.toLowerCase())
  }))
  // 稳定兜底顺序：推荐顺序 → 工具排序 → toolId，保证同等相关性下顺序固定。
  tools.sort((a, b) => (a.recommendedOrder - b.recommendedOrder)
    || ((Number(a.sortOrder) || 0) - (Number(b.sortOrder) || 0))
    || (a.toolId - b.toolId))
  return tools
}

/**
 * 关键词匹配分级：0 名称/别名，1 用途说明，2 标签/分类/任务段；无命中返回 -1。
 * 多关键词按“全部命中”处理，避免搜“图片 压缩”时把只有图片的工具也带出来。
 * 多个词命中不同层级时取最差的一级，保证排序反映最弱的那个命中。
 */
export function scoreTeacherTool(tool, keyword, taskNameMap) {
  const words = String(keyword || '').trim().toLowerCase().split(/\s+/).filter(Boolean)
  if (!words.length) return 0
  const title = normalizeText(tool?.title)
  const aliases = (tool?.aliasList || []).map(normalizeText)
  const description = normalizeText(tool?.description)
  const secondary = normalizeText([
    tool?.tags,
    (tool?.categories || []).join(' '),
    (tool?.taskCodeList || []).map(code => taskNameMap?.get(code) || '').join(' ')
  ].join(' '))
  let best = 0
  for (const word of words) {
    let tier = -1
    if (title.includes(word) || aliases.some(alias => alias.includes(word))) tier = 0
    else if (description.includes(word)) tier = 1
    else if (secondary.includes(word)) tier = 2
    if (tier < 0) return -1
    best = Math.max(best, tier)
  }
  return best
}

export function matchesTeacherTool(tool, keyword, taskNameMap) {
  return scoreTeacherTool(tool, keyword, taskNameMap) >= 0
}

export function buildTaskNameMap(tasks) {
  // 任务编码键统一小写，保证目录大小写漂移时任务名仍能命中。
  return new Map((tasks || []).map(task => [String(task.taskCode || '').toLowerCase(), String(task.taskName || '')]))
}

/** 把条件对象整理成实际生效的条件：空值不参与筛选，便于显示“已选条件”。 */
export function normalizeToolFilters(filters) {
  const active = {}
  // 任务编码统一小写，避免 “Material” 与 “material” 被当成两个条件。
  if (filters?.taskCode) active.taskCode = String(filters.taskCode).toLowerCase()
  if (filters?.categoryCode) active.categoryCode = filters.categoryCode
  if (filters?.sourceType) active.sourceType = filters.sourceType
  if (filters?.accessType) active.accessType = filters.accessType
  if (String(filters?.keyword || '').trim()) active.keyword = String(filters.keyword).trim()
  return active
}

export function hasActiveFilters(filters) {
  return Object.keys(normalizeToolFilters(filters)).length > 0
}

/**
 * 统一筛选：关键词、教学任务、分类、来源、访问条件共同生效。
 * 返回统一的工具数组（已按 toolId 去重），不含分类分组。
 */
export function filterTeacherTools(catalog, filters) {
  const active = normalizeToolFilters(filters)
  const taskNameMap = buildTaskNameMap(catalog?.tasks)
  // 目录 categoryId 可能是数字，工具侧合并后也可能混入字符串：统一转字符串再比，避免分类筛选对不上。
  const categoryCodeById = new Map((catalog?.categories || []).map(category => [String(category.categoryId), category.categoryCode]))
  const scored = []
  mergeCatalogTools(catalog).forEach(tool => {
    if (active.taskCode && !(tool.taskCodeList || []).includes(active.taskCode)) return
    if (active.categoryCode && !(tool.categoryIds || []).some(id => categoryCodeById.get(String(id)) === active.categoryCode)) return
    if (active.sourceType && tool.sourceType !== active.sourceType) return
    if (active.accessType && (tool.accessType || 'DIRECT') !== active.accessType) return
    const score = scoreTeacherTool(tool, active.keyword, taskNameMap)
    if (score < 0) return
    scored.push({ tool, score })
  })
  scored.sort((a, b) => (a.score - b.score) || (a.tool.recommendedOrder - b.tool.recommendedOrder)
    || ((Number(a.tool.sortOrder) || 0) - (Number(b.tool.sortOrder) || 0)) || (a.tool.toolId - b.tool.toolId))
  return scored.map(item => item.tool)
}

/** 兼容旧调用：只按关键词过滤目录，结果同样按 toolId 去重。 */
export function filterTeacherToolCatalog(catalog, keyword) {
  return { tools: filterTeacherTools(catalog, { keyword }) }
}

/** 分类内展示：搜索或主动展开时显示全部，否则只显示预览数量。 */
export function categoryToolsForDisplay(category, keyword, expanded) {
  const tools = category?.tools || []
  const showAll = Boolean(String(keyword || '').trim())
    || category?.sectionLevel === 'PRIMARY'
    || category?.defaultExpanded === 'Y'
    || expanded
  return showAll ? tools : tools.slice(0, Math.max(1, Number(category?.previewLimit) || 4))
}

export function isSafeHttpUrl(url) {
  try {
    const parsed = new URL(url)
    return ['http:', 'https:'].includes(parsed.protocol) && !parsed.username && !parsed.password
  } catch {
    return false
  }
}

export function openTeacherTool(tool, openWindow) {
  if (!isSafeHttpUrl(tool?.url) || typeof openWindow !== 'function') return false
  let opened = null
  try {
    // 不能直接带 noopener 打开目标地址：按规范该特性会让 window.open 返回 null，
    // 成功的打开会被误判成拦截，最近打开也就永远记不上。
    // 先同步开同源空白页，拿到的句柄才是“是否真的被拦截”的唯一可靠依据。
    opened = openWindow('', '_blank')
  } catch {
    return false
  }
  // 弹窗被拦截时返回 null：调用方不应记入最近打开。
  if (!opened) return false
  try {
    opened.opener = null
    // 空白页与本站同源，可在其中放一个带 noopener/noreferrer 的链接完成跳转：
    // 目标页既拿不到 window.opener，也不会带上来源页地址。
    const doc = opened.document
    if (!doc?.createElement || !doc.body) throw new Error('window not usable')
    const link = doc.createElement('a')
    link.href = tool.url
    link.target = '_self'
    link.rel = 'noopener noreferrer'
    doc.body.appendChild(link)
    link.click()
    return true
  } catch {
    // 句柄不可写说明窗口已被关闭或不可用，清掉空白标签并如实返回失败。
    try { opened.close() } catch { /* 窗口已不存在，无需再关闭 */ }
    return false
  }
}

const FAVORITES_PREFIX = 'teacher-tools-favorites:'
const RECENTS_PREFIX = 'teacher-tools-recents:'
const FAVORITES_LIMIT = 200
const RECENTS_LIMIT = 6

// 个人常用与最近打开只存本机浏览器，按账号隔离；没有跨设备偏好表，页面必须如实标注。
function storageKey(prefix, userId) {
  return `${prefix}${userId}`
}

function readIds(prefix, userId, storage) {
  if (!userId || !storage) return []
  try {
    const parsed = JSON.parse(storage.getItem(storageKey(prefix, userId)) || '[]')
    // 目录侧 toolId 可能是数字或字符串，统一转成数字后再比较，避免收藏与目录对不上。
    return Array.isArray(parsed) ? parsed.map(Number).filter(value => Number.isInteger(value) && value > 0) : []
  } catch {
    return []
  }
}

function writeIds(prefix, userId, ids, storage) {
  if (!userId || !storage) return
  try {
    storage.setItem(storageKey(prefix, userId), JSON.stringify(ids))
  } catch {
    // 隐私模式或配额满时静默放弃，常用功能不是阻塞任务。
  }
}

// 目录侧 toolId 可能是数字或字符串，这里统一转成正整数再比较，避免字符串与数字混用导致收藏回显丢失或重复收藏。
function normalizeId(value) {
  const id = Number(value)
  return Number.isInteger(id) && id > 0 ? id : 0
}

export function readFavoriteIds(userId, storage = globalThis.localStorage) {
  return readIds(FAVORITES_PREFIX, userId, storage)
}

/** 返回新的常用列表与动作结果，调用方据此提示“已加入 / 已移除”。 */
export function toggleFavoriteId(userId, toolId, storage = globalThis.localStorage) {
  // 未登录时不产生幻影收藏，调用方据此提示稍后再试。
  if (!userId || !storage) return { ids: [], added: false }
  const normalizedToolId = normalizeId(toolId)
  if (!normalizedToolId) return { ids: readIds(FAVORITES_PREFIX, userId, storage), added: false }
  const ids = readIds(FAVORITES_PREFIX, userId, storage)
  const exists = ids.includes(normalizedToolId)
  const next = exists ? ids.filter(id => id !== normalizedToolId) : [normalizedToolId, ...ids].slice(0, FAVORITES_LIMIT)
  writeIds(FAVORITES_PREFIX, userId, next, storage)
  return { ids: next, added: !exists }
}

export function readRecentIds(userId, storage = globalThis.localStorage) {
  return readIds(RECENTS_PREFIX, userId, storage)
}

/** 最近打开只记录“从本平台发起打开”，不推断外部网站是否真的被使用。 */
export function pushRecentId(userId, toolId, storage = globalThis.localStorage) {
  if (!userId || !storage) return []
  const normalizedToolId = normalizeId(toolId)
  if (!normalizedToolId) return readIds(RECENTS_PREFIX, userId, storage)
  const ids = readIds(RECENTS_PREFIX, userId, storage).filter(id => id !== normalizedToolId)
  const next = [normalizedToolId, ...ids].slice(0, RECENTS_LIMIT)
  writeIds(RECENTS_PREFIX, userId, next, storage)
  return next
}

/** 收藏与最近按 id 回查目录；下架、删除或无权的工具不在目录中，自然不再展示。 */
export function pickToolsByIds(tools, ids) {
  const map = new Map((tools || []).map(tool => [Number(tool.toolId), tool]))
  return (ids || []).map(id => map.get(Number(id))).filter(Boolean)
}
