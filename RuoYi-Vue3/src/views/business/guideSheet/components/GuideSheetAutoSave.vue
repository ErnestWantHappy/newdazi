<template>
  <div class="autosave-status" :class="`is-${state}`" role="status" aria-live="polite">
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
  } catch {
    lastSavedAt.value = localRecord.updateTime
    state.value = 'local'
  }
  emitStatus()
}

const saveQueue = createLatestValueQueue(persistDraftContent)

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
.autosave-status.is-local { color: #9b6c1c; }
</style>
