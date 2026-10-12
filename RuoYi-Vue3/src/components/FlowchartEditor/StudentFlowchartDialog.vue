<template>
  <el-dialog :model-value="modelValue" :title="`画程 · ${question?.questionContent || '流程图操作题'}`"
    width="96%" top="2vh" append-to-body destroy-on-close @close="closeDialog">
    <div v-loading="loading" class="student-flowchart-workspace">
      <div class="workspace-head">
        <el-alert v-if="mobileViewOnly" title="小屏设备仅支持查看，请使用电脑完成拖拽和提交。"
          type="info" :closable="false" show-icon />
        <el-alert v-if="conflict" :title="saveError || '检测到未同步草稿，请选择要继续使用的版本。'"
          type="warning" :closable="false" show-icon />
        <div v-else class="save-state" :class="saveStatus">
          <span class="save-dot"></span>{{ saveLabel }}
        </div>
        <div v-if="latestSubmission" class="submission-state">
          已提交第 {{ latestSubmission.versionNo }} 版
          <span v-if="latestSubmission.suggestedScore != null">· 结构检查建议 {{ latestSubmission.suggestedScore }} 分</span>
        </div>
        <div class="workspace-actions">
          <el-button v-if="conflict" @click="resolveConflict(false)">使用平台版本</el-button>
          <el-button v-if="conflict && !readOnly" @click="resolveConflict(true)">保留本机版本</el-button>
          <span v-if="readOnly" class="footer-tip">已批改，如需修改请联系老师退回</span>
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
        <span class="footer-tip">老师未批改前可继续修改并提交；已批改后需老师退回。</span>
        <el-button @click="closeDialog">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import useUserStore from '@/store/modules/user'
import FlowchartEditor from './FlowchartEditor.vue'
import { getStudentFlowchartWorkspace, saveStudentFlowchartDraft, submitStudentFlowchart } from '@/api/business/flowchart'
import { studentDraftKey, loadLocalFlowchartDraft, markLocalFlowchartDraftSynced, saveLocalFlowchartDraft } from './localDraft'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  lessonId: { type: [Number, String], default: null },
  question: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['update:modelValue', 'submitted'])
const userStore = useUserStore()
const loading = ref(false), submitting = ref(false), documentReady = ref(false)
const documentJson = ref(''), permissionsJson = ref('{}'), revision = ref(0)
const latestSubmission = ref(null), readOnly = ref(false)
const saveStatus = ref('saved'), conflict = ref(false)
const saveError = ref('')
const mobileViewOnly = ref(window.innerWidth < 900)
let session = null
const dialogHeight = computed(() => Math.max(420, Math.min(650, window.innerHeight - 260)))
const canEdit = computed(() => documentReady.value && !loading.value && !submitting.value && !readOnly.value && !mobileViewOnly.value && !conflict.value)
const saveLabel = computed(() => ({ dirty: '正在等待自动保存…', saving: '正在保存…', saved: '草稿已保存到平台', error: '保存失败，内容已暂存在本机' }[saveStatus.value] || '草稿已保存'))

function current(ctx) {
  return session === ctx && props.modelValue && ctx.token === userStore.token
    && ctx.userId === userStore.id && ctx.lessonId === props.lessonId && ctx.questionId === props.question?.questionId
}
function invalidate() {
  if (session) clearTimeout(session.timer)
  session = null
  documentReady.value = false
  submitting.value = false
}
watch(() => [props.modelValue, props.lessonId, props.question?.questionId, userStore.id, userStore.token], () => {
  invalidate()
  if (props.modelValue) loadWorkspace()
}, { immediate: true })
function updateViewportMode() { mobileViewOnly.value = window.innerWidth < 900 }
onMounted(() => window.addEventListener('resize', updateViewportMode))
onBeforeUnmount(() => { invalidate(); window.removeEventListener('resize', updateViewportMode) })

async function loadWorkspace() {
  invalidate()
  const ctx = { userId: userStore.id, token: userStore.token, lessonId: props.lessonId, questionId: props.question?.questionId }
  ctx.key = studentDraftKey(ctx.userId, ctx.lessonId, ctx.questionId)
  if (!ctx.key || !ctx.token) { loading.value = false; return }
  session = ctx
  loading.value = true
  conflict.value = false
  saveError.value = ''
  try {
    const response = await getStudentFlowchartWorkspace(ctx.lessonId, ctx.questionId)
    if (!current(ctx)) return
    const data = response.data || {}
    revision.value = Number(data.draft?.revision || 0)
    latestSubmission.value = data.latestSubmission || null
    readOnly.value = Boolean(data.readOnly)
    // 正式作品与未提交草稿分别保留；只读不能用草稿冒充老师批改的版本。
    documentJson.value = readOnly.value ? data.latestSubmission?.documentJson || '' : data.draft?.documentJson || data.snapshot?.starterJson || ''
    if (readOnly.value && !documentJson.value) throw new Error('正式作品暂不可用，请刷新后重试')
    permissionsJson.value = data.snapshot?.permissionsJson || '{}'
    saveStatus.value = 'saved'
    if (!readOnly.value) {
      const local = loadLocalFlowchartDraft(ctx.key, ctx.userId)
      if (local && !local.synced && local.documentJson !== documentJson.value) {
        if (Number(local.revision) === revision.value) {
          documentJson.value = local.documentJson
          saveStatus.value = 'dirty'
          scheduleSave(ctx)
          ElMessage.info('已恢复本机尚未同步的画程草稿')
        } else { conflict.value = true; saveStatus.value = 'error' }
      } else markLocalFlowchartDraftSynced(ctx.key, revision.value, documentJson.value, ctx.userId)
    }
    documentReady.value = true
  } catch (e) {
    if (current(ctx)) ElMessage.error(e?.message || '画程加载失败，请重新打开')
  } finally { if (current(ctx)) loading.value = false }
}
function handleDocumentChange(value) {
  if (!canEdit.value || !session || !current(session)) return
  documentJson.value = value
  saveStatus.value = 'dirty'
  saveLocalFlowchartDraft(session.key, value, revision.value, false, session.userId)
  scheduleSave(session)
}
function scheduleSave(ctx, delay = 2000) {
  clearTimeout(ctx.timer)
  ctx.timer = setTimeout(() => { if (current(ctx)) saveDraftNow(ctx) }, delay)
}
async function saveDraftNow(ctx = session) {
  if (!ctx || !current(ctx) || readOnly.value || conflict.value) return false
  if (saveStatus.value === 'saved') return true
  if (ctx.saving) { ctx.again = true; return false }
  ctx.saving = true
  saveStatus.value = 'saving'
  const savingDocument = documentJson.value
  try {
    const response = await saveStudentFlowchartDraft({ lessonId: ctx.lessonId, questionId: ctx.questionId, expectedRevision: revision.value, documentJson: savingDocument })
    if (!current(ctx)) return false
    revision.value = Number(response.data?.revision || revision.value + 1)
    if (documentJson.value === savingDocument) {
      saveStatus.value = 'saved'
      markLocalFlowchartDraftSynced(ctx.key, revision.value, savingDocument, ctx.userId)
    } else {
      saveStatus.value = 'dirty'
      saveLocalFlowchartDraft(ctx.key, documentJson.value, revision.value, false, ctx.userId)
      ctx.again = true
    }
    return true
  } catch (error) {
    if (current(ctx)) {
      saveStatus.value = 'error'; conflict.value = true
      saveError.value = error?.message || '保存失败，内容已保留在本机，请重新核对平台版本。'
      saveLocalFlowchartDraft(ctx.key, documentJson.value, revision.value, false, ctx.userId)
    }
    return false
  } finally {
    ctx.saving = false
    if (current(ctx) && ctx.again && !conflict.value) { ctx.again = false; scheduleSave(ctx, 300) }
  }
}
async function resolveConflict(keepLocal) {
  const ctx = session
  if (!ctx || !current(ctx) || loading.value) return
  const local = loadLocalFlowchartDraft(ctx.key, ctx.userId)
  const localDocument = local?.documentJson || documentJson.value
  try {
    await ElMessageBox.confirm(keepLocal ? '将以本机草稿继续编辑，并在重新核对平台修订号后保存。' : '将使用平台最新版本，本机未同步修改会被替换。', '选择草稿版本', { type: 'warning' })
    if (!current(ctx)) return
    loading.value = true
    const response = await getStudentFlowchartWorkspace(ctx.lessonId, ctx.questionId)
    if (!current(ctx)) return
    const data = response.data || {}
    revision.value = Number(data.draft?.revision || 0)
    readOnly.value = Boolean(data.readOnly)
    latestSubmission.value = data.latestSubmission || null
    permissionsJson.value = data.snapshot?.permissionsJson || '{}'
    documentJson.value = readOnly.value ? data.latestSubmission?.documentJson || ''
      : keepLocal ? localDocument : data.draft?.documentJson || data.snapshot?.starterJson || ''
    conflict.value = false
    saveError.value = ''
    saveStatus.value = keepLocal && !readOnly.value ? 'dirty' : 'saved'
    if (saveStatus.value === 'dirty') {
      saveLocalFlowchartDraft(ctx.key, documentJson.value, revision.value, false, ctx.userId)
      scheduleSave(ctx)
    } else if (!readOnly.value) markLocalFlowchartDraftSynced(ctx.key, revision.value, documentJson.value, ctx.userId)
  } catch (error) {
    if (current(ctx) && error !== 'cancel' && error !== 'close') saveError.value = error?.message || '平台版本读取失败，请重试'
  } finally { if (current(ctx)) loading.value = false }
}
async function submit() {
  const ctx = session
  if (!ctx || !current(ctx) || !canEdit.value) return
  clearTimeout(ctx.timer)
  if (saveStatus.value !== 'saved' && !(await saveDraftNow(ctx))) return
  // 等待保存期间仍可能编辑；有新改动时先完成下一次保存，不能提交上一版画布。
  if (!current(ctx) || saveStatus.value !== 'saved') return
  submitting.value = true
  try {
    await ElMessageBox.confirm('确认提交当前流程图吗？老师未批改前仍可修改并再次提交。', '提交画程作品', { type: 'warning', confirmButtonText: '确认提交', cancelButtonText: '再检查一下' })
    if (!current(ctx)) return
    await submitStudentFlowchart({ lessonId: ctx.lessonId, questionId: ctx.questionId, expectedRevision: revision.value })
    if (!current(ctx)) return
    ElMessage.success('画程作品提交成功，等待老师批改')
    emit('submitted')
    await loadWorkspace()
  } catch (e) {
    if (current(ctx) && e !== 'cancel' && e !== 'close') ElMessage.error(e?.message || '提交失败，请重试')
  } finally { if (current(ctx)) submitting.value = false }
}
function closeDialog() {
  const ctx = session
  if (ctx && current(ctx) && !readOnly.value && saveStatus.value === 'dirty') saveDraftNow(ctx)
  invalidate()
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
