<template>
  <div class="cryptpad-editor-page">
    <div class="editor-toolbar"><span>{{ session.title || '在线协作文档' }}</span><el-tag :type="saved ? 'success' : 'warning'">{{ saved ? '已保存' : '编辑中' }}</el-tag><el-button size="small" @click="reload">重新加载</el-button></div>
    <div v-if="error" class="editor-error"><el-result icon="warning" title="协作暂时不可用" :sub-title="error"><template #extra><el-button type="primary" @click="reload">重新加载</el-button><el-button @click="copyDiagnostics">复制诊断信息</el-button></template></el-result></div>
    <div v-else ref="container" id="cryptpad-editor" class="editor-container"><el-skeleton v-if="loading" :rows="8" animated /></div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCollaborationDocument, getCollaborationSession, saveCollaborationDocument } from '@/api/business/collaboration'

const route = useRoute()
const container = ref(null)
const loading = ref(true)
const saved = ref(true)
const error = ref('')
const session = reactive({})
let objectUrl = null
let apiScript = null
let initTimer = null
let initObserver = null
let initStartedAt = 0
let windowErrorHandler = null

const EDITOR_INIT_TIMEOUT = 20000

function browserDiagnostics() {
  return {
    userAgent: navigator.userAgent,
    language: navigator.language,
    webAssembly: typeof WebAssembly !== 'undefined',
    webSocket: typeof WebSocket !== 'undefined',
    blob: typeof Blob !== 'undefined',
    objectUrl: Boolean(window.URL && URL.createObjectURL),
    elapsedMs: initStartedAt ? Math.round(performance.now() - initStartedAt) : 0,
    apiUrl: session.apiUrl || ''
  }
}

function compatibilityError() {
  try {
    // CryptPad 2026 的脚本使用 class field、可选链和空值合并；旧 Chrome 会在加载脚本前解析失败。
    new Function('class CryptPadSyntaxProbe { value = 1; test() { return globalThis?.location?.href ?? "" } }')
  } catch (e) {
    return '当前 Google Chrome 版本过低，无法打开在线协作，请升级 Chrome 或使用 Edge 浏览器'
  }
  if (typeof WebAssembly === 'undefined' || typeof WebSocket === 'undefined' || typeof Blob === 'undefined') {
    return '当前浏览器缺少在线协作所需能力，请升级 Chrome 或 Edge 后重试'
  }
  return ''
}

function copyDiagnostics() {
  const text = JSON.stringify(browserDiagnostics(), null, 2)
  const writePromise = navigator.clipboard && typeof navigator.clipboard.writeText === 'function'
    ? navigator.clipboard.writeText(text)
    : Promise.reject(new Error('当前浏览器不支持剪贴板'))
  writePromise
    .then(() => ElMessage.success('诊断信息已复制'))
    .catch(() => ElMessage.info(text))
}

function saveFileName(file) {
  const extension = String(session.fileType || 'docx').replace(/[^A-Za-z0-9]/g, '') || 'docx'
  const name = String(file?.name || session.title || '协作文档').trim()
  return name.toLowerCase().endsWith(`.${extension.toLowerCase()}`) ? name : `${name}.${extension}`
}

function loadScript(url) {
  return new Promise((resolve, reject) => {
    if (window.CryptPadAPI) return resolve()
    apiScript = document.createElement('script')
    apiScript.src = url
    apiScript.onload = resolve
    apiScript.onerror = () => reject(new Error('CryptPad 集成脚本加载失败，请检查机房网络或浏览器拦截'))
    document.head.appendChild(apiScript)
  })
}

function waitForEditorFrame() {
  return new Promise((resolve, reject) => {
    const target = container.value
    if (!target) return reject(new Error('协作编辑器容器未找到'))
    const existing = target.querySelector('iframe')
    if (existing) return resolve()
    const finish = (callback, value) => {
      if (initTimer) clearTimeout(initTimer)
      if (initObserver) initObserver.disconnect()
      initTimer = null
      initObserver = null
      callback(value)
    }
    initObserver = new MutationObserver(() => {
      if (target.querySelector('iframe')) finish(resolve)
    })
    initObserver.observe(target, { childList: true, subtree: true })
    initTimer = setTimeout(() => finish(reject,
      new Error('协作编辑器加载超时，请升级浏览器或检查 office.xsedu.net.cn 网络访问')), EDITOR_INIT_TIMEOUT)
  })
}

async function open() {
  if (initTimer) clearTimeout(initTimer)
  loading.value = true
  error.value = ''
  initStartedAt = performance.now()
  const compatibilityMessage = compatibilityError()
  if (compatibilityMessage) {
    error.value = compatibilityMessage
    loading.value = false
    return
  }
  windowErrorHandler = event => {
    const filename = String(event?.filename || '')
    if (filename.includes('office.xsedu.net.cn') || filename.includes('common-coller.js')) {
      error.value = '协作编辑器脚本与当前浏览器不兼容，请升级 Chrome 或 Edge 后重试'
      loading.value = false
    }
  }
  window.addEventListener('error', windowErrorHandler, true)
  try {
    const response = await getCollaborationSession(route.params.roomId)
    Object.assign(session, response.data || response)
    const blob = await getCollaborationDocument(route.params.roomId)
    objectUrl = URL.createObjectURL(blob)
    await loadScript(session.apiUrl)
    window.CryptPadAPI('cryptpad-editor', {
      document: { url: objectUrl, fileType: session.fileType, title: session.title, key: session.documentKey },
      documentType: session.documentType,
      mode: session.mode,
      autosave: session.autosave,
      editorConfig: {
        lang: 'zh',
        // 姓名供成员列表展示，稳定 ID 防止 CryptPad 将多个登录者误判为同一匿名会话。
        user: { id: session.participantId, name: session.user }
      },
      events: {
        onSave: async (file, callback) => {
          saved.value = false
          try {
            const result = await saveCollaborationDocument(route.params.roomId, file, session.version, saveFileName(file))
            session.version = result?.data?.version ?? result?.version ?? session.version + 1
            saved.value = true
            callback()
          } catch (e) {
            ElMessage.error(e?.message || '协作文档保存失败')
            callback()
          }
        },
        onHasUnsavedChanges: value => { saved.value = !value }
      }
    })
    await waitForEditorFrame()
  } catch (e) {
    error.value = e?.message || '无法连接协作服务'
  } finally {
    loading.value = false
    if (windowErrorHandler) window.removeEventListener('error', windowErrorHandler, true)
    windowErrorHandler = null
  }
}
function reload() { if (objectUrl) URL.revokeObjectURL(objectUrl); container.value && (container.value.innerHTML = ''); open() }
onMounted(open)
onBeforeUnmount(() => {
  if (objectUrl) URL.revokeObjectURL(objectUrl)
  if (apiScript) apiScript.remove()
  if (initTimer) clearTimeout(initTimer)
  if (initObserver) initObserver.disconnect()
  if (windowErrorHandler) window.removeEventListener('error', windowErrorHandler, true)
})
</script>

<style scoped>
.cryptpad-editor-page { height: calc(100vh - 84px); display: flex; flex-direction: column; background: #f5f7fa; }
.editor-toolbar { min-height: 48px; padding: 0 16px; display: flex; align-items: center; gap: 12px; background: #fff; border-bottom: 1px solid #ebeef5; }
.editor-toolbar span { flex: 1; font-weight: 600; }
.editor-container { flex: 1; min-height: 0; background: #fff; }
.editor-error { flex: 1; display: flex; align-items: center; justify-content: center; }
</style>
