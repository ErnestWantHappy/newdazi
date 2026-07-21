<template>
  <main class="beginner-workbench">
    <header class="workbench-header">
      <div class="header-left">
        <el-button text :icon="ArrowLeft" aria-label="返回模板库" @click="$emit('back')" />
        <div>
          <strong>{{ draftMeta.sheetTitle || '新建电子导学单' }}</strong>
          <span>新手模式 · 三步完成课堂模板</span>
        </div>
      </div>
      <div class="header-actions">
        <GuideSheetAutoSave
          ref="autoSaveRef"
          :draft-key="draftKey"
          :sheet-id="draftMeta.sheetId"
          :payload="draftPayload"
          :enabled="!loading && Boolean(baseFormJson)"
          @restore="restoreDraft"
          @status="draftStatus = $event"
        />
        <el-button :icon="Setting" @click="$emit('advanced')">切换高级模式</el-button>
      </div>
    </header>

    <section class="step-band">
      <el-steps :active="step" finish-status="success" align-center>
        <el-step title="基本信息" description="标题、年级、学期、第几课" />
        <el-step title="教学内容" description="添加常用模块" />
        <el-step title="预览保存" description="确认后保存" />
      </el-steps>
    </section>

    <section v-loading="loading" class="wizard-body">
      <el-alert
        v-if="invalidRaw"
        type="warning"
        :closable="false"
        show-icon
        title="原模板内容暂时无法读取"
        description="原始内容仍会保留，创建可编辑副本后再保存即可继续使用。"
        class="recovery-alert"
      >
        <template #default>
          <el-button type="warning" plain @click="recoverDamagedForm">创建可编辑副本</el-button>
        </template>
      </el-alert>

      <div v-if="step === 0" class="basic-step">
        <div class="step-introduction">
          <span class="step-number">01</span>
          <div>
            <h2>填写基本信息</h2>
            <p>标题、年级、学期和第几课，方便以后查找和选用。</p>
          </div>
        </div>
        <el-form ref="metadataFormRef" :model="draftMeta" :rules="metadataRules" label-position="top" class="metadata-form">
          <el-form-item label="模板标题" prop="sheetTitle" class="span-two">
            <el-input v-model="draftMeta.sheetTitle" maxlength="100" show-word-limit placeholder="例如：第 6 课 网络安全课堂任务单" />
          </el-form-item>
          <el-form-item label="年级" prop="grade">
            <el-select v-model="draftMeta.grade" placeholder="请选择年级" style="width:100%">
              <el-option v-for="item in gradeOptions" :key="item.value" :label="item.label" :value="Number(item.value)" />
            </el-select>
          </el-form-item>
          <el-form-item label="学期" prop="semester">
            <el-select v-model="draftMeta.semester" placeholder="请选择学期" style="width:100%">
              <el-option v-for="item in semesterOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="第几课" prop="lessonNum">
            <el-input-number v-model="draftMeta.lessonNum" :min="1" :max="30" controls-position="right" style="width:100%" />
          </el-form-item>
          <el-form-item label="可见范围" prop="isPublic" class="span-two">
            <el-radio-group v-model="draftMeta.isPublic">
              <el-radio-button value="N">我的私有</el-radio-button>
              <el-radio-button value="Y">公共导学单</el-radio-button>
            </el-radio-group>
            <p class="field-help">公共导学单可供同县教师选用或复制，不会改动你的原模板。</p>
          </el-form-item>
        </el-form>
      </div>

      <div v-else-if="step === 1" class="content-step">
        <GuideSheetPresetSelector
          :model-value="selectedPreset"
          :show-reset="Boolean(selectedPreset && selectedPreset !== 'blank' && document.items.length)"
          @select="selectPreset"
        />
        <el-alert
          v-if="!aiAvailable"
          type="info"
          :closable="false"
          show-icon
          title="AI 服务暂不可用"
          description="不影响添加、编辑和保存教学内容。"
          class="ai-alert"
        />
        <div v-else class="ai-tools">
          <span><el-icon><MagicStick /></el-icon> 智能内容建议</span>
          <el-button plain size="small" @click="requestAi('generateObjectives')">生成学习目标</el-button>
          <el-button plain size="small" @click="requestAi('preClassCheck')">生成课前检测</el-button>
          <el-button plain size="small" @click="requestAi('reflection')">生成反思问题</el-button>
        </div>

        <div class="editor-grid">
          <BeginnerComponentLibrary @add="addModule" />
          <section class="module-canvas">
            <div class="canvas-toolbar">
              <div>
                <strong>课堂内容</strong>
                <span>{{ document.items.length }} 个模块<span v-if="document.advancedCount">，含 {{ document.advancedCount }} 个高级组件</span></span>
              </div>
              <div>
                <el-tooltip content="撤销" placement="top"><el-button :icon="Back" circle :disabled="!canUndo" @click="undo" /></el-tooltip>
                <el-tooltip content="重做" placement="top"><el-button :icon="Right" circle :disabled="!canRedo" @click="redo" /></el-tooltip>
              </div>
            </div>
            <draggable
              v-if="document.items.length"
              v-model="document.items"
              item-key="id"
              handle=".drag-handle"
              class="module-list"
              ghost-class="module-ghost"
              @end="commitDocument"
            >
              <template #item="{ element, index }">
                <article
                  :class="['module-card', { selected: selectedId === element.id, advanced: element.advanced }]"
                  @click="selectedId = element.id"
                >
                  <button type="button" class="drag-handle" aria-label="拖动排序"><el-icon><Rank /></el-icon></button>
                  <span class="module-order">{{ String(index + 1).padStart(2, '0') }}</span>
                  <div class="module-summary">
                    <strong>{{ element.title }}</strong>
                    <small>{{ element.advanced ? '高级组件，内容已完整保留' : moduleLabel(element.moduleType) }}</small>
                  </div>
                  <el-tag v-if="element.required && !element.advanced" size="small" type="danger" effect="plain">必答</el-tag>
                  <el-button text :icon="Delete" aria-label="删除模块" @click.stop="confirmDelete(element)" />
                </article>
              </template>
            </draggable>
            <el-empty v-else description="从左侧添加模块，或在上方选择课堂结构" :image-size="72" />
          </section>
          <BeginnerPropertyPanel
            :item="selectedItem"
            :ai-available="aiAvailable"
            @update="updateSelectedItem"
            @delete="selectedItem && confirmDelete(selectedItem)"
            @polish="requestAi('polish', $event)"
          />
        </div>
      </div>

      <div v-else class="preview-step">
        <div class="preview-main">
          <GuideSheetPreview :form-json="baseFormJson" />
        </div>
        <aside class="structure-summary">
          <div class="summary-heading">
            <span class="step-number">03</span>
            <div><strong>模板结构概要</strong><small>保存前最后确认</small></div>
          </div>
          <dl>
            <div><dt>模板标题</dt><dd>{{ draftMeta.sheetTitle || '未填写' }}</dd></div>
            <div><dt>年级学期</dt><dd>{{ gradeSemesterLabel }}</dd></div>
            <div><dt>第几课</dt><dd>第 {{ draftMeta.lessonNum || '-' }} 课</dd></div>
            <div><dt>教学模块</dt><dd>{{ beginnerItemCount }} 个</dd></div>
            <div v-if="document.advancedCount"><dt>高级组件</dt><dd>{{ document.advancedCount }} 个，已保留</dd></div>
          </dl>
          <div class="outline-list">
            <span v-for="(item, index) in document.items" :key="item.id">
              <em>{{ index + 1 }}</em>{{ item.title }}
            </span>
          </div>
          <el-button type="primary" :loading="saving" :icon="Check" class="save-primary" @click="requestSave('library')">
            保存并返回模板库
          </el-button>
          <el-button :loading="saving" :icon="Promotion" class="save-secondary" @click="requestSave('lesson')">
            保存并前往课程使用
          </el-button>
        </aside>
      </div>
    </section>

    <footer class="wizard-footer">
      <el-button v-if="step > 0" :icon="ArrowLeft" @click="step -= 1">上一步</el-button>
      <span v-else />
      <el-button v-if="step < 2" type="primary" @click="goNext">
        {{ step === 0 ? '选择教学内容' : '预览并保存' }}<el-icon class="el-icon--right"><ArrowRight /></el-icon>
      </el-button>
    </footer>

    <el-dialog v-model="aiDialogVisible" title="智能内容建议" width="min(620px, 92vw)" append-to-body destroy-on-close>
      <el-alert type="info" :closable="false" title="建议内容只会进入当前草稿，由你确认后才会应用。" />
      <el-input v-model="aiSuggestion" type="textarea" :rows="10" maxlength="3000" show-word-limit class="suggestion-input" />
      <template #footer>
        <el-button @click="aiDialogVisible = false">暂不使用</el-button>
        <el-button type="primary" @click="applyAiSuggestion">应用到草稿</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup>
import { computed, nextTick, reactive, ref, watch } from 'vue'
import draggable from 'vuedraggable'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  ArrowRight,
  Back,
  Check,
  Delete,
  MagicStick,
  Promotion,
  Rank,
  Right,
  Setting
} from '@element-plus/icons-vue'
import { generateGuideSheetContent } from '@/api/business/guideSheet'
import BeginnerComponentLibrary from './BeginnerComponentLibrary.vue'
import BeginnerPropertyPanel from './BeginnerPropertyPanel.vue'
import GuideSheetAutoSave from './GuideSheetAutoSave.vue'
import GuideSheetPresetSelector from './GuideSheetPresetSelector.vue'
import GuideSheetPreview from './GuideSheetPreview.vue'
import {
  appendBeginnerModule,
  createBeginnerItemsFromLines,
  readBeginnerDocument,
  writeBeginnerDocument
} from '../utils/formJsonBridge.js'
import {
  createBeginnerFormJson,
  DEFAULT_STRUCTURE_PRESET_ID,
  getModuleDefinition
} from '../utils/presetFactories.js'

const props = defineProps({
  form: { type: Object, required: true },
  formJson: { type: Object, default: null },
  invalidRaw: { type: String, default: '' },
  gradeOptions: { type: Array, default: () => [] },
  semesterOptions: { type: Array, default: () => [] },
  saving: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  aiAvailable: { type: Boolean, default: false },
  draftKey: { type: String, required: true }
})
const emit = defineEmits(['update-metadata', 'update-form-json', 'save', 'advanced', 'back'])

const step = ref(0)
const metadataFormRef = ref(null)
const autoSaveRef = ref(null)
const draftMeta = reactive({
  sheetId: null,
  sheetTitle: '',
  grade: null,
  semester: null,
  lessonNum: 1,
  isPublic: 'N',
  teachingTopic: '',
  estimatedMinutes: 20
})
const baseFormJson = ref(null)
const document = reactive({ items: [], advancedCount: 0 })
const selectedId = ref('')
const selectedPreset = ref(DEFAULT_STRUCTURE_PRESET_ID)
const history = ref([])
const historyIndex = ref(-1)
const syncingMetadata = ref(false)
const draftStatus = ref({ revision: 0 })
const aiDialogVisible = ref(false)
const aiSuggestion = ref('')
const aiAction = ref('')
const aiTargetId = ref('')
let localFormJsonEmissionPending = false

const metadataRules = {
  sheetTitle: [{ required: true, message: '请输入模板标题', trigger: 'blur' }],
  grade: [{ required: true, message: '请选择年级', trigger: 'change' }],
  semester: [{ required: true, message: '请选择学期', trigger: 'change' }],
  lessonNum: [{ required: true, message: '请设置课次', trigger: 'change' }]
}
const selectedItem = computed(() => document.items.find(item => item.id === selectedId.value) || null)
const beginnerItemCount = computed(() => document.items.filter(item => !item.advanced).length)
const gradeSemesterLabel = computed(() => {
  const grade = props.gradeOptions.find(item => String(item.value) === String(draftMeta.grade))?.label || draftMeta.grade || '-'
  const semester = props.semesterOptions.find(item => String(item.value) === String(draftMeta.semester))?.label || draftMeta.semester || '-'
  return `${grade} · ${semester}`
})
const canUndo = computed(() => historyIndex.value > 0)
const canRedo = computed(() => historyIndex.value >= 0 && historyIndex.value < history.value.length - 1)
const draftPayload = computed(() => ({
  metadata: { ...draftMeta },
  formJson: baseFormJson.value
}))

watch(
  () => props.form,
  value => {
    syncingMetadata.value = true
    Object.assign(draftMeta, {
      sheetId: value.sheetId || null,
      sheetTitle: value.sheetTitle || '',
      grade: value.grade ?? null,
      semester: value.semester ?? null,
      lessonNum: value.lessonNum || 1,
      isPublic: value.isPublic || 'N'
    })
    const config = props.formJson?.formConfig || {}
    draftMeta.teachingTopic = value.teachingTopic || config.beginnerTeachingTopic || ''
    draftMeta.estimatedMinutes = Number(value.estimatedMinutes || config.beginnerEstimatedMinutes || 20)
    nextTick(() => { syncingMetadata.value = false })
  },
  { immediate: true, deep: true }
)

watch(
  draftMeta,
  value => {
    if (syncingMetadata.value) return
    emit('update-metadata', { ...value })
    if (baseFormJson.value) {
      baseFormJson.value.formConfig ||= {}
      baseFormJson.value.formConfig.beginnerTeachingTopic = value.teachingTopic || ''
      baseFormJson.value.formConfig.beginnerEstimatedMinutes = Number(value.estimatedMinutes || 0)
      emitFormJson()
    }
  },
  { deep: true }
)

watch(
  () => props.formJson,
  value => {
    const preserveHistory = localFormJsonEmissionPending
    localFormJsonEmissionPending = false
    if (!value) {
      if (!props.invalidRaw && !baseFormJson.value) {
        // 默认空白画布，由教师从模块库添加内容
        setFormJson(createBeginnerFormJson(DEFAULT_STRUCTURE_PRESET_ID, {
          topic: draftMeta.teachingTopic,
          estimatedMinutes: draftMeta.estimatedMinutes
        }), true)
        selectedPreset.value = DEFAULT_STRUCTURE_PRESET_ID
      }
      return
    }
    const incoming = JSON.stringify(value)
    if (incoming !== JSON.stringify(baseFormJson.value)) setFormJson(value, !preserveHistory)
  },
  { immediate: true, deep: true }
)

watch(
  () => props.invalidRaw,
  value => {
    if (!value) return
    baseFormJson.value = null
    document.items = []
    document.advancedCount = 0
    selectedId.value = ''
    history.value = []
    historyIndex.value = -1
  },
  { immediate: true }
)

function setFormJson(value, resetHistory = false) {
  baseFormJson.value = JSON.parse(JSON.stringify(value))
  const nextDocument = readBeginnerDocument(baseFormJson.value)
  document.items = nextDocument.items
  document.advancedCount = nextDocument.advancedCount
  if (!document.items.some(item => item.id === selectedId.value)) selectedId.value = document.items[0]?.id || ''
  if (resetHistory) {
    history.value = [JSON.stringify(baseFormJson.value)]
    historyIndex.value = 0
  }
}

function pushHistory() {
  const snapshot = JSON.stringify(baseFormJson.value)
  if (history.value[historyIndex.value] === snapshot) return
  history.value = history.value.slice(0, historyIndex.value + 1)
  history.value.push(snapshot)
  if (history.value.length > 30) history.value.shift()
  historyIndex.value = history.value.length - 1
}

function emitFormJson() {
  document.advancedCount = document.items.filter(item => item.advanced).length
  localFormJsonEmissionPending = true
  emit('update-form-json', JSON.parse(JSON.stringify(baseFormJson.value)))
  nextTick(() => { localFormJsonEmissionPending = false })
}

function commitDocument() {
  baseFormJson.value = writeBeginnerDocument(baseFormJson.value, document)
  pushHistory()
  emitFormJson()
}

function moduleLabel(moduleType) {
  return getModuleDefinition(moduleType)?.label || '教学模块'
}

async function selectPreset(presetId) {
  const nextId = presetId || DEFAULT_STRUCTURE_PRESET_ID
  if (document.items.length) {
    try {
      await ElMessageBox.confirm(
        nextId === DEFAULT_STRUCTURE_PRESET_ID
          ? '重置为空白画布将清空当前草稿中的教学模块。已保存的正式模板不会受影响。'
          : '更换结构会替换当前草稿中的教学模块。已保存的正式模板不会受影响。',
        nextId === DEFAULT_STRUCTURE_PRESET_ID ? '重置为空白画布' : '更换课堂结构',
        { type: 'warning', confirmButtonText: '确认', cancelButtonText: '保留当前内容' }
      )
    } catch {
      return
    }
  }
  selectedPreset.value = nextId
  setFormJson(createBeginnerFormJson(nextId, {
    topic: draftMeta.teachingTopic,
    estimatedMinutes: draftMeta.estimatedMinutes
  }))
  pushHistory()
  emitFormJson()
}

function addModule(moduleType) {
  const result = appendBeginnerModule(baseFormJson.value, document, moduleType)
  baseFormJson.value = result.formJson
  document.items = result.document.items
  document.advancedCount = result.document.advancedCount
  selectedId.value = document.items[document.items.length - 1]?.id || ''
  // 手工增删模块后仍视为 blank 主路径，不再绑定历史结构 id
  selectedPreset.value = DEFAULT_STRUCTURE_PRESET_ID
  pushHistory()
  emitFormJson()
}

function updateSelectedItem(value) {
  const index = document.items.findIndex(item => item.id === selectedId.value)
  if (index < 0) return
  document.items[index] = value
  commitDocument()
}

async function confirmDelete(item) {
  try {
    await ElMessageBox.confirm(
      `确定删除“${item.title}”吗？删除后可使用撤销恢复。`,
      '删除教学模块',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  const index = document.items.findIndex(current => current.id === item.id)
  if (index < 0) return
  document.items.splice(index, 1)
  selectedId.value = document.items[Math.min(index, document.items.length - 1)]?.id || ''
  commitDocument()
}

function restoreHistory(index) {
  const value = history.value[index]
  if (!value) return
  historyIndex.value = index
  setFormJson(JSON.parse(value))
  emitFormJson()
}

function undo() { if (canUndo.value) restoreHistory(historyIndex.value - 1) }
function redo() { if (canRedo.value) restoreHistory(historyIndex.value + 1) }

async function goNext() {
  if (step.value === 0) {
    const valid = await metadataFormRef.value?.validate().catch(() => false)
    if (!valid) return
  }
  if (step.value === 1 && !document.items.length) {
    ElMessage.warning('请先从左侧添加至少一个教学模块')
    return
  }
  step.value += 1
}

function recoverDamagedForm() {
  selectedPreset.value = DEFAULT_STRUCTURE_PRESET_ID
  setFormJson(createBeginnerFormJson(DEFAULT_STRUCTURE_PRESET_ID, {
    topic: draftMeta.teachingTopic,
    estimatedMinutes: draftMeta.estimatedMinutes
  }), true)
  emitFormJson()
  ElMessage.success('已创建可编辑副本，原始内容会保留到你正式保存前')
}

async function requestSave(destination) {
  const revision = await autoSaveRef.value?.flush()
  emit('save', destination, revision || draftStatus.value.revision || 0)
}

async function requestAi(action, target = null) {
  if (!props.aiAvailable) {
    ElMessage.info('AI 服务暂不可用，普通编辑和保存不受影响')
    return
  }
  try {
    const response = await generateGuideSheetContent({
      action,
      grade: draftMeta.grade,
      lessonNum: draftMeta.lessonNum,
      topic: draftMeta.teachingTopic,
      input: target?.title || target?.content || ''
    })
    const result = response?.data ?? response
    if (!result?.available || !result?.content) {
      ElMessage.info(result?.message || 'AI 服务暂不可用')
      return
    }
    aiAction.value = action
    aiTargetId.value = target?.id || ''
    aiSuggestion.value = result.content
    aiDialogVisible.value = true
  } catch {
    ElMessage.info('AI 服务暂不可用，普通编辑和保存不受影响')
  }
}

function applyAiSuggestion() {
  if (aiAction.value === 'preClassCheck' || aiAction.value === 'reflection') {
    const generatedItems = createBeginnerItemsFromLines(aiAction.value, aiSuggestion.value)
    if (!generatedItems.length) {
      ElMessage.warning('建议内容为空，请补充后再应用')
      return
    }

    const defaultTitle = getModuleDefinition(aiAction.value)?.label
    const placeholderIndex = document.items.findIndex(item => (
      !item.advanced && item.moduleType === aiAction.value && item.title === defaultTitle
    ))
    let firstGeneratedIndex = document.items.length
    if (placeholderIndex >= 0) {
      firstGeneratedIndex = placeholderIndex
      document.items[placeholderIndex] = {
        ...document.items[placeholderIndex],
        title: generatedItems[0].title
      }
      document.items.splice(placeholderIndex + 1, 0, ...generatedItems.slice(1))
    } else {
      document.items.push(...generatedItems)
    }
    selectedId.value = document.items[firstGeneratedIndex]?.id || ''
    selectedPreset.value = ''
    commitDocument()
    aiDialogVisible.value = false
    ElMessage.success(`已加入 ${generatedItems.length} 个教学模块`)
    return
  }

  let target = document.items.find(item => item.id === aiTargetId.value)
  const moduleTypeMap = {
    generateObjectives: 'learningObjective',
    preClassCheck: 'preClassCheck',
    reflection: 'reflection'
  }
  if (!target) {
    const moduleType = moduleTypeMap[aiAction.value]
    target = document.items.find(item => item.moduleType === moduleType && !item.advanced)
    if (!target && moduleType) {
      addModule(moduleType)
      target = document.items[document.items.length - 1]
    }
  }
  if (!target) return
  if (aiAction.value === 'generateObjectives') target.content = aiSuggestion.value
  else target.title = aiSuggestion.value
  selectedId.value = target.id
  commitDocument()
  aiDialogVisible.value = false
  ElMessage.success('建议内容已加入当前草稿')
}

function restoreDraft(payload) {
  if (!payload || !payload.formJson) return
  const incoming = JSON.stringify(payload.formJson)
  if (incoming === JSON.stringify(baseFormJson.value)) return
  if (payload.metadata) {
    syncingMetadata.value = true
    Object.assign(draftMeta, payload.metadata)
    nextTick(() => { syncingMetadata.value = false })
  }
  setFormJson(payload.formJson, true)
  emit('update-metadata', { ...draftMeta })
  emitFormJson()
  ElMessage.info('已恢复上次自动保存的草稿')
}
</script>

<style scoped>
.beginner-workbench {
  --ink: #203840;
  --muted: #71848b;
  --line: #dce5e7;
  --accent: #287c75;
  min-height: calc(100vh - 84px);
  color: var(--ink);
  background: #f1f4f4;
}
.workbench-header, .wizard-footer { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 12px 20px; border-bottom: 1px solid var(--line); background: #fff; }
.workbench-header { position: sticky; top: 0; z-index: 12; }
.header-left, .header-actions { display: flex; align-items: center; gap: 12px; min-width: 0; }
.header-left strong, .header-left span { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.header-left strong { max-width: 42vw; font-size: 16px; }
.header-left span { margin-top: 2px; color: var(--muted); font-size: 12px; }
.step-band { padding: 16px max(20px, 8vw) 14px; border-bottom: 1px solid var(--line); background: #f8fafa; }
.step-band :deep(.el-step__title) { font-size: 13px; }
.step-band :deep(.el-step__description) { font-size: 11px; }
.wizard-body { min-height: 560px; }
.recovery-alert { margin: 14px 20px 0; }
.basic-step { max-width: 900px; padding: 40px 24px 32px; margin: 0 auto; }
.step-introduction { display: flex; align-items: flex-start; gap: 16px; margin-bottom: 28px; }
.step-number { display: grid; place-items: center; flex: 0 0 42px; height: 42px; color: #fff; border-radius: 4px; background: var(--accent); font-family: Georgia, serif; font-size: 16px; }
.step-introduction h2 { margin: 0 0 5px; font-family: 'Microsoft YaHei', sans-serif; font-size: 22px; letter-spacing: 0; }
.step-introduction p { margin: 0; color: var(--muted); font-size: 13px; }
.metadata-form { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); column-gap: 24px; padding: 24px; border: 1px solid var(--line); border-radius: 6px; background: #fff; }
.metadata-form .span-two { grid-column: span 2; }
.field-suffix { margin-left: 8px; color: var(--muted); font-size: 12px; }
.field-help { width: 100%; margin: 7px 0 0; color: var(--muted); font-size: 12px; }
.content-step { background: #fff; }
.ai-alert { width: auto; margin: 12px 20px 0; }
.ai-tools { display: flex; align-items: center; gap: 8px; padding: 10px 20px; border-bottom: 1px solid var(--line); background: #f7faf9; }
.ai-tools > span { display: inline-flex; align-items: center; gap: 6px; margin-right: auto; color: #38645f; font-size: 12px; font-weight: 650; }
.editor-grid { display: grid; grid-template-columns: 250px minmax(380px, 1fr) 330px; min-height: 620px; border-top: 1px solid var(--line); }
.module-canvas { min-width: 0; padding: 16px 18px 28px; background: #eef2f2; }
.canvas-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 12px; }
.canvas-toolbar strong, .canvas-toolbar span { display: block; }
.canvas-toolbar strong { font-size: 15px; }
.canvas-toolbar span { margin-top: 2px; color: var(--muted); font-size: 11px; }
.module-list { display: grid; gap: 8px; min-height: 120px; }
.module-card { display: grid; grid-template-columns: 28px 32px minmax(0, 1fr) auto 30px; align-items: center; gap: 8px; min-height: 68px; padding: 8px 10px; border: 1px solid #d8e2e4; border-radius: 6px; background: #fff; cursor: pointer; transition: border-color 150ms ease, box-shadow 150ms ease; }
.module-card:hover, .module-card.selected { border-color: var(--accent); box-shadow: 0 5px 16px rgba(39, 83, 81, 0.08); }
.module-card.advanced { border-style: dashed; background: #fafbfb; }
.drag-handle { display: grid; place-items: center; width: 28px; height: 34px; padding: 0; color: #91a0a5; border: 0; background: transparent; cursor: grab; }
.module-order { color: #809097; font-family: Georgia, serif; font-size: 12px; }
.module-summary { min-width: 0; }
.module-summary strong, .module-summary small { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.module-summary strong { color: #28434c; font-size: 13px; }
.module-summary small { margin-top: 3px; color: #829198; font-size: 11px; }
.module-ghost { opacity: 0.4; }
.preview-step { display: grid; grid-template-columns: minmax(0, 1fr) 300px; gap: 18px; padding: 20px; }
.preview-main { min-width: 0; }
.structure-summary { align-self: start; padding: 18px; border: 1px solid var(--line); border-radius: 6px; background: #fff; }
.summary-heading { display: flex; align-items: center; gap: 12px; padding-bottom: 14px; border-bottom: 1px solid #e2e9eb; }
.summary-heading strong, .summary-heading small { display: block; }
.summary-heading small { margin-top: 3px; color: var(--muted); font-size: 11px; }
.structure-summary dl { margin: 14px 0; }
.structure-summary dl > div { display: grid; grid-template-columns: 76px minmax(0, 1fr); gap: 8px; padding: 6px 0; font-size: 12px; }
.structure-summary dt { color: var(--muted); }
.structure-summary dd { margin: 0; color: #29434c; font-weight: 600; }
.outline-list { display: grid; gap: 5px; max-height: 220px; padding: 10px; margin-bottom: 14px; overflow: auto; border: 1px solid #e0e8ea; border-radius: 4px; background: #f8fafa; }
.outline-list span { display: grid; grid-template-columns: 22px minmax(0, 1fr); align-items: center; color: #536a72; font-size: 11px; }
.outline-list em { color: var(--accent); font-style: normal; }
.save-primary, .save-secondary { width: 100%; margin: 7px 0 0; }
.save-secondary { margin-left: 0; }
.wizard-footer { position: sticky; bottom: 0; z-index: 10; border-top: 1px solid var(--line); border-bottom: 0; }
.suggestion-input { margin-top: 14px; }
@media (max-width: 1100px) {
  .editor-grid { grid-template-columns: 1fr; }
  .preview-step { grid-template-columns: 1fr; }
  .structure-summary { width: auto; }
}
@media (max-width: 680px) {
  .workbench-header { align-items: flex-start; flex-direction: column; }
  .header-actions { justify-content: space-between; width: 100%; }
  .step-band { padding-inline: 6px; }
  .step-band :deep(.el-step__description) { display: none; }
  .basic-step { padding: 24px 10px; }
  .metadata-form { grid-template-columns: 1fr; padding: 14px; }
  .metadata-form .span-two { grid-column: span 1; }
  .ai-tools { align-items: stretch; flex-direction: column; }
  .ai-tools > span { margin: 0 0 4px; }
  .module-card { grid-template-columns: 24px 26px minmax(0, 1fr) 28px; }
  .module-card > .el-tag { display: none; }
  .preview-step { padding: 8px; }
}
</style>
