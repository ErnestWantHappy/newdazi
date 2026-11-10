<template>
  <section class="py-terminal" aria-label="Python 运行结果">
    <div class="py-head">
      <strong>运行结果</strong>
      <span role="status" :class="{ 'is-error': state === 'error' }">{{ statusText }}</span>
    </div>

    <div ref="screen" class="py-screen" role="log" aria-label="程序输出" tabindex="0">
      <span v-if="transcript">{{ transcript }}</span>
      <span v-else-if="state === 'idle'" class="py-placeholder">{{ placeholder }}</span>
      <form v-if="waitingInput" class="py-input" @submit.prevent="sendInput">
        <input
          ref="inputElement"
          v-model="inputText"
          aria-label="程序等待输入，输入后按回车"
          autocomplete="off"
          spellcheck="false"
          :maxlength="4096"
        />
        <button type="submit">回车确认</button>
      </form>
    </div>

    <div class="py-actions">
      <button type="button" class="py-primary" :disabled="busy" @click="start">
        {{ hasRun ? '重新运行' : '运行 Python' }}
      </button>
      <button v-if="busy && mode === 'local'" type="button" class="py-ghost" @click="stop">停止</button>
      <span class="py-mode">{{ modeLabel }}</span>
    </div>
    <p class="py-note">{{ noteText }}</p>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { createInteractiveRunner, canRunLocally } from '@/components/StudentProgrammingQuestion/interactiveRunner'
import { runInteractiveOnServer } from './serverInteractive'

const props = defineProps({
  // 要运行的代码；切换后自动清空上一次输出，避免串码。
  code: { type: String, default: '' },
  // 程序里 input() 的大致次数，用于提前告诉用户会填几次；无法确定时传 null。
  inputCount: { type: Number, default: null },
  // 服务器运行：接收累计的输入行，返回 { output, needInput, success }。
  serverRun: { type: Function, default: null },
  // 为空时按“直接运行”，教师批改页可传“运行查看仅供评分参考”。
  note: { type: String, default: '' }
})

const PYODIDE_BASE = new URL(import.meta.env.VITE_PYODIDE_BASE_URL || '/pyodide/', location.href).href.replace(/\/?$/, '/')

const transcript = ref('')
const statusText = ref('尚未运行')
const state = ref('idle')
const waitingInput = ref(false)
const inputText = ref('')
const hasRun = ref(false)
// 高版本浏览器优先本地执行；失败或不可用时自动降级到服务器，用户无需选择。
const mode = ref(canRunLocally() ? 'local' : 'server')
const screen = ref(null)
const inputElement = ref(null)
// 服务器模式累计输入行：每次“回车确认”追加一行后整体重放，保证提示语与输出顺序真实。
const serverInputs = ref([])
let sequence = 0

const busy = computed(() => ['loading', 'running', 'input'].includes(state.value))
const modeLabel = computed(() => mode.value === 'local' ? '本机运行' : '服务器运行')
const placeholder = computed(() => props.inputCount === 1
  ? '点击“运行 Python”，程序会提示你输入。'
  : '点击“运行 Python”，程序会在这里显示提示和结果。')
const noteText = computed(() => {
  if (props.note) return props.note
  if (mode.value === 'server') return '当前浏览器不支持本机运行，已自动切换到服务器运行，功能不受影响。'
  return '本机运行不占用服务器，结果不会保存。'
})

const runner = createInteractiveRunner(event => {
  if (event.type === 'output') {
    transcript.value += event.text
    scrollToEnd()
    return
  }
  if (event.type === 'loading') {
    state.value = 'loading'
    statusText.value = event.text || '正在准备 Python…'
    return
  }
  if (event.type === 'running') {
    state.value = 'running'
    statusText.value = '正在运行…'
    return
  }
  if (event.type === 'input') {
    state.value = 'input'
    waitingInput.value = true
    statusText.value = '等待输入，填好后按回车'
    nextTick(() => inputElement.value?.focus())
    return
  }
  if (event.type === 'done') {
    state.value = 'done'
    waitingInput.value = false
    statusText.value = '运行结束'
    return
  }
  if (event.type === 'stopped') {
    state.value = 'idle'
    waitingInput.value = false
    statusText.value = '已停止，可以重新运行'
    return
  }
  if (event.type === 'error') {
    // 兼容性原因失败才自动降级，其余错误如实展示，避免把代码错误当成浏览器问题。
    if (mode.value === 'local' && isCompatibilityIssue(event.text)) {
      mode.value = 'server'
      runOnServer()
      return
    }
    state.value = 'error'
    waitingInput.value = false
    statusText.value = '运行出错，可以修改后重试'
    transcript.value = event.text
    scrollToEnd()
  }
})

function isCompatibilityIssue(text) {
  const value = String(text || '')
  return /不支持|无法创建|加载失败|加载超时|无法启动|WebAssembly|Worker/i.test(value)
}

function scrollToEnd() {
  nextTick(() => { if (screen.value) screen.value.scrollTop = screen.value.scrollHeight })
}

function reset() {
  transcript.value = ''
  waitingInput.value = false
  inputText.value = ''
  serverInputs.value = []
  state.value = 'idle'
  statusText.value = '尚未运行'
}

function start() {
  if (!props.code.trim()) {
    statusText.value = '还没有代码'
    transcript.value = '请先在上方编辑区写好代码。'
    return
  }
  sequence++
  hasRun.value = true
  reset()
  if (mode.value === 'local') {
    state.value = 'loading'
    statusText.value = '正在准备 Python…'
    runner.run(props.code, PYODIDE_BASE)
  } else {
    runOnServer()
  }
}

async function runOnServer() {
  if (!props.serverRun) {
    state.value = 'error'
    statusText.value = '服务器运行暂不可用'
    transcript.value = '当前环境没有配置服务器运行，请稍后重试。'
    return
  }
  const ticket = ++sequence
  state.value = 'running'
  waitingInput.value = false
  statusText.value = '正在运行…'
  try {
    const result = await runInteractiveOnServer(props.serverRun, serverInputs.value)
    if (ticket !== sequence) return
    transcript.value = result?.output || '程序没有输出'
    if (result?.needInput) {
      state.value = 'input'
      waitingInput.value = true
      statusText.value = '等待输入，填好后按回车'
      nextTick(() => inputElement.value?.focus())
    } else if (result?.success === false) {
      state.value = 'error'
      statusText.value = '运行出错，可以修改后重试'
    } else {
      state.value = 'done'
      statusText.value = '运行结束'
    }
    scrollToEnd()
  } catch (error) {
    if (ticket !== sequence) return
    state.value = 'error'
    statusText.value = '运行失败，可以重试'
    transcript.value = error?.msg || error?.message || '运行服务暂时不可用，请稍后重试。'
    scrollToEnd()
  }
}

function sendInput() {
  if (!waitingInput.value) return
  const value = inputText.value
  if (mode.value === 'local') {
    if (runner.input(value)) {
      inputText.value = ''
      waitingInput.value = false
      state.value = 'running'
      statusText.value = '正在运行…'
    }
    return
  }
  // 服务器模式：追加这一行后整体重跑，程序会带着完整输入重新执行到下一个 input()。
  serverInputs.value = [...serverInputs.value, value]
  inputText.value = ''
  runOnServer()
}

function stop() {
  sequence++
  runner.stop()
}

watch(() => props.code, () => {
  sequence++
  runner.dispose()
  hasRun.value = false
  reset()
})
onBeforeUnmount(() => runner.dispose())

defineExpose({ start, stop, reset })
</script>

<style scoped>
.py-terminal { display: flex; flex-direction: column; gap: 8px; min-width: 0; }
.py-head { display: flex; align-items: center; justify-content: space-between; gap: 12px; color: #303133; }
.py-head span { color: #606266; font-size: 13px; }
.py-head span.is-error { color: #b42318; }
.py-screen {
  min-height: 132px;
  max-height: 300px;
  padding: 12px 14px;
  overflow: auto;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #f8fafc;
  color: #1f2937;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  font: 14px/1.75 Consolas, "Microsoft YaHei", monospace;
}
.py-placeholder { color: #667085; font-size: 13px; font-family: "Microsoft YaHei", sans-serif; }
.py-input { display: inline; white-space: normal; }
.py-input input {
  width: clamp(72px, 24vw, 240px);
  max-width: 100%;
  padding: 2px 4px;
  border: 0;
  border-bottom: 2px solid #409eff;
  background: #fff;
  color: inherit;
  font: inherit;
  border-radius: 0;
}
.py-input input:focus { outline: 2px solid #91caff; outline-offset: 2px; }
.py-input button {
  min-height: 34px;
  margin-left: 8px;
  padding: 4px 10px;
  border: 1px solid #b1c9df;
  border-radius: 4px;
  background: #fff;
  color: #235780;
  cursor: pointer;
}
.py-actions { display: flex; flex-wrap: wrap; align-items: center; gap: 10px; }
.py-primary {
  min-height: 34px;
  padding: 6px 16px;
  border: 0;
  border-radius: 4px;
  background: #409eff;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
}
.py-primary:disabled { background: #a0cfff; cursor: not-allowed; }
.py-ghost { min-height: 34px; padding: 4px 12px; border: 1px solid #dcdfe6; border-radius: 4px; background: #fff; color: #606266; cursor: pointer; }
.py-mode { color: #909399; font-size: 12px; }
.py-note { margin: 0; color: #667085; font-size: 12px; line-height: 1.6; }
</style>
