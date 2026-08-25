<template>
  <div class="student-iot-page">
    <div class="page-heading">
      <div>
        <p class="eyebrow">IOT CLASSROOM</p>
        <h2>物联网实验工作台</h2>
        <p class="muted">在 Mind+ 的 SIoT 模块中连接 EMQX MQTT 服务，使用掌控板完成编程；本页面提供本组 Topic、课堂连接参数与本组收到的数据。</p>
      </div>
      <div class="heading-actions">
        <el-button text type="primary" @click="goHome">返回学生首页</el-button>
        <el-button icon="Refresh" :loading="loading" @click="refreshAll">刷新状态</el-button>
      </div>
    </div>

    <!-- 业务隔离提醒 -->
    <el-alert
      type="info"
      show-icon
      :closable="false"
      class="isolation-banner mb-3"
    >
      <template #title>
        <span class="notice-title">课堂物联使用提示</span>
      </template>
      <div class="notice-body">
        本班同学使用相同的 MQTT 账号和课堂口令；不同小组使用各自专属的 Topic。请在 Mind+ 积木中严格填入本组 Topic，不要修改。（注：此为课堂业务隔离，非组间强安全隔离）
      </div>
    </el-alert>

    <div v-if="loading && !overview" class="loading-box">
      <el-icon class="is-loading"><Loading /></el-icon>
      <p>正在加载物联网实验信息...</p>
    </div>

    <el-empty
      v-else-if="!overview?.hasExperiment"
      :description="overview?.iotEnabled === false
        ? '当前课程尚未开启物联网实验，请联系老师先在课程设计中开启物联。'
        : '当前课程暂未配置物联网实验，或教师尚未开启物联活动。'"
      class="empty-card"
    />

    <template v-else>
      <!-- 实验概览与参数卡片 -->
      <el-row :gutter="20">
        <!-- 左侧：实验与班级凭据 -->
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="info-card mb-3">
            <template #header>
              <div class="card-header">
                <span class="card-title">实验与班级连接信息</span>
                <el-tag type="primary" size="small">{{ overview.activityCode }}</el-tag>
              </div>
            </template>

            <div class="exp-title-row">
              <h3>{{ overview.experimentTitle }}</h3>
              <p class="exp-desc" v-if="overview.description">{{ overview.description }}</p>
            </div>

            <el-descriptions :column="1" border class="cred-desc-table">
              <el-descriptions-item label="学生身份">
                <span class="val-bold">{{ overview.entryYear }}级{{ overview.classCode }}班</span>
                <span class="val-sub ml-2">学号 {{ overview.studentNo }}（{{ overview.studentName }}）</span>
              </el-descriptions-item>
              <el-descriptions-item label="MQTT 服务器">
                <code class="val-code highlight">{{ overview.brokerUrl || '10.52.1.129' }}</code>
              </el-descriptions-item>
              <el-descriptions-item label="MQTT 端口">
                <code class="val-code highlight">{{ overview.brokerPort || 1883 }}</code>
              </el-descriptions-item>
              <el-descriptions-item label="班级 MQTT 账号">
                <code class="val-code">{{ overview.mqttUsername }}</code>
                <el-button link type="primary" icon="CopyDocument" @click="copyText(overview.mqttUsername, '班级账号')" />
              </el-descriptions-item>
              <el-descriptions-item label="6 位课堂口令">
                <span class="passcode-text">{{ overview.passcode }}</span>
                <el-button link type="primary" icon="CopyDocument" @click="copyText(overview.passcode, '课堂口令')" />
              </el-descriptions-item>
            </el-descriptions>

            <div class="copy-all-bar mt-3">
              <el-button type="primary" size="large" icon="DocumentCopy" class="full-btn" @click="copyMindPlusConfig">
                一键复制 Mind+ SIoT / EMQX 配置参数
              </el-button>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：所属小组与组员 -->
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="info-card mb-3">
            <template #header>
              <div class="card-header">
                <span class="card-title">我的小组：{{ overview.groupName || '未分配' }}</span>
                <el-tag :type="overview.isOnline ? 'success' : 'info'" size="small">
                  {{ overview.isOnline ? '设备在线' : '设备未上线' }}
                </el-tag>
              </div>
            </template>

            <div class="group-topic-box mb-3">
              <div class="topic-label">我的专属发布 Topic：</div>
              <div class="topic-row">
                <code class="topic-code">{{ overview.topic }}</code>
                <el-button type="primary" plain size="small" icon="CopyDocument" @click="copyText(overview.topic, 'Topic')">
                  复制
                </el-button>
              </div>
            </div>

            <div class="group-members-section mb-3">
              <div class="section-subtitle">小组成员：</div>
              <div class="members-grid">
                <div
                  v-for="m in overview.groupMembers"
                  :key="m.studentNo"
                  class="member-badge"
                  :class="{ 'is-self': m.isSelf }"
                >
                  <span class="m-no">{{ m.studentNo }}</span>
                  <span class="m-name">{{ m.studentName }}</span>
                  <el-tag v-if="m.isSelf" type="success" size="small" effect="dark" class="self-tag">我</el-tag>
                </div>
              </div>
            </div>

            <!-- 最新一条接收数据 -->
            <div class="recent-data-section">
              <div class="section-subtitle">最新接收数据：</div>
              <div v-if="overview.latestPayloadText" class="latest-banner">
                <div class="latest-meta">
                  <el-tag size="small" type="primary" effect="plain">{{ overview.latestPayloadType || 'TEXT' }}</el-tag>
                  <span class="latest-time">接收时间：{{ formatTime(overview.latestReceivedAt || overview.lastSeenAt) }}</span>
                </div>
                <div class="latest-content">{{ overview.latestPayloadText }}</div>
              </div>
              <el-empty v-else description="暂未接收到本组数据" :image-size="50" class="payload-empty" />
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 本组历史数据：分页倒序展示，每 20 秒自动刷新 -->
      <el-card v-if="overview.groupId" shadow="never" class="history-card mb-3">
        <template #header>
          <div class="card-header">
            <div class="card-title-wrap">
              <span class="card-title">本组历史数据</span>
              <el-tag size="small" type="success">共 {{ historyTotal }} 条</el-tag>
            </div>
            <div class="history-actions">
              <span class="auto-hint">每 20 秒自动刷新</span>
              <el-button size="small" icon="Refresh" @click="loadHistory()">刷新</el-button>
            </div>
          </div>
        </template>
        <el-table
          v-loading="historyLoading"
          :data="historyRows"
          size="small"
          stripe
          empty-text="暂无历史数据。掌控板经 MQTT 上报后，这里才会出现记录。"
          max-height="420"
        >
          <el-table-column label="接收时间" width="180">
            <template #default="{ row }">{{ formatTime(row.receivedAt) }}</template>
          </el-table-column>
          <el-table-column label="格式" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.payloadType === 'JSON' ? 'warning' : row.payloadType === 'NUMBER' ? 'success' : 'primary'" effect="plain">
                {{ row.payloadType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="数据内容" min-width="260">
            <template #default="{ row }">
              <span class="history-payload">{{ row.payloadText ?? row.payloadNumber ?? '' }}</span>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-if="historyTotal > 0"
          class="history-pagination"
          layout="total, prev, pager, next"
          :total="historyTotal"
          :page-size="historyPageSize"
          :current-page="historyPage"
          @current-change="loadHistory"
        />
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getIotStudentOverview, listIotStudentMessages } from '@/api/business/iot'
import { copyToClipboard } from '@/utils/clipboard'

const route = useRoute()
const router = useRouter()
const lessonId = ref(Number(route.query.lessonId) || undefined)
const overview = ref(null)
const loading = ref(false)

// 本组历史数据（分页倒序）
const historyRows = ref([])
const historyTotal = ref(0)
const historyPage = ref(1)
const historyPageSize = ref(20)
const historyLoading = ref(false)

let autoTimer = null

onMounted(() => {
  loadOverview()
  // 课堂场景下学生长时间停留页面，轮询保持数据新鲜；页面不可见时跳过
  autoTimer = setInterval(() => {
    if (document.hidden) return
    refreshAll(true)
  }, 20000)
})

onBeforeUnmount(() => {
  if (autoTimer) {
    clearInterval(autoTimer)
    autoTimer = null
  }
})

async function loadOverview() {
  loading.value = true
  try {
    const res = await getIotStudentOverview(lessonId.value)
    overview.value = res?.data || null
    if (overview.value?.groupId) {
      loadHistory(1)
    } else {
      historyRows.value = []
      historyTotal.value = 0
    }
  } catch (error) {
    ElMessage.error(error?.message || '获取物联网实验信息失败')
  } finally {
    loading.value = false
  }
}

/** 分页加载本组历史数据（按接收时间倒序）。 */
async function loadHistory(page) {
  if (!lessonId.value || !overview.value?.groupId) return
  if (typeof page === 'number') historyPage.value = page
  historyLoading.value = true
  try {
    const res = await listIotStudentMessages({
      lessonId: lessonId.value,
      pageNum: historyPage.value,
      pageSize: historyPageSize.value
    })
    const payload = res?.data || {}
    historyRows.value = payload.rows || []
    historyTotal.value = Number(payload.total || 0)
  } catch (error) {
    ElMessage.error(error?.message || '获取历史数据失败')
  } finally {
    historyLoading.value = false
  }
}

/** 手动/自动刷新：概览 + 历史数据。静默模式用于轮询，不弹错误提示。 */
async function refreshAll(silent = false) {
  try {
    const res = await getIotStudentOverview(lessonId.value)
    overview.value = res?.data || null
    if (overview.value?.groupId) {
      await loadHistory()
    }
  } catch (error) {
    if (!silent) ElMessage.error(error?.message || '刷新失败')
  }
}

function goHome() {
  router.push('/student')
}

async function copyText(text, label = '内容') {
  if (!text) return
  const ok = await copyToClipboard(text)
  if (ok) {
    ElMessage.success(`${label}已复制到剪贴板`)
  } else {
    ElMessage.warning(`无法自动复制，请手动选中复制：${text}`)
  }
}

function copyMindPlusConfig() {
  if (!overview.value) return
  const o = overview.value
  const text = `服务器：${o.brokerUrl || '10.52.1.129'}\n端口：${o.brokerPort || 1883}\n账号：${o.mqttUsername}\n密码：${o.passcode}\nTopic：${o.topic}`
  copyText(text, 'Mind+ 配置参数')
}

function formatTime(value) {
  return value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : ''
}
</script>

<style scoped>
.student-iot-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.eyebrow {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #409eff;
  margin: 0 0 4px 0;
}

.page-heading h2 {
  margin: 0 0 6px 0;
  font-size: 22px;
  color: #303133;
}

.muted {
  color: #909399;
  font-size: 13px;
  margin: 0;
}

.mb-3 {
  margin-bottom: 16px;
}

.mt-3 {
  margin-top: 16px;
}

.ml-2 {
  margin-left: 8px;
}

.isolation-banner {
  border: 1px solid #b3d8ff;
  background-color: #ecf5ff;
}

.notice-title {
  font-weight: 700;
  font-size: 14px;
  color: #409eff;
}

.notice-body {
  font-size: 13px;
  color: #606266;
  margin-top: 4px;
}

.loading-box {
  text-align: center;
  padding: 60px 0;
  color: #909399;
}

.empty-card {
  background: #ffffff;
  border-radius: 8px;
  padding: 40px;
}

.info-card {
  border: 1px solid #e4e7ed;
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}

.card-title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.exp-title-row h3 {
  margin: 0 0 6px 0;
  font-size: 18px;
  color: #303133;
}

.exp-desc {
  font-size: 13px;
  color: #606266;
  margin: 0 0 14px 0;
}

.val-bold {
  font-weight: 600;
  color: #303133;
}

.val-sub {
  color: #909399;
  font-size: 12px;
}

.val-code {
  font-family: Consolas, monospace;
  font-weight: 600;
  color: #303133;
  background: #f4f4f5;
  padding: 2px 6px;
  border-radius: 4px;
}

.val-code.highlight {
  color: #409eff;
  background: #ecf5ff;
}

.passcode-text {
  font-family: Consolas, monospace;
  font-size: 20px;
  font-weight: 800;
  color: #67c23a;
  letter-spacing: 2px;
  background: #f0f9eb;
  padding: 2px 8px;
  border-radius: 4px;
}

.full-btn {
  width: 100%;
}

.group-topic-box {
  background: #f8faff;
  border: 1px solid #d9ecff;
  border-radius: 6px;
  padding: 12px;
}

.topic-label {
  font-size: 12px;
  color: #606266;
  margin-bottom: 6px;
}

.topic-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.topic-code {
  font-family: Consolas, monospace;
  font-size: 13px;
  font-weight: 600;
  color: #409eff;
  word-break: break-all;
}

.section-subtitle {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
}

.members-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.member-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #f4f4f5;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 13px;
}

.member-badge.is-self {
  background: #ecf5ff;
  border-color: #b3d8ff;
  font-weight: 600;
  color: #409eff;
}

.m-no {
  color: #909399;
  font-size: 12px;
}

.self-tag {
  margin-left: 2px;
}

/* 最新接收数据：浅色横幅，突出数据本身 */
.latest-banner {
  background: linear-gradient(135deg, #f0f9eb 0%, #f8faff 100%);
  border: 1px solid #e1f3d8;
  border-radius: 8px;
  padding: 12px 14px;
}

.latest-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.latest-time {
  font-size: 12px;
  color: #909399;
}

.latest-content {
  font-family: Consolas, monospace;
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 10px 12px;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 180px;
  overflow-y: auto;
}

.payload-empty {
  padding: 10px 0;
}

/* 本组历史数据 */
.history-card {
  border: 1px solid #e4e7ed;
}

.history-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.auto-hint {
  font-size: 12px;
  color: #909399;
}

.history-payload {
  font-family: Consolas, monospace;
  font-weight: 600;
  color: #303133;
  word-break: break-all;
}

.history-pagination {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
