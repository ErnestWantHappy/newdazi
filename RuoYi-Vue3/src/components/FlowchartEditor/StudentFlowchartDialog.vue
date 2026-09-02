<template>
  <el-dialog :model-value="modelValue" :title="`画程 · ${question?.questionContent || '流程图操作题'}`"
    width="96%" top="2vh" append-to-body destroy-on-close @close="closeDialog">
    <div v-loading="loading" class="student-flowchart-workspace">
      <div class="workspace-head">
        <el-alert v-if="mobileViewOnly" title="小屏设备仅支持查看，请使用电脑完成拖拽和提交。"
          type="info" :closable="false" show-icon />
        <el-alert v-if="conflict" title="检测到另一页面或本机未同步草稿，当前内容已保留在本机。请刷新页面后再决定是否继续。"
          type="warning" :closable="false" show-icon />
        <div v-else class="save-state" :class="saveStatus">
          <span class="save-dot"></span>{{ saveLabel }}
        </div>
        <div v-if="latestSubmission" class="submission-state">
          已提交第 {{ latestSubmission.versionNo }} 版
          <span v-if="latestSubmission.suggestedScore != null">· 结构检查建议 {{ latestSubmission.suggestedScore }} 分</span>
        </div>
        <div class="workspace-actions">
          <el-button v-if="readOnly && !mobileViewOnly" type="warning" plain :loading="reopening" @click="reopen">从此版本补交</el-button>
          <el-button v-if="canEdit" type="primary" :loading="submitting" :disabled="conflict" @click="submit">完成并提交</el-button>
        </div>
      </div>

      <flowchart-editor v-if="documentReady" v-model="documentJson"
        :mode="canEdit ? 'STUDENT' : 'READONLY'" :permissions="permissionsJson" :height="dialogHeight"
        @change="handleDocumentChange">
        <template #status><span v-if="canEdit">停止操作 2 秒后自动保存</span></template>
      </flowchart-editor>
    </div>
    <template #footer>
      <div class="dialog-footer flowchart-footer">
        <span class="footer-tip">正式提交后作品会锁定；需要补交时可从最新版本重新开始。</span>
        <el-button @click="closeDialog">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import FlowchartEditor from './FlowchartEditor.vue'
import {
  getStudentFlowchartWorkspace, reopenStudentFlowchart,
  saveStudentFlowchartDraft, submitStudentFlowchart
} from '@/api/business/flowchart'
import {
  loadLocalFlowchartDraft, markLocalFlowchartDraftSynced, saveLocalFlowchartDraft
} from './localDraft'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  lessonId: { type: [Number, String], default: null },
  question: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['update:modelValue', 'submitted'])
const loading = ref(false)
const submitting = ref(false)
const reopening = ref(false)
const documentReady = ref(false)
const documentJson = ref('')
const permissionsJson = ref('{}')
const revision = ref(0)
const latestSubmission = ref(null)
const readOnly = ref(false)
const saveStatus = ref('saved')
const conflict = ref(false)
const mobileViewOnly = ref(window.innerWidth < 900)
let saveTimer = null
let saving = false
let saveAgain = false

const draftKey = computed(() => `${props.lessonId}:${props.question?.questionId || ''}`)
const dialogHeight = computed(() => Math.max(420, Math.min(650, window.innerHeight - 260)))
const canEdit = computed(() => !readOnly.value && !mobileViewOnly.value)
const saveLabel = computed(() => ({
  dirty: '正在等待自动保存…', saving: '正在保存…', saved: '草稿已保存到平台', error: '保存失败，内容已暂存在本机'
}[saveStatus.value] || '草稿已保存'))

watch(() => props.modelValue, open => { if (open) loadWorkspace() })

function updateViewportMode() { mobileViewOnly.value = window.innerWidth < 900 }
onMounted(() => window.addEventListener('resize', updateViewportMode))
onBeforeUnmount(() => {
  if (saveTimer) clearTimeout(saveTimer)
  window.removeEventListener('resize', updateViewportMode)
})

async function loadWorkspace() {
  if (!props.lessonId || !props.question?.questionId) return
  loading.value = true
  documentReady.value = false
  conflict.value = false
  try {
    const response = await getStudentFlowchartWorkspace(props.lessonId, props.question.questionId)
    const data = response.data || {}
    revision.value = Number(data.draft?.revision || 0)
    documentJson.value = data.draft?.documentJson || data.snapshot?.starterJson || ''
    permissionsJson.value = data.snapshot?.permissionsJson || '{}'
    latestSubmission.value = data.latestSubmission || null
    readOnly.value = Boolean(data.readOnly)
    const local = loadLocalFlowchartDraft(draftKey.value)
    if (!readOnly.value && local && !local.synced && local.documentJson !== documentJson.value) {
      if (Number(local.revision) === revision.value) {
        documentJson.value = local.documentJson
        saveStatus.value = 'dirty'
        scheduleSave()
        ElMessage.info('已恢复本机尚未同步的画程草稿')
      } else {
        conflict.value = true
        saveStatus.value = 'error'
      }
    } else {
      saveStatus.value = 'saved'
      markLocalFlowchartDraftSynced(draftKey.value, revision.value, documentJson.value)
    }
    documentReady.value = true
  } finally {
    loading.value = false
  }
}

function handleDocumentChange(value) {
  if (!canEdit.value) return
  documentJson.value = value
  saveStatus.value = 'dirty'
  saveLocalFlowchartDraft(draftKey.value, value, revision.value, false)
  scheduleSave()
}

function scheduleSave(delay = 2000) {
  if (saveTimer) clearTimeout(saveTimer)
  saveTimer = setTimeout(() => saveDraftNow(), delay)
}

async function saveDraftNow() {
  if (readOnly.value || conflict.value || saveStatus.value === 'saved') return true
  if (saving) { saveAgain = true; return false }
  saving = true
  saveStatus.value = 'saving'
  const savingDocument = documentJson.value
  try {
    const response = await saveStudentFlowchartDraft({
      lessonId: props.lessonId,
      questionId: props.question.questionId,
      expectedRevision: revision.value,
      documentJson: savingDocument
    })
    revision.value = Number(response.data?.revision || revision.value + 1)
    if (documentJson.value === savingDocument) {
      saveStatus.value = 'saved'
      markLocalFlowchartDraftSynced(draftKey.value, revision.value, savingDocument)
    } else {
      saveStatus.value = 'dirty'
      saveAgain = true
    }
    return true
  } catch (_) {
    saveStatus.value = 'error'
    conflict.value = true
    saveLocalFlowchartDraft(draftKey.value, documentJson.value, revision.value, false)
    return false
  } finally {
    saving = false
    if (saveAgain && !conflict.value) { saveAgain = false; scheduleSave(300) }
  }
}

async function submit() {
  if (saveTimer) clearTimeout(saveTimer)
  if (saveStatus.value !== 'saved' && !(await saveDraftNow())) return
  await ElMessageBox.confirm('确认已经完成流程图并正式提交吗？提交后作品将锁定。', '提交画程作品', {
    type: 'warning', confirmButtonText: '确认提交', cancelButtonText: '再检查一下'
  })
  submitting.value = true
  try {
    const response = await submitStudentFlowchart({
      lessonId: props.lessonId,
      questionId: props.question.questionId,
      expectedRevision: revision.value
    })
    latestSubmission.value = response.data
    readOnly.value = true
    ElMessage.success('画程作品提交成功，等待老师批改')
    emit('submitted')
  } finally { submitting.value = false }
}

async function reopen() {
  await ElMessageBox.confirm('将从最新提交版本建立一份新草稿，完成后会作为新版本提交。', '开始补交', {
    type: 'warning', confirmButtonText: '开始补交', cancelButtonText: '取消'
  })
  reopening.value = true
  try {
    const response = await reopenStudentFlowchart({ lessonId: props.lessonId, questionId: props.question.questionId })
    documentJson.value = response.data?.documentJson || documentJson.value
    revision.value = Number(response.data?.revision || 0)
    readOnly.value = false
    conflict.value = false
    saveStatus.value = 'saved'
    markLocalFlowchartDraftSynced(draftKey.value, revision.value, documentJson.value)
  } finally { reopening.value = false }
}

function closeDialog() {
  if (saveTimer) clearTimeout(saveTimer)
  if (!readOnly.value && saveStatus.value === 'dirty') saveDraftNow()
  emit('update:modelValue', false)
}
</script>

<style scoped lang="scss">
.student-flowchart-workspace { min-height: 520px; }
.workspace-head { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 10px; }
.workspace-actions { display: flex; flex: 0 0 auto; gap: 8px; margin-left: auto; }
.save-state { display: inline-flex; align-items: center; gap: 7px; color: #5b7083; font-size: 13px; }
.save-dot { width: 8px; height: 8px; border-radius: 50%; background: #67c23a; }
.save-state.dirty .save-dot, .save-state.saving .save-dot { background: #e6a23c; }
.save-state.error .save-dot { background: #f56c6c; }
.submission-state { color: #409eff; font-size: 13px; }
.flowchart-footer { display: flex; align-items: center; justify-content: space-between; width: 100%; }
.footer-tip { color: #8492a6; font-size: 13px; }
@media (max-width: 700px) {
  .workspace-head { align-items: flex-start; flex-wrap: wrap; }
  .workspace-actions { width: 100%; margin-left: 0; }
}
</style>
