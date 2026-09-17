import request from '@/utils/request'

export function listPlatformUpdates(params) {
  return request({ url: '/business/platform-update/list', method: 'get', params })
}

export function listPlatformUpdateManagement(params) {
  return request({ url: '/business/platform-update/manage/list', method: 'get', params })
}

export function addPlatformUpdate(data) {
  return request({ url: '/business/platform-update', method: 'post', data })
}

export function updatePlatformUpdate(data) {
  return request({ url: '/business/platform-update', method: 'put', data })
}

export function updatePlatformUpdateStatus(updateId, status) {
  return request({ url: `/business/platform-update/${updateId}/status/${status}`, method: 'put' })
}
