import { createHomeTab, createModuleWidget } from './presetFactories.js'

const LEGACY_TYPE_MAP = Object.freeze({
  radio: 'singleChoice',
  checkbox: 'multipleChoice',
  textarea: 'shortAnswer',
  'file-upload': 'fileSubmission',
  'picture-upload': 'fileSubmission',
  rate: 'selfAssessment'
})

function clone(value) {
  return value == null ? value : JSON.parse(JSON.stringify(value))
}

const STRUCTURAL_WIDGET_TYPES = new Set([
  'grid',
  'grid-col',
  'table',
  'table-cell',
  'tab',
  'tab-pane',
  'card',
  'sub-form'
])

export function parseFormJsonSafely(value) {
  if (value && typeof value === 'object') {
    return { ok: true, formJson: clone(value), raw: JSON.stringify(value), reason: '' }
  }
  const raw = typeof value === 'string' ? value : ''
  if (!raw.trim()) return { ok: true, formJson: null, raw, reason: 'empty' }
  try {
    const parsed = JSON.parse(raw)
    if (!parsed || !Array.isArray(parsed.widgetList)) {
      return { ok: false, formJson: null, raw, reason: 'invalid-structure' }
    }
    return { ok: true, formJson: parsed, raw, reason: '' }
  } catch {
    return { ok: false, formJson: null, raw, reason: 'invalid-json' }
  }
}

export function hasRenderableWidgets(formJson) {
  const visited = new Set()

  function inspect(value) {
    if (!value || typeof value !== 'object' || visited.has(value)) return false
    visited.add(value)
    if (Array.isArray(value)) return value.some(inspect)

    if (typeof value.type === 'string') {
      const structural = value.category === 'container' || STRUCTURAL_WIDGET_TYPES.has(value.type)
      if (!structural) return true
    }

    return ['widgetList', 'tabs', 'rows', 'cols'].some(key => inspect(value[key]))
  }

  return inspect(formJson?.widgetList)
}

export function getEditableWidgetList(formJson, createIfMissing = false) {
  if (!formJson || typeof formJson !== 'object') return []
  if (!Array.isArray(formJson.widgetList)) {
    if (!createIfMissing) return []
    formJson.widgetList = []
  }
  const homeTab = formJson.widgetList.find(widget => (
    widget?.type === 'tab' && widget?.options?.name === 'HomeTab'
  ))
  if (!homeTab) return formJson.widgetList
  if (!Array.isArray(homeTab.tabs)) homeTab.tabs = []
  if (!homeTab.tabs[0] && createIfMissing) homeTab.tabs.push(createHomeTab().tabs[0])
  if (!homeTab.tabs[0]) return []
  if (!Array.isArray(homeTab.tabs[0].widgetList)) homeTab.tabs[0].widgetList = []
  return homeTab.tabs[0].widgetList
}

function inferModuleType(widget) {
  if (widget?.options?.beginnerModuleType) return widget.options.beginnerModuleType
  if (widget?.type === 'static-text') return 'knowledgeExplanation'
  return LEGACY_TYPE_MAP[widget?.type] || null
}

function toOptionList(items) {
  return Array.isArray(items)
    ? items.map((item, index) => ({
        label: typeof item === 'string' ? item : (item?.label || `选项 ${index + 1}`),
        value: typeof item === 'string'
          ? String.fromCharCode(65 + index)
          : String(item?.value ?? String.fromCharCode(65 + index))
      }))
    : []
}

function getWidgetLabel(widget) {
  return widget?.label || widget?.options?.label || widget?.title
    || widget?.id || widget?.name || widget?.guid || ''
}

function readWidget(widget, scoringSnapshot = {}) {
  const moduleType = inferModuleType(widget)
  if (!moduleType) {
    return {
      id: widget?.id || widget?.options?.name || `advanced-${Math.random().toString(36).slice(2)}`,
      advanced: true,
      moduleType: 'advanced',
      title: widget?.options?.label || '高级组件',
      rawWidget: clone(widget)
    }
  }
  const options = widget.options || {}
  const snapshotScoring = scoringSnapshot[getWidgetLabel(widget)]
  const hasWidgetScoring = widget.scoring && typeof widget.scoring === 'object'
    && Object.prototype.hasOwnProperty.call(widget.scoring, 'score')
  const scoring = hasWidgetScoring
    ? widget.scoring
    : (snapshotScoring && typeof snapshotScoring === 'object' ? snapshotScoring : {})
  return {
    id: widget.id || options.name,
    advanced: false,
    moduleType,
    title: options.label || '未命名教学模块',
    content: options.beginnerContent ?? options.textContent ?? '',
    placeholder: options.placeholder || '',
    required: Boolean(options.required),
    options: toOptionList(options.optionItems),
    correctAnswer: scoring.answer ?? '',
    score: Number(scoring.score || 0),
    scoringType: scoring.type || '',
    explanation: options.beginnerExplanation ?? scoring.explanation ?? '',
    rawWidget: clone(widget)
  }
}

export function readBeginnerDocument(formJson) {
  const scoringSnapshot = formJson?._scoringConfig || {}
  const items = getEditableWidgetList(formJson).map(widget => readWidget(widget, scoringSnapshot))
  return {
    items,
    advancedCount: items.filter(item => item.advanced).length
  }
}

function writeWidget(item) {
  if (item.advanced) return clone(item.rawWidget)
  const widget = clone(item.rawWidget) || createModuleWidget(item.moduleType)
  widget.options ||= {}
  widget.options.beginnerModuleType = item.moduleType
  widget.options.label = item.title || '未命名教学模块'
  widget.options.required = Boolean(item.required)
  widget.options.beginnerExplanation = item.explanation || ''

  if (widget.type === 'static-text') {
    widget.options.beginnerContent = item.content || ''
    widget.options.textContent = `${widget.options.label}：${item.content || ''}`
  }
  if (widget.type === 'textarea') widget.options.placeholder = item.placeholder || '请写下你的回答'
  if (widget.type === 'radio' || widget.type === 'checkbox') {
    widget.options.optionItems = toOptionList(item.options)
  }
  if (['radio', 'checkbox', 'textarea'].includes(widget.type)) {
    const answer = Array.isArray(item.correctAnswer) ? item.correctAnswer.join(',') : (item.correctAnswer ?? '')
    const defaultScoringType = ['preClassCheck', 'shortAnswer'].includes(item.moduleType)
      ? 'manual'
      : 'exact'
    widget.scoring = {
      score: Number(item.score || 0),
      answer: String(answer),
      type: item.scoringType || widget.scoring?.type || defaultScoringType,
      explanation: item.explanation || ''
    }
  }
  return widget
}

export function writeBeginnerDocument(baseFormJson, document) {
  const formJson = clone(baseFormJson) || { widgetList: [createHomeTab([])] }
  const widgets = getEditableWidgetList(formJson, true)
  const previousWidgets = [...widgets]
  const items = document?.items || []
  const nextWidgets = items.map(writeWidget)
  const scoringSnapshot = formJson._scoringConfig && typeof formJson._scoringConfig === 'object'
    ? clone(formJson._scoringConfig)
    : {}

  // 只替换新手组件的旧快照，无法映射的高级组件仍按原数据往返。
  previousWidgets.forEach(widget => {
    if (inferModuleType(widget)) delete scoringSnapshot[getWidgetLabel(widget)]
  })
  nextWidgets.forEach((widget, index) => {
    if (items[index]?.advanced || !widget?.scoring) return
    scoringSnapshot[getWidgetLabel(widget)] = clone(widget.scoring)
  })

  widgets.splice(0, widgets.length, ...nextWidgets)
  formJson._scoringConfig = scoringSnapshot
  return formJson
}

export function appendBeginnerModule(baseFormJson, document, moduleType) {
  const nextDocument = clone(document) || { items: [], advancedCount: 0 }
  nextDocument.items ||= []
  nextDocument.items.push(readWidget(createModuleWidget(moduleType)))
  return {
    document: nextDocument,
    formJson: writeBeginnerDocument(baseFormJson, nextDocument)
  }
}

export function splitSuggestionLines(value) {
  return String(value || '')
    .split(/\r?\n/)
    .map(line => line.trim().replace(/^(?:[-*\u2022]\s*|\d+[.、)]\s*)/, '').trim())
    .filter(Boolean)
}

export function createBeginnerItemsFromLines(moduleType, value) {
  return splitSuggestionLines(value).map(title => ({
    ...readWidget(createModuleWidget(moduleType)),
    title
  }))
}

const PREVIEW_CODE_KEYS = new Set([
  'onCreated',
  'onMounted',
  'onInput',
  'onChange',
  'onFocus',
  'onBlur',
  'onValidate',
  'onClick',
  'onRemoteQuery',
  'onBeforeUpload',
  'onUploadSuccess',
  'onUploadError',
  'onFileRemove',
  'remoteURL',
  'dataSource',
  'dataSourceName',
  'optionDataSource'
])

export function createSafePreviewFormJson(formJson) {
  const preview = clone(formJson)
  if (!preview) return null
  preview.formConfig ||= {}
  preview.formConfig.cssCode = ''
  preview.formConfig.functions = ''
  preview.formConfig.onFormCreated = ''
  preview.formConfig.onFormMounted = ''
  preview.formConfig.onFormDataChange = ''
  delete preview._scoringConfig

  const visited = new Set()
  function sanitize(value) {
    if (!value || typeof value !== 'object' || visited.has(value)) return
    visited.add(value)
    if (Array.isArray(value)) {
      value.forEach(sanitize)
      return
    }
    delete value.scoring
    if (value.type === 'html-text') {
      value.type = 'static-text'
      value.icon = 'static-text'
      value.options ||= {}
      value.options.textContent = '该高级内容已在预览中隐藏，请在高级模式中查看。'
      delete value.options.htmlContent
    }
    for (const key of Object.keys(value)) {
      if (PREVIEW_CODE_KEYS.has(key) || /^on[A-Z]/.test(key)) value[key] = ''
      else if (key === 'customClass') value[key] = ''
      else if (key === 'uploadURL') value[key] = ''
      else if (key === 'withCredentials') value[key] = false
      else if (key === 'beginnerExplanation') delete value[key]
      else sanitize(value[key])
    }
  }
  sanitize(preview)
  return preview
}
