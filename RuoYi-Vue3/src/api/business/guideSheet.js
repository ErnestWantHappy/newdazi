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

export function getProgress(sheetId, entryYear, classCode) {
  return request({
    url: '/business/guide-sheet/progress',
    method: 'get',
    params: { sheetId, entryYear, classCode }
  })
}

export function getUploads(sheetId, entryYear, classCode) {
  return request({
    url: '/business/guide-sheet/uploads',
    method: 'get',
    params: { sheetId, entryYear, classCode }
  })
}

export function getStudentGuideSheet() {
  return request({
    url: '/business/guide-sheet/student/current',
    method: 'get'
  })
}

export function submitGuideSheet(data, signal) {
  return request({
    url: '/business/guide-sheet/student/submit',
    method: 'post',
    data,
    signal,
    headers: { repeatSubmit: false }
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

export function exportGuideSheet(sheetId, entryYear, classCode) {
  return request({
    url: '/business/guide-sheet/export',
    method: 'get',
    params: { sheetId, entryYear, classCode },
    responseType: 'blob'
  })
}

export function getCreatorList() {
  return request({
    url: '/business/guide-sheet/creators',
    method: 'get'
  })
}

export function getGuideSheetLessons() {
  return request({
    url: '/business/guide-sheet/lessons',
    method: 'get'
  })
}

export function getGuideSheetClassOptions() {
  return request({
    url: '/business/guide-sheet/class-options',
    method: 'get'
  })
}

export function getGuideSheetCapabilities() {
  return request({
    url: '/business/guide-sheet/capabilities',
    method: 'get'
  })
}

export function saveGuideSheetManualGrades(sheetId, studentId, data) {
  return request({
    url: `/business/guide-sheet/${sheetId}/grading/manual/${studentId}`,
    method: 'put',
    data
  })
}

/** 学生获取自己的评分结果 */
export function getStudentGrading(sheetId) {
  return request({
    url: '/business/guide-sheet/student/grading/' + sheetId,
    method: 'get'
  })
}
