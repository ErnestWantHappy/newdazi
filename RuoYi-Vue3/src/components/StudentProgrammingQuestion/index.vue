<template>
  <el-card v-loading="loading" class="programming-card" shadow="never">
    <template #header>
      <div class="programming-header">
        <div class="title-line">
          <el-tag type="primary">Python 在线编程</el-tag>
          <span class="question-title">{{ question.questionContent || 'Python 编程题' }}</span>
        </div>
        <strong>{{ question.questionScore || 0 }} 分</strong>
      </div>
    </template>

    <el-alert v-if="loadError" :title="loadError" type="error" :closable="false"><el-button text @click="load">重新加载</el-button></el-alert>
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
      <div class="programming-actions">
        <el-button type="success" :disabled="loading || !!loadError || localRunning || isPending || submitting" @click="run">运行</el-button>
        <el-button v-if="localRunning" type="danger" plain @click="terminal?.stop()">停止运行</el-button>
        <el-button type="primary" :loading="submitting" :disabled="loading || !!loadError || isPending || localRunning" @click="submit">交作业</el-button>
        <el-button text :loading="saving" :disabled="loading || !!loadError" @click="saveDraft">保存草稿</el-button>
      </div>
      <interactive-terminal :key="question.questionId" ref="terminal" @busy="localRunning = $event" />
    </section>

    <section v-if="current" class="result-section">
      <div class="section-heading"><span>上次提交结果</span><el-button v-if="pollTimedOut" text type="primary" size="small" @click="refreshResult">刷新结果</el-button></div>
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

    <details v-if="historyList.length" class="history-section">
      <summary>历史提交（{{ historyList.length }} 条）</summary>
      <el-table :data="historyList" size="small" class="history-table">
        <el-table-column label="时间" width="165"><template #default="{ row }">{{ formatTime(row.submittedAt) }}</template></el-table-column>
        <el-table-column label="类型" width="100"><template #default="{ row }">{{ submissionKindLabel(row.submissionKind) }}</template></el-table-column>
        <el-table-column label="结果"><template #default="{ row }"><el-tag :type="tagType(row.statusCode)">{{ statusLabel(row.statusCode) }}</el-tag></template></el-table-column>
        <el-table-column label="通过" width="100"><template #default="{ row }">{{ row.passedCaseCount || 0 }}/{{ row.totalCaseCount || 0 }}</template></el-table-column>
        <el-table-column label="得分" width="80"><template #default="{ row }">{{ row.score == null ? '-' : row.score }}</template></el-table-column>
        <el-table-column label="操作" width="80"><template #default="{ row }"><el-button v-if="row.statusCode === 'WAITING'" link type="danger" @click="cancel(row)">取消</el-button></template></el-table-column>
      </el-table>
    </details>
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
import { cancelProgramming, getStudentProgramming, saveProgrammingDraft, submitProgramming } from '@/api/business/programming'
import InteractiveTerminal from './InteractiveTerminal.vue'

const props = defineProps({ lessonId: { type: Number, required: true }, question: { type: Object, required: true } })
const emit = defineEmits(['completed'])
const config = ref({})
const loading = ref(false)
const loadError = ref('')
const sourceCode = ref('')
const historyList = ref([])
const current = ref(null)
const saving = ref(false)
const terminal = ref(null)
const localRunning = ref(false)
const submitting = ref(false)
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
const pendingDraft = ref(false)
let loadedQuestionId = null
let lastSavedSource = null
let disposed = false
const themeCompartment = new Compartment()
const statusText = computed(() => current.value ? `${statusLabel(current.value.statusCode)}${current.value.statusMessage ? `：${current.value.statusMessage}` : ''}` : '')
const statusType = computed(() => !current.value ? 'info' : (tagType(current.value.statusCode) === 'danger' ? 'error' : tagType(current.value.statusCode)))
const isPending = computed(() => ['WAITING', 'JUDGING'].includes(current.value?.statusCode))

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
  const questionId = props.question.questionId
  const lessonId = props.lessonId
  loading.value = loadedQuestionId !== questionId
  loadError.value = ''
  try {
    const res = await getStudentProgramming(lessonId, questionId)
    if (disposed || questionId !== props.question.questionId || lessonId !== props.lessonId) return
    const data = res.data || {}
    config.value = data.config || {}
    historyList.value = data.history || []
    const firstLoad = loadedQuestionId !== props.question.questionId
    if (firstLoad) {
      const value = data.draft?.sourceCode || historyList.value.find(item => item.submissionKind === 'SUBMIT')?.sourceCode || config.value.starterCode || ''
      setEditorValue(value)
      lastSavedSource = data.draft?.sourceCode || null
      draftState.value = data.draft?.sourceCode ? '草稿已保存' : '草稿未保存'
    }
    loadedQuestionId = props.question.questionId
    current.value = historyList.value.find(item => item.submissionKind === 'SUBMIT') || null
    if (current.value?.statusCode === 'WAITING' || current.value?.statusCode === 'JUDGING') schedulePoll()
  } catch {
    if (!disposed && questionId === props.question.questionId) loadError.value = '作业加载失败，代码已保留，请重新加载'
  } finally {
    if (!disposed && questionId === props.question.questionId) loading.value = false
  }
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
  const questionId = props.question.questionId
  let saved = false
  try {
    await saveProgrammingDraft({ lessonId: props.lessonId, questionId, sourceCode: valueToSave })
    if (disposed || questionId !== props.question.questionId) return
    saved = true
    lastSavedSource = valueToSave
    pendingDraft.value = sourceCode.value !== valueToSave
    draftState.value = pendingDraft.value ? '仍有修改待保存' : '草稿已保存'
  } catch (error) {
    if (!disposed && questionId === props.question.questionId) draftState.value = '草稿保存失败，请点击“保存草稿”重试'
  } finally {
    saving.value = false
    if (!disposed && pendingDraft.value && (saved || questionId !== props.question.questionId)) { clearTimeout(draftTimer); draftTimer = setTimeout(saveDraft, 800) }
  }
}
function run() {
  if (!sourceCode.value.trim()) return ElMessage.warning('请先输入 Python 代码')
  if (loading.value || loadError.value || localRunning.value || isPending.value || submitting.value) return
  terminal.value?.run(sourceCode.value)
}
async function submit() {
  if (!sourceCode.value.trim()) return ElMessage.warning('请先输入 Python 代码')
  if (loading.value || loadError.value || submitting.value || isPending.value || localRunning.value) return ElMessage.info('请先等待当前任务结束或重新加载作业')
  submitting.value = true
  pollTimedOut.value = false
  try {
    const res = await submitProgramming({ lessonId: props.lessonId, questionId: props.question.questionId, sourceCode: sourceCode.value, submissionKey: makeKey() })
    current.value = res.submission || res.data?.submission
    if (current.value) historyList.value = [current.value, ...historyList.value.filter(item => item.submissionId !== current.value.submissionId)]
    pollCount = 0
    schedulePoll()
  } finally { submitting.value = false }
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
  terminal.value?.stop()
  localRunning.value = false
  clearTimeout(draftTimer)
  pendingDraft.value = false
  lastSavedSource = null
  loadedQuestionId = null
  current.value = null
  pollTimedOut.value = false
  pollCount = 0
  clearTimeout(pollTimer)
  nextTick(load)
}, { immediate: true })
onMounted(() => nextTick(mountEditor))
onUnmounted(() => { disposed = true; clearTimeout(draftTimer); clearTimeout(pollTimer); editorView?.destroy() })
</script>

<style scoped>
.programming-card { max-width: 100%; }
.programming-header,.programming-actions,.section-heading,.case-result-header { display:flex; align-items:center; justify-content:space-between; gap:12px; }
.title-line { display:flex; align-items:center; gap:10px; min-width:0; }
.question-title { color:#303133; line-height:1.5; word-break:break-word; }
.programming-header strong { color:#e6a23c; white-space:nowrap; }
.editor-section,.result-section,.history-section { margin-top:20px; }
.section-heading { min-height:32px; margin-bottom:10px; color:#303133; font-weight:600; }
.section-hint { color:#909399; font-size:12px; font-weight:400; }
.case-result { padding:14px; border:1px solid #ebeef5; border-radius:6px; background:#fff; color:#606266; line-height:1.7; }
.io-grid { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:10px; }
.io-grid label { display:block; margin-bottom:4px; color:#909399; font-size:12px; }
pre { min-height:38px; max-height:180px; margin:0; padding:9px 10px; overflow:auto; white-space:pre-wrap; word-break:break-word; background:#f5f7fa; border-radius:4px; color:#303133; font:13px/1.55 Consolas,"Courier New",monospace; }
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
.programming-actions { justify-content:flex-start; flex-wrap:wrap; gap:8px; margin-top:12px; }
.programming-actions :deep(.el-button + .el-button) { margin-left:0; }
.history-section summary { padding:10px 0; color:#606266; cursor:pointer; }
.case-results { display:grid; gap:12px; }
.case-result-header { margin-bottom:10px; }
.case-metrics { margin-top:8px; color:#909399; font-size:12px; }
.error-output { color:#f56c6c; }
.service-error-note { margin-top:12px; padding:12px; color:#b88230; background:#fdf6ec; border:1px solid #f3d19e; border-radius:4px; }
.history-table { width:100%; }
@media (max-width:700px) {
  .programming-header,.title-line { align-items:flex-start; flex-direction:column; }
  .editor-section > .section-heading { align-items:flex-start; flex-direction:column; gap:4px; }
  .io-grid { grid-template-columns:1fr; }
  .editor-tools { justify-content:flex-start; }
  .editor-shell.is-fullscreen { inset:0; }
}
</style>
