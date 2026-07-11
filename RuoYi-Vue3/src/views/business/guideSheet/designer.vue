<template>
  <div class="app-container">
    <!-- 上块：导学单设置 -->
    <el-card class="filter-card" shadow="never">
      <div class="toolbar-row">
        <div class="toolbar-section toolbar-title">
          <span class="toolbar-label">导学单标题</span>
          <el-input v-model="form.sheetTitle" placeholder="请输入导学单标题" size="large" />
        </div>

        <div class="toolbar-section toolbar-settings">
          <span class="toolbar-label">关联课程</span>
          <div class="settings-row">
            <el-select v-model="form.lessonId" placeholder="关联课程" clearable filterable size="default">
              <el-option v-for="l in lessonOptions" :key="l.lessonId" :label="l.lessonTitle" :value="l.lessonId" />
            </el-select>
          </div>
        </div>

        <div class="toolbar-section toolbar-classes">
          <span class="toolbar-label">班级指派</span>
          <el-select v-model="form.assignedClassCodes" multiple placeholder="请选择指派班级" size="default">
            <el-option v-for="cls in classOptions" :key="cls" :label="cls" :value="cls" />
          </el-select>
        </div>

        <div class="toolbar-section toolbar-public">
          <span class="toolbar-label">是否公开</span>
          <el-radio-group v-model="form.isPublic" size="default">
            <el-radio value="Y">是</el-radio>
            <el-radio value="N">否</el-radio>
          </el-radio-group>
        </div>

        <div class="toolbar-section toolbar-actions">
          <el-button type="primary" icon="Check" @click="handleSave">保存</el-button>
          <el-button type="success" icon="Upload" @click="handleSaveAndPublish" :loading="saving">保存并发布</el-button>
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
              {{ aiConfigured ? `AI评分已配置 · ${aiProvider}` : 'AI评分未配置，将转人工处理' }}
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
                <el-option label="AI评分" value="ai" :disabled="!aiConfigured" />
              </el-select>
              <div class="scoring-type-hint">{{ scoringTypeHint(scoringConfig[row.id].type) }}</div>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无表单字段，请先在上方设计器中添加字段" :image-size="60" />
      </template>
    </el-card>

    </div>
</template>

<script setup name="GuideSheetDesigner">
import { ref, reactive, onMounted, onBeforeMount, onBeforeUnmount, onActivated, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getGuideSheet, updateGuideSheet, addGuideSheet, publishGuideSheet, getGuideSheetLessons, getGuideSheetCapabilities } from '@/api/business/guideSheet'
import { ElMessage } from 'element-plus'
import { Plus, Minus } from '@element-plus/icons-vue'
import { pinyin } from 'pinyin-pro'

const router = useRouter()
const route = useRoute()

const saving = ref(false)
const dirty = ref(false)  // 标记表单是否有未保存的修改
const lessonOptions = ref([])
const classOptions = ref([])
const designerRef = ref(null)

// 评分相关
const scoringEnabled = ref(false)
const scoredFields = ref([])
const scoringConfig = reactive({})
const formJsonVersion = ref(0)
const aiConfigured = ref(false)
const aiProvider = ref('deepseek')
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
  form.lessonId = undefined
  form.classIds = undefined
  form.formJson = ''
  form.maxPages = 0
  form.status = undefined
  form.isPublic = 'Y'
  scoredFields.value = []
  Object.keys(scoringConfig).forEach(k => delete scoringConfig[k])
  rawFormJson.value = null
  scoringEnabled.value = false
  tabInjected = false
  dirty.value = false

  // 使用 setTimeout 确保 VForm3 已完全初始化
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
 * 轮询兜底：确保 HomeTab 标签页约束不被破坏
 * 复用 enforceHomeTabConstraints 逻辑，避免重复代码
 */
function ensureTabWidget() {
  if (!designerRef.value) return
  try {
    const json = designerRef.value.getFormJson()
    if (!json.widgetList) return

    const fixedJson = enforceHomeTabConstraints(json)
    if (fixedJson) {
      designerRef.value.setFormJson(fixedJson)
    }
  } catch (e) {
    // 忽略
  }
}

/**
 * 强制执行 HomeTab 约束：
 * 1. HomeTab 必须存在且位于 widgetList 首位
 * 2. HomeTab 不可删除、不可移动
 * 3. 所有其他组件必须嵌入 HomeTab 的第一个 tab-pane 内
 * 返回修复后的克隆对象（需 setFormJson），无违规返回 null
 */
function enforceHomeTabConstraints(formJson) {
  if (!formJson || !Array.isArray(formJson.widgetList)) return null

  const wl = formJson.widgetList
  let needFix = false

  // 1. 查找 HomeTab widget（通过 type === 'tab' 且 options.name === 'HomeTab'）
  const tabIndex = wl.findIndex(w => w.type === 'tab' && w.options && w.options.name === 'HomeTab')

  if (tabIndex === -1) {
    // HomeTab 不存在 → 注入到首位
    const cloned = deepClone(formJson)
    cloned.widgetList.unshift(createTabWidget())
    return cloned
  }

  if (tabIndex !== 0) {
    needFix = true
  }

  const tabWidget = wl[tabIndex]

  // 3. 确保 internal 标记存在（防止 VForm3 序列化/反序列化时剥离）
  if (tabWidget.internal !== true) {
    needFix = true
  }

  // 4. 确保 HomeTab 至少有一个 tab-pane
  if (!Array.isArray(tabWidget.tabs) || tabWidget.tabs.length === 0) {
    needFix = true
  }

  // 5. 检查 widgetList 中是否有非 HomeTab 的 widget
  if (wl.length > 1) {
    needFix = true
  }

  if (!needFix) return null

  // 有违规，克隆后修复
  const cloned = deepClone(formJson)
  const cwl = cloned.widgetList

  // 找到克隆后的 HomeTab
  const cTabIndex = cwl.findIndex(w => w.type === 'tab' && w.options && w.options.name === 'HomeTab')
  if (cTabIndex === -1) {
    cwl.unshift(createTabWidget())
    return cloned
  }

  // 确保 HomeTab 在首位
  if (cTabIndex !== 0) {
    const [ctw] = cwl.splice(cTabIndex, 1)
    cwl.unshift(ctw)
  }

  const cTab = cwl[0]

  // 显式确保 internal 标记（防止 VForm3 序列化/反序列化时剥离）
  cTab.internal = true

  // 确保至少有一个 tab-pane
  if (!Array.isArray(cTab.tabs) || cTab.tabs.length === 0) {
    const paneId = 'tab-pane-' + Math.random().toString(36).substring(2, 10)
    cTab.tabs = [{
      id: paneId, type: 'tab-pane', category: 'container', icon: 'tab-pane',
      internal: true, widgetList: [],
      options: { name: 'tab1', label: 'tab 1', hidden: false, active: false, disabled: false, customClass: '' }
    }]
  }

  // 确保第一个 tab-pane 也有 internal 标记
  if (cTab.tabs[0]) {
    cTab.tabs[0].internal = true
  }

  const targetPane = cTab.tabs[0]
  if (!Array.isArray(targetPane.widgetList)) {
    targetPane.widgetList = []
  }

  // 将所有非 HomeTab 的 widget 移动到 tab-pane 内
  const nonTabWidgets = cwl.slice(1)
  for (const w of nonTabWidgets) {
    targetPane.widgetList.push(w)
  }
  cwl.splice(1, cwl.length - 1)

  return cloned
}

/**
 * 递归遍历 widgetList，修复 picture-upload 组件的 uploadURL
 * VForm3 默认 uploadURL 为空字符串，导致上传请求 404
 */
function fixUploadURLs(formJson) {
  let needFix = false
  const uploadEndpoint = '/dev-api/common/upload'

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
  const uploadEndpoint = '/dev-api/common/upload'
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
const bannedWidgets = [
  'button', 'alert',
  'file-upload',
  'fold', 'snippet', 'type-editor',
  // 隐藏不需要的输入/时间/数值/富文本字段（单行输入 input 恢复显示）
  'text', 'number',
  'time', 'time-range', 'date', 'date-range', 'daterange',
  'slider', 'switch',
  'rich-editor',
  'picture-upload'
]

// pinyin 命名计数器，key=widgetType, value=当前编号
const pinyinCounters = reactive({})

const form = reactive({
  sheetId: undefined,
  sheetTitle: '',
  lessonId: undefined,
  formJson: '',
  maxPages: 0,
  assignedClassCodes: [],
  status: '0',
  isPublic: 'Y'
})

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

      // 强制执行 HomeTab 约束：不可删除、不可移动、所有组件嵌入其中
      const fixedJson = enforceHomeTabConstraints(formJson)
      if (fixedJson) {
        // 有违规需要修复，通过 setFormJson 触发重渲染
        nextTick(() => {
          designerRef.value?.setFormJson(fixedJson)
        })
        return
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
  } catch (e) {
    console.warn('onFormJsonChange error:', e)
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
      'static-text': '静态文本', 'html-text': 'HTML文本'
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
  } catch (e) {
    console.warn('autoRenameWidgets error:', e)
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
    'static-text': '静态文本', 'html-text': 'HTML文本', divider: '分割线',
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
function buildSaveData(status) {
  return {
    sheetId: form.sheetId,
    sheetTitle: form.sheetTitle,
    lessonId: form.lessonId,
    formJson: getFormJsonString(),
    maxPages: form.maxPages,
    status: status,
    assignedClassCodes: form.assignedClassCodes,
    isPublic: form.isPublic
  }
}

/**
 * 校验评分配置总分是否为100（仅在启用评分时校验）
 */
function validateScoringTotal() {
  if (!scoringEnabled.value) return true
  const total = scoredFields.value.reduce((sum, f) => sum + (scoringConfig[f.id]?.score || 0), 0)
  if (total !== 100) {
    ElMessage.warning(`评分配置总分必须为100，当前总分：${total}`)
    return false
  }
  const zeroScoreField = scoredFields.value.find(f => (scoringConfig[f.id]?.score || 0) === 0)
  if (zeroScoreField) {
    ElMessage.warning(`"${zeroScoreField.title}"的分值不能为0，请设置分值`)
    return false
  }
  return true
}

function handleSave() {
  if (!form.sheetId && !form.sheetTitle) {
    ElMessage.warning('请输入导学单标题')
    return
  }
  if (!validateScoringTotal()) return
  const data = buildSaveData(form.sheetId ? form.status : '0')
  saving.value = true
  const apiCall = form.sheetId ? updateGuideSheet(data) : addGuideSheet(data)
  apiCall.then(res => {
    if (!form.sheetId && res?.sheetId) form.sheetId = res.sheetId
    if (!form.sheetId && res?.data?.sheetId) form.sheetId = res.data.sheetId
    dirty.value = false
    ElMessage.success('保存成功')
  }).finally(() => { saving.value = false })
}

function handleSaveAndPublish() {
  if (!form.sheetTitle) {
    ElMessage.warning('请输入导学单标题')
    return
  }
  if (!form.assignedClassCodes?.length) {
    ElMessage.warning('请至少指派一个班级')
    return
  }
  try {
    const formJson = JSON.parse(getFormJsonString() || '{}')
    if (!Array.isArray(formJson.widgetList) || formJson.widgetList.length === 0) {
      ElMessage.warning('请先设计表单内容')
      return
    }
  } catch (e) {
    ElMessage.warning('表单内容格式无效，请重新保存设计')
    return
  }
  if (!validateScoringTotal()) return
  // 先保存为草稿，再调用发布接口（发布接口要求 status 必须为 '0'）
  const data = buildSaveData('0')
  saving.value = true
  const apiCall = form.sheetId ? updateGuideSheet(data) : addGuideSheet(data)
  apiCall.then(res => {
    if (!form.sheetId && res?.sheetId) form.sheetId = res.sheetId
    if (!form.sheetId && res?.data?.sheetId) form.sheetId = res.data.sheetId
    dirty.value = false
    return publishGuideSheet(form.sheetId)
  }).then(() => {
    ElMessage.success('保存并发布成功')
  }).catch(() => {
    ElMessage.warning('保存成功，但发布失败，请检查表单内容和班级指派')
  }).finally(() => { saving.value = false })
}

function openPreview() {
  // 利用 VForm 内置预览功能
  if (designerRef.value) {
    designerRef.value.previewForm()
  }
}

function goBack() {
  if (saving.value) return
  // 新建表单且无标题：直接返回，不保存（避免 sheet_title 空值导致数据库错误）
  if (!form.sheetId && !form.sheetTitle) {
    router.push({ path: '/business/guide-sheet-list' })
    return
  }
  // 无未保存的修改：直接返回，不重复保存
  if (!dirty.value) {
    router.push({ path: '/business/guide-sheet-list' })
    return
  }
  if (!validateScoringTotal()) return
  saving.value = true
  const data = buildSaveData(form.sheetId ? form.status : '0')
  const apiCall = form.sheetId ? updateGuideSheet(data) : addGuideSheet(data)
  apiCall.then(res => {
    if (!form.sheetId && res?.sheetId) form.sheetId = res.sheetId
    if (!form.sheetId && res?.data?.sheetId) form.sheetId = res.data.sheetId
    dirty.value = false
  }).finally(() => {
    saving.value = false
    router.push({ path: '/business/guide-sheet-list' })
  })
}

/**
 * 加载已有导学单数据
 */
function loadSheet(sheetId) {
  getGuideSheet(sheetId).then(res => {
    form.sheetId = res.data.sheetId
    form.sheetTitle = res.data.sheetTitle || ''
    form.lessonId = res.data.lessonId
    form.formJson = res.data.formJson || ''
    form.maxPages = res.data.maxPages || 0
    form.assignedClassCodes = res.data.assignedClassCodes || []
    form.status = res.data.status || '0'
    form.isPublic = res.data.isPublic || 'Y'
    classOptions.value = res.data.allClassesInGrade || []
    dirty.value = false

    // 将 JSON 回填到设计器
    nextTick(() => {
      if (designerRef.value && form.formJson) {
        try {
          const parsed = JSON.parse(form.formJson)
          // 修复图片上传组件的 uploadURL（兼容旧数据）
          fixUploadURLsInPlace(parsed)
          // 在 setFormJson 之前提取评分配置。
          // （防止 setFormJson 触发 onFormJsonChange 时覆盖尚未恢复的 scoringConfig）
          delete parsed._aiApiKey
          delete parsed._aiProvider
          delete parsed._aiModel
          delete parsed._aiCustomUrl
          extractScoredFields(parsed)
          // 检查是否有评分配置（递归检查 widgetList 或 _scoringConfig 快照）
          const hasScoring = Object.keys(parsed._scoringConfig || {}).length > 0
            || (parsed.widgetList || []).some(w => w.scoring && w.scoring.score > 0)
          if (hasScoring) scoringEnabled.value = true
          // 设置表单（会触发 onFormJsonChange → extractScoredFieldsPreserveConfig，此时 scoringConfig 已恢复）
          designerRef.value.setFormJson(parsed)
        } catch (e) {
          console.warn('表单JSON解析失败', e)
        }
      }
    })
  })
}

/**
 * 引用模式：加载模板导学单的 formJson，创建新导学单
 */
function loadSheetAsTemplate(copyFromId) {
  tabInjected = true  // 模板已有表单结构，无需注入标签页
  getGuideSheet(copyFromId).then(res => {
    form.sheetId = undefined  // 清空ID，保存时创建新记录
    form.sheetTitle = (res.data.sheetTitle || '') + '的副本'
    form.lessonId = res.data.lessonId
    form.formJson = res.data.formJson || ''
    form.maxPages = res.data.maxPages || 0
    form.assignedClassCodes = []  // 引用时不复制班级指派
    form.status = '0'
    form.isPublic = 'Y'
    classOptions.value = res.data.allClassesInGrade || []
    dirty.value = false

    nextTick(() => {
      if (designerRef.value && form.formJson) {
        try {
          const parsed = JSON.parse(form.formJson)
          // 修复图片上传组件的 uploadURL（兼容旧数据）
          fixUploadURLsInPlace(parsed)
          extractScoredFields(parsed)
          const hasScoring = Object.keys(parsed._scoringConfig || {}).length > 0
            || (parsed.widgetList || []).some(w => w.scoring && w.scoring.score > 0)
          if (hasScoring) scoringEnabled.value = true
          designerRef.value.setFormJson(parsed)
        } catch (e) {
          console.warn('模板表单JSON解析失败', e)
        }
      }
    })
  })
}

function fetchLessonOptions() {
  getGuideSheetLessons().then(res => {
    lessonOptions.value = res.data || []
  }).catch(() => {})
}

function fetchCapabilities() {
  getGuideSheetCapabilities().then(res => {
    aiConfigured.value = Boolean(res.aiConfigured)
    aiProvider.value = res.aiProvider || 'deepseek'
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

// 轮询兜底注入：新建导学单时等待 VForm3 初始化完成，直到标签页注入成功
let injectPollingTimer = null
function startInjectPolling() {
  if (tabInjected) return

  let attempts = 0
  const maxAttempts = 20  // 20 * 300ms = 6秒，超时停止

  injectPollingTimer = setInterval(() => {
    attempts++
    // 仅超时或已注入时停止轮询；designerRef 未就绪时继续等待
    if (attempts > maxAttempts || tabInjected) {
      clearInterval(injectPollingTimer)
      injectPollingTimer = null
      return
    }

    try {
      if (!designerRef.value) return  // VForm3 未就绪，继续轮询
      const json = designerRef.value.getFormJson()
      const { hasTab } = extractTabPages(json)
      if (!hasTab && json.widgetList) {
        const clonedJson = deepClone(json)
        clonedJson.widgetList.unshift(createTabWidget())
        designerRef.value.setFormJson(clonedJson)
        tabInjected = true
        clearInterval(injectPollingTimer)
        injectPollingTimer = null
      } else if (hasTab) {
        tabInjected = true
        clearInterval(injectPollingTimer)
        injectPollingTimer = null
      }
    } catch (e) {
      // ignore, VForm3 not ready
    }
  }, 300)
}

onMounted(() => {
  fetchLessonOptions()
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
    // 轮询作为兜底：确保即使 VForm3 初始化延迟也能注入标签页
    nextTick(startInjectPolling)
  }

  // 轮询兜底：每 5 秒检测一次字段变化 + 确保标签页不被删除
  pollingTimer = setInterval(() => {
    refreshScoredFields()
    ensureTabWidget()
  }, 5000)
})

onBeforeUnmount(() => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
  if (injectPollingTimer) {
    clearInterval(injectPollingTimer)
    injectPollingTimer = null
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
  if (!route.params.sheetId) {
    nextTick(() => resetToNewForm())
  }
})
</script>

<style scoped>
/* 上块：导学单设置卡片 */
.filter-card {
  margin-bottom: 16px;
}

.filter-card :deep(.el-card__body) {
  padding: 16px 20px;
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
  width: 300px;
  flex-shrink: 0;
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
  width: 140px;
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
  overflow: visible;
}

.designer-card :deep(.el-card__body) {
  padding: 0;
}

.designer-card :deep(.v-form-designer) {
  width: 100%;
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
