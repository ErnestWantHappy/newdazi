<template>
  <el-card class="programming-card" shadow="never">
    <template #header>
      <div class="programming-header">
        <div class="title-line">
          <el-tag type="primary">Python 在线编程</el-tag>
          <span class="question-title">{{ question.questionContent || 'Python 编程题' }}</span>
        </div>
        <strong>{{ question.questionScore || 0 }} 分</strong>
      </div>
    </template>

    <div class="programming-meta">
      <span>Python 3</span>
      <span>时限 {{ config.timeLimitSeconds || 2 }} 秒</span>
      <span>内存 {{ Math.round((config.memoryLimitKb || 131072) / 1024) }} MB</span>
      <span>公开示例 {{ publicCases.length }} 个</span>
    </div>

    <section class="statement-section">
      <div class="section-heading"><span>题目说明</span><el-tag size="small" effect="plain">操作题 · 自动判题</el-tag></div>
      <div class="statement-content">{{ question.questionContent || '请按照题目要求编写 Python 程序。' }}</div>
      <div class="statement-grid">
        <article v-if="config.inputDescription" class="statement-block"><h4>输入说明</h4><p>{{ config.inputDescription }}</p></article>
        <article v-if="config.outputDescription" class="statement-block"><h4>输出说明</h4><p>{{ config.outputDescription }}</p></article>
        <article v-if="config.constraintsText" class="statement-block"><h4>限制条件</h4><p>{{ config.constraintsText }}</p></article>
        <article v-if="config.notesText" class="statement-block"><h4>注意事项</h4><p>{{ config.notesText }}</p></article>
      </div>
    </section>

    <section v-if="publicCases.length" class="examples-section">
      <div class="section-heading"><span>公开样例</span><span class="section-hint">运行示例会使用这些输入</span></div>
      <div class="example-grid">
        <article v-for="item in publicCases" :key="item.testCaseId" class="example-item">
          <div class="example-title">{{ item.caseName || `测试点 ${item.testCaseId}` }}</div>
          <div class="io-grid">
            <div><label>样例输入</label><pre>{{ item.inputText || '（无输入）' }}</pre></div>
            <div><label>期望输出</label><pre>{{ item.expectedOutput || '（无输出）' }}</pre></div>
          </div>
          <p v-if="config.sampleExplanation && publicCases.length === 1" class="example-explanation"><b>样例解释：</b>{{ config.sampleExplanation }}</p>
        </article>
      </div>
    </section>

    <section class="editor-section">
      <div class="section-heading">
        <span>编辑代码</span>
        <div class="editor-tools">
          <el-button text size="small" @click="resetStarter">恢复初始代码</el-button>
          <el-button text size="small" @click="toggleTheme">{{ darkTheme ? '浅色主题' : '深色主题' }}</el-button>
          <el-button text size="small" @click="toggleFullscreen">{{ fullscreen ? '退出全屏' : '全屏编辑' }}</el-button>
        </div>
      </div>
      <div ref="editorHost" class="editor-shell" :class="{ 'is-dark': darkTheme, 'is-fullscreen': fullscreen }">
        <div class="editor-head"><span>main.py</span><span>{{ draftState }}</span></div>
        <div ref="editorElement" class="code-editor"></div>
      </div>
      <div v-if="needsInput" class="custom-input-panel">
        <div class="custom-input-heading">
          <label :for="inputId">程序输入（stdin）</label>
          <el-tooltip content="这里用于自己调试。运行示例使用题目给出的固定样例，提交判题使用系统测试数据。" placement="top">
            <span class="help-dot">?</span>
          </el-tooltip>
        </div>
        <el-input
          :id="inputId"
          v-model="customInput"
          type="textarea"
          :rows="5"
          resize="vertical"
          maxlength="65536"
          placeholder="按题目的输入格式填写，例如：3 5"
          :disabled="isPending"
        />
      </div>
      <div class="programming-actions">
        <el-button :loading="saving" :disabled="isPending" @click="saveDraft">保存草稿</el-button>
        <el-button :loading="running" :disabled="isPending" @click="run">运行示例</el-button>
        <el-button v-if="needsInput" type="success" plain :loading="customRunning" :disabled="isPending" @click="runCustom">自定义运行</el-button>
        <el-button type="primary" :loading="submitting" :disabled="isPending" @click="submit">提交判题</el-button>
      </div>
    </section>

    <section class="result-section">
      <div class="section-heading"><span>运行与提交结果</span><el-button v-if="pollTimedOut" text type="primary" size="small" @click="refreshResult">刷新结果</el-button></div>
      <el-alert v-if="pollTimedOut" title="结果查询超时，代码和提交已保留，请刷新结果或稍后查看历史提交。" type="warning" :closable="false" show-icon />
      <el-alert v-else :title="statusText" :type="statusType" :closable="false" show-icon />
      <div v-if="current?.cases?.length" class="case-results">
        <article v-for="row in current.cases" :key="row.testCaseId" class="case-result">
          <div class="case-result-header">
            <strong>{{ row.caseName || '公开测试点' }}</strong>
            <el-tag :type="tagType(row.statusCode)">{{ statusLabel(row.statusCode) }}</el-tag>
          </div>
          <div class="io-grid">
            <div><label>输入</label><pre>{{ row.inputText || '（无输入）' }}</pre></div>
            <div>
              <label>{{ Number(row.testCaseId) === 0 ? '输出校验' : '期望输出' }}</label>
              <pre>{{ Number(row.testCaseId) === 0 ? '自定义运行不比较标准答案' : (row.expectedOutput || '（无输出）') }}</pre>
            </div>
            <div><label>实际输出</label><pre>{{ row.actualOutput || '（无输出）' }}</pre></div>
            <div v-if="row.errorMessage"><label>错误信息</label><pre class="error-output">{{ row.errorMessage }}</pre></div>
          </div>
          <div class="case-metrics">运行时间：{{ row.timeSeconds == null ? '-' : `${row.timeSeconds}s` }} · 内存：{{ row.memoryKb == null ? '-' : `${row.memoryKb}KB` }}</div>
        </article>
      </div>
      <div v-else-if="current && current.statusCode === 'SERVICE_ERROR'" class="service-error-note">判题服务暂时不可用，代码和提交已保留，请稍后重试。</div>
    </section>

    <section v-if="historyList.length" class="history-section">
      <div class="section-heading"><span>历史提交</span><span class="section-hint">仅显示本人本题记录</span></div>
      <el-table :data="historyList" size="small" class="history-table">
        <el-table-column label="时间" width="165"><template #default="{ row }">{{ formatTime(row.submittedAt) }}</template></el-table-column>
        <el-table-column label="类型" width="100"><template #default="{ row }">{{ submissionKindLabel(row.submissionKind) }}</template></el-table-column>
        <el-table-column label="结果"><template #default="{ row }"><el-tag :type="tagType(row.statusCode)">{{ statusLabel(row.statusCode) }}</el-tag></template></el-table-column>
        <el-table-column label="通过" width="100"><template #default="{ row }">{{ row.passedCaseCount || 0 }}/{{ row.totalCaseCount || 0 }}</template></el-table-column>
        <el-table-column label="得分" width="80"><template #default="{ row }">{{ row.score == null ? '-' : row.score }}</template></el-table-column>
        <el-table-column label="操作" width="80"><template #default="{ row }"><el-button v-if="row.statusCode === 'WAITING'" link type="danger" @click="cancel(row)">取消</el-button></template></el-table-column>
      </el-table>
    </section>
  </el-card>
</template>

<script setup>
import { autocompletion, completeFromList } from '@codemirror/autocomplete'
import { defaultKeymap, history as cmHistory, historyKeymap, indentWithTab, toggleComment } from '@codemirror/commands'
import { python } from '@codemirror/lang-python'
import { foldGutter, indentOnInput, bracketMatching, syntaxHighlighting, defaultHighlightStyle } from '@codemirror/language'
import { search, searchKeymap } from '@codemirror/search'
import { Compartment, EditorState } from '@codemirror/state'
import { EditorView, keymap, lineNumbers, highlightActiveLine, drawSelection } from '@codemirror/view'
import { oneDark } from '@codemirror/theme-one-dark'
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { cancelProgramming, customRunProgramming, getStudentProgramming, runProgramming, saveProgrammingDraft, submitProgramming } from '@/api/business/programming'

const props = defineProps({ lessonId: { type: Number, required: true }, question: { type: Object, required: true } })
const emit = defineEmits(['completed'])
const config = ref({})
const publicCases = ref([])
const sourceCode = ref('')
const historyList = ref([])
const current = ref(null)
const saving = ref(false)
const running = ref(false)
const customRunning = ref(false)
const submitting = ref(false)
const customInput = ref('')
const draftState = ref('草稿未保存')
const pollTimedOut = ref(false)
const darkTheme = ref(false)
const fullscreen = ref(false)
const editorElement = ref(null)
const editorHost = ref(null)
let editorView
let draftTimer
let pollTimer
let pollCount = 0
let pendingDraft = false
let loadedQuestionId = null
let lastSavedSource = null
const themeCompartment = new Compartment()
const statusText = computed(() => current.value ? `${statusLabel(current.value.statusCode)}${current.value.statusMessage ? `：${current.value.statusMessage}` : ''}` : '可先查看公开样例，运行示例不会使用隐藏测试点。')
const statusType = computed(() => !current.value ? 'info' : (tagType(current.value.statusCode) === 'danger' ? 'error' : tagType(current.value.statusCode)))
const needsInput = computed(() => String(config.value.noInput ?? '0') !== '1')
const isPending = computed(() => ['WAITING', 'JUDGING'].includes(current.value?.statusCode))
const inputId = computed(() => `course-python-stdin-${props.question.questionId}`)

const completionWords = ['print', 'input', 'len', 'range', 'str', 'int', 'float', 'list', 'dict', 'set', 'tuple', 'sum', 'min', 'max', 'sorted', 'enumerate', 'True', 'False', 'None', 'if', 'elif', 'else', 'for', 'while', 'def', 'return', 'import', 'from', 'in', 'and', 'or', 'not']
const completionSource = completeFromList(completionWords.map(label => ({ label, type: 'keyword' })))

function editorExtensions() {
  return [
    lineNumbers(), foldGutter(), highlightActiveLine(), drawSelection(), cmHistory(), indentOnInput(), bracketMatching(),
    syntaxHighlighting(defaultHighlightStyle, { fallback: true }), python(), search(), autocompletion({ override: [completionSource] }),
    keymap.of([...defaultKeymap, ...historyKeymap, ...searchKeymap, indentWithTab, { key: 'Mod-/', run: toggleComment }]),
    EditorView.updateListener.of(update => {
      if (update.docChanged) {
        sourceCode.value = update.state.doc.toString()
        scheduleDraft()
      }
    }),
    themeCompartment.of(darkTheme.value ? oneDark : EditorView.theme({
      '&': { fontSize: '14px', minHeight: '300px' },
      '.cm-scroller': { fontFamily: 'Consolas, "Courier New", monospace', overflow: 'auto' },
      '.cm-content': { minHeight: '300px', padding: '14px 0' },
      '.cm-gutters': { minHeight: '300px' }
    }))
  ]
}

function mountEditor() {
  if (!editorElement.value || editorView) return
  editorView = new EditorView({ state: EditorState.create({ doc: sourceCode.value, extensions: editorExtensions() }), parent: editorElement.value })
}
function setEditorValue(value) {
  sourceCode.value = value || ''
  if (editorView && editorView.state.doc.toString() !== sourceCode.value) editorView.dispatch({ changes: { from: 0, to: editorView.state.doc.length, insert: sourceCode.value } })
}
function applyTheme() { if (editorView) editorView.dispatch({ effects: themeCompartment.reconfigure(darkTheme.value ? oneDark : EditorView.theme({ '&': { fontSize: '14px', minHeight: '300px' }, '.cm-scroller': { fontFamily: 'Consolas, "Courier New", monospace', overflow: 'auto' }, '.cm-content': { minHeight: '300px', padding: '14px 0' }, '.cm-gutters': { minHeight: '300px' } })) }) }
function toggleTheme() { darkTheme.value = !darkTheme.value; applyTheme() }
function toggleFullscreen() { fullscreen.value = !fullscreen.value; nextTick(() => editorView?.requestMeasure()) }
function resetStarter() { setEditorValue(config.value.starterCode || ''); scheduleDraft() }
function statusLabel(code) { return ({ WAITING: '等待判题', JUDGING: '判题中', COMPLETED: '运行完成', ACCEPTED: '通过', PARTIAL: '部分通过', WRONG_ANSWER: '答案错误', SYNTAX_ERROR: '语法错误', RUNTIME_ERROR: '运行错误', TIME_LIMIT: '超时', MEMORY_LIMIT: '内存超限', SERVICE_ERROR: '服务异常', CANCELLED: '已取消' })[code] || '等待判题' }
function tagType(code) { if (code === 'ACCEPTED' || code === 'COMPLETED') return 'success'; if (code === 'PARTIAL' || code === 'WAITING' || code === 'JUDGING') return 'warning'; if (code === 'SERVICE_ERROR') return 'info'; return 'danger' }
function submissionKindLabel(kind) { return ({ RUN: '示例运行', CUSTOM_RUN: '自定义运行', SUBMIT: '正式提交' })[kind] || kind }
function formatTime(value) { return value ? String(value).replace('T', ' ').slice(0, 16) : '-' }
function makeKey() { return `${Date.now()}${Math.random().toString(36).slice(2, 10)}` }

async function load() {
  const res = await getStudentProgramming(props.lessonId, props.question.questionId)
  const data = res.data || {}
  config.value = data.config || {}
  publicCases.value = data.publicCases || []
  historyList.value = data.history || []
  const firstLoad = loadedQuestionId !== props.question.questionId
  if (firstLoad) {
    const value = data.draft?.sourceCode || historyList.value.find(item => item.submissionKind === 'SUBMIT')?.sourceCode || config.value.starterCode || ''
    setEditorValue(value)
    lastSavedSource = data.draft?.sourceCode || null
    draftState.value = data.draft?.sourceCode ? '草稿已保存' : '草稿未保存'
  }
  loadedQuestionId = props.question.questionId
  current.value = historyList.value[0] || current.value
  if (current.value?.statusCode === 'WAITING' || current.value?.statusCode === 'JUDGING') schedulePoll()
}

function scheduleDraft() {
  pendingDraft.value = true
  draftState.value = '草稿待保存'
  clearTimeout(draftTimer)
  draftTimer = setTimeout(saveDraft, 800)
}
async function saveDraft() {
  clearTimeout(draftTimer)
  if (saving.value) return
  if (!sourceCode.value.trim()) { draftState.value = '草稿为空'; pendingDraft.value = false; return }
  if (lastSavedSource === sourceCode.value) { pendingDraft.value = false; draftState.value = '草稿已保存'; return }
  saving.value = true
  const valueToSave = sourceCode.value
  try {
    await saveProgrammingDraft({ lessonId: props.lessonId, questionId: props.question.questionId, sourceCode: valueToSave })
    lastSavedSource = valueToSave
    pendingDraft.value = sourceCode.value !== valueToSave
    draftState.value = pendingDraft.value ? '仍有修改待保存' : '草稿已保存'
  } catch (error) {
    draftState.value = '草稿保存失败'
  } finally {
    saving.value = false
    if (pendingDraft.value) { clearTimeout(draftTimer); draftTimer = setTimeout(saveDraft, 300) }
  }
}
async function run() { await enqueue('RUN') }
async function runCustom() { await enqueue('CUSTOM_RUN') }
async function submit() { await enqueue('SUBMIT') }
async function enqueue(kind) {
  if (!sourceCode.value.trim()) return ElMessage.warning('请先输入 Python 代码')
  if (isPending.value) return ElMessage.info('当前代码仍在排队或判题，请稍候')
  if (pendingDraft.value) await saveDraft()
  if (kind === 'RUN') running.value = true
  else if (kind === 'CUSTOM_RUN') customRunning.value = true
  else submitting.value = true
  pollTimedOut.value = false
  try {
    const api = kind === 'RUN' ? runProgramming : (kind === 'CUSTOM_RUN' ? customRunProgramming : submitProgramming)
    const res = await api({ lessonId: props.lessonId, questionId: props.question.questionId, sourceCode: sourceCode.value, customInput: kind === 'CUSTOM_RUN' ? customInput.value : undefined, submissionKey: makeKey() })
    current.value = res.submission || res.data?.submission
    if (current.value) historyList.value = [current.value, ...historyList.value.filter(item => item.submissionId !== current.value.submissionId)]
    pollCount = 0
    schedulePoll()
  } finally { running.value = false; customRunning.value = false; submitting.value = false }
}
async function cancel(row) { await cancelProgramming(props.lessonId, props.question.questionId, row.submissionId); await load() }
async function refreshResult() { pollTimedOut.value = false; pollCount = 0; await load(); if (current.value?.statusCode === 'WAITING' || current.value?.statusCode === 'JUDGING') schedulePoll() }
function schedulePoll() {
  clearTimeout(pollTimer)
  if (++pollCount > 36) { pollTimedOut.value = true; return }
  pollTimer = setTimeout(async () => {
    await load()
    if (current.value?.statusCode === 'WAITING' || current.value?.statusCode === 'JUDGING') schedulePoll()
    else if (current.value?.submissionKind === 'SUBMIT' && current.value?.statusCode !== 'SERVICE_ERROR' && current.value?.statusCode !== 'CANCELLED') emit('completed')
  }, 1000)
}

watch(() => props.question.questionId, () => {
  loadedQuestionId = null
  current.value = null
  pollTimedOut.value = false
  pollCount = 0
  clearTimeout(pollTimer)
  nextTick(load)
}, { immediate: true })
onMounted(() => nextTick(mountEditor))
onUnmounted(() => { clearTimeout(draftTimer); clearTimeout(pollTimer); editorView?.destroy() })
</script>

<style scoped>
.programming-card { max-width: 100%; }
.programming-header,.programming-actions,.section-heading,.case-result-header { display:flex; align-items:center; justify-content:space-between; gap:12px; }
.title-line { display:flex; align-items:center; gap:10px; min-width:0; }
.question-title { color:#303133; line-height:1.5; word-break:break-word; }
.programming-header strong { color:#e6a23c; white-space:nowrap; }
.programming-meta { display:flex; flex-wrap:wrap; gap:8px 18px; margin-bottom:18px; color:#606266; font-size:13px; }
.programming-meta span + span { border-left:1px solid #dcdfe6; padding-left:18px; }
.statement-section,.examples-section,.editor-section,.result-section,.history-section { margin-top:20px; }
.section-heading { min-height:32px; margin-bottom:10px; color:#303133; font-weight:600; }
.section-hint { color:#909399; font-size:12px; font-weight:400; }
.statement-content,.statement-block,.example-item,.case-result { color:#606266; line-height:1.7; }
.statement-content { white-space:pre-wrap; word-break:break-word; }
.statement-grid,.example-grid { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:12px; margin-top:12px; }
.statement-block { padding:12px; background:#f8fafc; border-left:3px solid #409eff; }
.statement-block h4 { margin:0 0 5px; color:#303133; font-size:14px; }
.statement-block p,.example-explanation { margin:0; white-space:pre-wrap; }
.example-item,.case-result { padding:14px; border:1px solid #ebeef5; border-radius:6px; background:#fff; }
.example-title { margin-bottom:10px; color:#303133; font-weight:600; }
.io-grid { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:10px; }
.io-grid label { display:block; margin-bottom:4px; color:#909399; font-size:12px; }
pre { min-height:38px; max-height:180px; margin:0; padding:9px 10px; overflow:auto; white-space:pre-wrap; word-break:break-word; background:#f5f7fa; border-radius:4px; color:#303133; font:13px/1.55 Consolas,"Courier New",monospace; }
.example-explanation { margin-top:10px; color:#606266; font-size:13px; }
.editor-tools { display:flex; flex-wrap:wrap; gap:2px; }
.editor-shell { overflow:hidden; border:1px solid #dcdfe6; border-radius:4px; }
.editor-shell.is-fullscreen { position:fixed; inset:12px; z-index:3000; display:flex; flex-direction:column; background:#fff; box-shadow:0 12px 40px rgba(0,0,0,.28); }
.editor-shell.is-fullscreen .code-editor { flex:1; overflow:auto; }
.editor-head { display:flex; justify-content:space-between; padding:8px 12px; background:#f5f7fa; color:#606266; font:13px/1.4 Consolas,"Courier New",monospace; }
.code-editor { min-height:300px; }
.code-editor :deep(.cm-editor) { min-height:300px; }
.editor-shell:focus-within { border-color:#409eff; box-shadow:0 0 0 1px #409eff; }
.code-editor :deep(.cm-focused) { outline:none; }
.is-dark { background:#282c34; border-color:#434a56; }
.is-dark .editor-head { background:#21252b; color:#abb2bf; }
.programming-actions { justify-content:flex-start; margin-top:12px; }
.custom-input-panel { margin-top:12px; padding:12px; border:1px solid #dfe7ee; border-radius:6px; background:#f8fafc; }
.custom-input-heading { display:flex; align-items:center; gap:7px; margin-bottom:8px; color:#303133; font-weight:600; }
.help-dot { display:inline-grid; place-items:center; width:18px; height:18px; border:1px solid #a8b3bd; border-radius:50%; color:#7b8792; font-size:12px; cursor:help; }
.case-results { display:grid; gap:12px; }
.case-result-header { margin-bottom:10px; }
.case-metrics { margin-top:8px; color:#909399; font-size:12px; }
.error-output { color:#f56c6c; }
.service-error-note { margin-top:12px; padding:12px; color:#b88230; background:#fdf6ec; border:1px solid #f3d19e; border-radius:4px; }
.history-table { width:100%; }
@media (max-width:700px) {
  .programming-header,.title-line { align-items:flex-start; flex-direction:column; }
  .programming-meta span + span { border-left:0; padding-left:0; }
  .statement-grid,.example-grid,.io-grid { grid-template-columns:1fr; }
  .editor-tools { justify-content:flex-start; }
  .editor-shell.is-fullscreen { inset:0; }
}
</style>
