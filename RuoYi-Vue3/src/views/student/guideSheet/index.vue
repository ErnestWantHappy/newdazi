<template>
  <div class="student-guide-sheet">
    <!-- 顶部导航栏 - 与主页统一 -->
    <header class="dashboard-header">
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
            <el-tag v-if="submitted" type="success" size="large">
              <el-icon><CircleCheckFilled /></el-icon> 已提交（可重新提交）
            </el-tag>
            <el-tag v-else type="warning" size="large">待完成</el-tag>
          </div>
          <div class="right-actions">
            <el-button icon="Check" type="primary" size="large" @click="handleSubmit" :loading="submitting">
              提交导学单
            </el-button>
            <el-button icon="Refresh" size="large" @click="handleSave" :loading="saving">
              保存草稿
            </el-button>
          </div>
        </div>

        <div v-if="teacherMsg" class="teacher-msg-bar">
          <el-alert :title="teacherMsg" type="warning" show-icon :closable="true" @close="teacherMsg = ''" />
        </div>

        <div class="form-wrapper">
          <v-form-render
            ref="renderRef"
            :form-json="formJsonObj"
            :form-data="answerData"
            :option-data="optionData"
          />
        </div>

        <div v-if="teacherMachineIp" class="upload-hint">
          <el-alert type="info" :closable="false" show-icon>
            <template #title>文件上传地址：{{ teacherMachineIp }}:5000</template>
            图片/视频等大文件将直接上传到教师机本地服务器
          </el-alert>
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
            <el-table-column prop="referenceAnswer" label="参考答案" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">
                <template v-if="row.desc !== '未作答'">
                  <span v-if="row.referenceAnswer">{{ row.referenceAnswer }}</span>
                  <span v-else class="ai-comment-empty">-</span>
                </template>
                <span v-else class="ai-comment-empty">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="desc" label="评语" min-width="160" show-overflow-tooltip />
            <el-table-column label="AI 评语" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">
                <span v-if="row.aiComment" class="ai-comment">{{ row.aiComment }}</span>
                <span v-else class="ai-comment-empty">-</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup name="StudentGuideSheet">
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getStudentGuideSheet, submitGuideSheet, sendHeartbeat, getStudentGrading } from '@/api/business/guideSheet'
import { setTeacherMachineIp } from '@/utils/teacherMachine'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, CircleCheckFilled } from '@element-plus/icons-vue'
import useUserStore from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const hasSheet = ref(false)
const sheetTitle = ref('')
const teacherMachineIp = ref('')
const sheetId = ref(null)
const formJsonObj = ref(null)
const answerData = ref({})
const optionData = ref({})
const maxPages = ref(0)
const submitted = ref(false)
const submitting = ref(false)
const saving = ref(false)
const teacherMsg = ref('')
const renderRef = ref(null)
const gradingResult = ref(null)  // BUG-08：评分结果

const studentName = ref('')

let heartbeatTimer = null
let autoSaveTimer = null
let abortController = null
let tabWatchTimer = null  // 定时检测标签页切换

/** 当前活跃的标签页索引（0-based，对应 VForm3 tab-pane 数组索引） */
const currentTabIndex = ref(0)
/** 评分卡片强制刷新 key */
const gradingKey = ref(0)

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

function switchToHome() {
  router.replace('/student/index')
}

function handleCommand(cmd) {
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定注销并退出系统吗？', '提示').then(() => {
      userStore.logOut().then(() => {
        location.href = '/index'
      })
    })
  }
}

function handleSave() {
  if (!renderRef.value) return
  // 取消上一次未完成的保存请求（ARCH-04 防抖）
  if (abortController) {
    abortController.abort()
  }
  abortController = new AbortController()
  saving.value = true
  const pageIndex = getCurrentTabIndex() + 1  // 1-based
  const data = {
    sheetId: sheetId.value,
    answerJson: JSON.stringify(answerData.value),
    currentPage: pageIndex,
    action: 'save'
  }
  submitGuideSheet(data).then(() => {
    ElMessage.success('草稿已保存')
  }).catch((err) => {
    if (err?.name !== 'AbortError' && err?.code !== 'ERR_CANCELED') {
      console.warn('保存草稿失败', err)
    }
  }).finally(() => { saving.value = false })
}

function handleSubmit() {
  if (!renderRef.value) return
  submitting.value = true
  // 分页批改：提交前先同步当前标签页索引
  updateCurrentTabIndex()
  renderRef.value.getFormData().then(formData => {
    const pageIndex = getCurrentTabIndex() + 1  // 1-based (传给后端 currentPage)
    const tabIndex = currentTabIndex.value       // 0-based (传给后端 tabIndex)
    const data = {
      sheetId: sheetId.value,
      answerJson: JSON.stringify(formData),
      currentPage: pageIndex,
      action: 'submit',
      tabIndex: tabIndex  // 分页批改：提交当前标签页索引，后端仅评分此页
    }
    submitGuideSheet(data).then(() => {
      submitted.value = true
      ElMessage.success('当前页已提交并批改')
      // 立即获取评分结果
      fetchGradingResult()
    }).finally(() => { submitting.value = false })
  }).catch(error => {
    ElMessage.error(error || '表单验证失败')
    submitting.value = false
  })
}

/** 更新当前标签页索引（从 DOM 读取） */
function updateCurrentTabIndex() {
  const idx = getCurrentTabIndex()
  currentTabIndex.value = idx
}

/** BUG-08：获取评分结果 */
function fetchGradingResult() {
  if (!sheetId.value) return
  getStudentGrading(sheetId.value).then(res => {
    if (res.data?.hasResult) {
      gradingResult.value = res.data
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

onMounted(() => {
  studentName.value = userStore.nickName || userStore.name || '同学'

  getStudentGuideSheet().then(res => {
    hasSheet.value = res.hasSheet || false
    if (hasSheet.value) {
      sheetTitle.value = res.sheetTitle || ''
      sheetId.value = res.sheetId
      if (res.formJson) {
        try {
          const parsed = JSON.parse(res.formJson)
          formJsonObj.value = parsed
          tabLabels.value = extractTabLabels(res.formJson)
        } catch (e) {
          console.warn('表单JSON解析失败', e)
        }
      }
      maxPages.value = res.maxPages || 0
      teacherMachineIp.value = res.teacherMachineIp || ''
      if (teacherMachineIp.value) {
        setTeacherMachineIp(teacherMachineIp.value)
      }
      const existing = res.existingAnswer
      if (existing && existing.status === '2' && existing.answerJson) {
        submitted.value = true
        try {
          answerData.value = JSON.parse(existing.answerJson) || {}
        } catch (e) {
          answerData.value = {}
        }
        // BUG-08：已提交时加载评分结果
        fetchGradingResult()
      } else if (existing && existing.answerJson) {
        try {
          answerData.value = JSON.parse(existing.answerJson) || {}
        } catch (e) {
          answerData.value = {}
        }
      }
      // 心跳定时器：每 30 秒上报当前所在标签页
      heartbeatTimer = setInterval(() => {
        const pageIndex = getCurrentTabIndex() + 1  // 1-based
        sendHeartbeat({ sheetId: sheetId.value, currentPage: pageIndex }).catch(() => {})
      }, 30000)
      // 自动保存定时器
      autoSaveTimer = setInterval(() => {
        handleSave()
      }, 30000)
      // 标签页切换监听：每 500ms 检测一次 DOM 中活跃标签页的变化
      nextTick(() => {
        setTimeout(() => {
          updateCurrentTabIndex()
          tabWatchTimer = setInterval(updateCurrentTabIndex, 500)
        }, 300)
      })
      // 监听标签页切换，实时更新评分结果过滤
      watch(currentTabIndex, () => {
        gradingKey.value++
      })
    }
    loading.value = false
  }).catch(() => {
    ElMessage.error('加载导学单失败')
    loading.value = false
  })

  // 页面关闭前保存
  window.addEventListener('beforeunload', onBeforeUnload)
})

function onBeforeUnload() {
  if (answerData.value && Object.keys(answerData.value).length > 0) {
    const pageIndex = getCurrentTabIndex() + 1  // 1-based
    const data = {
      sheetId: sheetId.value,
      answerJson: JSON.stringify(answerData.value),
      currentPage: pageIndex,
      action: 'save'
    }
    navigator.sendBeacon
      ? navigator.sendBeacon('/dev-api/business/guide-sheet/student/submit',
          new Blob([JSON.stringify(data)], { type: 'application/json' }))
      : submitGuideSheet(data)
  }
}

onBeforeUnmount(() => {
  if (heartbeatTimer) clearInterval(heartbeatTimer)
  if (autoSaveTimer) clearInterval(autoSaveTimer)
  if (tabWatchTimer) clearInterval(tabWatchTimer)
  if (abortController) abortController.abort()
  window.removeEventListener('beforeunload', onBeforeUnload)
})
</script>

<style scoped>
.student-guide-sheet {
  background-color: #f5f7fa;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 头部导航 - 与智慧课堂学生端保持一致的视觉风格 */
.dashboard-header {
  height: 64px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32px;
  position: sticky;
  top: 0;
  z-index: 2000;
  flex-shrink: 0;
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
  padding: 24px;
  overflow-y: auto;
}
.sheet-container {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  max-width: 1000px;
  margin: 0 auto;
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
  gap: 8px;
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
.upload-hint {
  max-width: 900px;
  margin: 20px auto 0;
}

/* BUG-08：独立评分结果卡片（提交前隐藏，提交后显示） */
.grading-card {
  max-width: 1000px;
  margin: 0 auto 16px;
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
.ai-comment { color: #909399; font-style: italic; font-size: 12px; }
.ai-comment-empty { color: #C0C4CC; font-size: 12px; }
</style>