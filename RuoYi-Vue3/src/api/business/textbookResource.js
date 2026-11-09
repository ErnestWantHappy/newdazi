import request from '@/utils/request'

const base = '/business/textbook/resources'

export function listBookResources() {
  return request({ url: `${base}/book-resources`, silentError: true })
}

export function getBookResources(bookId) {
  return request({ url: `${base}/books/${bookId}/book-resources`, silentError: true })
}

export function getBookResourcePreviewInfo(id) {
  return request({ url: `${base}/book-files/${id}/preview-info`, silentError: true })
}

export function getBookResourcePreviewPage(id, page, thumbnail = false) {
  return request({ url: `${base}/book-files/${id}/pages/${page}`, params: { thumbnail }, responseType: 'blob', timeout: 60000, silentError: true })
}

export function bookResourceDownloadUrl(id) {
  return `${import.meta.env.VITE_APP_BASE_API}${base}/book-files/${encodeURIComponent(id)}/download`
}

export function getGuides() {
  return request({ url: `${base}/guides`, silentError: true })
}

export function getGuide(grade) {
  return request({ url: `${base}/guides/${grade}`, silentError: true })
}

export function getGuidePreviewInfo(grade) {
  return request({ url: `${base}/guides/${grade}/preview-info`, silentError: true })
}

export function getGuidePreviewPage(grade, page, thumbnail = false) {
  return request({ url: `${base}/guides/${grade}/pages/${page}`, params: { thumbnail }, responseType: 'blob', timeout: 60000, silentError: true })
}

export function guideDownloadUrl(grade) {
  return `${import.meta.env.VITE_APP_BASE_API}${base}/guides/${encodeURIComponent(grade)}/download`
}

export function guideUsageUrl(grade) {
  return `${import.meta.env.VITE_APP_BASE_API}${base}/guides/${encodeURIComponent(grade)}/usage`
}

export function getGuideLessonPreviewInfo(id) {
  return request({ url: `${base}/guide-lessons/${id}/preview-info`, silentError: true })
}

export function getGuideLessonPreviewPage(id, page, thumbnail = false) {
  return request({ url: `${base}/guide-lessons/${id}/pages/${page}`, params: { thumbnail }, responseType: 'blob', timeout: 60000, silentError: true })
}

export function guideLessonDownloadUrl(id) {
  return `${import.meta.env.VITE_APP_BASE_API}${base}/guide-lessons/${encodeURIComponent(id)}/download`
}
