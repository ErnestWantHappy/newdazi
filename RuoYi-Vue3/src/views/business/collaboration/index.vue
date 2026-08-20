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
        <el-table-column label="操作" width="120"><template #default="{ row }"><el-button link type="primary" :disabled="row.status === 'CLOSED'" @click="openRoom(row)">进入房间</el-button></template></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCollaborationLesson, saveCollaborationLesson } from '@/api/business/collaboration'

const route = useRoute()
const router = useRouter()
const saving = ref(false)
const health = reactive({ ready: false })
const candidates = ref([])
const rooms = ref([])
const form = reactive({ enabled: false, questionId: null, materialId: null })
const lessonId = computed(() => route.params.lessonId)
const filteredMaterials = computed(() => candidates.value.filter(item => item.questionId === form.questionId))

async function load() {
  const data = await getCollaborationLesson(lessonId.value)
  Object.assign(health, data.data?.health || data.health || {})
  candidates.value = data.data?.candidates || data.candidates || []
  rooms.value = data.data?.rooms || data.rooms || []
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
onMounted(load)
</script>

<style scoped>
.header-row { display: flex; justify-content: space-between; align-items: center; }
.settings-form { max-width: 760px; margin-top: 24px; }
.option-file { float: right; color: #909399; }
.room-card { margin-top: 16px; }
</style>
