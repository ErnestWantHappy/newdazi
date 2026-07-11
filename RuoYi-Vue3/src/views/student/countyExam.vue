<template>
  <div class="county-student-page">
    <header class="page-header">
      <div class="header-left">
        <img src="@/assets/logo/logo.png" class="logo" alt="Logo" />
        <span class="platform-name">智慧课堂 - 区域抽测</span>
      </div>
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="34" shape="circle" icon="UserFilled" />
          <span>{{ studentInfo.studentName || '同学' }}</span>
          <el-icon><CaretBottom /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="password">修改密码</el-dropdown-item>
            <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </header>

    <main class="main-content">
      <div v-if="loading" class="loading-box">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在加载区域抽测...</span>
      </div>

      <el-empty v-else-if="!hasExam" description="暂无区域抽测" />

      <section v-else-if="ended" class="ended-panel">
        <h1>{{ examName }}</h1>
        <p>{{ endedMessage }}</p>
        <el-tag type="info" effect="plain">区域抽测已结束</el-tag>
      </section>

      <template v-else>
        <section class="exam-banner">
          <div>
            <h1>{{ examName }}</h1>
            <div class="student-tags">
              <el-tag type="info">{{ studentInfo.deptName || '-' }}</el-tag>
              <el-tag type="success">{{ studentInfo.gradeName || '' }}{{ studentInfo.classCode || '' }}班</el-tag>
              <el-tag type="warning">{{ studentInfo.studentName || '同学' }}</el-tag>
            </div>
          </div>
          <div class="timer-box" :class="{ warning: remainingSeconds <= 300 }">
            <span>剩余时间</span>
            <strong>{{ formatTime(remainingSeconds) }}</strong>
          </div>
        </section>

        <section v-if="typingQuestions.length" class="section-block">
          <div class="section-title">
            <el-icon><Monitor /></el-icon> 打字练习
          </div>

          <div
            v-for="question in typingQuestions"
            :key="question.questionId"
            class="typing-panel"
          >
            <div class="typing-stats">
              <div class="stat-item time">
                <label>剩余时间</label>
                <span :class="{ warning: (typingStates[question.questionId]?.timeLeft || 0) < 60 }">
                  {{ formatTime(typingStates[question.questionId]?.timeLeft || 0) }}
                </span>
              </div>
              <div class="stat-item">
                <label>总字数</label>
                <span>{{ question.wordCount || question.questionContent?.length || 0 }}</span>
              </div>
              <div class="stat-item">
                <label>完成字数</label>
                <span>{{ typingStates[question.questionId]?.completedCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <label>错误字数</label>
                <span class="error-text">{{ typingStates[question.questionId]?.errorCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <label>正确字数</label>
                <span class="success-text">{{ typingStates[question.questionId]?.correctCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <label>正确率</label>
                <span>{{ typingStates[question.questionId]?.accuracy || 100 }}%</span>
              </div>
              <div class="stat-item highlight">
                <label>打字速度</label>
                <span>{{ typingStates[question.questionId]?.speed || 0 }} 字/分</span>
              </div>
              <div class="stat-item">
                <label>完成率</label>
                <span>{{ typingStates[question.questionId]?.progress || 0 }}%</span>
              </div>

              <div class="action-buttons">
                <el-button
                  v-if="!typingStates[question.questionId]?.started"
                  type="success"
                  class="action-btn"
                  :disabled="isReadOnly"
                  @click="startTypingPractice(question.questionId)"
                >
                  开始练习
                </el-button>
                <el-button
                  v-else
                  type="primary"
                  class="action-btn"
                  :loading="typingStates[question.questionId]?.submitting"
                  :disabled="isReadOnly || typingStates[question.questionId]?.submitted"
                  @click="submitTyping(question)"
                >
                  {{ typingStates[question.questionId]?.submitting ? '提交中...' : (typingStates[question.questionId]?.submitted ? '已提交' : '提交打字') }}
                </el-button>
              </div>
            </div>

            <div class="typing-area">
              <div class="typing-status-bar" v-if="!typingStates[question.questionId]?.started">
                <el-alert
                  title="请点击左侧「开始练习」按钮开始计时"
                  type="info"
                  :closable="false"
                  center
                  show-icon
                />
              </div>
              <div class="typing-status-bar" v-else-if="typingStates[question.questionId]?.submitted">
                <el-alert
                  title="打字已提交"
                  type="success"
                  :closable="false"
                  center
                  show-icon
                />
              </div>

              <div class="original-text-box">
                <div class="box-label">文章段落：</div>
                <div
                  class="text-content"
                  @copy.prevent
                  @paste.prevent
                  @cut.prevent
                  @dragstart.prevent
                  @contextmenu.prevent
                >
                  <span
                    v-for="(char, idx) in question.questionContent || ''"
                    :key="idx"
                    :class="getCharClass(question.questionId, idx)"
                  >{{ char }}</span>
                </div>
              </div>

              <div class="input-box">
                <label>输入框：</label>
                <el-input
                  :ref="(el) => { if (el) inputRefs[question.questionId] = el }"
                  v-model="answers[question.questionId]"
                  type="textarea"
                  :rows="6"
                  placeholder="在此输入上方文字...（禁止复制粘贴）"
                  resize="none"
                  class="typing-input"
                  :disabled="isReadOnly || !typingStates[question.questionId]?.started || typingStates[question.questionId]?.submitted"
                  @input="value => handleTypingInput(question.questionId, value)"
                  @paste.prevent="handlePasteBlock"
                  @copy.prevent
                  @cut.prevent
                  @contextmenu.prevent
                />
              </div>
            </div>
          </div>
        </section>

        <section v-if="theoryQuestions.length" class="section-block">
          <div class="section-title">
            <el-icon><EditPen /></el-icon> 理论测试
          </div>
          <div class="theory-grid">
            <el-card
              v-for="(question, index) in theoryQuestions"
              :key="question.questionId"
              class="theory-card"
              shadow="hover"
            >
              <template #header>
                <div class="card-header">
                  <span class="badge">{{ questionTypeText(question.questionType) }}</span>
                </div>
              </template>

              <div class="question-stem">
                {{ index + 1 }}. {{ question.questionContent }}
              </div>

              <div v-if="question.questionType === 'choice'" class="options-group">
                <div
                  v-for="option in ['A', 'B', 'C', 'D']"
                  :key="option"
                  class="option-radio"
                  :class="{ active: answers[question.questionId] === option }"
                  @click="!theorySubmitted && !isReadOnly && setTheoryAnswer(question, option)"
                >
                  <span class="opt-label">{{ option }}</span>
                  <span class="opt-text">{{ question['option' + option] || '未配置' }}</span>
                </div>
              </div>

              <div
                v-else-if="question.questionType === 'judgment'"
                class="audit-group"
              >
                <el-radio-group v-model="answers[question.questionId]" :disabled="theorySubmitted || isReadOnly" @change="value => setTheoryAnswer(question, value)">
                  <el-radio value="T" border>正确</el-radio>
                  <el-radio value="F" border>错误</el-radio>
                </el-radio-group>
              </div>
            </el-card>
          </div>
          <div class="submit-theory-bar">
            <el-button
              type="success"
              size="large"
              :disabled="isReadOnly || theorySubmitted"
              @click="submitTheory"
            >
              {{ theorySubmitted ? '已提交' : '提交理论测试' }}
            </el-button>
          </div>
        </section>

        <section v-if="practicalQuestions.length" class="section-block">
          <div class="section-title">
            <el-icon><FolderOpened /></el-icon>
            <span>操作题</span>
          </div>
          <div class="question-list">
            <article v-for="(question, index) in practicalQuestions" :key="question.questionId" class="question-card">
              <div class="question-head">
                <span class="badge">操作题</span>
                <span>第 {{ index + 1 }} 题</span>
              </div>
              <p class="question-stem">{{ question.questionContent }}</p>
              <div v-if="question.filePath" class="material-row">
                <span>素材文件：{{ getFileName(question.filePath) }}</span>
                <el-button type="primary" size="small" icon="Download" @click="downloadMaterial(question.filePath)">下载素材</el-button>
              </div>
              <div class="upload-row">
                <div v-if="practicalUploads[question.questionId]" class="uploaded-file">
                  <el-icon><Document /></el-icon>
                  <span>{{ getFileName(practicalUploads[question.questionId]) }}</span>
                  <el-tag size="small" :type="previewTagType(question.questionId)">
                    {{ previewLabel(question.questionId) }}
                  </el-tag>
                  <el-button
                    v-if="canPreviewPractical(question.questionId)"
                    type="primary"
                    link
                    icon="View"
                    @click="previewPracticalWork(question.questionId)"
                  >
                    预览
                  </el-button>
                </div>
                <el-upload
                  :show-file-list="false"
                  :disabled="isReadOnly || practicalUploading[question.questionId]"
                  :action="uploadUrl"
                  :headers="uploadHeaders"
                  :before-upload="() => beforePracticalUpload(question.questionId)"
                  :on-success="(res) => handleUploadSuccess(question.questionId, res)"
                  :on-error="() => handleUploadError(question.questionId)"
                  accept=".docx,.doc,.pdf,.pptx,.ppt,.xlsx,.xls"
                >
                  <el-button type="success" icon="Upload" :loading="practicalUploading[question.questionId]" :disabled="isReadOnly">
                    {{ practicalUploads[question.questionId] ? '覆盖上传' : '上传作品' }}
                  </el-button>
                </el-upload>
              </div>
            </article>
          </div>
        </section>
      </template>
    </main>

    <el-dialog v-model="pwdDialogVisible" title="修改密码" width="400px" append-to-body>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPwd">确定</el-button>
      </template>
    </el-dialog>
    <PdfPreview ref="pdfPreviewRef" />
  </div>
</template>

<script setup name="StudentCountyExam">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCurrentCountyExam, saveCountyExamDraft } from '@/api/business/countyExam'
import { updateUserPwd } from '@/api/system/user'
import PdfPreview from '@/components/PdfPreview/index.vue'
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()

const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/business/countyExam/student/upload'
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + userStore.token
}))

const loading = ref(true)
const hasExam = ref(false)
const ended = ref(false)
const endedMessage = ref('')
const examId = ref(null)
const examName = ref('')
const examStartTime = ref('')
const studentInfo = ref({})
const allQuestions = ref([])
const submittedAnswers = ref({})
const answers = ref({})
const remainingSeconds = ref(0)
const timer = ref(null)
const timeoutHandling = ref(false)

const typingStates = ref({})
const practicalUploads = ref({})
const practicalPreviewStatuses = ref({})
const practicalPreviewPaths = ref({})
const practicalUploading = ref({})
const typingDraftTimers = ref({})
const inputRefs = ref({})
const timerIntervals = {}
const practicalPollingTimers = {}
const pdfPreviewRef = ref(null)

const pwdDialogVisible = ref(false)
const pwdFormRef = ref(null)
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const typingQuestions = computed(() => allQuestions.value.filter(item => item.questionType === 'typing'))
const theoryQuestions = computed(() => allQuestions.value.filter(item => ['choice', 'judgment'].includes(item.questionType)))
const practicalQuestions = computed(() => allQuestions.value.filter(item => item.questionType === 'practical'))
const theorySubmitted = computed(() => theoryQuestions.value.length > 0 && theoryQuestions.value.every(item => submittedAnswers.value[item.questionId]))
const isReadOnly = computed(() => ended.value || remainingSeconds.value <= 0)

async function fetchData() {
  loading.value = true
  try {
    clearTypingIntervals()
    const response = await getCurrentCountyExam().catch(() => ({ data: { hasExam: false } }))
    const data = response.data || {}
    hasExam.value = !!data.hasExam
    ended.value = !!data.ended
    endedMessage.value = data.message || '区域抽测已结束'
    examId.value = data.examId || null
    examName.value = data.examName || ''
    examStartTime.value = data.startTime || ''
    studentInfo.value = data.studentInfo || studentInfo.value || {}

    if (!data.hasExam || data.ended) {
      allQuestions.value = []
      submittedAnswers.value = {}
      clearExamTimer()
      clearTypingDraftTimers()
      clearAllPracticalPolling()
      return
    }

    allQuestions.value = data.questions || []
    submittedAnswers.value = data.submittedAnswers || {}
    remainingSeconds.value = Number(data.remainingSeconds || 0)
    initAnswers()
    initTypingStates()
    initPracticalStates()
    startExamTimer()
  } finally {
    loading.value = false
  }
}

function initAnswers() {
  answers.value = {}
  allQuestions.value.forEach(question => {
    const submitted = submittedAnswers.value[question.questionId]
    if (submitted?.answer) {
      answers.value[question.questionId] = normalizeQuestionAnswer(question.questionType, submitted.answer)
    }
  })
}

function initTypingStates() {
  typingStates.value = {}
  typingQuestions.value.forEach(question => {
    const submitted = submittedAnswers.value[question.questionId]
    const draft = loadTypingDraft(question.questionId)
    const text = submitted?.answer || draft || ''
    const localStart = loadTypingStart(question.questionId)
    const durationLimit = (question.typingDuration || 10) * 60
    const now = Date.now()
    const elapsedSeconds = localStart ? Math.max(0, Math.floor((now - localStart) / 1000)) : 0
    const timeLeft = submitted ? 0 : Math.max(0, durationLimit - elapsedSeconds)
    answers.value[question.questionId] = text
    typingStates.value[question.questionId] = {
      started: !!submitted || !!draft,
      finished: !!submitted,
      submitted: !!submitted,
      submitting: false,
      startTime: localStart || 0,
      timeLeft,
      durationLimit,
      completedCount: text.length,
      errorCount: 0,
      correctCount: 0,
      accuracy: 100,
      speed: 0,
      progress: 0
    }
    updateTypingStats(question.questionId, text)
    if (!submitted && (text || localStart) && timeLeft > 0) {
      startTypingTimer(question.questionId)
    }
  })
}

function initPracticalStates() {
  clearAllPracticalPolling()
  practicalUploads.value = {}
  practicalPreviewStatuses.value = {}
  practicalPreviewPaths.value = {}
  practicalUploading.value = {}
  practicalQuestions.value.forEach(question => {
    const submitted = submittedAnswers.value[question.questionId]
    if (submitted?.answer) {
      practicalUploads.value[question.questionId] = submitted.answer
      practicalPreviewStatuses.value[question.questionId] = submitted.previewStatus || ''
      practicalPreviewPaths.value[question.questionId] = submitted.previewPath || ''
      if (!submitted.previewPath && ['pending', 'converting', 'failed'].includes(submitted.previewStatus)) {
        schedulePracticalPreviewPolling(question.questionId)
      }
    }
  })
}

function startExamTimer() {
  clearExamTimer()
  timer.value = setInterval(() => {
    if (remainingSeconds.value > 0) {
      remainingSeconds.value--
      return
    }
    handleTimeout()
  }, 1000)
}

function clearExamTimer() {
  if (timer.value) {
    clearInterval(timer.value)
    timer.value = null
  }
}

function clearTypingDraftTimers() {
  Object.values(typingDraftTimers.value).forEach(item => {
    if (item) clearTimeout(item)
  })
  typingDraftTimers.value = {}
}

function clearTypingIntervals() {
  Object.keys(timerIntervals).forEach(questionId => {
    clearInterval(timerIntervals[questionId])
    delete timerIntervals[questionId]
  })
}

async function handleTimeout() {
  if (timeoutHandling.value) return
  timeoutHandling.value = true
  clearExamTimer()
  clearTypingIntervals()
  try {
    await saveCurrentDraft()
  } catch (error) {
    // 超时边界上保存失败时，以后端最终状态为准。
  }
  await fetchData()
  timeoutHandling.value = false
}

async function saveCurrentDraft() {
  if (!examId.value) return
  const payload = {}
  theoryQuestions.value.forEach(question => {
    if (!theorySubmitted.value && answers.value[question.questionId]) {
      payload[question.questionId] = normalizeQuestionAnswer(question.questionType, answers.value[question.questionId])
    }
  })
  typingQuestions.value.forEach(question => {
    const state = typingStates.value[question.questionId]
    if (state?.started && !state.submitted) {
      payload[question.questionId] = answers.value[question.questionId] || ''
    }
  })
  if (Object.keys(payload).length === 0) return
  await saveCountyExamDraft({ examId: examId.value, answers: payload })
}

function startTypingPractice(questionId) {
  const state = typingStates.value[questionId]
  if (!state || state.started || state.submitted) return
  state.started = true
  state.startTime = Date.now()
  saveTypingDraft(questionId, answers.value[questionId] || '')
  saveTypingStart(questionId, state.startTime)
  if (!state.timeLeft || state.timeLeft <= 0) {
    state.timeLeft = state.durationLimit
  }
  startTypingTimer(questionId)
  nextTick(() => {
    const inputEl = inputRefs.value[questionId]
    inputEl?.focus?.()
  })
}

function startTypingTimer(questionId) {
  if (timerIntervals[questionId]) return
  timerIntervals[questionId] = setInterval(() => {
    const state = typingStates.value[questionId]
    if (!state || state.submitted) {
      clearInterval(timerIntervals[questionId])
      delete timerIntervals[questionId]
      return
    }
    if (state.timeLeft > 0) {
      state.timeLeft--
      updateTypingStats(questionId, answers.value[questionId] || '')
    } else {
      autoSubmitTyping(questionId)
    }
  }, 1000)
}

function handleTypingInput(questionId, value) {
  const state = typingStates.value[questionId]
  if (!state?.started || state.submitted) return
  updateTypingStats(questionId, value)
  saveTypingDraft(questionId, value)
  scheduleTypingDraft(questionId)
  const question = allQuestions.value.find(item => item.questionId === questionId)
  const original = question?.questionContent || ''
  if (value.length >= original.length && original.length > 0) {
    autoSubmitTyping(questionId)
  }
}

function scheduleTypingDraft(questionId) {
  if (typingDraftTimers.value[questionId]) {
    clearTimeout(typingDraftTimers.value[questionId])
  }
  typingDraftTimers.value[questionId] = setTimeout(() => {
    saveTypingBackendDraft(questionId)
  }, 800)
}

async function saveTypingBackendDraft(questionId) {
  const state = typingStates.value[questionId]
  if (!examId.value || isReadOnly.value || !state?.started || state.submitted) return
  try {
    await saveCountyExamDraft({
      examId: examId.value,
      answers: { [questionId]: answers.value[questionId] || '' }
    })
  } catch (error) {
    // 草稿同步失败时保留本地缓存，下一次输入或超时兜底会再次提交。
  }
}

async function autoSubmitTyping(questionId) {
  const state = typingStates.value[questionId]
  if (!state?.started || state.submitted || state.submitting) return
  await submitTypingByQuestionId(questionId)
}

async function submitTyping(question) {
  if (!question) return
  await submitTypingByQuestionId(question.questionId)
}

async function submitTypingByQuestionId(questionId) {
  const state = typingStates.value[questionId]
  if (!state?.started || state.submitted || state.submitting) return
  state.submitting = true
  try {
    if (typingDraftTimers.value[questionId]) {
      clearTimeout(typingDraftTimers.value[questionId])
      typingDraftTimers.value[questionId] = null
    }
    if (timerIntervals[questionId]) {
      clearInterval(timerIntervals[questionId])
      delete timerIntervals[questionId]
    }
    const text = answers.value[questionId] || ''
    const fallbackStart = examStartTime.value ? new Date(examStartTime.value).getTime() : Date.now()
    const answerTime = Math.max(1, Math.round((Date.now() - (state.startTime || fallbackStart || Date.now())) / 1000))
    state.timeLeft = Math.max(0, state.durationLimit - answerTime)
    await saveCountyExamDraft({
      examId: examId.value,
      answers: { [questionId]: text },
      answerTimes: { [questionId]: answerTime },
      typingStats: { [questionId]: buildTypingStats(questionId, text, answerTime) }
    })
    state.submitted = true
    state.finished = true
    removeTypingDraft(questionId)
    removeTypingStart(questionId)
    ElMessage.success('打字已提交')
  } finally {
    state.submitting = false
  }
}

function updateTypingStats(questionId, inputVal) {
  const state = typingStates.value[questionId]
  if (!state) return
  const question = allQuestions.value.find(item => item.questionId === questionId)
  const original = question?.questionContent || ''
  let correct = 0
  let error = 0
  for (let i = 0; i < inputVal.length; i++) {
    if (i >= original.length) break
    if (inputVal[i] === original[i]) correct++
    else error++
  }
  state.completedCount = inputVal.length
  state.correctCount = correct
  state.errorCount = error
  state.accuracy = inputVal.length > 0 ? Number(((correct / inputVal.length) * 100).toFixed(1)) : 100
  const timeElapsed = Math.max(1, state.durationLimit - state.timeLeft)
  const minutes = Math.max(timeElapsed / 60, 1 / 60)
  state.speed = Number((correct / minutes).toFixed(1))
  state.progress = original.length > 0 ? Number(((correct / original.length) * 100).toFixed(1)) : 0
}

function buildTypingStats(questionId, text, answerTime) {
  const question = allQuestions.value.find(item => item.questionId === questionId)
  const original = question?.questionContent || ''
  let correct = 0
  const compareLength = Math.min(text.length, original.length)
  for (let i = 0; i < compareLength; i++) {
    if (text[i] === original[i]) correct++
  }
  const minutes = Math.max(answerTime / 60, 1 / 60)
  return {
    typingSpeed: Math.round(correct / minutes),
    accuracyRate: text.length > 0 ? Number(((correct / text.length) * 100).toFixed(2)) : 0,
    completionRate: original.length > 0 ? Number(((text.length / original.length) * 100).toFixed(2)) : 0
  }
}

function getCharClass(questionId, idx) {
  const inputVal = answers.value[questionId] || ''
  const question = allQuestions.value.find(item => item.questionId === questionId)
  const original = question?.questionContent || ''
  if (idx >= inputVal.length) return 'char-pending'
  if (inputVal[idx] === original[idx]) return 'char-correct'
  return 'char-error'
}

async function submitTheory() {
  const payload = {}
  let answeredCount = 0
  theoryQuestions.value.forEach(question => {
    const answer = answers.value[question.questionId]
    if (answer) {
      payload[question.questionId] = normalizeQuestionAnswer(question.questionType, answer)
      answeredCount++
    }
  })
  if (answeredCount === 0) {
    ElMessage.warning('请至少完成一道题目')
    return
  }
  await saveCountyExamDraft({ examId: examId.value, answers: payload })
  theoryQuestions.value.forEach(question => {
    if (payload[question.questionId]) {
      submittedAnswers.value[question.questionId] = { answer: payload[question.questionId] }
    }
  })
  ElMessage.success('理论测试已提交')
}

function setTheoryAnswer(question, value) {
  if (isReadOnly.value || theorySubmitted.value) return
  answers.value[question.questionId] = normalizeQuestionAnswer(question.questionType, value)
}

function beforePracticalUpload(questionId) {
  practicalUploading.value[questionId] = true
  return true
}

async function handleUploadSuccess(questionId, response) {
  try {
    if (response.code !== 200) {
      ElMessage.error(response.msg || '上传失败')
      return
    }
    practicalUploads.value[questionId] = response.fileName
    practicalPreviewStatuses.value[questionId] = 'pending'
    practicalPreviewPaths.value[questionId] = ''
    await saveCountyExamDraft({
      examId: examId.value,
      answers: { [questionId]: response.fileName }
    })
    ElMessage.success('上传成功，正在转换中')
    schedulePracticalPreviewPolling(questionId)
  } finally {
    practicalUploading.value[questionId] = false
  }
}

function handleUploadError(questionId) {
  practicalUploading.value[questionId] = false
  ElMessage.error('上传失败，请重试')
}

function handleUploadExceed() {
  ElMessage.warning('操作题仅允许上传 1 个文件')
}

function clearPracticalPolling(questionId) {
  if (practicalPollingTimers[questionId]) {
    clearTimeout(practicalPollingTimers[questionId])
    delete practicalPollingTimers[questionId]
  }
}

function clearAllPracticalPolling() {
  Object.keys(practicalPollingTimers).forEach(questionId => clearPracticalPolling(questionId))
}

function schedulePracticalPreviewPolling(questionId, attempt = 0) {
  clearPracticalPolling(questionId)
  if (!examId.value || practicalPreviewPaths.value[questionId]) return
  if (attempt >= 18) return
  practicalPollingTimers[questionId] = setTimeout(async () => {
    await refreshPracticalSubmission(questionId, attempt)
  }, attempt < 3 ? 2000 : 4000)
}

async function refreshPracticalSubmission(questionId, attempt = 0) {
  try {
    const response = await getCurrentCountyExam()
    const submitted = response.data?.submittedAnswers?.[questionId]
    if (!submitted) {
      schedulePracticalPreviewPolling(questionId, attempt + 1)
      return
    }
    syncPracticalSubmission(questionId, submitted)
    if (submitted.previewPath) {
      clearPracticalPolling(questionId)
      return
    }
    if (submitted.previewStatus === 'failed') {
      schedulePracticalPreviewPolling(questionId, attempt + 1)
      return
    }
    if (['pending', 'converting'].includes(submitted.previewStatus)) {
      schedulePracticalPreviewPolling(questionId, attempt + 1)
    }
  } catch (error) {
    schedulePracticalPreviewPolling(questionId, attempt + 1)
  }
}

function syncPracticalSubmission(questionId, submitted) {
  practicalUploads.value[questionId] = submitted.answer || practicalUploads.value[questionId] || ''
  practicalPreviewStatuses.value[questionId] = submitted.previewStatus || practicalPreviewStatuses.value[questionId] || 'pending'
  practicalPreviewPaths.value[questionId] = submitted.previewPath || ''
  submittedAnswers.value[questionId] = {
    ...(submittedAnswers.value[questionId] || {}),
    ...submitted
  }
}

function normalizeQuestionAnswer(questionType, answer) {
  if (questionType !== 'judgment') return answer
  const value = String(answer || '').trim()
  if (['对', '正确', 'T', '1', 'true'].includes(value) || value.toLowerCase() === 'true') return 'T'
  if (['错', '错误', 'F', '0', 'false'].includes(value) || value.toLowerCase() === 'false') return 'F'
  return value.toUpperCase()
}

function questionTypeText(type) {
  return ({ choice: '选择题', judgment: '判断题' })[type] || type
}

function previewLabel(questionId) {
  if (practicalPreviewPaths.value[questionId]) return '可预览'
  const status = practicalPreviewStatuses.value[questionId]
  if (status === 'success') return '可预览'
  if (status === 'pending' || status === 'converting' || status === 'failed') return '正在转换中'
  return '已上传'
}

function previewTagType(questionId) {
  if (practicalPreviewPaths.value[questionId] || practicalPreviewStatuses.value[questionId] === 'success') return 'success'
  return 'info'
}

function canPreviewPractical(questionId) {
  return !!practicalPreviewPaths.value[questionId]
}

function previewPracticalWork(questionId) {
  const previewPath = practicalPreviewPaths.value[questionId]
  if (!previewPath) {
    ElMessage.info('作品已上传，正在转换预览，请稍候再试')
    return
  }
  pdfPreviewRef.value?.open(import.meta.env.VITE_APP_BASE_API + '/common/resource/view?resource=' + encodeURIComponent(previewPath))
}

function getFileName(filePath) {
  if (!filePath) return ''
  return filePath.split('/').pop()
}

function downloadMaterial(filePath) {
  if (!filePath) return
  const link = document.createElement('a')
  link.href = import.meta.env.VITE_APP_BASE_API + filePath
  link.download = getFileName(filePath)
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

function formatTime(seconds) {
  const value = Math.max(0, Number(seconds || 0))
  const min = Math.floor(value / 60)
  const sec = value % 60
  return `${min}分${String(sec).padStart(2, '0')}秒`
}

function typingDraftKey(questionId) {
  return `county-exam-typing-${userStore.id || '0'}-${examId.value}-${questionId}`
}

function typingStartKey(questionId) {
  return `county-exam-typing-start-${userStore.id || '0'}-${examId.value}-${questionId}`
}

function saveTypingDraft(questionId, value) {
  localStorage.setItem(typingDraftKey(questionId), value || '')
}

function loadTypingDraft(questionId) {
  const key = typingDraftKey(questionId)
  const current = localStorage.getItem(key)
  if (current !== null && current !== undefined) return current
  // 兼容旧 key（未区分 userId），读取一次后迁移，避免串号
  const legacyKey = `county-exam-typing-${examId.value}-${questionId}`
  const legacy = localStorage.getItem(legacyKey)
  if (legacy) {
    localStorage.setItem(key, legacy)
    localStorage.removeItem(legacyKey)
    return legacy
  }
  return ''
}

function removeTypingDraft(questionId) {
  localStorage.removeItem(typingDraftKey(questionId))
  // 同步清理旧 key
  localStorage.removeItem(`county-exam-typing-${examId.value}-${questionId}`)
}

function saveTypingStart(questionId, value) {
  localStorage.setItem(typingStartKey(questionId), String(value || Date.now()))
}

function loadTypingStart(questionId) {
  const key = typingStartKey(questionId)
  const current = localStorage.getItem(key)
  if (current !== null && current !== undefined && current !== '') return Number(current) || 0
  // 兼容旧 key（未区分 userId）
  const legacyKey = `county-exam-typing-start-${examId.value}-${questionId}`
  const legacy = localStorage.getItem(legacyKey)
  if (legacy) {
    localStorage.setItem(key, legacy)
    localStorage.removeItem(legacyKey)
    return Number(legacy) || 0
  }
  return 0
}

function removeTypingStart(questionId) {
  localStorage.removeItem(typingStartKey(questionId))
  localStorage.removeItem(`county-exam-typing-start-${examId.value}-${questionId}`)
}

function handlePasteBlock() {
  ElMessage.warning('打字练习禁止粘贴')
}

function submitPwd() {
  pwdFormRef.value.validate(valid => {
    if (!valid) return
    updateUserPwd(pwdForm.value.oldPassword, pwdForm.value.newPassword).then(() => {
      ElMessage.success('修改成功，请重新登录')
      pwdDialogVisible.value = false
      userStore.logOut().then(() => {
        location.href = '/index'
      })
    })
  })
}

function handleCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定注销并退出系统吗？', '提示').then(() => {
      userStore.logOut().then(() => {
        location.href = '/index'
      })
    })
    return
  }
  if (command === 'password') {
    pwdDialogVisible.value = true
  }
}

onMounted(fetchData)
onUnmounted(() => {
  clearExamTimer()
  clearTypingIntervals()
  clearTypingDraftTimers()
  clearAllPracticalPolling()
})
</script>

<style lang="scss" scoped>
.county-student-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.page-header {
  height: 64px;
  padding: 0 32px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-left,
.user-info,
.student-tags,
.section-title,
.material-row,
.upload-row,
.uploaded-file {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo {
  height: 32px;
}

.platform-name {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

.user-info {
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 18px;
}

.main-content {
  width: min(1180px, calc(100% - 32px));
  margin: 0 auto;
  padding: 24px 0 40px;
}

.loading-box,
.ended-panel {
  min-height: 360px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  color: #606266;
}

.ended-panel h1,
.exam-banner h1 {
  margin: 0 0 12px;
  color: #1f2d3d;
}

.exam-banner {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.timer-box {
  min-width: 160px;
  text-align: center;
  padding: 14px 18px;
  border-radius: 8px;
  background: #ecf5ff;
  color: #1d4ed8;
}

.timer-box.warning {
  background: #fef0f0;
  color: #c2410c;
}

.timer-box span,
.stat-item label {
  display: block;
  font-size: 12px;
  margin-bottom: 4px;
}

.timer-box strong {
  font-size: 24px;
}

.section-block {
  margin-bottom: 24px;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  margin: 18px 0 12px;
}

.typing-panel {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  display: flex;
  overflow: hidden;
  min-height: 520px;
  margin-bottom: 20px;
}

.typing-stats {
  width: 200px;
  background: #f8f9fa;
  border-right: 1px solid #ebeef5;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #606266;
}

.stat-item label {
  color: #909399;
}

.stat-item span {
  font-weight: bold;
  color: #303133;
}

.stat-item.time span {
  font-size: 15px;
  color: #f56c6c;
}

.stat-item.time span.warning {
  color: #f56c6c;
}

.stat-item.highlight {
  background: #e6f7ff;
  padding: 8px;
  border-radius: 4px;
  margin: 4px -8px;
}

.stat-item.highlight span {
  color: #1890ff;
}

.error-text {
  color: #f56c6c !important;
}

.success-text {
  color: #67c23a !important;
}

.action-buttons {
  margin-top: auto;
  padding-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-btn {
  width: 100%;
}

.typing-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
  gap: 14px;
}

.typing-status-bar {
  margin-bottom: 6px;
}

.original-text-box {
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #f5f7fa;
}

.box-label {
  padding: 10px 16px;
  border-bottom: 1px solid #e4e7ed;
  font-weight: bold;
  color: #303133;
}

.text-content {
  min-height: 180px;
  max-height: 280px;
  overflow-y: auto;
  font-size: 18px;
  line-height: 2;
  letter-spacing: 1px;
  color: #303133;
  font-family: Consolas, "Courier New", monospace, "Microsoft YaHei";
  word-break: break-all;
  white-space: pre-wrap;
  box-sizing: border-box;
  padding: 5px 15px;
  border: 1px solid transparent;
  width: 100%;
}

.char-pending {
  color: #606266;
}

.char-correct {
  color: #67c23a;
}

.char-error {
  color: #f56c6c;
  background: #fef0f0;
}

.input-box {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 0 17px;
}

.input-box label {
  font-weight: bold;
  color: #303133;
}

.typing-input :deep(textarea) {
  font-size: 18px !important;
  line-height: 2 !important;
  letter-spacing: 1px !important;
  font-family: Consolas, "Courier New", monospace, "Microsoft YaHei" !important;
}

.theory-grid {
  display: grid;
  gap: 20px;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  margin-bottom: 20px;
}

.theory-card {
  border-radius: 8px;
  transition: border-color 0.3s;
}

.theory-card:hover {
  border-color: #409eff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.badge {
  display: inline-flex;
  align-items: center;
  height: 22px;
  padding: 0 8px;
  border-radius: 4px;
  background: #ecf5ff;
  color: #409eff;
  font-size: 12px;
}

.question-stem {
  font-size: 15px;
  color: #303133;
  margin: 12px 0 20px 0;
  line-height: 1.5;
  font-weight: 500;
}

.options-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-radio {
  display: flex;
  align-items: center;
  padding: 10px 14px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.option-radio:hover {
  background: #f5f7fa;
}

.option-radio.active {
  border-color: #409eff;
  background: #ecf5ff;
  color: #409eff;
}

.opt-label {
  font-weight: bold;
  margin-right: 10px;
}

.audit-group :deep(.el-radio-group) {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.submit-theory-bar {
  text-align: center;
  margin-top: 8px;
}

.question-list {
  display: grid;
  gap: 12px;
}

.question-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
}

.question-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #606266;
  margin-bottom: 10px;
}

.material-row {
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 6px;
  background: #f8fafc;
  margin-bottom: 12px;
}

.uploaded-file {
  color: #303133;
}

@media (max-width: 768px) {
  .page-header,
  .exam-banner {
    padding-left: 16px;
    padding-right: 16px;
  }

  .exam-banner {
    align-items: flex-start;
    flex-direction: column;
    gap: 14px;
  }

  .typing-panel {
    flex-direction: column;
  }

  .typing-stats {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #e5e7eb;
  }
}
</style>
