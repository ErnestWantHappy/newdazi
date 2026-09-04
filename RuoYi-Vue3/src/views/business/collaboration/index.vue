<template>
  <div class="app-container collaboration-settings">
    <el-card shadow="never">
      <template #header>
        <div class="header-row"><span>课程在线协作</span><el-tag :type="health.ready ? 'success' : 'warning'">{{ health.ready ? '服务已就绪' : '需要配置' }}</el-tag></div>
      </template>
      <el-alert v-if="!health.ready" type="warning" :closable="false" show-icon title="CryptPad 尚未就绪" description="请管理员先完成服务器配置，未就绪时不会创建协作房间。" />
      <el-form label-width="120px" class="settings-form">
        <el-form-item label="操作题起始文件">
          <el-select v-model="form.questionId" placeholder="请选择操作题文件" clearable style="width: 100%" @change="syncMaterial">
            <el-option v-for="item in candidates" :key="item.materialId" :label="`${item.questionContent || '操作题'} · ${item.fileName}`" :value="item.questionId">
              <span>{{ item.questionContent || '操作题' }}</span><span class="option-file">{{ item.fileName }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="文件副本">
          <el-select v-model="form.materialId" placeholder="请先选择操作题" style="width: 100%">
            <el-option v-for="item in filteredMaterials" :key="item.materialId" :label="item.fileName" :value="item.materialId" />
          </el-select>
        </el-form-item>
        <el-form-item label="协作开关">
          <el-switch v-model="form.enabled" active-text="开启后每个授课班独立一份文档" inactive-text="关闭并保留历史版本" />
        </el-form-item>
        <el-form-item><el-button type="primary" :loading="saving" @click="submit">保存协作设置</el-button><el-button @click="load">刷新</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never" class="room-card">
      <template #header><span>班级房间</span></template>
      <el-table :data="rooms" stripe empty-text="尚未创建班级房间">
        <el-table-column prop="entryYear" label="届别" width="100" />
        <el-table-column prop="classCode" label="班级" width="100" />
        <el-table-column prop="fileName" label="文件" min-width="180" />
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="status" label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status === 'OPEN' ? 'success' : 'info'">{{ row.status === 'OPEN' ? '开放' : '关闭' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="row.status === 'CLOSED'" @click="openRoom(row)">进入房间</el-button>
            <el-button link type="info" @click="openRevisions(row)">版本历史</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="revisionDialogVisible" title="版本历史" width="760px" destroy-on-close>
      <el-alert type="info" :closable="false" show-icon title="每次保存会生成一个不可变版本，版本号递增；当前文档内容为最新版本。" class="revision-alert" />
      <el-table v-loading="revisionsLoading" :data="revisions" stripe size="small" empty-text="暂无版本记录">
        <el-table-column label="版本" width="80" align="center"><template #default="{ row }"><el-tag size="small">v{{ row.versionNo }}</el-tag></template></el-table-column>
        <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column label="大小" width="100" align="right"><template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template></el-table-column>
        <el-table-column label="保存人" width="130"><template #default="{ row }">{{ row.savedByName || row.savedByUserId || '—' }}</template></el-table-column>
        <el-table-column label="保存时间" width="170"><template #default="{ row }">{{ formatTime(row.createTime) }}</template></el-table-column>
      </el-table>
    </el-dialog>
    <el-card shadow="never" class="activity-card">
      <template #header><div class="member-header"><span>非计分小组协作</span><el-button link type="primary" @click="prepareActivities">刷新分组快照</el-button></div></template>
      <el-alert type="info" :closable="false" show-icon title="每个小组拥有独立文档，不写入个人答案或成绩。版本列表中的保存人仅表示触发保存者，不表示全部内容作者。" class="revision-alert" />
      <el-form label-width="100px" class="settings-form">
        <el-form-item label="课时分组快照"><el-select v-model="activityForm.snapshotId" placeholder="请先在班级分组中冻结课时快照" style="width:100%" @change="syncGroupTasks"><el-option v-for="item in activitySetup.snapshots" :key="item.snapshotId" :label="`${item.entryYear}级${item.classCode}班 · ${formatTime(item.frozenTime)}`" :value="item.snapshotId" /></el-select></el-form-item>
        <el-form-item label="活动名称"><el-input v-model="activityForm.activityTitle" maxlength="120" /></el-form-item>
        <el-form-item v-for="group in selectedSnapshotGroups" :key="group.snapshotGroupId" :label="group.groupName"><el-select v-model="activityForm.groupTasks[group.snapshotGroupId]" placeholder="选择该组起始文件" style="width:100%"><el-option v-for="item in activitySetup.candidates" :key="`${group.snapshotGroupId}-${item.materialId}`" :label="item.fileName" :value="item.materialId" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" :disabled="!selectedSnapshotGroups.length" :loading="activitySaving" @click="createActivity">创建小组协作活动</el-button></el-form-item>
      </el-form>
      <el-table :data="activities" size="small" stripe empty-text="尚未创建小组协作活动"><el-table-column prop="activityTitle" label="活动" min-width="180" /><el-table-column prop="entryYear" label="届别" width="80" /><el-table-column prop="classCode" label="班级" width="80" /><el-table-column prop="frozenTime" label="首名学生进入后冻结" min-width="170"><template #default="{ row }">{{ formatTime(row.frozenTime) }}</template></el-table-column><el-table-column label="操作" width="90"><template #default="{ row }"><el-button link type="primary" @click="openActivity(row)">小组与轨迹</el-button></template></el-table-column></el-table>
    </el-card>
    <el-dialog v-model="activityDialogVisible" :title="activityDetail?.activityTitle || '小组协作轨迹'" width="820px"><el-table :data="activityDetail?.groupTasks || []" size="small" stripe><el-table-column prop="snapshotGroupId" label="快照组" width="100"/><el-table-column prop="versionName" label="起始文件" min-width="220"/><el-table-column prop="roomId" label="房间" width="90"/><el-table-column label="轨迹" width="100"><template #default="{ row }"><el-button link type="primary" @click="openTimeline(row)">查看</el-button></template></el-table-column></el-table><el-divider>操作轨迹</el-divider><el-timeline v-if="timeline.length"><el-timeline-item v-for="item in timeline" :key="item.eventId" :timestamp="formatTime(item.createTime)">{{ item.actorName || item.userId }} · {{ item.eventType }}<span v-if="item.eventDetail">（{{ item.eventDetail }}）</span></el-timeline-item></el-timeline><el-empty v-else description="选择小组查看操作轨迹" /></el-dialog>
    <el-card shadow="never" class="member-card">
      <template #header><div class="member-header"><span>课程学生成员</span><el-tag type="info">{{ members.length }}人</el-tag></div></template>
      <el-table :data="members" stripe empty-text="当前课程暂无学生成员">
        <el-table-column prop="studentNo" label="学号" width="100" />
        <el-table-column prop="studentName" label="姓名" width="140" />
        <el-table-column prop="classCode" label="班级" width="90" />
        <el-table-column label="账号状态" width="120"><template #default="{ row }"><el-tag :type="row.loginBound ? 'success' : 'info'">{{ row.loginBound ? '已绑定登录' : '未绑定登录' }}</el-tag></template></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createCollaborationActivity, getCollaborationActivities, getCollaborationActivitySetup, getCollaborationLesson, getCollaborationRevisions, getCollaborationTimeline, saveCollaborationLesson } from '@/api/business/collaboration'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const saving = ref(false)
const health = reactive({ ready: false })
const candidates = ref([])
const rooms = ref([])
const members = ref([])
const form = reactive({ enabled: false, questionId: null, materialId: null })
const lessonId = computed(() => route.params.lessonId)
const filteredMaterials = computed(() => candidates.value.filter(item => item.questionId === form.questionId))

const revisionDialogVisible = ref(false)
const revisionsLoading = ref(false)
const revisions = ref([])
const activitySaving = ref(false)
const activitySetup = reactive({ snapshots: [], candidates: [] })
const activityForm = reactive({ snapshotId: null, activityTitle: '', groupTasks: {} })
const activities = ref([])
const activityDialogVisible = ref(false)
const activityDetail = ref(null)
const timeline = ref([])
const selectedSnapshotGroups = computed(() => activitySetup.snapshots.find(item => item.snapshotId === activityForm.snapshotId)?.groups || [])

async function load() {
  const data = await getCollaborationLesson(lessonId.value)
  Object.assign(health, data.data?.health || data.health || {})
  candidates.value = data.data?.candidates || data.candidates || []
  rooms.value = data.data?.rooms || data.rooms || []
  members.value = data.data?.members || data.members || []
  const payload = data.data || data
  form.enabled = Boolean(payload.enabled)
  form.questionId = payload.questionId || candidates.value[0]?.questionId || null
  form.materialId = payload.materialId || candidates.value.find(item => item.questionId === form.questionId)?.materialId || null
  await Promise.all([prepareActivities(), loadActivities()])
}
async function prepareActivities() {
  const res = await getCollaborationActivitySetup(lessonId.value)
  const data = res.data || res || {}
  activitySetup.snapshots = data.snapshots || []
  activitySetup.candidates = data.candidates || []
}
async function loadActivities() { const res = await getCollaborationActivities(lessonId.value); activities.value = res.data || res || [] }
function syncGroupTasks() {
  const first = activitySetup.candidates[0]?.materialId || null
  const tasks = {}
  selectedSnapshotGroups.value.forEach(group => { tasks[group.snapshotGroupId] = first })
  activityForm.groupTasks = tasks
}
async function createActivity() {
  const tasks = selectedSnapshotGroups.value.map(group => {
    const materialId = activityForm.groupTasks[group.snapshotGroupId]
    const candidate = activitySetup.candidates.find(item => item.materialId === materialId)
    return { snapshotGroupId: group.snapshotGroupId, materialId, questionId: candidate?.questionId, versionName: candidate?.fileName }
  })
  if (tasks.some(item => !item.materialId || !item.questionId)) return ElMessage.warning('请为每个小组选择起始文件')
  activitySaving.value = true
  try { await createCollaborationActivity(lessonId.value, { snapshotId: activityForm.snapshotId, activityTitle: activityForm.activityTitle, entryYear: activitySetup.snapshots.find(item => item.snapshotId === activityForm.snapshotId)?.entryYear, classCode: activitySetup.snapshots.find(item => item.snapshotId === activityForm.snapshotId)?.classCode, groupTasks: tasks }); ElMessage.success('小组协作活动已创建'); await loadActivities() } finally { activitySaving.value = false }
}
async function openActivity(row) { const res = await requestActivity(row.activityId); activityDetail.value = res; timeline.value = []; activityDialogVisible.value = true }
async function requestActivity(activityId) { const res = await request({ url: `/business/collaboration/activity/${activityId}`, method: 'get' }); return res.data || res || {} }
async function openTimeline(row) { const res = await getCollaborationTimeline(row.roomId); timeline.value = res.data || res || [] }
function syncMaterial() { form.materialId = filteredMaterials.value[0]?.materialId || null }
async function submit() {
  saving.value = true
  try { await saveCollaborationLesson(lessonId.value, form); ElMessage.success('协作设置已保存'); await load() } finally { saving.value = false }
}
function openRoom(row) { router.push(`/business/collaboration/editor/${row.roomId}`) }

async function openRevisions(row) {
  revisionDialogVisible.value = true
  revisionsLoading.value = true
  revisions.value = []
  try {
    const res = await getCollaborationRevisions(row.roomId)
    revisions.value = res?.data || res || []
  } catch (e) {
    ElMessage.error(e?.message || '版本历史加载失败')
  } finally {
    revisionsLoading.value = false
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
.header-row { display: flex; justify-content: space-between; align-items: center; }
.settings-form { max-width: 760px; margin-top: 24px; }
.option-file { float: right; color: #909399; }
.room-card { margin-top: 16px; }
.activity-card { margin-top: 16px; }
.member-card { margin-top: 16px; }
.member-header { display: flex; justify-content: space-between; align-items: center; }
.revision-alert { margin-bottom: 12px; }
</style>
