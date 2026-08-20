<template>
  <div class="app-container platform-update-page">
    <section class="page-header">
      <div>
        <p>PLATFORM RELEASE NOTES</p>
        <h2>平台更新</h2>
        <span>查看已发布的功能优化与问题修复记录。</span>
      </div>
      <el-button v-if="isAdmin" type="primary" icon="Plus" @click="openCreate">新增记录</el-button>
    </section>

    <el-card shadow="never" class="search-card">
      <el-input v-model="keyword" clearable placeholder="搜索版本号或更新标题" @keyup.enter="reload">
        <template #append><el-button icon="Search" @click="reload">查询</el-button></template>
      </el-input>
    </el-card>

    <el-card shadow="never" v-loading="loading" class="timeline-card">
      <el-timeline v-if="rows.length" class="update-timeline">
        <el-timeline-item v-for="item in rows" :key="item.updateId" :timestamp="formatTime(item.publishedAt)" placement="top" type="primary" hollow>
          <article class="update-item">
            <div class="update-title"><el-tag effect="dark">v{{ item.versionNo }}</el-tag><h3>{{ item.title }}</h3></div>
            <p class="update-content">{{ item.content }}</p>
            <div v-if="isAdmin" class="update-actions">
              <el-tag v-if="item.status !== 'PUBLISHED'" :type="statusMeta(item.status).type">{{ statusMeta(item.status).label }}</el-tag>
              <el-button link type="primary" icon="Edit" @click="openEdit(item)">编辑</el-button>
              <el-button v-if="item.status !== 'PUBLISHED'" link type="success" @click="changeStatus(item, 'PUBLISHED')">发布</el-button>
              <el-button v-if="item.status === 'PUBLISHED'" link type="warning" @click="changeStatus(item, 'WITHDRAWN')">撤回</el-button>
              <el-button v-if="item.status === 'WITHDRAWN'" link @click="changeStatus(item, 'DRAFT')">转草稿</el-button>
            </div>
          </article>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无已发布的平台更新记录" />
      <pagination v-show="total > 0" :total="total" v-model:page="page.pageNum" v-model:limit="page.pageSize" @pagination="loadRows" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.updateId ? '编辑平台更新' : '新增平台更新'" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="平台版本" prop="versionNo"><el-input v-model="form.versionNo" placeholder="例如 1.8.0" maxlength="30" /></el-form-item>
        <el-form-item label="更新标题" prop="title"><el-input v-model="form.title" maxlength="100" show-word-limit /></el-form-item>
        <el-form-item label="实际时间" prop="publishedAt"><el-date-picker v-model="form.publishedAt" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" format="YYYY-MM-DD HH:mm" style="width:100%" /></el-form-item>
        <el-form-item label="更新说明" prop="content"><el-input v-model="form.content" type="textarea" :rows="7" maxlength="4000" show-word-limit placeholder="每行一项，面向教师和教研员说明本次已完成的更新。" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="submitForm">保存为草稿</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="PlatformUpdate">
import { computed, getCurrentInstance, onMounted, reactive, ref } from 'vue'
import useUserStore from '@/store/modules/user'
import { addPlatformUpdate, listPlatformUpdateManagement, listPlatformUpdates, updatePlatformUpdate, updatePlatformUpdateStatus } from '@/api/business/platformUpdate'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.roles.includes('admin'))
const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const rows = ref([])
const total = ref(0)
const page = reactive({ pageNum: 1, pageSize: 10 })
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive(emptyForm())
const rules = {
  versionNo: [{ required: true, pattern: /^\d+\.\d+\.\d+$/, message: '版本号格式为 1.0.0', trigger: 'blur' }],
  title: [{ required: true, message: '请输入更新标题', trigger: 'blur' }],
  publishedAt: [{ required: true, message: '请选择实际更新时间', trigger: 'change' }],
  content: [{ required: true, message: '请输入更新说明', trigger: 'blur' }]
}

function emptyForm() { return { updateId: null, versionNo: '', title: '', content: '', publishedAt: '' } }
function formatTime(value) { return value ? proxy.parseTime(value, '{y}-{m}-{d} {h}:{i}') : '--' }
function statusMeta(status) { return { DRAFT: { label: '草稿', type: 'info' }, WITHDRAWN: { label: '已撤回', type: 'warning' } }[status] || { label: '已发布', type: 'success' } }

async function loadRows() {
  loading.value = true
  try {
    const params = { pageNum: page.pageNum, pageSize: page.pageSize }
    const res = isAdmin.value
      ? await listPlatformUpdateManagement({ ...params, title: keyword.value || undefined })
      : await listPlatformUpdates({ ...params, keyword: keyword.value || undefined })
    rows.value = res.rows || []
    total.value = res.total || 0
  } finally { loading.value = false }
}
function reload() { page.pageNum = 1; loadRows() }
function openCreate() { Object.assign(form, emptyForm()); dialogVisible.value = true }
function openEdit(item) {
  Object.assign(form, { updateId: item.updateId, versionNo: item.versionNo, title: item.title, content: item.content, publishedAt: item.publishedAt })
  dialogVisible.value = true
}
async function submitForm() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.updateId) await updatePlatformUpdate({ ...form })
    else await addPlatformUpdate({ ...form })
    proxy.$modal.msgSuccess('记录已保存为草稿')
    dialogVisible.value = false
    await loadRows()
  } finally { saving.value = false }
}
async function changeStatus(item, status) {
  const text = { PUBLISHED: '发布', WITHDRAWN: '撤回', DRAFT: '转为草稿' }[status]
  await proxy.$modal.confirm(`确认${text} v${item.versionNo} 的更新记录吗？`)
  await updatePlatformUpdateStatus(item.updateId, status)
  proxy.$modal.msgSuccess(`记录已${text}`)
  await loadRows()
}
onMounted(loadRows)
</script>

<style scoped>
.platform-update-page { max-width: 1080px; margin: 0 auto; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; margin: 4px 0 22px; }
.page-header p { margin: 0 0 6px; color: #409eff; font-size: 12px; font-weight: 700; letter-spacing: 0; }
.page-header h2 { margin: 0 0 8px; font-size: 26px; }
.page-header span { color: #606266; }
.search-card { margin-bottom: 14px; }
.search-card :deep(.el-input) { max-width: 420px; }
.timeline-card { min-height: 320px; }
.update-timeline { padding: 10px 12px 0; }
.update-item { padding: 2px 0 14px; }
.update-title { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.update-title h3 { margin: 0; font-size: 17px; color: #303133; }
.update-content { margin: 10px 0 0; color: #606266; line-height: 1.8; white-space: pre-line; }
.update-actions { display: flex; align-items: center; gap: 8px; margin-top: 10px; }
@media (max-width: 600px) { .page-header { align-items: stretch; flex-direction: column; } .page-header .el-button { align-self: flex-start; } .search-card :deep(.el-input) { max-width: none; } .update-timeline { padding-left: 0; } }
</style>
