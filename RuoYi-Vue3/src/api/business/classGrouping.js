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
