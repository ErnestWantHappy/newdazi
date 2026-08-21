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
import { getCollaborationLesson, getCollaborationRevisions, saveCollaborationLesson } from '@/api/business/collaboration'

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
}
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
.member-card { margin-top: 16px; }
.member-header { display: flex; justify-content: space-between; align-items: center; }
.revision-alert { margin-bottom: 12px; }
</style>
