<template>
  <div v-if="status.enabled || message" class="student-seat-helper">
    <template v-if="status.associated">
      <el-tooltip :content="message || `已关联当前电脑。机位：${seatLabel}；内网IP：${status.localIp || '获取中'}。仅用于课堂机位管理。`" placement="bottom">
        <div class="seat-status-pill" :class="status.online && !message ? 'is-online' : 'is-offline'">
          <span class="presence-dot" />
          <span class="seat-text">{{ seatLabel }}</span>
          <span v-if="status.localIp" class="ip-text">{{ status.localIp }}</span>
          <el-tag v-if="message || !status.online" size="small" type="warning" effect="dark" class="offline-tag">{{ message ? '状态待核对' : '助手离线' }}</el-tag>
        </div>
      </el-tooltip>
    </template>
    <template v-else>
      <div class="seat-unbound-pill">
        <span class="unbound-hint">{{ message || '机位未关联' }}</span>
        <el-button
          size="small"
          type="primary"
          link
          class="download-btn"
          @click="Download.zip('/business/seat-helper/student/download', 'SeatHelper.exe')"
        >
          下载机位助手
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, inject, ref } from 'vue'
import Download from '@/plugins/download'
import { displaySeatName } from '@/utils/seatHelper'

// 首页仅显示状态，续期由学生布局维护，不受子页面卸载影响
const { status, message } = inject('student-seat-helper', {
  status: ref({ enabled: false }),
  message: ref('')
})
const seatLabel = computed(() => {
  const seat = displaySeatName(status.value.seatName)
  const room = status.value.roomName
  return [room && room !== '未分配机房' ? room : '', seat].filter(Boolean).join(' · ') || '当前电脑'
})
</script>

<style scoped>
.student-seat-helper {
  display: inline-flex;
  align-items: center;
  margin: 0 4px;
}

.seat-status-pill {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 4px 10px;
  border-radius: 999px;
  background: #f0f7ff;
  border: 1px solid #bfdbfe;
  color: #1e3a8a;
  font-size: 12px;
  font-weight: 500;
  cursor: default;
  transition: all 0.25s ease;
}

.seat-status-pill.is-online {
  background: #ecfdf5;
  border-color: #a7f3d0;
  color: #065f46;
}

.seat-status-pill.is-offline {
  background: #fffbeb;
  border-color: #fde68a;
  color: #92400e;
}

.presence-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 6px rgba(16, 185, 129, 0.6);
  flex-shrink: 0;
}

.seat-status-pill.is-offline .presence-dot {
  background: #f59e0b;
  box-shadow: 0 0 6px rgba(245, 158, 11, 0.6);
}

.seat-text {
  font-weight: 600;
  letter-spacing: 0.2px;
}

.ip-text {
  padding: 1px 6px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.05);
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 11px;
}

.offline-tag {
  font-size: 10px;
  height: 18px;
  padding: 0 4px;
  line-height: 18px;
}

.seat-unbound-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 2px 8px;
  border-radius: 6px;
  background: #f4f4f5;
  border: 1px dashed #d4d4d8;
  font-size: 12px;
}

.unbound-hint {
  color: #71717a;
}

.download-btn {
  padding: 0;
  font-size: 12px;
}
</style>
