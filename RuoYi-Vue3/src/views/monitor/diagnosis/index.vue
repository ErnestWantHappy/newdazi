<template>
  <div class="diagnosis-page">
    <section class="diagnosis-hero panel-card">
      <div class="hero-main">
        <div class="eyebrow">系统监控</div>
        <h1>系统诊断中心</h1>
        <p class="hero-desc">
          服务状态、缓存健康、慢 SQL、异常日志与最近操作，整理成可复制的排障证据
          <template v-if="data.environmentLabel"> · {{ data.environmentLabel }}</template>
          <template v-if="data.historySource"> · 历史日志来源：{{ data.historySource }}</template>
        </p>
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
          <div class="health-top">
            <span class="health-label">当前判断 · {{ scopeLabel }}</span>
            <el-icon v-if="healthLevelClass === 'is-stable'" class="health-icon is-ok"><CircleCheckFilled /></el-icon>
            <el-icon v-else class="health-icon is-warn"><WarningFilled /></el-icon>
          </div>
          <h2>{{ data.health?.title || '正在读取系统状态' }}</h2>
          <p>{{ riskText }}</p>
        </section>
      </el-col>
      <el-col :xs="12" :lg="4">
        <section class="mini-card">
          <div class="mini-head">
            <el-icon><User /></el-icon>
            <span title="最近5分钟有活动的去重账号，与在线用户列表口径一致">在线用户</span>
          </div>
          <strong>{{ data.onlineCount || 0 }}</strong>
          <em>近 5 分钟活跃</em>
        </section>
      </el-col>
      <el-col :xs="12" :lg="4">
        <section class="mini-card">
          <div class="mini-head">
            <el-icon><Key /></el-icon>
            <span>Redis Key</span>
          </div>
          <strong>{{ data.cache?.dbSize || 0 }}</strong>
          <em>缓存键数量</em>
        </section>
      </el-col>
      <el-col :xs="12" :lg="4">
        <section class="mini-card" :class="{ 'is-attention': (data.slowSql || []).length > 0 }">
          <div class="mini-head">
            <el-icon><DataLine /></el-icon>
            <span>慢 SQL</span>
          </div>
          <strong>{{ (data.slowSql || []).length }}</strong>
          <em>Druid 记录</em>
        </section>
      </el-col>
      <el-col :xs="12" :lg="4">
        <section class="mini-card" :class="{ 'is-danger': (data.recentErrors || []).length > 0 }">
          <div class="mini-head">
            <el-icon><Warning /></el-icon>
            <span>最近错误</span>
          </div>
          <strong>{{ (data.recentErrors || []).length }}</strong>
          <em>{{ scopeLabel }}内</em>
        </section>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="diagnosis-grid">
      <el-col :xs="24" :lg="9">
        <section class="panel panel-card">
          <div class="panel-head">
            <h2><el-icon><Odometer /></el-icon>资源压力</h2>
            <span>CPU · JVM · 系统内存</span>
          </div>
          <div ref="resourceChartRef" class="chart"></div>
          <div class="gauge-legend">
            <span class="legend-item"><i class="dot is-ok"></i>正常 &lt; 70%</span>
            <span class="legend-item"><i class="dot is-warn"></i>偏高 70% 至 &lt; 90%</span>
            <span class="legend-item"><i class="dot is-danger"></i>危险 &gt;= 90%</span>
          </div>
          <p class="resource-note">{{ jvmDetailText }}</p>
        </section>
      </el-col>
      <el-col :xs="24" :lg="15">
        <section class="panel panel-card">
          <div class="panel-head">
            <h2><el-icon><Warning /></el-icon>风险提示</h2>
            <span>{{ scopeLabel }}内优先处理这些信号</span>
          </div>
          <div v-if="riskList.length" class="risk-list">
            <div v-for="risk in riskList" :key="risk" class="risk-item">
              <i class="risk-bar"></i>{{ risk }}
            </div>
          </div>
          <el-empty v-else description="暂无明显风险" :image-size="80" />
          <div v-if="adviceSummary.length" class="advice-block">
            <h3>建议动作</h3>
            <div v-for="(item, index) in adviceSummary" :key="index" class="advice-item">
              <el-tag :type="severityTagType(item.severity)" size="small" effect="light">{{ categoryLabel(item.category) }}</el-tag>
              <div class="advice-body">
                <strong>{{ item.label }}</strong>
                <span>{{ item.advice }}</span>
              </div>
            </div>
          </div>
        </section>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="diagnosis-grid">
      <el-col :xs="24" :lg="10">
        <section class="panel panel-card">
          <div class="panel-head">
            <h2><el-icon><Cpu /></el-icon>主机硬件信息</h2>
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
        <section class="panel panel-card">
          <div class="panel-head">
            <h2><el-icon><Coin /></el-icon>磁盘状态</h2>
            <span>各分区容量与使用率</span>
          </div>
          <el-table :data="hardware.sysFiles || []" size="small" border>
            <el-table-column prop="dirName" label="盘符路径" min-width="90" />
            <el-table-column prop="sysTypeName" label="文件系统" min-width="70" />
            <el-table-column prop="total" label="总大小" min-width="80" />
            <el-table-column prop="free" label="可用大小" min-width="80" />
            <el-table-column prop="used" label="已用大小" min-width="80" />
            <el-table-column label="已用百分比" min-width="150">
              <template #default="{ row }">
                <el-progress
                  :percentage="Number(row.usage || 0)"
                  :stroke-width="10"
                  :color="gaugeLevel(Number(row.usage || 0)) === 'ok' ? '#1f9d8a' : gaugeLevel(Number(row.usage || 0)) === 'warn' ? '#d07a2d' : '#b85b52'"
                />
              </template>
            </el-table-column>
          </el-table>
        </section>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="diagnosis-grid">
      <el-col :span="24">
        <section class="panel panel-card jvm-panel">
          <div class="panel-head">
            <h2><el-icon><Platform /></el-icon>Java 虚拟机信息</h2>
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
        <section class="panel panel-card conversion-panel">
          <div class="panel-head">
            <div class="panel-title-group">
              <h2><el-icon><Files /></el-icon>文档转换健康</h2>
              <span>LibreOffice 进程、conversion 线程池和预览转换积压</span>
            </div>
            <div class="panel-actions">
              <el-tag :type="libreOfficeInfo.excessiveProcesses ? 'danger' : 'success'" effect="plain">
                进程 {{ libreOfficeInfo.processCount || 0 }} / 阈值 {{ libreOfficeInfo.processWarnThreshold || 0 }}
              </el-tag>
              <el-button size="small" :loading="cleanupLoading" @click="handleLibreOfficeCleanup">手动清理</el-button>
            </div>
          </div>
          <div class="conversion-metrics">
            <div class="conversion-card">
              <span>LibreOffice 服务池</span>
              <strong :class="libreOfficeInfo.serviceAvailable ? 'tone-ok' : 'tone-danger'">
                {{ libreOfficeInfo.serviceAvailable ? '可用' : '不可用' }}
              </strong>
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
              <strong :class="conversionBacklog.waiting > 0 ? 'tone-warn' : ''">{{ conversionBacklog.waiting }}</strong>
              <em>日常 {{ conversionInfo.dailyWaitingCount || 0 }}，区域抽测 {{ conversionInfo.countyWaitingCount || 0 }}</em>
            </div>
            <div class="conversion-card">
              <span>预览失败</span>
              <el-tooltip :content="previewErrorSummary" placement="top">
                <strong :class="conversionBacklog.failed > 0 ? 'tone-warn' : ''">{{ conversionBacklog.failed }}</strong>
              </el-tooltip>
              <em>累计；近 7 天 {{ conversionRecent7d }} 条</em>
            </div>
          </div>
        </section>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="diagnosis-grid">
      <el-col :span="24">
        <section class="panel panel-card">
          <div class="panel-head">
            <div class="panel-title-group">
              <h2><el-icon><Clock /></el-icon>诊断时间线</h2>
              <span>持久化的慢 SQL、慢接口与异常操作，便于定位卡顿时段；来源以记录的用户名为准，缺失时显示“来源未记录”</span>
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
            <el-table-column prop="operName" label="来源" width="110">
              <template #default="{ row }">
                <el-tag v-if="!row.operName" size="small" type="info" effect="plain">来源未记录</el-tag>
                <span v-else>{{ row.operName }}</span>
              </template>
            </el-table-column>
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
        <section class="panel panel-card">
          <div class="panel-head">
            <h2><el-icon><WarningFilled /></el-icon>最近系统异常</h2>
            <span>{{ scopeLabel }}内需处理的系统错误；业务校验提示已折叠到下方</span>
          </div>
          <el-table :data="data.recentErrors || []" height="330" :empty-text="`${scopeLabel}内暂无系统异常`">
            <el-table-column prop="oper_time" label="时间" width="170" />
            <el-table-column prop="title" label="模块" width="100" show-overflow-tooltip />
            <el-table-column prop="severity" label="等级" width="76">
              <template #default="{ row }">
                <el-tag :type="severityTagType(row.severity)" size="small">{{ severityLabel(row.severity) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="error_msg" label="错误信息" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">{{ plainText(row.error_msg) }}</template>
            </el-table-column>
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
        <section class="panel panel-card">
          <div class="panel-head">
            <h2><el-icon><Timer /></el-icon>慢接口</h2>
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

    <el-row v-if="businessTips.length" :gutter="16" class="diagnosis-grid">
      <el-col :span="24">
        <section class="panel panel-card">
          <el-collapse v-model="businessTipsOpen">
            <el-collapse-item name="tips">
              <template #title>
                <span class="business-tip-title">业务提示（{{ scopeLabel }}内 {{ businessTips.length }} 类 / {{ businessTipsTotal }} 次，已折叠）</span>
              </template>
              <el-table :data="businessTips" size="small" max-height="320">
                <el-table-column prop="occurCount" label="次数" width="70" />
                <el-table-column prop="lastTime" label="最近时间" width="170" />
                <el-table-column prop="title" label="模块" width="100" show-overflow-tooltip />
                <el-table-column prop="oper_url" label="接口" min-width="170" show-overflow-tooltip />
                <el-table-column prop="userCount" label="用户数" width="80" />
                <el-table-column prop="error_msg" label="错误信息" min-width="260" show-overflow-tooltip>
                  <template #default="{ row }">{{ plainText(row.error_msg) }}</template>
                </el-table-column>
                <el-table-column prop="advice" label="处置建议" min-width="200" show-overflow-tooltip />
                <el-table-column label="操作" width="86" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="copyRow(row)">复制</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-collapse-item>
          </el-collapse>
        </section>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="diagnosis-grid">
      <el-col :xs="24" :lg="14">
        <section class="panel panel-card">
          <div class="panel-head">
            <div class="panel-title-group">
              <h2><el-icon><DataLine /></el-icon>Druid 慢 SQL</h2>
              <span>{{ sqlStatScopeLabel }}</span>
            </div>
            <el-button size="small" :loading="selfTestLoading" @click="handlePerfEventSelfTest">链路自检</el-button>
          </div>
          <el-table :data="data.slowSql || []" height="360" empty-text="暂无 Druid SQL 统计">
            <el-table-column prop="maxTimespan" label="最大耗时" width="100">
              <template #default="{ row }">
                <el-tag :type="row.severity === 'critical' ? 'danger' : row.severity === 'info' ? 'info' : 'warning'" size="small">{{ row.maxTimespan }} ms</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="avgTime" label="平均耗时" width="92">
              <template #default="{ row }">{{ row.avgTime || 0 }} ms</template>
            </el-table-column>
            <el-table-column prop="executeCount" label="执行次数" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.highFrequency" size="small" type="info">高频</el-tag>
                {{ row.executeCount }}
              </template>
            </el-table-column>
            <el-table-column prop="totalTimeMs" label="累计耗时" width="110">
              <template #default="{ row }">{{ formatDurationMs(row.totalTimeMs) }}</template>
            </el-table-column>
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
            <el-table-column prop="sql" label="SQL" min-width="220" show-overflow-tooltip>
              <template #default="{ row }">
                <el-tooltip
                  :content="row.sql"
                  placement="top"
                  :popper-style="{ maxWidth: 'min(720px, calc(100vw - 32px))', maxHeight: '60vh', overflow: 'auto', whiteSpace: 'pre-wrap', overflowWrap: 'anywhere' }"
                >
                  <code class="sql-code">{{ row.sql }}</code>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="86" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="copyRow(row)">复制</el-button>
              </template>
            </el-table-column>
          </el-table>
        </section>
      </el-col>

      <el-col :xs="24" :lg="10">
        <section class="panel panel-card">
          <div class="panel-head">
            <h2><el-icon><List /></el-icon>后台任务状态</h2>
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
import { Coin, Cpu, DataLine, DocumentCopy, Files, Key, List, Odometer, Platform, Refresh, Timer, User, Warning, WarningFilled, CircleCheckFilled } from '@element-plus/icons-vue'
import { cleanupLibreOffice, getDiagnosisSummary, getDiagnosisEvents, perfEventSelfTest } from '@/api/monitor/diagnosis'
import { copyToClipboard } from '@/utils/clipboard'

const { proxy } = getCurrentInstance()
const data = ref({})
const perfEvents = ref([])
const diagnosisHours = ref(24)
const eventType = ref('')
const businessTipsOpen = ref([])
const selfTestLoading = ref(false)
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
const conversionRecent7d = computed(() =>
  Number(conversionInfo.value.dailyFailedRecent7d || 0) + Number(conversionInfo.value.countyFailedRecent7d || 0))
const previewErrorSummary = computed(() => {
  const rows = conversionInfo.value.previewErrorTop5 || []
  if (!rows.length) return '暂无失败原因统计'
  return rows.map(item => `${item.errorMessage || '未记录原因'}：${item.total} 条`).join('；')
})
const businessTips = computed(() => data.value.businessTips || [])
const businessTipsTotal = computed(() =>
  businessTips.value.reduce((sum, item) => sum + Number(item.occurCount || 0), 0))
const jvmUsageOfMax = computed(() => {
  const jvm = data.value.server?.jvm || {}
  const used = Number(jvm.used || 0)
  const max = Number(jvm.max || 0)
  return max > 0 ? (used / max) * 100 : 0
})
const jvmDetailText = computed(() => {
  const jvm = data.value.server?.jvm || {}
  return `JVM 已用 ${formatMb(jvm.used)} / 已提交 ${formatMb(jvm.total)} / 上限 ${formatMb(jvm.max)}（占上限 ${Math.round(jvmUsageOfMax.value)}%）`
})
const sqlStatScopeLabel = computed(() => {
  const scope = data.value.sqlStatScope || {}
  if (scope.startTime) {
    return `进程内统计，自 ${scope.startTime} 起，约 ${scope.uptimeHours || 0} 小时（重启清零）`
  }
  return '进程内统计，自后端启动起（重启清零）'
})
const cpuUsed = computed(() => Number(data.value.server?.cpu?.used || 0))
const memUsed = computed(() => Number(data.value.server?.mem?.usage || 0))

function formatMb(value) {
  const num = Number(value || 0)
  return num > 0 ? `${num.toFixed(1)} MB` : '—'
}

function formatDurationMs(value) {
  const num = Number(value || 0)
  if (num >= 3600000) return `${(num / 3600000).toFixed(1)} h`
  if (num >= 60000) return `${(num / 60000).toFixed(1)} min`
  return `${Math.round(num)} ms`
}

function plainText(value) {
  return String(value || '').replace(/<br\s*\/?>/gi, '；').replace(/<[^>]+>/g, '').trim()
}

function fmtGb(value) {
  if (value == null || Number.isNaN(Number(value))) return '—'
  return `${Number(value).toFixed(2)} GB`
}

// 三环仪表与磁盘进度条共用同一套语义色：正常 teal、偏高橙、危险红
function gaugeLevel(value) {
  if (value >= 90) return 'danger'
  if (value >= 70) return 'warn'
  return 'ok'
}

const GAUGE_COLORS = { ok: '#1f9d8a', warn: '#d07a2d', danger: '#b85b52' }

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
  resourceChart.setOption({
    tooltip: { formatter: '{b}: {c}%' },
    series: [
      gaugeItem('CPU', cpuUsed.value, ['14%', '58%']),
      gaugeItem('JVM', jvmUsageOfMax.value, ['50%', '58%']),
      gaugeItem('内存', memUsed.value, ['86%', '58%'])
    ]
  })
}

function gaugeItem(name, value, center) {
  const color = GAUGE_COLORS[gaugeLevel(value)]
  return {
    name,
    type: 'gauge',
    center,
    // 半径按容器短边一半的百分比：44% 保证窄视口（约 330px 宽）下三环互不重叠
    radius: '44%',
    min: 0,
    max: 100,
    progress: { show: true, width: 9, roundCap: true, itemStyle: { color } },
    axisLine: { lineStyle: { width: 9, color: [[1, 'rgba(124, 140, 160, 0.22)']] } },
    axisTick: { show: false },
    splitLine: { show: false },
    axisLabel: { show: false },
    pointer: { show: false },
    detail: { valueAnimation: true, formatter: value => `${Number(value).toFixed(1)}%`, fontSize: 17, fontWeight: 700, color, offsetCenter: [0, '10%'] },
    title: { offsetCenter: [0, '62%'], fontSize: 12.5 },
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

function handlePerfEventSelfTest() {
  selfTestLoading.value = true
  perfEventSelfTest().then(res => {
    const result = res.data || {}
    if (result.persisted) {
      proxy.$modal.msgSuccess('链路正常：慢 SQL 已拦截并写入事件表')
    } else {
      proxy.$modal.msgWarning(result.hint || '链路自检未通过，请查看后端日志')
    }
    loadEvents()
    loadData()
  }).catch(() => {
    proxy.$modal.msgError('链路自检请求失败，请查看后端日志')
  }).finally(() => {
    selfTestLoading.value = false
  })
}

function copyRow(row) {
  copyText(JSON.stringify(row, null, 2))
}

function copyText(text) {
  // 内网 HTTP 下 navigator.clipboard 不存在，必须走带 execCommand 降级的统一工具，否则点击无任何反馈
  copyToClipboard(text).then(ok => {
    if (ok) proxy.$modal.msgSuccess('已复制，可直接发给 AI 排查')
    else proxy.$modal.msgError('复制失败，请手动选择文本')
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
// 与 platform/overview 同一设计语言：浅灰底白卡 + teal 唯一强调色 + 语义三色。
// 颜色收敛到变量，html.dark 下整体覆盖（先例：help/index.vue）。
.diagnosis-page {
  --diag-bg: #f5f7f9;
  --diag-card: #ffffff;
  --diag-border: rgba(36, 49, 63, 0.08);
  --diag-shadow: 0 14px 34px rgba(32, 45, 58, 0.07);
  --diag-text: #24313f;
  --diag-text-2: #647080;
  --diag-text-3: #7a8795;
  --diag-soft: #f8fafc;
  --diag-soft-border: #e8edf2;
  --diag-teal: #1f9d8a;
  --diag-teal-soft: #eef8f5;
  --diag-amber: #d07a2d;
  --diag-amber-soft: #fff8ed;
  --diag-red: #b85b52;

  min-height: calc(100vh - 84px);
  padding: 22px;
  background:
    linear-gradient(135deg, rgba(31, 157, 138, 0.07), transparent 30%),
    linear-gradient(45deg, rgba(208, 122, 45, 0.06), transparent 36%),
    var(--diag-bg);
  color: var(--diag-text);
}

.panel-card {
  border: 1px solid var(--diag-border);
  border-radius: 10px;
  background: var(--diag-card);
  box-shadow: var(--diag-shadow);
  transition: box-shadow 0.25s ease, transform 0.25s ease;

  &:hover {
    box-shadow: 0 18px 42px rgba(32, 45, 58, 0.1);
  }
}

// ── 顶部 Hero ──────────────────────────────
.diagnosis-hero {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  padding: 22px 26px;
}

.eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--diag-teal);
  font-weight: 700;
  font-size: 13px;
  letter-spacing: 0.08em;

  &::before {
    content: '';
    width: 18px;
    height: 4px;
    border-radius: 2px;
    background: var(--diag-teal);
  }
}

.hero-main h1 {
  margin: 8px 0 8px;
  font-size: 27px;
  font-weight: 800;
  line-height: 1.2;
}

.hero-desc {
  margin: 0;
  color: var(--diag-text-2);
  font-size: 13.5px;
}

.hero-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
  flex-shrink: 0;
}

.diagnosis-grid {
  margin-top: 16px;
}

// ── 健康结论卡 ──────────────────────────────
.health-card {
  padding: 18px 20px;
  min-height: 128px;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 14px;
    bottom: 14px;
    width: 4px;
    border-radius: 0 4px 4px 0;
    background: var(--diag-teal);
  }

  &.is-warning::before {
    background: var(--diag-amber);
  }
}

.health-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.health-icon {
  font-size: 26px;

  &.is-ok {
    color: var(--diag-teal);
  }

  &.is-warn {
    color: var(--diag-amber);
  }
}

.health-label {
  color: var(--diag-text-3);
  font-size: 13px;
  font-weight: 600;
}

.health-card h2 {
  margin: 10px 0 8px;
  font-size: 20px;
  line-height: 1.35;
}

.health-card p {
  margin: 0;
  color: var(--diag-text-2);
  line-height: 1.7;
  font-size: 13.5px;
}

// ── Mini 统计卡 ──────────────────────────────
.mini-card {
  padding: 18px 20px;
  min-height: 128px;
  border-left: none;

  .mini-head {
    display: flex;
    align-items: center;
    gap: 8px;
    color: var(--diag-text-3);
    font-size: 13px;
    font-weight: 600;

    .el-icon {
      font-size: 16px;
      color: var(--diag-teal);
    }
  }

  strong {
    display: block;
    margin-top: 14px;
    font-size: 32px;
    font-weight: 800;
    line-height: 1;
    font-variant-numeric: tabular-nums;
    letter-spacing: -0.02em;
  }

  em {
    display: block;
    margin-top: 10px;
    font-style: normal;
    color: var(--diag-text-3);
    font-size: 12px;
  }

  &.is-attention strong,
  &.is-attention .mini-head .el-icon {
    color: var(--diag-amber);
  }

  &.is-danger strong,
  &.is-danger .mini-head .el-icon {
    color: var(--diag-red);
  }
}

// ── 面板通用 ──────────────────────────────
.panel {
  padding: 18px 20px;
  min-height: 390px;
}

.conversion-panel {
  min-height: 0;
}

// JVM 信息只有一张描述表，沿用 390px 最小高度会在表格下方留大片空白
.jvm-panel {
  min-height: 0;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  flex-wrap: wrap;

  h2 {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    margin: 0;
    font-size: 17px;
    font-weight: 800;

    .el-icon {
      color: var(--diag-teal);
      font-size: 18px;
    }
  }

  > span,
  .panel-title-group span {
    color: var(--diag-text-3);
    font-size: 12.5px;
    font-weight: 500;
  }
}

.panel-title-group {
  display: flex;
  flex-direction: column;
  gap: 4px;

  h2 {
    margin: 0;
  }
}

.panel-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

// ── 资源仪表 ──────────────────────────────
.chart {
  height: 260px;
}

.gauge-legend {
  display: flex;
  justify-content: center;
  gap: 18px;
  flex-wrap: wrap;
  margin-top: 2px;
  color: var(--diag-text-3);
  font-size: 12px;

  .legend-item {
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }

  .dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;

    &.is-ok {
      background: var(--diag-teal);
    }

    &.is-warn {
      background: var(--diag-amber);
    }

    &.is-danger {
      background: var(--diag-red);
    }
  }
}

.resource-note {
  margin: 10px 0 0;
  color: var(--diag-text-2);
  font-size: 12px;
  text-align: center;
}

// ── 风险与建议 ──────────────────────────────
.risk-list {
  display: grid;
  gap: 10px;
}

.risk-item {
  position: relative;
  padding: 11px 14px 11px 20px;
  border-radius: 8px;
  background: var(--diag-amber-soft);
  color: #5b4b35;
  line-height: 1.6;
  font-size: 13.5px;

  .risk-bar {
    position: absolute;
    left: 8px;
    top: 13px;
    bottom: 13px;
    width: 3px;
    border-radius: 2px;
    background: var(--diag-amber);
  }
}

.advice-block {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid var(--diag-soft-border);

  h3 {
    margin: 0 0 12px;
    font-size: 14px;
    font-weight: 700;
  }
}

.advice-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 11px 14px;
  margin-bottom: 10px;
  border-radius: 8px;
  background: var(--diag-soft);
  border-left: 3px solid var(--diag-teal);

  .el-tag {
    flex-shrink: 0;
    margin-top: 1px;
  }

  .advice-body {
    display: flex;
    flex-direction: column;
    gap: 3px;
    min-width: 0;

    strong {
      font-size: 13.5px;
    }

    span {
      color: var(--diag-text-2);
      font-size: 12.5px;
      line-height: 1.6;
    }
  }
}

.business-tip-title {
  font-weight: 700;
  color: var(--diag-text-2);
  font-size: 13.5px;
}

// ── 表格细节 ──────────────────────────────
.sql-desc {
  display: grid;
  gap: 4px;

  strong {
    color: var(--diag-text);
    font-size: 13px;
  }

  span {
    color: var(--diag-text-2);
    font-size: 12px;
    line-height: 1.5;
  }
}

.sql-code {
  font-family: 'Cascadia Code', 'JetBrains Mono', Consolas, 'Courier New', monospace;
  font-size: 11.5px;
  color: var(--diag-text-2);
  word-break: break-all;
  display: block;
  max-height: 3.4em;
  overflow: hidden;
}

.event-expand {
  padding: 8px 12px 12px;
  color: var(--diag-text-2);
  line-height: 1.7;
  word-break: break-all;
  font-size: 13px;
}

.job-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

// ── 文档转换卡片 ──────────────────────────────
.conversion-metrics {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.conversion-card {
  min-height: 104px;
  padding: 14px 16px;
  border: 1px solid var(--diag-soft-border);
  border-radius: 8px;
  background: var(--diag-soft);

  span,
  em {
    display: block;
    color: var(--diag-text-3);
    font-size: 12px;
    line-height: 1.5;
  }

  strong {
    display: block;
    margin: 8px 0;
    font-size: 24px;
    font-weight: 700;
    color: var(--diag-text);
    font-variant-numeric: tabular-nums;

    &.tone-ok {
      color: var(--diag-teal);
    }

    &.tone-warn {
      color: var(--diag-amber);
    }

    &.tone-danger {
      color: var(--diag-red);
    }
  }

  em {
    font-style: normal;
  }
}

// ── 暗色模式：跟随全局 html.dark，保证工具页夜间不刺眼 ──
// :global 必须包裹完整选择器（先例 help/index.vue），“前缀 global + 后缀 scoped”写法会被静默丢弃
:global(html.dark .diagnosis-page) {
  --diag-bg: #141414;
  --diag-card: #1d1d1d;
  --diag-border: rgba(255, 255, 255, 0.09);
  --diag-shadow: 0 14px 34px rgba(0, 0, 0, 0.45);
  --diag-text: #e5eaf3;
  --diag-text-2: #a3adc2;
  --diag-text-3: #8b95a5;
  --diag-soft: #232323;
  --diag-soft-border: #303030;
  --diag-teal-soft: rgba(31, 157, 138, 0.14);
  --diag-amber-soft: rgba(208, 122, 45, 0.14);

  background:
    linear-gradient(135deg, rgba(31, 157, 138, 0.1), transparent 30%),
    linear-gradient(45deg, rgba(208, 122, 45, 0.07), transparent 36%),
    var(--diag-bg);
}

:global(html.dark .diagnosis-page .risk-item) {
  color: #e0c9a8;
}

@media (max-width: 900px) {
  .diagnosis-page {
    padding: 14px;
  }

  .diagnosis-hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-main h1 {
    font-size: 23px;
  }

  .conversion-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
