<template>
  <section class="terminal-section" aria-label="Python 运行终端">
    <div class="terminal-heading"><strong>运行结果</strong><span role="status" :class="{ 'has-error': state === 'error' }">{{ status }}</span></div>
    <!-- 本地运行和服务器运行共用同一终端外观：都是“提示语 → 你输入 → 输出” -->
    <div ref="screen" class="terminal-screen" role="log" aria-label="程序输出" tabindex="0">
      <!-- 输入框紧跟在提示语同一行，和 Thonny 一样，不会出现“提示语换行后才有输入框” -->
      <span>{{ transcript }}</span><form v-if="state === 'input'" class="terminal-input" @submit.prevent="sendInput"><input ref="inputElement" v-model="inputText" aria-label="程序等待输入，输入后按回车" autocomplete="off" spellcheck="false" maxlength="4096" :disabled="!busy"><button type="submit">回车确认</button></form>
      <span v-if="!transcript && state === 'idle'" class="terminal-placeholder">点击“运行”，程序会在这里显示提示；按提示输入并按回车。</span>
    </div>
    <div class="terminal-note">运行用于调试，不计分；本次输出刷新后不保留。代码自动保存，交作业后可在提交记录中查看。</div>

    <!-- 运行方式由系统决定，只在降级时用一句轻提示告知，不打断操作 -->
    <p v-if="!localSupported" class="terminal-mode-hint">
      {{ fallbackNotice || '当前浏览器版本较低，已自动改用服务器运行，使用方法与原来相同。' }}升级到最新版
      <a href="https://www.google.cn/chrome/" target="_blank" rel="noopener">Chrome</a>
      或
      <a href="https://www.microsoft.com/edge" target="_blank" rel="noopener">Edge</a>
      后可在本机运行、启动更快。
    </p>
  </section>
</template>

<script setup>
import { computed, nextTick, onUnmounted, ref } from 'vue'
import { createInteractiveRunner } from './interactiveRunner'
import { canRunLocally } from './interactiveRunner.js'
import { shouldFallbackToServer, FALLBACK_NOTICE } from '@/components/PythonRunner/fallback'

// serverMode 仅保留兼容旧调用；实际运行方式由浏览器能力自动决定。
const props = defineProps({
  serverMode: { type: Boolean, default: false },
  // 课程与题目标识；本机运行失败自动降级时需要它
  context: { type: Object, default: null }
})
const emit = defineEmits(['busy', 'completed', 'fallback'])
const state = ref('idle')
const status = ref('等待运行')
const transcript = ref('')
const inputText = ref('')
// 服务器模式累计的输入行；每次提交都给到目前已填的全部内容
const serverInputs = ref([])
// 本机模式已填过的输入：本机中途失败转服务器时可以直接接着用
const localInputs = ref([])
let serverContext = null
const screen = ref(null)
const inputElement = ref(null)
const busy = computed(() => ['loading', 'running', 'input'].includes(state.value))
// 运行时再判一次：同一份前端会被不同版本浏览器使用，不能只按构建环境决定
const localSupported = ref(canRunLocally())
// 降级后要告诉学生发生了什么，但不能让他以为自己做错了
const fallbackNotice = ref('')
const runner = createInteractiveRunner(event => {
  if (event.type === 'output') transcript.value += event.text
  else {
    // 本机运行因浏览器能力失败时自动改走服务器，学生不需要重试或换浏览器。
    if (shouldFallbackToServer(event, localSupported.value ? 'local' : 'server')) {
      localSupported.value = false
      fallbackNotice.value = FALLBACK_NOTICE
      emit('fallback')
      if (serverContext) {
        transcript.value = ''
        // 已经填过的输入带过去，学生不用重输
        serverInputs.value = [...localInputs.value]
        rerunServer()
      } else {
        // 没有可运行的代码时也要复位按钮状态，不能让学生停在“运行中”。
        state.value = 'idle'
        status.value = '等待运行'
        emit('busy', false)
      }
      return
    }
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

/**
 * 唯一的运行入口：能本机就跑本机，跑不了自动改服务器；
 * 调用方不需要先判断浏览器能力，否则一旦判断与实际不符，学生就会卡在“点了没反应”。
 */
function run(code) {
  transcript.value = ''
  inputText.value = ''
  serverInputs.value = []
  localInputs.value = []
  // 记录代码与上下文；本机运行失败降级时可直接用同一份代码走服务器
  serverContext = { code, context: props.context }
  if (!localSupported.value) return rerunServer()
  runner.run(code, new URL(import.meta.env.VITE_PYODIDE_BASE_URL || '/pyodide/', location.href).href.replace(/\/?$/, '/'))
}
function runOnServer(code, context) {
  transcript.value = ''
  serverInputs.value = []
  localInputs.value = []
  serverContext = { code, context }
  localSupported.value = false
  return rerunServer()
}

/**
 * 用累计输入重跑一次；每次 input() 都会在服务端停下并返回 needInput。
 * 服务端返回的是“到目前为止的完整终端内容”，所以要先清空，否则每多输一次就会重复一遍之前的提示和输出。
 */
function rerunServer() {
  if (!serverContext) return Promise.resolve()
  transcript.value = ''
  return runner.runInteractiveOnServer(serverContext.code, serverContext.context, serverInputs.value)
}
function sendInput() {
  if (state.value !== 'input') return
  if (localSupported.value) {
    if (runner.input(inputText.value)) {
      localInputs.value = [...localInputs.value, inputText.value]
      inputText.value = ''
      state.value = 'running'
      status.value = '正在运行…'
    }
    return
  }
  // 服务器模式：累计这一行后重跑，下一个 input() 会再次进入等待输入状态
  serverInputs.value = [...serverInputs.value, inputText.value]
  inputText.value = ''
  rerunServer()
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
.terminal-mode-hint { margin:6px 0 0; color:#8a6d3b; font-size:12px; line-height:1.6; }
.terminal-mode-hint a { color:#1a6fbf; }
</style>
