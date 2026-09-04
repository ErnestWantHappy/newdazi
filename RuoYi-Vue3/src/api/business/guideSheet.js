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

export function getGuideSheetPreview(sheetId) {
  return request({
    url: '/business/guide-sheet/' + sheetId + '/preview',
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

// 归档导学单模板，保留已绑定课程的历史快照
export function archiveGuideSheet(sheetId) {
  return request({
    url: '/business/guide-sheet/' + sheetId + '/archive',
    method: 'put'
  })
}

// 复制模板时由服务端生成独立版本与归属信息
export function copyGuideSheet(sheetId) {
  return request({
    url: '/business/guide-sheet/' + sheetId + '/copy',
    method: 'post'
  })
}

// 获取课程绑定的不可变快照，用于课程设计器回显和预览
export function getGuideSheetBindingSnapshot(bindingId) {
  return request({
    url: '/business/guide-sheet/bindings/' + bindingId,
    method: 'get'
  })
}

export function getGuideSheetBindingPreview(bindingId) {
  return request({
    url: '/business/guide-sheet/bindings/' + bindingId + '/preview',
    method: 'get'
  })
}

export function getProgress(bindingId, entryYear, classCode) {
  return request({
    url: '/business/guide-sheet/progress',
    method: 'get',
    params: { bindingId, entryYear, classCode }
  })
}

export function getUploads(bindingId, entryYear, classCode) {
  return request({
    url: '/business/guide-sheet/uploads',
    method: 'get',
    params: { bindingId, entryYear, classCode }
  })
}

export function getTeacherGuideSheetAnswer(bindingId, studentId, entryYear, classCode) {
  return request({
    url: `/business/guide-sheet/bindings/${bindingId}/students/${studentId}/answer`,
    method: 'get',
    params: { entryYear, classCode }
  })
}

export function downloadGuideSheetUpload(uploadId, entryYear, classCode) {
  return request({
    url: `/business/guide-sheet/uploads/${uploadId}/content`,
    method: 'get',
    params: { entryYear, classCode },
    responseType: 'blob'
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

export function uploadGuideSheetFile(data) {
  return request({
    url: '/business/guide-sheet/student/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    }
  })
}

export function sendHeartbeat(data) {
  return request({
    url: '/business/guide-sheet/progress/heartbeat',
    method: 'put',
    data
  })
}

export function exportGuideSheet(bindingId, entryYear, classCode) {
  return request({
    url: '/business/guide-sheet/export',
    method: 'get',
    params: { bindingId, entryYear, classCode },
    responseType: 'blob'
  })
}

export function getCreatorList() {
  return request({
    url: '/business/guide-sheet/creators',
    method: 'get'
  })
}

export function getGuideSheetCapabilities() {
  return request({
    url: '/business/guide-sheet/capabilities',
    method: 'get'
  })
}

export function saveGuideSheetManualGrades(bindingId, studentId, data) {
  return request({
    url: `/business/guide-sheet/bindings/${bindingId}/grading/manual/${studentId}`,
    method: 'put',
    data
  })
}

/** 学生获取自己的评分结果 */
export function getStudentGrading(bindingId) {
  return request({
    url: '/business/guide-sheet/student/grading/' + bindingId,
    method: 'get'
  })
}

// 草稿与正式模板分开保存，避免自动保存意外生成可用模板。
export function getGuideSheetDraft(draftKey) {
  return request({
    url: '/business/guide-sheet/draft/' + encodeURIComponent(draftKey),
    method: 'get'
  })
}

export function saveGuideSheetDraft(data) {
  return request({
    url: '/business/guide-sheet/draft',
    method: 'put',
    data,
    headers: { repeatSubmit: false }
  })
}

export function completeGuideSheetDraft(draftKey, revision) {
  return request({
    url: '/business/guide-sheet/draft/' + encodeURIComponent(draftKey) + '/complete',
    method: 'post',
    data: { revision }
  })
}

export function generateGuideSheetContent(data) {
  return request({
    url: '/business/guide-sheet/ai/generate',
    method: 'post',
    data,
    timeout: 30000
  })
}

export function rescoreGuideSheetAnswer(answerId, data) {
  return request({
    url: '/business/guide-sheet/grading/recore/' + answerId,
    method: 'put',
    data
  })
}

// 兼容旧调用名称，后续页面统一使用语义正确的 rescore。
export const recoreGuideSheetAnswer = rescoreGuideSheetAnswer
