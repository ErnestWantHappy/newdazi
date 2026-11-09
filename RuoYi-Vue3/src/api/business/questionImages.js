import request from '@/utils/request'

export function uploadQuestionImage(file, onProgress) {
  const data = new FormData()
  data.append('file', file)
  return request({ url: '/business/question-images', method: 'post', data,
    headers: { 'Content-Type':'multipart/form-data', repeatSubmit: false }, timeout: 60000, silentError: true,
    onUploadProgress: event => onProgress?.(event.total ? Math.round(event.loaded * 100 / event.total) : null) })
}

export function readQuestionImage(imageId, context = {}, signal) {
  return request({ url: `/business/question-images/${encodeURIComponent(imageId)}/content`, method: 'get',
    params: context, responseType: 'blob', signal, silentError: true })
}

export function uploadQuestionWorkbook(file, requestId, onProgress, allowDuplicate = false) {
  const data = new FormData()
  data.append('file', file); data.append('requestId', requestId); data.append('allowDuplicate', String(allowDuplicate))
  return request({ url: '/business/question/import-batches', method: 'post', data,
    headers: { 'Content-Type':'multipart/form-data', repeatSubmit: false }, timeout: 180000, silentError: true,
    onUploadProgress: event => onProgress?.(event.total ? Math.round(event.loaded * 100 / event.total) : null) })
}
export const listQuestionImports = () => request({ url: '/business/question/import-batches', silentError: true })
export const getQuestionSaveReceipt = id => request({ url: `/business/question/save-receipts/${encodeURIComponent(id)}`, silentError:true })
export const getQuestionImport = id => request({ url: `/business/question/import-batches/${id}`, silentError: true })
export const patchQuestionImport = (id, itemId, data) => request({ url: `/business/question/import-batches/${id}/items/${itemId}`, method: 'patch', data, silentError: true })
export const reviewQuestionImport = (id, expectedRevision) => request({ url: `/business/question/import-batches/${id}/review`, method: 'post', data: { expectedRevision }, silentError: true })
export const confirmQuestionImport = (id, expectedRevision) => request({ url: `/business/question/import-batches/${id}/confirm`, method: 'post', data: { expectedRevision }, headers: { repeatSubmit: false }, timeout: 180000, silentError: true })
