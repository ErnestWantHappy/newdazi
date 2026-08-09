import request from '@/utils/request'

const base = '/business/teacher/grading/ai'

export function getAiConfig() { return request({ url: `${base}/config`, method: 'get' }) }
export function saveAiConfig(data) { return request({ url: `${base}/config`, method: 'put', data }) }
export function deleteAiConfig() { return request({ url: `${base}/config`, method: 'delete' }) }
export function testAiConfig() { return request({ url: `${base}/config/test`, method: 'post' }) }
export function createAiJob(data) { return request({ url: `${base}/jobs`, method: 'post', data }) }
export function getAiPreflight(params) { return request({ url: `${base}/preflight`, method: 'get', params }) }
export function getLatestAiJob(params) { return request({ url: `${base}/jobs/latest`, method: 'get', params }) }
export function uploadAiReferenceAnswer(data) {
  return request({ url: `${base}/reference-answer`, method: 'post', data, headers: { 'Content-Type': 'multipart/form-data' } })
}
export function getAiJob(jobId) { return request({ url: `${base}/jobs/${jobId}`, method: 'get' }) }
export function getAiJobEvents(jobId, afterEventId = 0) {
  return request({ url: `${base}/jobs/${jobId}/events`, method: 'get', params: { afterEventId } })
}
export function pauseAiJob(jobId) { return request({ url: `${base}/jobs/${jobId}/pause`, method: 'post' }) }
export function resumeAiJob(jobId) { return request({ url: `${base}/jobs/${jobId}/resume`, method: 'post' }) }
export function cancelAiJob(jobId) { return request({ url: `${base}/jobs/${jobId}/cancel`, method: 'post' }) }
export function retryFailedAiJob(jobId) { return request({ url: `${base}/jobs/${jobId}/retry-failed`, method: 'post' }) }
export function batchApplyAiSuggestions(jobId, applyMode) {
  return request({ url: `${base}/jobs/${jobId}/apply`, method: 'post', data: { applyMode } })
}
