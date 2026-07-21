<template>
  <section class="lesson-guide-panel" aria-labelledby="lesson-guide-title">
    <div class="panel-heading">
      <div>
        <h3 id="lesson-guide-title">电子导学单</h3>
        <p>学生范围与本课程班级保持一致，成绩单独统计。</p>
      </div>
      <el-switch
        :model-value="enabled"
        inline-prompt
        active-text="启用"
        inactive-text="关闭"
        @change="handleEnabledChange"
      />
    </div>

    <div v-if="selectedTemplate" class="template-summary" :class="{ disabled: !enabled }">
      <div class="template-mark">导</div>
      <div class="template-copy">
        <div class="template-title-row">
          <strong>{{ selectedTemplate.sheetTitle }}</strong>
          <el-tag :type="enabled ? 'success' : 'info'" effect="plain" size="small">
            {{ enabled ? '本课程已启用' : '已关闭，进度保留' }}
          </el-tag>
        </div>
        <p>{{ templateMeta(selectedTemplate) }}</p>
        <small v-if="selectedTemplate.fromBinding">课程中的内容不受原模板后续修改影响</small>
      </div>
      <div class="template-actions">
        <el-button icon="View" @click="openPreview">预览本课程内容</el-button>
        <el-button type="primary" plain icon="Refresh" @click="openSelector">更换模板</el-button>
        <el-button v-if="enabled" type="warning" plain icon="CircleClose" @click="disableAndKeep">
          关闭但保留进度
        </el-button>
      </div>
    </div>

    <div v-else class="empty-selection">
      <div>
        <strong>{{ enabled ? '请选择一份导学单模板' : '需要时可为本课程启用电子导学单' }}</strong>
        <p>模板只决定学习内容，不会改变课程班级。</p>
      </div>
      <el-button type="primary" icon="Collection" @click="openSelector">
        {{ enabled ? '选择模板' : '启用并选择模板' }}
      </el-button>
    </div>

    <el-dialog v-model="selectorVisible" title="选择导学单模板" width="min(960px, 96vw)" append-to-body destroy-on-close>
      <el-form :model="query" :inline="true" label-position="top" class="selector-filter">
        <el-form-item label="模板标题">
          <el-input v-model="query.sheetTitle" clearable placeholder="输入标题关键词" style="width: 180px" @keyup.enter="loadTemplates" />
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="query.grade" clearable placeholder="全部年级" style="width: 120px">
            <el-option
              v-for="dict in gradeOptions"
              :key="dict.value"
              :label="dict.label"
              :value="normalizeDictValue(dict.value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="query.semester" clearable placeholder="全部学期" style="width: 120px">
            <el-option
              v-for="dict in semesterOptions"
              :key="dict.value"
              :label="dict.label"
              :value="String(dict.value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="第几课">
          <el-select v-model="query.lessonNum" clearable placeholder="全部课次" style="width: 120px">
            <el-option v-for="num in 30" :key="num" :label="`第 ${num} 课`" :value="num" />
          </el-select>
        </el-form-item>
        <el-form-item label="可见范围">
          <el-select v-model="query.scope" clearable placeholder="全部" style="width: 140px">
            <el-option label="公共导学单" value="public" />
            <el-option label="我的私有" value="mine" />
          </el-select>
        </el-form-item>
        <el-form-item label=" ">
          <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="templates" row-key="sheetId" class="template-table">
        <el-table-column label="模板" min-width="280">
          <template #default="{ row }">
            <div class="list-template-title">
              <div class="list-template-heading">
                <strong>{{ row.sheetTitle }}</strong>
                <el-tag v-if="isCourseMatch(row)" type="success" effect="plain" size="small">本课适用</el-tag>
              </div>
              <span>{{ templateMeta(row) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="范围" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isPublic === 'Y' ? 'success' : 'info'" effect="plain" size="small">
              {{ row.isPublic === 'Y' ? '公共导学单' : '我的私有' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建人" prop="creatorName" width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="170" align="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="previewTemplate(row)">预览</el-button>
            <el-button link type="success" @click="selectTemplate(row)">选择</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无符合条件的导学单，可调整年级、学期或清空课次后再查" :image-size="72">
            <el-button v-if="hasActiveFilters" type="primary" plain @click="clearFiltersAndReload">
              清空筛选
            </el-button>
          </el-empty>
        </template>
      </el-table>
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="query.pageNum"
        v-model:limit="query.pageSize"
        @pagination="loadTemplates"
      />
    </el-dialog>

    <el-dialog v-model="previewVisible" :title="previewTitle" width="min(940px, 96vw)" append-to-body destroy-on-close>
      <div v-loading="previewLoading" class="preview-surface">
        <v-form-render
          v-if="previewFormJson"
          :form-json="previewFormJson"
          :form-data="{}"
          :option-data="{}"
        />
        <el-empty v-else-if="!previewLoading" description="该模板暂无可预览内容" />
      </div>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getGuideSheet,
  getGuideSheetBindingPreview,
  getGuideSheetPreview,
  listGuideSheet
} from '@/api/business/guideSheet'

const props = defineProps({
  enabled: { type: Boolean, default: false },
  sourceSheetId: { type: [Number, String], default: null },
  replaceRequested: { type: Boolean, default: false },
  currentBinding: { type: Object, default: null },
  grade: { type: [Number, String], default: null },
  semester: { type: [Number, String], default: null },
  lessonNum: { type: [Number, String], default: null },
  gradeOptions: { type: Array, default: () => [] },
  semesterOptions: { type: Array, default: () => [] }
})

const emit = defineEmits([
  'update:enabled',
  'update:sourceSheetId',
  'update:replaceRequested'
])

const selectorVisible = ref(false)
const previewVisible = ref(false)
const previewLoading = ref(false)
const previewTitle = ref('导学单预览')
const previewFormJson = ref(null)
const loading = ref(false)
const templates = ref([])
const total = ref(0)
const selectedOverride = ref(null)
let listRequestId = 0
let selectionRequestId = 0

const query = reactive({
  pageNum: 1,
  pageSize: 8,
  sheetTitle: '',
  grade: null,
  semester: null,
  lessonNum: null,
  scope: null
})

const bindingTemplate = computed(() => {
  const binding = props.currentBinding
  if (!binding) return null
  return {
    bindingId: binding.bindingId,
    sheetId: binding.sourceSheetId,
    sheetTitle: binding.snapshotTitle || '电子导学单',
    grade: binding.snapshotGrade,
    semester: binding.snapshotSemester,
    lessonNum: binding.snapshotLessonNum,
    fromBinding: true
  }
})

const selectedTemplate = computed(() => selectedOverride.value || bindingTemplate.value)

const hasActiveFilters = computed(() => {
  const hasTitle = Boolean(query.sheetTitle && String(query.sheetTitle).trim())
  const hasGrade = query.grade !== null && query.grade !== undefined && query.grade !== ''
  const hasSemester = query.semester !== null && query.semester !== undefined && query.semester !== ''
  const hasLessonNum = query.lessonNum !== null && query.lessonNum !== undefined && query.lessonNum !== ''
  return hasTitle || hasGrade || hasSemester || hasLessonNum || Boolean(query.scope)
})

watch(
  () => [props.sourceSheetId, props.currentBinding?.bindingId],
  async ([value]) => {
    const requestId = ++selectionRequestId
    if (!value) {
      if (!props.currentBinding) selectedOverride.value = null
      return
    }
    if (bindingTemplate.value && String(bindingTemplate.value.sheetId) === String(value)) {
      selectedOverride.value = null
      return
    }
    if (selectedOverride.value && String(selectedOverride.value.sheetId) === String(value)) return
    try {
      const response = await getGuideSheet(value)
      if (requestId !== selectionRequestId) return
      const data = response.data || response
      selectedOverride.value = { ...data, fromBinding: false }
    } catch (_error) {
      if (requestId === selectionRequestId) selectedOverride.value = null
    }
  },
  { immediate: true }
)

function optionLabel(options, value) {
  return options.find(item => String(item.value) === String(value))?.label || `${value || '-'}年级`
}

/** 字典值统一为数字（年级/课次），避免字符串与数字比对失败 */
function normalizeDictValue(value) {
  if (value === null || value === undefined || value === '') return value
  const num = Number(value)
  return Number.isNaN(num) ? value : num
}

function normalizeQueryValue(value) {
  if (value === null || value === undefined || value === '') return undefined
  return value
}

function templateMeta(template) {
  const grade = optionLabel(props.gradeOptions, template.grade)
  const semester = props.semesterOptions.find(item => String(item.value) === String(template.semester))?.label || '-'
  return `${grade} · ${semester} · 第 ${template.lessonNum || '-'} 课`
}

/** 与当前课程元数据完全一致时标记「本课适用」 */
function isCourseMatch(template) {
  if (props.grade === null || props.grade === undefined || props.grade === '') return false
  if (props.semester === null || props.semester === undefined || props.semester === '') return false
  if (props.lessonNum === null || props.lessonNum === undefined || props.lessonNum === '') return false
  return String(template.grade) === String(props.grade)
    && String(template.semester) === String(props.semester)
    && String(template.lessonNum) === String(props.lessonNum)
}

function handleEnabledChange(value) {
  if (value && !selectedTemplate.value) {
    emit('update:enabled', true)
    openSelector()
    return
  }
  emit('update:enabled', Boolean(value))
}

function disableAndKeep() {
  ElMessageBox.confirm(
    '关闭后学生暂时不能进入，但已有填写进度和成绩都会保留。',
    '关闭电子导学单',
    { type: 'warning', confirmButtonText: '关闭并保留进度' }
  ).then(() => emit('update:enabled', false)).catch(() => {})
}

/** 打开选模板：默认带入课程年级、学期；第几课不默认选中，方便同册复用 */
function resetQuery() {
  Object.assign(query, {
    pageNum: 1,
    sheetTitle: '',
    grade: props.grade != null && props.grade !== '' ? normalizeDictValue(props.grade) : null,
    semester: props.semester != null && props.semester !== '' ? String(props.semester) : null,
    lessonNum: null,
    scope: null
  })
  loadTemplates()
}

function clearFiltersAndReload() {
  Object.assign(query, {
    pageNum: 1,
    sheetTitle: '',
    grade: null,
    semester: null,
    lessonNum: null,
    scope: null
  })
  loadTemplates()
}

function handleSearch() {
  query.pageNum = 1
  loadTemplates()
}

function openSelector() {
  selectorVisible.value = true
  resetQuery()
}

function buildListParams() {
  const params = {
    pageNum: query.pageNum,
    pageSize: query.pageSize
  }
  const title = query.sheetTitle && String(query.sheetTitle).trim()
  if (title) params.sheetTitle = title
  const grade = normalizeQueryValue(query.grade)
  if (grade !== undefined) params.grade = normalizeDictValue(grade)
  const semester = normalizeQueryValue(query.semester)
  if (semester !== undefined) params.semester = String(semester)
  const lessonNum = normalizeQueryValue(query.lessonNum)
  if (lessonNum !== undefined) params.lessonNum = Number(lessonNum)
  // 仅显式 public / mine 传给后端；空值走默认可见性（本人 + 同县公开）
  if (query.scope === 'public' || query.scope === 'mine') {
    params.scope = query.scope
  } else {
    params.scope = 'all'
  }
  return params
}

async function loadTemplates() {
  const requestId = ++listRequestId
  loading.value = true
  try {
    const response = await listGuideSheet(buildListParams())
    if (requestId !== listRequestId) return
    const rows = response.rows || response.data || []
    // 本课匹配的模板排在前面，便于教师选用
    templates.value = [...rows].sort((a, b) => Number(isCourseMatch(b)) - Number(isCourseMatch(a)))
    total.value = Number(response.total || rows.length || 0)
  } finally {
    if (requestId === listRequestId) loading.value = false
  }
}

async function selectTemplate(row) {
  const current = bindingTemplate.value
  const isReplacement = Boolean(current && String(current.sheetId) !== String(row.sheetId))
  if (isReplacement) {
    try {
      await ElMessageBox.confirm(
        '更换模板将为本课程创建新的导学单快照，已有学生答卷仍会保留在原快照中。',
        '确认更换模板',
        { type: 'warning', confirmButtonText: '确认更换' }
      )
    } catch (_error) {
      return
    }
  }
  emit('update:replaceRequested', isReplacement)
  selectedOverride.value = { ...row, fromBinding: false }
  emit('update:sourceSheetId', row.sheetId)
  emit('update:enabled', true)
  selectorVisible.value = false
  ElMessage.success(`已选择“${row.sheetTitle}”`)
}

function openPreview() {
  if (!selectedTemplate.value) return
  previewTemplate(selectedTemplate.value)
}

async function previewTemplate(template) {
  previewVisible.value = true
  previewLoading.value = true
  previewFormJson.value = null
  previewTitle.value = template.sheetTitle || '导学单预览'
  try {
    const response = template.fromBinding && template.bindingId
      ? await getGuideSheetBindingPreview(template.bindingId)
      : await getGuideSheetPreview(template.sheetId)
    const data = response.data || response
    previewTitle.value = data.title || data.sheetTitle || previewTitle.value
    const raw = data.formJson
    previewFormJson.value = typeof raw === 'string' ? JSON.parse(raw) : raw
  } catch (error) {
    if (error instanceof SyntaxError) ElMessage.error('导学单内容格式无效，暂时无法预览')
  } finally {
    previewLoading.value = false
  }
}
</script>

<style scoped>
.lesson-guide-panel {
  margin: 8px 0 22px;
  padding: 18px;
  border: 1px solid #d9e3e5;
  border-left: 4px solid #197b72;
  background: #fbfdfd;
}

.panel-heading,
.template-summary,
.empty-selection,
.template-title-row,
.template-actions {
  display: flex;
  align-items: center;
}

.panel-heading {
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 14px;
}

.panel-heading h3,
.panel-heading p,
.empty-selection p,
.template-copy p {
  margin: 0;
}

.panel-heading h3 { color: #183f47; font-size: 16px; }
.panel-heading p,
.empty-selection p { margin-top: 4px; color: #6e7f85; font-size: 12px; }

.template-summary,
.empty-selection {
  gap: 14px;
  min-height: 76px;
  padding: 14px;
  border-top: 1px solid #e2eaeb;
}

.template-summary {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  align-items: start;
}

.template-summary.disabled { opacity: 0.78; }
.empty-selection { justify-content: space-between; }

.template-mark {
  display: grid;
  place-items: center;
  flex: 0 0 42px;
  height: 42px;
  color: #fff;
  border-radius: 6px 2px 6px 2px;
  background: #197b72;
  font-family: STKaiti, KaiTi, serif;
  font-size: 20px;
}

.template-copy { min-width: 0; flex: 1; }
.template-title-row { gap: 10px; flex-wrap: wrap; }
.template-title-row strong { min-width: 0; color: #213f49; overflow-wrap: anywhere; }
.template-copy p { margin-top: 5px; color: #597078; font-size: 12px; }
.template-copy small { color: #8a989d; }
.template-actions {
  grid-column: 2;
  justify-content: flex-start;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 10px;
}
.template-actions :deep(.el-button) { margin-left: 0; }

.selector-filter :deep(.el-form-item) { margin-right: 12px; margin-bottom: 12px; }
.list-template-title strong,
.list-template-title span { display: block; }
.list-template-heading {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.list-template-title strong { color: #23424a; }
.list-template-title span { margin-top: 4px; color: #7b898e; font-size: 12px; }

.preview-surface {
  min-height: 280px;
  max-height: 72vh;
  padding: 18px;
  overflow: auto;
  border: 1px solid #e1e8e9;
  background: #f7f9f9;
}

@media (max-width: 760px) {
  .panel-heading,
  .template-summary,
  .empty-selection { align-items: stretch; flex-direction: column; }
  .template-actions { justify-content: flex-start; }
  .template-actions :deep(.el-button) { margin-left: 0; }
  .template-mark { display: none; }
}
</style>
