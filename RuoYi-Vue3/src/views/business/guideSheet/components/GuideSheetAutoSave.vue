<template>
  <div
    class="autosave-status"
    :class="`is-${state}`"
    role="status"
    aria-live="polite"
    :title="canRetry ? '点击重新保存草稿' : ''"
    @click="canRetry && retry()"
  >
    <el-icon v-if="state === 'saving'" class="is-loading"><Loading /></el-icon>
    <el-icon v-else-if="state === 'saved'"><CircleCheck /></el-icon>
    <el-icon v-else><Clock /></el-icon>
    <span>{{ statusText }}</span>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { CircleCheck, Clock, Loading } from '@element-plus/icons-vue'
import { getGuideSheetDraft, saveGuideSheetDraft } from '@/api/business/guideSheet'
import { createLatestValueQueue } from '../utils/latestValueQueue.js'

const props = defineProps({
  draftKey: { type: String, required: true },
  sheetId: { type: [String, Number], default: null },
  payload: { type: Object, required: true },
  enabled: { type: Boolean, default: true }
})
const emit = defineEmits(['restore', 'status'])

const revision = ref(0)
const state = ref('idle')
const lastSavedAt = ref('')
const hydrated = ref(false)
let debounceTimer = null

const localKey = computed(() => `guide-sheet-draft:${props.draftKey}`)
const statusText = computed(() => {
  if (state.value === 'saving') return '正在保存草稿'
  if (state.value === 'conflict') return '草稿在别处也被修改，已暂存本机（点此重试）'
  if (state.value === 'failed') return '保存失败，草稿已留在本机'
  if (state.value === 'local') return '云端暂不可用，草稿已保存在本机'
  if (lastSavedAt.value) return `草稿已保存 ${formatTime(lastSavedAt.value)}`
  return '草稿将在编辑后自动保存'
})

function formatTime(value) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function readLocalDraft() {
  try {
    return JSON.parse(localStorage.getItem(localKey.value) || 'null')
  } catch {
    return null
  }
}

function writeLocalDraft(record) {
  try {
    localStorage.setItem(localKey.value, JSON.stringify(record))
  } catch {
    // 浏览器存储不可用时仍继续尝试服务端保存。
  }
}

function unwrapDraft(response) {
  const value = response?.data ?? response
  return value?.draftKey ? value : null
}

function chooseNewest(serverDraft, localDraft) {
  if (!serverDraft) return localDraft
  if (!localDraft) return serverDraft
  if (Number(serverDraft.revision || 0) !== Number(localDraft.revision || 0)) {
    return Number(serverDraft.revision || 0) > Number(localDraft.revision || 0) ? serverDraft : localDraft
  }
  return new Date(serverDraft.updateTime || 0) >= new Date(localDraft.updateTime || 0) ? serverDraft : localDraft
}

async function restoreDraft() {
  const localDraft = readLocalDraft()
  let serverDraft = null
  try {
    serverDraft = unwrapDraft(await getGuideSheetDraft(props.draftKey))
  } catch {
    serverDraft = null
  }
  const draft = chooseNewest(serverDraft, localDraft)
  // 本地内容可以更新，但云端 CAS 必须从服务器当前版本继续。
  revision.value = Number(serverDraft?.revision ?? draft?.revision ?? 0)
  lastSavedAt.value = draft?.updateTime || ''
  if (draft) state.value = draft === localDraft && draft !== serverDraft ? 'local' : 'saved'
  if (draft?.content) {
    try {
      emit('restore', JSON.parse(draft.content), draft)
    } catch {
      // 损坏草稿不会覆盖当前编辑内容。
    }
  }
  hydrated.value = true
  emitStatus()
}

function emitStatus() {
  emit('status', {
    revision: revision.value,
    state: state.value,
    updateTime: lastSavedAt.value
  })
}

function scheduleSave() {
  if (!hydrated.value || !props.enabled) return
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    saveQueue.enqueue(JSON.stringify(props.payload))
  }, 1200)
}

function persistLocalSnapshot() {
  if (!hydrated.value || !props.enabled) return
  const record = {
    draftKey: props.draftKey,
    sheetId: props.sheetId,
    revision: revision.value,
    content: JSON.stringify(props.payload),
    updateTime: new Date().toISOString()
  }
  writeLocalDraft(record)
}

async function persistDraftContent(content) {
  state.value = 'saving'
  emitStatus()
  const localRecord = {
    draftKey: props.draftKey,
    sheetId: props.sheetId,
    revision: revision.value,
    content,
    updateTime: new Date().toISOString()
  }
  writeLocalDraft(localRecord)
  try {
    const saved = unwrapDraft(await saveGuideSheetDraft({
      draftKey: props.draftKey,
      sheetId: props.sheetId,
      revision: revision.value,
      content
    }))
    if (saved) {
      revision.value = Number(saved.revision ?? revision.value)
      lastSavedAt.value = saved.updateTime || localRecord.updateTime
      writeLocalDraft({ ...localRecord, ...saved })
      state.value = 'saved'
    } else {
      lastSavedAt.value = localRecord.updateTime
      state.value = 'local'
    }
  } catch (error) {
    // 并发冲突（“草稿已在其他页面更新”）不是网络故障，必须把 CAS 版本重新对齐，
    // 否则之后每次自动保存都会用旧 revision，永远失败。
    if (isRevisionConflict(error)) {
      const resynced = await resyncRevision()
      if (resynced) {
        try {
          const retried = unwrapDraft(await saveGuideSheetDraft({
            draftKey: props.draftKey,
            sheetId: props.sheetId,
            revision: revision.value,
            content
          }))
          if (retried) {
            revision.value = Number(retried.revision ?? revision.value)
            lastSavedAt.value = retried.updateTime || localRecord.updateTime
            writeLocalDraft({ ...localRecord, ...retried })
            state.value = 'saved'
            emitStatus()
            return
          }
        } catch (_retryError) {
          // 仍失败时退回“已存本机 + 冲突”状态，让教师决定。
        }
      }
      // 取不到服务器版本时同样按冲突展示：这仍不是“云端不可用”，不能误导教师。
      state.value = 'conflict'
      lastSavedAt.value = localRecord.updateTime
      emitStatus()
      return
    }
    lastSavedAt.value = localRecord.updateTime
    state.value = 'local'
  }
  emitStatus()
}

function isRevisionConflict(error) {
  const message = String(error?.message || error || '')
  return message.includes('其他页面') || message.includes('草稿已在')
}

/** 冲突时拉一次服务器当前版本，只对齐 revision，不覆盖教师正在编辑的内容。 */
async function resyncRevision() {
  try {
    const serverDraft = unwrapDraft(await getGuideSheetDraft(props.draftKey))
    if (!serverDraft) return false
    revision.value = Number(serverDraft.revision ?? revision.value)
    return true
  } catch (_error) {
    return false
  }
}

const saveQueue = createLatestValueQueue(persistDraftContent)

/** 只有“云端没存住”的状态才允许点状态条重试，避免误触发无谓请求 */
const canRetry = computed(() => ['local', 'conflict', 'failed'].includes(state.value))

function retry() {
  if (!canRetry.value) return
  saveQueue.enqueue(JSON.stringify(props.payload))
}

watch(() => props.payload, scheduleSave, { deep: true })
watch(
  () => props.draftKey,
  () => {
    hydrated.value = false
    if (props.enabled) restoreDraft()
  }
)
watch(
  () => props.enabled,
  enabled => {
    if (enabled && !hydrated.value) restoreDraft()
  }
)

onMounted(() => {
  if (props.enabled) restoreDraft()
  window.addEventListener('beforeunload', persistLocalSnapshot)
})
onBeforeUnmount(() => {
  clearTimeout(debounceTimer)
  persistLocalSnapshot()
  window.removeEventListener('beforeunload', persistLocalSnapshot)
})

defineExpose({
  flush: async () => {
    clearTimeout(debounceTimer)
    saveQueue.enqueue(JSON.stringify(props.payload))
    await saveQueue.flush()
    return revision.value
  },
  getRevision: () => revision.value
})
</script>

<style scoped>
.autosave-status { display: inline-flex; align-items: center; gap: 6px; min-height: 28px; color: #76898f; font-size: 12px; white-space: nowrap; }
.autosave-status.is-saving { color: #9b6c1c; }
.autosave-status.is-saved { color: #26766f; }
.autosave-status.is-local { color: #9b6c1c; cursor: pointer; }
.autosave-status.is-conflict { color: #c45656; cursor: pointer; }
.autosave-status.is-failed { color: #c45656; cursor: pointer; }
</style>
