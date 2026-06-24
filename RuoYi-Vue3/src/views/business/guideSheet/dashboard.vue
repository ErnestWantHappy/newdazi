<template>
  <div class="app-container guide-sheet-dashboard">
    <div class="dashboard-header">
      <el-button icon="ArrowLeft" @click="goBack">返回列表</el-button>
      <h2 style="margin:0 16px">{{ sheetTitle || '导学单数据看板' }}</h2>
      <el-select v-model="classCode" placeholder="选择班级" style="width:160px" size="small" clearable @change="refresh">
        <el-option label="全部班级" value="" />
        <el-option v-for="c in assignedClasses" :key="c.value" :label="c.label" :value="c.value" />
      </el-select>
    </div>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="18">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-card shadow="hover" header="填写进度" class="chart-card">
              <div ref="pieChartRef" style="height:280px"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" header="统计概览" class="stat-cards-row">
              <div class="stat-card">
                <div class="stat-label">总人数</div>
                <div class="stat-value">{{ progressData.total || 0 }}</div>
              </div>
              <div class="stat-card stat-card-success">
                <div class="stat-label">已提交</div>
                <div class="stat-value">{{ progressData.submitted || 0 }}</div>
              </div>
              <div class="stat-card stat-card-warning">
                <div class="stat-label">填写中</div>
                <div class="stat-value">{{ (progressData.total || 0) - (progressData.submitted || 0) }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-card shadow="never" style="margin-top:16px" header="填写进度详情">
          <el-table :data="progressData.list" stripe size="small" max-height="500">
            <el-table-column label="姓名" prop="studentName" width="120" />
            <el-table-column label="学号" prop="studentNo" width="120" />
            <el-table-column label="当前页" prop="currentPage" width="80" align="center" />
            <el-table-column label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.isSubmitted === 'Y' ? 'success' : 'info'" size="small">
                  {{ scope.row.isSubmitted === 'Y' ? '已提交' : '填写中' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="最后心跳" prop="lastHeartbeat" width="180" />
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="never" header="课堂控制" class="control-panel">
          <div class="page-control">
            <div class="page-label">当前页：{{ currentPage }} / {{ maxPages }}</div>
            <el-slider v-model="currentPage" :min="1" :max="maxPages || 5" show-input @change="sendPageChange" />
          </div>
          <el-divider />
          <el-input v-model="broadcastMsg" placeholder="输入广播消息" size="small" @keyup.enter="sendBroadcast">
            <template #append><el-button @click="sendBroadcast" size="small">发送</el-button></template>
          </el-input>
          <el-button style="margin-top:8px;width:100%" @click="sendRefresh">刷新学生页面</el-button>
        </el-card>

        <el-card shadow="never" header="作品展示" style="margin-top:12px">
          <div class="works-grid" v-if="worksItems.length > 0">
            <div v-for="(item, i) in worksItems" :key="i" class="work-item" @click="previewWork(item)">
              <img v-if="isImageUrl(item.url)" :src="item.url" class="work-thumb" />
              <video v-else-if="isVideoUrl(item.url)" :src="item.url" class="work-thumb" />
              <div v-else class="work-file">{{ item.name }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无上传作品" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="GuideSheetDashboard">
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getGuideSheet, getProgress, getUploads } from '@/api/business/guideSheet'
import { setTeacherMachineIp, getListUrl } from '@/utils/teacherMachine'
import websocketClient from '@/plugins/websocket'
import useUserStore from '@/store/modules/user'
import * as echarts from 'echarts'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const sheetTitle = ref('')
const maxPages = ref(5)
const classCode = ref('')
const assignedClasses = ref([])  // [{value: '1', label: '1班'}, ...]
const currentPage = ref(1)
const broadcastMsg = ref('')
const progressData = ref({ total: 0, submitted: 0, list: [] })
const uploads = ref([])
const worksItems = ref([])

const pieChartRef = ref(null)
let pieChart = null
let pollTimer = null
let flaskPollTimer = null

function goBack() {
  router.push({ path: '/business/guide-sheet-list' })
}

function refresh() {
  const sheetId = route.params.sheetId
  if (!sheetId) return
  getProgress(sheetId, classCode.value).then(res => {
    progressData.value = res
    nextTick(() => updatePieChart())
  }).catch(() => {})
  getUploads(sheetId, classCode.value).then(res => {
    uploads.value = Array.isArray(res.data) ? res.data : (res.rows || [])
  }).catch(() => {})
}

function pollFlaskWorks() {
  const listUrl = getListUrl(classCode.value)
  if (!listUrl) return
  fetch(listUrl)
    .then(res => res.json())
    .then(data => {
      const items = (data.data || data || []).map(d => ({
        url: d.url || d.access_url || d.file_path || '',
        name: d.file_name || d.fileName || ''
      }))
      worksItems.value = items
    })
    .catch(() => {})
}

function updatePieChart() {
  if (!pieChartRef.value) return
  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }
  const submitted = progressData.value.submitted || 0
  const total = progressData.value.total || 0
  const inProgress = total - submitted
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%' },
    series: [{
      name: '填写进度',
      type: 'pie',
      radius: ['50%', '75%'],
      avoidLabelOverlap: false,
      label: { show: false },
      data: [
        { value: submitted, name: '已提交', itemStyle: { color: '#67C23A' } },
        { value: inProgress > 0 ? inProgress : 0, name: '填写中', itemStyle: { color: '#E6A23C' } }
      ]
    }]
  })
}

function sendPageChange() {
  const deptId = userStore.deptId || ''
  websocketClient.send({ type: 'page_change', page: currentPage.value, deptId, classCode: classCode.value })
}

function sendBroadcast() {
  if (!broadcastMsg.value.trim()) return
  websocketClient.send({ type: 'message', content: broadcastMsg.value.trim() })
  broadcastMsg.value = ''
}

function sendRefresh() {
  websocketClient.send({ type: 'refresh' })
}

function isImageUrl(url) {
  return url && /\.(jpg|jpeg|png|gif|webp|svg)(\?|$)/i.test(url)
}

function isVideoUrl(url) {
  return url && /\.(mp4|webm|ogg)(\?|$)/i.test(url)
}

function previewWork(item) {
  if (item.url) {
    window.open(item.url, '_blank')
  }
}

onMounted(() => {
  const sheetId = route.params.sheetId
  if (sheetId) {
    getGuideSheet(sheetId).then(res => {
      sheetTitle.value = res.data.sheetTitle || ''
      maxPages.value = res.data.maxPages || 5
      if (res.data.assignedClassCodes) {
        const codes = res.data.assignedClassCodes
        if (Array.isArray(codes)) {
          // 后端返回的是 List<String>，如 ["1", "2"]
          assignedClasses.value = codes
            .filter(c => c != null && c.trim() !== '')
            .map(c => ({ value: c.trim(), label: c.trim() + '班' }))
        } else if (typeof codes === 'string') {
          // 兼容旧数据：逗号分隔的字符串
          assignedClasses.value = codes.split(',').map(c => {
            const trim = c.trim()
            return trim ? { value: trim, label: trim.endsWith('班') ? trim : trim + '班' } : null
          }).filter(Boolean)
        }
      }
      const ip = res.data.teacherMachineIp
      if (ip) {
        setTeacherMachineIp(ip)
      }
      webSocketClient.connectClassroom(userStore.deptId || '', classCode.value || '')
    })
    refresh()
    pollTimer = setInterval(refresh, 5000)
    flaskPollTimer = setInterval(pollFlaskWorks, 3000)
  }
})

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
  if (flaskPollTimer) clearInterval(flaskPollTimer)
  if (pieChart) { pieChart.dispose(); pieChart = null }
  websocketClient.disconnect()
})
</script>

<style scoped>
.dashboard-header { display: flex; align-items: center; }
.chart-card {  }
.stat-cards-row { display: flex; gap: 12px; justify-content: space-around; }
.stat-card { text-align: center; padding: 8px 0; flex: 1; }
.stat-card .stat-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.stat-card .stat-value { font-size: 36px; font-weight: 600; color: #303133; }
.stat-card-success .stat-value { color: #67C23A; }
.stat-card-warning .stat-value { color: #E6A23C; }
.works-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; }
.work-item { cursor: pointer; border-radius: 4px; overflow: hidden; border: 1px solid #e4e7ed; }
.work-thumb { width: 100%; height: 80px; object-fit: cover; }
.work-file { padding: 12px 8px; font-size: 12px; text-align: center; color: #606266; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
