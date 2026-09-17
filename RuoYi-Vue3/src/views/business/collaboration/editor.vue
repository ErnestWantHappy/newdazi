<template>
  <div class="cryptpad-editor-page">
    <div class="editor-toolbar">
      <span>{{ session.title || '在线协作文档' }}</span>
      <el-tag v-if="!error" size="small" type="info">v{{ session.version || 1 }}</el-tag>
      <el-tag v-if="!error" :type="loading ? 'info' : saved ? 'success' : 'warning'">{{ loading ? '正在打开' : session.readOnly ? '历史作品 · 只读' : saved ? '已保存' : '编辑中' }}</el-tag>
      <el-button size="small" :disabled="!!error" @click="openMemberDrawer">
        <el-icon><User /></el-icon>
        <span class="member-count">在线成员 ({{ members.length }}/{{ rosterTotal }})</span>
      </el-button>
      <el-button size="small" @click="reload">重新加载</el-button>
    </div>
    <el-drawer v-model="memberDrawerVisible" title="在线成员监控" direction="rtl" size="360px" destroy-on-close>
      <div class="roster-head">
        <div class="roster-title">{{ roster.groupName || session.room?.groupName || '本组' }} · {{ roster.fileName || session.room?.fileName || session.title }}</div>
        <el-button size="small" :loading="rosterLoading" @click="loadRoster">刷新状态</el-button>
      </div>
      <div v-if="roster.teachers?.length" class="roster-section">
        <div class="roster-section-title">教师</div>
        <div v-for="t in roster.teachers" :key="'t-' + t.name" class="member-item">
          <span class="member-dot teacher" />
          <span class="member-name">{{ t.name }}</span>
          <el-tag size="small" type="primary" effect="dark">教师</el-tag>
        </div>
      </div>
      <div class="roster-section">
        <div class="roster-section-title">组内应到（{{ rosterMembers.length }}人）</div>
        <div v-if="rosterMembers.length" class="member-list">
          <div v-for="m in rosterMembers" :key="m.studentId" class="member-item">
            <span class="member-dot" :class="{ online: m.realtimeOnline }" />
            <span class="member-name">{{ m.name }}</span>
            <el-tag v-if="m.realtimeOnline" size="small" type="success" effect="dark">在线编辑中</el-tag>
            <el-tag v-else size="small" type="info">{{ m.enterTime ? '来过，未在编辑' : '未进入' }}</el-tag>
          </div>
        </div>
        <div v-else class="member-empty">{{ rosterLoading ? '正在加载花名册…' : '暂无本组花名册（旧全班房间只显示实时在线成员）' }}</div>
      </div>
      <div v-if="realtimeOnly.length" class="roster-section">
        <div class="roster-section-title">实时在线（不在花名册）</div>
        <div v-for="member in realtimeOnly" :key="member.key" class="member-item">
          <span class="member-dot online" />
          <span class="member-name">{{ member.name || '协作用户' }}</span>
          <el-tag size="small" type="success">在线编辑中</el-tag>
        </div>
      </div>
    </el-drawer>
    <div v-if="error" class="editor-error"><el-result icon="warning" title="协作暂时不可用" :sub-title="error"><template #extra><el-button type="primary" @click="reload">重新加载</el-button><el-button @click="copyDiagnostics">复制诊断信息</el-button></template></el-result></div>
    <div v-else :key="editorContainerId" ref="container" class="editor-container">
      <div :id="editorContainerId" class="editor-mount"></div>
      <div v-if="loading" class="editor-loading"><el-skeleton :rows="8" animated /><p>正在打开协作文档，请稍候…</p></div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { getCollaborationDocument, getCollaborationRoster, getCollaborationSession, heartbeatCollaborationRoom, leaveCollaborationRoom, saveCollaborationDocument } from '@/api/business/collaboration'

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
let heartbeatTimer = null
// CryptPad API 没有组件级销毁入口；用递增编号丢弃已离开页面的异步回调。
let initializationId = 0
let editorSequence = 0
const editorContainerId = ref(nextEditorContainerId())

const EDITOR_INIT_TIMEOUT = 90000
// 跨网段直连 129 失败时脚本请求会挂起无回调；用超时把假死变成可诊断的错误。
const SCRIPT_LOAD_TIMEOUT = 15000

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
// 成员抽屉：花名册来自后端（应到+进入时间），实时在线来自 CryptPad 用户列表，两者按姓名合并。
const memberDrawerVisible = ref(false)
const roster = ref({ groupName: '', fileName: '', members: [], teachers: [] })
const rosterLoading = ref(false)
async function loadRoster() {
  rosterLoading.value = true
  try {
    const response = await getCollaborationRoster(route.params.roomId)
    roster.value = response.data || response || { groupName: '', fileName: '', members: [], teachers: [] }
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || e?.message || '花名册加载失败')
  } finally {
    rosterLoading.value = false
  }
}
function openMemberDrawer() {
  memberDrawerVisible.value = true
  loadRoster()
}
const rosterMembers = computed(() => {
  const onlineNames = new Set(members.value.map(m => m.name))
  return (roster.value.members || []).map(m => ({ ...m, realtimeOnline: onlineNames.has(m.name) }))
})
const rosterTotal = computed(() => rosterMembers.value.length || members.value.length)
const realtimeOnly = computed(() => {
  const rosterNames = new Set((roster.value.members || []).map(m => m.name))
  return members.value.filter(m => !rosterNames.has(m.name))
})

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

function loadScriptWithTimeout(url) {
  if (!url) return Promise.reject(new Error('协作服务地址未配置，请联系管理员检查协作代理配置'))
  let timer = null
  const timeout = new Promise((_, reject) => {
    timer = setTimeout(() => reject(new Error(`协作编辑器脚本加载超时（${url}），请检查机房网络或协作代理配置`)), SCRIPT_LOAD_TIMEOUT)
  })
  return Promise.race([loadScript(url), timeout]).finally(() => { if (timer) clearTimeout(timer) })
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
function cleanupEditor() {
  if (heartbeatTimer) clearInterval(heartbeatTimer)
  heartbeatTimer = null
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
    if (filename.includes('common-coller.js')) {
      error.value = '协作编辑器脚本与当前浏览器不兼容，请升级 Chrome 或 Edge 后重试'
      loading.value = false
    }
  }
  window.addEventListener('error', windowErrorHandler, true)
  try {
    const response = await getCollaborationSession(route.params.roomId)
    if (!isCurrentInitialization(currentInitializationId)) return
    Object.assign(session, response.data || response)
    if (session.scope === 'STUDENT') heartbeatTimer = setInterval(() => heartbeatCollaborationRoom(route.params.roomId).catch(() => {}), 30000)
    const blob = await getCollaborationDocument(route.params.roomId)
    if (!isCurrentInitialization(currentInitializationId)) return
    objectUrl = URL.createObjectURL(blob)
    await loadScriptWithTimeout(session.apiUrl)
    if (!isCurrentInitialization(currentInitializationId)) return
    const editorReady = window.CryptPadAPI(new URL(session.baseUrl, window.location.origin).href, editorContainerId.value, {
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
    // iframe 出现只代表外壳创建；必须等待 CryptPad 完成文档初始化，才能提示已就绪。
    await Promise.race([editorReady, new Promise((_, reject) => {
      rejectEditorFrame = reject
      initTimer = setTimeout(() => reject(new Error('协作文档打开超时，请重试；若仍失败，请联系教师检查协作服务')), EDITOR_INIT_TIMEOUT)
    })])
    clearTimeout(initTimer)
    initTimer = null
    rejectEditorFrame = null
    if (!isCurrentInitialization(currentInitializationId)) return
  } catch (e) {
    if (!isCurrentInitialization(currentInitializationId)) return
    error.value = e?.message || (typeof e === 'string' ? e : '无法连接协作服务')
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
  if (session.scope === 'STUDENT') leaveCollaborationRoom(route.params.roomId).catch(() => {})
  cleanupEditor()
})
</script>

<style scoped>
.cryptpad-editor-page { height: calc(100vh - 84px); display: flex; flex-direction: column; background: #f5f7fa; }
.editor-toolbar { min-height: 48px; padding: 0 16px; display: flex; align-items: center; gap: 12px; background: #fff; border-bottom: 1px solid #ebeef5; }
.editor-toolbar span { flex: 1; font-weight: 600; }
.editor-toolbar .member-count { margin-left: 4px; }
.editor-container { flex: 1; min-height: 0; background: #fff; position: relative; }
.editor-mount { height: 100%; }
.editor-loading { position: absolute; inset: 0; background: #fff; padding: 32px; }
.editor-error { flex: 1; display: flex; align-items: center; justify-content: center; }
.member-empty { font-size: 12px; color: #909399; padding: 8px 0; }
.roster-head { display: flex; align-items: center; justify-content: space-between; gap: 8px; margin-bottom: 12px; }
.roster-title { font-size: 13px; font-weight: 600; color: #303133; }
.roster-section { margin-bottom: 16px; }
.roster-section-title { font-size: 12px; color: #909399; margin-bottom: 8px; }
.member-list { display: flex; flex-direction: column; gap: 6px; }
.member-item { display: flex; align-items: center; gap: 8px; padding: 6px 8px; border-radius: 4px; background: #f7f9fb; }
.member-dot { width: 8px; height: 8px; border-radius: 50%; background: #c0c4cc; flex: 0 0 auto; }
.member-dot.online { background: #67c23a; }
.member-dot.teacher { background: #409eff; }
.member-name { flex: 1; font-size: 13px; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>

