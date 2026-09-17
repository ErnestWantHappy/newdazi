export const SCHOOL_TYPES = [
  { value: '1', label: '小学' },
  { value: '2', label: '初中' },
  { value: '3', label: '高中' }
]

export const SEMESTERS = [
  { value: '1', label: '上学期' },
  { value: '2', label: '下学期' }
]

export const LESSON_KINDS = [
  { value: 'N', label: '数字课次' },
  { value: 'S', label: '专题课' },
  { value: 'R', label: '复习课' }
]

export function gradesForSchoolType(schoolType) {
  const ranges = { '1': [1, 6], '2': [7, 9], '3': [10, 12] }
  const range = ranges[String(schoolType)]
  if (!range) return []
  return Array.from({ length: range[1] - range[0] + 1 }, (_, index) => range[0] + index)
}

export function gradeLabel(grade) {
  const labels = ['一', '二', '三', '四', '五', '六', '七', '八', '九', '十', '十一', '十二']
  const number = Number(grade)
  return number >= 1 && number <= 12 ? `${labels[number - 1]}年级` : ''
}

export function optionLabel(options, value, fallback = '') {
  return options.find(item => String(item.value) === String(value))?.label || fallback
}

export function lessonLabel(post) {
  if (post?.lessonKind === 'N') return Number.isInteger(Number(post.lessonNo)) ? `第${post.lessonNo}课` : '数字课次'
  return optionLabel(LESSON_KINDS, post?.lessonKind, '')
}

export function linkStatus(resource, now = new Date()) {
  if (!resource?.expireTime) return { code: 'PERMANENT', text: '永久有效', expired: false }
  const expireTime = new Date(resource.expireTime)
  if (Number.isNaN(expireTime.getTime())) return { code: 'UNKNOWN', text: '有效期未知', expired: false }
  if (expireTime.getTime() <= now.getTime()) return { code: 'EXPIRED', text: '已过期', expired: true }
  return { code: 'ACTIVE', text: `有效至 ${formatDateTime(expireTime)}`, expired: false }
}

export function formatDateTime(value) {
  if (!value) return ''
  const date = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  const pad = number => String(number).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

export function isFutureActivityTime(value, now = new Date()) {
  if (!value) return true
  const normalized = typeof value === 'string' ? value.replace(' ', 'T') : value
  const activityTime = new Date(normalized).getTime()
  const currentTime = now instanceof Date ? now.getTime() : Number(now)
  return Number.isFinite(activityTime) && Number.isFinite(currentTime) && activityTime > currentTime
}

export function formatFileSize(bytes) {
  const size = Number(bytes)
  if (!Number.isFinite(size) || size < 0) return ''
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KiB`
  return `${(size / 1024 / 1024).toFixed(1)} MiB`
}
