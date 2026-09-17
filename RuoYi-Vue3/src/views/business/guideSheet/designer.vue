<template>
  <div class="guide-sheet-designer-shell">
  <BeginnerGuideWizard
    v-show="editorMode === 'beginner'"
    :form="form"
    :form-json="rawFormJson"
    :invalid-raw="invalidFormJson"
    :grade-options="biz_grade"
    :semester-options="biz_semester"
    :saving="saving"
    :loading="loadingSheet"
    :ai-available="aiConfigured"
    :draft-key="draftKey"
    @update-metadata="handleBeginnerMetadata"
    @update-form-json="handleBeginnerFormJson"
    @save="handleBeginnerSave"
    @advanced="enterAdvancedMode"
    @back="goBack"
  />
  <AdvancedModeBridge v-if="editorMode === 'advanced'" @return-beginner="returnToBeginnerMode">
  <div class="app-container">
    <!-- 模板设置只描述模板本身，课程和班级统一由课程设计器管理。 -->
    <el-card class="filter-card metadata-card" shadow="never">
      <div class="metadata-heading">
        <div>
          <span class="metadata-kicker">模板信息</span>
          <strong>{{ form.sheetId ? '编辑导学单模板' : '新建导学单模板' }}</strong>
        </div>
        <el-tag type="info" effect="plain">模板与课程投放已解耦</el-tag>
      </div>
      <div class="toolbar-row">
        <div class="toolbar-section toolbar-title">
          <span class="toolbar-label">导学单标题</span>
          <el-input v-model="form.sheetTitle" maxlength="100" show-word-limit placeholder="请输入导学单标题" size="large" @input="markDirty" />
        </div>
        <div class="toolbar-section toolbar-compact">
          <span class="toolbar-label">年级</span>
          <el-select v-model="form.grade" placeholder="年级" style="width: 120px" @change="markDirty">
            <el-option v-for="dict in biz_grade" :key="dict.value" :label="dict.label" :value="Number(dict.value)" />
          </el-select>
        </div>
        <div class="toolbar-section toolbar-compact">
          <span class="toolbar-label">学期</span>
          <el-select v-model="form.semester" placeholder="学期" style="width: 120px" @change="markDirty">
            <el-option v-for="dict in biz_semester" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </div>
        <div class="toolbar-section toolbar-compact">
          <span class="toolbar-label">第几课</span>
          <el-input-number v-model="form.lessonNum" :min="1" :max="30" controls-position="right" style="width: 110px" @change="markDirty" />
        </div>
        <div class="toolbar-section toolbar-public">
          <span class="toolbar-label">可见范围</span>
          <el-radio-group v-model="form.isPublic" size="default" @change="markDirty">
            <el-radio-button value="Y">公共导学单</el-radio-button>
            <el-radio-button value="N">我的私有</el-radio-button>
          </el-radio-group>
        </div>
        <div class="toolbar-section toolbar-actions">
          <el-button type="primary" icon="Check" :loading="saving" @click="handleSave">保存模板</el-button>
          <el-button icon="View" @click="openPreview">预览</el-button>
          <el-button icon="ArrowLeft" @click="goBack" :loading="saving">返回列表</el-button>
        </div>
      </div>
    </el-card>

    <!-- 中块：VForm3 表单设计器 -->
    <el-card class="designer-card" shadow="never">
      <v-form-designer
        ref="designerRef"
        :designer-config="designerConfig"
        :banned-widgets="bannedWidgets"
        @form-json-change="onFormJsonChange"
      />
    </el-card>

    <!-- 下块：评分配置 -->
    <el-card class="scoring-card" shadow="never">
      <template #header>
        <div class="scoring-header">
          <span class="scoring-title">评分配置</span>
          <div class="scoring-header-right">
            <el-button size="small" icon="Refresh" text @click="refreshScoredFields">刷新字段</el-button>
            <el-tag v-if="scoringEnabled" :type="aiConfigured ? 'success' : 'warning'" effect="plain">
              {{ aiConfigured ? 'AI评分已配置' : 'AI评分未配置，将转人工处理' }}
            </el-tag>
            <el-switch v-model="scoringEnabled" active-text="启用自动评分" />
          </div>
        </div>
      </template>
      <template v-if="scoringEnabled">
        <el-table v-if="scoredFields.length > 0" :data="scoredFields" border stripe size="small" max-height="360">
          <el-table-column prop="title" label="字段名称" width="160" />
          <el-table-column label="字段类型" width="100">
            <template #default="{ row }">
              <el-tag size="small" type="info">{{ widgetTypeLabel(row.type) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="分值" width="110">
            <template #default="{ row }">
              <div class="score-input-row">
                <el-button size="small" circle :icon="Minus" @click="decreaseScore(row.id)" />
                <el-input-number
                  v-model="scoringConfig[row.id].score"
                  :min="0" :max="100" :step="1"
                  size="small" :controls="false"
                  style="width: 60px"
                />
                <el-button size="small" circle :icon="Plus" @click="increaseScore(row.id)" />
              </div>
            </template>
          </el-table-column>
          <el-table-column label="参考答案" min-width="320">
            <template #default="{ row }">
              <!-- 单选 / 下拉选择：字母按钮组 -->
              <template v-if="row.type === 'radio' || row.type === 'select'">
                <div class="answer-buttons">
                  <el-button
                    v-if="row.type === 'select'"
                    size="small" round
                    :type="!scoringConfig[row.id]?.answer ? 'primary' : ''"

                    @click="scoringConfig[row.id].answer = ''"
                  >未设置</el-button>
                  <el-button
                    v-for="(opt, idx) in getFieldOptions(row.id, row.type)"
                    :key="opt.value"
                    :type="scoringConfig[row.id]?.answer === opt.value ? 'primary' : ''"
                    size="small" round

                    @click="scoringConfig[row.id].answer = opt.value"
                  >{{ indexToLetter(idx) }}.{{ opt.label }}</el-button>
                </div>
              </template>

              <!-- 多选 checkbox：字母按钮组（可多选） -->
              <template v-else-if="row.type === 'checkbox'">
                <div class="answer-buttons">
                  <el-button
                    v-for="(opt, idx) in getCheckboxOptions(row)"
                    :key="opt.value"
                    :type="isCheckboxChecked(row.id, opt.value) ? 'primary' : ''"
                    size="small" round

                    @click="toggleCheckboxAnswer(row.id, opt.value, !isCheckboxChecked(row.id, opt.value))"
                  >{{ indexToLetter(idx) }}.{{ opt.label }}</el-button>
                </div>
              </template>

              <!-- 级联选择 cascader：扁平化按钮组 -->
              <template v-else-if="row.type === 'cascader'">
                <div class="answer-buttons">
                  <el-button
                    size="small" round
                    :type="!scoringConfig[row.id]?._cascaderPath?.length ? 'primary' : ''"

                    @click="scoringConfig[row.id]._cascaderPath = ''"
                  >未设置</el-button>
                  <el-button
                    v-for="opt in flattenCascaderOptions(getCascaderOptions(row.id))"
                    :key="opt.path.join('-')"
                    :type="arraysEqual(scoringConfig[row.id]?._cascaderPath, opt.path) ? 'primary' : ''"
                    size="small" round

                    @click="scoringConfig[row.id]._cascaderPath = opt.path"
                  >{{ opt.label }}</el-button>
                </div>
              </template>

              <!-- 开关 switch -->
              <template v-else-if="row.type === 'switch'">
                <div class="answer-buttons">
                  <el-button
                    :type="scoringConfig[row.id]?.answer === 'true' ? 'success' : ''"
                    size="small" round

                    @click="scoringConfig[row.id].answer = 'true'"
                  >开</el-button>
                  <el-button
                    :type="scoringConfig[row.id]?.answer === 'false' ? 'danger' : ''"
                    size="small" round

                    @click="scoringConfig[row.id].answer = 'false'"
                  >关</el-button>
                </div>
              </template>

              <!-- 数值类：数字输入 -->
              <template v-else-if="['slider','rate','number'].includes(row.type)">
                <div class="answer-number-row">
                  <el-input-number
                    v-model="scoringConfig[row.id].answer"

                    :controls="false"
                    size="small"
                    style="width:100%"
                  />
                </div>
              </template>

              <!-- 日期/时间 -->
              <template v-else-if="['date','time','date-range','time-range','daterange'].includes(row.type)">
                <div class="answer-date-row">
                  <el-date-picker
                    v-model="scoringConfig[row.id].answer"
                    type="date"

                    placeholder="选择日期"
                    value-format="YYYY-MM-DD"
                    size="small"
                    style="width:100%"
                  />
                </div>
              </template>

              <!-- 颜色 -->
              <template v-else-if="row.type === 'color'">
                <div class="answer-color-row">
                  <el-color-picker
                    v-model="scoringConfig[row.id].answer"

                    show-alpha
                    size="small"
                  />
                  <el-input
                    v-model="scoringConfig[row.id].answer"

                    size="small"
                    style="width: 100px"
                    placeholder="#000000"
                  />
                </div>
              </template>

              <!-- 包含匹配：关键词标签输入 -->
              <template v-else-if="scoringConfig[row.id]?.type === 'contains'">
                <div class="keyword-tags">
                  <el-tag
                    v-for="(kw, idx) in (scoringConfig[row.id]?._keywords || [])"
                    :key="idx"
                    closable
                    size="small"
                    type="warning"
                    @close="removeKeyword(row.id, idx)"
                  >{{ kw }}</el-tag>
                  <el-input
                    v-if="keywordInputVisible[row.id]"
                    ref="keywordInputRef"
                    v-model="keywordInput[row.id]"
                    size="small"
                    style="width: 80px"
                    @keyup.enter="addKeyword(row.id)"
                    @blur="addKeyword(row.id)"
                  />
                  <el-button
                    v-else
                    size="small"
                    type="primary"
                    plain
                    @click="showKeywordInput(row.id)"
                  >+ 添加关键词</el-button>
                </div>
              </template>

              <!-- 默认：文本输入（text, textarea, rich-editor 等） -->
              <template v-else>
                <el-input
                  v-model="scoringConfig[row.id].answer"

                  :placeholder="row.type === 'textarea' ? '输入参考答案...' : '输入参考答案...'"
                  :type="row.type === 'textarea' ? 'textarea' : 'text'"
                  size="small"
                  :autosize="row.type === 'textarea' ? { minRows: 2, maxRows: 4 } : false"
                  clearable
                />
              </template>
            </template>
          </el-table-column>
          <el-table-column label="评分方式" width="160">
            <template #default="{ row }">
              <el-select v-model="scoringConfig[row.id].type" size="small" style="width: 140px" @change="onScoringTypeChange(row.id)">
                <el-option label="精确匹配" value="exact" />
                <el-option label="包含匹配" value="contains" />
                <el-option label="AI评分" value="ai" />
              </el-select>
              <div class="scoring-type-hint">{{ scoringTypeHint(scoringConfig[row.id].type) }}</div>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无表单字段，请先在上方设计器中添加字段" :image-size="60" />
      </template>
    </el-card>

    </div>
  </AdvancedModeBridge>
  </div>
</template>

<script setup name="GuideSheetDesigner">
import { ref, reactive, onMounted, onBeforeMount, onBeforeUnmount, onActivated, nextTick, watch, getCurrentInstance } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getGuideSheet, updateGuideSheet, addGuideSheet, getGuideSheetCapabilities } from '@/api/business/guideSheet'
import { ElMessage } from 'element-plus'
import { Plus, Minus } from '@element-plus/icons-vue'
import { pinyin } from 'pinyin-pro'
import AdvancedModeBridge from './components/AdvancedModeBridge.vue'
import BeginnerGuideWizard from './components/BeginnerGuideWizard.vue'
import { createBeginnerFormJson, DEFAULT_STRUCTURE_PRESET_ID } from './utils/presetFactories.js'
import { hasRenderableWidgets, parseFormJsonSafely } from './utils/formJsonBridge.js'
import { useBeginnerGuideDesigner } from './composables/useBeginnerGuideDesigner.js'

const router = useRouter()
const route = useRoute()
const { proxy } = getCurrentInstance()
const { biz_grade, biz_semester } = proxy.useDict('biz_grade', 'biz_semester')

const saving = ref(false)
const dirty = ref(false)  // 标记表单是否有未保存的修改
const designerRef = ref(null)

// 评分相关
const scoringEnabled = ref(false)
const scoredFields = ref([])
const scoringConfig = reactive({})
const formJsonVersion = ref(0)
const aiConfigured = ref(false)
const rawFormJson = ref(null)  // 保存原始 formJson 用于提取字段选项

let pollingTimer = null
/** 标记：新建导学单时是否已注入标签页组件（防止重复注入） */
let tabInjected = false

// noCache 场景：组件每次重建时重置 tabInjected，确保 VForm3 的 onFormJsonChange 能正常注入标签页
onBeforeMount(() => {
  tabInjected = false
})

/**
 * 重置为全新表单状态（仅含标签页组件）
 */
function resetToNewForm() {
  form.sheetId = undefined
  form.sheetTitle = ''
  form.grade = undefined
  form.semester = undefined
  form.lessonNum = 1
  form.formJson = ''
  form.maxPages = 0
  form.isPublic = 'N'
  form.versionNo = null
  form.teachingTopic = ''
  form.estimatedMinutes = 20
  scoredFields.value = []
  Object.keys(scoringConfig).forEach(k => delete scoringConfig[k])
  rawFormJson.value = null
  invalidFormJson.value = ''
  scoringEnabled.value = false
  tabInjected = false
  dirty.value = false

  // 新手模式先生成可直接编辑的课堂结构，高级设计器按需挂载。
  if (editorMode.value === 'beginner') {
    const initialJson = createBeginnerFormJson(DEFAULT_STRUCTURE_PRESET_ID)
    rawFormJson.value = initialJson
    form.formJson = JSON.stringify(initialJson)
    return
  }

  setTimeout(() => {
    if (designerRef.value && !tabInjected) {
      designerRef.value.setFormJson({ widgetList: [createTabWidget()] })
      tabInjected = true
    }
  }, 200)
}

/**
 * 提取表单中标签页的页面信息（页面名称列表）
 * 同时递归检测 widgetList 中是否存在 tab 类型的 widget
 */
function extractTabPages(formJson) {
  const pages = []
  let hasTab = false
  const visited = new Set()

  function walk(value) {
    if (value == null || typeof value !== 'object' || visited.has(value)) return
    visited.add(value)
    if (Array.isArray(value)) {
      for (const item of value) walk(item)
    } else {
      if (value.type === 'tab') {
        hasTab = true
        const tabs = value.tabs  // tabs 是 tab widget 的直接属性，非 options.tabs
        if (Array.isArray(tabs)) {
          for (const tab of tabs) {
            pages.push(tab.options?.label || tab.options?.name || '')
          }
        }
      }
      for (const key of Object.keys(value)) {
        const v = value[key]
        if (v && typeof v === 'object') walk(v)
      }
    }
  }

  walk(formJson?.widgetList || [])
  return { pages, hasTab }
}

/**
 * 创建一个有效的标签页 widget（结构与 VForm3 copyNewContainerWidget 输出一致）
 * 唯一名称固定为 HomeTab，铺满整个设计面板
 */
function createTabWidget() {
  const tabId = 'tab-' + Math.random().toString(36).substring(2, 10)
  const paneId = 'tab-pane-' + Math.random().toString(36).substring(2, 10)
  return {
    id: tabId,
    type: 'tab',
    category: 'container',
    icon: 'tab',
    displayType: 'border-card',
    internal: true,  // 标记为内部组件，禁止删除和移动
    tabs: [
      {
        id: paneId,
        type: 'tab-pane',
        category: 'container',
        icon: 'tab-pane',
        internal: true,
        widgetList: [],
        options: {
          name: 'tab1',
          label: 'tab 1',
          hidden: false,
          active: false,
          disabled: false,
          customClass: ''
        }
      }
    ],
    options: {
      name: 'HomeTab',
      hidden: false,
      customClass: ''
    }
  }
}

/**
 * 深度克隆对象
 */
function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') return obj
  if (obj instanceof Date) return new Date(obj.getTime())
  if (obj instanceof Array) return obj.map(item => deepClone(item))
  const cloned = {}
  for (const key in obj) {
    if (obj.hasOwnProperty(key)) {
      cloned[key] = deepClone(obj[key])
    }
  }
  return cloned
}

/**
 * 递归遍历 widgetList，修复 picture-upload 组件的 uploadURL
 * VForm3 默认 uploadURL 为空字符串，导致上传请求 404
 */
function fixUploadURLs(formJson) {
  let needFix = false
  // 使用环境变量，避免硬编码 /dev-api
  const uploadEndpoint = `${import.meta.env.VITE_APP_BASE_API || ''}/common/upload`

  function walk(widgetList) {
    if (!Array.isArray(widgetList)) return
    for (const w of widgetList) {
      if (w.type === 'picture-upload' && w.options && (w.options.uploadURL === '' || w.options.uploadURL === undefined)) {
        needFix = true
      }
      // 递归处理容器内的子组件
      if (w.type === 'tab' && Array.isArray(w.tabs)) {
        for (const pane of w.tabs) {
          if (Array.isArray(pane.widgetList)) walk(pane.widgetList)
        }
      } else if (Array.isArray(w.widgetList)) {
        walk(w.widgetList)
      }
    }
  }

  walk(formJson.widgetList)
  if (!needFix) return null

  // 克隆并修复
  const cloned = deepClone(formJson)

  function fixWalk(widgetList) {
    if (!Array.isArray(widgetList)) return
    for (const w of widgetList) {
      if (w.type === 'picture-upload' && w.options && (w.options.uploadURL === '' || w.options.uploadURL === undefined)) {
        w.options.uploadURL = uploadEndpoint
      }
      if (w.type === 'tab' && Array.isArray(w.tabs)) {
        for (const pane of w.tabs) {
          if (Array.isArray(pane.widgetList)) fixWalk(pane.widgetList)
        }
      } else if (Array.isArray(w.widgetList)) {
        fixWalk(w.widgetList)
      }
    }
  }

  fixWalk(cloned.widgetList)
  return cloned
}

/**
 * 原地修复 picture-upload 组件的 uploadURL（不克隆，直接修改对象）
 * 用于保存前确保 uploadURL 不会被遗漏
 */
function fixUploadURLsInPlace(obj) {
  if (!obj || typeof obj !== 'object') return
  // 使用环境变量，避免硬编码 /dev-api
  const uploadEndpoint = `${import.meta.env.VITE_APP_BASE_API || ''}/common/upload`
  const visited = new Set()

  function walk(value) {
    if (value == null || typeof value !== 'object' || visited.has(value)) return
    visited.add(value)
    if (Array.isArray(value)) {
      for (const item of value) walk(item)
    } else {
      if (value.type === 'picture-upload' && value.options && (value.options.uploadURL === '' || value.options.uploadURL === undefined || value.options.uploadURL == null)) {
        value.options.uploadURL = uploadEndpoint
      }
      for (const key of Object.keys(value)) {
        const v = value[key]
        if (v && typeof v === 'object') walk(v)
      }
    }
  }

  walk(obj.widgetList || [])
}

// 设计器中隐藏的无评分价值字段
const bannedWidgets = []

// pinyin 命名计数器，key=widgetType, value=当前编号
const pinyinCounters = reactive({})

const form = reactive({
  sheetId: undefined,
  versionNo: null,
  sheetTitle: '',
  grade: undefined,
  semester: undefined,
  lessonNum: 1,
  formJson: '',
  maxPages: 0,
  isPublic: 'N',
  teachingTopic: '',
  estimatedMinutes: 20
})

const {
  editorMode,
  loadingSheet,
  invalidFormJson,
  draftKey,
  handleBeginnerMetadata,
  handleBeginnerFormJson,
  enterAdvancedMode,
  returnToBeginnerMode,
  handleBeginnerSave
} = useBeginnerGuideDesigner({
  route,
  router,
  form,
  rawFormJson,
  dirty,
  saveTemplate,
  hydrateAdvancedDesigner,
  readAdvancedFormJson: getFormJsonString
})

function markDirty() {
  dirty.value = true
}

function hydrateAdvancedDesigner(attempt = 0) {
  if (!designerRef.value) {
    if (attempt < 20) setTimeout(() => hydrateAdvancedDesigner(attempt + 1), 100)
    return
  }
  try {
    const parsed = deepClone(rawFormJson.value) || createBeginnerFormJson(DEFAULT_STRUCTURE_PRESET_ID, {
      topic: form.teachingTopic,
      estimatedMinutes: form.estimatedMinutes
    })
    fixUploadURLsInPlace(parsed)
    extractScoredFields(parsed)
    const hasScoring = Object.keys(parsed._scoringConfig || {}).length > 0
      || scoredFields.value.some(field => Number(scoringConfig[field.id]?.score || 0) > 0)
    scoringEnabled.value = hasScoring
    tabInjected = true
    designerRef.value.setFormJson(parsed)
    nextTick(startDomObserver)
  } catch {
    ElMessage.warning('高级设计器暂时无法载入，请返回新手模式继续编辑')
  }
}


// 表单设计器配置
const designerConfig = ref({
  languageMenu: true,
  externalLink: false,
  formTemplates: true,
  eventCollapse: false,
  clearDesignerButton: true,
  previewFormButton: true,
  importJsonButton: true,
  exportJsonButton: true,
  exportCodeButton: true,
  language: 'zh-CN'
})

/**
 * 设计器表单内容变化时，更新组件内部状态
 */
function onFormJsonChange(formJson) {
  try {
    if (formJson) {
      // 标记表单已修改
      dirty.value = true
      // 新建导学单时，在 VForm3 初始化完成的首个事件中自动注入标签页
      if (!tabInjected && !route.params.sheetId) {
        tabInjected = true
        const { hasTab } = extractTabPages(formJson)
        if (!hasTab && formJson.widgetList) {
          // 深度克隆后注入标签页，再 setFormJson 触发重渲染
          const cloned = deepClone(formJson)
          cloned.widgetList.unshift(createTabWidget())
          nextTick(() => {
            designerRef.value?.setFormJson(cloned)
          })
          return  // 本次跳过业务处理，等 setFormJson 触发下一次事件
        }
      }

      // 修复图片上传组件的 uploadURL（默认空字符串导致 404）
      const urlFixedJson = fixUploadURLs(formJson)
      if (urlFixedJson) {
        nextTick(() => {
          designerRef.value?.setFormJson(urlFixedJson)
        })
        return
      }

      form.maxPages = (formJson.widgetList && Array.isArray(formJson.widgetList))
        ? formJson.widgetList.length || 1
        : 1
      // 为新增字段设置中文标签 + 拼音唯一名称
      autoRenameWidgets(formJson)
      // 同步评分配置字段列表 - 不覆盖已恢复的 scoringConfig（防止 setFormJson 触发后丢失已加载的参考答案）
      extractScoredFieldsPreserveConfig(formJson)
      formJsonVersion.value++
    }
  } catch {
    // 高级组件的瞬时事件不应中断教师当前编辑。
  }
}

/**
 * 自动为新增字段设置中文标签和拼音+数字的唯一名称
 */
function autoRenameWidgets(formJson) {
  try {
    const visited = new Set()
    const widgetTypeLabels = {
      text: '单行文本', textarea: '多行文本', number: '数字', input: '输入框',
      radio: '单选', checkbox: '多选', select: '下拉选择', cascader: '级联选择',
      date: '日期', 'date-range': '日期范围', daterange: '日期范围',
      time: '时间', 'time-range': '时间范围',
      rate: '评分', slider: '滑块', switch: '开关', color: '颜色选择',
      'rich-editor': '富文本', 'file-upload': '文件上传', 'picture-upload': '图片上传',
      'static-text': '静态文本', 'html-text': 'HTML文本', 'image-add': '图片展示'
    }

    function walk(value) {
      if (value == null || typeof value !== 'object' || visited.has(value)) return
      visited.add(value)
      if (Array.isArray(value)) {
        for (const item of value) walk(item)
      } else {
        const id = value.id || value.name
        const type = value.type
        // 如果有 options 对象，则标签/名称在 options 内（VForm3 结构）
        const labelHolder = value.options || value
        if (id && type) {
          // 标签：空标签或全局默认名则改为中文
          if (!labelHolder.label || labelHolder.label === type || /^[a-z]+\d*$/i.test(labelHolder.label)) {
            const cnLabel = widgetTypeLabels[type] || type
            if (!pinyinCounters[type]) pinyinCounters[type] = 1
            else pinyinCounters[type]++
            labelHolder.label = cnLabel + pinyinCounters[type]
          }
          // 名称：空名称或纯英文则改为拼音+数字
          if (!labelHolder.name || /^[a-z]+\d*$/i.test(labelHolder.name)) {
            const nameBase = pinyin(labelHolder.label || type, { toneType: 'none', type: 'array' }).join('')
            labelHolder.name = nameBase + (pinyinCounters[type] || 1)
          }
        }
        // 继续深入嵌套
        for (const key of Object.keys(value)) {
          const v = value[key]
          if (v && typeof v === 'object') walk(v)
        }
      }
    }

    walk(formJson.widgetList)
  } catch {
    // 旧模板字段不完整时保留原名称，由教师继续编辑。
  }
}

/**
 * 字段类型中文映射
 */
function widgetTypeLabel(type) {
  const map = {
    // === 单值输入（可评分）===
    text: '单行文本', textarea: '多行文本', number: '数字', input: '输入框',
    // === 选择类（可评分）===
    radio: '单选', checkbox: '多选', select: '下拉选择', cascader: '级联选择',
    // === 时间类（可评分）===
    date: '日期', 'date-range': '日期范围', daterange: '日期范围',
    time: '时间', 'time-range': '时间范围',
    // === 数值/状态类（可评分）===
    rate: '评分', slider: '滑块', switch: '开关', color: '颜色选择',
    // === 富文本 ===
    'rich-editor': '富文本',
    // === 文件上传（不可评分）===
    'file-upload': '文件上传', 'picture-upload': '图片上传',
    // === 展示类（不可评分）===
    'static-text': '静态文本', 'html-text': 'HTML文本', 'image-add': '图片展示', divider: '分割线',
    // === 容器类 ===
    grid: '栅格容器', 'grid-col': '栅格列',
    tab: '选项卡', 'tab-pane': '选项卡面板',
    card: '卡片容器', 'border-card': '边框卡片',
    table: '表格', 'table-cell': '表格单元格',
    // === 交互/其他 ===
    button: '按钮', alert: '警告', fold: '折叠面板',
    snippet: '代码片段', 'type-editor': '类型编辑器'
  }
  return map[type] || type || '未知'
}

/**
 * 判断评分答案是否非空（用户已配置过）
 */
function hasNonEmptyAnswer(answer) {
  if (answer == null || answer === '') return false
  if (Array.isArray(answer)) return answer.length > 0
  return true
}

/**
 * 从 formJson.widgetList 提取可评分字段，通用递归——不依赖特定容器属性名
 * 策略：遍历任意嵌套结构，凡是带 id/guid/name + type 的对象即为字段，数组和子对象一律深入
 */
function extractScoredFields(formJson) {
  rawFormJson.value = formJson  // 保存原始 formJson，供 getFieldOptions 使用
  const widgets = formJson?.widgetList || []
  const fields = []

  // 优先从 _scoringConfig 快照恢复评分配置（用 label 匹配，不受 VForm3 重分配 id/guid 影响）
  const scoringSnapshot = formJson?._scoringConfig || {}
  const hasSnapshot = Object.keys(scoringSnapshot).length > 0

  const scoreableTypes = [
    'radio', 'checkbox', 'select', 'cascader',
    'input', 'textarea', 'color'
  ]
  const visited = new Set()

  function walk(value) {
    if (value == null || typeof value !== 'object') return
    if (visited.has(value)) return
    visited.add(value)

    if (Array.isArray(value)) {
      for (const item of value) walk(item)
    } else {
      const id = value.id || value.name || value.guid
      const type = value.type
      if (id && type && scoreableTypes.includes(type)) {
        const title = getWidgetLabel(value)
        fields.push({ id, title, type })

        if (!scoringConfig[id]) {
          // 优先从 _scoringConfig 快照恢复（label 匹配，不受 id/guid 变化影响）
          const snap = scoringSnapshot[title]
          if (hasSnapshot && snap && typeof snap.score === 'number') {
            scoringConfig[id] = normalizeScoringConfigValue(snap, type)
          } else if (value.scoring && typeof value.scoring.score === 'number') {
            // 回退：从 w.scoring 恢复
            scoringConfig[id] = normalizeScoringConfigValue(value.scoring, type)
          } else {
            scoringConfig[id] = { score: 0, answer: type === 'checkbox' ? [] : '', type: 'exact' }
          }
          initKeywords(scoringConfig[id])
        }
      }
      for (const key of Object.keys(value)) {
        const v = value[key]
        if (v && typeof v === 'object') walk(v)
      }
    }
  }

  walk(widgets)

  // 清理已删除的字段（用 label 匹配，不受 id/guid 变化影响）
  const currentLabels = new Set(fields.map(f => f.title))
  const keysToDelete = []
  for (const key of Object.keys(scoringConfig)) {
    const cfg = scoringConfig[key]
    if (cfg && (cfg.score > 0 || hasNonEmptyAnswer(cfg.answer))) {
      const widget = findWidgetById(formJson?.widgetList || [], key)
      const lbl = widget ? getWidgetLabel(widget) : key
      if (!currentLabels.has(lbl)) {
        keysToDelete.push(key)
      }
    }
  }
  for (const key of keysToDelete) {
    delete scoringConfig[key]
  }
  scoredFields.value = fields
  // 有可评分字段时自动开启评分开关
  if (fields.length > 0 && !scoringEnabled.value) {
    const totalScore = fields.reduce((sum, f) => sum + (scoringConfig[f.id]?.score || 0), 0)
    if (totalScore > 0) {
      scoringEnabled.value = true
    }
  }
}

/**
 * 手动刷新评分字段列表（从设计器读取最新 formJson）
 * 注意：只刷新字段列表，不覆盖用户已设置的 scoringConfig
 */
function refreshScoredFields() {
  if (designerRef.value) {
    try {
      const json = designerRef.value.getFormJson()
      if (json && json.widgetList) {
        extractScoredFieldsPreserveConfig(json)
      }
    } catch (e) {
      // ignore
    }
  }
}

/**
 * 提取 widget 的显示标签（统一逻辑：VForm3 标签在 options.label）
 */
function getWidgetLabel(w) {
  return w.label || (w.options && w.options.label) || w.title || (w.id || w.name || w.guid) || ''
}

/**
 * 从 scoringConfig 值恢复为规范化配置对象
 */
function normalizeScoringConfigValue(cfg, widgetType) {
  let answer = cfg.answer
  if (widgetType === 'checkbox') {
    if (typeof answer === 'string' && answer.includes(',')) {
      answer = answer.split(',').map(v => v.trim()).filter(v => v !== '')
    } else if (typeof answer === 'string' && answer) {
      answer = [answer]
    } else if (!Array.isArray(answer)) {
      answer = []
    }
  } else if (widgetType === 'switch') {
    if (typeof answer === 'boolean') answer = answer ? 'true' : 'false'
    else if (answer !== null && answer !== undefined) answer = String(answer).trim()
    else answer = ''
  } else if (widgetType === 'cascader') {
    if (typeof answer === 'string' && answer) {
      answer = answer.split(',').map(v => v.trim()).filter(v => v !== '')
    }
    const cascaderPath = cfg._cascaderPath || (Array.isArray(answer) ? answer : [])
    return { score: cfg.score || 0, answer: Array.isArray(answer) ? answer : (answer || ''), type: cfg.type || 'exact', _cascaderPath: [...cascaderPath] }
  }
  return { score: cfg.score || 0, answer: Array.isArray(answer) ? answer : (answer || ''), type: cfg.type || 'exact' }
}

/** 初始化包含匹配的关键词数组 */
function initKeywords(cfg) {
  if (cfg.type === 'contains' && !cfg._keywords) {
    const answer = cfg.answer
    if (typeof answer === 'string' && answer.trim()) {
      cfg._keywords = answer.split(',').map(v => v.trim()).filter(v => v !== '')
    } else {
      cfg._keywords = []
    }
  }
}

/** 评分方式变更时处理 */
function onScoringTypeChange(widgetId) {
  const cfg = scoringConfig[widgetId]
  if (cfg) {
    initKeywords(cfg)
  }
}

/**
 * 提取评分字段但不覆盖已有的 scoringConfig（保留用户实时修改）
 * 核心策略：优先从 _scoringConfig 快照（label 作 key）恢复，不受 VForm3 重分配 id/guid 影响
 */
function extractScoredFieldsPreserveConfig(formJson) {
  // 保存旧 rawFormJson 中的 _scoringConfig 快照（label → 配置），再更新引用
  const scoringSnapshot = rawFormJson.value?._scoringConfig || {}
  rawFormJson.value = formJson

  const widgets = formJson?.widgetList || []
  const fields = []
  const scoreableTypes = [
    'radio', 'checkbox', 'select', 'cascader',
    'input', 'textarea', 'color'
  ]
  const visited = new Set()

  function walk(value) {
    if (value == null || typeof value !== 'object') return
    if (visited.has(value)) return
    visited.add(value)

    if (Array.isArray(value)) {
      for (const item of value) walk(item)
    } else {
      const id = value.id || value.name || value.guid
      const type = value.type
      if (id && type && scoreableTypes.includes(type)) {
        const title = getWidgetLabel(value)
        fields.push({ id, title, type })

        if (!scoringConfig[id]) {
          // 优先从 _scoringConfig 快照恢复（label 匹配，不受 id/guid 变化影响）
          const snap = scoringSnapshot[title]
          if (snap && typeof snap.score === 'number') {
            scoringConfig[id] = normalizeScoringConfigValue(snap, type)
          } else if (value.scoring && typeof value.scoring.score === 'number') {
            // 回退：从 w.scoring 恢复
            scoringConfig[id] = normalizeScoringConfigValue(value.scoring, type)
          } else {
            scoringConfig[id] = { score: 0, answer: type === 'checkbox' ? [] : '', type: 'exact' }
          }
          initKeywords(scoringConfig[id])
        }
      }
      for (const key of Object.keys(value)) {
        const v = value[key]
        if (v && typeof v === 'object') walk(v)
      }
    }
  }

  walk(widgets)

  // 清理已删除的字段（用 label 匹配，不受 id/guid 变化影响）
  const currentLabels = new Set(fields.map(f => f.title))
  const keysToDelete = []
  for (const key of Object.keys(scoringConfig)) {
    const cfg = scoringConfig[key]
    if (cfg && (cfg.score > 0 || hasNonEmptyAnswer(cfg.answer))) {
      const widget = findWidgetById(rawFormJson.value?.widgetList || [], key)
      const lbl = widget ? getWidgetLabel(widget) : key
      if (!currentLabels.has(lbl)) {
        keysToDelete.push(key)
      }
    }
  }
  for (const key of keysToDelete) {
    delete scoringConfig[key]
  }
  scoredFields.value = fields
}

function decreaseScore(widgetId) {
  const cfg = scoringConfig[widgetId]
  if (cfg && cfg.score > 0) cfg.score--
}

function increaseScore(widgetId) {
  const cfg = scoringConfig[widgetId]
  if (cfg && cfg.score < 100) cfg.score++
}

/**
 * 评分方式提示说明
 */
function scoringTypeHint(type) {
  const hints = {
    exact: '学生答案与参考答案完全一致才得分',
    contains: '按匹配关键词数量比例计分',
    ai: 'AI根据参考答案对学生答案智能评分'
  }
  return hints[type] || ''
}

// 关键词标签输入相关状态
const keywordInput = reactive({})
const keywordInputVisible = reactive({})
const keywordInputRef = ref(null)

/** 显示关键词输入框 */
function showKeywordInput(widgetId) {
  keywordInputVisible[widgetId] = true
  nextTick(() => {
    // 聚焦输入框
    const el = document.querySelector(`.keyword-tags input`)
    if (el) el.focus()
  })
}

/** 添加关键词 */
function addKeyword(widgetId) {
  const kw = (keywordInput[widgetId] || '').trim()
  if (!kw) {
    keywordInputVisible[widgetId] = false
    return
  }
  const cfg = scoringConfig[widgetId]
  if (!cfg._keywords) {
    cfg._keywords = []
  }
  if (!cfg._keywords.includes(kw)) {
    cfg._keywords.push(kw)
    // 同步到 answer 字段（逗号分隔）
    cfg.answer = cfg._keywords.join(',')
  }
  keywordInput[widgetId] = ''
  keywordInputVisible[widgetId] = false
}

/** 移除关键词 */
function removeKeyword(widgetId, idx) {
  const cfg = scoringConfig[widgetId]
  if (cfg._keywords) {
    cfg._keywords.splice(idx, 1)
    cfg.answer = cfg._keywords.join(',')
  }
}

/**
 * 判断多选选项是否已选中（绕过 el-checkbox-group 的响应式问题）
 */
function isCheckboxChecked(widgetId, value) {
  const cfg = scoringConfig[widgetId]
  if (!cfg || !Array.isArray(cfg.answer)) return false
  return cfg.answer.includes(value)
}

/**
 * 切换多选选项的选中状态（绕过 el-checkbox-group 的响应式问题）
 */
function toggleCheckboxAnswer(widgetId, value, checked) {
  const cfg = scoringConfig[widgetId]
  if (!cfg) return
  if (!Array.isArray(cfg.answer)) {
    cfg.answer = []
  }
  if (checked) {
    if (!cfg.answer.includes(value)) {
      cfg.answer = [...cfg.answer, value]
    }
  } else {
    cfg.answer = cfg.answer.filter(v => v !== value)
  }
}

/**
 * 索引转字母：0→A, 1→B, 2→C, ...
 */
function indexToLetter(i) {
  return String.fromCharCode(65 + (i % 26))
}

/**
 * 判断两个数组是否相等
 */
function arraysEqual(a, b) {
  if (!Array.isArray(a) || !Array.isArray(b)) return false
  if (a.length !== b.length) return false
  return a.every((v, i) => String(v) === String(b[i]))
}
/**
 * 获取 checkbox 字段的选项列表，并做防御性初始化
 */
function getCheckboxOptions(row) {
  const options = getFieldOptions(row.id, row.type)
  // 防御：如果 options 为空，返回空数组避免 v-for 报错
  return options || []
}

/**
 * 从原始 formJson 中提取指定字段的选项列表（用于 radio/checkbox/select）
 * VForm3 结构：options.optionItems 包含 [{label, value}, ...]
 * 优先从 designerRef 实时读取（确保拿到最新数据），fallback 到 rawFormJson
 */
function getFieldOptions(widgetId, widgetType) {
  const widgets = getWidgetListFromDesigner()
  if (!widgets || widgets.length === 0) return []

  const target = findWidgetById(widgets, widgetId)
  if (!target) return []

  let options = extractOptionsFromWidget(target)
  return normalizeOptions(options)
}

/**
 * 从 designerRef 实时获取 widgetList（优先于 rawFormJson ref）
 */
function getWidgetListFromDesigner() {
  if (designerRef.value) {
    try {
      const json = designerRef.value.getFormJson()
      if (json && json.widgetList && Array.isArray(json.widgetList)) {
        return json.widgetList
      }
    } catch (e) { /* ignore */ }
  }
  return rawFormJson.value?.widgetList || []
}

/**
 * 从 widget 对象中提取选项数组
 */
function extractOptionsFromWidget(target) {
  let options = []

  // VForm3 标准结构：widget.options.optionItems
  if (target.options && target.options.optionItems && Array.isArray(target.options.optionItems)) {
    options = target.options.optionItems
  }
  // 兼容：widget.options 直接是数组
  else if (Array.isArray(target.options)) {
    options = target.options
  }
  // 兼容：widget.options.options
  else if (target.options && typeof target.options === 'object' && Array.isArray(target.options.options)) {
    options = target.options.options
  }
  // 兼容：widget.props.options
  else if (target.props && Array.isArray(target.props.options)) {
    options = target.props.options
  }
  // 兼容：widget.widgetOptions
  else if (Array.isArray(target.widgetOptions)) {
    options = target.widgetOptions
  }

  return options
}

/**
 * 标准化选项格式 {label, value}
 */
function normalizeOptions(options) {
  return options.map(opt => ({
    label: opt.label || opt.text || opt.name || String(opt.value),
    value: opt.value != null ? opt.value : String(opt.value)
  })).filter(o => o.value !== '' && o.value != null)
}

/**
 * 递归查找 widget（支持任意嵌套的容器结构）
 * VForm3 字段用 guid 作为唯一标识
 */
function findWidgetById(widgets, targetId) {
  for (const w of widgets) {
    const id = w.id || w.name || w.guid
    if (id === targetId) return w
    for (const key of Object.keys(w)) {
      const val = w[key]
      if (Array.isArray(val)) {
        const found = findWidgetById(val, targetId)
        if (found) return found
      } else if (val && typeof val === 'object') {
        const found = findWidgetById([val], targetId)
        if (found) return found
      }
    }
  }
  return null
}

/**
 * 级联选择器的 options 数据结构（树形）
 */
function getCascaderOptions(widgetId) {
  const widgets = getWidgetListFromDesigner()
  const target = findWidgetById(widgets, widgetId)
  if (!target) return []

  let options = extractOptionsFromWidget(target)
  return normalizeCascaderOptions(options)
}

/**
 * 规范化级联选项为树形结构 [{label, value, children}]
 */
function normalizeCascaderOptions(options) {
  if (!Array.isArray(options) || options.length === 0) return []
  return options.map(opt => ({
    label: opt.label || opt.text || opt.name || '',
    value: opt.value != null ? opt.value : String(opt.value),
    children: normalizeCascaderOptions(opt.children || opt.options || opt.items || [])
  })).filter(o => o.label)
}

/**
 * 展平级联选项为扁平路径列表，[{label, value, path}]
 * 例如：[{label:'北京', value:'bj', path:['bj']}, {label:'朝阳区', value:'cyq', path:['bj','cyq']}]
 */
function flattenCascaderOptions(options, parentPath) {
  parentPath = parentPath || []
  const result = []
  for (const opt of options) {
    const path = [...parentPath, opt.value]
    result.push({
      label: opt.label,
      value: opt.value,
      path: path
    })
    if (opt.children && opt.children.length > 0) {
      result.push(...flattenCascaderOptions(opt.children, path))
    }
  }
  return result
}

/**
 * 获取当前表单 JSON 字符串（含评分配置 + AI API Key）
 */
function getFormJsonString() {
  if (designerRef.value) {
    const json = designerRef.value.getFormJson()
    if (!json) return ''
    // 递归遍历所有 widget（包括嵌套在容器内的），嵌入 scoring 属性
    const visited = new Set()
    function walkWidgets(value) {
      if (value == null || typeof value !== 'object' || visited.has(value)) return
      visited.add(value)
      if (Array.isArray(value)) {
        for (const item of value) walkWidgets(item)
      } else {
        const widgetId = value.id || value.name || value.guid
        const type = value.type
        if (widgetId) {
          const cfg = scoringConfig[widgetId]
          if (!cfg || !scoringEnabled.value) {
            if (value.scoring) delete value.scoring
          } else {
            let answerValue = cfg.answer
            if (Array.isArray(answerValue)) {
              const filtered = answerValue.filter(v => v !== '' && v != null)
              answerValue = filtered.join(',')
            } else if (type === 'cascader' && cfg._cascaderPath) {
              const filtered = cfg._cascaderPath.filter(v => v !== '' && v != null)
              answerValue = filtered.join(',')
            } else if (type === 'switch') {
              answerValue = answerValue === 'true' || answerValue === true
            } else if (answerValue !== null && answerValue !== undefined) {
              answerValue = String(answerValue).trim()
            } else {
              answerValue = ''
            }
            value.scoring = { score: cfg.score, answer: answerValue, type: cfg.type }
          }
        }
        // 递归进入子属性（容器内的 widgetList、cols 等）
        for (const key of Object.keys(value)) {
          const v = value[key]
          if (v && typeof v === 'object') walkWidgets(v)
        }
      }
    }
    walkWidgets(json.widgetList || [])

    // 修复图片上传组件的 uploadURL（确保保存时 uploadURL 始终正确）
    fixUploadURLsInPlace(json)

    // 构建 _scoringConfig 快照（递归遍历所有 widget，用 label 作 key）
    const scoringSnapshot = {}
    const visited2 = new Set()
    function collectSnapshot(value) {
      if (value == null || typeof value !== 'object' || visited2.has(value)) return
      visited2.add(value)
      if (Array.isArray(value)) {
        for (const item of value) collectSnapshot(item)
      } else {
        const widgetId = value.id || value.name || value.guid
        if (widgetId) {
          const cfg = scoringConfig[widgetId]
          if (cfg && scoringEnabled.value && (cfg.score > 0 || hasNonEmptyAnswer(cfg.answer))) {
            const label = getWidgetLabel(value)
            scoringSnapshot[label] = {
              score: cfg.score,
              answer: cfg.answer,
              type: cfg.type,
              _cascaderPath: cfg._cascaderPath || undefined
            }
          }
        }
        for (const key of Object.keys(value)) {
          const v = value[key]
          if (v && typeof v === 'object') collectSnapshot(v)
        }
      }
    }
    collectSnapshot(json.widgetList || [])
    json._scoringConfig = scoringSnapshot

    // 兼容清理旧数据，AI密钥和供应商配置只允许存在于服务端环境。
    delete json._aiApiKey
    delete json._aiProvider
    delete json._aiModel
    delete json._aiCustomUrl
    return JSON.stringify(json)
  }
  return form.formJson || ''
}

/**
 * 构建保存数据
 */
function buildSaveData() {
  return {
    sheetId: form.sheetId,
    versionNo: form.versionNo,
    sheetTitle: form.sheetTitle,
    grade: form.grade,
    semester: form.semester,
    lessonNum: form.lessonNum,
    formJson: getFormJsonString(),
    maxPages: form.maxPages,
    isPublic: form.isPublic
  }
}

/**
 * 校验评分配置是否可用于批改，不限定导学单必须采用百分制。
 */
function validateScoringTotal() {
  if (!scoringEnabled.value) return true
  const total = scoredFields.value.reduce((sum, f) => sum + (scoringConfig[f.id]?.score || 0), 0)
  if (total <= 0) {
    ElMessage.warning('启用自动评分后，请至少为一道题设置分值')
    return false
  }
  return true
}

function validateMetadata() {
  if (!form.sheetTitle?.trim()) {
    ElMessage.warning('请输入导学单标题')
    return false
  }
  if (form.grade === undefined || form.grade === null || form.grade === '') {
    ElMessage.warning('请选择年级')
    return false
  }
  if (form.semester === undefined || form.semester === null || form.semester === '') {
    ElMessage.warning('请选择学期')
    return false
  }
  if (!form.lessonNum) {
    ElMessage.warning('请设置第几课')
    return false
  }
  if (!form.isPublic) {
    ElMessage.warning('请选择模板可见范围')
    return false
  }
  return true
}

function validateFormContent(formJsonValue) {
  const parsed = parseFormJsonSafely(formJsonValue)
  if (!parsed.ok) {
    ElMessage.warning('导学单内容暂时无法读取，请先创建可编辑副本')
    return false
  }
  if (!hasRenderableWidgets(parsed.formJson)) {
    ElMessage.warning('请先添加至少一个教学模块，再保存模板')
    return false
  }
  return true
}

async function saveTemplate(showSuccess = true) {
  if (!validateMetadata() || !validateScoringTotal()) return false
  const data = buildSaveData()
  if (!validateFormContent(data.formJson)) return false
  saving.value = true
  try {
    const res = await (form.sheetId ? updateGuideSheet(data) : addGuideSheet(data))
    const saved = res?.data && typeof res.data === 'object' ? res.data : res
    if (!form.sheetId && saved?.sheetId) form.sheetId = saved.sheetId
    if (!form.sheetId && (typeof res?.data === 'number' || typeof res?.data === 'string')) form.sheetId = res.data
    if (saved?.versionNo != null) form.versionNo = saved.versionNo
    dirty.value = false
    if (showSuccess) {
      // 明确告知可在课程设计中选用，避免教师以为「建了却用不上」
      ElMessage.success({
        message: '导学单已保存，可在课程设计中启用并选择。',
        duration: 3000,
        showClose: true
      })
    }
    return true
  } finally {
    saving.value = false
  }
}

function handleSave() {
  saveTemplate(true)
}

function openPreview() {
  // 利用 VForm 内置预览功能
  if (designerRef.value) {
    designerRef.value.previewForm()
  }
}

async function goBack() {
  if (saving.value) return
  router.push({ path: '/business/guide-sheet-list' })
}

/**
 * 加载已有导学单数据
 */
function loadSheet(sheetId) {
  loadingSheet.value = true
  getGuideSheet(sheetId)
    .then(res => {
      const data = res.data || res
      form.sheetId = data.sheetId
      form.versionNo = data.versionNo
      form.sheetTitle = data.sheetTitle || ''
      form.grade = data.grade
      form.semester = data.semester
      form.lessonNum = data.lessonNum || 1
      form.formJson = data.formJson || ''
      form.maxPages = data.maxPages || 0
      form.isPublic = data.isPublic || 'N'
      applyLoadedFormJson(form.formJson)
      dirty.value = false
    })
    .finally(() => { loadingSheet.value = false })
}

/**
 * 引用模式：加载模板导学单的 formJson，创建新导学单
 */
function loadSheetAsTemplate(copyFromId) {
  tabInjected = true  // 模板已有表单结构，无需注入标签页
  loadingSheet.value = true
  getGuideSheet(copyFromId)
    .then(res => {
      const data = res.data || res
      form.sheetId = undefined  // 清空ID，保存时创建新记录
      form.versionNo = null
      form.sheetTitle = (data.sheetTitle || '') + '的副本'
      form.grade = data.grade
      form.semester = data.semester
      form.lessonNum = data.lessonNum || 1
      form.formJson = data.formJson || ''
      form.maxPages = data.maxPages || 0
      form.isPublic = 'N'
      applyLoadedFormJson(form.formJson)
      dirty.value = false
    })
    .finally(() => { loadingSheet.value = false })
}

function applyLoadedFormJson(value) {
  rawFormJson.value = null
  invalidFormJson.value = ''
  if (!value) return
  try {
    const parsed = JSON.parse(value)
    if (!Array.isArray(parsed.widgetList)) throw new Error('invalid structure')
    // 历史密钥字段只做兼容清理，永远不再回填到浏览器状态。
    delete parsed._aiApiKey
    delete parsed._aiProvider
    delete parsed._aiModel
    delete parsed._aiCustomUrl
    fixUploadURLsInPlace(parsed)
    rawFormJson.value = parsed
    form.teachingTopic = parsed.formConfig?.beginnerTeachingTopic || ''
    form.estimatedMinutes = Number(parsed.formConfig?.beginnerEstimatedMinutes || 20)
    extractScoredFields(parsed)
    scoringEnabled.value = Object.keys(parsed._scoringConfig || {}).length > 0
      || scoredFields.value.some(field => Number(scoringConfig[field.id]?.score || 0) > 0)
    if (editorMode.value === 'advanced') nextTick(() => hydrateAdvancedDesigner())
  } catch {
    invalidFormJson.value = value
  }
}

function fetchCapabilities() {
  getGuideSheetCapabilities().then(res => {
    const data = res.data || res
    aiConfigured.value = Boolean(data.aiConfigured)
  }).catch(() => {
    aiConfigured.value = false
  })
}

// 监听 formJsonVersion 变化，延迟刷新评分字段列表
watch(formJsonVersion, () => {
  if (!designerRef.value) return
  nextTick(() => {
    setTimeout(() => refreshScoredFields(), 300)
  })
})

// DOM 观察器：比 VNode 树遍历更快地检测 FieldPanel，用于注册「图片展示」扩展组件
let domObserverStarted = false
let domObserver = null
function startDomObserver() {
  if (editorMode.value !== 'advanced') return
  if (domObserverStarted) return
  const container = document.querySelector('.designer-card')
  if (!container) {
    setTimeout(startDomObserver, 50)
    return
  }
  domObserverStarted = true
  domObserver = new MutationObserver(() => {
    const panelEl = document.querySelector('.panel-container')
    if (panelEl) {
      registerCustomImageWidget()
      if (domObserver) {
        domObserver.disconnect()
        domObserver = null
        domObserverStarted = false
      }
    }
  })
  domObserver.observe(container, { childList: true, subtree: true })
  // 安全兜底：10 秒后断开观察器，避免长期占用
  setTimeout(() => {
    if (domObserver) {
      domObserver.disconnect()
      domObserver = null
      domObserverStarted = false
    }
  }, 10000)
}

/**
 * VForm3 自定义扩展组件：图片展示（image-add）
 * 来自 DigitalGuide 增量，挂在左侧「自定义扩展字段」面板，不参与自动评分。
 */
const IMAGE_ADD_WIDGET_SCHEMA = {
  type: 'image-add',
  icon: 'picture-upload-field',
  formItemFlag: true,
  options: {
    name: '',
    label: '图片展示',
    labelAlign: '',
    defaultValue: null,
    columnWidth: '200px',
    size: '',
    labelWidth: null,
    labelHidden: false,
    disabled: false,
    hidden: false,
    required: false,
    requiredHint: '',
    validation: '',
    validationHint: '',
    imageWidth: 200,
    imageHeight: 200,
    imageUrl: '',
    customClass: '',
    onCreated: '',
    onMounted: '',
    onChange: '',
    onValidate: ''
  }
}

/**
 * 向 VForm3 设计器 FieldPanel 注册「图片展示」组件。
 * 通过组件树找到 widget-panel，写入 customFields，并修补 i18n 与 schema 查找。
 */
function registerCustomImageWidget() {
  try {
    if (!designerRef.value) return
    const vFormInstance = designerRef.value

    let fieldPanel = null
    const root = vFormInstance.$.subTree
    if (!root) return

    function findFieldPanel(vnode) {
      if (!vnode || fieldPanel) return
      if (vnode.component) {
        const comp = vnode.component
        const name = comp.type && (comp.type.name || comp.type.__name)
        if (name === 'FieldPanel' || name === 'WidgetPanel' || name === 'widget-panel') {
          fieldPanel = comp
          return
        }
        if (comp.subTree) {
          findFieldPanel(comp.subTree)
        }
      }
      if (vnode.children && Array.isArray(vnode.children)) {
        for (const child of vnode.children) {
          findFieldPanel(child)
          if (fieldPanel) return
        }
      }
      if (vnode.dynamicChildren && Array.isArray(vnode.dynamicChildren)) {
        for (const child of vnode.dynamicChildren) {
          findFieldPanel(child)
          if (fieldPanel) return
        }
      }
    }
    findFieldPanel(root)

    if (!fieldPanel) return

    // FieldPanel 为 Options API，customFields 在 data 中
    const customFields = fieldPanel.data.customFields
    if (customFields && Array.isArray(customFields)) {
      const alreadyRegistered = customFields.some(f => f.type === 'image-add')
      if (!alreadyRegistered) {
        customFields.push({
          key: 'image_add_' + Date.now(),
          ...IMAGE_ADD_WIDGET_SCHEMA,
          displayName: '图片展示'
        })
      }
    }

    // image-add 不在 VForm3 内置 locale，需覆盖 proxy.i18n2t 才能显示中文标签
    if (!fieldPanel._i18nPatched) {
      fieldPanel._i18nPatched = true
      const proxy = fieldPanel.proxy
      const originalI18n2t = proxy.i18n2t
      proxy.i18n2t = function(d, e) {
        if (d === 'designer.widgetLabel.image-add') {
          return '图片展示'
        }
        return originalI18n2t.call(this, d, e)
      }
      if (customFields && customFields.length > 0) {
        const lastItem = customFields[customFields.length - 1]
        customFields.push({ ...lastItem, key: 'image_add_force_' + Date.now() })
        customFields.pop()
      }
    }

    // 属性面板依赖 getFieldWidgetByType；补丁保证 image-add 能打开配置项
    const designer = fieldPanel.props.designer
    if (designer && designer.getFieldWidgetByType) {
      if (!designer._imageAddPatched) {
        designer._imageAddPatched = true
        const originalGetFieldWidgetByType = designer.getFieldWidgetByType
        designer.getFieldWidgetByType = function(type) {
          if (type === 'image-add') {
            return {
              key: 'image_add',
              ...IMAGE_ADD_WIDGET_SCHEMA,
              displayName: '图片展示'
            }
          }
          return originalGetFieldWidgetByType.call(this, type)
        }
      }
    }
  } catch (e) {
    // 设计器尚未就绪时静默忽略，由轮询/DOM 观察器重试
  }
}

onMounted(() => {
  fetchCapabilities()
  const copyFrom = route.query.copyFrom
  const sheetId = route.params.sheetId
  if (copyFrom) {
    // 引用模式：加载模板导学单的 formJson，但创建新导学单
    loadSheetAsTemplate(copyFrom)
  } else if (sheetId) {
    tabInjected = true  // 已有导学单无需注入标签页
    loadSheet(sheetId)
  } else {
    // 新建导学单：显式重置所有状态为空白，确保不残留任何旧数据
    // resetToNewForm 内部会通过 setTimeout 注入 HomeTab 标签页
    resetToNewForm()
  }

  // 高级模式才运行 VForm3 的兼容轮询，避免新手流程产生后台开销。
  pollingTimer = setInterval(() => {
    if (editorMode.value !== 'advanced') return
    refreshScoredFields()
    registerCustomImageWidget()
  }, 5000)
})

onBeforeUnmount(() => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
  if (domObserver) {
    domObserver.disconnect()
    domObserver = null
    domObserverStarted = false
  }
})

// 监听路由变化：从其他页面跳转到新建表单时，重置为空白状态
watch(
  () => route.path,
  (newPath, oldPath) => {
    if (newPath === '/business/guide-sheet/designer' && oldPath && oldPath !== '/business/guide-sheet/designer') {
      nextTick(() => resetToNewForm())
    }
  }
)

// keep-alive 缓存激活时：若为新建表单，重置为仅含 HomeTab 标签页的空白状态
onActivated(() => {
  if (!route.params.sheetId && !dirty.value && !form.sheetTitle) {
    nextTick(() => resetToNewForm())
  }
  if (editorMode.value === 'advanced') nextTick(startDomObserver)
})

</script>

<style scoped>
.guide-sheet-designer-shell {
  min-width: 0;
}

/* 上块：导学单设置卡片 */
.filter-card {
  margin-bottom: 16px;
}

.filter-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.metadata-card {
  border: 0;
  border-top: 4px solid #1c7d86;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(35, 78, 86, 0.08);
}

.metadata-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid #edf1f3;
}

.metadata-heading strong {
  display: block;
  margin-top: 2px;
  color: #193b50;
  font-size: 17px;
}

.metadata-kicker {
  color: #2a8c86;
  font-size: 11px;
  letter-spacing: 2px;
}

.toolbar-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

.toolbar-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.toolbar-label {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
}

.toolbar-title {
  width: 320px;
  flex-shrink: 0;
}

.toolbar-compact {
  flex: 0 0 auto;
}

.toolbar-settings {
  width: 280px;
  flex-shrink: 0;
}

.settings-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.toolbar-classes {
  width: 260px;
  flex-shrink: 0;
}

.toolbar-public {
  width: 210px;
  flex-shrink: 0;
}

.toolbar-actions {
  display: flex;
  flex-direction: row;
  align-items: flex-end;
  gap: 6px;
  margin-left: auto;
  padding-top: 20px;
  flex-wrap: wrap;
}

/* 下块：评分配置卡片 */
.scoring-card {
  margin-top: 16px;
}

.scoring-card :deep(.el-card__header) {
  padding: 12px 20px;
}

.scoring-card :deep(.el-card__body) {
  padding: 12px 20px;
}

.scoring-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.scoring-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.scoring-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.score-input-row {
  display: flex;
  align-items: center;
  gap: 2px;
}

/* 参考答案按钮组 */
.answer-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
}

.answer-number-row {
  display: flex;
  gap: 4px;
  align-items: center;
}

.answer-date-row {
  display: flex;
  gap: 4px;
  align-items: center;
}

.answer-color-row {
  display: flex;
  gap: 4px;
  align-items: center;
}

/* 下块：表单设计器卡片 */
.designer-card {
  overflow: hidden;
  min-width: 0;
}

.designer-card :deep(.el-card__body) {
  padding: 0;
  min-width: 0;
  overflow: hidden;
}

/*
 * VForm3 根节点 class 也叫 main-container，会误吃到若依布局
 * 「侧栏宽度 margin-left:200px」，导致设计器整体右偏、左侧大留白。
 * 仅在导学单设计器卡片内清零，不影响全局业务页布局。
 */
.designer-card :deep(.el-container.main-container),
.designer-card :deep(.main-container.full-height) {
  margin-left: 0 !important;
  margin-right: 0 !important;
  width: 100% !important;
  max-width: 100% !important;
  min-width: 0;
  box-sizing: border-box;
}

.designer-card :deep(.v-form-designer) {
  width: 100%;
  min-width: 0;
}

/* 三栏：组件库 / 画布 / 属性，占满卡片宽度 */
.designer-card :deep(.el-container.main-container > .el-container) {
  width: 100%;
  min-width: 0;
}

.designer-card :deep(.side-panel) {
  flex: 0 0 260px;
  width: 260px !important;
  max-width: 260px;
}

.designer-card :deep(.center-layout-container) {
  flex: 1 1 auto;
  min-width: 0;
  width: auto !important;
}

.designer-card :deep(.form-widget-main) {
  min-width: 0;
  overflow-x: auto;
}

/* 右侧属性栏固定宽度，避免被挤出视口 */
.designer-card :deep(.el-container.main-container > .el-container > .el-aside:last-child) {
  flex: 0 0 300px;
  width: 300px !important;
  max-width: 300px;
  min-width: 220px;
}

/* 窄屏：允许整卡横向滚动，保证三栏仍可操作 */
@media (max-width: 1280px) {
  .designer-card {
    overflow-x: auto;
  }
  .designer-card :deep(.el-container.main-container) {
    min-width: 900px;
  }
}

/* 拖拽放置区域高亮 */
.designer-card :deep(.form-widget-list.drag-over) {
  outline: 2px dashed #409eff;
  outline-offset: 2px;
  background-color: rgba(64, 158, 255, 0.05);
}

/* 拖拽中的侧边栏项 */
.field-widget-item.dragging {
  opacity: 0.5;
}

/* 评分方式提示文字 */
.scoring-type-hint {
  font-size: 11px;
  color: #909399;
  margin-top: 3px;
  line-height: 1.4;
  max-width: 140px;
}

/* 关键词标签输入 */
.keyword-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
}
.keyword-tags .el-tag {
  margin: 0;
}
</style>
