<template>
  <section class="terminal-section" aria-label="Python 运行终端">
    <div class="terminal-heading"><strong>运行结果</strong><span role="status" :class="{ 'has-error': state === 'error' }">{{ status }}</span></div>
    <div ref="screen" class="terminal-screen" role="log" aria-label="程序输出" tabindex="0">
      <span>{{ transcript }}</span><form v-if="state === 'input'" class="terminal-input" @submit.prevent="sendInput"><input ref="inputElement" v-model="inputText" aria-label="程序等待输入，输入后按回车" autocomplete="off" spellcheck="false" maxlength="4096" :disabled="!busy"><button type="submit">回车确认</button></form>
      <span v-if="!transcript && state === 'idle'" class="terminal-placeholder">点击“运行”，按程序提示输入并按回车。</span>
    </div>
    <div class="terminal-note">运行用于调试，不计分；本次输出刷新后不保留。代码自动保存，交作业后可在提交记录中查看。</div>
  </section>
</template>

<script setup>
import { computed, nextTick, onUnmounted, ref } from 'vue'
import { createInteractiveRunner } from './interactiveRunner'

const emit = defineEmits(['busy'])
const state = ref('idle')
const status = ref('等待运行')
const transcript = ref('')
const inputText = ref('')
const screen = ref(null)
const inputElement = ref(null)
const busy = computed(() => ['loading', 'running', 'input'].includes(state.value))
const runner = createInteractiveRunner(event => {
  if (event.type === 'output') transcript.value += event.text
  else {
    state.value = event.type
    status.value = ({ loading: event.text, running: '正在运行…', input: '等待输入，按回车继续', done: '运行结束', error: '运行失败，可以修改后重试', stopped: '已停止，可以重新运行' })[event.type]
    if (event.type === 'error') transcript.value += `${transcript.value && !transcript.value.endsWith('\n') ? '\n' : ''}${event.text}\n`
    emit('busy', busy.value)
  }
  nextTick(() => {
    if (screen.value) screen.value.scrollTop = screen.value.scrollHeight
    if (state.value === 'input') inputElement.value?.focus()
  })
})

function run(code) {
  transcript.value = ''
  inputText.value = ''
  runner.run(code, new URL(import.meta.env.VITE_PYODIDE_BASE_URL || '/pyodide/', location.href).href.replace(/\/?$/, '/'))
}
function sendInput() {
  if (state.value !== 'input') return
  if (runner.input(inputText.value)) {
    inputText.value = ''
    state.value = 'running'
    status.value = '正在运行…'
  }
}
function stop() { runner.stop() }
onUnmounted(() => runner.dispose())
defineExpose({ run, stop })
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
</style>
