<template>
  <div class="cryptpad-editor-page">
    <div class="editor-toolbar">
      <span>{{ session.title || '在线协作文档' }}</span>
      <el-tag size="small" type="info">v{{ session.version || 1 }}</el-tag>
      <el-tag :type="saved ? 'success' : 'warning'">{{ saved ? '已保存' : '编辑中' }}</el-tag>
      <el-popover placement="bottom-end" :width="260" trigger="click" popper-class="member-popover">
        <template #reference>
          <el-button size="small" :disabled="error">
            <el-icon><User /></el-icon>
            <span class="member-count">在线成员 {{ members.length }}</span>
          </el-button>
        </template>
        <div class="member-panel">
          <div class="member-panel-title">当前房间在线成员</div>
          <div v-if="members.length" class="member-list">
            <div v-for="member in members" :key="member.key" class="member-item">
              <span class="member-dot" :class="{ online: !member.readOnly }"></span>
              <span class="member-name">{{ member.name || '协作用户' }}</span>
              <el-tag v-if="member.isSelf" size="small" type="success" effect="dark">我</el-tag>
              <el-tag v-if="member.readOnly" size="small" type="info">只读</el-tag>
            </div>
          </div>
          <div v-else class="member-empty">暂无其他成员，等待同学或老师进入…</div>
        </div>
      </el-popover>
      <el-button size="small" @click="reload">重新加载</el-button>
    </div>
    <div v-if="error" class="editor-error"><el-result icon="warning" title="协作暂时不可用" :sub-title="error"><template #extra><el-button type="primary" @click="reload">重新加载</el-button><el-button @click="copyDiagnostics">复制诊断信息</el-button></template></el-result></div>
    <div v-else :key="editorContainerId" ref="container" :id="editorContainerId" class="editor-container"><el-skeleton v-if="loading" :rows="8" animated /></div>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { getCollaborationDocument, getCollaborationSession, saveCollaborationDocument } from '@/api/business/collaboration'

const route = useRoute()
const container = ref(null)
const loading = ref(true)
const saved = ref(true)
const error = ref('')
const session = reactive({})
const members = ref([])
let objectUrl = null
let initTimer = null
let initObserver = null
let rejectEditorFrame = null
let initStartedAt = 0
let windowErrorHandler = null
let saveChain = Promise.resolve()
// CryptPad API 没有组件级销毁入口；用递增编号丢弃已离开页面的异步回调。
let initializationId = 0
let editorSequence = 0
const editorContainerId = ref(nextEditorContainerId())

const EDITOR_INIT_TIMEOUT = 20000

function nextEditorContainerId() {
  editorSequence += 1
  return `cryptpad-editor-${Date.now()}-${editorSequence}`
}

function isCurrentInitialization(id) {
  return id === initializationId
}

/** CryptPad USERLIST_CHANGE 返回 {netfluxId: {id,name,readOnly}}；转成稳定数组并按自己置顶。 */
function applyUserlist(list) {
  const rows = []
  if (list && typeof list === 'object') {
    for (const key of Object.keys(list)) {
      const user = list[key] || {}
      rows.push({
        key,
        id: user.id,
        name: String(user.name || '').trim() || '协作用户',
        readOnly: Boolean(user.readOnly),
        isSelf: Boolean(user.id && user.id === session.participantId)
      })
    }
  }
  rows.sort((a, b) => Number(b.isSelf) - Number(a.isSelf) || a.name.localeCompare(b.name, 'zh-CN'))
  members.value = rows
}

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
    const existing = document.querySelector('script[data-cryptpad-api="true"]')
    if (existing) {
      if (existing.dataset.cryptpadState === 'failed' || existing.dataset.cryptpadState === 'loaded') {
        existing.remove()
      } else {
        existing.addEventListener('load', resolve, { once: true })
        existing.addEventListener('error', () => reject(new Error('CryptPad 集成脚本加载失败，请检查机房网络或浏览器拦截')), { once: true })
        return
      }
    }
    const apiScript = document.createElement('script')
    apiScript.src = url
    apiScript.dataset.cryptpadApi = 'true'
    apiScript.onload = () => {
      apiScript.dataset.cryptpadState = 'loaded'
      resolve()
    }
    apiScript.onerror = () => {
      apiScript.dataset.cryptpadState = 'failed'
      reject(new Error('CryptPad 集成脚本加载失败，请检查机房网络或浏览器拦截'))
    }
    document.head.appendChild(apiScript)
  })
}

function waitForEditorFrame() {
  return new Promise((resolve, reject) => {
    rejectEditorFrame = reject
    const target = container.value
    if (!target) {
      rejectEditorFrame = null
      return reject(new Error('协作编辑器容器未找到'))
    }
    const existing = target.querySelector('iframe')
    if (existing) {
      rejectEditorFrame = null
      return resolve()
    }
    const finish = (callback, value) => {
      if (initTimer) clearTimeout(initTimer)
      if (initObserver) initObserver.disconnect()
      initTimer = null
      initObserver = null
      rejectEditorFrame = null
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

function cleanupEditor() {
  initializationId += 1
  if (initTimer) clearTimeout(initTimer)
  if (initObserver) initObserver.disconnect()
  if (rejectEditorFrame) rejectEditorFrame(new Error('协作编辑器已关闭'))
  initTimer = null
  initObserver = null
  rejectEditorFrame = null
  if (container.value) {
    const frame = container.value.querySelector('iframe')
    // 先断开 iframe，再移除节点，确保浏览器关闭旧的 CryptPad 实时连接。
    if (frame) frame.src = 'about:blank'
    container.value.innerHTML = ''
  }
  if (objectUrl) URL.revokeObjectURL(objectUrl)
  objectUrl = null
  members.value = []
  saveChain = Promise.resolve()
  if (windowErrorHandler) window.removeEventListener('error', windowErrorHandler, true)
  windowErrorHandler = null
}

async function open() {
  cleanupEditor()
  const currentInitializationId = initializationId
  loading.value = true
  error.value = ''
  saved.value = true
  editorContainerId.value = nextEditorContainerId()
  await nextTick()
  if (!isCurrentInitialization(currentInitializationId)) return
  if (initTimer) clearTimeout(initTimer)
  initStartedAt = performance.now()
  const compatibilityMessage = compatibilityError()
  if (compatibilityMessage) {
    error.value = compatibilityMessage
    loading.value = false
    return
  }
  windowErrorHandler = event => {
    if (!isCurrentInitialization(currentInitializationId)) return
    const filename = String(event?.filename || '')
    if (filename.includes('office.xsedu.net.cn') || filename.includes('common-coller.js')) {
      error.value = '协作编辑器脚本与当前浏览器不兼容，请升级 Chrome 或 Edge 后重试'
      loading.value = false
    }
  }
  window.addEventListener('error', windowErrorHandler, true)
  try {
    const response = await getCollaborationSession(route.params.roomId)
    if (!isCurrentInitialization(currentInitializationId)) return
    Object.assign(session, response.data || response)
    const blob = await getCollaborationDocument(route.params.roomId)
    if (!isCurrentInitialization(currentInitializationId)) return
    objectUrl = URL.createObjectURL(blob)
    await loadScript(session.apiUrl)
    if (!isCurrentInitialization(currentInitializationId)) return
    window.CryptPadAPI(editorContainerId.value, {
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
        onSave: (file, callback) => {
          if (!isCurrentInitialization(currentInitializationId)) return callback(new Error('协作编辑器已关闭'))
          saved.value = false
          // CryptPad 可能在短时间内连续触发 onSave；串行提交避免后一个保存携带旧版本号。
          saveChain = saveChain.then(async () => {
            try {
              if (!isCurrentInitialization(currentInitializationId)) throw new Error('协作编辑器已关闭')
              let result
              try {
                result = await saveCollaborationDocument(route.params.roomId, file, session.version, saveFileName(file))
              } catch (e) {
                const message = String(e?.response?.data?.msg || e?.message || '')
                if (!message.includes('版本已变化')) throw e
                const latest = await getCollaborationSession(route.params.roomId)
                session.version = latest?.data?.version ?? latest?.version ?? session.version
                result = await saveCollaborationDocument(route.params.roomId, file, session.version, saveFileName(file))
              }
              if (!isCurrentInitialization(currentInitializationId)) throw new Error('协作编辑器已关闭')
              session.version = result?.data?.version ?? result?.version ?? session.version + 1
              saved.value = true
              callback()
            } catch (e) {
              if (isCurrentInitialization(currentInitializationId)) {
                ElMessage.error(e?.message || '协作文档保存失败')
              }
              callback(e instanceof Error ? e : new Error('协作文档保存失败'))
            }
          })
        },
        onHasUnsavedChanges: value => {
          if (isCurrentInitialization(currentInitializationId)) saved.value = !value
        },
        // CryptPad 集成 API 的实时用户列表：进入/离开都会推送最新成员集合。
        onUserlistChange: list => {
          if (isCurrentInitialization(currentInitializationId)) applyUserlist(list)
        }
      }
    })
    await waitForEditorFrame()
    if (!isCurrentInitialization(currentInitializationId)) return
  } catch (e) {
    if (!isCurrentInitialization(currentInitializationId)) return
    error.value = e?.message || '无法连接协作服务'
  } finally {
    if (isCurrentInitialization(currentInitializationId)) loading.value = false
    if (isCurrentInitialization(currentInitializationId) && windowErrorHandler) {
      window.removeEventListener('error', windowErrorHandler, true)
      windowErrorHandler = null
    }
  }
}
function reload() {
  open()
}
onMounted(open)
onBeforeUnmount(() => {
  cleanupEditor()
})
</script>

<style scoped>
.cryptpad-editor-page { height: calc(100vh - 84px); display: flex; flex-direction: column; background: #f5f7fa; }
.editor-toolbar { min-height: 48px; padding: 0 16px; display: flex; align-items: center; gap: 12px; background: #fff; border-bottom: 1px solid #ebeef5; }
.editor-toolbar span { flex: 1; font-weight: 600; }
.editor-toolbar .member-count { margin-left: 4px; }
.editor-container { flex: 1; min-height: 0; background: #fff; }
.editor-error { flex: 1; display: flex; align-items: center; justify-content: center; }
</style>

<style>
.member-popover .member-panel-title { font-size: 13px; font-weight: 600; color: #303133; margin-bottom: 8px; }
.member-popover .member-list { display: flex; flex-direction: column; gap: 6px; max-height: 300px; overflow-y: auto; }
.member-popover .member-item { display: flex; align-items: center; gap: 8px; padding: 4px 6px; border-radius: 4px; background: #f7f9fb; }
.member-popover .member-dot { width: 8px; height: 8px; border-radius: 50%; background: #c0c4cc; flex: 0 0 auto; }
.member-popover .member-dot.online { background: #67c23a; }
.member-popover .member-name { flex: 1; font-size: 13px; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.member-popover .member-empty { font-size: 12px; color: #909399; padding: 8px 0; }
</style>
