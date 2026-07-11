<template>
  <div class="app-container county-grading-page" ref="gradingPageRef">
    <div class="grading-header" v-show="!isFullscreen">
      <div class="header-title">
        <h2>区域抽测评卷</h2>
        <span>匿名答卷仅显示题目、文件和评卷状态</span>
      </div>
      <div class="header-actions">
        <el-radio-group v-model="query.gradingStatus" @change="() => loadTasks(false)">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="0">待评</el-radio-button>
          <el-radio-button value="1">已评</el-radio-button>
        </el-radio-group>
        <el-button :icon="Refresh" @click="loadTasks">刷新</el-button>
        <el-button type="primary" plain :icon="FullScreen" @click="toggleFullscreen">
          {{ isFullscreen ? '退出全屏' : '全屏评卷' }}
        </el-button>
      </div>
    </div>

    <div class="grading-main" v-loading="loading">
      <aside class="task-list-panel">
        <div class="panel-title">
          <span>匿名答卷</span>
          <em>待评 {{ pendingCount }} / {{ submittedCount }}</em>
        </div>
        <el-scrollbar class="task-scroll">
          <button
            v-for="(task, index) in tasks"
            :key="task.answerId"
            class="task-item"
            :class="{ active: currentAnswerId === task.answerId, done: task.gradingStatus === '1' }"
            @click="selectTask(task.answerId)"
          >
            <span class="task-seq">答卷 #{{ index + 1 }}</span>
            <strong>{{ task.examName }}</strong>
            <span class="task-question">{{ trimText(task.questionContent, 46) }}</span>
            <span class="task-bottom">
              <el-tag size="small" :type="task.gradingStatus === '1' ? 'success' : 'warning'">
                {{ task.gradingStatus === '1' ? '已评' : '待评' }}
              </el-tag>
              <em>{{ previewStatusLabel(task) }}</em>
            </span>
          </button>
          <el-empty v-if="!loading && tasks.length === 0" description="暂无评卷任务" :image-size="80" />
        </el-scrollbar>
      </aside>

      <section class="preview-panel" v-loading="detailLoading">
        <template v-if="detail">
          <div class="preview-header">
            <div>
              <el-tag effect="plain">答卷 #{{ currentTaskNumber }}</el-tag>
              <h3>{{ detail.questionContent }}</h3>
              <p>{{ detail.examName }} · 满分 {{ maxScore }} 分</p>
            </div>
            <el-button v-if="detail.studentAnswer" :icon="Download" @click="openFile(detail.studentAnswer)">下载原文件</el-button>
          </div>
          <iframe v-if="previewUrl" :src="previewUrl" class="preview-frame" />
          <div v-else class="preview-empty">
            <el-icon><Document /></el-icon>
            <strong>{{ previewEmptyTitle }}</strong>
            <span>{{ previewEmptyDesc }}</span>
          </div>
        </template>
        <el-empty v-else description="请选择一份匿名答卷" />
      </section>

      <aside class="score-panel" v-if="detail">
        <div class="score-card">
          <div class="score-title">
            <span>评分</span>
            <el-tag :type="detail.gradingStatus === '1' ? 'success' : 'warning'" size="small">
              {{ detail.gradingStatus === '1' ? '已评' : '待评' }}
            </el-tag>
          </div>

          <div class="score-switch" v-if="scoringItems.length">
            <el-switch v-model="useItemScoring" active-text="分项评分" inactive-text="直接打分" />
          </div>

          <div v-if="useItemScoring && scoringItems.length" class="item-score-list">
            <div v-for="(item, index) in scoringItems" :key="item.itemId" class="item-score-row">
              <span>{{ item.itemName }}</span>
              <div>
                <el-input-number
                  :ref="el => setItemInputRef(el, index)"
                  v-model="itemScores[item.itemId]"
                  :min="0"
                  :max="Number(item.maxScore || 0)"
                  :precision="0"
                  controls-position="right"
                  @keyup.enter="onItemEnter(index)"
                />
                <em>/ {{ item.maxScore || 0 }}</em>
              </div>
            </div>
            <div class="score-total">
              <span>合计</span>
              <strong>{{ totalItemScore }}</strong>
            </div>
          </div>

          <el-form v-else label-position="top">
            <el-form-item label="得分">
              <el-input-number
                ref="manualScoreRef"
                v-model="manualScore"
                :min="0"
                :max="maxScore"
                :precision="0"
                controls-position="right"
                @keyup.enter="submitGrade"
              />
            </el-form-item>
          </el-form>

          <el-button type="primary" class="submit-btn" :icon="Check" :loading="submitting" @click="submitGrade">
            提交并下一份
          </el-button>
          <div class="nav-actions">
            <el-button :icon="ArrowLeft" :disabled="currentIndex <= 0" @click="prevTask">上一份</el-button>
            <el-button :icon="ArrowRight" :disabled="currentIndex >= tasks.length - 1" @click="nextTask">下一份</el-button>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup name="CountyExamGrading">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight, Check, Document, Download, FullScreen, Refresh } from '@element-plus/icons-vue'
import {
  getCountyExamGradingAnswer,
  getCountyExamGradingTasks,
  gradeCountyExamAnswer
} from '@/api/business/countyExam'

const loading = ref(false)
const detailLoading = ref(false)
const submitting = ref(false)
const tasks = ref([])
const detail = ref(null)
const currentAnswerId = ref(null)
const scoringItems = ref([])
const itemScores = reactive({})
const itemInputRefs = ref([])
const manualScore = ref(0)
const manualScoreRef = ref(null)
const useItemScoring = ref(false)
const gradingPageRef = ref(null)
const isFullscreen = ref(false)
const query = reactive({ gradingStatus: '0' })

const submittedCount = computed(() => tasks.value.length)
const gradedCount = computed(() => tasks.value.filter(item => item.gradingStatus === '1').length)
const pendingCount = computed(() => Math.max(submittedCount.value - gradedCount.value, 0))
const currentIndex = computed(() => tasks.value.findIndex(item => item.answerId === currentAnswerId.value))
const currentTaskNumber = computed(() => currentIndex.value >= 0 ? currentIndex.value + 1 : '-')
const maxScore = computed(() => Number(detail.value?.questionScore || 0))
const previewUrl = computed(() => {
  if (!detail.value?.previewPath) return ''
  return import.meta.env.VITE_APP_BASE_API + '/common/resource/view?resource=' + encodeURIComponent(detail.value.previewPath)
})
const previewEmptyTitle = computed(() => {
  if (detail.value?.previewStatus === 'pending') return '预览等待生成'
  if (detail.value?.previewStatus === 'converting') return '预览正在生成'
  if (detail.value?.previewStatus === 'failed') return '预览暂不可用'
  return '暂无可预览文件'
})
const previewEmptyDesc = computed(() => {
  if (detail.value?.previewStatus === 'failed') return '可下载原文件完成评阅，后台会继续记录转换诊断。'
  if (['pending', 'converting'].includes(detail.value?.previewStatus)) return '请稍候刷新，或先下载原文件查看。'
  return '该答卷没有生成预览文件。'
})
const totalItemScore = computed(() => {
  const total = scoringItems.value.reduce((sum, item) => sum + Number(itemScores[item.itemId] || 0), 0)
  return Number(total.toFixed(1))
})

async function loadTasks(keepCurrent = true) {
  loading.value = true
  try {
    const previousAnswerId = currentAnswerId.value
    const response = await getCountyExamGradingTasks({ gradingStatus: query.gradingStatus })
    tasks.value = response.data || []
    if (tasks.value.length === 0) {
      clearCurrentTask()
      return
    }
    const preserved = keepCurrent ? tasks.value.find(item => item.answerId === previousAnswerId) : null
    const nextTask = preserved || tasks.value.find(item => item.gradingStatus !== '1') || tasks.value[0]
    await selectTask(nextTask.answerId)
  } finally {
    loading.value = false
  }
}

async function selectTask(answerId) {
  currentAnswerId.value = answerId
  detailLoading.value = true
  try {
    const response = await getCountyExamGradingAnswer(answerId)
    detail.value = response.data
    scoringItems.value = response.data?.scoringItems || []
    Object.keys(itemScores).forEach(key => delete itemScores[key])
    const saved = {}
    ;(response.data?.scoringDetails || []).forEach(item => {
      saved[item.itemId] = item.score
    })
    scoringItems.value.forEach(item => {
      itemScores[item.itemId] = Number(saved[item.itemId] || 0)
    })
    useItemScoring.value = scoringItems.value.length > 0
    manualScore.value = Number(response.data?.score || 0)
    focusScoreInput()
  } finally {
    detailLoading.value = false
  }
}

async function submitGrade() {
  if (!detail.value) return
  const score = useItemScoring.value && scoringItems.value.length ? totalItemScore.value : Number(manualScore.value || 0)
  if (score < 0) {
    ElMessage.warning('得分不能为负数')
    return
  }
  if (score > maxScore.value) {
    ElMessage.warning(`得分不能超过题目满分 ${maxScore.value} 分`)
    return
  }
  const previousIndex = currentIndex.value
  submitting.value = true
  try {
    await gradeCountyExamAnswer({
      answerId: detail.value.answerId,
      score,
      scoringDetails: useItemScoring.value
        ? scoringItems.value.map(item => ({ itemId: item.itemId, score: Number(itemScores[item.itemId] || 0) }))
        : null
    })
    ElMessage.success('评分已提交')
    await reloadAfterSubmit(previousIndex)
  } finally {
    submitting.value = false
  }
}

async function reloadAfterSubmit(previousIndex) {
  const response = await getCountyExamGradingTasks({ gradingStatus: query.gradingStatus })
  tasks.value = response.data || []
  if (tasks.value.length === 0) {
    clearCurrentTask()
    return
  }
  const nextTask = tasks.value.find((item, index) => index >= previousIndex && item.gradingStatus !== '1')
    || tasks.value.find(item => item.gradingStatus !== '1')
    || tasks.value[Math.min(previousIndex + 1, tasks.value.length - 1)]
    || tasks.value[0]
  await selectTask(nextTask.answerId)
}

function clearCurrentTask() {
  detail.value = null
  currentAnswerId.value = null
  scoringItems.value = []
  Object.keys(itemScores).forEach(key => delete itemScores[key])
}

function prevTask() {
  if (currentIndex.value <= 0) return
  selectTask(tasks.value[currentIndex.value - 1].answerId)
}

function nextTask() {
  if (currentIndex.value >= tasks.value.length - 1) {
    ElMessage.info('已经是最后一份答卷')
    return
  }
  selectTask(tasks.value[currentIndex.value + 1].answerId)
}

function setItemInputRef(el, index) {
  if (el) itemInputRefs.value[index] = el
}

function onItemEnter(index) {
  if (index < scoringItems.value.length - 1) {
    focusNumberInput(itemInputRefs.value[index + 1])
    return
  }
  submitGrade()
}

function focusScoreInput() {
  nextTick(() => {
    if (useItemScoring.value && scoringItems.value.length) {
      focusNumberInput(itemInputRefs.value[0])
      return
    }
    focusNumberInput(manualScoreRef.value)
  })
}

function focusNumberInput(inputRef) {
  setTimeout(() => {
    const input = inputRef?.$el?.querySelector?.('input')
    input?.focus?.()
    input?.select?.()
  }, 80)
}

function previewStatusLabel(task) {
  if (task.previewPath || task.previewStatus === 'success') return '可预览'
  if (task.previewStatus === 'pending') return '待转换'
  if (task.previewStatus === 'converting') return '转换中'
  if (task.previewStatus === 'failed') return '预览暂不可用'
  return '已提交'
}

function openFile(path) {
  if (!path) return
  window.open(import.meta.env.VITE_APP_BASE_API + path, '_blank')
}

function trimText(text, length) {
  if (!text) return ''
  return text.length > length ? text.slice(0, length) + '...' : text
}

function toggleFullscreen() {
  if (!document.fullscreenElement) {
    gradingPageRef.value?.requestFullscreen?.()
    return
  }
  document.exitFullscreen?.()
}

function handleFullscreenChange() {
  isFullscreen.value = !!document.fullscreenElement
}

function handleKeydown(event) {
  if (event.key === 'PageUp') {
    event.preventDefault()
    prevTask()
  }
  if (event.key === 'PageDown') {
    event.preventDefault()
    nextTask()
  }
}

onMounted(() => {
  loadTasks(false)
  document.addEventListener('fullscreenchange', handleFullscreenChange)
  document.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped lang="scss">
.county-grading-page {
  height: calc(100vh - 84px);
  min-height: 680px;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;

  &:fullscreen {
    height: 100vh;
    min-height: 100vh;
    padding: 16px;
    background: #f5f7fa;
  }
}

.grading-header {
  min-height: 68px;
  padding: 12px 16px;
  margin-bottom: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.header-title {
  h2 {
    margin: 0 0 4px;
    font-size: 20px;
    letter-spacing: 0;
  }

  span {
    color: #606266;
    font-size: 13px;
  }
}

.header-actions,
.nav-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.grading-main {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr) 330px;
  gap: 12px;
}

.task-list-panel,
.preview-panel,
.score-panel {
  min-height: 0;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.task-list-panel {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-title {
  min-height: 52px;
  padding: 12px;
  border-bottom: 1px solid #eef0f3;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  font-weight: 700;

  em {
    color: #606266;
    font-size: 12px;
    font-style: normal;
    font-weight: 400;
  }
}

.task-scroll {
  flex: 1;
  padding: 10px;
}

.task-item {
  width: 100%;
  min-height: 112px;
  margin-bottom: 8px;
  padding: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.16s, background 0.16s;

  &:hover,
  &.active {
    border-color: #409eff;
    background: #ecf5ff;
  }

  &.done {
    border-color: #d1ead2;
  }

  strong,
  span,
  em {
    display: block;
  }
}

.task-seq {
  color: #409eff;
  font-weight: 700;
  font-size: 13px;
}

.task-question {
  min-height: 38px;
  color: #606266;
  line-height: 1.45;
  margin: 5px 0 8px;
}

.task-bottom {
  display: flex !important;
  align-items: center;
  justify-content: space-between;
  gap: 8px;

  em {
    color: #909399;
    font-size: 12px;
    font-style: normal;
  }
}

.preview-panel {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.preview-header {
  min-height: 96px;
  padding: 14px 16px;
  border-bottom: 1px solid #eef0f3;
  display: flex;
  justify-content: space-between;
  gap: 12px;

  h3 {
    margin: 10px 0 6px;
    font-size: 17px;
    line-height: 1.5;
  }

  p {
    margin: 0;
    color: #606266;
  }
}

.preview-frame {
  flex: 1;
  width: 100%;
  min-height: 0;
  border: 0;
  background: #fff;
}

.preview-empty {
  flex: 1;
  min-height: 360px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #606266;
  background: #f8fafc;

  .el-icon {
    font-size: 42px;
    color: #909399;
  }

  strong {
    color: #303133;
  }
}

.score-panel {
  padding: 12px;
  overflow: auto;
}

.score-card {
  display: grid;
  gap: 14px;
}

.score-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eef0f3;
  font-weight: 700;
}

.score-switch {
  display: flex;
  justify-content: center;
}

.item-score-list {
  display: grid;
  gap: 10px;
}

.item-score-row {
  display: grid;
  gap: 6px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #e5e7eb;

  > span {
    color: #303133;
    line-height: 1.45;
  }

  > div {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  em {
    color: #909399;
    font-style: normal;
  }
}

.score-total {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border-radius: 8px;
  background: #f8fafc;

  strong {
    font-size: 22px;
    color: #409eff;
  }
}

.submit-btn {
  width: 100%;
}

.nav-actions {
  justify-content: space-between;

  .el-button {
    flex: 1;
  }
}

@media (max-width: 1180px) {
  .grading-main {
    grid-template-columns: 270px minmax(0, 1fr);
  }

  .score-panel {
    grid-column: 1 / -1;
  }
}
</style>
