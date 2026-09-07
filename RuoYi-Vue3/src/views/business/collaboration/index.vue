<template>
  <div class="app-container collaboration-workspace">
    <el-card shadow="never">
      <template #header>
        <div class="workspace-header">
          <span>{{ workspace.lessonTitle || '在线协作工作台' }}</span>
          <div class="header-actions">
            <el-tag :type="workspace.health?.ready ? 'success' : 'warning'" size="small">
              {{ workspace.health?.ready ? '服务已就绪' : '服务未就绪' }}
            </el-tag>
            <el-button size="small" @click="historyDrawerVisible = true">往期作品回顾</el-button>
          </div>
        </div>
      </template>
      <el-alert v-if="!workspace.health?.ready" type="warning" :closable="false" show-icon
        title="协作服务暂不可用" :description="healthProblems" class="workspace-alert" />
      <el-form label-width="90px" class="workspace-form">
        <el-form-item label="协作班级">
          <el-checkbox v-model="selectAllClasses" :indeterminate="isIndeterminateClasses" @change="toggleSelectAllClasses">全选</el-checkbox>
          <el-checkbox-group v-model="selectedClassKeys" @change="onClassesChange" class="class-checkbox-group">
            <el-checkbox v-for="item in workspace.classes" :key="item.key" :value="item.key" border>
              {{ item.entryYear }}级{{ item.classCode }}班（{{ item.students.length }}人）
            </el-checkbox>
          </el-checkbox-group>
          <span v-if="!workspace.classes?.length" class="muted">本课程尚未指派班级</span>
        </el-form-item>
        <el-form-item v-if="isBatchMode" label="微调班级">
          <el-radio-group v-model="activeClassKey" @change="onClassChange">
            <el-radio-button v-for="key in selectedClassKeys" :key="key" :value="key">{{ classLabel(key) }}</el-radio-button>
          </el-radio-group>
          <span class="muted">勾选多个班级时批量建组；下方工作台仅微调当前选中的班级</span>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="workspace-card">
      <template #header>
        <div class="workspace-header">
          <span>已选文档（选几个文档，就分几个组）</span>
          <el-button size="small" type="primary" @click="openDocDialog">从题库添加/更换协作文档</el-button>
        </div>
      </template>
      <div v-if="selectedDocs.length" class="doc-cards">
        <el-card v-for="item in selectedDocs" :key="item.materialId" shadow="hover" class="doc-card">
          <div class="doc-card-title">{{ item.fileName }}</div>
          <div class="muted">{{ formatFileSize(item.fileSize) }} · 文件作品题 · {{ docMeta(item) }}</div>
          <div class="doc-card-question">{{ item.questionContent }}</div>
          <el-button link type="danger" @click="removeDoc(item.materialId)">移除</el-button>
        </el-card>
      </div>
      <el-empty v-else description="还没有选择协作文档，点击右上角按钮从题库挑选" />
      <div v-if="selectedDocs.length" class="muted">已选 {{ selectedDocs.length }} 个文档，对应划分为 {{ selectedDocs.length }} 个小组；协作不计分、不批改。</div>
    </el-card>

    <el-dialog v-model="docDialogVisible" title="选择协作文档" width="1000px" destroy-on-close>
      <el-form :inline="true" :model="docSearch" size="small" class="doc-search-form">
        <el-form-item label="年级">
          <el-select v-model="docSearch.grade" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="g in [7, 8, 9]" :key="g" :label="gradeName(g)" :value="g" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="docSearch.semester" placeholder="全部" clearable style="width: 110px">
            <el-option label="上册" value="0" />
            <el-option label="下册" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="课次">
          <el-select v-model="docSearch.lessonNum" placeholder="全部" clearable style="width: 110px">
            <el-option v-for="n in 20" :key="n" :label="`第 ${n} 课`" :value="n" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="docSearch.keyword" placeholder="标题/题干/文件名" clearable style="width: 200px" @keyup.enter="searchDocs" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchDocs">搜索</el-button>
          <el-button @click="resetDocSearch">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table ref="docTableRef" v-loading="docSearchLoading" :data="docSearchRows" size="small" stripe
        row-key="materialId" @selection-change="onDocSelectionChange" empty-text="没有符合条件的起始文件">
        <el-table-column type="selection" width="45" :selectable="() => true" :reserve-selection="true" />
        <el-table-column prop="fileName" label="文档名称" min-width="220" show-overflow-tooltip />
        <el-table-column label="文件大小" width="100">
          <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="questionContent" label="对应题目题干" min-width="220" show-overflow-tooltip />
        <el-table-column label="所属年级学期" width="150">
          <template #default="{ row }">{{ docMeta(row) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="previewDoc(row)">预览</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="docSearch.pageNum" :page-size="docSearch.pageSize"
        :total="docSearchTotal" layout="prev, pager, next, total" :pager-count="5" @current-change="searchDocs" class="doc-pagination" />
      <template #footer>
        <span class="muted">当前已勾选 {{ dialogSelection.length }} 个文档</span>
        <el-button @click="docDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDocs">确认选用</el-button>
      </template>
    </el-dialog>
    <pdf-preview ref="pdfPreviewRef" />

    <el-card shadow="never" class="workspace-card">
      <template #header>
        <div class="workspace-header">
          <span>分组与任务分配（按学号连续均分，可手动调整）</span>
          <el-button size="small" @click="autoSplit(true)">重新均分</el-button>
        </div>
      </template>
      <el-alert v-if="currentDetail && !currentDetail.frozenTime" type="info" :closable="false" show-icon
        title="本轮尚未开始：现在保存会直接调整当前分组，不产生新轮次。" class="workspace-alert" />
      <el-alert v-if="currentDetail?.frozenTime && !isNewRound" type="warning" :closable="false" show-icon
        title="学生已经开始协作：如需调整请点击“新建一轮”，旧分组与作品会保留。" class="workspace-alert" />
      <el-empty v-if="!editGroups.length" description="请先选择文档，系统会按学号自动均分" />
      <div v-for="(group, index) in editGroups" :key="index" class="edit-group">
        <div class="edit-group-title">
          <strong>第{{ index + 1 }}组</strong>
          <el-tag size="small" type="info">{{ group.studentIds.length }}人</el-tag>
          <el-select v-model="group.materialId" placeholder="选择本组任务文档" class="group-doc-select">
            <el-option v-for="item in selectedDocs" :key="item.materialId" :label="item.fileName" :value="item.materialId" />
          </el-select>
        </div>
        <div class="member-tags">
          <el-tag v-for="studentId in group.studentIds" :key="studentId" closable size="small"
            @close="removeMember(index, studentId)" class="member-tag">
            {{ studentLabel(studentId) }}
          </el-tag>
          <span v-if="!group.studentIds.length" class="muted">空组：请从待分配名单中加入学生</span>
        </div>
        <div class="member-move">
          <span class="muted">把本组成员移至：</span>
          <el-select v-model="moveTarget[index]" placeholder="选择目标组" size="small" class="move-select" @change="moveMember(index, $event)">
            <el-option v-for="target in moveOptions(index)" :key="target" :label="`第${target + 1}组`" :value="target" />
          </el-select>
          <el-select v-model="moveStudent[index]" placeholder="选择学生" size="small" class="move-select">
            <el-option v-for="studentId in group.studentIds" :key="studentId" :label="studentLabel(studentId)" :value="studentId" />
          </el-select>
        </div>
      </div>
      <div v-if="unassigned.length" class="unassigned">
        <strong>待分配（{{ unassigned.length }}人）：</strong>
        <el-tag v-for="student in unassigned" :key="student.studentId" size="small" class="member-tag">
          {{ student.studentNo }} {{ student.studentName }}
        </el-tag>
        <span class="muted">请把待分配学生移入各组，保存前不能遗漏。</span>
        <div v-for="student in unassigned" :key="'assign-' + student.studentId" class="assign-row">
          <span>{{ student.studentNo }} {{ student.studentName }}</span>
          <el-select :model-value="null" placeholder="加入分组" size="small" class="move-select" @change="assignMember(student.studentId, $event)">
            <el-option v-for="(group, idx) in editGroups" :key="idx" :label="`第${idx + 1}组`" :value="idx" />
          </el-select>
        </div>
      </div>
      <div class="workspace-actions">
        <el-button type="primary" :loading="saving" :disabled="!canSave" @click="save">保存并进入协作</el-button>
        <el-button v-if="currentDetail?.frozenTime" :disabled="isNewRound" @click="startNewRound">新建一轮协作</el-button>
        <el-button @click="load">刷新</el-button>
        <span v-if="saveError" class="save-error">{{ saveError }}</span>
      </div>
    </el-card>

    <el-card v-if="isBatchMode" shadow="never" class="workspace-card">
      <template #header><span>批量建组（{{ selectedClassKeys.length }}个班级 · 各班按学号连续均分）</span></template>
      <el-table :data="batchPreview" size="small" stripe empty-text="请先选择协作文档">
        <el-table-column label="班级" width="150">
          <template #default="{ row }">{{ row.entryYear }}级{{ row.classCode }}班</template>
        </el-table-column>
        <el-table-column label="人数" width="80" align="center">
          <template #default="{ row }">{{ row.students }}人</template>
        </el-table-column>
        <el-table-column label="将分为" width="80" align="center">
          <template #default="{ row }">{{ row.groups }}组</template>
        </el-table-column>
        <el-table-column label="当前状态" min-width="160">
          <template #default="{ row }">{{ row.statusText }}</template>
        </el-table-column>
      </el-table>
      <div class="workspace-actions">
        <el-button type="primary" :loading="saving" :disabled="!canBatchSave" @click="batchSave">一键为所选班级生成协作并保存</el-button>
        <span class="muted">已开始协作的班级会被跳过，请单独微调或新建轮次；其余班级互不影响。</span>
        <span v-if="saveError" class="save-error">{{ saveError }}</span>
      </div>
    </el-card>
    <el-card v-if="currentDetail" shadow="never" class="workspace-card">
      <template #header>
        <div class="workspace-header">
          <span>当前协作{{ currentDetail.frozenTime ? '（进行中）' : '（未开始）' }}</span>
          <el-tag size="small">{{ currentDetail.entryYear }}级{{ currentDetail.classCode }}班</el-tag>
        </div>
      </template>
      <el-table :data="currentDetail.groupTasks || []" size="small" stripe>
        <el-table-column label="小组" width="90">
          <template #default="{ row }">{{ detailGroupName(currentDetail, row.snapshotGroupId) }}</template>
        </el-table-column>
        <el-table-column prop="versionName" label="任务文档" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button link type="primary" @click="openRoom(row.roomId)">进入文档</el-button>
            <el-button link type="info" @click="openRevisions(row.roomId)">版本</el-button>
            <el-button link type="info" @click="openTimeline(row.roomId)">轨迹</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="historyDrawerVisible" title="往期作品回顾" direction="rtl" size="640px" destroy-on-close>
      <div class="muted">仅供查阅的过往归档作品，不影响当前教学。</div>
      <el-table :data="workspace.activities" size="small" stripe empty-text="暂无历史轮次">
        <el-table-column prop="activityTitle" label="轮次" min-width="170" show-overflow-tooltip />
        <el-table-column label="班级" width="110">
          <template #default="{ row }">{{ row.entryYear }}级{{ row.classCode }}班</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">{{ row.status === 'OPEN' ? (row.frozenTime ? '进行中' : '未开始') : '已归档' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110">
          <template #default="{ row }"><el-button link type="primary" @click="openActivity(row)">查看作品</el-button></template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <el-dialog v-model="activityDialogVisible" :title="activityDetailTitle" width="900px" destroy-on-close>
      <el-table :data="activityDetail?.groupTasks || []" size="small" stripe>
        <el-table-column label="小组" width="90">
          <template #default="{ row }">{{ detailGroupName(activityDetail, row.snapshotGroupId) }}</template>
        </el-table-column>
        <el-table-column label="成员" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ detailMemberText(activityDetail, row.snapshotGroupId) }}</template>
        </el-table-column>
        <el-table-column prop="versionName" label="任务文档" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button link type="primary" @click="openRoom(row.roomId)">进入文档</el-button>
            <el-button link type="info" @click="openRevisions(row.roomId)">版本</el-button>
            <el-button link type="info" @click="openTimeline(row.roomId)">轨迹</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-divider>操作轨迹</el-divider>
      <el-timeline v-if="timeline.length">
        <el-timeline-item v-for="item in timeline" :key="item.eventId" :timestamp="formatTime(item.createTime)">
          {{ item.actorName || item.userId }} · {{ item.eventType }}
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="选择小组查看操作轨迹" />
    </el-dialog>

    <el-dialog v-model="revisionDialogVisible" title="版本历史" width="760px" destroy-on-close>
      <el-table v-loading="revisionsLoading" :data="revisions" stripe size="small" empty-text="暂无版本记录">
        <el-table-column prop="versionNo" label="版本" width="80" align="center" />
        <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="保存时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCollaborationActivityDetail, getCollaborationRevisions, getCollaborationTimeline,
  getCollaborationWorkspace, saveCollaborationWorkspace, searchBankCollaborationMaterials
} from '@/api/business/collaboration'
import PdfPreview from '@/components/PdfPreview/index.vue'

const route = useRoute()
const router = useRouter()
const lessonId = computed(() => route.params.lessonId)

const workspace = reactive({ lessonTitle: '', health: {}, candidates: [], classes: [], activities: [] })
const activeClassKey = ref('')
// 协作班级多选：默认全选本课已指派班级；勾选 1 个进入单班微调，勾选多个进入批量建组。
const selectedClassKeys = ref([])
const selectedMaterialIds = ref([])
// 已选文档完整对象（名称/大小/题干/年级学期），弹窗确认与当前活动回显时填充。
const selectedDocs = ref([])
const historyDrawerVisible = ref(false)
const docDialogVisible = ref(false)
const docSearch = ref({ grade: null, semester: '', lessonNum: null, keyword: '', pageNum: 1, pageSize: 10 })
const docSearchRows = ref([])
const docSearchTotal = ref(0)
const docSearchLoading = ref(false)
const docTableRef = ref(null)
const dialogSelection = ref([])
const pdfPreviewRef = ref(null)
const editGroups = ref([])
const moveTarget = ref({})
const moveStudent = ref({})
const saving = ref(false)
const saveError = ref('')
const isNewRound = ref(false)
const requestId = ref(newRequestId())
// 已确认的班级：取消切换时恢复，避免 radio 已变更但数据仍是旧班级。
const confirmedClassKey = ref('')
// 保存成功后的基线：切换班级、改文档前对比，有未保存调整先确认，避免静默丢失。
const savedBaseline = ref('')

const activityDetail = ref(null)
const isBatchMode = computed(() => selectedClassKeys.value.length >= 2)
const selectAllClasses = computed({
  get: () => workspace.classes.length > 0 && selectedClassKeys.value.length === workspace.classes.length,
  set: () => {}
})
const isIndeterminateClasses = computed(() =>
  selectedClassKeys.value.length > 0 && selectedClassKeys.value.length < workspace.classes.length)
function classLabel(key) {
  const item = workspace.classes.find(c => c.key === key)
  return item ? `${item.entryYear}级${item.classCode}班` : key
}
// 年级数字转中文（题库 grade 为 1-9），学期 0=上册、1=下册。
function gradeName(grade) {
  const names = { 1: '一年级', 2: '二年级', 3: '三年级', 4: '四年级', 5: '五年级', 6: '六年级', 7: '七年级', 8: '八年级', 9: '九年级' }
  return names[Number(grade)] || (grade === null || grade === undefined || grade === '' ? '不限' : `${grade}年级`)
}
function docMeta(item) {
  if (!item) return '—'
  const parts = []
  if (item.grade !== null && item.grade !== undefined && item.grade !== '') parts.push(gradeName(item.grade))
  if (item.semester === '0' || item.semester === 0) parts.push('上册')
  else if (item.semester === '1' || item.semester === 1) parts.push('下册')
  if (item.lessonNum) parts.push(`第${item.lessonNum}课`)
  return parts.length ? parts.join(' · ') : '—'
}
const activityDialogVisible = ref(false)
const timeline = ref([])
const revisions = ref([])
const revisionsLoading = ref(false)
const revisionDialogVisible = ref(false)

const activeClass = computed(() => workspace.classes.find(item => item.key === activeClassKey.value) || null)
const classStudents = computed(() => {
  const list = (activeClass.value?.students || []).slice()
  list.sort((a, b) => String(a.studentNo || '').localeCompare(String(b.studentNo || ''), 'zh-CN', { numeric: true }))
  return list
})
const selectedClassItems = computed(() =>
  selectedClassKeys.value
    .map(key => workspace.classes.find(item => item.key === key))
    .filter(Boolean))
const batchPreview = computed(() =>
  selectedClassItems.value.map(item => ({
    entryYear: item.entryYear,
    classCode: item.classCode,
    students: (item.students || []).length,
    groups: selectedMaterialIds.value.length,
    statusText: item.current?.frozenTime ? '进行中（批量保存时跳过，需单独微调或新建轮次）' : (item.current ? '未开始（保存后直接调整分组）' : '尚未开设协作')
  })))
const canBatchSave = computed(() =>
  isBatchMode.value && selectedMaterialIds.value.length > 0 && !saving.value
    && selectedClassItems.value.some(item => !item.current?.frozenTime))
const currentDetail = computed(() => activeClass.value?.current || null)
const unassigned = computed(() => {
  const assigned = new Set(editGroups.value.flatMap(group => group.studentIds))
  return classStudents.value.filter(student => !assigned.has(Number(student.studentId)))
})
const editingFingerprint = computed(() => JSON.stringify({
  docs: selectedMaterialIds.value.map(Number),
  groups: editGroups.value.map(group => ({ doc: Number(group.materialId), members: group.studentIds.map(Number).sort((a, b) => a - b) }))
}))
const canSave = computed(() =>
  Boolean(activeClass.value) && selectedMaterialIds.value.length > 0 && editGroups.value.length > 0 && !saving.value)
const healthProblems = computed(() => {
  const problems = workspace.health?.problems || []
  return problems.length ? problems.join('；') : '请联系管理员检查协作服务配置。'
})
const activityDetailTitle = computed(() => {
  const detail = activityDetail.value
  return detail && detail.entryYear ? `${detail.entryYear}级${detail.classCode}班小组作品` : '小组作品'
})

function newRequestId() {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) return crypto.randomUUID()
  return `req-${Date.now()}-${Math.floor(Math.random() * 1000000)}`
}

async function load() {
  try {
    const response = await getCollaborationWorkspace(lessonId.value)
  const data = response.data || response || {}
  workspace.lessonTitle = data.lessonTitle || ''
  workspace.health = data.health || {}
  workspace.candidates = data.candidates || []
  workspace.classes = (data.classes || []).map(item => ({
    ...item,
    key: `${item.entryYear}:${item.classCode}`,
    students: item.students || []
  }))
  workspace.activities = data.activities || []
  const allKeys = workspace.classes.map(item => item.key)
  if (!selectedClassKeys.value.length) selectedClassKeys.value = allKeys
  selectedClassKeys.value = selectedClassKeys.value.filter(key => allKeys.includes(key))
  if (!selectedClassKeys.value.length && allKeys.length) selectedClassKeys.value = allKeys
  if (!selectedClassKeys.value.includes(activeClassKey.value)) {
    activeClassKey.value = selectedClassKeys.value.length ? selectedClassKeys.value[0] : ''
  }
  prefillFromCurrent()
  } catch (e) {
    ElMessage.error(e?.message || '协作工作台加载失败')
  }
}

function studentMap() {
  const map = new Map()
  for (const student of classStudents.value) map.set(Number(student.studentId), student)
  return map
}

function studentLabel(studentId) {
  const student = studentMap().get(Number(studentId))
  return student ? `${student.studentNo} ${student.studentName}` : `学生${studentId}`
}
function prefillFromCurrent() {
  const detail = currentDetail.value
  confirmedClassKey.value = activeClassKey.value
  isNewRound.value = false
  saveError.value = ''
  if (!detail || !detail.groups?.length) {
    selectedMaterialIds.value = []
    selectedDocs.value = []
    editGroups.value = []
    savedBaseline.value = editingFingerprint.value
    requestId.value = newRequestId()
    return
  }
  const tasks = detail.groupTasks || []
  const docIds = [...new Set(tasks.map(task => Number(task.sourceMaterialId)).filter(Boolean))]
  selectedMaterialIds.value = docIds.filter(id => workspace.candidates.some(item => Number(item.materialId) === id))
  selectedDocs.value = selectedMaterialIds.value
    .map(id => workspace.candidates.find(item => Number(item.materialId) === id))
    .filter(Boolean)
  const members = detail.members || []
  editGroups.value = (detail.groups || []).map(group => ({
    snapshotGroupId: group.snapshotGroupId,
    studentIds: members
      .filter(member => Number(member.snapshotGroupId) === Number(group.snapshotGroupId))
      .map(member => Number(member.studentId)),
    materialId: Number(tasks.find(task => Number(task.snapshotGroupId) === Number(group.snapshotGroupId))?.sourceMaterialId) || null
  }))
  savedBaseline.value = editingFingerprint.value
  requestId.value = newRequestId()
}

function hasUnsavedChanges() {
  return editingFingerprint.value !== savedBaseline.value
}

async function confirmDiscardChanges() {
  if (!hasUnsavedChanges()) return true
  try {
    await ElMessageBox.confirm('当前有未保存的分组调整，继续操作会丢失这些调整，是否继续？', '未保存的调整', {
      confirmButtonText: '继续', cancelButtonText: '取消', type: 'warning'
    })
    return true
  } catch {
    return false
  }
}

async function onClassChange() {
  if (!(await confirmDiscardChanges())) {
    activeClassKey.value = confirmedClassKey.value
    return
  }
  prefillFromCurrent()
}

async function toggleSelectAllClasses(checked) {
  if (!(await confirmDiscardChanges())) return
  selectedClassKeys.value = checked ? workspace.classes.map(item => item.key) : []
  if (checked && !selectedClassKeys.value.includes(activeClassKey.value)) {
    activeClassKey.value = selectedClassKeys.value.length ? selectedClassKeys.value[0] : ''
  }
  prefillFromCurrent()
}

async function onClassesChange() {
  if (!(await confirmDiscardChanges())) {
    prefillFromCurrent()
    return
  }
  if (!selectedClassKeys.value.includes(activeClassKey.value)) {
    activeClassKey.value = selectedClassKeys.value.length ? selectedClassKeys.value[0] : ''
  }
  prefillFromCurrent()
}

// 题库选题弹窗：搜索/分页/预览，确认后回填并自动均分。
function openDocDialog() {
  dialogSelection.value = [...selectedMaterialIds.value.map(Number)]
  docSearch.value.pageNum = 1
  docDialogVisible.value = true
  searchDocs()
}
async function searchDocs() {
  docSearchLoading.value = true
  try {
    const params = {
      pageNum: docSearch.value.pageNum,
      pageSize: docSearch.value.pageSize,
      grade: docSearch.value.grade ?? '',
      semester: docSearch.value.semester ?? '',
      lessonNum: docSearch.value.lessonNum ?? '',
      keyword: (docSearch.value.keyword || '').trim()
    }
    const response = await searchBankCollaborationMaterials(params)
    const data = response.data || response || {}
    docSearchRows.value = data.rows || []
    docSearchTotal.value = Number(data.total || 0)
    // 跨页保留勾选：本页命中已选文档时自动勾上
    await nextTick()
    const picked = new Set(dialogSelection.value.map(Number))
    for (const row of docSearchRows.value) {
      if (picked.has(Number(row.materialId))) docTableRef.value?.toggleRowSelection(row, true)
    }
  } catch (e) {
    ElMessage.error(e?.message || '文档搜索失败')
  } finally {
    docSearchLoading.value = false
  }
}
function resetDocSearch() {
  docSearch.value = { grade: null, semester: '', lessonNum: null, keyword: '', pageNum: 1, pageSize: 10 }
  searchDocs()
}
function onDocSelectionChange(selection) {
  dialogSelection.value = (selection || []).map(item => Number(item.materialId))
}
async function confirmDocs() {
  const ids = [...new Set(dialogSelection.value.map(Number))]
  if (ids.length > classStudents.value.length && !isBatchMode.value) {
    ElMessage.warning('文档数量不能超过本班学生人数')
    return
  }
  if (!(await confirmDiscardChanges())) return
  applyDocSelection(ids)
  docDialogVisible.value = false
  autoSplit(false)
}
function applyDocSelection(ids) {
  // 已选文档对象优先保留弹窗行（带年级学期与大小），回显时用候选列表补齐。
  const byId = new Map()
  for (const row of docSearchRows.value) byId.set(Number(row.materialId), row)
  for (const doc of selectedDocs.value) byId.set(Number(doc.materialId), doc)
  for (const item of workspace.candidates) {
    if (!byId.has(Number(item.materialId))) byId.set(Number(item.materialId), item)
  }
  selectedMaterialIds.value = ids
  selectedDocs.value = ids.map(id => byId.get(Number(id))).filter(Boolean)
}
function previewDoc(row) {
  if (row.previewPath && pdfPreviewRef.value) {
    const baseUrl = import.meta.env.VITE_APP_BASE_API
    pdfPreviewRef.value.open(`${baseUrl}/common/resource/view?resource=${encodeURIComponent(row.previewPath)}`)
  } else {
    ElMessage.warning('该文档暂无可预览的 PDF 版本')
  }
}

// 按学号连续均分：前面的组多 1 人，人数差不超过 1。
function splitGroupsFor(students, docIds) {
  const sorted = (students || []).slice().sort((a, b) =>
    String(a.studentNo || '').localeCompare(String(b.studentNo || ''), 'zh-CN', { numeric: true }))
  const count = docIds.length
  const groups = Array.from({ length: count }, (_, index) => ({
    studentIds: [],
    materialId: Number(docIds[index])
  }))
  sorted.forEach((student, index) => {
    const target = Math.min(Math.floor((index * count) / sorted.length), count - 1)
    groups[target].studentIds.push(Number(student.studentId))
  })
  return groups
}
function autoSplit(manual) {
  const students = classStudents.value
  const count = selectedMaterialIds.value.length
  if (!students.length) {
    if (manual) ElMessage.warning('本班暂无学生名单')
    return
  }
  if (!count) {
    if (manual) ElMessage.warning('请先选择协作文档')
    return
  }
  editGroups.value = splitGroupsFor(students, selectedMaterialIds.value)
  moveTarget.value = {}
  moveStudent.value = {}
}

// 批量建组：每个选中班级独立均分、独立保存，互不影响；已开始的班级跳过。
async function batchSave() {
  if (!selectedMaterialIds.value.length) {
    ElMessage.warning('请先选择协作文档')
    return
  }
  saving.value = true
  saveError.value = ''
  const done = []
  const skipped = []
  const failed = []
  try {
    for (const item of selectedClassItems.value) {
      if (item.current?.frozenTime) {
        skipped.push(`${item.entryYear}级${item.classCode}班`)
        continue
      }
      const groups = splitGroupsFor(item.students || [], selectedMaterialIds.value)
      const payload = {
        requestId: newRequestId(),
        entryYear: item.entryYear,
        classCode: item.classCode,
        materialIds: selectedMaterialIds.value.map(Number),
        groups: groups.map(group => ({ studentIds: group.studentIds, materialId: group.materialId })),
        currentActivityId: item.current?.activityId || null,
        newRound: false
      }
      try {
        await saveCollaborationWorkspace(lessonId.value, payload)
        done.push(`${item.entryYear}级${item.classCode}班`)
      } catch (e) {
        failed.push(`${item.entryYear}级${item.classCode}班（${e?.message || '保存失败'}）`)
      }
    }
    if (done.length) ElMessage.success(`已生成：${done.join('、')}`)
    if (skipped.length) ElMessage.warning(`已跳过进行中的班级：${skipped.join('、')}`)
    if (failed.length) {
      saveError.value = `失败：${failed.join('、')}`
      ElMessage.error(saveError.value)
    }
    await load()
  } finally {
    saving.value = false
  }
}

function removeMember(groupIndex, studentId) {
  const group = editGroups.value[groupIndex]
  if (!group) return
  group.studentIds = group.studentIds.filter(id => Number(id) !== Number(studentId))
}

function assignMember(studentId, groupIndex) {
  if (groupIndex === null || groupIndex === undefined || groupIndex === '') return
  const group = editGroups.value[Number(groupIndex)]
  if (!group) return
  if (!group.studentIds.map(Number).includes(Number(studentId))) group.studentIds.push(Number(studentId))
}

function moveOptions(groupIndex) {
  return editGroups.value.map((_, index) => index).filter(index => index !== groupIndex)
}

function moveMember(groupIndex, targetIndex) {
  const studentId = moveStudent.value[groupIndex]
  if (targetIndex === null || targetIndex === undefined || targetIndex === '') return
  if (!studentId) {
    ElMessage.warning('请先选择要移动的学生')
    return
  }
  removeMember(groupIndex, studentId)
  assignMember(studentId, targetIndex)
  moveTarget.value[groupIndex] = null
  moveStudent.value[groupIndex] = null
}

function validateBeforeSave() {
  if (!activeClass.value) return '请先选择协作班级'
  if (!selectedMaterialIds.value.length) return '请先选择协作文档'
  if (editGroups.value.length !== selectedMaterialIds.value.length) return '小组数量必须与所选文档数量一致，请重新均分'
  if (unassigned.value.length) return `还有 ${unassigned.value.length} 名学生未分配到小组`
  const seen = new Set()
  for (const group of editGroups.value) {
    if (!group.materialId) return '每个小组都要选择任务文档'
    if (!selectedMaterialIds.value.map(Number).includes(Number(group.materialId))) return '各组只能使用上方已选的文档'
    if (!group.studentIds.length) return '每个小组至少需要一名学生'
    for (const studentId of group.studentIds) {
      if (seen.has(Number(studentId))) return '每名学生只能属于一个小组'
      seen.add(Number(studentId))
    }
  }
  if (seen.size !== classStudents.value.length) return '分组必须覆盖本班全部学生'
  return ''
}

async function save() {
  const message = validateBeforeSave()
  if (message) {
    saveError.value = message
    ElMessage.warning(message)
    return
  }
  const detail = currentDetail.value
  if (detail?.frozenTime && !isNewRound.value) {
    saveError.value = '学生已经开始协作，请新建一轮以保留已有作品'
    ElMessage.warning(saveError.value)
    return
  }
  saving.value = true
  saveError.value = ''
  try {
    const payload = {
      requestId: requestId.value,
      entryYear: activeClass.value.entryYear,
      classCode: activeClass.value.classCode,
      materialIds: selectedMaterialIds.value.map(Number),
      groups: editGroups.value.map(group => ({ studentIds: group.studentIds.map(Number), materialId: Number(group.materialId) })),
      currentActivityId: detail?.activityId || null,
      newRound: isNewRound.value
    }
    const response = await saveCollaborationWorkspace(lessonId.value, payload)
    ElMessage.success(isNewRound.value ? '新一轮协作已创建，旧作品已保留' : '协作分组已保存')
    await load()
  } catch (e) {
    saveError.value = e?.message || '保存失败'
    ElMessage.error(saveError.value)
  } finally {
    saving.value = false
  }
}

function startNewRound() {
  // 新建一轮以当前分组为起点，教师调整后保存；旧轮次与作品保留。
  isNewRound.value = true
  requestId.value = newRequestId()
  ElMessage.info('已进入新建一轮模式：调整分组与文档后点击保存')
}

function detailGroupName(detail, snapshotGroupId) {
  return (detail?.groups || []).find(group => Number(group.snapshotGroupId) === Number(snapshotGroupId))?.groupName
    || `组 ${snapshotGroupId}`
}

function detailMemberText(detail, snapshotGroupId) {
  const list = (detail?.members || [])
    .filter(member => Number(member.snapshotGroupId) === Number(snapshotGroupId))
    .map(member => `${member.studentNo || ''} ${member.studentName || ''}`.trim())
    .filter(Boolean)
  return list.length ? list.join('、') : '—'
}

function openRoom(roomId) {
  router.push(`/business/collaboration/editor/${roomId}`)
}

async function openActivity(row) {
  const response = await getCollaborationActivityDetail(row.activityId)
  activityDetail.value = response.data || response || null
  timeline.value = []
  activityDialogVisible.value = true
}

async function openRevisions(roomId) {
  revisionDialogVisible.value = true
  revisionsLoading.value = true
  revisions.value = []
  try {
    const response = await getCollaborationRevisions(roomId)
    revisions.value = response?.data || response || []
  } catch (e) {
    ElMessage.error(e?.message || '版本历史加载失败')
  } finally {
    revisionsLoading.value = false
  }
}

async function openTimeline(rowOrId) {
  const roomId = typeof rowOrId === 'object' ? rowOrId.roomId : rowOrId
  try {
    const response = await getCollaborationTimeline(roomId)
    timeline.value = response?.data || response || []
    if (!activityDialogVisible.value) {
      activityDetail.value = null
      activityDialogVisible.value = true
    }
  } catch (e) {
    ElMessage.error(e?.message || '轨迹加载失败')
  }
}

function formatFileSize(value) {
  const size = Number(value)
  if (!size && size !== 0) return '—'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(2)} MB`
}

function formatTime(value) {
  return value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '—'
}

onMounted(load)
</script>

<style scoped>
.collaboration-workspace .workspace-header { display: flex; justify-content: space-between; align-items: center; }
.collaboration-workspace .workspace-card { margin-top: 16px; }
.collaboration-workspace .workspace-alert { margin-bottom: 12px; }
.collaboration-workspace .workspace-form { margin-top: 12px; }
.collaboration-workspace .header-actions { display: flex; align-items: center; gap: 8px; }
.collaboration-workspace .class-checkbox-group { margin-left: 12px; }
.collaboration-workspace .doc-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 12px; margin-bottom: 12px; }
.collaboration-workspace .doc-card-title { font-weight: 600; margin-bottom: 4px; word-break: break-all; }
.collaboration-workspace .doc-card-question { font-size: 12px; color: #606266; margin: 6px 0; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.collaboration-workspace .doc-search-form { margin-bottom: 12px; }
.collaboration-workspace .doc-pagination { margin-top: 12px; justify-content: flex-end; }
.collaboration-workspace .muted { color: #909399; font-size: 12px; }
.collaboration-workspace .edit-group { border: 1px solid #ebeef5; border-radius: 6px; padding: 12px; margin-top: 12px; }
.collaboration-workspace .edit-group-title { display: flex; align-items: center; gap: 10px; }
.collaboration-workspace .group-doc-select { width: 280px; margin-left: auto; }
.collaboration-workspace .member-tags { margin-top: 10px; display: flex; flex-wrap: wrap; gap: 6px; }
.collaboration-workspace .member-tag { margin: 2px 4px 2px 0; }
.collaboration-workspace .member-move { margin-top: 8px; display: flex; align-items: center; gap: 8px; }
.collaboration-workspace .move-select { width: 150px; }
.collaboration-workspace .unassigned { margin-top: 12px; padding: 10px; background: #fdf6ec; border-radius: 6px; }
.collaboration-workspace .assign-row { display: flex; align-items: center; gap: 10px; margin-top: 6px; }
.collaboration-workspace .workspace-actions { margin-top: 16px; display: flex; align-items: center; gap: 12px; }
.collaboration-workspace .save-error { color: #f56c6c; font-size: 12px; }
</style>
