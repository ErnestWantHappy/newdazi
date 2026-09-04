<template>
  <div class="app-container notification-page">
    <div class="notification-page__head"><h2>全部教研活动通知</h2><el-button :disabled="!unreadCount" @click="readAll">全部标记已读</el-button></div>
    <el-table v-loading="loading" :data="rows" @row-click="open">
      <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="row.readFlag === 'Y' ? 'info' : 'success'">{{ row.readFlag === 'Y' ? '已读' : '未读' }}</el-tag></template></el-table-column>
      <el-table-column prop="topicTitle" label="主题" min-width="300" show-overflow-tooltip />
      <el-table-column prop="creatorName" label="发布人" width="130" />
      <el-table-column label="活动时间" width="180"><template #default="{ row }">{{ row.activityTime || '未设置' }}</template></el-table-column>
      <el-table-column prop="notifyTime" label="通知时间" width="180" />
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="query.pageNum" v-model:limit="query.pageSize" @pagination="load" />
  </div>
</template>

<script setup name="ResearchActivityNotifications">
import { ElMessage } from 'element-plus'
import { listResearchNotifications, readAllResearchNotifications, readResearchNotification } from '@/api/business/researchActivity.js'

const router = useRouter()
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 20 })
const unreadCount = computed(() => rows.value.filter(item => item.readFlag !== 'Y').length)
async function load() { loading.value = true; try { const response = await listResearchNotifications(query); rows.value = response.rows || []; total.value = response.total || 0 } finally { loading.value = false } }
async function open(row) { if (row.readFlag !== 'Y') await readResearchNotification(row.recipientId); router.push(`/business/research-activity/detail/${row.topicId}`) }
async function readAll() { const response = await readAllResearchNotifications(); ElMessage.success(`已标记 ${response.data || 0} 条通知`); load() }
onMounted(load)
</script>

<style scoped>.notification-page { max-width: 1100px; margin: 0 auto; }.notification-page__head { display: flex; justify-content: space-between; align-items: center; }.notification-page :deep(.el-table__row) { cursor: pointer; }</style>
