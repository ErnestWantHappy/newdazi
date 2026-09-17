import request from '@/utils/request'

export function getClassroomDesktop(params) {
  return request({ url: '/business/class-group/desktop', method: 'get', params })
}

export function getClassroomDesktopOverview(params) {
  return request({ url: '/business/class-group/desktop/overview', method: 'get', params })
}

export function saveClassroomLayout(data) {
  return request({ url: '/business/class-group/desktop/layout', method: 'put', data })
}

export function getClassGroupSchemes(params) {
  return request({ url: '/business/class-group/schemes', method: 'get', params })
}

export function saveClassGroupScheme(data) {
  return request({ url: '/business/class-group/schemes', method: 'post', data })
}

export function generateClassGroupScheme(data) {
  return request({ url: '/business/class-group/schemes/generate', method: 'post', data })
}
export function previewClassGroupScheme(data) {
  return request({ url: '/business/class-group/schemes/preview', method: 'post', data })
}

// 把某班级某方案冻结为课时分组快照（同一课程同一班级幂等，已冻结则返回现有快照）
export function createClassGroupSnapshot(lessonId, data) {
  return request({ url: `/business/class-group/lessons/${lessonId}/snapshots`, method: 'post', data })
}

// 按学号连续自动分组并冻结为课时分组快照（已有快照直接沿用，不覆盖）
export function autoFreezeClassGroupSnapshot(lessonId, data) {
  return request({ url: `/business/class-group/lessons/${lessonId}/snapshots/auto`, method: 'post', data })
}
