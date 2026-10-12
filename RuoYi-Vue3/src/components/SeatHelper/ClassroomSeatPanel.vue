<template>
  <div v-if="data.enabled || loadError" class="class-seat-panel">
    <el-button v-if="loadError" size="small" @click="load">机位信息加载失败，点击重试</el-button>
    <div class="seat-panel-inner">
      <div class="panel-label">
        <el-icon class="panel-icon"><Monitor /></el-icon>
        <span class="label-text">机位管理</span>
      </div>

      <el-select
        v-if="data.rooms && data.rooms.length > 1"
        v-model="room"
        size="small"
        style="width: 140px"
        placeholder="选择机房"
        @change="changeRoom"
      >
        <el-option v-for="item in data.rooms" :key="item" :value="item" :label="item" />
      </el-select>
      <span v-else-if="room" class="single-room-badge">{{ room }}</span>

      <el-button
        size="small"
        type="primary"
        plain
        :disabled="historical || !identified.length"
        :loading="busy"
        @click="confirm(identified)"
      >
        一键确认常用机位 ({{ identified.length }})
      </el-button>

      <el-badge :value="changes" :hidden="!changes" type="danger" class="seat-badge-wrapper">
        <el-button
          size="small"
          :type="changes ? 'warning' : 'default'"
          plain
          @click="detailsVisible = true"
        >
          机位明细
        </el-button>
      </el-badge>
    </div>

    <!-- 机位核对与调整明细对话框 -->
    <el-dialog v-model="detailsVisible" title="班级机位明细与核对" width="min(920px, 96%)" append-to-body>
      <div class="dialog-tips-bar">
        <span>当前机房：<strong>{{ room || '未选择' }}</strong></span>
        <span class="tip-sub">已关联当前电脑的学生可一键固化为本机房常用机位。</span>
      </div>
      <el-table :data="data.rows || []" stripe size="small" style="width: 100%">
        <el-table-column prop="studentName" label="学生姓名" width="130">
          <template #default="{ row }">
            <strong>{{ row.studentName }}</strong>
          </template>
        </el-table-column>
        <el-table-column prop="seatName" label="当前检测机位" width="140">
          <template #default="{ row }">
            <span class="cell-seat" :class="{ 'is-none': !row.seatName }">{{ displaySeatName(row.seatName) || '未命名' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="localIp" label="内网 IP" width="160">
          <template #default="{ row }">
            <span class="cell-ip">{{ row.localIp || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="expectedSeat" label="常用机位" width="140">
          <template #default="{ row }">
            <span class="cell-expected">{{ displaySeatName(row.expectedSeat) || (row.expectedSeat ? '未命名' : '暂未绑定') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="seatTagType(row.state)" effect="plain">
              {{ seatStateLabels[row.state] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="快捷操作" min-width="180">
          <template #default="{ row }">
            <el-button
              v-if="!historical && row.deviceId"
              link
              type="primary"
              size="small"
              :disabled="busy || !ready"
              @click="rename(row)"
            >
              修改机位名
            </el-button>
            <el-button
              v-if="!historical && ['CHANGED', 'UNBOUND'].includes(row.state)"
              link
              type="success"
              size="small"
              :disabled="busy"
              @click="confirm([row])"
            >
              设为常用
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="detailsVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, watch, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Monitor } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { seatStateLabels, displaySeatName } from '@/utils/seatHelper'

const props = defineProps({
  lessonId: [String, Number],
  entryYear: String,
  classCode: String,
  historical: Boolean
})
const emit = defineEmits(['updated'])

const data = ref({ enabled: false })
const room = ref('')
const busy = ref(false)
const detailsVisible = ref(false)
const loadError = ref(false)
const ready = computed(() => data.value.enabled && !loadError.value && data.value.roomName === room.value)
let timer
let sequence = 0
let disposed = false

const identified = computed(() =>
  ready.value ? (data.value.rows || []).filter(r => ['NORMAL', 'UNBOUND', 'CHANGED'].includes(r.state)) : []
)

const changes = computed(() =>
  (data.value.rows || []).filter(r => ['CHANGED', 'OTHER_ROOM', 'SHARED_DEVICE'].includes(r.state)).length
)

const scope = () => ({
  lessonId: props.lessonId,
  entryYear: props.entryYear,
  classCode: props.classCode,
  roomName: room.value
})

function seatTagType(state) {
  if (state === 'NORMAL') return 'success'
  if (state === 'CHANGED' || state === 'OTHER_ROOM') return 'warning'
  if (state === 'SHARED_DEVICE') return 'danger'
  return 'info'
}

async function load() {
  const ticket = ++sequence
  if (props.historical || !props.lessonId || !props.entryYear || !props.classCode) {
    data.value = { enabled: false }
    emit('updated', [])
    return
  }
  try {
    const response = await request({ url: '/business/seat-helper/classroom', params: scope() })
    if (disposed || ticket !== sequence) return
    data.value = response.data || { enabled: false }
    loadError.value = false
    room.value = data.value.roomName || room.value
    emit('updated', data.value.rows || [])
  } catch {
    if (!disposed && ticket === sequence) {
      loadError.value = true
    }
  }
}

function changeRoom() {
  // 旧机房名单不能在新机房请求完成前用于确认或改名。
  data.value = { ...data.value, roomName: null, rows: [] }
  load()
}

async function confirm(rows) {
  if (busy.value || !ready.value || !rows.length) return
  const initial = JSON.stringify(scope())
  busy.value = true
  try {
    await ElMessageBox.confirm(
      `将确认 ${rows.length} 名学生的当前机位为本机房常用机位。未识别和缺席学生的原记录会保留。`,
      '确认常用机位',
      { type: 'info', confirmButtonText: '确认', cancelButtonText: '取消' }
    )
  } catch {
    busy.value = false
    return
  }
  if (initial !== JSON.stringify(scope())) { busy.value = false; return }
  busy.value = true
  try {
    const response = await request({
      url: '/business/seat-helper/classroom/confirm',
      method: 'post',
      data: {
        ...scope(),
        students: rows.map(r => ({ studentId: r.studentId, deviceId: r.deviceId }))
      }
    })
    ElMessage.success(`已确认 ${response.data?.confirmed ?? 0} 人；其余记录保留。`)
    await load()
  } catch {
    await load()
  } finally {
    busy.value = false
  }
}

watch(
  () => [props.lessonId, props.entryYear, props.classCode, props.historical],
  () => {
    clearInterval(timer)
    room.value = ''
    data.value = { enabled: false }
    loadError.value = false
    load()
    if (!disposed && !props.historical && props.lessonId && props.entryYear && props.classCode) {
      timer = setInterval(load, 30000)
    }
  },
  { immediate: true }
)

async function rename(row) {
  if (busy.value || props.historical || !ready.value) return
  const initial = JSON.stringify(scope())
  busy.value = true
  try {
    const selectedRoom = await ElMessageBox.prompt(
      '填写这台电脑所属机房：',
      '设置机房',
      {
        inputValue: row.roomName,
        inputValidator: val => !!val?.trim() && val.trim().length <= 80
      }
    )
    const selectedSeat = await ElMessageBox.prompt(
      '填写便于课堂核对的机位名称（如01机、A03等）：',
      '设置机位',
      {
        inputValue: displaySeatName(row.seatName),
        inputValidator: val => !!val?.trim() && val.trim().length <= 40
      }
    )
    if (initial !== JSON.stringify(scope())) return
    busy.value = true
    await request({
      url: '/business/seat-helper/classroom/device',
      method: 'post',
      data: {
        ...scope(),
        deviceId: row.deviceId,
        roomName: selectedRoom.value.trim(),
        seatName: selectedSeat.value.trim()
      }
    })
    if (initial !== JSON.stringify(scope())) return
    room.value = selectedRoom.value.trim()
    await load()
    ElMessage.success('机位名称已设置')
  } catch {
  } finally {
    busy.value = false
  }
}

onBeforeUnmount(() => {
  disposed = true
  sequence++
  clearInterval(timer)
})
</script>

<style scoped>
.class-seat-panel {
  display: inline-flex;
  align-items: center;
}

.seat-panel-inner {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 4px 12px;
  border-radius: 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.panel-label {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  font-weight: 600;
  color: #334155;
}

.panel-icon {
  font-size: 15px;
  color: #0284c7;
}

.single-room-badge {
  padding: 2px 8px;
  border-radius: 4px;
  background: #e0f2fe;
  color: #0369a1;
  font-size: 12px;
  font-weight: 500;
}

.dialog-tips-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  margin-bottom: 12px;
  background: #f1f5f9;
  border-radius: 6px;
  font-size: 13px;
  color: #334155;
}

.tip-sub {
  color: #64748b;
  font-size: 12px;
}

.cell-seat {
  font-weight: 600;
  color: #0f172a;
}

.cell-seat.is-none {
  color: #94a3b8;
  font-weight: normal;
}

.cell-ip {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
  color: #475569;
}

.cell-expected {
  color: #64748b;
}

.seat-badge-wrapper :deep(.el-badge__content) {
  top: 4px;
  right: 2px;
}
</style>
