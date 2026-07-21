const DEFAULT_FORM_CONFIG = Object.freeze({
  modelName: 'formData',
  refName: 'vForm',
  rulesName: 'rules',
  labelWidth: 110,
  labelPosition: 'top',
  size: '',
  labelAlign: 'label-left-align',
  cssCode: '',
  customClass: '',
  functions: '',
  layoutType: 'PC',
  jsonVersion: 3,
  onFormCreated: '',
  onFormMounted: '',
  onFormDataChange: ''
})

export const BEGINNER_MODULES = Object.freeze([
  { type: 'learningObjective', label: '学习目标', description: '让学生明确本课要学会什么', icon: 'Aim' },
  { type: 'preClassCheck', label: '课前检测', description: '了解学生已有基础', icon: 'DocumentChecked' },
  { type: 'knowledgeExplanation', label: '知识讲解', description: '呈现概念、步骤或操作提示', icon: 'Reading' },
  { type: 'singleChoice', label: '单选题', description: '设置一个正确答案', icon: 'CircleCheck' },
  { type: 'multipleChoice', label: '多选题', description: '设置一个或多个正确答案', icon: 'Select' },
  { type: 'shortAnswer', label: '填空或简答', description: '收集文字回答', icon: 'EditPen' },
  { type: 'fileSubmission', label: '文件或作品提交', description: '提交课堂作品或学习成果', icon: 'UploadFilled' },
  { type: 'selfAssessment', label: '学生自评', description: '学生对学习效果作出评价', icon: 'Star' },
  { type: 'reflection', label: '课堂反思', description: '引导学生总结收获与困惑', icon: 'ChatLineSquare' }
])

/** 默认主路径：空白画布，教师从常用模块库自行添加（P0-3） */
export const DEFAULT_STRUCTURE_PRESET_ID = 'blank'

/**
 * 结构预设：blank 为主路径；其余仅数据兼容旧草稿/历史数据，界面不再推荐。
 */
export const STRUCTURE_PRESETS = Object.freeze([
  { id: 'blank', name: '空白画布', description: '从空白开始，按需添加常用教学模块', modules: [] },
  // 以下为历史结构，仅 createBeginnerFormJson(presetId) 兼容，不在主界面展示
  { id: 'before-class', name: '课前预习单', description: '目标引领，检测已有基础', modules: ['learningObjective', 'preClassCheck', 'shortAnswer'] },
  { id: 'class-task', name: '课堂任务单', description: '讲练结合，及时自评', modules: ['learningObjective', 'knowledgeExplanation', 'singleChoice', 'shortAnswer', 'selfAssessment', 'reflection'] },
  { id: 'project-practice', name: '项目实践单', description: '任务驱动，提交项目成果', modules: ['learningObjective', 'knowledgeExplanation', 'fileSubmission', 'selfAssessment', 'reflection'] },
  { id: 'after-class', name: '课后复习单', description: '巩固练习，回顾学习过程', modules: ['learningObjective', 'singleChoice', 'multipleChoice', 'shortAnswer', 'reflection'] },
  { id: 'group-cooperation', name: '小组合作任务单', description: '明确分工，记录合作成果', modules: ['learningObjective', 'knowledgeExplanation', 'shortAnswer', 'fileSubmission', 'selfAssessment', 'reflection'] },
  { id: 'it-operation', name: '信息科技操作任务单', description: '突出操作步骤和作品提交', modules: ['learningObjective', 'knowledgeExplanation', 'preClassCheck', 'fileSubmission', 'selfAssessment', 'reflection'] }
])

/** 主界面不再推荐的历史结构（兼容数据仍可通过 id 生成） */
export const LEGACY_STRUCTURE_PRESET_IDS = Object.freeze(
  STRUCTURE_PRESETS.filter(item => item.id !== DEFAULT_STRUCTURE_PRESET_ID).map(item => item.id)
)

const MODULE_DEFAULTS = Object.freeze({
  learningObjective: { type: 'static-text', title: '学习目标', content: '完成本课学习后，我能够……' },
  preClassCheck: { type: 'textarea', title: '课前检测', placeholder: '请写下你的回答', required: true },
  knowledgeExplanation: { type: 'static-text', title: '知识讲解', content: '请在这里填写本课的关键知识、操作步骤或学习提示。' },
  singleChoice: { type: 'radio', title: '单选题', required: true, score: 10 },
  multipleChoice: { type: 'checkbox', title: '多选题', required: true, score: 10 },
  shortAnswer: { type: 'textarea', title: '填空或简答', placeholder: '请写下你的回答', required: true, score: 10 },
  fileSubmission: { type: 'file-upload', title: '文件或作品提交', required: true },
  selfAssessment: { type: 'rate', title: '学生自评', required: false },
  reflection: { type: 'textarea', title: '课堂反思', placeholder: '这节课我最大的收获是……', required: false }
})

function createId(prefix) {
  const random = Math.random().toString(36).slice(2, 9)
  return `${prefix}-${Date.now().toString(36)}-${random}`
}

function createName(type) {
  return `bg_${type}_${Math.random().toString(36).slice(2, 8)}`
}

function createCommonOptions(moduleType, values) {
  return {
    name: createName(moduleType),
    label: values.title,
    labelAlign: '',
    defaultValue: null,
    columnWidth: '200px',
    size: '',
    labelWidth: null,
    labelHidden: false,
    disabled: false,
    hidden: false,
    required: Boolean(values.required),
    requiredHint: values.required ? '请完成此项内容' : '',
    validation: '',
    validationHint: '',
    customClass: '',
    onCreated: '',
    onMounted: '',
    onChange: '',
    onValidate: '',
    beginnerModuleType: moduleType,
    beginnerExplanation: values.explanation || ''
  }
}

function createChoiceOptions() {
  return ['A', 'B', 'C', 'D'].map(letter => ({ label: `选项 ${letter}`, value: letter }))
}

export function createModuleWidget(moduleType, overrides = {}) {
  const defaults = MODULE_DEFAULTS[moduleType]
  if (!defaults) throw new Error(`不支持的教学模块：${moduleType}`)
  const values = { ...defaults, ...overrides }
  const options = createCommonOptions(moduleType, values)
  const widget = {
    id: createId(moduleType),
    type: values.type,
    icon: values.type,
    formItemFlag: values.type !== 'static-text',
    options
  }

  if (values.type === 'static-text') {
    options.labelHidden = true
    options.beginnerContent = values.content || ''
    options.textContent = `${values.title}：${values.content || ''}`
    options.fontSize = '16px'
    options.fontWeight = 'normal'
    options.textAlign = 'left'
  } else if (values.type === 'textarea') {
    options.placeholder = values.placeholder || '请写下你的回答'
    options.rows = 4
    options.autoFullWidth = true
  } else if (values.type === 'radio' || values.type === 'checkbox') {
    options.optionItems = Array.isArray(values.options) && values.options.length
      ? values.options.map((item, index) => ({
          label: typeof item === 'string' ? item : item.label,
          value: typeof item === 'string'
            ? String.fromCharCode(65 + index)
            : String(item.value ?? String.fromCharCode(65 + index))
        }))
      : createChoiceOptions()
    options.displayStyle = 'inline'
    options.optionItemsChecked = []
  } else if (values.type === 'file-upload') {
    options.uploadURL = ''
    options.uploadTip = '支持上传课堂作品或学习成果'
    options.withCredentials = true
    options.multipleSelect = false
    options.showFileList = true
    options.limit = 1
    options.fileMaxSize = 20
    options.fileTypes = []
  } else if (values.type === 'rate') {
    options.max = 5
    options.lowThreshold = 2
    options.highThreshold = 4
    options.allowHalf = false
    options.showText = false
    options.showScore = true
  }

  if (['preClassCheck', 'singleChoice', 'multipleChoice', 'shortAnswer'].includes(moduleType)) {
    const answer = values.correctAnswer ?? (values.type === 'checkbox' ? [] : '')
    widget.scoring = {
      score: Number(values.score ?? 10),
      answer: Array.isArray(answer) ? answer.join(',') : String(answer),
      type: ['preClassCheck', 'shortAnswer'].includes(moduleType) ? 'manual' : 'exact'
    }
  }

  return widget
}

export function createHomeTab(widgets = []) {
  return {
    id: createId('tab'),
    type: 'tab',
    category: 'container',
    icon: 'tab',
    displayType: 'border-card',
    internal: true,
    tabs: [{
      id: createId('tab-pane'),
      type: 'tab-pane',
      category: 'container',
      icon: 'tab-pane',
      internal: true,
      widgetList: widgets,
      options: {
        name: 'tab1',
        label: '学习任务',
        hidden: false,
        active: true,
        disabled: false,
        customClass: ''
      }
    }],
    options: { name: 'HomeTab', hidden: false, customClass: '' }
  }
}

/**
 * 生成新手模式 formJson。
 * 默认 blank：仅 HomeTab 空画布；历史 presetId 仍可生成对应模块列表。
 */
export function createBeginnerFormJson(presetId = DEFAULT_STRUCTURE_PRESET_ID, metadata = {}) {
  const preset = STRUCTURE_PRESETS.find(item => item.id === presetId)
    || STRUCTURE_PRESETS.find(item => item.id === DEFAULT_STRUCTURE_PRESET_ID)
  const widgets = (preset?.modules || []).map(moduleType => {
    const overrides = {}
    if (moduleType === 'learningObjective' && metadata.topic) {
      overrides.content = `围绕“${metadata.topic}”，明确本课学习目标。`
    }
    if (moduleType === 'knowledgeExplanation' && metadata.topic) {
      overrides.content = `请补充“${metadata.topic}”的关键知识与操作步骤。`
    }
    return createModuleWidget(moduleType, overrides)
  })

  return {
    widgetList: [createHomeTab(widgets)],
    formConfig: {
      ...DEFAULT_FORM_CONFIG,
      beginnerEstimatedMinutes: Number(metadata.estimatedMinutes || 0),
      beginnerStructurePreset: preset?.id || DEFAULT_STRUCTURE_PRESET_ID
    }
  }
}

export function getModuleDefinition(moduleType) {
  return BEGINNER_MODULES.find(item => item.type === moduleType) || null
}
