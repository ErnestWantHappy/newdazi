import request from '@/utils/request'

export function getCollaborationHealth() {
  return request({ url: '/business/collaboration/health', method: 'get' })
}

export function getCollaborationLesson(lessonId) {
  return request({ url: `/business/collaboration/lesson/${lessonId}`, method: 'get' })
}

export function saveCollaborationLesson(lessonId, data) {
  return request({ url: `/business/collaboration/lesson/${lessonId}`, method: 'put', data })
}

export function getCurrentCollaborationRooms() {
  return request({ url: '/business/collaboration/student/current', method: 'get' })
}

export function getCollaborationSession(roomId) {
  return request({ url: `/business/collaboration/room/${roomId}/session`, method: 'get' })
}

export function getCollaborationDocument(roomId) {
  return request({
    url: `/business/collaboration/room/${roomId}/document`,
    method: 'get',
    responseType: 'blob'
  })
}

export function saveCollaborationDocument(roomId, file, expectedVersion, fileName) {
  const data = new FormData()
  data.append('file', file, fileName || file.name || '协作文档')
  data.append('expectedVersion', String(expectedVersion))
  return request({
    url: `/business/collaboration/room/${roomId}/save`,
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data', repeatSubmit: false },
    timeout: 120000
  })
}
