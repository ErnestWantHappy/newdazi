<template>
  <div class="app-container classroom-desktop">
    <div class="desktop-toolbar">
      <div>
        <h2>{{ lessonTitle || `${entryYear}级${classCode}班` }} · 课堂监控大屏</h2>
        <span class="muted">终端在线与作答进度分别统计，在线状态不等同于考勤</span>
      </div>
      <div class="toolbar-actions">
        <el-switch v-model="showGroups" active-text="显示分组" />
        <el-button :icon="Setting" @click="openGroupDialog">设置分组</el-button>
        <el-button :type="layoutMode ? 'primary' : 'default'" :icon="Edit" @click="layoutMode = !layoutMode">调整座位</el-button>
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>
    </div>
    <el-alert v-if="error" type="warning" :closable="false" :title="error" class="mb12" />
    <el-empty v-if="!loading && !students.length" description="当前班级暂无学生" />
    <div v-loading="loading" class="student-grid" :style="showGroups ? undefined : gridStyle">
      <template v-if="showGroups">
        <section v-for="group in groupedStudents" :key="group.key" class="group-row">
          <div class="group-row-title"><span class="group-color" :style="{ backgroundColor: group.color || '#909399' }" />{{ group.name }}<el-tag size="small" type="info">{{ group.students.length }}人</el-tag></div>
          <div class="group-student-grid" :style="gridStyle">
            <el-card v-for="student in group.students" :key="student.studentId" class="student-card" :class="[{ online: student.online, leader: student.leaderStudentId === student.studentId }, student.taskState]" shadow="hover" draggable="true" @dragstart="dragStart(student)" @dragover.prevent @drop="drop(student)">
              <div class="card-head"><span class="status-dot" :class="student.online ? 'is-online' : 'is-offline'" /> <strong>{{ student.studentNo }} {{ student.studentName }}</strong><el-tag size="small" :type="student.online ? 'success' : 'info'">{{ student.online ? '已连接' : '未连接' }}</el-tag><el-tag v-if="student.leaderStudentId === student.studentId" size="small" type="warning">组长</el-tag></div>
              <div class="student-no">学号 {{ student.studentNo }}</div>
              <div v-if="lessonId" class="task-line"><el-tag size="small" :type="taskTagType(student.taskState)">{{ taskLabel(student.taskState) }}</el-tag><span v-if="student.totalQuestionCount > 0">{{ student.startedQuestionCount || 0 }}/{{ student.totalQuestionCount }} 题已开始</span></div>
              <div class="terminal-line">连接 IP：{{ student.connectionIp || '未连接' }}</div><div v-if="student.remark" class="remark-line">备注：{{ student.remark }}</div><div v-if="student.performance?.isAbsent" class="absent-line">本节课已请假</div><div class="score-summary"><div v-if="hasTyping">打字：{{ scoreLabel(student.typing?.score) }} 分 / {{ scoreLabel(student.typing?.speed) }} 字/分 / 正确率 {{ scoreLabel(student.typing?.accuracy) }}%</div><div v-if="hasTheory">理论：{{ scoreLabel(student.theory?.score) }} 分 / 正确率 {{ scoreLabel(student.theory?.accuracy) }}%</div><div v-if="hasPractical">操作题：{{ practicalLabel(student.practical) }}</div><div>课堂表现：{{ student.performance?.score || 0 }} 分<span v-if="student.performance?.reason">（{{ student.performance.reason }}）</span></div></div><div class="card-actions"><el-button size="small" type="primary" plain @click="openPerformance(student)">课堂表现</el-button><el-button size="small" type="info" plain @click="toggleAbsent(student)">{{ student.performance?.isAbsent ? '取消请假' : '请假' }}</el-button></div>
            </el-card>
          </div>
        </section>
      </template>
      <template v-else>
        <el-card v-for="student in orderedStudents" :key="student.studentId" class="student-card" :class="[{ online: student.online }, student.taskState]" shadow="hover" draggable="true" @dragstart="dragStart(student)" @dragover.prevent @drop="drop(student)">
          <div class="card-head"><span class="status-dot" :class="student.online ? 'is-online' : 'is-offline'" /> <strong>{{ student.studentNo }} {{ student.studentName }}</strong><el-tag size="small" :type="student.online ? 'success' : 'info'">{{ student.online ? '已连接' : '未连接' }}</el-tag><el-tag v-if="student.leaderStudentId === student.studentId" size="small" type="warning">组长</el-tag></div>
          <div class="student-no">学号 {{ student.studentNo }}</div>
          <div v-if="lessonId" class="task-line"><el-tag size="small" :type="taskTagType(student.taskState)">{{ taskLabel(student.taskState) }}</el-tag><span v-if="student.totalQuestionCount > 0">{{ student.startedQuestionCount || 0 }}/{{ student.totalQuestionCount }} 题已开始</span></div>
          <div class="terminal-line">连接 IP：{{ student.connectionIp || '未连接' }}</div><div v-if="student.remark" class="remark-line">备注：{{ student.remark }}</div><div v-if="student.performance?.isAbsent" class="absent-line">本节课已请假</div><div class="score-summary"><div v-if="hasTyping">打字：{{ scoreLabel(student.typing?.score) }} 分 / {{ scoreLabel(student.typing?.speed) }} 字/分 / 正确率 {{ scoreLabel(student.typing?.accuracy) }}%</div><div v-if="hasTheory">理论：{{ scoreLabel(student.theory?.score) }} 分 / 正确率 {{ scoreLabel(student.theory?.accuracy) }}%</div><div v-if="hasPractical">操作题：{{ practicalLabel(student.practical) }}</div><div>课堂表现：{{ student.performance?.score || 0 }} 分<span v-if="student.performance?.reason">（{{ student.performance.reason }}）</span></div></div><div class="card-actions"><el-button size="small" type="primary" plain @click="openPerformance(student)">课堂表现</el-button><el-button size="small" type="info" plain @click="toggleAbsent(student)">{{ student.performance?.isAbsent ? '取消请假' : '请假' }}</el-button></div>
        </el-card>
      </template>
    </div>
    <div v-if="layoutMode" class="layout-footer"><span>拖动卡片调整座位，完成后保存个人布局</span><el-button type="primary" size="small" @click="saveLayout">保存布局</el-button></div>

    <el-dialog v-model="groupDialogVisible" title="设置班级分组" width="min(900px, 94vw)" destroy-on-close>
      <el-alert title="同一组的学生将在课堂大屏中显示在同一行；每组可指定一名组长。保存会生成新的分组方案版本。" type="info" :closable="false" class="mb12" />
      <div class="group-tools">
        <el-input v-model="groupForm.schemeName" placeholder="方案名称，例如：机房座位分组" style="width: 220px" maxlength="100" />
        <el-input-number v-model="groupForm.membersPerGroup" :min="1" :max="students.length || 1" controls-position="right" /><span class="muted">每组人数</span>
        <el-select v-model="groupForm.mode" style="width: 150px"><el-option label="按学号交错" value="INTERLEAVE" /><el-option label="按学号连续" value="RANGE" /></el-select>
        <el-button type="primary" :loading="groupLoading" @click="generateGroups">自动生成</el-button>
      </div>
      <el-empty v-if="!groupForm.groups.length" description="请先自动生成分组，或选择已有方案" />
      <div v-for="(group, index) in groupForm.groups" :key="group.key || index" class="group-editor-row">
        <div class="group-editor-title">第{{ index + 1 }}组</div>
        <el-input v-model="group.groupName" placeholder="分组名称" maxlength="100" style="width: 150px" />
        <el-select v-model="group.studentIds" multiple filterable collapse-tags collapse-tags-tooltip placeholder="选择本组学生" class="group-member-select">
          <el-option v-for="student in students" :key="student.studentId" :label="`${student.studentNo} ${student.studentName}`" :value="student.studentId" />
        </el-select>
        <div class="member-names">成员：{{ group.studentIds.map(studentLabel).join('、') || '未选择' }}</div>
        <el-select v-model="group.leaderStudentId" clearable placeholder="选择组长" style="width: 150px">
          <el-option v-for="studentId in group.studentIds" :key="studentId" :label="studentLabel(studentId)" :value="studentId" />
        </el-select>
      </div>
      <template #footer><el-button @click="groupDialogVisible = false">取消</el-button><el-button type="primary" :loading="groupSaving" :disabled="!groupForm.groups.length" @click="saveGroups">保存分组</el-button></template>
    </el-dialog>
    <el-dialog v-model="performanceDialogVisible" title="课堂表现管理" width="420px" destroy-on-close>
      <div v-if="performanceStudent" class="performance-form"><div class="performance-student">{{ performanceStudent.studentNo }} {{ performanceStudent.studentName }}，当前 {{ performanceStudent.performance?.score || 0 }} 分</div><el-radio-group v-model="performanceForm.direction"><el-radio-button label="add">加分</el-radio-button><el-radio-button label="subtract">扣分</el-radio-button></el-radio-group><el-input-number v-model="performanceForm.points" :min="1" :max="10" controls-position="right" /><el-input v-model="performanceForm.reason" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="请输入原因" /></div>
      <template #footer><el-button @click="performanceDialogVisible = false">取消</el-button><el-button type="primary" :loading="performanceSaving" @click="savePerformanceChange">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Edit, Refresh, Setting } from '@element-plus/icons-vue'
import { getClassroomDesktop, getClassroomDesktopOverview, saveClassroomLayout, getClassGroupSchemes, saveClassGroupScheme, generateClassGroupScheme } from '@/api/business/classGrouping'
import { savePerformance } from '@/api/business/classroomPerformance'
import { setStudentAbsent } from '@/api/business/score'

const route = useRoute()
// URL 可能被浏览器历史或外部链接写成 lessonId=undefined；不应让该占位文本触发后端类型转换错误。
const rawLessonId = String(route.query.lessonId || '').trim()
const lessonId = /^\d+$/.test(rawLessonId) && Number(rawLessonId) > 0 ? rawLessonId : ''
const lessonTitle = String(route.query.lessonTitle || '')
const entryYear = String(route.query.entryYear || '')
const classCode = String(route.query.classCode || '')
const loading = ref(false)
const error = ref('')
const students = ref([])
const layout = ref(null)
const layoutMode = ref(false)
const showGroups = ref(false)
const groupDialogVisible = ref(false)
const groupLoading = ref(false)
const groupSaving = ref(false)
const groupForm = ref({ schemeName: '课堂分组', membersPerGroup: 8, mode: 'RANGE', groups: [] })
const dragging = ref(null)
const hasTyping = ref(false)
const hasTheory = ref(false)
const hasPractical = ref(false)
const performanceDialogVisible = ref(false)
const performanceStudent = ref(null)
const performanceSaving = ref(false)
const performanceForm = ref({ direction: 'add', points: 1, reason: '' })
let presenceRefreshTimer

const orderedStudents = computed(() => students.value.slice().sort((a, b) => (a.sortNo ?? 999999) - (b.sortNo ?? 999999)))
const groupedStudents = computed(() => {
  const groups = new Map()
  orderedStudents.value.forEach(student => {
    const key = student.groupId ? `group-${student.groupId}` : 'ungrouped'
    if (!groups.has(key)) groups.set(key, { key, name: student.groupName || '未分组', color: student.groupColor, students: [] })
    groups.get(key).students.push(student)
  })
  return Array.from(groups.values())
})
const gridStyle = computed(() => ({ gridTemplateColumns: `repeat(${layout.value?.columnsCount || 6}, minmax(150px, 1fr))` }))

function loadDesktop(silent = false) {
  if (!entryYear || !classCode) { error.value = '缺少班级参数'; return }
  if (loading.value) return
  if (!silent) loading.value = true
  error.value = ''
  const request = lessonId ? getClassroomDesktopOverview({ lessonId, entryYear, classCode }) : getClassroomDesktop({ entryYear, classCode })
  return request.then(res => {
    students.value = res.data?.students || []
    layout.value = res.data?.layout || null
    hasTyping.value = !!res.data?.hasTyping
    hasTheory.value = !!res.data?.hasTheory
    hasPractical.value = !!res.data?.hasPractical
  }).catch(e => { error.value = e?.msg || '课堂大屏加载失败' }).finally(() => { loading.value = false })
}

function load(silent = false) {
  return loadDesktop(silent)
}
function dragStart(student) { if (layoutMode.value) dragging.value = student }
function drop(target) {
  if (!layoutMode.value || !dragging.value || dragging.value.studentId === target.studentId) return
  const from = students.value.indexOf(dragging.value); const to = students.value.indexOf(target)
  const list = students.value.slice(); const [item] = list.splice(from, 1); list.splice(to, 0, item); students.value = list
  students.value.forEach((s, i) => { s.sortNo = i })
  dragging.value = null
}
function saveLayout() {
  saveClassroomLayout({ entryYear, classCode, columnsCount: layout.value?.columnsCount || 6, items: students.value.map((s, i) => ({ studentId: s.studentId, gridRow: Math.floor(i / (layout.value?.columnsCount || 6)), gridCol: i % (layout.value?.columnsCount || 6), sortNo: i })) }).then(res => { layout.value = res.data?.layout || layout.value; students.value = res.data?.students || students.value; layoutMode.value = false; ElMessage.success('布局已保存') })
}

function studentLabel(studentId) {
  const student = students.value.find(item => Number(item.studentId) === Number(studentId))
  return student ? `${student.studentNo} ${student.studentName}` : String(studentId)
}

function normalizeGroups(groups, members = []) {
  const memberMap = members.reduce((result, item) => {
    const groupId = String(item.groupId)
    if (!result[groupId]) result[groupId] = []
    result[groupId].push(Number(item.studentId))
    return result
  }, {})
  return (groups || []).map((group, index) => ({
    key: `group-${group.groupId || index}-${Date.now()}`,
    groupName: group.groupName || `第${index + 1}组`,
    color: group.color || '#409EFF',
    studentIds: (group.studentIds || memberMap[String(group.groupId)] || []).map(Number),
    leaderStudentId: group.leaderStudentId == null ? null : Number(group.leaderStudentId)
  }))
}

async function openGroupDialog() {
  groupDialogVisible.value = true
  groupLoading.value = true
  try {
    const response = await getClassGroupSchemes({ entryYear, classCode })
    const data = response.data || {}
    const latest = (data.schemes || [])[0]
    if (latest) {
      groupForm.value.schemeName = latest.schemeName || groupForm.value.schemeName
      groupForm.value.groups = normalizeGroups(latest.groups, latest.members)
    } else {
      groupForm.value.groups = []
    }
    groupForm.value.membersPerGroup = Math.min(8, Math.max(1, students.value.length || 1))
  } catch (e) {
    groupForm.value.groups = []
    ElMessage.warning(e?.msg || '分组方案加载失败，请先确认当前班级参数')
  } finally {
    groupLoading.value = false
  }
}

async function generateGroups() {
  if (!groupForm.value.schemeName.trim()) { ElMessage.warning('请输入方案名称'); return }
  groupLoading.value = true
  try {
    const response = await generateClassGroupScheme({ entryYear, classCode, schemeName: groupForm.value.schemeName.trim(), membersPerGroup: groupForm.value.membersPerGroup, mode: groupForm.value.mode })
    const data = response.data || {}
    groupForm.value.groups = normalizeGroups(data.groups, data.members)
  } catch (e) {
    ElMessage.error(e?.msg || '自动生成分组失败')
  } finally {
    groupLoading.value = false
  }
}

async function saveGroups() {
  const groups = groupForm.value.groups.map(group => ({ ...group, studentIds: (group.studentIds || []).map(Number) }))
  const allIds = groups.flatMap(group => group.studentIds)
  if (allIds.length !== students.value.length || new Set(allIds).size !== students.value.length) { ElMessage.warning('请确保每名学生只分配到一个组，且不能遗漏'); return }
  if (groups.some(group => group.leaderStudentId != null && !group.studentIds.includes(Number(group.leaderStudentId)))) { ElMessage.warning('组长必须是本组学生'); return }
  groupSaving.value = true
  try {
    await saveClassGroupScheme({ entryYear, classCode, schemeName: groupForm.value.schemeName.trim(), groups })
    groupDialogVisible.value = false
    showGroups.value = true
    await loadDesktop()
    ElMessage.success('分组已保存')
  } catch (e) {
    ElMessage.error(e?.msg || '分组保存失败')
  } finally {
    groupSaving.value = false
  }
}

function taskLabel(taskState) {
  return ({
    ENTERED: '已进入', WORKING: '作答中', SUBMITTED: '已提交',
    GRADED: '已批改', RETURNED: '待重做', NO_TASK: '暂无任务', NOT_ENTERED: '未进入'
  })[taskState] || '状态同步中'
}

function taskTagType(taskState) {
  return ({ WORKING: 'warning', SUBMITTED: 'success', GRADED: 'info', RETURNED: 'danger' })[taskState] || 'info'
}

function scoreLabel(value) { return value == null ? '未开始' : value }
function practicalLabel(data) {
  if (!data || !data.submittedCount) return '未提交'
  if (data.score == null) return '已提交，待批改'
  return `${data.score} 分`
}
function openPerformance(student) {
  if (student.performance?.isAbsent) { ElMessage.warning('该学生已请假，请先取消请假后再记录课堂表现'); return }
  performanceStudent.value = student
  performanceForm.value = { direction: 'add', points: 1, reason: '' }
  performanceDialogVisible.value = true
}
async function savePerformanceChange() {
  if (!performanceStudent.value || !performanceForm.value.reason.trim()) { ElMessage.warning('请填写课堂表现原因'); return }
  const oldScore = Number(performanceStudent.value.performance?.score || 0)
  const delta = performanceForm.value.direction === 'add' ? performanceForm.value.points : -performanceForm.value.points
  const score = oldScore + delta
  if (score < -10 || score > 10) { ElMessage.warning('课堂表现总分范围为 -10 到 +10'); return }
  performanceSaving.value = true
  try {
    await savePerformance({ studentId: performanceStudent.value.studentId, lessonId: Number(lessonId), score, reason: performanceForm.value.reason.trim(), isAbsent: false })
    performanceStudent.value.performance = { ...(performanceStudent.value.performance || {}), score, reason: performanceForm.value.reason.trim(), isAbsent: false }
    performanceDialogVisible.value = false
    ElMessage.success('课堂表现已保存')
  } catch (e) { ElMessage.error(e?.msg || '课堂表现保存失败') } finally { performanceSaving.value = false }
}
async function toggleAbsent(student) {
  try {
    const next = !student.performance?.isAbsent
    await setStudentAbsent(student.studentId, Number(lessonId), next)
    student.performance = { ...(student.performance || {}), isAbsent: next, score: next ? 0 : (student.performance?.score || 0) }
    ElMessage.success(next ? '已标记请假' : '已取消请假')
  } catch (e) { ElMessage.error(e?.msg || '请假状态保存失败') }
}

onMounted(() => {
  load()
  presenceRefreshTimer = window.setInterval(() => {
    if (document.visibilityState === 'visible' && !layoutMode.value) loadDesktop(true)
  }, 30000)
})
onBeforeUnmount(() => {
  window.clearInterval(presenceRefreshTimer)
})
</script>

<style scoped>
.desktop-toolbar,.card-head,.layout-footer,.group-tools,.group-row-title{display:flex;align-items:center;justify-content:space-between;gap:12px}.desktop-toolbar{margin-bottom:16px}.desktop-toolbar h2{margin:0 0 4px;font-size:20px}.muted,.student-no,.terminal-line,.task-line span{color:#909399;font-size:12px}.toolbar-actions{display:flex;gap:8px;align-items:center}.student-grid{display:grid;gap:12px}.group-row{border:1px solid #ebeef5;border-radius:6px;padding:12px;background:#fafafa}.group-row-title{justify-content:flex-start;margin-bottom:10px;font-weight:600}.group-student-grid{display:grid;gap:12px}.student-card{min-height:150px;border-top:3px solid #dcdfe6}.student-card.online{border-top-color:#67c23a}.student-card.leader{box-shadow:0 0 0 1px #e6a23c inset}.status-dot{width:8px;height:8px;border-radius:50%;background:#c0c4cc}.status-dot.is-online{background:#67c23a}.card-head{justify-content:flex-start}.card-head .el-tag{margin-left:auto}.task-line{margin:12px 0 8px;display:flex;align-items:center;gap:6px}.group-color{width:10px;height:10px;border-radius:2px}.layout-footer{position:sticky;bottom:0;background:#fff;border-top:1px solid #ebeef5;padding:12px 0;margin-top:16px}.group-tools{justify-content:flex-start;flex-wrap:wrap;margin-bottom:12px}.group-editor-row{display:flex;align-items:center;gap:10px;padding:10px 0;border-bottom:1px solid #ebeef5}.group-editor-title{width:56px;font-weight:600}.group-member-select{flex:1;min-width:220px}.mb12{margin-bottom:12px}.remark-line{margin-top:6px;color:#606266;font-size:12px;white-space:pre-wrap}.absent-line{margin-top:6px;color:#e6a23c;font-size:12px}.score-summary{margin-top:8px;color:#606266;font-size:12px;line-height:1.7}.card-actions{display:flex;gap:6px;margin-top:8px}.member-names{width:100%;font-size:12px;color:#606266;line-height:1.5}.performance-form{display:flex;flex-direction:column;gap:14px}.performance-student{font-weight:600}@media(max-width:900px){.desktop-toolbar{align-items:flex-start;flex-direction:column}.toolbar-actions{flex-wrap:wrap}.student-grid{grid-template-columns:repeat(2,minmax(140px,1fr))!important}.group-editor-row{align-items:stretch;flex-wrap:wrap}.group-member-select{min-width:100%}}
</style>
