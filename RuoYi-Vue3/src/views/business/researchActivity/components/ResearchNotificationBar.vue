<template>
  <el-card class="research-notification-bar" shadow="never" v-loading="loading">
    <div class="notification-head">
      <div><el-icon><Bell /></el-icon><strong>教研活动通知</strong><el-badge v-if="summary.unreadCount" :value="summary.unreadCount" /></div>
      <el-button link type="primary" @click="router.push('/business/research-notifications')">查看全部</el-button>
    </div>
    <template v-if="summary.items?.length">
      <button v-for="item in summary.items" :key="item.recipientId" class="notification-item" type="button" @click="openNotification(item)">
        <el-tag type="primary" size="small">通知</el-tag>
        <span class="notification-title">{{ item.topicTitle }}</span>
        <span class="notification-time">{{ item.activityTime ? `活动时间：${item.activityTime}` : item.notifyTime }}</span>
      </button>
    </template>
    <el-empty v-else :image-size="36" description="暂无待办教研活动通知" />
  </el-card>
</template>

<script setup>
import { getResearchNotificationSummary, readResearchNotification } from '@/api/business/researchActivity.js'

const router = useRouter()
const loading = ref(false)
const summary = reactive({ unreadCount: 0, items: [] })

async function load() {
  loading.value = true
  try {
    const response = await getResearchNotificationSummary(5)
    Object.assign(summary, response.data || { unreadCount: 0, items: [] })
  } catch {
    // 通知独立失败，不能影响教师首页其他课程数据。
    Object.assign(summary, { unreadCount: 0, items: [] })
  } finally { loading.value = false }
}
async function openNotification(item) {
  if (item.readFlag !== 'Y') await readResearchNotification(item.recipientId)
  router.push(`/business/research-activity/detail/${item.topicId}`)
}
onMounted(load)
</script>

<style scoped>
.research-notification-bar { margin-bottom: 16px; }
.notification-head, .notification-head > div, .notification-item { display: flex; align-items: center; gap: 9px; }
.notification-head { justify-content: space-between; margin-bottom: 8px; }
.notification-item { width: 100%; padding: 9px 4px; border: 0; border-top: 1px solid var(--el-border-color-lighter); background: transparent; cursor: pointer; text-align: left; }
.notification-item:hover { background: var(--el-fill-color-light); }
.notification-title { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.notification-time { color: var(--el-text-color-secondary); font-size: 12px; }
.research-notification-bar :deep(.el-empty) { padding: 4px 0; }
</style>
