<template>
  <section class="terminal-section" aria-label="Python 运行终端">
    <div class="terminal-heading"><strong>运行结果</strong><span role="status" :class="{ 'has-error': state === 'error' }">{{ status }}</span></div>
    <div v-if="!serverMode" ref="screen" class="terminal-screen" role="log" aria-label="程序输出" tabindex="0">
      <span>{{ transcript }}</span><form v-if="state === 'input'" class="terminal-input" @submit.prevent="sendInput"><input ref="inputElement" v-model="inputText" aria-label="程序等待输入，输入后按回车" autocomplete="off" spellcheck="false" maxlength="4096" :disabled="!busy"><button type="submit">回车确认</button></form>
      <span v-if="!transcript && state === 'idle'" class="terminal-placeholder">点击“运行”，按程序提示输入并按回车。</span>
    </div>
    <!-- 旧版浏览器：程序一次性在服务器跑示例输入，按测试点展示实际输出 -->
    <div v-else class="terminal-screen" role="log" aria-label="程序输出" tabindex="0">
      <article v-for="(item, index) in serverCases" :key="index" class="server-case">
        <div class="server-case-head"><strong>{{ item.caseName || `示例 ${index + 1}` }}</strong><el-tag size="small" :type="caseTagType(item.statusCode)">{{ caseLabel(item.statusCode) }}</el-tag></div>
        <div><label>输入</label><pre>{{ item.inputText || '（无输入）' }}</pre></div>
        <div><label>实际输出</label><pre>{{ item.actualOutput || '（无输出）' }}</pre></div>
        <div v-if="item.errorMessage"><label>错误信息</label><pre class="error-text">{{ item.errorMessage }}</pre></div>
        <div class="server-case-metrics">运行时间：{{ item.timeSeconds == null ? '-' : `${item.timeSeconds}s` }} · 内存：{{ item.memoryKb == null ? '-' : `${item.memoryKb}KB` }}</div>
      </article>
      <pre class="error-text">{{ transcript }}</pre>
      <span v-if="!serverCases.length && state === 'idle'" class="terminal-placeholder">点击“运行”，服务器会按上方示例输入运行代码。</span>
    </div>
    <div class="terminal-note">运行用于调试，不计分；本次输出刷新后不保留。代码自动保存，交作业后可在提交记录中查看。</div>
  </section>
</template>

<script setup>
import { computed, nextTick, onUnmounted, ref } from 'vue'
import { createInteractiveRunner } from './interactiveRunner'

const props = defineProps({ serverMode: { type: Boolean, default: false } })
const emit = defineEmits(['busy', 'completed'])
const state = ref('idle')
const status = ref('等待运行')
const transcript = ref('')
const inputText = ref('')
const serverCases = ref([])
const screen = ref(null)
const inputElement = ref(null)
const busy = computed(() => ['loading', 'running', 'input'].includes(state.value))
const runner = createInteractiveRunner(event => {
  if (event.type === 'output') transcript.value += event.text
  else if (event.type === 'server-result') serverCases.value = event.cases || []
  else {
    state.value = event.type
    status.value = ({ loading: event.text, running: '正在运行…', input: '等待输入，按回车继续', done: '运行结束', error: '运行失败，可以修改后重试', stopped: '已停止，可以重新运行' })[event.type]
    if (event.type === 'error') transcript.value = event.text
    if (event.type === 'done') emit('completed')
    emit('busy', busy.value)
  }
  nextTick(() => {
    if (screen.value) screen.value.scrollTop = screen.value.scrollHeight
    if (state.value === 'input') inputElement.value?.focus()
  })
})

const CASE_LABELS = { ACCEPTED: '与示例一致', WRONG_ANSWER: '与示例不一致', SYNTAX_ERROR: '语法错误', RUNTIME_ERROR: '运行错误', TIME_LIMIT: '运行超时', MEMORY_LIMIT: '内存超限', SERVICE_ERROR: '服务器繁忙', COMPLETED: '运行完成' }
function caseLabel(code) { return CASE_LABELS[code] || '运行失败' }
function caseTagType(code) { return code === 'ACCEPTED' ? 'success' : code === 'SERVICE_ERROR' ? 'info' : 'danger' }

function run(code) {
  transcript.value = ''
  inputText.value = ''
  serverCases.value = []
  runner.run(code, new URL(import.meta.env.VITE_PYODIDE_BASE_URL || '/pyodide/', location.href).href.replace(/\/?$/, '/'))
}
function runOnServer(code, context) {
  transcript.value = ''
  serverCases.value = []
  return runner.runOnServer(code, context)
}
function sendInput() {  if (state.value !== 'input') return
  if (runner.input(inputText.value)) {
    inputText.value = ''
    state.value = 'running'
    status.value = '正在运行…'
  }
}
function stop() { runner.stop() }
onUnmounted(() => runner.dispose())
defineExpose({ run, runOnServer, stop })
</script>

<style scoped>
.terminal-section { margin-top:16px; min-width:0; }
.terminal-heading { display:flex; justify-content:space-between; flex-wrap:wrap; gap:8px; margin-bottom:8px; color:#303133; }
.terminal-heading span { color:#606266; font-size:13px; }
.terminal-heading .has-error { color:#b42318; }
.terminal-screen { min-height:140px; max-height:320px; padding:14px; overflow:auto; border:1px solid #dcdfe6; border-radius:4px; background:#f8fafc; color:#1f2937; white-space:pre-wrap; overflow-wrap:anywhere; font:14px/1.7 Consolas,"Microsoft YaHei",monospace; }
.terminal-input { display:inline; white-space:normal; }
.terminal-input input { width:clamp(70px,25vw,240px); max-width:100%; padding:2px 4px; border:0; border-bottom:2px solid #409eff; background:#fff; color:inherit; font:inherit; border-radius:0; }
.terminal-input input:focus { outline:2px solid #91caff; outline-offset:2px; }
.terminal-input button { min-height:36px; margin-left:8px; padding:5px 8px; border:1px solid #b1c9df; border-radius:4px; background:#fff; color:#235780; cursor:pointer; }
.terminal-note,.terminal-placeholder { color:#667085; font-size:12px; }
.terminal-note { margin-top:8px; line-height:1.6; }
.server-case + .server-case { margin-top:12px; padding-top:12px; border-top:1px dashed #dcdfe6; }
.server-case-head { display:flex; justify-content:space-between; align-items:center; gap:8px; margin-bottom:6px; }
.server-case label { display:block; color:#667085; font-size:12px; }
.server-case pre { margin:2px 0 6px; padding:6px 8px; background:#fff; border:1px solid #e5e7eb; border-radius:4px; font:inherit; white-space:pre-wrap; overflow-wrap:anywhere; }
.server-case pre.error-text { color:#b42318; }
.terminal-screen > pre.error-text { margin:0; padding:0; border:0; background:transparent; color:#b42318; font:inherit; }
.server-case-metrics { color:#98a2b3; font-family:"Microsoft YaHei",sans-serif; font-size:12px; }
</style>
