import request from '@/utils/request'

export function listGuideSheet(query) {
  return request({
    url: '/business/guide-sheet/list',
    method: 'get',
    params: query
  })
}

export function getGuideSheet(sheetId) {
  return request({
    url: '/business/guide-sheet/' + sheetId,
    method: 'get'
  })
}

export function addGuideSheet(data) {
  return request({
    url: '/business/guide-sheet',
    method: 'post',
    data
  })
}

export function updateGuideSheet(data) {
  return request({
    url: '/business/guide-sheet',
    method: 'put',
    data
  })
}

export function delGuideSheet(sheetIds) {
  return request({
    url: '/business/guide-sheet/' + sheetIds,
    method: 'delete'
  })
}

export function publishGuideSheet(sheetId) {
  return request({
    url: '/business/guide-sheet/' + sheetId + '/publish',
    method: 'put'
  })
}

export function closeGuideSheet(sheetId) {
  return request({
    url: '/business/guide-sheet/' + sheetId + '/close',
    method: 'put'
  })
}

export function getProgress(sheetId, classCode) {
  return request({
    url: '/business/guide-sheet/progress',
    method: 'get',
    params: { sheetId, classCode }
  })
}

export function getUploads(sheetId, classCode) {
  return request({
    url: '/business/guide-sheet/uploads',
    method: 'get',
    params: { sheetId, classCode }
  })
}

export function getStudentGuideSheet() {
  return request({
    url: '/business/guide-sheet/student/current',
    method: 'get'
  })
}

export function submitGuideSheet(data) {
  return request({
    url: '/business/guide-sheet/student/submit',
    method: 'post',
    data
  })
}

export function confirmUpload(data) {
  return request({
    url: '/business/guide-sheet/student/upload-confirm',
    method: 'post',
    data
  })
}

export function sendHeartbeat(data) {
  return request({
    url: '/business/guide-sheet/progress/heartbeat',
    method: 'put',
    data
  })
}

export function exportGuideSheet(sheetId, classCode) {
  return request({
    url: '/business/guide-sheet/export',
    method: 'get',
    params: { sheetId, classCode },
    responseType: 'blob'
  })
}

export function getCreatorList() {
  return request({
    url: '/business/guide-sheet/creators',
    method: 'get'
  })
}
