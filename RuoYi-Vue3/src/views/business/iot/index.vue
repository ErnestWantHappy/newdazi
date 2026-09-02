<template>
  <div class="iot-page">
    <div class="page-heading">
      <div>
        <p class="eyebrow">CLASSROOM MQTT</p>
        <h2>课程物联网实验</h2>
        <p class="muted">学生继续使用 Mind+ 和掌控板；平台统一管理班级分组、6 位课堂口令与上报数据接收。</p>
      </div>
      <div class="heading-actions">
        <el-button icon="Refresh" :loading="loading" @click="refreshData">刷新数据</el-button>
        <el-button type="primary" icon="Plus" :disabled="!lessonId" @click="experimentDialog = true">新建实验</el-button>
      </div>
    </div>

    <el-alert v-if="errorMessage" :title="errorMessage" type="warning" show-icon :closable="false" class="mb-3" />

    <el-alert
      v-if="!lessonId"
      title="请从教师首页对应课程的“物联”入口进入实验配置。"
      type="info"
      show-icon
      :closable="false"
      class="mb-3"
    />

    <template v-else>
      <!-- 课程、实验与班级筛选栏 -->
      <el-card shadow="never" class="control-panel mb-3">
        <el-form inline @submit.prevent>
          <el-form-item label="当前课程">
            <el-tag type="primary" effect="plain" class="lesson-tag">{{ lessonTitle || `课程 #${lessonId}` }}</el-tag>
          </el-form-item>
          <el-form-item label="实验项目">
            <el-select v-model="experimentId" placeholder="选择实验" filterable style="min-width: 220px" @change="onExperimentChange">
              <el-option v-for="item in experiments" :key="item.experimentId" :label="item.title" :value="item.experimentId" />
            </el-select>
          </el-form-item>
          <el-form-item label="授课班级">
            <el-select v-model="selectedClassKey" placeholder="选择班级" style="min-width: 180px" @change="onClassChange">
              <el-option
                v-for="item in assignedClasses"
                :key="`${item.entryYear}_${item.classCode}`"
                :label="item.className || `${item.entryYear}级${item.classCode}班`"
                :value="`${item.entryYear}_${item.classCode}`"
              />
            </el-select>
          </el-form-item>
          <el-tag type="success" effect="light" class="broker-tag">
            Broker: {{ dashboard?.brokerUrl || '10.52.1.129' }}:{{ dashboard?.brokerPort || 1883 }}（标准 EMQX）
          </el-tag>
        </el-form>
      </el-card>

      <!-- 班级物联配置与分组控制台 -->
      <el-card v-if="currentClass" shadow="never" class="class-console-card mb-3">
        <template #header>
          <div class="card-header">
            <div class="header-title">
              <span class="class-title">{{ currentClass.entryYear }}级{{ currentClass.classCode }}班 物联配置</span>
              <el-tag v-if="classConfig?.groupedAt" type="success" size="small">
                已生成第 {{ classConfig.groupVersion || 1 }} 版分组快照 ({{ currentGroups.length }} 组)
              </el-tag>
              <el-tag v-else type="info" size="small">尚未生成分组</el-tag>
              <el-tag
                v-if="classConfig?.groupedAt"
                :type="brokerStatusType(classConfig.brokerSyncStatus)"
                size="small"
              >
                {{ brokerStatusLabel(classConfig.brokerSyncStatus) }}
              </el-tag>
            </div>
            <div class="header-actions">
              <el-button
                type="success"
                icon="FullScreen"
                :disabled="!classConfig?.groupedAt || classConfig?.brokerSyncStatus !== 'SYNCED'"
                @click="openClassCard"
              >
                课堂配置卡 (投屏/打印)
              </el-button>
            </div>
          </div>
        </template>

        <div class="console-body">
          <el-alert
            v-if="classConfig?.groupedAt && classConfig?.brokerSyncStatus !== 'SYNCED'"
            class="mb-3"
            :type="classConfig?.brokerSyncStatus === 'FAILED' ? 'error' : 'warning'"
            :closable="false"
            show-icon
          >
            <template #title>
              {{ classConfig?.brokerSyncError || 'MQTT 账号与精确 Topic 权限正在等待同步' }}
              <el-button
                link
                type="primary"
                :loading="syncingBroker"
                @click="retryBrokerSync"
              >
                重试同步
              </el-button>
            </template>
          </el-alert>
          <el-row :gutter="20" class="align-center">
            <!-- 班级账号与 6 位口令 -->
            <el-col :xs="24" :sm="12" :lg="10">
              <div class="credential-box">
                <div class="cred-item">
                  <span class="cred-label">班级 MQTT 账号：</span>
                  <code class="cred-code">{{ classConfig?.mqttUsername || '未生成' }}</code>
                  <el-button
                    v-if="classConfig?.mqttUsername"
                    link
                    type="primary"
                    icon="CopyDocument"
                    @click="copyText(classConfig.mqttUsername, '班级账号')"
                  />
                </div>
                <div class="cred-item mt-2">
                  <span class="cred-label">6 位课堂口令：</span>
                  <span class="passcode-display">{{ classConfig?.passcode || '------' }}</span>
                  <el-button
                    v-if="classConfig?.passcode"
                    link
                    type="primary"
                    icon="CopyDocument"
                    @click="copyText(classConfig.passcode, '课堂口令')"
                  />
                  <el-button
                    v-if="classConfig?.groupedAt"
                    link
                    type="danger"
                    icon="RefreshRight"
                    :loading="rotating"
                    @click="confirmRotatePasscode"
                  >
                    轮换口令
                  </el-button>
                </div>
              </div>
            </el-col>

            <!-- 分组人数与生成按钮 -->
            <el-col :xs="24" :sm="12" :lg="14">
              <div class="group-action-box">
                <div class="group-size-form">
                  <span class="form-label">每组人数：</span>
                  <el-input-number
                    v-model="groupSize"
                    :min="1"
                    :max="20"
                    :step="1"
                    size="default"
                    style="width: 130px;"
                  />
                  <span class="group-hint">（系统按学号从小到大连续分配）</span>
                </div>
                <div class="group-btn-wrap">
                  <el-button
                    :type="classConfig?.groupedAt ? 'warning' : 'primary'"
                    :icon="classConfig?.groupedAt ? 'Refresh' : 'MagicStick'"
                    :loading="groupingLoading"
                    @click="handleGroupingClick"
                  >
                    {{ classConfig?.groupedAt ? '重新生成分组' : '自动生成分组' }}
                  </el-button>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>

      <!-- 小组数据总览：合并了原小组列表、最近消息流与数据收集汇总，一组一行 -->
      <el-card v-if="currentClass" shadow="never" class="section-card overview-card mb-3">
        <template #header>
          <div class="card-header">
            <div class="header-title">
              <span class="class-title">小组数据总览</span>
              <el-tag size="small" type="info">{{ currentGroups.length }} 组 / {{ totalGroupStudents }} 人</el-tag>
              <el-tag size="small" type="success">共接收 {{ classTotalMessages }} 条数据</el-tag>
              <el-switch v-model="onlyWithData" size="small" active-text="只看有数据" />
            </div>
            <el-tag v-if="hasRecentActivity" size="small" type="success" effect="plain">实时接收中</el-tag>
            <el-tag v-else size="small" type="info" effect="plain">
              {{ lastClassReceivedAt ? `最近接收 ${formatClock(lastClassReceivedAt)}` : '尚未接收数据' }}
            </el-tag>
          </div>
        </template>
        <el-table
          :data="filteredGroups"
          size="small"
          empty-text="当前班级暂无分组，请点击上方“自动生成分组”"
          stripe
        >
          <el-table-column label="小组" width="110">
            <template #default="{ row }">
              <div class="group-cell">
                <strong>{{ row.groupName }}</strong>
                <span class="group-code">{{ row.groupCode }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="组员" min-width="160">
            <template #default="{ row }">
              <div class="members-tag-list">
                <el-tag
                  v-for="s in row.studentList"
                  :key="s.studentId"
                  size="small"
                  effect="plain"
                  class="member-tag"
                >
                  {{ s.studentNo }} {{ s.studentName }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="Topic" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="topic-text">{{ row.topic }}</span>
              <el-button link type="primary" icon="CopyDocument" @click="copyText(row.topic, 'Topic')" />
            </template>
          </el-table-column>
          <el-table-column label="在线" width="75">
            <template #default="{ row }">
              <el-tag :type="isOnline(row) ? 'success' : 'info'" size="small">
                {{ isOnline(row) ? '在线' : '离线' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="收到数据" width="105" align="center" sortable :sort-method="sortByMessageCount">
            <template #default="{ row }">
              <el-tag :type="(statsOf(row)?.messageCount || 0) > 0 ? 'success' : 'info'" size="small">
                {{ statsOf(row)?.messageCount || 0 }} 条
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="最新数据" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="statsOf(row)?.lastPayload != null" class="latest-payload">{{ formatPayload(statsOf(row).lastPayload, statsOf(row).lastPayloadType) }}</span>
              <span v-else class="muted">尚未收到数据</span>
            </template>
          </el-table-column>
          <el-table-column label="最近接收时间" width="160">
            <template #default="{ row }">
              <span v-if="statsOf(row)?.lastReceivedAt">{{ formatTime(statsOf(row).lastReceivedAt) }}</span>
              <span v-else class="muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="110" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" icon="DataAnalysis" @click="openGroupDetail(row)">
                查看数据
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 小组详情使用卡片弹窗，教师关闭后仍停留在当前班级总览。 -->
      <el-dialog
        v-model="detailMode"
        :title="detailGroup ? `${detailGroup.groupName} · 全部数据` : '小组数据'"
        width="min(1000px, 92vw)"
        top="5vh"
        destroy-on-close
        append-to-body
        @closed="backToOverview"
      >
        <template #header>
          <div class="card-header">
            <div class="header-title">
              <span class="class-title">{{ detailGroup?.groupName || '小组' }} 全部数据</span>
              <el-tag v-if="detailGroup" size="small" type="info">{{ detailGroup.groupCode }}</el-tag>
              <el-tag v-if="detailGroup" size="small" :type="isOnline(detailGroup) ? 'success' : 'info'">
                {{ isOnline(detailGroup) ? '在线' : '离线' }}
              </el-tag>
            </div>
            <span class="muted small">共 {{ detailTotal }} 条</span>
          </div>
        </template>

        <div v-if="detailGroup" class="detail-dialog-body">
        <div class="detail-meta mb-3">
          <span class="muted small">组员：</span>
          <el-tag
            v-for="s in detailGroup.studentList"
            :key="s.studentId"
            size="small"
            effect="plain"
            class="member-tag"
          >
            {{ s.studentNo }} {{ s.studentName }}
          </el-tag>
          <span class="topic-wrap">
            <span class="muted small">Topic：</span>
            <span class="topic-text">{{ detailGroup.topic }}</span>
            <el-button link type="primary" icon="CopyDocument" @click="copyText(detailGroup.topic, 'Topic')" />
          </span>
        </div>

        <!-- 最新数据横幅：不受下方筛选影响，始终展示该小组最新一条上报 -->
        <div class="latest-banner mb-3">
          <span class="latest-label">最新数据：</span>
          <template v-if="detailLatest">
            <code class="latest-value">{{ formatPayload(detailLatest.lastPayload, detailLatest.lastPayloadType) }}</code>
            <el-tag size="small" type="primary" effect="plain">{{ detailLatest.lastPayloadType || 'TEXT' }}</el-tag>
            <span class="muted small">{{ formatTime(detailLatest.lastReceivedAt) }}</span>
          </template>
          <span v-else class="muted">尚未收到该小组数据</span>
        </div>

        <div class="collect-toolbar">
          <el-select v-model="detailFilter.payloadType" clearable placeholder="全部格式" style="width: 130px" @change="loadDetailPage(1)">
            <el-option label="TEXT" value="TEXT" />
            <el-option label="JSON" value="JSON" />
            <el-option label="NUMBER" value="NUMBER" />
          </el-select>
          <el-input
            v-model="detailFilter.keyword"
            placeholder="搜索数据内容 / 设备"
            clearable
            style="width: 220px"
            @keyup.enter="loadDetailPage(1)"
            @clear="loadDetailPage(1)"
          />
          <el-button type="primary" plain icon="Search" @click="loadDetailPage(1)">查询</el-button>
          <el-button icon="Refresh" @click="loadDetailPage()">刷新</el-button>
        </div>

        <el-table
          v-loading="detailLoading"
          :data="detailRows"
          size="small"
          class="detail-table"
          empty-text="暂无数据。学生设备经 MQTT 上报后，这里才会出现记录。"
          max-height="480"
        >
          <el-table-column label="接收时间" width="170">
            <template #default="{ row }">{{ formatTime(row.receivedAt) }}</template>
          </el-table-column>
          <el-table-column label="格式" width="145">
            <template #default="{ row }">
              <el-tag size="small" effect="plain">{{ row.payloadType || 'TEXT' }}</el-tag>
              <span v-if="row.deviceCode && row.deviceCode !== 'data'" class="device-name">{{ row.deviceCode }}</span>
            </template>
          </el-table-column>
          <el-table-column label="数据内容" min-width="240" show-overflow-tooltip>
            <template #default="{ row }"><pre class="payload-display">{{ formatPayload(payloadOf(row), row.payloadType) }}</pre></template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-if="detailTotal > 0"
          class="collect-pagination"
          layout="total, prev, pager, next"
          :total="detailTotal"
          :page-size="detailPageSize"
          :current-page="detailPage"
          @current-change="loadDetailPage"
        />
        </div>
      </el-dialog>
    </template>

    <!-- 新建实验对话框 -->
    <el-dialog v-model="experimentDialog" title="新建物联网实验" width="480px" destroy-on-close>
      <el-form label-width="90px">
        <el-form-item label="活动编码" required>
          <el-input v-model="experimentForm.activityCode" placeholder="例如 exp01 或 light01" />
        </el-form-item>
        <el-form-item label="实验名称" required>
          <el-input v-model="experimentForm.title" placeholder="例如 光照传感器采集实验" />
        </el-form-item>
        <el-form-item label="实验说明">
          <el-input v-model="experimentForm.description" type="textarea" :rows="3" placeholder="请输入实验要求与说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="experimentDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitExperiment">创建实验</el-button>
      </template>
    </el-dialog>

    <!-- 课堂物联配置卡（投屏/打印） -->
    <el-dialog
      v-model="classCardDialog"
      title="班级物联配置卡 (课堂投屏 / 打印)"
      width="820px"
      top="5vh"
      class="class-card-modal"
    >
      <div v-if="classCard" id="print-area" class="projection-card">
        <div class="card-top-banner">
          <h3 class="card-exp-title">{{ classCard.experimentTitle }}</h3>
          <p class="card-class-subtitle">{{ classCard.entryYear }}级{{ classCard.classCode }}班 课堂配置卡</p>
        </div>

        <el-tabs v-model="classCardMode">
          <el-tab-pane label="初中 Mind+" name="mindplus">
        <el-descriptions :column="2" border class="server-meta-table mb-3">
          <el-descriptions-item label="MQTT 服务器">
            <span class="meta-val highlight">{{ classCard.brokerUrl }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="端口">
            <span class="meta-val highlight">{{ classCard.brokerPort }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="班级 MQTT 账号">
            <span class="meta-val">{{ classCard.mqttUsername }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="6 位课堂口令">
            <span class="meta-val passcode-large">{{ classCard.passcode }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="分组人数">每组 {{ classCard.groupSize }} 人</el-descriptions-item>
          <el-descriptions-item label="班级统计">共 {{ classCard.groupCount }} 组 / {{ classCard.studentCount }} 人</el-descriptions-item>
        </el-descriptions>

        <div class="group-projection-grid">
          <div v-for="g in classCard.groups" :key="g.groupId" class="proj-group-card">
            <div class="proj-group-header">
              <span class="p-group-title">{{ g.groupName }}</span>
              <span class="p-group-code">{{ g.groupCode }}</span>
            </div>
            <div class="proj-group-topic">
              <span class="p-topic-label">Topic:</span>
              <code class="p-topic-val">{{ g.topic }}</code>
            </div>
            <div class="proj-group-members">
              <span class="p-member-label">组员：</span>
              <span class="p-member-names">{{ (g.memberNames || []).join('、') }}</span>
            </div>
          </div>
        </div>

        <div class="card-footer-notice">
          <el-alert
            type="info"
            :closable="false"
            show-icon
            title="提示：同班学生使用相同的 MQTT 账号和课堂口令；各组请在 Mind+ SIoT 模块中配置对应的专属 Topic。"
          />
        </div>
          </el-tab-pane>

          <el-tab-pane label="小学实验板 Python" name="python">
            <el-alert
              type="success"
              :closable="false"
              show-icon
              title="已使用与初中相同的班级账号和小组 Topic。选择小组复制代码，再在 N17 工具中填写本地 2.4G WiFi。"
              class="mb-3"
            />
            <el-descriptions :column="2" border class="server-meta-table mb-3">
              <el-descriptions-item label="MQTT 服务器"><code>{{ classCard.brokerUrl }}</code></el-descriptions-item>
              <el-descriptions-item label="端口"><code>{{ classCard.brokerPort }}</code></el-descriptions-item>
              <el-descriptions-item label="班级账号"><code>{{ classCard.mqttUsername }}</code></el-descriptions-item>
              <el-descriptions-item label="课堂口令"><span class="passcode-large">{{ classCard.passcode }}</span></el-descriptions-item>
            </el-descriptions>
            <div class="group-projection-grid">
              <div v-for="g in classCard.groups" :key="`python-${g.groupId}`" class="proj-group-card">
                <div class="proj-group-header">
                  <span class="p-group-title">{{ g.groupName }}</span>
                  <el-button type="primary" size="small" icon="CopyDocument" @click="copyPrimaryPython(g)">
                    复制本组 Python 代码
                  </el-button>
                </div>
                <div class="proj-group-topic"><span class="p-topic-label">ClientID:</span> <code>{{ g.pythonClientId }}</code></div>
                <div class="proj-group-topic"><span class="p-topic-label">Topic:</span> <code class="p-topic-val">{{ g.topic }}</code></div>
                <div class="proj-group-members">{{ (g.memberNames || []).join('、') }}</div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <template #footer>
        <el-button icon="CopyDocument" @click="copyAllCardText">一键复制配置文本</el-button>
        <el-button type="primary" icon="Printer" @click="printCard">打印 / 保存 PDF</el-button>
        <el-button @click="classCardDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createIotExperiment,
  generateIotClassGrouping,
  getIotClassCard,
  getIotClassConfig,
  getIotDashboard,
  listIotExperiments,
  listIotGroups,
  listIotLessonClasses,
  listIotMessages,
  rotateIotClassPasscode,
  syncIotClassBroker
} from '@/api/business/iot'
import { copyToClipboard } from '@/utils/clipboard'
import { buildPrimaryIotPythonCode } from '@/utils/iotPythonTemplate'

const query = new URLSearchParams(window.location.search)
const lessonId = ref(Number(query.get('lessonId')) || undefined)
const lessonTitle = ref(query.get('lessonTitle') || '')
const initialEntryYear = query.get('entryYear') || ''
const initialClassCode = query.get('classCode') || ''

const experiments = ref([])
const experimentId = ref()
const assignedClasses = ref([])
const selectedClassKey = ref('')

const classConfig = ref(null)
const currentGroups = ref([])
const dashboard = ref(null)

const groupSize = ref(4)
const loading = ref(false)
const saving = ref(false)
const groupingLoading = ref(false)
const rotating = ref(false)
const syncingBroker = ref(false)
const errorMessage = ref('')

const experimentDialog = ref(false)
const classCardDialog = ref(false)
const classCard = ref(null)
const classCardMode = ref('mindplus')

const experimentForm = reactive({ activityCode: '', title: '', description: '' })

// 小组汇总统计（每组接收条数、最近接收时间、最新数据），随实时推送自动刷新
const groupStats = ref([])

// 小组详情视图（点进具体小组查看其全部上报数据）
const detailMode = ref(false)
const detailGroup = ref(null)
const detailRows = ref([])
const detailTotal = ref(0)
const detailPage = ref(1)
const detailPageSize = ref(20)
const detailLoading = ref(false)
const detailFilter = reactive({ payloadType: '', keyword: '' })
const onlyWithData = ref(false)
const clockNow = ref(Date.now())

let socket = null
let clockTimer = null

const currentClass = computed(() => {
  if (!selectedClassKey.value) return null
  const [entryYear, classCode] = selectedClassKey.value.split('_')
  return { entryYear, classCode }
})

const totalGroupStudents = computed(() => {
  return currentGroups.value.reduce((sum, g) => sum + (g.studentList?.length || 0), 0)
})

const classTotalMessages = computed(() => {
  return groupStats.value.reduce((sum, s) => sum + Number(s.messageCount || 0), 0)
})

const filteredGroups = computed(() => {
  if (!onlyWithData.value) return currentGroups.value
  return currentGroups.value.filter(group => Number(statsOf(group)?.messageCount || 0) > 0)
})

const lastClassReceivedAt = computed(() => {
  return groupStats.value
    .map(item => item.lastReceivedAt)
    .filter(Boolean)
    .sort((left, right) => new Date(right).getTime() - new Date(left).getTime())[0] || ''
})

const hasRecentActivity = computed(() => {
  if (!lastClassReceivedAt.value) return false
  return clockNow.value - new Date(lastClassReceivedAt.value).getTime() < 120000
})

const statsMap = computed(() => {
  const map = {}
  for (const s of groupStats.value) map[s.groupId] = s
  return map
})

const detailLatest = computed(() => {
  if (!detailGroup.value) return null
  const stats = statsMap.value[detailGroup.value.groupId]
  return stats && stats.lastPayload != null ? stats : null
})

onMounted(async () => {
  clockTimer = window.setInterval(() => { clockNow.value = Date.now() }, 30000)
  await loadExperimentsAndClasses()
})

onBeforeUnmount(() => {
  if (socket) socket.close()
  if (clockTimer) window.clearInterval(clockTimer)
})

function statsOf(group) {
  return statsMap.value[group.groupId] || null
}

async function loadExperimentsAndClasses() {
  if (!lessonId.value) return
  loading.value = true
  try {
    const [expRes, classRes] = await Promise.all([
      listIotExperiments(lessonId.value),
      listIotLessonClasses(lessonId.value)
    ])
    experiments.value = expRes?.data || []
    assignedClasses.value = classRes?.data || []

    if (!experimentId.value && experiments.value.length) {
      experimentId.value = experiments.value[0].experimentId
    }

    if (!selectedClassKey.value && assignedClasses.value.length) {
      const initialClass = assignedClasses.value.find(item =>
        String(item.entryYear) === String(initialEntryYear)
        && String(item.classCode) === String(initialClassCode)
      )
      const target = initialClass || assignedClasses.value[0]
      selectedClassKey.value = `${target.entryYear}_${target.classCode}`
    }

    await loadClassData()
  } catch (error) {
    errorMessage.value = error?.message || '实验或班级列表加载失败'
  } finally {
    loading.value = false
  }
}

async function loadClassData() {
  if (!experimentId.value) {
    dashboard.value = null
    classConfig.value = null
    currentGroups.value = []
    groupStats.value = []
    return
  }

  const cls = currentClass.value
  try {
    if (cls) {
      const [cfgRes, grpRes, dashRes] = await Promise.all([
        getIotClassConfig(experimentId.value, cls.entryYear, cls.classCode),
        listIotGroups(experimentId.value, cls.entryYear, cls.classCode),
        getIotDashboard(experimentId.value, { entryYear: cls.entryYear, classCode: cls.classCode, limit: 1 })
      ])
      classConfig.value = cfgRes?.data || null
      if (classConfig.value?.groupSize) {
        groupSize.value = classConfig.value.groupSize
      }
      currentGroups.value = grpRes?.data || []
      dashboard.value = dashRes?.data || null
      await loadGroupStats()
    } else {
      const dashRes = await getIotDashboard(experimentId.value, { limit: 1 })
      dashboard.value = dashRes?.data || null
      currentGroups.value = dashboard.value?.groups || []
      groupStats.value = []
    }
    connectRealtime()
    errorMessage.value = ''
  } catch (error) {
    errorMessage.value = error?.message || '物联数据加载失败'
  }
  // 若当前停留在小组详情页，主数据刷新时同步刷新明细
  if (detailMode.value && detailGroup.value) {
    loadDetailPage()
  }
}

/** 加载当前班级各小组汇总统计（含每组最新数据），pageSize=1 只为取 stats。 */
async function loadGroupStats() {
  const cls = currentClass.value
  if (!experimentId.value || !cls) {
    groupStats.value = []
    return
  }
  try {
    const res = await listIotMessages(experimentId.value, {
      entryYear: cls.entryYear,
      classCode: cls.classCode,
      pageNum: 1,
      pageSize: 1
    })
    groupStats.value = res?.data?.stats || []
  } catch (error) {
    groupStats.value = []
  }
}

/** 打开小组详情：查看该小组全部上报数据。 */
function openGroupDetail(group) {
  detailGroup.value = group
  detailMode.value = true
  detailFilter.payloadType = ''
  detailFilter.keyword = ''
  loadDetailPage(1)
}

function backToOverview() {
  detailMode.value = false
  detailGroup.value = null
  detailRows.value = []
  detailTotal.value = 0
}

/** 分页加载小组明细（按接收时间倒序）。 */
async function loadDetailPage(page) {
  if (!experimentId.value || !detailGroup.value) return
  if (typeof page === 'number') detailPage.value = page
  const cls = currentClass.value
  detailLoading.value = true
  try {
    const params = {
      pageNum: detailPage.value,
      pageSize: detailPageSize.value,
      groupId: detailGroup.value.groupId,
      payloadType: detailFilter.payloadType || undefined,
      keyword: detailFilter.keyword || undefined
    }
    if (cls) {
      params.entryYear = cls.entryYear
      params.classCode = cls.classCode
    }
    const res = await listIotMessages(experimentId.value, params)
    const payload = res?.data || {}
    detailRows.value = payload.rows || []
    detailTotal.value = Number(payload.total || 0)
    // stats 顺带返回，就地更新汇总，保证最新数据横幅常驻刷新
    if (payload.stats) groupStats.value = payload.stats
  } catch (error) {
    ElMessage.error(error?.message || '小组数据加载失败')
  } finally {
    detailLoading.value = false
  }
}

async function refreshData() {
  loading.value = true
  try {
    await loadClassData()
    ElMessage.success('数据已更新')
  } finally {
    loading.value = false
  }
}

function onExperimentChange() {
  backToOverview()
  detailPage.value = 1
  loadClassData()
}

function onClassChange() {
  backToOverview()
  detailPage.value = 1
  loadClassData()
}

async function submitExperiment() {
  if (!lessonId.value || !experimentForm.activityCode || !experimentForm.title) {
    return ElMessage.warning('请填写活动编码和实验名称')
  }
  saving.value = true
  try {
    await createIotExperiment({ lessonId: lessonId.value, ...experimentForm })
    experimentDialog.value = false
    Object.assign(experimentForm, { activityCode: '', title: '', description: '' })
    await loadExperimentsAndClasses()
    ElMessage.success('实验已创建')
  } catch (error) {
    ElMessage.error(error?.message || '实验创建失败')
  } finally {
    saving.value = false
  }
}

function handleGroupingClick() {
  const cls = currentClass.value
  if (!cls || !experimentId.value) return ElMessage.warning('请选择实验和班级')

  if (classConfig.value?.groupedAt) {
    ElMessageBox.confirm(
      '重新生成分组将覆盖当前班级已有的学生分组与设备对应关系，已在进行中的实验可能会受到影响。确定要重新分组吗？',
      '重新生成分组警告',
      {
        confirmButtonText: '确定重新分组',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      executeGrouping(true)
    }).catch(() => {})
  } else {
    executeGrouping(false)
  }
}

async function executeGrouping(force) {
  const cls = currentClass.value
  groupingLoading.value = true
  try {
    const res = await generateIotClassGrouping({
      experimentId: experimentId.value,
      entryYear: cls.entryYear,
      classCode: cls.classCode,
      groupSize: groupSize.value,
      force
    })
    ElMessage.success(`分组成功！共生成 ${res?.data?.totalGroups || 0} 个小组`)
    backToOverview()
    await loadClassData()
  } catch (error) {
    ElMessage.error(error?.message || '生成分组失败')
  } finally {
    groupingLoading.value = false
  }
}

function confirmRotatePasscode() {
  ElMessageBox.confirm(
    '轮换后旧口令将立即失效，所有学生和设备需使用新口令连接 MQTT。确定轮换吗？',
    '轮换课堂口令',
    {
      confirmButtonText: '确定轮换',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    const cls = currentClass.value
    rotating.value = true
    try {
      const res = await rotateIotClassPasscode({
        experimentId: experimentId.value,
        entryYear: cls.entryYear,
        classCode: cls.classCode
      })
      classConfig.value = res?.data || classConfig.value
      if (res?.data?.brokerSyncStatus === 'SYNCED') {
        ElMessage.success('课堂口令已轮换为：' + (res?.data?.passcode || ''))
      } else {
        ElMessage.error(res?.data?.brokerSyncError || '新口令已保存，但 MQTT 权限同步失败，请点击重试同步')
      }
    } catch (error) {
      ElMessage.error(error?.message || '口令轮换失败')
    } finally {
      rotating.value = false
    }
  }).catch(() => {})
}

async function openClassCard() {
  const cls = currentClass.value
  if (!cls || !experimentId.value) return
  try {
    const res = await getIotClassCard(experimentId.value, cls.entryYear, cls.classCode)
    classCard.value = res?.data || null
    classCardMode.value = 'mindplus'
    classCardDialog.value = true
  } catch (error) {
    ElMessage.error(error?.message || '获取配置卡失败')
  }
}

async function retryBrokerSync() {
  if (!classConfig.value?.configId) return
  syncingBroker.value = true
  try {
    const res = await syncIotClassBroker(classConfig.value.configId)
    classConfig.value = res?.data || classConfig.value
    if (classConfig.value?.brokerSyncStatus === 'SYNCED') {
      ElMessage.success('MQTT 账号与班级 Topic 权限同步成功')
    } else {
      ElMessage.error(classConfig.value?.brokerSyncError || '同步失败，请检查 EMQX 服务')
    }
  } catch (error) {
    ElMessage.error(error?.message || '同步失败，请稍后重试')
  } finally {
    syncingBroker.value = false
  }
}

function brokerStatusLabel(status) {
  if (status === 'SYNCED') return 'MQTT 权限已同步'
  if (status === 'FAILED') return 'MQTT 权限同步失败'
  return 'MQTT 权限待同步'
}

function brokerStatusType(status) {
  if (status === 'SYNCED') return 'success'
  if (status === 'FAILED') return 'danger'
  return 'warning'
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

function copyPrimaryPython(group) {
  if (!classCard.value || !group) return
  const c = classCard.value
  const code = buildPrimaryIotPythonCode({
    brokerUrl: c.brokerUrl,
    brokerPort: c.brokerPort,
    clientId: group.pythonClientId,
    username: c.mqttUsername,
    password: c.passcode,
    topic: group.topic
  })
  copyText(code, `${group.groupName} Python 代码`)
}

function copyAllCardText() {
  if (!classCard.value) return
  const c = classCard.value
  let text = `【${c.experimentTitle}】${c.entryYear}级${c.classCode}班 物联配置\n`
  text += `MQTT 服务器：${c.brokerUrl}\n`
  text += `端口：${c.brokerPort}\n`
  text += `班级账号：${c.mqttUsername}\n`
  text += `课堂口令：${c.passcode}\n\n`
  text += `小组列表与专属 Topic：\n`
  for (const g of c.groups || []) {
    text += `${g.groupName}（${(g.memberNames || []).join('、')}）：${g.topic}\n`
  }
  copyText(text, '全班配置卡文本')
}

function printCard() {
  window.print()
}

function connectRealtime() {
  if (!experimentId.value) return
  if (socket) socket.close()
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  socket = new WebSocket(`${protocol}//${window.location.host}/ws/iot/${experimentId.value}`)
  socket.onmessage = event => {
    try {
      if (JSON.parse(event.data).type === 'iot_refresh') {
        loadClassData()
      }
    } catch (_error) { }
  }
}

function sortByMessageCount(left, right) {
  return Number(statsOf(left)?.messageCount || 0) - Number(statsOf(right)?.messageCount || 0)
}

function payloadOf(row) {
  return row?.payloadText ?? row?.payloadNumber ?? ''
}

function formatPayload(value, type) {
  const normalizedType = String(type || 'TEXT').toUpperCase()
  if (normalizedType === 'NUMBER') return `数值 ${value ?? ''}`
  if (normalizedType === 'JSON') {
    try {
      const parsed = typeof value === 'string' ? JSON.parse(value) : value
      return JSON.stringify(parsed, null, 2)
    } catch (_error) {
      return String(value ?? '')
    }
  }
  const text = String(value ?? '')
  return text.length > 120 ? `${text.slice(0, 120)}…` : text
}

function formatTime(value) {
  return value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : ''
}

function formatClock(value) {
  return value ? new Date(value).toLocaleTimeString('zh-CN', { hour12: false }) : ''
}

function isOnline(row) {
  return row.lastSeenAt && Date.now() - new Date(row.lastSeenAt).getTime() < 120000
}
</script>

<style scoped>
.iot-page {
  padding: 20px;
  max-width: 1400px;
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

.small {
  font-size: 12px;
}

.mb-3 {
  margin-bottom: 16px;
}

.mt-2 {
  margin-top: 8px;
}

.control-panel :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 20px;
}

.lesson-tag {
  font-weight: 600;
}

.broker-tag {
  font-weight: 500;
}

.class-console-card {
  background: linear-gradient(135deg, #f8faff 0%, #f0f4fc 100%);
  border: 1px solid #dcdfe6;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.class-title {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}

.align-center {
  display: flex;
  align-items: center;
}

.credential-box {
  background: #ffffff;
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.cred-item {
  display: flex;
  align-items: center;
  font-size: 13px;
}

.cred-label {
  color: #606266;
  width: 120px;
}

.cred-code {
  font-family: Consolas, monospace;
  font-weight: 600;
  color: #409eff;
  background: #ecf5ff;
  padding: 2px 8px;
  border-radius: 4px;
}

.passcode-display {
  font-family: Consolas, monospace;
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 2px;
  color: #67c23a;
  background: #f0f9eb;
  padding: 2px 10px;
  border-radius: 4px;
}

.group-action-box {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 16px;
}

.group-size-form {
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-label {
  font-size: 13px;
  color: #606266;
}

.group-hint {
  font-size: 12px;
  color: #909399;
}

.section-card {
  border: 1px solid #e4e7ed;
}

.group-cell {
  display: flex;
  flex-direction: column;
  line-height: 1.4;
}

.group-code {
  font-size: 11px;
  color: #909399;
}

.latest-payload {
  font-family: Consolas, monospace;
  font-weight: 700;
  color: #409eff;
}

.device-name {
  display: block;
  margin-top: 4px;
  color: #606266;
  font-size: 11px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.payload-display {
  margin: 0;
  color: #303133;
  font: 12px/1.5 Consolas, "Courier New", monospace;
  white-space: pre-wrap;
  word-break: break-word;
}

.detail-dialog-body {
  min-height: 280px;
}

.detail-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}

.topic-wrap {
  display: inline-flex;
  align-items: center;
  margin-left: 12px;
}

.latest-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #f0f9eb;
  border: 1px solid #e1f3d8;
  border-radius: 6px;
  padding: 10px 16px;
}

.latest-label {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.latest-value {
  font-family: Consolas, monospace;
  font-size: 15px;
  font-weight: 700;
  color: #67c23a;
  background: #ffffff;
  padding: 2px 10px;
  border-radius: 4px;
  max-width: 480px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.collect-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.collect-pagination {
  margin-top: 12px;
  justify-content: flex-end;
}

.members-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.member-tag {
  background: #f4f4f5;
  color: #606266;
}

.topic-text {
  font-family: Consolas, monospace;
  font-size: 12px;
  color: #303133;
}

/* 课堂投屏配置卡样式 */
.projection-card {
  padding: 16px;
  background: #ffffff;
}

.card-top-banner {
  text-align: center;
  margin-bottom: 20px;
  border-bottom: 2px solid #409eff;
  padding-bottom: 12px;
}

.card-exp-title {
  font-size: 22px;
  font-weight: 800;
  color: #303133;
  margin: 0 0 6px 0;
}

.card-class-subtitle {
  font-size: 15px;
  color: #606266;
  margin: 0;
}

.meta-val {
  font-family: Consolas, monospace;
  font-weight: 600;
}

.meta-val.highlight {
  color: #409eff;
  font-size: 14px;
}

.passcode-large {
  font-size: 20px;
  font-weight: 800;
  color: #67c23a;
  letter-spacing: 2px;
}

.group-projection-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.proj-group-card {
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  padding: 12px;
  background: #fafafa;
}

.proj-group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.p-group-title {
  font-size: 15px;
  font-weight: 700;
  color: #303133;
}

.p-group-code {
  font-size: 12px;
  color: #909399;
}

.proj-group-topic {
  font-size: 12px;
  margin-bottom: 6px;
  word-break: break-all;
}

.p-topic-label {
  color: #909399;
}

.p-topic-val {
  font-family: Consolas, monospace;
  color: #409eff;
  font-weight: 600;
}

.proj-group-members {
  font-size: 12px;
  color: #606266;
}

.p-member-label {
  color: #909399;
}

@media print {
  body * {
    visibility: hidden;
  }
  #print-area, #print-area * {
    visibility: visible;
  }
  #print-area {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
  }
}
</style>
