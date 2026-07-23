import request from '@/utils/request'

const BASE_URL = '/business/research-activity'

export function listResearchTopics(params, signal) {
  return request({ url: `${BASE_URL}/topics`, method: 'get', params, signal })
}

export function searchResearchTopics(params, signal) {
  return request({ url: `${BASE_URL}/search/topics`, method: 'get', params, signal })
}

export function getResearchTopic(topicId) {
  return request({ url: `${BASE_URL}/topics/${topicId}`, method: 'get' })
}

export function createResearchTopic(data) {
  return request({ url: `${BASE_URL}/topics`, method: 'post', data })
}

export function updateResearchTopic(topicId, data) {
  return request({ url: `${BASE_URL}/topics/${topicId}`, method: 'put', data })
}

export function deleteResearchTopic(topicId) {
  return request({ url: `${BASE_URL}/topics/${topicId}`, method: 'delete' })
}

export function restoreResearchTopic(topicId) {
  return request({ url: `${BASE_URL}/topics/${topicId}/restore`, method: 'put' })
}

export function listHiddenResearchTopics(params) {
  return request({ url: `${BASE_URL}/hidden/topics`, method: 'get', params })
}

export function pinResearchTopic(topicId, pinned) {
  return request({ url: `${BASE_URL}/topics/${topicId}/pin`, method: 'put', params: { pinned } })
}

export function listResearchPosts(topicId, params) {
  return request({ url: `${BASE_URL}/topics/${topicId}/posts`, method: 'get', params })
}

export function createResearchPost(topicId, data) {
  return request({ url: `${BASE_URL}/topics/${topicId}/posts`, method: 'post', data })
}

export function updateResearchPost(postId, data) {
  return request({ url: `${BASE_URL}/posts/${postId}`, method: 'put', data })
}

export function saveResearchResourcePost(topicId, postId, payload, file) {
  const form = new FormData()
  form.append('payload', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
  if (file) form.append('file', file)
  return request({
    url: postId ? `${BASE_URL}/resource-posts/${postId}` : `${BASE_URL}/topics/${topicId}/resource-posts`,
    method: postId ? 'put' : 'post',
    data: form,
    timeout: 120000,
    headers: { 'Content-Type': 'multipart/form-data', repeatSubmit: false }
  })
}

export function deleteResearchPost(postId) {
  return request({ url: `${BASE_URL}/posts/${postId}`, method: 'delete' })
}

export function restoreResearchPost(postId) {
  return request({ url: `${BASE_URL}/posts/${postId}/restore`, method: 'put' })
}

export function listHiddenResearchPosts(params) {
  return request({ url: `${BASE_URL}/hidden/posts`, method: 'get', params })
}

export function pinResearchPost(postId, pinned) {
  return request({ url: `${BASE_URL}/posts/${postId}/pin`, method: 'put', params: { pinned } })
}

export function searchResearchResources(params, signal) {
  return request({ url: `${BASE_URL}/search/resources`, method: 'get', params, signal })
}

export function listResearchTeacherTargets(params) {
  return request({ url: `${BASE_URL}/notification-targets/teachers`, method: 'get', params })
}

export function notifyResearchTopic(topicId, data) {
  return request({ url: `${BASE_URL}/topics/${topicId}/notify`, method: 'post', data })
}

export function getResearchNotificationSummary(limit = 5) {
  return request({ url: `${BASE_URL}/notifications/summary`, method: 'get', params: { limit } })
}

export function listResearchNotifications(params) {
  return request({ url: `${BASE_URL}/notifications`, method: 'get', params })
}

export function readResearchNotification(recipientId) {
  return request({ url: `${BASE_URL}/notifications/${recipientId}/read`, method: 'put' })
}

export function readAllResearchNotifications() {
  return request({ url: `${BASE_URL}/notifications/read-all`, method: 'put' })
}

export function downloadResearchResource(resourceId) {
  return request({ url: `${BASE_URL}/resources/${resourceId}/download`, method: 'get', responseType: 'blob' })
}

export function accessResearchLink(resourceId) {
  return request({ url: `${BASE_URL}/resources/${resourceId}/access`, method: 'post' })
}
