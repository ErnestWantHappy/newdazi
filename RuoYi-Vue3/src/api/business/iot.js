import request from '@/utils/request'

export function listIotExperiments(lessonId) {
  return request({ url: '/business/iot/experiments', method: 'get', params: { lessonId } })
}

export function createIotExperiment(data) {
  return request({ url: '/business/iot/experiments', method: 'post', data })
}

export function listIotLessonClasses(lessonId) {
  return request({ url: '/business/iot/lesson-classes', method: 'get', params: { lessonId } })
}

export function getIotClassConfig(experimentId, entryYear, classCode) {
  return request({ url: '/business/iot/class-config', method: 'get', params: { experimentId, entryYear, classCode } })
}

export function generateIotClassGrouping(data) {
  return request({ url: '/business/iot/generate-grouping', method: 'post', data })
}

export function rotateIotClassPasscode(data) {
  return request({ url: '/business/iot/rotate-passcode', method: 'post', data })
}

export function getIotClassCard(experimentId, entryYear, classCode) {
  return request({ url: '/business/iot/class-card', method: 'get', params: { experimentId, entryYear, classCode } })
}

export function syncIotClassBroker(configId) {
  return request({ url: `/business/iot/class-config/${configId}/sync-broker`, method: 'post' })
}

export function listIotGroups(experimentId, entryYear, classCode) {
  return request({ url: `/business/iot/experiments/${experimentId}/groups`, method: 'get', params: { entryYear, classCode } })
}

export function createIotGroup(data) {
  return request({ url: '/business/iot/groups', method: 'post', data })
}

export function createIotDevice(data) {
  return request({ url: '/business/iot/devices', method: 'post', data })
}

export function listIotDevices(groupId) {
  return request({ url: `/business/iot/groups/${groupId}/devices`, method: 'get' })
}

export function getIotDashboard(experimentId, params = {}) {
  const query = typeof params === 'object' ? params : { limit: params }
  return request({ url: `/business/iot/experiments/${experimentId}/dashboard`, method: 'get', params: query })
}

export function listIotMessages(experimentId, params = {}) {
  return request({ url: `/business/iot/experiments/${experimentId}/messages`, method: 'get', params })
}

export function getIotStudentOverview(lessonId) {
  return request({ url: '/business/iot/student/overview', method: 'get', params: { lessonId } })
}

export function listIotStudentMessages(params) {
  return request({ url: '/business/iot/student/messages', method: 'get', params })
}
