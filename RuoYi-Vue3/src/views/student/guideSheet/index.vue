<template>
  <div class="student-guide-sheet" :class="{ embedded }">
    <!-- 顶部导航栏 - 与主页统一 -->
    <header v-if="!embedded" class="dashboard-header">
      <div class="dashboard-header__inner">
        <div class="header-left">
          <img src="@/assets/logo/logo.png" class="logo" alt="Logo" />
          <span class="platform-name">智慧课堂 - 学生端</span>
          <div class="view-toggle">
            <el-button size="small" plain @click="switchToHome">主页</el-button>
            <el-button size="small" type="primary" disabled>导学单</el-button>
          </div>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="36" shape="circle" icon="UserFilled" />
              <span class="user-name">{{ studentName }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-state">
      <el-icon class="is-loading" :size="48"><Loading /></el-icon>
      <p>正在加载导学单...</p>
    </div>

    <!-- 无导学单 -->
    <div v-else-if="!hasSheet" class="empty-state">
      <el-empty description="暂无导学单，请等待教师发布" :image-size="160">
        <el-button type="primary" icon="ArrowLeft" @click="switchToHome">返回智慧课堂首页</el-button>
      </el-empty>
    </div>

    <!-- 导学单内容 -->
    <div v-else class="sheet-wrapper">
      <div class="sheet-container">
        <div class="sheet-operations">
          <div class="left-info">
            <div class="sheet-heading">
              <span class="sheet-title">{{ sheetTitle }}</span>
              <span v-if="lastSavedAt" class="save-time">最近保存 {{ lastSavedAt }}</span>
            </div>
            <el-tag v-if="submitted" type="success" size="large">
              <el-icon><CircleCheckFilled /></el-icon> 已提交（可重新提交）
            </el-tag>
            <el-tag v-else type="warning" size="large">待完成</el-tag>
          </div>
          <div class="right-actions">
            <el-button icon="Refresh" size="large" @click="handleSave()" :loading="saving" :disabled="submitted || !formReady">
              保存草稿
            </el-button>
            <el-button icon="Check" type="primary" size="large" @click="handleSubmit" :loading="submitting" :disabled="!formReady">
              提交导学单
            </el-button>
          </div>
        </div>

        <div v-if="teacherMsg" class="teacher-msg-bar">
          <el-alert :title="teacherMsg" type="warning" show-icon :closable="true" @close="teacherMsg = ''" />
        </div>

        <el-alert
          v-if="formLoadError"
          class="form-error"
          :title="formLoadError"
          type="error"
          show-icon
          :closable="false"
        />

        <div v-else class="form-wrapper">
          <v-form-render
            ref="renderRef"
            :form-json="formJsonObj"
            :form-data="answerData"
            :option-data="optionData"
            @field-change="handleFieldChange"
          />
        </div>
      </div>

      <!-- 评分结果卡片 —— 始终显示 -->
      <div class="grading-card" :key="gradingKey">
        <el-card shadow="hover">
          <template #header>
            <div class="grading-card-header">
              <span class="grading-card-title">评分结果</span>
              <el-tag
                v-if="gradingResult"
                :type="gradingResult.totalScore >= 60 ? 'success' : 'danger'"
                size="large"
              >
                累计得分：{{ gradingResult.totalScore }} 分
              </el-tag>
              <span v-else class="grading-tab-hint">尚未提交</span>
              <span v-if="currentTabName && gradingResult" class="grading-tab-hint">
                {{ currentTabName }}
                <template v-if="filteredGradingDetails.length > 0">
                  {{ filteredTabScore }}/{{ filteredTabMaxScore }} 分
                </template>
                <template v-else>（未提交评分）</template>
              </span>
            </div>
          </template>
          <el-table
            :data="filteredGradingDetails"
            stripe
            size="small"
            empty-text="当前页暂无评分数据，请提交后查看"
          >
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="fieldTitle" label="题目" min-width="140" show-overflow-tooltip />
            <el-table-column label="得分" width="110" align="center" sortable prop="score" :sort-method="(a,b) => a.score - b.score">
              <template #default="{ row }">
                <span :class="scoreClass(row)">{{ row.score }} / {{ row.maxScore }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="desc" label="评语" min-width="160" show-overflow-tooltip />
          </el-table>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup name="StudentGuideSheet">
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRouter, onBeforeRouteLeave } from 'vue-router'
import { getStudentGuideSheet, submitGuideSheet, sendHeartbeat, getStudentGrading } from '@/api/business/guideSheet'
import websocketClient from '@/plugins/websocket'
import { getAuthorizationHeader } from '@/utils/session'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, CircleCheckFilled } from '@element-plus/icons-vue'
import useUserStore from '@/store/modules/user'
import { hasRenderableWidgets } from '@/views/business/guideSheet/utils/formJsonBridge.js'
import { normalizeStudentFormData } from './utils/studentFormState.js'
import { configureStudentUploadWidget } from './utils/studentUploadAdapter.js'

const router = useRouter()
const userStore = useUserStore()
const props = defineProps({
  embedded: { type: Boolean, default: false },
  expectedBindingId: { type: [Number, String], default: null }
})
const emit = defineEmits(['switch-mode'])
const embedded = computed(() => props.embedded)
const guideSheetSubmitUrl = `${import.meta.env.VITE_APP_BASE_API || ''}/business/guide-sheet/student/submit`

const loading = ref(true)
const hasSheet = ref(false)
const sheetTitle = ref('')
const bindingId = ref(null)
const formJsonObj = ref(null)
const formLoadError = ref('')
const formReady = ref(false)
const answerData = ref({})
const optionData = ref({})
const maxPages = ref(0)
const submitted = ref(false)
const submitting = ref(false)
const saving = ref(false)
const teacherMsg = ref('')
const renderRef = ref(null)
const gradingResult = ref(null)  // BUG-08：评分结果
const lastSavedAt = ref('')
const hasUnsavedChanges = ref(false)

const studentName = ref('')
const uploadEndpoint = `${import.meta.env.VITE_APP_BASE_API || ''}/business/guide-sheet/student/upload`

let heartbeatTimer = null
let autoSaveTimer = null
let autoSaveDebounceTimer = null
let formClickTarget = null
let websocketUnsubscribe = null
let saveLoopPromise = null
let pendingSave = null
let clientRevision = 0
let lastSavedFingerprint = ''
const uploadClientIds = new Map()

/** 当前活跃的标签页索引（0-based，对应 VForm3 tab-pane 数组索引） */
const currentTabIndex = ref(0)
/** 评分卡片强制刷新 key */
const gradingKey = ref(0)

// 在 setup 阶段注册，确保组件卸载时 Vue 会自动停止监听。
watch(currentTabIndex, () => {
  gradingKey.value++
})

// 加载分支结束后 VForm 才会挂载，依赖模板引用触发可避免上传配置早于组件创建。
watch(renderRef, current => {
  if (!current) return
  nextTick(() => {
    if (renderRef.value === current) bindFormInteractions()
  })
}, { flush: 'post' })

/**
 * 从 DOM 中获取当前活跃的标签页索引（0-based）
 * VForm3 渲染的 tab 使用 Element Plus 的 el-tabs 组件
 */
function getCurrentTabIndex() {
  try {
    const formEl = renderRef.value?.$el
    if (!formEl) return 0
    // 查找所有 el-tabs 容器中第一个可见的活跃 tab
    const activeTab = formEl.querySelector('.el-tabs__item.is-active')
    if (!activeTab) return 0
    const tabContainer = activeTab.closest('.el-tabs')
    if (!tabContainer) return 0
    const allTabs = tabContainer.querySelectorAll('.el-tabs__item')
    const index = Array.from(allTabs).indexOf(activeTab)
    return index >= 0 ? index : 0
  } catch (e) {
    return 0
  }
}

function activateTab(page) {
  nextTick(() => {
    const formEl = renderRef.value?.$el
    const tabs = formEl?.querySelectorAll('.el-tabs__item') || []
    const target = tabs[Math.max(0, Number(page || 1) - 1)]
    if (target) {
      target.click()
      updateCurrentTabIndex()
      configureSecureUploads()
    }
  })
}

async function switchToHome() {
  if (props.embedded) {
    if (await ensureCanLeave()) emit('switch-mode', 'daily')
    return
  }
  router.replace('/student/index')
}

async function ensureCanLeave() {
  if (!renderRef.value || !bindingId.value) return true
  if (!submitted.value && await handleSave({ silent: true })) return true
  const currentData = snapshotCurrentFormData()
  const currentFingerprint = createFingerprint(currentData, getCurrentTabIndex() + 1)
  if (!hasUnsavedChanges.value && currentFingerprint === lastSavedFingerprint) return true
  try {
    await ElMessageBox.confirm('当前修改尚未保存，确定离开吗？', '未保存提醒', {
      confirmButtonText: '仍然离开',
      cancelButtonText: '继续填写',
      type: 'warning'
    })
    return true
  } catch (_error) {
    return false
  }
}

onBeforeRouteLeave(() => ensureCanLeave())

function handleCommand(cmd) {
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定注销并退出系统吗？', '提示').then(() => {
      userStore.logOut().then(() => {
        location.href = '/index'
      })
    })
  }
}

function snapshotCurrentFormData() {
  const currentData = renderRef.value?.getFormData(false) || {}
  // VForm3 使用独立内部模型，保存前必须读取当前快照，不能复用初始化数据。
  const snapshot = JSON.parse(JSON.stringify(currentData))
  answerData.value = snapshot
  return snapshot
}

function revisionStorageKey() {
  return `guide-sheet-revision:${bindingId.value}`
}

function restoreClientRevision(serverRevision) {
  const storedRevision = Number(localStorage.getItem(revisionStorageKey()) || 0)
  clientRevision = Math.max(Number(serverRevision || 0), Number.isFinite(storedRevision) ? storedRevision : 0)
  localStorage.setItem(revisionStorageKey(), String(clientRevision))
}

function nextClientRevision() {
  clientRevision += 1
  localStorage.setItem(revisionStorageKey(), String(clientRevision))
  return clientRevision
}

function createFingerprint(formData, currentPage) {
  return JSON.stringify({ formData, currentPage })
}

function buildDraftTask(options = {}) {
  const currentPage = getCurrentTabIndex() + 1
  const formData = snapshotCurrentFormData()
  const fingerprint = createFingerprint(formData, currentPage)
  if (!options.force && fingerprint === lastSavedFingerprint && !pendingSave) {
    return null
  }
  return {
    payload: {
      bindingId: bindingId.value,
      answerJson: JSON.stringify(formData),
      currentPage,
      clientRevision: nextClientRevision(),
      action: 'save'
    },
    fingerprint,
    notify: !options.silent
  }
}

async function flushSaveQueue() {
  if (saveLoopPromise) return saveLoopPromise
  saveLoopPromise = (async () => {
    let successful = true
    saving.value = true
    while (pendingSave) {
      const task = pendingSave
      pendingSave = null
      try {
        const response = await submitGuideSheet(task.payload)
        successful = true
        const savedRevision = Number(response?.savedRevision ?? task.payload.clientRevision)
        clientRevision = Math.max(clientRevision, Number.isFinite(savedRevision) ? savedRevision : 0)
        localStorage.setItem(revisionStorageKey(), String(clientRevision))
        lastSavedFingerprint = task.fingerprint
        lastSavedAt.value = new Date().toLocaleTimeString('zh-CN', {
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        })
        if (task.notify && !pendingSave) ElMessage.success('草稿已保存')
      } catch (_error) {
        successful = false
        if (task.notify) ElMessage.error('草稿暂未保存，请稍后重试')
        if (!pendingSave) break
      }
    }
    hasUnsavedChanges.value = !successful || Boolean(pendingSave)
    return successful
  })()
  try {
    return await saveLoopPromise
  } finally {
    saveLoopPromise = null
    saving.value = false
  }
}

async function handleSave(options = {}) {
  const silent = Boolean(options?.silent)
  if (!formReady.value) {
    if (!silent) ElMessage.warning('这份导学单还没有可填写的学习内容')
    return false
  }
  if (submitted.value) {
    if (!silent) ElMessage.warning('导学单已提交，如需修改请重新提交')
    return false
  }
  if (!renderRef.value || !bindingId.value) return false
  const task = buildDraftTask(options)
  if (!task) {
    hasUnsavedChanges.value = false
    if (!silent) ElMessage.success('当前内容已保存')
    return true
  }
  // 只保留等待队列中的最新快照；已发出的请求完成后再发送，避免并发覆盖。
  pendingSave = task
  hasUnsavedChanges.value = true
  return flushSaveQueue()
}

async function handleSubmit() {
  if (!formReady.value) {
    ElMessage.warning('这份导学单还没有可提交的学习内容')
    return
  }
  if (!renderRef.value || !bindingId.value || submitting.value) return
  submitting.value = true
  if (autoSaveDebounceTimer) {
    clearTimeout(autoSaveDebounceTimer)
    autoSaveDebounceTimer = null
  }
  try {
    // 提交前等待唯一的草稿写入完成，杜绝迟到草稿覆盖最终答卷。
    if (saveLoopPromise || pendingSave) await flushSaveQueue()
    updateCurrentTabIndex()
    const formData = await renderRef.value.getFormData()
    answerData.value = JSON.parse(JSON.stringify(formData || {}))
    const pageIndex = getCurrentTabIndex() + 1
    const data = {
      bindingId: bindingId.value,
      answerJson: JSON.stringify(formData),
      currentPage: pageIndex,
      clientRevision: nextClientRevision(),
      action: 'submit'
    }
    await submitGuideSheet(data)
    submitted.value = true
    hasUnsavedChanges.value = false
    pendingSave = null
    lastSavedFingerprint = createFingerprint(formData, pageIndex)
    ElMessage.success('导学单已提交并批改')
    fetchGradingResult()
  } catch (error) {
    hasUnsavedChanges.value = true
    const message = typeof error === 'string' ? error : error?.message
    ElMessage.error(message || '请检查必答内容后再提交')
  } finally {
    submitting.value = false
  }
}

function handleFieldChange() {
  if (submitting.value) return
  hasUnsavedChanges.value = true
  if (submitted.value) return
  if (autoSaveDebounceTimer) clearTimeout(autoSaveDebounceTimer)
  autoSaveDebounceTimer = setTimeout(() => {
    handleSave({ silent: true })
  }, 2000)
}

/** 更新当前标签页索引（从 DOM 读取） */
function updateCurrentTabIndex() {
  const idx = getCurrentTabIndex()
  currentTabIndex.value = idx
}

/** BUG-08：获取评分结果 */
function fetchGradingResult() {
  if (!bindingId.value) return
  getStudentGrading(bindingId.value).then(res => {
    const result = res?.data || res
    if (result?.hasResult) {
      gradingResult.value = result
    } else {
      // 尚未评分，清空已有结果
      gradingResult.value = null
    }
    gradingKey.value++
  }).catch(() => {})
}

/** 当前标签页名称（从 formJson 中提取的 tab 标签列表） */
const tabLabels = ref([])

/** 当前选中的标签页索引（通过 DOM 检测） */
const currentTabName = computed(() => {
  const idx = currentTabIndex.value
  if (tabLabels.value.length > 0 && idx < tabLabels.value.length) {
    return tabLabels.value[idx]
  }
  return ''
})

/** 解析评分详情 JSON */
function parseGradingDetail(detail) {
  if (!detail) return []
  try {
    return typeof detail === 'string' ? JSON.parse(detail) : detail
  } catch (e) {
    return []
  }
}

/** 按当前标签页索引过滤评分详情 */
const filteredGradingDetails = computed(() => {
  const all = parseGradingDetail(gradingResult.value?.gradingDetail)
  if (!all.length || !tabLabels.value.length) return all
  const tabIdx = currentTabIndex.value
  // 过滤出当前标签页的评分项（后端新增 tabIndex 字段）
  return all.filter(item => {
    // 如果没有 tabIndex 字段（兼容旧数据），显示所有
    if (item.tabIndex === undefined || item.tabIndex === null) return true
    return item.tabIndex === tabIdx
  })
})

/** 当前标签页评分小计 */
const filteredTabScore = computed(() => {
  return filteredGradingDetails.value.reduce((sum, d) => sum + (d.score || 0), 0)
})

/** 当前标签页满分小计 */
const filteredTabMaxScore = computed(() => {
  return filteredGradingDetails.value.reduce((sum, d) => sum + (d.maxScore || 0), 0)
})

/** 评分样式类 */
function scoreClass(row) {
  if (row.score === row.maxScore) return 'score-full'
  if (row.score === 0) return 'score-zero'
  return 'score-partial'
}

/** 从 formJson 中提取 tab-pane 标签列表 */
function extractTabLabels(formJson) {
  const labels = []
  try {
    const json = typeof formJson === 'string' ? JSON.parse(formJson) : formJson
    const visited = new Set()
    function walk(value) {
      if (value == null || typeof value !== 'object' || visited.has(value)) return
      visited.add(value)
      if (Array.isArray(value)) {
        for (const item of value) walk(item)
      } else {
        if (value.type === 'tab' && Array.isArray(value.tabs)) {
          for (const t of value.tabs) {
            labels.push(t.options?.label || t.options?.name || '')
          }
        }
        for (const key of Object.keys(value)) {
          const v = value[key]
          if (v && typeof v === 'object') walk(v)
        }
      }
    }
    walk(json?.widgetList || [])
  } catch (e) { /* ignore */ }
  return labels
}

function visitWidgets(value, visitor, visited = new Set()) {
  if (!value || typeof value !== 'object' || visited.has(value)) return
  visited.add(value)
  if (Array.isArray(value)) {
    value.forEach(item => visitWidgets(item, visitor, visited))
    return
  }
  if (value.type) visitor(value)
  Object.values(value).forEach(child => visitWidgets(child, visitor, visited))
}

function prepareStudentFormJson(source) {
  if (!source || typeof source !== 'object' || !Array.isArray(source.widgetList)) {
    throw new Error('invalid form json')
  }
  if (!source.formConfig || typeof source.formConfig !== 'object') source.formConfig = {}
  let unnamedUploadIndex = 0
  visitWidgets(source.widgetList, widget => {
    if (widget.type !== 'file-upload' && widget.type !== 'picture-upload') return
    widget.options = widget.options && typeof widget.options === 'object' ? widget.options : {}
    if (!widget.options.name) {
      widget.options.name = widget.id || `studentUpload${++unnamedUploadIndex}`
    }
    widget.options.uploadURL = uploadEndpoint
    widget.options.withCredentials = false
    const accessBase = String(import.meta.env.VITE_APP_BASE_API || '').replace(/\/$/, '')
    widget.options.onUploadSuccess = [
      `const accessBase = ${JSON.stringify(accessBase)};`,
      'const payload = result && result.data ? result.data : result;',
      'if (!payload || !payload.fileName || !payload.accessUrl) return null;',
      "const accessUrl = String(payload.accessUrl);",
      "const url = /^(?:https?:)?\\/\\//i.test(accessUrl) ? accessUrl : accessBase + (accessUrl.startsWith('/') ? '' : '/') + accessUrl;",
      'return { name: payload.fileName, url };'
    ].join('\n')
    if (!Array.isArray(widget.options.fileTypes) || widget.options.fileTypes.length === 0) {
      widget.options.fileTypes = widget.type === 'picture-upload'
        ? ['jpg', 'jpeg', 'png', 'gif', 'webp']
        : ['pdf', 'doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx', 'txt', 'jpg', 'jpeg', 'png', 'mp4', 'zip']
    }
  })
  return source
}

function createUploadClientId() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  const randomPart = Math.random().toString(36).slice(2)
  return `${Date.now().toString(36)}-${randomPart}`
}

function configureSecureUploads() {
  if (!renderRef.value || !formJsonObj.value || !bindingId.value) return
  visitWidgets(formJsonObj.value.widgetList, widget => {
    if (widget.type !== 'file-upload' && widget.type !== 'picture-upload') return
    const fieldName = widget.options?.name || widget.id
    const widgetRef = fieldName ? renderRef.value.getWidgetRef(fieldName) : null
    if (!widgetRef) return
    configureStudentUploadWidget(widgetRef, {
      bindingId: bindingId.value,
      fieldName,
      accessBase: String(import.meta.env.VITE_APP_BASE_API || '').replace(/\/$/, ''),
      getAuthorizationHeader,
      getClientUploadId: file => {
        const fileIdentity = file?.uid || `${file?.name}:${file?.size}:${file?.lastModified}`
        const uploadKey = `${fieldName}:${fileIdentity}`
        if (!uploadClientIds.has(uploadKey)) uploadClientIds.set(uploadKey, createUploadClientId())
        return uploadClientIds.get(uploadKey)
      },
      notifyError: message => ElMessage.error(message)
    })
  })
}

function bindFormInteractions() {
  if (formClickTarget) formClickTarget.removeEventListener('click', handleFormClick)
  formClickTarget = renderRef.value?.$el || null
  if (!formClickTarget) return
  formClickTarget.addEventListener('click', handleFormClick)
  configureSecureUploads()
}

function handleFormClick(event) {
  if (!event.target?.closest?.('.el-tabs__item')) return
  nextTick(() => {
    updateCurrentTabIndex()
    configureSecureUploads()
  })
}

function parseAnswerJson(value) {
  if (!value) return {}
  try {
    const parsed = typeof value === 'string' ? JSON.parse(value) : value
    return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {}
  } catch (_error) {
    return {}
  }
}

onMounted(async () => {
  studentName.value = userStore.nickName || userStore.name || '同学'
  try {
    const res = await getStudentGuideSheet()
    if (res.blockedByCountyExam) {
      loading.value = false
      await router.replace('/student/county-exam')
      return
    }
    if (props.expectedBindingId && res.bindingId
        && String(props.expectedBindingId) !== String(res.bindingId)) {
      hasSheet.value = false
      loading.value = false
      ElMessage.error('课程导学单上下文已变化，请刷新课程后重试')
      if (props.embedded) emit('switch-mode', 'daily')
      return
    }
    hasSheet.value = Boolean(res.hasSheet && res.bindingId)
    if (hasSheet.value) {
      sheetTitle.value = res.sheetTitle || '电子导学单'
      bindingId.value = res.bindingId
      try {
        const parsed = typeof res.formJson === 'string' ? JSON.parse(res.formJson) : res.formJson
        const prepared = prepareStudentFormJson(parsed)
        if (hasRenderableWidgets(prepared)) {
          formJsonObj.value = prepared
          formReady.value = true
          tabLabels.value = extractTabLabels(prepared)
        } else {
          formJsonObj.value = null
          formLoadError.value = '这份导学单还没有学习内容，请联系任课教师补充后再试。'
        }
      } catch (_error) {
        formLoadError.value = '导学单内容暂时无法显示，请联系任课教师检查模板。'
      }
      maxPages.value = res.maxPages || 0
      if (res.websocketPath) {
        websocketClient.connect(res.websocketPath)
        websocketUnsubscribe = websocketClient.on('message', payload => {
          if (payload?.type === 'message') teacherMsg.value = payload.content || ''
          if (payload?.type === 'refresh') window.location.reload()
          if (payload?.type === 'page_change') activateTab(payload.page)
        })
      }
      const existing = res.existingAnswer
      answerData.value = normalizeStudentFormData(
        formJsonObj.value,
        parseAnswerJson(existing?.answerJson)
      )
      submitted.value = existing?.status === '2'
      restoreClientRevision(existing?.draftRevision)
      currentTabIndex.value = Math.max(0, Number(existing?.currentPage || 1) - 1)
      lastSavedFingerprint = createFingerprint(answerData.value, currentTabIndex.value + 1)
      if (submitted.value) fetchGradingResult()

      heartbeatTimer = setInterval(() => {
        const pageIndex = getCurrentTabIndex() + 1
        sendHeartbeat({ bindingId: bindingId.value, currentPage: pageIndex }).catch(() => {})
      }, 30000)
      autoSaveTimer = setInterval(() => {
        if (!submitted.value) handleSave({ silent: true })
      }, 30000)
      if (formReady.value && existing?.currentPage) activateTab(existing.currentPage)
    }
    loading.value = false
  } catch (error) {
    const message = String(error?.message || '')
    if (message.includes('区域抽测')) {
      await router.replace('/student/county-exam')
    } else {
      ElMessage.error('加载导学单失败，请稍后重试')
    }
    loading.value = false
  }

  window.addEventListener('beforeunload', onBeforeUnload)
})

function onBeforeUnload(event) {
  if (!renderRef.value || !bindingId.value) return
  const currentData = snapshotCurrentFormData()
  const pageIndex = getCurrentTabIndex() + 1
  const fingerprint = createFingerprint(currentData, pageIndex)
  const isDirty = hasUnsavedChanges.value || fingerprint !== lastSavedFingerprint
  if (!isDirty) return
  if (!submitted.value && Object.keys(currentData).length > 0) {
    const data = {
      bindingId: bindingId.value,
      answerJson: JSON.stringify(currentData),
      currentPage: pageIndex,
      clientRevision: nextClientRevision(),
      action: 'save'
    }
    fetch(guideSheetSubmitUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...getAuthorizationHeader()
      },
      body: JSON.stringify(data),
      keepalive: true
    }).catch(() => {})
  }
  event.preventDefault()
  event.returnValue = ''
}

onBeforeUnmount(() => {
  if (websocketUnsubscribe) websocketUnsubscribe()
  websocketClient.disconnect()
  if (heartbeatTimer) clearInterval(heartbeatTimer)
  if (autoSaveTimer) clearInterval(autoSaveTimer)
  if (autoSaveDebounceTimer) clearTimeout(autoSaveDebounceTimer)
  if (formClickTarget) formClickTarget.removeEventListener('click', handleFormClick)
  window.removeEventListener('beforeunload', onBeforeUnload)
})

defineExpose({ ensureCanLeave })
</script>

<style scoped>
.student-guide-sheet {
  background-color: #f5f7fa;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.student-guide-sheet.embedded {
  min-height: calc(100vh - 64px);
}

/* 头部导航 - 与智慧课堂学生端保持一致的视觉风格 */
.dashboard-header {
  height: 64px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 0 32px;
  position: sticky;
  top: 0;
  z-index: 2000;
  flex-shrink: 0;
}
.dashboard-header__inner {
  width: 100%;
  max-width: 1200px;
  height: 100%;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.view-toggle {
  display: flex;
  align-items: center;
  gap: 0;
  margin-left: 16px;
}
.view-toggle .el-button {
  border-radius: 0;
}
.view-toggle .el-button:first-child {
  border-radius: 4px 0 0 4px;
}
.view-toggle .el-button:last-child {
  border-radius: 0 4px 4px 0;
}
.logo {
  height: 32px;
}
.platform-name {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
}
.header-right {
  display: flex;
  align-items: center;
}
.header-right .user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 20px;
  transition: background 0.2s;
}
.header-right .user-info:hover {
  background: #f0f2f5;
}
.user-name {
  font-weight: 500;
  font-size: 14px;
}

/* 加载/空状态 */
.loading-state, .empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 16px;
}
.loading-state { gap: 16px; }

/* 导学单内容区 */
.sheet-wrapper {
  flex: 1;
  width: 100%;
  padding: 24px;
  overflow-y: auto;
  box-sizing: border-box;
}
.sheet-container {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  width: 100%;
  max-width: 1000px;
  margin: 0 auto;
  box-sizing: border-box;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.sheet-operations {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #409EFF;
}
.left-info {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}
.sheet-heading {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 3px;
}
.sheet-title {
  overflow: hidden;
  color: #1f2937;
  font-size: 17px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.save-time {
  color: #7b8492;
  font-size: 12px;
}
.right-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.form-wrapper {
  max-width: 900px;
  margin: 0 auto;
}
.teacher-msg-bar {
  max-width: 900px;
  margin: 0 auto 12px;
}
.form-error {
  max-width: 900px;
  margin: 0 auto;
}

/* BUG-08：独立评分结果卡片（提交前隐藏，提交后显示） */
.grading-card {
  max-width: 1000px;
  margin: 16px auto;
}
.grading-card-header {
  display: flex;
  align-items: center;
  gap: 16px;
}
.grading-card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.grading-tab-hint {
  margin-left: auto;
  font-size: 13px;
  color: #909399;
}
.score-full { color: #67C23A; font-weight: 600; }
.score-zero { color: #F56C6C; font-weight: 600; }
.score-partial { color: #E6A23C; font-weight: 600; }
@media (max-width: 768px) {
  .dashboard-header {
    height: auto;
    min-height: 60px;
    padding: 8px 12px;
  }
  .dashboard-header__inner {
    gap: 8px;
  }
  .header-left {
    min-width: 0;
    gap: 8px;
  }
  .logo {
    height: 28px;
  }
  .platform-name {
    overflow: hidden;
    max-width: 112px;
    font-size: 15px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .view-toggle {
    margin-left: 4px;
  }
  .header-right .user-info {
    padding: 4px;
  }
  .user-name {
    display: none;
  }
  .sheet-wrapper {
    padding: 12px;
  }
  .sheet-container {
    padding: 16px;
  }
  .sheet-operations {
    align-items: stretch;
    flex-direction: column;
    gap: 14px;
  }
  .left-info {
    align-items: flex-start;
    justify-content: space-between;
  }
  .sheet-heading {
    flex: 1;
  }
  .right-actions {
    display: grid;
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  }
  .right-actions :deep(.el-button) {
    width: 100%;
    margin-left: 0;
  }
  .grading-card-header {
    align-items: flex-start;
    flex-wrap: wrap;
    gap: 8px 12px;
  }
  .grading-tab-hint {
    width: 100%;
    margin-left: 0;
  }
}
</style>
