<template>
  <aside class="case-panel" aria-label="本课教学资源">
    <header><strong>本课教学资源</strong><el-button text @click="$emit('close')">收起</el-button></header>
    <p v-if="downloadHint" class="download-hint" :class="{ 'is-error': downloadHintError }" role="status">{{ downloadHint }}</p>
    <p v-if="loading">正在加载课例…</p>
    <div v-else-if="error">课例加载失败 <el-button link type="primary" @click="load">重试</el-button></div>
    <el-empty v-else-if="!cases.length" description="本课暂无已发布教学资源" :image-size="50" />
    <el-collapse v-else v-model="expanded" class="case-list">
      <el-collapse-item v-for="item in cases" :key="item.caseId" :name="item.caseId">
        <template #title>
          <div class="case-heading">
            <div class="case-heading-main">
              <div class="case-title-row">
                <strong>{{ caseTitle(item) }}</strong>
                <el-tag size="small" :type="isLegacy(item) ? 'success' : 'info'" effect="plain">{{ originLabel(item) }}</el-tag>
              </div>
              <div v-if="item.school || item.author" class="muted">{{ [item.author, item.school].filter(Boolean).join(' · ') }}</div>
              <div v-if="item.researchTopicTitle" class="muted research-topic" :title="item.researchTopicTitle">来源教研活动：{{ item.researchTopicTitle }}</div>
            </div>
            <CaseVotes v-if="isLegacy(item)" :item="item" :can-vote="canVote" :busy="voting !== null" @vote="vote" />
          </div>
        </template>
        <p v-if="voteHints[item.caseId]" role="status" class="vote-hint">{{ voteHints[item.caseId] }}</p>
        <section v-for="resource in item.resources" :key="resource.resourceId" class="resource">
          <strong v-if="resource.title !== item.title">{{ resource.title }}</strong>
          <div class="muted">{{ categoryNames[resource.category] || '配套资源' }} · {{ formatBytes(resource.bytes) }} · {{ resource.filename.split('.').pop().toUpperCase() }}</div>
          <details v-if="resource.requirements" class="resource-usage">
            <summary>使用说明</summary>
            <p>{{ resource.requirements }}</p>
          </details>
          <el-button v-if="Number(resource.previewable)" size="small" type="primary" plain @click="preview(resource)">预览</el-button>
          <el-button v-else-if="isPlayableMedia(resource)" size="small" type="primary" plain @click="preview(resource)">播放</el-button>
          <el-button v-if="Number(resource.htmlPreview)" size="small" type="success" plain @click="openHtml(resource)">打开网页</el-button>
          <el-button size="small" :loading="downloading === resource.resourceId" :disabled="downloading !== null" @click="download(resource)">{{ downloadLabel(resource) }}</el-button>
          <el-button v-if="resource.extraFilename" size="small" :loading="downloading === `extra-${resource.resourceId}`" :disabled="downloading !== null" @click="downloadExtra(resource)">下载 {{ resource.extraFilename.split('.').pop().toUpperCase() }} 版</el-button>
        </section>
      </el-collapse-item>
    </el-collapse>
    <el-dialog v-model="previewVisible" :title="previewTitle" class="case-preview-dialog" width="96%" top="3vh" append-to-body destroy-on-close @close="onPreviewClose" @closed="clearPreview">
      <div v-if="previewResource" class="preview-actions">
        <el-select :model-value="previewResource.resourceId" aria-label="切换预览文件" @change="id => preview(previewSiblings.find(resource => resource.resourceId === id))">
          <el-option v-for="resource in previewSiblings" :key="resource.resourceId" :label="resource.title" :value="resource.resourceId" />
        </el-select>
        <el-button :loading="downloading === previewResource.resourceId" :disabled="downloading !== null" @click="download(previewResource)">{{ downloadLabel(previewResource) }}</el-button>
        <CaseVotes v-if="previewOwner && isLegacy(previewOwner)" :item="previewOwner" :can-vote="canVote" :busy="voting !== null" @vote="vote" />
        <el-button v-if="previewTools.length" text @click="toolsVisible = !toolsVisible">{{ toolsVisible ? '收起配套材料' : '配套材料' }}</el-button>
      </div>
      <p v-if="previewOwner && voteHints[previewOwner.caseId]" role="status" class="vote-hint">{{ voteHints[previewOwner.caseId] }}</p>
      <p v-if="downloadHint" class="download-hint" :class="{ 'is-error': downloadHintError }" role="status">{{ downloadHint }}</p>
      <div v-if="previewResource" class="preview-layout">
        <div v-if="previewMediaType" class="media-preview">
          <p v-if="mediaError" role="alert">预览加载失败，可以关闭后重新预览或下载原件。</p>
          <!-- 超大视频在线解码会拖慢甚至让页面崩溃，改为提示下载后本地播放。 -->
          <p v-else-if="(previewMediaType === 'video' || previewMediaType === 'audio') && Number(previewResource.bytes) > LARGE_MEDIA_LIMIT" class="muted">
            该媒体约 {{ formatBytes(previewResource.bytes) }}，在线播放容易卡顿或失败，建议下载后用本地播放器观看。
          </p>
          <video v-else-if="previewMediaType === 'video'" :key="previewResource.resourceId" :src="caseMediaUrl(previewResource.resourceId)" controls preload="metadata" @loadeddata="markPreviewReady" @error="mediaError = true" />
          <audio v-else-if="previewMediaType === 'audio'" :key="previewResource.resourceId" :src="caseMediaUrl(previewResource.resourceId)" controls preload="metadata" @loadeddata="markPreviewReady" @error="mediaError = true" />
          <img v-else :key="previewResource.resourceId" :src="caseMediaUrl(previewResource.resourceId)" :alt="previewTitle" @load="markPreviewReady" @error="mediaError = true" />
        </div>
        <CasePreview v-else :key="previewResource.resourceId" :resource-id="Number(previewResource.resourceId)" :title="previewTitle" @ready="markPreviewReady" @pending="markPreviewPending" @failed="markPreviewPending" />
        <aside v-if="toolsVisible && previewTools.length" class="preview-tools">
          <div class="tools-heading"><h3>配套材料</h3><el-button text @click="toolsVisible = false">收起</el-button></div>
          <section v-for="tool in previewTools" :key="tool.resourceId" class="resource">
            <strong>{{ tool.title }}</strong><div class="muted">{{ formatBytes(tool.bytes) }}</div>
            <details v-if="tool.requirements" class="resource-usage">
              <summary>使用说明</summary>
              <p>{{ tool.requirements }}</p>
            </details>
            <el-button type="primary" :loading="downloading === tool.resourceId" :disabled="downloading !== null" @click="download(tool)">{{ downloadLabel(tool) }}</el-button>
          </section>
        </aside>
      </div>
    </el-dialog>
    <el-dialog v-model="invitationVisible" title="你是否推荐这份课例？" width="min(420px, 92vw)" append-to-body :close-on-click-modal="false">
      <template v-if="invitationOwner">
        <p>{{ invitationOwner.title }} · {{ invitationOwner.author }}</p>
        <CaseVotes :item="invitationOwner" :can-vote="canVote" :busy="voting !== null" @vote="vote" />
        <p v-if="voteHints[invitationOwner.caseId]" role="status">{{ voteHints[invitationOwner.caseId] }}</p>
      </template>
    </el-dialog>
  </aside>
</template>

<script setup>
import { computed, ref, watch, onBeforeUnmount } from 'vue'
import { getSectionCases, caseDownloadUrl, caseExtraDownloadUrl, caseMediaUrl, caseHtmlUrl, saveCaseFeedback, requestCaseInvitation } from '@/api/business/textbookCase'
import useUserStore from '@/store/modules/user'
import CasePreview from './CasePreview.vue'
import CaseVotes from './CaseVotes.vue'

const props = defineProps({ bookId: Number, sectionKey: String })
defineEmits(['close'])
const cases = ref([])
const expanded = ref([])
const loading = ref(false)
const error = ref(false)
const downloading = ref(null)
const downloadHint = ref('')
const downloadHintError = ref(false)
const previewVisible = ref(false)
const previewTitle = ref('')
const previewResource = ref(null)
const mediaError = ref(false)
const canVote = computed(() => useUserStore().roles.includes('teacher'))
const voting = ref(null), voteHints = ref({}), toolsVisible = ref(false)
const invitationVisible = ref(false), invitationOwner = ref(null)
const previewOwner = computed(() => cases.value.find(item => item.resources.some(resource => resource.resourceId === previewResource.value?.resourceId)))
let previewActiveMs = 0, lastActiveStamp = 0, invitationPending = false, alive = true
let previewReady = false
const downloadInvitations = new Set()
// 与后端预览上限同一思路：超过 100MiB 的视频不在弹层里在线解码。
const LARGE_MEDIA_LIMIT = 100 * 1024 * 1024
const previewMediaType = computed(() => {
  const extension = previewResource.value?.filename.split('.').pop().toLowerCase()
  if (['mp4', 'webm'].includes(extension)) return 'video'
  if (['mp3', 'wav', 'm4a', 'ogg'].includes(extension)) return 'audio'
  if (['jpg', 'jpeg', 'png', 'webp'].includes(extension)) return 'image'
  return ''
})
const previewTools = computed(() => {
  const owner = cases.value.find(item => item.resources.some(resource => resource.resourceId === previewResource.value?.resourceId))
  return (owner?.resources || []).filter(resource => !Number(resource.previewable)
    && (resource.category === 'ARCHIVE' || /\.(?:zip|rar|7z|tar|tgz|tar\.gz)$/i.test(resource.filename)))
})
const previewSiblings = computed(() => {
  const owner = cases.value.find(item => item.resources.some(resource => resource.resourceId === previewResource.value?.resourceId))
  return (owner?.resources || []).filter(resource => Number(resource.previewable))
})
const categoryNames = { DESIGN: '教学设计', SLIDES: '教学课件', WORKSHEET: '任务单', TOOL: '实验工具', MATERIAL: '配套素材', ARCHIVE: '配套材料' }
let sequence = 0

function formatBytes(value) {
  return Number(value) >= 1048576 ? `${(Number(value) / 1048576).toFixed(1)} MiB` : `${Math.ceil(Number(value) / 1024)} KiB`
}

async function load() {
  const ticket = ++sequence
  cases.value = []
  error.value = false
  loading.value = true
  try {
    const res = await getSectionCases(props.bookId, props.sectionKey)
    if (ticket !== sequence) return
    cases.value = res.data || []
    expanded.value = cases.value.length ? [cases.value[0].caseId] : []
  } catch {
    if (ticket === sequence) error.value = true
  } finally {
    if (ticket === sequence) loading.value = false
  }
}

function clearPreview() {
  previewResource.value = null
}

function preview(resource) {
  if (!resource) return
  pausePreviewTimer()
  previewActiveMs = 0
  lastActiveStamp = 0
  previewReady = false
  mediaError.value = false
  previewResource.value = resource
  previewTitle.value = resource.title
  previewVisible.value = true
}

// 作者网页在新标签打开，由后端 CSP sandbox 隔离，避免在同源顶层执行不可信内容。
function openHtml(resource) {
  const opened = window.open(caseHtmlUrl(resource.resourceId), '_blank', 'noopener,noreferrer')
  if (!opened) showHint('浏览器拦截了新标签，请允许本站弹窗后重试', true)
  else showHint(`已在新标签打开《${resource.title}》`)
}

function isLegacy(item) {
  // 来源缺失是历史脏数据，不能等同于获得过评价的 LEGACY 课例。
  return item?.origin === 'LEGACY'
}

function isPlayableMedia(resource) {
  const extension = resource.filename?.split('.').pop().toLowerCase()
  return ['mp3', 'wav', 'm4a', 'ogg', 'mp4', 'webm', 'jpg', 'jpeg', 'png', 'webp'].includes(extension)
}

function originLabel(item) {
  return isLegacy(item) ? '优秀课例'
    : item.origin === 'RESEARCH_ACTIVITY' ? '教研活动课例'
      : item.origin === 'NINGBO2023' ? '宁波版 2023 参考'
        : item.origin === 'NINGBO2026' ? '宁波版 2026' : '来源未标注'
}

function caseTitle(item) {
  return item.origin === 'NINGBO2023' ? item.title.replace(/\s*[（(]2023版参考[）)]\s*$/, '') : item.title
}

function downloadExtra(resource) {
  return download(resource, true)
}

function downloadLabel(resource) {
  if (resource.category === 'ARCHIVE' || /\.(?:zip|rar|7z|tar|tgz|tar\.gz)$/i.test(resource.filename) || resource.category === 'TOOL') return '下载配套材料包'
  return `下载${categoryNames[resource.category] || resource.title}`
}

function markPreviewReady() {
  previewReady = true
  resumePreviewTimer()
}

function markPreviewPending() {
  pausePreviewTimer()
  previewReady = false
}

function pausePreviewTimer() {
  if (lastActiveStamp) {
    previewActiveMs += performance.now() - lastActiveStamp
    lastActiveStamp = 0
  }
}

function resumePreviewTimer() {
  if (previewReady && document.visibilityState === 'visible' && document.hasFocus()
    && previewVisible.value && previewResource.value && !mediaError.value && !lastActiveStamp) {
    lastActiveStamp = performance.now()
  }
}

function onPreviewClose() {
  pausePreviewTimer()
  // 仅在预览成功且累计活跃停留满30秒时触发自愿抽样邀请
  if (previewReady && !mediaError.value && previewActiveMs >= 30000) invite(previewOwner.value)
  previewReady = false
  previewActiveMs = 0
  lastActiveStamp = 0
}

async function vote(item, value, reason = undefined, complete = () => {}) {
  if (!canVote.value || voting.value !== null) { complete(false, '请等待当前评价保存完成'); return }
  voting.value = item.caseId
  voteHints.value[item.caseId] = ''
  try {
    const res = await saveCaseFeedback(item.caseId, value, reason)
    Object.assign(item, { myReason: null }, res.data)
    voteHints.value[item.caseId] = value === 0 ? '已撤销评价' : (reason ? '已保存评价与理由，可随时修改' : '已保存，可随时修改')
    if (invitationOwner.value?.caseId === item.caseId) invitationVisible.value = false
    complete(true)
  } catch {
    voteHints.value[item.caseId] = '评价保存失败，原选择已保留，请重试'
    complete(false, '评价保存失败，内容已保留，请重试')
  } finally {
    voting.value = null
  }
}

async function invite(item) {
  if (!alive || !item || !isLegacy(item) || !canVote.value || Number(item.myVote) || invitationPending || invitationVisible.value) return
  const ticket = sequence
  invitationPending = true
  try {
    const res = await requestCaseInvitation(item.caseId)
    if (alive && ticket === sequence && !Number(item.myVote) && res.data?.show) {
      invitationOwner.value = item
      invitationVisible.value = true
    }
  } catch { /* 评价邀请失败不干扰备课和下载。 */ }
  finally { invitationPending = false }
}

function flushDownloadInvitations() {
  if (document.visibilityState !== 'visible' || !document.hasFocus()) return
  const item = downloadInvitations.values().next().value
  downloadInvitations.clear()
  if (item) invite(item)
}

async function download(resource, extra = false) {
  if (downloading.value !== null) return
  downloading.value = extra ? `extra-${resource.resourceId}` : resource.resourceId
  try {
    const link = document.createElement('a')
    link.href = extra ? caseExtraDownloadUrl(resource.resourceId) : caseDownloadUrl(resource.resourceId)
    link.download = extra ? resource.extraFilename : resource.filename
    link.target = '_blank'
    link.rel = 'noopener'
    document.body.appendChild(link)
    link.click()
    link.remove()
    showHint(`已发起下载《${resource.title}》，可在浏览器下载记录中查看进度`)
    const owner = cases.value.find(item => item.resources.some(entry => entry.resourceId === resource.resourceId))
    if (isLegacy(owner || {})) downloadInvitations.add(owner)
    await new Promise(resolve => window.setTimeout(resolve, 1000))
    flushDownloadInvitations()
  } catch {
    showHint('下载失败，请重试', true)
  } finally {
    downloading.value = null
  }
}

// 手机上面板是浮层，全局提示会盖住标题，改为面板内行内提示。
let hintTimer = 0
function showHint(text, isError = false) {
  downloadHint.value = text
  downloadHintError.value = isError
  window.clearTimeout(hintTimer)
  hintTimer = window.setTimeout(() => { downloadHint.value = '' }, isError ? 6000 : 4000)
}

function onVisibilityChange() {
  if (document.hidden) {
    pausePreviewTimer()
  } else {
    resumePreviewTimer()
    flushDownloadInvitations()
  }
}

function onWindowBlur() {
  pausePreviewTimer()
}

function onWindowFocus() {
  resumePreviewTimer()
  flushDownloadInvitations()
}

watch(() => [props.bookId, props.sectionKey], () => {
  pausePreviewTimer()
  previewActiveMs = 0
  lastActiveStamp = 0
  previewReady = false
  downloadInvitations.clear()
  invitationVisible.value = false
  previewVisible.value = false
  clearPreview()
  load()
}, { immediate: true })

window.addEventListener('focus', onWindowFocus)
window.addEventListener('blur', onWindowBlur)
document.addEventListener('visibilitychange', onVisibilityChange)

onBeforeUnmount(() => {
  alive = false
  ++sequence
  pausePreviewTimer()
  window.clearTimeout(hintTimer)
  clearPreview()
  window.removeEventListener('focus', onWindowFocus)
  window.removeEventListener('blur', onWindowBlur)
  document.removeEventListener('visibilitychange', onVisibilityChange)
})
</script>

<style scoped>
.case-panel { width: min(var(--case-panel-width, 360px), 100%); flex: 0 0 auto; min-height: 0; min-width: 0; display: flex; flex-direction: column; box-sizing: border-box; background: white; padding: 12px; z-index: 12; }
.case-list { flex: 1; min-height: 0; overflow-y: auto; overscroll-behavior: contain; }
.case-heading { width: 100%; display: flex; flex-direction: column; align-items: flex-start; gap: 8px; padding-right: 6px; }
.case-heading-main { width: 100%; min-width: 0; display: flex; flex-direction: column; align-items: flex-start; }
.case-title-row { width: 100%; display: flex; align-items: flex-start; justify-content: flex-start; gap: 6px; min-width: 0; }
.case-title-row strong { min-width: 0; overflow-wrap: anywhere; text-align: left; }
.case-title-row :deep(.el-tag) { flex: none; margin-left: 0; }
.research-topic { max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.vote-hint { font-size: 12px; color: #606266; margin: 6px 0; }
header { display: flex; justify-content: space-between; align-items: center; }
.download-hint { margin: 6px 0 0; padding: 6px 8px; border-radius: 4px; background: #f0f9eb; color: #529b2e; font-size: 12px; line-height: 1.5; }
.download-hint.is-error { background: #fef0f0; color: #c45656; }
.preview-actions { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; flex: none; }
.preview-actions .el-select { width: min(260px, 100%); }
.preview-actions .el-button { margin-left: 0; }
:deep(.el-collapse-item__header) { min-height: 68px; height: auto; line-height: 1.6; padding: 8px 0; }
.muted { color: #909399; font-size: 12px; line-height: 1.7; }
.resource { padding: 12px 0; border-top: 1px solid #ebeef5; }
.resource p { white-space: pre-line; overflow-wrap: anywhere; }
.resource > .muted { margin: 4px 0 10px; }
.resource-usage { margin: 8px 0 12px; color: #606266; font-size: 13px; line-height: 1.6; }
.resource-usage summary { cursor: pointer; width: fit-content; }
.preview-layout { display: flex; gap: 16px; flex: 1; min-height: 0; min-width: 0; position: relative; }
.media-preview { flex: 1; min-width: 0; min-height: 0; overflow: auto; display: grid; place-items: center; background: #eef1f5; }
.media-preview video, .media-preview img { display: block; max-width: 100%; max-height: 100%; object-fit: contain; margin: auto; }
.media-preview audio { width: min(100%, 640px); margin: auto; }
.preview-tools { width: 280px; flex-shrink: 0; min-height: 0; overflow-y: auto; background: white; padding: 0 10px; box-sizing: border-box; }
.tools-heading { display: flex; align-items: center; justify-content: space-between; }
.tools-heading h3 { font-size: 14px; }
.preview-tools p { white-space: pre-line; overflow-wrap: anywhere; }
@media (max-width: 1000px) { .preview-tools { position: absolute; right: 0; top: 0; bottom: 0; width: min(320px, 100%); z-index: 2; border-left: 1px solid #ddd; box-shadow: -4px 0 16px #0002; } }
.preview-state { min-height: 120px; display: grid; place-content: center; }
@media (max-width: 1200px) { .case-panel { position: absolute; right: 0; top: 0; bottom: 0; width: min(100%, 380px); box-shadow: -4px 0 16px #0002; box-sizing: border-box; } }
@media (max-width: 600px) { .research-topic { white-space: normal; overflow-wrap: anywhere; } }
</style>

<style>
/* 弹窗和阅读画布各自占据剩余空间，外层不再产生第二条纵向滚动条。 */
.case-preview-dialog { height: 94dvh; max-width: 1800px; display: flex; flex-direction: column; margin-bottom: 0; box-sizing: border-box; }
.case-preview-dialog .el-dialog__header { flex: none; padding-bottom: 12px; }
.case-preview-dialog .el-dialog__title { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.case-preview-dialog .el-dialog__body { display: flex; flex-direction: column; flex: 1; min-height: 0; overflow: hidden; }
@media (max-width: 600px) { .case-preview-dialog.el-dialog:not(.is-fullscreen) { width: 100% !important; max-width: none; height: 100dvh; margin: 0 !important; border-radius: 0; padding: 12px; } }
</style>
