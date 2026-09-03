<template>
  <div class="app-container classroom-desktop">
    <div class="desktop-toolbar">
      <div><h2>{{ entryYear }}级{{ classCode }}班学生桌面</h2><span class="muted">终端监控 · 在线状态不等同于考勤</span></div>
      <div class="toolbar-actions">
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button label="terminal">终端视图</el-radio-button>
          <el-radio-button label="task">作业状态</el-radio-button>
        </el-radio-group>
        <el-button :type="layoutMode ? 'primary' : 'default'" :icon="Edit" @click="layoutMode = !layoutMode">调整座位</el-button>
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>
    </div>
    <el-alert v-if="error" type="warning" :closable="false" :title="error" class="mb12" />
    <el-empty v-if="!loading && !students.length" description="当前班级暂无学生" />
    <div v-loading="loading" class="student-grid" :style="gridStyle">
      <el-card v-for="student in orderedStudents" :key="student.studentId" class="student-card" :class="{ online: student.online, leader: student.leaderStudentId === student.studentId }" shadow="hover" draggable="true" @dragstart="dragStart(student)" @dragover.prevent @drop="drop(student)">
        <div class="card-head"><span class="status-dot" :class="student.online ? 'is-online' : 'is-offline'" /> <strong>{{ student.studentName }}</strong><el-tag v-if="student.leaderStudentId === student.studentId" size="small" type="warning">组长</el-tag></div>
        <div class="student-no">学号 {{ student.studentNo }}</div>
        <div class="group-line"><span class="group-color" :style="{ backgroundColor: student.groupColor || '#909399' }" />{{ student.groupName || '未分组' }}</div>
        <div v-if="viewMode === 'terminal'" class="terminal-line">{{ student.connectionIp || '未连接' }}</div>
        <div v-else class="terminal-line">{{ student.taskStateLabel || '未进入' }}</div>
      </el-card>
    </div>
    <div v-if="layoutMode" class="layout-footer"><span>拖动卡片调整座位，完成后保存个人布局</span><el-button type="primary" size="small" @click="saveLayout">保存布局</el-button></div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Edit, Refresh } from '@element-plus/icons-vue'
import { getClassroomDesktop, saveClassroomLayout } from '@/api/business/classGrouping'

const route = useRoute()
const entryYear = String(route.query.entryYear || '')
const classCode = String(route.query.classCode || '')
const loading = ref(false)
const error = ref('')
const students = ref([])
const layout = ref(null)
const layoutMode = ref(false)
const viewMode = ref('terminal')
const dragging = ref(null)

const orderedStudents = computed(() => students.value.slice().sort((a, b) => (a.sortNo ?? 999999) - (b.sortNo ?? 999999)))
const gridStyle = computed(() => ({ gridTemplateColumns: `repeat(${layout.value?.columnsCount || 6}, minmax(150px, 1fr))` }))

function load() {
  if (!entryYear || !classCode) { error.value = '缺少班级参数'; return }
  loading.value = true; error.value = ''
  getClassroomDesktop({ entryYear, classCode }).then(res => { students.value = res.data?.students || []; layout.value = res.data?.layout || null }).catch(e => { error.value = e?.msg || '学生桌面加载失败' }).finally(() => { loading.value = false })
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
onMounted(load)
</script>

<style scoped>
.desktop-toolbar,.card-head,.layout-footer{display:flex;align-items:center;justify-content:space-between;gap:12px}.desktop-toolbar{margin-bottom:16px}.desktop-toolbar h2{margin:0 0 4px;font-size:20px}.muted,.student-no,.terminal-line{color:#909399;font-size:12px}.toolbar-actions{display:flex;gap:8px;align-items:center}.student-grid{display:grid;gap:12px}.student-card{min-height:132px;border-top:3px solid #dcdfe6}.student-card.online{border-top-color:#67c23a}.student-card.leader{box-shadow:0 0 0 1px #e6a23c inset}.status-dot{width:8px;height:8px;border-radius:50%;background:#c0c4cc}.status-dot.is-online{background:#67c23a}.card-head{justify-content:flex-start}.card-head .el-tag{margin-left:auto}.group-line{margin:14px 0 8px;display:flex;align-items:center;gap:6px}.group-color{width:10px;height:10px;border-radius:2px}.layout-footer{position:sticky;bottom:0;background:#fff;border-top:1px solid #ebeef5;padding:12px 0;margin-top:16px}.mb12{margin-bottom:12px}@media(max-width:900px){.desktop-toolbar{align-items:flex-start;flex-direction:column}.toolbar-actions{flex-wrap:wrap}.student-grid{grid-template-columns:repeat(2,minmax(140px,1fr))!important}}
</style>
