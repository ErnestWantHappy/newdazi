import request from '@/utils/request'

export function getCaseSummary() {
  return request({ url: '/business/textbook/cases/summary', silentError: true })
}

export function getSectionCases(bookId, sectionKey) {
  return request({ url: `/business/textbook/cases/book/${bookId}/section/${encodeURIComponent(sectionKey)}`, silentError: true })
}

export function getCaseFile(id, preview = false) {
  return request({ url: `/business/textbook/cases/files/${id}`, params: { preview }, responseType: 'blob', timeout: 180000, silentError: true })
}

export function caseDownloadUrl(id) {
  return `${import.meta.env.VITE_APP_BASE_API}/business/textbook/cases/download/${encodeURIComponent(id)}`
}

export function caseMediaUrl(id) {
  return `${import.meta.env.VITE_APP_BASE_API}/business/textbook/cases/media/${encodeURIComponent(id)}`
}

/** 作者网页在独立标签打开，由后端以 CSP sandbox 隔离，不进平台同源顶层。 */
export function caseHtmlUrl(id) {
  return `${import.meta.env.VITE_APP_BASE_API}/business/textbook/cases/html/${encodeURIComponent(id)}`
}

export function getCasePreviewInfo(id) {
  return request({ url: `/business/textbook/cases/files/${id}/preview-info`, silentError: true })
}

export function getCasePreviewPage(id, page, thumbnail = false) {
  return request({ url: `/business/textbook/cases/files/${id}/pages/${page}`, params: { thumbnail }, responseType: 'blob', timeout: 60000, silentError: true })
}

export function saveCaseFeedback(id, vote, reason = undefined) {
  return request({ url: `/business/textbook/cases/${id}/feedback`, method: 'put', data: { vote, reason }, silentError: true, headers: { repeatSubmit: false } })
}

export function getCaseComments(id) {
  return request({ url: `/business/textbook/cases/${id}/feedback/comments`, silentError: true })
}

export function requestCaseInvitation(id) {
  return request({ url: `/business/textbook/cases/${id}/invitation`, method: 'post', silentError: true })
}
