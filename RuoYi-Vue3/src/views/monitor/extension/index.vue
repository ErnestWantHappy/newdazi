<template>
  <div class="app-container">
    <el-row :gutter="16" class="head-bar">
      <el-col :span="16">
        <span class="title">扩展服务监控（10.52.1.129）</span>
        <span class="sub">Judge0 Python 判题 · CryptPad 在线协作 · EMQX 物联网 · MQTT 接收器</span>
      </el-col>
      <el-col :span="8" style="text-align: right">
        <span class="sub" style="margin-right: 12px">
          上次刷新：{{ lastCheckedAt || '—' }}<template v-if="latencyMs != null">（{{ latencyMs }}ms）</template>
        </span>
        <el-switch v-model="autoRefresh" active-text="自动刷新(10s)" />
        <el-button size="mini" icon="Refresh" style="margin-left: 12px" @click="fetchHealth">立即刷新</el-button>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="host-dash">
      <el-col :sm="24" :lg="14">
        <el-card shadow="hover">
          <template #header><span class="card-title">129 主机资源（来源：EMQX 节点指标 + SSH 硬件探针）</span></template>
          <el-row :gutter="12">
            <el-col :span="8">
              <div class="res-block">
                <div class="res-label">CPU 负载 load1 / load5 / load15</div>
                <div class="res-value">
                  <template v-if="sysInfo.cpu?.load1 != null">
                    <el-tag type="info">{{ fmtNum(sysInfo.cpu.load1) }}</el-tag>
                    <el-tag type="warning">{{ fmtNum(sysInfo.cpu.load5) }}</el-tag>
                    <el-tag type="danger">{{ fmtNum(sysInfo.cpu.load15) }}</el-tag>
                  </template>
                  <el-tag v-else type="info">不可用</el-tag>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="res-block">
                <div class="res-label">物理内存使用率</div>
                <el-progress :percentage="memPercent == null ? 0 : memPercent"
                             :status="memPercent != null && memPercent >= 85 ? 'exception' : 'success'" />
                <div class="res-sub">{{ memText }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="res-block">
                <div class="res-label">磁盘 / GPU / 判题管道</div>
                <div class="res-sub">{{ sysInfo.disk?.available ? ('磁盘 ' + (sysInfo.disk.disks || []).length + ' 个分区已采集') : (sysInfo.disk?.note || '磁盘指标待接入') }}</div>
                <div class="res-sub">GPU：{{ sysInfo.gpu?.available ? sysInfo.gpu.name : (sysInfo.gpu?.note || '未接入') }}</div>
                <div class="res-sub" v-if="sysInfo.judgePipeline?.queueSize != null">
                  判题排队 {{ sysInfo.judgePipeline.queueSize }} · 活跃 {{ sysInfo.judgePipeline.activeCount }}
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>


    <el-alert v-if="!hostHw.available" type="warning" show-icon :closable="false" class="hw-missing"
      title="129 硬件信息暂缺"
      :description="hostHw.error || 'SSH 凭据未恢复或探针不可达；恢复凭据并配置 MONITOR_HOST129_SSH_COMMAND 后自动出数据。其余服务探针不受影响。'"
      style="margin-bottom: 16px" />

    <el-row :gutter="16" class="host-dash" v-if="hostHw.available">
      <el-col :sm="24" :lg="14">
        <el-card shadow="hover">
          <template #header><span class="card-title">129 硬件信息（SSH 探针 · {{ hostHw.os || '—' }}）</span></template>
          <el-row :gutter="12">
            <el-col :span="12">
              <div class="res-block">
                <div class="res-label">CPU 型号 / 核数</div>
                <div class="res-value"><el-tag type="info">{{ hostHw.cpu?.model || '—' }}</el-tag></div>
                <div class="res-sub">{{ hostHw.cpu?.cores ?? '—' }} 核 · 主机名 {{ hostHw.hostname || '—' }} · IP {{ hostHw.ip || '—' }}</div>
              </div>
              <div class="res-block" style="margin-top:8px">
                <div class="res-label">操作系统 / 内核</div>
                <div class="res-sub">{{ hostHw.os || '—' }}{{ hostHw.kernel ? ' · ' + hostHw.kernel : '' }}</div>
              </div>
              <div class="res-block" style="margin-top:8px">
                <div class="res-label">运行环境版本</div>
                <div class="res-sub">CryptPad Node：{{ hostHw.cryptpadNodeVersion || '—' }}</div>
                <div class="res-sub">宿主 Node：{{ hostHw.nodeVersion || '无' }} · Judge0 Java：{{ hostHw.javaVersion || '—' }}</div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="res-block">
                <div class="res-label">内存（探针口径）</div>
                <el-progress :percentage="hwMemPercent == null ? 0 : hwMemPercent"
                             :status="hwMemPercent != null && hwMemPercent >= 85 ? 'exception' : 'success'" />
                <div class="res-sub">{{ fmtGb(hostHw.memory?.totalBytes) }} 总量 · 可用 {{ fmtGb(hostHw.memory?.availableBytes) }}</div>
              </div>
              <div class="res-block" style="margin-top:8px" v-if="hostHw.java">
                <div class="res-label">Java 虚拟机（JVM）</div>
                <div class="res-sub">版本：{{ hostHw.java.version || '—' }} · 启动时长：{{ hostHw.java.startTime || '—' }}</div>
                <div class="res-sub" style="word-break: break-all">路径：{{ hostHw.java.home || '—' }}</div>
                <div class="res-sub" style="word-break: break-all">运行参数：{{ hostHw.java.arguments || '—' }}</div>
              </div>
              <div class="res-block" style="margin-top:8px" v-else>
                <div class="res-label">Java 虚拟机（JVM）</div>
                <div class="res-sub">129 无 Java 服务进程或未采集</div>
              </div>
            </el-col>
          </el-row>
          <el-table :data="hostHw.disks || []" size="mini" style="margin-top:8px">
            <el-table-column prop="mount" label="挂载点" min-width="90" />
            <el-table-column prop="fs" label="文件系统" min-width="70" />
            <el-table-column label="总容量" min-width="80">
              <template #default="{ row }">{{ fmtGb(row.totalBytes) }}</template>
            </el-table-column>
            <el-table-column label="可用" min-width="80">
              <template #default="{ row }">{{ fmtGb(row.freeBytes) }}</template>
            </el-table-column>
            <el-table-column label="已用百分比" min-width="150">
              <template #default="{ row }">
                <el-progress :percentage="Number(row.usedPercent || 0)"
                             :status="Number(row.usedPercent || 0) >= 85 ? 'exception' : 'success'" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :sm="24" :lg="10">
        <el-card shadow="hover">
          <template #header><span class="card-title">双机对比 · 123 主站 vs 129 扩展机</span></template>
          <el-table :data="compareRows" size="small">
            <el-table-column prop="item" label="指标" width="120" />
            <el-table-column prop="host123" label="123 主站" />
            <el-table-column prop="host129" label="129 扩展机" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16">

      <!-- Judge0 -->
      <el-col :sm="12" :lg="6">
        <el-card shadow="hover">
          <template #header><span class="card-title">Python 判题 Judge0</span></template>
          <p class="status-line"><el-tag :type="tagType(probeStatus(data.judge0))">{{ probeText(data.judge0) }}</el-tag></p>
          <div class="kv"><span>版本</span><b>{{ data.judge0?.version || '—' }}</b></div>
          <div class="kv"><span>响应</span><b>{{ data.judge0?.latencyMs != null ? data.judge0.latencyMs + 'ms' : '—' }}</b></div>
          <div class="err" v-if="data.judge0?.error">{{ data.judge0.error }}</div>
        </el-card>
      </el-col>

      <!-- CryptPad -->
      <el-col :sm="12" :lg="6">
        <el-card shadow="hover">
          <template #header><span class="card-title">在线协作 CryptPad</span></template>
          <p class="status-line"><el-tag :type="tagType(probeStatus(data.cryptpad))">{{ probeText(data.cryptpad) }}</el-tag></p>
          <div class="kv"><span>存储可写</span><b>{{ yesNo(data.cryptpad?.storageWritable) }}</b></div>
          <div class="err" v-for="(p, i) in data.cryptpad?.problems || []" :key="'p' + i">{{ p }}</div>
          <div class="err" v-if="data.cryptpad?.error">{{ data.cryptpad.error }}</div>
        </el-card>
      </el-col>

      <!-- EMQX -->
      <el-col :sm="12" :lg="6">
        <el-card shadow="hover">
          <template #header><span class="card-title">物联网 EMQX</span></template>
          <p class="status-line"><el-tag :type="tagType(probeStatus(data.emqx))">{{ probeText(data.emqx) }}</el-tag></p>
          <div class="kv"><span>节点数</span><b>{{ data.emqx?.nodeCount ?? '—' }}</b></div>
          <div class="kv"><span>设备连接</span><b>{{ data.emqx?.connections ?? '—' }}</b></div>
          <div class="err" v-if="data.emqx?.error">{{ data.emqx.error }}</div>
        </el-card>
      </el-col>

      <!-- MQTT 接收器 -->
      <el-col :sm="12" :lg="6">
        <el-card shadow="hover">
          <template #header><span class="card-title">平台 MQTT 接收器</span></template>
          <p class="status-line"><el-tag :type="tagType(mqttStatus)">{{ mqttText }}</el-tag></p>
          <div class="kv"><span>Broker</span><b>{{ data.mqttReceiver?.brokerUrl || '—' }}</b></div>
          <div class="kv"><span>订阅主题</span><b>{{ data.mqttReceiver?.subscription || '—' }}</b></div>
          <div class="err" v-if="mqttStatus === 'down'">接收器与 Broker 断开，物联数据将无法入库</div>
        </el-card>
      </el-col>
    </el-row>

    <el-alert v-if="loadError" type="error" :title="loadError" show-icon style="margin-top: 16px" />
  </div>
</template>

<script setup name="ExtensionMonitor">
import { computed, onUnmounted, reactive, ref } from 'vue'
import { getExtensionHealth } from '@/api/monitor/extension'
import { getDiagnosisSummary } from '@/api/monitor/diagnosis'

const data = reactive({})
const autoRefresh = ref(true)
const lastCheckedAt = ref('')
const latencyMs = ref(null)
const loadError = ref('')
let timer = null

// 状态语义四档：up 正常 / degraded 降级 / down 故障 / unconfigured 未配置
const STATUS_MAP = {
  up: ['success', '正常'],
  degraded: ['warning', '降级'],
  down: ['danger', '故障'],
  unconfigured: ['info', '未配置'],
  unknown: ['info', '获取中']
}

function probeStatus(p) {
  return p && p.status ? p.status : 'unknown'
}
function probeText(p) {
  const [label] = STATUS_MAP[probeStatus(p)]
  return label
}
function tagType(status) {
  const [type] = STATUS_MAP[status] || STATUS_MAP.unknown
  return type
}
function yesNo(v) {
  return v === true ? '是' : v === false ? '否' : '—'
}

// MQTT 接收器状态归一：进程内连接结果 + 功能开关合成
const mqttStatus = computed(() => {
  const m = data.mqttReceiver || {}
  return m.status === 'up' ? 'up' : (m.enabled ? 'down' : 'unconfigured')
})
const mqttText = computed(() => {
  if (mqttStatus.value === 'up') return '已连接'
  if (mqttStatus.value === 'down') return '未连接'
  return '未配置'
})

async function fetchHealth() {
  try {
    const res = await getExtensionHealth()
    const d = res.data || {}
    Object.keys(data).forEach((k) => delete data[k])
    Object.assign(data, d)
    latencyMs.value = d.latencyMs
    lastCheckedAt.value = d.checkedAt ? new Date(d.checkedAt).toLocaleTimeString() : ''
    loadError.value = ''
  } catch (e) {
    loadError.value = '健康数据获取失败：' + (e.message || '网络异常')
  }
}
// ===== 129 主机资源 + 双机对比 =====
const sysInfo = computed(() => data.systemInfo || {})
const hostHw = computed(() => data.hostHardware || {})
const hwMemPercent = computed(() => {
  const m = hostHw.value.memory || {}
  if (!m.totalBytes) return null
  return Math.round(((m.totalBytes - (m.availableBytes || 0)) * 100) / m.totalBytes)
})
function fmtGb(bytes) {
  if (bytes == null || Number.isNaN(Number(bytes))) return '—'
  return (Number(bytes) / 1024 / 1024 / 1024).toFixed(1) + ' GB'
}
const memPercent = computed(() => {
  const p = sysInfo.value.memory?.usagePercent
  return p == null ? null : Number(p)
})
const memText = computed(() => {
  const m = sysInfo.value.memory || {}
  if (m.usedBytes == null || m.totalBytes == null) return '—'
  return (m.usedBytes / 1073741824).toFixed(1) + ' / ' + (m.totalBytes / 1073741824).toFixed(1) + ' GB'
})

// 123 主站侧数据复用诊断中心 summary（admin/researcher 同权限），失败静默降级为“—”
const host123 = ref({})
function fetchHost123() {
  getDiagnosisSummary({ hours: 24 }).then(res => {
    host123.value = res.data || {}
  }).catch(() => {})
}
const pct = (v) => v == null ? '—' : Math.round(v) + '%'
const compareRows = computed(() => [
  { item: '内存使用率', host123: pct(host123.value.server?.mem?.usage), host129: memPercent.value == null ? '不可用' : memPercent.value + '%' },
  { item: 'JVM 使用率', host123: pct(host123.value.server?.jvm?.usage), host129: '独立服务机' },
  // 量纲说明：123 是 CPU 使用率百分比，129 的 load5 是运行队列负载均值（非百分比），不可直接比较
  { item: 'CPU 使用率% / 负载数值(5min)', host123: host123.value.server?.cpu?.used == null ? '—' : pct(host123.value.server.cpu.used), host129: sysInfo.value.cpu?.load5 != null ? fmtNum(sysInfo.value.cpu.load5) + '（负载）' : '不可用' },
  { item: '判题排队', host123: String(sysInfo.value.judgePipeline?.queueSize ?? '—'), host129: '由 Judge0 管理' }
])
function fmtNum(v) {
  const n = Number(v)
  return isNaN(n) ? '—' : n.toFixed(2)
}

fetchHealth()
fetchHost123()
timer = setInterval(() => { if (autoRefresh.value) fetchHealth() }, 10000)
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<style lang="scss" scoped>
.head-bar {
  margin-bottom: 16px;
  .title {
    font-size: 18px;
    font-weight: 600;
    margin-right: 12px;
  }
  .sub {
    color: #909399;
    font-size: 12px;
  }
}
.card-title {
  font-weight: 600;
}
.status-line {
  margin: 0 0 8px;
}
.kv {
  display: flex;
  justify-content: space-between;
  line-height: 28px;
  span { color: #909399; }
  b { font-weight: 500; }
}
.err {
  margin-top: 8px;
  padding: 6px 8px;
  background: #fef0f0;
  color: #f56c6c;
  border-radius: 4px;
  font-size: 12px;
  word-break: break-all;
}
.host-dash {
  margin-bottom: 16px;
  .card-title { font-weight: 600; }
  .res-block { padding: 4px 0; }
  .res-label { color: #909399; font-size: 12px; margin-bottom: 6px; }
  .res-value .el-tag { margin-right: 6px; }
  .res-sub { color: #606266; font-size: 12px; line-height: 20px; word-break: break-all; }
}
</style>
