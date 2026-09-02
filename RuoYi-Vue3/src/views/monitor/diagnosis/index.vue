<template>
  <div class="diagnosis-page">
    <section class="diagnosis-hero">
      <div>
        <div class="eyebrow">系统监控</div>
        <h1>系统诊断中心</h1>
        <p>把服务状态、缓存健康、慢 SQL、异常日志和最近操作整理成可复制的排障证据。</p>
      </div>
      <div class="hero-actions">
        <el-radio-group v-model="diagnosisHours" size="small" @change="loadData">
          <el-radio-button :value="24">近 24 小时</el-radio-button>
          <el-radio-button :value="168">近 7 天</el-radio-button>
        </el-radio-group>
        <el-button :icon="DocumentCopy" type="primary" plain @click="copyReport">复制诊断报告</el-button>
        <el-button :icon="Refresh" @click="loadData">刷新</el-button>
      </div>
    </section>

    <el-row :gutter="16" class="diagnosis-grid">
      <el-col :xs="24" :lg="8">
        <section class="health-card" :class="healthLevelClass">
          <span class="health-label">当前判断 · {{ scopeLabel }}</span>
          <h2>{{ data.health?.title || '正在读取系统状态' }}</h2>
          <p>{{ riskText }}</p>
        </section>
      </el-col>
      <el-col :xs="12" :lg="4">
        <section class="mini-card">
          <span>在线用户</span>
          <strong>{{ data.onlineCount || 0 }}</strong>
        </section>
      </el-col>
      <el-col :xs="12" :lg="4">
        <section class="mini-card">
          <span>Redis Key</span>
          <strong>{{ data.cache?.dbSize || 0 }}</strong>
        </section>
      </el-col>
      <el-col :xs="12" :lg="4">
        <section class="mini-card">
          <span>慢 SQL</span>
          <strong>{{ (data.slowSql || []).length }}</strong>
        </section>
      </el-col>
      <el-col :xs="12" :lg="4">
        <section class="mini-card">
          <span>最近错误</span>
          <strong>{{ (data.recentErrors || []).length }}</strong>
        </section>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="diagnosis-grid">
      <el-col :xs="24" :lg="9">
        <section class="panel">
          <div class="panel-head">
            <h2>资源压力</h2>
            <span>CPU、JVM、系统内存</span>
          </div>
          <div ref="resourceChartRef" class="chart"></div>
        </section>
      </el-col>
      <el-col :xs="24" :lg="15">
        <section class="panel">
          <div class="panel-head">
            <h2>风险提示</h2>
            <span>{{ scopeLabel }}内优先处理这些信号</span>
          </div>
          <div v-if="riskList.length" class="risk-list">
            <div v-for="risk in riskList" :key="risk" class="risk-item">{{ risk }}</div>
          </div>
          <el-empty v-else description="暂无明显风险" :image-size="92" />
          <div v-if="adviceSummary.length" class="advice-block">
            <h3>建议动作</h3>
            <div v-for="(item, index) in adviceSummary" :key="index" class="advice-item">
              <el-tag :type="severityTagType(item.severity)" size="small">{{ categoryLabel(item.category) }}</el-tag>
              <strong>{{ item.label }}</strong>
              <span>{{ item.advice }}</span>
            </div>
          </div>
        </section>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="diagnosis-grid">
      <el-col :xs="24" :lg="10">
        <section class="panel">
          <div class="panel-head">
            <h2>主机硬件信息</h2>
            <span>服务器与物理资源属性</span>
          </div>
          <el-alert
            v-if="data.serverDegraded"
            title="主机采集超时，以下数值可能为降级占位，不代表真实空闲"
            type="warning"
            :closable="false"
            show-icon
            style="margin-bottom: 8px"
          />
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="CPU 型号">{{ hardware.cpu?.model || '—' }}</el-descriptions-item>
            <el-descriptions-item label="核心数">{{ hardware.cpu?.cpuNum || '—' }} 核</el-descriptions-item>
            <el-descriptions-item label="总内存">{{ fmtGb(hardware.mem?.total) }}</el-descriptions-item>
            <el-descriptions-item label="服务器名称">{{ hardware.sys?.computerName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="服务器 IP">{{ hardware.sys?.computerIp || '—' }}</el-descriptions-item>
            <el-descriptions-item label="操作系统">{{ hardware.sys?.osName || '—' }}（{{ hardware.sys?.osArch || '—' }}）</el-descriptions-item>
            <el-descriptions-item label="Node.js">{{ data.nodeVersion || '未检测到' }}</el-descriptions-item>
          </el-descriptions>
        </section>
      </el-col>
      <el-col :xs="24" :lg="14">
        <section class="panel">
          <div class="panel-head">
            <h2>磁盘状态</h2>
            <span>各分区容量与使用率</span>
          </div>
          <el-table :data="hardware.sysFiles || []" size="small" border>
            <el-table-column prop="dirName" label="盘符路径" min-width="90" />
            <el-table-column prop="sysTypeName" label="文件系统" min-width="70" />
            <el-table-column prop="total" label="总大小" min-width="80" />
            <el-table-column prop="free" label="可用大小" min-width="80" />
            <el-table-column prop="used" label="已用大小" min-width="80" />
            <el-table-column label="已用百分比" min-width="140">
              <template #default="{ row }">
                <el-progress :percentage="Number(row.usage || 0)" :stroke-width="12" />
              </template>
            </el-table-column>
          </el-table>
        </section>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="diagnosis-grid">
      <el-col :span="24">
        <section class="panel">
          <div class="panel-head">
            <h2>Java 虚拟机信息</h2>
            <span>{{ hardware.jvm?.name || '—' }}</span>
          </div>
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="Java 版本">{{ hardware.jvm?.version || '—' }}</el-descriptions-item>
            <el-descriptions-item label="启动时间">{{ hardware.jvm?.startTime || '—' }}</el-descriptions-item>
            <el-descriptions-item label="运行时长">{{ hardware.jvm?.runTime || '—' }}</el-descriptions-item>
            <el-descriptions-item label="安装路径" :span="2">{{ hardware.jvm?.home || '—' }}</el-descriptions-item>
            <el-descriptions-item label="项目路径">{{ hardware.sys?.userDir || '—' }}</el-descriptions-item>
            <el-descriptions-item label="运行参数" :span="3">{{ hardware.jvm?.inputArgs || '—' }}</el-descriptions-item>
          </el-descriptions>
        </section>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="diagnosis-grid">
      <el-col :span="24">
        <section class="panel conversion-panel">
          <div class="panel-head">
            <div>
              <h2>文档转换健康</h2>
              <span>LibreOffice 进程、conversion 线程池和预览转换积压</span>
            </div>
            <el-tag :type="libreOfficeInfo.excessiveProcesses ? 'danger' : 'success'" effect="plain">
              进程 {{ libreOfficeInfo.processCount || 0 }} / 阈值 {{ libreOfficeInfo.processWarnThreshold || 0 }}
            </el-tag>
            <el-button size="small" :loading="cleanupLoading" @click="handleLibreOfficeCleanup">手动清理</el-button>
          </div>
          <div class="conversion-metrics">
            <div class="conversion-card">
              <span>LibreOffice 服务池</span>
              <strong>{{ libreOfficeInfo.serviceAvailable ? '可用' : '不可用' }}</strong>
              <em>实例 {{ libreOfficeInfo.instanceCount || 0 }}，已安装 {{ libreOfficeInfo.installed ? '是' : '否' }}</em>
            </div>
            <div class="conversion-card">
              <span>活跃转换线程</span>
              <strong>{{ conversionInfo.activeCount || 0 }}</strong>
              <em>核心 {{ conversionInfo.corePoolSize || 0 }} / 最大 {{ conversionInfo.maximumPoolSize || 0 }}</em>
            </div>
            <div class="conversion-card">
              <span>队列长度</span>
              <strong>{{ conversionInfo.queueSize || 0 }}</strong>
              <em>容量 {{ conversionInfo.queueCapacity || 0 }}，剩余 {{ conversionInfo.queueRemainingCapacity || 0 }}</em>
            </div>
            <div class="conversion-card">
              <span>等待转换</span>
              <strong>{{ conversionBacklog.waiting }}</strong>
              <em>日常 {{ conversionInfo.dailyWaitingCount || 0 }}，区域抽测 {{ conversionInfo.countyWaitingCount || 0 }}</em>
            </div>
            <div class="conversion-card">
              <span>最近失败</span>
              <strong>{{ conversionBacklog.failed }}</strong>
              <em>日常 {{ conversionInfo.dailyFailedCount || 0 }}，区域抽测 {{ conversionInfo.countyFailedCount || 0 }}</em>
            </div>
          </div>
        </section>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="diagnosis-grid">
      <el-col :span="24">
        <section class="panel">
          <div class="panel-head">
            <div>
              <h2>诊断时间线</h2>
              <span>持久化的慢 SQL、慢接口与异常操作，便于定位卡顿时段</span>
            </div>
            <div class="panel-actions">
              <el-select v-model="eventType" clearable placeholder="全部类型" size="small" style="width: 130px" @change="loadEvents">
                <el-option label="慢 SQL" value="slow_sql" />
                <el-option label="慢接口" value="slow_api" />
                <el-option label="异常" value="error_api" />
              </el-select>
            </div>
          </div>
          <el-table :data="perfEvents" height="320" :empty-text="`${scopeLabel}内暂无性能事件`">
            <el-table-column prop="occurTime" label="发生时间" width="170" />
            <el-table-column prop="eventType" label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="eventTagType(row)" size="small">{{ formatEventType(row) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="业务说明" min-width="180" show-overflow-tooltip />
            <el-table-column prop="durationMs" label="耗时" width="90">
              <template #default="{ row }">{{ row.durationMs }} ms</template>
            </el-table-column>
            <el-table-column prop="operName" label="用户" width="110" show-overflow-tooltip />
            <el-table-column prop="sourceUrl" label="接口/SQL" min-width="180" show-overflow-tooltip />
            <el-table-column prop="severity" label="等级" width="88">
              <template #default="{ row }">
                <el-tag :type="severityTagType(row.severity)" size="small">
                  {{ severityLabel(row.severity) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column type="expand" width="48">
              <template #default="{ row }">
                <div class="event-expand">
                  <p v-if="row.description"><strong>说明：</strong>{{ row.description }}</p>
                  <p v-if="row.errorMsg"><strong>错误：</strong>{{ row.errorMsg }}</p>
                  <p v-if="row.sqlText"><strong>SQL：</strong>{{ row.sqlText }}</p>
                  <p v-if="row.advice"><strong>建议：</strong>{{ row.advice }}</p>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </section>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="diagnosis-grid">
      <el-col :xs="24" :lg="12">
        <section class="panel">
          <div class="panel-head">
            <h2>最近异常与业务提示</h2>
            <span>{{ scopeLabel }}内的业务拦截与系统错误，按等级区分</span>
          </div>
          <el-table :data="data.recentErrors || []" height="330" :empty-text="`${scopeLabel}内暂无错误`">
            <el-table-column prop="oper_time" label="时间" width="170" />
            <el-table-column prop="title" label="模块" width="100" show-overflow-tooltip />
            <el-table-column prop="severity" label="等级" width="76">
              <template #default="{ row }">
                <el-tag :type="severityTagType(row.severity)" size="small">{{ severityLabel(row.severity) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="error_msg" label="错误信息" min-width="160" show-overflow-tooltip />
            <el-table-column prop="advice" label="处置建议" min-width="180" show-overflow-tooltip />
            <el-table-column prop="oper_name" label="用户" width="100" />
            <el-table-column label="操作" width="86" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="copyRow(row)">复制</el-button>
              </template>
            </el-table-column>
          </el-table>
        </section>
      </el-col>

      <el-col :xs="24" :lg="12">
        <section class="panel">
          <div class="panel-head">
            <h2>慢接口</h2>
            <span>{{ scopeLabel }}内耗时超过 1 秒的请求</span>
          </div>
          <el-table :data="data.slowOperations || []" height="330" :empty-text="`${scopeLabel}内暂无慢接口`">
            <el-table-column prop="oper_time" label="时间" width="170" />
            <el-table-column prop="title" label="模块" width="100" show-overflow-tooltip />
            <el-table-column prop="oper_url" label="接口" min-width="150" show-overflow-tooltip />
            <el-table-column prop="cost_time" label="耗时(ms)" width="96" />
            <el-table-column prop="advice" label="处置建议" min-width="180" show-overflow-tooltip />
            <el-table-column prop="oper_name" label="用户" width="100" />
            <el-table-column label="操作" width="86" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="copyRow(row)">复制</el-button>
              </template>
            </el-table-column>
          </el-table>
        </section>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="diagnosis-grid">
      <el-col :xs="24" :lg="14">
        <section class="panel">
          <div class="panel-head">
            <h2>Druid 慢 SQL</h2>
            <span>数据库层面最值得关注的 SQL</span>
          </div>
          <el-table :data="data.slowSql || []" height="360" empty-text="暂无 Druid SQL 统计">
            <el-table-column prop="maxTimespan" label="最大耗时" width="100">
              <template #default="{ row }">
                <el-tag :type="row.severity === 'critical' ? 'danger' : 'warning'" size="small">{{ row.maxTimespan }} ms</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="avgTime" label="平均耗时" width="92">
              <template #default="{ row }">{{ row.avgTime || 0 }} ms</template>
            </el-table-column>
            <el-table-column prop="executeCount" label="执行次数" width="92" />
            <el-table-column prop="runningCount" label="当前并发" width="92" />
            <el-table-column prop="lastTime" label="最近执行" width="170" />
            <el-table-column label="业务说明" min-width="160">
              <template #default="{ row }">
                <div class="sql-desc">
                  <strong>{{ row.title }}</strong>
                  <span>{{ row.advice || row.description }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="sql" label="SQL" min-width="220" show-overflow-tooltip />
            <el-table-column label="操作" width="86" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="copyRow(row)">复制</el-button>
              </template>
            </el-table-column>
          </el-table>
        </section>
      </el-col>

      <el-col :xs="24" :lg="10">
        <section class="panel">
          <div class="panel-head">
            <h2>后台任务状态</h2>
            <span>平台业务任务用途与执行频率</span>
          </div>
          <el-table :data="sortedJobs" height="360" empty-text="暂无后台任务">
            <el-table-column label="任务" min-width="130">
              <template #default="{ row }">
                <div class="job-name">
                  <span>{{ row.displayName || row.job_name }}</span>
                  <el-tag v-if="row.taskCategory === 'framework_demo'" size="small" type="info">演示</el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="purpose" label="业务用途" min-width="180" show-overflow-tooltip />
            <el-table-column prop="scheduleDesc" label="执行频率" width="120" />
            <el-table-column prop="cron_expression" label="Cron" width="120" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="82">
              <template #default="{ row }">
                <el-tag :type="row.status === '0' ? 'success' : 'info'">{{ row.status === '0' ? '运行' : '暂停' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </section>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="SystemDiagnosis">
import * as echarts from 'echarts'
import { DocumentCopy, Refresh } from '@element-plus/icons-vue'
import { cleanupLibreOffice, getDiagnosisSummary, getDiagnosisEvents } from '@/api/monitor/diagnosis'

const { proxy } = getCurrentInstance()
const data = ref({})
const perfEvents = ref([])
const diagnosisHours = ref(24)
const eventType = ref('')
const resourceChartRef = ref(null)
const cleanupLoading = ref(false)
let resourceChart = null

const hardware = computed(() => data.value.server || {})
const riskList = computed(() => data.value.health?.risks || [])
const adviceSummary = computed(() => data.value.adviceSummary || [])
const scopeLabel = computed(() => data.value.health?.scopeLabel || (diagnosisHours.value >= 168 ? '近 7 天' : `近 ${diagnosisHours.value} 小时`))
const sortedJobs = computed(() => {
  const jobs = [...(data.value.jobs || [])]
  return jobs.sort((a, b) => {
    if (a.taskCategory === b.taskCategory) return 0
    return a.taskCategory === 'framework_demo' ? 1 : -1
  })
})
const riskText = computed(() => riskList.value[0] || adviceSummary.value[0]?.advice || '服务、缓存和接口日志暂无明显异常信号。')
const healthLevelClass = computed(() => data.value.health?.level === 'warning' ? 'is-warning' : 'is-stable')
const conversionInfo = computed(() => data.value.conversion || {})
const libreOfficeInfo = computed(() => conversionInfo.value.libreOffice || {})
const conversionBacklog = computed(() => ({
  waiting: Number(conversionInfo.value.dailyWaitingCount || 0) + Number(conversionInfo.value.countyWaitingCount || 0),
  failed: Number(conversionInfo.value.dailyFailedCount || 0) + Number(conversionInfo.value.countyFailedCount || 0)
}))

function fmtGb(value) {
  if (value == null || Number.isNaN(Number(value))) return '—'
  return `${Number(value).toFixed(2)} GB`
}


function loadData() {
  // 容错：后端已做并行+超时降级，此处再兜底网络异常，保留上次数据避免页面瘫痪
  getDiagnosisSummary({ hours: diagnosisHours.value }).then(res => {
    data.value = res.data || {}
    nextTick(renderResourceChart)
  }).catch(() => {
    proxy.$modal.msgError('诊断数据获取失败，已保留上次结果，请稍后重试')
  })
  loadEvents()
}

function loadEvents() {
  getDiagnosisEvents({ hours: diagnosisHours.value, type: eventType.value || undefined }).then(res => {
    perfEvents.value = res.data || []
  })
}

function categoryLabel(category) {
  const map = { resource: '资源', business: '业务', performance: '性能', system: '系统' }
  return map[category] || '系统'
}

function severityTagType(severity) {
  if (severity === 'critical') return 'danger'
  if (severity === 'warning') return 'warning'
  return 'info'
}

function severityLabel(severity) {
  if (severity === 'critical') return '严重'
  if (severity === 'warning') return '关注'
  return '提示'
}

function formatEventType(row) {
  if (row?.eventType === 'error_api' && row?.category === 'business') return '业务提示'
  const type = row?.eventType
  const map = { slow_sql: '慢 SQL', slow_api: '慢接口', error_api: '异常' }
  return map[type] || type
}

function eventTagType(row) {
  const type = row?.eventType
  if (row?.severity === 'info') return 'info'
  if (type === 'error_api') return 'danger'
  if (type === 'slow_sql') return 'warning'
  return 'info'
}

function renderResourceChart() {
  if (!resourceChartRef.value) return
  if (resourceChart) resourceChart.dispose()
  resourceChart = echarts.init(resourceChartRef.value)
  const server = data.value.server || {}
  resourceChart.setOption({
    color: ['#1f9d8a', '#d07a2d', '#b85b52'],
    tooltip: { formatter: '{b}: {c}%' },
    series: [
      gaugeItem('CPU', Number(server.cpu?.used || 0), ['16%', '55%']),
      gaugeItem('JVM', Number(server.jvm?.usage || 0), ['50%', '55%']),
      gaugeItem('内存', Number(server.mem?.usage || 0), ['84%', '55%'])
    ]
  })
}

function gaugeItem(name, value, center) {
  return {
    name,
    type: 'gauge',
    center,
    radius: '54%',
    min: 0,
    max: 100,
    progress: { show: true, width: 8 },
    axisLine: { lineStyle: { width: 8, color: [[0.7, '#dfe8e5'], [0.9, '#f0d8b8'], [1, '#e4b8b4']] } },
    axisTick: { show: false },
    splitLine: { show: false },
    axisLabel: { show: false },
    pointer: { show: false },
    detail: { valueAnimation: true, formatter: '{value}%', fontSize: 17, offsetCenter: [0, '8%'] },
    title: { offsetCenter: [0, '58%'], fontSize: 13, color: '#52616f' },
    data: [{ value, name }]
  }
}

function copyReport() {
  copyText(data.value.report || '暂无诊断报告')
}

function handleLibreOfficeCleanup() {
  cleanupLoading.value = true
  cleanupLibreOffice().then(res => {
    proxy.$modal.msgSuccess(res.data?.message || 'LibreOffice 清理任务已执行')
    loadData()
  }).finally(() => {
    cleanupLoading.value = false
  })
}

function copyRow(row) {
  copyText(JSON.stringify(row, null, 2))
}

function copyText(text) {
  navigator.clipboard.writeText(text).then(() => {
    proxy.$modal.msgSuccess('已复制，可直接发给 AI 排查')
  }).catch(() => {
    proxy.$modal.msgError('复制失败，请手动选择文本')
  })
}

function handleResize() {
  resourceChart && resourceChart.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (resourceChart) {
    resourceChart.dispose()
    resourceChart = null
  }
})
</script>

<style scoped lang="scss">
.diagnosis-page {
  min-height: calc(100vh - 84px);
  padding: 22px;
  background:
    linear-gradient(135deg, rgba(184, 91, 82, 0.08), transparent 30%),
    linear-gradient(45deg, rgba(31, 157, 138, 0.08), transparent 36%),
    #f5f7f9;
  color: #24313f;
}

.diagnosis-hero,
.panel,
.health-card,
.mini-card {
  border: 1px solid rgba(36, 49, 63, 0.08);
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 14px 34px rgba(32, 45, 58, 0.07);
}

.diagnosis-hero {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-end;
  padding: 26px 28px;
}

.diagnosis-hero h1 {
  margin: 8px 0 10px;
  font-size: 34px;
  font-weight: 800;
  letter-spacing: 0;
}

.diagnosis-hero p {
  margin: 0;
  color: #647080;
}

.eyebrow {
  color: #b85b52;
  font-weight: 800;
  font-size: 13px;
}

.hero-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.diagnosis-grid {
  margin-top: 16px;
}

.health-card,
.mini-card,
.panel {
  padding: 18px;
}

.health-card {
  min-height: 128px;
}

.health-card.is-stable {
  border-left: 5px solid #1f9d8a;
}

.health-card.is-warning {
  border-left: 5px solid #d07a2d;
}

.health-label,
.mini-card span,
.panel-head span {
  color: #7a8795;
  font-size: 13px;
  font-weight: 700;
}

.health-card h2 {
  margin: 12px 0 8px;
  font-size: 20px;
  letter-spacing: 0;
}

.health-card p {
  margin: 0;
  color: #647080;
  line-height: 1.7;
}

.mini-card {
  min-height: 128px;
}

.mini-card strong {
  display: block;
  margin-top: 18px;
  font-size: 30px;
  color: #24313f;
}

.panel {
  min-height: 390px;
}

.conversion-panel {
  min-height: 0;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  align-items: flex-start;
}

.panel-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.sql-desc {
  display: grid;
  gap: 4px;
}

.sql-desc strong {
  color: #24313f;
}

.sql-desc span {
  color: #647080;
  font-size: 12px;
}

.event-expand {
  padding: 8px 12px 12px;
  color: #52616f;
  line-height: 1.7;
  word-break: break-all;
}

.panel-head h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 0;
}

.chart {
  height: 300px;
}

.risk-list {
  display: grid;
  gap: 12px;
}

.risk-item {
  padding: 13px 14px;
  border-radius: 6px;
  background: #fff8ed;
  border-left: 4px solid #d07a2d;
  color: #5b4b35;
  line-height: 1.6;
}

.advice-block {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid #e8edf2;
}

.advice-block h3 {
  margin: 0 0 12px;
  font-size: 15px;
  color: #24313f;
}

.advice-item {
  display: grid;
  gap: 6px;
  padding: 12px 14px;
  margin-bottom: 10px;
  border-radius: 6px;
  background: #f7faf9;
  border-left: 4px solid #1f9d8a;
}

.advice-item strong {
  color: #24313f;
}

.advice-item span {
  color: #647080;
  line-height: 1.6;
}

.job-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.conversion-metrics {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.conversion-card {
  min-height: 104px;
  padding: 14px;
  border: 1px solid #e8edf2;
  border-radius: 8px;
  background: #f8fafc;

  span,
  em {
    display: block;
    color: #7a8795;
    font-size: 12px;
    line-height: 1.5;
  }

  strong {
    display: block;
    margin: 8px 0;
    font-size: 24px;
    color: #24313f;
  }

  em {
    font-style: normal;
  }
}

@media (max-width: 900px) {
  .diagnosis-page {
    padding: 14px;
  }

  .diagnosis-hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .diagnosis-hero h1 {
    font-size: 29px;
  }

  .conversion-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
