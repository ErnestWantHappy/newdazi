export function splitTags(tags) {
  return String(tags || '')
    .split(/[,，、]/)
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

export function matchesTeacherTool(tool, keyword) {
  const normalized = String(keyword || '').trim().toLowerCase()
  if (!normalized) return true
  return [tool?.title, tool?.description, tool?.tags]
    .some(value => String(value || '').toLowerCase().includes(normalized))
}

export function filterTeacherToolCatalog(catalog, keyword) {
  const recommended = (catalog?.recommended || []).filter(tool => matchesTeacherTool(tool, keyword))
  const categories = (catalog?.categories || [])
    .map(category => ({
      ...category,
      tools: (category.tools || []).filter(tool => matchesTeacherTool(tool, keyword))
    }))
    .filter(category => category.tools.length > 0)
  return { recommended, categories }
}

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
  const opened = openWindow(tool.url, '_blank', 'noopener,noreferrer')
  if (opened) opened.opener = null
  return true
}
