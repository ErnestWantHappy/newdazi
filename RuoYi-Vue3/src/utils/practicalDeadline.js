const STATUS_META = {
  NO_PRACTICAL: { label: '本课程无操作题', type: 'info' },
  NOT_TRIGGERED: { label: '尚未开始批改计时', type: 'info' },
  GRADING: { label: '批改中', type: 'primary' },
  DUE_SOON: { label: '即将到期', type: 'warning' },
  COMPLETED: { label: '已完成', type: 'success' },
  OVERDUE: { label: '已逾期', type: 'danger' },
  REOPENED: { label: '重新开放', type: '' }
}

export function deadlineStatusMeta(code) {
  return STATUS_META[code] || { label: code || '未知状态', type: 'info' }
}

export function formatDeadlineTime(value) {
  if (!value) return '--'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

export function formatDeadlineRemaining(status) {
  if (!status?.currentDeadlineTime) return '--'
  const now = status.serverNow ? new Date(status.serverNow).getTime() : Date.now()
  const due = new Date(status.currentDeadlineTime).getTime()
  const diff = Math.abs(due - now)
  const days = Math.floor(diff / 86400000)
  const hours = Math.floor((diff % 86400000) / 3600000)
  const text = `${days}天${hours}小时`
  return due <= now ? `已逾期${text}` : `剩余${text}`
}
